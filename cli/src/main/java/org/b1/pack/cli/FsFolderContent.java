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
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.b1.pack.api.common.FolderBuilder;
import org.b1.pack.api.common.FolderContent;
import org.b1.pack.api.common.PackEntry;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FsFolderContent implements FolderContent {

    private final Map<List<String>, FolderBuilder> builderMap = Maps.newHashMap();
    private final Map<List<String>, File> fileMap;

    public FsFolderContent(List<String> names) {
        this(getFileMap(names));
    }

    public FsFolderContent(Map<List<String>, File> fileMap) {
        this.fileMap = fileMap;
    }

    @Override
    public void writeTo(FolderBuilder builder) throws IOException {
        Preconditions.checkState(builderMap.put(Collections.<String>emptyList(), builder) == null);
        for (Map.Entry<List<String>, File> entry : fileMap.entrySet()) {
            addFile(entry.getKey(), entry.getValue());
        }
    }

    private void addFile(List<String> path, File file) throws IOException {
        if (path.isEmpty()) {
            addChildren(builderMap.get(Collections.<String>emptyList()), file);
        } else {
            PackEntry entry = new FsPackEntry(Iterables.getLast(path), file.lastModified());
            if (file.isFile()) {
                getParentBuilder(path).addFile(entry, file.length()).setContent(new FsFileContent(file));
            } else {
                addChildren(getParentBuilder(path).addFolder(entry), file);
            }
        }
    }

    private FolderBuilder getParentBuilder(List<String> path) throws IOException {
        List<String> key = path.subList(0, path.size() - 1);
        FolderBuilder builder = builderMap.get(key);
        if (builder == null) {
            builder = getParentBuilder(key).addFolder(new FsPackEntry(Iterables.getLast(path), null));
            builderMap.put(key, builder);
        }
        return builder;
    }

    private static void addChildren(FolderBuilder builder, File folder) throws IOException {
        System.out.println("Adding " + folder);
        for (File file : Preconditions.checkNotNull(folder.listFiles(), "Cannot list %s", folder)) {
            PackEntry entry = new FsPackEntry(file.getName(), file.lastModified());
            if (file.isFile()) {
                builder.addFile(entry, file.length()).setContent(new FsFileContent(file));
            } else {
                addChildren(builder.addFolder(entry), file);
            }
        }
    }

    private static Map<List<String>, File> getFileMap(List<String> names) {
        Map<List<String>, File> fileMap = Maps.newLinkedHashMap();
        for (String name : names) {
            File file = new File(name);
            Preconditions.checkArgument(file.exists(), "File not found: %s", file);
            List<String> path = FileTools.getPath(file);
            Preconditions.checkArgument(fileMap.put(path, file) == null, "Duplicate path: %s", path);
        }
        return fileMap;
    }
}
