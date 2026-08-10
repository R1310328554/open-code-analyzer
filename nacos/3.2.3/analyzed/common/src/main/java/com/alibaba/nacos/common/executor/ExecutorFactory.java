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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Unified thread pool creation factory, and actively create thread pool resources by ThreadPoolManager for unified life
 * cycle management {@link ExecutorFactory.Managed}.
 *
 * <p>Unified thread pool creation factory without life cycle management {@link ExecutorFactory}.
 * <p>Nacos 统一线程池工厂：顶层静态方法直接创建 {@link java.util.concurrent.ExecutorService}；内部类 {@link Managed} 创建后会注册到 {@link ThreadPoolManager} 统一生命周期管理。</p>
 *
 * <p>two check style ignore will be removed after issue#2856 finished.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@SuppressWarnings({"checkstyle:overloadmethodsdeclarationorder", "checkstyle:missingjavadocmethod"})
public final class ExecutorFactory {
    
    /** 创建单线程固定池（默认线程工厂） */
    public static ExecutorService newSingleExecutorService() {
        return Executors.newFixedThreadPool(1);
    }
    
    /** 创建单线程固定池（指定线程工厂） */
    public static ExecutorService newSingleExecutorService(final ThreadFactory threadFactory) {
        return Executors.newFixedThreadPool(1, threadFactory);
    }
    
    /** 创建 nThreads 大小的固定线程池（默认线程工厂） */
    public static ExecutorService newFixedExecutorService(final int nThreads) {
        return Executors.newFixedThreadPool(nThreads);
    }
    
    /** 创建 nThreads 大小的固定线程池（指定线程工厂） */
    public static ExecutorService newFixedExecutorService(final int nThreads,
        final ThreadFactory threadFactory) {
        return Executors.newFixedThreadPool(nThreads, threadFactory);
    }
    
    /** 创建单线程调度线程池 */
    public static ScheduledExecutorService newSingleScheduledExecutorService(
        final ThreadFactory threadFactory) {
        return Executors.newScheduledThreadPool(1, threadFactory);
    }
    
    /** 创建 nThreads 大小的调度线程池 */
    public static ScheduledExecutorService newScheduledExecutorService(final int nThreads,
        final ThreadFactory threadFactory) {
        return Executors.newScheduledThreadPool(nThreads, threadFactory);
    }
    
    /** 创建自定义 {@link ThreadPoolExecutor}（无界 {@link java.util.concurrent.LinkedBlockingQueue}） */
    public static ThreadPoolExecutor newCustomerThreadExecutor(final int coreThreads,
        final int maxThreads,
        final long keepAliveTimeMs, final ThreadFactory threadFactory) {
        return new ThreadPoolExecutor(coreThreads, maxThreads, keepAliveTimeMs,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), threadFactory);
    }
    
    public static final class Managed {
        
        /** 注册到 {@link ThreadPoolManager} 时使用的默认命名空间 */
        private static final String DEFAULT_NAMESPACE = "nacos";
        
        /** 全局线程池管理器单例 */
        private static final ThreadPoolManager THREAD_POOL_MANAGER =
            ThreadPoolManager.getInstance();
        
        /**
         * Create a new single executor service with default thread factory and register to manager.
         * <p>创建单线程池并注册到 {@link ThreadPoolManager}，便于统一销毁。</p>
         *
         * @param group group name
         * @return new single executor service
         */
        public static ExecutorService newSingleExecutorService(final String group) {
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            THREAD_POOL_MANAGER.register(DEFAULT_NAMESPACE, group, executorService);
            return executorService;
        }
        
        /**
         * Create a new single executor service with input thread factory and register to manager.
         * <p>使用指定线程工厂创建单线程池并注册到管理器。</p>
         *
         * @param group         group name
         * @param threadFactory thread factory
         * @return new single executor service
         */
        public static ExecutorService newSingleExecutorService(final String group,
            final ThreadFactory threadFactory) {
            ExecutorService executorService = Executors.newFixedThreadPool(1, threadFactory);
            THREAD_POOL_MANAGER.register(DEFAULT_NAMESPACE, group, executorService);
            return executorService;
        }
        
        /**
         * Create a new fixed executor service with default thread factory and register to manager.
         * <p>创建固定大小线程池并注册到管理器。</p>
         *
         * @param group    group name
         * @param nThreads thread number
         * @return new fixed executor service
         */
        public static ExecutorService newFixedExecutorService(final String group,
            final int nThreads) {
            ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
            THREAD_POOL_MANAGER.register(DEFAULT_NAMESPACE, group, executorService);
            return executorService;
        }
        
        /**
         * Create a new fixed executor service with input thread factory and register to manager.
         * <p>使用指定线程工厂创建固定大小线程池并注册。</p>
         *
         * @param group         group name
         * @param nThreads      thread number
         * @param threadFactory thread factory
         * @return new fixed executor service
         */
        public static ExecutorService newFixedExecutorService(final String group,
            final int nThreads,
            final ThreadFactory threadFactory) {
            ExecutorService executorService = Executors.newFixedThreadPool(nThreads, threadFactory);
            THREAD_POOL_MANAGER.register(DEFAULT_NAMESPACE, group, executorService);
            return executorService;
        }
        
        /**
         * Create a new single scheduled executor service with input thread factory and register to manager.
         * <p>创建单线程调度池并注册到管理器。</p>
         *
         * @param group         group name
         * @param threadFactory thread factory
         * @return new single scheduled executor service
         */
        public static ScheduledExecutorService newSingleScheduledExecutorService(final String group,
            final ThreadFactory threadFactory) {
            ScheduledExecutorService executorService =
                Executors.newScheduledThreadPool(1, threadFactory);
            THREAD_POOL_MANAGER.register(DEFAULT_NAMESPACE, group, executorService);
            return executorService;
        }
        
        /**
         * Create a new scheduled executor service with input thread factory and register to manager.
         * <p>创建多线程调度池并注册到管理器。</p>
         *
         * @param group         group name
         * @param nThreads      thread number
         * @param threadFactory thread factory
         * @return new scheduled executor service
         */
        public static ScheduledExecutorService newScheduledExecutorService(final String group,
            final int nThreads,
            final ThreadFactory threadFactory) {
            ScheduledExecutorService executorService =
                Executors.newScheduledThreadPool(nThreads, threadFactory);
            THREAD_POOL_MANAGER.register(DEFAULT_NAMESPACE, group, executorService);
            return executorService;
        }
        
        /**
         * Create a new custom executor service and register to manager.
         * <p>创建自定义 {@link ThreadPoolExecutor} 并注册到管理器。</p>
         *
         * @param group           group name
         * @param coreThreads     core thread number
         * @param maxThreads      max thread number
         * @param keepAliveTimeMs keep alive time milliseconds
         * @param threadFactory   thread factory
         * @return new custom executor service
         */
        public static ThreadPoolExecutor newCustomerThreadExecutor(final String group,
            final int coreThreads,
            final int maxThreads, final long keepAliveTimeMs, final ThreadFactory threadFactory) {
            ThreadPoolExecutor executor =
                new ThreadPoolExecutor(coreThreads, maxThreads, keepAliveTimeMs,
                    TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), threadFactory);
            THREAD_POOL_MANAGER.register(DEFAULT_NAMESPACE, group, executor);
            return executor;
        }
        
        @JustForTest
        public static ThreadPoolManager getThreadPoolManager() {
            return THREAD_POOL_MANAGER;
        }
    }
}
