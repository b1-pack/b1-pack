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
import org.b1.pack.api.writer.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class WriteCommand implements PackCommand {

    @Override
    public void execute(ArgSet argSet) throws IOException {
        ArgSet.checkParameter(argSet.getTypeFlag() == null, "Invalid type");
        System.out.println("Starting");
        WriterProvider provider = new FsWriterProvider(
                FileTools.getOutputFolder(argSet),
                argSet.getPackName(),
                argSet.isSplit() ? 1 : 0, argSet.getVolumeSize());
        final Map<List<String>, FsObject> rootMap = FileTools.createRootMap(argSet.getFileNames());
        final Map<List<String>, WriterEntry> entryMap = Maps.newHashMap();
        PackWriter.getInstance(argSet.getTypeFormat()).write(provider, new WriterCommand() {
            @Override
            public void execute(WriterPack pack) throws IOException {
                for (FsObject fsObject : rootMap.values()) {
                    addEntry(pack, getParent(pack, entryMap, fsObject.getPath()), fsObject.getFile());
                }
            }
        });
        System.out.println();
        System.out.println("Done");
    }

    private WriterEntry getParent(WriterPack pack, Map<List<String>, WriterEntry> entryMap, List<String> path) {

    }
    
    private void addEntry(WriterPack pack, WriterEntry parent, File file) throws IOException {
        FsWriterEntry entry = new FsWriterEntry(parent, file.getName(), file.lastModified());
        if (file.isFile()) {
            pack.addFile(entry, new FsWriterContent(file));
        } else if (file.isDirectory()) {
            pack.addFolder(entry);
            for (File child : file.listFiles()) {
                addEntry(pack, entry, child);
            }
        }
    }
}
