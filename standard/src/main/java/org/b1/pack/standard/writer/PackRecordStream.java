/*
 * Copyright 2011 b1.org
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

import com.google.common.io.CountingOutputStream;
import com.google.common.primitives.Ints;
import org.b1.pack.api.common.PackException;
import org.b1.pack.api.writer.PwProvider;
import org.b1.pack.api.writer.PwVolume;
import org.b1.pack.standard.common.Numbers;
import org.b1.pack.standard.common.RecordPointer;
import org.b1.pack.standard.common.VolumeNameExpert;
import org.b1.pack.standard.common.Volumes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;

import static com.google.common.base.Preconditions.checkState;
import static org.b1.pack.standard.common.Constants.PLAIN_BLOCK;
import static org.b1.pack.standard.common.Constants.MAX_CHUNK_SIZE;
import static org.b1.pack.standard.common.Numbers.writeLong;
import static org.b1.pack.standard.common.Volumes.createVolumeHead;
import static org.b1.pack.standard.common.Volumes.createVolumeTail;

public class PackRecordStream extends OutputStream {

    private static final int PLAIN_BLOCK_OVERHEAD = Numbers.serializeLong((long) MAX_CHUNK_SIZE).length + 6;

    private final ByteArrayOutputStream chunk = new ByteArrayOutputStream(MAX_CHUNK_SIZE);
    private final String archiveId = Volumes.createArchiveId();
    private final PwProvider provider;
    private final VolumeNameExpert nameExpert;
    private RecordPointer catalogPointer;
    private long volumeNumber;
    private PwVolume volume;
    private CountingOutputStream volumeStream;
    private CheckedOutputStream chunkStream;
    private long volumeLimit;
    private int chunkLimit;
    private long volumeSize;

    public PackRecordStream(PwProvider provider) {
        this.provider = provider;
        nameExpert = new VolumeNameExpert(provider.getPackName(), provider.getExpectedVolumeCount());
    }

    public RecordPointer getCurrentPointer() throws IOException {
        ensureFreeSpace();
        return new RecordPointer(volumeNumber, volumeStream.getCount(), chunk.size());
    }

    public void startCatalog() throws IOException {
        checkState(catalogPointer == null);
        //todo ensure more free space
        catalogPointer = getCurrentPointer();
        setVolumeLimit();
        setChunkLimit();
    }

    @Override
    public void write(int b) throws IOException {
        ensureFreeSpace();
        chunkStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            ensureFreeSpace();
            int size = Math.min(len, chunkLimit - chunk.size());
            chunkStream.write(b, off, size);
            off += size;
            len -= size;
        }
    }

    @Override
    public void close() throws IOException {
        endChunk();

        endVolume(true);
    }

    public void completeVolume() throws IOException {
        if (volume != null) {
            volume.complete();
            volume = null;
        }
    }

    private void ensureFreeSpace() throws IOException {
        if (chunk.size() < chunkLimit) {
            return;
        }
        endChunk();
        startChunk();
        setChunkLimit();
        if (chunkLimit > 0) {
            return;
        }
        endVolume(false);
        completeVolume();
        startVolume();
        setVolumeLimit();
        setChunkLimit();
        if (chunkLimit <= 0) {
            throw new PackException("Volume size too small");
        }
    }

    private void startVolume() throws IOException {
        checkState(volume == null);
        checkState(volumeStream == null);
        volume = provider.getVolume(nameExpert.getVolumeName(++volumeNumber));
        volumeSize = volume.getSize();
        volumeStream = new CountingOutputStream(volume.getOutputStream());
        volumeStream.write(createVolumeHead(archiveId, volumeNumber, null));
    }

    private void setVolumeLimit() {
        volumeLimit = volumeSize == 0 ? Long.MAX_VALUE : volumeSize - createVolumeTail(false, catalogPointer, 0).length - 1;
    }

    private void endVolume(boolean lastVolume) throws IOException {
        if (volumeStream == null) return;
        try {
            writeLong(null, volumeStream);
            volumeStream.write(Volumes.createVolumeTail(lastVolume, catalogPointer, lastVolume ? 0 : volumeSize - volumeStream.getCount()));
        } finally {
            volumeStream.close();
            volumeStream = null;
        }
    }

    private void startChunk() {
        checkState(chunk.size() == 0);
        checkState(chunkStream == null);
        chunkStream = new CheckedOutputStream(chunk, new Adler32());
    }

    private void setChunkLimit() {
        chunkLimit = volumeStream == null ? 0 : (int) Math.min(MAX_CHUNK_SIZE, volumeLimit - volumeStream.getCount() - PLAIN_BLOCK_OVERHEAD);
    }

    private void endChunk() throws IOException {
        if (chunk.size() > 0) {
            writeLong(PLAIN_BLOCK, volumeStream);
            writeLong((long) chunk.size(), volumeStream);
            chunk.writeTo(volumeStream);
            writeLong(0L, volumeStream);
            volumeStream.write(Ints.toByteArray((int) chunkStream.getChecksum().getValue()));
            chunk.reset();
        }
        chunkStream = null;
    }
}
