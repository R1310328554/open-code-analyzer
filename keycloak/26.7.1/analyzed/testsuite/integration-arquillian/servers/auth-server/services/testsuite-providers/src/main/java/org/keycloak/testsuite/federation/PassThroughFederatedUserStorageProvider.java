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
package org.keycloak.testsuite.federation;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.UserStorageUtil;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;
import org.keycloak.storage.user.UserLookupProvider;

/**
 * 透传联邦用户存储提供者：提供单一用户 {@code passthrough}，全部属性与凭据存于用户联邦存储。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PassThroughFederatedUserStorageProvider implements
        UserStorageProvider,
        UserLookupProvider,
        CredentialInputValidator,
        CredentialInputUpdater
{

    /** 支持的凭据类型集合（仅密码）。 */
    public static final Set<String> CREDENTIAL_TYPES = Collections.singleton(PasswordCredentialModel.TYPE);
    /** 透传测试用户名。 */
    public static final String PASSTHROUGH_USERNAME = "passthrough";
    /** 初始明文密码。 */
    public static final String INITIAL_PASSWORD = "secret";
    /** 当前 Keycloak 会话。 */
    private KeycloakSession session;
    /** 用户存储组件模型。 */
    private ComponentModel component;

    /**
     * @param session Keycloak 会话
     * @param component 组件模型
     */
    public PassThroughFederatedUserStorageProvider(KeycloakSession session, ComponentModel component) {
        this.session = session;
        this.component = component;
    }

    public Set<String> getSupportedCredentialTypes() {
        return CREDENTIAL_TYPES;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return getSupportedCredentialTypes().contains(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        if (!PasswordCredentialModel.TYPE.equals(credentialType)) return false;
        return true;
    }

    /** {@inheritDoc} 校验初始密码或联邦存储中的明文密码凭据。 */
    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (input.getType().equals(PasswordCredentialModel.TYPE)) {
             if (INITIAL_PASSWORD.equals(input.getChallengeResponse())) {
                 return true;
             }
            return UserStorageUtil.userFederatedStorage(session).getStoredCredentialsByTypeStream(realm, user.getId(), "CLEAR_TEXT_PASSWORD")
                    .map(credentialModel -> credentialModel.getSecretData())
                    .anyMatch(Predicate.isEqual("{\"value\":\"" + input.getChallengeResponse() + "\"}"));
        }
        return false;
    }

    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
        // 测试联邦凭据属性：在联邦存储中创建或更新明文密码
        if (input.getType().equals(PasswordCredentialModel.TYPE)) {
            Optional<CredentialModel> existing = UserStorageUtil.userFederatedStorage(session)
                    .getStoredCredentialsByTypeStream(realm, user.getId(), "CLEAR_TEXT_PASSWORD")
                    .findFirst();
            if (existing.isPresent()) {
                CredentialModel model = existing.get();
                model.setType("CLEAR_TEXT_PASSWORD");
                model.setSecretData("{\"value\":\"" + input.getChallengeResponse() + "\"}");
                UserStorageUtil.userFederatedStorage(session).updateCredential(realm, user.getId(), model);
            } else {
                CredentialModel model = new CredentialModel();
                model.setType("CLEAR_TEXT_PASSWORD");
                model.setSecretData("{\"value\":\"" + input.getChallengeResponse() + "\"}");
                UserStorageUtil.userFederatedStorage(session).createCredential(realm, user.getId(), model);
            }
            return true;
        }
        return false;
    }

    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
        UserStorageUtil.userFederatedStorage(session).getStoredCredentialsByTypeStream(realm, user.getId(), "CLEAR_TEXT_PASSWORD")
                .collect(Collectors.toList())
                .forEach(credModel -> UserStorageUtil.userFederatedStorage(session).removeStoredCredential(realm, user.getId(), credModel.getId()));
    }

    @Override
    public Stream<String> getDisableableCredentialTypesStream(RealmModel realm, UserModel user) {
        return CREDENTIAL_TYPES.stream();
    }

    @Override
    public void close() {

    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        if (!StorageId.externalId(id).equals(PASSTHROUGH_USERNAME)) return null;
        return getUserModel(realm);
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        if  (!PASSTHROUGH_USERNAME.equals(username)) return null;

        return getUserModel(realm);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        Optional<StorageId> result = UserStorageUtil.userFederatedStorage(session)
                .getUsersByUserAttributeStream(realm, AbstractUserAdapterFederatedStorage.EMAIL_ATTRIBUTE, email)
                .map(StorageId::new)
                .filter(storageId -> Objects.equals(storageId.getExternalId(), PASSTHROUGH_USERNAME))
                .filter(storageId -> Objects.equals(storageId.getProviderId(), component.getId()))
                .findFirst();
        return result.isPresent() ? getUserModel(realm) : null;
    }

    /** 构建透传用户的联邦存储适配器。 @param realm 所属领域 */
    private UserModel getUserModel(final RealmModel realm) {
        return new AbstractUserAdapterFederatedStorage.Streams(session, realm, component) {
            @Override
            public String getUsername() {
                return PASSTHROUGH_USERNAME;
            }

            @Override
            public void setUsername(String username) {
            }
        };
    }
}
