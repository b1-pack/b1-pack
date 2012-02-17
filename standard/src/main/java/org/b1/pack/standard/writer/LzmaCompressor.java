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

import SevenZip.Compression.LZMA.Encoder;
import org.b1.pack.api.compression.LzmaCompressionMethod;
import org.b1.pack.standard.common.RecordPointer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

class LzmaCompressor extends OutputStream implements Callable<Void> {

    private final PipedInputStream pipedInputStream = new PipedInputStream();
    private final PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
    private final LzmaCompressionMethod compressionMethod;
    private final BlockWriter blockWriter;
    private final RecordPointer startPointer;
    private final Future<Void> future;
    private long count;

    public LzmaCompressor(LzmaCompressionMethod compressionMethod, BlockWriter blockWriter, ExecutorService executorService) throws IOException {
        this.blockWriter = blockWriter;
        this.compressionMethod = compressionMethod;
        this.startPointer = blockWriter.getCurrentPointer();
        this.future = executorService.submit(this);
    }

    public RecordPointer getCurrentPointer() throws IOException {
        return new RecordPointer(startPointer.volumeNumber, startPointer.blockOffset, count);
    }

    @Override
    public void write(int b) throws IOException {
        pipedOutputStream.write(b);
        count++;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        pipedOutputStream.write(b, off, len);
        count += len;
    }

    @Override
    public void close() throws IOException {
        pipedOutputStream.close();
        try {
            future.get();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public Void call() throws IOException {
        Encoder encoder = new Encoder();
        encoder.SetEndMarkerMode(true);
        encoder.WriteCoderProperties(blockWriter);
        encoder.Code(pipedInputStream, blockWriter, -1, -1, null);
        return null;
    }
}
