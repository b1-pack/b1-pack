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

public class Constants {

    public static final int MIN_CHUNK_SIZE = 0xFF;
    public static final int MAX_CHUNK_SIZE = 0xFFFFF;

    public static final int RECORD_POINTER = 0;
    public static final int CATALOG_FILE = 1;
    public static final int COMPLETE_FILE = 2;
    public static final int CATALOG_FOLDER = 3;
    public static final int COMPLETE_FOLDER = 4;

    public static final int AES_BLOCK = 0;
    public static final int PLAIN_BLOCK = 1;
    public static final int FIRST_LZMA_BLOCK = 2;
    public static final int NEXT_LZMA_BLOCK = 3;

    public static final int LAST_MODIFIED_TIME = 0;
    public static final int UNIX_PERMISSIONS = 1;
    public static final int WINDOWS_ATTRIBUTES = 2;
    public static final int FILE_OWNER = 3;
    public static final int FILE_GROUP = 4;

}
