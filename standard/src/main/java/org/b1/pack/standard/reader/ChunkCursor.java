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

import com.google.common.io.CountingInputStream;
import org.b1.pack.standard.common.BlockPointer;
import org.b1.pack.standard.common.Constants;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

class ChunkCursor implements Closeable {

    private final BlockCursor blockCursor;
    private BlockPointer blockPointer;
    private CountingInputStream inputStream = new CountingInputStream(new ByteArrayInputStream(new byte[0]));

    public ChunkCursor(BlockCursor blockCursor) {
        this.blockCursor = blockCursor;
    }

    public BlockPointer getBlockPointer() {
        return blockPointer;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public long getRecordOffset() {
        return inputStream.getCount();
    }

    public void seek(BlockPointer pointer) throws IOException {
        blockCursor.seek(pointer);
        initChunk();
    }

    public void next() throws IOException {
        blockCursor.next();
        initChunk();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    private void initChunk() throws IOException {
        inputStream.close();
        blockPointer = blockCursor.getBlockPointer();
        inputStream = new CountingInputStream(blockCursor.getBlockType() == Constants.PLAIN_BLOCK ? blockCursor.getInputStream()
                : new LzmaDecodingInputStream(new LzmaEncodedInputStream(blockCursor), blockCursor.getExecutorService()));

    }
}
