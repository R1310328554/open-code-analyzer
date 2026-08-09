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
package org.apache.rocketmq.broker;

import java.util.Properties;
import org.apache.rocketmq.auth.config.AuthConfig;
import org.apache.rocketmq.common.BrokerConfig;
import org.apache.rocketmq.remoting.netty.NettyClientConfig;
import org.apache.rocketmq.remoting.netty.NettyServerConfig;
import org.apache.rocketmq.store.config.MessageStoreConfig;

/**
 * Broker 启动配置上下文：聚合配置文件路径、原始 Properties 以及
 * {@link BrokerConfig}、Netty 与 {@link MessageStoreConfig}、{@link AuthConfig} 等运行时配置对象。
 */
public class ConfigContext {
    private String configFilePath;
    private Properties properties;

    private BrokerConfig brokerConfig;
    private NettyServerConfig nettyServerConfig;
    private NettyClientConfig nettyClientConfig;
    private MessageStoreConfig messageStoreConfig;
    private AuthConfig authConfig;

    /** 通过 {@link Builder} 组装不可变配置快照。 */
    private ConfigContext(Builder builder) {
        this.configFilePath = builder.configFilePath;
        this.properties = builder.properties;
        this.brokerConfig = builder.brokerConfig;
        this.nettyServerConfig = builder.nettyServerConfig;
        this.nettyClientConfig = builder.nettyClientConfig;
        this.messageStoreConfig = builder.messageStoreConfig;
        this.authConfig = builder.authConfig;
    }

    /** 返回 broker 配置文件路径。 */
    public String getConfigFilePath() {
        return configFilePath;
    }

    /** 返回从配置文件解析出的原始属性集合。 */
    public Properties getProperties() {
        return properties;
    }

    /** 返回 broker 核心运行参数。 */
    public BrokerConfig getBrokerConfig() {
        return brokerConfig;
    }

    /** 返回 Netty 服务端监听与线程池配置。 */
    public NettyServerConfig getNettyServerConfig() {
        return nettyServerConfig;
    }

    /** 返回 Netty 客户端连接与超时配置。 */
    public NettyClientConfig getNettyClientConfig() {
        return nettyClientConfig;
    }

    /** 返回消息存储层（CommitLog/ConsumeQueue）配置。 */
    public MessageStoreConfig getMessageStoreConfig() {
        return messageStoreConfig;
    }

    /** 返回认证与授权相关配置。 */
    public AuthConfig getAuthConfig() {
        return authConfig;
    }

    /** 流式构建 {@link ConfigContext} 的建造者。 */
    public static class Builder {
        private String configFilePath;
        private Properties properties;

        private BrokerConfig brokerConfig;
        private NettyServerConfig nettyServerConfig;
        private NettyClientConfig nettyClientConfig;
        private MessageStoreConfig messageStoreConfig;
        private AuthConfig authConfig;

        public Builder() {
        }

        /** 设置配置文件路径。 */
        public Builder configFilePath(String configFilePath) {
            this.configFilePath = configFilePath;
            return this;
        }

        /** 设置原始配置属性。 */
        public Builder properties(Properties properties) {
            this.properties = properties;
            return this;
        }

        /** 设置 {@link BrokerConfig}。 */
        public Builder brokerConfig(BrokerConfig brokerConfig) {
            this.brokerConfig = brokerConfig;
            return this;
        }

        /** 设置 Netty 服务端配置。 */
        public Builder nettyServerConfig(NettyServerConfig nettyServerConfig) {
            this.nettyServerConfig = nettyServerConfig;
            return this;
        }

        /** 设置 Netty 客户端配置。 */
        public Builder nettyClientConfig(NettyClientConfig nettyClientConfig) {
            this.nettyClientConfig = nettyClientConfig;
            return this;
        }

        /** 设置消息存储配置。 */
        public Builder messageStoreConfig(MessageStoreConfig messageStoreConfig) {
            this.messageStoreConfig = messageStoreConfig;
            return this;
        }

        /** 设置认证授权配置。 */
        public Builder authConfig(AuthConfig authConfig) {
            this.authConfig = authConfig;
            return this;
        }

        /** 构建不可变 {@link ConfigContext} 实例。 */
        public ConfigContext build() {
            return new ConfigContext(this);
        }
    }
}
