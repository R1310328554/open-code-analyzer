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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.keycloak.Config;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentFactoryProvider;
import org.keycloak.component.ComponentFactoryProviderFactory;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.ThemeManager;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.InvalidationHandler;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.provider.ProviderEventListener;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.ProviderManager;
import org.keycloak.provider.Spi;
import org.keycloak.services.resources.admin.fgap.AdminPermissions;
import org.keycloak.theme.ThemeManagerFactory;

import org.jboss.logging.Logger;

/**
 * {@link KeycloakSessionFactory} 默认实现基类：管理 SPI/Provider 工厂注册、热部署与缓存失效。
 * <p>负责按依赖顺序初始化工厂、解析默认 Provider、支持 ProviderManager 动态部署/卸载。</p>
 */
public abstract class DefaultKeycloakSessionFactory implements KeycloakSessionFactory {

    private static final Logger logger = Logger.getLogger(DefaultKeycloakSessionFactory.class);

    protected Set<Spi> spis = new HashSet<>();
    protected Map<Class<? extends Provider>, String> provider = new HashMap<>();
    protected volatile Map<Class<? extends Provider>, Map<String, ProviderFactory>> factoriesMap = new HashMap<>();
    protected CopyOnWriteArrayList<ProviderEventListener> listeners = new CopyOnWriteArrayList<>();

    // TODO: Likely should be changed to int and use Time.currentTime() to be compatible with all our "time" reps
    protected long serverStartupTimestamp = System.currentTimeMillis();

    protected ComponentFactoryProviderFactory componentFactoryPF;

    private volatile boolean bootstrapCompleted;

