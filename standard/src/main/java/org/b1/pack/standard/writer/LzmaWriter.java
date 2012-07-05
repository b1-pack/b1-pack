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
import org.b1.pack.api.builder.Writable;
import org.b1.pack.standard.common.RecordPointer;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

class LzmaWriter extends ChunkWriter implements Callable<Void> {

    private final Pipe pipe = Pipe.open();
    private final InputStream pipedInputStream = new BufferedInputStream(Channels.newInputStream(pipe.source()));
    private final OutputStream pipedOutputStream = Channels.newOutputStream(pipe.sink());
    private final LzmaMethod lzmaMethod;
    private final OutputStream outputStream;
    private final RecordPointer startPointer;
    private final Future<Void> future;
    private long count;

    public LzmaWriter(LzmaMethod lzmaMethod, BlockWriter blockWriter, ExecutorService executorService) throws IOException {
        this.lzmaMethod = lzmaMethod;
        this.outputStream = new BufferedOutputStream(blockWriter);
        this.startPointer = blockWriter.getCurrentPointer();
        this.future = executorService.submit(this);
    }

    public RecordPointer getCurrentPointer() throws IOException {
        return new RecordPointer(startPointer.volumeNumber, startPointer.blockOffset, count);
    }

    public long getCount() {
        return count;
    }

    @Override
    public void write(int b) throws IOException {
        try {
            pipedOutputStream.write(b);
            count++;
        } catch (IOException e) {
            checkEncoder();
            throw e;
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        try {
            pipedOutputStream.write(b, off, len);
            count += len;
        } catch (IOException e) {
            checkEncoder();
            throw e;
        }
    }

    @Override
    public void write(Writable value) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        pipedOutputStream.close();
        try {
            future.get();
        } catch (Exception e) {
            throw (IOException) new IOException().initCause(e);
        }
    }

    @Override
    public Void call() throws IOException {
        try {
            Encoder encoder = new Encoder();
            encoder.SetEndMarkerMode(true);
            encoder.SetDictionarySize(lzmaMethod.getDictionarySize());
            encoder.SetNumFastBytes(lzmaMethod.getNumberOfFastBytes());
            encoder.WriteCoderProperties(outputStream);
            encoder.Code(pipedInputStream, outputStream, -1, -1, null);
            outputStream.flush();
            return null;
        } finally {
            pipedInputStream.close();
        }
    }

    public void cleanup() {
        future.cancel(true);
    }

    private void checkEncoder() throws IOException {
        if (future.isDone()) {
            try {
                future.get();
            } catch (Exception e) {
                throw (IOException) new IOException().initCause(e);
            }
        }
    }
}
