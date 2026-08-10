package org.keycloak.testframework.https;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;

import org.keycloak.common.crypto.CryptoIntegration;
import org.keycloak.common.crypto.CryptoProvider;
import org.keycloak.common.util.KeystoreUtil;
import org.keycloak.crypto.def.DefaultCryptoProvider;

import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.ssl.SSLContextBuilder;

/**
 * 为 Keycloak 服务器与测试客户端提供托管 TLS/mTLS 证书及密钥库/信任库的工具类。
 * <p>
 * 可自动生成临时测试证书，或从类路径加载手动配置的 store，并构建客户端 {@link SSLContext}。
 */
public class ManagedCertificates {

    /** 自动生成密钥库的临时目录。 */
    private final static Path KEYSTORES_DIR = Path.of(System.getProperty("java.io.tmpdir"));

    /** 测试用密钥库/信任库统一密码。 */
    private static final String STORE_PASSWORD = "mysuperstrongstorepassword";
    /** {@link #STORE_PASSWORD} 的字符数组形式。 */
    private static final char[] STORE_PASSWORD_CHARS = STORE_PASSWORD.toCharArray();

    /** 是否启用 TLS。 */
    private final boolean tlsEnabled;
    /** 是否启用 mTLS。 */
    private final boolean mTlsEnabled;
    /** 密钥库文件格式。 */
    private final KeystoreUtil.KeystoreFormat keystoreFormat;

    /** 用于生成与加载密钥库的加密提供者。 */
    private final CryptoProvider cryptoProvider;

    /** 服务端密钥库磁盘路径。 */
    private final Path serverKeystorePath;
    /** 服务端信任库磁盘路径。 */
    private final Path serverTruststorePath;

    /** 客户端密钥库磁盘路径。 */
    private final Path clientKeystorePath;
    /** 已加载的客户端密钥库。 */
    private KeyStore clientKeyStore;

    /** 客户端信任库磁盘路径。 */
    private final Path clientTruststorePath;
    /** 已加载的客户端信任库。 */
    private KeyStore clientTrustStore;

    /** TLS 启用时预构建的客户端 SSL 上下文。 */
    private final SSLContext clientSslContext;

    /**
     * 按构建器配置生成或加载测试证书与各 store。
     *
     * @param configBuilder 证书配置构建器
     * @throws ManagedCertificatesException 证书生成或加载失败时抛出
     */
    public ManagedCertificates(CertificatesConfigBuilder configBuilder) throws ManagedCertificatesException {
        if (!CryptoIntegration.isInitialised()) {
            CryptoIntegration.setProvider(new DefaultCryptoProvider());
        }
        cryptoProvider = CryptoIntegration.getProvider();

        keystoreFormat = configBuilder.getKeystoreFormat();
        tlsEnabled = configBuilder.isTlsEnabled();
        mTlsEnabled = configBuilder.isMTlsEnabled();

        if (configBuilder.getServerKeystore() == null) {
            serverKeystorePath = resolvePath("kc-testing-server-keystore");
            serverTruststorePath = resolvePath("kc-testing-server-truststore");
            clientKeystorePath = resolvePath("kc-testing-client-keystore");
            clientTruststorePath = resolvePath("kc-testing-client-truststore");

            if (!Files.exists(serverKeystorePath) || !Files.exists(serverTruststorePath) || !Files.exists(clientKeystorePath) || !Files.exists(clientTruststorePath)) {
                createStores();
            } else {
                clientKeyStore = load(clientKeystorePath);
                clientTrustStore = load(clientTruststorePath);
            }
        } else {
            serverKeystorePath = checkPath(configBuilder.getServerKeystore());
            serverTruststorePath = checkPath(configBuilder.getServerTruststore());
            clientKeystorePath = checkPath(configBuilder.getClientKeystore());
            clientTruststorePath = checkPath(configBuilder.getClientTruststore());

            clientKeyStore = load(clientKeystorePath);
            clientTrustStore = load(clientTruststorePath);
        }

        clientSslContext = tlsEnabled ? createClientSSLContext() : null;
    }

