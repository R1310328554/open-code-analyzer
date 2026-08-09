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

package org.apache.rocketmq.store.plugin;

import org.apache.rocketmq.common.BrokerConfig;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.remoting.Configuration;
import org.apache.rocketmq.store.MessageArrivingListener;
import org.apache.rocketmq.store.config.MessageStoreConfig;
import org.apache.rocketmq.store.stats.BrokerStatsManager;

/**
 * 消息存储插件上下文：向插件注入 Store 配置、统计、监听与 Broker 配置。
 */
public class MessageStorePluginContext {
    /** 消息存储配置。 */
    private MessageStoreConfig messageStoreConfig;
    /** Broker 统计管理器。 */
    private BrokerStatsManager brokerStatsManager;
    /** 消息到达监听器。 */
    private MessageArrivingListener messageArrivingListener;
    /** Broker 运行时配置。 */
    private BrokerConfig brokerConfig;
    /** 远程配置中心句柄。 */
    private final Configuration configuration;

    public MessageStorePluginContext(MessageStoreConfig messageStoreConfig,
        BrokerStatsManager brokerStatsManager, MessageArrivingListener messageArrivingListener,
        BrokerConfig brokerConfig, Configuration configuration) {
        super();
        this.messageStoreConfig = messageStoreConfig;
        this.brokerStatsManager = brokerStatsManager;
        this.messageArrivingListener = messageArrivingListener;
        this.brokerConfig = brokerConfig;
        this.configuration = configuration;
    }

    /** 返回消息存储配置。 */
    public MessageStoreConfig getMessageStoreConfig() {
        return messageStoreConfig;
    }

    /** 返回 Broker 统计管理器。 */
    public BrokerStatsManager getBrokerStatsManager() {
        return brokerStatsManager;
    }

    /** 返回消息到达监听器。 */
    public MessageArrivingListener getMessageArrivingListener() {
        return messageArrivingListener;
    }

    /** 返回 Broker 配置。 */
    public BrokerConfig getBrokerConfig() {
        return brokerConfig;
    }

    /** 将远程配置属性注入对象并注册到配置中心。 */
    public void registerConfiguration(Object config) {
        MixAll.properties2Object(configuration.getAllConfigs(), config);
        configuration.registerConfig(config);
    }
}
