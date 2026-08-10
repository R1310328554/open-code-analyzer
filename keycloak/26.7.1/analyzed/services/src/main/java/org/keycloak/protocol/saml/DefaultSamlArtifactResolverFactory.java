package org.keycloak.protocol.saml;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * 默认 SAML Artifact 解析器工厂。
 * <p>在 {@code postInit} 中创建单例 {@link DefaultSamlArtifactResolver}，标识为 {@code default}。</p>
 */
public class DefaultSamlArtifactResolverFactory implements ArtifactResolverFactory {
    
    /** SAML 2.0 Artifact TypeCode（0x0004） */
    public static final byte[] TYPE_CODE = {0, 4};

    /** 共享的 artifact 解析器实例 */
    private DefaultSamlArtifactResolver artifactResolver;

    /** @param session Keycloak 会话 @return 单例 {@link DefaultSamlArtifactResolver} */
    @Override
    public DefaultSamlArtifactResolver create(KeycloakSession session) {
        return artifactResolver;
    }

    @Override
    public void init(Config.Scope config) {
        // 无需初始化
    }

    /** 工厂初始化后创建解析器单例 @param factory 会话工厂 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        artifactResolver = new DefaultSamlArtifactResolver();
    }

    @Override
    public void close() {
        // 无需关闭
    }

    /** @return 工厂标识 {@code default} */
    @Override
    public String getId() {
        return "default";
    }

}
