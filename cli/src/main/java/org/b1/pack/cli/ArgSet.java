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
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.b1.pack.api.common.PackException;
import org.b1.pack.api.common.PackService;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgSet {

    public static final Pattern SIZE_PATTERN = Pattern.compile("(\\d+)([kMG]?B)");
    public static final Pattern TYPE_PATTERN = Pattern.compile("(.*?)(?:;(.))?");
    public static final ImmutableMap<String, Integer> SIZE_MULTIPLIERS =
            ImmutableMap.of("B", 1, "kB", 1000, "MB", 1000 * 1000, "GB", 1000 * 1000 * 1000);

    private String command;
    private String packName;
    private List<String> fileNames;
    private long volumeSize;
    private String typeFormat = PackService.B1;
    private String typeFlag;
    private String outputDirectory;
    private String compressionMethod;
    private boolean help;
    private boolean password;

    public ArgSet(String[] args) {
        OptionParser parser = new OptionParser();
        OptionSpec<String> volumeOption = parser.accepts("v").withRequiredArg();
        OptionSpec<String> typeOption = parser.accepts("type").withRequiredArg();
        OptionSpec<String> outputOption = parser.accepts("o").withRequiredArg();
        OptionSpec<String> compressionOption = parser.accepts("m").withRequiredArg();
        OptionSpec helpOption = parser.acceptsAll(Arrays.asList("?", "h", "help"));
        OptionSpec passwordOption = parser.accepts("p");
        OptionSet optionSet = parser.parse(args);
        LinkedList<String> arguments = new LinkedList<String>(optionSet.nonOptionArguments());
        command = arguments.pollFirst();
        packName = arguments.pollFirst();
        fileNames = arguments;
        initVolumeSize(optionSet.valueOf(volumeOption));
        initType(optionSet.valueOf(typeOption));
        outputDirectory = optionSet.valueOf(outputOption);
        compressionMethod = optionSet.valueOf(compressionOption);
        help = optionSet.has(helpOption);
        password = optionSet.has(passwordOption);
    }

    public static void checkParameter(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new PackException(errorMessage.toString());
        }
    }

    private void initVolumeSize(String size) {
        if (size == null) return;
        Matcher matcher = SIZE_PATTERN.matcher(size);
        checkParameter(matcher.matches(), "Invalid volume size");
        volumeSize = Long.parseLong(matcher.group(1)) * SIZE_MULTIPLIERS.get(matcher.group(2));
    }

    private void initType(String type) {
        if (type == null) return;
        Matcher matcher = TYPE_PATTERN.matcher(type);
        Preconditions.checkArgument(matcher.matches());
        typeFormat = matcher.group(1);
        typeFlag = matcher.group(2);
    }

    public String getCommand() {
        return command;
    }

    public String getPackName() {
        return packName;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public long getVolumeSize() {
        return volumeSize;
    }

    public String getTypeFormat() {
        return typeFormat;
    }

    public String getTypeFlag() {
        return typeFlag;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public String getCompressionMethod() {
        return compressionMethod;
    }

    public boolean isHelp() {
        return help;
    }

    public boolean isPassword() {
        return password;
    }
}
