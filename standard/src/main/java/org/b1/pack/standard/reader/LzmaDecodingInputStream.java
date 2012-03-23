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
import SevenZip.Compression.LZMA.Encoder;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

class LzmaDecodingInputStream extends InputStream implements Callable<Void> {

    private final PipedInputStream pipedInputStream = new PipedInputStream();
    private final PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
    private final InputStream inputStream;
    private final Future<Void> future;

    public LzmaDecodingInputStream(InputStream inputStream, ExecutorService executorService) throws IOException {
        this.inputStream = inputStream;
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
        byte[] properties = new byte[Encoder.kPropSize];
        ByteStreams.readFully(inputStream, properties);
        Decoder decoder = new Decoder();
        Preconditions.checkState(decoder.SetDecoderProperties(properties));
        Preconditions.checkState(decoder.Code(inputStream, pipedOutputStream, -1));
        return null;
    }

    @Override
    public void close() throws IOException {
        if (future.isDone()) {
            try {
                future.get();
            } catch (Exception e) {
                throw new IOException(e);
            }
        } else {
            pipedInputStream.close();
            future.cancel(false);
        }
    }
}
