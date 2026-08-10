/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.models.Constants;
import org.keycloak.representations.idm.ClientPolicyExecutorConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.context.AuthorizationRequestContext;
import org.keycloak.sessions.AuthenticationSessionModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 认证流选择 Executor：在授权请求阶段将指定认证流别名与 LOA 写入 {@link AuthenticationSessionModel} auth note。
 * <p>配合 ACR/LOA 条件使用，强制后续登录流程走配置的 {@code auth-flow-alias}。</p>
 *
 * @author <a href="mailto:ggrazian@redhat.com">Giuseppe Graziano</a>
 */
public class AuthenticationFlowSelectorExecutor implements ClientPolicyExecutorProvider<AuthenticationFlowSelectorExecutor.Configuration> {

    /** Executor 运行时配置。 */
    private Configuration configuration;

    /** 无参构造，由 Factory 创建实例。 */
    public AuthenticationFlowSelectorExecutor() {
    }

    /** {@inheritDoc} 注入认证流别名与 LOA 配置 */
    @Override
    public void setupConfiguration(Configuration config) {
        this.configuration = config;
    }

    /** {@inheritDoc} @return {@link Configuration} */
    @Override
    public Class<Configuration> getExecutorConfigurationClass() {
        return Configuration.class;
    }

    /** 认证流选择 Executor 配置：别名与 LOA。 */
    public static class Configuration extends ClientPolicyExecutorConfigurationRepresentation {
        /** 要强制使用的认证流别名。 */
        @JsonProperty("auth-flow-alias")
        protected String authFlowAlias;

        /** 认证流对应的 LOA（Level of Authentication）等级。 */
        @JsonProperty("auth-flow-loa")
        protected Integer authFlowLoa;

        /** @return 认证流别名 */
        public String getAuthFlowAlias() {
            return authFlowAlias;
        }

        /** @param authFlowAlias 认证流别名 */
        public void setAuthFlowAlias(String authFlowAlias) {
            this.authFlowAlias = authFlowAlias;
        }

        /** @return 认证流 LOA */
        public Integer getAuthFlowLoa() {
            return authFlowLoa;
        }

        /** @param authFlowLoa 认证流 LOA */
        public void setAuthFlowLoa(Integer authFlowLoa) {
            this.authFlowLoa = authFlowLoa;
        }
    }

    /** {@inheritDoc} @return Executor 提供方 ID */
    @Override
    public String getProviderId() {
        return PKCEEnforcerExecutorFactory.PROVIDER_ID;
    }

    /** {@inheritDoc} 在 {@link ClientPolicyEvent#AUTHORIZATION_REQUEST} 上选择认证流 */
    @Override
    public void executeOnEvent(ClientPolicyContext context) throws ClientPolicyException {
        if (context.getEvent() == ClientPolicyEvent.AUTHORIZATION_REQUEST) {
            AuthorizationRequestContext authorizationRequestContext = (AuthorizationRequestContext) context;
            executeOnAuthorizationRequest(authorizationRequestContext.getAuthenticationSession());
        }
    }

    /** 将配置的认证流别名与 LOA 写入认证会话 auth note。 */
    private void executeOnAuthorizationRequest(AuthenticationSessionModel authSession) {
        if (configuration.getAuthFlowAlias() != null) {
            authSession.setAuthNote(Constants.REQUESTED_AUTHENTICATION_FLOW, configuration.getAuthFlowAlias());
            // 若配置了 LOA，则写入 ACR 条件使用的认证等级 note
            if (configuration.getAuthFlowLoa() != null) {
                authSession.setAuthNote(Constants.AUTHENTICATION_FLOW_LEVEL_OF_AUTHENTICATION, String.valueOf(configuration.getAuthFlowLoa()));
            }
        }
    }


}
