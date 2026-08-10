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

package com.alibaba.nacos.core.remote.grpc.interceptor;

import io.grpc.ServerInterceptor;

/**
 * Nacos gRPC 服务端拦截器 SPI 接口，扩展 {@link ServerInterceptor} 并声明 SDK/CLUSTER 类型。
 * Nacos grpc server interceptor.
 *
 * @author xiweng.yy
 */
public interface NacosGrpcServerInterceptor extends ServerInterceptor {
    
    /** SDK 通道拦截器类型常量。 */
    String SDK_INTERCEPTOR = "SDK";
    
    /** 集群通道拦截器类型常量。 */
    String CLUSTER_INTERCEPTOR = "CLUSTER";
    
    /**
     * 返回拦截器所属通道类型。
     * Get the type of Interceptor.
     *
     * @return should be `CLUSTER` or `SDK`
     */
    String type();
}
