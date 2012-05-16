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
import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;

public class PackCipher {

    private static final int KEY_SIZE = 256;
    private final HMac hMac;
    private final int iterationCount;

    public PackCipher(char[] password, byte[] salt, int iterationCount) {
        this.iterationCount = iterationCount;
        SHA256Digest digest = new SHA256Digest();
        hMac = new HMac(digest);
        PKCS5S2ParametersGenerator generator = new PKCS5S2ParametersGenerator(digest);
        generator.init(PBEParametersGenerator.PKCS5PasswordToUTF8Bytes(password), salt, iterationCount);
        hMac.init(generator.generateDerivedParameters(KEY_SIZE));
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
}
