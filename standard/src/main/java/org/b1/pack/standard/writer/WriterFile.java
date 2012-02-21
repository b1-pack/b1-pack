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

import com.google.common.base.Preconditions;
import com.google.common.io.CountingOutputStream;
import org.b1.pack.api.writer.WriterContent;
import org.b1.pack.api.writer.WriterEntry;
import org.b1.pack.standard.common.PbMutableInt;
import org.b1.pack.standard.common.Numbers;
import org.b1.pack.standard.maker.ChunkedOutputStream;

import java.io.IOException;

import static org.b1.pack.standard.common.Constants.*;
import static org.b1.pack.standard.common.Numbers.MAX_LONG_SIZE;

class WriterFile extends WriterObject {

    private final WriterContent content;
    private Long size;
    private PbMutableInt futureSize;

    public WriterFile(long id, WriterFolder parent, WriterEntry entry, WriterContent content) throws IOException {
        super(id, parent, entry);
        this.content = content;
        size = content.getSize();
    }

    @Override
    public void saveCatalogRecord(RecordWriter writer) throws IOException {
        writeBasicCatalogRecord(CATALOG_FILE, writer);
        if (size != null) {
            Numbers.writeLong(size, writer);
        } else {
            futureSize = new PbMutableInt(MAX_LONG_SIZE);
            writer.write(futureSize);
        }
    }

    @Override
    public void saveCompleteRecord(RecordWriter writer) throws IOException {
        if (writeBasicCompleteRecord(COMPLETE_FILE, writer)) {
            if (size != null) {
                writeFixedSizeContent(writer);
            } else {
                writeChunkedContent(writer);
            }
        }
    }

    private void writeFixedSizeContent(RecordWriter writer) throws IOException {
        Numbers.writeLong(size, writer);
        CountingOutputStream stream = new CountingOutputStream(writer);
        content.writeTo(stream);
        Preconditions.checkState(stream.getCount() == size, "Content size does not match");
        Numbers.writeLong(0, writer);
    }

    private void writeChunkedContent(RecordWriter writer) throws IOException {
        ChunkedOutputStream chunkedOutputStream = new ChunkedOutputStream(MAX_CHUNK_SIZE, writer);
        CountingOutputStream stream = new CountingOutputStream(chunkedOutputStream);
        content.writeTo(stream);
        size = stream.getCount();
        chunkedOutputStream.close();
        if (futureSize != null) futureSize.setValue(size);
    }
}
