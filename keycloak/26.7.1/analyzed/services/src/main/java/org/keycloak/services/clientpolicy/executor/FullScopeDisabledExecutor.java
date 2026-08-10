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
 *
 */

package org.keycloak.services.clientpolicy.executor;

import org.keycloak.events.Errors;
import org.keycloak.representations.idm.ClientPolicyExecutorConfigurationRepresentation;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.context.ClientCRUDContext;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 禁用 fullScopeAllowed 执行器。
 * <p>在客户端注册/更新时校验或自动配置，禁止客户端启用 fullScopeAllowed 开关。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class FullScopeDisabledExecutor implements ClientPolicyExecutorProvider<FullScopeDisabledExecutor.Configuration> {

    /** 执行器运行时配置 */
    private FullScopeDisabledExecutor.Configuration configuration;

    @Override
    public void executeOnEvent(ClientPolicyContext context) throws ClientPolicyException {
        switch (context.getEvent()) {
            case REGISTER:
            case UPDATE:
                ClientCRUDContext clientUpdateContext = (ClientCRUDContext)context;
                autoConfigure(clientUpdateContext.getProposedClientRepresentation());
                validate(clientUpdateContext.getProposedClientRepresentation());
                break;
            default:
                return;
        }
    }

    @Override
    public void setupConfiguration(FullScopeDisabledExecutor.Configuration config) {
        this.configuration = config;
    }

    @Override
    public Class<FullScopeDisabledExecutor.Configuration> getExecutorConfigurationClass() {
        return FullScopeDisabledExecutor.Configuration.class;
    }

    public static class Configuration extends ClientPolicyExecutorConfigurationRepresentation {
        /** 为 true 时自动将 fullScopeAllowed 设为 false */
        @JsonProperty("auto-configure")
        protected Boolean autoConfigure;

        public Boolean isAutoConfigure() {
            return autoConfigure;
        }

        public void setAutoConfigure(Boolean autoConfigure) {
            this.autoConfigure = autoConfigure;
        }
    }

    @Override
    public String getProviderId() {
        return FullScopeDisabledExecutorFactory.PROVIDER_ID;
    }

    private void autoConfigure(ClientRepresentation rep) {
        if (configuration.isAutoConfigure()) {
            rep.setFullScopeAllowed(false);
        }
    }

    private void validate(ClientRepresentation proposedClient) throws ClientPolicyException {
        if (proposedClient.isFullScopeAllowed() != null && proposedClient.isFullScopeAllowed()) {
            throw new ClientPolicyException(Errors.INVALID_REGISTRATION, "Not permitted to enable fullScopeAllowed");
        }
    }
}
