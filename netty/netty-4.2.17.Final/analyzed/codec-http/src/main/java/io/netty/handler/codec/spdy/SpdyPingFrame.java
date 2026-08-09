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
package io.netty.handler.codec.spdy;

/**
 * SPDY 协议的 PING 帧：会话级心跳与 RTT 测量，不绑定任何流。
 * <p>发起方填入唯一 {@code id}，对端收到后应原样回显相同 id 的 PING 帧。
 */
public interface SpdyPingFrame extends SpdyFrame {

    /**
     * 返回本 PING 帧的标识符。
     */
    int id();

    /**
     * 设置 PING 标识符（发送方应保证在会话内唯一）。
     */
    SpdyPingFrame setId(int id);
}
