/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.jose.jwe.enc;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.keycloak.common.crypto.CryptoIntegration;
import org.keycloak.jose.jwe.JWE;
import org.keycloak.jose.jwe.JWEKeyStorage;
import org.keycloak.jose.jwe.JWEUtils;

/**
 * AES-GCM 内容加密（RFC 7518 5.3）：IV 96 位，认证标签 128 位。
 */
public abstract class AesGcmEncryptionProvider implements JWEEncryptionProvider {

    // IV 必须为 96 位；认证标签必须为 128 位
    // https://tools.ietf.org/html/rfc7518#section-5.3
    private static final int AUTH_TAG_SIZE_BYTE = 16;
    private static final int IV_SIZE_BYTE = 12;

    @Override
    public void encodeJwe(JWE jwe) throws Exception {

        byte[] contentBytes = jwe.getContent();

        // IV 必须为 nonce（一次性随机数）
        byte[] initializationVector = JWEUtils.generateSecret(IV_SIZE_BYTE);

        Key aesKey = jwe.getKeyStorage().getCEKKey(JWEKeyStorage.KeyUse.ENCRYPTION, false);
        if (aesKey == null) {
            throw new IllegalArgumentException("AES CEK key not present");
        }

        int expectedAesKeyLength = getExpectedAesKeyLength();
        if (expectedAesKeyLength != aesKey.getEncoded().length) {
            throw new IllegalStateException("Length of aes key should be " + expectedAesKeyLength +", but was " + aesKey.getEncoded().length);
        }

        // https://tools.ietf.org/html/rfc7516#appendix-A.1.5
        byte[] aad = jwe.getBase64Header().getBytes(StandardCharsets.UTF_8);

        byte[] cipherBytes = encryptBytes(contentBytes, initializationVector, aesKey, aad);
        byte[] authenticationTag = getAuthenticationTag(cipherBytes);
        byte[] encryptedContent = getEncryptedContent(cipherBytes);
        jwe.setEncryptedContentInfo(initializationVector, encryptedContent, authenticationTag);

    }

    @Override
    public void verifyAndDecodeJwe(JWE jwe) throws Exception {
        Key aesKey = jwe.getKeyStorage().getCEKKey(JWEKeyStorage.KeyUse.ENCRYPTION, false);
        if (aesKey == null) {
            throw new IllegalArgumentException("AES CEK key not present");
        }

        int expectedAesKeyLength = getExpectedAesKeyLength();
        if (expectedAesKeyLength != aesKey.getEncoded().length) {
            throw new IllegalStateException("Length of aes key should be " + expectedAesKeyLength +", but was " + aesKey.getEncoded().length);
        }

        // https://tools.ietf.org/html/rfc7516#appendix-A.1.5
        byte[] aad = jwe.getBase64Header().getBytes(StandardCharsets.UTF_8);
        byte[] decryptedTargetContent = getAeadDecryptedTargetContent(jwe);
        byte[] contentBytes = decryptBytes(decryptedTargetContent, jwe.getInitializationVector(), aesKey, aad);

        jwe.content(contentBytes);
    }

    private byte[] encryptBytes(byte[] contentBytes, byte[] ivBytes, Key aesKey, byte[] aad) throws GeneralSecurityException {
        Cipher cipher = CryptoIntegration.getProvider().getAesGcmCipher();
        GCMParameterSpec gcmParams = new GCMParameterSpec(AUTH_TAG_SIZE_BYTE * 8, ivBytes);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmParams);
        cipher.updateAAD(aad);
        byte[] cipherText = new byte[cipher.getOutputSize(contentBytes.length)];
        cipher.doFinal(contentBytes, 0, contentBytes.length, cipherText);
        return cipherText;
    }

    private byte[] decryptBytes(byte[] encryptedBytes, byte[] ivBytes, Key aesKey, byte[] aad) throws GeneralSecurityException {
        Cipher cipher = CryptoIntegration.getProvider().getAesGcmCipher();
        GCMParameterSpec gcmParams = new GCMParameterSpec(AUTH_TAG_SIZE_BYTE * 8, ivBytes);
        cipher.init(Cipher.DECRYPT_MODE, aesKey, gcmParams);
        cipher.updateAAD(aad);
        return cipher.doFinal(encryptedBytes);
    }

    private byte[] getAuthenticationTag(byte[] cipherBytes) {
        // AES-GCM 密文由密文与认证标签组成；JWE 中标签单独编码，需从 GCM 输出中拆分
        // https://tools.ietf.org/html/rfc5116#section-5.1
        byte[] authenticationTag = new byte[AUTH_TAG_SIZE_BYTE];
        System.arraycopy(cipherBytes, cipherBytes.length - authenticationTag.length, authenticationTag, 0, authenticationTag.length);
        return authenticationTag;
    }

    private byte[] getEncryptedContent(byte[] cipherBytes) throws NoSuchAlgorithmException, InvalidKeyException {
        // 从 GCM 输出中提取不含认证标签的密文部分，供 JWE 单独编码
        // https://tools.ietf.org/html/rfc5116#section-5.1
        byte[] encryptedContent = new byte[cipherBytes.length - AUTH_TAG_SIZE_BYTE];
        System.arraycopy(cipherBytes, 0, encryptedContent, 0, encryptedContent.length);
        return encryptedContent;
    }

    private byte[] getAeadDecryptedTargetContent(JWE jwe) {
        // 解密前将 JWE 密文与认证标签拼回 GCM 期望的 AEAD 输入格式
        byte[] encryptedContent = jwe.getEncryptedContent();
        byte[] authTag = jwe.getAuthenticationTag();
        byte[] decryptedTargetContent = new byte[authTag.length + encryptedContent.length];
        System.arraycopy(encryptedContent, 0, decryptedTargetContent, 0, encryptedContent.length);
        System.arraycopy(authTag, 0, decryptedTargetContent, encryptedContent.length, authTag.length);
        return decryptedTargetContent;
    }

    @Override
    public byte[] serializeCEK(JWEKeyStorage keyStorage) {
        Key aesKey = keyStorage.getCEKKey(JWEKeyStorage.KeyUse.ENCRYPTION, false);
        if (aesKey == null) {
            throw new IllegalArgumentException("AES CEK key not present");
        }
        byte[] aesBytes = aesKey.getEncoded();
        return aesBytes;
    }

    @Override
    public void deserializeCEK(JWEKeyStorage keyStorage) {
        byte[] cekBytes = keyStorage.getCekBytes();
        SecretKeySpec aesKey = new SecretKeySpec(cekBytes, "AES");
        keyStorage.setCEKKey(aesKey, JWEKeyStorage.KeyUse.ENCRYPTION);
    }

    protected abstract int getExpectedAesKeyLength();

}
