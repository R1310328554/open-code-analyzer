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
package org.apache.rocketmq.remoting.netty;

import org.apache.rocketmq.remoting.common.TlsMode;

import static org.apache.rocketmq.remoting.netty.TlsSystemConfig.TLS_ENABLE;

/**
 * Netty Remoting 客户端配置：线程池、信号量、超时、TLS 与写缓冲水位等参数。
 */
public class NettyClientConfig {
    /** Netty 客户端 Worker 线程数。 */
    private int clientWorkerThreads = NettySystemConfig.clientWorkerSize;
    /** 异步回调线程池大小，默认为 CPU 核数。 */
    private int clientCallbackExecutorThreads = Runtime.getRuntime().availableProcessors();
    /** oneway 并发请求信号量上限。 */
    private int clientOnewaySemaphoreValue = NettySystemConfig.CLIENT_ONEWAY_SEMAPHORE_VALUE;
    /** 异步 RPC 并发请求信号量上限。 */
    private int clientAsyncSemaphoreValue = NettySystemConfig.CLIENT_ASYNC_SEMAPHORE_VALUE;
    /** TCP 连接超时毫秒数。 */
    private int connectTimeoutMillis = NettySystemConfig.connectTimeoutMillis;
    /** 通道非活跃状态判定间隔（毫秒）。 */
    private long channelNotActiveInterval = 1000 * 60;

    /** 是否扫描并连接可用的 NameServer 地址。 */
    private boolean isScanAvailableNameSrv = true;

    /**
     * 通道读写均空闲超过该秒数时触发 IdleStateEvent；{@code 0} 表示禁用。
     */
    private int clientChannelMaxIdleTimeSeconds = NettySystemConfig.clientChannelMaxIdleTimeSeconds;

    private int clientSocketSndBufSize = NettySystemConfig.socketSndbufSize;
    private int clientSocketRcvBufSize = NettySystemConfig.socketRcvbufSize;
    private boolean clientPooledByteBufAllocatorEnable = false;
    private boolean clientCloseSocketIfTimeout = NettySystemConfig.clientCloseSocketIfTimeout;

    /** 是否启用 TLS，可由系统属性 {@link TlsSystemConfig#TLS_ENABLE} 覆盖。 */
    private boolean useTLS = Boolean.parseBoolean(System.getProperty(TLS_ENABLE,
        String.valueOf(TlsSystemConfig.tlsMode == TlsMode.ENFORCING)));

    private String socksProxyConfig = "{}";

    private int writeBufferHighWaterMark = NettySystemConfig.writeBufferHighWaterMark;
    private int writeBufferLowWaterMark = NettySystemConfig.writeBufferLowWaterMark;

    /** 为 true 时禁用独立回调线程池。 */
    private boolean disableCallbackExecutor = false;
    /** 为 true 时禁用 Netty Worker EventLoopGroup。 */
    private boolean disableNettyWorkerGroup = false;

    /** 断线重连的最大间隔秒数。 */
    private long maxReconnectIntervalTimeSeconds = 60;

    /** 收到 HTTP/2 GOAWAY 等信号时是否自动重连。 */
    private boolean enableReconnectForGoAway = true;

    public boolean isClientCloseSocketIfTimeout() {
        return clientCloseSocketIfTimeout;
    }

    public void setClientCloseSocketIfTimeout(final boolean clientCloseSocketIfTimeout) {
        this.clientCloseSocketIfTimeout = clientCloseSocketIfTimeout;
    }

    public int getClientWorkerThreads() {
        return clientWorkerThreads;
    }

    public void setClientWorkerThreads(int clientWorkerThreads) {
        this.clientWorkerThreads = clientWorkerThreads;
    }

    public int getClientOnewaySemaphoreValue() {
        return clientOnewaySemaphoreValue;
    }

