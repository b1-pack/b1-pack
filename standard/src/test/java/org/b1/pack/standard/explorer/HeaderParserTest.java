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

import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;

public class HeaderParserTest {

    private final HeaderParser parser = new HeaderParser();

    @Test
    public void testParse_head() throws Exception {
        parser.parse("b1:as v:0.1 a:qwerty n:1 t:100");
        Assert.assertEquals(Arrays.asList("b1:as", "v:0.1", "a:qwerty", "n:1", "t:100"), parser.getItems());
    }

    @Test
    public void testParse_tail() throws Exception {
        parser.parse("c:11/22/33   b1:ae");
        Assert.assertEquals(Arrays.asList("c:11/22/33", "b1:ae"), parser.getItems());
    }

    @Test
    public void testParse_quoted() throws Exception {
        parser.parse("aaa 'bbb' 'cc''c' ''''");
        Assert.assertEquals(Arrays.asList("aaa", "bbb", "cc'c", "'"), parser.getItems());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_quotedNotClosed() throws Exception {
        parser.parse("aaa 'cc''c");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_quotedNotSeparated() throws Exception {
        parser.parse("aaa 'ccc'd eee");
    }
}
