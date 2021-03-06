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
import com.google.common.collect.Maps;
import org.b1.pack.api.builder.*;
import org.b1.pack.standard.common.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StandardBuilderPack extends BuilderPack {

    private final Map<ObjectKey, Long> objectIdMap = Maps.newHashMap();
    private final List<PbRecord> catalogRecords = Lists.newArrayList();
    private final List<PbRecord> completeRecords = Lists.newArrayList();
    private final Map<Writable, PbRecordPointer> pointerMap = Maps.newHashMap();
    private final long maxVolumeSize;
    private final int blockOffsetSize;
    private final PbRecordPointer catalogPointer;
    private long objectCount;

    public StandardBuilderPack(BuilderProvider provider) {
        maxVolumeSize = provider.getMaxVolumeSize();
        blockOffsetSize = Numbers.getSerializedSize(maxVolumeSize - 1);
        catalogPointer = createPointer();
        pointerMap.put(PbInt.NULL, catalogPointer);
    }

    @Override
    public void addFolder(BuilderFolder folder) {
        addFolderRecords(createHeader(folder), folder);
    }

    @Override
    public void addFile(BuilderFile file) {
        PbRecordHeader header = createHeader(file);
        PbRecordPointer pointer = createPointer();
        addRecords(pointer,
                new PbRecord(new PbCatalogFile(pointer, header, file.getSize())),
                new PbRecord(new PbCompleteFile(header, file)));
    }

    public List<BuilderVolume> getVolumes() {
        VolumeBuilder builder = new VolumeBuilder(maxVolumeSize, pointerMap, objectCount);
        builder.addContent(createCatalog());
        for (PbRecord record : completeRecords) {
            builder.addContent(record);
        }
        builder.addContent(PbInt.NULL);
        return builder.getVolumes();
    }

    private PbRecordPointer createPointer() {
        return new PbRecordPointer(Numbers.MAX_INT_SIZE, blockOffsetSize, 1);
    }

    private PbRecordHeader createHeader(BuilderObject object) {
        LinkedList<String> path = Lists.newLinkedList(object.getPath());
        String name = path.removeLast();
        Long parentId = null;
        for (String s : path) {
            ObjectKey key = new ObjectKey(parentId, s);
            Long id = objectIdMap.get(key);
            if (id == null) {
                id = createNewId(key);
                addFolderRecords(new PbRecordHeader(id, parentId, s, System.currentTimeMillis()), null);
            }
            parentId = id;
        }
        ObjectKey key = new ObjectKey(parentId, name);
        Preconditions.checkArgument(!objectIdMap.containsKey(key), "Path already exists");
        return new PbRecordHeader(createNewId(key), parentId, name, object.getLastModifiedTime());
    }

    private Long createNewId(ObjectKey key) {
        Long id = ++objectCount;
        objectIdMap.put(key, id);
        return id;
    }

    private void addFolderRecords(PbRecordHeader header, BuilderFolder folder) {
        PbRecordPointer pointer = createPointer();
        addRecords(pointer,
                new PbRecord(new PbCatalogFolder(pointer, header)),
                new PbRecord(new PbCompleteFolder(header, folder)));
    }

    private void addRecords(PbRecordPointer pointer, PbRecord catalogRecord, PbRecord completeRecord) {
        pointerMap.put(completeRecord, pointer);
        catalogRecords.add(catalogRecord);
        completeRecords.add(completeRecord);
    }

    private Writable createCatalog() {
        PbRecord[] catalog = new PbRecord[catalogRecords.size() + 1];
        catalogRecords.toArray(catalog);
        catalog[catalog.length - 1] = new PbRecord(catalogPointer);
        return new CompositeWritable(catalog);
    }
}
