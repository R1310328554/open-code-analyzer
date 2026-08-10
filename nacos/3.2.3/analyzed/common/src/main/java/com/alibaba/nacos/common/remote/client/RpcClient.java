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

package com.alibaba.nacos.common.remote.client;

import com.alibaba.nacos.api.ability.constant.AbilityKey;
import com.alibaba.nacos.api.ability.constant.AbilityStatus;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.remote.RequestCallBack;
import com.alibaba.nacos.api.remote.RequestFuture;
import com.alibaba.nacos.api.remote.request.ClientDetectionRequest;
import com.alibaba.nacos.api.remote.request.ConnectResetRequest;
import com.alibaba.nacos.api.remote.request.HealthCheckRequest;
import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.api.remote.response.ClientDetectionResponse;
import com.alibaba.nacos.api.remote.response.ConnectResetResponse;
import com.alibaba.nacos.api.remote.response.ErrorResponse;
import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.lifecycle.Closeable;
import com.alibaba.nacos.common.packagescan.resource.DefaultResourceLoader;
import com.alibaba.nacos.common.packagescan.resource.ResourceLoader;
import com.alibaba.nacos.common.remote.ConnectionType;
import com.alibaba.nacos.common.remote.PayloadRegistry;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.InternetAddressUtil;
import com.alibaba.nacos.common.utils.LoggerUtils;
import com.alibaba.nacos.common.utils.NumberUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.alibaba.nacos.api.exception.NacosException.SERVER_ERROR;

/**
 * 抽象 RPC 远程客户端：管理服务器列表、连接生命周期、重连与健康检查，
 * 提供同步/异步请求、服务端推送处理及连接事件通知；gRPC 等子类实现具体传输。
 * abstract remote client to connect to server.
 *
 * @author liuzunfei
 * @version $Id: RpcClient.java, v 0.1 2020年07月13日 9:15 PM liuzunfei Exp $
 */
public abstract class RpcClient implements Closeable {
    
    private static final Logger LOGGER =
        LoggerFactory.getLogger("com.alibaba.nacos.common.remote.client");
    
    /** 服务器地址列表提供者，负责轮询/切换目标节点 */
    private ServerListFactory serverListFactory;
    
    /** 连接事件队列，由专用线程消费并通知监听器 */
    protected BlockingQueue<ConnectionEvent> eventLinkedBlockingQueue = new LinkedBlockingQueue<>();
    
    /** 客户端当前生命周期状态（原子引用，支持 CAS 流转） */
    protected volatile AtomicReference<RpcClientStatus> rpcClientStatus = new AtomicReference<>(
        RpcClientStatus.WAIT_INIT);
    
    /** 处理连接事件与健康检查/重连的后台调度线程池 */
    protected ScheduledExecutorService clientEventExecutor;
    
    /** 重连信号队列，容量 1 避免重复堆积切换请求 */
    private final BlockingQueue<ReconnectContext> reconnectionSignal = new ArrayBlockingQueue<>(1);
    
    /** 当前活跃的服务端连接 */
    protected volatile Connection currentConnection;
    
    /** 租户标识，随连接上下文传递 */
    private String tenant;
    
    /** 上次成功 RPC 或推送的时间戳，用于保活检测 */
    private long lastActiveTimeStamp = System.currentTimeMillis();
    
    /** 已注册的连接状态变更监听器列表 */

    protected List<ConnectionEventListener> connectionEventListeners = new ArrayList<>();
    
    /** 服务端主动推送请求的处理器链 */

    protected List<ServerRequestHandler> serverRequestHandlers = new ArrayList<>();
    
    /** 从 serverAddress 中剥离协议前缀（如 http://）的正则 */
    private static final Pattern EXCLUDE_PROTOCOL_PATTERN = Pattern.compile("(?<=\\w{1,5}://)(.*)");
    
    /** 客户端运行时配置（超时、重试、保活等） */
    protected RpcClientConfig rpcClientConfig;
    
    /** 资源加载器，供子类读取证书等文件 */
    protected final ResourceLoader resourceLoader = new DefaultResourceLoader();
    
    /** 类加载时初始化 Payload 类型注册表 */
    static {
        PayloadRegistry.init();
    }
    
    public RpcClient(RpcClientConfig rpcClientConfig) {
        this(rpcClientConfig, null);
    }
    
    public RpcClient(RpcClientConfig rpcClientConfig, ServerListFactory serverListFactory) {
        this.rpcClientConfig = rpcClientConfig;
        this.serverListFactory = serverListFactory;
        init();
    }
    
