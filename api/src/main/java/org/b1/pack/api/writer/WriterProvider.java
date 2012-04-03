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

package org.b1.pack.api.writer;

import org.b1.pack.api.compression.CompressionMethod;
import org.b1.pack.api.encryption.EncryptionMethod;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class WriterProvider {

    public abstract boolean isSeekable();

    public abstract WriterVolume getVolume(long number) throws IOException;

    public long getMaxVolumeSize() {
        return Long.MAX_VALUE;
    }

    public long getMaxVolumeCount() {
        return Integer.MAX_VALUE;
    }

    public CompressionMethod getCompressionMethod() {
        return null;
    }

    public EncryptionMethod getEncryptionMethod() {
        return null;
    }

    public ExecutorService getExecutorService() {
        return Executors.newCachedThreadPool();
    }
}
