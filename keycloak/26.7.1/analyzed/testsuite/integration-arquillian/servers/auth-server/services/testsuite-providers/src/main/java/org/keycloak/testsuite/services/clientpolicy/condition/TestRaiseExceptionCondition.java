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

package org.keycloak.testsuite.services.clientpolicy.condition;

import org.keycloak.OAuthErrorException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.ClientPolicyConditionConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.ClientPolicyVote;
import org.keycloak.services.clientpolicy.condition.AbstractClientPolicyConditionProvider;

/**
 * 测试用客户端策略条件，故意抛出 {@link ClientPolicyException} 以验证错误处理。
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class TestRaiseExceptionCondition extends AbstractClientPolicyConditionProvider<TestRaiseExceptionCondition.Configuration> {

    /** 创建条件提供者实例。 */
    public TestRaiseExceptionCondition(KeycloakSession session) {
        super(session);
    }

    /** {@inheritDoc} 返回 {@link Configuration} 配置类。 */
    @Override
    public Class<Configuration> getConditionConfigurationClass() {
        return Configuration.class;
    }

    /** 测试条件配置，继承默认表示形式。 */
    public static class Configuration extends ClientPolicyConditionConfigurationRepresentation {
    }

    /** {@inheritDoc} 返回 {@link TestRaiseExceptionConditionFactory#PROVIDER_ID}。 */
    @Override
    public String getProviderId() {
        return TestRaiseExceptionConditionFactory.PROVIDER_ID;
    }

    /** {@inheritDoc} 始终抛出故意构造的服务器错误异常。 */
    @Override
    public ClientPolicyVote applyPolicy(ClientPolicyContext context) throws ClientPolicyException {
        throw new ClientPolicyException(OAuthErrorException.SERVER_ERROR, "intentional exception for test");
    }

}
