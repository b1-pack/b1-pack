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

package org.b1.pack.standard.explorer;

import com.google.common.io.CountingInputStream;
import com.google.common.primitives.Ints;
import org.b1.pack.api.common.PackException;
import org.b1.pack.api.explorer.PxProvider;
import org.b1.pack.api.explorer.PxVolume;
import org.b1.pack.standard.common.MemoryBuffer;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.ByteStreams.copy;
import static com.google.common.io.ByteStreams.skipFully;
import static org.b1.pack.standard.common.Volumes.*;

public class VolumeManager implements Closeable {

    private static final int MAX_TAIL_SIZE = 1024;
    private final PxProvider provider;
    private String archiveId;

    public VolumeManager(PxProvider provider) {
        this.provider = provider;
    }

    public HeaderSet getHeaderSet() throws IOException {
        HeaderSet result = getHeaderSet(1);
        if (result.getCatalogPointer() == null) {
            result.setCatalogPointer(getHeaderSet(provider.getVolumeCount()).getCatalogPointer());
            if (result.getCatalogPointer() == null) {
                throw new PackException("Last volume not found");
            }
        }
        return result;
    }

    public CountingInputStream getInputStream(long volumeNumber) throws IOException {
        return getOpenVolume(getVolume(volumeNumber), volumeNumber).getStream();
    }

    @Override
    public void close() throws IOException {
        provider.close();
    }

    private PxVolume getVolume(long volumeNumber) {
        PxVolume volume = provider.getVolume(volumeNumber);
        if (volume == null) {
            throw new PackException("Volume " + volumeNumber + " not found");
        }
        return volume;
    }

    private HeaderSet getHeaderSet(long volumeNumber) throws IOException {
        PxVolume volume = getVolume(volumeNumber);
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

    private OpenVolume getOpenVolume(PxVolume volume, long volumeNumber) throws IOException {
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

    private void validateVolume(PxVolume volume, HeaderSet set, long volumeNumber) {
        if (archiveId == null) {
            archiveId = set.getArchiveId();
        }
        checkVolume(volume, set.getHeaderType().equals(volumeNumber == 1 ? AS : VS));
        checkVolume(volume, set.getSchemaVersion() != null && set.getSchemaVersion() <= SCHEMA_VERSION);
        checkVolume(volume, set.getArchiveId() != null && set.getArchiveId().equals(archiveId));
        checkVolume(volume, set.getVolumeNumber() != null && set.getVolumeNumber() == volumeNumber);
    }

    private static String readHead(PxVolume volume, InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        while (true) {
            int b = stream.read();
            checkVolume(volume, b != -1);
            if ((byte) b == SEPARATOR_BYTE) break;
            buffer.write(b);
            if (buffer.size() == B1_AS.length()) {
                String signature = buffer.toString(UTF_8.name());
                checkVolume(volume, signature.equals(B1_AS) || signature.equals(B1_VS));
            }
        }
        checkVolume(volume, buffer.size() > B1_AS.length());
        return buffer.toString(UTF_8.name());
    }

    private static String readTail(PxVolume volume, CountingInputStream stream) throws IOException {
        long available = volume.getSize() - stream.getCount();
        int capacity = Ints.checkedCast(Math.min(available, MAX_TAIL_SIZE));
        MemoryBuffer buffer = new MemoryBuffer(capacity);
        skipFully(stream, available - capacity);
        copy(stream, buffer);
        int index = buffer.lastIndexOf(SEPARATOR_BYTE) + 1;
        checkVolume(volume, index > 0);
        String result = buffer.getString(index, capacity - index, UTF_8);
        checkVolume(volume, result.endsWith(B1_AE) || result.endsWith(B1_VE));
        return result;
    }

    private static void checkVolume(PxVolume volume, boolean expression) {
        if (!expression) {
            throw new PackException("Volume broken or not a B1 archive: " + volume.getName());
        }
    }
}
