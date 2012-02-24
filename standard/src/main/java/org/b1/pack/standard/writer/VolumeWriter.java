/*
 * Copyright 2012 b1.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.b1.pack.standard.writer;

import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import org.b1.pack.api.builder.Writable;
import org.b1.pack.api.writer.WriterVolume;
import org.b1.pack.standard.common.PbBlock;
import org.b1.pack.standard.common.PbInt;
import org.b1.pack.standard.common.RecordPointer;
import org.b1.pack.standard.common.Volumes;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.SortedMap;

class VolumeWriter {

    private final SortedMap<Long, PbBlock> suspendedBlocks = Maps.newTreeMap();
    private final String archiveId;
    private final long volumeNumber;
    private final long maxVolumeSize;
    private final WriterVolume volume;
    private OutputStream outputStream;
    private Long objectCount;
    private RecordPointer catalogPointer;
    private long sizeLimit;
    private long spaceLimit;
    private long streamEnd;
    private boolean streamAtEnd;

    public VolumeWriter(String archiveId, long volumeNumber, long maxVolumeSize, WriterVolume volume) {
        this.archiveId = archiveId;
        this.volumeNumber = volumeNumber;
        this.maxVolumeSize = maxVolumeSize;
        this.volume = volume;
    }

    public long getVolumeNumber() {
        return volumeNumber;
    }

    public long getStreamEnd() {
        return streamEnd;
    }

    public long getFreeSpace() {
        return spaceLimit - streamEnd;
    }

    public boolean isSuspended() {
        return !suspendedBlocks.isEmpty();
    }

    public void setObjectCount(Long objectCount) {
        this.objectCount = objectCount;
    }

    public void setCatalogPointer(RecordPointer catalogPointer) throws IOException {
        this.catalogPointer = catalogPointer;
        setLimits();
    }

    public void suspendBlock(PbBlock block) throws IOException {
        initialize();
        suspendedBlocks.put(streamEnd, block);
        streamAtEnd = false;
        streamEnd += block.getSize();
    }

    public void writeBlock(PbBlock block) throws IOException {
        initialize();
        seekToEnd();
        writeToStream(block);
        streamEnd += block.getSize();
    }

    public void flush() throws IOException {
        initialize();
        if (suspendedBlocks.isEmpty()) return;
        streamAtEnd = false;
        for (Map.Entry<Long, PbBlock> entry : suspendedBlocks.entrySet()) {
            volume.seek(outputStream, entry.getKey());
            writeToStream(entry.getValue());
        }
        suspendedBlocks.clear();
    }

    public void close(boolean lastVolume) throws IOException {
        flush();
        seekToEnd();
        writeToStream(PbInt.NULL);
        outputStream.write(Volumes.createVolumeTail(lastVolume, catalogPointer, lastVolume ? 0 : sizeLimit - streamEnd));
        outputStream.close();
        volume.afterSave();
    }

    public void cleanup() {
        Closeables.closeQuietly(outputStream);
    }

    private void setLimits() throws IOException {
        sizeLimit = Math.min(maxVolumeSize, volume.getMaxSize());
        spaceLimit = sizeLimit - Volumes.createVolumeTail(false, catalogPointer, 0).length - PbInt.NULL.getSize();
    }

    private void initialize() throws IOException {
        if (outputStream != null) return;
        outputStream = volume.getOutputStream();
        byte[] volumeHead = Volumes.createVolumeHead(archiveId, volumeNumber, objectCount);
        outputStream.write(volumeHead);
        streamEnd = volumeHead.length;
        streamAtEnd = true;
        setLimits();
    }

    private void seekToEnd() throws IOException {
        if (streamAtEnd) return;
        volume.seek(outputStream, streamEnd);
        streamAtEnd = true;
    }

    private void writeToStream(Writable writable) throws IOException {
        writable.writeTo(outputStream, 0, writable.getSize());
    }
}
