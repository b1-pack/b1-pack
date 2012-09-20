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
import org.b1.pack.standard.common.Constants;

import java.io.IOException;
import java.io.InputStream;

class LzmaEncodedInputStream extends InputStream {

    private final BlockCursor blockCursor;
    private boolean closed;

    public LzmaEncodedInputStream(BlockCursor blockCursor) {
        this.blockCursor = blockCursor;
    }

    @Override
    public int read() throws IOException {
        while (true) {
            assertOpen();
            int result = blockCursor.getInputStream().read();
            if (result != -1) return result;
            moveToNextBlock();
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        while (true) {
            assertOpen();
            int result = blockCursor.getInputStream().read(b, off, len);
            if (result != -1) return result;
            moveToNextBlock();
        }
    }

    private void moveToNextBlock() throws IOException {
        blockCursor.next();
        Preconditions.checkState(blockCursor.getBlockType() == Constants.NEXT_LZMA_BLOCK);
    }

    @Override
    public void close() throws IOException {
        closed = true;
    }

    protected void assertOpen() throws IOException {
        if (closed) throw new IOException("Stream closed");
    }
}
