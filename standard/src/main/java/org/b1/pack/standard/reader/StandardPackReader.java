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

import org.b1.pack.api.reader.PackReader;
import org.b1.pack.api.reader.ReaderCommand;
import org.b1.pack.api.reader.ReaderProvider;

import java.io.IOException;

import static org.b1.pack.api.common.PackFormat.B1;

public class StandardPackReader extends PackReader {

    @Override
    public void read(ReaderProvider provider, ReaderCommand command) throws IOException {
        command.execute(new StandardReaderPack(provider));
    }

    @Override
    protected boolean isFormatSupported(String format) {
        return B1.equals(format);
    }
}
