/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.client.naming.cache;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.lifecycle.Closeable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.alibaba.nacos.client.utils.LogUtils.NAMING_LOGGER;

/**
 * 服务信息磁盘缓存异步刷新器。
 *
 * <p>以固定间隔合并同一 serviceKey 的待写事件，调用 {@link DiskCache} 批量落盘，避免推送风暴时频繁写盘。</p>
 *
 * @author Zhengcy05
 */
public class ServiceInfoDiskCacheRefresher implements Closeable {
    
    /** 默认刷盘间隔（毫秒）。 */
    static final long DEFAULT_FLUSH_INTERVAL_MILLISECONDS = 100L;
    
    /** 关闭时等待刷盘线程结束的超时（毫秒）。 */
    static final long DEFAULT_SHUTDOWN_TIMEOUT_MILLISECONDS = 3000L;
    
    private static final String REFRESHER_THREAD_NAME =
        "com.alibaba.nacos.client.naming.disk.cache.refresher";
    
    /** 待刷盘事件映射，同一 serviceKey 仅保留最新快照。 */
    private final ConcurrentMap<String, ServiceInfoDiskCacheRefreshEvent> pendingEvents;
    
    /** 单线程定时刷盘执行器。 */
    private final ScheduledThreadPoolExecutor refreshExecutor;
    
    /** 磁盘写入策略（生产环境委托 {@link DiskCache}）。 */
    private final DiskCacheWriter diskCacheWriter;
    
    /** 关闭等待超时配置。 */
    private final long shutdownTimeoutMilliseconds;
    
    /**
     * 使用默认刷盘间隔与关闭超时创建刷新器。
     */
    public ServiceInfoDiskCacheRefresher() {
        this(DEFAULT_FLUSH_INTERVAL_MILLISECONDS, DEFAULT_SHUTDOWN_TIMEOUT_MILLISECONDS,
            DiskCache::writeWithResult);
    }
    
    /**
     * 供测试或自定义运行时参数使用的构造器。
     *
     * <p>生产构造器保持简洁，此构造器可注入确定性写入器。</p>
     *
     * @param flushIntervalMilliseconds 刷盘间隔（毫秒）
     * @param shutdownTimeoutMilliseconds 关闭等待超时（毫秒）
     * @param diskCacheWriter 磁盘缓存写入器
     */
    ServiceInfoDiskCacheRefresher(long flushIntervalMilliseconds, long shutdownTimeoutMilliseconds,
        DiskCacheWriter diskCacheWriter) {
        this.pendingEvents = new ConcurrentHashMap<>(16);
        this.refreshExecutor = new ScheduledThreadPoolExecutor(1,
            new NameThreadFactory(REFRESHER_THREAD_NAME));
        this.diskCacheWriter = diskCacheWriter;
        this.shutdownTimeoutMilliseconds = shutdownTimeoutMilliseconds;
        this.refreshExecutor.scheduleWithFixedDelay(this::safeFlushPendingEvents,
            flushIntervalMilliseconds, flushIntervalMilliseconds, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 发布刷新事件，同一 serviceKey 仅保留最新快照。
     *
     * @param event 刷新事件
     */
    public void publishEvent(ServiceInfoDiskCacheRefreshEvent event) {
        pendingEvents.put(event.getServiceKey(), event);
    }
    
    /** 立即刷盘所有待处理事件（包内/测试可见）。 */
    /**
     * Flush pending refresh events immediately.
      * <p>磁盘缓存异步刷盘器；详见类级说明。</p>
     */
    void flushNow() {
        safeFlushPendingEvents();
    }
    
    /**
     * 获取待刷盘事件数量。
     *
     * @return 待刷盘事件数
     */
    int pendingEventSize() {
        return pendingEvents.size();
    }
    
    /**
     * 判断刷盘执行器是否已关闭。
     *
     * @return 已关闭返回 {@code true}
     */
    boolean isShutdown() {
        return refreshExecutor.isShutdown();
    }
    
    /** 捕获异常的安全刷盘入口，避免定时任务因单次失败终止。 */
    private void safeFlushPendingEvents() {
        try {
            flushPendingEvents();
        } catch (Throwable e) {
            NAMING_LOGGER.error("[NA] failed to flush service info disk cache refresh event", e);
        }
    }
    
    /** 遍历待写事件并调用写入器，成功后从映射中移除。 */
    private void flushPendingEvents() {
        for (String serviceKey : pendingEvents.keySet()) {
            ServiceInfoDiskCacheRefreshEvent event = pendingEvents.get(serviceKey);
            if (null == event) {
                continue;
            }
            boolean writeResult =
                diskCacheWriter.write(event.getServiceInfo(), event.getCacheDir());
            if (writeResult) {
                pendingEvents.remove(serviceKey, event);
            }
        }
    }
    
    /**
     * 关闭前先刷盘，再等待执行器终止。
     *
     * @throws NacosException 等待过程中被中断时抛出
     */
    @Override
    public void shutdown() throws NacosException {
        flushPendingEvents();
        refreshExecutor.shutdown();
        try {
            if (!refreshExecutor.awaitTermination(shutdownTimeoutMilliseconds,
                TimeUnit.MILLISECONDS)) {
                NAMING_LOGGER.warn("[NA] timeout while waiting service info disk cache refresher "
                    + "to shutdown, pending event size: {}", pendingEvents.size());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NacosException(NacosException.CLIENT_DISCONNECT,
                "Interrupted while shutting down service info disk cache refresher", e);
        }
        flushPendingEvents();
    }
    
    /** 磁盘缓存写入函数式接口，便于测试注入。 */
    @FunctionalInterface
    interface DiskCacheWriter {
        
        /**
         * 将服务信息写入磁盘缓存。
         *
         * @param serviceInfo 服务信息
         * @param cacheDir 缓存目录
         * @return 写入成功返回 {@code true}
         */
        boolean write(ServiceInfo serviceInfo, String cacheDir);
    }
}
