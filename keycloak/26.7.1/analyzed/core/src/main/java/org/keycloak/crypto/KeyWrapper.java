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

import java.security.Key;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.SecretKey;

import static org.keycloak.common.crypto.CryptoConstants.EC_KEY_SECP256R1;
import static org.keycloak.common.crypto.CryptoConstants.EC_KEY_SECP384R1;
import static org.keycloak.common.crypto.CryptoConstants.EC_KEY_SECP521R1;

/**
 * 密钥包装类：聚合 JWK/JWKS 中的 kid、算法、用途、状态及各类密钥材料（对称/非对称/证书链）。
 */
public class KeyWrapper {

    /** 提供该密钥的 CryptoProvider 标识。 */
    private String providerId;
    /** 提供者优先级，数值越大越优先。 */
    private long providerPriority;
    /** JWK {@code kid}（密钥 ID）。 */
    private String kid;
    /** JWK {@code alg} 声明值。 */
    private String algorithm;
    /** JWK {@code kty} 密钥类型。 */
    private String type;
    /** 密钥用途（签名/加密）。 */
    private KeyUse use;
    /** 密钥生命周期状态。 */
    private KeyStatus status;
    /** 对称密钥材料（HMAC 等）。 */
    private SecretKey secretKey;
    /** 公钥材料。 */
    private Key publicKey;
    /** 私钥材料。 */
    private Key privateKey;
    /** 关联的 X.509 证书。 */
    private X509Certificate certificate;
    /** 完整证书链。 */
    private List<X509Certificate> certificateChain;
    /** 是否为默认客户端证书密钥。 */
    private boolean isDefaultClientCertificate;
    /** 椭圆曲线名称（EC/OKP 密钥）。 */
    private String curve;

    /** @return 提供该密钥的 CryptoProvider 标识 */
    public String getProviderId() {
        return providerId;
    }

    /** @param providerId 提供该密钥的 CryptoProvider 标识 */
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    /** @return 提供者优先级 */
    public long getProviderPriority() {
        return providerPriority;
    }

    /** @param providerPriority 提供者优先级 */
    public void setProviderPriority(long providerPriority) {
        this.providerPriority = providerPriority;
    }

    /** @return JWK {@code kid} */
    public String getKid() {
        return kid;
    }

    /** @param kid JWK {@code kid} */
    public void setKid(String kid) {
        this.kid = kid;
    }

    /**
     * 返回可选 {@code alg} 声明的原始值。
     *
     * @return 算法标识
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * 返回 {@code alg} 声明值；若未显式设置，则按密钥类型与曲线推断默认值。
     *
     * <p>对 {@link KeyType#RSA} 密钥，默认 {@link Algorithm#RS256}（OIDC 推荐算法）。
     *
     * <p>对 {@link KeyType#EC} 密钥，按曲线返回 {@link Algorithm#ES256}、{@link Algorithm#ES384}
     * 或 {@link Algorithm#ES512}。
     *
     * <p>对 {@link KeyType#OKP} 密钥，返回 {@link Algorithm#EdDSA}（该类型唯一支持的算法）。
     *
     * @return 已设置的算法或按密钥类型推断的默认值
     */
    public String getAlgorithmOrDefault() {
        if (algorithm == null && type != null) {
            switch (type) {
                case KeyType.EC:
                    if (curve != null) {
                        switch (curve) {
                            case "P-256":
                            case EC_KEY_SECP256R1:
                                return Algorithm.ES256;
                            case "P-384":
                            case EC_KEY_SECP384R1:
                                return Algorithm.ES384;
                            case "P-512":
                            case "P-521":
                            case EC_KEY_SECP521R1:
                                return Algorithm.ES512;
                        }
                    }
                case KeyType.RSA:
                    return Algorithm.RS256;
                case KeyType.OKP:
                    return Algorithm.EdDSA;
            }
        }
        return algorithm;
    }

    /** @param algorithm JWK {@code alg} 声明值 */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /** @return JWK {@code kty} 密钥类型 */
    public String getType() {
        return type;
    }

    /** @param type JWK {@code kty} 密钥类型 */
    public void setType(String type) {
        this.type = type;
    }

    /** @return 密钥用途 */
    public KeyUse getUse() {
        return use;
    }

    /** @param use 密钥用途 */
    public void setUse(KeyUse use) {
        this.use = use;
    }

    /** @return 密钥生命周期状态 */
    public KeyStatus getStatus() {
        return status;
    }

    /** @param status 密钥生命周期状态 */
    public void setStatus(KeyStatus status) {
        this.status = status;
    }

    /** @return 对称密钥材料 */
    public SecretKey getSecretKey() {
        return secretKey;
    }

    /** @param secretKey 对称密钥材料 */
    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    /** @return 私钥材料 */
    public Key getPrivateKey() {
        return privateKey;
    }

    /** @param privateKey 私钥材料 */
    public void setPrivateKey(Key privateKey) {
        this.privateKey = privateKey;
    }

    /** @return 公钥材料 */
    public Key getPublicKey() {
        return publicKey;
    }

    /** @param publicKey 公钥材料 */
    public void setPublicKey(Key publicKey) {
        this.publicKey = publicKey;
    }

    /** @return 关联的 X.509 证书 */
    public X509Certificate getCertificate() {
        return certificate;
    }

    /** @param certificate 关联的 X.509 证书 */
    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }

    /** @return 完整证书链 */
    public List<X509Certificate> getCertificateChain() {
        return certificateChain;
    }

    /** @param certificateChain 完整证书链 */
    public void setCertificateChain(List<X509Certificate> certificateChain) {
        this.certificateChain = certificateChain;
    }

    /** @return 是否为默认客户端证书密钥 */
    public boolean isDefaultClientCertificate() {
        return isDefaultClientCertificate;
    }

    /** @param isDefaultClientCertificate 是否为默认客户端证书密钥 */
    public void setIsDefaultClientCertificate(boolean isDefaultClientCertificate) {
        this.isDefaultClientCertificate = isDefaultClientCertificate;
    }

    /** @param curve 椭圆曲线名称 */
    public void setCurve(String curve) {
        this.curve = curve;
    }

    /** @return 椭圆曲线名称 */
    public String getCurve() {
        return curve;
    }

    /**
     * 浅拷贝当前密钥包装，证书链单独复制为新列表。
     *
     * @return 克隆后的密钥包装
     */
    public KeyWrapper cloneKey() {
        KeyWrapper key = new KeyWrapper();
        key.providerId = this.providerId;
        key.providerPriority = this.providerPriority;
        key.kid = this.kid;
        key.algorithm = this.algorithm;
        key.type = this.type;
        key.use = this.use;
        key.status = this.status;
        key.secretKey = this.secretKey;
        key.publicKey = this.publicKey;
        key.privateKey = this.privateKey;
        key.certificate = this.certificate;
        key.curve = this.curve;
        if (this.certificateChain != null) {
            key.certificateChain = new ArrayList<>(this.certificateChain);
        }
        key.isDefaultClientCertificate = this.isDefaultClientCertificate;
        return key;
    }
}
