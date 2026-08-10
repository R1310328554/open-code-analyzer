package org.keycloak.testframework.oauth;

/**
 * {@link OAuthIdentityProviderConfig} 的默认空实现。
 * <p>
 * 不对 {@link OAuthIdentityProviderConfigBuilder} 做任何修改，适用于标准模拟 IdP 场景。
 */
public class DefaultOAuthIdentityProviderConfig implements OAuthIdentityProviderConfig {
    /** {@inheritDoc} 原样返回构建器，不应用额外配置。 */
    @Override
    public OAuthIdentityProviderConfigBuilder configure(OAuthIdentityProviderConfigBuilder config) {
        return config;
    }
}
