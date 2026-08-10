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

package com.alibaba.nacos.core.remote.grpc.negotiator;

import com.alibaba.nacos.common.utils.Pair;
import com.alibaba.nacos.core.remote.grpc.negotiator.tls.ClusterDefaultTlsProtocolNegotiatorBuilder;

/**
 * 集群间 RPC 协议协商器 Builder 单例：默认 TLS 协商，可通过系统属性切换实现。
 * Manages ProtocolNegotiatorBuilders for cluster communication. Provides a singleton instance of
 * ProtocolNegotiatorBuilder configured for this purpose. Defaults to TLS protocol negotiation but can be overridden via
 * system properties.
 *
 *
 * <p>Property key for configuring the ProtocolNegotiator type for cluster communication.
 *
 * @author stone-98
 * @date 2024/2/21
 */
public class ClusterProtocolNegotiatorBuilderSingleton
    extends AbstractProtocolNegotiatorBuilderSingleton {
    
    /** 集群 RPC 协商器类型的配置属性键。 */

    public static final String TYPE_PROPERTY_KEY =
        "nacos.remote.cluster.server.rpc.protocol.negotiator.type";
    
    /** 集群协商器 Builder 单例实例。 */

    private static final ClusterProtocolNegotiatorBuilderSingleton SINGLETON =
        new ClusterProtocolNegotiatorBuilderSingleton();
    
    /** 私有构造：绑定集群协商器类型属性键。 */

    public ClusterProtocolNegotiatorBuilderSingleton() {
        super(TYPE_PROPERTY_KEY);
    }
    
    /**
     * 获取集群协商器 Builder 单例。
     * Retrieves the singleton instance of ClusterProtocolNegotiatorBuilderSingleton.
     *
     * @return the singleton instance
     */
    public static AbstractProtocolNegotiatorBuilderSingleton getSingleton() {
        return SINGLETON;
    }
    
    /**
     * 默认使用 {@link ClusterDefaultTlsProtocolNegotiatorBuilder}。
     * Provides the default ProtocolNegotiatorBuilder pair.
     *
     * @return a Pair containing the default type and builder instance
     */
    @Override
    protected Pair<String, ProtocolNegotiatorBuilder> defaultBuilderPair() {
        return Pair.with(TYPE_PROPERTY_KEY, new ClusterDefaultTlsProtocolNegotiatorBuilder());
    }
    
    /**
     * 返回当前集群协商器类型标识。
     * Retrieves the type of ProtocolNegotiatorBuilder configured for cluster communication.
     *
     * @return the type of ProtocolNegotiatorBuilder
     */
    @Override
    public String type() {
        return super.actualType;
    }
}
