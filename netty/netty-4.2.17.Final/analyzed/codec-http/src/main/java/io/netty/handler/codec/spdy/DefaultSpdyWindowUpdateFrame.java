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
import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

import io.netty.util.internal.StringUtil;

/**
 * {@link SpdyWindowUpdateFrame} 的默认实现：增量更新流或会话的接收窗口。
 * <p>{@code streamId=0} 更新会话级窗口；正 {@code streamId} 更新对应流窗口。
 * {@code deltaWindowSize} 必须为正，表示允许对端再发送的字节数。
 */
public class DefaultSpdyWindowUpdateFrame implements SpdyWindowUpdateFrame {

    /** 目标流 ID，0 表示会话级窗口 */
    private int streamId;
    /** 窗口增量（字节） */
    private int deltaWindowSize;

    /**
     * 创建 WINDOW_UPDATE 帧。
     *
     * @param streamId        the Stream-ID of this frame
     * @param deltaWindowSize the Delta-Window-Size of this frame
     */
    public DefaultSpdyWindowUpdateFrame(int streamId, int deltaWindowSize) {
        setStreamId(streamId);
        setDeltaWindowSize(deltaWindowSize);
    }

    @Override
    public int streamId() {
        return streamId;
    }

    @Override
    public SpdyWindowUpdateFrame setStreamId(int streamId) {
        checkPositiveOrZero(streamId, "streamId");
        this.streamId = streamId;
        return this;
    }

    @Override
    public int deltaWindowSize() {
        return deltaWindowSize;
    }

    @Override
    public SpdyWindowUpdateFrame setDeltaWindowSize(int deltaWindowSize) {
        checkPositive(deltaWindowSize, "deltaWindowSize");
        this.deltaWindowSize = deltaWindowSize;
        return this;
    }

    @Override
    public String toString() {
        return new StringBuilder()
            .append(StringUtil.simpleClassName(this))
            .append(StringUtil.NEWLINE)
            .append("--> Stream-ID = ")
            .append(streamId())
            .append(StringUtil.NEWLINE)
            .append("--> Delta-Window-Size = ")
            .append(deltaWindowSize())
            .toString();
    }
}
