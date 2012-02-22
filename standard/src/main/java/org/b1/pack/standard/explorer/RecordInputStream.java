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

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.CountingInputStream;
import com.google.common.primitives.Ints;
import org.b1.pack.standard.common.Constants;
import org.b1.pack.standard.common.MemoryBuffer;
import org.b1.pack.standard.common.Numbers;
import org.b1.pack.standard.common.RecordPointer;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;

public class RecordInputStream extends InputStream {

    private final MemoryBuffer memoryBuffer = new MemoryBuffer(Constants.MAX_CHUNK_SIZE + 1);
    private final VolumeManager volumeManager;
    private long volumeNumber;
    private long blockOffset;
    private CountingInputStream stream;

    public RecordInputStream(VolumeManager volumeManager) {
        this.volumeManager = volumeManager;
    }

    @Override
    public int read() throws IOException {
        int result = memoryBuffer.read();
        if (result != -1) return result;
        readNextBlock();
        return memoryBuffer.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = memoryBuffer.read(b, off, len);
        if (result != -1) return result;
        readNextBlock();
        return memoryBuffer.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        long result = memoryBuffer.skip(n);
        if (result > 0) return result;
        readNextBlock();
        return memoryBuffer.skip(n);
    }

    @Override
    public void close() throws IOException {
        try {
            if (stream != null) {
                stream.close();
            }
        } finally {
            volumeManager.close();
        }
    }

    public void seek(RecordPointer pointer) throws IOException {
        if (stream != null) {
            if (volumeNumber == pointer.volumeNumber) {
                if (blockOffset == pointer.blockOffset) {
                    memoryBuffer.setIndex(pointer.recordOffset);
                    return;
                }
                long delta = pointer.blockOffset - stream.getCount();
                if (delta >= 0) {
                    ByteStreams.skipFully(stream, delta);
                    readNextBlock();
                    memoryBuffer.setIndex(pointer.recordOffset);
                    return;
                }
            }
            stream.close();
        }
        stream = volumeManager.getInputStream(pointer.volumeNumber);
        volumeNumber = pointer.volumeNumber;
        ByteStreams.skipFully(stream, pointer.blockOffset - stream.getCount());
        readNextBlock();
        memoryBuffer.setIndex(pointer.recordOffset);
    }

    private void readNextBlock() throws IOException {
        while (true) {
            if (stream != null) {
                blockOffset = stream.getCount();
                Long blockType = Numbers.readLong(stream);
                if (blockType != null) {
                    readBlock(blockType);
                    return;
                }
                stream.close();
            }
            stream = volumeManager.getInputStream(++volumeNumber);
        }
    }

    private void readBlock(Long blockType) throws IOException {
        memoryBuffer.reset();
        Preconditions.checkArgument(blockType == Constants.PLAIN_BLOCK);
        Adler32 adler32 = new Adler32();
        ByteStreams.copy(new ChunkedInputStream(stream), new CheckedOutputStream(memoryBuffer, adler32));
        byte[] buffer = new byte[4];
        ByteStreams.readFully(stream, buffer);
        Preconditions.checkArgument(Ints.fromByteArray(buffer) == (int) adler32.getValue(), "Invalid checksum");
        Preconditions.checkArgument(memoryBuffer.size() > 0);
    }
}
