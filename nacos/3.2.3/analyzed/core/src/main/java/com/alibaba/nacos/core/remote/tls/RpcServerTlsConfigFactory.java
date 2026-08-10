/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.remote.tls;

import com.alibaba.nacos.common.remote.client.RpcTlsConfigFactory;
import com.alibaba.nacos.common.remote.client.RpcConstants;

import java.util.Properties;

import static com.alibaba.nacos.common.remote.client.RpcConstants.NACOS_SERVER_RPC;
import static com.alibaba.nacos.common.remote.client.RpcConstants.ServerSuffix.COMPATIBILITY;
import static com.alibaba.nacos.common.remote.client.RpcConstants.ServerSuffix.MUTUAL_AUTH;
import static com.alibaba.nacos.common.remote.client.RpcConstants.ServerSuffix.SSL_CONTEXT_REFRESHER;
import static com.alibaba.nacos.common.remote.client.RpcConstants.ServerSuffix.TLS_CERT_CHAIN_PATH;
import static com.alibaba.nacos.common.remote.client.RpcConstants.ServerSuffix.TLS_CERT_KEY;
import static com.alibaba.nacos.common.remote.client.RpcConstants.ServerSuffix.TLS_CIPHERS;
import static com.alibaba.nacos.common.remote.client.RpcConstants.ServerSuffix.TLS_ENABLE;
import static com.alibaba.nacos.common.remote.client.RpcConstants.ServerSuffix.TLS_PROTOCOLS;
import static com.alibaba.nacos.common.remote.client.RpcConstants.ServerSuffix.TLS_PROVIDER;
import static com.alibaba.nacos.common.remote.client.RpcConstants.ServerSuffix.TLS_TRUST_ALL;
import static com.alibaba.nacos.common.remote.client.RpcConstants.ServerSuffix.TLS_TRUST_COLLECTION_CHAIN_PATH;
import static com.alibaba.nacos.common.remote.client.RpcConstants.ServerSuffix.TLS_TRUST_PWD;

/**
 * RPC 服务端 TLS 配置工厂，从 {@link java.util.Properties} 解析 SDK 与集群通道的 TLS 参数。
 *
 * <p>实现 {@link com.alibaba.nacos.common.remote.client.RpcTlsConfigFactory}，按配置前缀（{@code nacos.server.rpc} / {@code nacos.peer.rpc}）组装 {@link RpcServerTlsConfig}。</p>
 *
 * @author stone-98
 * @date 2024/4/8
 */
public class RpcServerTlsConfigFactory implements RpcTlsConfigFactory {
    
    /** 单例实例。 */
    private static RpcServerTlsConfigFactory instance;
    
    /** 私有构造，禁止外部实例化。 */
    private RpcServerTlsConfigFactory() {
    }
    
    /** 获取工厂单例（懒加载）。 */
    public static synchronized RpcServerTlsConfigFactory getInstance() {
        if (instance == null) {
            instance = new RpcServerTlsConfigFactory();
        }
        return instance;
    }
    
    /**
     * 创建 SDK 客户端连接对应的 RPC 服务端 TLS 配置。
     *
     * @param properties 含 TLS 键值对的配置属性
     * @return SDK 通道 {@link RpcServerTlsConfig}
     */
    @Override
    public RpcServerTlsConfig createSdkConfig(Properties properties) {
        return createServerTlsConfig(properties, NACOS_SERVER_RPC);
    }
    
    /**
     * 创建集群节点间 RPC 的 TLS 配置。
     *
     * @param properties 含 TLS 键值对的配置属性
     * @return 集群通道 {@link RpcServerTlsConfig}
     */
    @Override
    public RpcServerTlsConfig createClusterConfig(Properties properties) {
        return createServerTlsConfig(properties, RpcConstants.NACOS_PEER_RPC);
    }
    
    /**
     * 按指定前缀从属性中解析并填充 {@link RpcServerTlsConfig}。
     *
     * @param properties 配置属性源
     * @param prefix     配置键前缀（如 {@code nacos.server.rpc}）
     * @return 解析后的 TLS 配置对象
     */
    public RpcServerTlsConfig createServerTlsConfig(Properties properties, String prefix) {
        RpcServerTlsConfig tlsConfig = new RpcServerTlsConfig();
        tlsConfig.setEnableTls(getBooleanProperty(properties, prefix + TLS_ENABLE, false));
        tlsConfig.setMutualAuthEnable(getBooleanProperty(properties, prefix + MUTUAL_AUTH, false));
        tlsConfig.setProtocols(properties.getProperty(prefix + TLS_PROTOCOLS));
        tlsConfig.setCiphers(properties.getProperty(prefix + TLS_CIPHERS));
        tlsConfig.setTrustCollectionCertFile(
            properties.getProperty(prefix + TLS_TRUST_COLLECTION_CHAIN_PATH));
        tlsConfig.setCertChainFile(properties.getProperty(prefix + TLS_CERT_CHAIN_PATH));
        tlsConfig.setCertPrivateKey(properties.getProperty(prefix + TLS_CERT_KEY));
        tlsConfig.setTrustAll(getBooleanProperty(properties, prefix + TLS_TRUST_ALL, true));
        tlsConfig.setCertPrivateKeyPassword(properties.getProperty(prefix + TLS_TRUST_PWD));
        tlsConfig.setSslProvider(properties.getProperty(prefix + TLS_PROVIDER));
        tlsConfig.setSslContextRefresher(properties.getProperty(prefix + SSL_CONTEXT_REFRESHER));
        tlsConfig.setCompatibility(getBooleanProperty(properties, prefix + COMPATIBILITY, true));
        return tlsConfig;
    }
}
