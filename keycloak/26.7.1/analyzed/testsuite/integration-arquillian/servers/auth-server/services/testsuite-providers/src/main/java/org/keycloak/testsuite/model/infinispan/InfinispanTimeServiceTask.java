/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.testsuite.model.infinispan;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.infinispan.commons.logging.Log;
import org.infinispan.commons.logging.LogFactory;
import org.infinispan.commons.time.TimeService;
import org.infinispan.expiration.ExpirationManager;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.factories.impl.BasicComponentRegistry;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.tasks.ServerTask;
import org.infinispan.tasks.TaskContext;
import org.infinispan.tasks.TaskExecutionMode;
import org.infinispan.util.EmbeddedTimeService;

/**
 * Infinispan 服务器任务：按参数偏移集群时间并触发各缓存过期处理。
 */
public class InfinispanTimeServiceTask implements ServerTask<String> {

    private static final Log log = LogFactory.getLog(InfinispanTimeServiceTask.class);
    /** 任务执行上下文。 */
    private TaskContext context = null;
    /** 时间偏移量（秒）。 */
    private static int offset;

    public InfinispanTimeServiceTask() {
        log.info("InfinispanTimeServiceTask construction");
    }

    /** 替换 TimeService、重连组件并处理所有缓存过期。 */
    @Override
    public String call() {
        EmbeddedCacheManager cacheManager = context.getCacheManager();
        Map<String, Object> params = new HashMap<>();
        if (this.context.getParameters().isPresent())
            params = this.context.getParameters().get();
        if (params.containsKey("timeService")) {
            offset = (int) params.get("timeService");

            // 重新接线 TimeService
            GlobalComponentRegistry cr = GlobalComponentRegistry.of(cacheManager);
            BasicComponentRegistry bcr = cr.getComponent(BasicComponentRegistry.class);
            bcr.replaceComponent(TimeService.class.getName(), KEYCLOAK_TIME_SERVICE, true);
            cr.rewire();
            cr.rewireNamedRegistries();

            // 处理所有缓存的过期条目
            cacheManager.getCacheNames().stream()
                    .map(cacheManager::getCache)
                    .filter(Objects::nonNull)
                    .map(cache -> cache.getAdvancedCache().getExpirationManager())
                    .forEach(ExpirationManager::processExpiration);
        }

        return "InfinispanTimeServiceTask: Infinispan server time moved by " + offset + " seconds.";
    }

    @Override
    public String getName() {
        log.info("getName() called");
        return "InfinispanTimeServiceTask";
    }

    @Override
    public void setTaskContext(TaskContext context) {
        this.context = context;
    }

    /** {@inheritDoc} 在所有节点上执行。 */
    @Override
    public TaskExecutionMode getExecutionMode() {
        return TaskExecutionMode.ALL_NODES;
    }

    /** 带秒级偏移的 Keycloak 测试用 TimeService 实现。 */
    public static final TimeService KEYCLOAK_TIME_SERVICE = new EmbeddedTimeService() {

        /** 返回含偏移的当前毫秒时间戳。 */
        private long getCurrentTimeMillis() {
            return System.currentTimeMillis() + (TimeUnit.SECONDS.toMillis(offset));
        }

        @Override
        public long wallClockTime() {
            return getCurrentTimeMillis();
        }

        @Override
        public long time() {
            return TimeUnit.MILLISECONDS.toNanos(getCurrentTimeMillis());
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(getCurrentTimeMillis());
        }
    };
}