    /** 若构造时已注入 ServerListFactory，则将状态推进为 INITIALIZED */
    protected void init() {
        if (this.serverListFactory != null) {
            rpcClientStatus.compareAndSet(RpcClientStatus.WAIT_INIT, RpcClientStatus.INITIALIZED);
            LoggerUtils.printIfInfoEnabled(LOGGER,
                "RpcClient init in constructor, ServerListFactory = {}",
                serverListFactory.getClass().getName());
        }
    }
    
    /**
     * 注入服务器列表工厂（仅 WAIT_INIT 状态下生效，且只能初始化一次）。
     * init server list factory. only can init once.
     *
     * @param serverListFactory serverListFactory
     */
    public RpcClient serverListFactory(ServerListFactory serverListFactory) {
        if (!isWaitInitiated()) {
            return this;
        }
        this.serverListFactory = serverListFactory;
        rpcClientStatus.compareAndSet(RpcClientStatus.WAIT_INIT, RpcClientStatus.INITIALIZED);
        
        LoggerUtils.printIfInfoEnabled(LOGGER, "[{}] RpcClient init, ServerListFactory = {}",
            rpcClientConfig.name(),
            serverListFactory.getClass().getName());
        return this;
    }
    
    /**
     * 向所有监听器广播连接断开事件。
     * Notify when client disconnected.
     *
     * @param connection connection has disconnected
     */
    protected void notifyDisConnected(Connection connection) {
        if (connectionEventListeners.isEmpty()) {
            return;
        }
        LoggerUtils.printIfInfoEnabled(LOGGER, "[{}] Notify disconnected event to listeners",
            rpcClientConfig.name());
        for (ConnectionEventListener connectionEventListener : connectionEventListeners) {
            try {
                connectionEventListener.onDisConnect(connection);
            } catch (Throwable throwable) {
                LoggerUtils.printIfErrorEnabled(LOGGER,
                    "[{}] Notify disconnect listener error, listener = {}",
                    rpcClientConfig.name(), connectionEventListener.getClass().getName());
            }
        }
    }
    
    /**
     * 向所有监听器广播新连接建立事件。
     * Notify when client new connected.
     *
     * @param connection connection has connected
     */
    protected void notifyConnected(Connection connection) {
        if (connectionEventListeners.isEmpty()) {
            return;
        }
        LoggerUtils.printIfInfoEnabled(LOGGER, "[{}] Notify connected event to listeners.",
            rpcClientConfig.name());
        for (ConnectionEventListener connectionEventListener : connectionEventListeners) {
            try {
                connectionEventListener.onConnected(connection);
            } catch (Throwable throwable) {
                LoggerUtils.printIfErrorEnabled(LOGGER,
                    "[{}] Notify connect listener error, listener = {}",
                    rpcClientConfig.name(), connectionEventListener.getClass().getName());
            }
        }
    }
    
    /** 是否仍处于等待 ServerListFactory 的 WAIT_INIT 状态 */

    public boolean isWaitInitiated() {
        return this.rpcClientStatus.get() == RpcClientStatus.WAIT_INIT;
    }
    
    /** 是否处于 RUNNING 状态且可正常收发请求 */

    public boolean isRunning() {
        return this.rpcClientStatus.get() == RpcClientStatus.RUNNING;
    }
    
    /** 是否已关闭 */

    public boolean isShutdown() {
        return this.rpcClientStatus.get() == RpcClientStatus.SHUTDOWN;
    }
    
    /** 校验当前连接节点是否仍在服务器列表中，不在则异步切换 */

