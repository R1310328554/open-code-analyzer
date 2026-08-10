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

package org.keycloak.credential;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.keycloak.common.util.reflections.Types;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.credential.WebAuthnCredentialModel;
import org.keycloak.storage.AbstractStorageManager;
import org.keycloak.storage.DatastoreProvider;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.StoreManagers;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.UserStorageProviderFactory;
import org.keycloak.storage.UserStorageProviderModel;
import org.keycloak.tracing.TracingProvider;

import io.opentelemetry.api.trace.StatusCode;

/**
 * 针对指定用户的凭据管理器：校验、更新与持久化用户凭据。
 * <p>
 * 协调联邦用户存储、本地存储与各类 {@link CredentialProvider} 实现。
 *
 * @author Alexander Schwartz
 */
public class UserCredentialManager extends AbstractStorageManager<UserStorageProvider, UserStorageProviderModel> implements org.keycloak.models.UserCredentialManager {

    /** 视为第一因素认证（1FA）的凭据类型列表。 */
    private static final List<String> FIRST_FACTOR_CREDENTIAL_TYPES = List.of(
            PasswordCredentialModel.TYPE,
            CredentialModel.CLIENT_CERT,
            CredentialModel.KERBEROS,
            WebAuthnCredentialModel.TYPE_PASSWORDLESS
    );

    /** 目标用户。 */
    private final UserModel user;
    /** 当前 Keycloak 会话。 */
    private final KeycloakSession session;
    /** 用户所属 realm。 */
    private final RealmModel realm;

    /**
     * 构造用户凭据管理器。
     * <p>
     * 不建议在用户存储 Provider 中直接调用；请使用 {@link org.keycloak.models.UserProvider#getUserCredentialManager(UserModel) session.users().getUserCredentialManager(user)}。
     */
    public UserCredentialManager(KeycloakSession session, RealmModel realm, UserModel user) {
        super(session, UserStorageProviderFactory.class, UserStorageProvider.class, UserStorageProviderModel::new, "user");
        this.user = user;
        this.session = session;
        this.realm = realm;
    }

    /** 校验一组凭据输入是否全部有效。 */
    @Override
    public boolean isValid(List<CredentialInput> inputs) {
        if (!isValid(user)) {
            return false;
        }

        List<CredentialInput> toValidate = new LinkedList<>(inputs);

        // 联邦用户：先由对应 UserStorageProvider 校验
        if (user.isFederated()) {
            UserStorageProviderModel model = getStorageProviderModel(realm, user.getFederationLink());
            if (model == null || !model.isEnabled()) return false;

            CredentialInputValidator validator = getStorageProviderInstance(model, CredentialInputValidator.class);
            if (validator != null) {
                validate(realm, user, toValidate, validator);
            }
        }

        getCredentialProviders(session, CredentialInputValidator.class)
                .forEach(validator -> validate(realm, user, toValidate, validator));

        return toValidate.isEmpty();
    }

    /** 更新单个凭据输入（如修改密码）。 */
    @Override
    public boolean updateCredential(CredentialInput input) {
        if (!StorageId.isLocalStorage(user.getId())) throwExceptionIfInvalidUser(user);

        if (user.isFederated()) {
            UserStorageProviderModel model = getStorageProviderModel(realm, user.getFederationLink());
            if (model == null || !model.isEnabled()) return false;

            CredentialInputUpdater updater = getStorageProviderInstance(model, CredentialInputUpdater.class);
            if (updater != null && updater.supportsCredentialType(input.getType())) {
                if (updater.updateCredential(realm, user, input)) return true;
            }
        }

        return getCredentialProviders(session, CredentialInputUpdater.class)
                .filter(updater -> updater.supportsCredentialType(input.getType()))
                .anyMatch(updater -> updater.updateCredential(realm, user, input));
    }

    /** 更新已持久化的凭据模型。 */
    @Override
    public void updateStoredCredential(CredentialModel cred) {
        throwExceptionIfInvalidUser(user);
        getStoreForUser(user).updateCredential(realm, user, cred);
    }

    /** 创建并持久化新凭据。 */
    @Override
    public CredentialModel createStoredCredential(CredentialModel cred) {
        throwExceptionIfInvalidUser(user);
        return getStoreForUser(user).createCredential(realm, user, cred);
    }

    /** 按 ID 删除已存储凭据。 */
    @Override
    public boolean removeStoredCredentialById(String id) {
        throwExceptionIfInvalidUser(user);
        return getStoreForUser(user).removeStoredCredential(realm, user, id);
    }

