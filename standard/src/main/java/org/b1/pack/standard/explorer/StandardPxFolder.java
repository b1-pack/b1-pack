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

import org.b1.pack.api.explorer.PxFolder;
import org.b1.pack.api.explorer.PxVisitor;

import java.io.IOException;
import java.util.List;

public class StandardPxFolder implements PxFolder {

    private final RecordHeader header;
    private final List<String> path;

    public StandardPxFolder(RecordHeader header, List<String> path) {
        this.header = header;
        this.path = path;
    }

    @Override
    public List<String> getPath() {
        return path;
    }

    @Override
    public Long getLastModifiedTime() {
        return header.lastModifiedTime;
    }

    @Override
    public void accept(PxVisitor visitor) throws IOException {
        visitor.visit(this);
    }
}
