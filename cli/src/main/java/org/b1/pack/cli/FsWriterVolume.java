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

package org.b1.pack.cli;

import com.google.common.base.Preconditions;
import org.b1.pack.api.writer.WriterVolume;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FsWriterVolume extends WriterVolume {

    private final File file;
    private File tempFile;

    public FsWriterVolume(File file) {
        this.file = file;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        System.out.println();
        System.out.println("Creating volume " + file);
        System.out.println();
        Preconditions.checkState(!file.exists(), "File already exists: %s", file);
        Preconditions.checkState(tempFile == null);
        tempFile = FileTools.createTempFile(file);
        return new FileOutputStream(tempFile);
    }

    @Override
    public void seek(OutputStream stream, long position) throws IOException {
        ((FileOutputStream) stream).getChannel().position(position);
    }

    @Override
    public void save() throws IOException {
        Preconditions.checkState(tempFile.renameTo(file));
    }
}
