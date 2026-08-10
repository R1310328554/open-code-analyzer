/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.crl.infinispan;

import java.security.cert.X509CRL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.keycloak.Config;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.crl.CrlStorageProvider;
import org.keycloak.crl.CrlStorageProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import org.infinispan.Cache;

/**
 * {@link CrlStorageProvider} 的 Infinispan 实现工厂。
 * <p>
 * 管理 CRL 专用缓存引用、并发加载去重映射，以及 {@code cacheTime} /
 * {@code minTimeBetweenRequests} 等可配置 TTL 策略。
 *
 * @author rmartinc
 */
public class InfinispanCrlStorageProviderFactory implements CrlStorageProviderFactory, InfinispanCrlStorageProvider.SharedData {

    /** 工厂在 Keycloak SPI 中的标识符。 */
    public static final String PROVIDER_ID = "infinispan";

    /** 懒加载的 CRL Infinispan 缓存。 */
    private volatile Cache<String, X509CRLEntry> crlCache;
    /** 按 CRL 键跟踪进行中的加载任务，避免 thundering herd。 */
    private final Map<String, FutureTask<X509CRL>> tasksInProgress = new ConcurrentHashMap<>();
    /** CRL 条目最大缓存时长（毫秒）；{@code -1} 表示无固定上限。 */
    private volatile long cacheTime;
    /** 两次 CRL 远程请求之间的最小间隔（毫秒）。 */
    private volatile long minTimeBetweenRequests;

    /** {@inheritDoc} 创建绑定本工厂共享数据的存储提供者。 */
    @Override
    public CrlStorageProvider create(KeycloakSession session) {
        lazyInit(session);
        return new InfinispanCrlStorageProvider(this);
    }

    /** {@inheritDoc} 暴露 {@code cacheTime} 与 {@code minTimeBetweenRequests} 配置项。 */
    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                    .name("cacheTime")
                    .type("int")
                    .helpText(
                            """
                            Interval in seconds that the CRL is cached. The next update time of the CRL is always a minimum if present.
                            Zero or a negative value means CRL is cached until the next update time specified in the CRL (or infinite if the
                            CRL does not contain the next update).
                            """
                    )
                    .defaultValue(-1)
                    .add()
                .property()
                    .name("minTimeBetweenRequests")
                    .type("int")
                    .helpText(
                            """
                            Minimum interval in seconds between two requests to retrieve the CRL. The CRL is not updated
                            from the URL again until this minimum time has passed since the previous refresh. In theory
                            this option is never used if the CRL is refreshed correctly in the next update time.
                            The interval should be a positive number. Default 10 seconds.
                            """
                    )
                    .defaultValue(10)
                    .add()
                .build();
    }

    /** {@inheritDoc} 将秒级配置转换为毫秒并存储。 */
    @Override
    public void init(Config.Scope config) {
        final long tmpCacheTime = config.getLong("cacheTime", -1L);
        cacheTime = tmpCacheTime > 0? TimeUnit.SECONDS.toMillis(tmpCacheTime) : -1L;

        final long tmpMinTimeBetweenRequests = config.getLong("minTimeBetweenRequests", 10L);
        minTimeBetweenRequests = tmpMinTimeBetweenRequests > 0? TimeUnit.SECONDS.toMillis(tmpMinTimeBetweenRequests) : 10_000L;
    }

    /** {@inheritDoc} 无后置初始化逻辑。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // no-op
    }

    /** {@inheritDoc} 缓存由连接提供者统一管理。 */
    @Override
    public void close() {
        // no-op
    }

    /** {@inheritDoc} 返回 {@link #PROVIDER_ID}。 */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** 双重检查锁定：首次创建提供者时解析 CRL 专用 Infinispan 缓存。 */
    private void lazyInit(KeycloakSession session) {
        if (crlCache == null) {
            synchronized (this) {
                if (crlCache == null) {
                    this.crlCache = session.getProvider(InfinispanConnectionProvider.class).getCache(InfinispanConnectionProvider.CRL_CACHE_NAME);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Cache<String, X509CRLEntry> cache() {
        return crlCache;
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, FutureTask<X509CRL>> tasksInProgress() {
        return tasksInProgress;
    }

    /** {@inheritDoc} */
    @Override
    public long cacheTime() {
        return cacheTime;
    }

    /** {@inheritDoc} */
    @Override
    public long minTimeBetweenRequests() {
        return minTimeBetweenRequests;
    }
}
