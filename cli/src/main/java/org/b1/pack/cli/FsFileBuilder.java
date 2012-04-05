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
import com.google.common.io.Files;
import org.b1.pack.api.common.FileBuilder;
import org.b1.pack.api.common.FileContent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FsFileBuilder implements FileBuilder {

    private final File targetFile;
    private final Long lastModifiedTime;

    public FsFileBuilder(File targetFile, Long lastModifiedTime) {
        this.targetFile = targetFile;
        this.lastModifiedTime = lastModifiedTime;
    }

    @Override
    public void setContent(FileContent content) throws IOException {
        Preconditions.checkState(!targetFile.exists(), "File already exists: %s", targetFile);
        System.out.println("Extracting " + targetFile);
        File tempFile = FileTools.createTempFile(targetFile);
        FileOutputStream stream = new FileOutputStream(tempFile);
        try {
            content.writeTo(stream, 0, null);
        } finally {
            stream.close();
        }
        Files.move(tempFile, targetFile);
    }

    @Override
    public void save() {
        FileTools.setLastModified(targetFile, lastModifiedTime);
    }
}
