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

import org.b1.pack.api.common.FolderBuilder;
import org.b1.pack.api.reader.PackReader;
import org.b1.pack.api.reader.ReaderProvider;

import java.io.IOException;

import static org.b1.pack.api.common.PackFormat.B1;

public class StandardPackReader extends PackReader {

    @Override
    public void read(ReaderProvider provider) throws IOException {
        VolumeCursor volumeCursor = new VolumeCursor(provider);
        try {
            volumeCursor.initialize();
            read(volumeCursor, provider.getFolderBuilder());
        } finally {
            volumeCursor.close();
        }
    }

    @Override
    protected boolean isFormatSupported(String format) {
        return B1.equals(format);
    }

    private static void read(VolumeCursor volumeCursor, FolderBuilder builder) throws IOException {
        PackInputStream stream = new PackInputStream(new ChunkCursor(new BlockCursor(volumeCursor)));
        try {
            stream.seek(volumeCursor.getCatalogPointer());
            new RecordReader(builder).read(stream, volumeCursor.getObjectTotal());
        } finally {
            stream.close();
        }
    }
}
