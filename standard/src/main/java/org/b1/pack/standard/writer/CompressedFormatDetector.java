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
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Enumeration;
import java.util.StringTokenizer;

class CompressedFormatDetector {

    private static CompressedFormatDetector instance;

    private final ImmutableSet<String> nameExtensions;

    private CompressedFormatDetector(ImmutableSet<String> nameExtensions) {
        this.nameExtensions = nameExtensions;
    }

    public static CompressedFormatDetector getInstance() {
        if (instance == null) {
            // ok to create several instances in rare cases
            instance = new CompressedFormatDetector(loadExtensions());
        }
        return instance;
    }

    public boolean isNameExtensionOfCompressedFile(String extension) {
        return nameExtensions.contains(extension.toLowerCase());
    }

    private static ImmutableSet<String> loadExtensions() {
        try {
            ImmutableSet.Builder<String> builder = ImmutableSet.builder();
            Enumeration<URL> resources = getResources("org/b1/pack/standard/writer/compressedFormats.txt");
            while (resources.hasMoreElements()) {
                readResource(resources.nextElement(), builder);
            }
            return builder.build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Enumeration<URL> getResources(String name) throws IOException {
        ClassLoader classLoader = LzmaMethod.class.getClassLoader();
        Enumeration<URL> resources = classLoader != null ? classLoader.getResources(name) : ClassLoader.getSystemResources(name);
        Preconditions.checkState(resources.hasMoreElements(), "Cannot find %s", name);
        return resources;
    }

    private static void readResource(final URL url, ImmutableSet.Builder<String> builder) throws IOException {
        StringTokenizer tokenizer = new StringTokenizer(CharStreams.toString(new InputSupplier<Reader>() {
            @Override
            public Reader getInput() throws IOException {
                return new InputStreamReader(url.openStream(), Charsets.UTF_8);
            }
        }));
        while (tokenizer.hasMoreTokens()) {
            builder.add(tokenizer.nextToken().toLowerCase());
        }
    }
}
