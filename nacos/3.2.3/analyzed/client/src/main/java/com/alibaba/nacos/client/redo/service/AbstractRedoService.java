/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.redo.service;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.redo.data.RedoData;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.lifecycle.Closeable;
import com.alibaba.nacos.common.remote.client.Connection;
import com.alibaba.nacos.common.remote.client.ConnectionEventListener;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 客户端 Redo 服务抽象基类。
 *
 * <p>监听 gRPC 连接事件，断连时将缓存标记为未注册并定时调度 {@link AbstractRedoTask} 重试；各模块通过子类实现具体 redo 逻辑。</p>
 *
 * @author xiweng.yy
 */
public abstract class AbstractRedoService implements ConnectionEventListener, Closeable {
    
    /** redo 线程名格式（module 占位）。 */
    private static final String REDO_THREAD_NAME_PATTERN = "com.alibaba.nacos.client.%s.redo";
    
    /** 模块日志器。 */
    private final Logger logger;
    
    /** 定时 redo 调度线程池。 */
    private final ScheduledExecutorService redoExecutor;
    
    /** 按业务类型与 key 索引的 redo 数据映射。 */
    private final Map<Class<?>, Map<String, RedoData<?>>> redoDataMap;
    
    /** redo 线程池大小（来自配置）。 */
    private int redoThreadCount;
    
    /** redo 任务固定延迟间隔（毫秒）。 */
    private long redoDelayTime;
    
    /** gRPC 是否已连接。 */
    private volatile boolean connected = false;
    
    /** 从配置初始化 redo 参数并创建命名线程池。 */
    protected AbstractRedoService(Logger logger, NacosClientProperties properties, String module) {
        this.logger = logger;
        setProperties(properties);
        this.redoExecutor = new ScheduledThreadPoolExecutor(redoThreadCount,
            new NameThreadFactory(String.format(REDO_THREAD_NAME_PATTERN, module)));
        this.redoDataMap = new ConcurrentHashMap<>(2);
    }
    
    /** 读取 {@link PropertyKeyConst#REDO_DELAY_TIME} 等 redo 相关配置。 */
    private void setProperties(NacosClientProperties properties) {
        redoDelayTime = properties.getLong(PropertyKeyConst.REDO_DELAY_TIME,
            Constants.DEFAULT_REDO_DELAY_TIME);
        redoThreadCount = properties.getInteger(PropertyKeyConst.REDO_DELAY_THREAD_COUNT,
            Constants.DEFAULT_REDO_THREAD_COUNT);
    }
    
    /** 以固定延迟启动 redo 定时任务。 */
    protected void startRedoTask() {
        this.redoExecutor.scheduleWithFixedDelay(buildRedoTask(), redoDelayTime, redoDelayTime,
            TimeUnit.MILLISECONDS);
    }
    
    /**
     * 由子类构造具体 redo 任务。
     *
     * @return redo 任务实例
     */
    protected abstract AbstractRedoTask buildRedoTask();
    
    /** 连接建立时标记 connected 为 true。 */
    @Override
    public void onConnected(Connection connection) {
        connected = true;
        logger.info("Grpc connection connect");
    }
    
    /** 断连时将所有 redo 数据标记为未注册，触发后续重试注册。 */
    @Override
    public void onDisConnect(Connection connection) {
        connected = false;
        logger.warn("Grpc connection disconnect, mark to redo");
        for (Class<?> each : redoDataMap.keySet()) {
            Map<String, RedoData<?>> actualRedoData = this.redoDataMap.get(each);
            synchronized (actualRedoData) {
                actualRedoData.values().forEach(redoData -> redoData.setRegistered(false));
            }
        }
        logger.warn("mark to redo completed");
    }
    
    /** 清空缓存并立即关闭 redo 线程池。 */
    @Override
    public void shutdown() {
        logger.info("Shutdown grpc redo service executor {}", redoExecutor);
        redoDataMap.clear();
        redoExecutor.shutdownNow();
    }
    
    /** 返回当前 gRPC 连接状态。 */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * 按类型与 key 缓存 redo 数据。
     *
     * @param key redo 数据键
     * @param redoData redo 数据
     * @param clazz {@link RedoData} 中负载的类型
     */
    public <T> void cachedRedoData(String key, RedoData<T> redoData, Class<T> clazz) {
        Map<String, RedoData<?>> actualRedoData = this.redoDataMap.computeIfAbsent(clazz,
            k -> new ConcurrentHashMap<>(2));
        synchronized (actualRedoData) {
            actualRedoData.put(key, redoData);
        }
    }
    
