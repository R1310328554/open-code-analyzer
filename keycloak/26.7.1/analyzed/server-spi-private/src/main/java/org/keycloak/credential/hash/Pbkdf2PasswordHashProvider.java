/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.credential.hash;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.keycloak.common.crypto.CryptoIntegration;
import org.keycloak.common.util.PaddingUtils;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.credential.PasswordCredentialModel;

/**
 * PBKDF2 密码哈希算法实现，支持可配置迭代次数、盐值、派生密钥长度与密码填充。
 *
 * @author <a href="mailto:me@tsudot.com">Kunal Kerkar</a>
 */
public class Pbkdf2PasswordHashProvider implements PasswordHashProvider {

    private final String providerId;

    private final String pbkdf2Algorithm;
    private final int defaultIterations;

    private final int maxPaddingLength;
    private final int derivedKeySize;
    /** 默认派生密钥位数（512 位）。 */
    public static final int DEFAULT_DERIVED_KEY_SIZE = 512;

    /** 四参数构造器，派生密钥长度使用 {@link #DEFAULT_DERIVED_KEY_SIZE}。 */
    public Pbkdf2PasswordHashProvider(String providerId, String pbkdf2Algorithm, int defaultIterations, int minPbkdf2PasswordLengthForPadding) {
        this(providerId, pbkdf2Algorithm, defaultIterations, minPbkdf2PasswordLengthForPadding, DEFAULT_DERIVED_KEY_SIZE);
    }
    /**
     * @param providerId 提供者 ID（如 {@code pbkdf2-sha256}）
     * @param pbkdf2Algorithm JCA 算法名（如 {@code PBKDF2WithHmacSHA256}）
     * @param defaultIterations 默认哈希迭代次数
     * @param maxPaddingLength 密码填充目标长度
     * @param derivedKeySize 派生密钥位数
     */
    public Pbkdf2PasswordHashProvider(String providerId, String pbkdf2Algorithm, int defaultIterations, int maxPaddingLength, int derivedKeySize) {
        this.providerId = providerId;
        this.pbkdf2Algorithm = pbkdf2Algorithm;
        this.defaultIterations = defaultIterations;
        this.maxPaddingLength = maxPaddingLength;
        this.derivedKeySize = derivedKeySize;
    }

    /** 校验凭证的算法、迭代次数与密钥长度是否符合当前密码策略。 */
    @Override
    public boolean policyCheck(PasswordPolicy policy, PasswordCredentialModel credential) {
        int policyHashIterations = policy != null ? policy.getHashIterations() : -1;
        if (policyHashIterations == -1) {
            policyHashIterations = defaultIterations;
        }

        return credential.getPasswordCredentialData().getHashIterations() == policyHashIterations
                && providerId.equals(credential.getPasswordCredentialData().getAlgorithm())
                && derivedKeySize == keySize(credential);
    }

    /** 生成带随机盐的新 {@link PasswordCredentialModel}。 */
    @Override
    public PasswordCredentialModel encodedCredential(String rawPassword, int iterations) {
        if (iterations == -1) {
            iterations = defaultIterations;
        }

        byte[] salt = getSalt();
        String encodedPassword = encodedCredential(rawPassword, iterations, salt, derivedKeySize);

        return PasswordCredentialModel.createFromValues(providerId, salt, iterations, encodedPassword);
    }

    /** 仅返回 Base64 编码的哈希值（不含盐与元数据）。 */
    @Override
    public String encode(String rawPassword, int iterations) {
        if (iterations == -1) {
            iterations = defaultIterations;
        }

        byte[] salt = getSalt();
        return encodedCredential(rawPassword, iterations, salt, derivedKeySize);
    }

    /** 用相同盐与迭代次数重新编码明文，与存储值比对。 */
    @Override
    public boolean verify(String rawPassword, PasswordCredentialModel credential) {
        return encodedCredential(rawPassword, credential.getPasswordCredentialData().getHashIterations(), credential.getPasswordSecretData().getSalt(), keySize(credential)).equals(credential.getPasswordSecretData().getValue());
    }

    private int keySize(PasswordCredentialModel credential) {
        try {
            byte[] bytes = Base64.getMimeDecoder().decode(credential.getPasswordSecretData().getValue());
            return bytes.length * 8;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Credential could not be decoded", e);
        }
    }

    /** 无状态实现，关闭操作为空。 */
    public void close() {
    }

    private String encodedCredential(String rawPassword, int iterations, byte[] salt, int derivedKeySize) {
        String rawPasswordWithPadding = PaddingUtils.padding(rawPassword, maxPaddingLength);
        KeySpec spec = new PBEKeySpec(rawPasswordWithPadding.toCharArray(), salt, iterations, derivedKeySize);

        try {
            byte[] key = getSecretKeyFactory().generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(key);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Credential could not be encoded", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] getSalt() {
        byte[] buffer = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(buffer);
        return buffer;
    }

    private SecretKeyFactory getSecretKeyFactory() {
        try {
            return CryptoIntegration.getProvider().getSecretKeyFact(pbkdf2Algorithm);
            
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("PBKDF2 algorithm not found", e);
        }
    }

    /** @return 当前使用的 PBKDF2 JCA 算法名 */
    public String getPbkdf2Algorithm() {
        return pbkdf2Algorithm;
    }
}