    public void onServerListChange() {
        if (currentConnection != null && currentConnection.serverInfo != null) {
            ServerInfo serverInfo = currentConnection.serverInfo;
            boolean found = false;
            for (String serverAddress : serverListFactory.getServerList()) {
                if (resolveServerInfo(serverAddress).getAddress()
                    .equalsIgnoreCase(serverInfo.getAddress())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                LoggerUtils.printIfInfoEnabled(LOGGER,
                    "Current connected server {} is not in latest server list, switch switchServerAsync",
                    serverInfo.getAddress());
                switchServerAsync();
            }
            
        }
    }
    
    /**
     * 启动客户端：创建事件线程、尝试同步连接、注册推送处理器；
     * 失败则进入异步重连循环。
     * Start this client.
     */
    public final void start() throws NacosException {
        
        boolean success =
            rpcClientStatus.compareAndSet(RpcClientStatus.INITIALIZED, RpcClientStatus.STARTING);
        if (!success) {
            return;
        }
        
        clientEventExecutor = new ScheduledThreadPoolExecutor(2,
            new NameThreadFactory("com.alibaba.nacos.client.remote.worker"));
        
        // 连接事件消费线程：从队列取 CONNECTED/DISCONNECTED 并回调监听器
        clientEventExecutor.submit(() -> {
            while (!clientEventExecutor.isTerminated() && !clientEventExecutor.isShutdown()) {
                ConnectionEvent take;
                try {
                    take = eventLinkedBlockingQueue.take();
                    if (take.isConnected()) {
                        notifyConnected(take.connection);
                    } else if (take.isDisConnected()) {
                        notifyDisConnected(take.connection);
                    }
                } catch (Throwable e) {
                    // Do nothing
                }
            }
        });
        
        clientEventExecutor.submit(() -> {
            while (true) {
                try {
                    if (isShutdown()) {
                        break;
                    }
                    ReconnectContext reconnectContext = reconnectionSignal
                        .poll(rpcClientConfig.connectionKeepAlive(), TimeUnit.MILLISECONDS);
                    if (reconnectContext == null) {
                        // 保活超时：发起健康检查，失败则标记 UNHEALTHY 并重连
                        if (System.currentTimeMillis() - lastActiveTimeStamp >= rpcClientConfig
                            .connectionKeepAlive()) {
                            boolean isHealthy = healthCheck();
                            if (!isHealthy) {
                                if (currentConnection == null) {
                                    continue;
                                }
                                LoggerUtils.printIfInfoEnabled(LOGGER,
                                    "[{}] Server healthy check fail, currentConnection = {}",
                                    rpcClientConfig.name(), currentConnection.getConnectionId());
                                
                                RpcClientStatus rpcClientStatus =
                                    RpcClient.this.rpcClientStatus.get();
                                if (RpcClientStatus.SHUTDOWN.equals(rpcClientStatus)) {
                                    break;
                                }
                                
                                boolean statusFlowSuccess = RpcClient.this.rpcClientStatus
                                    .compareAndSet(rpcClientStatus, RpcClientStatus.UNHEALTHY);
                                if (statusFlowSuccess) {
                                    reconnectContext = new ReconnectContext(null, false);
                                } else {
                                    continue;
                                }
                                
                            } else {
                                lastActiveTimeStamp = System.currentTimeMillis();
                                continue;
                            }
                        } else {
                            continue;
                        }
                        
                    }
                    
                    if (reconnectContext.serverInfo != null) {
                        // 推荐节点不在列表中时丢弃推荐，改由工厂轮询选取
                        boolean serverExist = false;
                        for (String server : getServerListFactory().getServerList()) {
                            ServerInfo serverInfo = resolveServerInfo(server);
                            if (serverInfo.getServerIp()
                                .equals(reconnectContext.serverInfo.getServerIp())) {
                                serverExist = true;
                                reconnectContext.serverInfo.serverPort = serverInfo.serverPort;
                                break;
                            }
                        }
                        if (!serverExist) {
                            LoggerUtils.printIfInfoEnabled(LOGGER,
                                "[{}] Recommend server is not in server list, ignore recommend server {}",
                                rpcClientConfig.name(), reconnectContext.serverInfo.getAddress());
                            
                            reconnectContext.serverInfo = null;
                            
                        }
                    }
                    reconnect(reconnectContext.serverInfo, reconnectContext.onRequestFail);
                } catch (Throwable throwable) {
                    // Do nothing
                }
            }
        });
        
        // 启动阶段同步重试连接；全部失败则异步 switchServer
        Connection connectToServer = null;
        rpcClientStatus.set(RpcClientStatus.STARTING);
        
        int startUpRetryTimes = rpcClientConfig.retryTimes();
        while (startUpRetryTimes >= 0 && connectToServer == null) {
            try {
                startUpRetryTimes--;
                ServerInfo serverInfo = nextRpcServer();
                
                LoggerUtils.printIfInfoEnabled(LOGGER,
                    "[{}] Try to connect to server on start up, server: {}",
                    rpcClientConfig.name(), serverInfo);
                
                connectToServer = connectToServer(serverInfo);
            } catch (Throwable e) {
                LoggerUtils.printIfWarnEnabled(LOGGER,
                    "[{}] Fail to connect to server on start up, error message = {}, start up retry times left: {}",
                    rpcClientConfig.name(), e.getMessage(), startUpRetryTimes, e);
            }
            
        }
        
        if (connectToServer != null) {
            LoggerUtils
                .printIfInfoEnabled(LOGGER,
                    "[{}] Success to connect to server [{}] on start up, connectionId = {}",
                    rpcClientConfig.name(), connectToServer.serverInfo.getAddress(),
                    connectToServer.getConnectionId());
            this.currentConnection = connectToServer;
            rpcClientStatus.set(RpcClientStatus.RUNNING);
            eventLinkedBlockingQueue
                .offer(new ConnectionEvent(ConnectionEvent.CONNECTED, currentConnection));
        } else {
            switchServerAsync();
        }
        
        registerServerRequestHandler(new ConnectResetRequestHandler());
        
        // 注册客户端探测请求处理器，响应 ClientDetectionRequest
        registerServerRequestHandler((request, connection) -> {
            if (request instanceof ClientDetectionRequest) {
                return new ClientDetectionResponse();
            }
            
            return null;
        });
        
    }
    
    /** 处理服务端下发的 ConnectReset 指令，触发切节点 */
    class ConnectResetRequestHandler implements ServerRequestHandler {
        
        @Override
        public Response requestReply(Request request, Connection connection) {
            
            if (request instanceof ConnectResetRequest) {
                
                try {
                    synchronized (RpcClient.this) {
                        if (isRunning()) {
                            ConnectResetRequest connectResetRequest = (ConnectResetRequest) request;
                            if (StringUtils.isNotBlank(connectResetRequest.getServerIp())) {
                                ServerInfo serverInfo = resolveServerInfo(
                                    connectResetRequest.getServerIp() + Constants.COLON
                                        + connectResetRequest
                                            .getServerPort());
                                switchServerAsync(serverInfo, false);
                            } else {
                                switchServerAsync();
                            }
                            afterReset(connectResetRequest);
                        }
                    }
                } catch (Exception e) {
                    LoggerUtils.printIfErrorEnabled(LOGGER, "[{}] Switch server error, {}",
                        rpcClientConfig.name(), e);
                }
                return new ConnectResetResponse();
            }
            return null;
        }
    }
    
    /**
     * 收到 ConnectReset 后的钩子，GrpcClient 可覆写做额外清理。
     * . invoke after receiving reset request
     *
     * @param request request for resetting
     */
    protected void afterReset(ConnectResetRequest request) {
        // hook for GrpcClient
    }
    
    @Override
    public void shutdown() throws NacosException {
        LOGGER.info("Shutdown rpc client, set status to shutdown");
        rpcClientStatus.set(RpcClientStatus.SHUTDOWN);
        LOGGER.info("Shutdown client event executor " + clientEventExecutor);
        if (clientEventExecutor != null) {
            clientEventExecutor.shutdownNow();
        }
        closeConnection(currentConnection);
    }
    
    /** 向当前连接发送 HealthCheckRequest，验证连接仍注册且服务端正常 */
    private boolean healthCheck() {
        HealthCheckRequest healthCheckRequest = new HealthCheckRequest();
        if (this.currentConnection == null) {
            return false;
        }
        int reTryTimes = rpcClientConfig.healthCheckRetryTimes();
        while (reTryTimes >= 0) {
            reTryTimes--;
            try {
                if (reTryTimes > 1) {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(500));
                }
                Response response = this.currentConnection
                    .request(healthCheckRequest, rpcClientConfig.healthCheckTimeOut());
                // 不仅校验服务端响应，还确认连接仍处于注册状态
                return response != null && response.isSuccess();
            } catch (Exception e) {
                // ignore
            }
        }
        return false;
    }
    
