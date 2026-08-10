package org.keycloak.common.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Signature;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.spec.ECParameterSpec;
import java.util.stream.Stream;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.net.ssl.SSLSocketFactory;

import org.keycloak.common.util.KeystoreUtil.KeystoreFormat;

/**
 * 非 FIPS 与 FIPS 模式下 JCA API 差异的抽象层。
 *
 * <p>各 {@link CryptoProvider} 实现负责返回与当前 BouncyCastle 变体兼容的算法工厂、
 * 证书工具、PEM 解析器及 SSL 套接字工厂装饰器。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface CryptoProvider {

    /**
     * @return BouncyCastle 安全提供方实例（标准版或 FIPS 版）
     */
    Provider getBouncyCastleProvider();

    /**
     * 提供方优先级；classpath 上存在多个实现时，数值越大越优先。
     */
    int order();

    /**
     * 按算法名获取特定 SPI 实现（实现类随 FIPS/非 FIPS 环境变化）。
     *
     * @param clazz 期望返回的类型
     * @param algorithm JCA 算法名
     * @return 算法提供方实例
     */
    <T> T getAlgorithmProvider(Class<T> clazz, String algorithm);

    /**
     * @return 与当前环境匹配的 {@link CertificateUtilsProvider}
     */
    CertificateUtilsProvider getCertificateUtils();


    /**
     * @return 与当前环境匹配的 {@link PemUtilsProvider}
     */
    PemUtilsProvider getPemUtils();

    /** 获取 OCSP 相关提供方（若支持）。 */
    <T> T getOCSPProver(Class<T> clazz);


    /** @return 用户身份提取 SPI */
    public UserIdentityExtractorProvider getIdentityExtractorProvider();

    /** @return ECDSA 签名格式转换 SPI */
    public ECDSACryptoProvider getEcdsaCryptoProvider();


    /**
     * 创建指定椭圆曲线名的 {@link ECParameterSpec}。
     *
     * @param curveName 曲线标识（如 secp256r1）
     * @return EC 参数规格
     */
    ECParameterSpec createECParams(String curveName);

    KeyPairGenerator getKeyPairGen(String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException;

    KeyFactory getKeyFactory(String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException;

    Cipher getAesCbcCipher() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException;

    Cipher getAesGcmCipher() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException;

    SecretKeyFactory getSecretKeyFact(String keyAlgorithm) throws NoSuchAlgorithmException, NoSuchProviderException;

    KeyStore getKeyStore(KeystoreFormat format) throws KeyStoreException, NoSuchProviderException;

    /**
     * @return 当前 CryptoProvider 实际支持的密钥库类型/算法
     */
    default Stream<KeystoreFormat> getSupportedKeyStoreTypes() {
        return Stream.of(KeystoreFormat.values())
                .filter(format -> {
                    try {
                        getKeyStore(format);
                        return true;
                    } catch (KeyStoreException | NoSuchProviderException ex) {
                        return false;
                    }
                });
    }

    CertificateFactory getX509CertFactory() throws CertificateException, NoSuchProviderException;

    CertStore getCertStore(CollectionCertStoreParameters collectionCertStoreParameters) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException;

    CertPathBuilder getCertPathBuilder() throws NoSuchAlgorithmException, NoSuchProviderException;

    Signature getSignature(String sigAlgName) throws NoSuchAlgorithmException, NoSuchProviderException;

    /**
     * 包装 SSLSocketFactory，为信任库场景（Keycloak 作为 TLS 客户端）附加额外行为。
     *
     * @param delegate 原始工厂，通常为 JVM 默认实现
     * @return 装饰后的工厂
     */
    SSLSocketFactory wrapFactoryForTruststore(SSLSocketFactory delegate);

    /**
     * @return 本 CryptoProvider 支持的 RSA 模长（比特）列表
     */
    default String[] getSupportedRsaKeySizes() {
        return new String[] {"1024", "2048", "3072", "4096"};
    }
}
