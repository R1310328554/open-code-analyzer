/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.services.clientpolicy.condition;

import java.util.List;

import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.ClientPolicyConditionConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.ClientPolicyVote;
import org.keycloak.services.clientpolicy.context.IdentityProviderContext;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 身份提供方条件：当客户端策略上下文涉及 IdP 时，按 IdP 别名是否在配置列表中匹配策略。
 * <p>适用于 JWT Authorization Grant 等需要联邦身份提供方的操作。</p>
 * @author rmartinc
 */
public class IdentityProviderCondition extends AbstractClientPolicyConditionProvider<IdentityProviderCondition.Configuration> {

    /** 条件配置：期望匹配的身份提供方别名列表。 */
    public static class Configuration extends ClientPolicyConditionConfigurationRepresentation {

        @JsonProperty(IdentityProviderConditionFactory.IDENTITY_PROVIDERS_ALIASES)
        protected List<String> identityProviderAliases;

        /** @return 身份提供方别名列表 */
        public List<String> getIdentityProviderAliases() {
            return identityProviderAliases;
        }

        /** @param identityProviderAliases 身份提供方别名列表 */
        public void setIdentityProviderAliases(List<String> identityProviderAliases) {
            this.identityProviderAliases = identityProviderAliases;
        }
    }

    /** @param session Keycloak 会话 */
    public IdentityProviderCondition(KeycloakSession session) {
        super(session);
    }

    /** {@inheritDoc} @return 条件配置类型 */
    @Override
    public Class<Configuration> getConditionConfigurationClass() {
        return Configuration.class;
    }

    /** {@inheritDoc} @return {@link IdentityProviderConditionFactory#PROVIDER_ID} */
    @Override
    public String getProviderId() {
        return IdentityProviderConditionFactory.PROVIDER_ID;
    }

    /** {@inheritDoc} 在 {@link IdentityProviderContext} 上按 IdP 别名投票 */
    @Override
    public ClientPolicyVote applyPolicy(ClientPolicyContext context) throws ClientPolicyException {
        if (context instanceof IdentityProviderContext idpContext) {
            return isIdentityProvider(idpContext.getIdentityProviderAlias()) ? ClientPolicyVote.YES : ClientPolicyVote.NO;
        }
        return ClientPolicyVote.ABSTAIN;
    }

    /** 判断 IdP 别名是否在配置列表中。 @param identityProviderAlias 上下文中的 IdP 别名 */
    private boolean isIdentityProvider(String identityProviderAlias) {
        return configuration.getIdentityProviderAliases().contains(identityProviderAlias);
    }
}