    @Override
    public void register(ProviderEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unregister(ProviderEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void publish(ProviderEvent event) {
        for (ProviderEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    /** 初始化所有 Provider 工厂并注册管理权限监听器。 */
    public void init() {
        initProviderFactories();

        AdminPermissions.registerListener(this);
    }

    /** 初始化全部 Provider 工厂（含组件工厂优先 postInit）。 */
    protected void initProviderFactories() {
        initProviderFactories(true, factoriesMap);
    }

    protected void initProviderFactories(boolean updateComponentFactory, Map<Class<? extends Provider>, Map<String, ProviderFactory>> factories) {
        if (updateComponentFactory) {
            // 组件工厂须最先初始化，以便其他工厂的 postInit 可使用组件工厂
            updateComponentFactoryProviderFactory();
            if (componentFactoryPF != null) {
                componentFactoryPF.postInit(this);
            }
        }

        Set<Class<? extends Provider>> initializedProviders = new HashSet<>();
        Stack<ProviderFactory> recursionPrevention = new Stack<>();

        for(Map.Entry<Class<? extends Provider>, Map<String, ProviderFactory>>  f : factories.entrySet()) {
            initializeProviders(f.getKey(), factories, initializedProviders, recursionPrevention);
        }
    }

    private void initializeProviders(Class<? extends Provider> provider, Map<Class<? extends Provider>, Map<String, ProviderFactory>> factories, Set<Class<? extends Provider>> initializedProviders, Stack<ProviderFactory> recursionPrevention) {
        if (initializedProviders.contains(provider)) {
            return;
        }
        for (ProviderFactory<?> factory : factories.get(provider).values()) {
            if (factory == componentFactoryPF)
                continue;

            for (Class<? extends Provider> providerDep : factory.dependsOn()) {
                if (recursionPrevention.contains(factory)) {
                    List<String> stackForException = recursionPrevention.stream().map(providerFactory -> providerFactory.getClass().getName()).toList();
                    throw new RuntimeException("Detected a recursive dependency on provider " + providerDep.getName() +
                          " while the initialization of the following provider factories is ongoing: " + stackForException);
                }
                Map<String, ProviderFactory> f = factories.get(providerDep);
                if (f == null) {
                    throw new RuntimeException("No provider factories exists for provider " + providerDep.getSimpleName() + " required by " + factory.getClass().getName() + " (" + factory.getId() + ")");
                }
                recursionPrevention.push(factory);
                initializeProviders(providerDep, factories, initializedProviders, recursionPrevention);
                recursionPrevention.pop();
            }
            factory.postInit(this);
            initializedProviders.add(provider);
        }
    }

    protected Map<Class<? extends Provider>, Map<String, ProviderFactory>> getFactoriesCopy() {
        Map<Class<? extends Provider>, Map<String, ProviderFactory>> copy = new HashMap<>();
        for (Map.Entry<Class<? extends Provider>, Map<String, ProviderFactory>> entry : factoriesMap.entrySet()) {
            Map<String, ProviderFactory> valCopy = new HashMap<>(entry.getValue());
            copy.put(entry.getKey(), valCopy);
        }
        return copy;

    }

    /**
     * 热部署 ProviderManager 中的新 SPI/工厂，替换旧工厂并刷新主题缓存。
     * @param pm 待部署的 Provider 管理器
     */
        registerNewSpis(pm);

        Map<Class<? extends Provider>, Map<String, ProviderFactory>> copy = getFactoriesCopy();
        Map<Class<? extends Provider>, Map<String, ProviderFactory>> newFactories = loadFactories(pm);
        Map<Class<? extends Provider>, Map<String, ProviderFactory>> deployed = new HashMap<>();
        List<ProviderFactory> undeployed = new LinkedList<>();

        for (Map.Entry<Class<? extends Provider>, Map<String, ProviderFactory>> entry : newFactories.entrySet()) {
            Class<? extends Provider> provider = entry.getKey();
            Map<String, ProviderFactory> current = copy.get(provider);
            if (current == null) {
                copy.put(provider, entry.getValue());
            } else {
                for (Map.Entry<String, ProviderFactory> e : entry.getValue().entrySet()) {
                    deployed.compute(provider, (k, v) -> {
                        Map<String, ProviderFactory> map = Objects.requireNonNullElseGet(v, HashMap::new);
                        map.put(e.getKey(), e.getValue());
                        return map;
                    });
                    ProviderFactory old = current.remove(e.getValue().getId());
                    if (old != null) {
                        undeployed.add(old);
                    }
                }
                current.putAll(entry.getValue());
            }

        }
        factoriesMap = copy;
        // 热部署后需重建默认 Provider 映射
        checkProvider();
        boolean cfChanged = false;
        for (ProviderFactory factory : undeployed) {
            invalidate(null, ObjectType.PROVIDER_FACTORY, factory.getClass());
            factory.close();
            cfChanged |= (componentFactoryPF == factory);
        }
        initProviderFactories(cfChanged, deployed);

        if (pm.getInfo().hasThemes() || pm.getInfo().hasThemeResources()) {
            ((ThemeManagerFactory)getProviderFactory(ThemeManager.class)).clearCache();
        }
    }

    // Register SPIs of this providerManager, which are possibly not yet registered in this factory
    private void registerNewSpis(ProviderManager pm) {
        Set<String> existingSpiNames = this.spis.stream()
                .map(spi -> spi.getName())
                .collect(Collectors.toSet());

        this.spis = new HashSet<>(this.spis);
        for (Spi newSpi : pm.loadSpis()) {
            if (!existingSpiNames.contains(newSpi.getName())) {
                this.spis.add(newSpi);
            }
        }
    }

    /** 卸载 ProviderManager 加载的工厂并关闭实例。 @param pm 待卸载的管理器 */
    public void undeploy(ProviderManager pm) {
        logger.debug("undeploy");
        // we make a copy to avoid concurrent access exceptions
        Map<Class<? extends Provider>, Map<String, ProviderFactory>> copy = getFactoriesCopy();
        MultivaluedHashMap<Class<? extends Provider>, ProviderFactory> factories = pm.getLoadedFactories();
        List<ProviderFactory> undeployed = new LinkedList<>();
        for (Map.Entry<Class<? extends Provider>, List<ProviderFactory>> entry : factories.entrySet()) {
            Map<String, ProviderFactory> registered = copy.get(entry.getKey());
            for (ProviderFactory factory : entry.getValue()) {
                undeployed.add(factory);
                logger.debugv("undeploying {0} of id {1}", factory.getClass().getName(), factory.getId());
                if (registered != null) {
                    registered.remove(factory.getId());
                }
            }
        }
        factoriesMap = copy;
        for (ProviderFactory factory : undeployed) {
            factory.close();
        }
    }

    /** 重建各 SPI 的默认 Provider ID 映射。 */
    protected void checkProvider() {
        // make sure to recreated the default providers map
        provider.clear();

        for (Spi spi : spis) {
            String provider = Config.getProvider(spi.getName());
            if (provider != null) {
                if (getProviderFactory(spi.getProviderClass(), provider) == null) {
                    throw new RuntimeException("Failed to find provider " + provider + " for " + spi.getName());
                }
                this.provider.put(spi.getProviderClass(), provider);
            } else {
                Map<String, ProviderFactory> factories = factoriesMap.get(spi.getProviderClass());
                String defaultProvider = resolveDefaultProvider(factories, spi);
                if (defaultProvider != null) {
                    this.provider.put(spi.getProviderClass(), defaultProvider);
                }
            }
        }
    }

    /**
     * 解析 SPI 的默认 Provider ID（配置项、唯一工厂、order 或 "default"）。
     * @param factories 已加载工厂映射
     * @param spi 目标 SPI
     * @return 默认 Provider ID，无法解析时 {@code null}
     */
        if (factories == null) {
            return null;
        }

        String defaultProvider = Config.getDefaultProvider(spi.getName());
        if (defaultProvider != null) {
            if (!factories.containsKey(defaultProvider)) {
                throw new RuntimeException("Failed to find provider " + defaultProvider + " for " + spi.getName());
            }
        } else if (factories.size() == 1) {
            defaultProvider = factories.values().iterator().next().getId();
        } else {
            Optional<ProviderFactory> highestPriority = factories.values().stream().filter(p -> p.order() > 0).max(Comparator.comparing(ProviderFactory::order));
            if (highestPriority.isPresent()) {
                defaultProvider = highestPriority.get().getId();
            } else if (factories.containsKey("default")) {
                defaultProvider = "default";
            }
        }

        if (defaultProvider != null) {
            logger.debugv("Set default provider for {0} to {1}", spi.getName(), defaultProvider);
            return defaultProvider;
        } else {
            logger.debugv("No default provider for {0}", spi.getName());
            return null;
        }
    }

    protected Map<Class<? extends Provider>, Map<String, ProviderFactory>> loadFactories(ProviderManager pm) {
        Map<Class<? extends Provider>, Map<String, ProviderFactory>> factoryMap = new HashMap<>();
        Set<Spi> spiList = spis;

        for (Spi spi : spiList) {

            Map<String, ProviderFactory> factories = new HashMap<>();
            factoryMap.put(spi.getProviderClass(), factories);

            String provider = Config.getProvider(spi.getName());
            if (provider != null) {

                ProviderFactory factory = pm.load(spi, provider);
                if (factory == null) {
                    continue;
                }

                Config.Scope scope = Config.scope(spi.getName(), provider);
                if (isEnabled(factory, scope)) {
                    factory.init(scope);

                    if (spi.isInternal() && !isInternal(factory)) {
                        ServicesLogger.LOGGER.spiMayChange(factory.getId(), factory.getClass().getName(), spi.getName());
                    }

                    factories.put(factory.getId(), factory);

                    logger.debugv("Loaded SPI {0} (provider = {1})", spi.getName(), provider);
                }

            } else {
                for (ProviderFactory factory : pm.load(spi)) {
                    Config.Scope scope = Config.scope(spi.getName(), factory.getId());
                    if (isEnabled(factory, scope)) {
                        factory.init(scope);

                        if (spi.isInternal() && !isInternal(factory)) {
                            ServicesLogger.LOGGER.spiMayChange(factory.getId(), factory.getClass().getName(), spi.getName());
                        }
                        factories.put(factory.getId(), factory);
                    } else {
                        logger.debugv("SPI {0} provider {1} disabled", spi.getName(), factory.getId());
                    }
                }
            }
        }
        return factoryMap;
    }

    protected boolean isEnabled(ProviderFactory factory, Config.Scope scope) {
        if (!scope.getBoolean("enabled", true)) {
            return false;
        }
        if (factory instanceof EnvironmentDependentProviderFactory) {
            return ((EnvironmentDependentProviderFactory) factory).isSupported(scope);
        }
        return true;
    }

    /** {@inheritDoc} @return 已注册 SPI 集合 */
    @Override
    public Set<Spi> getSpis() {
        return spis;
    }

    @Override
    public Spi getSpi(Class<? extends Provider> providerClass) {
        for (Spi spi : spis) {
            if (spi.getProviderClass().equals(providerClass)) {
                return spi;
            }
        }
        return null;
    }

    /** {@inheritDoc} 按默认 Provider ID 获取工厂 */
    @Override
    public <T extends Provider> ProviderFactory<T> getProviderFactory(Class<T> clazz) {
         return getProviderFactory(clazz, provider.get(clazz));
    }

    @Override
    public <T extends Provider> ProviderFactory<T> getProviderFactory(Class<T> clazz, String id) {
        Map<String, ProviderFactory> map = factoriesMap.get(clazz);
        if (map == null) {
            return null;
        }
        return map.get(id);
    }

    @Override
    public <T extends Provider> ProviderFactory<T> getProviderFactory(Class<T> clazz, String realmId, String componentId, Function<KeycloakSessionFactory, ComponentModel> modelGetter) {
        return (this.componentFactoryPF == null)
          ? null
          : this.componentFactoryPF.getProviderFactory(clazz, realmId, componentId, modelGetter);
    }

    /** {@inheritDoc} 通知支持 {@link InvalidationHandler} 的工厂失效缓存 */
    @Override
    public void invalidate(KeycloakSession session, InvalidableObjectType type, Object... ids) {
        factoriesMap.values().stream()
          .map(Map::values)
          .flatMap(Collection::stream)
          .filter(InvalidationHandler.class::isInstance)
          .map(InvalidationHandler.class::cast)
          .forEach(ih -> ih.invalidate(session, type, ids));
    }

    @Override
    public Stream<ProviderFactory> getProviderFactoriesStream(Class<? extends Provider> clazz) {
        if (factoriesMap == null) {
            return Stream.empty();
        }
        Map<String, ProviderFactory> providerFactoryMap = factoriesMap.get(clazz);
        if (providerFactoryMap == null) {
            return Stream.empty();
        }
        return providerFactoryMap.values().stream();
    }

    <T extends Provider> Set<String> getAllProviderIds(Class<T> clazz) {
        Map<String, ProviderFactory> factoryMap = factoriesMap.get(clazz);
        if (factoryMap == null) {
            return Collections.emptySet();
        }
        Set<String> ids = new HashSet<>();
        for (ProviderFactory f : factoryMap.values()) {
            ids.add(f.getId());
        }
        return ids;
    }

    Class<? extends Provider> getProviderClass(String providerClassName) {
        for (Class<? extends Provider> clazz : factoriesMap.keySet()) {
            if (clazz.getName().equals(providerClassName)) {
                return clazz;
            }
        }
        return null;
    }

    /** {@inheritDoc} 按依赖逆序关闭所有 Provider 工厂。 */
    @Override
    public void close() {
        // Create a tree-structure to represent reverse relation of ProviderFactory#dependsOn to Providers
        Map<Class<? extends Provider>, Node<Set<ProviderFactory>>> nodes = new HashMap<>();
        for (Map.Entry<Class<? extends Provider>, Map<String, ProviderFactory>>  f : factoriesMap.entrySet()) {
            Class<? extends Provider> provider = f.getKey();
            for (Map.Entry<String, ProviderFactory> entry : f.getValue().entrySet()) {
                ProviderFactory pf = entry.getValue();
                Node<Set<ProviderFactory>> node = nodes.computeIfAbsent(provider, k -> new Node<>(new HashSet<>()));
                // Add ProviderFactory to the associated Provider node
                node.data.add(pf);
                // If dependencies exist, make this node a child of the Provider dependencies node so that we can ensure
                // that the leaves of the tree are closed first
                pf.dependsOn().forEach(dep -> {
                    node.parent = nodes.computeIfAbsent((Class<? extends Provider>) dep, k -> new Node<>(new HashSet<>()));
                    node.parent.children.add(node);
                });
            }
        }
        nodes.values().forEach(this::closeProvider);
    }

    private void closeProvider(Node<Set<ProviderFactory>> node) {
        for (var it = node.children.iterator(); it.hasNext(); ) {
            closeProvider(it.next());
            it.remove();
        }

        // Provider has no other dependent ProviderFactories, it's ProviderFactories can safely be closed
        for (var it = node.data.iterator(); it.hasNext(); ) {
            ProviderFactory pf = it.next();
            logger.debugf("Closing ProviderFactory: %s", pf.getClass().getName());
            pf.close();
            it.remove();
        }
    }

    private static class Node<T> {
        private final T data;
        private Node<T> parent;
        private List<Node<T>> children;

        public Node(T data) {
            this.data = data;
            this.parent = null;
            this.children = new ArrayList<>();
        }
    }

    /** 判断工厂是否属于 Keycloak 内部包（非 examples）。 */
    public static boolean isInternal(ProviderFactory<?> factory) {
        String packageName = factory.getClass().getPackage().getName();
        return packageName.startsWith("org.keycloak") && !packageName.startsWith("org.keycloak.examples");
    }

    /**
     * @return Keycloak 服务器启动时间戳（毫秒）
     * @return timestamp of Keycloak server startup
     */
    @Override
    public long getServerStartupTimestamp() {
        return serverStartupTimestamp;
    }

    protected void updateComponentFactoryProviderFactory() {
        this.componentFactoryPF = (ComponentFactoryProviderFactory) getProviderFactory(ComponentFactoryProvider.class);
    }

    /** 标记服务器引导流程已完成。 */
    public void setBootstrapCompleted() {
        this.bootstrapCompleted = true;
    }

    /** @return 引导是否已完成 */
    public boolean isBootstrapCompleted() {
        return this.bootstrapCompleted;
    }

}
