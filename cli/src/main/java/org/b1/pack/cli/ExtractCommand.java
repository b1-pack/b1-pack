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

import com.google.common.collect.Sets;
import org.b1.pack.api.reader.PackReader;
import org.b1.pack.api.volume.VolumeFinder;
import org.b1.pack.api.volume.VolumeService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;

public class ExtractCommand implements PackCommand {

    @Override
    public void execute(ArgSet argSet) throws IOException {
        File file = new File(argSet.getPackName());
        if (!file.isFile()) {
            throw new FileNotFoundException("File not found: " + file);
        }
        final File outputFolder = FileTools.getOutputFolder(argSet);
        System.out.println("Extracting from \"" + file +
                "\" to \"" + (outputFolder != null ? outputFolder.getPath() : ".") + "\".");
        System.out.println();
        File parentFolder = file.getParentFile();
        VolumeFinder volumeFinder = VolumeService.getInstance(argSet.getTypeFormat())
                .createVolumeFinder(new FsVolumeFinderProvider(parentFolder, file.getName()));
        HashSet<String> fileNameSet = argSet.getFileNames().isEmpty() ? null : Sets.newHashSet(argSet.getFileNames());
        FsFolderBuilder folderBuilder = new FsFolderBuilder(outputFolder, null, "", fileNameSet);
        PackReader.getInstance(argSet.getTypeFormat()).read(new FsReaderProvider(folderBuilder, parentFolder, volumeFinder, argSet.getPassword()));
        if (fileNameSet != null && !fileNameSet.isEmpty()) {
            throw new IllegalStateException("Cannot extract: " + fileNameSet);
        }
        System.out.println();
        System.out.println("Done");
    }
}
