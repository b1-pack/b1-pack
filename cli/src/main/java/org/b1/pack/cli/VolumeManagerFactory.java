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

import org.b1.pack.api.explorer.ExplorerProvider;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VolumeManagerFactory {

    private static final Pattern PATTERN = Pattern.compile("(?i)(.*\\.part)(\\d+)(.b1)");

    public static ExplorerProvider createVolumeManager(File packFile) {
        Matcher matcher = PATTERN.matcher(packFile.getName());
        if (!matcher.matches()) {
            return new BasicExplorerProvider(packFile);
        }
        String prefix = matcher.group(1);
        String number = matcher.group(2);
        String suffix = matcher.group(3);
        return new MultipartExplorerProvider(prefix, suffix, number.length(), Integer.parseInt(number));
    }

}
