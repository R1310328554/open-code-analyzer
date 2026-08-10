package org.keycloak.admin.ui.rest;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProvider;
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProviderFactory;
import org.keycloak.services.resources.admin.fgap.AdminPermissionEvaluator;

/**
 * Admin Console v2 扩展 REST 提供者工厂与实现（ID {@code ui-ext}），在启用 {@link Profile.Feature#ADMIN_V2} 时注册子资源。
 */
public final class AdminExtProvider implements AdminRealmResourceProviderFactory, AdminRealmResourceProvider, EnvironmentDependentProviderFactory {
    /** {@inheritDoc} 本工厂同时充当 Provider 实例。 */
    public AdminRealmResourceProvider create(KeycloakSession session) {
        return this;
    }

    public void init(Config.Scope config) {
    }

    public void postInit(KeycloakSessionFactory factory) {
    }

    public void close() {
    }

    public String getId() {
        return "ui-ext";
    }

    /** 为指定领域创建 {@link AdminExtResource} 根资源。 */
    public Object getResource(KeycloakSession session, RealmModel realm, AdminPermissionEvaluator auth, AdminEventBuilder adminEvent) {
        return new AdminExtResource(session, realm, auth, adminEvent);
    }

    /** 仅在 Admin Console v2 特性开启时加载。 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.ADMIN_V2);
    }
}
