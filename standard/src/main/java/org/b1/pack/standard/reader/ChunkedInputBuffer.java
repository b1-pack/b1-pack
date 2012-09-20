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

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import org.b1.pack.standard.common.Numbers;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

class ChunkedInputBuffer {

    private byte[] buf = new byte[0];
    private int size;

    public byte[] getBuf() {
        return buf;
    }

    public int size() {
        return size;
    }

    public void resetAndRead(InputStream stream) throws IOException {
        size = 0;
        while (true) {
            int count = Ints.checkedCast(Preconditions.checkNotNull(Numbers.readLong(stream), "Null chunked data"));
            if (count == 0) {
                return;
            }
            if (size + count > buf.length) {
                byte[] buffer = new byte[size + count];
                System.arraycopy(buf, 0, buffer, 0, size);
                buf = buffer;
            }
            while (count > 0) {
                int n = stream.read(buf, size, count);
                if (n <= 0) throw new EOFException();
                size += n;
                count -= n;
            }
        }
    }
}
