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

package org.keycloak.adapters.saml.profile.webbrowsersso;

import org.keycloak.adapters.saml.OnSessionCreated;
import org.keycloak.adapters.saml.SamlDeployment;
import org.keycloak.adapters.saml.SamlSessionStore;
import org.keycloak.adapters.saml.profile.SamlInvocationContext;
import org.keycloak.adapters.spi.AuthOutcome;
import org.keycloak.adapters.spi.HttpFacade;

/**
 * 浏览器 SSO 认证处理器，用于普通页面请求（无显式 SAML 端点参数）。
 *
 * <p>与 {@link SamlEndpoint} 不同，本类不从请求参数读取 SAML 载荷，而是依赖
 * 会话缓存或发起新的 IdP 登录流程。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class BrowserHandler extends WebBrowserSsoAuthenticationHandler {

    /**
     * 创建浏览器 SSO 处理器。
     *
     * @param facade       HTTP 门面
     * @param deployment   SAML 部署配置
     * @param sessionStore 会话存储
     */
    public BrowserHandler(HttpFacade facade, SamlDeployment deployment, SamlSessionStore sessionStore) {
        super(facade, deployment, sessionStore);
    }

    /**
     * 处理浏览器请求：无 SAML 参数时检查缓存会话或发起登录。
     *
     * @param onCreateSession 会话创建回调
     * @return 认证结果
     */
    @Override
    public AuthOutcome handle(OnSessionCreated onCreateSession) {
        return doHandle(new SamlInvocationContext(null, null, null), onCreateSession);
    }
}
