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
package org.keycloak.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;

import org.keycloak.common.VerificationException;
import org.keycloak.common.crypto.CryptoIntegration;

/**
 * 基于非对称公钥的 JWS 验签上下文，将 {@link KeyWrapper} 中的公钥用于 {@link SignatureVerifierContext}。
 */
public class AsymmetricSignatureVerifierContext implements SignatureVerifierContext {

    /** 封装待验签公钥及其元数据的包装对象。 */
    private final KeyWrapper key;

    /**
     * @param key 含公钥与算法信息的密钥包装
     */
    public AsymmetricSignatureVerifierContext(KeyWrapper key) {
        this.key = key;
    }

    @Override
    public String getKid() {
        return key.getKid();
    }

    @Override
    public String getAlgorithm() {
        return key.getAlgorithmOrDefault();
    }

    /**
     * 使用公钥验证给定数据与签名的匹配性。
     *
     * @param data 原始待验签字节
     * @param signature 待验证的签名值
     * @return 验签是否通过
     * @throws VerificationException 验签过程失败时抛出
     */
    @Override
    public boolean verify(byte[] data, byte[] signature) throws VerificationException {
        try {
            Signature verifier = getSignature();
            verifier.initVerify((PublicKey) key.getPublicKey());
            verifier.update(data);
            return verifier.verify(signature);
        } catch (Exception e) {
            throw new VerificationException("Signing failed", e);
        }
    }

    /**
     * 获取 {@link Signature} 实例；标准 JCA 算法不可用时回退至 Crypto 提供者的实现。
     */
    private Signature getSignature()
            throws NoSuchAlgorithmException, NoSuchProviderException {
        try {
            return Signature.getInstance(JavaAlgorithm.getJavaAlgorithm(key.getAlgorithmOrDefault(), key.getCurve()));
        } catch (NoSuchAlgorithmException e) {
            // 使用当前 Crypto 提供者的覆盖实现重试
            return CryptoIntegration.getProvider().getSignature(key.getAlgorithmOrDefault());
        }
    }
}
