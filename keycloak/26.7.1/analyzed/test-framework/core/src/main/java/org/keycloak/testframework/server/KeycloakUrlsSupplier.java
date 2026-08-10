package org.keycloak.testframework.server;

import java.util.List;

import org.keycloak.testframework.annotations.InjectKeycloakUrls;
import org.keycloak.testframework.injection.DependenciesBuilder;
import org.keycloak.testframework.injection.Dependency;
import org.keycloak.testframework.injection.InstanceContext;
import org.keycloak.testframework.injection.RequestedInstance;
import org.keycloak.testframework.injection.Supplier;

/**
 * 为 {@link InjectKeycloakUrls} 注入 {@link KeycloakUrls} 实例的 {@link Supplier}。
 * <p>
 * 从已启动的 {@link KeycloakServer} 读取应用与管理基址。
 */
public class KeycloakUrlsSupplier implements Supplier<KeycloakUrls, InjectKeycloakUrls> {

    /** {@inheritDoc} — 依赖托管 {@link KeycloakServer} 实例。 */
    @Override
    public List<Dependency> getDependencies(RequestedInstance<KeycloakUrls, InjectKeycloakUrls> instanceContext) {
        return DependenciesBuilder.create(KeycloakServer.class).build();
    }

    /** {@inheritDoc} — 基于服务器基址构造 URL 助手。 */
    @Override
    public KeycloakUrls getValue(InstanceContext<KeycloakUrls, InjectKeycloakUrls> instanceContext) {
        KeycloakServer server = instanceContext.getDependency(KeycloakServer.class);
        return new KeycloakUrls(server.getBaseUrl(), server.getManagementBaseUrl());
    }

    /** {@inheritDoc} — 任意 {@link KeycloakUrls} 请求均兼容。 */
    @Override
    public boolean compatible(InstanceContext<KeycloakUrls, InjectKeycloakUrls> a, RequestedInstance<KeycloakUrls, InjectKeycloakUrls> b) {
        return true;
    }
}
