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

import org.b1.pack.api.common.FileBuilder;
import org.b1.pack.api.common.FolderBuilder;
import org.b1.pack.api.common.PackEntry;
import org.b1.pack.standard.common.Constants;

import java.io.IOException;

class StandardFolderBuilder extends StandardObjectBuilder implements FolderBuilder {

    public StandardFolderBuilder(long id, RecordWriter recordWriter, StandardFolderBuilder parent, PackEntry entry) {
        super(id, recordWriter, parent, entry);
    }

    @Override
    public FileBuilder addFile(PackEntry entry, Long size) throws IOException {
        return recordWriter.createFileBuilder(this, entry, size);
    }

    @Override
    public FolderBuilder addFolder(PackEntry entry) throws IOException {
        return recordWriter.createFolderBuilder(this, entry);
    }

    @Override
    public void save() throws IOException {
        // no-op
    }

    @Override
    public void saveCatalogRecord() throws IOException {
        writeBasicCatalogRecord(Constants.CATALOG_FOLDER);
    }

    @Override
    public void saveCompleteRecord() throws IOException {
        writeBasicCompleteRecord(Constants.COMPLETE_FOLDER);
    }
}
