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

package org.keycloak.models.workflow;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel.RealmRemovedEvent;
import org.keycloak.models.UserModel.UserRemovedEvent;

/**
 * JPA {@link WorkflowStateProvider} 工厂：监听用户/realm 删除并清理 workflow 状态。
 */
public class JpaWorkflowStateProviderFactory implements WorkflowStateProviderFactory {

    /** Provider SPI ID。 */
    public static final String PROVIDER_ID = "jpa";

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        factory.register(fired -> {
            if (fired instanceof UserRemovedEvent event) {
                onUserRemovedEvent(event);
            } if (fired instanceof RealmRemovedEvent event) {
                onRealmRemovedEvent(event);
            }
        });
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public WorkflowStateProvider create(KeycloakSession session) {
        return new JpaWorkflowStateProvider(session);
    }

    @Override
    public void close() {
    }

    /** realm 删除后清理全部 workflow 状态。 */
    private void onRealmRemovedEvent(RealmRemovedEvent event) {
        KeycloakSession session = event.getKeycloakSession();
        WorkflowStateProvider provider = session.getProvider(WorkflowStateProvider.class);
        provider.removeAll();
    }

    /** 用户删除后按 resourceId 清理 workflow 状态。 */
    private void onUserRemovedEvent(UserRemovedEvent event) {
        KeycloakSession session = event.getKeycloakSession();
        WorkflowStateProvider provider = session.getProvider(WorkflowStateProvider.class);
        provider.removeByResource(event.getUser().getId());
    }
}
