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

import com.google.common.io.CountingOutputStream;
import org.b1.pack.api.maker.PmFile;
import org.b1.pack.standard.common.Constants;
import org.b1.pack.standard.common.Numbers;
import org.b1.pack.standard.common.ObjectKey;
import org.b1.pack.standard.common.OutputStreamWrapper;

import java.io.IOException;
import java.io.OutputStream;

public class MakerFile extends MakerObject {

    private Long size;

    public MakerFile(long id, PmFile file) {
        super(id, file);
    }

    public OutputStream writeCompleteRecord(ObjectKey key, final PackRecordStream recordStream) throws IOException {
        writeCompleteRecordPart(Constants.COMPLETE_FILE, key, recordStream);
        return new OutputStreamWrapper<CountingOutputStream>(new CountingOutputStream(new ChunkedOutputStream(Constants.MAX_CHUNK_SIZE, recordStream))) {
            @Override
            public void close() throws IOException {
                super.close();
                long count = stream.getCount();
                Numbers.writeLong(count, recordStream);
                size = count;
            }
        };
    }

    public void writeCatalogRecord(ObjectKey key, OutputStream stream) throws IOException {
        writeCatalogRecordPart(Constants.CATALOG_FILE, key, stream);
        Numbers.writeLong(size, stream);
    }

    public boolean isWritten() {
        return size != null;
    }
}
