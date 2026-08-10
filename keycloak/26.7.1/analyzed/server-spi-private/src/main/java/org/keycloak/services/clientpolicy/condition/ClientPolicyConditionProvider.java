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

import org.keycloak.provider.Provider;
import org.keycloak.representations.idm.ClientPolicyConditionConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.ClientPolicyVote;

/**
 * 客户端策略条件提供者：判定客户端策略适用于哪些客户端。
 * <p>This condition determines to which client a client policy is adopted.
 * The condition can be evaluated on the events defined in {@link ClientPolicyEvent}.</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public interface ClientPolicyConditionProvider<CONFIG extends ClientPolicyConditionConfigurationRepresentation> extends Provider {

    /** 默认无关闭资源逻辑。 */
    @Override
    default void close() {
    }

    /**
     * 设置本条件的配置。
     * setup this condition's configuration.
     *
     * @param config
     */
    void setupConfiguration(CONFIG config);

    /**
     * @return 与 {@link #setupConfiguration(ClientPolicyConditionConfigurationRepresentation)} 的 config 参数匹配的配置类
     */
    default Class<CONFIG> getConditionConfigurationClass() {
        return (Class<CONFIG>) ClientPolicyConditionConfigurationRepresentation.class;
    }

    /**
     * 在 {@link ClientPolicyEvent} 定义的事件上评估条件。
     * returns ABSTAIN if this condition is not evaluated due to its nature.
     * returns YES if the client satisfies this condition on the event defined in {@link ClientPolicyEvent}.
     * If not, returns NO.
     *
     * @param context - the context of the event.
     * @return returns ABSTAIN if this condition is not evaluated due to its nature.
     * @throws {@link ClientPolicyException} - thrown if the condition is not evaluated in its nature on the event specified by context.
     */
    default ClientPolicyVote applyPolicy(ClientPolicyContext context) throws ClientPolicyException {
        return ClientPolicyVote.ABSTAIN;
    }

    /**
     * 是否反转 {@link #applyPolicy} 的投票结果（YES↔NO，ABSTAIN 不变）。
     * tells whether the result of applyPolicy method is inverted or not as follows.
     *  ClientPolicyVote.YES is inverted to ClientPolicyVote.NO
     *  ClientPolicyVote.NO is inverted to ClientPolicyVote.YES
     *  ClientPolicyVote.ABSTAIN remains unchanged
     *
     * @return true if the result of applyPolicy method is inverted.
     */
    boolean isNegativeLogic() throws ClientPolicyException;

    /** @return 条件提供者名称（默认类名） */
    default String getName() {
        return getClass().toString();
    }

    /** @return 提供者 ID */
    String getProviderId();
}
