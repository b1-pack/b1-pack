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

import com.google.common.collect.Maps;
import org.b1.pack.api.writer.WriterCommand;
import org.b1.pack.api.writer.WriterEntry;
import org.b1.pack.api.writer.WriterPack;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FsWriterCommand implements WriterCommand {

    private final Map<List<String>, FsWriterEntry> baseEntryMap = Maps.newHashMap();
    private final Map<List<String>, File> baseFileMap;

    public FsWriterCommand(Map<List<String>, File> baseFileMap) {
        this.baseFileMap = baseFileMap;
    }

    @Override
    public void execute(WriterPack pack) throws IOException {
        for (Map.Entry<List<String>, File> entry : baseFileMap.entrySet()) {
            List<String> path = entry.getKey();
            File file = entry.getValue();
            if (path.isEmpty()) {
                addChildren(pack, null, file);
            } else {
                addFile(pack, getBaseEntry(pack, getParent(path)), getName(path), file);
            }
        }
    }

    private void addChildren(WriterPack pack, FsWriterEntry entry, File file) throws IOException {
        for (File child : file.listFiles()) {
            addFile(pack, entry, child.getName(), child);
        }
    }

    private void addFile(WriterPack pack, WriterEntry parent, String name, File file) throws IOException {
        FsWriterEntry entry = new FsWriterEntry(parent, name, file.lastModified());
        if (file.isFile()) {
            pack.addFile(entry, new FsWriterContent(file));
        } else {
            pack.addFolder(entry);
            addChildren(pack, entry, file);
        }
    }

    private WriterEntry getBaseEntry(WriterPack pack, List<String> path) throws IOException {
        if (path.isEmpty()) {
            return null;
        }
        FsWriterEntry result = baseEntryMap.get(path);
        if (result == null) {
            result = new FsWriterEntry(getBaseEntry(pack, getParent(path)), getName(path), null);
            pack.addFolder(result);
            baseEntryMap.put(path, result);
        }
        return result;
    }

    private static String getName(List<String> path) {
        return path.get(path.size() - 1);
    }

    private static List<String> getParent(List<String> path) {
        return path.subList(0, path.size() - 1);
    }
}
