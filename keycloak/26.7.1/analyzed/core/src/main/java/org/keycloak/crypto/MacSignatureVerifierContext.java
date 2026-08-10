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

import java.security.MessageDigest;
import javax.crypto.Mac;

import org.keycloak.common.VerificationException;

/**
 * 基于 HMAC（HS256/HS384/HS512）的 JWS 签名验证上下文。
 */
public class MacSignatureVerifierContext implements SignatureVerifierContext {

    /** 封装对称密钥及其元数据的包装对象。 */
    private final KeyWrapper key;

    /**
     * @param key 含对称密钥与算法信息的密钥包装
     */
    public MacSignatureVerifierContext(KeyWrapper key) {
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
     * 重新计算 HMAC 并与给定签名做常量时间比较。
     *
     * @param data 原始待验签字节
     * @param signature 待验证的 HMAC 签名
     * @return 签名匹配返回 {@code true}
     * @throws VerificationException 验签过程失败时抛出
     */
    @Override
    public boolean verify(byte[] data, byte[] signature) throws VerificationException {
        try {
            Mac mac = Mac.getInstance(JavaAlgorithm.getJavaAlgorithm(key.getAlgorithmOrDefault()));
            mac.init(key.getSecretKey());
            mac.update(data);
            byte[] verificationSignature = mac.doFinal();
            return MessageDigest.isEqual(verificationSignature, signature);
        } catch (Exception e) {
            throw new VerificationException("Signing failed", e);
        }
    }

}
