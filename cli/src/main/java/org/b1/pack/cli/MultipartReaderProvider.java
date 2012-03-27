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

import org.b1.pack.api.reader.ReaderProvider;
import org.b1.pack.api.reader.ReaderVolume;

import java.io.File;
import java.util.LinkedList;

public class MultipartReaderProvider extends ReaderProvider {

    private final String prefix;
    private final String suffix;
    private final int digitCount;
    private final int minVolumeCount;

    public MultipartReaderProvider(String prefix, String suffix, int digitCount, int minVolumeCount) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.digitCount = digitCount;
        this.minVolumeCount = minVolumeCount;
    }

    @Override
    public ReaderVolume getVolume(long number) {
        String n = Long.toString(number);
        LinkedList<String> list = new LinkedList<String>();
        list.add(n);
        while (n.length() < digitCount) {
            list.add(n = '0' + n);
        }
        while (!list.isEmpty()) {
            File file = new File(prefix + list.removeLast() + suffix);
            if (file.isFile()) {
                return new FsReaderVolume(file);
            }
        }
        return null;
    }

    @Override
    public long getVolumeCount() {
        int count = minVolumeCount;
        while (getVolume(count + 1) != null) {
            count++;
        }
        return count;
    }
}
