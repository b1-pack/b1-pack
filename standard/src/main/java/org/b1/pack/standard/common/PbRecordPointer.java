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

package org.b1.pack.standard.common;

import org.b1.pack.api.builder.Writable;

import java.io.IOException;
import java.io.OutputStream;

public class PbRecordPointer implements Writable {

    private final PbMutableInt volumeNumber;
    private final PbMutableInt blockOffset;
    private final PbMutableInt recordOffset;
    private final CompositeWritable writable;

    public PbRecordPointer(int volumeNumberSize, int blockOffsetSize, int recordOffsetSize) {
        volumeNumber = new PbMutableInt(volumeNumberSize);
        blockOffset = new PbMutableInt(blockOffsetSize);
        recordOffset = new PbMutableInt(recordOffsetSize);
        writable = new CompositeWritable(volumeNumber, blockOffset, recordOffset);
    }

    public void init(RecordPointer pointer) {
        setVolumeNumber(pointer.volumeNumber);
        setBlockOffset(pointer.blockOffset);
        setRecordOffset(pointer.recordOffset);
    }

    public void setVolumeNumber(long volumeNumber) {
        this.volumeNumber.setValue(volumeNumber);
    }

    public void setBlockOffset(long blockOffset) {
        this.blockOffset.setValue(blockOffset);
    }

    public void setRecordOffset(long recordOffset) {
        this.recordOffset.setValue(recordOffset);
    }

    @Override
    public long getSize() {
        return writable.getSize();
    }

    @Override
    public void writeTo(OutputStream stream, long start, long end) throws IOException {
        writable.writeTo(stream, start, end);
    }
}
