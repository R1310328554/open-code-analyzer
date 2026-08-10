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
package org.keycloak.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.keycloak.common.util.StackUtil;
import org.keycloak.component.ComponentFactory;
import org.keycloak.component.ComponentModel;
import org.keycloak.jose.jws.DefaultTokenManager;
import org.keycloak.keys.DefaultKeyManager;
import org.keycloak.models.ClientProvider;
import org.keycloak.models.ClientScopeProvider;
import org.keycloak.models.GroupProvider;
import org.keycloak.models.IdentityProviderStorageProvider;
import org.keycloak.models.KeyManager;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.KeycloakTransactionManager;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.RevokedTokenProvider;
import org.keycloak.models.RoleProvider;
import org.keycloak.models.SingleUseObjectProvider;
import org.keycloak.models.ThemeManager;
import org.keycloak.models.TokenManager;
import org.keycloak.models.UserLoginFailureProvider;
import org.keycloak.models.UserProvider;
import org.keycloak.models.UserSessionProvider;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.InvalidationHandler.InvalidableObjectType;
import org.keycloak.provider.InvalidationHandler.ObjectType;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.services.clientpolicy.ClientPolicyManager;
import org.keycloak.sessions.AuthenticationSessionProvider;
import org.keycloak.storage.DatastoreProvider;
import org.keycloak.vault.DefaultVaultTranscriber;
import org.keycloak.vault.VaultProvider;
import org.keycloak.vault.VaultTranscriber;

import org.jboss.logging.Logger;

