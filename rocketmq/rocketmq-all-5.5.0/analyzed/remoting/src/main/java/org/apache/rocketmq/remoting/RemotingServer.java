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

import io.netty.channel.Channel;
import java.util.concurrent.ExecutorService;
import org.apache.rocketmq.common.Pair;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.apache.rocketmq.remoting.exception.RemotingTooMuchRequestException;
import org.apache.rocketmq.remoting.netty.NettyRequestProcessor;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

/**
 * Remoting 服务端接口：监听端口、注册处理器并向客户端通道发起 RPC。
 */
public interface RemotingServer extends RemotingService {

    /** 按请求码注册业务处理器。 */
    void registerProcessor(final int requestCode, final NettyRequestProcessor processor,
        final ExecutorService executor);

    /** 注册未匹配请求码时的默认处理器。 */
    void registerDefaultProcessor(final NettyRequestProcessor processor, final ExecutorService executor);

    /** 返回本地监听端口。 */
    int localListenPort();

    /** 获取指定请求码对应的处理器与线程池。 */
    Pair<NettyRequestProcessor, ExecutorService> getProcessorPair(final int requestCode);

    /** 获取默认处理器与线程池。 */
    Pair<NettyRequestProcessor, ExecutorService> getDefaultProcessorPair();

    /** 在指定端口创建子 Remoting 服务端实例。 */
    RemotingServer newRemotingServer(int port);

    /** 移除并关闭指定端口的子服务端。 */
    void removeRemotingServer(int port);

    /** 经已有通道同步发送请求并等待响应。 */
    RemotingCommand invokeSync(final Channel channel, final RemotingCommand request,
        final long timeoutMillis) throws InterruptedException, RemotingSendRequestException,
        RemotingTimeoutException;

    /** 经已有通道异步发送请求。 */
    void invokeAsync(final Channel channel, final RemotingCommand request, final long timeoutMillis,
        final InvokeCallback invokeCallback) throws InterruptedException,
        RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException;

    /** 经已有通道单向发送请求。 */
    void invokeOneway(final Channel channel, final RemotingCommand request, final long timeoutMillis)
        throws InterruptedException, RemotingTooMuchRequestException, RemotingTimeoutException,
        RemotingSendRequestException;

    /** 向客户端通道写回响应并在完成后执行 callback。 */
    void writeResponse(final Channel channel, final RemotingCommand request, 
        final RemotingCommand response, final java.util.function.Consumer<io.netty.util.concurrent.Future<?>> callback);

}
