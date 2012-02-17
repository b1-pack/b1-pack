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

import com.google.common.collect.Maps;
import org.b1.pack.api.builder.Writable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.NavigableMap;

public class CompositeWritable implements Writable {

    private final NavigableMap<Long, Writable> map = Maps.newTreeMap();
    private long size;

    public CompositeWritable(Writable... writables) {
        for (Writable writable : writables) {
            add(writable);
        }
    }

    public void add(Writable writable) {
        long writableSize = writable.getSize();
        if (writableSize > 0) {
            map.put(size += writableSize, writable);
        }
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public void writeTo(OutputStream stream, long start, long end) throws IOException {
        for (Map.Entry<Long, Writable> entry : map.tailMap(start, false).entrySet()) {
            Writable part = entry.getValue();
            long partSize = part.getSize();
            long partEnd = entry.getKey();
            long partStart = partEnd - partSize;
            part.writeTo(stream, Math.max(start - partStart, 0), partSize - Math.max(partEnd - end, 0));
            if (end <= partEnd) {
                return;
            }
        }
        throw new IllegalArgumentException();
    }
}
