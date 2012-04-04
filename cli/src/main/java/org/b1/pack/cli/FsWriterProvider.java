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

import com.google.common.base.Objects;
import org.b1.pack.api.compression.CompressionMethod;
import org.b1.pack.api.writer.WriterProvider;
import org.b1.pack.api.writer.WriterVolume;

import java.io.File;
import java.io.IOException;

public class FsWriterProvider extends WriterProvider {

    private final VolumeNameExpert volumeNameExpert;
    private final long maxVolumeSize;
    private boolean seekable = true;
    private CompressionMethod compressionMethod;

    public FsWriterProvider(File outputFolder, String packName, Long maxVolumeSize) {
        this.volumeNameExpert = new VolumeNameExpert(outputFolder, packName, maxVolumeSize != null ? 1 : 0);
        this.maxVolumeSize = Objects.firstNonNull(maxVolumeSize, Long.MAX_VALUE);
    }

    @Override
    public boolean isSeekable() {
        return seekable;
    }

    @Override
    public WriterVolume getVolume(long number) throws IOException {
        return new FsWriterVolume(volumeNameExpert.getVolumeFile(number));
    }

    @Override
    public long getMaxVolumeSize() {
        return maxVolumeSize;
    }

    @Override
    public CompressionMethod getCompressionMethod() {
        return compressionMethod;
    }

    public void setSeekable(boolean seekable) {
        this.seekable = seekable;
    }

    public void setCompressionMethod(CompressionMethod compressionMethod) {
        this.compressionMethod = compressionMethod;
    }
}
