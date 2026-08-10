package org.keycloak.services.resources.account;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.requiredactions.DeleteAccount;
import org.keycloak.authentication.requiredactions.UpdateEmail;
import org.keycloak.common.Profile;
import org.keycloak.common.Version;
import org.keycloak.common.util.Environment;
import org.keycloak.models.AccountRoles;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.FederatedIdentityModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.IdentityProviderQuery;
import org.keycloak.models.IdentityProviderStorageProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.TokenManager;
import org.keycloak.protocol.oidc.utils.RedirectUtils;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.Auth;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.resource.AccountResourceProvider;
import org.keycloak.services.resources.RealmsResource;
import org.keycloak.services.util.ResolveRelative;
import org.keycloak.services.util.ViteManifest;
import org.keycloak.services.validation.Validation;
import org.keycloak.theme.FreeMarkerException;
import org.keycloak.theme.Theme;
import org.keycloak.theme.ThemeResourcesParser;
import org.keycloak.theme.beans.LocaleBean;
import org.keycloak.theme.beans.MessageFormatterMethod;
import org.keycloak.theme.freemarker.FreeMarkerProvider;
import org.keycloak.urls.UrlType;
import org.keycloak.util.JsonSerialization;
import org.keycloak.utils.MediaType;
import org.keycloak.utils.SecureContextResolver;

import org.jboss.resteasy.reactive.NoCache;

/**
 * 账户控制台 HTML 页面资源提供者。
 * <p>通过 FreeMarker 渲染账户管理 SPA 入口页，注入主题、国际化、功能开关及 Vite 构建产物等上下文变量。</p>
 * <p>Created by st on 29/03/17.</p>
 */
public class AccountConsole implements AccountResourceProvider {

    /** 匹配 Java 资源束占位符 {@code {0}} 的正则 */
    private final Pattern bundleParamPattern = Pattern.compile("(\\{\\s*(\\d+)\\s*\\})");

    /** Keycloak 会话 */
    protected final KeycloakSession session;

    /** 应用认证管理器，用于 identity cookie 认证 */
    private final AppAuthManager authManager;
    /** 当前领域 */
    private final RealmModel realm;
    /** 账户管理客户端（{@code account}） */
    private final ClientModel client;
    /** 账户主题 */
    private final Theme theme;

    /** 当前用户认证上下文（可能为 null） */
    private Auth auth;

    /**
     * 构造账户控制台实例。
     * @param session Keycloak 会话
     * @param client 账户管理客户端
     * @param theme 账户主题
     */
    public AccountConsole(KeycloakSession session, ClientModel client, Theme theme) {
        this.session = session;
        this.realm = session.getContext().getRealm();
        this.client = client;
        this.theme = theme;
        this.authManager = new AppAuthManager();
        init();
    }

    /** 尝试通过 identity cookie 初始化 {@link #auth} */
    public void init() {
        AuthenticationManager.AuthResult authResult = authManager.authenticateIdentityCookie(session, realm);
        if (authResult != null) {
            auth = new Auth(realm, authResult.token(), authResult.user(), client, authResult.session(), true);
        }
    }

    /** {@inheritDoc} 返回自身作为 JAX-RS 资源 */
    @Override
    public Object getResource() {
        return this;
    }

    /** {@inheritDoc} 无资源需释放 */
    @Override
    public void close() {
    }

    @GET
    @NoCache
    @Path("{path:.*}")
    /**
     * 捕获所有子路径 GET 请求，渲染账户控制台主页。
     * @param path 请求路径（通配）
     * @return HTML 响应
     */
    public Response getMainPage(@PathParam("path") String path) throws IOException, FreeMarkerException {

        return renderAccountConsole();
    }

