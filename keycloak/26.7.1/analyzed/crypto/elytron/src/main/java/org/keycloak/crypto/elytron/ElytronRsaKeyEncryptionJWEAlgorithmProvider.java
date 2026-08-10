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
package org.keycloak.crypto.elytron;

import java.security.Key;
import javax.crypto.Cipher;

import org.keycloak.jose.jwe.JWEHeader;
import org.keycloak.jose.jwe.JWEHeader.JWEHeaderBuilder;
import org.keycloak.jose.jwe.JWEKeyStorage;
import org.keycloak.jose.jwe.alg.JWEAlgorithmProvider;
import org.keycloak.jose.jwe.enc.JWEEncryptionProvider;

/**
 * 基于 RSA 密钥加密的 JWE 算法提供器，使用 JCA {@link Cipher} 封装/解封 CEK。
 *
 * @author <a href="mailto:david.anderson@redhat.com">David Anderson</a>
 */
public class ElytronRsaKeyEncryptionJWEAlgorithmProvider implements JWEAlgorithmProvider {

    /** JCA 算法名称，如 {@code RSA/ECB/PKCS1Padding}。 */
    private final String jcaAlgorithmName;

    /**
     * @param jcaAlgorithmName JCA 加密算法名称
     */
    public ElytronRsaKeyEncryptionJWEAlgorithmProvider(String jcaAlgorithmName) {
        this.jcaAlgorithmName = jcaAlgorithmName;
    }

    /** {@inheritDoc} 使用 RSA 私钥解密 CEK。 */
    @Override
    public byte[] decodeCek(byte[] encodedCek, Key privateKey, JWEHeader header, JWEEncryptionProvider encryptionProvider) throws Exception {
        Cipher cipher = getCipherProvider();
        initCipher(cipher, Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encodedCek);
    }

    /** {@inheritDoc} 使用 RSA 公钥加密 CEK。 */
    @Override
    public byte[] encodeCek(JWEEncryptionProvider encryptionProvider, JWEKeyStorage keyStorage, Key publicKey, JWEHeaderBuilder headerBuilder) throws Exception {
        Cipher cipher = getCipherProvider();
        initCipher(cipher, Cipher.ENCRYPT_MODE, publicKey);
        byte[] cekBytes = keyStorage.getCekBytes();
        return cipher.doFinal(cekBytes);
    }

    private Cipher getCipherProvider() throws Exception {
        return Cipher.getInstance(jcaAlgorithmName);
    }

    /**
     * 初始化 Cipher；子类可覆盖以设置 OAEP 等参数。
     *
     * @param cipher 待初始化的 Cipher
     * @param mode 加密或解密模式
     * @param key 密钥
     */
    protected void initCipher(Cipher cipher, int mode, Key key) throws Exception {
        cipher.init(mode, key);
    }
}
