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
import com.google.common.primitives.Ints;
import org.b1.pack.api.reader.ReaderProvider;
import org.b1.pack.api.reader.ReaderVolume;
import org.b1.pack.standard.common.MemoryBuffer;
import org.b1.pack.standard.common.RecordPointer;
import org.b1.pack.standard.common.Volumes;
import org.b1.pack.standard.explorer.HeaderSet;
import org.b1.pack.standard.explorer.OpenVolume;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

class VolumeCursor implements Closeable {

    private static final int MAX_TAIL_SIZE = 1024;

    private final ReaderProvider provider;
    private ExecutorService executorService;
    private String archiveId;
    private long volumeNumber;
    private Long objectTotal;
    private RecordPointer catalogPointer;
    private HeaderSet headerSet;
    private CountingInputStream inputStream;

    public VolumeCursor(ReaderProvider provider) {
        this.provider = provider;
    }

    public HeaderSet getHeaderSet() throws IOException {
        HeaderSet result = getHeaderSet(1);
        if (result.getCatalogPointer() == null) {
            result.setCatalogPointer(getHeaderSet(provider.getVolumeCount()).getCatalogPointer());
            Preconditions.checkNotNull(result.getCatalogPointer(), "Last volume not found");
        }
        return result;
    }

    public ExecutorService getExecutorService() {
        return executorService != null ? executorService : (executorService = provider.getExecutorService());
    }

    public Long getObjectTotal() {
        return objectTotal;
    }

    public RecordPointer getCatalogPointer() {
        return catalogPointer;
    }

    public InputStream getInputStream(long volumeNumber) throws IOException {
        return inputStream;
    }

    public void seek(long volumeNumber, long blockOffset) throws IOException {

    }

    public boolean next() throws IOException {
        return false;
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private ReaderVolume getVolume(long volumeNumber) {
        return Preconditions.checkNotNull(provider.getVolume(volumeNumber), "Volume %s not found", volumeNumber);
    }

    private HeaderSet getHeaderSet(long volumeNumber) throws IOException {
        ReaderVolume volume = getVolume(volumeNumber);
        OpenVolume openVolume = getOpenVolume(volume, volumeNumber);
        try {
            HeaderSet result = openVolume.getHeaderSet();
            if (result.getCatalogPointer() == null) {
                result.setCatalogPointer(new HeaderSet(readTail(volume, openVolume.getStream())).getCatalogPointer());
            }
            return result;
        } finally {
            openVolume.getStream().close();
        }
    }

    private OpenVolume getOpenVolume(ReaderVolume volume, long volumeNumber) throws IOException {
        boolean pending = true;
        CountingInputStream stream = new CountingInputStream(volume.getInputStream());
        try {
            HeaderSet headerSet = new HeaderSet(readHead(volume, stream));
            validateVolume(volume, headerSet, volumeNumber);
            pending = false;
            return new OpenVolume(headerSet, stream);
        } finally {
            if (pending) {
                stream.close();
            }
        }
    }

    private void validateVolume(ReaderVolume volume, HeaderSet set, long volumeNumber) {
        if (archiveId == null) {
            archiveId = set.getArchiveId();
        }
        checkVolume(volume, set.getHeaderType().equals(volumeNumber == 1 ? Volumes.AS : Volumes.VS));
        checkVolume(volume, set.getSchemaVersion() != null && set.getSchemaVersion() <= Volumes.SCHEMA_VERSION);
        checkVolume(volume, set.getArchiveId() != null && set.getArchiveId().equals(archiveId));
        checkVolume(volume, set.getVolumeNumber() != null && set.getVolumeNumber() == volumeNumber);
    }

    private static String readHead(ReaderVolume volume, InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        while (true) {
            int b = stream.read();
            checkVolume(volume, b != -1);
            if ((byte) b == Volumes.SEPARATOR_BYTE) break;
            buffer.write(b);
            if (buffer.size() == Volumes.B1_AS.length()) {
                String signature = buffer.toString(Charsets.UTF_8.name());
                checkVolume(volume, signature.equals(Volumes.B1_AS) || signature.equals(Volumes.B1_VS));
            }
        }
        checkVolume(volume, buffer.size() > Volumes.B1_AS.length());
        return buffer.toString(Charsets.UTF_8.name());
    }

    private static String readTail(ReaderVolume volume, CountingInputStream stream) throws IOException {
        long available = volume.getSize() - stream.getCount();
        int capacity = Ints.checkedCast(Math.min(available, MAX_TAIL_SIZE));
        MemoryBuffer buffer = new MemoryBuffer(capacity);
        ByteStreams.skipFully(stream, available - capacity);

        ByteStreams.copy(stream, buffer);
        int index = buffer.lastIndexOf(Volumes.SEPARATOR_BYTE) + 1;
        checkVolume(volume, index > 0);
        String result = buffer.getString(index, capacity - index, Charsets.UTF_8);
        checkVolume(volume, result.endsWith(Volumes.B1_AE) || result.endsWith(Volumes.B1_VE));
        return result;
    }

    private static void checkVolume(ReaderVolume volume, boolean expression) {
        Preconditions.checkState(expression, "Volume broken or not a B1 archive: %s", volume.getName());
    }
}
