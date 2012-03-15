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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.b1.pack.api.explorer.*;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ListCommand implements PackCommand {

    @Override
    public void execute(ArgSet argSet) throws IOException {
        Preconditions.checkArgument(argSet.getFileNames().isEmpty(), "Filters not supported");
        File file = new File(argSet.getPackName());
        System.out.println("Listing " + file);
        System.out.println();
        System.out.println("Name");
        System.out.println("Type             Size     Date       Time");
        printLine();
        ExplorerPack explorerPack = PxFactory.newInstance(argSet.getTypeFormat()).createPackExplorer(VolumeManagerFactory.createVolumeManager(file));
        try {
            explorerPack.listObjects(new ListVisitor());
        } finally {
            explorerPack.close();
        }
        printLine();
        System.out.println();
        System.out.println("Done");
    }

    private static void printLine() {
        for (int i = 0; i < 80; i++) {
            System.out.print('-');
        }
        System.out.println();
    }

    private static void printInfo(List<String> path, char type, @Nullable Long size, Long time) {
        System.out.println(Joiner.on(File.separator).join(path));
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

    private static class ListVisitor implements ExplorerVisitor {
        @Override
        public void visit(ExplorerFolder folder) {
            printInfo(folder.getPath(), 'D', null, folder.getLastModifiedTime());
        }

        @Override
        public void visit(ExplorerFile file) {
            printInfo(file.getPath(), 'F', file.getSize(), file.getLastModifiedTime());
        }
    }
}
