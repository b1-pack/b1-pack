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

import com.google.common.io.Files;
import org.b1.pack.api.common.PackException;
import org.b1.pack.api.maker.PackMaker;
import org.b1.pack.api.writer.PackWriter;
import org.b1.pack.api.writer.WriterCommand;
import org.b1.pack.api.writer.WriterPack;
import org.b1.pack.api.writer.WriterProvider;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class WriteCommand implements PackCommand {

    @Override
    public void execute(ArgSet argSet) throws IOException {
        ArgSet.checkParameter(argSet.getTypeFlag() == null, "Invalid type");
        System.out.println("Starting");
        Map<List<String>, FsObject> rootMap = FileTools.createRootMap(argSet.getFileNames());

        //todo
        WriterProvider provider = new FsWriterProvider(
                FileTools.getOutputFolder(argSet),
                argSet.getPackName(),
                argSet.isSplit() ? 1 : 0, argSet.getVolumeSize());
        PackWriter.getInstance(argSet.getTypeFormat()).write(provider, new WriterCommand() {
            @Override
            public void execute(WriterPack pack) throws IOException {

            }
        });

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
