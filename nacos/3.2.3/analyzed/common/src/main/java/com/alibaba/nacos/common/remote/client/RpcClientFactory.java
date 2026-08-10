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

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.remote.ConnectionType;
import com.alibaba.nacos.common.remote.client.grpc.GrpcClientConfig;
import com.alibaba.nacos.common.remote.client.grpc.GrpcClusterClient;
import com.alibaba.nacos.common.remote.client.grpc.GrpcSdkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC 客户端工厂：按 clientName 缓存并创建 SDK / 集群 gRPC 客户端，
 * 支持多模块各自持有独立 {@link RpcClient} 实例。
 * RpcClientFactory.to support multi client for different modules of usage.
 *
 * @author liuzunfei
 * @version $Id: RpcClientFactory.java, v 0.1 2020年07月14日 3:41 PM liuzunfei Exp $
 */
public class RpcClientFactory {
    
    private static final Logger LOGGER =
        LoggerFactory.getLogger("com.alibaba.nacos.common.remote.client");
    
    /** clientName → RpcClient 全局缓存，computeIfAbsent 保证同名单例 */
    private static final Map<String, RpcClient> CLIENT_MAP = new ConcurrentHashMap<>();
    
    /**
     * get all client.
     *
     * @return client collection.
      * <p>RPC 客户端工厂；详见类级说明。</p>
     */
    public static Set<Map.Entry<String, RpcClient>> getAllClientEntries() {
        return CLIENT_MAP.entrySet();
    }
    
    /**
     * shut down client.
     *
     * @param clientName client name.
      * <p>RPC 客户端工厂；详见类级说明。</p>
     */
    public static void destroyClient(String clientName) throws NacosException {
        RpcClient rpcClient = CLIENT_MAP.remove(clientName);
        if (rpcClient != null) {
            rpcClient.shutdown();
        }
    }
    
    /** 按名称获取已创建的客户端，不存在返回 null */
    public static RpcClient getClient(String clientName) {
        return CLIENT_MAP.get(clientName);
    }
    
    /**
     * create a rpc client.
     *
     * @param clientName     client name.
     * @param connectionType client type.
     * @return rpc client.
      * <p>RPC 客户端工厂；详见类级说明。</p>
     */
    public static RpcClient createClient(String clientName, ConnectionType connectionType,
        Map<String, String> labels) {
        return createClient(clientName, connectionType, null, null, labels);
    }
    
    public static RpcClient createClient(String clientName, ConnectionType connectionType,
        Map<String, String> labels,
        RpcClientTlsConfig tlsConfig) {
        return createClient(clientName, connectionType, null, null, labels, tlsConfig);
    }
    
    /**
     * create client with properties.
     *
     * @return rpc client.
     * @date 2024/3/7
      * <p>RPC 客户端工厂；详见类级说明。</p>
     */
    public static RpcClient createClient(String clientName, ConnectionType connectionType,
        Map<String, String> labels,
        Properties properties, RpcClientTlsConfig tlsConfig) {
        return createClient(clientName, connectionType, null, null, labels, tlsConfig);
    }
    
    public static RpcClient createClient(String clientName, ConnectionType connectionType,
        Integer threadPoolCoreSize,
        Integer threadPoolMaxSize, Map<String, String> labels) {
        return createClient(clientName, connectionType, threadPoolCoreSize, threadPoolMaxSize,
            labels, null);
    }
    
    /**
     * create a rpc client.
     *
     * @param clientName         client name.
     * @param connectionType     client type.
     * @param threadPoolCoreSize grpc thread pool core size
     * @param threadPoolMaxSize  grpc thread pool max size
     * @param tlsConfig          tlsconfig
     * @return rpc client.
      * <p>RPC 客户端工厂；详见类级说明。</p>
     */
    public static RpcClient createClient(String clientName, ConnectionType connectionType,
        Integer threadPoolCoreSize,
        Integer threadPoolMaxSize, Map<String, String> labels, RpcClientTlsConfig tlsConfig) {
        
        // 当前仅支持 gRPC 连接类型
        if (!ConnectionType.GRPC.equals(connectionType)) {
            throw new UnsupportedOperationException(
                "unsupported connection type :" + connectionType.getType());
        }
        
        return CLIENT_MAP.computeIfAbsent(clientName, clientNameInner -> {
            LOGGER.info("[RpcClientFactory] create a new rpc client of " + clientName);
            return new GrpcSdkClient(clientNameInner, threadPoolCoreSize, threadPoolMaxSize, labels,
                tlsConfig);
        });
    }
    
