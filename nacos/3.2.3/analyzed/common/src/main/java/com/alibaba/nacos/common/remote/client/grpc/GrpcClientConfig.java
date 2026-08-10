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

package com.alibaba.nacos.common.remote.client.grpc;

import com.alibaba.nacos.common.remote.TlsConfig;
import com.alibaba.nacos.common.remote.client.RpcClientConfig;
import com.alibaba.nacos.common.remote.client.RpcClientTlsConfig;

/**
 * gRPC 客户端配置接口：在 {@link RpcClientConfig} 基础上扩展线程池、消息大小、Channel 保活及 TLS 等 gRPC 特有参数。
 * GrpcClient config. Use to collect and init Grpc client configuration.
 *
 * @author karsonto
 */
public interface GrpcClientConfig extends RpcClientConfig {
    
    /** 获取 gRPC 回调线程池核心线程数 */
    /**
     * get threadPoolCoreSize.
     *
     * @return threadPoolCoreSize.
      * <p>gRPC 客户端配置接口；详见类级说明。</p>
     */
    int threadPoolCoreSize();
    
    /** 获取 gRPC 回调线程池最大线程数 */
    /**
     * get threadPoolMaxSize.
     *
     * @return threadPoolMaxSize.
      * <p>gRPC 客户端配置接口；详见类级说明。</p>
     */
    int threadPoolMaxSize();
    
    /** 获取空闲线程存活时间（毫秒） */
    /**
     * get thread pool keep alive time.
     *
     * @return threadPoolKeepAlive.
      * <p>gRPC 客户端配置接口；详见类级说明。</p>
     */
    long threadPoolKeepAlive();
    
    /** 获取建连前 ServerCheck 探测超时（毫秒） */
    /**
     * get server check time out.
     *
     * @return serverCheckTimeOut.
      * <p>gRPC 客户端配置接口；详见类级说明。</p>
     */
    long serverCheckTimeOut();
    
    /** 获取 gRPC 线程池任务队列容量 */
    /**
     * get thread pool queue size.
     *
     * @return threadPoolQueueSize.
      * <p>gRPC 客户端配置接口；详见类级说明。</p>
     */
    int threadPoolQueueSize();
    
    /** 获取 gRPC 入站消息最大字节数 */
    /**
     * get maxInboundMessage size.
     *
     * @return maxInboundMessageSize.
      * <p>gRPC 客户端配置接口；详见类级说明。</p>
     */
    int maxInboundMessageSize();
    
    /** 获取 gRPC Channel HTTP/2 保活间隔（毫秒） */
    /**
     * get channelKeepAlive time.
     *
     * @return channelKeepAlive.
      * <p>gRPC 客户端配置接口；详见类级说明。</p>
     */
    int channelKeepAlive();
    
    /** 获取 Channel 保活 ACK 超时（毫秒） */
    /**
     * get channelKeepAliveTimeout.
     *
     * @return channelKeepAliveTimeout.
      * <p>gRPC 客户端配置接口；详见类级说明。</p>
     */
    long channelKeepAliveTimeout();
    
    /** 获取 TLS/SSL 配置 */
    /**
     * getTlsConfig.
     *
     * @return TlsConfig.
      * <p>gRPC 客户端配置接口；详见类级说明。</p>
     */
    TlsConfig tlsConfig();
    
    /** 设置客户端 TLS 配置 */
    /**
     * Set TlsConfig.
     *
     * @param tlsConfig tlsConfig of client.
      * <p>gRPC 客户端配置接口；详见类级说明。</p>
     */
    void setTlsConfig(RpcClientTlsConfig tlsConfig);
    
    /** 设置客户端名称（日志与监控标识） */
    /**
     * Set name of client.
     *
     * @param name name of client.
      * <p>gRPC 客户端配置接口；详见类级说明。</p>
     */
    void setName(String name);
    
    /** 获取能力协商（SetupAck）等待超时，单位毫秒 */
    /**
     * get timeout of connection setup(TimeUnit.MILLISECONDS).
     *
     * @return timeout of connection setup
      * <p>gRPC 客户端配置接口；详见类级说明。</p>
     */
    long capabilityNegotiationTimeout();
    
    /** 是否允许核心线程在空闲时被回收 */
    /**
     * get allowCoreThreadTimeOut flag for thread pool.
     *
     * @return allowCoreThreadTimeOut flag
      * <p>gRPC 客户端配置接口；详见类级说明。</p>
     */
    boolean allowCoreThreadTimeOut();
}
