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

import com.alibaba.nacos.api.ability.constant.AbilityMode;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.grpc.auto.BiRequestStreamGrpc;
import com.alibaba.nacos.api.grpc.auto.Payload;
import com.alibaba.nacos.api.grpc.auto.RequestGrpc;
import com.alibaba.nacos.api.remote.request.ConnectResetRequest;
import com.alibaba.nacos.api.remote.request.ConnectionSetupRequest;
import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.api.remote.request.ServerCheckRequest;
import com.alibaba.nacos.api.remote.request.SetupAckRequest;
import com.alibaba.nacos.api.remote.response.ErrorResponse;
import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.api.remote.response.ServerCheckResponse;
import com.alibaba.nacos.api.remote.response.SetupAckResponse;
import com.alibaba.nacos.common.ability.discover.NacosAbilityManagerHolder;
import com.alibaba.nacos.common.packagescan.resource.Resource;
import com.alibaba.nacos.common.remote.ConnectionType;
import com.alibaba.nacos.common.remote.TlsConfig;
import com.alibaba.nacos.common.remote.client.Connection;
import com.alibaba.nacos.common.remote.client.RpcClient;
import com.alibaba.nacos.common.remote.client.RpcClientStatus;
import com.alibaba.nacos.common.remote.client.RpcClientTlsConfig;
import com.alibaba.nacos.common.remote.client.ServerListFactory;
import com.alibaba.nacos.common.remote.client.ServerRequestHandler;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.LoggerUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.common.utils.ThreadFactoryBuilder;
import com.alibaba.nacos.common.utils.TlsTypeResolve;
import com.alibaba.nacos.common.utils.VersionUtils;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 抽象 gRPC 远程客户端：继承 {@link RpcClient}，负责 Channel 创建、ServerCheck、双向流绑定、TLS 上下文构建及与服务端的能力协商（Setup/SetupAck）。
 * gRPC Client.
 *
 * @author liuzunfei
 * @version $Id: GrpcClient.java, v 0.1 2020年07月13日 9:16 PM liuzunfei Exp $
 */
