/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;

/**
 * Special event which will be fired and passed to the
 * {@link ChannelInboundHandler#userEventTriggered(ChannelHandlerContext, Object)} methods once the input of
 * a {@link SocketChannel} was shutdown and the {@link SocketChannelConfig#isAllowHalfClosure()} method returns
 * {@code true}.
 * <p>用户事件：{@link SocketChannel} 输入侧已 shutdown 且允许半关闭时，
 * 经 {@link ChannelInboundHandler#userEventTriggered} 向上传播。</p>
 */
public final class ChannelInputShutdownEvent {

    /**
     * Instance to use
     * <p>单例实例，用于 {@code fireUserEventTriggered}。</p>
     */
    @SuppressWarnings("InstantiationOfUtilityClass")
    public static final ChannelInputShutdownEvent INSTANCE = new ChannelInputShutdownEvent();

    private ChannelInputShutdownEvent() { }
}
