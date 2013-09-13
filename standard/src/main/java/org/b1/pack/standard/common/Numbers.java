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

import java.io.*;

public class Numbers {

    public static final int MAX_LONG_SIZE = getSerializedSize(Long.MIN_VALUE);
    public static final int MAX_INT_SIZE = getSerializedSize((long) Integer.MIN_VALUE);

    public static byte[] serializeLong(Long value) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            writeLong(value, stream);
            return stream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] serializeLong(Long value, int size) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            writeLong(value, stream);
            int minSize = stream.size();
            if (minSize == size) {
                return stream.toByteArray();
            }
            Preconditions.checkArgument(minSize < size);
            while (stream.size() < size - 1) {
                stream.write(0x80);
            }
            stream.write(0);
            byte[] result = stream.toByteArray();
            result[minSize - 1] |= 0x80;
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getSerializedSize(long value) {
        return serializeLong(value).length;
    }

    public static void writeLong(Long value, OutputStream stream) throws IOException {
        if (value == null) {
            stream.write(1);
        } else {
            writeLong(value.longValue(), stream);
        }
    }

    public static void writeLong(long value, OutputStream stream) throws IOException {
        int sign = 0;
        if (value < 0) {
            sign = 1;
            value = -value;
        }
        int b = (int) ((value & 0x3F) << 1) | sign;
        value >>>= 6;
        while (value != 0) {
            stream.write(b | 0x80);
            b = (int) (value & 0x7F);
            value >>>= 7;
        }
        stream.write(b);
    }

    public static Long readLong(InputStream stream) throws IOException {
        int b = read(stream);
        int sign = b & 1;
        long value = (b >> 1) & 0x3F;
        for (int i = 6; (b & 0x80) != 0; i += 7) {
            b = read(stream);
            long bits = b & 0x7FL;
            if (i >= 62 && bits != 0 && (i > 62 || (bits == 2 ? value != 0 : bits != 1))) {
                throw new IllegalArgumentException("Value too long");
            }
            value |= bits << i;
        }
        return sign == 0 ? Long.valueOf(value) : value == 0 ? null : -value;
    }

    private static int read(InputStream stream) throws IOException {
        int result = stream.read();
        if (result == -1) {
            throw new EOFException();
        }
        return result;
    }
}
