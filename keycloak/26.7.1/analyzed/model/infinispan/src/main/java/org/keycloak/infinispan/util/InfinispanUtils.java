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

package org.keycloak.infinispan.util;

import java.util.Map;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.common.util.MultiSiteUtils;
import org.keycloak.provider.ProviderConfigurationBuilder;

import static org.keycloak.common.Profile.Feature.CLUSTERLESS;

/**
 * Infinispan 相关工具类：部署模式判断、重试配置、虚拟线程开关等。
 */
public final class InfinispanUtils {

    /** 工具类不可实例化。 */
    private InfinispanUtils() {
    }

    /** Infinispan/JGroups 虚拟线程开关的系统属性名。 */
    private static final String INFINISPAN_VIRTUAL_THREADS_PROP = "org.infinispan.threads.virtual";

    // Infinispan 15 起 NioServer.Selector 会占用一个 worker；启用虚拟线程至少需要 4 核并行度
    /** 启用虚拟线程所需的最小并行度/池大小。 */
    private static final int MIN_VT_POOL_SIZE = 4;

    /** 所有 Infinispan 相关 provider 的统一排序值。 */
    public static final int PROVIDER_ORDER = 1;

    /** 嵌入式缓存 provider 的 ID。 */
    public static final String EMBEDDED_PROVIDER_ID = "infinispan";

    /** 远程缓存 provider 的 ID。 */
    public static final String REMOTE_PROVIDER_ID = "remote";

    /** 是否以纯外部/远程 Infinispan 模式运行（多站点或 clusterless）。 */
    public static boolean isRemoteInfinispan() {
        return MultiSiteUtils.isMultiSiteEnabled() || Profile.isFeatureEnabled(CLUSTERLESS);
    }

    /** 是否使用嵌入式 Infinispan 缓存。 */
    public static boolean isEmbeddedInfinispan() {
        return !isRemoteInfinispan();
    }

    // ---- 出错重试：指数退避 ----

    /** 默认最大重试次数。 */
    public static final int DEFAULT_MAX_RETRIES = 10;
    /** 最大重试次数配置键。 */
    private static final String CONFIG_MAX_RETRIES = "maxRetries";

    /** 指数退避基础时间（毫秒）默认值。 */
    public static final int DEFAULT_RETRIES_BASE_TIME_MILLIS = 10;
    /** 退避基础时间配置键。 */
    private static final String CONFIG_RETRIES_BASE_TIME_MILLIS = "retryBaseTime";

    /** 向 provider 配置构建器注册 maxRetries 属性。 */
    public static void configureMaxRetries(ProviderConfigurationBuilder builder) {
        builder.property()
                .name(CONFIG_MAX_RETRIES)
                .type("int")
                .helpText("The maximum number of retries if an error occurs. A value of zero or less disable any retries.")
                .defaultValue(DEFAULT_MAX_RETRIES)
                .add();
    }

    /** 向 provider 配置构建器注册 retryBaseTime 属性。 */
    public static void configureRetryBaseTime(ProviderConfigurationBuilder builder) {
        builder.property()
                .name(CONFIG_RETRIES_BASE_TIME_MILLIS)
                .type("int")
                .helpText("The base back-off time in milliseconds.")
                .defaultValue(DEFAULT_RETRIES_BASE_TIME_MILLIS)
                .add();
    }

    /** 从配置作用域读取最大重试次数（下限为 0）。 */
    public static int getMaxRetries(Config.Scope config) {
        return Math.max(0, config.getInt(CONFIG_MAX_RETRIES, DEFAULT_MAX_RETRIES));
    }

    /** 从配置作用域读取退避基础时间（下限为 1 毫秒）。 */
    public static int getRetryBaseTimeMillis(Config.Scope config) {
        return Math.max(1, config.getInt(CONFIG_RETRIES_BASE_TIME_MILLIS, DEFAULT_RETRIES_BASE_TIME_MILLIS));
    }

    /** 将 maxRetries 写入运行时可观测信息映射。 */
    public static void maxRetriesToOperationalInfo(Map<String, String> map, int value) {
        map.put(CONFIG_MAX_RETRIES, Integer.toString(value));
    }

    /** 将 retryBaseTime 写入运行时可观测信息映射。 */
    public static void retryBaseTimeMillisToOperationalInfo(Map<String, String> map, int value) {
        map.put(CONFIG_RETRIES_BASE_TIME_MILLIS, Integer.toString(value));
    }

    /** 是否已启用 Infinispan/JGroups 虚拟线程（默认 true，除非系统属性显式关闭）。 */
    public static boolean isVirtualThreadsEnabled() {
        return Boolean.parseBoolean(System.getProperty(INFINISPAN_VIRTUAL_THREADS_PROP, "true"));
    }

    /** 若用户未设置虚拟线程属性，则根据 CPU 并行度自动开启或关闭。 */
    public static void configureVirtualThreads() {
        // 默认尝试启用 Infinispan 与 JGroups 虚拟线程
        if (System.getProperty(INFINISPAN_VIRTUAL_THREADS_PROP) == null) {
            // 用户未显式设置时，由 Keycloak 根据并行度决定
            System.setProperty(INFINISPAN_VIRTUAL_THREADS_PROP, Boolean.toString(getParallelism() >= MIN_VT_POOL_SIZE));
        }
    }

    /** 启用虚拟线程时校验 JDK 虚拟线程调度器的 parallelism 与 maxPoolSize 是否满足最小要求。 */
    public static void ensureVirtualThreadsParallelism() {
        if (isVirtualThreadsEnabled()) {
            if (getParallelism() < MIN_VT_POOL_SIZE) {
                throw new RuntimeException("To be able to use Infinispan/JGroups virtual threads, you need to set the Java system property jdk.virtualThreadScheduler.parallelism to at least " + MIN_VT_POOL_SIZE);
            }
            if (getMaxPoolSize() < MIN_VT_POOL_SIZE) {
                throw new RuntimeException("To be able to use Infinispan/JGroups virtual threads, you need to set the Java system property jdk.virtualThreadScheduler.maxPoolSize to at least " + MIN_VT_POOL_SIZE);
            }
        }
    }

    /** 读取 jdk.virtualThreadScheduler.maxPoolSize，未设置时视为 {@link Integer#MAX_VALUE}。 */
    private static int getMaxPoolSize() {
        String maxPoolSizeValue = System.getProperty("jdk.virtualThreadScheduler.maxPoolSize");
        if (maxPoolSizeValue != null) {
            return Integer.parseInt(maxPoolSizeValue);
        } else {
            return Integer.MAX_VALUE;
        }
    }

    /** 读取 jdk.virtualThreadScheduler.parallelism，未设置时使用 {@link Runtime#availableProcessors()}。 */
    private static int getParallelism() {
        int parallelism;
        String parallelismValue = System.getProperty("jdk.virtualThreadScheduler.parallelism");
        if (parallelismValue != null) {
            parallelism = Integer.parseInt(parallelismValue);
        } else {
            parallelism = Runtime.getRuntime().availableProcessors();
        }
        return parallelism;
    }
}