    /**
     * 返回 Keycloak 服务端密钥库路径（含服务端私钥证书）。
     * <p>
     * TLS 未启用时返回 {@code null}。
     *
     * @return 密钥库文件路径
     */
    public String getServerKeyStorePath() {
        return tlsEnabled ? serverKeystorePath.toString() : null;
    }

    /**
     * 返回服务端密钥库密码。
     * <p>
     * TLS 未启用时返回 {@code null}。
     *
     * @return 密钥库密码
     */
    public String getServerKeyStorePassword() {
        return tlsEnabled ? STORE_PASSWORD : null;
    }

    /**
     * 返回服务端信任库路径（含客户端公钥证书，用于 mTLS）。
     * <p>
     * mTLS 未启用时返回 {@code null}。
     *
     * @return 信任库文件路径
     */
    public String getServerTrustStorePath() {
        return mTlsEnabled ? serverTruststorePath.toString() : null;
    }

    /**
     * 返回服务端信任库密码。
     * <p>
     * mTLS 未启用时返回 {@code null}。
     *
     * @return 信任库密码
     */
    public String getServerTrustStorePassword() {
        return mTlsEnabled ? STORE_PASSWORD : null;
    }

    /**
     * 返回已配置客户端信任库（及可选 mTLS 密钥）的 {@link SSLContext}。
     *
     * @return 客户端 SSL 上下文，TLS 未启用时为 {@code null}
     */
    public SSLContext getClientSSLContext() {
        return clientSslContext;
    }

    /**
     * 使用类路径上的客户端密钥库与信任库文件创建 {@link SSLContext}。
     *
     * @param keystore 类路径上的密钥库路径
     * @param truststore 类路径上的信任库路径
     * @param mTlsEnabled 是否启用双向 TLS
     * @return 配置完成的 SSL 上下文
     */
    public SSLContext createClientSSLContext(String keystore, String truststore, boolean mTlsEnabled) {
        Path truststorePath = checkPath(truststore);
        KeyStore truststoreStore = load(truststorePath);
        if (mTlsEnabled) {
            Path keystorePath = mTlsEnabled ? checkPath(keystore) : null;
            KeyStore keystoreStore = load(keystorePath);
            return createClientSSLContext(keystoreStore, truststoreStore, mTlsEnabled);
        } else {
            return createClientSSLContext(null, truststoreStore, false);
        }
    }

    /**
     * 返回是否启用了 TLS。
     *
     * @return 启用时为 <code>true</code>
     */
    public boolean isTlsEnabled()  {
        return tlsEnabled;
    }

    /**
     * 返回是否启用了 mTLS。
     *
     * @return 启用时为 <code>true</code>
     */
    public boolean isMTlsEnabled()  {
        return mTlsEnabled;
    }

    /** 返回密钥库格式。 */
    public KeystoreUtil.KeystoreFormat getKeystoreFormat() {
        return keystoreFormat;
    }

