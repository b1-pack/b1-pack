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

import org.b1.pack.api.writer.WriterProvider;
import org.b1.pack.api.writer.WriterVolume;

import java.io.File;
import java.io.IOException;

public class FsWriterProvider extends WriterProvider {

    private final VolumeNameExpert nameExpert;
    private final long maxVolumeSize;

    public FsWriterProvider(File outputFolder, String packName, int volumeCount, long maxVolumeSize) {
        this.nameExpert = new VolumeNameExpert(outputFolder, packName, volumeCount);
        this.maxVolumeSize = maxVolumeSize;
    }

    @Override
    public boolean isSeekable() {
        return true;
    }

    @Override
    public WriterVolume getVolume(long number) throws IOException {
        return new FsWriterVolume(nameExpert.getVolumeFile(number));
    }

    @Override
    public long getMaxVolumeSize() {
        return maxVolumeSize;
    }

    @Override
    public long getMaxVolumeCount() {
        return maxVolumeSize == Long.MAX_VALUE ? 1 : Integer.MAX_VALUE;
    }
}
