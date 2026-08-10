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

import java.util.HashMap;
import java.util.Map;

import org.keycloak.events.Event;
import org.keycloak.models.KeycloakSession;

/**
 * <p>{@link TestEventsListenerProvider} 的扩展实现，将会话上下文中的 Realm 与
 * Client 信息写入事件详情字段。</p>
 *
 * @author rmartinc
 */
public class TestEventsListenerContextDetailsProvider extends TestEventsListenerProvider {

    /** 当前 Keycloak 会话，用于读取上下文详情。 */
    private final KeycloakSession session;

    /**
     * @param session Keycloak 会话
     */
    public TestEventsListenerContextDetailsProvider(KeycloakSession session) {
        super(session);
        this.session = session;
    }

    /** {@inheritDoc} 在转发事件前补充 Realm 与 Client 上下文详情。 */
    @Override
    public void onEvent(Event event) {
        event = event.clone();
        Map<String, String> details = event.getDetails();
        if (details == null) {
            details = new HashMap<>();
            event.setDetails(details);
        }
        if (session.getContext().getRealm() != null) {
            details.put(TestEventsListenerContextDetailsProviderFactory.CONTEXT_REALM_DETAIL, session.getContext().getRealm().getName());
        }
        if (session.getContext().getClient() != null) {
            details.put(TestEventsListenerContextDetailsProviderFactory.CONTEXT_CLIENT_DETAIL, session.getContext().getClient().getClientId());
        }
        super.onEvent(event);
    }
}
