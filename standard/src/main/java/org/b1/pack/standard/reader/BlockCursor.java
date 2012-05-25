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

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.CountingInputStream;
import com.google.common.primitives.Ints;
import org.b1.pack.standard.common.*;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;

class BlockCursor implements Closeable {

    private final MemoryOutputStream outputStream = new MemoryOutputStream();
    private final VolumeCursor volumeCursor;
    private BlockPointer blockPointer;
    private Long blockType;
    private CountingInputStream inputStream;

    public BlockCursor(VolumeCursor volumeCursor) {
        this.volumeCursor = volumeCursor;
    }

    public ExecutorService getExecutorService() {
        return volumeCursor.getExecutorService();
    }

    public BlockPointer getBlockPointer() {
        return checkInitialized(blockPointer);
    }

    public long getBlockType() {
        return checkInitialized(blockType);
    }

    public InputStream getInputStream() {
        return checkInitialized(inputStream);
    }

    public void seek(BlockPointer pointer) throws IOException {
        if (pointer.equals(blockPointer)) {
            if (inputStream.getCount() > 0) createInputStream();
            return;
        }
        volumeCursor.seek(pointer);
        next();
    }

    public void next() throws IOException {
        readBlockType();
        InputStream inputStream = volumeCursor.getInputStream();
        VolumeCipher volumeCipher = volumeCursor.getVolumeCipher();
        if (volumeCipher != null) {
            Preconditions.checkState(blockType == Constants.AES_BLOCK);
            inputStream = new ByteArrayInputStream(volumeCipher.cipherBlock(
                    false, blockPointer.blockOffset, ByteStreams.toByteArray(new ChunkedInputStream(inputStream))));
            blockType = Preconditions.checkNotNull(Numbers.readLong(inputStream));
        }
        Preconditions.checkState(
                blockType == Constants.PLAIN_BLOCK ||
                blockType == Constants.FIRST_LZMA_BLOCK ||
                blockType == Constants.NEXT_LZMA_BLOCK);
        outputStream.reset();
        Adler32 adler32 = new Adler32();
        ByteStreams.copy(new ChunkedInputStream(inputStream), new CheckedOutputStream(outputStream, adler32));
        Preconditions.checkArgument(outputStream.size() > 0, "Empty block");
        byte[] buffer = new byte[4];
        ByteStreams.readFully(inputStream, buffer);
        Preconditions.checkArgument(Ints.fromByteArray(buffer) == (int) adler32.getValue(), "Invalid checksum");
        if (volumeCipher != null) {
            Preconditions.checkState(inputStream.available() == 0);
        }
        createInputStream();
    }

    @Override
    public void close() throws IOException {
        volumeCursor.close();
    }

    private void createInputStream() {
        inputStream = new CountingInputStream(new ByteArrayInputStream(outputStream.getBuf(), 0, outputStream.size()));
    }

    private void readBlockType() throws IOException {
        while (true) {
            blockPointer = volumeCursor.getBlockPointer();
            blockType = Numbers.readLong(volumeCursor.getInputStream());
            if (blockType != null) {
                return;
            }
            volumeCursor.next();
        }
    }

    private static <T> T checkInitialized(T reference) {
        return Preconditions.checkNotNull(reference, "Block not initialized");
    }
}
