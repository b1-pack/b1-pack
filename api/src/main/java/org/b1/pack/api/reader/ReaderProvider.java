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

package org.b1.pack.api.reader;

import org.b1.pack.api.common.FolderBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ReaderProvider {

    public abstract FolderBuilder getFolderBuilder();

    public abstract ReaderVolume getVolume(long number);

    public abstract long getVolumeCount();

    public ExecutorService getExecutorService() {
        return Executors.newCachedThreadPool();
    }

    public char[] getPassword() {
        return null;
    }
}
