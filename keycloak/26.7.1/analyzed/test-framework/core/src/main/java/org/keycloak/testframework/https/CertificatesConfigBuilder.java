package org.keycloak.testframework.https;


import org.keycloak.common.util.KeystoreUtil;

/**
 * 测试用 TLS/mTLS 证书与密钥库的流式配置构建器。
 * <p>
 * 可指定密钥库格式、是否启用 TLS/mTLS，或通过类路径文件手动指定各 store。
 */
public class CertificatesConfigBuilder {

    /** 密钥库文件格式，默认 JKS。 */
    private KeystoreUtil.KeystoreFormat keystoreFormat = KeystoreUtil.KeystoreFormat.JKS;
    /** 是否启用 TLS。 */
    private boolean tlsEnabled = false;
    /** 是否启用 Keycloak 与客户端之间的 mTLS。 */
    private boolean mTlsEnabled = false;
    /** 服务端密钥库类路径路径（手动配置时使用）。 */
    private String serverKeystore;
    /** 服务端信任库类路径路径。 */
    private String serverTruststore;
    /** 客户端密钥库类路径路径。 */
    private String clientKeystore;
    /** 客户端信任库类路径路径。 */
    private String clientTruststore;

    /** 创建默认配置构建器（JKS、TLS/mTLS 均关闭）。 */
    public CertificatesConfigBuilder() {
    }

    /**
     * 指定密钥库文件格式。
     *
     * @param keystoreFormat 要使用的密钥库格式
     * @return 当前构建器
     */
    public CertificatesConfigBuilder keystoreFormat(KeystoreUtil.KeystoreFormat keystoreFormat) {
        this.keystoreFormat = keystoreFormat;
        return this;
    }

    /** 返回当前密钥库格式。 */
    public KeystoreUtil.KeystoreFormat getKeystoreFormat() {
        return this.keystoreFormat;
    }

    /**
     * 启用或关闭 TLS。
     *
     * @param tlsEnabled 为 <code>true</code> 时启用 TLS
     * @return 当前构建器
     */
    public CertificatesConfigBuilder tlsEnabled(boolean tlsEnabled) {
        this.tlsEnabled = tlsEnabled;
        return this;
    }

    /** 返回是否启用 TLS（TLS 或 mTLS 任一开启即为 true）。 */
    public boolean isTlsEnabled() {
        return tlsEnabled || mTlsEnabled;
    }

    /**
     * 启用 Keycloak 与客户端之间的双向 TLS（mTLS）认证。
     *
     * @param mTlsEnabled 为 <code>true</code> 时启用 mTLS
     * @return 当前构建器
     */
    public CertificatesConfigBuilder mTlsEnabled(boolean mTlsEnabled) {
        this.mTlsEnabled = mTlsEnabled;
        return this;
    }

    /** 返回是否启用 mTLS。 */
    public boolean isMTlsEnabled() {
        return mTlsEnabled;
    }

    /** 返回手动配置的服务端密钥库类路径。 */
    public String getServerKeystore() {
        return serverKeystore;
    }

    /** 返回手动配置的服务端信任库类路径。 */
    public String getServerTruststore() {
        return serverTruststore;
    }

    /** 返回手动配置的客户端密钥库类路径。 */
    public String getClientKeystore() {
        return clientKeystore;
    }

    /** 返回手动配置的客户端信任库类路径。 */
    public String getClientTruststore() {
        return clientTruststore;
    }

    /**
     * 使用类路径上的文件手动配置各密钥库/信任库。
     *
     * @param serverKeystore 服务端密钥库类路径
     * @param serverTruststore 服务端信任库类路径
     * @param clientKeystore 客户端密钥库类路径
     * @param clientTruststore 客户端信任库类路径
     * @return 当前构建器
     */
    public CertificatesConfigBuilder stores(String serverKeystore, String serverTruststore, String clientKeystore, String clientTruststore) {
        this.serverKeystore = serverKeystore;
        this.serverTruststore = serverTruststore;
        this.clientKeystore = clientKeystore;
        this.clientTruststore = clientTruststore;
        return this;
    }
}
