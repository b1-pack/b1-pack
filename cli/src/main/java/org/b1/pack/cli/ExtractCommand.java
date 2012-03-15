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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import org.b1.pack.api.explorer.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ExtractCommand implements PackCommand {

    @Override
    public void execute(ArgSet argSet) throws IOException {
        Preconditions.checkArgument(argSet.getFileNames().isEmpty(), "Filters not supported");
        File file = new File(argSet.getPackName());
        final File outputFolder = FileTools.getOutputFolder(argSet);
        System.out.println("Extracting from \"" + file + "\" to \"" + outputFolder + "\".");
        System.out.println();
        PackExplorer explorer = PackExplorer.getInstance(argSet.getTypeFormat());
        explorer.explore(ExplorerProviderFactory.createExplorerProvider(file), new ExplorerCommand() {
            @Override
            public void execute(ExplorerPack pack) throws IOException {
                pack.listObjects(new ExtractVisitor(outputFolder));
            }
        });
        System.out.println();
        System.out.println("Done");
    }

    private static void startExtracting(String path, File file) {
        Preconditions.checkState(!file.exists(), "File already exists: %s", file);
        System.out.println("Extracting " + path);
    }

    private static void extractFile(final ExplorerFile pxFile, File nativeFile) throws IOException {
        File tempFile = FileTools.createTempFile(nativeFile);
        Files.copy(new InputSupplier<InputStream>() {
            @Override
            public InputStream getInput() throws IOException {
                return pxFile.getInputStream();
            }
        }, tempFile);
        Files.move(tempFile, nativeFile);
    }

    private static void createFolder(File folder) throws IOException {
        if (!folder.mkdirs()) {
            throw new IOException("Cannot create folder: " + folder);
        }
    }

    private static void setAttributes(ExplorerObject object, File file) throws IOException {
        Long time = object.getLastModifiedTime();
        if (time != null && !file.setLastModified(time)) {
            throw new IOException("Cannot set time: " + file);
        }
    }

    private static class ExtractVisitor implements ExplorerVisitor {

        private final File outputFolder;

        private ExtractVisitor(File outputFolder) {
            this.outputFolder = outputFolder != null ? outputFolder : new File(".");
        }

        @Override
        public void visit(ExplorerFolder folder) throws IOException {
            String path = getPath(folder);
            File nativeFile = new File(outputFolder, path);
            startExtracting(path, nativeFile);
            createFolder(nativeFile);
            setAttributes(folder, nativeFile);
        }

        @Override
        public void visit(ExplorerFile file) throws IOException {
            String path = getPath(file);
            File nativeFile = new File(outputFolder, path);
            startExtracting(path, nativeFile);
            extractFile(file, nativeFile);
            setAttributes(file, nativeFile);
        }

        private String getPath(ExplorerObject object) {
            return Joiner.on(File.separator).join(object.getPath());
        }
    }
}
