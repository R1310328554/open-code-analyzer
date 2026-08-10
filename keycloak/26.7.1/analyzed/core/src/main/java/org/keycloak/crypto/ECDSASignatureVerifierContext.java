/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.common.VerificationException;

/**
 * ECDSA 验签上下文：将 JWS 的 R||S 拼接签名转换为 DER 后再调用父类 JCA 验签。
 */
public class ECDSASignatureVerifierContext extends AsymmetricSignatureVerifierContext{
    /**
     * @param key 含 ECDSA 公钥的密钥包装
     */
    public ECDSASignatureVerifierContext(KeyWrapper key) {
        super(key);
    }

    /**
     * 验签前将 JWS 拼接签名转换为 JCA 所需的 DER 格式。
     *
     * @param data 原始待验签字节
     * @param signature JWS 格式的 ECDSA 签名
     * @return 验签是否通过
     * @throws VerificationException 格式转换或验签失败时抛出
     */
    @Override
    public boolean verify(byte[] data, byte[] signature) throws VerificationException {
        try {
            int expectedSize = ECDSAAlgorithm.getSignatureLength(getAlgorithm());
            byte[] derSignature = ECDSAAlgorithm.concatenatedRSToASN1DER(signature, expectedSize);
            return super.verify(data, derSignature);
        } catch (Exception e) {
            throw new VerificationException("Verification failed", e);
        }
    }
}
