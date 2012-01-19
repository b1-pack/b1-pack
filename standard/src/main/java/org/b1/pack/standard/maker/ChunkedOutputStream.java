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

package org.b1.pack.standard.maker;

import com.google.common.base.Preconditions;
import org.b1.pack.standard.common.Numbers;

import java.io.IOException;
import java.io.OutputStream;

public class ChunkedOutputStream extends OutputStream {

    private final OutputStream stream;
    private final byte[] buffer;
    private int count;

    public ChunkedOutputStream(int capacity, OutputStream stream) {
        Preconditions.checkArgument(capacity > 0);
        this.stream = stream;
        this.buffer = new byte[capacity];
    }

    @Override
    public void write(int b) throws IOException {
        buffer[count++] = (byte) b;
        ensureFreeSpace();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        Preconditions.checkPositionIndexes(off, off + len, b.length);
        while (len > 0) {
            int size = Math.min(len, buffer.length - count);
            System.arraycopy(b, off, buffer, count, size);
            off += size;
            len -= size;
            count += size;
            ensureFreeSpace();
        }
    }

    private void ensureFreeSpace() throws IOException {
        if (count == buffer.length) {
            flushBuffer();
        }
    }

    @Override
    public void flush() throws IOException {
        stream.flush();
    }

    @Override
    public void close() throws IOException {
        if (count > 0) {
            flushBuffer();
        }
        Numbers.writeLong(0, stream);
        count = Integer.MAX_VALUE;
    }

    private void flushBuffer() throws IOException {
        Numbers.writeLong(count, stream);
        stream.write(buffer, 0, count);
        count = 0;
    }
}
