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
import org.b1.pack.standard.common.SynchronousPipe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

class LzmaDecodingInputStream extends InputStream implements Callable<Void> {

    private static final Logger log = Logger.getLogger(LzmaDecodingInputStream.class.getName());

    private final SynchronousPipe pipe = new SynchronousPipe();
    private final InputStream pipedInputStream = pipe.inputStream;
    private final OutputStream pipedOutputStream = pipe.outputStream;
    private final InputStream inputStream;
    private final Decoder decoder;
    private final Future<Void> future;
    private volatile boolean decodingComplete;
    private boolean streamClosed;

    public LzmaDecodingInputStream(InputStream inputStream, Decoder decoder, ExecutorService executorService) throws IOException {
        this.inputStream = inputStream;
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
            decodingComplete = true;
            pipedOutputStream.close();
        }
    }

    @Override
    public void close() throws IOException {
        if (streamClosed) return;
        streamClosed = true;
        boolean errorsReported = decodingComplete;
        pipedInputStream.close();
        try {
            future.cancel(true);
            future.get();
        } catch (Exception e) {
            if (errorsReported) {
                throw (IOException) new IOException().initCause(e);
            } else {
                log.log(Level.FINEST, "Ignoring exception", e);
            }
        }
    }
}