    /**
     * 移除不再期望注册的 redo 条目。
     *
     * @param key redo 数据键
     * @param clazz 负载类型
     */
    public <T> void removeRedoData(String key, Class<T> clazz) {
        Map<String, RedoData<?>> actualRedoData = this.redoDataMap.computeIfAbsent(clazz,
            k -> new ConcurrentHashMap<>(2));
        synchronized (actualRedoData) {
            RedoData<?> redoData = actualRedoData.get(key);
            if (null != redoData && !redoData.isExpectedRegistered()) {
                actualRedoData.remove(key);
            }
        }
    }
    
    /**
     * 注册成功后标记 registered 状态。
     *
     * @param key redo 数据键
     * @param clazz 负载类型
     */
    public <T> void dataRegistered(String key, Class<T> clazz) {
        Map<String, RedoData<?>> actualRedoData = this.redoDataMap.computeIfAbsent(clazz,
            k -> new ConcurrentHashMap<>(2));
        synchronized (actualRedoData) {
            RedoData<?> redoData = actualRedoData.get(key);
            if (null != redoData) {
                redoData.registered();
            }
        }
    }
    
    /**
     * 发起注销时标记 unregistering 并清除期望注册。
     *
     * @param key redo 数据键
     * @param clazz 负载类型
     */
    public <T> void dataDeregister(String key, Class<T> clazz) {
        Map<String, RedoData<?>> actualRedoData = this.redoDataMap.computeIfAbsent(clazz,
            k -> new ConcurrentHashMap<>(2));
        synchronized (actualRedoData) {
            RedoData<?> redoData = actualRedoData.get(key);
            if (null != redoData) {
                redoData.setUnregistering(true);
                redoData.setExpectedRegistered(false);
            }
        }
    }
    
    /**
     * 注销完成后更新 redo 状态。
     *
     * @param key redo 数据键
     * @param clazz 负载类型
     */
    public <T> void dataDeregistered(String key, Class<T> clazz) {
        Map<String, RedoData<?>> actualRedoData = this.redoDataMap.computeIfAbsent(clazz,
            k -> new ConcurrentHashMap<>(2));
        synchronized (actualRedoData) {
            RedoData<?> redoData = actualRedoData.get(key);
            if (null != redoData) {
                redoData.unregistered();
            }
        }
    }
    
    /**
     * 判断指定 key 的数据是否已在服务端注册。
     *
     * @param key redo 数据键
     * @param clazz 负载类型
     * @return 已注册返回 {@code true}
     */
    public boolean isDataRegistered(String key, Class<?> clazz) {
        Map<String, RedoData<?>> actualRedoData = this.redoDataMap.computeIfAbsent(clazz,
            k -> new ConcurrentHashMap<>(2));
        synchronized (actualRedoData) {
            RedoData<?> redoData = actualRedoData.get(key);
            return null != redoData && redoData.isRegistered();
        }
    }
    
    /**
     * 查找指定类型下所有需要执行 redo 的数据。
     *
     * @return 待 redo 的 {@link RedoData} 集合
     */
    public <T> Set<RedoData<T>> findRedoData(Class<T> clazz) {
        Set<RedoData<T>> result = new HashSet<>();
        Map<String, RedoData<?>> actualRedoData = this.redoDataMap.computeIfAbsent(clazz,
            k -> new ConcurrentHashMap<>(2));
        synchronized (actualRedoData) {
            for (RedoData<?> each : actualRedoData.values()) {
                if (each.isNeedRedo()) {
                    result.add((RedoData<T>) each);
                }
            }
        }
        return result;
    }
    
    /**
     * 获取缓存的 redo 数据。
     *
     * @param key redo 数据键
     * @param clazz 负载类型
     * @return 缓存的 redo 数据，不存在时返回 null
     */
    public <T> RedoData<T> getRedoData(String key, Class<?> clazz) {
        Map<String, RedoData<?>> actualRedoData = this.redoDataMap.computeIfAbsent(clazz,
            k -> new ConcurrentHashMap<>(2));
        synchronized (actualRedoData) {
            return (RedoData<T>) actualRedoData.get(key);
        }
    }
}
