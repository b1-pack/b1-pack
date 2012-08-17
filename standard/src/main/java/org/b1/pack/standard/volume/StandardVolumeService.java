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
package org.b1.pack.standard.volume;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.b1.pack.api.volume.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.b1.pack.api.common.PackFormat.B1;

public class StandardVolumeService extends VolumeService {

    private static final Pattern ALLOCATOR_PATTERN = Pattern.compile("(?i)(.*?)(\\.b1)?");
    private static final Pattern FINDER_PATTERN = Pattern.compile("(?i)(.*\\.part)(\\d+)(\\.b1)");

    @Override
    public VolumeAllocator createVolumeAllocator(VolumeAllocatorProvider provider) {
        String volumeName = provider.getVolumeName();
        long volumeCount = provider.getVolumeCount();
        Matcher matcher = ALLOCATOR_PATTERN.matcher(volumeName);
        Preconditions.checkState(matcher.matches());
        String baseName = matcher.group(1);
        String extension = Objects.firstNonNull(matcher.group(2), ".b1");
        return volumeCount == 0
                ? new StandardBasicVolumeAllocator(baseName + extension)
                : new StandardMultipartVolumeAllocator(baseName, volumeCount, extension);
    }

    @Override
    public VolumeFinder createVolumeFinder(VolumeFinderProvider provider) {
        String volumeName = provider.getVolumeName();
        Matcher matcher = FINDER_PATTERN.matcher(volumeName);
        if (!matcher.matches()) {
            return new StandardBasicVolumeFinder(volumeName);
        }
        String prefix = matcher.group(1);
        String number = matcher.group(2);
        String suffix = matcher.group(3);
        return new StandardMultipartVolumeFinder(provider, prefix, suffix, number.length(), Integer.parseInt(number));
    }

    @Override
    protected boolean isFormatSupported(String format) {
        return B1.equals(format);
    }
}
