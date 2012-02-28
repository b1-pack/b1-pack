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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;

public class Main {

    public static final ImmutableMap<String, PackCommand> COMAND_MAP =
            ImmutableMap.of("a", new AddCommand(), "l", new ListCommand(), "x", new ExtractCommand());

    public static void main(String[] args) throws Exception {
        try {
            ArgSet argSet = new ArgSet(args);
            String command = argSet.getCommand();
            if (argSet.isHelp()) {
                printHelp();
                Preconditions.checkArgument(command == null, "Command ignored");
            } else {
                Preconditions.checkNotNull(command, "No command");
                Preconditions.checkNotNull(COMAND_MAP.get(command), "Invalid command: %s", command).execute(argSet);
            }
        } catch (Exception e) {
            printError(e);
            if (Boolean.getBoolean(Main.class.getName() + ".debug")) {
                throw e;
            } else {
                System.exit(1);
            }
        }
    }

    private static void printHelp() throws IOException {
        InputStream stream = Preconditions.checkNotNull(Main.class.getResourceAsStream("help.txt"));
        try {
            ByteStreams.copy(stream, System.out);
        } finally {
            stream.close();
        }
    }

    private static void printError(Exception e) {
        System.err.println();
        System.err.print("Error: ");
        System.err.println(Objects.firstNonNull(e.getMessage(), e.getClass().getSimpleName()));
        System.err.println();
        System.err.println("For help type: b1 -h");
    }
}
