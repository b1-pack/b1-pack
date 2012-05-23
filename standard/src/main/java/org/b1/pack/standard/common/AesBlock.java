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

import com.google.common.primitives.Ints;
import org.b1.pack.api.builder.Writable;

import java.io.IOException;
import java.io.OutputStream;

public class AesBlock implements Writable {

    private static final PbInt AES_BLOCK_CODE = new PbInt((long) Constants.AES_BLOCK);

    private final VolumeCipher cipher;
    private final long offset;
    private final PbBlock block;

    public AesBlock(VolumeCipher cipher, long offset, PbBlock block) {
        this.cipher = cipher;
        this.offset = offset;
        this.block = block;
    }

    @Override
    public long getSize() {
        return AES_BLOCK_CODE.getSize() + PbBinary.getSerializedSize(block.getSize() + VolumeCipher.MAC_BYTE_SIZE);
    }

    @Override
    public void writeTo(OutputStream stream, long start, long end) throws IOException {
        long size = block.getSize();
        MemoryOutputStream buffer = new MemoryOutputStream(Ints.checkedCast(size));
        block.writeTo(buffer, 0, size);
        Writable content = new ByteArrayWritable(cipher.cipherBlock(true, offset, buffer.getBuf()));
        new CompositeWritable(AES_BLOCK_CODE, new PbBinary(content)).writeTo(stream, start, end);
    }
}
