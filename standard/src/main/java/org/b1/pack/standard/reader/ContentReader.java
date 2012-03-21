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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import org.b1.pack.api.reader.FileVisitor;
import org.b1.pack.standard.common.Constants;
import org.b1.pack.standard.common.Numbers;
import org.b1.pack.standard.common.RecordPointer;
import org.b1.pack.standard.explorer.ChunkedInputStream;
import org.b1.pack.standard.explorer.RecordHeader;

import java.io.IOException;
import java.io.OutputStream;

class ContentReader {
    
    private final Long id;
    private final RecordPointer pointer;
    private final FileVisitor visitor;

    public ContentReader(Long id, RecordPointer pointer, FileVisitor visitor) {
        this.id = id;
        this.pointer = pointer;
        this.visitor = visitor;
    }

    public void read(RecordInputStream stream) throws IOException {
        stream.seek(pointer);
        Preconditions.checkArgument(Numbers.readLong(stream) == Constants.COMPLETE_FILE);
        RecordHeader header = RecordHeader.readRecordHeader(stream);// ignore for now
        Preconditions.checkState(Objects.equal(header.id, id));
        OutputStream outputStream = visitor.visitContent();
        if (outputStream != null) {
            try {
                ByteStreams.copy(new ChunkedInputStream(stream), outputStream);
            } finally {
                outputStream.close();
            }
        }
        visitor.visitEnd();
    }
}

