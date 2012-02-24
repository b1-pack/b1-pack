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

import org.b1.pack.api.maker.PmProvider;
import org.b1.pack.api.maker.PmVolume;

import java.io.File;

public class FsPmProvider extends PmProvider {

    private VolumeNameExpert nameExpert;
    private final long volumeSize;

    public FsPmProvider(File outputFolder, String packName, int volumeCount, long maxVolumeSize) {
        this.nameExpert = new VolumeNameExpert(outputFolder, packName, volumeCount);
        this.volumeSize = maxVolumeSize;
    }

    @Override
    public PmVolume getVolume(long number) {
        return new FsPmVolume(nameExpert.getVolumeFile(number), volumeSize);
    }
}
