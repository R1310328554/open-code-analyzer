package org.keycloak.testframework.https;

/**
 * {@link CertificatesConfig} 的默认空实现，不修改构建器中的任何选项。
 */
public class DefaultCertificatesConfig implements CertificatesConfig {

    /** {@inheritDoc} 原样返回传入的构建器。 */
    @Override
    public CertificatesConfigBuilder configure(CertificatesConfigBuilder config) {
        return config;
    }
}
