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
import org.b1.pack.api.common.CompressionMethod;
import org.b1.pack.api.writer.PackWriter;

import java.io.IOException;

public class WriteCommand implements PackCommand {

    @Override
    public void execute(ArgSet argSet) throws IOException {
        System.out.println("Starting");
        FsWriterProvider provider = new FsWriterProvider(
                FileTools.getOutputFolder(argSet), argSet.getPackName(), argSet.getMaxVolumeSize());
        provider.setSeekable(isSeekable(argSet.getTypeFlag()));
        if (argSet.getCompression() != null) {
            provider.setCompressionMethod(new CompressionMethod(argSet.getCompression()));
        }
        if (argSet.getEncryptionName() != null) {
            provider.setEncryptionMethod(new FsEncryptionMethod(argSet.getEncryptionName(), argSet.getPassword(), argSet.getIterationCount()));
        } else {
            Preconditions.checkArgument(argSet.getPassword() == null, "No encryption method specified");
        }
        PackWriter writer = PackWriter.getInstance(argSet.getTypeFormat());
        writer.write(provider, FileTools.createFolderContent(argSet.getFileNames()));
        System.out.println();
        System.out.println("Done");
    }

    private static boolean isSeekable(String typeFlag) {
        if ("stream".equals(typeFlag)) return false;
        Preconditions.checkArgument(typeFlag == null, "Invalid type flag: %s", typeFlag);
        return true;
    }
}
