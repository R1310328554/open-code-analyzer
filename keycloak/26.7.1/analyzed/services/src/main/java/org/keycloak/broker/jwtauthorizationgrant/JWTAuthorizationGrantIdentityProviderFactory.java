package org.keycloak.broker.jwtauthorizationgrant;

import java.util.Map;

import org.keycloak.Config;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.common.Profile;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.EnvironmentDependentProviderFactory;

/**
 * JWT 授权授予身份提供者 SPI 工厂，Provider ID 为 {@code jwt-authorization-grant}。
 */
public class JWTAuthorizationGrantIdentityProviderFactory extends AbstractIdentityProviderFactory<JWTAuthorizationGrantIdentityProvider> implements EnvironmentDependentProviderFactory {

    /** Provider ID 常量。 */
    public static final String PROVIDER_ID = "jwt-authorization-grant";

    @Override
    /** @return 管理控制台显示名称 */
    public String getName() {
        return "JWT Authorization Grant";
    }

    @Override
    /** 创建 JWT 授权授予身份提供者实例。 */
    public JWTAuthorizationGrantIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        return new JWTAuthorizationGrantIdentityProvider(session, new JWTAuthorizationGrantIdentityProviderConfig(model));
    }

    @Override
    /** 不支持从字符串解析配置。 */
    public Map<String, String> parseConfig(KeycloakSession session, String configString) {
        throw new UnsupportedOperationException();
    }

    @Override
    /** @return 新的空配置模型 */
    public IdentityProviderModel createConfig() {
        return new JWTAuthorizationGrantIdentityProviderConfig();
    }

    @Override
    /** @return {@link #PROVIDER_ID} */
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    /** 需启用 {@link Profile.Feature#JWT_AUTHORIZATION_GRANT} 特性。 */
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.JWT_AUTHORIZATION_GRANT);
    }

}
