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
import org.b1.pack.standard.common.Volumes;
import org.b1.pack.standard.explorer.HeaderSet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

class HeaderReader {

    private final String volumeName;

    public HeaderReader(String volumeName) {
        this.volumeName = volumeName;
    }

    public HeaderSet readHead(InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        while (true) {
            int b = stream.read();
            checkVolume(b != -1);
            if ((byte) b == Volumes.SEPARATOR_BYTE) break;
            buffer.write(b);
            if (buffer.size() == Volumes.B1_AS.length()) {
                String signature = buffer.toString(Charsets.UTF_8.name());
                checkVolume(signature.equals(Volumes.B1_AS) || signature.equals(Volumes.B1_VS));
            }
        }
        return new HeaderSet(buffer.toString(Charsets.UTF_8.name()));
    }

    public HeaderSet readTail(InputStream stream) throws IOException {
        String tail = new String(ByteStreams.toByteArray(stream), Charsets.UTF_8);
        checkVolume(tail.endsWith(Volumes.B1_AE) || tail.endsWith(Volumes.B1_VE));
        return new HeaderSet(tail);
    }

    private void checkVolume(boolean expression) {
        Preconditions.checkState(expression, "Volume broken or not a B1 archive: %s", volumeName);
    }
}
