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

package com.alibaba.nacos.core.remote.grpc.filter;

import io.grpc.ServerTransportFilter;

/**
 * Nacos gRPC 服务端传输层过滤器抽象基类，区分 SDK 与 CLUSTER 两种通道类型。
 * Nacos grpc server transport filter.
 *
 * @author xiweng.yy
 */
public abstract class NacosGrpcServerTransportFilter extends ServerTransportFilter {
    
    /** SDK 客户端通道类型标识。 */
    public static final String SDK_FILTER = "SDK";
    
    /** 集群节点间通道类型标识。 */
    public static final String CLUSTER_FILTER = "CLUSTER";
    
    /**
     * 返回过滤器所属通道类型。
     * Get the type of Interceptor.
     *
     * @return should be `CLUSTER` or `SDK`
     */
    public abstract String type();
}
