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

import com.google.common.base.Objects;
import org.b1.pack.api.builder.Writable;
import org.b1.pack.api.common.PackEntry;
import org.b1.pack.api.writer.WriterProvider;
import org.b1.pack.standard.common.Constants;
import org.b1.pack.standard.common.Numbers;
import org.b1.pack.standard.common.PbRecordPointer;
import org.b1.pack.standard.common.RecordPointer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;

class PackOutputStream extends OutputStream {

    private final WriterProvider provider;
    private final LzmaMethod lzmaMethod;
    private final BlockWriter blockWriter;
    private final ExecutorService executorService;
    private final int volumeNumberSize;
    private final int blockOffsetSize;
    private final int recordOffsetSize;
    private LzmaWriter lzmaWriter;

    public PackOutputStream(WriterProvider provider) {
        this.provider = provider;
        lzmaMethod = LzmaMethod.valueOf(provider.getCompressionMethod());
        blockWriter = new BlockWriter(provider, lzmaMethod == null ? null : lzmaMethod.getName());
        executorService = lzmaMethod == null ? null : provider.getExecutorService();
        volumeNumberSize = Numbers.getSerializedSize(provider.getMaxVolumeSize() == Long.MAX_VALUE ? 1 : provider.getMaxVolumeCount());
        blockOffsetSize = Numbers.getSerializedSize(provider.getMaxVolumeSize());
        recordOffsetSize = Numbers.getSerializedSize(lzmaMethod == null ? Constants.MAX_CHUNK_SIZE : lzmaMethod.getSolidBlockSize());
    }

    public boolean isSeekable() {
        return provider.isSeekable();
    }

    public PbRecordPointer createEmptyPointer() {
        return new PbRecordPointer(volumeNumberSize, blockOffsetSize, recordOffsetSize);
    }

    public void setObjectCount(Long objectCount) {
        blockWriter.setObjectCount(objectCount);
    }

    public RecordPointer startCatalog(boolean compressed) throws IOException {
        disableCompression();
        if (compressed) enableCompression();
        return blockWriter.saveCatalogPointer();
    }

    public void switchCompression(PackEntry entry, Long size) throws IOException {
        if (lzmaMethod != null) {
            setCompressible(lzmaMethod.isCompressible(entry, size));
        }
    }

    public void setCompressible(boolean compressible) throws IOException {
        if (compressible && lzmaMethod != null) {
            if (lzmaWriter != null && lzmaWriter.getCount() >= lzmaMethod.getSolidBlockSize()) {
                disableCompression();
            }
            enableCompression();
        } else {
            disableCompression();
        }
    }

    public RecordPointer getCurrentPointer() throws IOException {
        return getChunkWriter().getCurrentPointer();
    }

    @Override
    public void write(int b) throws IOException {
        getChunkWriter().write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        getChunkWriter().write(b, off, len);
    }

    public void write(Writable value) throws IOException {
        getChunkWriter().write(value);
    }

    public void save() throws IOException {
        disableCompression();
        blockWriter.save();
    }

    @Override
    public void close() throws IOException {
        disableCompression();
        blockWriter.close();
        shutdownExecutor();
    }

    public void cleanup() {
        if (lzmaWriter != null) {
            lzmaWriter.cleanup();
        }
        blockWriter.cleanup();
        shutdownExecutor();
    }

    private void shutdownExecutor() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private ChunkWriter getChunkWriter() {
        return Objects.firstNonNull(lzmaWriter, blockWriter);
    }

    private void enableCompression() throws IOException {
        if (lzmaWriter != null || lzmaMethod == null) return;
        blockWriter.setCompressed(true);
        lzmaWriter = new LzmaWriter(lzmaMethod, blockWriter, executorService);
    }

    private void disableCompression() throws IOException {
        if (lzmaWriter == null) return;
        lzmaWriter.close();
        blockWriter.setCompressed(false);
        lzmaWriter = null;
    }
}
