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

package org.b1.pack.standard.writer;

import com.google.common.base.Charsets;
import org.b1.pack.api.writer.WriterEntry;
import org.b1.pack.standard.common.Numbers;
import org.b1.pack.standard.common.PbRecordPointer;
import org.b1.pack.standard.common.RecordPointer;

import java.io.IOException;

abstract class StandardObjectBuilder {

    private final long id;
    protected final RecordWriter recordWriter;
    protected final PackOutputStream stream;
    private final StandardFolderBuilder parent;
    private final WriterEntry entry;
    protected boolean completeRecordSaved;
    private RecordPointer pointer;
    private PbRecordPointer futurePointer;

    protected StandardObjectBuilder(long id, RecordWriter recordWriter, StandardFolderBuilder parent, WriterEntry entry) {
        this.id = id;
        this.recordWriter = recordWriter;
        this.parent = parent;
        this.entry = entry;
        stream = recordWriter.getPackOutputStream();
    }

    public abstract void saveCatalogRecord() throws IOException;

    public abstract void saveCompleteRecord() throws IOException;

    protected void writeBasicCatalogRecord(int recordType) throws IOException {
        writeLong(recordType);
        writePointer();
        writeHeader();
    }

    protected boolean writeBasicCompleteRecord(int recordType) throws IOException {
        if (completeRecordSaved) {
            return false;
        }
        if (parent != null) {
            parent.saveCompleteRecord();
        }
        completeRecordSaved = true;
        stream.switchCompression(entry);
        pointer = stream.getCurrentPointer();
        if (futurePointer != null) {
            futurePointer.init(pointer);
        }
        writeLong(recordType);
        writeHeader();
        return true;
    }

    private void writePointer() throws IOException {
        if (pointer != null) {
            writeLong(pointer.volumeNumber);
            writeLong(pointer.blockOffset);
            writeLong(pointer.recordOffset);
        } else {
            futurePointer = stream.createEmptyPointer();
            stream.write(futurePointer);
        }
    }

    private void writeHeader() throws IOException {
        writeLong(id);
        writeLong(parent == null ? null : ((StandardObjectBuilder) parent).id);
        writeString(entry.getName());
        Long lastModifiedTime = entry.getLastModifiedTime();
        if (lastModifiedTime != null) {
            writeLong(0);
            writeLong(lastModifiedTime);
        }
        Numbers.writeLong(null, stream);
    }

    protected void writeLong(Long value) throws IOException {
        Numbers.writeLong(value, stream);
    }

    protected void writeLong(long value) throws IOException {
        Numbers.writeLong(value, stream);
    }

    protected void writeString(String s) throws IOException {
        byte[] bytes = s.getBytes(Charsets.UTF_8);
        writeLong(bytes.length);
        stream.write(bytes);
    }
}
