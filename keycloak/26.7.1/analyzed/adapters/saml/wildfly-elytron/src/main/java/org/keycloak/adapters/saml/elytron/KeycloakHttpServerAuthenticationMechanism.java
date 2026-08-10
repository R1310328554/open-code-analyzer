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

import java.net.URI;
import java.util.Map;
import java.util.regex.Pattern;
import javax.security.auth.callback.CallbackHandler;

import jakarta.servlet.http.HttpServletResponse;

import org.keycloak.adapters.saml.SamlAuthenticator;
import org.keycloak.adapters.saml.SamlDeployment;
import org.keycloak.adapters.saml.SamlDeploymentContext;
import org.keycloak.adapters.spi.AuthChallenge;
import org.keycloak.adapters.spi.AuthOutcome;
import org.keycloak.adapters.spi.SessionIdMapper;
import org.keycloak.adapters.spi.SessionIdMapperUpdater;

import org.jboss.logging.Logger;
import org.wildfly.security.http.HttpAuthenticationException;
import org.wildfly.security.http.HttpScope;
import org.wildfly.security.http.HttpServerAuthenticationMechanism;
import org.wildfly.security.http.HttpServerRequest;
import org.wildfly.security.http.Scope;

/**
 * WildFly Elytron 的 Keycloak SAML HTTP 服务端认证机制。
 *
 * <p>对每个入站请求评估 SAML 部署上下文，选择 {@link ElytronSamlEndpoint} 或
 * {@link ElytronSamlAuthenticator} 执行认证，并根据 {@link AuthOutcome} 驱动
 * Elytron 认证状态转换（完成、进行中、失败或匿名）。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
class KeycloakHttpServerAuthenticationMechanism implements HttpServerAuthenticationMechanism {

    /** 本类日志记录器。 */
    static Logger LOGGER = Logger.getLogger(KeycloakHttpServerAuthenticationMechanism.class);
    /** Elytron 机制注册名。 */
    static final String NAME = "KEYCLOAK-SAML";

    /** 机制配置属性。 */
    private final Map<String, ?> properties;
    /** Elytron 认证回调处理器。 */
    private final CallbackHandler callbackHandler;
    /** SAML 部署上下文（可为 null，此时从应用作用域附件读取）。 */
    private final SamlDeploymentContext deploymentContext;
    /** HTTP 会话与 SSO 会话 ID 映射器。 */
    private final SessionIdMapper idMapper;
    /** 会话 ID 映射更新器。 */
    private final SessionIdMapperUpdater idMapperUpdater;

    /**
     * 创建 Keycloak SAML HTTP 认证机制实例。
     *
     * @param properties        机制属性
     * @param callbackHandler   Elytron 回调处理器
     * @param deploymentContext SAML 部署上下文
     * @param idMapper          会话 ID 映射器
     * @param idMapperUpdater   映射更新器
     */
    public KeycloakHttpServerAuthenticationMechanism(Map<String, ?> properties, CallbackHandler callbackHandler, SamlDeploymentContext deploymentContext, SessionIdMapper idMapper, SessionIdMapperUpdater idMapperUpdater) {
        this.properties = properties;
        this.callbackHandler = callbackHandler;
        this.deploymentContext = deploymentContext;
        this.idMapper = idMapper;
        this.idMapperUpdater = idMapperUpdater;
    }

    @Override
    public String getMechanismName() {
        return NAME;
    }

    /**
     * 评估 HTTP 请求并驱动 SAML 认证流程。
     *
     * @param request Elytron HTTP 服务端请求
     * @throws HttpAuthenticationException 认证处理异常
     */
    @Override
    public void evaluateRequest(HttpServerRequest request) throws HttpAuthenticationException {
        LOGGER.debugf("Evaluating request for path [%s]", request.getRequestURI());
        SamlDeploymentContext deploymentContext = getDeploymentContext(request);

        if (deploymentContext == null) {
            LOGGER.debugf("Ignoring request for path [%s] from mechanism [%s]. No deployment context found.", request.getRequestURI(), getMechanismName());
            request.noAuthenticationInProgress();
            return;
        }

        ElytronHttpFacade httpFacade = new ElytronHttpFacade(request, getSessionIdMapper(request), getSessionIdMapperUpdater(request), deploymentContext, callbackHandler);
        SamlDeployment deployment = httpFacade.getDeployment();

        if (!deployment.isConfigured()) {
            request.noAuthenticationInProgress();
            return;
        }

        if (deployment.getLogoutPage() != null && httpFacade.getRequest().getRelativePath().contains(deployment.getLogoutPage())) {
            LOGGER.debugf("Ignoring request for [%s] and logout page [%s].", request.getRequestURI(), deployment.getLogoutPage());
            httpFacade.authenticationCompleteAnonymous();
            return;
        }

        SamlAuthenticator authenticator;

        if (httpFacade.getRequest().getRelativePath().endsWith("/saml")) {
            authenticator = new ElytronSamlEndpoint(httpFacade, deployment);
        } else {
            authenticator = new ElytronSamlAuthenticator(httpFacade, deployment, callbackHandler);

        }

        AuthOutcome outcome = authenticator.authenticate();

        if (outcome == AuthOutcome.AUTHENTICATED) {
            httpFacade.authenticationComplete();
            return;
        }

        if (outcome == AuthOutcome.NOT_AUTHENTICATED) {
            httpFacade.noAuthenticationInProgress(null);
            return;
        }

        if (outcome == AuthOutcome.LOGGED_OUT) {
            if (deployment.getLogoutPage() != null) {
                redirectLogout(deployment, httpFacade);
            }
            httpFacade.authenticationInProgress();
            return;
        }

        AuthChallenge challenge = authenticator.getChallenge();

        if (challenge != null) {
            httpFacade.noAuthenticationInProgress(challenge);
            return;
        }

        if (outcome == AuthOutcome.FAILED) {
            httpFacade.authenticationFailed();
            return;
        }

        httpFacade.authenticationInProgress();
    }

    /** 从构造注入或应用作用域附件获取 SAML 部署上下文。 */
    private SamlDeploymentContext getDeploymentContext(HttpServerRequest request) {
        if (this.deploymentContext == null) {
            return (SamlDeploymentContext) request.getScope(Scope.APPLICATION).getAttachment(KeycloakConfigurationServletListener.ADAPTER_DEPLOYMENT_CONTEXT_ATTRIBUTE_ELYTRON);
        }

        return this.deploymentContext;
    }

    /** 优先使用应用作用域附件中的映射器，否则回退到构造注入实例。 */
    private SessionIdMapper getSessionIdMapper(HttpServerRequest request) {
        HttpScope scope = request.getScope(Scope.APPLICATION);
        SessionIdMapper res = scope == null ? null : (SessionIdMapper) scope.getAttachment(KeycloakConfigurationServletListener.ADAPTER_SESSION_ID_MAPPER_ATTRIBUTE_ELYTRON);
        return res == null ? this.idMapper : res;
    }

    /** 优先使用应用作用域附件中的更新器，否则回退到构造注入实例。 */
    private SessionIdMapperUpdater getSessionIdMapperUpdater(HttpServerRequest request) {
        HttpScope scope = request.getScope(Scope.APPLICATION);
        SessionIdMapperUpdater res = scope == null ? null : (SessionIdMapperUpdater) scope.getAttachment(KeycloakConfigurationServletListener.ADAPTER_SESSION_ID_MAPPER_UPDATER_ATTRIBUTE_ELYTRON);
        return res == null ? this.idMapperUpdater : res;
    }

    /** 登出完成后重定向到部署配置的登出页。 */
    protected void redirectLogout(SamlDeployment deployment, ElytronHttpFacade exchange) {
        sendRedirect(exchange, deployment.getLogoutPage());
    }

    /** 匹配绝对 URL 协议前缀（如 {@code https:}）的正则。 */
    private static final Pattern PROTOCOL_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*:");

    /**
     * 发送 HTTP 302 重定向；相对路径会拼接当前请求的 scheme、host 与 context path。
     *
     * @param exchange Elytron HTTP 门面
     * @param location 目标位置（绝对或相对 URL）
     */
    static void sendRedirect(final ElytronHttpFacade exchange, final String location) {
        if (location == null) {
            LOGGER.warn("Logout page not set.");
            exchange.getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (PROTOCOL_PATTERN.matcher(location).find()) {
            exchange.getResponse().setHeader("Location", location);
        } else {
            URI uri = exchange.getURI();
            String path = uri.getPath();
            String relativePath = exchange.getRequest().getRelativePath();
            String contextPath = path.substring(0, path.indexOf(relativePath));
            String loc;
            int port = uri.getPort();
            if (port == -1) {
                loc = uri.getScheme() + "://" + uri.getHost() + contextPath + location;
            } else {
                loc = uri.getScheme() + "://" + uri.getHost() + ":" + port + contextPath + location;
            }
            exchange.getResponse().setHeader("Location", loc);
        }
        exchange.getResponse().setStatus(HttpServletResponse.SC_FOUND);
    }
}
