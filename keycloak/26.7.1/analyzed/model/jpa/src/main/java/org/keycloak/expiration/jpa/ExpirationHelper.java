/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.expiration.jpa;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import org.keycloak.Config;
import org.keycloak.common.util.DurationConverter;
import org.keycloak.config.OptionsUtil;
import org.keycloak.executors.ExecutorsProvider;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.configuration.ServerConfigStorageProvider;
import org.keycloak.timer.TimerProvider;

import org.jboss.logging.Logger;

import static org.keycloak.executors.ExecutorsProvider.EXPIRATION_TASKS;

/**
 * 在 Provider 工厂中配置与启动 {@link ExpirationTask} 的共享工具类。
 * <p>
 * 提供标准配置键（{@code expirationTaskIntervalSeconds}、{@code expirationTaskTimeoutSeconds}）、
 * 时长解析及执行器查找。
 * </p>
 */
public final class ExpirationHelper {

    private static final String EXPIRATION_TASK_INTERVAL_KEY = "expirationTaskIntervalSeconds";
    private static final int DEFAULT_EXPIRATION_TASK_INTERVAL = 600;
    private static final String EXPIRATION_TASK_TIMEOUT_KEY = "expirationTaskTimeoutSeconds";
    private static final int DEFAULT_EXPIRATION_TASK_TIMEOUT = 300;
    private static final String EXPIRATION_TASK_MAX_REMOVAL_KEY = "expirationTaskMaxRemoval";

    private ExpirationHelper() {
    }

    /** 读取过期清理任务的运行间隔（秒）。 */
    public static int getExpirationTaskInterval(Config.Scope config, Logger logger) {
        return parseDuration(config, logger, EXPIRATION_TASK_INTERVAL_KEY, DEFAULT_EXPIRATION_TASK_INTERVAL, "expiration task interval");
    }

    /** 读取单次过期清理事务的超时时间（秒）。 */
    public static int getExpirationTaskTimeout(Config.Scope config, Logger logger) {
        return parseDuration(config, logger, EXPIRATION_TASK_TIMEOUT_KEY, DEFAULT_EXPIRATION_TASK_TIMEOUT, "expiration task timeout");
    }

    /**
     * 从 Provider 配置读取每批最多删除条目数；未设置或无效时回退到
     * {@link ExpirationTaskBuilder#DEFAULT_MAX_REMOVAL}。
     */
    public static int getExpirationTaskMaxRemoval(Config.Scope config, Logger logger) {
        var value = config.getInt(EXPIRATION_TASK_MAX_REMOVAL_KEY, ExpirationTaskBuilder.DEFAULT_MAX_REMOVAL);
        if (value <= 0) {
            logger.warnf("Invalid expiration task max removal specified: %d. Using default value of %d.", value, ExpirationTaskBuilder.DEFAULT_MAX_REMOVAL);
            return ExpirationTaskBuilder.DEFAULT_MAX_REMOVAL;
        }
        return value;
    }

    /** 过期任务依赖的执行器、定时器与服务器配置存储 Provider。 */
    public static Set<Class<? extends Provider>> dependsOn() {
        return Set.of(ExecutorsProvider.class, TimerProvider.class, ServerConfigStorageProvider.class);
    }

    /** 从会话工厂获取专用于过期任务的线程池执行器。 */
    public static Executor expirationExecutor(KeycloakSessionFactory factory) {
        try (var session = factory.create()) {
            return session.getProvider(ExecutorsProvider.class).getExecutor(EXPIRATION_TASKS);
        }
    }

    /** 向 Provider 配置构建器追加过期任务相关属性项。 */
    public static void addConfiguration(ProviderConfigurationBuilder builder, String what) {
        builder.property()
                .name(EXPIRATION_TASK_INTERVAL_KEY)
                .type(ProviderConfigProperty.STRING_TYPE)
                .helpText("The interval in seconds between expired " + what + " cleanup runs. " + OptionsUtil.DURATION_DESCRIPTION)
                .defaultValue(DEFAULT_EXPIRATION_TASK_INTERVAL)
                .add();
        builder.property()
                .name(EXPIRATION_TASK_TIMEOUT_KEY)
                .type(ProviderConfigProperty.STRING_TYPE)
                .helpText("The transaction timeout in seconds for each expired " + what + " cleanup run. " + OptionsUtil.DURATION_DESCRIPTION)
                .defaultValue(DEFAULT_EXPIRATION_TASK_TIMEOUT)
                .add();
        builder.property()
                .name(EXPIRATION_TASK_MAX_REMOVAL_KEY)
                .type(ProviderConfigProperty.INTEGER_TYPE)
                .helpText("The maximum number of expired " + what + " entries to remove per batch.")
                .defaultValue(ExpirationTaskBuilder.DEFAULT_MAX_REMOVAL)
                .add();
    }

    /** 将过期任务运行参数写入运维信息 Map。 */
    public static void addToOperationalInfo(int interval, int timeout, int maxRemoval, Map<String, String> info) {
        info.put(EXPIRATION_TASK_INTERVAL_KEY, interval + " seconds");
        info.put(EXPIRATION_TASK_TIMEOUT_KEY, timeout + " seconds");
        info.put(EXPIRATION_TASK_MAX_REMOVAL_KEY, Integer.toString(maxRemoval));
    }

    /** 解析配置中的时长字符串为秒数，无效时返回默认值并记录警告。 */
    private static int parseDuration(Config.Scope config, Logger logger, String key, int defaultValueSeconds, String what) {
        var duration = DurationConverter.parseDuration(config.get(key));
        if (duration == null) {
            return defaultValueSeconds;
        }
        var seconds = Math.toIntExact(duration.getSeconds());
        if (seconds <= 0) {
            logger.warnf("Invalid %s specified: %s. Using default value of %s seconds.", what, duration, defaultValueSeconds);
            return defaultValueSeconds;
        }
        return seconds;
    }

}