public abstract class GrpcClient extends RpcClient {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcClient.class);
    
    private final GrpcClientConfig clientConfig;
    
    private ThreadPoolExecutor grpcExecutor;
    
    /** 能力协商上下文：阻塞等待 SetupAck 并写入服务端能力表 */
    private final RecAbilityContext recAbilityContext = new RecAbilityContext(null);
    
    /** 处理 SetupAck 请求，释放能力协商 CountDownLatch */
    private SetupRequestHandler setupRequestHandler;
    
    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.GRPC;
    }
    
    /** 以名称创建，内部使用 {@link DefaultGrpcClientConfig} 默认参数 */
    /**
     * constructor.
     *
     * @param name .
      * <p>抽象 gRPC 客户端；详见类级说明。</p>
     */
    public GrpcClient(String name) {
        this(DefaultGrpcClientConfig.newBuilder().setName(name).build());
    }
    
    /**
     * constructor.
     *
     * @param clientConfig .
      * <p>抽象 gRPC 客户端；详见类级说明。</p>
     */
    public GrpcClient(GrpcClientConfig clientConfig) {
        super(clientConfig);
        this.clientConfig = clientConfig;
        initSetupHandler();
    }
    
    /**
     * constructor.
     *
     * @param clientConfig      .
     * @param serverListFactory .
      * <p>抽象 gRPC 客户端；详见类级说明。</p>
     */
    public GrpcClient(GrpcClientConfig clientConfig, ServerListFactory serverListFactory) {
        super(clientConfig, serverListFactory);
        this.clientConfig = clientConfig;
        initSetupHandler();
    }
    
    /** 注册 {@link SetupRequestHandler} 处理连接建立后的 SetupAck */
    private void initSetupHandler() {
        // 注册 SetupAck 处理器以接收服务端能力表
        setupRequestHandler = new SetupRequestHandler(this.recAbilityContext);
    }
    
    /**
     * constructor.
     *
     * @param name               .
     * @param threadPoolCoreSize .
     * @param threadPoolMaxSize  .
     * @param labels             .
      * <p>抽象 gRPC 客户端；详见类级说明。</p>
     */
    public GrpcClient(String name, Integer threadPoolCoreSize, Integer threadPoolMaxSize,
        Map<String, String> labels) {
        this(DefaultGrpcClientConfig.newBuilder().setName(name)
            .setThreadPoolCoreSize(threadPoolCoreSize)
            .setThreadPoolMaxSize(threadPoolMaxSize).setLabels(labels).build());
    }
    
    public GrpcClient(String name, Integer threadPoolCoreSize, Integer threadPoolMaxSize,
        Map<String, String> labels,
        RpcClientTlsConfig tlsConfig) {
        this(DefaultGrpcClientConfig.newBuilder().setName(name)
            .setThreadPoolCoreSize(threadPoolCoreSize)
            .setTlsConfig(tlsConfig).setThreadPoolMaxSize(threadPoolMaxSize).setLabels(labels)
            .build());
    }
    
    protected ThreadPoolExecutor createGrpcExecutor(String serverIp) {
        // 线程名使用 String.format，IPv6 地址中的 % 需先转义避免格式化异常
        serverIp = serverIp.replaceAll("%", "-");
        ThreadPoolExecutor grpcExecutor = new ThreadPoolExecutor(clientConfig.threadPoolCoreSize(),
            clientConfig.threadPoolMaxSize(), clientConfig.threadPoolKeepAlive(),
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(clientConfig.threadPoolQueueSize()),
            new ThreadFactoryBuilder().daemon(true)
                .nameFormat("nacos-grpc-client-executor-" + serverIp + "-%d")
                .build());
        grpcExecutor.allowCoreThreadTimeOut(clientConfig.allowCoreThreadTimeOut());
        return grpcExecutor;
    }
    
    @Override
    public void shutdown() throws NacosException {
        super.shutdown();
        if (grpcExecutor != null) {
            LOGGER.info("Shutdown grpc executor " + grpcExecutor);
            grpcExecutor.shutdown();
        }
    }
    
    /**
     * 基于已建 Channel 创建 {@link RequestGrpc.RequestFutureStub}。
     * Create a stub using a channel.
     *
     * @param managedChannelTemp channel.
     * @return if server check success,return a non-null stub.
     */
    protected RequestGrpc.RequestFutureStub createNewChannelStub(
        ManagedChannel managedChannelTemp) {
        return RequestGrpc.newFutureStub(managedChannelTemp);
    }
    
    /** 按 IP/端口构建 ManagedChannel，配置压缩、保活、TLS 与线程池 */
    /**
     * create a new channel with specific server address.
     *
     * @param serverIp   serverIp.
     * @param serverPort serverPort.
     * @return if server check success,return a non-null channel.
      * <p>抽象 gRPC 客户端；详见类级说明。</p>
     */
    private ManagedChannel createNewManagedChannel(String serverIp, int serverPort) {
        LOGGER.info("grpc client connection server: {} ip, serverPort: {}, grpcTslConfig: {}",
            serverIp, serverPort,
            JacksonUtils.toJson(clientConfig.tlsConfig()));
        ManagedChannelBuilder<?> managedChannelBuilder =
            buildChannel(serverIp, serverPort, buildSslContext()).executor(
                grpcExecutor).compressorRegistry(CompressorRegistry.getDefaultInstance())
                .decompressorRegistry(DecompressorRegistry.getDefaultInstance())
                .maxInboundMessageSize(clientConfig.maxInboundMessageSize())
                .keepAliveTime(clientConfig.channelKeepAlive(), TimeUnit.MILLISECONDS)
                .keepAliveTimeout(clientConfig.channelKeepAliveTimeout(), TimeUnit.MILLISECONDS);
        return managedChannelBuilder.build();
    }
    
    /** 立即 shutdown 指定 Channel（建连失败或探测失败时清理资源） */
    /**
     * shutdown a  channel.
     *
     * @param managedChannel channel to be shutdown.
      * <p>抽象 gRPC 客户端；详见类级说明。</p>
     */
    private void shuntDownChannel(ManagedChannel managedChannel) {
        if (managedChannel != null && !managedChannel.isShutdown()) {
            managedChannel.shutdownNow();
        }
    }
    
    /**
     * 发送 {@link ServerCheckRequest} 验证服务端可达；失败返回 null 并记录 TLS 提示。
     * check server if success.
     *
     * @param requestBlockingStub requestBlockingStub used to check server.
     * @return success or not
     */
    private Response serverCheck(String ip, int port,
        RequestGrpc.RequestFutureStub requestBlockingStub) {
        try {
            ServerCheckRequest serverCheckRequest = new ServerCheckRequest();
            Payload grpcRequest = GrpcUtils.convert(serverCheckRequest);
            ListenableFuture<Payload> responseFuture = requestBlockingStub.request(grpcRequest);
            Payload response =
                responseFuture.get(clientConfig.serverCheckTimeOut(), TimeUnit.MILLISECONDS);
            // 此处仅接收 ServerCheck 响应，不校验业务 success 标志
            return (Response) GrpcUtils.parse(response);
        } catch (Exception e) {
            LoggerUtils.printIfErrorEnabled(LOGGER,
                "Server check fail, please check server {}, port {} is available, error ={}", ip,
                port, e);
            if (this.clientConfig != null && this.clientConfig.tlsConfig() != null
                && this.clientConfig.tlsConfig()
                    .getEnableTls()) {
                LoggerUtils.printIfErrorEnabled(LOGGER,
                    "current client is require tls encrypted, server must support tls ,please check");
            }
            return null;
        }
    }
    
    private StreamObserver<Payload> bindRequestStream(
        final BiRequestStreamGrpc.BiRequestStreamStub streamStub,
        final GrpcConnection grpcConn) {
        return streamStub.requestBiStream(new StreamObserver<Payload>() {
            
            @Override
            public void onNext(Payload payload) {
                LoggerUtils.printIfDebugEnabled(LOGGER,
                    "[{}]Stream server request receive, original info: {}",
                    grpcConn.getConnectionId(), payload.toString());
                try {
                    Object parseBody = GrpcUtils.parse(payload);
                    final Request request = (Request) parseBody;
                    if (request != null) {
                        try {
                            if (request instanceof SetupAckRequest) {
                                // SetupAck 到达时连接可能尚未完全就绪，connection 可为 null
                                setupRequestHandler.requestReply(request, null);
                                return;
                            }
                            Response response = handleServerRequest(request);
                            if (response != null) {
                                response.setRequestId(request.getRequestId());
                                sendResponse(response);
                            } else {
                                LOGGER.warn("[{}]Fail to process server request, ackId->{}",
                                    grpcConn.getConnectionId(),
                                    request.getRequestId());
                            }
                        } catch (Exception e) {
                            LoggerUtils.printIfErrorEnabled(LOGGER,
                                "[{}]Handle server request exception: {}",
                                grpcConn.getConnectionId(), payload.toString(), e.getMessage());
                            Response errResponse = ErrorResponse.build(NacosException.CLIENT_ERROR,
                                "Handle server request error");
                            errResponse.setRequestId(request.getRequestId());
                            sendResponse(errResponse);
                        }
                    }
                } catch (Exception e) {
                    LoggerUtils.printIfErrorEnabled(LOGGER,
                        "[{}]Error to process server push response: {}",
                        grpcConn.getConnectionId(), payload.getBody().getValue().toStringUtf8());
                    // 解析失败时释放能力协商阻塞并通知
                    recAbilityContext.release(null);
                }
            }
            
            @Override
            public void onError(Throwable throwable) {
                boolean isRunning = isRunning();
                boolean isAbandon = grpcConn.isAbandon();
                if (isRunning && !isAbandon) {
                    LoggerUtils.printIfErrorEnabled(LOGGER,
                        "[{}]Request stream error, switch server,error={}",
                        grpcConn.getConnectionId(), throwable);
                    if (rpcClientStatus.compareAndSet(RpcClientStatus.RUNNING,
                        RpcClientStatus.UNHEALTHY)) {
                        switchServerAsync();
                    }
                } else {
                    LoggerUtils.printIfWarnEnabled(LOGGER,
                        "[{}]Ignore error event,isRunning:{},isAbandon={}",
                        grpcConn.getConnectionId(), isRunning, isAbandon);
                }
            }
            
            @Override
            public void onCompleted() {
                boolean isRunning = isRunning();
                boolean isAbandon = grpcConn.isAbandon();
                if (isRunning && !isAbandon) {
                    LoggerUtils.printIfErrorEnabled(LOGGER,
                        "[{}]Request stream onCompleted, switch server",
                        grpcConn.getConnectionId());
                    if (rpcClientStatus.compareAndSet(RpcClientStatus.RUNNING,
                        RpcClientStatus.UNHEALTHY)) {
                        switchServerAsync();
                    }
                } else {
                    LoggerUtils.printIfInfoEnabled(LOGGER,
                        "[{}]Ignore complete event,isRunning:{},isAbandon={}",
                        grpcConn.getConnectionId(), isRunning, isAbandon);
                }
            }
        });
    }
    
    private void sendResponse(Response response) {
        try {
            ((GrpcConnection) this.currentConnection).sendResponse(response);
        } catch (Exception e) {
            LOGGER.error("[{}]Error to send ack response, ackId->{}",
                this.currentConnection.getConnectionId(),
                response.getRequestId());
        }
    }
    
    @Override
    public Connection connectToServer(ServerInfo serverInfo) {
        // 记录 ServerCheck 返回的最新 connectionId
        String connectionId = "";
        try {
            if (grpcExecutor == null) {
                this.grpcExecutor = createGrpcExecutor(serverInfo.getServerIp());
            }
            int port = serverInfo.getServerPort() + rpcPortOffset();
            ManagedChannel managedChannel = createNewManagedChannel(serverInfo.getServerIp(), port);
            RequestGrpc.RequestFutureStub newChannelStubTemp = createNewChannelStub(managedChannel);
            
            Response response = serverCheck(serverInfo.getServerIp(), port, newChannelStubTemp);
            if (!(response instanceof ServerCheckResponse)) {
                shuntDownChannel(managedChannel);
                return null;
            }
            // 尽早提交本节点能力表
            // 旧版 Server 不支持能力表时为 null
            ServerCheckResponse serverCheckResponse = (ServerCheckResponse) response;
            connectionId = serverCheckResponse.getConnectionId();
            
            BiRequestStreamGrpc.BiRequestStreamStub biRequestStreamStub =
                BiRequestStreamGrpc.newStub(
                    newChannelStubTemp.getChannel());
            GrpcConnection grpcConn = new GrpcConnection(serverInfo, grpcExecutor);
            grpcConn.setConnectionId(connectionId);
            // 不支持能力协商时 supportAbilityNegotiation 为 false
            if (serverCheckResponse.isSupportAbilityNegotiation()) {
                // 标记需要同步等待 SetupAck
                this.recAbilityContext.reset(grpcConn);
                // 未收到能力表前 abilityTable 保持 null
                grpcConn.setAbilityTable(null);
            }
            
            // 建立双向流并将 onError/onCompleted 绑定到连接切换逻辑
            StreamObserver<Payload> payloadStreamObserver =
                bindRequestStream(biRequestStreamStub, grpcConn);
            
            // 用于经双向流向 Server 发送 Response/Request
            grpcConn.setPayloadStreamObserver(payloadStreamObserver);
            grpcConn.setGrpcFutureServiceStub(newChannelStubTemp);
            grpcConn.setChannel(managedChannel);
            // 发送 ConnectionSetupRequest（版本、标签、能力表、租户）
            ConnectionSetupRequest conSetupRequest = new ConnectionSetupRequest();
            conSetupRequest.setClientVersion(getClientVersion());
            conSetupRequest.setLabels(super.getLabels());
            // set ability table
            conSetupRequest.setAbilityTable(
                NacosAbilityManagerHolder.getInstance().getCurrentNodeAbilities(abilityMode()));
            conSetupRequest.setTenant(super.getTenant());
            grpcConn.sendRequest(conSetupRequest);
            // 等待 SetupAck 或兼容旧版的固定延迟
            if (recAbilityContext.isNeedToSync()) {
                // 阻塞等待能力协商超时
                recAbilityContext.await(this.clientConfig.capabilityNegotiationTimeout(),
                    TimeUnit.MILLISECONDS);
                // 超时未收到能力表则放弃本次连接
                if (!recAbilityContext.check(grpcConn)) {
                    return null;
                }
            } else {
                // 兼容不支持能力协商的旧 Server：固定 sleep 100ms
                // 旧协议下默认 100ms 后视为注册成功
                // 给服务端处理 Setup 请求的缓冲时间
                Thread.sleep(100L);
            }
            return grpcConn;
        } catch (Exception e) {
            LOGGER.error("[{}]Fail to connect to server!,error={}", GrpcClient.this.getName(), e);
            // 建连异常时释放能力协商 latch
            recAbilityContext.release(null);
        }
        return null;
    }
    
    protected String getClientVersion() {
        return VersionUtils.getFullClientVersion();
    }
    
    /** 子类声明能力上报模式：SDK 或 Cluster */
    /**
     * ability mode: sdk client or cluster client.
     *
     * @return mode
      * <p>抽象 gRPC 客户端；详见类级说明。</p>
     */
    protected abstract AbilityMode abilityMode();
    
    @Override
    protected void afterReset(ConnectResetRequest request) {
        recAbilityContext.release(null);
    }
    
    /** 能力协商同步上下文：CountDownLatch 阻塞客户端直至收到 SetupAck */
    /**
     * This is for receiving server abilities.
      * <p>抽象 gRPC 客户端；详见类级说明。</p>
     */
    static class RecAbilityContext {
        
        /** 当前等待写入能力表的连接 */
        private volatile Connection connection;
        
        /** 阻塞建连线程直至 release 或超时 */
        private volatile CountDownLatch blocker;
        
        private volatile boolean needToSync = false;
        
        public RecAbilityContext(Connection connection) {
            this.connection = connection;
            this.blocker = new CountDownLatch(1);
        }
        
        /** 本次建连是否需要等待能力表同步 */
        /**
         * whether to sync for ability table.
         *
         * @return whether to sync for ability table.
          * <p>抽象 gRPC 客户端；详见类级说明。</p>
         */
        public boolean isNeedToSync() {
            return this.needToSync;
        }
        
        /** 为新连接重置 latch 并开启同步等待 */
        /**
         * reset with new connection which is waiting for ability table.
         *
         * @param connection new connection which is waiting for ability table.
          * <p>抽象 gRPC 客户端；详见类级说明。</p>
         */
        public void reset(Connection connection) {
            this.connection = connection;
            this.blocker = new CountDownLatch(1);
            this.needToSync = true;
        }
        
        /** 收到 SetupAck 后写入能力表并 countDown */
        /**
         * notify sync by abilities.
         *
         * @param abilities abilities.
          * <p>抽象 gRPC 客户端；详见类级说明。</p>
         */
        public void release(Map<String, Boolean> abilities) {
            if (this.connection != null) {
                this.connection.setAbilityTable(abilities);
                // 避免重复写入同一连接
                this.connection = null;
            }
            if (this.blocker != null) {
                blocker.countDown();
            }
            this.needToSync = false;
        }
        
        /** 在指定超时内等待 SetupAck */
        /**
         * await for abilities.
         *
         * @param timeout timeout.
         * @param unit    unit.
         * @throws InterruptedException by blocker.
          * <p>抽象 gRPC 客户端；详见类级说明。</p>
         */
        public void await(long timeout, TimeUnit unit) throws InterruptedException {
            if (this.blocker != null) {
                this.blocker.await(timeout, unit);
            }
            this.needToSync = false;
        }
        
        /** 校验是否已设置能力表；失败则标记 abandon 并关闭连接 */
        /**
         * check whether receive abilities.
         *
         * @param connection conn.
         * @return whether receive abilities.
          * <p>抽象 gRPC 客户端；详见类级说明。</p>
         */
        public boolean check(Connection connection) {
            if (!connection.isAbilitiesSet()) {
                LOGGER.error(
                    "Client don't receive server abilities table even empty table but server supports ability negotiation."
                        + " You can check if it is need to adjust the timeout of ability negotiation by property: {}"
                        + " if always fail to connect.",
                    GrpcConstants.GRPC_CHANNEL_CAPABILITY_NEGOTIATION_TIMEOUT);
                connection.setAbandon(true);
                connection.close();
                return false;
            }
            return true;
        }
    }
    
    private Optional<SslContext> buildSslContext() {
        
        TlsConfig tlsConfig = clientConfig.tlsConfig();
        if (!tlsConfig.getEnableTls()) {
            return Optional.empty();
        }
        try {
            SslContextBuilder builder = GrpcSslContexts.forClient();
            if (StringUtils.isNotBlank(tlsConfig.getSslProvider())) {
                builder.sslProvider(TlsTypeResolve.getSslProvider(tlsConfig.getSslProvider()));
            }
            
            if (StringUtils.isNotBlank(tlsConfig.getProtocols())) {
                builder.protocols(tlsConfig.getProtocols().split(","));
            }
            if (StringUtils.isNotBlank(tlsConfig.getCiphers())) {
                builder.ciphers(Arrays.asList(tlsConfig.getCiphers().split(",")));
            }
            if (tlsConfig.getTrustAll()) {
                builder.trustManager(InsecureTrustManagerFactory.INSTANCE);
            } else {
                if (StringUtils.isBlank(tlsConfig.getTrustCollectionCertFile())) {
                    throw new IllegalArgumentException("trustCollectionCertFile must be not null");
                }
                Resource resource =
                    resourceLoader.getResource(tlsConfig.getTrustCollectionCertFile());
                builder.trustManager(resource.getInputStream());
            }
            
            if (tlsConfig.getMutualAuthEnable()) {
                if (StringUtils.isBlank(tlsConfig.getCertChainFile()) || StringUtils.isBlank(
                    tlsConfig.getCertPrivateKey())) {
                    throw new IllegalArgumentException(
                        "client certChainFile or certPrivateKey must be not null");
                }
                Resource certChainFile = resourceLoader.getResource(tlsConfig.getCertChainFile());
                Resource privateKey = resourceLoader.getResource(tlsConfig.getCertPrivateKey());
                builder.keyManager(certChainFile.getInputStream(), privateKey.getInputStream(),
                    tlsConfig.getCertPrivateKeyPassword());
            }
            return Optional.of(builder.build());
        } catch (Exception e) {
            throw new RuntimeException("Unable to build SslContext", e);
        }
    }
    
    private ManagedChannelBuilder buildChannel(String serverIp, int port,
        Optional<SslContext> sslContext) {
        if (sslContext.isPresent()) {
            return NettyChannelBuilder.forAddress(serverIp, port)
                .negotiationType(NegotiationType.TLS)
                .sslContext(sslContext.get());
            
        } else {
            return ManagedChannelBuilder.forAddress(serverIp, port).usePlaintext();
        }
    }
    
    /** 处理服务端下发的 {@link SetupAckRequest}，完成能力协商 */
    /**
     * Setup response handler.
      * <p>抽象 gRPC 客户端；详见类级说明。</p>
     */
    class SetupRequestHandler implements ServerRequestHandler {
        
        private final RecAbilityContext abilityContext;
        
        public SetupRequestHandler(RecAbilityContext abilityContext) {
            this.abilityContext = abilityContext;
        }
        
        @Override
        public Response requestReply(Request request, Connection connection) {
            // SetupAck 表示连接注册完成
            if (request instanceof SetupAckRequest) {
                SetupAckRequest setupAckRequest = (SetupAckRequest) request;
                // 写入能力表并释放阻塞
                recAbilityContext.release(
                    Optional.ofNullable(setupAckRequest.getAbilityTable())
                        .orElse(new HashMap<>(0)));
                return new SetupAckResponse();
            }
            return null;
        }
    }
}