    /** 按 ID 获取已存储凭据。 */
    @Override
    public CredentialModel getStoredCredentialById(String id) {
        return getStoreForUser(user).getStoredCredentialById(realm, user, id);
    }

    /** 返回用户全部已存储凭据流。 */
    @Override
    public Stream<CredentialModel> getStoredCredentialsStream() {
        return getStoreForUser(user).getStoredCredentialsStream(realm, user);
    }

    /** 返回联邦存储中的凭据流（仅联邦用户）。 */
    @Override
    public Stream<CredentialModel> getFederatedCredentialsStream() {
        if (user.isFederated()) {
            UserStorageProviderModel model = getStorageProviderModel(realm, user.getFederationLink());

            if (model == null || !model.isEnabled()) {
                return Stream.empty();
            }

            CredentialInputUpdater credentialProvider = getStorageProviderInstance(model, CredentialInputUpdater.class);

            if (credentialProvider != null) {
                return credentialProvider.getCredentials(realm, user);
            }
        }

        return Stream.empty();
    }

    /** 按类型过滤已存储凭据。 */
    @Override
    public Stream<CredentialModel> getStoredCredentialsByTypeStream(String type) {
        return getStoreForUser(user).getStoredCredentialsByTypeStream(realm, user, type);
    }

    /** 按名称与类型查找已存储凭据。 */
    @Override
    public CredentialModel getStoredCredentialByNameAndType(String name, String type) {
        return getStoreForUser(user).getStoredCredentialByNameAndType(realm, user, name, type);
    }

    /** 调整凭据在列表中的顺序。 */
    @Override
    public boolean moveStoredCredentialTo(String id, String newPreviousCredentialId) {
        throwExceptionIfInvalidUser(user);
        return getStoreForUser(user).moveCredentialTo(realm, user, id, newPreviousCredentialId);
    }

    /** 更新凭据的用户可见标签。 */
    @Override
    public void updateCredentialLabel(String credentialId, String userLabel) {
        throwExceptionIfInvalidUser(user);
        CredentialModel credential = getStoredCredentialById(credentialId);
        credential.setUserLabel(userLabel);
        updateStoredCredential(credential);
    }

    /** 禁用指定类型的凭据。 */
    @Override
    public void disableCredentialType(String credentialType) {
        if (!StorageId.isLocalStorage(user.getId())) throwExceptionIfInvalidUser(user);
        if (user.isFederated()) {
            UserStorageProviderModel model = getStorageProviderModel(realm, user.getFederationLink());
            if (model == null || !model.isEnabled()) return;

            CredentialInputUpdater updater = getStorageProviderInstance(model, CredentialInputUpdater.class);
            if (updater.supportsCredentialType(credentialType)) {
                updater.disableCredentialType(realm, user, credentialType);
            }
        }

        getCredentialProviders(session, CredentialInputUpdater.class)
                .filter(updater -> updater.supportsCredentialType(credentialType))
                .forEach(updater -> updater.disableCredentialType(realm, user, credentialType));
    }

    /** 返回当前用户可禁用的凭据类型流。 */
    @Override
    public Stream<String> getDisableableCredentialTypesStream() {
        Stream<String> types = Stream.empty();
        if (user.isFederated()) {
            UserStorageProviderModel model = getStorageProviderModel(realm, user.getFederationLink());
            if (model == null || !model.isEnabled()) return types;

            CredentialInputUpdater updater = getStorageProviderInstance(model, CredentialInputUpdater.class);
            if (updater != null) types = updater.getDisableableCredentialTypesStream(realm, user);
        }

        return Stream.concat(types, getCredentialProviders(session, CredentialInputUpdater.class)
                        .flatMap(updater -> updater.getDisableableCredentialTypesStream(realm, user)))
                .distinct();
    }

    /** 判断用户是否已配置指定类型的凭据。 */
    @Override
    public boolean isConfiguredFor(String type) {
        UserStorageCredentialConfigured userStorageConfigured = isConfiguredThroughUserStorage(realm, user, type);

        // 优先依据联邦存储判断；否则回退到本地校验
        switch (userStorageConfigured) {
            case CONFIGURED: return true;
            case USER_STORAGE_DISABLED: return false;
        }

        return isConfiguredLocally(type);
    }

    /** 仅检查本地/Provider 侧是否已配置指定凭据类型。 */
    @Override
    public boolean isConfiguredLocally(String type) {
        return getCredentialProviders(session, CredentialInputValidator.class)
                .anyMatch(validator -> validator.supportsCredentialType(type) && validator.isConfiguredFor(realm, user, type));
    }

