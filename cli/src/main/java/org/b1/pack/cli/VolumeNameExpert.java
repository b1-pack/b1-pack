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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VolumeNameExpert {

    private static final Pattern NAME_PATTERN = Pattern.compile("(.*?)(\\.[bB]1)?");

    private final File outputFolder;
    private final String baseName;
    private final String extension;
    private final String format;

    public VolumeNameExpert(File outputFolder, String packName, long volumeCount) {
        this.outputFolder = outputFolder;
        Matcher matcher = NAME_PATTERN.matcher(packName);
        Preconditions.checkState(matcher.matches());
        baseName = matcher.group(1);
        extension = Objects.firstNonNull(matcher.group(2), ".b1");
        format = volumeCount == 0 ? null : "%0" + (String.valueOf(volumeCount).length() + 1) + "d";
    }

    public File getVolumeFile(long volumeNumber) {
        StringBuilder builder = new StringBuilder(baseName);
        if (format != null) {
            builder.append(".part").append(String.format(format, volumeNumber));
        } else {
            Preconditions.checkArgument(volumeNumber == 1);
        }
        return new File(outputFolder, builder.append(extension).toString());
    }
}
