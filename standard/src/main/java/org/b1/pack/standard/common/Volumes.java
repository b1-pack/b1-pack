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

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Bytes;

import javax.annotation.Nullable;
import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.util.UUID;

public class Volumes {

    public static final String B1 = "b1";
    public static final String V = "v";
    public static final String A = "a";
    public static final String N = "n";
    public static final String T = "t";
    public static final String C = "c";
    public static final String AS = "as";
    public static final String VS = "vs";
    public static final char COLON = ':';
    public static final String SLASH = "/";
    public static final String B1_AS = "b1:as";
    public static final String B1_VS = "b1:vs";
    public static final String B1_AE = "b1:ae";
    public static final String B1_VE = "b1:ve";

    public static final double SCHEMA_VERSION = 0.2;
    public static final byte SEPARATOR_BYTE = (byte) 0xFC;
    private static final byte[] SEPARATOR = new byte[]{SEPARATOR_BYTE};


    public static String createArchiveId() {
        UUID uuid = UUID.randomUUID();
        return DatatypeConverter.printBase64Binary(ByteBuffer.allocate(16)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits()).array()).replace("=", "");
    }

    public static byte[] createVolumeHead(String archiveId, long volumeNumber, @Nullable Long objectCount, String method) {
        StringBuilder builder = new StringBuilder(volumeNumber == 1 ? B1_AS : B1_VS)
                .append(" v:").append(SCHEMA_VERSION)
                .append(" a:").append(archiveId)
                .append(" n:").append(volumeNumber);
        if (volumeNumber == 1) {
            if (objectCount != null) {
                builder.append(" t:").append(objectCount);
            }
            if (method != null) {
                Preconditions.checkArgument(!method.contains(" "));
                builder.append(" m:").append(method);
            }
        }
        return Bytes.concat(builder.toString().getBytes(Charsets.UTF_8), SEPARATOR);
    }

    public static byte[] createVolumeTail(boolean lastVolume, RecordPointer catalogPointer, long minSize) {
        StringBuilder builder = new StringBuilder();
        if (catalogPointer != null) {
            builder.append("c:")
                    .append(catalogPointer.volumeNumber)
                    .append('/')
                    .append(catalogPointer.blockOffset)
                    .append('/')
                    .append(catalogPointer.recordOffset)
                    .append(' ');
        }
        String signature = lastVolume ? B1_AE : B1_VE;
        while (builder.length() < minSize - signature.length() - 1) {
            builder.append(' ');
        }
        builder.append(signature);
        return Bytes.concat(SEPARATOR, builder.toString().getBytes(Charsets.UTF_8));
    }
}
