/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.testsuite.federation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.keycloak.common.util.Time;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.hash.PasswordHashProvider;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.OTPPolicy;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.cache.UserCache;
import org.keycloak.models.credential.PasswordUserCredentialModel;
import org.keycloak.models.credential.RecoveryAuthnCodesCredentialModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.TimeBasedOTP;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.UserStorageUtil;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;
import org.keycloak.util.JsonSerialization;

import org.jboss.logging.Logger;

/**
 * 基于 Keycloak 4.8.3 实现的 UserStorage，用于向后兼容性测试。后续 Keycloak 版本
 * 应能在不修改本提供者源码的情况下正常工作。
 * <p>
 * TODO: 建立可靠机制，确保本提供者源码与 Keycloak 4.8.3 真正兼容
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class BackwardsCompatibilityUserStorage implements UserLookupProvider, UserStorageProvider, UserRegistrationProvider,
        CredentialInputUpdater, CredentialInputValidator, UserQueryProvider {

    private static final Logger log = Logger.getLogger(BackwardsCompatibilityUserStorage.class);

    /** 内存用户映射，键为规范化用户名。 */
    protected final Map<String, MyUser> users;
    /** 关联的组件模型。 */
    protected final ComponentModel model;
    /** 当前 Keycloak 会话。 */
    protected final KeycloakSession session;

    /**
     * @param session Keycloak 会话
     * @param model 用户存储组件模型
     * @param users 共享内存用户映射
     */
    public BackwardsCompatibilityUserStorage(KeycloakSession session, ComponentModel model, Map<String, MyUser> users) {
        this.session = session;
        this.model = model;
        this.users = users;
    }

    /** 将用户名规范化为小写，与 4.8.3 行为保持一致。 */
    private static String translateUserName(String userName) {
        return userName == null ? null : userName.toLowerCase();
    }

    /** {@inheritDoc} 按存储 ID 解析外部用户名并查找用户。 */
    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        StorageId storageId = new StorageId(id);
        final String username = storageId.getExternalId();
        if (!users.containsKey(translateUserName(username))) return null;

        return createUser(realm, username);
    }

    /** 为给定用户名创建联邦存储用户适配器。 */
    private UserModel createUser(RealmModel realm, String username) {
        return new AbstractUserAdapterFederatedStorage(session, realm, model) {
            @Override
            public String getUsername() {
                return username;
            }

            @Override
            public void setUsername(String username1) {
                if (!username1.equals(username)) {
                    throw new RuntimeException("Unsupported to change username");
                }
            }
        };
    }

    /** {@inheritDoc} 判断是否支持密码、OTP 或恢复码凭据类型。 */
    @Override
    public boolean supportsCredentialType(String credentialType) {
        if (CredentialModel.PASSWORD.equals(credentialType)
                || isOTPType(credentialType)
                || credentialType.equals(RecoveryAuthnCodesCredentialModel.TYPE)) {
            return true;
        } else {
            log.infof("Unsupported credential type: %s", credentialType);
            return false;
        }
    }

    /** 判断凭据类型是否为 OTP 系列（OTP/HOTP/TOTP）。 */
    private boolean isOTPType(String credentialType) {
        return CredentialModel.OTP.equals(credentialType)
                || CredentialModel.HOTP.equals(credentialType)
                || CredentialModel.TOTP.equals(credentialType);
    }

    /** {@inheritDoc} 更新密码、OTP 或恢复码凭据，兼容 4.8.3 API 签名。 */
    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {
        if (!(input instanceof UserCredentialModel)) return false;

        if (input.getType().equals(UserCredentialModel.PASSWORD)) {

            // 与 4.8.3 兼容 — 使用旧版 PasswordUserCredentialModel 类型
            if (!(input instanceof PasswordUserCredentialModel)) {
                log.warn("Input is not PasswordUserCredentialModel");
                return false;
            }

            PasswordUserCredentialModel userCredentialModel = (PasswordUserCredentialModel) input;

            // 4.8.3 中调用密码凭据更新时不应设置这些字段
            assertNull(userCredentialModel.getDevice());
            assertNull(userCredentialModel.getAlgorithm());

            PasswordPolicy policy = session.getContext().getRealm().getPasswordPolicy();
            PasswordHashProvider hashProvider = getHashProvider(policy);

            CredentialModel newPassword = new CredentialModel();
            newPassword.setType(CredentialModel.PASSWORD);
            long createdDate = Time.currentTimeMillis();
            newPassword.setCreatedDate(createdDate);

            // 与 4.8.3 兼容 — 使用 hashProvider 的旧版 encode 方法签名
            hashProvider.encode(userCredentialModel.getValue(), policy.getHashIterations(), newPassword);

            // 验证凭据模型字段已正确填充
            assertNotNull(newPassword.getAlgorithm());
            assertNotNull(newPassword.getValue());
            assertNotNull(newPassword.getSalt());

            users.get(translateUserName(user.getUsername())).hashedPassword = newPassword;

            UserCache userCache = UserStorageUtil.userCache(session);
            if (userCache != null) {
                userCache.evict(realm, user);
            }
            return true;
        } else if (isOTPType(input.getType())) {
            UserCredentialModel otpCredential = (UserCredentialModel) input;

            // 4.8.3 中调用 OTP 凭据更新时不应设置这些字段
            assertNull(otpCredential.getDevice());
            assertNull(otpCredential.getAlgorithm());

            OTPPolicy otpPolicy = session.getContext().getRealm().getOTPPolicy();

            CredentialModel newOTP = new CredentialModel();
            newOTP.setId(KeycloakModelUtils.generateId());
            newOTP.setType(input.getType());
            long createdDate = Time.currentTimeMillis();
            newOTP.setCreatedDate(createdDate);
            newOTP.setValue(otpCredential.getValue());

            newOTP.setCounter(otpPolicy.getInitialCounter());
            newOTP.setDigits(otpPolicy.getDigits());
            newOTP.setAlgorithm(otpPolicy.getAlgorithm());
            newOTP.setPeriod(otpPolicy.getPeriod());

            users.get(translateUserName(user.getUsername())).otp = newOTP;

            return true;
        } else if (input.getType().equals(RecoveryAuthnCodesCredentialModel.TYPE)) {
            CredentialModel recoveryCodesModel = new CredentialModel();
            recoveryCodesModel.setId(KeycloakModelUtils.generateId());
            recoveryCodesModel.setType(input.getType());
            recoveryCodesModel.setCredentialData(input.getChallengeResponse());
            long createdDate = Time.currentTimeMillis();
            recoveryCodesModel.setCreatedDate(createdDate);
            users.get(translateUserName(user.getUsername())).recoveryCodes = recoveryCodesModel;
            return true;
        } else {
            log.infof("Attempt to update unsupported credential of type: %s", input.getType());
            return false;
        }
    }

    /** 根据 Realm 密码策略获取对应的密码哈希提供者。 */
    protected PasswordHashProvider getHashProvider(PasswordPolicy policy) {
        if (policy != null && policy.getHashAlgorithm() != null) {
            return session.getProvider(PasswordHashProvider.class, policy.getHashAlgorithm());
        } else {
            return session.getProvider(PasswordHashProvider.class);
        }
    }

    /** {@inheritDoc} 禁用 OTP 凭据类型。 */
    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
        if (isOTPType(credentialType)) {
            MyUser myUser = getMyUser(user);
            myUser.otp = null;
        } else {
            log.infof("Unsupported to disable credential of type: %s", credentialType);
        }
    }

    /** 从内存映射中获取与 UserModel 对应的 MyUser 记录。 */
    private MyUser getMyUser(UserModel user) {
        return users.get(translateUserName(user.getUsername()));
    }

    /** {@inheritDoc} 返回用户已配置的 OTP 与恢复码凭据流。 */
    @Override
    public Stream<CredentialModel> getCredentials(RealmModel realm, UserModel user) {
        var myUser = getMyUser(user);
        RecoveryAuthnCodesCredentialModel model;
        List<CredentialModel> credentialModels = new ArrayList<>();
        if (myUser.recoveryCodes != null) {
            try {
                model = RecoveryAuthnCodesCredentialModel.createFromValues(
                        JsonSerialization.readValue(myUser.recoveryCodes.getCredentialData(), List.class),
                        myUser.recoveryCodes.getCreatedDate(),
                        myUser.recoveryCodes.getUserLabel()
                );
                credentialModels.add(model);
            } catch (IOException e) {
                log.error("Could not deserialize  credential of type: recovery-codes");
            }
        }
        if (myUser.otp != null) {
            credentialModels.add(myUser.getOtp());
        }

        return credentialModels.stream();
    }

    /** {@inheritDoc} 返回可禁用的凭据类型（当前仅 OTP）。 */
    @Override
    public Stream<String> getDisableableCredentialTypesStream(RealmModel realm, UserModel user) {
        Set<String> types = new HashSet<>();

        MyUser myUser = getMyUser(user);
        if (myUser != null && myUser.otp != null) {
            types.add(CredentialModel.OTP);
        }

        return types.stream();
    }

    /** {@inheritDoc} 判断用户是否已配置指定凭据类型。 */
    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        // 始终假定密码凭据受支持
        if (CredentialModel.PASSWORD.equals(credentialType)) return true;
        MyUser myUser = getMyUser(user);
        if (myUser == null) return false;

        if (isOTPType(credentialType) && myUser.otp != null) {
            return true;
        } else if (credentialType.equals(RecoveryAuthnCodesCredentialModel.TYPE) && myUser.recoveryCodes != null) {
            return true;
        } else {
            log.infof("Not supported credentialType '%s' for user '%s'", credentialType, user.getUsername());
            return false;
        }
    }

    /** {@inheritDoc} 校验密码、OTP 或恢复码，兼容 4.8.3 verify 方法签名。 */
    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        MyUser myUser = users.get(translateUserName(user.getUsername()));
        if (myUser == null) return false;

        if (input.getType().equals(UserCredentialModel.PASSWORD)) {
            if (!(input instanceof PasswordUserCredentialModel)) return false;
            CredentialModel hashedPassword = myUser.hashedPassword;
            if (hashedPassword == null) {
                log.warnf("Password not set for user %s", user.getUsername());
                return false;
            }

            PasswordUserCredentialModel userCredentialModel = (PasswordUserCredentialModel) input;

            // 4.8.3 中校验密码凭据时不应设置这些字段
            assertNull(userCredentialModel.getDevice());
            assertNull(userCredentialModel.getAlgorithm());

            PasswordPolicy policy = session.getContext().getRealm().getPasswordPolicy();
            PasswordHashProvider hashProvider = getHashProvider(policy);

            String rawPassword = userCredentialModel.getValue();

            // 与 4.8.3 兼容 — 使用 hashProvider 的旧版 verify 方法签名
            return hashProvider.verify(rawPassword, hashedPassword);
        } else if (isOTPType(input.getType())) {
            UserCredentialModel otpCredential = (UserCredentialModel) input;

            // 特殊硬编码 OTP，始终视为有效
            if ("123456".equals(otpCredential.getValue())) {
                return true;
            }

            CredentialModel storedOTPCredential = myUser.otp;
            if (storedOTPCredential == null) {
                log.warnf("Not found credential for the user %s", user.getUsername());
                return false;
            }

            TimeBasedOTP validator = new TimeBasedOTP(storedOTPCredential.getAlgorithm(), storedOTPCredential.getDigits(),
                    storedOTPCredential.getPeriod(), realm.getOTPPolicy().getLookAheadWindow());
            return validator.validateTOTP(otpCredential.getValue(), storedOTPCredential.getValue().getBytes());
        } else if (input.getType().equals(RecoveryAuthnCodesCredentialModel.TYPE)) {
            CredentialModel storedRecoveryKeys = myUser.recoveryCodes;
            if (storedRecoveryKeys == null) {
                log.warnf("Not found credential for the user %s", user.getUsername());
                return false;
            }
            List generatedKeys;
            try {
                generatedKeys = JsonSerialization.readValue(storedRecoveryKeys.getCredentialData(), List.class);
            } catch (IOException e) {
                log.warnf("Cannot deserialize recovery keys credential for the user %s", user.getUsername());
                return false;
            }

            return generatedKeys.stream().anyMatch(key -> key.equals(input.getChallengeResponse()));
        }  else {
            log.infof("Not supported to validate credential of type '%s' for user '%s'", input.getType(), user.getUsername());
            return false;
        }
    }

    /** {@inheritDoc} 按用户名查找用户。 */
    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        if (!users.containsKey(translateUserName(username))) return null;

        return createUser(realm, username);
    }

    /** {@inheritDoc} 本提供者不支持按邮箱查找。 */
    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        return null;
    }

    /** {@inheritDoc} 在内存映射中注册新用户。 */
    @Override
    public UserModel addUser(RealmModel realm, String username) {
        users.put(translateUserName(username), new MyUser(username));
        return createUser(realm, username);
    }

    /** {@inheritDoc} 从内存映射中移除用户。 */
    @Override
    public boolean removeUser(RealmModel realm, UserModel user) {
        return users.remove(translateUserName(user.getUsername())) != null;
    }


    // UserQueryProvider 方法

    /** {@inheritDoc} 返回内存中的用户总数。 */
    @Override
    public int getUsersCount(RealmModel realm) {
        return users.size();
    }

    /** {@inheritDoc} 按搜索词查找用户流。 */
    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search) {
        return searchForUserStream(realm, search, -1, -1);
    }

    /** {@inheritDoc} 带分页的搜索实现。 */
    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search, Integer firstResult, Integer maxResults) {
        UserModel user = getUserByUsername(realm, search);
        return user == null ? Stream.empty() : Stream.of(user);
    }

    /** {@inheritDoc} 按参数映射搜索用户。 */
    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params) {
        return searchForUserStream(realm, params, null, null);
    }

    /** {@inheritDoc} 带分页的参数映射搜索。 */
    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult, Integer maxResults) {
        return searchForUserStream(realm, params.get(UserModel.SEARCH), firstResult, maxResults);
    }

    /** {@inheritDoc} 组成员查询（当前不支持）。 */
    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult, Integer maxResults) {
        // 假定不支持此操作
        return Stream.empty();
    }

    /** {@inheritDoc} 组成员查询（当前不支持）。 */
    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group) {
        // 假定不支持此操作
        return Stream.empty();
    }

    /** {@inheritDoc} 按用户属性搜索（当前不支持）。 */
    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
        // 假定不支持此操作
        return Stream.empty();
    }

    /** {@inheritDoc} 关闭提供者；当前实现无需额外清理。 */
    @Override
    public void close() {
    }


    /** 内存用户记录，持有用户名与各类凭据。 */
    static class MyUser {

        /** 用户名。 */
        private String username;
        /** 哈希后的密码凭据。 */
        private CredentialModel hashedPassword;
        /** OTP 凭据。 */
        private CredentialModel otp;
        /** 恢复码凭据。 */
        private CredentialModel recoveryCodes;

        private MyUser(String username) {
            this.username = username;
        }

        /** 返回 OTP 凭据模型。 */
        public CredentialModel getOtp() {
            return otp;
        }

        /** 返回恢复码凭据模型。 */
        public CredentialModel getRecoveryCodes() {
            return recoveryCodes;
        }
    }


    /** 断言对象为 null，用于兼容性测试。 */
    private void assertNull(Object obj) {
        if (obj != null) {
            throw new AssertionError("Object wasn't null");
        }
    }

    /** 断言对象非 null，用于兼容性测试。 */
    private void assertNotNull(Object obj) {
        if (obj == null) {
            throw new AssertionError("Object was null");
        }
    }

    /** 断言两对象相等，用于兼容性测试。 */
    private void assertEquals(Object obj1, Object obj2) {
        if (!(obj1.equals(obj2))) {
            throw new AssertionError("Objects not equals");
        }
    }

}
