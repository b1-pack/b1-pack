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
import com.google.common.io.Files;
import org.b1.pack.api.common.FileBuilder;
import org.b1.pack.api.common.FileContent;
import org.b1.pack.api.common.FolderBuilder;
import org.b1.pack.api.common.PackEntry;
import org.b1.pack.api.reader.PackReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExtractCommand implements PackCommand {

    @Override
    public void execute(ArgSet argSet) throws IOException {
        Preconditions.checkArgument(argSet.getFileNames().isEmpty(), "Filters not supported");
        File file = new File(argSet.getPackName());
        final File outputFolder = FileTools.getOutputFolder(argSet);
        System.out.println("Extracting from \"" + file + "\" to \"" + (outputFolder != null ? outputFolder.getPath() : ".") + "\".");
        System.out.println();
        PackReader reader = PackReader.getInstance(argSet.getTypeFormat());
        reader.read(ReaderProviderFactory.createReaderProvider(file), new ExtractFolderBuilder(outputFolder, null));
        System.out.println();
        System.out.println("Done");
    }

    private static void setLastModified(File file, Long time) {
        Preconditions.checkState(time == null || file.setLastModified(time), "Cannot set time: %s", file);
    }

    private static class ExtractFolderBuilder implements FolderBuilder {

        private final File targetFolder;
        private final Long lastModifiedTime;

        private ExtractFolderBuilder(File targetFolder, Long lastModifiedTime) {
            this.targetFolder = targetFolder;
            this.lastModifiedTime = lastModifiedTime;
        }

        @Override
        public FileBuilder addFile(PackEntry entry, Long size) {
            File nativeFile = getNativeFile(entry.getName());
            return new ExtractFileBuilder(nativeFile, entry.getLastModifiedTime());
        }

        @Override
        public FolderBuilder addFolder(PackEntry entry) {
            File nativeFile = getNativeFile(entry.getName());
            System.out.println("Extracting " + nativeFile);
            Preconditions.checkState(nativeFile.mkdir(), "Cannot create folder: %s", nativeFile);
            return new ExtractFolderBuilder(nativeFile, entry.getLastModifiedTime());
        }

        @Override
        public void save() {
            setLastModified(targetFolder, lastModifiedTime);
        }

        private File getNativeFile(String name) {
            return targetFolder == null ? new File(name) : new File(targetFolder, name);
        }
    }

    private static class ExtractFileBuilder implements FileBuilder {

        private final File targetFile;
        private final Long lastModifiedTime;

        public ExtractFileBuilder(File targetFile, Long lastModifiedTime) {
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
            setLastModified(targetFile, lastModifiedTime);
        }
    }
}
