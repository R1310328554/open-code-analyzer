/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

/**
 * ECDSA 签名上下文：先以 JCA DER 格式签名，再转换为 JWS 要求的 R||S 拼接格式。
 *
 * @author rmartinc
 */
public class ECDSASignatureSignerContext extends AsymmetricSignatureSignerContext {

    /**
     * @param key 含 ECDSA 私钥的密钥包装
     * @throws SignatureException 密钥不可用或算法不支持时抛出
     */
    public ECDSASignatureSignerContext(KeyWrapper key) throws SignatureException {
        super(key);
    }

    /**
     * 签名并将 DER 结果转换为 JWS 拼接格式。
     *
     * @param data 待签名原始字节
     * @return JWS 格式的 ECDSA 签名
     * @throws SignatureException 签名或格式转换失败时抛出
     */
    @Override
    public byte[] sign(byte[] data) throws SignatureException {
        try {
            int size = ECDSAAlgorithm.getSignatureLength(getAlgorithm());
            return ECDSAAlgorithm.asn1derToConcatenatedRS(super.sign(data), size);
        } catch (Exception e) {
            throw new SignatureException("Signing failed", e);
        }
    }
}
