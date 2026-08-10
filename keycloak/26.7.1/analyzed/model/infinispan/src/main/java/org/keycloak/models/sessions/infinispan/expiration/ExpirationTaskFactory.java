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

package org.keycloak.models.sessions.infinispan.expiration;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;

import org.keycloak.Config;
import org.keycloak.config.MetricsOptions;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.infinispan.util.InfinispanUtils;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionProvider;
import org.keycloak.models.sessions.infinispan.InfinispanUserSessionProviderFactory;
import org.keycloak.provider.ProviderFactory;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.infinispan.client.hotrod.RemoteCache;

import static org.keycloak.connections.infinispan.InfinispanConnectionProvider.WORK_CACHE_NAME;

/**
 * {@link ExpirationTask} 工厂：按 Infinispan 部署模式（嵌入式/远程）创建对应实现。
 * <p>
 * 返回的实例尚未调用 {@link ExpirationTask#start()}。
 */
public final class ExpirationTaskFactory {

    /**
     * 根据 Keycloak 配置创建 {@link ExpirationTask}；若启用 metrics 则注册耗时 Timer。
     *
     * @param session 当前 {@link KeycloakSession}
     * @return 尚未启动的新 {@link ExpirationTask} 实例
     */
    public static ExpirationTask create(KeycloakSession session, int expirationPeriodSeconds) {
        Consumer<Duration> onTaskExecuted = null;
        if (Config.scope().root().getBoolean(MetricsOptions.METRICS_ENABLED.getKey(), Boolean.FALSE)) {
            var timer = Timer.builder("keycloak.session.expiration.task")
                    .description("Keycloak User and Client sessions expiration tasks duration.")
                    .publishPercentileHistogram()
                    .register(Metrics.globalRegistry);
            onTaskExecuted = timer::record;
        }
        return create(session, expirationPeriodSeconds, onTaskExecuted);
    }

    /**
     * 根据显式参数创建 {@link ExpirationTask}。
     *
     * @param session                     当前 {@link KeycloakSession}
     * @param expirationTaskPeriodSeconds 检查数据库过期会话的周期（秒）
     * @param onTaskExecuted              可选回调；每轮 purge 完成时以纳秒精度报告耗时
     * @return 尚未启动的新 {@link ExpirationTask} 实例
     */
    public static ExpirationTask create(KeycloakSession session, int expirationTaskPeriodSeconds, Consumer<Duration> onTaskExecuted) {
        var connectionProvider = session.getProvider(InfinispanConnectionProvider.class);
        var schedulerExecutor = connectionProvider.getScheduledExecutor();

        if (InfinispanUtils.isEmbeddedInfinispan()) {
            var workCache = connectionProvider.getCache(WORK_CACHE_NAME);
            if (workCache.getCacheConfiguration().clustering().cacheMode().isClustered()) {
                // 嵌入式集群：按 DistributionManager 分片 realm
                var distributionManager = workCache.getAdvancedCache().getDistributionManager();
                return new DistributionAwareExpirationTask(session.getKeycloakSessionFactory(), schedulerExecutor, expirationTaskPeriodSeconds, onTaskExecuted, distributionManager);
            }

            // 单机嵌入式：本地检查全部 realm
            return new LocalExpirationTask(session.getKeycloakSessionFactory(), schedulerExecutor, expirationTaskPeriodSeconds, onTaskExecuted);
        }

        // 远程 Infinispan：通过 ConsistentHash 心跳协调分片
        RemoteCache<String, String> workCache = connectionProvider.getRemoteCache(WORK_CACHE_NAME);
        String nodeName = connectionProvider.getNodeInfo().nodeName();
        return new RemoteExpirationTask(session.getKeycloakSessionFactory(), schedulerExecutor, expirationTaskPeriodSeconds, onTaskExecuted, workCache, nodeName);
    }

    /**
     * 判断本实例是否负责清理指定 {@code realm} 的过期会话。
     * <p>
     * 仅供测试使用，生产环境勿调用。
     */
    public static boolean isSelectedForExpireSessionsInRealm(KeycloakSession session, RealmModel realm) {
        return getEventTask(session)
                .map(BaseExpirationTask::realmFilter)
                .map(filter -> filter.test(realm))
                .orElse(false);
    }

    /**
     * 手动触发一次过期清理，绕过调度器。
     * <p>
     * 仅供测试使用，生产环境勿调用。
     */
    public static void manualTriggerTask(KeycloakSession session) {
        getEventTask(session).ifPresent(BaseExpirationTask::purgeExpired);
    }

    /**
     * 返回外部 Infinispan 集群模式下已知 Keycloak 实例数量。
     * <p>
     * 仅供测试使用，生产环境勿调用。
     */
    public static int membersSize(KeycloakSession session) {
        return getEventTask(session)
                .filter(RemoteExpirationTask.class::isInstance)
                .map(RemoteExpirationTask.class::cast)
                .map(RemoteExpirationTask::membersSize)
                .orElse(0);
    }

    /** 从 {@link InfinispanUserSessionProviderFactory} 取得底层 {@link BaseExpirationTask}。 */
    private static Optional<BaseExpirationTask> getEventTask(KeycloakSession session) {
        ProviderFactory<UserSessionProvider> provider = session.getKeycloakSessionFactory().getProviderFactory(UserSessionProvider.class);
        if (!(provider instanceof InfinispanUserSessionProviderFactory iuspf)) {
            return Optional.empty();
        }
        ExpirationTask task = iuspf.getExpirationTask();
        if (!(task instanceof BaseExpirationTask bet)) {
            return Optional.empty();
        }
        return Optional.of(bet);
    }

}
