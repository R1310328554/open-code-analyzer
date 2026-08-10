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
package org.keycloak.storage.federated;

import java.util.stream.Stream;

import org.keycloak.credential.CredentialModel;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.Provider;

/**
 * 联邦用户凭据存储接口，管理外部用户存储无法直接承载的凭据数据（密码、OTP 等）。
 *
 * <p>实现通常由 JPA 联邦存储 Provider 提供，供 {@link UserFederatedStorageProvider} 聚合使用。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserFederatedUserCredentialStore extends Provider {

    /** 更新指定联邦用户的凭据。 */
    void updateCredential(RealmModel realm, String userId, CredentialModel cred);

    /** 为指定联邦用户创建新凭据。 */
    CredentialModel createCredential(RealmModel realm, String userId, CredentialModel cred);

    /** 按 ID 删除联邦用户的已存储凭据。 */
    boolean removeStoredCredential(RealmModel realm, String userId, String id);

    /** 按 ID 获取联邦用户的已存储凭据。 */
    CredentialModel getStoredCredentialById(RealmModel realm, String userId, String id);

    /**
     * 获取 {@code userId} 标识的联邦用户关联的全部凭据。
     *
     * @param realm a reference to the realm.
     * @param userId the user identifier.
     * @return a non-null {@link Stream} of credentials.
     */
    Stream<CredentialModel> getStoredCredentialsStream(RealmModel realm, String userId);

    /**
     * 获取 {@code userId} 标识的联邦用户中指定 {@code type} 类型的凭据。
     *
     * @param realm a reference to the realm.
     * @param userId the user identifier.
     * @param type the credential type.
     * @return a non-null {@link Stream} of credentials.
     */
    Stream<CredentialModel> getStoredCredentialsByTypeStream(RealmModel realm, String userId, String type);

    /** 按名称与类型获取联邦用户的已存储凭据。 */
    CredentialModel getStoredCredentialByNameAndType(RealmModel realm, String userId, String name, String type);

    /**
     * @deprecated This interface is no longer necessary; collection-based methods were removed from the parent interface
     * and therefore the parent interface can be used directly
     */
    @Deprecated
    interface Streams extends UserFederatedUserCredentialStore {
    }
}
