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

import static io.netty.util.internal.ObjectUtil.checkPositive;

/**
 * {@link SpdyStreamFrame} 的抽象基类：所有绑定 Stream-ID 的 SPDY 帧共用字段。
 * <p>{@code streamId} 必须为正整数；{@code last} 标记该帧是否为该流上最后一条消息
 * （SPDY/3 半关闭语义）。
 */
public abstract class DefaultSpdyStreamFrame implements SpdyStreamFrame {

    /** SPDY 流标识，0 为会话级控制流 */
    private int streamId;
    /** 是否为该流上的最后一帧（半关闭标志） */
    private boolean last;

    /**
     * 创建流帧基类实例。
     *
     * @param streamId the Stream-ID of this frame
     */
    protected DefaultSpdyStreamFrame(int streamId) {
        setStreamId(streamId);
    }

    @Override
    public int streamId() {
        return streamId;
    }

    @Override
    public SpdyStreamFrame setStreamId(int streamId) {
        checkPositive(streamId, "streamId");
        this.streamId = streamId;
        return this;
    }

    @Override
    public boolean isLast() {
        return last;
    }

    @Override
    public SpdyStreamFrame setLast(boolean last) {
        this.last = last;
        return this;
    }
}
