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

package org.b1.pack.standard.reader;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.CountingInputStream;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.b1.pack.api.reader.ReaderProvider;
import org.b1.pack.api.reader.ReaderVolume;
import org.b1.pack.standard.common.*;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

class VolumeCursor implements Closeable {

    private static final int MAX_TAIL_SIZE = 1024;

    private final ReaderProvider provider;
    private ExecutorService executorService;
    private PackCipher packCipher;
    private VolumeCipher volumeCipher;
    private String archiveId;
    private Long objectTotal;
    private RecordPointer catalogPointer;
    private long volumeNumber;
    private ReaderVolume volume;
    private HeaderSet headerSet;
    private CountingInputStream inputStream;

    public VolumeCursor(ReaderProvider provider) {
        this.provider = provider;
    }

    public void initialize() throws IOException {
        openVolume(1);
        objectTotal = headerSet.getObjectTotal();
        if (!initCatalogPointer()) {
            openVolume(provider.getVolumeCount());
            Preconditions.checkState(initCatalogPointer(), "Catalog pointer not found");
        }
    }

    public ExecutorService getExecutorService() {
        return executorService != null ? executorService : (executorService = provider.getExecutorService());
    }

    public Long getObjectTotal() throws IOException {
        return objectTotal;
    }

    public RecordPointer getCatalogPointer() throws IOException {
        return catalogPointer;
    }

    public BlockPointer getBlockPointer() {
        return new BlockPointer(volumeNumber, inputStream.getCount());
    }

    public InputStream getInputStream() throws IOException {
        return inputStream;
    }

    public VolumeCipher getVolumeCipher() {
        return volumeCipher;
    }

    public void seek(BlockPointer pointer) throws IOException {
        if (pointer.volumeNumber == volumeNumber) {
            long skipCount = pointer.blockOffset - inputStream.getCount();
            if (skipCount >= 0) {
                ByteStreams.skipFully(inputStream, skipCount);
                return;
            }
        }
        openVolume(pointer.volumeNumber);
        long skipCount = pointer.blockOffset - inputStream.getCount();
        Preconditions.checkState(skipCount >= 0);
        ByteStreams.skipFully(inputStream, skipCount);
    }

    public void next() throws IOException {
        openVolume(volumeNumber + 1);
    }

    @Override
    public void close() throws IOException {
        try {
            if (inputStream != null) inputStream.close();
        } finally {
            if (executorService != null) executorService.shutdown();
        }
    }

    private boolean initCatalogPointer() throws IOException {
        return (catalogPointer = headerSet.getCatalogPointer()) != null ||
                (catalogPointer = readTail().getCatalogPointer()) != null;
    }

    private void openVolume(long number) throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
        volumeNumber = number;
        volume = Preconditions.checkNotNull(provider.getVolume(number), "Volume %s not found", number);
        inputStream = new CountingInputStream(volume.getInputStream());
        headerSet = readHead(number);
        checkVolume(headerSet.getSchemaVersion() != null && headerSet.getSchemaVersion() <= Volumes.SCHEMA_VERSION);
        checkVolume(headerSet.getArchiveId() != null && headerSet.getArchiveId().equals(archiveId));
        checkVolume(headerSet.getVolumeNumber() != null && headerSet.getVolumeNumber() == volumeNumber);
    }

    private HeaderSet readHead(long volumeNumber) throws IOException {
        String signature = volumeNumber == 1 ? Volumes.B1_AS : Volumes.B1_VS;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        while (true) {
            int b = inputStream.read();
            checkVolume(b != -1);
            if ((byte) b == Volumes.SEPARATOR_BYTE) break;
            buffer.write(b);
            if (buffer.size() == signature.length()) {
                checkVolume(buffer.toString(Charsets.UTF_8.name()).equals(signature));
            }
        }
        checkVolume(buffer.size() > signature.length());
        HeaderSet headerSet = new HeaderSet(buffer.toString(Charsets.UTF_8.name()));
        Integer iterationCount = headerSet.getIterationCount();
        if (archiveId == null) {
            archiveId = headerSet.getArchiveId();
            checkVolume(archiveId != null);
            if (iterationCount != null) {
                packCipher = new PackCipher(provider.getPassword(), DatatypeConverter.parseBase64Binary(archiveId), iterationCount);
            }
        } else {
            if (iterationCount == null) {
                checkVolume(packCipher == null);
            } else {
                checkVolume(packCipher != null && packCipher.getIterationCount() == iterationCount);
            }
        }
        if (packCipher == null) {
            return headerSet;
        }
        byte[] encryptedHeaders = headerSet.getEncryptedHeaders();
        checkVolume(encryptedHeaders != null);
        volumeCipher = packCipher.getVolumeCipher(volumeNumber);
        String plaintext = new String(volumeCipher.cipherHead(false, encryptedHeaders), Charsets.UTF_8);
        checkVolume(plaintext.startsWith(signature));
        return new HeaderSet(plaintext);
    }

    private HeaderSet readTail() throws IOException {
        long available = Preconditions.checkNotNull(volume.getSize(), "Volume size unknown") - inputStream.getCount();
        int capacity = Ints.checkedCast(Math.min(available, MAX_TAIL_SIZE));
        ByteStreams.skipFully(inputStream, available - capacity);
        MemoryOutputStream outputStream = new MemoryOutputStream(capacity);
        ByteStreams.copy(inputStream, outputStream);
        Preconditions.checkState(outputStream.size() == capacity);
        int index = Bytes.lastIndexOf(outputStream.getBuf(), Volumes.SEPARATOR_BYTE) + 1;
        checkVolume(index > 0);
        String result = new String(outputStream.getBuf(), index, capacity - index, Charsets.UTF_8);
        checkVolumeEnd(result);
        HeaderSet headerSet = new HeaderSet(result);
        if (packCipher == null) {
            return headerSet;
        }
        byte[] encryptedHeaders = headerSet.getEncryptedHeaders();
        checkVolume(encryptedHeaders != null);
        String plaintext = new String(volumeCipher.cipherTail(false, encryptedHeaders), Charsets.UTF_8);
        checkVolumeEnd(plaintext);
        return new HeaderSet(plaintext);
    }

    private void checkVolumeEnd(String result) {
        checkVolume(result.endsWith(Volumes.B1_AE) || result.endsWith(Volumes.B1_VE));
    }

    private void checkVolume(boolean expression) {
        Preconditions.checkState(expression, "Volume broken or not a B1 archive: %s", volume.getName());
    }
}
