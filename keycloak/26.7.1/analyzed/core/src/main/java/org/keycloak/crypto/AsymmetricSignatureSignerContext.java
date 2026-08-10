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

import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

/**
 * 基于非对称私钥的 JWS 签名上下文，将 {@link KeyWrapper} 中的密钥材料用于 {@link SignatureSignerContext}。
 */
public class AsymmetricSignatureSignerContext implements SignatureSignerContext {

    /** 封装待签名密钥及其元数据的包装对象。 */
    protected final KeyWrapper key;

    /**
     * @param key 含私钥与算法信息的密钥包装
     * @throws SignatureException 密钥不可用或算法不支持时抛出
     */
    public AsymmetricSignatureSignerContext(KeyWrapper key) throws SignatureException {
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

    @Override
    public String getHashAlgorithm() {
        return JavaAlgorithm.getJavaAlgorithmForHash(key.getAlgorithmOrDefault(), key.getCurve());
    }

    /**
     * 使用私钥对给定字节序列执行数字签名。
     *
     * @param data 待签名的原始字节
     * @return 签名结果
     * @throws SignatureException 签名过程失败时抛出
     */
    @Override
    public byte[] sign(byte[] data) throws SignatureException {
        try {
            Signature signature = Signature.getInstance(JavaAlgorithm.getJavaAlgorithm(key.getAlgorithmOrDefault(), key.getCurve()));
            signature.initSign((PrivateKey) key.getPrivateKey());
            signature.update(data);
            return signature.sign();
        } catch (Exception e) {
            throw new SignatureException("Signing failed", e);
        }
    }

    /**
     * @return 与密钥关联的 X.509 证书链；无链时返回单证书或 {@code null}
     */
    @Override
    public List<X509Certificate> getCertificateChain() {
        if (key.getCertificateChain() != null && !key.getCertificateChain().isEmpty()) {
            return key.getCertificateChain();
        } else if (key.getCertificate() != null) {
            return Collections.singletonList(key.getCertificate());
        }
        return null;
    }

}
