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
import org.keycloak.adapters.spi.AuthOutcome;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.saml.common.constants.GeneralConstants;

/**
 * SAML 协议端点处理器，专门处理携带 {@code SAMLRequest}/{@code SAMLResponse} 的回调请求。
 *
 * <p>通常映射至 {@code /saml} 等路径，接收 IdP 重定向或 POST 回传的 SAML 消息。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SamlEndpoint extends WebBrowserSsoAuthenticationHandler {

    /**
     * 创建 SAML 端点处理器。
     *
     * @param facade       HTTP 门面
     * @param deployment   SAML 部署配置
     * @param sessionStore 会话存储
     */
    public SamlEndpoint(HttpFacade facade, SamlDeployment deployment, SamlSessionStore sessionStore) {
        super(facade, deployment, sessionStore);
    }

    /**
     * 从请求参数提取 SAML 载荷并分发至请求或响应处理逻辑。
     *
     * @param onCreateSession 会话创建回调
     * @return 认证结果；无 SAML 参数时返回 {@link AuthOutcome#NOT_ATTEMPTED}
     */
    @Override
    public AuthOutcome handle(OnSessionCreated onCreateSession) {
        String samlRequest = facade.getRequest().getFirstParam(GeneralConstants.SAML_REQUEST_KEY);
        String samlResponse = facade.getRequest().getFirstParam(GeneralConstants.SAML_RESPONSE_KEY);
        String relayState = facade.getRequest().getFirstParam(GeneralConstants.RELAY_STATE);
        if (samlRequest != null) {
            return handleSamlRequest(samlRequest, relayState);
        } else if (samlResponse != null) {
            return handleSamlResponse(samlResponse, relayState, onCreateSession);
        }
        return AuthOutcome.NOT_ATTEMPTED;

    }
}
