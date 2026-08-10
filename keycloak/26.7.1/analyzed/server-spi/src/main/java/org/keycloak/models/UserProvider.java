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

package org.keycloak.models;

import java.util.stream.Stream;

import org.keycloak.component.ComponentModel;
import org.keycloak.provider.Provider;
import org.keycloak.storage.user.UserBulkUpdateProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;

/**
 * 用户 Provider：用户 CRUD、同意、联邦身份与可验证凭据的存储 SPI。
 * <p>聚合 {@link UserLookupProvider}、{@link UserQueryProvider} 等子接口。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface UserProvider extends Provider,
        UserLookupProvider,
        UserQueryProvider,
        UserRegistrationProvider,
        UserBulkUpdateProvider {

    /**
     * 设置用户的 notBefore 值。
     * Sets the notBefore value for the given user
     *
     * @param realm a reference to the realm
     * @param user the user model
     * @param notBefore new value for notBefore
     *
     * @throws ModelException when user doesn't exist in the storage
     */
    void setNotBeforeForUser(RealmModel realm, UserModel user, int notBefore);

    /**
     * 获取用户的 notBefore 值。
     * Gets the notBefore value for the given user
     *
     * @param realm a reference to the realm
     * @param user the user model
     * @return the value of notBefore
     *
     * @throws ModelException when user doesn't exist in the storage
     */
    int getNotBeforeOfUser(RealmModel realm, UserModel user);

    /**
     * 返回客户端对应的服务账户 {@link UserModel}。
     * Return a UserModel representing service account of the client
     *
     * @param client the client model
     * @throws IllegalArgumentException when there are more service accounts associated with the given clientId
     * @return userModel representing service account of the client
     */
    UserModel getServiceAccount(ClientModel client);

    /**
     * 向存储新增用户（仅用于本地存储）。
     * Adds a new user into the storage.
     * <p/>
     * only used for local storage
     *
     * @param realm the realm that user will be created in
     * @param id id of the new user. Should be generated to a random value if {@code null}.
     * @param username username
     * @param addDefaultRoles if {@code true}, the user should join all realm default roles
     * @param addDefaultRequiredActions if {@code true}, all default required actions are added to the created user
     * @return model of created user
     *
     * @throws NullPointerException when username or realm is {@code null}
     * @throws ModelDuplicateException when a user with given id or username already exists
     */
    UserModel addUser(RealmModel realm, String id, String username, boolean addDefaultRoles, boolean addDefaultRequiredActions);

    /**
     * 移除指定用户存储 Provider 导入的全部用户。
     * Removes any imported users from a specific User Storage Provider.
     *
     * @param realm a reference to the realm
     * @param storageProviderId id of the user storage provider
     */
    void removeImportedUsers(RealmModel realm, String storageProviderId);

    /**
     * 将指定存储 Provider 导入用户的 federation link 置为 null。
     * Set federation link to {@code null} to imported users of a specific User Storage Provider
     *
     * @param realm a reference to the realm
     * @param storageProviderId id of the storage provider
     */
    void unlinkUsers(RealmModel realm, String storageProviderId);

    /* 用户 OAuth 同意相关方法 */
    /* USER CONSENTS methods */

    /**
     * 为用户添加 OAuth 同意记录。
     * Add user consent for the user.
     *
     * @param realm a reference to the realm
     * @param userId id of the user
     * @param consent all details corresponding to the granted consent
     *
     * @throws ModelException If there is no user with userId
     */
    void addConsent(RealmModel realm, String userId, UserConsentModel consent);

    /**
     * 按用户 ID 与客户端内部 ID 返回用户同意模型。
     * Returns UserConsentModel given by a user with the userId for the client with clientInternalId
     *
     * @param realm a reference to the realm
     * @param userId id of the user
     * @param clientInternalId id of the client
     * @return consent given by the user to the client or {@code null} if no consent or user exists
     *
     * @throws ModelException when there are more consents fulfilling specified parameters
     */
    UserConsentModel getConsentByClient(RealmModel realm, String userId, String clientInternalId);

    /**
     * 获取指定用户的全部同意记录。
     * Obtains the consents associated with the user identified by the specified {@code userId}.
     *
     * @param realm a reference to the realm.
     * @param userId the user identifier.
     * @return a non-null {@link Stream} of consents associated with the user.
     */
    Stream<UserConsentModel> getConsentsStream(RealmModel realm, String userId);

    /**
     * 更新已存储用户同意中的客户端范围。
     * Update client scopes in the stored user consent
     *
     * @param realm a reference to the realm
     * @param userId id of the user
     * @param consent new details of the user consent
     *
     * @throws ModelException when consent doesn't exist for the userId
     */
    void updateConsent(RealmModel realm, String userId, UserConsentModel consent);

    /**
     * 按用户 ID 与客户端 ID 撤销用户同意。
     * Remove a user consent given by the user id and client id
     *
     * @param realm a reference to the realm
     * @param userId id of the user
     * @param clientInternalId id of the client
     * @return {@code true} if the consent was removed, {@code false} otherwise
     *
     * TODO: Make this method return Boolean so that store can return "I don't know" answer, this can be used for example in async stores
     */
    boolean revokeConsentForClient(RealmModel realm, String userId, String clientInternalId);

    /**
     * 为用户创建指定客户端范围的可验证凭据。
     * Create verifiable credential of specified credential scope for this user
     *
     * @param userId id of the user
     * @param credentialModel credential model with "clientScopeId" set. The other fields will be generated if not set
     * @return credentialModel with all the fields set
     */
    UserVerifiableCredentialModel addVerifiableCredential(String userId, UserVerifiableCredentialModel credentialModel);

    /**
     * 移除用户指定客户端范围的可验证凭据。
     * Remove verifiable credential of specified client scope from this user
     *
     * @param userId id if the user
     * @param clientScopeId client scope ID to delete
     * @return true if credential was successfully removed. False otherwise
     */
    boolean removeVerifiableCredential(String userId, String clientScopeId);

    /**
     * 返回用户的全部可验证凭据。
     * Return all verifiable credentials of specified user
     *
     * @param userId id if the user
     * @return a non-null {@link Stream} of all verifiable credentials of specified user
     */
    Stream<UserVerifiableCredentialModel> getVerifiableCredentialsByUser(String userId);

    /**
     * 按 ID 获取可验证凭据。
     * Get a verifiable credential by its ID
     *
     * @param id the verifiable credential ID
     * @return the credential model, or null if not found
     */
    UserVerifiableCredentialModel getVerifiableCredentialById(String id);

    /**
     * 按用户 ID 与客户端范围 ID 获取可验证凭据。
     * Get a verifiable credential for a user by client scope ID
     *
     * @param userId id of the user
     * @param clientScopeId client scope ID
     * @return the credential model, or null if not found
     */
    UserVerifiableCredentialModel getVerifiableCredentialByClientScope(String userId, String clientScopeId);

    /**
     * 刷新用户属性快照并递增 revision 以更新可验证凭据。
     * Update verifiable credential by refreshing user attributes snapshot and incrementing revision
     *
     * @param userId id of the user
     * @param clientScopeId client scope ID to update
     * @return updated credential model
     * @throws ModelException if credential doesn't exist
     */
    UserVerifiableCredentialModel updateVerifiableCredential(String userId, String clientScopeId);

    /* 联邦身份相关方法 */
    /* FEDERATED IDENTITIES methods */

    /**
     * 在 realm 内为用户添加联邦身份关联。
     * Adds a federated identity link for the user within the realm
     *
     * @param realm a reference to the realm
     * @param user the user model
     * @param socialLink the federated identity model containing all details of the association between the user and
     *                   the identity provider
     */
    void addFederatedIdentity(RealmModel realm, UserModel user, FederatedIdentityModel socialLink);

    /**
     * 移除用户与指定 IdP 的联邦关联。
     * Removes federation link between the user and the identity provider given by its id
     *
     * @param realm a reference to the realm
     * @param user the user model
     * @param socialProvider alias of the identity provider, see {@link IdentityProviderModel#getAlias()}
     * @return {@code true} if the association was removed, {@code false} otherwise
     *
     * TODO: Make this method return Boolean so that store can return "I don't know" answer, this can be used for example in async stores
     */
    boolean removeFederatedIdentity(RealmModel realm, UserModel user, String socialProvider);

    /**
     * 更新联邦用户与 IdP 的关联详情。
     * Update details of association between the federatedUser and the idp given by the federatedIdentityModel
     *
     * @param realm a reference to the realm
     * @param federatedUser the user model
     * @param federatedIdentityModel the federated identity model containing all details of the association between
     *                               the user and the identity provider
     */
    void updateFederatedIdentity(RealmModel realm, UserModel federatedUser, FederatedIdentityModel federatedIdentityModel);

    /**
     * 获取用户的全部联邦身份。
     * Obtains the federated identities of the specified user.
     *
     * @param realm a reference to the realm.
     * @param user the reference to the user.
     * @return a non-null {@link Stream} of federated identities associated with the user.
     */
    Stream<FederatedIdentityModel> getFederatedIdentitiesStream(RealmModel realm, UserModel user);

    /**
     * 返回用户与 IdP 的关联详情。
     * Returns details of the association between the user and the socialProvider.
     *
     * @param realm a reference to the realm
     * @param user the user model
     * @param socialProvider the id of the identity provider
     * @return federatedIdentityModel or {@code null} if no association exists
     */
    FederatedIdentityModel getFederatedIdentity(RealmModel realm, UserModel user, String socialProvider);

    /**
     * 按联邦身份链接查找对应用户。
     * Returns a userModel that corresponds to the given socialLink.
     *
     * @param realm a reference to the realm
     * @param socialLink the socialLink
     * @return the user corresponding to socialLink and {@code null} if no such user exists
     *
     * @throws IllegalStateException when there are more users for the given socialLink
     */
    UserModel getUserByFederatedIdentity(RealmModel realm, FederatedIdentityModel socialLink);

    /* 预删除回调：关联实体删除时清理用户相关数据 */
    /* PRE REMOVE methods - for cleaning user related properties when some other entity is removed */

    /**
     * realm 删除时调用，应移除该 realm 全部用户。
     * Called when a realm is removed.
     * Should remove all users that belong to the realm.
     *
     * @param realm a reference to the realm
     */
    void preRemove(RealmModel realm);

    /**
     * IdP 删除时调用，应移除相关联邦身份。
     * Called when an identity provider is removed.
     * Should remove all federated identities assigned to users from the provider.
     *
     * @param realm a reference to the realm
     * @param provider provider model
     */
    void preRemove(RealmModel realm, IdentityProviderModel provider);

    /**
     * 角色删除时调用，应移除用户的该角色成员关系。
     * Called when a role is removed.
     * Should remove the role membership for each user.
     *
     * @param realm a reference to the realm
     * @param role the role model
     */
    void preRemove(RealmModel realm, RoleModel role);

    /**
     * 组删除时调用，应移除用户的该组成员关系。
     * Called when a group is removed.
     * Should remove the group membership for each user.
     *
     * @param realm a reference to the realm
     * @param group the group model
     */
    void preRemove(RealmModel realm, GroupModel group);

    /**
     * 客户端删除时调用，应移除与该客户端相关的用户同意。
     * Called when a client is removed.
     * Should remove all user consents associated with the client
     *
     * @param realm a reference to the realm
     * @param client the client model
     */
    void preRemove(RealmModel realm, ClientModel client);

    /**
     * 协议映射器删除时调用。
     * Called when a protocolMapper is removed
     *
     * @param protocolMapper the protocolMapper model
     */
    void preRemove(ProtocolMapperModel protocolMapper);

    /**
     * 客户端范围删除时调用，应从用户同意中移除该范围。
     * Called when a client scope is removed.
     * Should remove the clientScope from each user consent
     *
     * @param clientScope the clientScope model
     */
    void preRemove(ClientScopeModel clientScope);

    /**
     * 组件删除时调用，应清理关联的用户存储数据。
     * Called when a component is removed.
     * Should remove all data in UserStorage associated with removed component.
     * For example,
     * <ul>
     *     <li>if component corresponds to UserStorageProvider all imported users from the provider should be removed,</li>
     *     <li>if component corresponds to ClientStorageProvider all consents granted for clients imported from the
     *     provider should be removed</li>
     * </ul>
     *
     * @param realm a reference to the realm
     * @param component the component model
     */
    void preRemove(RealmModel realm, ComponentModel component);

    /**
     * 返回适用于大多数用户 Provider 的 {@link SubjectCredentialManager} 默认实现。
     * Default implementation of {@link SubjectCredentialManager} suitable for most of user providers
     *
     * @return user credential manager
     */
    UserCredentialManager getUserCredentialManager(UserModel user);

    /**
     * 记录已向用户签发的可验证凭据。
     * Record that a verifiable credential was issued to a user.
     *
     * @param issuedVc model with userId, clientId, verifiableCredentialId set
     * @return issuedVerifiableCredentialModel with all the fields set (including ID)
     */
    IssuedVerifiableCredentialModel addIssuedVerifiableCredential(IssuedVerifiableCredentialModel issuedVc);

    /**
     * 获取指定用户的全部已签发可验证凭据。
     * Get all issued verifiable credentials for a specific user.
     *
     * @param userId user ID
     * @return stream of issued verifiable credentials, sorted by issuedAt descending
     */
    Stream<IssuedVerifiableCredentialModel> getIssuedVerifiableCredentialsStreamByUser(String userId);

    /**
     * 按 ID 移除已签发的可验证凭据。
     * Remove an issued verifiable credential by its ID.
     *
     * @param credentialId the ID of the issued credential to remove
     * @return {@code true} if the credential was removed, {@code false} if it was not found
     */
    boolean removeIssuedVerifiableCredential(String credentialId);

    /**
     * 移除所有 realm 中已过期的已签发可验证凭据。
     * Remove all expired issued verifiable credentials across all realms.
     * This is called periodically by the scheduled cleanup task.
     */
    void removeExpiredIssuedVerifiableCredentials();

}
