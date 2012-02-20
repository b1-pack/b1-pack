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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import org.b1.pack.api.builder.Writable;
import org.b1.pack.api.writer.WriterProvider;
import org.b1.pack.standard.common.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

class BlockWriter extends OutputStream {

    private final String archiveId = Volumes.createArchiveId();
    private final List<VolumeWriter> suspendedWriters = Lists.newArrayList();
    private final ByteArrayOutputStream readyContent = new ByteArrayOutputStream();
    private final WriterProvider provider;
    private CompositeWritable suspendedContent = new CompositeWritable();
    private VolumeWriter volumeWriter;
    private RecordPointer catalogPointer;
    private Long objectCount;
    private long maxContentSize;

    public BlockWriter(WriterProvider provider) {
        this.provider = provider;
    }

    public RecordPointer getCurrentPointer() throws IOException {
        ensureFreeSpace();
        return new RecordPointer(volumeWriter.getVolumeNumber(), volumeWriter.getStreamEnd(), getContentSize());
    }

    @Override
    public void write(int b) throws IOException {
        ensureFreeSpace();
        readyContent.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            int size = Ints.checkedCast(Math.min(len, ensureFreeSpace()));
            readyContent.write(b, off, size);
            off += size;
            len -= size;
        }
    }

    public void write(Writable value) throws IOException {
        long off = 0;
        long len = value.getSize();
        while (len > 0) {
            long size = Math.min(len, ensureFreeSpace());
            suspendReadyContent();
            suspendedContent.add(new PartialWritable(value, off, off + size));
            off += size;
            len -= size;
        }
    }

    private void suspendReadyContent() {
        if (readyContent.size() > 0) {
            suspendedContent.add(new ByteArrayWritable(readyContent.toByteArray()));
            readyContent.reset();
        }
    }

    @Override
    public void flush() throws IOException {

    }

    @Override
    public void close() throws IOException {

    }

    public void complete() throws IOException {

    }

    public void setObjectCount(Long objectCount) {
        this.objectCount = objectCount;
        if (volumeWriter != null) volumeWriter.setObjectCount(objectCount);
    }

    public void saveCatalogPoiner() throws IOException {
        if (catalogPointer != null) {
            return;
        }
        catalogPointer = getCurrentPointer();//todo reserve more space
        if (volumeWriter != null) {
            volumeWriter.setCatalogPointer(catalogPointer);
        }
    }

    private long ensureFreeSpace() throws IOException {
        long result = maxContentSize - getContentSize();
        if (result > 0) {
            return result;
        }
        if (volumeWriter == null) {
            createVolumeWriter(1);
        } else {
            flushBlock();
        }
        long contentSize = getMaxContentSize();
        if (contentSize <= 0) {
            completeVolumeWriter();
            createVolumeWriter(volumeWriter.getVolumeNumber() + 1);
            contentSize = getMaxContentSize();
            Preconditions.checkArgument(contentSize > 0, "Volume size too small");
        }
        return maxContentSize = contentSize;
    }

    private long getContentSize() {
        return suspendedContent.getSize() + readyContent.size();
    }

    private void createVolumeWriter(long volumeNumber) throws IOException {
        volumeWriter = new VolumeWriter(archiveId, volumeNumber, provider.getMaxVolumeSize(), provider.getVolume(volumeNumber));
        volumeWriter.setCatalogPointer(catalogPointer);
        volumeWriter.setObjectCount(objectCount);
    }

    private void completeVolumeWriter() throws IOException {
        if (volumeWriter.isSuspended()) {
            suspendedWriters.add(volumeWriter);
        } else {
            volumeWriter.complete(false);
        }
    }

    private long getMaxContentSize() {
        long space = volumeWriter.getFreeSpace();
        long size = Math.min(space, Constants.MAX_CHUNK_SIZE);
        return size - Math.max(0, getBlockSize(size) - space);
    }


    private long getBlockSize(long chunkSize) {
        return Numbers.getSerializedSize(chunkSize) + chunkSize + PbInt.NULL.getSize() + Ints.BYTES;
    }

    private void flushBlock() throws IOException {
        if (suspendedContent.getSize() > 0) {
            suspendReadyContent();
            volumeWriter.suspendBlock(createBlock(suspendedContent));
            suspendedContent = new CompositeWritable();
        } else if (readyContent.size() > 0) {
            //todo avoid array coping
            volumeWriter.writeBlock(createBlock(new ByteArrayWritable(readyContent.toByteArray())));
            readyContent.reset();
        }
    }

    private PbBlock createBlock(Writable content) {
        return PbBlock.wrapPlainBlock(new PbPlainBlock(content));
    }
}
