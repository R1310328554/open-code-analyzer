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
package org.keycloak.quarkus.runtime.services.resources;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Stream;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import org.keycloak.common.util.UriUtils;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.quarkus.runtime.Environment;
import org.keycloak.quarkus.runtime.configuration.Configuration;
import org.keycloak.quarkus.runtime.configuration.mappers.HostnameV2PropertyMappers;
import org.keycloak.services.Urls;
import org.keycloak.services.cors.Cors;
import org.keycloak.theme.FreeMarkerException;
import org.keycloak.theme.Theme;
import org.keycloak.theme.freemarker.FreeMarkerProvider;
import org.keycloak.urls.UrlType;
import org.keycloak.utils.SecureContextResolver;

import io.quarkus.resteasy.reactive.server.EndpointDisabled;

/**
 * 主机名/代理调试 REST 资源（需 {@code kc.hostname-debug=true}）：渲染 realm 级 HTML 调试页并暴露 CORS/代理头测试端点。
 */
@Provider
@Path("/realms")
@EndpointDisabled(name = "kc.hostname-debug", stringValue = "false", disableIfMissing = true)
public class DebugHostnameSettingsResource {
    /** 调试页 URL 路径后缀。 */
    public static final String DEFAULT_PATH_SUFFIX = "hostname-debug";
    /** CORS/代理头测试子路径。 */
    public static final String PATH_FOR_TEST_CORS_IN_HEADERS = "test";


    @Context
    private KeycloakSession keycloakSession;

    /** 构造时收集的相关 Hostname/HTTP 配置快照。 */
    private final Map<String, String> allConfigPropertiesMap;

    public DebugHostnameSettingsResource() {

        this.allConfigPropertiesMap = new LinkedHashMap<>();
        String[] relevantOptions = ConstantsDebugHostname.RELEVANT_OPTIONS_V2;
        for (String key : relevantOptions) {
            addOption(key);
        }

    }

    /** 渲染指定 realm 的主机名调试 HTML 页面。 */
    @GET
    @Path("/{realmName}/" + DEFAULT_PATH_SUFFIX)
    @Produces(MediaType.TEXT_HTML)
    public String debug(final @PathParam("realmName") String realmName) throws IOException, FreeMarkerException {
        RealmModel realmModel = keycloakSession.realms().getRealmByName(realmName);

        if (realmModel == null) {
            throw new NotFoundException();
        }

        FreeMarkerProvider freeMarkerProvider = keycloakSession.getProvider(FreeMarkerProvider.class);

        List<String> configWarnings = new ArrayList<String>();
        HostnameV2PropertyMappers.validateConfig(configWarnings::add);

        URI frontendUri = keycloakSession.getContext().getUri(UrlType.FRONTEND).getBaseUri();
        URI backendUri = keycloakSession.getContext().getUri(UrlType.BACKEND).getBaseUri();
        URI adminUri = keycloakSession.getContext().getUri(UrlType.ADMIN).getBaseUri();

        String frontendTestUrl = getTest(realmModel, frontendUri, true);
        String backendTestUrl = getTest(realmModel, backendUri, false);
        String adminTestUrl = getTest(realmModel, adminUri, false);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("configWarnings", configWarnings);
        attributes.put("frontendUrl", frontendUri.toString());
        attributes.put("backendUrl", backendUri.toString());
        attributes.put("adminUrl", adminUri.toString());

        attributes.put("realm", realmModel.getName());
        attributes.put("realmUrl", realmModel.getAttribute("frontendUrl"));
        attributes.put("implVersion", "V2");

        attributes.put("frontendTestUrl", frontendTestUrl);
        attributes.put("backendTestUrl", backendTestUrl);
        attributes.put("adminTestUrl", adminTestUrl);

        attributes.put("serverMode", Environment.isDevMode() ? "dev [start-dev]" : "production [start]");

        attributes.put("config", this.allConfigPropertiesMap);
        attributes.put("headers", getHeaders());

        return freeMarkerProvider.processTemplate(
                attributes,
                "debug-hostname-settings.ftl",
                keycloakSession.theme().getTheme("base", Theme.Type.LOGIN)
        );
    }

