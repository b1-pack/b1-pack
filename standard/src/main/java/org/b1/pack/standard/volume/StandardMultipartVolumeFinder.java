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

import org.b1.pack.api.volume.VolumeFinder;
import org.b1.pack.api.volume.VolumeFinderProvider;

import java.util.LinkedList;

class StandardMultipartVolumeFinder extends VolumeFinder {

    private final VolumeFinderProvider provider;
    private final String prefix;
    private final String suffix;
    private final int digitCount;
    private final int minVolumeCount;

    public StandardMultipartVolumeFinder(VolumeFinderProvider provider, String prefix, String suffix, int digitCount, int minVolumeCount) {
        this.provider = provider;
        this.prefix = prefix;
        this.suffix = suffix;
        this.digitCount = digitCount;
        this.minVolumeCount = minVolumeCount;
    }

    @Override
    public String getVolumeName(long volumeNumber) {
        String n = Long.toString(volumeNumber);
        LinkedList<String> list = new LinkedList<String>();
        list.add(n);
        while (n.length() < digitCount) {
            list.add(n = '0' + n);
        }
        while (!list.isEmpty()) {
            String volumeName = prefix + list.removeLast() + suffix;
            if (provider.isVolumePresent(volumeName)) {
                return volumeName;
            }
        }
        return null;
    }

    @Override
    public int getVolumeCount() {
        int count = minVolumeCount;
        while (getVolumeName(count + 1) != null) {
            count++;
        }
        return count;
    }
}
