/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.crypto.elytron;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Signature;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.net.ssl.SSLSocketFactory;

import org.keycloak.common.crypto.CertificateUtilsProvider;
import org.keycloak.common.crypto.CryptoConstants;
import org.keycloak.common.crypto.CryptoProvider;
import org.keycloak.common.crypto.ECDSACryptoProvider;
import org.keycloak.common.crypto.PemUtilsProvider;
import org.keycloak.common.crypto.UserIdentityExtractorProvider;
import org.keycloak.common.util.KeystoreUtil.KeystoreFormat;
import org.keycloak.crypto.JavaAlgorithm;

/**
 * WildFly Elytron 密码学提供器，聚合 JWE 算法、证书工具、OCSP、ECDSA 等 Elytron 实现。
 */
public class WildFlyElytronProvider implements CryptoProvider {

    /** 按算法名缓存的 JWE 算法提供器实例。 */
    private Map<String, Object> providers = new ConcurrentHashMap<>();

    /** 注册 A128KW、RSA、ECDH-ES 等 JWE 算法提供器。 */
    public WildFlyElytronProvider() {
        providers.put(CryptoConstants.A128KW, new AesKeyWrapAlgorithmProvider());
        providers.put(CryptoConstants.RSA1_5, new ElytronRsaKeyEncryptionJWEAlgorithmProvider("RSA/ECB/PKCS1Padding"));
        providers.put(CryptoConstants.RSA_OAEP, new ElytronRsaKeyEncryptionJWEAlgorithmProvider("RSA/ECB/OAEPWithSHA-1AndMGF1Padding"));
        providers.put(CryptoConstants.RSA_OAEP_256, new ElytronRsaKeyEncryption256JWEAlgorithmProvider("RSA/ECB/OAEPWithSHA-256AndMGF1Padding"));
        providers.put(CryptoConstants.ECDH_ES, new ElytronEcdhEsAlgorithmProvider());
        providers.put(CryptoConstants.ECDH_ES_A128KW, new ElytronEcdhEsAlgorithmProvider());
        providers.put(CryptoConstants.ECDH_ES_A192KW, new ElytronEcdhEsAlgorithmProvider());
        providers.put(CryptoConstants.ECDH_ES_A256KW, new ElytronEcdhEsAlgorithmProvider());
    }

    /** {@inheritDoc} Elytron 模式不暴露 BouncyCastle Provider，返回 null。 */
    @Override
    public Provider getBouncyCastleProvider() {
        return null;
    }

    /** {@inheritDoc} 提供器排序优先级（200）。 */
    @Override
    public int order() {
        return 200;
    }

    /** {@inheritDoc} 按算法名获取已注册的 JWE 算法提供器。 */
    @Override
    public <T> T getAlgorithmProvider(Class<T> clazz, String algorithm) {
        Object o = providers.get(algorithm);
        if (o == null) {
            throw new IllegalArgumentException("Not found provider of algorithm type: " + algorithm);
        }
        return clazz.cast(o);
    }

    /** {@inheritDoc} 返回 Elytron 证书工具实现。 */
    @Override
    public CertificateUtilsProvider getCertificateUtils() {
        return new ElytronCertificateUtilsProvider();
    }

    /** {@inheritDoc} 返回 Elytron PEM 工具实现。 */
    @Override
    public PemUtilsProvider getPemUtils() {
        return new ElytronPEMUtilsProvider();
    }

    /** {@inheritDoc} 返回 Elytron OCSP 提供器。 */
    @Override
    public <T> T getOCSPProver(Class<T> clazz) {
        return clazz.cast(new ElytronOCSPProvider());
    }

    /** {@inheritDoc} 返回 Elytron 用户身份提取器工厂。 */
    @Override
    public UserIdentityExtractorProvider getIdentityExtractorProvider() {
        return new ElytronUserIdentityExtractorProvider();
    }

    /** {@inheritDoc} 返回 Elytron ECDSA 辅助实现。 */
    @Override
    public ECDSACryptoProvider getEcdsaCryptoProvider() {
        return new ElytronECDSACryptoProvider();
    }

