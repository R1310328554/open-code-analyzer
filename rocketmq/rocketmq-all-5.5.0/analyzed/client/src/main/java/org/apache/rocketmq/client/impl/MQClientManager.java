/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.client.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.client.producer.ProduceAccumulator;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * 客户端实例管理器（单例）：按 clientId 复用 {@link MQClientInstance} 与
 * {@link ProduceAccumulator}，避免同一 JVM 内重复创建网络连接与后台线程。
 */
public class MQClientManager {
    /** 日志记录器。 */
    private final static Logger log = LoggerFactory.getLogger(MQClientManager.class);
    /** 全局单例。 */
    private static MQClientManager instance = new MQClientManager();
    /** 新建 MQClientInstance 时的递增序号。 */
    private AtomicInteger factoryIndexGenerator = new AtomicInteger();
    /** clientId → MQClientInstance 映射表。 */
    private ConcurrentMap<String/* clientId */, MQClientInstance> factoryTable =
        new ConcurrentHashMap<>();
    /** clientId → 发送累加器映射表。 */
    private ConcurrentMap<String/* clientId */, ProduceAccumulator> accumulatorTable =
        new ConcurrentHashMap<String, ProduceAccumulator>();


    /** 私有构造，禁止外部实例化。 */
    private MQClientManager() {

    }

    /** 返回全局单例。 */
    public static MQClientManager getInstance() {
        return instance;
    }

    /** 按配置获取或创建 MQClientInstance（无 RPC Hook）。 */
    public MQClientInstance getOrCreateMQClientInstance(final ClientConfig clientConfig) {
        return getOrCreateMQClientInstance(clientConfig, null);
    }
    /** 按配置与 RPC Hook 获取或创建 MQClientInstance；同 clientId 复用已有实例。 */
    public MQClientInstance getOrCreateMQClientInstance(final ClientConfig clientConfig, RPCHook rpcHook) {
        String clientId = clientConfig.buildMQClientId();
        MQClientInstance instance = this.factoryTable.get(clientId);
        if (null == instance) {
            instance =
                new MQClientInstance(clientConfig.cloneClientConfig(),
                    this.factoryIndexGenerator.getAndIncrement(), clientId, rpcHook);
            MQClientInstance prev = this.factoryTable.putIfAbsent(clientId, instance);
            if (prev != null) {
                instance = prev;
                log.warn("Returned Previous MQClientInstance for clientId:[{}]", clientId);
            } else {
                log.info("Created new MQClientInstance for clientId:[{}]", clientId);
            }
        }

        return instance;
    }
    /** 按 clientId 获取或创建发送累加器。 */
    public ProduceAccumulator getOrCreateProduceAccumulator(final ClientConfig clientConfig) {
        String clientId = clientConfig.buildMQClientId();
        ProduceAccumulator accumulator = this.accumulatorTable.get(clientId);
        if (null == accumulator) {
            accumulator = new ProduceAccumulator(clientId);
            ProduceAccumulator prev = this.accumulatorTable.putIfAbsent(clientId, accumulator);
            if (prev != null) {
                accumulator = prev;
                log.warn("Returned Previous ProduceAccumulator for clientId:[{}]", clientId);
            } else {
                log.info("Created new ProduceAccumulator for clientId:[{}]", clientId);
            }
        }

        return accumulator;
    }

    /** 从工厂表移除指定 clientId 的实例（关闭后清理）。 */
    public void removeClientFactory(final String clientId) {
        this.factoryTable.remove(clientId);
    }

    /** 返回 MQClientInstance 工厂表（测试或监控用）。 */
    public ConcurrentMap<String, MQClientInstance> getFactoryTable() {
        return factoryTable;
    }
}
