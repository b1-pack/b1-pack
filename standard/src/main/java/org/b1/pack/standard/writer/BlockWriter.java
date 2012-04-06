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

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

class BlockWriter extends ChunkWriter {

    private final String archiveId = Volumes.createArchiveId();
    private final List<VolumeWriter> suspendedWriters = Lists.newArrayList();
    private final MemoryOutputStream readyContent = new MemoryOutputStream();
    private final WriterProvider provider;
    private final String method;
    private CompositeWritable suspendedContent = new CompositeWritable();
    private VolumeWriter volumeWriter;
    private RecordPointer catalogPointer;
    private Long objectCount;
    private long maxContentSize;
    private boolean compressed;
    private boolean firstBlockInChunk;

    public BlockWriter(WriterProvider provider, String method) {
        this.provider = provider;
        this.method = method;
    }

    @Override
    public RecordPointer getCurrentPointer() throws IOException {
        ensureFreeSpace();
        return new RecordPointer(volumeWriter.getVolumeNumber(), volumeWriter.getStreamEnd(), getContentSize());
    }

    public void setObjectCount(Long objectCount) {
        this.objectCount = objectCount;
    }

    public void setCompressed(boolean compressed) throws IOException {
        flushContent();
        this.compressed = compressed;
        firstBlockInChunk = true;
    }

    public RecordPointer saveCatalogPointer() throws IOException {
        flushContent();
        if (catalogPointer != null) {
            return getCurrentPointer();
        }
        catalogPointer = getCurrentPointer();
        if (volumeWriter != null) {
            volumeWriter.setCatalogPointer(catalogPointer);
            maxContentSize = getMaxContentSize();
            if (maxContentSize <= 0) {
                volumeWriter.setCatalogPointer(null);
                catalogPointer = getCurrentPointer();
                volumeWriter.setCatalogPointer(catalogPointer);
            }
        }
        return catalogPointer;
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

    @Override
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

    public void save() throws IOException {
        flushContent();
        for (VolumeWriter writer : suspendedWriters) {
            writer.close(false);
        }
        suspendedWriters.clear();
        volumeWriter.flush();
    }

    @Override
    public void close() throws IOException {
        save();
        volumeWriter.close(true);
    }

    public void cleanup() {
        for (VolumeWriter writer : suspendedWriters) {
            writer.cleanup();
        }
        if (volumeWriter != null) {
            volumeWriter.cleanup();
        }
    }

    private long getContentSize() {
        return suspendedContent.getSize() + readyContent.size();
    }

    private void suspendReadyContent() {
        if (readyContent.size() > 0) {
            suspendedContent.add(new ByteArrayWritable(readyContent.toByteArray()));
            readyContent.reset();
        }
    }

    private long ensureFreeSpace() throws IOException {
        long result = maxContentSize - getContentSize();
        if (result > 0) {
            return result;
        }
        if (volumeWriter == null) {
            volumeWriter = createVolumeWriter(1);
        } else {
            flushContent();
        }
        long contentSize = getMaxContentSize();
        if (contentSize <= 0) {
            completeVolumeWriter();
            volumeWriter = createVolumeWriter(volumeWriter.getVolumeNumber() + 1);
            contentSize = getMaxContentSize();
            Preconditions.checkArgument(contentSize > 0, "Volume size too small");
        }
        return maxContentSize = contentSize;
    }

    private VolumeWriter createVolumeWriter(long volumeNumber) throws IOException {
        return new VolumeWriter(archiveId, volumeNumber, objectCount, method,
                provider.getMaxVolumeSize(), provider.getVolume(volumeNumber), catalogPointer);
    }

    private void completeVolumeWriter() throws IOException {
        if (volumeWriter.isSuspended()) {
            suspendedWriters.add(volumeWriter);
        } else {
            volumeWriter.close(false);
        }
    }

    private long getMaxContentSize() {
        long space = volumeWriter.getFreeSpace();
        long size = Math.min(space, Constants.MAX_CHUNK_SIZE);
        long contentSize = size - Math.max(0, getBlockSize(size) - space);
        return contentSize >= Constants.MIN_CHUNK_SIZE ? contentSize : 0;
    }

    private long getBlockSize(final long chunkSize) {
        return createBlock(new Writable() {
            @Override
            public long getSize() {
                return chunkSize;
            }

            @Override
            public void writeTo(OutputStream stream, long start, long end) throws IOException {
                throw new UnsupportedOperationException();
            }
        }).getSize();
    }

    private void flushContent() throws IOException {
        if (suspendedContent.getSize() > 0) {
            suspendReadyContent();
            volumeWriter.suspendBlock(createBlock(suspendedContent));
            suspendedContent = new CompositeWritable();
            afterFlush();
        } else if (readyContent.size() > 0) {
            volumeWriter.writeBlock(createBlock(new ByteArrayWritable(readyContent.getBuf(), readyContent.size())));
            readyContent.reset();
            afterFlush();
        }
    }

    private void afterFlush() {
        maxContentSize = 0;
        firstBlockInChunk = false;
    }

    private PbBlock createBlock(Writable content) {
        PbPlainBlock block = new PbPlainBlock(content);
        return compressed ? PbBlock.wrapLzmaBlock(firstBlockInChunk, block) : PbBlock.wrapPlainBlock(block);
    }
}
