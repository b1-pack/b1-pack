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

package org.b1.pack.standard.common;

import com.google.common.primitives.Ints;
import org.b1.pack.api.builder.*;
import org.b1.pack.api.explorer.*;
import org.b1.pack.api.writer.*;
import org.junit.Test;

import java.io.*;
import java.util.List;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.io.ByteStreams.toByteArray;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.logging.Logger.getLogger;
import static org.b1.pack.api.common.PackService.B1;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class IntegrationTest {

    @Test
    public void testBuilder() throws IOException {
        String folderName = "builderFolder";
        String fileName = "builderFile.txt";
        long fileTime = System.currentTimeMillis();
        byte[] fileContent = "Hello, World!".getBytes(UTF_8);
        final String packName = "builderTest";
        String volumeName = packName + ".b1";
        // START SNIPPET: builder
        PackBuilder builder = PbFactory.newInstance(B1).createPackBuilder(createPbProvider(packName));
        PbFile pbFile = createPbFile(folderName, fileName, fileTime, fileContent);
        builder.addFile(pbFile);
        PbVolume pbVolume = getOnlyElement(builder.getVolumes());
        byte[] volumeContent = getPbVolumeContent(pbVolume);
        // END SNIPPET: builder
        assertEquals(volumeName, pbVolume.getName());
        verifyVolume(folderName, fileName, fileTime, fileContent, volumeName, volumeContent);
    }

    @Test
    public void testWriter() throws IOException {
        String folderName = "writerFolder";
        String fileName = "writerFile.txt";
        long fileTime = System.currentTimeMillis();
        byte[] fileContent = "Hello, B1!".getBytes(UTF_8);
        String packName = "writerTest";
        String volumeName = packName + ".b1";

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PwProvider pwProvider = createPwProvider(packName, volumeName, buffer);
        PackWriter writer = PwFactory.newInstance(B1).createPackWriter(pwProvider);
        try {
            OutputStream stream = writer.addFile(createPwFile(folderName, fileName, fileTime));
            try {
                stream.write(fileContent);
            } finally {
                stream.close();
            }
        } finally {
            writer.close();
        }
        byte[] volumeContent = buffer.toByteArray();
        verifyVolume(folderName, fileName, fileTime, fileContent, volumeName, volumeContent);
    }

    private void verifyVolume(String folderName, String fileName, long fileTime, byte[] fileContent,
                              String volumeName, byte[] volumeContent) throws IOException {
        // START SNIPPET: explorer
        PxVolume pxVolume = createPxVolume(volumeName, volumeContent);
        PxProvider pxProvider = createPxProvider(pxVolume);
        PackExplorer explorer = PxFactory.newInstance(B1).createPackExplorer(pxProvider);
        List<PxFolder> folders = newArrayList();
        List<PxFile> files = newArrayList();
        PxVisitor pxVisitor = createPxVisitor(folders, files);
        explorer.listObjects(pxVisitor);
        // END SNIPPET: explorer

        PxFolder folder = getOnlyElement(folders);
        assertEquals(singletonList(folderName), folder.getPath());
        PxFile file = getOnlyElement(files);
        assertEquals(asList(folderName, fileName), file.getPath());
        assertEquals(fileTime, file.getLastModifiedTime().longValue());
        assertEquals(fileContent.length, file.getSize());
        assertArrayEquals(fileContent, getPxFileContent(file));
    }

    private static PbProvider createPbProvider(final String packName) {
        return new PbProvider() {
            @Override
            public String getPackName() {
                return packName;
            }

            @Override
            public long getVolumeSize() {
                return Long.MAX_VALUE;
            }
        };
    }

    private static PbFile createPbFile(final String folderName, final String fileName,
                                       final long lastModifiedTime, final byte[] content) {
        return new PbFile() {
            public List<String> getPath() {
                return asList(folderName, fileName);
            }

            public Long getLastModifiedTime() {
                return lastModifiedTime;
            }

            public void beforeAdd() {
                getLogger(getClass().getName()).fine("Adding " + getPath());
            }

            public long getSize() {
                return content.length;
            }

            public void writeTo(OutputStream stream, long start, long end) throws IOException {
                stream.write(content, Ints.checkedCast(start), Ints.checkedCast(end - start));
            }
        };
    }

    private static byte[] getPbVolumeContent(PbVolume pbVolume) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        pbVolume.writeTo(stream, 0, Ints.checkedCast(pbVolume.getSize()));
        return stream.toByteArray();
    }

    private static PwProvider createPwProvider(final String packName, final String volumeName,
                                               final ByteArrayOutputStream buffer) {
        return new PwProvider() {
            @Override
            public String getPackName() {
                return packName;
            }

            @Override
            public Long getExpectedVolumeCount() {
                return null;
            }

            @Override
            public PwVolume getVolume(String name) {
                assertEquals(volumeName, name);
                return createPwVolume(buffer);
            }
        };
    }

    private static PwVolume createPwVolume(final ByteArrayOutputStream buffer) {
        return new PwVolume() {
            @Override
            public long getSize() {
                return Long.MAX_VALUE;
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                buffer.reset();
                return buffer;
            }

            @Override
            public void complete() throws IOException {
                // no-op
            }
        };
    }

    private static PwFile createPwFile(final String folderName, final String fileName, final long fileTime) {
        return new PwFile() {
            @Override
            public List<String> getPath() {
                return asList(folderName, fileName);
            }

            @Override
            public Long getLastModifiedTime() {
                return fileTime;
            }
        };
    }

    private static PxVolume createPxVolume(final String name, final byte[] packContent) {
        return new PxVolume() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public long getSize() {
                return packContent.length;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(packContent);
            }
        };
    }

    private static PxProvider createPxProvider(final PxVolume pxVolume) {
        return new PxProvider() {
            @Override
            public PxVolume getVolume(long number) {
                checkArgument(number == 1);
                return pxVolume;
            }

            @Override
            public long getVolumeCount() {
                return 1;
            }

            @Override
            public void close() throws IOException {
                // no-op
            }
        };
    }

    private static PxVisitor createPxVisitor(final List<PxFolder> folders, final List<PxFile> files) {
        return new PxVisitor() {
            @Override
            public void visit(PxFolder folder) throws IOException {
                folders.add(folder);
            }

            @Override
            public void visit(PxFile file) throws IOException {
                files.add(file);
            }
        };
    }

    private static byte[] getPxFileContent(PxFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        try {
            return toByteArray(inputStream);
        } finally {
            inputStream.close();
        }
    }
}
