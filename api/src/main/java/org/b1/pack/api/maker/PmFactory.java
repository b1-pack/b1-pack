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

package org.b1.pack.api.maker;

import org.b1.pack.api.common.PackException;
import org.b1.pack.api.common.PackService;

import java.io.IOException;
import java.util.ServiceLoader;

public abstract class PmFactory {

    public static PmFactory newInstance(String format) {
        for (PackService packService : ServiceLoader.load(PackService.class)) {
            PmFactory factory = packService.getPwFactory(format);
            if (factory != null) return factory;
        }
        throw new PackException("Unsupported format: " + format);
    }

    public abstract PackMaker createPackWriter(PmProvider provider) throws IOException;

}
