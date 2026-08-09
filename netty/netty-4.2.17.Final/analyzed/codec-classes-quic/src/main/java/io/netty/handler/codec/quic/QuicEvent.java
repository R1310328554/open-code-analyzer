/*
 * Copyright 2021 The Netty Project
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
package io.netty.handler.codec.quic;

/**
 * QUIC 用户事件标记接口；事件经 {@link io.netty.channel.ChannelPipeline#fireUserEventTriggered(Object)}
 * 在 {@link io.netty.channel.ChannelPipeline} 中传播，用于通知应用层 QUIC 特有状态变化。
 */
public interface QuicEvent { }
