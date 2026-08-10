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
package org.keycloak.credential;

import java.util.stream.Stream;

import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

/**
 * 用户凭据存储 SPI：持久化与查询用户的 {@link CredentialModel}。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserCredentialStore extends Provider {
    /** 更新用户已有凭据。 */
    void updateCredential(RealmModel realm, UserModel user, CredentialModel cred);
    /** 创建并持久化新凭据。 */
    CredentialModel createCredential(RealmModel realm, UserModel user, CredentialModel cred);

    /**
     * 删除用户指定 ID 的凭据。
     * Removes credential with the {@code id} for the {@code user}.
     *
     * @param realm realm.
     * @param user user
     * @param id id
     * @return {@code true} if the credential was removed, {@code false} otherwise
     *
     * TODO: Make this method return Boolean so that store can return "I don't know" answer, this can be used for example in async stores
     */
    boolean removeStoredCredential(RealmModel realm, UserModel user, String id);
    /** 按 ID 获取已存储凭据。 */
    CredentialModel getStoredCredentialById(RealmModel realm, UserModel user, String id);

    /**
     * 获取用户全部已存储凭据。
     * Obtains the stored credentials associated with the specified user.
     *
     * @param realm a reference to the realm.
     * @param user the user whose credentials are being searched.
     * @return a non-null {@link Stream} of credentials.
     */
    Stream<CredentialModel> getStoredCredentialsStream(RealmModel realm, UserModel user);

    /**
     * 获取用户指定类型的已存储凭据。
     * Obtains the stored credentials associated with the specified user that match the specified type.
     *
     * @param realm a reference to the realm.
     * @param user the user whose credentials are being searched.
     * @param type the type of credentials being searched.
     * @return a non-null {@link Stream} of credentials.
     */
    Stream<CredentialModel> getStoredCredentialsByTypeStream(RealmModel realm, UserModel user, String type);

    /** 按用户标签名与类型获取凭据。 */
    CredentialModel getStoredCredentialByNameAndType(RealmModel realm, UserModel user, String name, String type);

    // 列表排序操作
    //list operations
    /** 调整凭据在列表中的顺序。 */
    boolean moveCredentialTo(RealmModel realm, UserModel user, String id, String newPreviousCredentialId);

}
