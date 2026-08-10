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

package org.keycloak.protocol.oid4vc.issuance.signing.vcdm;

/**
 * W3C 注册的 Linked Data 签名套件（Signature Suite）枚举。
 * <p>取值与 {@see https://w3c-ccg.github.io/ld-cryptosuite-registry} 注册表一致，用于 VCDM（Verifiable Credentials Data Model）Linked Data 证明的算法标识。</p>
 *
 * @author <a href="https://github.com/wistefan">Stefan Wiedemann</a>
 */
public enum LDSignatureType {

    /** Ed25519 签名套件（2018 版）。 */
    ED_25519_SIGNATURE_2018("Ed25519Signature2018"),
    /** Ed25519 签名套件（2020 版）。 */
    ED_25519_SIGNATURE_2020("Ed25519Signature2020"),
    /** ECDSA secp256k1 曲线签名套件（2019 版）。 */
    ECDSA_SECP_256K1_SIGNATURE_2019("EcdsaSecp256k1Signature2019"),
    /** RSA 签名套件（2018 版）。 */
    RSA_SIGNATURE_2018("RsaSignature2018"),
    /** JSON Web Signature 2020 签名套件。 */
    JSON_WEB_SIGNATURE_2020("JsonWebSignature2020"),
    /** 基于 JCS 规范化的 Ed25519 签名套件（2020 版）。 */
    JCS_ED_25519_SIGNATURE_2020("JcsEd25519Signature2020");

    /** 注册表中的字符串标识值。 */
    private final String value;

    /** @param value 签名套件在注册表中的字符串标识 */
    LDSignatureType(String value) {
        this.value = value;
    }

    /** @return 签名套件的字符串标识值 */
    public String getValue() {
        return value;
    }

    /**
     * 按字符串标识解析签名套件（忽略大小写）。
     * @param value 注册表标识字符串
     * @return 匹配的枚举常量
     * @throws IllegalArgumentException 无匹配类型时抛出
     */
    public static LDSignatureType getByValue(String value) {
        for (LDSignatureType signatureType : values()) {
            if (signatureType.getValue().equalsIgnoreCase(value))
                return signatureType;
        }
        throw new IllegalArgumentException(String.format("No signature of type %s exists.", value));
    }
}