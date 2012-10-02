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
import org.b1.pack.api.common.FolderBuilder;
import org.b1.pack.api.reader.ReaderProvider;
import org.b1.pack.api.reader.ReaderVolume;
import org.b1.pack.api.volume.VolumeFinder;

import java.io.Console;
import java.io.File;

public class FsReaderProvider extends ReaderProvider {

    private final FolderBuilder folderBuilder;
    private final File parentFolder;
    private final VolumeFinder volumeFinder;
    private final String password;

    public FsReaderProvider(FolderBuilder folderBuilder, File parentFolder, VolumeFinder volumeFinder, String password) {
        this.folderBuilder = folderBuilder;
        this.parentFolder = parentFolder;
        this.volumeFinder = volumeFinder;
        this.password = password;
    }

    @Override
    public FolderBuilder getFolderBuilder() {
        return folderBuilder;
    }

    @Override
    public ReaderVolume getVolume(long number) {
        return new FsReaderVolume(new File(parentFolder, volumeFinder.getVolumeName(number)));
    }

    @Override
    public long getVolumeCount() {
        return volumeFinder.getVolumeCount();
    }

    @Override
    public char[] getPassword() {
        if (password != null) {
            return password.toCharArray();
        }
        Console console = Preconditions.checkNotNull(System.console(), "Console is not available for password input");
        return console.readPassword("Enter password for decryption (will not be echoed): ");
    }
}