    /** 返回已通过联邦用户存储配置的凭据类型流。 */
    @Override
    public Stream<String> getConfiguredUserStorageCredentialTypesStream() {
        return getCredentialProviders(session, CredentialProvider.class).map(CredentialProvider::getType)
                .filter(credentialType -> UserStorageCredentialConfigured.CONFIGURED == isConfiguredThroughUserStorage(realm, user, credentialType));
    }

    /** 通过匹配的 CredentialProvider 创建凭据。 */
    @Override
    public CredentialModel createCredentialThroughProvider(CredentialModel model) {
        throwExceptionIfInvalidUser(user);
        return session.getKeycloakSessionFactory()
                .getProviderFactoriesStream(CredentialProvider.class)
                .map(f -> session.getProvider(CredentialProvider.class, f.getId()))
                .filter(provider -> provider.supportsCredentialType(model))
                .map(cp -> cp.createCredential(realm, user, cp.getCredentialFromModel(model)))
                .findFirst()
                .orElse(null);
    }

    /** 联邦用户存储凭据配置状态枚举。 */
    private enum UserStorageCredentialConfigured {
        /** 已在联邦存储中配置。 */
        CONFIGURED,
        /** 联邦存储已禁用。 */
        USER_STORAGE_DISABLED,
        /** 未在联邦存储中配置。 */
        NOT_CONFIGURED
    }

    /** 检查指定凭据类型是否通过联邦用户存储配置。 */
    private UserStorageCredentialConfigured isConfiguredThroughUserStorage(RealmModel realm, UserModel user, String type) {
        if (user.isFederated()) {
            UserStorageProviderModel model = getStorageProviderModel(realm, user.getFederationLink());
            if (model == null || !model.isEnabled()) return UserStorageCredentialConfigured.USER_STORAGE_DISABLED;

            CredentialInputValidator validator = getStorageProviderInstance(model, CredentialInputValidator.class);
            if (validator != null && validator.supportsCredentialType(type) && validator.isConfiguredFor(realm, user, type)) {
                return UserStorageCredentialConfigured.CONFIGURED;
            }
        }

        return UserStorageCredentialConfigured.NOT_CONFIGURED;
    }

    /** 服务账户用户不可管理凭据。 */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isValid(UserModel user) {
        Objects.requireNonNull(user);
        return user.getServiceAccountClientLink() == null;
    }

    /** 使用指定校验器逐项验证并从未验证列表中移除已通过项。 */
    private void validate(RealmModel realm, UserModel user, List<CredentialInput> toValidate, CredentialInputValidator validator) {
        toValidate.removeIf(input -> {
            if(validator.supportsCredentialType(input.getType())) {
                return session.getProvider(TracingProvider.class).trace(validator.getClass(), "isValid", span -> {
                    boolean valid = validator.isValid(realm, user, input);
                    if (!valid) {
                        span.setStatus(StatusCode.ERROR);
                    }
                    return valid;
                });
            }
            return false;
        });
    }

    /** 获取实现指定能力接口的全部 CredentialProvider 实例流。 */
    private static <T> Stream<T> getCredentialProviders(KeycloakSession session, Class<T> type) {
        //noinspection unchecked
        return session.getKeycloakSessionFactory().getProviderFactoriesStream(CredentialProvider.class)
                .filter(f -> Types.supports(type, f, CredentialProviderFactory.class))
                .map(f -> (T) session.getProvider(CredentialProvider.class, f.getId()));
    }

    /** 对不可管理凭据的用户抛出运行时异常。 */
    private void throwExceptionIfInvalidUser(UserModel user) {
        if (!isValid(user)) {
            throw new RuntimeException("You can not manage credentials for this user");
        }
    }

    /** 根据用户 ID 选择本地或联邦凭据存储。 */
    private UserCredentialStore getStoreForUser(UserModel user) {
        StoreManagers p = (StoreManagers) session.getProvider(DatastoreProvider.class);
        if (StorageId.isLocalStorage(user.getId())) {
            return (UserCredentialStore) p.userLocalStorage();
        } else {
            return (UserCredentialStore) p.userFederatedStorage();
        }
    }

    /** 返回第一因素认证凭据流（密码、证书、Kerberos、无密码 WebAuthn 等）。 */
    @Override
    public Stream<CredentialModel> getFirstFactorCredentialsStream() {
        return getStoredCredentialsStream()
                .filter(c -> FIRST_FACTOR_CREDENTIAL_TYPES.contains(c.getType()));
    }
}
