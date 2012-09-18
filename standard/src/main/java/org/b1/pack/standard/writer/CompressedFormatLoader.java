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

package org.b1.pack.standard.writer;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.LineProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;

class CompressedFormatLoader implements LineProcessor<ImmutableSet<String>> {

    private static final Splitter SPLITTER = Splitter.on(' ').omitEmptyStrings();

    private final ImmutableSet.Builder<String> builder = ImmutableSet.builder();

    @Override
    public boolean processLine(String line) throws IOException {
        for (String s : SPLITTER.split(line)) {
            builder.add(s.toLowerCase());
        }
        return true;
    }

    @Override
    public ImmutableSet<String> getResult() {
        return builder.build();
    }

    public static ImmutableSet<String> getCompressedFormatSet() {
        CompressedFormatLoader loader = new CompressedFormatLoader();
        try {
            Enumeration<URL> resources = getResources("org/b1/pack/standard/writer/compressedFormats.txt");
            while (resources.hasMoreElements()) {
                readLines(resources.nextElement(), loader);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return loader.getResult();
    }

    private static void readLines(URL url, LineProcessor<?> loader) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), Charsets.UTF_8));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!loader.processLine(line)) break;
            }
        } finally {
            reader.close();
        }
    }

    private static Enumeration<URL> getResources(String name) throws IOException {
        ClassLoader classLoader = LzmaMethod.class.getClassLoader();
        Enumeration<URL> resources = classLoader != null ? classLoader.getResources(name) : ClassLoader.getSystemResources(name);
        Preconditions.checkState(resources.hasMoreElements(), "Cannot find %s", name);
        return resources;
    }
}
