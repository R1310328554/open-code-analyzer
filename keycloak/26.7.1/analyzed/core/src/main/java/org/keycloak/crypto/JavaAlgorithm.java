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

/**
 * JWA/JOSE 算法标识与 Java JCA {@link java.security.Signature}、哈希算法名称之间的映射工具。
 */
public class JavaAlgorithm {

    /** RSA PKCS#1 v1.5 + SHA-256 签名算法名。 */
    public static final String RS256 = "SHA256withRSA";
    public static final String RS384 = "SHA384withRSA";
    public static final String RS512 = "SHA512withRSA";
    /** HMAC 算法名。 */
    public static final String HS256 = "HMACSHA256";
    public static final String HS384 = "HMACSHA384";
    public static final String HS512 = "HMACSHA512";
    /** ECDSA 签名算法名。 */
    public static final String ES256 = "SHA256withECDSA";
    public static final String ES384 = "SHA384withECDSA";
    public static final String ES512 = "SHA512withECDSA";
    /** RSA-PSS 签名算法名。 */
    public static final String PS256 = "SHA256withRSAandMGF1";
    public static final String PS384 = "SHA384withRSAandMGF1";
    public static final String PS512 = "SHA512withRSAandMGF1";
    /** EdDSA 曲线算法名。 */
    public static final String Ed25519 = "Ed25519";
    public static final String Ed448 = "Ed448";
    public static final String AES = "AES";
    public static final String ECDSA = "ECDSA";

    /** 标准哈希算法名。 */
    public static final String SHA256 = "SHA-256";
    public static final String SHA384 = "SHA-384";
    public static final String SHA512 = "SHA-512";
    public static final String SHAKE256 = "SHAKE256";

    /**
     * 将 JWA 算法标识映射为 JCA 签名/ MAC 算法名（无曲线时使用默认）。
     *
     * @param algorithm JWA 算法标识
     * @return JCA 算法名
     */
    public static String getJavaAlgorithm(String algorithm) {
        return getJavaAlgorithm(algorithm, null);
    }

    /**
     * 将 JWA 算法标识映射为 JCA 签名/ MAC 算法名；EdDSA 需结合曲线参数。
     *
     * @param algorithm JWA 算法标识
     * @param curve EdDSA 曲线名称，可为 {@code null}
     * @return JCA 算法名
     */
    public static String getJavaAlgorithm(String algorithm, String curve) {
        switch (algorithm) {
            case Algorithm.RS256:
                return RS256;
            case Algorithm.RS384:
                return RS384;
            case Algorithm.RS512:
                return RS512;
            case Algorithm.HS256:
                return HS256;
            case Algorithm.HS384:
                return HS384;
            case Algorithm.HS512:
                return HS512;
            case Algorithm.ES256:
                return ES256;
            case Algorithm.ES384:
                return ES384;
            case Algorithm.ES512:
                return ES512;
            case Algorithm.PS256:
                return PS256;
            case Algorithm.PS384:
                return PS384;
            case Algorithm.PS512:
                return PS512;
            case Algorithm.EdDSA:
                if (curve != null) {
                    return curve;
                }
                return Ed25519;
            case Algorithm.AES:
                return AES;
            default:
                throw new IllegalArgumentException("Unknown algorithm " + algorithm);
        }
    }

    /**
     * 返回与 JWA 算法关联的哈希算法名（无曲线时使用默认）。
     *
     * @param algorithm JWA 算法标识
     * @return 哈希算法名
     */
    public static String getJavaAlgorithmForHash(String algorithm) {
        return getJavaAlgorithmForHash(algorithm, null);
    }

    /**
     * 返回与 JWA 算法关联的哈希算法名；EdDSA 依曲线选择 SHA-512 或 SHAKE256。
     *
     * @param algorithm JWA 算法标识
     * @param curve EdDSA 曲线名称，可为 {@code null}
     * @return 哈希算法名
     */
    public static String getJavaAlgorithmForHash(String algorithm, String curve) {
        switch (algorithm) {
            case Algorithm.RS256:
                return SHA256;
            case Algorithm.RS384:
                return SHA384;
            case Algorithm.RS512:
                return SHA512;
            case Algorithm.HS256:
                return SHA256;
            case Algorithm.HS384:
                return SHA384;
            case Algorithm.HS512:
                return SHA512;
            case Algorithm.ES256:
                return SHA256;
            case Algorithm.ES384:
                return SHA384;
            case Algorithm.ES512:
                return SHA512;
            case Algorithm.PS256:
                return SHA256;
            case Algorithm.PS384:
                return SHA384;
            case Algorithm.PS512:
                return SHA512;
            case Algorithm.EdDSA:
                if (curve != null) {
                    switch (curve) {
                        case Algorithm.Ed25519:
                            return SHA512;
                        case Algorithm.Ed448:
                            return SHAKE256;
                        default:
                            throw new IllegalArgumentException("Unknown curve for EdDSA " + curve);
                    }
                }
                return SHA512;
            case Algorithm.AES:
                return AES;
            default:
                throw new IllegalArgumentException("Unknown algorithm " + algorithm);
        }
    }

    /**
     * 由 JCA 密钥算法名推导 JWK {@code kty} 类型。
     *
     * @param keyAlgorithm JCA 密钥算法名
     * @return JWK 密钥类型常量
     */
    public static String getKeyType(String keyAlgorithm) {
        switch (keyAlgorithm) {
            case KeyType.RSA:
                return KeyType.RSA;
            case KeyType.EC:
            case ECDSA:
                return KeyType.EC;
            case Algorithm.EdDSA:
            case Algorithm.Ed448:
            case Algorithm.Ed25519:
                return KeyType.OKP;
            default:
                return KeyType.OCT;
        }
    }

    /**
     * @param algorithm JWA 算法标识
     * @return 映射后的 JCA 算法名是否包含 RSA
     */
    public static boolean isRSAJavaAlgorithm(String algorithm) {
        return getJavaAlgorithm(algorithm).contains("RSA");
    }

    /**
     * @param algorithm JWA 算法标识
     * @return 映射后的 JCA 算法名是否包含 ECDSA
     */
    public static boolean isECJavaAlgorithm(String algorithm) {
        return getJavaAlgorithm(algorithm).contains("ECDSA");
    }

    /**
     * @param algorithm JWA 算法标识
     * @return 映射后的 JCA 算法名是否为 EdDSA 系列
     */
    public static boolean isEddsaJavaAlgorithm(String algorithm) {
        return getJavaAlgorithm(algorithm).contains("Ed");
    }

}
