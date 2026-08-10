/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.services.resources.admin;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.keycloak.Config;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.Profile;
import org.keycloak.common.Version;
import org.keycloak.common.util.Environment;
import org.keycloak.common.util.UriUtils;
import org.keycloak.headers.SecurityHeadersProvider;
import org.keycloak.http.HttpRequest;
import org.keycloak.http.HttpResponse;
import org.keycloak.models.AdminRoles;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocolService;
import org.keycloak.services.Urls;
import org.keycloak.services.cors.Cors;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.util.ViteManifest;
import org.keycloak.theme.FreeMarkerException;
import org.keycloak.theme.ThemeResourcesParser;
import org.keycloak.theme.freemarker.FreeMarkerProvider;
import org.keycloak.urls.UrlType;
import org.keycloak.utils.MediaType;
import org.keycloak.utils.SecureContextResolver;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.NoCache;

import static org.keycloak.models.UserModel.IS_TEMP_ADMIN_ATTR_NAME;

/**
 * 领域管理控制台 REST 资源（{@code /admin/{realm}/console}）。
 * <p>提供 whoami 权限查询、主页面渲染、登出及本地化消息。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class AdminConsole {
    /** 日志记录器 */
    protected static final Logger logger = Logger.getLogger(AdminConsole.class);

    /** 客户端连接信息 */
    protected final ClientConnection clientConnection;

    /** HTTP 请求 */
    protected final HttpRequest request;

    /** HTTP 响应 */
    protected final HttpResponse response;

    /** Keycloak 会话 */
    protected final KeycloakSession session;

    /** 当前领域 */
    protected final RealmModel realm;

    /** 从会话上下文构造控制台资源。 */
    public AdminConsole(KeycloakSession session) {
        this.session = session;
        this.realm = session.getContext().getRealm();
        this.clientConnection = session.getContext().getConnection();
        this.request = session.getContext().getHttpRequest();
        this.response = session.getContext().getHttpResponse();
    }

    /** 控制台 whoami 端点返回的当前用户与权限信息。 */
    public static class WhoAmI {
        /** 用户 ID */
        protected String userId;
        /** 当前领域名称 */
        protected String realm;
        /** 显示名称（姓名或用户名） */
        protected String displayName;
        /** 用户区域设置 */
        protected Locale locale;
        /** 是否为临时管理员账户 */
        protected boolean isTemporary;

        /** 是否具备 create-realm 权限 */
        @JsonProperty("createRealm")
        protected boolean createRealm;
        /** 各领域的 realm-admin 角色集合 */
        @JsonProperty("realm_access")
        protected Map<String, Set<String>> realmAccess = new HashMap<String, Set<String>>();

        /** 默认构造器（JSON 反序列化）。 */
        public WhoAmI() {
        }

        /** 全参构造器。 */
        public WhoAmI(String userId, String realm, String displayName, boolean createRealm, Map<String, Set<String>> realmAccess, Locale locale, boolean isTemporary) {
            this.userId = userId;
            this.realm = realm;
            this.displayName = displayName;
            this.createRealm = createRealm;
            this.realmAccess = realmAccess;
            this.locale = locale;
            this.isTemporary = isTemporary;
        }

        /** @return 用户 ID */
        public String getUserId() {
            return userId;
        }

        /** @param userId 用户 ID */
        public void setUserId(String userId) {
            this.userId = userId;
        }

        /** @return 领域名称 */
        public String getRealm() {
            return realm;
        }

        /** @param realm 领域名称 */
        public void setRealm(String realm) {
            this.realm = realm;
        }

        /** @return 显示名称 */
        public String getDisplayName() {
            return displayName;
        }

        /** @param displayName 显示名称 */
        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        /** @return 是否可创建领域 */
        public boolean isCreateRealm() {
            return createRealm;
        }

        /** @param createRealm 是否可创建领域 */
        public void setCreateRealm(boolean createRealm) {
            this.createRealm = createRealm;
        }

        /** @return 领域角色映射 */
        public Map<String, Set<String>> getRealmAccess() {
            return realmAccess;
        }

        /** @param realmAccess 领域角色映射 */
        public void setRealmAccess(Map<String, Set<String>> realmAccess) {
            this.realmAccess = realmAccess;
        }

        /** @return 区域设置 */
        public Locale getLocale() {
            return locale;
        }

        /** @param locale 区域设置 */
        public void setLocale(Locale locale) {
            this.locale = locale;
        }

        /** @return BCP 47 语言标签 */
        @JsonProperty(value = "locale")
        public String getLocaleLanguageTag() {
            return locale != null ? locale.toLanguageTag() : null;
        }

        /** @return 是否为临时管理员 */
        public boolean isTemporary() {
            return isTemporary;
        }

        /** @param temporary 是否为临时管理员 */
        public void setTemporary(boolean temporary) {
            isTemporary = temporary;
        }
    }

    /** whoami 端点 CORS 预检。 */
    @Path("whoami")
    @OPTIONS
    public Response whoAmIPreFlight() {
        return new AdminCorsPreflightService().preflight();
    }

    /**
     * 返回当前登录管理员的权限与身份信息。
     *
     * @param currentRealm 当前操作的领域名称（master 用户跨领域时使用）
     * @return {@link WhoAmI} JSON 响应
     */
    @Path("whoami")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @NoCache
    public Response whoAmI(@QueryParam("currentRealm") String currentRealm) {
        if (!Profile.isFeatureEnabled(Profile.Feature.ADMIN_API)) {
            throw new NotFoundException();
        }

        RealmManager realmManager = new RealmManager(session);
        AuthenticationManager.AuthResult authResult = new AppAuthManager.BearerTokenAuthenticator(session)
                .setRealm(realm)
                .setConnection(clientConnection)
                .setHeaders(session.getContext().getRequestHeaders())
                .authenticate();

        if (authResult == null) {
            throw new NotAuthorizedException("Bearer");
        }

        final String issuedFor = authResult.token().getIssuedFor();
        if (!Constants.ADMIN_CONSOLE_CLIENT_ID.equals(issuedFor)) {
            if (issuedFor == null) {
                throw new ForbiddenException("No azp claim in the token");
            }
            // 检查客户端 security-admin-console 属性以允许替代控制台客户端
            ClientModel client  = session.clients().getClientByClientId(realm, issuedFor);
            if (client == null || !Boolean.parseBoolean(client.getAttribute(Constants.SECURITY_ADMIN_CONSOLE_ATTR))) {
                throw new ForbiddenException("Token issued for an application that is not the admin console: " + issuedFor);
            }
        }

        UserModel user= authResult.user();
        String displayName;
        if ((user.getFirstName() != null && !user.getFirstName().trim().equals("")) || (user.getLastName() != null && !user.getLastName().trim().equals(""))) {
            displayName = user.getFirstName();
            if (user.getLastName() != null) {
                displayName = displayName != null ? displayName + " " + user.getLastName() : user.getLastName();
            }
        } else {
            displayName = user.getUsername();
        }

        RealmModel masterRealm = getAdminstrationRealm(realmManager);
        Map<String, Set<String>> realmAccess = new HashMap<String, Set<String>>();
        if (masterRealm == null)
            throw new NotFoundException("No realm found");
        boolean createRealm = false;
        if (realm.equals(masterRealm)) {
            logger.debug("setting up realm access for a master realm user");
            RoleModel createRealmRole = masterRealm.getRole(AdminRoles.CREATE_REALM);
            if (createRealmRole != null) {
                createRealm = user.hasRole(createRealmRole);
            }
            addMasterRealmAccess(user, currentRealm != null ? currentRealm : realm.getName(), realmAccess);
        } else {
            logger.debug("setting up realm access for a realm user");
            addRealmAccess(realm, user, realmAccess);
        }

        if (realmAccess.isEmpty() || realmAccess.values().iterator().next().isEmpty()) {
            // 用户在目标领域无任何管理角色则返回 403
            throw new ForbiddenException("No realm access");
        }

        Locale locale = session.getContext().resolveLocale(user);

        return Cors.builder()
                .checkAllowedOrigins(authResult.token())
                .allowedMethods("GET")
                .auth()
                .add(Response.ok(new WhoAmI(user.getId(), realm.getName(), displayName, createRealm, realmAccess, locale, Boolean.parseBoolean(user.getFirstAttribute(IS_TEMP_ADMIN_ATTR_NAME)))));
    }

    /** 填充指定领域的 realm-admin 客户端角色。 */
    private void addRealmAccess(RealmModel realm, UserModel user, Map<String, Set<String>> realmAdminAccess) {
        RealmManager realmManager = new RealmManager(session);
        ClientModel realmAdminApp = realm.getClientByClientId(realmManager.getRealmAdminClientId(realm));
        getRealmAdminAccess(realm, realmAdminApp, user, realmAdminAccess);
    }

    /** 为 master 领域用户填充目标领域的管理角色（含 admin 超级角色）。 */
    private void addMasterRealmAccess(UserModel user, String currentRealm, Map<String, Set<String>> realmAdminAccess) {
        final RealmModel realm = session.realms().getRealmByName(currentRealm);
        if (realm != null) {
            getRealmAdminAccess(realm, realm.getMasterAdminClient(), user, realmAdminAccess);
            RealmModel masterRealm = session.realms().getRealmByName(Config.getAdminRealm());
            RoleModel adminRole = masterRealm.getRole(AdminRoles.ADMIN);
            if (adminRole != null && user.hasRole(adminRole)) {
                realmAdminAccess.get(currentRealm).add(AdminRoles.ADMIN);
            }
        } else {
            throw new NotFoundException("Realm not found");
        }
    }

    /** 收集用户在 realm-admin 客户端上已分配的角色名称。 */
    private void getRealmAdminAccess(RealmModel realm, ClientModel client, UserModel user, Map<String, Set<String>> realmAdminAccess) {
        Set<String> realmRoles = client.getRolesStream()
          .filter(user::hasRole)
          .map(RoleModel::getName)
          .collect(Collectors.toSet());

        realmAdminAccess.put(realm.getName(), realmRoles);
    }

    /**
     * 从管理控制台登出并重定向回控制台首页。
     *
     * @return 302 重定向至 OIDC 登出端点
     */
    @Path("logout")
    @GET
    @NoCache
    public Response logout() {
        URI redirect = AdminRoot.adminConsoleUrl(session.getContext().getUri(UrlType.ADMIN)).build(realm.getName());

        return Response.status(302).location(
                OIDCLoginProtocolService.logoutUrl(session.getContext().getUri(UrlType.ADMIN)).queryParam("post_logout_redirect_uri", redirect.toString()).build(realm.getName())
        ).build();
    }

    /** @return Keycloak 管理领域（通常为 master） */
    protected RealmModel getAdminstrationRealm(RealmManager realmManager) {
        return realmManager.getKeycloakAdministrationRealm();
    }

    /**
     * 渲染领域管理控制台主页面（FreeMarker + Vite 资源）。
     */
    @GET
    @NoCache
    public Response getMainPage() throws IOException, FreeMarkerException {
        final var baseUriInfo = session.getContext().getUri(UrlType.FRONTEND);
        final var adminUriInfo = session.getContext().getUri(UrlType.ADMIN);

        // 若 URL 无尾部斜杠则 302 重定向至带斜杠路径
        if (!adminUriInfo.getRequestUri().getPath().endsWith("/")) {
            return Response.status(302).location(adminUriInfo.getRequestUriBuilder().path("/").build()).build();
        } else {
            // 解析前端与管理控制台基础 URL
            final var serverBaseUri = baseUriInfo.getBaseUri();
            final var adminBaseUri = adminUriInfo.getBaseUri();

            // 去除 URL 尾部斜杠
            final var serverBaseUrl = serverBaseUri.toString().replaceFirst("/+$", "");
            final var adminBaseUrl = adminBaseUri.toString().replaceFirst("/+$", "");

            final var map = new HashMap<String, Object>();
            final var theme = AdminRoot.getTheme(session, realm);
            final var isSecureContext = SecureContextResolver.isSecureContext(session);

            map.put("isSecureContext", isSecureContext);
            map.put("serverBaseUrl", serverBaseUrl);
            map.put("adminBaseUrl", adminBaseUrl);
            // TODO：部分模板变量已弃用，仅为旧主题保留兼容
            // 后续版本应从 Administration Console 模板中一并移除
            map.put("authServerUrl", serverBaseUrl); // Superseded by 'serverBaseUrl', remove in the future.
            map.put("authUrl", adminBaseUrl); // Superseded by 'adminBaseUrl', remove in the future.
            map.put("consoleBaseUrl", Urls.adminConsoleRoot(adminBaseUri, realm.getName()).getPath());
            map.put("resourceUrl", Urls.themeRoot(adminBaseUri).getPath() + "/admin/" + theme.getName());
            map.put("resourceCommonUrl", Urls.themeRoot(adminBaseUri).getPath() + "/common/keycloak");
            map.put("masterRealm", Config.getAdminRealm());
            map.put("resourceVersion", Version.RESOURCES_VERSION);
            map.put("loginRealm", realm.getName());
            map.put("clientId", Constants.ADMIN_CONSOLE_CLIENT_ID);
            map.put("properties", theme.getProperties());
            map.put("themeResources", ThemeResourcesParser.parse(theme.getProperties()));
            map.put("darkMode", "true".equals(theme.getProperties().getProperty("darkMode"))
                    && realm.getAttribute("darkMode", true));

            final var devServerUrl = Environment.isDevMode() ? System.getenv(ViteManifest.ADMIN_VITE_URL) : null;

            if (devServerUrl != null) {
                map.put("devServerUrl", devServerUrl);
            }

            final var manifestFile = theme.getResourceAsStream(".vite/manifest.json");

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

            final var freeMarkerUtil = session.getProvider(FreeMarkerProvider.class);
            final var result = freeMarkerUtil.processTemplate(map, "index.ftl", theme);
            final var builder = Response.status(Response.Status.OK).type(MediaType.TEXT_HTML_UTF_8).language(Locale.ENGLISH).entity(result);

            // 管理控制台与前端 URL 不同时允许 iframe 嵌入
            if (!adminBaseUri.equals(serverBaseUri)) {
                session.getProvider(SecurityHeadersProvider.class).options().allowFrameSrc(UriUtils.getOrigin(serverBaseUri));
            }

            return builder.build();
        }
    }

    /** index.html 路径重定向至控制台根路径。 */
    @GET
    @Path("{indexhtml: index.html}") // this expression is a hack to get around jaxdoclet generation bug.  Doesn't like index.html
    public Response getIndexHtmlRedirect() {
        return Response.status(302).location(session.getContext().getUri(UrlType.ADMIN).getRequestUriBuilder().path("../").build()).build();
    }

    /** 返回指定语言的 admin-messages 本地化 JSON。 */
    @GET
    @Path("messages.json")
    @Produces(MediaType.APPLICATION_JSON)
    public Properties getMessages(@QueryParam("lang") String lang) {
        return AdminRoot.getMessages(session, realm, lang, "admin-messages");
    }

}
