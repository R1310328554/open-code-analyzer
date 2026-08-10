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
package org.keycloak.services.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

import org.keycloak.common.Version;
import org.keycloak.common.util.MimeTypeUtil;
import org.keycloak.encoding.ResourceEncodingHelper;
import org.keycloak.encoding.ResourceEncodingProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.cors.Cors;
import org.keycloak.services.util.CacheControlUtil;
import org.keycloak.services.util.LocaleUtil;
import org.keycloak.theme.Theme;

import org.jboss.logging.Logger;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * 主题静态资源 REST 端点（{@code /resources}）。
 * <p>提供登录/账户等主题的 CSS、JS、图片及本地化 JSON。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@Provider
@Path("/resources")
public class ThemeResource {

    /** 日志记录器 */
    private static final Logger log = Logger.getLogger(ThemeResource.class);
    /** 资源版本标签格式（5 位十六进制） */
    private static final Pattern RESOURCE_TAG_PATTERN = Pattern.compile("[0-9a-z]{5}");

    /** 注入的 Keycloak 会话 */
    @Context
    private KeycloakSession session;

    /**
     * 获取主题静态资源（支持版本重定向与 ETag 缓存）
     *
     * @param version
     * @param themeType
     * @param themeName
     * @param path
     * @return
     */
    @GET
    @Path("/{version}/{themeType}/{themeName}/{path:.*}")
    public Response getResource(@PathParam("version") String version, @PathParam("themeType") String themeType, @PathParam("themeName") String themeName, @PathParam("path") String path, @HeaderParam(HttpHeaders.IF_NONE_MATCH) String etag, @Context UriInfo uriInfo) {
        final Optional<Theme.Type> type = getThemeType(themeType);

        if (type.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            String contentType = MimeTypeUtil.getContentType(path);
            Theme theme = session.theme().getTheme(themeName, type.get());

            boolean hasContentHash = theme.hasContentHash(path);

            String base = uriInfo.getBaseUri().getPath();
            base = base.substring(0, base.length() - 1);
            if (!RESOURCE_TAG_PATTERN.matcher(version).matches()) {
                // 防止开放或半开放重定向到其他 URL
                log.debugf("Illegal version passed, returning a 404: %s", uriInfo.getRequestUri().getPath());
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            // 请求版本不匹配且无内容哈希时尝试重定向到当前资源版本
            // and we didn't default to the default theme as the theme is unknown.
            if (!version.equals(Version.RESOURCES_VERSION) && !hasContentHash && Objects.equals(theme.getName(), themeName)) {
                // If it is not the right version, and it does not have a content hash, redirect.
                // If it is not the right version, but it has a content hash, continue to see if it exists.

                // RESTEasy 不支持原始 URL，需通过路径前缀校验编码
                // Unfortunately, RESTEasy doesn't support this, and UrlInfo will throw an IllegalArgumentException.
                if (!uriInfo.getRequestUri().toURL().getPath().startsWith(base + UriBuilder.fromResource(ThemeResource.class)
                        .path("/{version}/{themeType}/{themeName}/{path}").build(version, theme.getType().toString().toLowerCase(), theme.getName(), path).getPath())) {
                    // 防止半开放重定向
                    log.debugf("No URL encoding should be necessary for the path, returning a 404: %s", uriInfo.getRequestUri().getPath());
                    return Response.status(Response.Status.NOT_FOUND).build();
                }

                if (!theme.hasResource(path)) {
                    // 目标资源不存在则直接 404
                    log.debugf("Resource doesn't exist, returning a 404: %s", path);
                    return Response.status(Response.Status.NOT_FOUND).build();
                }

                URI redirectUri = UriBuilder.fromResource(ThemeResource.class)
                        .path("/{version}/{themeType}/{themeName}/{path}")
                        // We will not add the query parameters to the redirect as it is difficult to sanitize them, and the theme handler doesn't need them.
                        // The 'path' can contain slashes, so encoding of slashes is set to false.
                        .build(new Object[]{Version.RESOURCES_VERSION, theme.getType().toString().toLowerCase(), theme.getName(), path}, false);
                if (!redirectUri.normalize().equals(redirectUri)) {
                    // This prevents half-open redirects
                    log.debugf("Redirect URL should not require normalization, returning a 404: %s", redirectUri.toString());
                    return Response.status(Response.Status.NOT_FOUND).build();
                }

                // 确认资源存在后可安全重定向

                // 重定向后浏览器可缓存正确版本资源（滚动重启场景）
                // This assumes that it is better to try to some content even if it is outdated or too new, instead of returning a 404.
                // This should usually work for images, CSS or (simple) JavaScript referenced in the login theme that needs to be
                // loaded while the rolling restart is progressing.
                return Response.temporaryRedirect(redirectUri)
                        .build();
            }

            if (hasContentHash && Objects.equals(etag, Version.RESOURCES_VERSION)) {
                // ETag 匹配则返回 304 Not Modified
                return Response.notModified()
                        .header(HttpHeaders.ETAG, Version.RESOURCES_VERSION)
                        .cacheControl(CacheControlUtil.getDefaultCacheControl()).build();
            }

            ResourceEncodingProvider encodingProvider = session.theme().isCacheEnabled() ? ResourceEncodingHelper.getResourceEncodingProvider(session, contentType) : null;

            InputStream resource;
            if (encodingProvider != null) {
                resource = encodingProvider.getEncodedStream(() -> theme.getResourceAsStream(path), themeType, themeName, path.replace('/', File.separatorChar));
            } else {
                resource = theme.getResourceAsStream(path);
            }

            if (resource != null) {
                Response.ResponseBuilder rb = Response.ok(resource).type(contentType).cacheControl(CacheControlUtil.getDefaultCacheControl());

                if (hasContentHash) {
                    // 带内容哈希的资源设置 ETag 以支持条件请求
                    rb.header(HttpHeaders.ETAG, Version.RESOURCES_VERSION);
                }
                if (encodingProvider != null) {
                    rb.encoding(encodingProvider.getEncoding());
                }
                return rb.build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            ServicesLogger.LOGGER.failedToGetThemeRequest(e);
            return Response.serverError().build();
        }
    }

    @Path("/{realm}/{themeType}/{locale}")
    @OPTIONS
    /** 本地化文本端点 CORS 预检 */
    public Response localizationTextPreflight() {
        return Cors.builder().auth().preflight().add(Response.ok());
    }

    @GET
    @Path("/{realm}/{themeType}/{locale}")
    @Produces(MediaType.APPLICATION_JSON)
    /**
     * 返回指定领域/主题/语言的本地化键值 JSON。
     * @param realmName 领域名称
     * @param theme 主题名称（可选）
     * @param localeString 语言标签
     * @param themeType 主题类型
     * @param showSource 是否显示消息来源（主题/领域）
     */
    public Response getLocalizationTexts(@PathParam("realm") String realmName, @QueryParam("theme") String theme,
                                         @PathParam("locale") String localeString, @PathParam("themeType") String themeType,
                                         @QueryParam("source") boolean showSource) throws IOException {
        final RealmModel realm = session.realms().getRealmByName(realmName);
        final Optional<Theme.Type> type = getThemeType(themeType);
        if (realm == null || type.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        session.getContext().setRealm(realm);
        List<KeySource> result;

        Theme theTheme;
        if (theme == null) {
            theTheme = session.theme().getTheme(type.get());
        } else {
            theTheme = session.theme().getTheme(theme, type.get());
        }

        final Locale locale = Locale.forLanguageTag(localeString);
        if (showSource) {
            Properties messagesByLocale = theTheme.getMessages("messages", locale);
            Set<KeySource> resultSet = messagesByLocale.entrySet().stream().map(e ->
                    new KeySource((String) e.getKey(), (String) e.getValue(), Source.THEME)).collect(toSet());

            Map<Locale, Properties> realmLocalizationMessages = LocaleUtil.getRealmLocalizationTexts(realm, locale);
            for (Locale currentLocale = locale; currentLocale != null; currentLocale = LocaleUtil.getParentLocale(currentLocale, realm)) {
                final List<KeySource> realmOverride = realmLocalizationMessages.get(currentLocale).entrySet().stream().map(e ->
                        new KeySource((String) e.getKey(), (String) e.getValue(), Source.REALM)).collect(toList());
                resultSet.addAll(realmOverride);
            }
            result = new ArrayList<>(resultSet);
        } else {
            result = theTheme.getEnhancedMessages(realm, locale).entrySet().stream().map(e ->
                    new KeySource((String) e.getKey(), (String) e.getValue())).collect(toList());
        }

        return Cors.builder().allowAllOrigins().auth().add(Response.ok(result));
    }

    /** 解析主题类型字符串，无效时返回 empty */
    private static Optional<Theme.Type> getThemeType(String themeType) {
        try {
            return Optional.of(Theme.Type.valueOf(themeType.toUpperCase()));
        } catch (IllegalArgumentException iae) {
            return Optional.empty();
        }
    }
}

/** 本地化消息来源：主题或领域覆盖 */
enum Source {
    THEME,
    REALM
}

/** 本地化键值对（可选带来源） */
class KeySource {
    private String key;
    private String value;
    private Source source;

    /** 构造无来源的键值对 */
    public KeySource(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /** 构造带来源的键值对 */
    public KeySource(String key, String value, Source source) {
        this(key, value);
        this.source = source;
    }

    /** @return 消息键 */
    public String getKey() {
        return key;
    }

    /** @return 消息值 */
    public String getValue() {
        return value;
    }

    /** @return 消息来源 */
    public Source getSource() {
        return source;
    }
}
