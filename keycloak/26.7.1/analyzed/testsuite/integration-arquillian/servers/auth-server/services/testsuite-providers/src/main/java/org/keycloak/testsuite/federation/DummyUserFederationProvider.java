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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.OTPCredentialModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.UserStoragePrivateUtil;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserRegistrationProvider;

/**
 * 虚拟用户联邦提供者，用于集成测试中的用户查找、注册与凭据校验。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class DummyUserFederationProvider implements UserStorageProvider,
        UserLookupProvider,
        UserRegistrationProvider,
        CredentialInputValidator {

    /** 内存用户映射，键为用户名。 */
    private final Map<String, UserModel> users;
    /** 当前 Keycloak 会话。 */
    private KeycloakSession session;
    /** 关联的组件模型。 */
    private ComponentModel component;

    /** 测试用户 {@code test-user} 的硬编码密码。 */
    public static final String HARDCODED_PASSWORD = "secret";

    /** 测试用户 {@code test-user} 的硬编码 OTP，始终视为有效。 */
    public static final String HARDCODED_OTP = "123456";



    /**
     * @param session Keycloak 会话
     * @param component 用户存储组件模型
     * @param users 共享内存用户映射
     */
    public DummyUserFederationProvider(KeycloakSession session, ComponentModel component, Map<String, UserModel> users) {
        this.users = users;
        this.session = session;
        this.component = component;
    }



    /** {@inheritDoc} 在本地存储中创建用户并设置联邦链接。 */
    @Override
    public UserModel addUser(RealmModel realm, String username) {
        UserModel local = UserStoragePrivateUtil.userLocalStorage(session).addUser(realm, username);
        local.setFederationLink(component.getId());

        users.put(username, local);
        return local;
    }

    /** {@inheritDoc} 从内存映射中移除用户。 */
    @Override
    public boolean removeUser(RealmModel realm, UserModel user) {
        return users.remove(user.getUsername()) != null;
    }

    /** {@inheritDoc} 本提供者不支持按 ID 查找。 */
    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        return null;
    }

    /** {@inheritDoc} 按用户名从内存映射查找用户。 */
    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        return users.get(username);
    }

    /** {@inheritDoc} 本提供者不支持按邮箱查找。 */
    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        return null;
    }

    /** {@inheritDoc} Realm 删除前的预清理钩子。 */
    @Override
    public void preRemove(RealmModel realm) {

    }

    /** {@inheritDoc} 角色删除前的预清理钩子。 */
    @Override
    public void preRemove(RealmModel realm, RoleModel role) {

    }

    /** {@inheritDoc} 组删除前的预清理钩子。 */
    @Override
    public void preRemove(RealmModel realm, GroupModel group) {

    }

    /** 返回本提供者支持的凭据类型集合。 */
    public Set<String> getSupportedCredentialTypes() {
        return new HashSet<>(Arrays.asList(PasswordCredentialModel.TYPE, OTPCredentialModel.TYPE));
    }

    /** {@inheritDoc} 判断凭据类型是否在支持列表中。 */
    @Override
    public boolean supportsCredentialType(String credentialType) {
        return getSupportedCredentialTypes().contains(credentialType);
    }

    /** {@inheritDoc} 仅 {@code test-user} 视为已配置对应凭据类型。 */
    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        if (!supportsCredentialType(credentialType)) return false;

        if (user.getUsername().equals("test-user")) {
            return true;
        } else {
            return false;
        }
    }

    /** {@inheritDoc} 对 {@code test-user} 校验硬编码密码或 OTP。 */
    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        if (user.getUsername().equals("test-user")) {
            if (PasswordCredentialModel.TYPE.equals(credentialInput.getType())) {
                return HARDCODED_PASSWORD.equals(credentialInput.getChallengeResponse());
            } else if (OTPCredentialModel.TYPE.equals(credentialInput.getType())) {
                return HARDCODED_OTP.equals(credentialInput.getChallengeResponse());
            }
        }
        return false;
    }

     /** {@inheritDoc} 关闭提供者；当前实现无需额外清理。 */
     @Override
    public void close() {

    }
}
