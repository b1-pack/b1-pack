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
package org.b1.pack.standard.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronousPipe {

    protected final Lock lock = new ReentrantLock();
    protected final Condition dataPresent = lock.newCondition();
    protected final Condition dataAbsent = lock.newCondition();
    protected final byte[] readerBuffer = new byte[1];
    protected final byte[] writerBuffer = new byte[1];

    protected byte[] buffer;
    protected int offset;
    protected int length;
    protected boolean inputClosed;
    protected boolean outputClosed;

    public final InputStream inputStream = new InputStream() {

        @Override
        public int read() throws IOException {
            return read(readerBuffer, 0, 1) == 1 ? readerBuffer[0] & 0xFF : -1;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (len == 0) return 0;
            obtainLock();
            try {
                waitForDataPresent();
                assertInputOpen();
                if (length == 0) return -1;
                int result = Math.min(len, length);
                System.arraycopy(buffer, offset, b, off, result);
                offset += result;
                if ((length -= result) == 0) {
                    dataAbsent.signal();
                }
                return result;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void close() throws IOException {
            obtainLock();
            try {
                inputClosed = true;
                dataPresent.signal();
                dataAbsent.signal();
            } finally {
                lock.unlock();
            }
        }
    };

    public final OutputStream outputStream = new OutputStream() {

        @Override
        public void write(int b) throws IOException {
            writerBuffer[0] = (byte) b;
            write(writerBuffer, 0, 1);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            obtainLock();
            try {
                waitForDataAbsent();
                assertInputOpen();
                assertOutputOpen();
                buffer = b;
                offset = off;
                length = len;
                dataPresent.signal();
                waitForDataAbsent();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void close() throws IOException {
            obtainLock();
            try {
                outputClosed = true;
                dataPresent.signal();
                dataAbsent.signal();
            } finally {
                lock.unlock();
            }
        }

    };

    protected void assertInputOpen() throws IOException {
        if (inputClosed) throw new IOException("Input closed");
    }

    protected void assertOutputOpen() throws IOException {
        if (outputClosed) throw new IOException("Output closed");
    }

    protected void obtainLock() throws InterruptedIOException {
        try {
            lock.lockInterruptibly();
        } catch (InterruptedException e) {
            throw (InterruptedIOException) new InterruptedIOException().initCause(e);
        }
    }

    protected void waitForDataPresent() throws InterruptedIOException {
        while (length == 0 && !inputClosed && !outputClosed) {
            try {
                dataPresent.await();
            } catch (InterruptedException e) {
                throw (InterruptedIOException) new InterruptedIOException().initCause(e);
            }
        }
    }

    protected void waitForDataAbsent() throws InterruptedIOException {
        while (length > 0 && !inputClosed && !outputClosed) {
            try {
                dataAbsent.await();
            } catch (InterruptedException e) {
                throw (InterruptedIOException) new InterruptedIOException().initCause(e);
            }
        }
    }
}
