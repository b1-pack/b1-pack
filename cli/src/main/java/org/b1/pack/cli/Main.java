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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import joptsimple.OptionException;
import org.b1.pack.api.common.PackException;

import java.io.IOException;
import java.io.InputStream;

public class Main {

    public static final ImmutableMap<String, PackCommand> COMAND_MAP =
            ImmutableMap.of("a", new AddCommand(), "l", new ListCommand(), "x", new ExtractCommand());

    public static void main(String[] args) throws IOException {
        try {
            ArgSet argSet = new ArgSet(args);
            if (argSet.isHelp()) {
                printHelp();
                ArgSet.checkParameter(argSet.getCommand() == null, "Command ignored");
                return;
            }
            ArgSet.checkParameter(argSet.getCommand() != null, "No command");
            PackCommand command = COMAND_MAP.get(argSet.getCommand());
            ArgSet.checkParameter(command != null, "Invalid command");
            command.execute(argSet);
        } catch (OptionException e) {
            printError(e);
        } catch (PackException e) {
            printError(e);
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
        System.err.println(e.getMessage());
        System.err.println();
        System.err.println("For help type: b1 -h");
    }
}
