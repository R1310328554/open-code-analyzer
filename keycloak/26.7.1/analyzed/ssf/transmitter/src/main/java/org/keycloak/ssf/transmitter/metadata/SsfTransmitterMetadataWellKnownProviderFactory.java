package org.keycloak.ssf.transmitter.metadata;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.ssf.Ssf;
import org.keycloak.wellknown.WellKnownProvider;
import org.keycloak.wellknown.WellKnownProviderFactory;

/**
 * {@code SsfTransmitterMetadataWellKnownProvider} 的工厂实现。
 * 与 Keycloak Well-Known Provider 基础设施集成，仅在系统配置 Profile 启用 SSF 功能
 * 且当前 realm 启用了 SSF 发送方特性时可用。
 */
public class SsfTransmitterMetadataWellKnownProviderFactory implements WellKnownProviderFactory, EnvironmentDependentProviderFactory {

    /** Well-Known 端点的 Provider 标识符。 */
    public static final String PROVIDER_ID = "ssf-configuration";

    @Override
    public WellKnownProvider create(KeycloakSession session) {
        if (!isEnabledForRealm(session)) {
            return null;
        }
        return new SsfTransmitterMetadataWellKnownProvider(session);
    }

    /** 判断当前 realm 是否启用了 SSF 发送方。 */
    protected boolean isEnabledForRealm(KeycloakSession session) {
        return Ssf.isTransmitterEnabled(session.getContext().getRealm());
    }

    @Override
    public void init(Config.Scope config) {
        // NOOP
    }

    @Override
    public boolean isAvailableViaServerMetadata() {
        return true;
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // NOOP
    }

    @Override
    public void close() {
        // NOOP
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.SSF);
    }
}
