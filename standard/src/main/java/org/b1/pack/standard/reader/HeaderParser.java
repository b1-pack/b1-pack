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

package org.b1.pack.standard.reader;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.List;

class HeaderParser {

    private static final char SPACE = ' ';
    private static final char SINGLE_QUOTE = '\'';

    private final List<String> items = Lists.newArrayList();
    private final StringBuilder builder = new StringBuilder();
    private State state = new InitialState();

    public HeaderParser() {
    }

    public List<String> getItems() {
        return items;
    }

    public void parse(String s) {
        for (char c : s.toCharArray()) {
            state.read(c);
        }
        state.close();
    }

    private void saveItem() {
        items.add(builder.toString());
        builder.setLength(0);
        state = new InitialState();
    }

    private interface State {
        void read(char c);

        void close();
    }

    private class InitialState implements State {
        @Override
        public void read(char c) {
            if (c == SINGLE_QUOTE) {
                state = new StartState();
            } else if (c != SPACE) {
                state = new SimpleState();
                state.read(c);
            }
        }

        @Override
        public void close() {
            // no-op
        }
    }

    private class SimpleState implements State {
        @Override
        public void read(char c) {
            if (c == SPACE) {
                saveItem();
            } else {
                builder.append(c);
            }
        }

        @Override
        public void close() {
            saveItem();
        }
    }

    private class StartState implements State {
        @Override
        public void read(char c) {
            if (c == SINGLE_QUOTE) {
                state = new EndState();
            } else {
                builder.append(c);
            }
        }

        @Override
        public void close() {
            throw new IllegalArgumentException("Quoted item not closed");
        }
    }

    private class EndState implements State {
        @Override
        public void read(char c) {
            if (c == SINGLE_QUOTE) {
                builder.append(c);
                state = new StartState();
            } else {
                Preconditions.checkArgument(c == SPACE, "Quoted item not separated");
                saveItem();
            }
        }

        @Override
        public void close() {
            saveItem();
        }
    }
}
