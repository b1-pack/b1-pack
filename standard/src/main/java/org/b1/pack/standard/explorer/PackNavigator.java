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

package org.b1.pack.standard.explorer;

import org.b1.pack.standard.common.InputStreamWrapper;
import org.b1.pack.standard.common.RecordPointer;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkState;

public class PackNavigator implements Closeable {

    private final RecordInputStream recordStream;
    private boolean streamOpen;

    public PackNavigator(VolumeManager volumeManager) {
        recordStream = new RecordInputStream(volumeManager);
    }

    public InputStream getRecordStream(RecordPointer pointer) throws IOException {
        checkState(!streamOpen, "Only one stream can be open at a time");
        recordStream.seek(pointer);
        streamOpen = true;
        return new InputStreamWrapper<RecordInputStream>(recordStream) {
            @Override
            public void close() throws IOException {
                stream = null;
                streamOpen = false;
            }
        };
    }

    @Override
    public void close() throws IOException {
        recordStream.close();
    }
}
