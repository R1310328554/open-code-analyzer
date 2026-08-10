/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.storage.user;

import java.util.Date;

import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.storage.UserStorageProviderModel;

/**
 * 用户导入同步能力接口（可选）。
 *
 * <p>由支持将外部用户同步到 Keycloak 本地存储的 {@link org.keycloak.storage.UserStorageProviderFactory UserStorageProviderFactory} 实现。
 * 若要在管理控制台中使用同步功能，必须实现此接口。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface ImportSynchronization {

    /** 执行全量用户同步。 */
    SynchronizationResult sync(KeycloakSessionFactory sessionFactory, String realmId, UserStorageProviderModel model);

    /** 自 {@code lastSync} 以来执行增量用户同步。 */
    SynchronizationResult syncSince(Date lastSync, KeycloakSessionFactory sessionFactory, String realmId, UserStorageProviderModel model);
}
