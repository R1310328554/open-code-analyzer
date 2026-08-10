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

package org.keycloak.adapters.saml.elytron;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.cert.X509Certificate;

import org.keycloak.adapters.saml.SamlDeployment;
import org.keycloak.adapters.saml.SamlDeploymentContext;
import org.keycloak.adapters.saml.SamlSession;
import org.keycloak.adapters.saml.SamlSessionStore;
import org.keycloak.adapters.spi.AuthChallenge;
import org.keycloak.adapters.spi.AuthenticationError;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.spi.LogoutError;
import org.keycloak.adapters.spi.SessionIdMapper;
import org.keycloak.adapters.spi.SessionIdMapperUpdater;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.common.util.UriUtils;

import org.jboss.logging.Logger;
import org.wildfly.security.auth.callback.AnonymousAuthorizationCallback;
import org.wildfly.security.auth.callback.AuthenticationCompleteCallback;
import org.wildfly.security.auth.callback.SecurityIdentityCallback;
import org.wildfly.security.auth.server.SecurityIdentity;
import org.wildfly.security.http.HttpScope;
import org.wildfly.security.http.HttpServerCookie;
import org.wildfly.security.http.HttpServerRequest;
import org.wildfly.security.http.HttpServerResponse;
import org.wildfly.security.http.Scope;

