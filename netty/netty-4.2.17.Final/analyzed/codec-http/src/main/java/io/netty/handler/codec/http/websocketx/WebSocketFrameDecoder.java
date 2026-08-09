/*
 * Copyright 2013 The Netty Project
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
package io.netty.handler.codec.http.websocketx;

import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelPipeline;

/**
 * WebSocket 帧解码器标记接口，便于在 {@link ChannelPipeline} 中按类型查找解码 handler。
 * <p>实现类如 {@link WebSocket08FrameDecoder}、{@link WebSocket00FrameDecoder}。
 */
public interface WebSocketFrameDecoder extends ChannelInboundHandler {
}
