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

package org.keycloak.adapters.saml;

import org.keycloak.adapters.saml.profile.SamlAuthenticationHandler;
import org.keycloak.adapters.saml.profile.ecp.EcpAuthenticationHandler;
import org.keycloak.adapters.saml.profile.webbrowsersso.WebBrowserSsoAuthenticationHandler;
import org.keycloak.adapters.spi.AuthChallenge;
import org.keycloak.adapters.spi.AuthOutcome;
import org.keycloak.adapters.spi.HttpFacade;

import org.jboss.logging.Logger;

/**
 * SAML 认证器抽象基类，根据请求类型选择 ECP 或 Web Browser SSO 处理器并完成认证流程。
 *
 * <p>构造时创建 {@link SamlAuthenticationHandler}，{@link #authenticate()} 委托处理器执行 SAML 握手，
 * 成功后通过 {@link OnSessionCreated} 回调触发 {@link #completeAuthentication(SamlSession)}。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public abstract class SamlAuthenticator {

    /** 本类日志记录器。 */
    protected static Logger log = Logger.getLogger(SamlAuthenticator.class);

    /** 当前请求使用的 SAML 认证处理器。 */
    private final SamlAuthenticationHandler handler;

    /**
     * 根据 HTTP 门面、部署配置与会话存储创建认证器并初始化处理器。
     *
     * @param facade HTTP 请求/响应门面
     * @param deployment SAML 部署配置
     * @param sessionStore SAML 会话存储
     */
    public SamlAuthenticator(final HttpFacade facade, final SamlDeployment deployment, final SamlSessionStore sessionStore) {
        this.handler = createAuthenticationHandler(facade, deployment, sessionStore);
    }

    /**
     * 返回当前认证流程所需的质询（如重定向至 IdP 登录页）。
     *
     * @return 认证质询对象，无质询时可为 {@code null}
     */
    public AuthChallenge getChallenge() {
        return this.handler.getChallenge();
    }

    /**
     * 执行 SAML 认证；会话创建成功后调用 {@link #completeAuthentication(SamlSession)}。
     *
     * @return 认证结果枚举
     */
    public AuthOutcome authenticate() {
        log.debugf("SamlAuthenticator is using handler [%s]", this.handler);
        return this.handler.handle(new OnSessionCreated() {
            @Override
            public void onSessionCreated(SamlSession samlSession) {
                completeAuthentication(samlSession);
            }
        });
    }

    /**
     * 认证成功后由子类实现的收尾逻辑（如写入应用会话）。
     *
     * @param samlSession 已建立的 SAML 会话
     */
    protected abstract void completeAuthentication(SamlSession samlSession);

    /**
     * 按请求特征选择 ECP 或 Web Browser SSO 认证处理器。
     *
     * @param facade HTTP 门面
     * @param deployment SAML 部署配置
     * @param sessionStore 会话存储
     * @return 适配当前请求的认证处理器
     */
    protected SamlAuthenticationHandler createAuthenticationHandler(HttpFacade facade, SamlDeployment deployment, SamlSessionStore sessionStore) {
        if (EcpAuthenticationHandler.canHandle(facade)) {
            return EcpAuthenticationHandler.create(facade, deployment, sessionStore);
        }

        // 默认使用 Web Browser SSO 配置文件
        return createBrowserHandler(facade, deployment, sessionStore);
    }

    /**
     * 创建 Web Browser SSO 认证处理器。
     *
     * @param facade HTTP 门面
     * @param deployment SAML 部署配置
     * @param sessionStore 会话存储
     * @return Web Browser SSO 处理器实例
     */
    protected SamlAuthenticationHandler createBrowserHandler(HttpFacade facade, SamlDeployment deployment, SamlSessionStore sessionStore) {
        return WebBrowserSsoAuthenticationHandler.create(facade, deployment, sessionStore);
    }
}