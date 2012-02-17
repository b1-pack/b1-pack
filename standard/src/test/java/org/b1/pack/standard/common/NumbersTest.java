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

import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class NumbersTest {

    @Test
    public void testWriteLong() throws Exception {
        checkWrite(new byte[]{0}, 0L);
        checkWrite(new byte[]{1}, null);
        checkWrite(new byte[]{2}, 1L);
        checkWrite(new byte[]{3}, -1L);
        checkWrite(new byte[]{126}, 63L);
        checkWrite(new byte[]{127}, -63L);
        checkWrite(new byte[]{-128, 1}, 64L);
        checkWrite(new byte[]{-127, 1}, -64L);
        assertEquals(10, Numbers.getSerializedSize(Long.MAX_VALUE));
        assertEquals(10, Numbers.getSerializedSize(Long.MIN_VALUE));
    }

    @Test
    public void testReadLong() throws Exception {
        checkRead(new byte[]{0}, 0L);
        checkRead(new byte[]{1}, null);
        checkRead(new byte[]{2}, 1L);
        checkRead(new byte[]{3}, -1L);
    }

    @Test
    public void testWriteLong_ReadLong() throws Exception {
        checkWriteRead(0L, null, 1L, -1L, 1000L, -1000L, Long.MAX_VALUE, Long.MIN_VALUE);
    }

    @Test
    public void testWriteLong_Size() throws Exception {
        assertArrayEquals(new byte[]{-128, 1}, Numbers.serializeLong(64L, 2));
        assertArrayEquals(new byte[]{-128, -127, 0}, Numbers.serializeLong(64L, 3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWriteLong_SmallSize() throws Exception {
        Numbers.serializeLong(64L, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadLong_Large() throws Exception {
        byte[] maxBytes = Numbers.serializeLong(Long.MAX_VALUE);
        maxBytes[maxBytes.length - 1]++;
        Long result = readLong(maxBytes);
        Assert.fail("Expected overflow but got" + result);
    }

    private static void checkWrite(byte[] expected, @Nullable Long value) throws IOException {
        assertArrayEquals(expected, Numbers.serializeLong(value));
    }

    private static void checkRead(byte[] value, @Nullable Long expected) throws IOException {
        assertEquals(expected, readLong(value));
    }

    private static void checkWriteRead(Long... values) throws IOException {
        for (Long expected : values) {
            assertEquals(expected, readLong(Numbers.serializeLong(expected)));
            assertEquals(expected, readLong(Numbers.serializeLong(expected, 15)));
        }
    }

    private static Long readLong(byte[] value) throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream(value);
        Long actual = Numbers.readLong(stream);
        assertEquals(0, stream.available());
        return actual;
    }
}
