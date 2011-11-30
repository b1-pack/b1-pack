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

import org.b1.pack.api.explorer.PxProvider;
import org.b1.pack.api.explorer.PxVolume;

import java.io.File;
import java.io.IOException;

public class BasicProvider implements PxProvider {

    private final File packFile;

    public BasicProvider(File packFile) {
        this.packFile = packFile;
    }

    @Override
    public PxVolume getVolume(long number) {
        return number == 1 ? new FsPxVolume(packFile) : null;
    }

    @Override
    public long getVolumeCount() {
        return 1;
    }

    @Override
    public void close() throws IOException {
        // no-op
    }
}
