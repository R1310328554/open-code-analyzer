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

import java.io.IOException;

import org.keycloak.common.crypto.CryptoIntegration;

/**
 * ECDSA 签名算法枚举，定义各 JWA 算法对应的 JWS 签名长度（R||S 拼接字节数），
 * 并提供 JWS 与 JCA 之间 DER/拼接格式的转换委托。
 *
 * @author rmartinc
 */
public enum ECDSAAlgorithm {
    /** ES256：P-256 曲线，签名长度 64 字节。 */
    ES256(64),
    /** ES384：P-384 曲线，签名长度 96 字节。 */
    ES384(96),
    /** ES512：P-521 曲线，签名长度 132 字节。 */
    ES512(132);

    /** JWS 使用的 R||S 拼接签名长度（字节）。 */
    private final int signatureLength;

    ECDSAAlgorithm(int signatureLength) {
        this.signatureLength = signatureLength;
    }

    /**
     * @return JWS 拼接签名长度（字节）
     */
    public int getSignatureLength() {
        return this.signatureLength;
    }

    /**
     * 按 JWA 算法名查询对应的拼接签名长度。
     *
     * @param alg JWA 算法标识（如 ES256）
     * @return 签名长度（字节）
     */
    public static int getSignatureLength(String alg) {
        return valueOf(alg).getSignatureLength();
    }

    /**
     * 将 JWS 的 R||S 拼接签名转换为 JCA 所需的 ASN.1 DER 编码。
     *
     * @param signature 拼接格式签名
     * @param signLength 期望的拼接长度
     * @return DER 编码签名
     * @throws IOException 转换失败时抛出
     */
    public static byte[] concatenatedRSToASN1DER(final byte[] signature, int signLength) throws IOException {
        return CryptoIntegration.getProvider().getEcdsaCryptoProvider().concatenatedRSToASN1DER(signature, signLength);
    }

    /**
     * 将 JCA 的 ASN.1 DER 签名转换为 JWS 使用的 R||S 拼接格式。
     *
     * @param derEncodedSignatureValue DER 编码签名
     * @param signLength 目标拼接长度
     * @return 拼接格式签名
     * @throws IOException 转换失败时抛出
     */
    public static byte[] asn1derToConcatenatedRS(final byte[] derEncodedSignatureValue, int signLength) throws IOException {
        return CryptoIntegration.getProvider().getEcdsaCryptoProvider().asn1derToConcatenatedRS(derEncodedSignatureValue, signLength);
    }
}
