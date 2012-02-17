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

package org.b1.pack.standard.writer;

import org.b1.pack.api.common.PackService;
import org.b1.pack.api.writer.PackWriter;
import org.b1.pack.api.writer.WriterCommand;
import org.b1.pack.api.writer.WriterProvider;

import java.io.IOException;

@SuppressWarnings("UnusedDeclaration")
public class StandardPackWriter extends PackWriter {

    @Override
    public void write(WriterProvider provider, WriterCommand command) throws IOException {
        StandardWriterPack pack = new StandardWriterPack(provider);
        try {
            command.execute(pack);
            pack.complete();
        } finally {
            pack.close();
        }
    }

    @Override
    protected boolean isFormatSupported(String format) {
        return PackService.B1.equals(format);
    }
}
