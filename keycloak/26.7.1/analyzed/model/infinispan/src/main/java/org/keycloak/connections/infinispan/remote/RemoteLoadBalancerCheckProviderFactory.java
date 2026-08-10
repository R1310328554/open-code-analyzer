/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.connections.infinispan.remote;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.keycloak.Config;
import org.keycloak.common.util.MultiSiteUtils;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.health.LoadBalancerCheckProvider;
import org.keycloak.health.LoadBalancerCheckProviderFactory;
import org.keycloak.infinispan.util.InfinispanUtils;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import org.infinispan.client.hotrod.impl.InternalRemoteCache;
import org.infinispan.client.hotrod.impl.operations.PingResponse;
import org.infinispan.commons.util.concurrent.CompletableFutures;
import org.infinispan.factories.ComponentRegistry;
import org.infinispan.persistence.manager.PersistenceManager;
import org.infinispan.util.concurrent.ActionSequencer;
import org.jboss.logging.Logger;

import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.CLUSTERED_CACHE_NAMES;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.LOCAL_CACHE_NAMES;
import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.skipSessionsCacheIfRequired;

/**
 * 多站点部署下的负载均衡健康检查工厂：周期性探测嵌入式与远程 Infinispan 缓存可用性。
 * <p>
 * 当任一本地缓存未启动、持久化不可用或远程缓存 ping 失败时，向负载均衡器报告"不健康"，
 * 以便在跨数据中心故障时停止向该节点转发流量。
 */
public class RemoteLoadBalancerCheckProviderFactory implements LoadBalancerCheckProviderFactory, EnvironmentDependentProviderFactory {

    /** 远程缓存可用性轮询的默认间隔（毫秒）。 */
    private static final int DEFAULT_POLL_INTERVAL = 5000;
    /** 连接提供者不可用时始终返回"健康"的占位实现。 */
    private static final LoadBalancerCheckProvider ALWAYS_HEALTHY = () -> false;
    private static final Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

    /** 远程/嵌入式缓存探测的轮询间隔（毫秒）。 */
    private volatile int pollIntervalMillis;
    /** 对外暴露的健康检查提供者实例。 */
    private volatile LoadBalancerCheckProvider provider;
    /** Infinispan 连接，用于访问本地与远程缓存。 */
    private InfinispanConnectionProvider connectionProvider;
    /** 定时执行远程 ping 的任务句柄。 */
    private ScheduledFuture<?> availabilityFuture;
    /** 所有远程缓存 ping 检查项的集合与调度器。 */
    private RemoteCacheCheckList remoteCacheCheckList;

    /** 仅在启用多站点（multi-site）时加载此工厂。 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return MultiSiteUtils.isMultiSiteEnabled();
    }

    /** {@inheritDoc} 返回共享的单例健康检查提供者。 */
    @Override
    public LoadBalancerCheckProvider create(KeycloakSession session) {
        return provider;
    }

    /** {@inheritDoc} 读取 {@code poll-interval} 配置。 */
    @Override
    public void init(Config.Scope config) {
        pollIntervalMillis = config.getInt("poll-interval", DEFAULT_POLL_INTERVAL);
    }

