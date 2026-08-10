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

package com.alibaba.nacos.core.remote.grpc.negotiator;

/**
 * 协议协商器构建 SPI：按 type() 标识创建 {@link NacosGrpcProtocolNegotiator} 实例。
 * Protocol negotiator builder.
 *
 * @author xiweng.yy
 */
public interface ProtocolNegotiatorBuilder {
    
    /**
     * 构建新的协议协商器实例（TLS 未启用时可能为 null）。
     * Build new ProtocolNegotiator.
     *
     * @return ProtocolNegotiator, Nullable.
     */
    NacosGrpcProtocolNegotiator build();
    
    /**
     * 返回 Builder 类型标识，供 SPI 注册与属性选择。
     * Builder type of ProtocolNegotiator.
     *
     * @return type
     */
    String type();
}
