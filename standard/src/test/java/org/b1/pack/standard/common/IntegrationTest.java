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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import org.b1.pack.api.builder.*;
import org.b1.pack.api.common.*;
import org.b1.pack.api.reader.PackReader;
import org.b1.pack.api.reader.ReaderProvider;
import org.b1.pack.api.reader.ReaderVolume;
import org.b1.pack.api.writer.PackWriter;
import org.b1.pack.api.writer.WriterProvider;
import org.b1.pack.api.writer.WriterVolume;
import org.junit.Test;

import java.io.*;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Arrays.asList;
import static java.util.logging.Logger.getLogger;
import static org.b1.pack.api.common.PackFormat.B1;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class IntegrationTest {

    @Test
    public void testBuilder() throws IOException {
        final String folderName = "builderFolder";
        final String fileName = "builderFile.txt";
        final long fileTime = System.currentTimeMillis();
        final byte[] fileContent = "Hello, World!".getBytes(UTF_8);
        final String packName = "builderTest";
        String volumeName = packName + ".b1";
        // START SNIPPET: builder
        final BuilderFolder builderFolder = createBuilderFolder(folderName, fileTime);
        final BuilderFile builderFile = createBuilderFile(folderName, fileName, fileTime, fileContent);
        List<BuilderVolume> volumes = PackBuilder.getInstance(B1).build(new BuilderProvider(), new BuilderCommand() {
            @Override
            public void execute(BuilderPack pack) {
                pack.addFolder(builderFolder);
                pack.addFile(builderFile);
            }
        });
        BuilderVolume builderVolume = getOnlyElement(volumes);
        byte[] volumeContent = getBuilderVolumeContent(builderVolume);
        // END SNIPPET: builder
        assertEquals(1, builderVolume.getNumber());
        verifyVolumeWithReader(folderName, fileName, fileTime, fileContent, volumeName, volumeContent);
    }

    @Test
    public void testWriter() throws IOException {
        String folderName = "writerFolder";
        String fileName = "writerFile.txt";
        long fileTime = System.currentTimeMillis();
        final byte[] fileContent = "Hello, test!".getBytes(UTF_8);
        String packName = "writerTest";
        String volumeName = packName + ".b1";
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        // START SNIPPET: writer
        WriterProvider provider = createWriterProvider(buffer);
        final PackEntry folder = createPackEntry(folderName, fileTime);
        final PackEntry file = createPackEntry(fileName, fileTime);
        PackWriter.getInstance(B1).write(provider, new FolderContent() {
            @Override
            public void writeTo(FolderBuilder builder) throws IOException {
                builder.addFolder(folder).addFile(file, (long) fileContent.length).setContent(createFileContent(fileContent));
            }
        });
        // END SNIPPET: writer
        byte[] volumeContent = buffer.toByteArray();
        verifyVolumeWithReader(folderName, fileName, fileTime, fileContent, volumeName, volumeContent);
    }

    private void verifyVolumeWithReader(String folderName, String fileName, long fileTime, byte[] fileContent,
                                        String volumeName, byte[] volumeContent) throws IOException {
        // START SNIPPET: reader
        ReaderVolume readerVolume = createReaderVolume(volumeName, volumeContent);
        ReaderProvider readerProvider = createReaderProvider(readerVolume);
        List<String> folderList = Lists.newArrayList();
        Map<String, byte[]> fileMap = Maps.newHashMap();
        final FolderBuilder builder = createFolderBuilder("", fileTime, folderList, fileMap);
        PackReader.getInstance(B1).read(readerProvider, builder);
        // END SNIPPET: reader
        assertEquals(folderName, getOnlyElement(folderList));
        Map.Entry<String, byte[]> fileEntry = getOnlyElement(fileMap.entrySet());
        assertEquals(folderName + "/" + fileName, fileEntry.getKey());
        assertArrayEquals(fileContent, fileEntry.getValue());
    }

    private static BuilderFolder createBuilderFolder(final String folderName, final long lastModifiedTime) {
        return new BuilderFolder() {
            public List<String> getPath() {
                return asList(folderName);
            }

            public Long getLastModifiedTime() {
                return lastModifiedTime;
            }

            public void beforeAdd() {
                getLogger(getClass().getName()).fine("Adding " + getPath());
            }
        };
    }
    private static BuilderFile createBuilderFile(final String folderName, final String fileName,
                                                 final long lastModifiedTime, final byte[] content) {
        return new BuilderFile() {
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

    private static byte[] getBuilderVolumeContent(BuilderVolume builderVolume) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        builderVolume.writeTo(stream, 0, Ints.checkedCast(builderVolume.getSize()));
        return stream.toByteArray();
    }

    private static WriterProvider createWriterProvider(final ByteArrayOutputStream buffer) {
        return new WriterProvider() {
            @Override
            public boolean isSeekable() {
                return false;
            }

            @Override
            public WriterVolume getVolume(long number) throws IOException {
                assertEquals(1, number);
                return createWriterVolume(buffer);
            }
        };
    }

    private static WriterVolume createWriterVolume(final ByteArrayOutputStream buffer) {
        return new WriterVolume() {
            @Override
            public OutputStream getOutputStream() throws IOException {
                buffer.reset();
                return buffer;
            }
        };
    }

    private static PackEntry createPackEntry(final String fileName, final long fileTime) {
        return new PackEntry() {
            @Override
            public String getName() {
                return fileName;
            }

            @Override
            public Long getLastModifiedTime() {
                return fileTime;
            }
        };
    }

    private FileContent createFileContent(final byte[] fileContent) {
        return new FileContent() {
            @Override
            public void writeTo(OutputStream stream) throws IOException {
                stream.write(fileContent);
            }
        };
    }

    private static ReaderVolume createReaderVolume(final String name, final byte[] packContent) {
        return new ReaderVolume() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Long getSize() {
                return (long) packContent.length;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(packContent);
            }
        };
    }

    private static ReaderProvider createReaderProvider(final ReaderVolume readerVolume) {
        return new ReaderProvider() {
            @Override
            public ReaderVolume getVolume(long number) {
                checkArgument(number == 1);
                return readerVolume;
            }

            @Override
            public long getVolumeCount() {
                return 1;
            }
        };
    }

    private static FolderBuilder createFolderBuilder(final String prefix, final long fileTime, final List<String> folderList, final Map<String, byte[]> fileMap) {
        return new FolderBuilder() {
            @Override
            public FileBuilder addFile(final PackEntry entry, final Long size) {
                Preconditions.checkState(entry.getLastModifiedTime() == fileTime);
                return new FileBuilder() {
                    @Override
                    public void setContent(FileContent content) throws IOException {
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        content.writeTo(stream);
                        Preconditions.checkState(stream.size() == size);
                        fileMap.put(prefix + entry.getName(), stream.toByteArray());
                    }

                    @Override
                    public void flush() throws IOException {
                        //no-op
                    }
                };
            }

            @Override
            public FolderBuilder addFolder(PackEntry entry) {
                Preconditions.checkState(entry.getLastModifiedTime() == fileTime);
                folderList.add(prefix + entry.getName());
                return createFolderBuilder(prefix + entry.getName() + "/", fileTime, folderList, fileMap);
            }

            @Override
            public void flush() throws IOException {
                //no-op
            }
        };
    }

}
