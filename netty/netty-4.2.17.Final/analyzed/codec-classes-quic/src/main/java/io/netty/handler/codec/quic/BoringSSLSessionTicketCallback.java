/*
 * Copyright 2023 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.quic;

import io.netty.util.internal.PlatformDependent;
import org.jetbrains.annotations.Nullable;

/**
 * TLS 会话票证（Session Ticket）密钥的 JNI 桥接类。
 * <p>
 * 配置的密钥数量通常较少，故用二维字节数组存储；JNI 通过 {@link #findSessionTicket} 按名称查找。
 */
final class BoringSSLSessionTicketCallback {

    // 密钥条目不多，直接用数组存储
    private volatile byte[][] sessionKeys;

    // 由 JNI 调用，根据 keyname 返回 49 字节的二进制密钥块
    byte @Nullable [] findSessionTicket(byte @Nullable [] keyname) {
        byte[][] keys = this.sessionKeys;
        if (keys == null || keys.length == 0) {
            return null;
        }
        if (keyname == null) {
            // 无名称时返回首选（第一个）密钥
            return keys[0];
        }

        for (int i = 0; i < keys.length; i++) {
            byte[] key = keys[i];
            // key[1..] 为 16 字节名称，与 keyname 比较
            if (PlatformDependent.equals(keyname, 0, key, 1, keyname.length)) {
                return key;
            }
        }
        return null;
    }

    /** 将 {@link SslSessionTicketKey} 数组转换为 native 层所需的二进制格式并更新缓存。 */
    void setSessionTicketKeys(SslSessionTicketKey @Nullable [] keys) {
        if (keys != null && keys.length != 0) {
            byte[][] sessionKeys = new byte[keys.length][];
            for (int i = 0; i < keys.length; ++i) {
                SslSessionTicketKey key = keys[i];
                byte[] binaryKey = new byte[49];
                // 首字节标记首选密钥：1 表示 preferred，0 表示备选
                binaryKey[0] = i == 0 ? (byte) 1 : (byte) 0;
                int dstCurPos = 1;
                System.arraycopy(key.name, 0, binaryKey, dstCurPos, 16);
                dstCurPos += 16;
                System.arraycopy(key.hmacKey, 0, binaryKey, dstCurPos, 16);
                dstCurPos += 16;
                System.arraycopy(key.aesKey, 0, binaryKey, dstCurPos, 16);
                sessionKeys[i] = binaryKey;
            }
            this.sessionKeys = sessionKeys;
        } else {
            sessionKeys = null;
        }
    }
}
