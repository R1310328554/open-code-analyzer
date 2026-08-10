/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

import org.keycloak.Config;
import org.keycloak.Config.Scope;
import org.keycloak.cluster.ClusterProvider;
import org.keycloak.common.util.StackUtil;
import org.keycloak.component.ComponentFactoryProviderFactory;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentModelScope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.provider.InvalidationHandler;
import org.keycloak.provider.InvalidationHandler.InvalidableObjectType;
import org.keycloak.provider.InvalidationHandler.ObjectType;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;

import org.jboss.logging.Logger;

/**
 * 默认组件工厂提供者工厂。
 * <p>按 realm/组件 ID 缓存并初始化 {@link ProviderFactory}，支持集群失效传播；实现 {@link ComponentFactoryProviderFactory} 与 {@link InvalidationHandler}。</p>
 *
 * @author hmlnarik
 */
public class DefaultComponentFactoryProviderFactory implements ComponentFactoryProviderFactory {

    /** 日志记录器 */
    private static final Logger LOG = Logger.getLogger(DefaultComponentFactoryProviderFactory.class);
    /** 提供方标识 */
    public static final String PROVIDER_ID = "default";

    /** 组件 ID → 已初始化 ProviderFactory 缓存 */
    private final AtomicReference<ConcurrentMap<String, ProviderFactory>> componentsMap = new AtomicReference<>(new ConcurrentHashMap<>());

    /** 依赖失效映射：键（realm/工厂类）失效时，值中组件 ID 一并失效。 */
    /** 失效键到依赖组件 ID 集合的映射 */
    private final ConcurrentMap<Object, Set<String>> dependentInvalidations = new ConcurrentHashMap<>();

    /** Keycloak 会话工厂 */
    private KeycloakSessionFactory factory;
    /** 组件工厂缓存是否可用（需集群 Provider 或强制开启） */
    private boolean componentCachingAvailable;
    /** 配置是否启用组件缓存 */
    private boolean componentCachingEnabled;
    /** 是否强制启用缓存（单节点部署） */
    private Boolean componentCachingForced;

    /** 读取 cachingEnabled/cachingForced 配置 @param config 配置作用域 */
    @Override
    public void init(Scope config) {
        this.componentCachingEnabled = config.getBoolean("cachingEnabled", true);
        this.componentCachingForced = config.getBoolean("cachingForced", false);
    }

    /** 检测集群 Provider 并决定是否启用组件缓存 @param factory 会话工厂 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        this.factory = factory;
        this.componentCachingAvailable = this.componentCachingEnabled && this.factory.getProviderFactory(ClusterProvider.class) != null;
        if (! componentCachingEnabled) {
            LOG.warn("Caching of components disabled by the configuration which may have performance impact.");
        } else if (! componentCachingAvailable) {
            if (Objects.equals(componentCachingForced, Boolean.TRUE)) {
                LOG.warn("Component caching forced even though no system-wide ClusterProviderFactory found. This would be only reliable in single-node deployment.");
                this.componentCachingAvailable = true;
            } else {
                LOG.warn("No system-wide ClusterProviderFactory found. Cannot send messages across cluster, thus disabling caching of components. Consider setting cachingForced option in single-node deployment.");
            }
        }
    }

    /**
     * 获取或创建组件对应的 ProviderFactory。
     * @param clazz Provider 类型
     * @param realmId 领域 ID
     * @param componentId 组件 ID
     * @param modelGetter 可选的组件模型获取函数
     * @return 已初始化的工厂，未找到时 null
     */
        ProviderFactory res = componentsMap.get().get(componentId);
        if (res != null) {
            LOG.tracef("Found cached ProviderFactory for %s in (%s, %s)", clazz, realmId, componentId);
            return res;
        }

        // 在写入缓存前完成昂贵的初始化操作
        final ComponentModel cm;
        if (modelGetter == null) {
            LOG.debugf("Getting component configuration for component (%s, %s) from realm configuration", clazz, realmId, componentId);
            cm = KeycloakModelUtils.getComponentModel(factory, realmId, componentId);
        } else {
            LOG.debugf("Getting component configuration for component (%s, %s) via provided method", realmId, componentId);
            cm = modelGetter.apply(factory);
        }

        if (cm == null) {
            return null;
        }

