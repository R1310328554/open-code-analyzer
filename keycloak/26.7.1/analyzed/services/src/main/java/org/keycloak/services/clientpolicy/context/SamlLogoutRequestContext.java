/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.clientpolicy.context;

import org.keycloak.dom.saml.v2.protocol.LogoutRequestType;
import org.keycloak.models.ClientModel;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;

/**
 * SAML 登出请求（LogoutRequest）客户端策略上下文。
 * <p>在处理 SAML 单点登出请求时触发，继承 {@link AbstractSamlRequestContext} 暴露请求、客户端与绑定。</p>
 *
 * @author rmartinc
 */
public class SamlLogoutRequestContext extends AbstractSamlRequestContext<LogoutRequestType> {

    /**
     * @param request SAML LogoutRequest
     * @param client 发起请求的客户端
     * @param protocolBinding SAML 协议绑定 URI
     */
    public SamlLogoutRequestContext(final LogoutRequestType request, final ClientModel client, final String protocolBinding) {
        super(request, client, protocolBinding);
    }

    /** {@inheritDoc} @return {@link ClientPolicyEvent#SAML_LOGOUT_REQUEST} */
    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.SAML_LOGOUT_REQUEST;
    }
}
