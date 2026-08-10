/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.jose.jwe;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import org.keycloak.jose.jwe.enc.JWEEncryptionProvider;

/**
 * JWE 密钥存储：管理加密/解密密钥、内容加密密钥（CEK）字节及其按用途拆分后的 {@link Key}。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class JWEKeyStorage {

    /** 用于 CEK 加密的密钥（密钥管理算法侧）。 */
    private Key encryptionKey;
    /** 用于 CEK 解密的密钥。 */
    private Key decryptionKey;

    /** 原始 CEK 字节，可由 {@link JWEEncryptionProvider} 序列化/反序列化。 */
    private byte[] cekBytes;

    /** 按用途（加密、MAC 签名等）缓存的 CEK 子密钥。 */
    private Map<KeyUse, Key> decodedCEK = new HashMap<>();

    /** 当前内容加密算法提供者，负责 CEK 长度与编解码。 */
    private JWEEncryptionProvider encryptionProvider;


    public Key getEncryptionKey() {
        return encryptionKey;
    }

    public JWEKeyStorage setEncryptionKey(Key encryptionKey) {
        this.encryptionKey = encryptionKey;
        return this;
    }

    public Key getDecryptionKey() {
        return decryptionKey;
    }

    public JWEKeyStorage setDecryptionKey(Key decryptionKey) {
        this.decryptionKey = decryptionKey;
        return this;
    }

    public void setCEKBytes(byte[] cekBytes) {
        this.cekBytes = cekBytes;
    }

    /** 获取 CEK 字节；若尚未设置则通过 {@link JWEEncryptionProvider#serializeCEK} 生成。 */
    public byte[] getCekBytes() {
        if (cekBytes == null) {
            cekBytes = encryptionProvider.serializeCEK(this);
        }
        return cekBytes;
    }

    public JWEKeyStorage setCEKKey(Key key, KeyUse keyUse) {
        decodedCEK.put(keyUse, key);
        return this;
    }


    /**
     * 按用途获取 CEK 子密钥；缺失时可触发 CEK 生成与反序列化。
     *
     * @param keyUse 密钥用途（加密或 MAC）
     * @param generateIfNotPresent 是否在 CEK 字节缺失时自动生成
     */
    public Key getCEKKey(KeyUse keyUse, boolean generateIfNotPresent) {
        Key key = decodedCEK.get(keyUse);
        if (key == null) {
            if (encryptionProvider != null) {

                if (cekBytes == null && generateIfNotPresent) {
                    generateCekBytes();
                }

                if (cekBytes != null) {
                    encryptionProvider.deserializeCEK(this);
                }
            } else {
                throw new IllegalStateException("encryptionProvider needs to be set");
            }
        }

        return decodedCEK.get(keyUse);
    }


    private void generateCekBytes() {
        int cekLength = encryptionProvider.getExpectedCEKLength();
        cekBytes = JWEUtils.generateSecret(cekLength);
    }


    public void setEncryptionProvider(JWEEncryptionProvider encryptionProvider) {
        this.encryptionProvider = encryptionProvider;
    }


    /** CEK 拆分后的密钥用途。 */
    public enum KeyUse {
        /** AES 等内容加密。 */
        ENCRYPTION,
        /** HMAC 等完整性校验。 */
        SIGNATURE
    }

}
