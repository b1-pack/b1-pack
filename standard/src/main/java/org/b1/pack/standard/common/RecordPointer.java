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

package org.b1.pack.standard.common;

public class RecordPointer {

    public final long volumeNumber;
    public final long blockOffset;
    public final long recordOffset;

    public RecordPointer(long volumeNumber, long blockOffset, long recordOffset) {
        this.volumeNumber = volumeNumber;
        this.blockOffset = blockOffset;
        this.recordOffset = recordOffset;
    }
}
