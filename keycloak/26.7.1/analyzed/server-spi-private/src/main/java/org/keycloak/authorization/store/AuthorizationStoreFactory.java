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

package org.keycloak.authorization.store;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.authorization.store.syncronization.ClientApplicationSynchronizer;
import org.keycloak.authorization.store.syncronization.GroupSynchronizer;
import org.keycloak.authorization.store.syncronization.OrganizationSynchronizer;
import org.keycloak.authorization.store.syncronization.RealmSynchronizer;
import org.keycloak.authorization.store.syncronization.RoleSynchronizer;
import org.keycloak.authorization.store.syncronization.Synchronizer;
import org.keycloak.authorization.store.syncronization.UserSynchronizer;
import org.keycloak.models.ClientModel.ClientRemovedEvent;
import org.keycloak.models.GroupModel.GroupRemovedEvent;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.OrganizationModel.OrganizationRemovedEvent;
import org.keycloak.models.RealmModel.RealmRemovedEvent;
import org.keycloak.models.RoleContainerModel.RoleRemovedEvent;
import org.keycloak.models.UserModel.UserRemovedEvent;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.provider.ProviderFactory;

/**
 * 授权存储工厂 SPI，扩展 {@link ProviderFactory}。
 * <p>在 {@link #postInit} 阶段注册领域模型删除事件的授权数据同步监听器。</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public interface AuthorizationStoreFactory extends ProviderFactory<StoreFactory> {

    /** 工厂初始化完成后注册授权数据同步监听器。 */
    @Override
    default void postInit(KeycloakSessionFactory factory) {
        registerSynchronizationListeners(factory);
    }

    /** 将客户端、领域、用户、组、角色与组织删除事件映射到对应 {@link Synchronizer}。 */
    default void registerSynchronizationListeners(KeycloakSessionFactory factory) {
        Map<Class<? extends ProviderEvent>, Synchronizer> synchronizers = new HashMap<>();

        synchronizers.put(ClientRemovedEvent.class, new ClientApplicationSynchronizer());
        synchronizers.put(RealmRemovedEvent.class, new RealmSynchronizer());
        synchronizers.put(UserRemovedEvent.class, new UserSynchronizer());
        synchronizers.put(GroupRemovedEvent.class, new GroupSynchronizer());
        synchronizers.put(RoleRemovedEvent.class, new RoleSynchronizer());
        synchronizers.put(OrganizationRemovedEvent.class, new OrganizationSynchronizer());

        factory.register(event -> {
            try {
                synchronizers.forEach((eventType, synchronizer) -> {
                    if (eventType.isInstance(event)) {
                        synchronizer.synchronize(event, factory);
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException("Error synchronizing authorization data.", e);
            }
        });
    }
}
