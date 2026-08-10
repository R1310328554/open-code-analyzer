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

import javax.crypto.Mac;

/**
 * 基于 HMAC（HS256/HS384/HS512）的 JWS 签名上下文。
 */
public class MacSignatureSignerContext implements SignatureSignerContext {

    /** 封装对称密钥及其元数据的包装对象。 */
    private final KeyWrapper key;

    /**
     * @param key 含对称密钥与算法信息的密钥包装
     * @throws SignatureException 密钥不可用或算法不支持时抛出
     */
    public MacSignatureSignerContext(KeyWrapper key) throws SignatureException {
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
        return JavaAlgorithm.getJavaAlgorithmForHash(key.getAlgorithmOrDefault());
    }

    /**
     * 使用 HMAC 对给定字节序列计算 MAC 作为 JWS 签名。
     *
     * @param data 待签名原始字节
     * @return HMAC 签名结果
     * @throws SignatureException 签名过程失败时抛出
     */
    @Override
    public byte[] sign(byte[] data) throws SignatureException {
        try {
            Mac mac = Mac.getInstance(JavaAlgorithm.getJavaAlgorithm(key.getAlgorithmOrDefault()));
            mac.init(key.getSecretKey());
            mac.update(data);
            return mac.doFinal();
        } catch (Exception e) {
            throw new SignatureException("Signing failed", e);
        }
    }

}
