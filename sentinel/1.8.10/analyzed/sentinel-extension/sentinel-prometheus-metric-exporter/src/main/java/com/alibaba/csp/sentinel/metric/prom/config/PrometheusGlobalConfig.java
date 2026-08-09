/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.metric.prom.config;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * Prometheus 指标导出全局配置：端口、抓取条数、延迟、资源过滤与指标类型等。
 * 配置项通过 {@link SentinelConfig} 读取，支持 JVM 参数覆盖。
 *
 * @author karl-sy
 * @date 2023-07-13 21:15
 * @since 2.0.0
 */
public class PrometheusGlobalConfig {

    /** HTTP 抓取端口配置键。 */
    public static final String PROM_FETCH_PORT = "csp.sentinel.prometheus.fetch.port";
    /** 默认抓取端口。 */
    public static final String DEFAULT_PROM_FETCH_PORT = "9092";

    /** 单次抓取最大 MetricNode 条数配置键。 */
    public static final String PROM_FETCH_SIZE = "csp.sentinel.prometheus.fetch.size";
    public static final String DEFAULT_PROM_FETCH_SIZE = "1024";

    /** 抓取延迟秒数配置键（跳过最近 N 秒未落盘数据）。 */
    public static final String PROM_FETCH_DELAY = "csp.sentinel.prometheus.fetch.delay";
    public static final String DEFAULT_PROM_FETCH_DELAY = "0";

    /** 资源名过滤配置键。 */
    public static final String PROM_FETCH_IDENTIFY = "csp.sentinel.prometheus.fetch.identify";

    /** 导出指标类型列表配置键（{@code |} 分隔）。 */
    public static final String PROM_FETCH_TYPES = "csp.sentinel.prometheus.fetch.types";
    public static final String DEFAULT_PROM_FETCH_TYPES = "passQps|blockQps|exceptionQps|rt|concurrency";

    /** Prometheus 指标族名称（app）配置键。 */
    public static final String PROM_APP = "csp.sentinel.prometheus.app";
    public static final String DEFAULT_PROM_APP = "SENTINEL_APP";

    /** @return HTTP 抓取端口 */
    public static int getPromFetchPort() {
        String config = SentinelConfig.getConfig(PROM_FETCH_PORT);
        config = StringUtil.isNotBlank(config) ? config : DEFAULT_PROM_FETCH_PORT;
        return Integer.parseInt(config);
    }

    /** @return 单次抓取条数上限 */
    public static int getPromFetchSize() {
        String config = SentinelConfig.getConfig(PROM_FETCH_SIZE);
        config = StringUtil.isNotBlank(config) ? config : DEFAULT_PROM_FETCH_SIZE;
        return Integer.parseInt(config);
    }

    /** @return 抓取延迟秒数 */
    public static int getPromFetchDelayTime() {
        String config = SentinelConfig.getConfig(PROM_FETCH_DELAY);
        config = StringUtil.isNotBlank(config) ? config : DEFAULT_PROM_FETCH_DELAY;
        return Integer.parseInt(config);
    }

    /** @return 资源名过滤条件，可为 null */
    public static String getPromFetchIdentify() {
        return SentinelConfig.getConfig(PROM_FETCH_IDENTIFY);
    }

    /** @return 需导出的指标类型数组 */
    public static String[] getPromFetchTypes() {
        String config = SentinelConfig.getConfig(PROM_FETCH_TYPES);
        config = StringUtil.isNotBlank(config) ? config : DEFAULT_PROM_FETCH_TYPES;
        try {
            return config.split("\\|");
        }catch (Throwable e){
            return DEFAULT_PROM_FETCH_TYPES.split("\\|");
        }
    }

    /** @return 规范化后的 Prometheus 指标族名称 */
    public static String getPromFetchApp() {
        String appName = SentinelConfig.getConfig(PROM_APP);
        if (appName == null) {
            appName = SentinelConfig.getAppName();
        }

        if (appName == null) {
            appName = DEFAULT_PROM_APP;
        }
        appName = appName.replaceAll("\\.","_");
        appName = appName.replaceAll("-","_");
        return appName;
    }

}
