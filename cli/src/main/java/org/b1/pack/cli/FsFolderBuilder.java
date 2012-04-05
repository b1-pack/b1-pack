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
import org.b1.pack.api.common.FileBuilder;
import org.b1.pack.api.common.FolderBuilder;
import org.b1.pack.api.common.PackEntry;

import java.io.File;

public class FsFolderBuilder implements FolderBuilder {

    private final File targetFolder;
    private final Long lastModifiedTime;

    public FsFolderBuilder(File targetFolder, Long lastModifiedTime) {
        this.targetFolder = targetFolder;
        this.lastModifiedTime = lastModifiedTime;
    }

    @Override
    public FileBuilder addFile(PackEntry entry, Long size) {
        File nativeFile = getNativeFile(entry.getName());
        return new FsFileBuilder(nativeFile, entry.getLastModifiedTime());
    }

    @Override
    public FolderBuilder addFolder(PackEntry entry) {
        File nativeFile = getNativeFile(entry.getName());
        System.out.println("Creating " + nativeFile);
        Preconditions.checkState(nativeFile.mkdir(), "Cannot create folder: %s", nativeFile);
        return new FsFolderBuilder(nativeFile, entry.getLastModifiedTime());
    }

    @Override
    public void save() {
        FileTools.setLastModified(targetFolder, lastModifiedTime);
    }

    private File getNativeFile(String name) {
        return targetFolder == null ? new File(name) : new File(targetFolder, name);
    }
}
