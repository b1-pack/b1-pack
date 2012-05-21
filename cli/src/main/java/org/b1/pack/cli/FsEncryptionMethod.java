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
package org.b1.pack.cli;

import com.google.common.base.Preconditions;
import org.b1.pack.api.common.EncryptionMethod;

import java.io.Console;
import java.util.Arrays;

public class FsEncryptionMethod extends EncryptionMethod {

    private final String password;

    public FsEncryptionMethod(String name, String password) {
        super(name);
        this.password = password;
    }

    @Override
    public char[] getPassword() {
        if (password != null) {
            return password.toCharArray();
        }
        Console console = Preconditions.checkNotNull(System.console(), "Console is not available for password input");
        char[] password1 = console.readPassword("Enter password for encryption (will not be echoed): ");
        char[] password2 = console.readPassword("Verify password for encryption (will not be echoed): ");
        if (Arrays.equals(password1, password2)) {
            clearPassword(password1);
            return password2;
        } else {
            clearPassword(password1);
            clearPassword(password2);
            throw new IllegalStateException("Passwords do not match");
        }
    }

    private static void clearPassword(char[] password) {
        Arrays.fill(password, (char) 0);
    }
}
