/*
 * Copyright 2011 b1.org
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

package org.b1.pack.standard.maker;

import org.b1.pack.api.maker.PmFolder;
import org.b1.pack.standard.common.Constants;
import org.b1.pack.standard.common.ObjectKey;

import java.io.IOException;
import java.io.OutputStream;

public class MakerFolder extends MakerObject {

    public MakerFolder(long id, PmFolder folder) {
        super(id, folder);
    }

    public void writeCompleteRecord(ObjectKey key, PackRecordStream stream) throws IOException {
        writeCompleteRecordPart(Constants.COMPLETE_FOLDER, key, stream);
    }

    public void writeCatalogRecord(ObjectKey key, OutputStream stream) throws IOException {
        writeCatalogRecordPart(Constants.CATALOG_FOLDER, key, stream);
    }
}
