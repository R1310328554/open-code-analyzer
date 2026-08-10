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

package org.keycloak.connections.infinispan;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.keycloak.Config;
import org.keycloak.cluster.ClusterEvent;
import org.keycloak.cluster.ClusterProvider;
import org.keycloak.common.Profile;
import org.keycloak.common.util.DurationConverter;
import org.keycloak.config.HttpOptions;
import org.keycloak.connections.infinispan.remote.RemoteInfinispanConnectionProvider;
import org.keycloak.connections.infinispan.shutdown.ShutdownManager;
import org.keycloak.connections.infinispan.shutdown.TopologyChangeCacheListener;
import org.keycloak.infinispan.health.ClusterHealth;
import org.keycloak.infinispan.util.InfinispanUtils;
import org.keycloak.marshalling.KeycloakIndexSchemaUtil;
import org.keycloak.marshalling.KeycloakModelSchema;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.cache.infinispan.ClearCacheEvent;
import org.keycloak.models.cache.infinispan.events.RealmRemovedEvent;
import org.keycloak.models.cache.infinispan.events.RealmUpdatedEvent;
import org.keycloak.models.sessions.infinispan.query.ClientSessionQueries;
import org.keycloak.models.sessions.infinispan.query.UserSessionQueries;
import org.keycloak.models.sessions.infinispan.remote.RemoteInfinispanAuthenticationSessionProviderFactory;
import org.keycloak.models.sessions.infinispan.remote.RemoteUserLoginFailureProviderFactory;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.PostMigrationEvent;
import org.keycloak.provider.InvalidationHandler.ObjectType;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.provider.ProviderEventListener;
import org.keycloak.provider.ServerInfoAwareProviderFactory;
import org.keycloak.services.resources.ShutdownDelayInitiatedEvent;
import org.keycloak.spi.infinispan.CacheEmbeddedConfigProvider;
import org.keycloak.spi.infinispan.CacheRemoteConfigProvider;
import org.keycloak.spi.infinispan.impl.embedded.CacheConfigurator;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.configuration.io.ConfigurationWriter;
import org.infinispan.commons.io.StringBuilderWriter;
import org.infinispan.commons.util.Version;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.health.CacheHealth;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Transport;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.jboss.logging.Logger;

import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.AUTHENTICATION_SESSIONS_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.CLIENT_SESSION_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.CLUSTERED_CACHE_NAMES;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.CRL_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.KEYS_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.LOGIN_FAILURE_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.OFFLINE_CLIENT_SESSION_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.OFFLINE_USER_SESSION_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.USER_SESSION_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.WORK_CACHE_NAME;
import static org.keycloak.connections.infinispan.InfinispanUtil.setTimeServiceToKeycloakTime;
import static org.keycloak.models.cache.infinispan.InfinispanCacheRealmProviderFactory.REALM_CLEAR_CACHE_EVENTS;
import static org.keycloak.models.cache.infinispan.InfinispanCacheRealmProviderFactory.REALM_INVALIDATION_EVENTS;

