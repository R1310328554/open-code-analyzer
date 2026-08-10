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

package org.keycloak.models.cache.infinispan.organization;

import org.keycloak.Config.Scope;
import org.keycloak.models.GroupModel;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.organization.OrganizationProvider;
import org.keycloak.organization.OrganizationProviderFactory;
import org.keycloak.organization.utils.Organizations;

/**
 * Infinispan 组织缓存提供者的 SPI 工厂。
 * <p>
 * 在 {@link #postInit(KeycloakSessionFactory)} 中注册领域/用户/组事件监听，在 IdP、成员或组织组变更时驱动缓存失效。
 */
public class InfinispanOrganizationProviderFactory implements OrganizationProviderFactory {

    /** SPI 提供者标识，与 {@link InfinispanOrganizationProvider} 配套使用。 */
    public static final String PROVIDER_ID = "infinispan";

    @Override
    public OrganizationProvider create(KeycloakSession session) {
        return new InfinispanOrganizationProvider(session);
    }

    @Override
    public void init(Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        factory.register(e -> {
            if (e instanceof RealmModel.IdentityProviderUpdatedEvent event) {
                registerOrganizationInvalidation(event.getKeycloakSession(), event.getUpdatedIdentityProvider());
            }
            if (e instanceof RealmModel.IdentityProviderRemovedEvent event) {
                registerOrganizationInvalidation(event.getKeycloakSession(), event.getRemovedIdentityProvider());
            }
            if (e instanceof UserModel.UserPreRemovedEvent event) {
                KeycloakSession session = event.getKeycloakSession();
                InfinispanOrganizationProvider orgProvider = (InfinispanOrganizationProvider) session.getProvider(OrganizationProvider.class, getId());
                orgProvider.getByMember(event.getUser()).forEach(organization -> orgProvider.registerMemberInvalidation(organization, event.getUser()));
            }
            if (e instanceof GroupModel.GroupRemovedEvent event) {
                registerOrgGroupInvalidation(event);

                KeycloakSession session = event.getKeycloakSession();
                session.users().getGroupMembersStream(session.getContext().getRealm(), event.getGroup()).forEach(member -> registerOrgGroupMembershipInvalidation(event, member));
            }
            if (e instanceof GroupModel.GroupUpdatedEvent event) {
                registerOrgGroupInvalidation(event);
            }
            if (e instanceof GroupModel.GroupMemberJoinEvent event) {
                registerOrgGroupMembershipInvalidation(event, event.getUser());
            }
            if (e instanceof GroupModel.GroupMemberLeaveEvent event) {
                registerOrgGroupMembershipInvalidation(event, event.getUser());
            }
        });
    }

    /** 身份提供者变更时，若绑定组织则失效对应组织缓存。 */
    private void registerOrganizationInvalidation(KeycloakSession session, IdentityProviderModel idp) {
        if (idp.getOrganizationId() != null) {
            InfinispanOrganizationProvider orgProvider = (InfinispanOrganizationProvider) session.getProvider(OrganizationProvider.class, getId());
            if (orgProvider != null) {
                OrganizationModel organization = orgProvider.getById(idp.getOrganizationId());
                orgProvider.registerOrganizationInvalidation(organization);
            }
        }
    }

    private void registerOrgGroupInvalidation(GroupModel.GroupEvent event) {
        GroupModel group = event.getGroup();

        // 仅处理组织组，普通组变更不影响组织缓存
        if (!Organizations.isOrganizationGroup(group)) {
            return;
        }

        KeycloakSession session = event.getKeycloakSession();
        InfinispanOrganizationProvider orgProvider = (InfinispanOrganizationProvider) session.getProvider(OrganizationProvider.class, getId());
        if (orgProvider != null) {
            orgProvider.registerOrganizationInvalidation(group.getOrganization());
        }
    }

    private void registerOrgGroupMembershipInvalidation(GroupModel.GroupEvent event, UserModel member) {
        GroupModel group = event.getGroup();

        // 仅处理组织组，普通组变更不影响组织缓存
        if (!Organizations.isOrganizationGroup(group)) {
            return;
        }

        KeycloakSession session = event.getKeycloakSession();
        InfinispanOrganizationProvider orgProvider = (InfinispanOrganizationProvider) session.getProvider(OrganizationProvider.class, getId());
        if (orgProvider != null) {
            // 仅失效「成员所属组织组」索引，不失效组织成员关系本身
            orgProvider.registerOrgGroupsMembershipInvalidation(group.getOrganization(), member);
        }
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public int order() {
        return 10;
    }
}