    public void setClientOnewaySemaphoreValue(int clientOnewaySemaphoreValue) {
        this.clientOnewaySemaphoreValue = clientOnewaySemaphoreValue;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public int getClientCallbackExecutorThreads() {
        return clientCallbackExecutorThreads;
    }

    public void setClientCallbackExecutorThreads(int clientCallbackExecutorThreads) {
        this.clientCallbackExecutorThreads = clientCallbackExecutorThreads;
    }

    public long getChannelNotActiveInterval() {
        return channelNotActiveInterval;
    }

    public void setChannelNotActiveInterval(long channelNotActiveInterval) {
        this.channelNotActiveInterval = channelNotActiveInterval;
    }

    public int getClientAsyncSemaphoreValue() {
        return clientAsyncSemaphoreValue;
    }

    public void setClientAsyncSemaphoreValue(int clientAsyncSemaphoreValue) {
        this.clientAsyncSemaphoreValue = clientAsyncSemaphoreValue;
    }

    public int getClientChannelMaxIdleTimeSeconds() {
        return clientChannelMaxIdleTimeSeconds;
    }

    public void setClientChannelMaxIdleTimeSeconds(int clientChannelMaxIdleTimeSeconds) {
        this.clientChannelMaxIdleTimeSeconds = clientChannelMaxIdleTimeSeconds;
    }

    public int getClientSocketSndBufSize() {
        return clientSocketSndBufSize;
    }

    public void setClientSocketSndBufSize(int clientSocketSndBufSize) {
        this.clientSocketSndBufSize = clientSocketSndBufSize;
    }

    public int getClientSocketRcvBufSize() {
        return clientSocketRcvBufSize;
    }

    public void setClientSocketRcvBufSize(int clientSocketRcvBufSize) {
        this.clientSocketRcvBufSize = clientSocketRcvBufSize;
    }

    public boolean isClientPooledByteBufAllocatorEnable() {
        return clientPooledByteBufAllocatorEnable;
    }

    public void setClientPooledByteBufAllocatorEnable(boolean clientPooledByteBufAllocatorEnable) {
        this.clientPooledByteBufAllocatorEnable = clientPooledByteBufAllocatorEnable;
    }

    public boolean isUseTLS() {
        return useTLS;
    }

    public void setUseTLS(boolean useTLS) {
        this.useTLS = useTLS;
    }

    public int getWriteBufferLowWaterMark() {
        return writeBufferLowWaterMark;
    }

    public void setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        this.writeBufferLowWaterMark = writeBufferLowWaterMark;
    }

    public int getWriteBufferHighWaterMark() {
        return writeBufferHighWaterMark;
    }

    public void setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        this.writeBufferHighWaterMark = writeBufferHighWaterMark;
    }

    public boolean isDisableCallbackExecutor() {
        return disableCallbackExecutor;
    }

    public void setDisableCallbackExecutor(boolean disableCallbackExecutor) {
        this.disableCallbackExecutor = disableCallbackExecutor;
    }

    public boolean isDisableNettyWorkerGroup() {
        return disableNettyWorkerGroup;
    }

    public void setDisableNettyWorkerGroup(boolean disableNettyWorkerGroup) {
        this.disableNettyWorkerGroup = disableNettyWorkerGroup;
    }

    public long getMaxReconnectIntervalTimeSeconds() {
        return maxReconnectIntervalTimeSeconds;
    }

    public void setMaxReconnectIntervalTimeSeconds(long maxReconnectIntervalTimeSeconds) {
        this.maxReconnectIntervalTimeSeconds = maxReconnectIntervalTimeSeconds;
    }

    public boolean isEnableReconnectForGoAway() {
        return enableReconnectForGoAway;
    }

    public void setEnableReconnectForGoAway(boolean enableReconnectForGoAway) {
        this.enableReconnectForGoAway = enableReconnectForGoAway;
    }

    public String getSocksProxyConfig() {
        return socksProxyConfig;
    }

    public void setSocksProxyConfig(String socksProxyConfig) {
        this.socksProxyConfig = socksProxyConfig;
    }

    public boolean isScanAvailableNameSrv() {
        return isScanAvailableNameSrv;
    }

    public void setScanAvailableNameSrv(boolean scanAvailableNameSrv) {
        this.isScanAvailableNameSrv = scanAvailableNameSrv;
    }
}
