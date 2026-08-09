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
package org.apache.rocketmq.remoting;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.apache.rocketmq.remoting.exception.RemotingTooMuchRequestException;
import org.apache.rocketmq.remoting.netty.NettyRequestProcessor;
import org.apache.rocketmq.remoting.netty.ResponseFuture;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * Remoting 客户端接口：维护 NameServer 地址列表并提供同步/异步/单向 RPC。
 */
public interface RemotingClient extends RemotingService {

    /** 更新 NameServer 地址列表。 */
    void updateNameServerAddressList(final List<String> addrs);

    /** 返回当前配置的 NameServer 地址列表。 */
    List<String> getNameServerAddressList();

    /** 返回当前可达的 NameServer 地址列表。 */
    List<String> getAvailableNameSrvList();

    /** 同步 RPC：阻塞等待响应或超时/连接异常。 */
    RemotingCommand invokeSync(final String addr, final RemotingCommand request,
        final long timeoutMillis) throws InterruptedException, RemotingConnectException,
        RemotingSendRequestException, RemotingTimeoutException;

    /** 异步 RPC：通过 {@link InvokeCallback} 接收结果。 */
    void invokeAsync(final String addr, final RemotingCommand request, final long timeoutMillis,
        final InvokeCallback invokeCallback) throws InterruptedException, RemotingConnectException,
        RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException;

    /** 单向 RPC：发送后不等待响应。 */
    void invokeOneway(final String addr, final RemotingCommand request, final long timeoutMillis)
        throws InterruptedException, RemotingConnectException, RemotingTooMuchRequestException,
        RemotingTimeoutException, RemotingSendRequestException;

    /** 基于 {@link #invokeAsync} 的 CompletableFuture 封装。 */
    default CompletableFuture<RemotingCommand> invoke(final String addr, final RemotingCommand request,
        final long timeoutMillis) {
        CompletableFuture<RemotingCommand> future = new CompletableFuture<>();
        try {
            invokeAsync(addr, request, timeoutMillis, new InvokeCallback() {

                @Override
                public void operationComplete(ResponseFuture responseFuture) {

                }

                @Override
                public void operationSucceed(RemotingCommand response) {
                    future.complete(response);
                }

                @Override
                public void operationFail(Throwable throwable) {
                    future.completeExceptionally(throwable);
                }
            });
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }
        return future;
    }

    /** 按请求码注册处理器与执行线程池。 */
    void registerProcessor(final int requestCode, final NettyRequestProcessor processor,
        final ExecutorService executor);

    /** 设置异步回调专用线程池。 */
    void setCallbackExecutor(final ExecutorService callbackExecutor);

    /** 判断到指定地址的通道是否可写（未触发高水位）。 */
    boolean isChannelWritable(final String addr);

    /** 探测目标地址是否可达。 */
    boolean isAddressReachable(final String addr);

    /** 关闭到指定地址列表的全部连接通道。 */
    void closeChannels(final List<String> addrList);
}
