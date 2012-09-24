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
package org.b1.pack.standard.reader;

import SevenZip.Compression.LZMA.Decoder;
import SevenZip.Compression.LZMA.Encoder;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;

class LzmaDecoder {

    public static final int PROPERTIES_SIZE = Encoder.kPropSize;

    private final Decoder decoder = new Decoder();
    private final ExecutorService executorService;

    public LzmaDecoder(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void init(byte[] properties) {
        Preconditions.checkState(decoder.SetDecoderProperties(properties));
    }

    public InputStream getInputStream(InputStream encodedInputStream) throws IOException {
        return new LzmaDecodingInputStream(encodedInputStream, decoder, executorService);
    }
}
