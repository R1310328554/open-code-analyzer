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
 * SPDY GOAWAY 帧：通知对端关闭整个 SPDY 会话。
 * <p>携带最后成功处理的 Stream-ID 与 {@link SpdySessionStatus} 关闭原因；
 * 对端收到后应停止新建流并清理会话资源。
 */
public interface SpdyGoAwayFrame extends SpdyFrame {

    /**
     * 返回 Last-good-stream-ID（最后成功处理的流 ID）。
     */
    int lastGoodStreamId();

    /**
     * 设置 Last-good-stream-ID；不可为负数。
     */
    SpdyGoAwayFrame setLastGoodStreamId(int lastGoodStreamId);

    /**
     * 返回会话关闭状态码。
     */
    SpdySessionStatus status();

    /**
     * 设置会话关闭状态码。
     */
    SpdyGoAwayFrame setStatus(SpdySessionStatus status);
}
