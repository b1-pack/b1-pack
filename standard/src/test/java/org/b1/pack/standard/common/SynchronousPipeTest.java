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

import com.google.common.base.Charsets;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static junit.framework.Assert.*;

public class SynchronousPipeTest {

    private static final String TEST_VALUE = "0123456789";
    public static final byte[] TEST_BYTES = TEST_VALUE.getBytes(Charsets.UTF_8);

    private final SynchronousPipe pipe = new SynchronousPipe();
    private volatile boolean readerStarted;
    private volatile boolean writerStarted;
    private volatile boolean writerClosed;

    @Test
    public void test_read_write() throws Exception {
        ExecutorService service = Executors.newCachedThreadPool();
        Future<Boolean> readerFuture = service.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                byte[] buffer = new byte[1000];
                readerStarted = true;
                assertEquals(0, pipe.inputStream.read(buffer, 5, 0));
                assertFalse(writerStarted);
                assertEquals(2, pipe.inputStream.read(buffer, 10, 2));
                assertEquals("01", new String(buffer, 10, 2, Charsets.UTF_8));
                assertTrue(writerStarted);
                assertEquals(8, pipe.inputStream.read(buffer, 1, 10));
                assertEquals("23456789", new String(buffer, 1, 8, Charsets.UTF_8));
                assertFalse(writerClosed);
                assertEquals(-1, pipe.inputStream.read(buffer, 1, 10));
                assertTrue(writerClosed);
                return true;
            }
        });
        Thread.sleep(100);
        assertTrue(readerStarted);
        Future<Boolean> writerFuture = service.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                writerStarted = true;
                pipe.outputStream.write(TEST_BYTES);
                Thread.sleep(100);
                pipe.outputStream.close();
                writerClosed = true;
                return true;
            }
        });
        assertTrue(readerFuture.get());
        assertTrue(writerFuture.get());
    }

    @Test
    public void test_singleBytes() throws Exception {
        ExecutorService service = Executors.newCachedThreadPool();
        Future<Boolean> writerFuture = service.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                pipe.outputStream.write(5);
                pipe.outputStream.write(-1);
                pipe.outputStream.close();
                return true;
            }
        });
        Future<Boolean> readerFuture = service.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                assertEquals(5, pipe.inputStream.read());
                assertEquals(255, pipe.inputStream.read());
                assertEquals(-1, pipe.inputStream.read());
                return true;
            }
        });
        assertTrue(readerFuture.get());
        assertTrue(writerFuture.get());
    }

    @Test
    public void test_inputClose_inputRead() throws Exception {
        pipe.inputStream.close();
        try {
            //noinspection ResultOfMethodCallIgnored
            pipe.inputStream.read();
            fail();
        } catch (IOException e) {
            assertEquals("Input closed", e.getMessage());
        }
    }

    @Test
    public void test_outputClose_inputRead() throws Exception {
        pipe.outputStream.close();
        assertEquals(-1, pipe.inputStream.read());
    }

    @Test
    public void test_inputClose_outputWrite() throws Exception {
        pipe.inputStream.close();
        try {
            pipe.outputStream.write(TEST_BYTES);
            fail();
        } catch (IOException e) {
            assertEquals("Input closed", e.getMessage());
        }
    }

    @Test
    public void test_outputClose_outputWrite() throws Exception {
        pipe.outputStream.close();
        try {
            pipe.outputStream.write(TEST_BYTES);
            fail();
        } catch (IOException e) {
            assertEquals("Output closed", e.getMessage());
        }
    }
}