    /**
     * {@inheritDoc} 初始化连接提供者、构建远程 ping 列表并启动定时轮询任务。
     */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        try (var session = factory.create()) {
            var provider = session.getProvider(InfinispanConnectionProvider.class);
            if (provider == null) {
                logger.warn("InfinispanConnectionProvider is not available. Load balancer check will be always healthy for Infinispan.");
                this.provider = ALWAYS_HEALTHY;
                return;
            }
            this.connectionProvider = provider;

            var remoteCacheChecks = skipSessionsCacheIfRequired(Arrays.stream(CLUSTERED_CACHE_NAMES))
                    .map(s -> new RemoteCacheCheck(s, provider))
                    .collect(Collectors.toList());
            var sequencer = new ActionSequencer(connectionProvider.getExecutor("load-balancer-check"), false, null);

            this.remoteCacheCheckList = new RemoteCacheCheckList(remoteCacheChecks, sequencer);
            this.availabilityFuture = provider.getScheduledExecutor()
                    .scheduleAtFixedRate(remoteCacheCheckList, pollIntervalMillis, pollIntervalMillis, TimeUnit.MILLISECONDS);

            this.provider = this::isAnyCacheDown;
        }
    }

    /** 取消定时任务并释放引用。 */
    @Override
    public void close() {
        if (availabilityFuture != null) {
            availabilityFuture.cancel(true);
            availabilityFuture = null;
        }
        provider = null;
        remoteCacheCheckList = null;
    }

    /** {@inheritDoc} 返回远程 Infinispan 提供者的 ID。 */
    @Override
    public String getId() {
        return InfinispanUtils.REMOTE_PROVIDER_ID;
    }

    /** {@inheritDoc} 与 Infinispan 连接工厂相同的排序优先级。 */
    @Override
    public int order() {
        return InfinispanUtils.PROVIDER_ORDER;
    }

   /** {@inheritDoc} 声明依赖 Infinispan 连接提供者。 */
   @Override
   public Set<Class<? extends Provider>> dependsOn() {
      return Set.of(InfinispanConnectionProvider.class);
   }

   /** {@inheritDoc} 暴露轮询间隔配置元数据。 */
   @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name("poll-interval")
                .type("int")
                .helpText("The Remote caches poll interval, in milliseconds, for connection availability")
                .defaultValue(DEFAULT_POLL_INTERVAL)
                .add()
                .build();
    }

    /** 嵌入式或远程任一缓存不可用时返回 {@code true}（表示负载均衡应视为不健康）。 */
    private boolean isAnyCacheDown() {
        return isEmbeddedCachesDown() || remoteCacheCheckList.isDown();
    }

    /** 检查所有本地嵌入式缓存是否已启动且持久化层可用。 */
    private boolean isEmbeddedCachesDown() {
        for (var name : LOCAL_CACHE_NAMES) {
            var cache = connectionProvider.getCache(name, false);

            // 检查缓存是否已启动
            if (cache == null || !cache.getStatus().allowInvocations()) {
                logger.debugf("Cache '%s' is not started yet.", name);
                return true; // 无需继续检查其他缓存
            }

            var persistenceManager = ComponentRegistry.componentOf(cache, PersistenceManager.class);
            if (persistenceManager != null && !persistenceManager.isAvailable()) {
                logger.debugf("Persistence for embedded cache '%s' is down.", name);
                return true; // 无需继续检查其他缓存
            }
        }
        return false;
    }

    /**
     * 定时任务：按缓存名串行调度各 {@link RemoteCacheCheck} 的 ping 操作。
     *
     * @param list      远程缓存检查项列表
     * @param sequencer 按 key 排序的动作序列器，避免并发 ping 冲突
     */
    private record RemoteCacheCheckList(List<RemoteCacheCheck> list, ActionSequencer sequencer) implements Runnable {
        @Override
        public void run() {
            list.forEach(remoteCacheCheck -> sequencer.orderOnKey(remoteCacheCheck.name(), remoteCacheCheck));
        }

        /** 任一远程缓存 ping 失败则视为不可用。 */
        public boolean isDown() {
            return list.stream().anyMatch(RemoteCacheCheck::isDown);
        }
    }

    /** 对单个远程缓存执行 Hot Rod ping 并维护可用状态标志。 */
    private static class RemoteCacheCheck implements Callable<CompletionStage<Void>>, BiFunction<PingResponse, Throwable, Void> {

        /** 远程缓存名称。 */
        private final String name;
        /** 用于获取远程缓存的连接提供者。 */
        private final InfinispanConnectionProvider provider;
        /** 最近一次 ping 是否失败。 */
        private volatile boolean isDown;

        private RemoteCacheCheck(String name, InfinispanConnectionProvider provider) {
            this.name = name;
            this.provider = provider;
        }

        String name() {
            return name;
        }

        boolean isDown() {
            return isDown;
        }

        /** 发起异步 ping；非 InternalRemoteCache 时视为可用。 */
        @Override
        public CompletionStage<Void> call() {
            try {
                var cache = provider.getRemoteCache(name);
                if (cache instanceof InternalRemoteCache<Object, Object>) {
                    return ((InternalRemoteCache<Object, Object>) cache).ping()
                            .handle(this);
                }
                isDown = false;
            } catch (Exception e) {
                if (!isDown) {
                    logger.warnf("Remote cache '%' is down.", name);
                }
                isDown = true;
            }
            return CompletableFutures.completedNull();
        }

        /** ping 响应或异常到达时更新 {@link #isDown} 状态。 */
        @Override
        public Void apply(PingResponse response, Throwable throwable) {
            var successPing = response != null && response.isSuccess();
            logger.debugf("Received Ping response for cache '%s'. Success=%s, Throwable=%s", name, successPing, throwable);
            isDown = throwable != null || !successPing;
            return null;
        }
    }
}