    public void switchServerAsyncOnRequestFail() {
        switchServerAsync(null, true);
    }
    
    public void switchServerAsync() {
        switchServerAsync(null, false);
    }
    
    protected void switchServerAsync(final ServerInfo recommendServerInfo, boolean onRequestFail) {
        reconnectionSignal.offer(new ReconnectContext(recommendServerInfo, onRequestFail));
    }
    
    /**
     * 重连核心逻辑：可选推荐节点，循环尝试 connectToServer，
     * 成功后废弃旧连接并发布 CONNECTED 事件。
     * switch server .
     */
    protected void reconnect(final ServerInfo recommendServerInfo, boolean onRequestFail) {
        
        try {
            
            AtomicReference<ServerInfo> recommendServer =
                new AtomicReference<>(recommendServerInfo);
            if (onRequestFail && healthCheck()) {
                LoggerUtils.printIfInfoEnabled(LOGGER,
                    "[{}] Server check success, currentServer is {} ",
                    rpcClientConfig.name(), currentConnection.serverInfo.getAddress());
                rpcClientStatus.set(RpcClientStatus.RUNNING);
                return;
            }
            
            LoggerUtils.printIfInfoEnabled(LOGGER,
                "[{}] Try to reconnect to a new server, server is {}",
                rpcClientConfig.name(),
                recommendServerInfo == null ? " not appointed, will choose a random server."
                    : (recommendServerInfo.getAddress() + ", will try it once."));
            
            // loop until start client success.
            boolean switchSuccess = false;
            
            int reConnectTimes = 0;
            int retryTurns = 0;
            Exception lastException;
            while (!switchSuccess && !isShutdown()) {
                
                // 1. 选取目标服务器（推荐或轮询）
                ServerInfo serverInfo = null;
                try {
                    serverInfo =
                        recommendServer.get() == null ? nextRpcServer() : recommendServer.get();
                    // 2. 建立到新服务器的传输连接
                    Connection connectionNew = connectToServer(serverInfo);
                    if (connectionNew != null) {
                        LoggerUtils
                            .printIfInfoEnabled(LOGGER,
                                "[{}] Success to connect a server [{}], connectionId = {}",
                                rpcClientConfig.name(), serverInfo.getAddress(),
                                connectionNew.getConnectionId());
                        // successfully create a new connect.
                        if (currentConnection != null) {
                            LoggerUtils.printIfInfoEnabled(LOGGER,
                                "[{}] Abandon prev connection, server is {}, connectionId is {}",
                                rpcClientConfig.name(), currentConnection.serverInfo.getAddress(),
                                currentConnection.getConnectionId());
                            // 标记旧连接 abandon，关闭后不再触发其断开事件
                            currentConnection.setAbandon(true);
                            closeConnection(currentConnection);
                        }
                        currentConnection = connectionNew;
                        rpcClientStatus.set(RpcClientStatus.RUNNING);
                        switchSuccess = true;
                        eventLinkedBlockingQueue
                            .add(new ConnectionEvent(ConnectionEvent.CONNECTED, currentConnection));
                        return;
                    }
                    
                    // 客户端已关闭时清理半成品连接
                    if (isShutdown()) {
                        closeConnection(currentConnection);
                    }
                    
                    lastException = null;
                    
                } catch (Throwable throwable) {
                    LoggerUtils.printIfErrorEnabled(LOGGER, "Fail to connect server, error = {}",
                        throwable.getMessage());
                    lastException = new Exception(throwable);
                } finally {
                    recommendServer.set(null);
                }
                
                if (CollectionUtils.isEmpty(RpcClient.this.serverListFactory.getServerList())) {
                    throw new Exception("server list is empty");
                }
                
                if (reConnectTimes > 0
                    && reConnectTimes
                        % RpcClient.this.serverListFactory.getServerList().size() == 0) {
                    LoggerUtils.printIfInfoEnabled(LOGGER,
                        "[{}] Fail to connect server, after trying {} times, last try server is {}, error = {}",
                        rpcClientConfig.name(), reConnectTimes, serverInfo,
                        lastException == null ? "unknown" : lastException);
                    if (Integer.MAX_VALUE == retryTurns) {
                        retryTurns = 50;
                    } else {
                        retryTurns++;
                    }
                }
                
                reConnectTimes++;
                
                try {
                    // 非 RUNNING 时递增退避，避免频繁打满服务器列表
                    if (!isRunning()) {
                        // first round, try servers at a delay 100ms;second round, 200ms; max delays 5s. to be reconsidered.
                        Thread.sleep(Math.min(retryTurns + 1, 50) * 100L);
                    }
                } catch (InterruptedException e) {
                    // Do nothing.
                    // set the interrupted flag
                    Thread.currentThread().interrupt();
                }
            }
            
            if (isShutdown()) {
                LoggerUtils.printIfInfoEnabled(LOGGER,
                    "[{}] Client is shutdown, stop reconnect to server",
                    rpcClientConfig.name());
            }
            
        } catch (Exception e) {
            LoggerUtils
                .printIfWarnEnabled(LOGGER, "[{}] Fail to reconnect to server, error is {}",
                    rpcClientConfig.name(),
                    e);
        }
    }
    
