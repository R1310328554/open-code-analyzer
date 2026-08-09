/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.cluster.server.envoy.rls;

import java.io.IOException;

import com.alibaba.csp.sentinel.log.RecordLog;

import io.grpc.Server;
import io.grpc.ServerBuilder;

/**
 * Sentinel Envoy RLS gRPC 服务端封装，同时注册 v2 与 v3 RateLimitService 实现。
 *
 * @author Eric Zhao
 */
public class SentinelRlsGrpcServer {

    private final Server server;

    public SentinelRlsGrpcServer(int port) {
        ServerBuilder<?> builder = ServerBuilder.forPort(port)
            .addService(new com.alibaba.csp.sentinel.cluster.server.envoy.rls.service.v3.SentinelEnvoyRlsServiceImpl())
            .addService(new SentinelEnvoyRlsServiceImpl());

        server = builder.build();
    }

    /** 启动 gRPC 服务端并输出监听端口日志。 */
    public void start() throws IOException {
        // gRPC Server 内部已处理启动状态，此处不再重复检查。
        server.start();
        String message = "[SentinelRlsGrpcServer] RLS server is running at port " + server.getPort();
        RecordLog.info(message);
        System.out.println(message);
    }

    /** 立即关闭 gRPC 服务端。 */
    public void shutdown() {
        server.shutdownNow();
    }

    public boolean isShutdown() {
        return server.isShutdown();
    }

    /** 阻塞等待 gRPC 服务端终止。 */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
