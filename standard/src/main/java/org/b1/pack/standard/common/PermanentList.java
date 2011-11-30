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

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

public final class PermanentList<T> extends AbstractList<T> {

    private final List<T> list;
    private final T value;

    private PermanentList(List<T> list, T value) {
        this.list = list;
        this.value = value;
    }

    public static <T> PermanentList<T> of(T t) {
        return new PermanentList<T>(Collections.<T>emptyList(), t);
    }

    public PermanentList<T> with(T t) {
        return new PermanentList<T>(this, t);
    }

    @Override
    public T get(int index) {
        return index == list.size() ? value : list.get(index);
    }

    @Override
    public int size() {
        return list.size() + 1;
    }
}
