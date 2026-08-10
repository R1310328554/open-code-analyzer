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

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.keycloak.component.ComponentModel;
import org.keycloak.provider.InvalidationHandler.InvalidableObjectType;
import org.keycloak.provider.Provider;
import org.keycloak.services.clientpolicy.ClientPolicyManager;
import org.keycloak.sessions.AuthenticationSessionProvider;
import org.keycloak.vault.VaultTranscriber;

/**
 * Keycloak 会话：单次请求/事务的工作单元，提供 Provider 访问与上下文管理。
 * <p>实现 {@link AutoCloseable}，会话结束时释放资源。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface KeycloakSession extends AutoCloseable {

    /** @return 当前请求上下文 */
    KeycloakContext getContext();

    /** @return 事务管理器 */
    KeycloakTransactionManager getTransactionManager();

    /**
     * 获取本会话的 Provider 实例；若尚未创建则通过工厂分配。
     * Get dedicated provider instance of provider type clazz that was created for this session.  If one hasn't been created yet,
     * find the factory and allocate by calling ProviderFactory.create(KeycloakSession).  The provider to use is determined
     * by the "provider" config entry in keycloak-server boot configuration. See the <a href="https://www.keycloak.org/docs/latest/server_development/index.html#_use_available_providers">Server developer guide</a> for the details.
     *
     *
     *
     * @param clazz
     * @param <T>
     * @return
     */
    <T extends Provider> T getProvider(Class<T> clazz);

    /**
     * Get dedicated provider instance for a specific provider factory of id of provider type clazz that was created for this session.
     * If one hasn't been created yet,
     * find the factory and allocate by calling ProviderFactory.create(KeycloakSession).

     * @param clazz
     * @param id
     * @param <T>
     * @return
     */
    <T extends Provider> T getProvider(Class<T> clazz, String id);

    /**
     * 从当前 Realm 获取组件 Provider；调用前须在上下文中设置 Realm，见 {@link KeycloakContext#getRealm()}。
     * Returns a component provider for a component from the realm that is relevant to this session.
     * The relevant realm must be set prior to calling this method in the context, see {@link KeycloakContext#getRealm()}.
     * @param <T>
     * @param clazz
     * @param componentId Component configuration
     * @throws IllegalArgumentException If the realm is not set in the context.
     * @return Provider configured according to the {@param componentId}, {@code null} if it cannot be instantiated.
     */
    <T extends Provider> T getComponentProvider(Class<T> clazz, String componentId);

    /**
     * Returns a component provider for a component from the realm that is relevant to this session.
     * The relevant realm must be set prior to calling this method in the context, see {@link KeycloakContext#getRealm()}.
     * @param <T>
     * @param clazz
     * @param componentId Component configuration
     * @param modelGetter Getter to retrieve componentModel
     * @throws IllegalArgumentException If the realm is not set in the context.
     * @return Provider configured according to the {@param componentId}, {@code null} if it cannot be instantiated.
     */
    <T extends Provider> T getComponentProvider(Class<T> clazz, String componentId, Function<KeycloakSessionFactory, ComponentModel> modelGetter);

    /**
     *
     * @param <T>
     * @param clazz
     * @param componentModel
     * @return
     * @deprecated Deprecated in favor of {@link #getComponentProvider)
     */
    @Deprecated
    <T extends Provider> T getProvider(Class<T> clazz, ComponentModel componentModel);

    /**
     * Get all provider factories that manage provider instances of class.
     *
     * @param clazz
     * @param <T>
     * @return
     */
    <T extends Provider> Set<String> listProviderIds(Class<T> clazz);

    <T extends Provider> Set<T> getAllProviders(Class<T> clazz);

    Class<? extends Provider> getProviderClass(String providerClassName);

    Object getAttribute(String attribute);
    <T> T getAttribute(String attribute, Class<T> clazz);
    default <T> T getAttributeOrDefault(String attribute, T defaultValue) {
        T value = (T) getAttribute(attribute);

        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    Object removeAttribute(String attribute);
    void setAttribute(String name, Object value);

    Map<String, Object> getAttributes();

    /**
     * 立即使给定对象的中间状态失效，并在会话结束时再次失效。
     * Invalidates intermediate states of the given objects, both immediately and at the end of this session.
     * @param type Type of the objects to invalidate
     * @param params Parameters used for the invalidation
     */
    void invalidate(InvalidableObjectType type, Object... params);

    void enlistForClose(Provider provider);

    KeycloakSessionFactory getKeycloakSessionFactory();

    /**
     * 返回受管 Realm Provider，并启动 Provider 事务（由 KeycloakSession 事务管理）。
     * Returns a managed provider instance.  Will start a provider transaction.  This transaction is managed by the KeycloakSession
     * transaction.
     *
     * @return
     * @throws IllegalStateException if transaction is not active
     */
    RealmProvider realms();

    /**
     * Returns a managed provider instance.  Will start a provider transaction.  This transaction is managed by the KeycloakSession
     * transaction.
     *
     * @return
     * @throws IllegalStateException if transaction is not active
     */
    ClientProvider clients();

    /**
     * Returns a managed provider instance.  Will start a provider transaction.  This transaction is managed by the KeycloakSession
     * transaction.
     *
     * @return Currently used ClientScopeProvider instance.
     * @throws IllegalStateException if transaction is not active
     */
    ClientScopeProvider clientScopes();

    /**
     * 返回受管组 Provider 实例。
     * Returns a managed group provider instance.
     *
     * @return Currently used GroupProvider instance.
     * @throws IllegalStateException if transaction is not active
     */
    GroupProvider groups();

    /**
     * Returns a managed provider instance.  Will start a provider transaction.  This transaction is managed by the KeycloakSession
     * transaction.
     *
     * @return
     * @throws IllegalStateException if transaction is not active
     */
    RoleProvider roles();

    /**
     * Returns a managed provider instance.  Will start a provider transaction.  This transaction is managed by the KeycloakSession
     * transaction.
     *
     * @return
     * @throws IllegalStateException if transaction is not active
     */
    UserSessionProvider sessions();

    /**
     * Returns a managed provider instance.  Will start a provider transaction.  This transaction is managed by the KeycloakSession
     * transaction.
     *
     * @return {@link UserLoginFailureProvider}
     * @throws IllegalStateException if transaction is not active
     */
    UserLoginFailureProvider loginFailures();

    AuthenticationSessionProvider authenticationSessions();

    SingleUseObjectProvider singleUseObjects();

    RevokedTokenProvider revokedTokens();

    /**
     * 返回默认 IdP 存储 Provider。
     * Returns the default IDP provider .
     *
     * @return the default IDP provider.
     */
    IdentityProviderStorageProvider identityProviders();

    @Override
    void close();

    /**
     * 系统内所有用户的缓存视图，包含 UserStorageProvider 加载的用户。
     * A cached view of all users in system including  users loaded by UserStorageProviders
     *
     * @return UserProvider instance
     */
    UserProvider users();

    /**
     * 密钥管理器。
     * Key manager
     *
      * @return
     */
    KeyManager keys();

    /**
     * 主题管理器。
     * Theme manager
     *
     * @return
     */
    ThemeManager theme();

    /**
     * 令牌管理器。
     * Token manager
     *
     * @return
     */
    TokenManager tokens();

    /**
     * 保险库转录器，用于读取密钥等敏感配置。
     * Vault transcriber
     */
    VaultTranscriber vault();

    /**
     * 客户端策略管理器。
     * Client Policy Manager
     */
    ClientPolicyManager clientPolicy();

    /** @return 会话是否已关闭 */
    boolean isClosed();

}
