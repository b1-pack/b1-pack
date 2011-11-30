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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class ObjectKey {

    private Long parentId;
    private String name;

    public ObjectKey(Long parentId, String name) {
        this.parentId = parentId;
        this.name = Preconditions.checkNotNull(name);
    }

    public Long getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ObjectKey) {
            ObjectKey that = (ObjectKey) o;
            return Objects.equal(that.parentId, parentId) && that.name.equals(name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(parentId, name);
    }
}
