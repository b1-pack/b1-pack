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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

class BlockCursor implements Closeable {

    private final VolumeCursor volumeCursor;
    private long volumeNumber;
    private long blockOffset;
    private long blockType;
    private InputStream inputStream;

    public BlockCursor(VolumeCursor volumeCursor) {
        this.volumeCursor = volumeCursor;
    }

    public ExecutorService getExecutorService() {
        return volumeCursor.getExecutorService();
    }

    public long getVolumeNumber() {
        return volumeNumber;
    }

    public long getBlockOffset() {
        return blockOffset;
    }

    public long getBlockType() {
        return blockType;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public boolean seek(long volumeNumber, long blockOffset) throws IOException {

        return false;
    }
    
    public boolean next() throws IOException {
        return false;
    }

    @Override
    public void close() throws IOException {
        volumeCursor.close();
    }
}
