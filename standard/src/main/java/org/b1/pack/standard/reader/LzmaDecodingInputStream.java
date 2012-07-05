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

package org.b1.pack.standard.reader;

import SevenZip.Compression.LZMA.Decoder;
import com.google.common.base.Preconditions;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

class LzmaDecodingInputStream extends InputStream implements Callable<Void> {

    private final Pipe pipe = Pipe.open();
    private final InputStream pipedInputStream = Channels.newInputStream(pipe.source());
    private final OutputStream pipedOutputStream = new BufferedOutputStream(Channels.newOutputStream(pipe.sink()));
    private final InputStream inputStream;
    private final Decoder decoder;
    private final Future<Void> future;

    public LzmaDecodingInputStream(InputStream inputStream, Decoder decoder, ExecutorService executorService) throws IOException {
        this.inputStream = new BufferedInputStream(inputStream);
        this.decoder = decoder;
        this.future = executorService.submit(this);
    }

    @Override
    public int read() throws IOException {
        return pipedInputStream.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return pipedInputStream.read(b, off, len);
    }

    @Override
    public Void call() throws Exception {
        try {
            Preconditions.checkState(decoder.Code(inputStream, pipedOutputStream, -1));
            return null;
        } finally {
            pipedOutputStream.close();
        }
    }

    @Override
    public void close() throws IOException {
        if (future.isDone()) {
            try {
                future.get();
            } catch (Exception e) {
                throw (IOException) new IOException().initCause(e);
            }
        } else {
            pipedInputStream.close();
        }
    }
}
