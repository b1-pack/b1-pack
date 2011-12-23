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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.b1.pack.api.builder.PbVolume;
import org.b1.pack.api.builder.Writable;
import org.b1.pack.api.common.PackException;
import org.b1.pack.standard.common.Constants;
import org.b1.pack.standard.common.RecordPointer;
import org.b1.pack.standard.common.VolumeNameExpert;
import org.b1.pack.standard.common.Volumes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class VolumeBuilder {

    private final List<CompositeWritable> volumeContents = Lists.newArrayList();
    private final String archiveId = Volumes.createArchiveId();
    private final String packName;
    private final long volumeSize;
    private final Map<Writable, PbRecordPointer> pointerMap;
    private final RecordPointer catalogPointer;
    private final long volumeLimit;

    private long volumeNumber;
    private CompositeWritable volumeContent;

    public VolumeBuilder(String packName, long volumeSize, Map<Writable, PbRecordPointer> pointerMap, long objectCount) {
        this.packName = packName;
        this.volumeSize = volumeSize;
        this.pointerMap = pointerMap;
        initVolume(objectCount);
        catalogPointer = new RecordPointer(volumeNumber, volumeContent.getSize(), 0);
        volumeLimit = volumeSize == 0 ? 0 : volumeSize - PbInt.NULL.getSize() - Volumes.createVolumeTail(false, catalogPointer, 0).length;
    }

    public void addContent(Writable content) {
        Preconditions.checkNotNull(volumeContent);
        long contentOffset = 0;
        while (contentOffset < content.getSize()) {
            long chunkSize = addChunk(content, contentOffset);
            if (chunkSize == 0) {
                completeVolume(false);
                initVolume(null);
                chunkSize = addChunk(content, contentOffset);
                if (chunkSize <= 0) {
                    throw new PackException("Volume size too small");
                }
            }
            contentOffset += chunkSize;
        }
    }

    public List<PbVolume> getVolumes() {
        if (volumeContent != null) {
            completeVolume(true);
        }
        int volumeCount = volumeContents.size();
        VolumeNameExpert nameExpert = new VolumeNameExpert(packName, volumeLimit == 0 ? 0 : volumeCount);
        List<PbVolume> result = Lists.newArrayListWithCapacity(volumeCount);
        for (int i = 0; i < volumeCount; i++) {
            result.add(new StandardPbVolume(nameExpert.getVolumeName(i + 1), volumeContents.get(i)));
        }
        return result;
    }

    private void initVolume(@Nullable Long objectCount) {
        volumeNumber++;
        volumeContent = new CompositeWritable();
        volumeContent.add(new ByteArrayWritable(Volumes.createVolumeHead(archiveId, volumeNumber, objectCount)));
    }

    private void completeVolume(boolean lastVolume) {
        volumeContent.add(PbInt.NULL);
        long minSize = lastVolume ? 0 : volumeSize - volumeContent.getSize();
        volumeContent.add(new ByteArrayWritable(Volumes.createVolumeTail(lastVolume, catalogPointer, minSize)));
        volumeContents.add(volumeContent);
        volumeContent = null;
    }

    private long addChunk(Writable content, long contentOffset) {
        long chunkSize = Math.min(content.getSize() - contentOffset, Constants.MAX_CHUNK_SIZE);
        Writable chunk = new PartialWritable(content, contentOffset, contentOffset + chunkSize);
        PbBlock block = new PbBlock(new PbAdler32Block(chunk));
        if (volumeLimit != 0) {
            long freeSpace = volumeLimit - volumeContent.getSize();
            if (block.getSize() > freeSpace) {
                chunkSize -= block.getSize() - freeSpace;
                if (chunkSize <= 0) {
                    return 0;
                }
                chunk = new PartialWritable(content, contentOffset, contentOffset + chunkSize);
                block = new PbBlock(new PbAdler32Block(chunk));
            }
        }
        if (contentOffset == 0) {
            updatePointers(content);
        }
        volumeContent.add(block);
        return chunk.getSize();
    }

    private void updatePointers(Writable content) {
        PbRecordPointer pointer = pointerMap.get(content);
        if (pointer != null) {
            pointer.setVolumeNumber(volumeNumber);
            pointer.setBlockOffset(volumeContent.getSize());
            pointer.setRecordOffset(0);
        }
    }
}
