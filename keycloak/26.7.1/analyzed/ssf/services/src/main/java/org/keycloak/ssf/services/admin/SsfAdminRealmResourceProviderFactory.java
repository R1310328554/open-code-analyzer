package org.keycloak.ssf.services.admin;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProvider;
import org.keycloak.services.resources.admin.ext.AdminRealmResourceProviderFactory;
import org.keycloak.ssf.Ssf;

/**
 * 暴露 {@link SsfAdminResource}，挂载于管理 API 的 {@code /admin/realms/{realm}/ssf} 路径下。
 */
public class SsfAdminRealmResourceProviderFactory implements AdminRealmResourceProviderFactory, EnvironmentDependentProviderFactory {

    /**
     * SSF 管理端点位于 {@code $KC_ADMIN_URL/admin/realms/{realm}/ssf}。
     *
     * @return 提供者标识符
     */
    @Override
    public String getId() {
        return "ssf";
    }

    @Override
    public AdminRealmResourceProvider create(KeycloakSession session) {

        if (!Ssf.isTransmitterEnabled(session.getContext().getRealm())) {
            return null;
        }

        return new SsfAdminRealmResourceProvider();
    }

    @Override
    public void init(Config.Scope config) {
        // NOOP
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // NOOP
    }

    @Override
    public void close() {
        // NOOP
    }

    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.SSF);
    }
}
