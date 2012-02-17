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
import org.b1.pack.standard.common.PbRecordPointer;
import org.b1.pack.standard.common.Numbers;
import org.b1.pack.standard.common.RecordPointer;

import java.io.IOException;
import java.io.OutputStream;

abstract class WriterObject {

    protected final long id;
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

    public abstract void saveCatalogRecord(ArchiveWriter writer) throws IOException;

    public abstract void saveCompleteRecord(ArchiveWriter writer) throws IOException;

    protected void writeBasicCatalogRecord(int recordType, ArchiveWriter writer) throws IOException {
        Numbers.writeLong(recordType, writer);
        writePointer(writer);
        writeHeader(writer);
    }

    protected boolean writeBasicCompleteRecord(int recordType, ArchiveWriter writer) throws IOException {
        if (completeRecordSaved) {
            return false;
        }
        if (parent != null) {
            parent.saveCompleteRecord(writer);
        }
        completeRecordSaved = true;
        writer.setCompressible(compressible);
        pointer = writer.getCurrentPointer();
        if (futurePointer != null) {
            futurePointer.init(pointer);
        }
        Numbers.writeLong(recordType, writer);
        writeHeader(writer);
        return true;
    }

    private void writePointer(ArchiveWriter writer) throws IOException {
        if (pointer != null) {
            Numbers.writeLong(pointer.volumeNumber, writer);
            Numbers.writeLong(pointer.blockOffset, writer);
            Numbers.writeLong(pointer.recordOffset, writer);
        } else {
            futurePointer = writer.createEmptyPointer();
            writer.write(futurePointer);
        }
    }

    private void writeHeader(OutputStream stream) throws IOException {
        Numbers.writeLong(id, stream);
        Numbers.writeLong(parent == null ? null : parent.id, stream);
        writeString(stream, name);
        if (lastModifiedTime != null) {
            Numbers.writeLong(0, stream);
            Numbers.writeLong(lastModifiedTime, stream);
        }
        Numbers.writeLong(null, stream);
    }

    private static void writeString(OutputStream stream, String s) throws IOException {
        byte[] bytes = s.getBytes(Charsets.UTF_8);
        Numbers.writeLong(bytes.length, stream);
        stream.write(bytes);
    }
}