/**
 * WildFly Elytron HTTP 服务器请求与 Keycloak {@link HttpFacade} 的桥接实现。
 *
 * <p>将 Elytron 的 {@link HttpServerRequest}/{@link HttpServerResponse} 适配为
 * SAML 适配器所需的请求/响应抽象，并管理认证状态切换与会话存储。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
class ElytronHttpFacade implements HttpFacade {

    /** 是否启用 ELYWEB-163 URI 解码变通方案（Issue #10894）。 */
    private static final boolean elyweb163Workaround;
    /** 本类日志记录器。 */
    private static final Logger log = Logger.getLogger(ElytronHttpFacade.class);

    /** Elytron HTTP 服务器请求。 */
    private final HttpServerRequest request;
    /** Elytron 认证回调处理器。 */
    private final CallbackHandler callbackHandler;
    /** SAML 部署上下文（支持按请求解析）。 */
    private final SamlDeploymentContext deploymentContext;
    /** SAML 会话存储。 */
    private final SamlSessionStore sessionStore;
    /** 延迟执行的响应操作链。 */
    private Consumer<HttpServerResponse> responseConsumer;
    /** 认证完成后的 Elytron 安全身份。 */
    private SecurityIdentity securityIdentity;
    /** 是否已恢复挂起的原始请求。 */
    private boolean restored;
    /** 当前 SAML 会话（认证完成前暂存）。 */
    private SamlSession samlSession;
    /** 解析后的查询参数缓存。 */
    protected MultivaluedHashMap<String, String> queryParameters;

    static {
        // Issue #10894：旧版 wildfly/EAP 的 elytron-web 需启用 ELYWEB-163 变通
        boolean tmpElyweb163Workaround = false;
        String prop = System.getProperty("org.keycloak.adapters.elytronweb.ELYWEB-163.workaround");
        if (prop != null) {
            tmpElyweb163Workaround = Boolean.parseBoolean(prop);
            log.tracef("Forcing workaround for issue ELYWEB-163 in elytron-web %b", tmpElyweb163Workaround);
        } else {
            try {
                Class clazz = ElytronHttpFacade.class.getClassLoader().loadClass("org.wildfly.elytron.web.undertow.server.ElytronHttpExchange");
                String version = clazz.getPackage().getImplementationVersion();
                Integer[] array = parseVersion(version);
                // 缺陷在 1.9.2 与 1.10.1 中已修复
                tmpElyweb163Workaround = array != null
                        && (versionIsLessThan(array, new Integer[]{1, 9, 2})
                        || (versionIsLessThan(array, new Integer[]{1, 10, 1}) && versionIsGreaterOrEqualThan(array, new Integer[]{1, 10, 0})));
                log.tracef("Version detected for elytron-web %s workaround for ELYWEB-163 %b", version, tmpElyweb163Workaround);
            } catch (Exception e) {
                log.tracef(e, "Cannot detect version of elytron-web workaround for ELYWEB-163 %b", tmpElyweb163Workaround);
            }
        }
        elyweb163Workaround = tmpElyweb163Workaround;
    }

    /** 解析 elytron-web 版本号字符串为整数数组。 */
    private static Integer[] parseVersion(String version) {
        if (version != null)  {
            String[] versionArray = version.split(Pattern.quote("."));
            List<Integer> versionList = new ArrayList<>();
            for (int i = 0; i < versionArray.length; i++) {
                if (versionArray[i].matches("[0-9]+")) {
                    versionList.add(Integer.parseInt(versionArray[i]));
                }
            }
            if (!versionList.isEmpty()) {
                return versionList.toArray(new Integer[0]);
            }
        }
        return null;
    }

    /** 比较版本号：{@code array1} 是否严格小于 {@code array2}。 */
    private static boolean versionIsLessThan(Integer[] array1, Integer[] array2) {
        if (array1 == null || array2 == null || array1.length == 0 || array2.length == 0) {
            throw new IllegalArgumentException("Arrays cannot be null or empty");
        }
        for (int i = 0; i < array1.length && i < array2.length; i++) {
            if (array1[i] < array2[i]) {
                return true;
            } else if (array1[i] > array2[i]) {
                return false;
            }
        }
        // 前缀相等时较短版本号更小，如 1.1 < 1.1.1
        return array1.length < array2.length;
    }

    /** 比较版本号：{@code array1} 是否大于等于 {@code array2}。 */
    private static boolean versionIsGreaterOrEqualThan(Integer[] array1, Integer[] array2) {
        return !versionIsLessThan(array1, array2);
    }

    /**
     * 构造 Elytron HTTP 门面。
     *
     * @param request          Elytron HTTP 请求
     * @param idMapper         会话 ID 映射器
     * @param idMapperUpdater  映射更新器
     * @param deploymentContext SAML 部署上下文
     * @param handler          Elytron 回调处理器
     */
    public ElytronHttpFacade(HttpServerRequest request, SessionIdMapper idMapper, SessionIdMapperUpdater idMapperUpdater, SamlDeploymentContext deploymentContext, CallbackHandler handler) {
        this.request = request;
        this.deploymentContext = deploymentContext;
        this.callbackHandler = handler;
        this.responseConsumer = response -> {};
        this.sessionStore = createTokenStore(idMapper, idMapperUpdater);
    }

    /** 创建 Elytron SAML 会话存储。 */
    private SamlSessionStore createTokenStore(SessionIdMapper idMapper, SessionIdMapperUpdater idMapperUpdater) {
        return new ElytronSamlSessionStore(this, idMapper, idMapperUpdater, getDeployment());
    }

    /** 暂存 SAML 会话，供后续 {@link #authenticationComplete()} 使用。 */
    void authenticationComplete(SamlSession samlSession) {
        this.samlSession = samlSession;
    }

    /** 基于 SAML 主体完成 Elytron 认证并注册登出回调。 */
    void authenticationComplete() {
        this.securityIdentity = SecurityIdentityUtil.authorize(this.callbackHandler, samlSession.getPrincipal());

        if (this.securityIdentity != null) {
            this.request.authenticationComplete(response -> {
                if (!restored) {
                    responseConsumer.accept(response);
                }
            }, () -> ((ElytronTokeStore) sessionStore).logout(true));
        }
    }

    /** 以匿名身份完成认证并转发至原始路径（用于登出场景）。 */
    void authenticationCompleteAnonymous() {
        try {
            AnonymousAuthorizationCallback anonymousAuthorizationCallback = new AnonymousAuthorizationCallback(null);

            callbackHandler.handle(new Callback[]{anonymousAuthorizationCallback});

            if (anonymousAuthorizationCallback.isAuthorized()) {
                callbackHandler.handle(new Callback[]{AuthenticationCompleteCallback.SUCCEEDED, new SecurityIdentityCallback()});
                request.authenticationComplete(response -> response.forward(getRequest().getRelativePath()));
            } else {
                request.noAuthenticationInProgress(response -> response.forward(getRequest().getRelativePath()));
            }
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error processing callbacks during logout.", e);
        }
    }

    /** 通知 Elytron 认证失败。 */
    void authenticationFailed() {
        this.request.authenticationFailed("Authentication Failed", response -> responseConsumer.accept(response));
    }

    /** 无认证进行中：可选执行质询后结束认证流程。 */
    void noAuthenticationInProgress(AuthChallenge challenge) {
        if (challenge != null) {
            challenge.challenge(this);
        }
        this.request.noAuthenticationInProgress(response -> responseConsumer.accept(response));
    }

    /** 通知 Elytron 认证仍在进行中（如等待 IdP 重定向）。 */
    void authenticationInProgress() {
        this.request.authenticationInProgress(response -> responseConsumer.accept(response));
    }

    /** 获取指定 Elytron 作用域。 */
    HttpScope getScope(Scope scope) {
        return request.getScope(scope);
    }

    /** 按 ID 获取指定类型的 Elytron 作用域。 */
    HttpScope getScope(Scope scope, String id) {
        return request.getScope(scope, id);
    }

    /** 列出指定作用域下的所有 ID。 */
    Collection<String> getScopeIds(Scope scope) {
        return request.getScopeIds(scope);
    }

    /** 按当前请求解析 SAML 部署配置。 */
    SamlDeployment getDeployment() {
        return deploymentContext.resolveDeployment(this);
    }

    @Override
    public Request getRequest() {
        return new Request() {
            private InputStream inputStream;

            @Override
            public String getMethod() {
                return request.getRequestMethod();
            }

            @Override
            public String getURI() {
                if (elyweb163Workaround) {
                    return URLDecoder.decode(request.getRequestURI().toString(), StandardCharsets.UTF_8);
                } else {
                    return request.getRequestURI().toString();
                }
            }

            @Override
            public String getRelativePath() {
                return request.getRequestPath();
            }

            @Override
            public boolean isSecure() {
                return request.getRequestURI().getScheme().equals("https");
            }

            @Override
            public String getFirstParam(String param) {
                return request.getFirstParameterValue(param);
            }

            @Override
            public String getQueryParamValue(String param) {
                if (elyweb163Workaround) {
                    URI requestURI = request.getRequestURI();
                    String query = requestURI.getQuery();
                    if (query != null) {
                        String[] parameters = query.split("&");
                        for (String parameter : parameters) {
                            String[] keyValue = parameter.split("=", 2);
                            if (keyValue[0].equals(param)) {
                                return URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                            }
                        }
                    }
                    return null;
                } else {
                    if (queryParameters == null) {
                        queryParameters = UriUtils.decodeQueryString(request.getRequestURI().getRawQuery());
                    }
                    return queryParameters.getFirst(param);
                }
            }

            @Override
            public Cookie getCookie(final String cookieName) {
                List<HttpServerCookie> cookies = request.getCookies();

                if (cookies != null) {
                    for (HttpServerCookie cookie : cookies) {
                        if (cookie.getName().equals(cookieName)) {
                            return new Cookie(cookie.getName(), cookie.getValue(), cookie.getVersion(), cookie.getDomain(), cookie.getPath());
                        }
                    }
                }

                return null;
            }

            @Override
            public String getHeader(String name) {
                return request.getFirstRequestHeaderValue(name);
            }

            @Override
            public List<String> getHeaders(String name) {
                return request.getRequestHeaderValues(name);
            }

            @Override
            public InputStream getInputStream() {
                return getInputStream(false);
            }

            @Override
            public InputStream getInputStream(boolean buffered) {
                if (inputStream != null) {
                    return inputStream;
                }

                if (buffered) {
                    return inputStream = new BufferedInputStream(request.getInputStream());
                }

                return request.getInputStream();
            }

            @Override
            public String getRemoteAddr() {
                InetSocketAddress sourceAddress = request.getSourceAddress();
                if (sourceAddress == null) {
                    return "";
                }
                InetAddress address = sourceAddress.getAddress();
                if (address == null) {
                    // this is unresolved, so we just return the host name not exactly spec, but if the name should be
                    // resolved then a PeerNameResolvingHandler should be used and this is probably better than just
                    // returning null
                    return sourceAddress.getHostString();
                }
                return address.getHostAddress();
            }

            @Override
            public void setError(AuthenticationError error) {
                request.getScope(Scope.EXCHANGE).setAttachment(AuthenticationError.class.getName(), error);
            }

            @Override
            public void setError(LogoutError error) {
                request.getScope(Scope.EXCHANGE).setAttachment(LogoutError.class.getName(), error);
            }
        };
    }

    @Override
    public Response getResponse() {
        return new Response() {
            @Override
            public void setStatus(final int status) {
                responseConsumer = responseConsumer.andThen(response -> response.setStatusCode(status));
            }

            @Override
            public void addHeader(final String name, final String value) {
                responseConsumer = responseConsumer.andThen(response -> response.addResponseHeader(name, value));
            }

            @Override
            public void setHeader(String name, String value) {
                addHeader(name, value);
            }

            @Override
            public void resetCookie(final String name, final String path) {
                responseConsumer = responseConsumer.andThen(response -> setCookie(name, "", path, null, 0, false, false, response));
            }

            @Override
            public void setCookie(final String name, final String value, final String path, final String domain, final int maxAge, final boolean secure, final boolean httpOnly) {
                responseConsumer = responseConsumer.andThen(response -> setCookie(name, value, path, domain, maxAge, secure, httpOnly, response));
            }

            private void setCookie(final String name, final String value, final String path, final String domain, final int maxAge, final boolean secure, final boolean httpOnly, HttpServerResponse response) {
                response.setResponseCookie(new HttpServerCookie() {
                    @Override
                    public String getName() {
                        return name;
                    }

                    @Override
                    public String getValue() {
                        return value;
                    }

                    @Override
                    public String getDomain() {
                        return domain;
                    }

                    @Override
                    public int getMaxAge() {
                        return maxAge;
                    }

                    @Override
                    public String getPath() {
                        return path;
                    }

                    @Override
                    public boolean isSecure() {
                        return secure;
                    }

                    @Override
                    public int getVersion() {
                        return 0;
                    }

                    @Override
                    public boolean isHttpOnly() {
                        return httpOnly;
                    }
                });
            }

            @Override
            public OutputStream getOutputStream() {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                responseConsumer = responseConsumer.andThen(new Consumer<HttpServerResponse>() {
                    @Override
                    public void accept(HttpServerResponse httpServerResponse) {
                        try {
                            httpServerResponse.getOutputStream().write(stream.toByteArray());
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to write to response output stream", e);
                        }
                    }
                });
                return stream;
            }

            @Override
            public void sendError(int code) {
                setStatus(code);
            }

            @Override
            public void sendError(final int code, final String message) {
                responseConsumer = responseConsumer.andThen(response -> {
                    response.setStatusCode(code);
                    response.addResponseHeader("Content-Type", "text/html");
                    try {
                        response.getOutputStream().write(message.getBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            @Override
            public void end() {

            }
        };
    }

    /** Elytron 环境下不提供客户端证书链。 */
    @Override
    public X509Certificate[] getCertificateChain() {
        return new X509Certificate[0];
    }

    /** 恢复挂起的原始 HTTP 请求。 */
    public boolean restoreRequest() {
        restored = this.request.resumeRequest();
        return restored;
    }

    /** 挂起当前请求，等待 SAML 回调后恢复。 */
    public void suspendRequest() {
        responseConsumer = responseConsumer.andThen(httpServerResponse -> request.suspendRequest());
    }

    /** @return 是否已通过 Elytron 认证 */
    public boolean isAuthorized() {
        return this.securityIdentity != null;
    }

    /** @return 当前请求的完整 URI */
    public URI getURI() {
        return request.getRequestURI();
    }

    /** @return SAML 会话存储 */
    public SamlSessionStore getSessionStore() {
        return sessionStore;
    }
}
