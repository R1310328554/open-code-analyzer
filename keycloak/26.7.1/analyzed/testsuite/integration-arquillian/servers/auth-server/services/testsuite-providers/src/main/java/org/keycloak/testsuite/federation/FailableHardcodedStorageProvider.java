/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.utils.UserModelDelegate;
import org.keycloak.storage.UserStoragePrivateUtil;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.ImportedUserValidation;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;

/**
 * 可配置失败模式的硬编码用户存储提供者，用于测试导入、凭据与查询路径的异常处理。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class FailableHardcodedStorageProvider implements UserStorageProvider, UserLookupProvider, UserQueryProvider,
        ImportedUserValidation, CredentialInputUpdater, CredentialInputValidator {

    /** 硬编码测试用户名。 */
    public static String username = "billb";
    /** 硬编码测试密码。 */
    public static String password = "password";
    /** 硬编码测试邮箱。 */
    public static String email = "billb@nowhere.com";
    /** 硬编码名字。 */
    public static String first = "Bill";
    /** 硬编码姓氏。 */
    public static String last = "Burke";
    /** 硬编码用户属性映射。 */
    public static MultivaluedHashMap<String, String> attributes = new MultivaluedHashMap<>();

    /** 全局静态失败开关。 */
    public static boolean fail;

    /** 关联的组件模型。 */
    protected ComponentModel model;
    /** 当前 Keycloak 会话。 */
    protected KeycloakSession session;
    /** 组件级失败模式是否启用。 */
    protected boolean componentFail;
    /** 是否在用户验证阶段强制失败。 */
    private boolean failOnValidation;

    /**
     * @param model 用户存储组件模型
     * @param session Keycloak 会话
     * @param failOnValidation 验证阶段是否强制失败
     */
    public FailableHardcodedStorageProvider(ComponentModel model, KeycloakSession session, boolean failOnValidation) {
        this.model = model;
        this.session = session;
        componentFail = isInFailMode(model);
        this.failOnValidation = failOnValidation;
    }

    /** 判断组件配置是否启用了失败模式。 */
    public static boolean isInFailMode(ComponentModel model) {
        return model.getConfig().getFirst("fail") != null && model.getConfig().getFirst("fail").equalsIgnoreCase("true");
    }

    /** {@inheritDoc} 仅支持密码凭据类型。 */
    @Override
    public boolean supportsCredentialType(String credentialType) {
        checkForceFail();
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    /** {@inheritDoc} 更新硬编码用户的密码。 */
    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
        checkForceFail();
        if (!(input instanceof UserCredentialModel)) return false;
        if (!user.getUsername().equals(username)) throw new RuntimeException("UNKNOWN USER!");

        if (input.getType().equals(PasswordCredentialModel.TYPE)) {
            password = input.getChallengeResponse();
            return true;

        } else {
            return false;
        }
    }

    /** {@inheritDoc} 禁用凭据类型；当前实现为空操作。 */
    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
        checkForceFail();

    }

    /** {@inheritDoc} 返回可禁用的凭据类型流（当前为空）。 */
    @Override
    public Stream<String> getDisableableCredentialTypesStream(RealmModel realm, UserModel user) {
        checkForceFail();
        return Stream.empty();
    }

    /** {@inheritDoc} 密码凭据始终视为已配置。 */
    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        checkForceFail();
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    /** {@inheritDoc} 校验硬编码用户密码。 */
    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        checkForceFail();
        if (!user.getUsername().equals("billb")) throw new RuntimeException("UNKNOWN USER!");
        if (credentialInput.getType().equals(PasswordCredentialModel.TYPE)) {
            return password != null && password.equals(credentialInput.getChallengeResponse());
        } else {
            return false;
        }
    }

    /** 用户模型委托，同步更新静态硬编码字段。 */
    private static class Delegate extends UserModelDelegate {
        public Delegate(UserModel delegate) {
            super(delegate);
        }

        /** {@inheritDoc} 同步更新静态用户名。 */
        @Override
        public void setUsername(String name) {
            super.setUsername(name);
            username = name;
        }

        /** {@inheritDoc} 同步更新静态单值属性。 */
        @Override
        public void setSingleAttribute(String name, String value) {
            super.setSingleAttribute(name, value);
            attributes.putSingle(name, value);
        }

        /** {@inheritDoc} 同步更新静态多值属性。 */
        @Override
        public void setAttribute(String name, List<String> values) {
            super.setAttribute(name, values);
            attributes.put(name, values);
        }

        /** {@inheritDoc} 同步移除静态属性。 */
        @Override
        public void removeAttribute(String name) {
            super.removeAttribute(name);
            attributes.remove(name);
        }

        /** {@inheritDoc} 同步更新静态名字。 */
        @Override
        public void setFirstName(String firstName) {
            super.setFirstName(firstName);
            first = firstName;
        }

        /** {@inheritDoc} 同步更新静态姓氏。 */
        @Override
        public void setLastName(String lastName) {
            super.setLastName(lastName);
            last = lastName;
        }

        /** {@inheritDoc} 同步更新静态邮箱。 */
        @Override
        public void setEmail(String em) {
            super.setEmail(em);
            email = em;
        }
    }

    /** {@inheritDoc} 验证导入用户，可选强制抛出验证失败。 */
    @Override
    public UserModel validate(RealmModel realm, UserModel user) {
        checkForceFail();
        if (failOnValidation) {
            throw new RuntimeException("Forcing validation failure");
        }
        return new Delegate(user);
    }

    /** {@inheritDoc} 按 ID 查找不应被调用。 */
    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        checkForceFail();
        throw new RuntimeException("THIS IMPORTS  SHOULD NEVER BE CALLED");
    }

    /** {@inheritDoc} 按用户名导入或返回硬编码用户。 */
    @Override
    public UserModel getUserByUsername(RealmModel realm, String uname) {
        checkForceFail();
        if (!username.equals(uname)) return null;
        UserModel local = UserStoragePrivateUtil.userLocalStorage(session).getUserByUsername(realm, uname);
        if (local != null && !model.getId().equals(local.getFederationLink())) {
            throw new RuntimeException("local storage has wrong federation link");
        }
        if (local != null) return new Delegate(local);
        local = UserStoragePrivateUtil.userLocalStorage(session).addUser(realm, uname);
        local.setEnabled(true);
        local.setFirstName(first);
        local.setLastName(last);
        local.setEmail(email);
        local.setFederationLink(model.getId());
        for (var entry : attributes.entrySet()) {
            List<String> values = entry.getValue();
            if (values == null) continue;
            local.setAttribute(entry.getKey(), values);
        }
        return new Delegate(local);
    }

    /** {@inheritDoc} 本提供者不支持按邮箱查找。 */
    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        checkForceFail();
        return null;
    }

    /** 若全局或组件级失败开关启用，则抛出强制失败异常。 */
    protected void checkForceFail() {
        if (fail || componentFail) throwFailure();
    }

    /** 抛出统一的强制失败运行时异常。 */
    public static  void throwFailure() {
        throw new RuntimeException("FORCED FAILURE");
    }

    /** {@inheritDoc} 硬编码用户数量恒为 1。 */
    @Override
    public int getUsersCount(RealmModel realm) {
        checkForceFail();
        return 1;
    }

    /** {@inheritDoc} 按搜索词查找硬编码用户。 */
    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search) {
        checkForceFail();
        if (!search.equals(username)) return Stream.empty();
        UserModel model = getUserByUsername(realm, username);
        return model != null ? Stream.of(model) : Stream.empty();
    }

    /** {@inheritDoc} 带分页的搜索实现。 */
    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search, Integer firstResult, Integer maxResults) {
        checkForceFail();
        if (!search.equals(username)) return Stream.empty();
        UserModel model = getUserByUsername(realm, username);
        return model != null ? Stream.of(model) : Stream.empty();
    }

    /** {@inheritDoc} 按参数映射搜索用户。 */
    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params) {
        checkForceFail();
        if (!username.equals(params.get("username")))return Stream.empty();
        UserModel model = getUserByUsername(realm, username);
        return model != null ? Stream.of(model) : Stream.empty();
    }

    /** {@inheritDoc} 带分页的参数映射搜索。 */
    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult, Integer maxResults) {
        checkForceFail();
        if (!username.equals(params.get("username")))return Stream.empty();
        UserModel model = getUserByUsername(realm, username);
        return model != null ? Stream.of(model) : Stream.empty();
    }

    /** {@inheritDoc} 组成员查询（当前不支持）。 */
    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult, Integer maxResults) {
        checkForceFail();
        return Stream.empty();
    }

    /** {@inheritDoc} 组成员查询（当前不支持）。 */
    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group) {
        checkForceFail();
        return Stream.empty();
    }

    /** {@inheritDoc} 按用户属性搜索（当前不支持）。 */
    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
        checkForceFail();
        return Stream.empty();
    }

    /** {@inheritDoc} 关闭提供者；当前实现无需额外清理。 */
    @Override
    public void close() {

    }
}
