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

package org.b1.pack.standard.explorer;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Ints;

import java.io.IOException;
import java.io.InputStream;

import static org.b1.pack.standard.common.Numbers.readLong;

public class RecordHeader {

    public final Long id;
    public final Long parentId;
    public final String name;
    public final Long lastModifiedTime;

    public RecordHeader(Long id, Long parentId, String name, Long lastModifiedTime) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.lastModifiedTime = lastModifiedTime;
    }

    public static RecordHeader readRecordHeader(InputStream stream) throws IOException {
        Long id = readLong(stream);
        Long parentId = readLong(stream);
        String name = readText(stream);
        Long lastModifiedTime = null;
        Long code = readLong(stream);
        if (code != null) {
            Preconditions.checkArgument(code == 0);
            lastModifiedTime = readLong(stream);
            Preconditions.checkArgument(readLong(stream) == null);
        }
        return new RecordHeader(id, parentId, name, lastModifiedTime);
    }

    private static String readText(InputStream stream) throws IOException {
        Long size = readLong(stream);
        if (size == null) {
            return null;
        }
        byte[] bytes = new byte[Ints.checkedCast(size)];
        ByteStreams.readFully(stream, bytes);
        return new String(bytes, Charsets.UTF_8);
    }
}