    /** {@inheritDoc} 根据曲线名生成 EC 参数规格。 */
    @Override
    public ECParameterSpec createECParams(String curveName) {
        AlgorithmParameters params;
        try {
            params = AlgorithmParameters.getInstance("EC");
            params.init(new ECGenParameterSpec(curveName));
            return params.getParameterSpec(ECParameterSpec.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate EC parameter spec", e);
        }
    }

    /** {@inheritDoc} 获取密钥对生成器。 */
    @Override
    public KeyPairGenerator getKeyPairGen(String algorithm) throws NoSuchAlgorithmException {
        return KeyPairGenerator.getInstance(algorithm);
    }

    /** {@inheritDoc} 获取密钥工厂（ECDSA 映射为 EC）。 */
    @Override
    public KeyFactory getKeyFactory(String algorithm) throws NoSuchAlgorithmException {
        if("ECDSA".equals(algorithm)) {
            // ECDSA 不是 JavaSE 列出的 KeyFactory 算法名
            // see https://docs.oracle.com/en/java/javase/11/docs/specs/security/standard-names.html#cipher-algorithm-names
            algorithm = "EC";
        }
        return KeyFactory.getInstance(algorithm);
    }

    /** {@inheritDoc} 获取 AES/CBC/PKCS5Padding Cipher。 */
    @Override
    public Cipher getAesCbcCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
        return Cipher.getInstance("AES/CBC/PKCS5Padding");
    }

    /** {@inheritDoc} 获取 AES/GCM/NoPadding Cipher。 */
    @Override
    public Cipher getAesGcmCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
        return Cipher.getInstance("AES/GCM/NoPadding");
    }

    /** {@inheritDoc} 获取对称密钥工厂。 */
    @Override
    public SecretKeyFactory getSecretKeyFact(String keyAlgorithm) throws NoSuchAlgorithmException {
        return SecretKeyFactory.getInstance(keyAlgorithm);
    }

    /** {@inheritDoc} 按格式获取 KeyStore 实例。 */
    @Override
    public KeyStore getKeyStore(KeystoreFormat format) throws KeyStoreException {
            return KeyStore.getInstance(format.toString());
    }

    /** {@inheritDoc} 获取 X.509 证书工厂。 */
    @Override
    public CertificateFactory getX509CertFactory() throws CertificateException {
        return CertificateFactory.getInstance("X.509");
    }

    /** {@inheritDoc} 获取 Collection 类型 CertStore。 */
    @Override
    public CertStore getCertStore(CollectionCertStoreParameters certStoreParams) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {

        return CertStore.getInstance("Collection", certStoreParams);

    }

    /** {@inheritDoc} 获取 PKIX 证书路径构建器。 */
    @Override
    public CertPathBuilder getCertPathBuilder() throws NoSuchAlgorithmException {
        return CertPathBuilder.getInstance("PKIX");
    }

    /** {@inheritDoc} 获取签名实例（含 RSASSA-PSS 参数配置）。 */
    @Override
    public Signature getSignature(String sigAlgName) throws NoSuchAlgorithmException {
        String javaAlgorithm = JavaAlgorithm.getJavaAlgorithm(sigAlgName);

        switch (javaAlgorithm) {
            case JavaAlgorithm.PS256, JavaAlgorithm.PS384, JavaAlgorithm.PS512:
                var signature = Signature.getInstance("RSASSA-PSS");

                int digestLength = Integer.parseInt(javaAlgorithm.substring(3, 6));
                MGF1ParameterSpec ps = new MGF1ParameterSpec("SHA-" + digestLength);
                AlgorithmParameterSpec params = new PSSParameterSpec(
                        ps.getDigestAlgorithm(), "MGF1", ps, digestLength / 8, 1);

                try {
                    signature.setParameter(params);
                } catch (InvalidAlgorithmParameterException e) {
                    throw new RuntimeException(e);
                }

                return signature;

            default:
                return Signature.getInstance(javaAlgorithm);
        }
    }

    /** {@inheritDoc} Elytron 模式下直接返回委托工厂，不做额外包装。 */
    @Override
    public SSLSocketFactory wrapFactoryForTruststore(SSLSocketFactory delegate) {
        return delegate;
    }
}
