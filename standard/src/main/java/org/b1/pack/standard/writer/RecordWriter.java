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
import org.b1.pack.api.compression.LzmaCompressionMethod;
import org.b1.pack.api.writer.WriterProvider;
import org.b1.pack.standard.common.Numbers;
import org.b1.pack.standard.common.PbRecordPointer;
import org.b1.pack.standard.common.RecordPointer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;

class RecordWriter extends OutputStream {

    private final WriterProvider provider;
    private final BlockWriter blockWriter;
    private final int volumeNumberSize;
    private final int blockOffsetSize;
    private final LzmaCompressionMethod compressionMethod;
    private final ExecutorService executorService;
    private LzmaWriter lzmaWriter;

    public RecordWriter(WriterProvider provider) {
        this.provider = provider;
        blockWriter = new BlockWriter(provider);
        volumeNumberSize = Numbers.getSerializedSize(provider.getMaxVolumeCount());
        blockOffsetSize = Numbers.getSerializedSize(provider.getMaxVolumeSize());
        compressionMethod = (LzmaCompressionMethod) provider.getCompressionMethod();
        executorService = compressionMethod == null ? null : provider.getExecutorService();
    }

    public boolean isSeekable() {
        return provider.isSeekable();
    }

    public PbRecordPointer createEmptyPointer() {
        //todo optimize pointer max size
        return new PbRecordPointer(volumeNumberSize, blockOffsetSize, Numbers.MAX_LONG_SIZE);
    }

    public void setObjectCount(Long objectCount) {
        blockWriter.setObjectCount(objectCount);
    }

    public RecordPointer saveCatalogPointer() throws IOException {
        return blockWriter.saveCatalogPointer();
    }

    public void setCompressible(boolean compressible) throws IOException {
        if (compressible && compressionMethod != null) {
            if (lzmaWriter != null && lzmaWriter.getCount() >= compressionMethod.getSolidBlockSize()) {
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

    @Override
    public void flush() throws IOException {
        disableCompression();
        blockWriter.flush();
    }

    @Override
    public void close() throws IOException {
        disableCompression();
        blockWriter.close();
    }

    public void cleanup() {
        if (lzmaWriter != null) {
            lzmaWriter.cleanup();
        }
        blockWriter.cleanup();
    }

    private ChunkWriter getChunkWriter() {
        return Objects.firstNonNull(lzmaWriter, blockWriter);
    }

    private void enableCompression() throws IOException {
        if (lzmaWriter != null) return;
        blockWriter.setCompressed(true);
        lzmaWriter = new LzmaWriter(compressionMethod, blockWriter, executorService);
    }

    private void disableCompression() throws IOException {
        if (lzmaWriter == null) return;
        lzmaWriter.close();
        blockWriter.setCompressed(false);
        lzmaWriter = null;
    }
}
