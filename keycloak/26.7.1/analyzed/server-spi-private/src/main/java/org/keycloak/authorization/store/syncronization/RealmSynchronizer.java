/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.authorization.store.syncronization;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.authorization.store.ResourceServerStore;
import org.keycloak.authorization.store.StoreFactory;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel.RealmRemovedEvent;
import org.keycloak.provider.ProviderFactory;

/**
 * 领域删除事件同步器：删除该领域下所有客户端对应的资源服务器。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class RealmSynchronizer implements Synchronizer<RealmRemovedEvent> {
    /** 领域移除时遍历其客户端并删除关联资源服务器。 */
    @Override
    public void synchronize(RealmRemovedEvent event, KeycloakSessionFactory factory) {
        ProviderFactory<AuthorizationProvider> providerFactory = factory.getProviderFactory(AuthorizationProvider.class);
        AuthorizationProvider authorizationProvider = providerFactory.create(event.getKeycloakSession());
        StoreFactory storeFactory = authorizationProvider.getStoreFactory();
        ResourceServerStore resourceServerStore = storeFactory.getResourceServerStore();

        event.getRealm().getClientsStream().forEach(resourceServerStore::delete);
    }
}
