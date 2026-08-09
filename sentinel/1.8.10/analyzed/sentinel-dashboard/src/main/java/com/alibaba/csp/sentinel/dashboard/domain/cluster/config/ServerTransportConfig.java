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
package com.alibaba.csp.sentinel.dashboard.domain.cluster.config;

/**
 * 集群令牌服务端传输配置，定义 Netty 监听端口与连接空闲超时。
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ServerTransportConfig {

    /** 默认监听端口。 */
    public static final int DEFAULT_PORT = 18730;
    /** 默认连接空闲超时（秒）。 */
    public static final int DEFAULT_IDLE_SECONDS = 600;

    /** 服务端监听端口。 */
    private Integer port;
    /** 连接空闲超过该秒数将被关闭。 */
    private Integer idleSeconds;

    public ServerTransportConfig() {
        this(DEFAULT_PORT, DEFAULT_IDLE_SECONDS);
    }

    public ServerTransportConfig(Integer port, Integer idleSeconds) {
        this.port = port;
        this.idleSeconds = idleSeconds;
    }

    public Integer getPort() {
        return port;
    }

    public ServerTransportConfig setPort(Integer port) {
        this.port = port;
        return this;
    }

    public Integer getIdleSeconds() {
        return idleSeconds;
    }

    public ServerTransportConfig setIdleSeconds(Integer idleSeconds) {
        this.idleSeconds = idleSeconds;
        return this;
    }

    @Override
    public String toString() {
        return "ServerTransportConfig{" +
            "port=" + port +
            ", idleSeconds=" + idleSeconds +
            '}';
    }
}
