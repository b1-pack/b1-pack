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

import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

public final class MemoryBuffer extends ByteArrayOutputStream {

    private int index;

    public MemoryBuffer(int capacity) {
        super(capacity);
    }

    public void setIndex(long index) {
        int position = Ints.checkedCast(index);
        Preconditions.checkPositionIndex(position, count);
        this.index = position;
    }

    public int read() {
        return index < count ? buf[index++] & 0xFF : -1;
    }

    public int read(byte[] b, int off, int len) {
        int left = count - index;
        if (left <= 0) {
            return -1;
        }
        int size = Math.min(left, len);
        System.arraycopy(buf, index, b, off, size);
        index += size;
        return size;
    }

    public long skip(long n) {
        long result = Math.max(0, Math.min(count - index, n));
        index += result;
        return result;
    }

    @Override
    public void reset() {
        super.reset();
        index = 0;
    }

    public int lastIndexOf(byte target) {
        for (int i = count - 1; i >= 0; i--) {
            if (buf[i] == target) {
                return i;
            }
        }
        return -1;
    }

    public String getString(int offset, int length, Charset charset) {
        Preconditions.checkPositionIndexes(offset, offset + length, count);
        return new String(buf, offset, length, charset);
    }
}
