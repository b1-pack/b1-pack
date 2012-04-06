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
import org.b1.pack.api.common.FileBuilder;
import org.b1.pack.api.common.FileContent;
import org.b1.pack.api.common.PackEntry;
import org.b1.pack.standard.common.Constants;
import org.b1.pack.standard.common.Numbers;
import org.b1.pack.standard.common.PbMutableInt;

import java.io.IOException;
import java.io.OutputStream;

class StandardFileBuilder extends StandardObjectBuilder implements FileBuilder {

    private Long size;
    private FileContent content;
    private PbMutableInt futureSize;

    public StandardFileBuilder(long id, RecordWriter recordWriter, StandardFolderBuilder parent, PackEntry entry, Long size) throws IOException {
        super(id, recordWriter, parent, entry);
        this.size = size;
    }

    @Override
    public void setContent(FileContent content) {
        this.content = content;
    }

    @Override
    public void save() throws IOException {
        if (!completeRecordSaved) {
            recordWriter.setContentMode();
            saveCompleteRecord();
        }
    }

    @Override
    public void saveCatalogRecord() throws IOException {
        writeBasicCatalogRecord(Constants.CATALOG_FILE);
        if (size != null) {
            writeLong(size);
        } else {
            futureSize = new PbMutableInt(Numbers.MAX_LONG_SIZE);
            stream.write(futureSize);
        }
    }

    @Override
    public void saveCompleteRecord() throws IOException {
        if (writeBasicCompleteRecord(Constants.COMPLETE_FILE)) {
            if (size != null) {
                writeFixedSizeContent();
            } else {
                writeChunkedContent();
            }
        }
    }

    protected void switchCompression() throws IOException {
        stream.switchCompression(entry);
    }

    private void writeFixedSizeContent() throws IOException {
        Numbers.writeLong(size, stream);
        Preconditions.checkState(writeContent(stream) == size, "Content size does not match");
        Numbers.writeLong(0, stream);
    }

    private void writeChunkedContent() throws IOException {
        ChunkedOutputStream chunkedOutputStream = new ChunkedOutputStream(Constants.MAX_CHUNK_SIZE, stream);
        size = writeContent(chunkedOutputStream);
        chunkedOutputStream.close();
        if (futureSize != null) futureSize.setValue(size);
    }

    private long writeContent(OutputStream s) throws IOException {
        ContentOutputStream stream = new ContentOutputStream(s);
        Preconditions.checkNotNull(content, "No file content").writeTo(stream, 0, null);
        return stream.getCount();
    }
}
