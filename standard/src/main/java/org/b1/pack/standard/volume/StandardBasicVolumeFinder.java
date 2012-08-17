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

class StandardBasicVolumeFinder extends VolumeFinder {

    private final String volumeName;

    public StandardBasicVolumeFinder(String volumeName) {
        this.volumeName = volumeName;
    }

    @Override
    public String getVolumeName(long volumeNumber) {
        return volumeNumber == 1 ? volumeName : null;
    }

    @Override
    public int getVolumeCount() {
        return 1;
    }
}
