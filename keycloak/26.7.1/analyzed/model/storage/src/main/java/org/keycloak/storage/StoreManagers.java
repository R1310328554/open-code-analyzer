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

import org.keycloak.models.ClientProvider;
import org.keycloak.models.ClientScopeProvider;
import org.keycloak.models.GroupProvider;
import org.keycloak.models.RoleProvider;
import org.keycloak.models.UserProvider;
import org.keycloak.storage.federated.UserFederatedStorageProvider;

/**
 * 数据存储管理器聚合接口：暴露客户端、角色、组与用户等底层存储 Provider。
 */
public interface StoreManagers {
    
    /** 客户端存储管理器。 */
    ClientProvider clientStorageManager();

    /** 客户端 Scope 存储管理器。 */
    ClientScopeProvider clientScopeStorageManager();

    /** 角色存储管理器。 */
    RoleProvider roleStorageManager();

    /** 组存储管理器。 */
    GroupProvider groupStorageManager();

    /** 用户存储管理器（含联邦与本地聚合逻辑）。 */
    UserProvider userStorageManager();

    /** 本地用户存储。 */
    UserProvider userLocalStorage();

    /** 联邦用户附加数据存储。 */
    UserFederatedStorageProvider userFederatedStorage();
}