    /** 前端/后端 CORS 与代理配置冒烟测试，返回诊断文本。 */
    @GET
    @Path("/{realmName}/" + DEFAULT_PATH_SUFFIX + "/" + PATH_FOR_TEST_CORS_IN_HEADERS)
    @Produces(MediaType.TEXT_PLAIN)
    public Response test(final @PathParam("realmName") String realmName, @DefaultValue("false") @QueryParam("frontEnd") boolean frontEnd) {
        String text = "OK";
        String corsOrigin = keycloakSession.getContext().getRequestHeaders().getHeaderString(Cors.ORIGIN_HEADER);
        URI requestUri = keycloakSession.getContext().getUri().getRequestUri();
        String requestOrigin = UriUtils.getOrigin(requestUri);
        URI frontendUri = keycloakSession.getContext().getUri(UrlType.FRONTEND).getBaseUri();

        if (frontEnd) {
            boolean originMatches = requestOrigin.equals(UriUtils.getOrigin(frontendUri));
            HttpHeaders requestHeaders = keycloakSession.getContext().getRequestHeaders();
            boolean fowarded = requestHeaders.getHeaderString(ConstantsDebugHostname.FORWARDED_PROXY_HEADER) != null;
            boolean xfowarded = Stream.of(ConstantsDebugHostname.X_FORWARDED_PROXY_HEADERS)
                    .map(requestHeaders::getHeaderString).anyMatch(Objects::nonNull);

            if (!originMatches) { // 可能导致 CORS 校验失败
                text = "Default origin check failing, request hostname does not match frontend hostname. Please check you proxy settings.";
                if (!keycloakSession.getContext().getHttpRequest().isProxyTrusted()) {
                    text += " Note the proxy is not trusted.";
                }
                if (!fowarded && !xfowarded) {
                    text += " No proxy headers are set on the request.";
                }
            }

            boolean https = requestUri.getScheme().equals("https");
            if (https) {
                // reencrypt 模式可能需要设置代理头；passthrough 则不应设置
                // TODO：尚无完善检测方式，可能需要比对前端连接证书与入站请求证书
            } else if (!SecureContextResolver.isSecureContext(keycloakSession)) {
                text += " Non-secure context detected - Keycloak will not function properly when accessed over http at a non-localhost host.";
            }
        }

        Response.ResponseBuilder builder = Response.ok(text);
        builder.header(Cors.ACCESS_CONTROL_ALLOW_ORIGIN, corsOrigin);
        builder.header(Cors.ACCESS_CONTROL_ALLOW_METHODS, "GET");
        return builder.build();
    }

    /** 若配置存在则将键值写入调试页配置映射。 */
    private void addOption(String key) {
        Configuration.getOptionalKcValue(key).ifPresent(value -> this.allConfigPropertiesMap.put(key, value));
    }

    /** 收集当前请求中与主机名/代理相关的头。 */
    private Map<String, String> getHeaders() {
        Map<String, String> headers = new TreeMap<>();
        HttpHeaders requestHeaders = keycloakSession.getContext().getRequestHeaders();
        for (String h : ConstantsDebugHostname.RELEVANT_HEADERS) {
            addProxyHeader(h, headers, requestHeaders);
        }
        return headers;
    }

    /** 将非空请求头写入展示用映射。 */
    private void addProxyHeader(String header, Map<String, String> proxyHeaders, HttpHeaders requestHeaders) {
        String value = requestHeaders.getHeaderString(header);
        if (value != null && !value.isEmpty()) {
            proxyHeaders.put(header, value);
        }
    }

    /** 构造 CORS 测试 URL（含 frontEnd 查询参数）。 */
    private String getTest(RealmModel realmModel, URI baseUri, boolean frontEnd) {
        return Urls.realmBase(baseUri)
                   .path("/{realmName}/{debugHostnameSettingsPath}/{pathForTestCORSInHeaders}")
                   .queryParam("frontEnd", frontEnd)
                   .build(realmModel.getName(), DEFAULT_PATH_SUFFIX, PATH_FOR_TEST_CORS_IN_HEADERS)
                   .toString();
    }

}
