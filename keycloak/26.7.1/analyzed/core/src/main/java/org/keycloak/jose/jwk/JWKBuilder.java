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

import java.security.Key;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.List;

import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.KeyUtils;
import org.keycloak.common.util.PemUtils;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.KeyType;
import org.keycloak.crypto.KeyUse;

import static org.keycloak.jose.jwk.JWKUtil.toIntegerBytes;

/**
 * 从 Java {@link PublicKey} 构建各类 JWK 的流式构建器（RSA、EC、OKP、AKP）。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class JWKBuilder {

    // 内部工具类，仅在支持 EdEC 的 JDK 版本上加载 EdECUtilsImpl
    protected static final EdECUtils EdEC_UTILS;

    static {
        EdECUtils tmp;
        try {
            // 检测运行时能否加载 EdEC 实现类
            tmp = (EdECUtils) Class.forName("org.keycloak.jose.jwk.EdECUtilsImpl")
                    .getDeclaredConstructor().newInstance();
        } catch(Throwable e) {
            // 回退到不支持 EdEC 的实现
            tmp = new EdECUtilsUnsupportedImpl();
        }
        EdEC_UTILS = tmp;
    }

    /** 未指定用途时的默认公钥用途：签名。 */
    public static final KeyUse DEFAULT_PUBLIC_KEY_USE = KeyUse.SIG;

    protected String kid;

    protected String algorithm;

    private JWKBuilder() {
    }

    /** 创建新的构建器实例。 */
    public static JWKBuilder create() {
        return new JWKBuilder();
    }

    /**
     * 指定密钥 ID。
     *
     * @param kid 密钥 ID
     * @return 当前构建器（链式调用）
     */
    public JWKBuilder kid(String kid) {
        this.kid = kid;
        return this;
    }

    /**
     * 指定 JWK {@code alg} 字段。
     *
     * @param algorithm 算法名
     * @return 当前构建器
     */
    public JWKBuilder algorithm(String algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    /**
     * 以 RS256 算法构建 RSA JWK。
     *
     * @param key RSA 公钥
     * @return RSA JWK
     */
    public JWK rs256(PublicKey key) {
        this.algorithm = Algorithm.RS256;
        return rsa(key);
    }

    /**
     * 构建 AKP（如 ML-DSA）公钥 JWK。
     *
     * @param key AKP 公钥
     * @return AKP JWK
     */
    public JWK akp(PublicKey key) {
        AKPPublicJWK k = new AKPPublicJWK();

        String kid = this.kid != null ? this.kid : KeyUtils.createKeyId(key);
        k.setKeyId(kid);
        k.setKeyType(KeyType.AKP);
        k.setAlgorithm(algorithm);
        k.setPub(AKPUtils.toEncodedPub(key, algorithm));
        k.setPublicKeyUse(KeyUse.SIG.getSpecName());

        return k;
    }

    /** 以默认签名用途构建 RSA JWK。 */
    public JWK rsa(Key key) {
        return rsa(key, null, KeyUse.SIG);
    }

    /** 附带单张 X.509 证书构建 RSA JWK。 */
    public JWK rsa(Key key, X509Certificate certificate) {
        return rsa(key, Collections.singletonList(certificate), KeyUse.SIG);
    }

    /** 附带证书链构建 RSA JWK（用途未指定）。 */
    public JWK rsa(Key key, List<X509Certificate> certificates) {
        return rsa(key, certificates, null);
    }

    /**
     * 构建 RSA JWK，可选证书链与用途。
     *
     * @param key RSA 公钥
     * @param certificates X.509 证书链，可为 {@code null}
     * @param keyUse 密钥用途
     * @return RSA JWK
     */
    public JWK rsa(Key key, List<X509Certificate> certificates, KeyUse keyUse) {
        RSAPublicKey rsaKey = (RSAPublicKey) key;

        RSAPublicJWK k = new RSAPublicJWK();

        String kid = this.kid != null ? this.kid : KeyUtils.createKeyId(key);
        k.setKeyId(kid);
        k.setKeyType(KeyType.RSA);
        k.setAlgorithm(algorithm);
        k.setPublicKeyUse(keyUse == null ? KeyUse.SIG.getSpecName() : keyUse.getSpecName());
        k.setModulus(Base64Url.encode(toIntegerBytes(rsaKey.getModulus())));
        k.setPublicExponent(Base64Url.encode(toIntegerBytes(rsaKey.getPublicExponent())));

        if (certificates != null && !certificates.isEmpty()) {
            String[] certificateChain = new String[certificates.size()];
            for (int i = 0; i < certificates.size(); i++) {
                certificateChain[i] = PemUtils.encodeCertificate(certificates.get(i));
            }
            k.setX509CertificateChain(certificateChain);
        }

        return k;
    }

    /**
     * 构建 RSA JWK 并覆盖用途字段。
     *
     * @param key RSA 公钥
     * @param keyUse 密钥用途
     * @return RSA JWK
     */
    public JWK rsa(Key key, KeyUse keyUse) {
        JWK k = rsa(key);
        String keyUseString = keyUse == null ? DEFAULT_PUBLIC_KEY_USE.getSpecName() : keyUse.getSpecName();
        if (KeyUse.ENC == keyUse) keyUseString = "enc";
        k.setPublicKeyUse(keyUseString);
        return k;
    }

    /** 以默认签名用途构建 EC JWK。 */
    public JWK ec(Key key) {
        return ec(key, DEFAULT_PUBLIC_KEY_USE);
    }

    /** 指定用途构建 EC JWK。 */
    public JWK ec(Key key, KeyUse keyUse) {
        return this.ec(key, null, keyUse);
    }

    /**
     * 构建 EC JWK，可选证书链与用途。
     *
     * @param key EC 公钥
     * @param certificates X.509 证书链
     * @param keyUse 密钥用途
     * @return EC JWK
     */
    public JWK ec(Key key, List<X509Certificate> certificates, KeyUse keyUse) {
        ECPublicKey ecKey = (ECPublicKey) key;

        ECPublicJWK k = new ECPublicJWK();

        String kid = this.kid != null ? this.kid : KeyUtils.createKeyId(key);
        int fieldSize = ecKey.getParams().getCurve().getField().getFieldSize();

        k.setKeyId(kid);
        k.setKeyType(KeyType.EC);
        k.setAlgorithm(algorithm);
        k.setPublicKeyUse(keyUse == null ? DEFAULT_PUBLIC_KEY_USE.getSpecName() : keyUse.getSpecName());
        k.setCrv("P-" + fieldSize);
        k.setX(Base64Url.encode(toIntegerBytes(ecKey.getW().getAffineX(), fieldSize)));
        k.setY(Base64Url.encode(toIntegerBytes(ecKey.getW().getAffineY(), fieldSize)));

        if (certificates != null && !certificates.isEmpty()) {
            String[] certificateChain = new String[certificates.size()];
            for (int i = 0; i < certificates.size(); i++) {
                certificateChain[i] = PemUtils.encodeCertificate(certificates.get(i));
            }
            k.setX509CertificateChain(certificateChain);
        }

        return k;
    }

    /** 以默认签名用途构建 OKP（EdDSA）JWK。 */
    public JWK okp(Key key) {
        return okp(key, DEFAULT_PUBLIC_KEY_USE);
    }

    /**
     * 构建 OKP JWK，委托 {@link EdECUtils#okp}（JDK 版本决定具体实现）。
     *
     * @param key EdEC 公钥
     * @param keyUse 密钥用途
     * @return OKP JWK
     */
    public JWK okp(Key key, KeyUse keyUse) {
        return EdEC_UTILS.okp(kid, algorithm, key, keyUse);
    }
}
