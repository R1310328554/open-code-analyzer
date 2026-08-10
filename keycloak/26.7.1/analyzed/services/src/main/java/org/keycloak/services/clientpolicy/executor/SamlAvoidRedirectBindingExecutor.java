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

package org.keycloak.services.clientpolicy.executor;

import java.net.URI;

import org.keycloak.OAuthErrorException;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.saml.SamlClient;
import org.keycloak.protocol.saml.SamlProtocol;
import org.keycloak.representations.idm.ClientPolicyExecutorConfigurationRepresentation;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.context.AdminClientRegisteredContext;
import org.keycloak.services.clientpolicy.context.AdminClientUpdatedContext;
import org.keycloak.services.clientpolicy.context.SamlAuthnRequestContext;
import org.keycloak.services.clientpolicy.context.SamlLogoutRequestContext;

/**
 * SAML 避免 Redirect 绑定执行器。
 * <p>在 SAML 客户端注册/更新时强制启用 POST 绑定；在认证与登出请求中禁止使用 HTTP-Redirect 绑定。</p>
 *
 * @author rmartinc
 */
public class SamlAvoidRedirectBindingExecutor implements ClientPolicyExecutorProvider<ClientPolicyExecutorConfigurationRepresentation> {

    /** @param session Keycloak 会话（本执行器不使用） */
    public SamlAvoidRedirectBindingExecutor(KeycloakSession session) {
    }

    @Override
    public String getProviderId() {
        return SamlAvoidRedirectBindingExecutorFactory.PROVIDER_ID;
    }

    /** 按 SAML 客户端 CRUD 或请求事件校验绑定方式 */
    @Override
    public void executeOnEvent(ClientPolicyContext context) throws ClientPolicyException {
        // 客户端注册/更新或 SAML 认证/登出请求
        switch (context.getEvent()) {
            case REGISTERED -> {
                confirmPostBindingIsForced(((AdminClientRegisteredContext)context).getTargetClient());
            }
            case UPDATED -> {
                confirmPostBindingIsForced(((AdminClientUpdatedContext)context).getTargetClient());
            }
            case SAML_AUTHN_REQUEST -> {
                confirmRedirectBindingIsNotUsed((SamlAuthnRequestContext) context);
            }
            case SAML_LOGOUT_REQUEST -> {
                confirmRedirectBindingIsNotUsed((SamlLogoutRequestContext) context);
            }
        }
    }

    /** 校验 SAML 协议客户端必须启用 forcePostBinding */
    private void confirmPostBindingIsForced(ClientModel client) throws ClientPolicyException {
        if (SamlProtocol.LOGIN_PROTOCOL.equals(client.getProtocol())) {
            SamlClient samlClient = new SamlClient(client);
            if (!samlClient.forcePostBinding()) {
                throw new ClientPolicyException(OAuthErrorException.INVALID_CLIENT_METADATA, "Force POST binding is not enabled");
            }
        }
    }

    /** 校验 SAML 认证请求未使用 Redirect 绑定 */
    private void confirmRedirectBindingIsNotUsed(SamlAuthnRequestContext context) throws ClientPolicyException {
        SamlClient samlClient = new SamlClient(context.getClient());
        if (samlClient.forcePostBinding()) {
            return;
        }
        URI requestedBinding = context.getRequest().getProtocolBinding();
        if (requestedBinding == null) {
            // 未显式指定 binding 时，按实际请求 binding 判断
            if (context.getProtocolBinding().equals(SamlProtocol.SAML_REDIRECT_BINDING)) {
                throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, "REDIRECT binding is used for the login request and it is not allowed.");
            }
        } else {
            // 显式指定 binding 时，禁止 redirect 或 artifact+redirect 组合
            if (JBossSAMLURIConstants.SAML_HTTP_REDIRECT_BINDING.get().equals(requestedBinding.toString())
                    || (JBossSAMLURIConstants.SAML_HTTP_ARTIFACT_BINDING.get().equals(requestedBinding.toString())
                            && context.getProtocolBinding().equals(SamlProtocol.SAML_REDIRECT_BINDING))) {
                throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, "REDIRECT binding is used for the login request and it is not allowed.");
            }
        }
    }

    /** 校验 SAML 登出请求未使用 Redirect 绑定 */
    private void confirmRedirectBindingIsNotUsed(SamlLogoutRequestContext context) throws ClientPolicyException {
        SamlClient samlClient = new SamlClient(context.getClient());
        if (samlClient.forcePostBinding()) {
            return;
        }
        if (context.getProtocolBinding().equals(SamlProtocol.SAML_REDIRECT_BINDING)) {
            throw new ClientPolicyException(OAuthErrorException.INVALID_REQUEST, "REDIRECT binding is used for the logout request and it is not allowed.");
        }
    }
}
