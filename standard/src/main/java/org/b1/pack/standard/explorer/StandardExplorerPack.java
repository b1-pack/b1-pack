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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import org.b1.pack.api.explorer.ExplorerObject;
import org.b1.pack.api.explorer.ExplorerPack;
import org.b1.pack.api.explorer.ExplorerVisitor;
import org.b1.pack.standard.common.Constants;
import org.b1.pack.standard.common.Numbers;
import org.b1.pack.standard.common.PermanentList;
import org.b1.pack.standard.common.RecordPointer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class StandardExplorerPack extends ExplorerPack {

    private final VolumeManager volumeManager;
    private final PackNavigator navigator;

    public StandardExplorerPack(VolumeManager volumeManager) {
        this.volumeManager = volumeManager;
        this.navigator = new PackNavigator(volumeManager);
    }

    @Override
    public void listObjects(ExplorerVisitor visitor) throws IOException {
        for (ExplorerObject object : getObjects()) {
            object.accept(visitor);
        }
    }

    private List<ExplorerObject> getObjects() throws IOException {
        List<ExplorerObject> objects = Lists.newArrayList();
        Map<Long, PermanentList<String>> pathMap = Maps.newHashMap();
        HeaderSet headerSet = volumeManager.getHeaderSet();
        Long total = headerSet.getObjectTotal();
        InputStream stream = navigator.getRecordStream(headerSet.getCatalogPointer());
        try {
            Long recordType;
            while ((total == null || objects.size() < total) && (recordType = Numbers.readLong(stream)) != null) {
                switch (Ints.checkedCast(recordType)) {
                    case Constants.RECORD_POINTER:
                        RecordPointer catalogPointer = readPointer(stream);
                        stream.close();
                        stream = navigator.getRecordStream(catalogPointer);
                        break;
                    case Constants.CATALOG_FILE:
                        RecordPointer filePointer = readPointer(stream);
                        RecordHeader fileHeader = RecordHeader.readRecordHeader(stream);
                        Long fileSize = Numbers.readLong(stream);
                        objects.add(new StandardExplorerFile(navigator, filePointer, fileHeader, getPath(pathMap, fileHeader), fileSize));
                        break;
                    case Constants.CATALOG_FOLDER:
                        readPointer(stream); // ignore for now
                        RecordHeader folderHeader = RecordHeader.readRecordHeader(stream);
                        objects.add(new StandardExplorerFolder(folderHeader, getPath(pathMap, folderHeader)));
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid entry");
                }
            }
        } finally {
            stream.close();
        }
        return objects;
    }

    public void close() throws IOException {
        navigator.close();
    }

    private static List<String> getPath(Map<Long, PermanentList<String>> map, RecordHeader header) {
        PermanentList<String> path = header.parentId == null
                ? PermanentList.of(header.name)
                : Preconditions.checkNotNull(map.get(header.parentId), "Invalid reference").with(header.name);
        Preconditions.checkArgument(map.put(header.id, path) == null, "Double entry");
        return path;
    }

    private static RecordPointer readPointer(InputStream stream) throws IOException {
        return new RecordPointer(Numbers.readLong(stream), Numbers.readLong(stream), Numbers.readLong(stream));
    }
}
