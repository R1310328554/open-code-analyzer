/*
 * Copyright 2022. Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialModel;

/**
 * 主体凭据管理器：校验与管理已知实体（如用户）的凭据。
 * Validates and manages the credentials of a known entity (for example, a user).
 *
 * NOTE: This class might be renamed to {@link org.keycloak.models.UserCredentialManager} in Keycloak 27. Please use the {@link org.keycloak.models.UserCredentialManager}
 * already if you can
 */
public interface SubjectCredentialManager {

    /**
     * 校验凭据列表。
     * Validate a list of credentials.
     *
     * @return <code>true</code> if inputs are valid
     */
    boolean isValid(List<CredentialInput> inputs);

    /**
     * Validate a list of credentials.
     *
     * @return <code>true</code> if inputs are valid
     */
    default boolean isValid(CredentialInput... inputs) {
        return isValid(Arrays.asList(inputs));
    }

    /**
     * 用实体提供的输入更新凭据。
     * Updates a credential of the entity with the inputs provided by the entity.
     * @return <code>true</code> if credentials have been updated successfully
     */
    boolean updateCredential(CredentialInput input);

    /**
     * 用更新的 {@link CredentialModel} 更新存储凭据；通常由 {@link org.keycloak.credential.CredentialProvider} 调用。
     * Updates a credential of the entity with an updated {@link CredentialModel}.
     * Usually called by a {@link org.keycloak.credential.CredentialProvider}.
     */
    void updateStoredCredential(CredentialModel cred);

    /**
     * Updates a credential of the entity with an updated {@link CredentialModel}.
     * Usually called by a {@link org.keycloak.credential.CredentialProvider}.
     */
    CredentialModel createStoredCredential(CredentialModel cred);

    /**
     * 创建存储凭据；通常由 {@link org.keycloak.credential.CredentialProvider} 或账户管理调用。
     * Updates a credential of the entity with an updated {@link CredentialModel}.
     * Usually called by a {@link org.keycloak.credential.CredentialProvider}, or from the account management
     * when a user removes, for example, an OTP token.
     */
    boolean removeStoredCredentialById(String id);

    /**
     * 按 ID 读取存储凭据。
     * Read a stored credential.
     */
    CredentialModel getStoredCredentialById(String id);

    /**
     * 以流形式读取全部存储凭据。
     * Read stored credentials as a stream.
     */
    Stream<CredentialModel> getStoredCredentialsStream();

    /**
     * 以流形式返回联合存储凭据。
     * Returns a stream consisting of the federated credentials.
     *
     * @return a stream consisting of the federated credentials
     */
    default Stream<CredentialModel> getFederatedCredentialsStream() {
        return Stream.empty();
    }

    /**
     * 以流形式返回本地与联合凭据。
     * Returns a stream consisting of both local and federated credentials.
     *
     * @return a stream of both local and federated credentials
     */
    default Stream<CredentialModel> getCredentials() {
        return Stream.concat(getStoredCredentialsStream(), getFederatedCredentialsStream());
    }

    /**
     * 按类型以流形式读取存储凭据。
     * Read stored credentials by type as a stream.
     */
    Stream<CredentialModel> getStoredCredentialsByTypeStream(String type);

    /** @param name 凭据名称
     * @param type 凭据类型
     * @return 匹配的存储凭据 */
    CredentialModel getStoredCredentialByNameAndType(String name, String type);

    /**
     * 调整存储凭据顺序。
     * Re-order the stored credentials.
     */
    boolean moveStoredCredentialTo(String id, String newPreviousCredentialId);

    /**
     * 更新实体所有者设置的凭据标签。
     * Update the label for a stored credentials chosen by the owner of the entity.
     */
    void updateCredentialLabel(String credentialId, String credentialLabel);

    /**
     * 按类型禁用凭据。
     * Disable a credential by type.
     */
    void disableCredentialType(String credentialType);

    /**
     * 列出可禁用的凭据类型。
     * List the credentials that can be disabled, for example, to show the list to the entity (aka user) or an admin.
     * @return stream with credential types that can be disabled
     */
    Stream<String> getDisableableCredentialTypesStream();

       /**
     * 以流形式返回第一因素凭据。
     * Returns a stream consisting of the first-factor credentials.
     *
     * @return a stream consisting of the first-factor credentials
     */
    default Stream<CredentialModel> getFirstFactorCredentialsStream() {
        return getStoredCredentialsStream();
    }

    /**
     * 检查实体是否已配置指定类型的凭据。
     * Check if the credential type is configured for this entity.
     * @param type credential type to check
     * @return <code>true</code> if the credential type has been
     */
    boolean isConfiguredFor(String type);

    /** @deprecated 本地配置检查（将移除） */
    // TODO: not needed for new store? -> no, will be removed without replacement
    @Deprecated
    boolean isConfiguredLocally(String type);

    /** @deprecated 已配置的用户存储凭据类型流（将移除） */
    // TODO: not needed for new store? -> no, will be removed without replacement
    @Deprecated
    Stream<String> getConfiguredUserStorageCredentialTypesStream();

    /** @deprecated 通过 Provider 创建凭据（将移除） */
    // TODO: not needed for new store? -> no, will be removed without replacement
    @Deprecated
    CredentialModel createCredentialThroughProvider(CredentialModel model);
}
