/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.authorization.store.syncronization;

import java.util.EnumMap;
import java.util.Map;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.fgap.AdminPermissionsSchema;
import org.keycloak.authorization.model.PermissionTicket;
import org.keycloak.authorization.store.PermissionTicketStore;
import org.keycloak.authorization.store.PolicyStore;
import org.keycloak.authorization.store.ResourceStore;
import org.keycloak.authorization.store.StoreFactory;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserModel.UserRemovedEvent;
import org.keycloak.provider.ProviderFactory;

/**
 * 用户删除事件同步器：清理 FGAP 资源、用户权限票据与用户拥有的资源/策略。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class UserSynchronizer implements Synchronizer<UserRemovedEvent> {

    /** 用户移除时清理 FGAP 对象、权限票据与用户资源。 */
    @Override
    public void synchronize(UserRemovedEvent event, KeycloakSessionFactory factory) {
        ProviderFactory<AuthorizationProvider> providerFactory = factory.getProviderFactory(AuthorizationProvider.class);
        AuthorizationProvider authorizationProvider = providerFactory.create(event.getKeycloakSession());

        AdminPermissionsSchema.SCHEMA.removeResourceObject(authorizationProvider, event);

        removeFromUserPermissionTickets(event, authorizationProvider);
        removeUserResources(event, authorizationProvider);
    }

    /** 删除用户拥有的资源，并更新或删除仅引用该资源的策略。 */
    private void removeUserResources(UserRemovedEvent event, AuthorizationProvider authorizationProvider) {
        StoreFactory storeFactory = authorizationProvider.getStoreFactory();
        PolicyStore policyStore = storeFactory.getPolicyStore();
        ResourceStore resourceStore = storeFactory.getResourceStore();
        UserModel userModel = event.getUser();

        resourceStore.findByOwner(null, userModel.getId(), resource -> {
            String resourceId = resource.getId();
            policyStore.findByResource(resource.getResourceServer(), resource).forEach(policy -> {
                if (policy.getResources().size() == 1) {
                    policyStore.delete(policy.getId());
                } else {
                    policy.removeResource(resource);
                }
            });
            resourceStore.delete(resourceId);
        });
    }

    /** 删除用户作为所有者或请求者的全部权限票据。 */
    private void removeFromUserPermissionTickets(UserRemovedEvent event, AuthorizationProvider authorizationProvider) {
        StoreFactory storeFactory = authorizationProvider.getStoreFactory();
        PermissionTicketStore ticketStore = storeFactory.getPermissionTicketStore();
        UserModel userModel = event.getUser();
        Map<PermissionTicket.FilterOption, String> attributes = new EnumMap<>(PermissionTicket.FilterOption.class);

        attributes.put(PermissionTicket.FilterOption.OWNER, userModel.getId());

        for (PermissionTicket ticket : ticketStore.find(null, attributes, null, null)) {
            ticketStore.delete(ticket.getId());
        }

        attributes.clear();

        attributes.put(PermissionTicket.FilterOption.REQUESTER, userModel.getId());

        for (PermissionTicket ticket : ticketStore.find(null, attributes, null, null)) {
            ticketStore.delete(ticket.getId());
        }
    }
}
