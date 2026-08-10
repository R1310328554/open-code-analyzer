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
 * 对旧版 SSE（Shared Signals and Events）协议的支持，SSF 协议的前身。
 *
 * 创建 {@code SseTransmitterMetadataWellKnownProvider} 实例的工厂实现。
 * 集成 Keycloak Well-Known 提供者基础设施，仅在系统配置配置文件启用 SSF 功能
 * 且当前 realm 启用 SSF 发送方功能时可用。
 */
public class SseTransmitterMetadataWellKnownProviderFactory implements WellKnownProviderFactory, EnvironmentDependentProviderFactory {

    public static final String PROVIDER_ID = "sse-configuration";

    /**
     * 禁用旧版 SSE well-known 端点的 SPI 属性。
     * 设置 {@code spi-wellknown--sse-configuration--enabled=false} 可完全注销本工厂——
     * 仅需 SSF 1.0 {@code /.well-known/ssf-configuration} 端点的部署可选择不暴露前身路径。
     * 默认为 {@code true}，以与仍依赖旧版发现文档的接收方保持向后兼容。
     */
    public static final String CONFIG_ENABLED = "enabled";

    @Override
    public WellKnownProvider create(KeycloakSession session) {
        if (!isEnabledForRealm(session)) {
            return null;
        }
        return new SseTransmitterMetadataWellKnownProvider(session);
    }

    protected boolean isEnabledForRealm(KeycloakSession session) {
        return Ssf.isTransmitterEnabled(session.getContext().getRealm());
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

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        if (!Profile.isFeatureEnabled(Profile.Feature.SSF)) {
            return false;
        }
        return config.getBoolean(CONFIG_ENABLED, false);
    }
}
