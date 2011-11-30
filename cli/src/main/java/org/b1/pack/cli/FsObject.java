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

import java.io.File;
import java.util.List;

public class FsObject {

    private File file;
    private List<String> path;

    public FsObject(File file, List<String> path) {
        this.path = path;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public List<String> getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FsObject && ((FsObject) o).path.equals(path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