    /**
     * create a rpc client.
     *
     * @param clientName         client name.
     * @param connectionType     client type.
     * @param grpcClientConfig   grpc client config.
     * @return rpc client.
      * <p>RPC 客户端工厂；详见类级说明。</p>
     */
    public static RpcClient createClient(String clientName, ConnectionType connectionType,
        GrpcClientConfig grpcClientConfig) {
        
        if (!ConnectionType.GRPC.equals(connectionType)) {
            throw new UnsupportedOperationException(
                "unsupported connection type :" + connectionType.getType());
        }
        
        return CLIENT_MAP.computeIfAbsent(clientName, clientNameInner -> {
            LOGGER.info("[RpcClientFactory] create a new rpc client of " + clientName);
            grpcClientConfig.setName(clientNameInner);
            return new GrpcSdkClient(grpcClientConfig);
        });
    }
    
    /**
     * Creates an RPC client for cluster communication with default thread pool settings.
     *
     * @param clientName     The name of the client.
     * @param connectionType The type of client connection.
     * @param labels         Additional labels for RPC-related attributes.
     * @return An RPC client for cluster communication.
      * <p>RPC 客户端工厂；详见类级说明。</p>
     */
    public static RpcClient createClusterClient(String clientName, ConnectionType connectionType,
        Map<String, String> labels) {
        return createClusterClient(clientName, connectionType, null, null, labels);
    }
    
    /**
     * Creates an RPC client for cluster communication with TLS configuration.
     *
     * @param clientName     The name of the client.
     * @param connectionType The type of client connection.
     * @param labels         Additional labels for RPC-related attributes.
     * @param tlsConfig      TLS configuration for secure communication.
     * @return An RPC client for cluster communication with TLS configuration.
      * <p>RPC 客户端工厂；详见类级说明。</p>
     */
    public static RpcClient createClusterClient(String clientName, ConnectionType connectionType,
        Map<String, String> labels, RpcClientTlsConfig tlsConfig) {
        return createClusterClient(clientName, connectionType, null, null, labels, tlsConfig);
    }
    
    /**
     * Creates an RPC client for cluster communication with custom thread pool settings.
     *
     * @param clientName         The name of the client.
     * @param connectionType     The type of client connection.
     * @param threadPoolCoreSize The core size of the gRPC thread pool.
     * @param threadPoolMaxSize  The maximum size of the gRPC thread pool.
     * @param labels             Additional labels for RPC-related attributes.
     * @return An RPC client for cluster communication with custom thread pool settings.
      * <p>RPC 客户端工厂；详见类级说明。</p>
     */
    public static RpcClient createClusterClient(String clientName, ConnectionType connectionType,
        Integer threadPoolCoreSize, Integer threadPoolMaxSize, Map<String, String> labels) {
        return createClusterClient(clientName, connectionType, threadPoolCoreSize,
            threadPoolMaxSize, labels, null);
    }
    
    /**
     * createClusterClient.
     *
     * @param clientName         client name.
     * @param connectionType     connectionType.
     * @param threadPoolCoreSize coreSize.
     * @param threadPoolMaxSize  threadPoolSize.
     * @param labels             tables.
     * @param tlsConfig          tlsConfig.
     * @return
      * <p>RPC 客户端工厂；详见类级说明。</p>
     */
    public static RpcClient createClusterClient(String clientName, ConnectionType connectionType,
        Integer threadPoolCoreSize, Integer threadPoolMaxSize, Map<String, String> labels,
        RpcClientTlsConfig tlsConfig) {
        if (!ConnectionType.GRPC.equals(connectionType)) {
            throw new UnsupportedOperationException(
                "unsupported connection type :" + connectionType.getType());
        }
        
        // 集群通信客户端，用于节点间 RPC
        return CLIENT_MAP.computeIfAbsent(clientName,
            clientNameInner -> new GrpcClusterClient(clientNameInner, threadPoolCoreSize,
                threadPoolMaxSize, labels,
                tlsConfig));
    }
    
    /**
     * create a cluster rpc client.
     *
     * @param clientName         client name.
     * @param connectionType     client type.
     * @param grpcClientConfig   grpc client config.
     * @return rpc client.
      * <p>RPC 客户端工厂；详见类级说明。</p>
     */
    public static RpcClient createClusterClient(String clientName, ConnectionType connectionType,
        GrpcClientConfig grpcClientConfig) {
        if (!ConnectionType.GRPC.equals(connectionType)) {
            throw new UnsupportedOperationException(
                "unsupported connection type :" + connectionType.getType());
        }
        
        return CLIENT_MAP.computeIfAbsent(clientName, clientNameInner -> {
            LOGGER.info("[RpcClientFactory] create a new cluster rpc client of " + clientName);
            grpcClientConfig.setName(clientNameInner);
            return new GrpcClusterClient(grpcClientConfig);
        });
    }
}
