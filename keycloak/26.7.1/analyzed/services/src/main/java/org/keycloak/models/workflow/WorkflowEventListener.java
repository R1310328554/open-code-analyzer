/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package org.keycloak.models.workflow;

import java.util.Objects;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.provider.ProviderEventListener;

/**
 * 工作流事件监听器：将用户事件、管理事件与 {@link ProviderEvent} 转换为 {@link WorkflowEvent} 并提交调度。
 * <p>委托所有已注册的 {@link WorkflowEventProvider} 创建事件，由 {@link WorkflowProvider#submit} 异步执行工作流。</p>
 */
public class WorkflowEventListener implements EventListenerProvider, ProviderEventListener {

    private final KeycloakSession session;

    /** @param session Keycloak 会话 */
    public WorkflowEventListener(KeycloakSession session) {
        this.session = session;
    }

    /** 处理用户域事件并尝试调度匹配的工作流。 */
    @Override
    public void onEvent(Event event) {
        session.getAllProviders(WorkflowEventProvider.class).stream()
                .map(provider -> provider.create(event))
                .filter(Objects::nonNull)
                .forEach(this::trySchedule);
    }

    /** 处理管理域事件并尝试调度匹配的工作流。 */
    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        session.getAllProviders(WorkflowEventProvider.class).stream()
                .map(provider -> provider.create(event))
                .filter(Objects::nonNull)
                .forEach(this::trySchedule);
    }

    /** 处理 {@link ProviderEvent}；无 Realm 上下文时直接返回。 */
    @Override
    public void onEvent(ProviderEvent event) {
        RealmModel realm = session.getContext().getRealm();
        if (realm == null) {
            return;
        }
        session.getAllProviders(WorkflowEventProvider.class).stream()
                .map(provider -> provider.create(event))
                .filter(Objects::nonNull)
                .forEach(this::trySchedule);
    }

    /** 将非空 {@link WorkflowEvent} 提交至 {@link WorkflowProvider}。 */
    private void trySchedule(WorkflowEvent event) {
        if (event != null) {
            session.getProvider(WorkflowProvider.class).submit(event);
        }
    }

    @Override
    public void close() {

    }
}
