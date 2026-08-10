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

package org.keycloak.services.clientpolicy.condition;

import java.util.List;

import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.protocol.oidc.utils.AcrUtils;
import org.keycloak.representations.idm.ClientPolicyConditionConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.ClientPolicyVote;
import org.keycloak.services.clientpolicy.context.AuthorizationRequestContext;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * 客户端策略条件：在 {@link ClientPolicyEvent#AUTHORIZATION_REQUEST} 事件中检查授权请求是否包含配置的 ACR 值。
 * <p>匹配时将请求的 ACR 写入认证会话 note，供后续策略/认证流程使用。</p>
 *
 * @author <a href="mailto:ggrazian@redhat.com">Giuseppe Graziano</a>
 */
public class AcrCondition extends AbstractClientPolicyConditionProvider<AcrCondition.Configuration> {

    /** @param session Keycloak 会话 */
    public AcrCondition(KeycloakSession session) {
        super(session);
    }

    /** 条件配置：期望匹配的 ACR 属性值 */
    public static class Configuration extends ClientPolicyConditionConfigurationRepresentation {

        /** 配置项 {@code acr-property}：需出现在请求 ACR 列表中的值 */
        @JsonProperty("acr-property")
        protected String acrProperty;

        /** @return 配置的 ACR 值 */
        public String getAcrProperty() {
            return acrProperty;
        }

        /** @param acrProperty 配置的 ACR 值 */
        public void setAcrProperty(String acrProperty) {
            this.acrProperty = acrProperty;
        }
    }

    /** @return 条件配置类型 */
    @Override
    public Class<Configuration> getConditionConfigurationClass() {
        return Configuration.class;
    }

    /** @return 条件提供方 ID */
    @Override
    public String getProviderId() {
        return AnyClientConditionFactory.PROVIDER_ID;
    }

    /** 在授权请求事件中评估 ACR 是否匹配配置 @param context 策略上下文 @return 投票结果 */
    @Override
    public ClientPolicyVote applyPolicy(ClientPolicyContext context) throws ClientPolicyException {
        if (context.getEvent() == ClientPolicyEvent.AUTHORIZATION_REQUEST) {
            AuthorizationRequestContext authorizationRequestContext = ((AuthorizationRequestContext) context);
            if (containsAcr(authorizationRequestContext)) {
                authorizationRequestContext.getAuthenticationSession().setAuthNote(Constants.CLIENT_POLICY_REQUESTED_ACR, configuration.getAcrProperty());
                return ClientPolicyVote.YES;
            }
            else {
                return ClientPolicyVote.NO;
            }
        }
        return ClientPolicyVote.ABSTAIN;
    }

    /** 判断授权请求 claims/acr 参数是否包含配置的 ACR 值 */
    private boolean containsAcr(AuthorizationRequestContext context) {
        List<String> acrValues = AcrUtils.getAcrValues(context.getAuthorizationEndpointRequest().getClaims(), context.getAuthorizationEndpointRequest().getAcr(), session.getContext().getClient());
        return acrValues != null && !acrValues.isEmpty() && acrValues.contains(configuration.getAcrProperty());
    }

}