/**
 * Infinispan 连接提供者的默认工厂实现。
 * <p>
 * 负责创建嵌入式 {@link EmbeddedCacheManager} 与可选的 {@link RemoteCacheManager}，
 * 根据部署模式返回 {@link DefaultInfinispanConnectionProvider} 或 {@link RemoteInfinispanConnectionProvider}，
 * 并管理集群健康检查、优雅关闭与系统级集群事件监听。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class DefaultInfinispanConnectionProviderFactory implements InfinispanConnectionProviderFactory, ProviderEventListener, ServerInfoAwareProviderFactory {

    /** 保护 CacheManager 生命周期操作的读写锁（防止关闭时死锁，见 KEYCLOAK-9871）。 */
    private static final ReadWriteLock READ_WRITE_LOCK = new ReentrantReadWriteLock();
    private static final Logger logger = Logger.getLogger(DefaultInfinispanConnectionProviderFactory.class);
    /** 关闭超时配置键。 */
    private static final String SHUTDOWN_TIMEOUT = "shutdownTimeout";

    /** SPI 配置作用域。 */
    private Config.Scope config;

    /** 嵌入式 Infinispan 缓存管理器。 */
    private volatile EmbeddedCacheManager cacheManager;
    /** Hot Rod 远程缓存管理器（可选）。 */
    private volatile RemoteCacheManager remoteCacheManager;
    /** 懒初始化的连接提供者实例。 */
    private volatile InfinispanConnectionProvider connectionProvider;
    /** 集群健康检查组件。 */
    private volatile ClusterHealth clusterHealth;
    /** 优雅关闭管理器。 */
    private volatile ShutdownManager shutdownManager;

    @Override
    public InfinispanConnectionProvider create(KeycloakSession session) {
        return lazyInit(session);
    }

    /*
        Infinispan 12.1.7.Final 至 14.0.19.Final 的 workaround，防止
        DefaultInfinispanConnectionProviderFactory 关闭时发生死锁。
        经大量分析后作为永久方案保留。
        详见 https://github.com/keycloak/keycloak/issues/9871
    */
    /** 在 CacheManager 读锁保护下执行任务。 */
    public static void runWithReadLockOnCacheManager(Runnable task) {
        Lock lock = DefaultInfinispanConnectionProviderFactory.READ_WRITE_LOCK.readLock();
        lock.lock();
        try {
            task.run();
        } finally {
            lock.unlock();
        }
    }

    /** 在 CacheManager 写锁保护下执行任务（用于 stop 等写操作）。 */
    public static void runWithWriteLockOnCacheManager(Runnable task) {
        Lock lock = DefaultInfinispanConnectionProviderFactory.READ_WRITE_LOCK.writeLock();
        lock.lock();
        try {
            task.run();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        logger.debug("Closing provider");
        if (shutdownManager != null) {
            shutdownManager.onShutdown();
            shutdownManager = null;
        }
        runWithWriteLockOnCacheManager(() -> {
            if (cacheManager != null) {
                cacheManager.stop();
                cacheManager = null;
            }
        });
        if (remoteCacheManager != null) {
            remoteCacheManager.close();
            remoteCacheManager = null;
        }
    }

    @Override
    public String getId() {
        return "default";
    }

    @Override
    public void init(Config.Scope config) {
        this.config = config;
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        factory.register(this);
    }

    /** 懒初始化嵌入式/远程 CacheManager 并创建 ConnectionProvider。 */
    protected InfinispanConnectionProvider lazyInit(KeycloakSession keycloakSession) {
        if (connectionProvider != null) {
            return connectionProvider;
        }
        synchronized (this) {
            if (connectionProvider != null) {
                return connectionProvider;
            }

            this.cacheManager = createEmbeddedCacheManager(keycloakSession);
            injectKeycloakTimeService(cacheManager);
            var topologyInfo = new TopologyInfo(cacheManager);
            var nodeInfo = NodeInfo.of(cacheManager);
            logger.info(nodeInfo.printInfo());

            addShutdownListeners();

            this.remoteCacheManager = createRemoteCacheManager(keycloakSession);
            this.connectionProvider = InfinispanUtils.isRemoteInfinispan() ?
                    new RemoteInfinispanConnectionProvider(cacheManager, remoteCacheManager, topologyInfo, nodeInfo) :
                    new DefaultInfinispanConnectionProvider(cacheManager, topologyInfo, nodeInfo);

            clusterHealth = GlobalComponentRegistry.componentOf(cacheManager, ClusterHealth.class);
            return connectionProvider;
        }
    }

    /** 为分布式缓存注册拓扑变更关闭监听器，防止状态转移期间数据丢失。 */
    private void addShutdownListeners() {
        var sm = new ShutdownManager(getShutdownDelay().toMillis(), getShutdownTimeout().toMillis());
        for (var name : CLUSTERED_CACHE_NAMES) {
            if (!cacheManager.cacheConfigurationExists(name)) {
                logger.debugf("Cache '%s' not defined; skipping the shutdown listener", name);
                continue;
            }
            var cache = cacheManager.getCache(name);
            if (cache == null ){
                logger.debugf("Cache '%s' not defined; skipping the shutdown listener", name);
                return;
            }
            var cacheConfig = cache.getCacheConfiguration();
            if (!cacheConfig.clustering().cacheMode().isClustered() || cacheConfig.clustering().cacheMode().isReplicated() || cacheConfig.clustering().cacheMode().isInvalidation()) {
                // 本地或复制缓存无数据丢失风险，跳过关闭监听器
                logger.debugf("Cache '%s' uses mode '%s' and no data loss risk exists; skipping the shutdown listener", name, cacheConfig.clustering().cacheMode());
                continue;
            }
            if (!cacheConfig.clustering().stateTransfer().fetchInMemoryState()) {
                // 状态转移已禁用，可跳过
                logger.debugf("Cache '%s' has state transfer disabled; skipping the shutdown listener", name);
                continue;
            }
            var listener = TopologyChangeCacheListener.waitForStableTopology(cache);
            sm.addListener(listener);
        }
        shutdownManager = sm;
    }

    private Duration getShutdownTimeout() {
        return convertDurationAndEnsureGreaterOrEqualsThanZero(config.get(SHUTDOWN_TIMEOUT));
    }

    private Duration getShutdownDelay() {
        return convertDurationAndEnsureGreaterOrEqualsThanZero(config.root().get(HttpOptions.SHUTDOWN_DELAY.getKey()));
    }

    private static Duration convertDurationAndEnsureGreaterOrEqualsThanZero(String value) {
        var duration = DurationConverter.parseDuration(value);
        if (duration == null || duration.isZero() || duration.isNegative()) {
            return Duration.ZERO;
        }
        return duration;
    }

    /**
     * 创建嵌入式 CacheManager：从 {@link CacheEmbeddedConfigProvider} 读取配置，
     * stateless 模式下移除集群缓存（保留 work 缓存）。
     */
    protected EmbeddedCacheManager createEmbeddedCacheManager(KeycloakSession session) {
        var holder = session.getProvider(CacheEmbeddedConfigProvider.class).configuration();

        // stateless 模式下移除集群缓存
        if (Profile.isFeatureEnabled(Profile.Feature.STATELESS)) {
            Arrays.stream(CLUSTERED_CACHE_NAMES)
                    .filter(Predicate.not(WORK_CACHE_NAME::equals))
                    .forEach(holder.getNamedConfigurationBuilders()::remove);
        }

        StringBuilderWriter sw = new StringBuilderWriter();
        ParserRegistry parser = new ParserRegistry();
        try (ConfigurationWriter w = ConfigurationWriter.to(sw).prettyPrint(true).build()) {
            var globalConfig = holder.getGlobalConfigurationBuilder().build();
            var cacheConfigs = holder.getNamedConfigurationBuilders().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().build()));
            parser.serialize(w, globalConfig, cacheConfigs);
            logger.debugf("Infinispan configuration:\n%s", sw);
        }

        var cm = getDefaultCacheManager(session, holder);
        if (cm.getCacheManagerConfiguration().metrics().enabled()) {
            var transport = GlobalComponentRegistry.componentOf(cm, Transport.class);
            if (transport != null) {
                // 此处启用会遗漏部分消息统计（如 state transfer），但适用于所有协议栈（含用户自定义）
                ((JGroupsTransport) transport).getChannel().getProtocolStack().getTransport().enableStats(true);
            }
        }
        cm.getCache(KEYS_CACHE_NAME, true);
        cm.getCache(CRL_CACHE_NAME, true);

        logger.debugv("Using container managed Infinispan cache container, lookup={0}", cm);
        return cm;
    }

    /** 创建 DefaultCacheManager，挂起 JTA 事务以避免 JDBC_PING2 操作绑定到当前事务。 */
    private static DefaultCacheManager getDefaultCacheManager(KeycloakSession session, ConfigurationBuilderHolder holder) {
        // 禁用 JTA 事务上下文，避免 JDBC_PING2 交互绑定到当前事务
        DefaultCacheManager[] _cm = new DefaultCacheManager[1];
        //noinspection resource
        KeycloakModelUtils.suspendJtaTransaction(session.getKeycloakSessionFactory(), () ->
                _cm[0] = new DefaultCacheManager(holder, true));
        return _cm[0];
    }

    /**
     * 创建 Hot Rod RemoteCacheManager 并上传 ProtoStream 索引 schema。
     * 远程缓存功能未启用时返回 null。
     */
    protected RemoteCacheManager createRemoteCacheManager(KeycloakSession session) {
        var remoteConfig = session.getProvider(CacheRemoteConfigProvider.class).configuration();
        if (remoteConfig.isEmpty()) {
            logger.debug("Remote Cache feature is disabled");
            return null;
        }
        logger.debug("Remote Cache feature is enabled");
        var rcm = new RemoteCacheManager(remoteConfig.get());

        // 在访问缓存前先上传 schema；列表仅启动时使用，不做缓存
        var entities = List.of(
                new KeycloakIndexSchemaUtil.IndexedEntity(RemoteUserLoginFailureProviderFactory.PROTO_ENTITY, LOGIN_FAILURE_CACHE_NAME),
                new KeycloakIndexSchemaUtil.IndexedEntity(RemoteInfinispanAuthenticationSessionProviderFactory.PROTO_ENTITY, AUTHENTICATION_SESSIONS_CACHE_NAME),
                new KeycloakIndexSchemaUtil.IndexedEntity(ClientSessionQueries.CLIENT_SESSION, CLIENT_SESSION_CACHE_NAME),
                new KeycloakIndexSchemaUtil.IndexedEntity(ClientSessionQueries.CLIENT_SESSION, OFFLINE_CLIENT_SESSION_CACHE_NAME),
                new KeycloakIndexSchemaUtil.IndexedEntity(UserSessionQueries.USER_SESSION, USER_SESSION_CACHE_NAME),
                new KeycloakIndexSchemaUtil.IndexedEntity(UserSessionQueries.USER_SESSION, OFFLINE_USER_SESSION_CACHE_NAME)
        );
        KeycloakIndexSchemaUtil.uploadAndReindexCaches(rcm, KeycloakModelSchema.INSTANCE, entities);
        return rcm;
    }

    /**
     * @deprecated not invoked anymore. Overwrite {@link #createEmbeddedCacheManager(KeycloakSession)}.
     */
    @Deprecated(since = "26.0", forRemoval = true)
    protected EmbeddedCacheManager initContainerManaged(EmbeddedCacheManager cacheManager) {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated not used anymore. Overwrite {@link #createEmbeddedCacheManager(KeycloakSession)} if you want to
     * create a custom {@link EmbeddedCacheManager}.
     */
    @Deprecated(since = "26.3", forRemoval = true)
    protected EmbeddedCacheManager initEmbedded() {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated not used anymore
     */
    @Deprecated(since = "26.3", forRemoval = true)
    protected Configuration getKeysCacheConfig() {
        return CacheConfigurator.getCacheConfiguration(KEYS_CACHE_NAME, true).build();
    }

    /**
     * @deprecated Use {@link CacheConfigurator#getCrlCacheConfig()}
     */
    @Deprecated(since = "26.3", forRemoval = true)
    protected Configuration getCrlCacheConfig() {
        return CacheConfigurator.getCrlCacheConfig().build();
    }

    /** 注册系统级集群事件监听器（Realm 缓存失效等）。 */
    private void registerSystemWideListeners(KeycloakSession session) {
        KeycloakSessionFactory sessionFactory = session.getKeycloakSessionFactory();
        ClusterProvider cluster = session.getProvider(ClusterProvider.class);
        cluster.registerListener(REALM_CLEAR_CACHE_EVENTS, (ClusterEvent event) -> {
            if (event instanceof ClearCacheEvent) {
                sessionFactory.invalidate(null, ObjectType._ALL_);
            }
        });
        cluster.registerListener(REALM_INVALIDATION_EVENTS, (ClusterEvent event) -> {
            if (event instanceof RealmUpdatedEvent rr) {
                sessionFactory.invalidate(null, ObjectType.REALM, rr.getId());
            } else if (event instanceof RealmRemovedEvent rr) {
                sessionFactory.invalidate(null, ObjectType.REALM, rr.getId());
            }
        });
    }

    /** 可选注入 Keycloak 时间服务到 Infinispan（用于测试或特殊部署）。 */
    private void injectKeycloakTimeService(EmbeddedCacheManager cacheManager) {
        if (config.getBoolean("useKeycloakTimeService", Boolean.FALSE)) {
            setTimeServiceToKeycloakTime(cacheManager);
        }
    }

    @Override
    public Set<Class<? extends Provider>> dependsOn() {
        return Set.of(CacheRemoteConfigProvider.class, CacheEmbeddedConfigProvider.class);
    }

    @Override
    public void onEvent(ProviderEvent event) {
        if (event instanceof PostMigrationEvent pme) {
            KeycloakModelUtils.runJobInTransaction(pme.getFactory(), this::registerSystemWideListeners);
        } else if (event instanceof ShutdownDelayInitiatedEvent se) {
            Optional.ofNullable(shutdownManager).ifPresent(sm -> sm.onShutdownStarted(se.timestamp()));
        }
    }

    @Override
    public Map<String, String> getOperationalInfo() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("product", Version.getBrandName());
        info.put("version", Version.getBrandVersion());
        if (InfinispanUtils.isRemoteInfinispan()) {
            addRemoteOperationalInfo(info);
        } else {
            addEmbeddedOperationalInfo(info);
        }
        return info;
    }

    /** 触发集群健康检查并返回结果。 */
    @Override
    public boolean isClusterHealthy() {
        clusterHealth.triggerClusterHealthCheck();
        return clusterHealth.isHealthy();
    }

    @Override
    public boolean isClusterHealthSupported() {
        return clusterHealth.isSupported();
    }

    /** 嵌入式模式下返回 JGroups 协调者状态。 */
    @Override
    public boolean isCoordinator() {
        return cacheManager.isCoordinator();
    }

    @Override
    public boolean isCoordinatorSupported() {
        return true;
    }

    /** 收集嵌入式 Infinispan 运维信息（集群规模、各缓存健康状态）。 */
    private void addEmbeddedOperationalInfo(Map<String, String> info) {
        var cacheManagerInfo = cacheManager.getCacheManagerInfo();
        info.put("clusterSize", Integer.toString(cacheManagerInfo.getClusterSize()));
        var cacheNames = Arrays.stream(CLUSTERED_CACHE_NAMES)
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
        for (CacheHealth health : cacheManager.getHealth().getCacheHealth(cacheNames)) {
            info.put(health.getCacheName() + ":Cache", health.getStatus().toString());
        }
    }

    /** 收集远程 Infinispan 运维信息（Hot Rod 连接数）。 */
    private void addRemoteOperationalInfo(Map<String, String> info) {
        info.put("connectionCount", Integer.toString(remoteCacheManager.getConnectionCount()));
    }
}
