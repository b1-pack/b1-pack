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
package org.b1.pack.standard.common;

import com.google.common.base.Charsets;
import org.b1.pack.api.common.InvalidPasswordException;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.Strings;

import java.io.IOException;
import java.util.Arrays;

public class PackCipher {

    private static final int KEY_SIZE = 256;
    private final HMac hMac;
    private final int iterationCount;

    public PackCipher(char[] password, byte[] salt, int iterationCount) {
        if (password == null) throw new InvalidPasswordException("No password provided");
        this.iterationCount = iterationCount;
        hMac = new HMac(new SHA256Digest());
        hMac.init(generateKey(password, salt, iterationCount));
    }

    public int getIterationCount() {
        return iterationCount;
    }

    public VolumeCipher getVolumeCipher(long volumeNumber) {
        return new VolumeCipher(generateKey(hMac, longToUtf8(volumeNumber)), iterationCount);
    }

    public static KeyParameter generateKey(HMac hMac, byte[] in) {
        hMac.update(in, 0, in.length);
        byte[] out = new byte[hMac.getMacSize()];
        hMac.doFinal(out, 0);
        return new KeyParameter(out);
    }

    public static byte[] longToUtf8(long value) {
        return Long.toString(value).getBytes(Charsets.UTF_8);
    }

    private CipherParameters generateKey(char[] password, byte[] salt, int iterationCount) {
        byte[] utf8Password = getUtf8Password(password);
        try {
            PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(new SHA256Digest());
            generator.init(utf8Password, salt, iterationCount);
            return generator.generateDerivedParameters(KEY_SIZE);
        } finally {
            Arrays.fill(utf8Password, (byte) 0);
        }
    }

    private byte[] getUtf8Password(char[] password) {
        MemoryOutputStream stream = new MemoryOutputStream(password.length * 4);
        try {
            Strings.toUTF8ByteArray(password, stream);
            return stream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            Arrays.fill(password, (char) 0);
            Arrays.fill(stream.getBuf(), (byte) 0);
        }
    }
}
