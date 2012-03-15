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

import org.b1.pack.api.builder.PackBuilder;
import org.b1.pack.api.common.PackService;
import org.b1.pack.api.explorer.PxFactory;
import org.b1.pack.standard.builder.StandardPackBuilder;
import org.b1.pack.standard.explorer.StandardPxFactory;

public class StandardPackService extends PackService {

    @Override
    public PackBuilder getPbFactory(String format) {
        if (format.equals(B1)) {
            return new StandardPackBuilder();
        }
        return super.getPbFactory(format);
    }

    @Override
    public PxFactory getPxFactory(String format) {
        if (format.equals(B1)) {
            return new StandardPxFactory();
        }
        return super.getPxFactory(format);
    }
}