/**
 * {@link KeycloakSession} 默认抽象实现。
 * <p>管理 Provider 懒加载与缓存、事务、会话属性、组件 Provider 及各类 datastore 门面；关闭时提交/回滚事务并关闭已注册 Provider。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public abstract class DefaultKeycloakSession implements KeycloakSession {

    /** 所属会话工厂 */
    private final DefaultKeycloakSessionFactory factory;
    /** 会话内 Provider 实例缓存（键含类型与 ID/组件信息） */
    private final Map<List<String>, Provider> providers = new HashMap<>();
    /** 需在会话关闭时显式关闭的 Provider 列表 */
    private final List<Provider> closable = new LinkedList<>();
    /** 会话事务管理器 */
    private final DefaultKeycloakTransactionManager transactionManager;
    /** 会话级属性存储 */
    private final Map<String, Object> attributes = new HashMap<>();
    /** 关闭时需向工厂传播的失效事件 */
    private final Map<InvalidableObjectType, Set<Object>> invalidationMap = new HashMap<>();
    /** 懒加载的数据存储 Provider */
    private DatastoreProvider datastoreProvider;
    /** 请求上下文 */
    private final KeycloakContext context;
    private KeyManager keyManager;
    private TokenManager tokenManager;
    private VaultTranscriber vaultTranscriber;
    private ClientPolicyManager clientPolicyManager;
    /** 会话是否已关闭 */
    private boolean closed = false;

    /** @param factory Keycloak 会话工厂 */
    public DefaultKeycloakSession(DefaultKeycloakSessionFactory factory) {
        this.factory = factory;
        this.transactionManager = new DefaultKeycloakTransactionManager(this);
        context = createKeycloakContext(this);
        LOG.tracef("Created %s%s", this, StackUtil.getShortStackTrace());
    }

    /** @return 请求上下文 */
    @Override
    public KeycloakContext getContext() {
        return context;
    }

    /** 懒加载 DatastoreProvider @return 数据存储 Provider */
    private DatastoreProvider getDatastoreProvider() {
        if (this.datastoreProvider == null) {
            this.datastoreProvider = getProvider(DatastoreProvider.class);
        }
        return this.datastoreProvider;
    }

    /** 失效工厂缓存并在关闭时重放 PROVIDER_FACTORY 失效 @param type 失效类型 @param ids 对象 ID */
    @Override
    public void invalidate(InvalidableObjectType type, Object... ids) {
        factory.invalidate(this, type, ids);
        if (type == ObjectType.PROVIDER_FACTORY) {
            invalidationMap.computeIfAbsent(type, o -> new HashSet<>()).addAll(Arrays.asList(ids));
        }
    }

    /** 注册需在会话关闭时关闭的 Provider（去重） @param provider Provider 实例 */
    @Override
    public void enlistForClose(Provider provider) {
        for (Provider p : closable) {
            if (p == provider) {    // 同一 Provider 不重复添加
                return;
            }
        }
        closable.add(provider);
    }

    @Override
    public Object getAttribute(String attribute) {
        return attributes.get(attribute);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String attribute, Class<T> clazz) {
        Object value = getAttribute(attribute);
        return clazz.isInstance(value) ? (T) value : null;
    }

    @Override
    public Object removeAttribute(String attribute) {
        return attributes.remove(attribute);
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    @Override
    public KeycloakTransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public KeycloakSessionFactory getKeycloakSessionFactory() {
        return factory;
    }

    @Override
    public UserProvider users() {
        return getDatastoreProvider().users();
    }

    /** 获取默认 Provider 实例 @param clazz Provider 类型 @return Provider 或 null */
    @Override
    public <T extends Provider> T getProvider(Class<T> clazz) {
        List<String> key = List.of(clazz.getName());
        return getOrCreateProvider(key, () -> factory.getProviderFactory(clazz));
    }

    private <T extends Provider> T getOrCreateProvider(List<String> key, Supplier<ProviderFactory<T>> supplier) {
        @SuppressWarnings("unchecked")
        T provider = (T) providers.get(key);
        // KEYCLOAK-11890：避免在 computeIfAbsent 重映射函数中修改 Map
        // 按 JDK-8071667，重映射期间不应修改 Map；JDK 9+ 会抛 ConcurrentModificationException
        // allowed on JDK 1.8, attempt of such a modification throws ConcurrentModificationException with JDK 9+
        if (provider == null) {
            ProviderFactory<T> providerFactory = supplier.get();
            if (providerFactory != null) {
                provider = providerFactory.create(DefaultKeycloakSession.this);
                providers.put(key, provider);
            }
        }
        return provider;
    }

    /** 按 ID 获取 Provider @param id 工厂标识 @return Provider 或 null */
    @Override
    public <T extends Provider> T getProvider(Class<T> clazz, String id) {
        List<String> key = List.of(clazz.getName(), id);
        return getOrCreateProvider(key, () -> factory.getProviderFactory(clazz, id));
    }

    /** 从当前 realm 配置加载组件 Provider @param componentId 组件 ID @return Provider 实例 */
    @Override
    public <T extends Provider> T getComponentProvider(Class<T> clazz, String componentId) {
        final RealmModel realm = getContext().getRealm();
        if (realm == null) {
            throw new IllegalArgumentException("Realm not set in the context.");
        }

        // 从 realm 配置加载 ComponentModel
        return this.getComponentProvider(clazz, componentId, KeycloakModelUtils.componentModelGetter(realm.getId(), componentId));
    }

    @Override
    public <T extends Provider> T getComponentProvider(Class<T> clazz, String componentId, Function<KeycloakSessionFactory, ComponentModel> modelGetter) {
        List<String> key = List.of("component", clazz.getName(), componentId);
        final RealmModel realm = getContext().getRealm();
        return getOrCreateProvider(key, () -> factory.getProviderFactory(clazz, Optional.ofNullable(realm).map(RealmModel::getId).orElse(null), componentId, modelGetter));
    }

    /** 按 ComponentModel 创建并缓存组件 Provider @param componentModel 组件模型 @return Provider 实例 */
    @Override
    public <T extends Provider> T getProvider(Class<T> clazz, ComponentModel componentModel) {
        String modelId = componentModel.getId();

        Object found = getAttribute(modelId);
        if (found != null) {
            return clazz.cast(found);
        }

        ProviderFactory<T> providerFactory = factory.getProviderFactory(clazz, componentModel.getProviderId());
        if (providerFactory == null) {
            return null;
        }

        ComponentFactory<T, T> componentFactory = (ComponentFactory<T, T>) providerFactory;
        T provider = componentFactory.create(this, componentModel);
        enlistForClose(provider);
        setAttribute(modelId, provider);

        return provider;
    }

    @Override
    public <T extends Provider> Set<String> listProviderIds(Class<T> clazz) {
        return factory.getAllProviderIds(clazz);
    }

    @Override
    public <T extends Provider> Set<T> getAllProviders(Class<T> clazz) {
        return listProviderIds(clazz).stream()
            .map(id -> getProvider(clazz, id))
            .collect(Collectors.toSet());
    }

    @Override
    public Class<? extends Provider> getProviderClass(String providerClassName) {
        return factory.getProviderClass(providerClassName);
    }

    @Override
    public RealmProvider realms() {
        return getDatastoreProvider().realms();
    }

    @Override
    public ClientProvider clients() {
        return getDatastoreProvider().clients();
    }

    @Override
    public ClientScopeProvider clientScopes() {
        return getDatastoreProvider().clientScopes();
    }

    @Override
    public GroupProvider groups() {
        return getDatastoreProvider().groups();
    }

    @Override
    public RoleProvider roles() {
        return getDatastoreProvider().roles();
    }


    @Override
    public UserSessionProvider sessions() {
        return getDatastoreProvider().userSessions();
    }

    @Override
    public UserLoginFailureProvider loginFailures() {
        return getDatastoreProvider().loginFailures();
    }

    @Override
    public AuthenticationSessionProvider authenticationSessions() {
        return getDatastoreProvider().authSessions();
    }

    @Override
    public SingleUseObjectProvider singleUseObjects() {
        return getDatastoreProvider().singleUseObjects();
    }

    @Override
    public RevokedTokenProvider revokedTokens() {
        return getDatastoreProvider().revokedTokens();
    }

    @Override
    public IdentityProviderStorageProvider identityProviders() {
        return getDatastoreProvider().identityProviders();
    }

    @Override
    public KeyManager keys() {
        if (keyManager == null) {
            keyManager = new DefaultKeyManager(this);
        }
        return keyManager;
    }

    @Override
    public ThemeManager theme() {
        return this.getProvider(ThemeManager.class);
    }

    @Override
    public TokenManager tokens() {
        if (tokenManager == null) {
            tokenManager = new DefaultTokenManager(this);
        }
        return tokenManager;
    }

    @Override
    public VaultTranscriber vault() {
        if (this.vaultTranscriber == null) {
            this.vaultTranscriber = new DefaultVaultTranscriber(this.getProvider(VaultProvider.class));
        }
        return this.vaultTranscriber;
    }

    @Override
    public ClientPolicyManager clientPolicy() {
        if (clientPolicyManager == null) {
            clientPolicyManager = getProvider(ClientPolicyManager.class);
        }
        return clientPolicyManager;
    }

    private static final Logger LOG = Logger.getLogger(DefaultKeycloakSession.class);

    /** 提交或回滚事务，关闭所有 Provider 并标记会话已关闭 @throws IllegalStateException 重复关闭时 */
    @Override
    public void close() {
        if (LOG.isTraceEnabled()) {
            LOG.tracef("Closing %s%s%s", this,
              getTransactionManager().isActive() ? " (transaction active" + (getTransactionManager().getRollbackOnly() ? ", ROLLBACK-ONLY" : "") + ")" : "",
              StackUtil.getShortStackTrace());
        }

        if (closed) {
            throw new IllegalStateException("Illegal call to #close() on already closed " + this);
        }

        RuntimeException re = closeTransactionManager();

        try {
            Consumer<? super Provider> safeClose = p -> {
                try {
                    if (p != null) {
                        p.close();
                    }
                } catch (Exception e) {
                    LOG.warnf(e, "Unable to close provider %s", p.getClass().getName());
                }
            };
            providers.values().forEach(safeClose);
            closable.forEach(safeClose);
            for (Entry<InvalidableObjectType, Set<Object>> me : invalidationMap.entrySet()) {
                factory.invalidate(this, me.getKey(), me.getValue().toArray());
            }
        } finally {
            this.closed = true;
        }

        if (re != null) {
            throw re;
        }
    }

    /** 关闭活跃事务：回滚或提交 @return 提交/回滚时的 RuntimeException 或 null */
    protected RuntimeException closeTransactionManager() {
        if (! this.transactionManager.isActive()) {
            return null;
        }

        try {
            if (this.transactionManager.getRollbackOnly()) {
                this.transactionManager.rollback();
            } else {
                this.transactionManager.commit();
            }
        } catch (RuntimeException re) {
            return re;
        }

        return null;
    }

    @Override
    public String toString() {
        return String.format("session @ %08x", System.identityHashCode(this));
    }

    /** 创建运行时特定的 KeycloakContext @param session 当前会话 @return DefaultKeycloakContext 子类 */
    protected abstract DefaultKeycloakContext createKeycloakContext(KeycloakSession session);

    /** @return 会话是否已关闭 */
    public boolean isClosed() {
        return closed;
    }
}
