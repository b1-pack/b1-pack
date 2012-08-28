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

import SevenZip.Compression.LZMA.Decoder;
import SevenZip.Compression.LZMA.Encoder;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.CountingInputStream;
import org.b1.pack.standard.common.BlockPointer;
import org.b1.pack.standard.common.Constants;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

class ChunkCursor implements Closeable {

    private final BlockCursor blockCursor;
    private final byte[] lzmaProperties = new byte[Encoder.kPropSize];
    private final Decoder lzmaDecoder = new Decoder();
    private BlockPointer blockPointer;
    private CountingInputStream inputStream = new CountingInputStream(new ByteArrayInputStream(new byte[0]));
    private long streamOffset;

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
        return streamOffset + inputStream.getCount();
    }

    public void seek(BlockPointer pointer) throws IOException {
        inputStream.close();
        blockCursor.seek(pointer);
        initChunk();
    }

    public void next() throws IOException {
        streamOffset = getRecordOffset();
        inputStream.close();
        if (blockCursor.getInputStream().available() > 0) {
            Preconditions.checkState(
                    blockCursor.getBlockType() == Constants.FIRST_LZMA_BLOCK ||
                    blockCursor.getBlockType() == Constants.NEXT_LZMA_BLOCK);
            inputStream = createLzmaInputStream(new LzmaEncodedInputStream(blockCursor));
            return;
        }
        blockCursor.next();
        if (blockCursor.getBlockType() == Constants.NEXT_LZMA_BLOCK) {
            inputStream = createLzmaInputStream(new LzmaEncodedInputStream(blockCursor));
            return;
        }
        initChunk();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    private void initChunk() throws IOException {
        blockPointer = blockCursor.getBlockPointer();
        streamOffset = 0;
        if (blockCursor.getBlockType() == Constants.PLAIN_BLOCK) {
            inputStream = new CountingInputStream(blockCursor.getInputStream());
            return;
        }
        Preconditions.checkState(blockCursor.getBlockType() == Constants.FIRST_LZMA_BLOCK);
        LzmaEncodedInputStream stream = new LzmaEncodedInputStream(blockCursor);
        ByteStreams.readFully(stream, lzmaProperties);
        Preconditions.checkState(lzmaDecoder.SetDecoderProperties(lzmaProperties));
        inputStream = createLzmaInputStream(stream);
    }

    private CountingInputStream createLzmaInputStream(LzmaEncodedInputStream stream) throws IOException {
        return new CountingInputStream(new LzmaDecodingInputStream(stream, lzmaDecoder, blockCursor.getExecutorService()));
    }
}
