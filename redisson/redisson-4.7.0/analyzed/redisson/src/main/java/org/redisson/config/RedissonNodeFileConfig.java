/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.config;

import org.redisson.api.RedissonNodeInitializer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Redisson Node 的文件/YAML 配置，继承 {@link Config} 的全部 Redis 连接项。
 * <p>额外定义 MapReduce 工作线程数、按名称的 ExecutorService 线程池
 * 及节点启动 {@link org.redisson.api.RedissonNodeInitializer} 回调。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonNodeFileConfig extends Config {

    /** MapReduce 工作线程数：0=CPU 核数，-1=禁用。 */
    private int mapReduceWorkers = 0;
    /** 节点启动完成后的初始化回调。 */
    private RedissonNodeInitializer redissonNodeInitializer;
    /** 命名 ExecutorService 名称 → 工作线程数。 */
    private Map<String, Integer> executorServiceWorkers = new HashMap<>();

    public RedissonNodeFileConfig() {
        super();
    }

    public RedissonNodeFileConfig(Config oldConf) {
        super(oldConf);
    }

    public RedissonNodeFileConfig(RedissonNodeFileConfig oldConf) {
        super(oldConf);
        this.executorServiceWorkers = new HashMap<>(oldConf.executorServiceWorkers);
        this.redissonNodeInitializer = oldConf.redissonNodeInitializer;
        this.mapReduceWorkers = oldConf.mapReduceWorkers;
    }
    
    /**
     * 设置 MapReduce 工作线程数。
     * <p>{@code 0} = 当前 CPU 核数；{@code -1} = 禁用 MapReduce。
     * <p>默认 {@code 0}。
     *
     * @param mapReduceWorkers MapReduce 线程数
     * @return config
     */
    public RedissonNodeFileConfig setMapReduceWorkers(int mapReduceWorkers) {
        this.mapReduceWorkers = mapReduceWorkers;
        return this;
    }
    public int getMapReduceWorkers() {
        return mapReduceWorkers;
    }
    
    /**
     * 按 ExecutorService 名称设置各自的工作线程数。
     *
     * @param workers 服务名 → 线程数映射
     * @return config
     */
    public RedissonNodeFileConfig setExecutorServiceWorkers(Map<String, Integer> workers) {
        this.executorServiceWorkers = workers;
        return this;
    }
    public Map<String, Integer> getExecutorServiceWorkers() {
        return executorServiceWorkers;
    }
    
    /**
     * 设置节点启动后的初始化器。
     *
     * @param redissonNodeInitializer 初始化器实例
     * @return config
     */
    public RedissonNodeFileConfig setRedissonNodeInitializer(RedissonNodeInitializer redissonNodeInitializer) {
        this.redissonNodeInitializer = redissonNodeInitializer;
        return this;
    }
    public RedissonNodeInitializer getRedissonNodeInitializer() {
        return redissonNodeInitializer;
    }

    /**
     * 从 YAML 文件加载 Redisson Node 配置。
     *
     * @param file YAML 文件
     * @return 解析后的配置
     * @throws IOException 读取或解析失败
     */
    public static RedissonNodeFileConfig fromYAML(File file) throws IOException {
        ConfigSupport support = new ConfigSupport();
        return support.fromYAML(file, RedissonNodeFileConfig.class);
    }
    
}
