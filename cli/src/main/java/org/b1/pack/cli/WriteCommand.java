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
import com.google.common.collect.Maps;
import org.b1.pack.api.compression.CompressionMethod;
import org.b1.pack.api.compression.LzmaCompressionMethod;
import org.b1.pack.api.writer.PackWriter;
import org.b1.pack.api.writer.WriterFolderContent;
import org.b1.pack.api.writer.WriterProvider;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class WriteCommand implements PackCommand {

    @Override
    public void execute(ArgSet argSet) throws IOException {
        WriterProvider provider = new FsWriterProvider(
                new VolumeNameExpert(FileTools.getOutputFolder(argSet), argSet.getPackName(), argSet.isSplit() ? 1 : 0),
                argSet.getVolumeSize(),
                isSeekable(argSet.getTypeFlag()),
                getCompressionMethod(argSet.getCompressionMethod()));
        System.out.println("Starting");
        PackWriter.getInstance(argSet.getTypeFormat()).write(provider, getFolderContent(argSet.getFileNames()));
        System.out.println();
        System.out.println("Done");
    }

    private CompressionMethod getCompressionMethod(String method) {
        return method == null ? null : new LzmaCompressionMethod();
    }

    private boolean isSeekable(String typeFlag) {
        if ("stream".equals(typeFlag)) {
            return false;
        }
        Preconditions.checkArgument(typeFlag == null, "Invalid type flag: %s", typeFlag);
        return true;
    }

    private WriterFolderContent getFolderContent(List<String> names) {
        LinkedHashMap<List<String>, File> fileMap = Maps.newLinkedHashMap();
        for (String name : names.isEmpty() ? Collections.singleton(".") : names) {
            File file = new File(name);
            Preconditions.checkArgument(file.exists(), "File not found: %s", file);
            List<String> path = FileTools.getPath(file);
            Preconditions.checkArgument(fileMap.put(path, file) == null, "Duplicate path: %s", path);
        }
        return new FsFolderContent(fileMap);
    }
}
