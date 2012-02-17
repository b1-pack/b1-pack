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

package org.b1.pack.standard.builder;

import org.b1.pack.api.builder.Writable;
import org.b1.pack.standard.common.CompositeWritable;
import org.b1.pack.standard.common.Constants;
import org.b1.pack.standard.common.PbInt;
import org.b1.pack.standard.common.PbRecordPointer;

public class PbRecord extends CompositeWritable {

    public PbRecord(PbRecordPointer pointer) {
        this(Constants.RECORD_POINTER, pointer);
    }

    public PbRecord(PbCatalogFile file) {
        this(Constants.CATALOG_FILE, file);
    }

    public PbRecord(PbCompleteFile file) {
        this(Constants.COMPLETE_FILE, file);
    }

    public PbRecord(PbCatalogFolder folder) {
        this(Constants.CATALOG_FOLDER, folder);
    }

    public PbRecord(PbCompleteFolder folder) {
        this(Constants.COMPLETE_FOLDER, folder);
    }

    private PbRecord(long code, Writable writable) {
        super(new PbInt(code), writable);
    }
}
