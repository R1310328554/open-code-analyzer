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

import io.grpc.netty.shaded.io.grpc.netty.InternalProtocolNegotiator;

/**
 * Nacos gRPC 协议协商器扩展接口：在 Netty {@link InternalProtocolNegotiator.ProtocolNegotiator}
 * 基础上增加热重载能力。
 * Nacos Grpc protocol negotiator.
 *
 * @author xiweng.yy
 */
public interface NacosGrpcProtocolNegotiator extends InternalProtocolNegotiator.ProtocolNegotiator {
    
    /**
     * 热重载协商器（如 TLS 配置/证书变更后刷新 SslContext）。
     * Reload this negotiator, such as config, tls context and so on if necessary.
     */
    void reloadNegotiator();
}
