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

import org.b1.pack.api.explorer.PxFile;
import org.b1.pack.api.explorer.PxVisitor;
import org.b1.pack.standard.common.RecordPointer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static org.b1.pack.standard.common.Constants.COMPLETE_FILE;
import static org.b1.pack.standard.common.Numbers.readLong;
import static org.b1.pack.standard.explorer.RecordHeader.readRecordHeader;

public class StandardPxFile implements PxFile {

    private final PackNavigator navigator;
    private final RecordPointer pointer;
    private final RecordHeader header;
    private final List<String> path;
    private final long size;

    public StandardPxFile(PackNavigator navigator, RecordPointer pointer, RecordHeader header, List<String> path, long size) {
        this.navigator = navigator;
        this.pointer = pointer;
        this.header = header;
        this.path = path;
        this.size = size;
    }

    @Override
    public List<String> getPath() {
        return path;
    }

    @Override
    public Long getLastModifiedTime() {
        return header.lastModifiedTime;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        boolean pending = true;
        InputStream stream = navigator.getRecordStream(pointer);
        try {
            checkArgument(readLong(stream) == COMPLETE_FILE);
            readRecordHeader(stream); // ignore for now
            pending = false;
            return new ChunkedInputStream(stream);
        } finally {
            if (pending) {
                stream.close();
            }
        }
    }

    @Override
    public void accept(PxVisitor visitor) throws IOException {
        visitor.visit(this);
    }
}
