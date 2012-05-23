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

import org.b1.pack.standard.common.RecordPointer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HeaderSetTest {

    @Test
    public void test_head() throws Exception {
        HeaderSet set = new HeaderSet("b1:as v:0.1 a:qwerty n:1 t:100");
        assertEquals(0.1d, set.getSchemaVersion(), 0);
        assertEquals("qwerty", set.getArchiveId());
        assertEquals(1, set.getVolumeNumber().intValue());
        assertEquals(100, set.getObjectTotal().longValue());
    }

    @Test
    public void test_tail() throws Exception {
        HeaderSet set = new HeaderSet("c:11/22/33   b1:ve");
        RecordPointer pointer = set.getCatalogPointer();
        assertEquals(11, pointer.volumeNumber);
        assertEquals(22, pointer.blockOffset);
        assertEquals(33, pointer.recordOffset);
    }
}
