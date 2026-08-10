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

import javax.security.auth.callback.CallbackHandler;

import org.keycloak.adapters.saml.SamlAuthenticator;
import org.keycloak.adapters.saml.SamlDeployment;
import org.keycloak.adapters.saml.SamlSession;
import org.keycloak.adapters.saml.SamlSessionStore;
import org.keycloak.adapters.saml.profile.SamlAuthenticationHandler;
import org.keycloak.adapters.saml.profile.webbrowsersso.BrowserHandler;
import org.keycloak.adapters.spi.HttpFacade;

/**
 * WildFly Elytron 环境下的 SAML 认证器。
 *
 * <p>继承 {@link SamlAuthenticator}，认证成功后通过 {@link ElytronHttpFacade}
 * 完成 Elytron 安全身份绑定；浏览器处理器使用 {@link BrowserHandler}。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class ElytronSamlAuthenticator extends SamlAuthenticator {
    /** Elytron 认证回调处理器。 */
    private final CallbackHandler callbackHandler;
    /** Elytron HTTP 门面。 */
    private final ElytronHttpFacade facade;

    /**
     * 创建 Elytron SAML 认证器。
     *
     * @param facade          HTTP 门面
     * @param samlDeployment  SAML 部署配置
     * @param callbackHandler Elytron 回调处理器
     */
    public ElytronSamlAuthenticator(ElytronHttpFacade facade, SamlDeployment samlDeployment, CallbackHandler callbackHandler) {
        super(facade, samlDeployment, facade.getSessionStore());
        this.callbackHandler = callbackHandler;
        this.facade = facade;
    }

    /**
     * 认证成功后通知 Elytron 门面完成安全上下文切换。
     *
     * @param samlSession 已建立的 SAML 会话
     */
    @Override
    protected void completeAuthentication(SamlSession samlSession) {
        facade.authenticationComplete(samlSession);
    }

    /**
     * 创建浏览器 SSO 处理器（Elytron 场景使用 {@link BrowserHandler}）。
     *
     * @param facade       HTTP 门面
     * @param deployment   SAML 部署配置
     * @param sessionStore 会话存储
     * @return 浏览器 SSO 处理器
     */
    @Override
    protected SamlAuthenticationHandler createBrowserHandler(HttpFacade facade, SamlDeployment deployment, SamlSessionStore sessionStore) {
        return new BrowserHandler(facade, deployment, sessionStore);
    }
}
