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

import org.b1.pack.api.common.CompressionMethod;
import org.b1.pack.api.common.EncryptionMethod;
import org.b1.pack.api.common.FolderContent;
import org.b1.pack.api.volume.VolumeAllocator;
import org.b1.pack.api.writer.WriterProvider;
import org.b1.pack.api.writer.WriterVolume;

import java.io.File;
import java.io.IOException;

public class FsWriterProvider extends WriterProvider {

    private final FolderContent folderContent;
    private final File outputFolder;
    private final VolumeAllocator volumeAllocator;
    private final long maxVolumeSize;
    private boolean seekable = true;
    private CompressionMethod compressionMethod;
    private EncryptionMethod encryptionMethod;

    public FsWriterProvider(FolderContent folderContent, File outputFolder, VolumeAllocator volumeAllocator, Long maxVolumeSize) {
        this.folderContent = folderContent;
        this.outputFolder = outputFolder;
        this.volumeAllocator = volumeAllocator;
        this.maxVolumeSize = maxVolumeSize;
    }

    @Override
    public FolderContent getFolderContent() {
        return folderContent;
    }

    @Override
    public boolean isSeekable() {
        return seekable;
    }

    @Override
    public WriterVolume getVolume(long number) throws IOException {
        String name = volumeAllocator.getVolumeName(number);
        return new FsWriterVolume(outputFolder == null ? new File(name) : new File(outputFolder, name));
    }

    @Override
    public long getMaxVolumeSize() {
        return maxVolumeSize;
    }

    @Override
    public CompressionMethod getCompressionMethod() {
        return compressionMethod;
    }

    public EncryptionMethod getEncryptionMethod() {
        return encryptionMethod;
    }

    public void setSeekable(boolean seekable) {
        this.seekable = seekable;
    }

    public void setCompressionMethod(CompressionMethod compressionMethod) {
        this.compressionMethod = compressionMethod;
    }

    public void setEncryptionMethod(EncryptionMethod encryptionMethod) {
        this.encryptionMethod = encryptionMethod;
    }
}
