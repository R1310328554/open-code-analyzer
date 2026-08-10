package org.keycloak.testframework.admin;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

/**
 * {@link Keycloak} Admin REST 客户端的流式构建器，封装 {@link KeycloakBuilder} 并支持自动关闭注册。
 */
public class AdminClientBuilder {

    private final AdminClientFactory adminClientFactory;
    private final KeycloakBuilder delegate;
    private boolean close = false;

    /**
     * @param adminClientFactory 所属工厂，用于 autoClose 时登记实例
     * @param delegate 底层 KeycloakBuilder
     */
    public AdminClientBuilder(AdminClientFactory adminClientFactory, KeycloakBuilder delegate) {
        this.adminClientFactory = adminClientFactory;
        this.delegate = delegate;
    }

    /** @param realm 目标 realm 名称 */
    public AdminClientBuilder realm(String realm) {
        delegate.realm(realm);
        return this;
    }

    /** @param grantType OAuth2 授权类型 */
    public AdminClientBuilder grantType(String grantType) {
        delegate.grantType(grantType);
        return this;
    }

    /** @param username 资源所有者用户名（密码模式） */
    public AdminClientBuilder username(String username) {
        delegate.username(username);
        return this;
    }

    /** @param password 资源所有者密码 */
    public AdminClientBuilder password(String password) {
        delegate.password(password);
        return this;
    }

    /** @param clientId OAuth 客户端 ID */
    public AdminClientBuilder clientId(String clientId) {
        delegate.clientId(clientId);
        return this;
    }

    /** @param scope 请求的 OAuth scope */
    public AdminClientBuilder scope(String scope) {
        delegate.scope(scope);
        return this;
    }

    /** @param clientSecret 机密客户端密钥 */
    public AdminClientBuilder clientSecret(String clientSecret) {
        delegate.clientSecret(clientSecret);
        return this;
    }

    /** @param accessToken 已有 Bearer 访问令牌 */
    public AdminClientBuilder authorization(String accessToken) {
        delegate.authorization(accessToken);
        return this;
    }

    /** 构建后将客户端注册到工厂，测试结束时统一 close。 */
    public AdminClientBuilder autoClose() {
        this.close = true;
        return this;
    }

    /** 构建 Keycloak 客户端，若已 autoClose 则加入工厂关闭列表。 */
    public Keycloak build() {
        Keycloak keycloak = delegate.build();
        if (close) {
            adminClientFactory.addToClose(keycloak);
        }
        return keycloak;
    }
}
