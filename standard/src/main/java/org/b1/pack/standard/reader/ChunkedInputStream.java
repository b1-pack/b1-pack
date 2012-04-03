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

package org.b1.pack.standard.reader;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import org.b1.pack.standard.common.Numbers;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

class ChunkedInputStream extends InputStream {

    private final InputStream stream;
    private long count;
    private boolean isNull;
    private boolean closed;

    public ChunkedInputStream(InputStream stream) {
        this.stream = stream;
    }

    public boolean isNull() throws IOException {
        return isEnd() && isNull;
    }

    @Override
    public int read() throws IOException {
        if (isEnd()) {
            return -1;
        }
        int result = stream.read();
        if (result == -1) {
            throw new EOFException();
        }
        count--;
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (isEnd()) {
            return -1;
        }
        int result = stream.read(b, off, count > Integer.MAX_VALUE ? len : Math.min(len, Ints.checkedCast(count)));
        if (result <= 0) {
            throw new EOFException();
        }
        count -= result;
        return result;
    }

    private boolean isEnd() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
        if (count == 0) {
            Long value = Numbers.readLong(stream);
            if (value == null) {
                isNull = true;
                count = Long.MIN_VALUE;
            } else if (value == 0) {
                count = Long.MIN_VALUE;
            } else {
                Preconditions.checkState(value > 0, "Size is negative");
                count = value;
            }
        }
        return count == Long.MIN_VALUE;
    }

    @Override
    public void close() throws IOException {
        stream.close();
        closed = true;
    }
}
