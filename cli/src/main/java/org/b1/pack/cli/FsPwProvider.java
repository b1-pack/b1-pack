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

import org.b1.pack.api.writer.PwProvider;
import org.b1.pack.api.writer.PwVolume;

import java.io.File;

public class FsPwProvider extends PwProvider {

    private final File outputFolder;
    private final String packName;
    private final long volumeSize;

    public FsPwProvider(File outputFolder, String packName, long volumeSize) {
        this.outputFolder = outputFolder;
        this.packName = packName;
        this.volumeSize = volumeSize;
    }

    @Override
    public String getPackName() {
        return packName;
    }

    @Override
    public long getExpectedVolumeCount() {
        return volumeSize == 0 ? 0 : 1;
    }

    @Override
    public PwVolume getVolume(String name) {
        return new FsPwVolume(new File(outputFolder, name), volumeSize);
    }
}
