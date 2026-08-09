/*
 * Copyright 2020  Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.taobao.arthas.grpcweb.proxy;

import io.grpc.Channel;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * 管理与后端 gRPC 服务的连接。
 *
 * <p>当前实现为单 {@link ManagedChannel} 直连 localhost，尚未实现连接池。
 * 代理层通过本类获取 Channel，再挂载 {@link GrpcWebClientInterceptor} 拦截响应。</p>
 */
public class GrpcServiceConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());
    /** 与本地 gRPC 服务通信的托管通道 */
    private final ManagedChannel channel;

    /**
     * 按指定端口建立到 localhost 的明文 gRPC 连接。
     *
     * @param grpcPortNum 后端 gRPC 服务监听端口
     */
    public GrpcServiceConnectionManager(int grpcPortNum) {
        // TODO: 后续可改为连接池管理多条 Channel
        channel = ManagedChannelBuilder.forAddress("localhost", grpcPortNum).usePlaintext().build();
        logger.info("**** connection channel initiated");
    }

    /**
     * 返回挂载了客户端拦截器的 Channel，用于将 gRPC 响应回写为 gRPC-Web HTTP 流。
     *
     * @param interceptor 负责把 Metadata/Trailer 转为 HTTP chunk 的拦截器
     * @return 包装后的 {@link Channel}
     */
    Channel getChannelWithClientInterceptor(GrpcWebClientInterceptor interceptor) {
        return ClientInterceptors.intercept(channel, interceptor);
    }

    /** 返回底层托管通道，供异常时 {@code shutdownNow()} 等生命周期操作使用。 */
    public ManagedChannel getChannel() {
        return channel;
    }
}
