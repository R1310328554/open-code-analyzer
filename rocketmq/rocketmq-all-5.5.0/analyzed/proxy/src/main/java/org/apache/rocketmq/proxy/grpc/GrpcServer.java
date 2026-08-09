/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.proxy.grpc;

import com.google.common.annotations.VisibleForTesting;
import io.grpc.Server;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.utils.StartAndShutdown;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.proxy.service.cert.TlsCertificateManager;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

/**
 * gRPC 服务端生命周期管理：封装 Netty gRPC Server 的启动、优雅关闭与 TLS 证书热重载。
 */
public class GrpcServer implements StartAndShutdown {
    private static final Logger log = LoggerFactory.getLogger(LoggerName.PROXY_LOGGER_NAME);

    /** 底层 gRPC Server 实例。 */
    private final Server server;

    /** 关闭等待超时数值。 */
    private final long timeout;

    /** 关闭等待超时时间单位。 */
    private final TimeUnit unit;

    /** TLS 证书管理器，负责注册/注销重载监听器。 */
    private final TlsCertificateManager tlsCertificateManager;
    /** TLS 上下文重载回调处理器（测试可见）。 */
    @VisibleForTesting final GrpcTlsReloadHandler tlsReloadHandler;

    protected GrpcServer(Server server, long timeout, TimeUnit unit,
        TlsCertificateManager tlsCertificateManager) throws Exception {
        this.server = server;
        this.timeout = timeout;
        this.unit = unit;
        this.tlsCertificateManager = tlsCertificateManager;
        this.tlsReloadHandler = new GrpcTlsReloadHandler();
    }

    /** 注册 TLS 重载监听并启动 gRPC 服务。 */
    public void start() throws Exception {
        // 注册 TLS 上下文重载监听器
        tlsCertificateManager.registerReloadListener(this.tlsReloadHandler);

        this.server.start();
        log.info("grpc server start successfully.");
    }

    /** 注销 TLS 监听并优雅关闭 gRPC 服务。 */
    public void shutdown() {
        try {
            // 注销 TLS 上下文重载监听器
            tlsCertificateManager.unregisterReloadListener(this.tlsReloadHandler);

            this.server.shutdown().awaitTermination(timeout, unit);

            log.info("grpc server shutdown successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed to shutdown grpc server", e);
        }
    }

    @VisibleForTesting
    /** 证书更新时重新加载 gRPC SslContext。 */
    class GrpcTlsReloadHandler implements TlsCertificateManager.TlsContextReloadListener {
        @Override
        public void onTlsContextReload() {
            try {
                ProxyAndTlsProtocolNegotiator.loadSslContext();
                log.info("SslContext reloaded for grpc server");
            } catch (CertificateException | IOException e) {
                log.error("Failed to reload SslContext for server", e);
            }
        }
    }
}
