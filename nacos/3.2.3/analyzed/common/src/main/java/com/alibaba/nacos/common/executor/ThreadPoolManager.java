/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.common.executor;

import com.alibaba.nacos.common.JustForTest;
import com.alibaba.nacos.common.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * // TODO Access Metric.
 *
 * <p>For unified management of thread pool resources, the consumer can simply call the register method to {@link
 * ThreadPoolManager#register(String, String, ExecutorService)} the thread pool that needs to be included in the life
 * cycle management of the resource
 * <p>线程池资源统一生命周期管理器：按 namespace → group 两级组织 {@link ExecutorService}，JVM 关闭钩子会调用 {@link #shutdown()} 优雅关闭全部已注册池。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public final class ThreadPoolManager {
    
    /** 本类日志记录器 */
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolManager.class);
    
    /** namespace → group → 线程池集合 的资源表 */
    private Map<String, Map<String, Set<ExecutorService>>> resourcesManager;
    
    /** 单例实例 */
    private static final ThreadPoolManager INSTANCE = new ThreadPoolManager();
    
    /** 是否已全局 shutdown，防止重复关闭 */
    private static final AtomicBoolean CLOSED = new AtomicBoolean(false);
    
    /** 类加载时初始化资源表并注册 JVM 关闭钩子 */
    static {
        INSTANCE.init();
        ThreadUtils.addShutdownHook(new Thread(() -> {
            LOGGER.info("[ThreadPoolManager] Start destroying ThreadPool");
            shutdown();
            LOGGER.info("[ThreadPoolManager] Completed destruction of ThreadPool");
        }));
    }
    
    /** @return 全局 {@link ThreadPoolManager} 单例 */
    public static ThreadPoolManager getInstance() {
        return INSTANCE;
    }
    
    private ThreadPoolManager() {
    }
    
    private void init() {
        resourcesManager = new ConcurrentHashMap<>(8);
    }
    
    /**
     * Register the thread pool resources with the resource manager.
     * <p>将线程池纳入指定 namespace/group 的统一管理。</p>
     *
     * @param namespace namespace name
     * @param group     group name
     * @param executor  {@link ExecutorService}
     */
    public void register(String namespace, String group, ExecutorService executor) {
        resourcesManager.compute(namespace, (namespaceKey, map) -> {
            if (map == null) {
                map = new HashMap<>(8);
            }
            map.computeIfAbsent(group, groupKey -> new HashSet<>()).add(executor);
            return map;
        });
    }
    
    /**
     * Cancel the uniform lifecycle management for all threads under this resource.
     * <p>取消整个 group 下所有线程池的注册（不主动 shutdown）。</p>
     *
     * @param namespace namespace name
     * @param group     group name
     */
    public void deregister(String namespace, String group) {
        resourcesManager.computeIfPresent(namespace, (key, map) -> {
            map.remove(group);
            return map;
        });
    }
    
    /**
     * Undoing the uniform lifecycle management of {@link ExecutorService} under this resource.
     * <p>从 group 中移除单个线程池的注册。</p>
     *
     * @param namespace namespace name
     * @param group     group name
     * @param executor  {@link ExecutorService}
     */
    public void deregister(String namespace, String group, ExecutorService executor) {
        resourcesManager.computeIfPresent(namespace, (namespaceKey, map) -> {
            map.computeIfPresent(group, (groupKey, set) -> {
                set.remove(executor);
                return set;
            });
            return map;
        });
    }
    
    /**
     * Destroys all thread pool resources under this namespace.
     * <p>关闭并清空指定 namespace 下全部线程池。</p>
     *
     * @param namespace namespace
     */
    public void destroy(final String namespace) {
        Map<String, Set<ExecutorService>> map = resourcesManager.remove(namespace);
        if (map != null) {
            for (Set<ExecutorService> set : map.values()) {
                for (ExecutorService executor : set) {
                    ThreadUtils.shutdownThreadPool(executor);
                }
                set.clear();
            }
            map.clear();
        }
    }
    
    /**
     * This namespace destroys all thread pool resources under the grouping.
     * <p>关闭并移除指定 namespace/group 下的全部线程池。</p>
     *
     * @param namespace namespace
     * @param group     group
     */
    public void destroy(final String namespace, final String group) {
        resourcesManager.computeIfPresent(namespace, (namespaceKey, map) -> {
            map.computeIfPresent(group, (groupKey, set) -> {
                for (ExecutorService executor : set) {
                    ThreadUtils.shutdownThreadPool(executor);
                }
                set.clear();
                return null;
            });
            return map;
        });
    }
    
    /**
     * Shutdown thread pool manager.
     * <p>全局关闭：遍历所有 namespace 并调用 {@link #destroy(String)}，仅执行一次。</p>
     */
    public static void shutdown() {
        if (!CLOSED.compareAndSet(false, true)) {
            return;
        }
        Set<String> namespaces = INSTANCE.resourcesManager.keySet();
        for (String namespace : namespaces) {
            INSTANCE.destroy(namespace);
        }
    }
    
    @JustForTest
    public Map<String, Map<String, Set<ExecutorService>>> getResourcesManager() {
        return resourcesManager;
    }
}