    /** 关闭连接并将 DISCONNECTED 事件入队 */
    private void closeConnection(Connection connection) {
        if (connection != null) {
            LOGGER.info("Close current connection " + connection.getConnectionId());
            connection.close();
            eventLinkedBlockingQueue
                .add(new ConnectionEvent(ConnectionEvent.DISCONNECTED, connection));
        }
    }
    
    /** 返回底层传输类型（如 GRPC） */

    public abstract ConnectionType getConnectionType();
    
    /** Nacos 主端口到 RPC 端口的偏移量，由子类定义 */

    public abstract int rpcPortOffset();
    
    /** 当前连接对应的服务器 IP:Port，无连接时返回 null */

    public ServerInfo getCurrentServer() {
        if (this.currentConnection != null) {
            return currentConnection.serverInfo;
        }
        return null;
    }
    
    /** 使用配置默认超时发送同步请求 */
    public Response request(Request request) throws NacosException {
        return request(request, rpcClientConfig.timeOutMills());
    }
    
    /** 带指定超时的同步请求，含重试与 UN_REGISTER 时自动切节点 */
    public Response request(Request request, long timeoutMills) throws NacosException {
        int retryTimes = 0;
        Response response;
        Throwable exceptionThrow = null;
        long start = System.currentTimeMillis();
        while (retryTimes <= rpcClientConfig.retryTimes() && (timeoutMills <= 0
            || System.currentTimeMillis() < timeoutMills + start)) {
            boolean waitReconnect = false;
            try {
                if (this.currentConnection == null || !isRunning()) {
                    waitReconnect = true;
                    throw new NacosException(NacosException.CLIENT_DISCONNECT,
                        "Client not connected, current status:" + rpcClientStatus.get());
                }
                response = this.currentConnection.request(request, timeoutMills);
                if (response == null) {
                    throw new NacosException(SERVER_ERROR, "Unknown Exception.");
                }
                if (response instanceof ErrorResponse) {
                    if (response.getErrorCode() == NacosException.UN_REGISTER) {
                        synchronized (this) {
                            waitReconnect = true;
                            if (rpcClientStatus.compareAndSet(RpcClientStatus.RUNNING,
                                RpcClientStatus.UNHEALTHY)) {
                                LoggerUtils.printIfErrorEnabled(LOGGER,
                                    "Connection is unregistered, switch server, connectionId = {}, request = {}",
                                    currentConnection.getConnectionId(),
                                    request.getClass().getSimpleName());
                                switchServerAsync();
                            }
                        }
                        
                    }
                    throw new NacosException(response.getErrorCode(), response.getMessage());
                }
                // 成功响应，刷新活跃时间戳
                lastActiveTimeStamp = System.currentTimeMillis();
                return response;
                
            } catch (Throwable e) {
                if (waitReconnect) {
                    try {
                        // 断连时短暂等待重连后再重试
                        Thread.sleep(Math.min(100, timeoutMills / 3));
                    } catch (Exception exception) {
                        // Do nothing.
                    }
                }
                
                LoggerUtils.printIfErrorEnabled(LOGGER,
                    "Send request fail, request = {}, retryTimes = {}, errorMessage = {}", request,
                    retryTimes,
                    e.getMessage());
                
                exceptionThrow = e;
                
            }
            retryTimes++;
            
        }
        
        if (rpcClientStatus.compareAndSet(RpcClientStatus.RUNNING, RpcClientStatus.UNHEALTHY)) {
            switchServerAsyncOnRequestFail();
        }
        
        if (exceptionThrow != null) {
            throw (exceptionThrow instanceof NacosException) ? (NacosException) exceptionThrow
                : new NacosException(SERVER_ERROR, exceptionThrow);
        } else {
            throw new NacosException(SERVER_ERROR, "Request fail, unknown Error");
        }
    }
    
