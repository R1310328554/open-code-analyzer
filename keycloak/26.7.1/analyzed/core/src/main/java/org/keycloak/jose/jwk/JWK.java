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

package org.keycloak.jose.jwk;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.keycloak.common.util.PemUtils;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON Web Key（JWK）基类，映射 RFC 7517 通用字段；子类（如 {@link RSAPublicJWK}）承载算法专用参数。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class JWK {

    /** JSON 属性名：密钥 ID（{@code kid}）。 */
    public static final String KEY_ID = "kid";

    /** JSON 属性名：密钥类型（{@code kty}）。 */
    public static final String KEY_TYPE = "kty";

    /** JSON 属性名：算法（{@code alg}）。 */
    public static final String ALGORITHM = "alg";

    /** JSON 属性名：公钥用途（{@code use}）。 */
    public static final String PUBLIC_KEY_USE = "use";

    /** JSON 属性名：X.509 证书链（{@code x5c}）。 */
    public static final String X5C = "x5c";

    /** JSON 属性名：X.509 证书 SHA-1 指纹（{@code x5t}）。 */
    public static final String SHA1_509_THUMBPRINT = "x5t";

    /** JSON 属性名：X.509 证书 SHA-256 指纹（{@code x5t#S256}）。 */
    public static final String SHA256_509_THUMBPRINT = "x5t#S256";

    /**
     * 公钥用途枚举；与 {@link org.keycloak.crypto.KeyUse} 重复，新代码应优先使用后者。
     */
    @Deprecated
    public enum Use {
        /** 签名。 */
        SIG("sig"),
        /** 加密。 */
        ENCRYPTION("enc"),
        /** JWT SVID。 */
        JWT_SVID("jwt-svid");

        private String str;

        Use(String str) {
            this.str = str;
        }

        /** 返回 JWK 规范中的用途字符串。 */
        public String asString() {
            return str;
        }
    }

    @JsonProperty(KEY_ID)
    private String keyId;

    @JsonProperty(KEY_TYPE)
    private String keyType;

    @JsonProperty(ALGORITHM)
    private String algorithm;

    @JsonProperty(PUBLIC_KEY_USE)
    private String publicKeyUse;

    @JsonProperty(X5C)
    private String[] x509CertificateChain;

    @JsonProperty(SHA1_509_THUMBPRINT)
    private String sha1x509Thumbprint;

    @JsonProperty(SHA256_509_THUMBPRINT)
    private String sha256x509Thumbprint;

    /** 未映射到标准字段的扩展声明。 */
    protected Map<String, Object> otherClaims = new HashMap<String, Object>();


    /** 返回密钥 ID。 */
    public String getKeyId() {
        return keyId;
    }

    /** 设置密钥 ID。 */
    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    /** 返回密钥类型（{@code kty}）。 */
    public String getKeyType() {
        return keyType;
    }

    /** 设置密钥类型。 */
    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    /** 返回算法名。 */
    public String getAlgorithm() {
        return algorithm;
    }

    /** 设置算法名。 */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /** 返回公钥用途（{@code sig}/{@code enc} 等）。 */
    public String getPublicKeyUse() {
        return publicKeyUse;
    }

    /** 设置公钥用途。 */
    public void setPublicKeyUse(String publicKeyUse) {
        this.publicKeyUse = publicKeyUse;
    }

    /** 返回 X.509 证书链（PEM 片段数组）。 */
    public String[] getX509CertificateChain() {
        return x509CertificateChain;
    }

    /** 设置 X.509 证书链。 */
    public void setX509CertificateChain(String[] x509CertificateChain) {
        this.x509CertificateChain = x509CertificateChain;
    }

    /**
     * 返回 SHA-1 证书指纹；若未显式设置且存在 {@code x5c}，则按需计算。
     *
     * @return Base64URL 编码的 SHA-1 指纹
     */
    public String getSha1x509Thumbprint() {
        if (sha1x509Thumbprint == null && x509CertificateChain != null && x509CertificateChain.length > 0) {
            try {
                sha1x509Thumbprint = PemUtils.generateThumbprint(x509CertificateChain, "SHA-1");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        return sha1x509Thumbprint;
    }

    /** 设置 SHA-1 证书指纹。 */
    public void setSha1x509Thumbprint(String sha1x509Thumbprint) {
        this.sha1x509Thumbprint = sha1x509Thumbprint;
    }

    /**
     * 返回 SHA-256 证书指纹；若未显式设置且存在 {@code x5c}，则按需计算。
     *
     * @return Base64URL 编码的 SHA-256 指纹
     */
    public String getSha256x509Thumbprint() {
        if (sha256x509Thumbprint == null && x509CertificateChain != null && x509CertificateChain.length > 0) {
            try {
                sha256x509Thumbprint = PemUtils.generateThumbprint(x509CertificateChain, "SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        return sha256x509Thumbprint;
    }

    /** 设置 SHA-256 证书指纹。 */
    public void setSha256x509Thumbprint(String sha256x509Thumbprint) {
        this.sha256x509Thumbprint = sha256x509Thumbprint;
    }

    /** 返回扩展声明映射（Jackson {@code @JsonAnyGetter}）。 */
    @JsonAnyGetter
    public Map<String, Object> getOtherClaims() {
        return otherClaims;
    }

    /** 写入单个扩展声明（Jackson {@code @JsonAnySetter}）。 */
    @JsonAnySetter
    public void setOtherClaims(String name, Object value) {
        otherClaims.put(name, value);
    }

    /**
     * 以统一方式读取自定义声明。子类（如 {@link OKPPublicJWK}）可能将声明存为 Java 属性，
     * 而本类则存入 {@link #otherClaims}；本方法两种来源均可透明访问。
     *
     * @param claimName 声明名
     * @param claimType 期望类型
     * @return 声明值，不存在时返回 {@code null}
     */
    @JsonIgnore
    public <T> T getOtherClaim(String claimName, Class<T> claimType) {
        Object o = getOtherClaims().get(claimName);
        return o == null ? null : claimType.cast(o);
    }

}
