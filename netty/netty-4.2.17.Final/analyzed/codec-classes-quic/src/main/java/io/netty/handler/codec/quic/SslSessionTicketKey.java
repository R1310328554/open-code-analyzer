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

import java.util.Arrays;

/**
 * TLS 会话票据（Session Ticket）加密密钥三元组：名称、HMAC 密钥与 AES 密钥。
 * 供 BoringSSL 会话恢复回调使用。
 */
public final class SslSessionTicketKey {
    /** 票据密钥名称长度（字节）。 */
    public static final int NAME_SIZE = 16;
    /** HMAC 密钥长度（字节）。 */
    public static final int HMAC_KEY_SIZE = 16;
    /** AES 加密密钥长度（字节）。 */
    public static final int AES_KEY_SIZE = 16;
    /** 完整票据密钥总长度（name + hmac + aes）。 */
    public static final int TICKET_KEY_SIZE = NAME_SIZE + HMAC_KEY_SIZE + AES_KEY_SIZE;

    // 包可见字段，供 BoringSSLSessionTicketCallback 直接访问，避免 clone
    final byte[] name;
    final byte[] hmacKey;
    final byte[] aesKey;

    /**
     * 构造会话票据密钥；各数组长度须与对应 SIZE 常量一致。
     * @param name the name of the session ticket key
     * @param hmacKey the HMAC key of the session ticket key
     * @param aesKey the AES key of the session ticket key
     */
    public SslSessionTicketKey(byte[] name, byte[] hmacKey, byte[] aesKey) {
        if (name == null || name.length != NAME_SIZE) {
            throw new IllegalArgumentException("Length of name must be " + NAME_SIZE);
        }
        if (hmacKey == null || hmacKey.length != HMAC_KEY_SIZE) {
            throw new IllegalArgumentException("Length of hmacKey must be " + HMAC_KEY_SIZE);
        }
        if (aesKey == null || aesKey.length != AES_KEY_SIZE) {
            throw new IllegalArgumentException("Length of aesKey must be " + AES_KEY_SIZE);
        }
        this.name = name.clone();
        this.hmacKey = hmacKey.clone();
        this.aesKey = aesKey.clone();
    }

    /**
     * 返回密钥名称的副本。
     *
     * @return the name of the session ticket key
     */
    public byte[] name() {
        return name.clone();
    }

    /**
     * 返回 HMAC 密钥的副本。
     * @return the HMAC key of the session ticket key
     */
    public byte[] hmacKey() {
        return hmacKey.clone();
    }

    /**
     * 返回 AES 密钥的副本。
     * @return the AES key of the session ticket key
     */
    public byte[] aesKey() {
        return aesKey.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SslSessionTicketKey that = (SslSessionTicketKey) o;

        if (!Arrays.equals(name, that.name)) {
            return false;
        }
        if (!Arrays.equals(hmacKey, that.hmacKey)) {
            return false;
        }
        return Arrays.equals(aesKey, that.aesKey);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(name);
        result = 31 * result + Arrays.hashCode(hmacKey);
        result = 31 * result + Arrays.hashCode(aesKey);
        return result;
    }

    @Override
    public String toString() {
        return "SessionTicketKey{" +
                "name=" + Arrays.toString(name) +
                ", hmacKey=" + Arrays.toString(hmacKey) +
                ", aesKey=" + Arrays.toString(aesKey) +
                '}';
    }
}