    /**
     * 组装模板变量并渲染账户控制台 HTML。
     * @return HTML 响应
     */
    protected Response renderAccountConsole() throws IOException, FreeMarkerException {
        final var serverUriInfo = session.getContext().getUri(UrlType.FRONTEND);
        final var serverBaseUri = serverUriInfo.getBaseUri();
        // 去除 URL 末尾斜杠
        final var serverBaseUrl = serverBaseUri.toString().replaceFirst("/+$", "");

        final var map = new HashMap<String, Object>();
        final var accountBaseUrl = serverUriInfo.getBaseUriBuilder()
                .path(RealmsResource.class)
                .path(realm.getName())
                .path(Constants.ACCOUNT_MANAGEMENT_CLIENT_ID)
                .path("/")
                .build(realm);

        final var isSecureContext = SecureContextResolver.isSecureContext(session);

        map.put("isSecureContext", isSecureContext);
        map.put("serverBaseUrl", serverBaseUrl);
        // TODO：部分变量仅为旧主题向后兼容保留，后续版本应移除
        // Note that these should be removed from the template of the Account Console as well.
        map.put("authUrl", serverBaseUrl + "/"); // 已由 serverBaseUrl 取代，后续移除
        map.put("authServerUrl", serverBaseUrl + "/"); // 已由 serverBaseUrl 取代，后续移除
        map.put("baseUrl", accountBaseUrl.getPath().endsWith("/") ? accountBaseUrl : accountBaseUrl + "/");
        map.put("realm", realm);
        map.put("clientId", Constants.ACCOUNT_CONSOLE_CLIENT_ID);
        map.put("resourceUrl", Urls.themeRoot(serverBaseUri).getPath() + "/" + Constants.ACCOUNT_MANAGEMENT_CLIENT_ID + "/" + theme.getName());
        map.put("resourceCommonUrl", Urls.themeRoot(serverBaseUri).getPath() + "/common/keycloak");
        map.put("resourceVersion", Version.RESOURCES_VERSION);

        MultivaluedMap<String, String> queryParameters = session.getContext().getUri().getQueryParameters();
        var requestedScopes = queryParameters.getFirst(OIDCLoginProtocol.SCOPE_PARAM);

        if (requestedScopes == null) {
            requestedScopes = AuthenticationManager.getRequestedScopes(session, realm.getClientByClientId(Constants.ACCOUNT_CONSOLE_CLIENT_ID));
        }

        if (requestedScopes != null) {
            map.put(OIDCLoginProtocol.SCOPE_PARAM, requestedScopes);
        }

        String[] referrer = getReferrer();
        if (referrer != null) {
            map.put("referrer", referrer[0]);
            map.put("referrerName", referrer[1]);
            map.put("referrer_uri", referrer[2]);
        }

        UserModel user = null;
        if (auth != null) user = auth.getUser();
        Locale locale = session.getContext().resolveLocale(user);
        map.put("locale", locale.toLanguageTag());
        Properties messages = theme.getEnhancedMessages(realm, locale);
        map.put("localeDir", new LocaleBean(realm, locale, session.getContext().getUri().getRequestUriBuilder(), messages).isRtl() ? "rtl" : "ltr");
        map.put("msg", new MessageFormatterMethod(locale, messages));
        map.put("msgJSON", messagesToJsonString(messages));
        map.put("supportedLocales", supportedLocales(messages));
        map.put("properties", theme.getProperties());
        map.put("themeResources", ThemeResourcesParser.parse(theme.getProperties()));
        map.put("darkMode", "true".equals(theme.getProperties().getProperty("darkMode"))
                && realm.getAttribute("darkMode", true));
        map.put("theme", (Function<String, String>) file -> {
            try {
                final InputStream resource = theme.getResourceAsStream(file);
                return new Scanner(resource, StandardCharsets.UTF_8).useDelimiter("\\A").next();
            } catch (IOException e) {
                throw new RuntimeException("could not load file", e);
            }
        });

        map.put("isAuthorizationEnabled", Profile.isFeatureEnabled(Profile.Feature.AUTHORIZATION));
        map.put("isLinkedAccountsEnabled", isLinkedAccountsEnabled(user));

        boolean deleteAccountAllowed = false;
        boolean isViewGroupsEnabled = false;
        boolean isViewApplicationsEnabled = false;
        boolean isOid4VciEnabled = false;
        if (user != null) {
            AccountRoleChecker roleChecker = new AccountRoleChecker(session, realm, user);
            // manage-account 角色在 account 客户端 API 层相当于复合角色
            deleteAccountAllowed = roleChecker.hasOneOfRole(AccountRoles.MANAGE_ACCOUNT, AccountRoles.DELETE_ACCOUNT) && realm.getRequiredActionProviderByAlias(DeleteAccount.PROVIDER_ID).isEnabled();
            isViewGroupsEnabled = roleChecker.hasOneOfRole(AccountRoles.MANAGE_ACCOUNT, AccountRoles.VIEW_GROUPS)
                    && user.getGroupsCount() > 0;
            isViewApplicationsEnabled = roleChecker.hasOneOfRole(AccountRoles.MANAGE_ACCOUNT, AccountRoles.VIEW_APPLICATIONS);
            isOid4VciEnabled = Profile.isFeatureEnabled(Profile.Feature.OID4VC_VCI) && realm.isVerifiableCredentialsEnabled()  && roleChecker.hasOneOfRole(AccountRoles.MANAGE_ACCOUNT, AccountRoles.VIEW_VERIFIABLE_CREDENTIALS);
        }

        map.put("deleteAccountAllowed", deleteAccountAllowed);

        map.put("isViewApplicationsEnabled", isViewApplicationsEnabled);
        map.put("isViewGroupsEnabled", isViewGroupsEnabled);
        map.put("isViewOrganizationsEnabled", realm.isOrganizationsEnabled());
        map.put("isOid4VciEnabled", isOid4VciEnabled);

        map.put("updateEmailFeatureEnabled", Profile.isFeatureEnabled(Profile.Feature.UPDATE_EMAIL));
        map.put("updateEmailActionEnabled", UpdateEmail.isEnabled(realm));

        final var devServerUrl = Environment.isDevMode() ? System.getenv(ViteManifest.ACCOUNT_VITE_URL) : null;

        if (devServerUrl != null) {
            map.put("devServerUrl", devServerUrl);
        }

        final var manifestFile = theme.getResourceAsStream(ViteManifest.MANIFEST_FILE_PATH);

        if (devServerUrl == null && manifestFile != null) {
            final var manifest = ViteManifest.parseFromInputStream(manifestFile);
            final var entryChunk = manifest.getEntryChunk();
            final var entryStyles = entryChunk.css().orElse(new String[] {});
            final var entryScript = entryChunk.file();
            final var entryImports = entryChunk.imports().orElse(new String[] {});

            map.put("entryStyles", entryStyles);
            map.put("entryScript", entryScript);
            map.put("entryImports", entryImports);
        }

        FreeMarkerProvider freeMarkerUtil = session.getProvider(FreeMarkerProvider.class);
        String result = renderAccountConsole(freeMarkerUtil, map);
        Response.ResponseBuilder builder = Response.status(Response.Status.OK).type(MediaType.TEXT_HTML_UTF_8).language(Locale.ENGLISH).entity(result);
        return builder.build();
    }

