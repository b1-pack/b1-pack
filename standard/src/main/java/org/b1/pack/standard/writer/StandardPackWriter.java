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

package org.b1.pack.standard.writer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.b1.pack.api.writer.PackWriter;
import org.b1.pack.api.writer.PwFile;
import org.b1.pack.api.writer.PwFolder;
import org.b1.pack.api.writer.PwProvider;
import org.b1.pack.standard.common.ObjectKey;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.b1.pack.standard.common.Numbers.writeLong;

public class StandardPackWriter implements PackWriter {

    private final Map<ObjectKey, WriterObject> objectMap = Maps.newLinkedHashMap();
    private final PackRecordStream recordStream;
    private WriterFile lastFile;
    private boolean progress;
    private long lastId;

    public StandardPackWriter(PwProvider provider) throws IOException {
        recordStream = new PackRecordStream(provider);
    }

    @Override
    public void addFolder(PwFolder folder) throws IOException {
        progress = true;
        checkLastFileWritten();
        saveFolder(createNewKey(folder.getPath()), new WriterFolder(nextId(), folder));
        progress = false;
    }

    @Override
    public OutputStream addFile(PwFile file) throws IOException {
        progress = true;
        checkLastFileWritten();
        OutputStream stream = saveFile(createNewKey(file.getPath()), new WriterFile(nextId(), file));
        progress = false;
        return stream;
    }

    @Override
    public void close() throws IOException {
        try {
            if (!progress) {
                checkLastFileWritten();
                saveCatalog();
            }
        } finally {
            recordStream.close();
        }
        if (!progress) {
            recordStream.completeVolume();
        }
    }

    private void checkLastFileWritten() {
        checkState(lastFile == null || lastFile.isWritten(), "File stream not closed");
    }

    private long nextId() {
        return ++lastId;
    }

    private void saveFolder(ObjectKey key, WriterFolder folder) throws IOException {
        objectMap.put(key, folder);
        folder.writeCompleteRecord(key, recordStream);
    }

    private OutputStream saveFile(ObjectKey key, WriterFile file) throws IOException {
        objectMap.put(key, file);
        lastFile = file;
        return file.writeCompleteRecord(key, recordStream);
    }

    private void saveCatalog() throws IOException {
        recordStream.startCatalog();
        for (Map.Entry<ObjectKey, WriterObject> entry : objectMap.entrySet()) {
            entry.getValue().writeCatalogRecord(entry.getKey(), recordStream);
        }
        writeLong(null, recordStream);
    }

    private ObjectKey createNewKey(Iterable<String> path) throws IOException {
        LinkedList<String> list = Lists.newLinkedList(path);
        String objectName = list.removeLast();
        WriterFolder parent = null;
        for (String folderName : list) {
            ObjectKey key = createKey(parent, folderName);
            parent = (WriterFolder) objectMap.get(key);
            if (parent == null) {
                parent = new WriterFolder(nextId(), null);
                saveFolder(key, parent);
            }
        }
        ObjectKey key = createKey(parent, objectName);
        checkArgument(!objectMap.containsKey(key));
        return key;
    }

    private static ObjectKey createKey(WriterFolder parent, String name) {
        return new ObjectKey(parent == null ? null : parent.getId(), name);
    }
}
