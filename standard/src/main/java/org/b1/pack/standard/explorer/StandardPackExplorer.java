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

import org.b1.pack.api.explorer.ExplorerCommand;
import org.b1.pack.api.explorer.ExplorerProvider;
import org.b1.pack.api.explorer.PackExplorer;

import java.io.IOException;

import static org.b1.pack.api.common.PackFormat.B1;

public class StandardPackExplorer extends PackExplorer {

    @Override
    public void explore(ExplorerProvider provider, ExplorerCommand command) throws IOException {
        StandardExplorerPack pack = new StandardExplorerPack(new VolumeManager(provider));
        try {
            command.execute(pack);
        } finally {
            pack.close();
        }
    }

    @Override
    protected boolean isFormatSupported(String format) {
        return B1.equals(format);
    }
}
