/*
 * Copyright 2016 The Netty Project
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

package io.netty.handler.codec.http2;

/**
 * HTTP/2 {@code PING} 帧对象：用于连接保活、RTT 测量或确认对端仍存活。
 */
public interface Http2PingFrame extends Http2Frame {

    /**
     * 为 {@code true} 表示这是对先前 PING 的 ACK 响应，而非新发起的探测。
     */
    boolean ack();

    /**
     * 返回 8 字节不透明载荷；发送方自定义，对端原样回显。
     */
    long content();
}
