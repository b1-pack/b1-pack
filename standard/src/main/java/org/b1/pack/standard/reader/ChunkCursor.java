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

import com.google.common.io.ByteStreams;
import com.google.common.io.CountingInputStream;
import org.b1.pack.standard.common.Constants;
import org.b1.pack.standard.common.RecordPointer;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

class ChunkCursor implements Closeable {

    private final BlockCursor blockCursor;
    private CountingInputStream inputStream;
    private long volumeNumber;
    private long blockOffset;

    public ChunkCursor(BlockCursor blockCursor) {
        this.blockCursor = blockCursor;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void seek(RecordPointer pointer) throws IOException {
        if (inputStream != null && pointer.volumeNumber == volumeNumber && pointer.blockOffset == blockOffset) {
            long skipCount = pointer.recordOffset - inputStream.getCount();
            if (skipCount >= 0) {
                ByteStreams.skipFully(inputStream, skipCount);
                return;
            }
        }
        if (inputStream != null) {
            inputStream.close();
        }
        blockCursor.seek(volumeNumber, blockOffset);
        initChunk();
        ByteStreams.skipFully(inputStream, pointer.recordOffset);
    }

    public boolean next() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
        if (blockCursor.next()) {
            initChunk();
            return true;
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
    }

    private void initChunk() throws IOException {
        volumeNumber = blockCursor.getVolumeNumber();
        blockOffset = blockCursor.getBlockOffset();
        inputStream = new CountingInputStream(blockCursor.getBlockType() == Constants.PLAIN_BLOCK ? blockCursor.getInputStream()
                : new LzmaDecodingInputStream(new LzmaEncodedInputStream(blockCursor), blockCursor.getExecutorService()));

    }
}
