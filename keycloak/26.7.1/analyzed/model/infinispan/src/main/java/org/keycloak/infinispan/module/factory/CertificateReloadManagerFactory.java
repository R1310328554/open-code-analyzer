package org.keycloak.infinispan.module.factory;

import org.keycloak.infinispan.module.configuration.global.KeycloakConfiguration;
import org.keycloak.jgroups.certificates.CertificateReloadManager;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.spi.infinispan.JGroupsCertificateProvider;

import org.infinispan.factories.AbstractComponentFactory;
import org.infinispan.factories.AutoInstantiableFactory;
import org.infinispan.factories.annotations.DefaultFactoryFor;

/**
 * {@link CertificateReloadManager} 的 Infinispan 组件工厂。
 * <p>
 * 从 {@link KeycloakConfiguration} 读取 {@link KeycloakSessionFactory}，并在 JGroups 证书提供者
 * 已启用且支持轮换/重载时创建管理器实例。
 */
@DefaultFactoryFor(classes = CertificateReloadManager.class)
public class CertificateReloadManagerFactory extends AbstractComponentFactory implements AutoInstantiableFactory {

    /** {@inheritDoc} 条件性构造证书重载管理器；不支持时返回 null。 */
    @Override
    public Object construct(String componentName) {
        var kcConfig = globalConfiguration.module(KeycloakConfiguration.class);
        if (kcConfig == null) {
            return null;
        }
        var sessionFactory = kcConfig.keycloakSessionFactory();
        if (supportsReloadAndRotation(sessionFactory)) {
            return new CertificateReloadManager(sessionFactory);
        }
        return null;
    }

    /** 探测 JGroups 证书 SPI 是否启用且支持 rotate/reload 操作。 */
    private boolean supportsReloadAndRotation(KeycloakSessionFactory factory) {
        try (var session = factory.create()) {
            var provider = session.getProvider(JGroupsCertificateProvider.class);
            return provider != null && provider.isEnabled() && provider.supportRotateAndReload();
        }
    }
}
