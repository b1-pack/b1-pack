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

package org.b1.pack.standard.reader;

import com.google.common.base.Preconditions;
import org.b1.pack.standard.common.RecordPointer;
import org.b1.pack.standard.common.Volumes;

import javax.xml.bind.DatatypeConverter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class HeaderSet {

    private static final Pattern ENCRYPTION_METHOD_PATTERN = Pattern.compile("1/(\\d+)");

    private Double schemaVersion;
    private String archiveId;
    private Long volumeNumber;
    private Long objectTotal;
    private RecordPointer catalogPointer;
    private Integer iterationCount;
    private byte[] encryptedHeaders;

    public HeaderSet(String s) {
        HeaderParser parser = new HeaderParser();
        parser.parse(s);
        for (String item : parser.getItems()) {
            int i = item.indexOf(Volumes.COLON);
            if (i >= 0) {
                init(item.substring(0, i), item.substring(i + 1));
            }
        }
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

    public Integer getIterationCount() {
        return iterationCount;
    }

    public byte[] getEncryptedHeaders() {
        return encryptedHeaders;
    }

    private void init(String key, String value) {
        if (key.equals(Volumes.V)) {
            Preconditions.checkArgument(schemaVersion == null);
            schemaVersion = Double.valueOf(value);
        } else if (key.equals(Volumes.A)) {
            Preconditions.checkArgument(archiveId == null);
            archiveId = value;
        } else if (key.equals(Volumes.N)) {
            Preconditions.checkArgument(volumeNumber == null);
            volumeNumber = Long.valueOf(value);
        } else if (key.equals(Volumes.T)) {
            Preconditions.checkArgument(objectTotal == null);
            objectTotal = Long.valueOf(value);
        } else if (key.equals(Volumes.C)) {
            Preconditions.checkArgument(catalogPointer == null);
            String[] values = value.split(Volumes.SLASH);
            Preconditions.checkArgument(values.length == 3);
            catalogPointer = new RecordPointer(Long.parseLong(values[0]), Long.parseLong(values[1]), Long.parseLong(values[2]));
        } else if (key.equals(Volumes.E)) {
            Preconditions.checkArgument(iterationCount == null);
            Matcher matcher = ENCRYPTION_METHOD_PATTERN.matcher(value);
            Preconditions.checkArgument(matcher.matches());
            iterationCount = Integer.parseInt(matcher.group(1));
        } else if (key.equals(Volumes.X)) {
            Preconditions.checkArgument(encryptedHeaders == null);
            encryptedHeaders = DatatypeConverter.parseBase64Binary(value);
        }
    }
}
