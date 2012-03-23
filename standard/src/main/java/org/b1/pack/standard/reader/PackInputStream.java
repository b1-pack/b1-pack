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

import org.b1.pack.standard.common.RecordPointer;

import java.io.IOException;
import java.io.InputStream;

class PackInputStream extends InputStream {

    private final ChunkCursor chunkCursor;

    public PackInputStream(ChunkCursor chunkCursor) {
        this.chunkCursor = chunkCursor;
    }

    public void seek(RecordPointer pointer) throws IOException {
        chunkCursor.seek(pointer);
    }

    @Override
    public int read() throws IOException {
        do {
            InputStream stream = chunkCursor.getInputStream();
            if (stream != null) {
                int result = stream.read();
                if (result != -1) return result;
            }
        } while (chunkCursor.next());
        return -1;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        do {
            InputStream stream = chunkCursor.getInputStream();
            if (stream != null) {
                int result = stream.read(b, off, len);
                if (result != -1) return result;
            }
        } while (chunkCursor.next());
        return -1;
    }

    @Override
    public void close() throws IOException {
        chunkCursor.close();
    }
}
