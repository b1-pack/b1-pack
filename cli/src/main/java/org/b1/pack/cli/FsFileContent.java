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

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import org.b1.pack.api.common.FileContent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FsFileContent implements FileContent {

    private final File file;

    public FsFileContent(File file) {
        this.file = file;
    }

    @Override
    public void writeTo(OutputStream stream, long start, Long end) throws IOException {
        System.out.println("Adding " + file);
        long length = (end != null ? end : file.length()) - start;
        InputSupplier<InputStream> slice = ByteStreams.slice(Files.newInputStreamSupplier(file), start, length);
        Preconditions.checkState(ByteStreams.copy(slice, stream) == length);
    }
}
