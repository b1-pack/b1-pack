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

import org.b1.pack.api.builder.Writable;
import org.b1.pack.api.writer.WriterProvider;
import org.b1.pack.standard.common.Numbers;
import org.b1.pack.standard.common.PbRecordPointer;
import org.b1.pack.standard.common.RecordPointer;

import java.io.IOException;
import java.io.OutputStream;

class RecordWriter extends OutputStream {

    private final WriterProvider provider;
    private final BlockWriter blockWriter;
    private final int volumeNumberSize;
    private final int blockOffsetSize;

    public RecordWriter(WriterProvider provider) {
        this.provider = provider;
        blockWriter = new BlockWriter(provider);
        volumeNumberSize = Numbers.getSerializedSize(provider.getMaxVolumeCount());
        blockOffsetSize = Numbers.getSerializedSize(provider.getMaxVolumeSize());
    }

    public boolean isSeekable() {
        return provider.isSeekable();
    }

    public RecordPointer getCurrentPointer() throws IOException {
        return blockWriter.getCurrentPointer();
    }

    public PbRecordPointer createEmptyPointer() {
        return new PbRecordPointer(volumeNumberSize, blockOffsetSize, Numbers.MAX_LONG_SIZE);//todo
    }

    @Override
    public void write(int b) throws IOException {
        blockWriter.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        blockWriter.write(b, off, len);
    }

    public void write(Writable value) throws IOException {
        blockWriter.write(value);
    }

    @Override
    public void flush() throws IOException {
        blockWriter.flush();
    }

    @Override
    public void close() throws IOException {
        blockWriter.close();
    }

    public void cleanup() {
        blockWriter.cleanup();
    }

    public void setObjectCount(Long objectCount) {
        blockWriter.setObjectCount(objectCount);
    }

    public void setCompressible(boolean compressible) throws IOException {
        //todo
    }

    public void saveCatalogPoiner() throws IOException {
        blockWriter.saveCatalogPoiner();
    }
}
