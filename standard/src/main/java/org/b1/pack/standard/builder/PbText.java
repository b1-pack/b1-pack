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
import org.b1.pack.standard.common.ByteArrayWritable;
import org.b1.pack.standard.common.CompositeWritable;
import org.b1.pack.standard.common.PbInt;
import org.b1.pack.standard.common.Volumes;

public class PbText extends CompositeWritable {

    public PbText(String value) {
        super(create(value));
    }

    private static Writable[] create(String value) {
        byte[] utf8 = Volumes.getUtf8Bytes(value);
        return new Writable[]{new PbInt((long) utf8.length), new ByteArrayWritable(utf8)};
    }
}
