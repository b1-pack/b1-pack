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

package org.b1.pack.standard.explorer;

import org.b1.pack.standard.common.RecordPointer;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Long.parseLong;
import static org.b1.pack.standard.common.Volumes.*;

public class HeaderSet {

    private String headerType;
    private Double schemaVersion;
    private String archiveId;
    private Long volumeNumber;
    private Long objectTotal;
    private RecordPointer catalogPointer;

    public HeaderSet(String s) {
        HeaderParser parser = new HeaderParser();
        parser.parse(s);
        for (String item : parser.getItems()) {
            int i = item.indexOf(COLON);
            if (i >= 0) {
                init(item.substring(0, i), item.substring(i + 1));
            }
        }
    }

    public String getHeaderType() {
        return headerType;
    }

    public Double getSchemaVersion() {
        return schemaVersion;
    }

    public String getArchiveId() {
        return archiveId;
    }

    public Long getVolumeNumber() {
        return volumeNumber;
    }

    public Long getObjectTotal() {
        return objectTotal;
    }

    public RecordPointer getCatalogPointer() {
        return catalogPointer;
    }

    public void setCatalogPointer(RecordPointer catalogPointer) {
        this.catalogPointer = catalogPointer;
    }

    private void init(String key, String value) {
        if (key.equals(B1)) {
            checkArgument(headerType == null);
            headerType = value;
        } else if (key.equals(V)) {
            checkArgument(schemaVersion == null);
            schemaVersion = Double.valueOf(value);
        } else if (key.equals(A)) {
            checkArgument(archiveId == null);
            archiveId = value;
        } else if (key.equals(N)) {
            checkArgument(volumeNumber == null);
            volumeNumber = Long.valueOf(value);
        } else if (key.equals(T)) {
            checkArgument(objectTotal == null);
            objectTotal = Long.valueOf(value);
        } else if (key.equals(C)) {
            checkArgument(catalogPointer == null);
            String[] values = value.split(SLASH);
            checkArgument(values.length == 3);
            catalogPointer = new RecordPointer(parseLong(values[0]), parseLong(values[1]), parseLong(values[2]));
        }
    }
}
