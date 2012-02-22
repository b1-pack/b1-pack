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
import org.b1.pack.api.writer.WriterContent;
import org.b1.pack.api.writer.WriterEntry;
import org.b1.pack.standard.common.Constants;
import org.b1.pack.standard.common.Numbers;
import org.b1.pack.standard.common.PbMutableInt;
import org.b1.pack.standard.maker.ChunkedOutputStream;

import java.io.IOException;

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
    public void saveCatalogRecord(RecordWriter recordWriter) throws IOException {
        writeBasicCatalogRecord(Constants.CATALOG_FILE, recordWriter);
        if (size != null) {
            Numbers.writeLong(size, recordWriter);
        } else {
            futureSize = new PbMutableInt(Numbers.MAX_LONG_SIZE);
            recordWriter.write(futureSize);
        }
    }

    @Override
    public void saveCompleteRecord(RecordWriter recordWriter) throws IOException {
        if (writeBasicCompleteRecord(Constants.COMPLETE_FILE, recordWriter)) {
            if (size != null) {
                writeFixedSizeContent(recordWriter);
            } else {
                writeChunkedContent(recordWriter);
            }
        }
    }

    private void writeFixedSizeContent(RecordWriter recordWriter) throws IOException {
        Numbers.writeLong(size, recordWriter);
        ContentOutputStream stream = new ContentOutputStream(recordWriter);
        content.writeTo(stream);
        Preconditions.checkState(stream.getCount() == size, "Content size does not match");
        Numbers.writeLong(0, recordWriter);
    }

    private void writeChunkedContent(RecordWriter recordWriter) throws IOException {
        ChunkedOutputStream chunkedOutputStream = new ChunkedOutputStream(Constants.MAX_CHUNK_SIZE, recordWriter);
        ContentOutputStream stream = new ContentOutputStream(recordWriter);
        content.writeTo(stream);
        size = stream.getCount();
        chunkedOutputStream.close();
        if (futureSize != null) futureSize.setValue(size);
    }
}
