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

import com.google.common.base.Preconditions;
import org.b1.pack.api.builder.BuilderPack;
import org.b1.pack.api.builder.PbFactory;
import org.b1.pack.api.builder.PbProvider;
import org.b1.pack.api.builder.PbVolume;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class BuildCommand implements PackCommand {

    @Override
    public void execute(final ArgSet argSet) throws IOException {
        System.out.println("Starting");
        File outputFolder = FileTools.getOutputFolder(argSet);
        Set<FsObject> fsObjects = FileTools.getFsObjects(argSet.getFileNames());
        BuilderPack builderPack = PbFactory.newInstance(argSet.getTypeFormat()).createBuilderPack(new PbProvider() {
            @Override
            public long getMaxVolumeSize() {
                return argSet.getVolumeSize();
            }
        });
        for (FsObject fsObject : fsObjects) {
            File file = fsObject.getFile();
            if (file.isFile()) {
                builderPack.addFile(new FsPbFile(fsObject));
            } else if (file.isDirectory()) {
                builderPack.addFolder(new FsPbFolder(fsObject));
            } else {
                throw new IllegalArgumentException("Not found: " + file);
            }
        }
        List<PbVolume> volumes = builderPack.getVolumes();
        VolumeNameExpert expert = new VolumeNameExpert(outputFolder, argSet.getPackName(), argSet.isSplit() ? volumes.size() : 0);
        for (int i = 0, volumesSize = volumes.size(); i < volumesSize; i++) {
            buildVolume(expert.getVolumeFile(i + 1), volumes.get(i));
        }
        System.out.println();
        System.out.println("Done");
    }

    private void buildVolume(File file, PbVolume volume) throws IOException {
        System.out.println();
        System.out.println("Creating volume " + file);
        System.out.println();
        Preconditions.checkState(!file.exists(), "File already exists: %s", file);
        FileTools.saveToFile(volume, file);
    }
}
