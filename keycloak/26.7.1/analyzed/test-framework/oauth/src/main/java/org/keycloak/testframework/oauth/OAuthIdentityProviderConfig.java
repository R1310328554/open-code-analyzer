package org.keycloak.testframework.oauth;

/**
 * OAuth 身份提供者测试配置的函数式接口。
 * <p>
 * 实现类通过 {@link OAuthIdentityProviderConfigBuilder} 定制模拟 IdP 的运行模式与 JWK 行为。
 */
public interface OAuthIdentityProviderConfig {

    /**
     * 在构建器上应用本配置。
     *
     * @param config 身份提供者配置构建器
     * @return 已应用配置的同一构建器，便于链式调用
     */
    OAuthIdentityProviderConfigBuilder configure(OAuthIdentityProviderConfigBuilder config);

}
