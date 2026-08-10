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

package org.keycloak.storage;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserProvider;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.storage.UserStorageProviderModel.SyncMode;
import org.keycloak.storage.datastore.DefaultDatastoreProvider;
import org.keycloak.storage.user.SynchronizationResult;

/**
 * 用户存储私有工具类：提供本地用户存储访问及全量/增量同步的便捷入口。
 *
 * @author Alexander Schwartz
 */
public class UserStoragePrivateUtil {

    /** 获取当前会话的本地用户 Provider。 */
    public static UserProvider userLocalStorage(KeycloakSession session) {
        return ((DefaultDatastoreProvider) session.getProvider(DatastoreProvider.class)).userLocalStorage();
    }

    /** 在独立事务中执行指定 Provider 的全量用户同步。 */
    public static SynchronizationResult runFullSync(KeycloakSessionFactory sessionFactory, UserStorageProviderModel provider) {
        return KeycloakModelUtils.runJobInTransactionWithResult(sessionFactory, session -> {
            RealmModel realm = session.realms().getRealm(provider.getParentId());
            session.getContext().setRealm(realm);
            return new UserStorageSyncTask(provider, SyncMode.FULL).runWithResult(session);
        });
    }

    /** 在独立事务中执行指定 Provider 的增量（变更）用户同步。 */
    public static SynchronizationResult runPeriodicSync(KeycloakSessionFactory sessionFactory, UserStorageProviderModel provider) {
        return KeycloakModelUtils.runJobInTransactionWithResult(sessionFactory, session -> {
            RealmModel realm = session.realms().getRealm(provider.getParentId());
            session.getContext().setRealm(realm);
            return new UserStorageSyncTask(provider, SyncMode.CHANGED).runWithResult(session);
        });
    }
}
