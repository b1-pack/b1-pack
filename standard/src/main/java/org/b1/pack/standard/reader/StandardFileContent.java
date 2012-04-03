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
import org.b1.pack.api.common.FileBuilder;
import org.b1.pack.api.common.FileContent;
import org.b1.pack.standard.common.Constants;
import org.b1.pack.standard.common.Numbers;
import org.b1.pack.standard.common.RecordPointer;

import java.io.IOException;
import java.io.OutputStream;

class StandardFileContent extends FileContent {
    
    private final Long id;
    private final RecordPointer pointer;
    private final PackInputStream inputStream;
    private final FileBuilder builder;

    public StandardFileContent(Long id, RecordPointer pointer, PackInputStream inputStream, FileBuilder builder) {
        this.id = id;
        this.pointer = pointer;
        this.inputStream = inputStream;
        this.builder = builder;
    }

    public void save() throws IOException {
        builder.setContent(this);
        builder.save();
    }

    @Override
    public void writeTo(OutputStream stream) throws IOException {
        inputStream.seek(pointer);
        Preconditions.checkArgument(Numbers.readLong(inputStream) == Constants.COMPLETE_FILE);
        RecordHeader header = RecordHeader.readRecordHeader(inputStream);// ignore for now
        Preconditions.checkState(Objects.equal(header.id, id));
        ByteStreams.copy(new ChunkedInputStream(inputStream), stream);
    }
}
