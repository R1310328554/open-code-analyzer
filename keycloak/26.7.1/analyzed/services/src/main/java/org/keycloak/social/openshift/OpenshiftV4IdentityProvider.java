package org.keycloak.social.openshift;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.mappers.AbstractJsonUserAttributeMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.http.simple.SimpleHttp;
import org.keycloak.models.KeycloakSession;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

/**
 * OpenShift v4 OAuth2 社交身份提供者。
 * <p>通过集群 OAuth 元数据发现端点动态配置授权/令牌 URL，
 * 并使用 OpenShift User API 获取联邦用户身份。</p>
 *
 * @author David Festal and Sebastian Łaskawiec
 */
public class OpenshiftV4IdentityProvider extends AbstractOAuth2IdentityProvider<OpenshiftV4IdentityProviderConfig> implements SocialIdentityProvider<OpenshiftV4IdentityProviderConfig> {

    /** 默认 OpenShift API 基址（预览集群）。 */
    public static final String BASE_URL = "https://api.preview.openshift.com";
    /** OAuth 授权服务器元数据发现路径。 */
    public static final String OPENSHIFT_OAUTH_METADATA_ENDPOINT = "/.well-known/oauth-authorization-server";
    /** 当前用户资源路径（{@code ~} 表示调用者自身）。 */
    public static final String PROFILE_RESOURCE = "/apis/user.openshift.io/v1/users/~";
    /** 默认 OAuth scope。 */
    public static final String DEFAULT_SCOPE = "user:info";
    /** 集群管理员内置用户名。 */
    private static final String KUBEADM_NAME = "kube:admin";

    /**
     * 构造 OpenShift v4 IdP。
     * <p>从 OAuth 元数据端点读取 authorization_endpoint 与 token_endpoint，
     * 并设置 UserInfo URL 为集群 User API。</p>
     */
    public OpenshiftV4IdentityProvider(KeycloakSession session, OpenshiftV4IdentityProviderConfig config) {
        super(session, config);
        final String baseUrl = Optional.ofNullable(config.getBaseUrl()).orElse(BASE_URL);
        Map<String, Object> oauthDescriptor = getAuthJson(session, config.getBaseUrl());
        logger.debugv("Openshift v4 OAuth descriptor: {0}", oauthDescriptor);
        config.setAuthorizationUrl((String) oauthDescriptor.get("authorization_endpoint"));
        config.setTokenUrl((String) oauthDescriptor.get("token_endpoint"));
        config.setUserInfoUrl(baseUrl + PROFILE_RESOURCE);
    }

    /** 拉取并解析 OAuth 授权服务器元数据 JSON。 */
    Map<String, Object> getAuthJson(KeycloakSession session, String baseUrl) {
        try {
            InputStream response = getOauthMetadataInputStream(session, baseUrl);
            Map<String, Object> map = mapMetadata(response);
            return map;
        } catch (Exception e) {
            throw new IdentityBrokerException("Could not initialize oAuth metadata", e);
        }
    }

    /** HTTP GET 请求 OAuth 元数据端点并返回响应体流。 */
    InputStream getOauthMetadataInputStream(KeycloakSession session, String baseUrl) throws IOException {
        HttpClient httpClient = session.getProvider(HttpClientProvider.class).getHttpClient();
        HttpGet getRequest = new HttpGet(baseUrl + OPENSHIFT_OAUTH_METADATA_ENDPOINT);
        getRequest.addHeader("accept", "application/json");

        HttpResponse response = httpClient.execute(getRequest);

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
        }
        return response.getEntity().getContent();
    }

    /** 将元数据 JSON 输入流反序列化为 Map。 */
    Map mapMetadata(InputStream response) throws IOException {
        return new ObjectMapper().readValue(response, Map.class);
    }

    /** 返回默认 OAuth scope。 */
    @Override
    protected String getDefaultScopes() {
        return DEFAULT_SCOPE;
    }

    /** 使用 Bearer 令牌从 OpenShift User API 获取用户资料。 */
    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
        try {
            final JsonNode profile = fetchProfile(accessToken);
            final BrokeredIdentityContext user = extractUserContext(profile);
            AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, getConfig().getAlias());
            return user;
        } catch (Exception e) {
            throw new IdentityBrokerException("Could not obtain user profile from Openshift.", e);
        }
    }

    /**
     * 从 User API JSON 提取联邦身份上下文。
     * <p>优先使用 metadata.uid；对 {@code kube:admin} 用户可回退到 name 作为 ID。</p>
     */
    private BrokeredIdentityContext extractUserContext(JsonNode profile) {
        JsonNode metadata = profile.get("metadata");
        logger.debugv("extractUserContext: metadata = {0}", metadata);
        final BrokeredIdentityContext user = new BrokeredIdentityContext(
                getJsonProperty(metadata, "uid") != null
                        ? getJsonProperty(metadata, "uid")
                        : tryGetKubeAdmin(metadata)
        , getConfig());
        user.setUsername(getJsonProperty(metadata, "name"));
        user.setName(getJsonProperty(profile, "fullName"));
        user.setIdp(this);
        return user;
    }

    /** 对 kube:admin 用户返回 name 作为联邦 ID，否则返回 null。 */
    private String tryGetKubeAdmin(JsonNode metadata) {
        String nameProperty = getJsonProperty(metadata, "name");
        if(!KUBEADM_NAME.equals(nameProperty)){
            return null;
        }
        return nameProperty;
    }

    /** 携带 Bearer 访问令牌请求 User API。 */
    private JsonNode fetchProfile(String accessToken) throws IOException {
        return SimpleHttp.create(session).doGet(getConfig().getUserInfoUrl())
                .header("Authorization", "Bearer " + accessToken)
                .asJson();
    }

    /** 支持外部令牌交换。 */
    @Override
    protected boolean supportsExternalExchange() {
        return true;
    }

    /** 返回用于令牌校验的 User API URL。 */
    @Override
    protected String getProfileEndpointForValidation(EventBuilder event) {
        return getConfig().getUserInfoUrl();
    }

    /** 从已有 JSON 资料构建联邦身份（用于外部交换等场景）。 */
    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode profile) {
        final BrokeredIdentityContext user = extractUserContext(profile);
        AbstractJsonUserAttributeMapper.storeUserProfileForMapper(user, profile, getConfig().getAlias());
        return user;
    }

}
