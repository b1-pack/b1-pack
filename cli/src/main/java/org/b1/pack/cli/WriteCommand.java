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

import com.google.common.io.Files;
import org.b1.pack.api.common.PackException;
import org.b1.pack.api.writer.PackWriter;
import org.b1.pack.api.writer.PwFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

public class WriteCommand implements PackCommand {

    @Override
    public void execute(ArgSet argSet) throws IOException {
        System.out.println("Starting");
        File outputFolder = FileTools.getOutputFolder(argSet);
        Set<FsObject> fsObjects = FileTools.getFsObjects(argSet.getFileNames());
        FsPwProvider provider = new FsPwProvider(outputFolder, argSet.getPackName(), argSet.getVolumeSize());
        PackWriter writer = PwFactory.newInstance(argSet.getTypeFormat()).createPackWriter(provider);
        try {
            for (FsObject fsObject : fsObjects) {
                addObject(writer, fsObject);
            }
        } finally {
            writer.close();
        }
        System.out.println();
        System.out.println("Done");
    }

    private void addObject(PackWriter writer, FsObject fsObject) throws IOException {
        File file = fsObject.getFile();
        System.out.println("Adding " + file);
        if (file.isFile()) {
            addFile(writer, fsObject);
        } else if (file.isDirectory()) {
            writer.addFolder(new FsPwFolder(fsObject));
        } else {
            throw new PackException("Not found: " + file);
        }
    }

    private void addFile(PackWriter writer, FsObject fsObject) throws IOException {
        OutputStream stream = writer.addFile(new FsPwFile(fsObject));
        try {
            Files.copy(fsObject.getFile(), stream);
        } finally {
            stream.close();
        }
    }
}
