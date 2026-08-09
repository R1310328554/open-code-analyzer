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
 * 绑定到单条 SPDY 流的帧类型基接口（如 DATA、HEADERS、RST_STREAM 等）。
 * <p>流 ID 奇偶区分客户端/服务器发起；{@code isLast} 表示本端在该流上不再发送后续帧。
 */
public interface SpdyStreamFrame extends SpdyFrame {

    /**
     * 返回本帧所属的 Stream-ID。
     */
    int streamId();

    /**
     * 设置 Stream-ID（必须为正整数）。
     */
    SpdyStreamFrame setStreamId(int streamID);

    /**
     * 若本帧是该流上发送方的最后一帧，返回 {@code true}。
     */
    boolean isLast();

    /**
     * 标记本帧是否为该流上的最后一帧（半关闭发送方向）。
     */
    SpdyStreamFrame setLast(boolean last);
}
