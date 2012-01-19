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

import org.b1.pack.api.common.PackException;
import org.b1.pack.api.maker.PmVolume;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkState;

public class FsPmVolume extends PmVolume {

    private final File file;
    private final long size;
    private File tempFile;

    public FsPmVolume(File file, long size) {
        this.file = file;
        this.size = size;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        System.out.println();
        System.out.println("Creating volume " + file);
        System.out.println();
        if (file.exists()) {
            throw new PackException("File already exists: " + file);
        }
        checkState(tempFile == null);
        tempFile = FileTools.createTempFile(file);
        return new FileOutputStream(tempFile);
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public void complete() throws IOException {
        checkState(tempFile.renameTo(file));
    }
}