    /** 异步请求，失败时按配置重试并在断连时等待重连 */

    public void asyncRequest(Request request, RequestCallBack callback) throws NacosException {
        int retryTimes = 0;
        Throwable exceptionToThrow = null;
        long start = System.currentTimeMillis();
        while (retryTimes <= rpcClientConfig.retryTimes()
            && System.currentTimeMillis() < start + callback
                .getTimeout()) {
            boolean waitReconnect = false;
            try {
                if (this.currentConnection == null || !isRunning()) {
                    waitReconnect = true;
                    throw new NacosException(NacosException.CLIENT_DISCONNECT,
                        "Client not connected.");
                }
                this.currentConnection.asyncRequest(request, callback);
                return;
            } catch (Throwable e) {
                if (waitReconnect) {
                    try {
                        // wait client to reconnect.
                        Thread.sleep(Math.min(100, callback.getTimeout() / 3));
                    } catch (Exception exception) {
                        // Do nothing.
                    }
                }
                LoggerUtils.printIfErrorEnabled(LOGGER,
                    "[{}] Send request fail, request = {}, retryTimes = {}, errorMessage = {}",
                    rpcClientConfig.name(), request, retryTimes, e.getMessage());
                exceptionToThrow = e;
                
            }
            retryTimes++;
            
        }
        
        if (rpcClientStatus.compareAndSet(RpcClientStatus.RUNNING, RpcClientStatus.UNHEALTHY)) {
            switchServerAsyncOnRequestFail();
        }
        if (exceptionToThrow != null) {
            throw (exceptionToThrow instanceof NacosException) ? (NacosException) exceptionToThrow
                : new NacosException(SERVER_ERROR, exceptionToThrow);
        } else {
            throw new NacosException(SERVER_ERROR, "AsyncRequest fail, unknown error");
        }
    }
    
    /** 返回 RequestFuture 的异步请求，支持超时与重试 */

