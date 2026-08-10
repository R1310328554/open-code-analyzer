/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.common.remote.client;

import com.alibaba.nacos.common.remote.TlsConfig;

import java.util.Properties;

/**
 * RPC TLS 配置工厂 SPI：从 {@link Properties} 解析 SDK 与集群两种场景的 {@link TlsConfig}。
 * 实现类通过 {@link com.alibaba.nacos.common.spi.NacosServiceLoader} 加载，供 gRPC 客户端构建 SSL 上下文。
 * RpcTlsConfigFactory.
 *
 * @author stone-98
 * @date 2024/4/8
 */
public interface RpcTlsConfigFactory {
    
    /**
     * 基于配置属性创建 SDK 客户端使用的 TLS 配置（证书、协议、双向认证等）。
     * Create a TlsConfig for SDK connections based on the provided properties.
     *
     * @param properties Properties containing configuration
     * @return TlsConfig instance for SDK connections
     */
    TlsConfig createSdkConfig(Properties properties);
    
    /**
     * 基于配置属性创建集群节点间通信使用的 TLS 配置。
     * Create a TlsConfig for cluster connections based on the provided properties.
     *
     * @param properties Properties containing configuration
     * @return TlsConfig instance for cluster connections
     */
    TlsConfig createClusterConfig(Properties properties);
    
    /**
     * 从 {@link Properties} 读取布尔配置项；缺失时返回默认值。
     * Get boolean property from properties.
     *
     * @param properties   Properties containing configuration
     * @param key          Key of the property
     * @param defaultValue Default value to return if the property is not found or is invalid
     * @return Boolean value of the property, or the provided defaultValue if not found or invalid
     */
    default Boolean getBooleanProperty(Properties properties, String key, Boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
}
