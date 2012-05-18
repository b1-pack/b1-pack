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

import org.b1.pack.api.common.FileBuilder;
import org.b1.pack.api.common.FolderBuilder;
import org.b1.pack.api.common.PackEntry;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

public class ListBuilder implements FolderBuilder {

    private final String namePrefix;
    private boolean needHeader;

    public ListBuilder(String namePrefix, boolean needHeader) {
        this.namePrefix = namePrefix;
        this.needHeader = needHeader;
    }

    @Override
    public FileBuilder addFile(PackEntry entry, Long size) throws IOException {
        printHeader();
        printInfo(namePrefix + entry.getName(), 'F', size, entry.getLastModifiedTime());
        return null;
    }

    private void printHeader() {
        if (needHeader) {
            needHeader = false;
            System.out.println();
            System.out.println("Name");
            System.out.println("Type             Size     Date       Time");
            printLine();
        }
    }

    @Override
    public FolderBuilder addFolder(PackEntry entry) throws IOException {
        String path = namePrefix + entry.getName();
        printInfo(path, 'D', null, entry.getLastModifiedTime());
        return new ListBuilder(path + File.separator, false);
    }

    @Override
    public void save() throws IOException {
        if (namePrefix.isEmpty()) {
            if (needHeader) {
                System.out.println("No files found");
            } else {
                printLine();
                System.out.println();
            }
        }
    }

    private static void printLine() {
        for (int i = 0; i < 80; i++) {
            System.out.print('-');
        }
        System.out.println();
    }

    private static void printInfo(String path, char type, @Nullable Long size, Long time) {
        System.out.println(path);
        System.out.print(type);
        if (size != null) {
            System.out.format("%20d", size);
        } else {
            System.out.format("%20c", ' ');
        }
        if (time != null) {
            System.out.format("  %tF  %1$tT", time);
        }
        System.out.println();
    }
}
