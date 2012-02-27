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

package org.b1.pack.cli;

import org.b1.pack.api.writer.WriterEntry;

public class FsWriterEntry extends WriterEntry {

    private final WriterEntry parent;
    private final String name;
    private final Long lastModifiedTime;

    public FsWriterEntry(WriterEntry parent, String name, Long lastModifiedTime) {
        this.parent = parent;
        this.name = name;
        this.lastModifiedTime = lastModifiedTime;
    }

    @Override
    public WriterEntry getParent() {
        return parent;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Long getLastModifiedTime() {
        return lastModifiedTime;
    }

    @Override
    public void beforeAdd() {
        System.out.print("Adding ");
        printName(this);
        System.out.println();
    }

    private static void printName(WriterEntry entry) {
        WriterEntry parent = entry.getParent();
        if (parent != null) {
            printName(parent);
            System.out.print('/');
        }
        System.out.print(entry.getName());
    }
}
