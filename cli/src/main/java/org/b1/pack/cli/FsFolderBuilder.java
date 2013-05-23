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
import java.util.Set;

public class FsFolderBuilder implements FolderBuilder {

    private final File targetFolder;
    private final Long lastModifiedTime;
    private final String prefixInArchive;
    private final Set<String> fileNameSet;

    public FsFolderBuilder(File targetFolder, Long lastModifiedTime, String prefixInArchive, Set<String> fileNameSet) {
        this.targetFolder = targetFolder;
        this.lastModifiedTime = lastModifiedTime;
        this.prefixInArchive = prefixInArchive;
        this.fileNameSet = fileNameSet;
    }

    @Override
    public FileBuilder addFile(PackEntry entry, Long size) {
        if (fileNameSet == null || fileNameSet.remove(getPath(entry))) {
            File nativeFile = getNativeFile(entry.getName());
            return new FsFileBuilder(nativeFile, entry.getLastModifiedTime());
        }
        return null;
    }

    @Override
    public FolderBuilder addFolder(PackEntry entry) {
        String path = getPath(entry);
        String prefix = path + "/";
        if (fileNameSet == null || fileNameSet.remove(path)) {
            return getFolderBuilder(entry, prefix, null);
        }
        if (namesWithPrefixPresent(prefix)) {
            return getFolderBuilder(entry, prefix, fileNameSet);
        }
        return null;
    }

    @Override
    public void save() {
        FileTools.setLastModified(targetFolder, lastModifiedTime);
    }

    private String getPath(PackEntry entry) {
        return prefixInArchive + entry.getName();
    }

    private FolderBuilder getFolderBuilder(PackEntry entry, String prefix, Set<String> fileNameSet) {
        File nativeFile = getNativeFile(entry.getName());
        System.out.println("Creating " + nativeFile);
        Preconditions.checkState(nativeFile.mkdir(), "Cannot create folder: %s", nativeFile);
        return new FsFolderBuilder(nativeFile, entry.getLastModifiedTime(), prefix, fileNameSet);
    }

    private boolean namesWithPrefixPresent(String prefix) {
        for (String fileName : fileNameSet) {
            if (fileName.startsWith(prefix)) return true;
        }
        return false;
    }

    private File getNativeFile(String name) {
        return targetFolder == null ? new File(name) : new File(targetFolder, name);
    }
}
