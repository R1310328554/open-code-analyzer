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

import java.util.Properties;

import static com.alibaba.nacos.common.remote.client.RpcConstants.ClientSuffix.MUTUAL_AUTH;
import static com.alibaba.nacos.common.remote.client.RpcConstants.ClientSuffix.TLS_CERT_CHAIN_PATH;
import static com.alibaba.nacos.common.remote.client.RpcConstants.ClientSuffix.TLS_CERT_KEY;
import static com.alibaba.nacos.common.remote.client.RpcConstants.ClientSuffix.TLS_CIPHERS;
import static com.alibaba.nacos.common.remote.client.RpcConstants.ClientSuffix.TLS_ENABLE;
import static com.alibaba.nacos.common.remote.client.RpcConstants.ClientSuffix.TLS_PROTOCOLS;
import static com.alibaba.nacos.common.remote.client.RpcConstants.ClientSuffix.TLS_PROVIDER;
import static com.alibaba.nacos.common.remote.client.RpcConstants.ClientSuffix.TLS_TRUST_ALL;
import static com.alibaba.nacos.common.remote.client.RpcConstants.ClientSuffix.TLS_TRUST_COLLECTION_CHAIN_PATH;
import static com.alibaba.nacos.common.remote.client.RpcConstants.ClientSuffix.TLS_TRUST_PWD;
import static com.alibaba.nacos.common.remote.client.RpcConstants.NACOS_CLIENT_RPC;
import static com.alibaba.nacos.common.remote.client.RpcConstants.NACOS_PEER_RPC;

/**
 * RPC 客户端 TLS 配置工厂（单例）：从 Properties 分别构建 SDK 与集群
 * 通信所需的 {@link RpcClientTlsConfig}，键前缀见 {@link RpcConstants}。
 * TlsConfigFactory.
 *
 * @author stone-98
 */
public class RpcClientTlsConfigFactory implements RpcTlsConfigFactory {
    
    /** 单例实例 */
    private static RpcClientTlsConfigFactory instance;
    
    private RpcClientTlsConfigFactory() {
    }
    
    /** 获取 TLS 配置工厂单例 */
    public static synchronized RpcClientTlsConfigFactory getInstance() {
        if (instance == null) {
            instance = new RpcClientTlsConfigFactory();
        }
        return instance;
    }
    
    /**
     * 从 Properties 构建 SDK 客户端 TLS 配置（前缀 {@link RpcConstants#NACOS_CLIENT_RPC}）。
     * Create SDK client TLS config.
     *
     * @param properties Properties containing TLS configuration
     * @return RpcClientTlsConfig object representing the TLS configuration
     */
    @Override
    public RpcClientTlsConfig createSdkConfig(Properties properties) {
        RpcClientTlsConfig tlsConfig = new RpcClientTlsConfig();
        tlsConfig
            .setEnableTls(getBooleanProperty(properties, NACOS_CLIENT_RPC + TLS_ENABLE, false));
        tlsConfig.setMutualAuthEnable(
            getBooleanProperty(properties, NACOS_CLIENT_RPC + MUTUAL_AUTH, false));
        tlsConfig.setProtocols(properties.getProperty(NACOS_CLIENT_RPC + TLS_PROTOCOLS));
        tlsConfig.setCiphers(properties.getProperty(NACOS_CLIENT_RPC + TLS_CIPHERS));
        tlsConfig.setTrustCollectionCertFile(
            properties.getProperty(NACOS_CLIENT_RPC + TLS_TRUST_COLLECTION_CHAIN_PATH));
        tlsConfig.setCertChainFile(properties.getProperty(NACOS_CLIENT_RPC + TLS_CERT_CHAIN_PATH));
        tlsConfig.setCertPrivateKey(properties.getProperty(NACOS_CLIENT_RPC + TLS_CERT_KEY));
        tlsConfig
            .setTrustAll(getBooleanProperty(properties, NACOS_CLIENT_RPC + TLS_TRUST_ALL, true));
        tlsConfig
            .setCertPrivateKeyPassword(properties.getProperty(NACOS_CLIENT_RPC + TLS_TRUST_PWD));
        tlsConfig.setSslProvider(properties.getProperty(NACOS_CLIENT_RPC + TLS_PROVIDER));
        return tlsConfig;
    }
    
    /**
     * 从 Properties 构建集群节点间 TLS 配置（前缀 {@link RpcConstants#NACOS_PEER_RPC}）。
     * Create cluster client TLS config.
     *
     * @param properties Properties containing TLS configuration
     * @return RpcClientTlsConfig object representing the TLS configuration
     */
    @Override
    public RpcClientTlsConfig createClusterConfig(Properties properties) {
        RpcClientTlsConfig tlsConfig = new RpcClientTlsConfig();
        tlsConfig.setEnableTls(getBooleanProperty(properties,
            NACOS_PEER_RPC + RpcConstants.ServerSuffix.TLS_ENABLE, false));
        tlsConfig.setMutualAuthEnable(getBooleanProperty(properties,
            NACOS_PEER_RPC + RpcConstants.ServerSuffix.MUTUAL_AUTH, false));
        tlsConfig.setProtocols(
            properties.getProperty(NACOS_PEER_RPC + RpcConstants.ServerSuffix.TLS_PROTOCOLS));
        tlsConfig.setCiphers(
            properties.getProperty(NACOS_PEER_RPC + RpcConstants.ServerSuffix.TLS_CIPHERS));
        tlsConfig.setTrustCollectionCertFile(properties.getProperty(
            NACOS_PEER_RPC + RpcConstants.ServerSuffix.TLS_TRUST_COLLECTION_CHAIN_PATH));
        tlsConfig.setCertChainFile(
            properties.getProperty(NACOS_PEER_RPC + RpcConstants.ServerSuffix.TLS_CERT_CHAIN_PATH));
        tlsConfig.setCertPrivateKey(
            properties.getProperty(NACOS_PEER_RPC + RpcConstants.ServerSuffix.TLS_CERT_KEY));
        tlsConfig.setTrustAll(getBooleanProperty(properties,
            NACOS_PEER_RPC + RpcConstants.ServerSuffix.TLS_TRUST_ALL, true));
        tlsConfig.setCertPrivateKeyPassword(
            properties.getProperty(NACOS_PEER_RPC + RpcConstants.ServerSuffix.TLS_TRUST_PWD));
        tlsConfig.setSslProvider(
            properties.getProperty(NACOS_PEER_RPC + RpcConstants.ServerSuffix.TLS_PROVIDER));
        return tlsConfig;
    }
    
}