    /** 使用 FreeMarker 处理 {@code index.ftl} 模板 */
    protected String renderAccountConsole(FreeMarkerProvider freeMarkerUtil, Map<String, Object> map) throws FreeMarkerException {
        return freeMarkerUtil.processTemplate(map, "index.ftl", theme);
    }

    /** 返回领域支持的语言及其显示名映射 */
    protected Map<String, String> supportedLocales(Properties messages) {
        return realm.getSupportedLocalesStream()
                .collect(Collectors.toMap(Function.identity(), l -> messages.getProperty("locale_" + l, l)));
    }

    /** 将主题消息属性序列化为 JSON 供前端 ngx-translate 使用 */
    protected String messagesToJsonString(Properties props) {
        if (props == null) return "";
        Properties newProps = new Properties();
        for (String prop : props.stringPropertyNames()) {
            newProps.put(prop, convertPropValue(props.getProperty(prop)));
        }
        try {
            return JsonSerialization.writeValueAsString(newProps);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** 转换资源束值为 ngx-translate 兼容格式 */
    private String convertPropValue(String propertyValue) {
        // 模拟 FreeMarker 模板中 java.text.MessageFormat 的行为：
        // To print a single quote one needs to write two single quotes.
        // Single quotes will be stripped.
        // Usually single quotes would escape parameters, but this not implemented here.
        propertyValue = propertyValue.replaceAll("'('?)", "$1");
        propertyValue = putJavaParamsInNgTranslateFormat(propertyValue);

        return propertyValue;
    }

    // 将 Java 资源束占位符转为 ngx-translate 格式
    // Do you like {0} and {1} ?
    //    becomes
    // Do you like {{param_0}} and {{param_1}} ?
    private String putJavaParamsInNgTranslateFormat(String propertyValue) {
        Matcher matcher = bundleParamPattern.matcher(propertyValue);
        while (matcher.find()) {
            propertyValue = propertyValue.replace(matcher.group(1), "{{param_" + matcher.group(2) + "}}");
        }

        return propertyValue;
    }

    @GET
    @Path("index.html")
    /** 将 index.html 请求重定向到上级路径 */
    public Response getIndexHtmlRedirect() {
        return Response.status(302).location(session.getContext().getUri().getRequestUriBuilder().path("../").build()).build();
    }

    /** 解析并校验 referrer 查询参数，返回 [clientId, displayName, uri] */
    private String[] getReferrer() {
        String referrer = session.getContext().getUri().getQueryParameters().getFirst("referrer");

        if (referrer == null) {
            return null;
        }

        ClientModel referrerClient = realm.getClientByClientId(referrer);

        if (referrerClient == null) {
            return null;
        }

        String referrerUri = session.getContext().getUri().getQueryParameters().getFirst("referrer_uri");

        if (referrerUri != null) {
            referrerUri = RedirectUtils.verifyRedirectUri(session, referrerUri, referrerClient);
        } else {
            referrerUri = ResolveRelative.resolveRelativeUri(session, referrerClient.getRootUrl(), referrerClient.getBaseUrl());
        }

        if (referrerUri == null) {
            return null;
        }

        String referrerName = referrerClient.getName();

        if (Validation.isBlank(referrerName)) {
            referrerName = referrer;
        }

        return new String[]{referrer, referrerName, referrerUri};
    }

    /** 判断是否启用关联账户功能（存在可用 IdP 或用户已关联 IdP） */
    protected boolean isLinkedAccountsEnabled(UserModel user) {
        if (user == null) {
            return false;
        }

        IdentityProviderStorageProvider identityProviders = session.identityProviders();
        Stream<IdentityProviderModel> realmBrokers = identityProviders.getAllStream(IdentityProviderQuery.userAuthentication()
                .with(IdentityProviderModel.ENABLED, "true")
                .with(IdentityProviderModel.ORGANIZATION_ID, ""),
                0, 1);
        Stream<IdentityProviderModel> linkedBrokers = session.users().getFederatedIdentitiesStream(realm, user)
                .map(FederatedIdentityModel::getIdentityProvider)
                .map(identityProviders::getByAlias);

        return Stream.concat(realmBrokers, linkedBrokers).findAny().isPresent();
    }

    /**
     * 检查用户是否拥有将出现在 {@code account-console} 客户端访问令牌中的账户角色。
     */
    static class AccountRoleChecker {

        /** 账户管理客户端 */
        private final ClientModel accountClient;
        /** 按请求 scope 解析后的角色集合 */
        private final Set<RoleModel> scopeResolvedRoles;

        /** 根据 account-console 客户端 scope 解析用户角色 */
        AccountRoleChecker(KeycloakSession session, RealmModel realm, UserModel user) {
            this.accountClient = realm.getClientByClientId(Constants.ACCOUNT_MANAGEMENT_CLIENT_ID);
            ClientModel accountConsoleClient = realm.getClientByClientId(Constants.ACCOUNT_CONSOLE_CLIENT_ID);
            this.scopeResolvedRoles = TokenManager.getAccess(user, accountConsoleClient, TokenManager.getRequestedClientScopes(session, null, accountConsoleClient, user));
        }

        /** 检查用户是否拥有指定账户角色 */
        boolean hasRole(String roleName) {
            RoleModel role = accountClient.getRole(roleName);
            return role != null && scopeResolvedRoles.contains(role);
        }

        /** 检查用户是否拥有任一指定账户角色 */
        boolean hasOneOfRole(String... roleNames) {
            for (String roleName : roleNames) {
                if (hasRole(roleName)) {
                    return true;
                }
            }
            return false;
        }
    }
}
