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

package org.b1.pack.cli;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import org.b1.pack.api.builder.BuilderFile;

import java.io.*;
import java.util.List;

public class FsBuilderFile implements BuilderFile {

    private final FsObject fsObject;
    private long size;

    public FsBuilderFile(FsObject fsObject) {
        this.fsObject = fsObject;
        this.size = fsObject.getFile().length();
    }

    @Override
    public List<String> getPath() {
        return fsObject.getPath();
    }

    @Override
    public Long getLastModifiedTime() {
        return fsObject.getFile().lastModified();
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public void writeTo(OutputStream stream, long start, long end) throws IOException {
        Preconditions.checkArgument(start < end);
        InputStream inputStream = new FileInputStream(fsObject.getFile());
        try {
            ByteStreams.skipFully(inputStream, start);
            byte[] buffer = new byte[0x10000];
            while (start < end) {
                int count = inputStream.read(buffer, 0, (int) Math.min(buffer.length, end - start));
                if (count <= 0) throw new EOFException();
                stream.write(buffer, 0, count);
                start += count;
            }
            Preconditions.checkState(start == end);
        } finally {
            inputStream.close();
        }
    }

    @Override
    public void beforeAdd() {
        System.out.println("Adding " + fsObject.getFile());
    }
}