    public RequestFuture requestFuture(Request request) throws NacosException {
        int retryTimes = 0;
        long start = System.currentTimeMillis();
        Exception exceptionToThrow = null;
        while (retryTimes <= rpcClientConfig.retryTimes()
            && System.currentTimeMillis() < start + rpcClientConfig
                .timeOutMills()) {
            boolean waitReconnect = false;
            try {
                if (this.currentConnection == null || !isRunning()) {
                    waitReconnect = true;
                    throw new NacosException(NacosException.CLIENT_DISCONNECT,
                        "Client not connected.");
                }
                return this.currentConnection.requestFuture(request);
            } catch (Exception e) {
                if (waitReconnect) {
                    try {
                        // wait client to reconnect.
                        Thread.sleep(100L);
                    } catch (Exception exception) {
                        // Do nothing.
                    }
                }
                LoggerUtils.printIfErrorEnabled(LOGGER,
                    "[{}] Send request fail, request = {}, retryTimes = {}, errorMessage = {}",
                    rpcClientConfig.name(), request, retryTimes, e.getMessage());
                exceptionToThrow = e;
                
            }
            retryTimes++;
        }
        
        if (rpcClientStatus.compareAndSet(RpcClientStatus.RUNNING, RpcClientStatus.UNHEALTHY)) {
            switchServerAsyncOnRequestFail();
        }
        
        if (exceptionToThrow != null) {
            throw (exceptionToThrow instanceof NacosException) ? (NacosException) exceptionToThrow
                : new NacosException(SERVER_ERROR, exceptionToThrow);
        } else {
            throw new NacosException(SERVER_ERROR, "Request future fail, unknown error");
        }
        
    }
    
    /**
     * 子类实现：与指定 ServerInfo 建立传输连接并返回 Connection。
     * connect to server.
     *
     * @param serverInfo server address to connect.
     * @return return connection when successfully connect to server, or null if failed.
     * @throws Exception exception when fail to connect to server.
     */
    public abstract Connection connectToServer(ServerInfo serverInfo) throws Exception;
    
    /**
     * 处理服务端推送：依次调用已注册 Handler，返回首个非 null 响应。
     * handle server request.
     *
     * @param request request.
     * @return response.
     */
    protected Response handleServerRequest(final Request request) {
        
        LoggerUtils.printIfInfoEnabled(LOGGER,
            "[{}] Receive server push request, request = {}, requestId = {}",
            rpcClientConfig.name(), request.getClass().getSimpleName(), request.getRequestId());
        lastActiveTimeStamp = System.currentTimeMillis();
        for (ServerRequestHandler serverRequestHandler : serverRequestHandlers) {
            try {
                Response response = serverRequestHandler.requestReply(request, currentConnection);
                
                if (response != null) {
                    LoggerUtils.printIfInfoEnabled(LOGGER,
                        "[{}] Ack server push request, request = {}, requestId = {}",
                        rpcClientConfig.name(), request.getClass().getSimpleName(),
                        request.getRequestId());
                    return response;
                }
            } catch (Exception e) {
                LoggerUtils.printIfInfoEnabled(LOGGER,
                    "[{}] HandleServerRequest:{}, errorMessage = {}",
                    rpcClientConfig.name(), serverRequestHandler.getClass().getName(),
                    e.getMessage());
                throw e;
            }
            
        }
        return null;
    }
    
    /**
     * 注册连接事件监听器，连接建立/断开时回调。
     * Register connection handler. Will be notified when inner connection's state changed.
     *
     * @param connectionEventListener connectionEventListener
     */
    public synchronized void registerConnectionListener(
        ConnectionEventListener connectionEventListener) {
        
        LoggerUtils.printIfInfoEnabled(LOGGER,
            "[{}] Registry connection listener to current client:{}",
            rpcClientConfig.name(), connectionEventListener.getClass().getName());
        this.connectionEventListeners.add(connectionEventListener);
    }
    
    /**
     * 注册服务端推送请求处理器。
     * Register serverRequestHandler, the handler will handle the request from server side.
     *
     * @param serverRequestHandler serverRequestHandler
     */
    public synchronized void registerServerRequestHandler(
        ServerRequestHandler serverRequestHandler) {
        LoggerUtils.printIfInfoEnabled(LOGGER, "[{}] Register server push request handler:{}",
            rpcClientConfig.name(),
            serverRequestHandler.getClass().getName());
        
        this.serverRequestHandlers.add(serverRequestHandler);
    }
    
    /**
     * Getter method for property <tt>name</tt>.
     *
     * @return property value of name
      * <p>抽象 RPC 远程客户端；详见类级说明。</p>
     */
    public String getName() {
        return rpcClientConfig.name();
    }
    
    /**
     * Getter method for property <tt>serverListFactory</tt>.
     *
     * @return property value of serverListFactory
      * <p>抽象 RPC 远程客户端；详见类级说明。</p>
     */
    public ServerListFactory getServerListFactory() {
        return serverListFactory;
    }
    
    /** 从 ServerListFactory 取下一地址并解析为 ServerInfo */
    protected ServerInfo nextRpcServer() {
        String serverAddress = getServerListFactory().genNextServer();
        return resolveServerInfo(serverAddress);
    }
    
    /** 解析工厂当前指针指向的服务器地址 */
    protected ServerInfo currentRpcServer() {
        String serverAddress = getServerListFactory().getCurrentServer();
        return resolveServerInfo(serverAddress);
    }
    
