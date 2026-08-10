/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.executors;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import org.jboss.logging.Logger;

/**
 * 默认执行器 SPI 工厂：按任务类型提供线程池。
 * <p>托管环境（WildFly）优先通过 JNDI 查找 {@code java:jboss/ee/concurrency/executor/<taskType>}；嵌入式模式则按配置创建固定或弹性线程池。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DefaultExecutorsProviderFactory implements ExecutorsProviderFactory {

    protected static final Logger logger = Logger.getLogger(DefaultExecutorsProviderFactory.class);

    private static final int DEFAULT_MIN_THREADS = 4;
    private static final int DEFAULT_MAX_THREADS = 64;

    /** WildFly 托管执行器 JNDI 名称前缀。 */
    private static final String MANAGED_EXECUTORS_SERVICE_JNDI_PREFIX = "java:jboss/ee/concurrency/executor/";

    // WildFly 默认托管执行器 JNDI 名称
    private static final String DEFAULT_MANAGED_EXECUTORS_SERVICE_JNDI = MANAGED_EXECUTORS_SERVICE_JNDI_PREFIX + "default";

    /** 工厂配置作用域。 */
    private Config.Scope config;

    /** 是否运行在托管（WildFly）环境，首次检测后缓存。 */
    private Boolean managed = null;

    /** 按任务类型缓存的线程池映射。 */
    private final Map<String, ExecutorService> executors = new ConcurrentHashMap<>();


    @Override
    /** @param session 当前会话 @return 按任务类型委托本工厂线程池的执行器提供者 */
    public ExecutorsProvider create(KeycloakSession session) {
        return new ExecutorsProvider() {

            @Override
            public ExecutorService getExecutor(String taskType) {
                return DefaultExecutorsProviderFactory.this.getExecutor(taskType);
            }

            @Override
            /** 嵌入式模式下关闭所有已创建的线程池。 */
    public void close() {

            }
        };
    }

    @Override
    /** 保存工厂配置供嵌入式线程池创建使用。 */
    public void init(Config.Scope config) {
        this.config = config;
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {
        if (managed != null && !managed) {
            for (Map.Entry<String, ExecutorService> executor : executors.entrySet()) {
                logger.debugf("Shutting down executor for task '%s'", executor.getKey());
                executor.getValue().shutdown();
            }
        }
    }

    @Override
    /** @return SPI 工厂标识 {@code default} */
    public String getId() {
        return "default";
    }


    // 内部实现

    /** 按任务类型获取或懒创建线程池。 */
    protected ExecutorService getExecutor(String taskType) {
        ExecutorService existing = executors.get(taskType);

        if (existing == null) {
            synchronized (this) {
                if (!executors.containsKey(taskType)) {
                    ExecutorService executor = retrievePool(taskType);
                    executors.put(taskType, executor);
                }

                existing = executors.get(taskType);
            }
        }

        return existing;
    }


    /** 托管环境走 JNDI，否则创建嵌入式线程池。 */
    protected ExecutorService retrievePool(String taskType) {
        if (managed == null) {
            detectManaged();
        }

        if (managed) {
            return getPoolManaged(taskType);
        } else {
            return createPoolEmbedded(taskType);
        }
    }

    /** 探测是否存在 WildFly 托管执行器 JNDI 绑定。 */
    protected void detectManaged() {
        String jndiName = MANAGED_EXECUTORS_SERVICE_JNDI_PREFIX + "default";
        try {
            new InitialContext().lookup(jndiName);
            logger.debugf("We are in managed environment. Executor '%s' was available.", jndiName);
            managed = true;
        } catch (NamingException nnfe) {
            logger.debugf("We are not in managed environment. Executor '%s' was not available.", jndiName);
            managed = false;
        }
    }


    /** 从 JNDI 查找任务专用或默认托管执行器。 */
    protected ExecutorService getPoolManaged(String taskType) {
        try {
            InitialContext ctx = new InitialContext();

            // 优先查找任务专用 JNDI 执行器
            String jndiName = MANAGED_EXECUTORS_SERVICE_JNDI_PREFIX + taskType;
            try {
                ExecutorService executor = (ExecutorService) ctx.lookup(jndiName);
                logger.debugf("Found executor for '%s' under JNDI name '%s'", taskType, jndiName);
                return executor;
            } catch (NameNotFoundException nnfe) {
                logger.debugf("Not found executor for '%s' under specific JNDI name '%s'. Fallback to the default pool", taskType, jndiName);

                ExecutorService executor = (ExecutorService) ctx.lookup(DEFAULT_MANAGED_EXECUTORS_SERVICE_JNDI);
                logger.debugf("Found default executor for '%s' of JNDI name '%s'", taskType, DEFAULT_MANAGED_EXECUTORS_SERVICE_JNDI);
                return executor;
            }
        } catch (NamingException ne) {
            throw new IllegalStateException(ne);
        }
    }


    /** 按 {@code <taskType>.min/max} 配置创建嵌入式线程池。 */
    protected ExecutorService createPoolEmbedded(String taskType) {
        Config.Scope currentScope = config.scope(taskType);
        int min = DEFAULT_MIN_THREADS;
        int max = DEFAULT_MAX_THREADS;

        if (currentScope != null) {
            min = currentScope.getInt("min", DEFAULT_MIN_THREADS);
            max = currentScope.getInt("max", DEFAULT_MAX_THREADS);
        }

        logger.debugf("Creating pool for task '%s': min=%d, max=%d", taskType, min, max);

        ThreadFactory threadFactory = createThreadFactory(taskType);

        if (min == max) {
            return Executors.newFixedThreadPool(min, threadFactory);
        } else {
            // 类似 newCachedThreadPool，但 min/max 线程数可配置
            return new ThreadPoolExecutor(min, max,
                    60L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(),
                    threadFactory);
        }
    }


    /** 创建带 {@code kc-<taskType>-<group>-<n>} 命名规则的线程工厂。 */
    protected ThreadFactory createThreadFactory(String taskType) {
        return new ThreadFactory() {

            private AtomicInteger i = new AtomicInteger(0);
            private int group = new Random().nextInt(2048);

            @Override
            public Thread newThread(Runnable r) {
                int threadNumber = i.getAndIncrement();
                String threadName = "kc-" + taskType + "-" + group + "-" + threadNumber;

                if (logger.isTraceEnabled()) {
                    logger.tracef("Creating thread: %s", threadName);
                }

                return new Thread(r, threadName);
            }

        };
    }

}
