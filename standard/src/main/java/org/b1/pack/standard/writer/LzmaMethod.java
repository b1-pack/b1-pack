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

package org.b1.pack.standard.writer;

import com.google.common.collect.ImmutableSet;
import org.b1.pack.api.common.CompressionMethod;
import org.b1.pack.api.common.PackEntry;

class LzmaMethod {

    private static final ImmutableSet<String> compressedFormatSet = CompressedFormatLoader.getCompressedFormatSet();

    private final CompressionMethod method;
    private final long solidBlockSize;
    private final int dictionarySize;
    private final int numberOfFastBytes;
    private final boolean smart;

    public LzmaMethod(CompressionMethod method, long solidBlockSize, int dictionarySize, int numberOfFastBytes, boolean smart) {
        this.method = method;
        this.solidBlockSize = solidBlockSize;
        this.dictionarySize = dictionarySize;
        this.numberOfFastBytes = numberOfFastBytes;
        this.smart = smart;
    }

    public String getName() {
        return method.getName();
    }

    public long getSolidBlockSize() {
        return solidBlockSize;
    }

    public int getDictionarySize() {
        return dictionarySize;
    }

    public int getNumberOfFastBytes() {
        return numberOfFastBytes;
    }

    public boolean isCompressible(PackEntry entry, Long size) {
        if (!method.isCompressible(entry, size)) return false;
        if (!smart) return true;
        String entryName = entry.getName();
        int dotIndex = entryName.lastIndexOf('.');
        return dotIndex < 0 || !compressedFormatSet.contains(entryName.substring(dotIndex + 1).toLowerCase());
    }

    public static LzmaMethod valueOf(CompressionMethod method) {
        if (method == null) {
            return null;
        }
        String name = method.getName();
        if (CompressionMethod.SMART.equals(name) || CompressionMethod.CLASSIC.equals(name)) {
            return new LzmaMethod(method, 1 << 27, 1 << 20, 1 << 5, CompressionMethod.SMART.equals(name));
        }
        if (CompressionMethod.MAXIMUM.equals(name)) {
            return new LzmaMethod(method, 1L << 32, 1 << 25, 1 << 6, false);
        }
        throw new IllegalArgumentException("Unsupported compression method: " + name);
    }
}
