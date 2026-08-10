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

import org.keycloak.common.util.PemUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RSA 公钥的 JWK 表示，包含模数 {@code n} 与公钥指数 {@code e}。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class RSAPublicJWK extends JWK {

    /** JWK 密钥类型：RSA。 */
    public static final String RSA = "RSA";
    /** 常用算法名 RS256（与 {@code alg} 字段对应）。 */
    public static final String RS256 = "RS256";

    /** JSON 属性名：RSA 模数。 */
    public static final String MODULUS = "n";
    /** JSON 属性名：RSA 公钥指数。 */
    public static final String PUBLIC_EXPONENT = "e";

    @JsonProperty(MODULUS)
    private String modulus;

    @JsonProperty("e")
    private String publicExponent;

    @JsonProperty("x5c")
    private String[] x509CertificateChain;

    private String sha1x509Thumbprint;

    private String sha256x509Thumbprint;

    /** 返回 RSA 模数（Base64URL）。 */
    public String getModulus() {
        return modulus;
    }

    /** 设置 RSA 模数。 */
    public void setModulus(String modulus) {
        this.modulus = modulus;
    }

    /** 返回 RSA 公钥指数（Base64URL）。 */
    public String getPublicExponent() {
        return publicExponent;
    }

    /** 设置 RSA 公钥指数。 */
    public void setPublicExponent(String publicExponent) {
        this.publicExponent = publicExponent;
    }
    
    /** 返回 X.509 证书链。 */
    public String[] getX509CertificateChain() {
        return x509CertificateChain;
    }

    /**
     * 设置 X.509 证书链，并同步计算 SHA-1/SHA-256 指纹。
     *
     * @param x509CertificateChain PEM 片段数组
     */
    public void setX509CertificateChain(String[] x509CertificateChain) {
        this.x509CertificateChain = x509CertificateChain;
        if (x509CertificateChain != null && x509CertificateChain.length > 0) {
            try {
                sha1x509Thumbprint = PemUtils.generateThumbprint(x509CertificateChain, "SHA-1");
                sha256x509Thumbprint = PemUtils.generateThumbprint(x509CertificateChain, "SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /** 返回 SHA-1 证书指纹（{@code x5t}）。 */
    @JsonProperty("x5t")
    public String getSha1x509Thumbprint() {
        return sha1x509Thumbprint;
    }

    /** 返回 SHA-256 证书指纹（{@code x5t#S256}）。 */
    @JsonProperty("x5t#S256")
    public String getSha256x509Thumbprint() {
        return sha256x509Thumbprint;
    }

    /**
     * 优先从 RSA 专用字段读取声明，否则委托 {@link JWK#getOtherClaim}。
     *
     * @param claimName 声明名
     * @param claimType 期望类型
     * @return 声明值，不存在时返回 {@code null}
     */
    @JsonIgnore
    @Override
    public <T> T getOtherClaim(String claimName, Class<T> claimType) {
        Object claim = null;
        switch (claimName) {
            case MODULUS:
                claim = getModulus();
                break;
            case PUBLIC_EXPONENT:
                claim = getPublicExponent();
                break;
        }
        if (claim != null) {
            return claimType.cast(claim);
        } else {
            return super.getOtherClaim(claimName, claimType);
        }
    }
}