    /**
     * 校验类路径上的密钥库/信任库文件路径并解析为 {@link Path}。
     *
     * @param path 类路径资源路径
     * @return 文件系统路径
     * @throws IllegalArgumentException 路径为空或资源不存在时抛出
     */
    static Path checkPath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("The path cannot be null");
        }

        URL url = CertificatesConfigBuilder.class.getClassLoader().getResource(path);
        if (url == null || !url.getProtocol().equalsIgnoreCase("file")) {
            throw new IllegalArgumentException("Keystore not found in classpath: " + path);
        }

        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Error loading keystore:" + path, e);
        }
    }

    /** 使用实例内已加载的客户端 store 创建 SSL 上下文。 */
    private SSLContext createClientSSLContext() {
        return createClientSSLContext(clientKeyStore, clientTrustStore, mTlsEnabled);
    }

    /**
     * 根据给定密钥库与信任库构建客户端 SSL 上下文。
     *
     * @param keystore 客户端密钥库（mTLS 时使用）
     * @param truststore 客户端信任库
     * @param mTlsEnabled 是否加载客户端密钥材料
     * @return 构建完成的 {@link SSLContext}
     */
    private SSLContext createClientSSLContext(KeyStore keystore, KeyStore truststore, boolean mTlsEnabled) {
        try {
            SSLContextBuilder sslContextBuilder = SSLContextBuilder.create()
                    .loadTrustMaterial(truststore, TrustAllStrategy.INSTANCE);

            if (mTlsEnabled) {
                sslContextBuilder.loadKeyMaterial(keystore, STORE_PASSWORD_CHARS);
            }

            return sslContextBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 生成服务端/客户端测试密钥对并写入临时密钥库与信任库文件。 */
    private void createStores() {
        try {
            KeyPair serverKeyPair = generateKeyPair();
            X509Certificate serverCertificate = generateX509CertificateCertificate(serverKeyPair, "localhost");

            KeyPair clientKeyPair = generateKeyPair();
            X509Certificate clientCertificate = generateX509CertificateCertificate(clientKeyPair, "myclient");

            KeyStore serverKeyStore = cryptoProvider.getKeyStore(keystoreFormat);
            serverKeyStore.load(null, STORE_PASSWORD_CHARS);
            serverKeyStore.setKeyEntry("server-key", serverKeyPair.getPrivate(), STORE_PASSWORD_CHARS, new X509Certificate[] { serverCertificate });
            save(serverKeyStore, serverKeystorePath);

            KeyStore serverTrustStore = cryptoProvider.getKeyStore(keystoreFormat);
            serverTrustStore.load(null, STORE_PASSWORD_CHARS);
            serverTrustStore.setCertificateEntry("myclient-certificate", clientCertificate);
            save(serverTrustStore, serverTruststorePath);

            clientKeyStore = cryptoProvider.getKeyStore(keystoreFormat);
            clientKeyStore.load(null, STORE_PASSWORD_CHARS);
            clientKeyStore.setKeyEntry("client-key", clientKeyPair.getPrivate(), STORE_PASSWORD_CHARS, new X509Certificate[] { clientCertificate });
            save(clientKeyStore, clientKeystorePath);

            clientTrustStore = cryptoProvider.getKeyStore(keystoreFormat);
            clientTrustStore.load(null, STORE_PASSWORD_CHARS);
            clientTrustStore.setCertificateEntry("server-certificate", serverCertificate);
            save(clientTrustStore, clientTruststorePath);
        } catch (Exception e) {
            throw new ManagedCertificatesException(e);
        }
    }

    /** 在临时目录下解析带格式后缀的 store 文件路径。 */
    private Path resolvePath(String fileName) {
        return KEYSTORES_DIR.resolve(fileName + "." + keystoreFormat.getPrimaryExtension());
    }

    /** 将密钥库持久化到指定路径。 */
    private void save(KeyStore store, Path storePath) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
        try (FileOutputStream fos = new FileOutputStream(storePath.toFile())) {
            store.store(fos, STORE_PASSWORD_CHARS);
        }
    }

    /** 从磁盘加载密钥库或信任库。 */
    private KeyStore load(Path keyStorePath) {
        try (FileInputStream fis = new FileInputStream(keyStorePath.toFile())) {
            KeyStore keyStore = cryptoProvider.getKeyStore(keystoreFormat);
            keyStore.load(fis, STORE_PASSWORD_CHARS);
            return keyStore;
        } catch (Exception e) {
            throw new ManagedCertificatesException(e);
        }
    }

    /** 生成 RSA 密钥对。 */
    private KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        return cryptoProvider.getKeyPairGen("RSA").generateKeyPair();
    }

    /**
     * 为给定主体生成自签名根证书及 V3 证书链。
     *
     * @param keyPair 密钥对
     * @param subject 证书主体 DN
     * @return X509 证书
     */
    private X509Certificate generateX509CertificateCertificate(KeyPair keyPair, String subject) throws Exception {
        // 生成 V1 自签名根 CA 证书
        X509Certificate caCert = cryptoProvider.getCertificateUtils().generateV1SelfSignedCertificate(keyPair, subject);

        // 基于根证书生成 V3 证书链
        return cryptoProvider.getCertificateUtils().generateV3Certificate(keyPair, keyPair.getPrivate(), caCert, subject);
    }
}
