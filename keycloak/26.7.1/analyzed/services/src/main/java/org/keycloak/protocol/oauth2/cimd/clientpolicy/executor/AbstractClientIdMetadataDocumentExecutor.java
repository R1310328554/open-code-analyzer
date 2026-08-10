package org.keycloak.protocol.oauth2.cimd.clientpolicy.executor;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.keycloak.OAuthErrorException;
import org.keycloak.common.Profile;
import org.keycloak.common.util.Time;
import org.keycloak.http.simple.SimpleHttp;
import org.keycloak.http.simple.SimpleHttpRequest;
import org.keycloak.http.simple.SimpleHttpResponse;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oauth2.cimd.provider.ClientIdMetadataDocumentProvider;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.utils.RedirectUtils;
import org.keycloak.representations.idm.ClientPolicyExecutorConfigurationRepresentation;
import org.keycloak.representations.oidc.OIDCClientRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.context.PreAuthorizationRequestContext;
import org.keycloak.services.clientpolicy.executor.ClientPolicyExecutorProvider;
import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.jboss.logging.Logger;

/**
 * OAuth 客户端标识元数据文档（CIMD）执行器抽象基类，实现 Internet-Draft v00 规范。
 * @see <a href="https://datatracker.ietf.org/doc/html/draft-ietf-oauth-client-id-metadata-document-00">OAuth Client ID Metadata Document (CIMD) [Internet Draft]</a>
 *
 * <p>同时实现 MCP（Model Context Protocol）2025-11-25 授权相关约束。</p>
 * @see <a href="https://modelcontextprotocol.io/specification/2025-11-25/basic/authorization">Model Context Protocol (MCP) [2025-11-25]</a>
 *
 * <p>满足 CIMD/MCP 中 MUST/SHOULD 级别要求及安全考量章节。</p>
 *
 * <p>主要能力：</p>
 * <ul>
 *     <li>Client ID 校验：验证 {@code client_id} 参数是否符合规范</li>
 *     <li>Client ID 策略验证：按策略判断 client_id 是否合法</li>
 *     <li>拉取客户端元数据：通过 client_id URL 获取元数据文档</li>
 *     <li>元数据校验与策略验证</li>
 *     <li>在 {@link OIDCClientRepresentation} 中增强元数据以便转换</li>
 * </ul>
 *
 * <p>元数据缓存委托给 {@link ClientIdMetadataDocumentProvider}；具体子类可扩展额外策略。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */public abstract class AbstractClientIdMetadataDocumentExecutor<CONFIG extends AbstractClientIdMetadataDocumentExecutor.Configuration> implements ClientPolicyExecutorProvider<CONFIG> {

    protected final KeycloakSession session;
    protected CONFIG configuration;
    protected ClientIdMetadataDocumentProvider<CONFIG> provider;

    // 工厂级全局配置
    protected ClientIdMetadataDocumentExecutorFactoryProviderConfig providerConfig;

    protected abstract Logger getLogger();

    protected AbstractClientIdMetadataDocumentExecutor(KeycloakSession session, ClientIdMetadataDocumentExecutorFactoryProviderConfig providerConfig) {
        this.session = session;
        this.providerConfig = providerConfig;
    }

    protected ClientIdMetadataDocumentProvider<CONFIG> getProvider() {
        return provider;
    }

    public CONFIG getConfiguration() {
        return configuration;
    }

    /** 执行器配置：HTTP scheme、受信域名、同域限制与必填属性等。 */
    public static class Configuration extends ClientPolicyExecutorConfigurationRepresentation {
        // Client ID 格式校验相关配置
        @JsonProperty(AbstractClientIdMetadataDocumentExecutorFactory.ALLOW_HTTP_SCHEME)
        protected boolean allowHttpScheme = false;
        @JsonProperty(AbstractClientIdMetadataDocumentExecutorFactory.TRUSTED_DOMAINS)
        protected List<String> trustedDomains = null;

        // 客户端元数据策略校验相关配置
        @JsonProperty(AbstractClientIdMetadataDocumentExecutorFactory.RESTRICT_SAME_DOMAIN)
        protected boolean restrictSameDomain = false;
        @JsonProperty(AbstractClientIdMetadataDocumentExecutorFactory.REQUIRED_PROPERTIES)
        protected List<String> requiredProperties = null;

        public Configuration() {
        }

        public boolean isAllowHttpScheme() {
            return allowHttpScheme;
        }

        public void setAllowHttpScheme(boolean allowHttpScheme) {
            this.allowHttpScheme = allowHttpScheme;
        }

        public List<String> getTrustedDomains() {
            return trustedDomains;
        }

        public void setTrustedDomains(List<String> permittedDomains) {
            this.trustedDomains = permittedDomains;
        }

        public boolean isRestrictSameDomain() {
            return restrictSameDomain;
        }

        public void setRestrictSameDomain(boolean restrictSameDomain) {
            this.restrictSameDomain = restrictSameDomain;
        }

        public List<String> getRequiredProperties() {
            return requiredProperties;
        }

        public void setRequiredProperties(List<String> requiredProperties) {
            this.requiredProperties = requiredProperties;
        }
    }

    /**
     * CIMD/MCP 要求授权服务器缓存客户端元数据；此类封装元数据与 Cache-Control 头值。
     */
    /** 客户端元数据与 Cache-Control 头的组合载体。 */
    public static class OIDCClientRepresentationWithCacheControl {
        private final OIDCClientRepresentation oidcClientRepresentation;
        private final ClientMetadataCacheControl clientMetadataCacheControl;

        public OIDCClientRepresentationWithCacheControl(OIDCClientRepresentation oidcClientRepresentation,
                                                        ClientMetadataCacheControl clientMetadataCacheControl) {
            this.oidcClientRepresentation = oidcClientRepresentation;
            this.clientMetadataCacheControl = clientMetadataCacheControl;
        }

        public OIDCClientRepresentation getOidcClientRepresentation() {
            return oidcClientRepresentation;
        }

        public ClientMetadataCacheControl getClientMetadataCacheControl() {
            return clientMetadataCacheControl;
        }
    }

    /**
     * 解析 HTTP Cache-Control 响应头，用于确定客户端元数据缓存生命周期。支持指令：
     * <ul>
     *     <li>only consider directives of a response.</li>
     *     <li>do not consider whether private or public.</li>
     *     <li>consider max-age and s-maxage directives showing the lifetime of client metadata.</li>
     *     <li>s-maxage takes precedence over max-age</li>
     *     <li>consider no-cache and no-store directives showing no caching client metadata.</li>
     *     <li>do not consider other directives.</li>
     * </ul>
     */
    /** 解析并规范化 Cache-Control 响应头，计算元数据缓存过期时间。 */
    public static class ClientMetadataCacheControl {
        private boolean noCache = false;
        private boolean noStore = false;
        private int maxAgeValue = -1;
        private int sMaxAgeValue = -1;
        private final String normalizedCacheControlHeaderValue;
        private final int minCacheTime;
        private final int maxCacheTime;

        public ClientMetadataCacheControl(String rawCacheControlHeaderValue, int minCacheTime, int maxCacheTime) {
            this.minCacheTime = minCacheTime;
            this.maxCacheTime = maxCacheTime;
            if (rawCacheControlHeaderValue == null) {
                normalizedCacheControlHeaderValue = null;
            } else {
                normalizedCacheControlHeaderValue = rawCacheControlHeaderValue.toLowerCase().replaceAll("\\s", "");
                String[] directives = normalizedCacheControlHeaderValue.split(",", 0);
                boolean isMaxAgeExist = false;
                boolean isSMaxAgeExist = false;
                String maxAgeRaw = null;
                String sMaxAgeRaw = null;
                for (String directive : directives) {
                    if ("no-cache".equals(directive)) {
                        noCache = true;
                    } else if ("no-store".equals(directive)) {
                        noStore = true;
                    } else if (directive.startsWith("max-age=")) {
                        isMaxAgeExist = true;
                        maxAgeRaw = directive;
                    } else if (directive.startsWith("s-maxage=")) {
                        isSMaxAgeExist = true;
                        sMaxAgeRaw = directive;
                    }
                }
                if (isMaxAgeExist && maxAgeRaw != null) {
                    maxAgeValue = parseExpiryValue(maxAgeRaw);
                }
                if (isSMaxAgeExist && sMaxAgeRaw != null) {
                    sMaxAgeValue = parseExpiryValue(sMaxAgeRaw);
                }
            }
        }

        public boolean isNoCache() {
            return noCache;
        }

        public boolean isNoStore() {
            return noStore;
        }

        public boolean isMaxAge() {
            return maxAgeValue >= 0;
        }

        public int getMaxAgeValue() {
            return maxAgeValue;
        }

        public boolean isSmaxAge() {
            return sMaxAgeValue >= 0;
        }

        public int getSmaxAgeValue() {
            return sMaxAgeValue;
        }

        public final String getNormalizedCacheControlHeaderValue() {
            return normalizedCacheControlHeaderValue;
        }

        public int getCacheExpiryTimeInSec() {
            if (isNoCache() || isNoStore()) {
                return minCacheTime > 0 ? Time.currentTime() + minCacheTime : Time.currentTime();
            }
            if (isSmaxAge()) { // s-maxage 优先于 max-age
                return Time.currentTime() + getSmaxAgeValue();
            }
            if (isMaxAge()) {
                return Time.currentTime() + getMaxAgeValue();
            }
            return minCacheTime > 0 ? Time.currentTime() + minCacheTime : Time.currentTime();
        }

        private int parseExpiryValue(String directive) {
            String[] parts = directive.split("=", 2);
            try {
                if (parts.length == 2) {
                    int returnValue = Integer.parseInt(parts[1]);
                    if (returnValue < minCacheTime) {
                        returnValue = minCacheTime;
                    } else if (returnValue > maxCacheTime) {
                        returnValue = maxCacheTime;
                    }
                    return returnValue;
                }
            } catch (NumberFormatException e) {
                // 解析失败时忽略该指令
            }
            return -1;
        }
    }

    /**
     * 元数据拉取操作类型：
     * <ul>
     *   <li>CREATE — 尚未缓存，需首次拉取并创建</li>
     *   <li>UPDATE — 已过期，需重新拉取并更新</li>
     *   <li>NO_UPDATE — 缓存仍有效，无需拉取</li>
     * </ul>
     */
    /** 元数据拉取/更新操作枚举。 */
    public enum FetchOperation {
        CREATE,
        UPDATE,
        NO_UPDATE
    }

    @Override
    /** 在 {@code PRE_AUTHORIZATION_REQUEST} 事件上执行 CIMD 完整处理流程。 */
    public void executeOnEvent(ClientPolicyContext context) throws ClientPolicyException {
        if (!Profile.isFeatureEnabled(Profile.Feature.CIMD)) {
            getLogger().warnf("CIMD executor is used, but CIMD feature is disabled. So CIMD is not enforced for the clients. " +
                    "Please enable CIMD feature in order to be able to have CIMD applied.");
            return;
        }

        switch (context.getEvent()) {
            case PRE_AUTHORIZATION_REQUEST:
                PreAuthorizationRequestContext preAuthorizationRequestContext = (PreAuthorizationRequestContext)context;
                process(preAuthorizationRequestContext);
                break;
            default:
        }
    }

    private void process(PreAuthorizationRequestContext preAuthorizationRequestContext) throws ClientPolicyException {
        provider = session.getProvider(ClientIdMetadataDocumentProvider.class, providerConfig.getCimdProviderName());
        provider.setConfiguration(getConfiguration());

        String clientId = preAuthorizationRequestContext.getClientId();

        // 授权请求参数校验
        URI redirectUriURI = verifyAuthorizationRequest(preAuthorizationRequestContext);

        // Client ID 格式校验（CIMD MUST 规则）
        URI clientIdURI = verifyClientId(clientId);

        // Client ID 策略验证（授权服务器自定义）
        validateClientId(clientIdURI);

        // 判断是否需要（重新）拉取客户端元数据
        FetchOperation fetchOp = provider.determineFetchOperation(clientId);
        if (fetchOp == FetchOperation.NO_UPDATE) {
            // 缓存有效，跳过拉取
            return;
        }

        // 拉取 Client ID 元数据文档
        OIDCClientRepresentationWithCacheControl clientOIDCWithCacheControl = fetchClientMetadata(clientIdURI, fetchOp == FetchOperation.UPDATE, provider);
        if (clientOIDCWithCacheControl == null) {
            // 304 Not Modified，仅更新缓存过期时间
            return;
        }

        // 客户端元数据规范校验
        verifyClientMetadata(clientIdURI, redirectUriURI, clientOIDCWithCacheControl.getOidcClientRepresentation());

        // 客户端元数据策略验证
        validateClientMetadata(clientIdURI, redirectUriURI, clientOIDCWithCacheControl.getOidcClientRepresentation());

        if (fetchOp == FetchOperation.CREATE) {
            // 首次创建客户端元数据缓存
            provider.createClientMetadata(clientOIDCWithCacheControl);
        } else if (fetchOp == FetchOperation.UPDATE) {
            // 更新已存在的客户端元数据缓存
            provider.updateClientMetadata(clientOIDCWithCacheControl);
        }
    }

    // 授权请求校验错误消息
    public static final String ERR_INVALID_PARAMETER = "Invalid Authorization Request: it does not include redirect_uri parameter";

    // Client ID 格式校验错误消息
    public static final String ERR_CLIENTID_MALFORMED_URL = "Invalid Client ID: malformed URL.";
    public static final String ERR_CLIENTID_INVALID_SCHEME = "Invalid Client ID: invalid scheme.";
    public static final String ERR_CLIENTID_EMPTY_PATH = "Invalid Client ID: empty path.";
    public static final String ERR_CLIENTID_PATH_TRAVERSAL = "Invalid Client ID: path traverse segment included.";
    public static final String ERR_CLIENTID_FRAGMENT = "Invalid Client ID: fragment included.";
    public static final String ERR_CLIENTID_USERINFO = "Invalid Client ID: userinfo included.";
    public static final String ERR_CLIENTID_QUERY = "Invalid Client ID: query included.";

    // Client ID 策略验证错误消息
    public static final String ERR_NOTALLOWED_DOMAIN = "Invalid Client ID: domain not allowed.";

    // 客户端元数据规范校验错误消息
    public static final String ERR_METADATA_NOCONTENT = "Invalid Client Metadata: no content.";
    public static final String ERR_METADATA_NOCLIENTID = "Invalid Client Metadata: no client_id.";
    public static final String ERR_METADATA_CLIENTID_UNMATCH = "Invalid Client Metadata: client_id property does not exactly match client_id parameter.";
    public static final String ERR_METADATA_NOTALLOWED_CLIENTAUTH = "Invalid Client Metadata: token_endpoint_auth_method property in client metadata is not-allowed authentication method..";
    public static final String ERR_METADATA_CLIENTSECRET = "Invalid Client Metadata: client_secret or client_secret_expires_at property in client metadata is must not included.";
    public static final String ERR_METADATA_REDIRECTURI = "Invalid Client Metadata: redirect_uri parameter does not exactly match the one of redirect_uris property in client metadata.";
    public static final String ERR_METADATA_MALFORMED_URL = "Invalid Client Metadata: malformed URL.";

    // Client ID 与元数据共用的校验错误消息
    public static final String ERR_HOST_UNRESOLVED = "Invalid Client ID / Metadata: host unresolved.";

    // 客户端元数据策略验证错误消息
    public static final String ERR_METADATA_URIS_SAMEDOMAIN = "Invalid Client Metadata: client_id parameter, redirect_uri parameter and at least one of redirect_uris properties in client metadata should be under the same domain.";
    public static final String ERR_METADATA_NO_REQUIRED_PROPERTIES = "Invalid Client Metadata: it does not include all required properties.";
    public static final String ERR_METADATA_NO_ALL_URIS_SAMEDOMAIN = "Invalid Client Metadata: some uri property is not under the same permitted domain";


    // 实现细节
    // 拉取元数据相关错误
    public static final String ERR_METADATA_FETCH_FAILED = "Client Metadata fetch failed";

    /**
     * 校验授权请求是否包含必需参数且格式正确。
     *
     * @param preAuthorizationRequestContext an authorization request
     * @return {@code URI} {@code redirect_uri} parameter value as {@link URI}
     * @throws ClientPolicyException when verification of an authorization request fails.
     */
    protected URI verifyAuthorizationRequest(PreAuthorizationRequestContext preAuthorizationRequestContext) throws ClientPolicyException {
        if (preAuthorizationRequestContext.getRequestParameters() == null) {
            getLogger().warn("authorization request does not include any parameter.");
            throw invalidClientIdMetadata(ERR_INVALID_PARAMETER);
        }

        if (preAuthorizationRequestContext.getRequestParameters().getFirst(OIDCLoginProtocol.CLIENT_ID_PARAM) == null) {
            getLogger().warn("authorization request does not include client_id.");
            throw invalidClientIdMetadata(ERR_INVALID_PARAMETER);
        }

        String redirectUri = preAuthorizationRequestContext.getRequestParameters().getFirst(OIDCLoginProtocol.REDIRECT_URI_PARAM);
        if (redirectUri == null) {
            getLogger().warn("authorization request does not include redirect_uri parameter.");
            throw invalidClientIdMetadata(ERR_INVALID_PARAMETER);
        }

        final URI uri;
        try {
            uri = new URI(redirectUri);
        } catch (URISyntaxException e) {
            getLogger().warnv("Malformed URL: redirectUri = {0}", redirectUri);
            throw invalidClientIdMetadata(ERR_INVALID_PARAMETER);
        }

        return uri;
    }

    /**
     * 校验 {@code client_id} 参数是否满足 CIMD/MCP 规范（HTTPS、路径、禁止 fragment/query 等）。
     *
     * @param clientId a value of {@code client_id} parameter of an authorization request
     * @return {@code URI} {@code client_uri} parameter value as {@link URI}
     * @throws ClientPolicyException when verification of an authorization request fails.
     */
    protected URI verifyClientId(final String clientId) throws ClientPolicyException {
        getLogger().debugv("verifyClientId: clientId = {0}", clientId);

        // client_id 必须为 URL
        final URI uri;
        try {
            uri = new URI(clientId);
        } catch (URISyntaxException e) {
            getLogger().warnv("Malformed URL: clientId = {0}", clientId);
            throw invalidClientIdMetadata(ERR_CLIENTID_MALFORMED_URL);
        }

        // 默认必须为 https scheme（开发环境可配置允许 http）
        if (!getConfiguration().isAllowHttpScheme() && !"https".equals(uri.getScheme())) {
            getLogger().warnv("Invalid URL Scheme: scheme = {0}", uri.getScheme());
            throw invalidClientIdMetadata(ERR_CLIENTID_INVALID_SCHEME);
        }

        // URL 必须包含非空路径
        if (uri.getPath() == null || uri.getPath().isEmpty()) {
            getLogger().warn("Empty path:");
            throw invalidClientIdMetadata(ERR_CLIENTID_EMPTY_PATH);
        }

        // 禁止含 {@code .} 或 {@code ..} 路径段（防路径遍历）
        if (isUnsafeUriPath(uri)) {
            getLogger().warnv("traverse path segment: raw path = {0}", uri.getRawPath());
            throw invalidClientIdMetadata(ERR_CLIENTID_PATH_TRAVERSAL);
        }

        // 禁止含 fragment
        if (uri.getFragment() != null) {
            getLogger().warnv("url fragment: fragment = {0}", uri.getFragment());
            throw invalidClientIdMetadata(ERR_CLIENTID_FRAGMENT);
        }

        // 禁止含 userinfo
        if (uri.getUserInfo() != null) {
            getLogger().warnv("user information: userinfo = {0}", uri.getUserInfo());
            throw invalidClientIdMetadata(ERR_CLIENTID_USERINFO);
        }

        // 不应含 query 字符串
        if (uri.getQuery() != null) {
            getLogger().warnv("url query: query = {0}", uri.getQuery());
            throw invalidClientIdMetadata(ERR_CLIENTID_QUERY);
        }

        // Client identifier URLs MAY contain a port.
        // -> no check, a port is allowed.

        // A short Client identifier URL is RECOMMENDED.
        // -> no check

        // A stable URL that does not frequently change for the client is RECOMMENDED.
        // -> no check

        // 受信域名与 SSRF 防护：
        //  It checks if a host part is under one of trusted domains.
        //  It checks if an address resolved from a property whose value is URI is loopback address.
        //  It checks if an address resolved from a property whose value is URI is private address.
        List<String> trustedDomains = convertContentFilledList(getConfiguration().getTrustedDomains());
        verifyUri(clientId, trustedDomains, (error, logMessageTemplate) -> {
            getLogger().warnv(logMessageTemplate, "client_id", clientId);
            throw invalidClientIdMetadata(error);
        });

        return uri;
    }

    /**
     * 按授权服务器自定义策略验证 client_id（基类默认无额外约束，子类可覆盖）。
     *
     * @param clientIdURI a value of {@code client_id} parameter of an authorization request in {@link URI}
     * @throws ClientPolicyException when validation of an authorization request fails.
     */
    protected void validateClientId(final URI clientIdURI) throws ClientPolicyException {
        // 授权服务器可对用作 client_id 的域名制定自有信任策略
    }

    // 与 TrustedHostClientRegistrationPolicy 相同的域名匹配逻辑
    protected boolean checkTrustedDomain(String hostname, String trustedDomain) {
        if (trustedDomain.startsWith("*.")) {
            String domain = trustedDomain.substring(2);
            return hostname.equals(domain) || hostname.endsWith("." + domain);
        }
        return hostname.equals(trustedDomain);
    }

    /**
     * 拉取客户端元数据；若已存在缓存则在 304 响应时仅更新过期时间。
     *
     * @param clientIdURI a value of {@code client_id} parameter of an authorization request in {@link URI}
     * @param isUpdate indicates the client metadata has been already created
     * @param provider {@link ClientIdMetadataDocumentProvider} for updating cache expiry time
     * @return {@code OIDCClientRepresentationWithCacheControl} a combination of a client metadata and Cache-Control header value accompanied by the metadata response.
     * {@code null} if a client metadata was re-fetched but the HTTP response status code is 304 Not Modified.
     * @throws ClientPolicyException when fetching a client metadata fails.
     */
    protected OIDCClientRepresentationWithCacheControl fetchClientMetadata(final URI clientIdURI, final boolean isUpdate,
                                                                           ClientIdMetadataDocumentProvider provider) throws ClientPolicyException {
        String clientId = clientIdURI.toString();

        SimpleHttpRequest simpleHttp = SimpleHttp.create(session).withMaxConsumedResponseSize(providerConfig.getUpperLimitMetadataBytes()).doGet(clientId);

        OIDCClientRepresentation clientOIDC;
        try (SimpleHttpResponse response = simpleHttp.asResponse()) {
            if (!isUpdate && response.getStatus() != Response.Status.OK.getStatusCode()) {
                getLogger().warnv("fetching client metadata for the first time failed: clientId = {0}", clientId);
                throw invalidClientIdMetadata(ERR_METADATA_FETCH_FAILED);
            }
            if (isUpdate && response.getStatus() != Response.Status.OK.getStatusCode() && response.getStatus() != Response.Status.NOT_MODIFIED.getStatusCode()) {
                getLogger().warnv("fetching client metadata for updating failed: clientId = {0}", clientId);
                throw invalidClientIdMetadata(ERR_METADATA_FETCH_FAILED);
            }

            String cacheControlHeaderValue = response.getFirstHeader(HttpHeaders.CACHE_CONTROL);
            ClientMetadataCacheControl clientMetadataCacheControl = new ClientMetadataCacheControl(cacheControlHeaderValue, providerConfig.getMinCacheTime(), providerConfig.getMaxCacheTime());

            if (isUpdate) {
                // 理想情况下应比对拉取元数据与现有元数据
                // 因注册时可能附加额外属性，完整比对较困难
                // 因此此处不执行内容比对
                if (response.getStatus() == Response.Status.NOT_MODIFIED.getStatusCode()) {
                    ClientModel clientModel = session.getContext().getRealm().getClientByClientId(clientId);
                    // 304 时仅更新缓存过期时间
                    provider.setCacheExpiryTimeToClientMetadata(clientModel, clientMetadataCacheControl.getCacheExpiryTimeInSec());
                    return null;
                }
            }

            clientOIDC = response.asJson(OIDCClientRepresentation.class);

            // 为成功转换为 ClientRepresentation 而增强元数据
            augmentClientOIDC(clientOIDC);

            return new OIDCClientRepresentationWithCacheControl(clientOIDC, clientMetadataCacheControl);
        } catch (IOException e) {
            getLogger().warnv("HTTP connection failure: {0}", e);
            throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, ERR_METADATA_FETCH_FAILED);
        }
    }

    /**
     * 校验拉取的客户端元数据是否满足 CIMD/MCP 规范要求。
     *
     * @param clientIdURI a value of {client_id} parameter of an authorization request in {@link URI}
     * @param redirectUriURI a value of {redirect_uri} parameter of an authorization request in {@link URI}
     * @param clientOIDC a client metadata
     * @return {@code URI} {@code client_id} property of a client metadata in {@link URI}
     * @throws ClientPolicyException when verifying a client metadata fails.
     */
    protected URI verifyClientMetadata(final URI clientIdURI, final URI redirectUriURI, final OIDCClientRepresentation clientOIDC) throws ClientPolicyException {
        String clientId = clientIdURI.toString();
        String redirectUri = redirectUriURI.toString();

        if (clientOIDC == null) {
            getLogger().warn("client metadata does not have its content.");
            throw invalidClientIdMetadata(ERR_METADATA_NOCONTENT);
        }

        // 元数据文档必须包含 client_id 属性
        if (clientOIDC.getClientId() == null) {
            getLogger().warn("client metadata does not include client_id property.");
            throw invalidClientIdMetadata(ERR_METADATA_NOCLIENTID);
        }

        // client_id 属性值必须与文档 URL 精确匹配（RFC3986 字符串比较）
        // using simple string comparison as defined in [RFC3986] Section 6.2.1.
        if (!clientOIDC.getClientId().equals(clientId)) {
            getLogger().warnv("client_id property in client metadata does not exactly match client_id parameter in authorization request. property = {0}, parameter = {1}", clientOIDC.getClientId(), clientId);
            throw invalidClientIdMetadata(ERR_METADATA_CLIENTID_UNMATCH);
        }

        // token_endpoint_auth_method 不得使用基于共享对称密钥的方式
        // client_secret_post, client_secret_basic, client_secret_jwt,
        // or any other method based around a shared symmetric secret.
        if (clientOIDC.getTokenEndpointAuthMethod() != null && NOTALLOWED_ALGORITHMS.contains(clientOIDC.getTokenEndpointAuthMethod())) {
            getLogger().warnv("not allowed client auth method: token_endpoint_auth_method = {0}", clientOIDC.getTokenEndpointAuthMethod());
            throw invalidClientIdMetadata(ERR_METADATA_NOTALLOWED_CLIENTAUTH);
        }

        // 不得包含 client_secret 或 client_secret_expires_at
        if (clientOIDC.getClientSecret() != null || clientOIDC.getClientSecretExpiresAt() != null) {
            getLogger().warn("client metadata includes client_secret or client_secret_expires_at.");
            throw invalidClientIdMetadata(ERR_METADATA_CLIENTSECRET);
        }

        // 必须校验授权请求 redirect_uri 与元数据 redirect_uris 之一精确匹配
        // against those in the metadata document.
        if (clientOIDC.getRedirectUris() == null || RedirectUtils.verifyRedirectUri(session, clientOIDC.getClientUri(), redirectUri, Set.copyOf(clientOIDC.getRedirectUris()), true) == null) {
            getLogger().warnv("redirect_uri parameter does not exactly match the one of redirect_uris property in client metadata: redirectUri = {0}", redirectUri);
            throw invalidClientIdMetadata(ERR_METADATA_REDIRECTURI);
        }

        // Trusted domains and SSRF attack countermeasure:
        //  It checks if a host part is under one of trusted domains.
        //  It checks if an address resolved from a property whose value is URI is loopback address.
        //  It checks if an address resolved from a property whose value is URI is private address.
        // CIMD 强制校验：client_id
        // RFC 7591 强制校验：redirect_uris
        // RFC 7591 可选 URI 属性（若存在则校验）

        List<String> trustedDomains = convertContentFilledList(getConfiguration().getTrustedDomains());
        verifyUriProperty(clientOIDC.getClientId(), "client_id", trustedDomains);

        for (String redirect_uri : clientOIDC.getRedirectUris()) {
            verifyUriProperty(redirect_uri, "redirect_uris", trustedDomains);
        }
 
        verifyUriPropertyIfPresent(clientOIDC.getLogoUri(), "logo_uri", trustedDomains);
        verifyUriPropertyIfPresent(clientOIDC.getClientUri(), "client_uri", trustedDomains);
        verifyUriPropertyIfPresent(clientOIDC.getTosUri(), "tos_uri", trustedDomains);
        verifyUriPropertyIfPresent(clientOIDC.getPolicyUri(), "policy_uri", trustedDomains);
        verifyUriPropertyIfPresent(clientOIDC.getJwksUri(), "jwks_uri", trustedDomains);

        URI clientIdURIfromMetadata;
        try {
            clientIdURIfromMetadata = new URI(clientOIDC.getClientId());
        } catch (URISyntaxException e) {
            // 不应到达此处
            getLogger().warnv("Malformed URL: clientId in metadata = {0}", clientOIDC.getClientId());
            throw invalidClientIdMetadata(ERR_CLIENTID_MALFORMED_URL);
        }

        return clientIdURIfromMetadata;
    }

    private void verifyUriProperty(String uriString, String propertyName, List<String> trustedDomains) throws ClientPolicyException {
        verifyUri(uriString, trustedDomains, (error, logMessageTemplate) -> {
            getLogger().warnv(logMessageTemplate, propertyName, uriString);
            throw invalidClientIdMetadata(error);
        });
    }

    private void verifyUriPropertyIfPresent(String uriString, String propertyName, List<String> trustedDomains) throws ClientPolicyException {
        if (uriString != null) {
            verifyUriProperty(uriString, propertyName, trustedDomains);
        }
    }

    // 检测编码或未编码的 {@code /../}、{@code /./} 等不安全路径段
    private final static Pattern UNSAFE_PATH_PATTERN = Pattern.compile(
            "(/|%2[fF]|%5[cC]|\\\\)(%2[eE]|\\.){1,2}(/|%2[fF]|%5[cC]|\\\\)|(/|%2[fF]|%5[cC]|\\\\)(%2[eE]|\\.){1,2}$");

    private boolean isUnsafeUriPath(URI redirectUri) {
        return UNSAFE_PATH_PATTERN.matcher(redirectUri.getRawPath()).find();
    }

    private void verifyUri(String uriString, List<String> trustedDomains, ErrorHandler errorHandler) throws ClientPolicyException {
        if (trustedDomains.isEmpty()) {        // 未配置受信域名则拒绝
            getLogger().debug("trusted domain list is vacant.");
            throw invalidClientIdMetadata(ERR_NOTALLOWED_DOMAIN);
        }

        final URI uri;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            errorHandler.onError(ERR_METADATA_MALFORMED_URL, "Malformed URL: {0} property in metadata = {1}");
            return;
        }

        if (uri.getHost() == null) {
            getLogger().warnv("not trusted domain: host = {0}", uri.getHost());
            throw invalidClientIdMetadata(ERR_HOST_UNRESOLVED);
        }

        if (trustedDomains.stream().noneMatch(i->checkTrustedDomain(uri.getHost(), i))) {
            getLogger().warnv("not trusted domain: host = {0}", uri.getHost());
            throw invalidClientIdMetadata(ERR_NOTALLOWED_DOMAIN);
        }
    }

    public interface ErrorHandler {
        void onError(String error, String logMessageTemplate) throws ClientPolicyException;
    }

    /**
     * 按策略验证客户端元数据（同域限制、必填属性等）。
     *
     * @param clientIdURI a value of {client_id} parameter of an authorization request in {@link URI}
     * @param redirectUriURI a value of {redirect_uri} parameter of an authorization request in {@link URI}
     * @param clientOIDC a client metadata
     * @throws ClientPolicyException when validating a client metadata fails.
     */
    protected void validateClientMetadata(final URI clientIdURI, final URI redirectUriURI, final OIDCClientRepresentation clientOIDC) throws ClientPolicyException {
        // 授权服务器可对 redirect_uris 与 client_id/client_uri 施加额外关系约束
        // between the redirect_uris and the client_id or client_uri properties

        // 同域策略：client_id、redirect_uri 及元数据中各 URI 属性须在同一受信域
        List<String> trustedDomains = convertContentFilledList(getConfiguration().getTrustedDomains());
        if (getConfiguration().isRestrictSameDomain() && trustedDomains != null && !trustedDomains.isEmpty()) {
            // 元数据规范校验已保证 client_id 与 redirect_uri 精确匹配
            //  - client_id parameter value in an authorization request exactly matches client_id property in metadata
            //  - redirect_uri parameter value in an authorization request exactly matches one of client_uris property in metadata
            // Therefore, only considering domain parts of client_id parameter value, redirect_uri parameter value matches one of permitted domains configuration.
            if (trustedDomains.stream().noneMatch(i->checkTrustedDomain(clientIdURI.getHost(), i) && checkTrustedDomain(redirectUriURI.getHost(), i))) {
                getLogger().warnv("client_id and redirect_uri domain not match: client_id host part = {0}, redirect_uri host part = {1}", clientIdURI.getHost(), redirectUriURI.getHost());
                throw invalidClientIdMetadata(ERR_METADATA_URIS_SAMEDOMAIN);
            }

            List<String> l = Stream.of(clientOIDC.getClientId(), clientOIDC.getClientUri(), clientOIDC.getLogoUri(), clientOIDC.getTosUri(), clientOIDC.getPolicyUri(), clientOIDC.getJwksUri())
                    .filter(Objects::nonNull).toList();
            try {
                for (String s : l) {
                    URI u = new URI(s);
                    if (trustedDomains.stream().filter(i->!i.isBlank()).noneMatch(i -> checkTrustedDomain(u.getHost(), i) && checkTrustedDomain(redirectUriURI.getHost(), i))) {
                        getLogger().warnv("not under the same domain = {0}", u.getHost());
                        throw invalidClientIdMetadata(ERR_METADATA_NO_ALL_URIS_SAMEDOMAIN);
                    }
                }
            } catch (URISyntaxException e) {
                getLogger().warnv("URI not resolved {0}}", e);
                throw invalidClientIdMetadata(ERR_METADATA_NO_ALL_URIS_SAMEDOMAIN);
            }
        }

        // 必填属性策略
        List<String> requiredProperties = convertContentFilledList(getConfiguration().getRequiredProperties());
        if (requiredProperties != null && !requiredProperties.isEmpty()) {
            JsonNode jn = JsonSerialization.writeValueAsNode(clientOIDC);
            if (requiredProperties.stream().filter(i->!i.isBlank()).anyMatch(i->jn.get(i) == null)) {
                getLogger().warn("metadata does not include required properties");
                throw invalidClientIdMetadata(ERR_METADATA_NO_REQUIRED_PROPERTIES);
            }
        }
    }

    // 为支持 CIMD 公开客户端，故意未将 "none" 列入禁止列表
    protected static final Set<String> NOTALLOWED_ALGORITHMS = new LinkedHashSet<>(Arrays.asList(
            OIDCLoginProtocol.CLIENT_SECRET_POST,
            OIDCLoginProtocol.CLIENT_SECRET_BASIC,
            OIDCLoginProtocol.CLIENT_SECRET_JWT
    ));

    protected List<String> convertContentFilledList(List<String> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream().filter(Objects::nonNull).filter(i->!i.isBlank()).distinct().toList();
    }

    // to successfully convert it to Client Representation, intentionally augment it.

    /**
     * 增强拉取的元数据以便转换为 {@code ClientRepresentation}（如缺省 token_endpoint_auth_method 时设为 none）。
     *
     * @param oidcClient a fetched client metadata
     */
    protected void augmentClientOIDC(OIDCClientRepresentation oidcClient) {
        // 允许公开客户端：
        // DescriptionConverter.toInternal 在 token_endpoint_auth_method 为 none 时识别为公开客户端
        // 元数据缺少该字段时默认 none，即按公开客户端处理
        if (oidcClient.getTokenEndpointAuthMethod() == null) {
            oidcClient.setTokenEndpointAuthMethod("none");
        }
    }

    protected static ClientPolicyException invalidClientIdMetadata(String errorDetail) {
        return new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, errorDetail);
    }
}
