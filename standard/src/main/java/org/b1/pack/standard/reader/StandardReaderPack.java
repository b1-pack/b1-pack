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

import org.b1.pack.api.reader.PackVisitor;
import org.b1.pack.api.reader.ReaderPack;

import java.io.IOException;

class StandardReaderPack extends ReaderPack {

    private final VolumeCursor volumeCursor;

    public StandardReaderPack(VolumeCursor volumeCursor) {
        this.volumeCursor = volumeCursor;
    }

    @Override
    public void accept(PackVisitor visitor) throws IOException {
        PackInputStream stream = new PackInputStream(new ChunkCursor(new BlockCursor(volumeCursor)));
        try {
            stream.seek(volumeCursor.getCatalogPointer());
            new RecordReader(visitor).read(stream, volumeCursor.getObjectTotal());
        } finally {
            stream.close();
        }
    }
}
