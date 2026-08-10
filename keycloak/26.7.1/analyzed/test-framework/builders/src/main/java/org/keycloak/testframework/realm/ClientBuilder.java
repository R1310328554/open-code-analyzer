package org.keycloak.testframework.realm;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;

/** {@link ClientRepresentation} 的流式构建器，用于测试框架中组装 OAuth/OIDC 客户端配置。 */
public class ClientBuilder extends Builder<ClientRepresentation> {

    private ClientBuilder(ClientRepresentation rep) {
        super(rep);
    }

    /** 创建默认启用的空客户端构建器。 */
    public static ClientBuilder create() {
        return new ClientBuilder(new ClientRepresentation()).enabled(true);
    }

    /** 创建指定 clientId 且默认启用的客户端构建器。 */
    public static ClientBuilder create(String clientId) {
        return create().clientId(clientId);
    }

    /** 基于已有表示包装为构建器。 */
    public static ClientBuilder update(ClientRepresentation rep) {
        return new ClientBuilder(rep);
    }

    /** @param enabled 是否启用客户端 */
    public ClientBuilder enabled(Boolean enabled) {
        rep.setEnabled(enabled);
        return this;
    }

    /** @param clientId OAuth client_id */
    public ClientBuilder clientId(String clientId) {
        rep.setClientId(clientId);
        return this;
    }

    /** @param id 内部 UUID */
    public ClientBuilder id(String id) {
        rep.setId(id);
        return this;
    }

    /** @param secret 客户端密钥 */
    public ClientBuilder secret(String secret) {
        rep.setSecret(secret);
        return this;
    }

    /** @param name 显示名称 */
    public ClientBuilder name(String name) {
        rep.setName(name);
        return this;
    }

    /** @param description 描述 */
    public ClientBuilder description(String description) {
        rep.setDescription(description);
        return this;
    }

    /** @param type 客户端类型 */
    public ClientBuilder type(String type) {
        rep.setType(type);
        return this;
    }

    /** 设为公开客户端（无 client secret）。 */
    public ClientBuilder publicClient() {
        return publicClient(true);
    }

    /** @param publicClient 是否为公开客户端 */
    public ClientBuilder publicClient(Boolean publicClient) {
        rep.setPublicClient(publicClient);
        return this;
    }

    /** 追加重定向 URI。 */
    public ClientBuilder redirectUris(String... redirectUris) {
        rep.setRedirectUris(combine(rep.getRedirectUris(), redirectUris));
        return this;
    }

    /** @param adminUrl 管理 URL */
    public ClientBuilder adminUrl(String adminUrl) {
        rep.setAdminUrl(adminUrl);
        return this;
    }

    /** @param rootUrl 根 URL */
    public ClientBuilder rootUrl(String rootUrl) {
        rep.setRootUrl(rootUrl);
        return this;
    }

    /** @param baseUrl 基础 URL */
    public ClientBuilder baseUrl(String baseUrl) {
        rep.setBaseUrl(baseUrl);
        return this;
    }

    /** @param protocol 协议（如 openid-connect） */
    public ClientBuilder protocol(String protocol) {
        rep.setProtocol(protocol);
        return this;
    }

    /** @param bearerOnly 是否仅 Bearer 令牌访问 */
    public ClientBuilder bearerOnly(Boolean bearerOnly) {
        rep.setBearerOnly(bearerOnly);
        return this;
    }

    /** 启用服务账户。 */
    public ClientBuilder serviceAccountsEnabled() {
        return serviceAccountsEnabled(true);
    }

    /** @param enabled 是否启用服务账户 */
    public ClientBuilder serviceAccountsEnabled(Boolean enabled) {
        rep.setServiceAccountsEnabled(enabled);
        return this;
    }

    /** 启用 Direct Access Grants（资源所有者密码凭据）。 */
    public ClientBuilder directAccessGrantsEnabled() {
        return directAccessGrantsEnabled(true);
    }

    /** @param enabled 是否启用 Direct Access Grants */
    public ClientBuilder directAccessGrantsEnabled(Boolean enabled) {
        rep.setDirectAccessGrantsEnabled(enabled);
        return this;
    }

    /** @param enabled 是否启用授权服务（同时启用服务账户） */
    public ClientBuilder authorizationServicesEnabled(Boolean enabled) {
        serviceAccountsEnabled(enabled);
        rep.setAuthorizationServicesEnabled(enabled);
        return this;
    }

    /** @param enabled 是否允许 Full Scope */
    public ClientBuilder fullScopeEnabled(Boolean enabled) {
        rep.setFullScopeAllowed(enabled);
        return this;
    }

    /** @param enabled 是否启用前端通道登出 */
    public ClientBuilder frontchannelLogout(Boolean enabled) {
        rep.setFrontchannelLogout(enabled);
        return this;
    }

    /** @param authenticatorType 客户端认证器类型 */
    public ClientBuilder authenticatorType(String authenticatorType) {
        rep.setClientAuthenticatorType(authenticatorType);
        return this;
    }

    /** 设置单个客户端属性。 */
    public ClientBuilder attribute(String key, String value) {
        rep.setAttributes(Builder.createIfNull(rep.getAttributes(), HashMap::new));
        rep.getAttributes().put(key, value);
        return this;
    }

    /** 批量合并客户端属性。 */
    public ClientBuilder attributes(Map<String, String> attributes) {
        rep.setAttributes(combineMap(rep.getAttributes(), attributes));
        return this;
    }

    /** 移除指定属性键。 */
    public ClientBuilder removeAttributes(String... keys) {
        rep.setAttributes(removeKeys(rep.getAttributes(), keys));
        return this;
    }

    /** 追加默认客户端 scope。 */
    public ClientBuilder defaultClientScopes(String... defaultClientScopes) {
        rep.setDefaultClientScopes(combine(rep.getDefaultClientScopes(), defaultClientScopes));
        return this;
    }

    /** 追加可选客户端 scope。 */
    public ClientBuilder optionalClientScopes(String... optionalClientScopes) {
        rep.setOptionalClientScopes(combine(rep.getOptionalClientScopes(), optionalClientScopes));
        return this;
    }

    /** 追加协议映射器。 */
    public ClientBuilder protocolMappers(ProtocolMapperRepresentation... mappers) {
        rep.setProtocolMappers(combine(rep.getProtocolMappers(), mappers));
        return this;
    }

    /** @deprecated 使用 realm 角色分配替代 */
    @Deprecated
    public ClientBuilder defaultRoles(String... roles) {
        rep.setDefaultRoles(combine(rep.getDefaultRoles(), roles));
        return this;
    }

    /** @param enabled 是否要求用户同意 */
    public ClientBuilder consentRequired(Boolean enabled) {
        rep.setConsentRequired(enabled);
        return this;
    }

    /** 追加 Web Origins（CORS）。 */
    public ClientBuilder webOrigins(String... webOrigins) {
        rep.setWebOrigins(combine(rep.getWebOrigins(), webOrigins));
        return this;
    }

    /** @param alwaysDisplayInConsole 是否在控制台始终显示 */
    public ClientBuilder alwaysDisplayInConsole(Boolean alwaysDisplayInConsole) {
        rep.setAlwaysDisplayInConsole(alwaysDisplayInConsole);
        return this;
    }

}
