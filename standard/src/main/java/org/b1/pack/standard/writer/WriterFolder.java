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

import org.b1.pack.api.writer.WriterEntry;

import java.io.IOException;

import static org.b1.pack.standard.common.Constants.CATALOG_FOLDER;
import static org.b1.pack.standard.common.Constants.COMPLETE_FOLDER;

class WriterFolder extends WriterObject {

    public WriterFolder(long id, WriterFolder parent, WriterEntry entry) {
        super(id, parent, entry);
    }

    @Override
    public void saveCatalogRecord(RecordWriter writer) throws IOException {
        writeBasicCatalogRecord(CATALOG_FOLDER, writer);
    }

    @Override
    public void saveCompleteRecord(RecordWriter writer) throws IOException {
        writeBasicCompleteRecord(COMPLETE_FOLDER, writer);
    }
}
