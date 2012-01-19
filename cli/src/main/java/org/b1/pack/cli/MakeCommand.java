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
import org.b1.pack.api.maker.PackMaker;
import org.b1.pack.api.maker.PmFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

public class MakeCommand implements PackCommand {

    @Override
    public void execute(ArgSet argSet) throws IOException {
        System.out.println("Starting");
        File outputFolder = FileTools.getOutputFolder(argSet);
        Set<FsObject> fsObjects = FileTools.getFsObjects(argSet.getFileNames());
        FsPmProvider provider = new FsPmProvider(outputFolder, argSet.getPackName(), argSet.getVolumeSize());
        PackMaker maker = PmFactory.newInstance(argSet.getTypeFormat()).createPackMaker(provider);
        try {
            for (FsObject fsObject : fsObjects) {
                addObject(maker, fsObject);
            }
        } finally {
            maker.close();
        }
        System.out.println();
        System.out.println("Done");
    }

    private void addObject(PackMaker maker, FsObject fsObject) throws IOException {
        File file = fsObject.getFile();
        System.out.println("Adding " + file);
        if (file.isFile()) {
            addFile(maker, fsObject);
        } else if (file.isDirectory()) {
            maker.addFolder(new FsPmFolder(fsObject));
        } else {
            throw new PackException("Not found: " + file);
        }
    }

    private void addFile(PackMaker maker, FsObject fsObject) throws IOException {
        OutputStream stream = maker.addFile(new FsPmFile(fsObject));
        try {
            Files.copy(fsObject.getFile(), stream);
        } finally {
            stream.close();
        }
    }
}
