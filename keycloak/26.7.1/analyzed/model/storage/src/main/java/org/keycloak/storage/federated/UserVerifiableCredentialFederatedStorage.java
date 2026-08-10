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

import org.keycloak.models.IssuedVerifiableCredentialModel;
import org.keycloak.models.UserVerifiableCredentialModel;

/**
 * 联邦用户可验证凭证（Verifiable Credential）存储接口。
 *
 * <p>管理用户持有的 VC 配置及已签发的 VC 记录，支持按用户、客户端作用域查询与过期清理。
 */
public interface UserVerifiableCredentialFederatedStorage {

    /**
     * 为联邦用户添加可验证凭证配置。
     *
     * @param userId 联邦用户 ID
     * @param credentialModel 待添加的凭证模型
     * @return 已填充生成字段的凭证模型
     */
    UserVerifiableCredentialModel addVerifiableCredential(String userId, UserVerifiableCredentialModel credentialModel);

    /**
     * 更新联邦用户的可验证凭证配置。
     *
     * @param userId 联邦用户 ID
     * @param clientScopeId 客户端作用域 ID
     * @return 更新后的凭证模型
     */
    UserVerifiableCredentialModel updateVerifiableCredential(String userId, String clientScopeId);

    /**
     * 移除联邦用户的可验证凭证配置。
     *
     * @param userId 联邦用户 ID
     * @param clientScopeId 待移除的客户端作用域 ID
     * @return 成功移除返回 true，未找到返回 false
     */
    boolean removeVerifiableCredential(String userId, String clientScopeId);

    /**
     * 获取联邦用户的全部可验证凭证配置。
     *
     * @param userId 联邦用户 ID
     * @return 可验证凭证流
     */
    Stream<UserVerifiableCredentialModel> getVerifiableCredentialsByUser(String userId);

    /**
     * 按用户与客户端作用域获取可验证凭证配置。
     *
     * @param userId 联邦用户 ID
     * @param clientScopeId 客户端作用域 ID
     * @return 可验证凭证模型；未找到时返回 {@code null}
     */
    UserVerifiableCredentialModel getVerifiableCredentialByClientScope(String userId, String clientScopeId);

    /**
     * 按 ID 获取可验证凭证配置。
     *
     * @param id 可验证凭证 ID
     * @return 可验证凭证模型；未找到时返回 {@code null}
     */
    UserVerifiableCredentialModel getVerifiableCredentialById(String id);

    /**
     * 为联邦用户添加已签发的可验证凭证记录。
     *
     * @param issuedVc 待添加的已签发凭证
     * @return 已填充生成字段的已签发凭证
     */
    IssuedVerifiableCredentialModel addIssuedVerifiableCredential(IssuedVerifiableCredentialModel issuedVc);

    /**
     * 获取联邦用户的全部已签发可验证凭证。
     *
     * @param userId 联邦用户 ID
     * @return 已签发可验证凭证流
     */
    Stream<IssuedVerifiableCredentialModel> getIssuedVerifiableCredentialsStreamByUser(String userId);

    /**
     * 按 ID 移除已签发的可验证凭证。
     *
     * @param issuedCredentialId 待移除的已签发凭证 ID
     * @return 成功移除返回 true，未找到返回 false
     */
    boolean removeIssuedVerifiableCredential(String issuedCredentialId);

    /**
     * 清理所有用户中已过期的已签发可验证凭证。
     */
    void removeExpiredIssuedVerifiableCredentials();

}
