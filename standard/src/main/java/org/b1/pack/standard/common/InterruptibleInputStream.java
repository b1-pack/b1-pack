/*
 * Copyright 2011 b1.org
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

package org.b1.pack.standard.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

public class InterruptibleInputStream extends InputStream {

    private final Thread thread;
    private final InputStream stream;

    public InterruptibleInputStream(Thread thread, InputStream stream) {
        this.thread = thread;
        this.stream = stream;
    }

    @Override
    public int read() throws IOException {
        check();
        return stream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        check();
        return stream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        check();
        return stream.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        check();
        return stream.skip(n);
    }

    @Override
    public int available() throws IOException {
        check();
        return stream.available();
    }

    @Override
    public void close() throws IOException {
        // do not call check() here
        stream.close();
    }

    @Override
    public void mark(int readlimit) {
        stream.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        check();
        stream.reset();
    }

    @Override
    public boolean markSupported() {
        return stream.markSupported();
    }

    private void check() throws InterruptedIOException {
        if (thread.isInterrupted()) {
            throw new InterruptedIOException("Thread interrupted: " + thread.getName());
        }
    }
}
