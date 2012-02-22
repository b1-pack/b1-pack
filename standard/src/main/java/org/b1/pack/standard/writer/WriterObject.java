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

abstract class WriterObject {

    private final long id;
    private final WriterFolder parent;
    private final String name;
    private final Long lastModifiedTime;
    private final boolean compressible;
    private boolean completeRecordSaved;
    private RecordPointer pointer;
    private PbRecordPointer futurePointer;

    protected WriterObject(long id, WriterFolder parent, WriterEntry entry) {
        this.id = id;
        this.parent = parent;
        this.name = entry.getName();
        this.lastModifiedTime = entry.getLastModifiedTime();
        this.compressible = entry.isCompressible();
    }

    public abstract void saveCatalogRecord(RecordWriter recordWriter) throws IOException;

    public abstract void saveCompleteRecord(RecordWriter recordWriter) throws IOException;

    protected void writeBasicCatalogRecord(int recordType, RecordWriter recordWriter) throws IOException {
        Numbers.writeLong(recordType, recordWriter);
        writePointer(recordWriter);
        writeHeader(recordWriter);
    }

    protected boolean writeBasicCompleteRecord(int recordType, RecordWriter recordWriter) throws IOException {
        if (completeRecordSaved) {
            return false;
        }
        if (parent != null) {
            parent.saveCompleteRecord(recordWriter);
        }
        completeRecordSaved = true;
        recordWriter.setCompressible(compressible);
        pointer = recordWriter.getCurrentPointer();
        if (futurePointer != null) {
            futurePointer.init(pointer);
        }
        Numbers.writeLong(recordType, recordWriter);
        writeHeader(recordWriter);
        return true;
    }

    private void writePointer(RecordWriter recordWriter) throws IOException {
        if (pointer != null) {
            Numbers.writeLong(pointer.volumeNumber, recordWriter);
            Numbers.writeLong(pointer.blockOffset, recordWriter);
            Numbers.writeLong(pointer.recordOffset, recordWriter);
        } else {
            futurePointer = recordWriter.createEmptyPointer();
            recordWriter.write(futurePointer);
        }
    }

    private void writeHeader(RecordWriter recordWriter) throws IOException {
        Numbers.writeLong(id, recordWriter);
        Numbers.writeLong(parent == null ? null : ((WriterObject) parent).id, recordWriter);
        writeString(recordWriter, name);
        if (lastModifiedTime != null) {
            Numbers.writeLong(0, recordWriter);
            Numbers.writeLong(lastModifiedTime, recordWriter);
        }
        Numbers.writeLong(null, recordWriter);
    }

    private static void writeString(RecordWriter recordWriter, String s) throws IOException {
        byte[] bytes = s.getBytes(Charsets.UTF_8);
        Numbers.writeLong(bytes.length, recordWriter);
        recordWriter.write(bytes);
    }
}
