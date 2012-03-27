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
import org.b1.pack.api.reader.*;
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
        final BuilderFile builderFile = createBuilderFile(folderName, fileName, fileTime, fileContent);
        List<BuilderVolume> volumes = PackBuilder.getInstance(B1).build(new BuilderProvider(), new BuilderCommand() {
            @Override
            public void execute(BuilderPack pack) {
                pack.addFile(builderFile);
            }
        });
        BuilderVolume builderVolume = getOnlyElement(volumes);
        byte[] volumeContent = getBuilderVolumeContent(builderVolume);
        // END SNIPPET: builder
        assertEquals(1, builderVolume.getNumber());
        verifyVolume(folderName, fileName, fileTime, fileContent, volumeName, volumeContent);
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
        WriterProvider provider = createWriterProvider(buffer);
        final WriterEntry folder = createWriterEntry(null, folderName, fileTime);
        final WriterEntry file = createWriterEntry(folder, fileName, fileTime);
        PackWriter.getInstance(B1).write(provider, new WriterCommand() {
            @Override
            public void execute(WriterPack pack) throws IOException {
                pack.addFolder(folder);
                pack.addFile(file, createWriterContent(fileContent));
            }
        });
        byte[] volumeContent = buffer.toByteArray();
        verifyVolume(folderName, fileName, fileTime, fileContent, volumeName, volumeContent);
        verifyVolume2(folderName, fileName, fileTime, fileContent, volumeName, volumeContent);
    }

    private void verifyVolume(String folderName, String fileName, long fileTime, byte[] fileContent,
                              String volumeName, byte[] volumeContent) throws IOException {
        // START SNIPPET: explorer
        ExplorerVolume explorerVolume = createExplorerVolume(volumeName, volumeContent);
        ExplorerProvider explorerProvider = createExplorerProvider(explorerVolume);
        List<ExplorerFolder> folders = newArrayList();
        List<ExplorerFile> files = newArrayList();
        final ExplorerVisitor explorerVisitor = createExplorerVisitor(folders, files);
        PackExplorer.getInstance(B1).explore(explorerProvider, new ExplorerCommand() {
            @Override
            public void execute(ExplorerPack pack) throws IOException {
                pack.listObjects(explorerVisitor);
            }
        });
        // END SNIPPET: explorer
        ExplorerFolder folder = getOnlyElement(folders);
        assertEquals(singletonList(folderName), folder.getPath());
        ExplorerFile file = getOnlyElement(files);
        assertEquals(asList(folderName, fileName), file.getPath());
        assertEquals(fileTime, file.getLastModifiedTime().longValue());
        assertEquals(fileContent.length, file.getSize());
        assertArrayEquals(fileContent, getExplorerFileContent(file));
    }

    private void verifyVolume2(String folderName, String fileName, long fileTime, byte[] fileContent,
                              String volumeName, byte[] volumeContent) throws IOException {
        // START SNIPPET: reader
        ReaderVolume readerVolume = createReaderVolume(volumeName, volumeContent); 
        ReaderProvider readerProvider = createReaderProvider(readerVolume);
        PackReader.getInstance(B1).read(readerProvider, new ReaderCommand() {
            @Override
            public void execute(ReaderPack pack) throws IOException {
                pack.accept(new ReaderPackVisitor() {
                    @Override
                    public ReaderFileVisitor visitFile(ReaderEntry entry, long size) {
                        throw new IllegalStateException();
                    }

                    @Override
                    public ReaderFolderVisitor visitFolder(ReaderEntry entry) {
                        System.out.println("IntegrationTest.visitFolder");
                        System.out.println("entry.getName() = " + entry.getName());
                        System.out.println("entry.getLastModifiedTime() = " + entry.getLastModifiedTime());
                        return new ReaderFolderVisitor() {
                            @Override
                            public ReaderPackVisitor visitChildren() {
                                System.out.println("IntegrationTest.visitChildren");
                                return new ReaderPackVisitor() {
                                    @Override
                                    public ReaderFileVisitor visitFile(ReaderEntry entry, long size) {
                                        System.out.println("IntegrationTest.visitFile");
                                        System.out.println("entry.getName() = " + entry.getName());
                                        System.out.println("entry.getLastModifiedTime() = " + entry.getLastModifiedTime());
                                        System.out.println("size = " + size);
                                        return new ReaderFileVisitor() {
                                            @Override
                                            public void visitContent(ReaderContent content) throws IOException {
                                                System.out.println("IntegrationTest.visitContent");
                                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                                content.writeTo(stream);
                                                System.out.println("stream.toString() = " + stream.toString());
                                            }

                                            @Override
                                            public void visitEnd() {
                                                System.out.println("IntegrationTest.visitEnd file");
                                            }
                                        };
                                    }

                                    @Override
                                    public ReaderFolderVisitor visitFolder(ReaderEntry entry) {
                                        throw new IllegalStateException();
                                    }
                                };
                            }

                            @Override
                            public void visitEnd() {
                                System.out.println("IntegrationTest.visitEnd - folder");
                            }
                        };
                    }
                });
            }
        });
        // END SNIPPET: reader
/*
        ExplorerFolder folder = getOnlyElement(folders);
        assertEquals(singletonList(folderName), folder.getPath());
        ExplorerFile file = getOnlyElement(files);
        assertEquals(asList(folderName, fileName), file.getPath());
        assertEquals(fileTime, file.getLastModifiedTime().longValue());
        assertEquals(fileContent.length, file.getSize());
        assertArrayEquals(fileContent, getExplorerFileContent(file));
*/
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

    private static WriterEntry createWriterEntry(final WriterEntry parent, final String fileName, final long fileTime) {
        return new WriterEntry() {
            @Override
            public WriterEntry getParent() {
                return parent;
            }

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

    private WriterContent createWriterContent(final byte[] fileContent) {
        return new WriterContent() {
            @Override
            public Long getSize() throws IOException {
                return (long) fileContent.length;
            }

            @Override
            public void writeTo(OutputStream stream) throws IOException {
                stream.write(fileContent);
            }
        };
    }

    private static ExplorerVolume createExplorerVolume(final String name, final byte[] packContent) {
        return new ExplorerVolume() {
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

    private static ExplorerProvider createExplorerProvider(final ExplorerVolume explorerVolume) {
        return new ExplorerProvider() {
            @Override
            public ExplorerVolume getVolume(long number) {
                checkArgument(number == 1);
                return explorerVolume;
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

    private static ExplorerVisitor createExplorerVisitor(final List<ExplorerFolder> folders, final List<ExplorerFile> files) {
        return new ExplorerVisitor() {
            @Override
            public void visit(ExplorerFolder folder) throws IOException {
                folders.add(folder);
            }

            @Override
            public void visit(ExplorerFile file) throws IOException {
                files.add(file);
            }
        };
    }

    private static byte[] getExplorerFileContent(ExplorerFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        try {
            return toByteArray(inputStream);
        } finally {
            inputStream.close();
        }
    }
}
