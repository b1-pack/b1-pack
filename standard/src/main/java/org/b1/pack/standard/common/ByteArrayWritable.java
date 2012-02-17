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

import com.google.common.primitives.Ints;
import org.b1.pack.api.builder.Writable;

import java.io.IOException;
import java.io.OutputStream;

public class ByteArrayWritable implements Writable {

    private final byte[] bytes;

    public ByteArrayWritable(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public long getSize() {
        return bytes.length;
    }

    @Override
    public void writeTo(OutputStream stream, long start, long end) throws IOException {
        stream.write(bytes, Ints.checkedCast(start), Ints.checkedCast(end - start));
    }
}
