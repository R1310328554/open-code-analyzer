/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.organization.jpa;

import org.keycloak.Config.Scope;
import org.keycloak.models.GroupModel;
import org.keycloak.models.GroupModel.GroupEvent;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.ModelValidationException;
import org.keycloak.organization.OrganizationProvider;
import org.keycloak.organization.OrganizationProviderFactory;
import org.keycloak.organization.utils.Organizations;
import org.keycloak.provider.ProviderEvent;

/**
 * JPA 组织 Provider 工厂（ID {@value #ID}）。
 * <p>
 * 创建 {@link JpaOrganizationProvider}，并监听 {@link GroupEvent} 以防止非法修改组织内部组。
 */
public class JpaOrganizationProviderFactory implements OrganizationProviderFactory {

    /** Provider 标识符。 */
    public static final String ID = "jpa";

    @Override
    public OrganizationProvider create(KeycloakSession session) {
        return new JpaOrganizationProvider(session);
    }

    @Override
    public void init(Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        factory.register(this::handleEvents);
    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return ID;
    }

    /** 拦截组变更事件，禁止无权限修改组织组。 */
    private void handleEvents(ProviderEvent e) {
        if (e instanceof GroupEvent event) {
            KeycloakSession session = event.getKeycloakSession();
            GroupModel group = event.getGroup();
            if (!Organizations.canManageOrganizationGroup(session, group)) {
                throw new ModelValidationException("Can not update organization group");
            }
        }
    }
}
