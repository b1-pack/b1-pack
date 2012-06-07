/*
 * Copyright 2012 b1.org
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

import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CompositeWritableTest {

    @Test
    public void testWriteTo_1() throws Exception {
        CompositeWritable writable = new CompositeWritable();
        for (int i = 0; i < 10; i++) {
            writable.add(new ByteArrayWritable(String.valueOf(i).getBytes()));
        }
        assertEquals(10, writable.getSize());

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        writable.writeTo(stream, 0, 10);
        assertEquals("0123456789", stream.toString());

        stream.reset();
        writable.writeTo(stream, 1, 10);
        assertEquals("123456789", stream.toString());

        stream.reset();
        writable.writeTo(stream, 0, 5);
        assertEquals("01234", stream.toString());

        stream.reset();
        writable.writeTo(stream, 7, 8);
        assertEquals("7", stream.toString());

        stream.reset();
        writable.writeTo(stream, 5, 5);
        assertEquals("", stream.toString());

        try {
            stream.reset();
            writable.writeTo(stream, 5, 4);
            fail();
        } catch (Exception e) {
            //ok
        }

        try {
            stream.reset();
            writable.writeTo(stream, 5, 20);
            fail();
        } catch (Exception e) {
            //ok
        }
    }

    @Test
    public void testWriteTo_2() throws Exception {
        CompositeWritable writable = new CompositeWritable();
        for (int i = 0; i < 10; i++) {
            writable.add(new ByteArrayWritable((String.valueOf(i) + i).getBytes()));
        }
        assertEquals(20, writable.getSize());

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        writable.writeTo(stream, 0, 20);
        assertEquals("00112233445566778899", stream.toString());

        stream.reset();
        writable.writeTo(stream, 0, 1);
        assertEquals("0", stream.toString());

        stream.reset();
        writable.writeTo(stream, 1, 10);
        assertEquals("011223344", stream.toString());

        stream.reset();
        writable.writeTo(stream, 1, 11);
        assertEquals("0112233445", stream.toString());
    }
}