    /**
     * 解析地址字符串为 ServerInfo：剥离协议、拆分 IP:Port、默认 8848。
     * resolve server info.
     *
     * @param serverAddress address.
     * @return
     */
    private ServerInfo resolveServerInfo(String serverAddress) {
        Matcher matcher = EXCLUDE_PROTOCOL_PATTERN.matcher(serverAddress);
        if (matcher.find()) {
            serverAddress = matcher.group(1);
        }
        String[] ipPortTuple = InternetAddressUtil.splitIpPortStr(serverAddress);
        int defaultPort = Integer.parseInt(System.getProperty("nacos.server.port", "8848"));
        String serverPort =
            CollectionUtils.getOrDefault(ipPortTuple, 1, Integer.toString(defaultPort));
        
        return new ServerInfo(ipPortTuple[0], NumberUtils.toInt(serverPort, defaultPort));
    }
    
    /** 目标服务器 IP 与主端口（RPC 端口由 rpcPortOffset 计算） */
    public static class ServerInfo {
        
        /** 服务器 IP 或主机名 */
        protected String serverIp;
        
        /** Nacos 主服务端口 */
        protected int serverPort;
        
        public ServerInfo() {
            
        }
        
        public ServerInfo(String serverIp, int serverPort) {
            this.serverPort = serverPort;
            this.serverIp = serverIp;
        }
        
        /** 返回 {@code ip:port} 格式地址 */

        public String getAddress() {
            return serverIp + Constants.COLON + serverPort;
        }
        
        /**
         * Setter method for property <tt>serverIp</tt>.
         *
         * @param serverIp value to be assigned to property serverIp
          * <p>抽象 RPC 远程客户端；详见类级说明。</p>
         */
        public void setServerIp(String serverIp) {
            this.serverIp = serverIp;
        }
        
        /**
         * Setter method for property <tt>serverPort</tt>.
         *
         * @param serverPort value to be assigned to property serverPort
          * <p>抽象 RPC 远程客户端；详见类级说明。</p>
         */
        public void setServerPort(int serverPort) {
            this.serverPort = serverPort;
        }
        
        /**
         * Getter method for property <tt>serverIp</tt>.
         *
         * @return property value of serverIp
          * <p>抽象 RPC 远程客户端；详见类级说明。</p>
         */
        public String getServerIp() {
            return serverIp;
        }
        
        /**
         * Getter method for property <tt>serverPort</tt>.
         *
         * @return property value of serverPort
          * <p>抽象 RPC 远程客户端；详见类级说明。</p>
         */
        public int getServerPort() {
            return serverPort;
        }
        
        @Override
        public String toString() {
            return "{serverIp = '" + serverIp + '\'' + ", server main port = " + serverPort + '}';
        }
    }
    
    /** 内部连接事件：CONNECTED(1) 或 DISCONNECTED(0) */
    public static class ConnectionEvent {
        
        /** 连接已建立 */
        public static final int CONNECTED = 1;
        
        /** 连接已断开 */
        public static final int DISCONNECTED = 0;
        
        int eventType;
        
        Connection connection;
        
        public ConnectionEvent(int eventType, Connection connection) {
            this.eventType = eventType;
            this.connection = connection;
        }
        
        public boolean isConnected() {
            return eventType == CONNECTED;
        }
        
        public boolean isDisConnected() {
            return eventType == DISCONNECTED;
        }
    }
    
    /**
     * Getter method for property <tt>labels</tt>.
     *
     * @return property value of labels
      * <p>抽象 RPC 远程客户端；详见类级说明。</p>
     */
    public Map<String, String> getLabels() {
        return rpcClientConfig.labels();
    }
    
    /** 重连上下文：可选推荐 ServerInfo 及是否因请求失败触发 */
    static class ReconnectContext {
        
        public ReconnectContext(ServerInfo serverInfo, boolean onRequestFail) {
            this.onRequestFail = onRequestFail;
            this.serverInfo = serverInfo;
        }
        
        /** 是否由请求失败触发的切换（会先做一次 healthCheck） */
        boolean onRequestFail;
        
        /** 服务端推荐的目标节点，可为 null 表示轮询 */
        ServerInfo serverInfo;
    }
    
    public String getTenant() {
        return tenant;
    }
    
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }
    
    /**
     * 查询当前连接对某能力键的支持情况；连接未就绪时返回 null。
     * Return ability of current connection.
     *
     * @param abilityKey ability key
     * @return whether support, return null if connection is not ready
     */
    public AbilityStatus getConnectionAbility(AbilityKey abilityKey) {
        if (currentConnection != null) {
            return currentConnection.getConnectionAbility(abilityKey);
        }
        // 连接尚未建立时不做能力查询
        return null;
    }
}
