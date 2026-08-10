/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.testsuite.events;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * <p>与 {@linkplain TestEventsListenerProviderFactory} 类似的事件监听器工厂，其实现会将
 * 会话上下文中的 Realm 名称与 clientId 写入事件详情，用于验证上下文是否正确传播至监听器。</p>
 *
 * @author rmartinc
 */
public class TestEventsListenerContextDetailsProviderFactory implements EventListenerProviderFactory {

    /** 提供者唯一标识符。 */
    public static final String PROVIDER_ID = "event-queue-context-details";
    /** 事件详情中 Realm 名称的键名。 */
    public static final String CONTEXT_REALM_DETAIL = "context.realmName";
    /** 事件详情中 Client ID 的键名。 */
    public static final String CONTEXT_CLIENT_DETAIL = "context.clientId";

    /** {@inheritDoc} 创建带上下文详情的事件监听器实例。 */
    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new TestEventsListenerContextDetailsProvider(session);
    }

    /** {@inheritDoc} 初始化工厂配置。 */
    @Override
    public void init(Config.Scope config) {
    }

    /** {@inheritDoc} 会话工厂就绪后的回调。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    /** {@inheritDoc} 关闭工厂。 */
    @Override
    public void close() {
    }

    /** {@inheritDoc} 返回工厂标识。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
