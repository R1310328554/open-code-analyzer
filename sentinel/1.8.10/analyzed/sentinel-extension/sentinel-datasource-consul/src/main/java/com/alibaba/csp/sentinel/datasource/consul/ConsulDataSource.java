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
package com.alibaba.csp.sentinel.datasource.consul;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AssertUtil;

import com.alibaba.csp.sentinel.util.StringUtil;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;

import java.util.concurrent.*;

/**
 * <p>
 * 基于 Consul KV 的只读数据源。
 * </p>
 * <p>
 * 初始化时从 Consul 加载规则，随后启动后台 watcher 监听 KV 变更并更新内存。
 * Consul 无原生 KV 变更推送 HTTP API，故采用
 * <a href="https://www.consul.io/api/features/blocking.html">blocking queries</a>
 * 长轮询：按 index 查询会阻塞至变更或超时；若返回 index 大于上次，表示数据已更新。
 * </p>
 *
 * @author wavesZh
 * @author Zhiguo.Chen
 */
public class ConsulDataSource<T> extends AbstractDataSource<String, T> {

    private static final int DEFAULT_PORT = 8500;

    private final String address;
    private final String token;
    private final String ruleKey;
    /** 长轮询超时时间（秒），超时或无变更则返回。 */
    private final int watchTimeout;

    /** 记录 Consul 返回的 index，用于 blocking query 增量监听。 */
    private volatile long lastIndex;

    private final ConsulClient client;

    private final ConsulKVWatcher watcher = new ConsulKVWatcher();

    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private final ExecutorService watcherService = Executors.newSingleThreadExecutor(
        new NamedThreadFactory("sentinel-consul-ds-watcher", true));

    public ConsulDataSource(String host, String ruleKey, int watchTimeoutInSecond, Converter<String, T> parser) {
        this(host, DEFAULT_PORT, ruleKey, watchTimeoutInSecond, parser);
    }

    /**
     * 构造 Consul 数据源（默认端口 8500）。
     *
     * @param parser       自定义配置解析器，不可为空
     * @param host         Consul Agent 主机
     * @param port         Consul Agent 端口
     * @param ruleKey      Consul KV 键
     * @param watchTimeout 长轮询超时（秒）
     */
    public ConsulDataSource(String host, int port, String ruleKey, int watchTimeout, Converter<String, T> parser) {
        this(host, port, null, ruleKey, watchTimeout, parser);
    }

    /**
     * 构造带 ACL Token 的 Consul 数据源。
     *
     * @param parser       自定义配置解析器
     * @param host         Consul Agent 主机
     * @param port         Consul Agent 端口
     * @param token        ACL Token（可为 null）
     * @param ruleKey      Consul KV 键
     * @param watchTimeout 长轮询超时（秒）
     */
    public ConsulDataSource(String host, int port, String token, String ruleKey, int watchTimeout, Converter<String, T> parser) {
        super(parser);
        AssertUtil.notNull(host, "Consul host can not be null");
        AssertUtil.notEmpty(ruleKey, "Consul ruleKey can not be empty");
        AssertUtil.isTrue(watchTimeout >= 0, "watchTimeout should not be negative");
        this.client = new ConsulClient(host, port);
        this.address = host + ":" + port;
        this.token = token;
        this.ruleKey = ruleKey;
        this.watchTimeout = watchTimeout;
        loadInitialConfig();
        startKVWatcher();
    }

    private void startKVWatcher() {
        watcherService.submit(watcher);
    }

    private void loadInitialConfig() {
        try {
            T newValue = loadConfig();
            if (newValue == null) {
                RecordLog.warn(
                    "[ConsulDataSource] WARN: initial config is null, you may have to check your data source");
            }
            getProperty().updateValue(newValue);
        } catch (Exception ex) {
            RecordLog.warn("[ConsulDataSource] Error when loading initial config", ex);
        }
    }

    @Override
    public String readSource() throws Exception {
        if (this.client == null) {
            throw new IllegalStateException("Consul has not been initialized or error occurred");
        }
        Response<GetValue> response = getValueImmediately(ruleKey);
        if (response != null) {
            GetValue value = response.getValue();
            lastIndex = response.getConsulIndex();
            return value != null ? value.getDecodedValue() : null;
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        watcher.stop();
        watcherService.shutdown();
    }

    private class ConsulKVWatcher implements Runnable {
        private volatile boolean running = true;

        @Override
        public void run() {
            while (running) {
                // 无变更时将阻塞最长 watchTimeout 秒
                Response<GetValue> response = getValue(ruleKey, lastIndex, watchTimeout);
                if (response == null) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(watchTimeout * 1000);
                    } catch (InterruptedException e) {
                    }
                    continue;
                }
                GetValue getValue = response.getValue();
                Long currentIndex = response.getConsulIndex();
                if (currentIndex == null || currentIndex <= lastIndex) {
                    continue;
                }
                lastIndex = currentIndex;
                if (getValue != null) {
                    String newValue = getValue.getDecodedValue();
                    try {
                        getProperty().updateValue(parser.convert(newValue));
                        RecordLog.info("[ConsulDataSource] New property value received for ({}, {}): {}",
                            address, ruleKey, newValue);
                    } catch (Exception ex) {
                        // 解析失败时记录日志，不中断 watcher
                        RecordLog.warn("[ConsulDataSource] Failed to update value for ({}, {}), raw value: {}",
                            address, ruleKey, newValue);
                    }
                }
            }
        }

        private void stop() {
            running = false;
        }
    }

    /**
     * 非阻塞方式立即读取 Consul KV。
     *
     * @param key Consul KV 键
     * @return 键对应值，失败返回 null
     */
    private Response<GetValue> getValueImmediately(String key) {
        return getValue(key, -1, -1);
    }

    /**
     * 阻塞方式读取 Consul KV（blocking query）。
     *
     * @param key      Consul KV 键
     * @param index    上次已知 index
     * @param waitTime 最长等待秒数
     * @return 键对应值，失败返回 null
     */
    private Response<GetValue> getValue(String key, long index, long waitTime) {
        try {
            if (StringUtil.isNotBlank(token)) {
                return client.getKVValue(key, token, new QueryParams(waitTime, index));
            } else {
                return client.getKVValue(key, new QueryParams(waitTime, index));
            }
        } catch (Throwable t) {
            RecordLog.warn("[ConsulDataSource] Failed to get value for key: " + key, t);
        }
        return null;
    }

}
