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

import com.google.common.base.Preconditions;
import org.b1.pack.api.common.InvalidPasswordException;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.macs.HMac;
import org.spongycastle.crypto.modes.GCMBlockCipher;
import org.spongycastle.crypto.modes.gcm.BasicGCMMultiplier;
import org.spongycastle.crypto.params.AEADParameters;
import org.spongycastle.crypto.params.KeyParameter;

public class VolumeCipher {

    public static final int MAC_BYTE_SIZE = 16;

    private static final byte[] BLANK_NONCE = new byte[12];
    private static final byte[] HEAD_SALT = new byte[]{0x01};
    private static final byte[] TAIL_SALT = new byte[]{0x02};

    private final HMac hMac;
    private final int iterationCount;

    public VolumeCipher(KeyParameter key, int iterationCount) {
        this.iterationCount = iterationCount;
        hMac = new HMac(new SHA256Digest());
        hMac.init(key);
    }

    public int getIterationCount() {
        return iterationCount;
    }

    public byte[] cipherHead(boolean encryption, byte[] in) {
        return doCipher(encryption, HEAD_SALT, in);
    }

    public byte[] cipherTail(boolean encryption, byte[] in) {
        return doCipher(encryption, TAIL_SALT, in);
    }

    public byte[] cipherBlock(boolean encryption, long blockOffset, byte[] in) {
        return doCipher(encryption, PackCipher.longToUtf8(blockOffset), in);
    }

    private byte[] doCipher(boolean encryption, byte[] salt, byte[] in) {
        GCMBlockCipher cipher = new GCMBlockCipher(new AESFastEngine(), new BasicGCMMultiplier());
        AEADParameters parameters = new AEADParameters(PackCipher.generateKey(hMac, salt), MAC_BYTE_SIZE * 8, BLANK_NONCE, null);
        cipher.init(encryption, parameters);
        byte[] out = new byte[cipher.getOutputSize(in.length)];
        int count = cipher.processBytes(in, 0, in.length, out, 0);
        try {
            Preconditions.checkState(count + cipher.doFinal(out, count) == out.length);
        } catch (InvalidCipherTextException e) {
            throw new InvalidPasswordException("Password is invalid or archive is corrupt", e);
        }
        return out;
    }
}
