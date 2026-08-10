package org.keycloak.testframework.admin;

import java.util.List;
import javax.net.ssl.SSLContext;

import org.keycloak.testframework.annotations.InjectAdminClientFactory;
import org.keycloak.testframework.https.ManagedCertificates;
import org.keycloak.testframework.injection.DependenciesBuilder;
import org.keycloak.testframework.injection.Dependency;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;
import org.keycloak.testframework.server.KeycloakServer;

/**
 * 注入 {@link AdminClientFactory}：根据 {@link KeycloakServer} 基址与 {@link ManagedCertificates} TLS 设置构造工厂。
 */
public class AdminClientFactorySupplier implements Supplier<AdminClientFactory, InjectAdminClientFactory> {

    /** 依赖 KeycloakServer 与 ManagedCertificates。 */
    @Override
    public List<Dependency> getDependencies(RequestedInstance<AdminClientFactory, InjectAdminClientFactory> instanceContext) {
        return DependenciesBuilder.create(KeycloakServer.class).add(ManagedCertificates.class).build();
    }

    /** 按是否启用 TLS 选择 HTTP 或 HTTPS 工厂。 */
    @Override
    public AdminClientFactory getValue(InstanceContext<AdminClientFactory, InjectAdminClientFactory> instanceContext) {
        KeycloakServer server = instanceContext.getDependency(KeycloakServer.class);
        ManagedCertificates managedCert = instanceContext.getDependency(ManagedCertificates.class);

        if (!managedCert.isTlsEnabled()) {
            return new AdminClientFactory(server.getBaseUrl());
        } else {
            SSLContext sslContext = managedCert.getClientSSLContext();
            return new AdminClientFactory(server.getBaseUrl(), sslContext);
        }
    }

    /** 工厂实例始终兼容复用。 */
    @Override
    public boolean compatible(InstanceContext<AdminClientFactory, InjectAdminClientFactory> a, RequestedInstance<AdminClientFactory, InjectAdminClientFactory> b) {
        return true;
    }

    /** 关闭工厂内登记的所有 Admin 客户端。 */
    @Override
    public void close(InstanceContext<AdminClientFactory, InjectAdminClientFactory> instanceContext) {
        instanceContext.getValue().close();
    }

}
