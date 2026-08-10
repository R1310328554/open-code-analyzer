/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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

import org.keycloak.provider.Provider;
import org.keycloak.representations.idm.ClientPolicyExecutorConfigurationRepresentation;
import org.keycloak.services.clientpolicy.ClientPolicyContext;
import org.keycloak.services.clientpolicy.ClientPolicyEvent;
import org.keycloak.services.clientpolicy.ClientPolicyException;

/**
 * 客户端策略执行器提供者：对适用客户端执行策略动作。
 * <p>This executor specifies what action is executed on the client to which a client policy is adopted.
 * The executor can be executed on the events defined in {@link ClientPolicyEvent}.</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public interface ClientPolicyExecutorProvider<CONFIG extends ClientPolicyExecutorConfigurationRepresentation> extends Provider {

    /** 默认无关闭资源逻辑。 */
    @Override
    default void close() {
    }

    /**
     * 设置本执行器的配置。
     * setup this executor's configuration.
     *
     * @param config
     */
    default void setupConfiguration(CONFIG config) {
    }

    /**
     * @return 与 {@link #setupConfiguration(ClientPolicyExecutorConfigurationRepresentation)} 的 config 参数匹配的配置类
     */
    default Class<CONFIG> getExecutorConfigurationClass() {
        return (Class<CONFIG>) ClientPolicyExecutorConfigurationRepresentation.class;
    }

    /**
     * 在 {@link ClientPolicyEvent} 定义的事件上对客户端执行动作。
     * execute actions against the client on the event defined in {@link ClientPolicyEvent}.
     * 
     * @param context - the context of the event.
     * @throws {@link ClientPolicyException} - if something wrong happens when execution actions.
     */
    default void executeOnEvent(ClientPolicyContext context) throws ClientPolicyException {
    }

    /** @return 执行器提供者名称（默认类名） */
    default String getName() {
        return getClass().toString();
    }

    /** @return 提供者 ID */
    String getProviderId();
}
