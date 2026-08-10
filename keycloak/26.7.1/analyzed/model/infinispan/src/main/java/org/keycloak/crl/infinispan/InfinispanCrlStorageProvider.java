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

import java.security.GeneralSecurityException;
import java.security.cert.X509CRL;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.keycloak.common.util.Time;
import org.keycloak.crl.CrlStorageProvider;

import org.infinispan.Cache;
import org.jboss.logging.Logger;

/**
 * 基于 Infinispan 缓存的 X509 CRL 存储与加载提供者。
 * <p>
 * 支持按 {@code nextUpdate} 与配置的缓存时长决定条目有效期，并通过
 * {@code putIfAbsent} 风格的任务去重避免同一 CRL 并发重复拉取。
 *
 * @author rmartinc
 */
public class InfinispanCrlStorageProvider implements CrlStorageProvider {

    private static final Logger log = Logger.getLogger(InfinispanCrlStorageProvider.class);

    /** 与工厂共享的缓存、进行中的加载任务及 TTL 配置。 */
    private final SharedData data;

    /**
     * @param data 工厂提供的共享缓存与配置数据
     */
    public InfinispanCrlStorageProvider(SharedData data) {
        this.data = data;
    }

    /**
     * {@inheritDoc} 优先返回未过期的缓存条目；否则通过 {@code loader} 重新加载并写回缓存。
     */
    @Override
    public X509CRL get(String key, Callable<X509CRL> loader) throws GeneralSecurityException {
        final X509CRLEntry crlEntry = data.cache().get(key);
        final long currentTime = Time.currentTimeMillis();
        if (crlEntry != null && (crlEntry.crl().getNextUpdate() == null || crlEntry.crl().getNextUpdate().compareTo(new Date(currentTime)) > 0)) {
            log.debugf("returning CRL '%s' from cache because it's cached OK", key);
            return crlEntry.crl();
        }

        // 缓存未命中或已过期，重新加载 CRL 条目
        return reloadCrl(key, loader, currentTime, crlEntry);
    }

    /**
     * {@inheritDoc} 强制刷新指定键的 CRL，返回缓存内容是否发生变化。
     */
    @Override
    public boolean refreshCache(String key, Callable<X509CRL> loader) throws GeneralSecurityException {
        final X509CRLEntry entry = data.cache().get(key);
        final X509CRL crl = reloadCrl(key, loader, Time.currentTimeMillis(), entry);
        return  crl != null && (entry == null || entry.crl() != crl);
    }

    /** {@inheritDoc} 无额外资源需释放。 */
    @Override
    public void close() {
        // no-op
    }

    /**
     * 在最小请求间隔保护下重新加载 CRL；同一键的并发请求共享单个 {@link FutureTask}。
     *
     * @param key         CRL 缓存键
     * @param loader      实际从 URL/存储拉取 CRL 的可调用对象
     * @param currentTime 当前时间戳（毫秒）
     * @param crlEntry    已有的缓存条目，可能为 {@code null}
     * @return 加载后的 X509CRL，失败时可能为 {@code null}
     */
    private X509CRL reloadCrl(String key, Callable<X509CRL> loader, long currentTime, X509CRLEntry crlEntry) {
        if (crlEntry != null && currentTime < crlEntry.lastRequestTime()+ data.minTimeBetweenRequests()){
            log.debugf("Avoiding loading crl with key '%s' again, last refreshed time %d", key, crlEntry.lastRequestTime());
            return crlEntry.crl();
        }

        FutureTask<X509CRL> task = new FutureTask<>(() -> loadCrl(key, loader, currentTime));

        final FutureTask<X509CRL> existing = data.tasksInProgress().putIfAbsent(key, task);
        if (existing == null) {
            log.debugf("Reloading crl for model key '%s'.", key);
            task.run();
        } else {
            task = existing;
        }

        try {
            return task.get();
        } catch (ExecutionException ee) {
            throw new RuntimeException("Error when loading crl " + key + " : " + ee.getMessage(), ee);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error. Interrupted when loading crl " + key, ie);
        } finally {
            // 若本线程首次插入任务，加载完成后从进行中队列移除
            if (existing == null) {
                data.tasksInProgress().remove(key);
            }
        }
    }

    /**
     * 调用 loader 获取 CRL 并按 lifespan 写入 Infinispan 缓存。
     *
     * @param key         缓存键
     * @param loader      CRL 加载器
     * @param currentTime 加载时刻（毫秒）
     */
    private X509CRL loadCrl(String key, Callable<X509CRL> loader, long currentTime) throws Exception {
        final X509CRL crl = loader.call();
        if (crl == null) {
            log.warnf("Loading crl with key '%s' returned null.", key);
            return null;
        }
        long lifespan = getLifespan(crl, currentTime);
        if (lifespan > 0) {
            data.cache().put(key, new X509CRLEntry(crl, currentTime), lifespan, TimeUnit.MILLISECONDS);
            log.debugf("The crl with key '%s' was retrieved successfully and cached for %d millis.", key, lifespan);
        } else {
            data.cache().put(key, new X509CRLEntry(crl, currentTime));
            log.debugf("The crl with key '%s' was retrieved successfully and cached forever.", key);
        }
        return crl;
    }

    /**
     * 根据 CRL {@code nextUpdate} 与工厂配置的 {@code cacheTime} 计算缓存存活时间（毫秒）。
     *
     * @param crl         待缓存的 CRL
     * @param currentTime 当前时间戳
     * @return 缓存 TTL；{@code <= 0} 表示永不过期（由 Infinispan 默认策略处理）
     */
    private long getLifespan(X509CRL crl, long currentTime) {
        final long cacheTime = data.cacheTime();

        if (crl.getNextUpdate() == null) {
            return cacheTime;
        }

        final long nextUpdateTime = crl.getNextUpdate().getTime() - currentTime;
        if (nextUpdateTime <= 0) {
            // CRL 已过期，仅缓存最短间隔以避免频繁请求
            return data.minTimeBetweenRequests();
        } else if (cacheTime > 0) {
            // 取配置的 cacheTime 与 nextUpdate 剩余时间的较小值
            return Math.min(cacheTime, nextUpdateTime);
        } else {
            // 默认无限缓存时，以 nextUpdate 为上限
            return nextUpdateTime;
        }
    }

    /**
     * 由工厂实现的共享数据接口：暴露 CRL 缓存、进行中的加载任务及 TTL 配置。
     */
    protected interface SharedData {
        /** CRL Infinispan 缓存。 */
        Cache<String, X509CRLEntry> cache();
        /** 按键去重的进行中 CRL 加载任务。 */
        Map<String, FutureTask<X509CRL>> tasksInProgress();
        /** 配置的 CRL 最大缓存时长（毫秒），{@code -1} 表示无固定上限。 */
        long cacheTime();
        /** 两次远程 CRL 请求之间的最小间隔（毫秒）。 */
        long minTimeBetweenRequests();
    }
}
