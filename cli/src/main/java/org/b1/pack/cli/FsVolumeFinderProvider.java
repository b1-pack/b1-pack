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

import org.b1.pack.api.volume.VolumeFinderProvider;

import java.io.File;

public class FsVolumeFinderProvider extends VolumeFinderProvider {

    private final File parentFolder;
    private final String volumeName;

    public FsVolumeFinderProvider(File parentFolder, String volumeName) {
        this.parentFolder = parentFolder;
        this.volumeName = volumeName;
    }

    @Override
    public String getVolumeName() {
        return volumeName;
    }

    @Override
    public boolean isVolumePresent(String volumeName) {
        return new File(parentFolder, volumeName).isFile();
    }
}
