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

import com.google.common.collect.Lists;
import org.b1.pack.api.common.FileBuilder;
import org.b1.pack.api.common.FolderBuilder;
import org.b1.pack.api.common.PackEntry;
import org.b1.pack.api.writer.WriterProvider;
import org.b1.pack.standard.common.Constants;
import org.b1.pack.standard.common.Numbers;
import org.b1.pack.standard.common.PbRecordPointer;
import org.b1.pack.standard.common.RecordPointer;

import java.io.IOException;
import java.util.List;

class RecordWriter implements FolderBuilder {

    private final List<StandardObjectBuilder> builderList = Lists.newArrayList();
    private final PackOutputStream packOutputStream;
    private long objectCount;
    private boolean catalogMode;
    private PbRecordPointer nextCatalogPointer;

    public RecordWriter(WriterProvider provider) {
        packOutputStream = new PackOutputStream(provider);
    }

    @Override
    public FileBuilder addFile(PackEntry entry, Long size) throws IOException {
        return createFileBuilder(null, entry, size);
    }

    @Override
    public FolderBuilder addFolder(PackEntry entry) throws IOException {
        return createFolderBuilder(null, entry);
    }

    @Override
    public void save() throws IOException {
        save(true);
    }

    public PackOutputStream getPackOutputStream() {
        return packOutputStream;
    }

    public StandardFileBuilder createFileBuilder(StandardFolderBuilder parent, PackEntry entry, Long size) throws IOException {
        StandardFileBuilder builder = new StandardFileBuilder(++objectCount, this, parent, entry, size);
        builderList.add(builder);
        return builder;
    }

    public FolderBuilder createFolderBuilder(StandardFolderBuilder parent, PackEntry entry) throws IOException {
        StandardFolderBuilder builder = new StandardFolderBuilder(++objectCount, this, parent, entry);
        builderList.add(builder);
        return builder;
    }

    public void close() throws IOException {
        packOutputStream.setObjectCount(objectCount);
        save(false);
        Numbers.writeLong(null, packOutputStream);
        packOutputStream.close();
    }

    public void cleanup() {
        packOutputStream.cleanup();
    }

    private void save(boolean intermediate) throws IOException {
        if (intermediate && builderList.isEmpty()) {
            return;
        }
        if (packOutputStream.isSeekable()) {
            saveCatalogRecords(false);
            saveCompleteRecords();
        } else {
            saveCompleteRecords();
            if (intermediate) return;
            saveCatalogRecords(true);
        }
        packOutputStream.setCompressible(false);
        setCatalogMode(false);
        builderList.clear();
        packOutputStream.save();
    }

    private void saveCatalogRecords(boolean compressed) throws IOException {
        setCatalogMode(compressed);
        for (StandardObjectBuilder builder : builderList) {
            builder.saveCatalogRecord();
        }
    }

    private void saveCompleteRecords() throws IOException {
        setContentMode();
        packOutputStream.setCompressible(true);
        for (StandardObjectBuilder builder : builderList) {
            builder.saveCompleteRecord();
        }
    }

    private void setCatalogMode(boolean compressed) throws IOException {
        RecordPointer pointer = packOutputStream.startCatalog(compressed);
        if (nextCatalogPointer != null) {
            nextCatalogPointer.init(pointer);
            nextCatalogPointer = null;
        }
        catalogMode = true;
    }

    public void setContentMode() throws IOException {
        if (catalogMode && nextCatalogPointer == null) {
            nextCatalogPointer = packOutputStream.createEmptyPointer();
            Numbers.writeLong(Constants.RECORD_POINTER, packOutputStream);
            packOutputStream.write(nextCatalogPointer);
        }
        catalogMode = false;
    }
}
