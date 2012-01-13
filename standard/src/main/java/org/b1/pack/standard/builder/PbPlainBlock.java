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

package org.b1.pack.standard.builder;

import com.google.common.primitives.Ints;
import org.b1.pack.api.builder.Writable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;

public class PbPlainBlock implements Writable {

    private final Writable content;

    public PbPlainBlock(Writable content) {
        this.content = content;
    }

    @Override
    public long getSize() {
        return createPlainBlock(content, 0).getSize();
    }

    @Override
    public void writeTo(OutputStream stream, long start, long end) throws IOException {
        int size = Ints.checkedCast(content.getSize());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(size);
        Adler32 adler32 = new Adler32();
        content.writeTo(new CheckedOutputStream(outputStream, adler32), 0, size);
        Writable block = createPlainBlock(new ByteArrayWritable(outputStream.toByteArray()), (int) adler32.getValue());
        block.writeTo(stream, start, end);
    }

    private static Writable createPlainBlock(Writable content, int checksum) {
        return new CompositeWritable(new PbBinary(content), new ByteArrayWritable(Ints.toByteArray(checksum)));
    }
}
