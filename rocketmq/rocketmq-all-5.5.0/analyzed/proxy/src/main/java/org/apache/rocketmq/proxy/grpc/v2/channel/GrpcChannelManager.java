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

package org.apache.rocketmq.proxy.grpc.v2.channel;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.rocketmq.common.ThreadFactoryImpl;
import org.apache.rocketmq.common.utils.StartAndShutdown;
import org.apache.rocketmq.common.utils.ThreadUtils;
import org.apache.rocketmq.proxy.common.ProxyContext;
import org.apache.rocketmq.proxy.config.ConfigurationManager;
import org.apache.rocketmq.proxy.config.ProxyConfig;
import org.apache.rocketmq.proxy.grpc.v2.common.GrpcClientSettingsManager;
import org.apache.rocketmq.proxy.service.relay.ProxyRelayResult;
import org.apache.rocketmq.proxy.service.relay.ProxyRelayService;
import org.apache.rocketmq.remoting.protocol.ResponseCode;

/**
 * gRPC 客户端通道管理器：按 clientId 维护 {@link GrpcClientChannel} 及异步回调 nonce 映射。
 */
public class GrpcChannelManager implements StartAndShutdown {
    private final ProxyRelayService proxyRelayService;
    private final GrpcClientSettingsManager grpcClientSettingsManager;
    /** clientId 到 gRPC 通道的并发映射。 */
    protected final ConcurrentMap<String, GrpcClientChannel> clientIdChannelMap = new ConcurrentHashMap<>();

    /** 异步回调 nonce 自增生成器。 */
    protected final AtomicLong nonceIdGenerator = new AtomicLong(0);
    /** nonce 到远程调用 Future 的映射，供 Telemetry 回写结果。 */
    protected final ConcurrentMap<String /* nonce */, ResultFuture> resultNonceFutureMap = new ConcurrentHashMap<>();

    protected final ScheduledExecutorService scheduledExecutorService = ThreadUtils.newSingleThreadScheduledExecutor(
        new ThreadFactoryImpl("GrpcChannelManager_")
    );

    public GrpcChannelManager(ProxyRelayService proxyRelayService, GrpcClientSettingsManager grpcClientSettingsManager) {
        this.proxyRelayService = proxyRelayService;
        this.grpcClientSettingsManager = grpcClientSettingsManager;
        this.init();
    }

    protected void init() {
        this.scheduledExecutorService.scheduleAtFixedRate(
            this::scanExpireResultFuture,
            10, 1, TimeUnit.SECONDS
        );
    }

    /** 按 clientId 创建或复用 gRPC 客户端通道。 */
    public GrpcClientChannel createChannel(ProxyContext ctx, String clientId) {
        return this.clientIdChannelMap.computeIfAbsent(clientId,
            k -> new GrpcClientChannel(proxyRelayService, grpcClientSettingsManager, this, ctx, clientId));
    }

    /** 按 clientId 获取已注册通道，不存在则返回 null。 */
    public GrpcClientChannel getChannel(String clientId) {
        return clientIdChannelMap.get(clientId);
    }

    public GrpcClientChannel removeChannel(String clientId) {
        return this.clientIdChannelMap.remove(clientId);
    }

    /** 注册异步响应 Future 并返回唯一 nonce。 */
    public <T> String addResponseFuture(CompletableFuture<ProxyRelayResult<T>> responseFuture) {
        String nonce = this.nextNonce();
        this.resultNonceFutureMap.put(nonce, new ResultFuture<>(responseFuture));
        return nonce;
    }

    public <T> CompletableFuture<ProxyRelayResult<T>> getAndRemoveResponseFuture(String nonce) {
        ResultFuture<T> resultFuture = this.resultNonceFutureMap.remove(nonce);
        if (resultFuture != null) {
            return resultFuture.future;
        }
        return null;
    }

    protected String nextNonce() {
        return String.valueOf(this.nonceIdGenerator.getAndIncrement());
    }

    /** 定时扫描并超时完成未响应的 nonce Future。 */
    protected void scanExpireResultFuture() {
        ProxyConfig proxyConfig = ConfigurationManager.getProxyConfig();
        long timeOutMs = TimeUnit.SECONDS.toMillis(proxyConfig.getGrpcProxyRelayRequestTimeoutInSeconds());

        Set<String> nonceSet = this.resultNonceFutureMap.keySet();
        for (String nonce : nonceSet) {
            ResultFuture<?> resultFuture = this.resultNonceFutureMap.get(nonce);
            if (resultFuture == null) {
                continue;
            }
            if (System.currentTimeMillis() - resultFuture.createTime > timeOutMs) {
                resultFuture = this.resultNonceFutureMap.remove(nonce);
                if (resultFuture != null) {
                    resultFuture.future.complete(new ProxyRelayResult<>(ResponseCode.SYSTEM_BUSY, "call remote timeout", null));
                }
            }
        }
    }

    @Override
    public void shutdown() throws Exception {
        this.scheduledExecutorService.shutdown();
    }

    @Override
    public void start() throws Exception {

    }

    /** 包装异步 Future 及其创建时间戳。 */
    protected static class ResultFuture<T> {
        public CompletableFuture<ProxyRelayResult<T>> future;
        public long createTime = System.currentTimeMillis();

        public ResultFuture(CompletableFuture<ProxyRelayResult<T>> future) {
            this.future = future;
        }
    }
}