        final String provider = cm.getProviderId();
        ProviderFactory<T> pf = provider == null
          ? factory.getProviderFactory(clazz)
          : factory.getProviderFactory(clazz, provider);

        if (pf == null) {   // Either not found or not enabled
            LOG.debugf("ProviderFactory for %s in (%s, %s) not found", clazz, realmId, componentId);
            return null;
        }

        final ProviderFactory newFactory;
        try {
            newFactory = pf.getClass().getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            LOG.warn("Cannot instantiate factory", ex);
            return null;
        }

        Scope scope = Config.scope(factory.getSpi(clazz).getName(), provider);
        ComponentModelScope configScope = new ComponentModelScope(scope, cm);

        ProviderFactory<T> providerFactory;
        if (this.componentCachingAvailable) {
            providerFactory = componentsMap.get().computeIfAbsent(componentId, cId -> initializeFactory(clazz, realmId, componentId, newFactory, configScope));
        } else {
            providerFactory = initializeFactory(clazz, realmId, componentId, newFactory, configScope);
        }
        return providerFactory;
    }

    /** 初始化工厂并注册依赖失效关系 @param newFactory 新工厂实例 @param configScope 组件配置作用域 @return 初始化后的工厂 */
    @SuppressWarnings("unchecked")
    protected <T extends Provider> ProviderFactory<T> initializeFactory(Class<T> clazz, String realmId, String componentId, final ProviderFactory newFactory, ComponentModelScope configScope) {
        LOG.debugf("Initializing ProviderFactory for %s in (%s, %s)", clazz, realmId, componentId);

        newFactory.init(configScope);
        newFactory.postInit(factory);

        if (realmId == null) {
            realmId = configScope.getComponentParentId();
        }
        if (realmId != null) {
            dependentInvalidations.computeIfAbsent(realmId, k -> ConcurrentHashMap.newKeySet()).add(componentId);
        }
        dependentInvalidations.computeIfAbsent(newFactory.getClass(), k -> ConcurrentHashMap.newKeySet()).add(componentId);

        return newFactory;
    }

    /** 按类型失效组件工厂缓存并传播给 InvalidationHandler @param type 失效对象类型 @param ids 失效对象 ID */
    @Override
    public void invalidate(KeycloakSession session, InvalidableObjectType type, Object... ids) {
        if (LOG.isDebugEnabled()) {
            LOG.debugf("Invalidating %s: %s", type, Arrays.asList(ids));
        }
        LOG.tracef("invalidate(%s)%s", type, StackUtil.getShortStackTrace());

        if (type == ObjectType._ALL_) {
            final ConcurrentMap<String, ProviderFactory> cm = componentsMap.getAndSet(new ConcurrentHashMap<>());
            dependentInvalidations.clear();
            cm.values().forEach(ProviderFactory::close);
        } else if (type == ObjectType.COMPONENT) {
            Stream.of(ids)
              .map(componentsMap.get()::remove).filter(Objects::nonNull)
              .forEach(ProviderFactory::close);
            propagateInvalidation(session, componentsMap.get(), type, ids);
        } else if (type == ObjectType.REALM || type == ObjectType.PROVIDER_FACTORY) {
            Stream.of(ids)
              .map(dependentInvalidations::get).filter(Objects::nonNull).flatMap(Collection::stream)
              .map(componentsMap.get()::remove).filter(Objects::nonNull)
              .forEach(ProviderFactory::close);
            Stream.of(ids).forEach(dependentInvalidations::remove);
            propagateInvalidation(session, componentsMap.get(), type, ids);
        } else {
            propagateInvalidation(session, componentsMap.get(), type, ids);
        }
    }

    /** 向已缓存工厂中的 InvalidationHandler 传播失效事件 */
    private void propagateInvalidation(KeycloakSession session, ConcurrentMap<String, ProviderFactory> componentsMap, InvalidableObjectType type, Object[] ids) {
        componentsMap.values()
          .stream()
          .filter(InvalidationHandler.class::isInstance)
          .map(InvalidationHandler.class::cast)
          .forEach(ih -> ih.invalidate(session, type, ids));
    }

    /** @return 提供方标识 {@link #PROVIDER_ID} */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** 关闭所有已缓存的 ProviderFactory */
    @Override
    public void close() {
        componentsMap.get().values().forEach(ProviderFactory::close);
    }

}
