package org.keycloak.testframework.https;

/**
 * 托管测试证书的声明式配置接口。
 * <p>
 * 实现类通过 {@link #configure(CertificatesConfigBuilder)} 向构建器追加或覆盖 TLS/mTLS 与密钥库选项。
 */
public interface CertificatesConfig {

    /**
     * 将本配置应用到 {@link CertificatesConfigBuilder}。
     *
     * @param config 证书配置构建器
     * @return 配置后的构建器
     */
    CertificatesConfigBuilder configure(CertificatesConfigBuilder config);
}
