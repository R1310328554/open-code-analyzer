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

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

import io.netty.util.internal.StringUtil;

/**
 * {@link SpdySynStreamFrame} 的默认实现：客户端发起新 SPDY 流的请求帧。
 * <p>携带请求头部、关联流 ID（推送场景）、优先级（0–7）及单向标志；
 * 等价于 HTTP 请求行 + 头部，不含 body。
 */
public class DefaultSpdySynStreamFrame extends DefaultSpdyHeadersFrame
        implements SpdySynStreamFrame {

    /** 关联流 ID（server push 时指向父流，0 表示无关联） */
    private int associatedStreamId;
    /** 流优先级，0 最高、7 最低 */
    private byte priority;
    /** 是否为单向流（仅接收数据，如 server push） */
    private boolean unidirectional;

    /**
     * 创建 SYN_STREAM 帧（默认校验头部）。
     *
     * @param streamId           the Stream-ID of this frame
     * @param associatedStreamId the Associated-To-Stream-ID of this frame
     * @param priority           the priority of the stream
     */
    public DefaultSpdySynStreamFrame(int streamId, int associatedStreamId, byte priority) {
        this(streamId, associatedStreamId, priority, true);
    }

    /**
     * 创建 SYN_STREAM 帧，可选头部校验。
     *
     * @param streamId           the Stream-ID of this frame
     * @param associatedStreamId the Associated-To-Stream-ID of this frame
     * @param priority           the priority of the stream
     * @param validateHeaders    validate the header names and values when adding them to the {@link SpdyHeaders}
     */
    public DefaultSpdySynStreamFrame(int streamId, int associatedStreamId, byte priority, boolean validateHeaders) {
        super(streamId, validateHeaders);
        setAssociatedStreamId(associatedStreamId);
        setPriority(priority);
    }

    @Override
    public SpdySynStreamFrame setStreamId(int streamId) {
        super.setStreamId(streamId);
        return this;
    }

    @Override
    public SpdySynStreamFrame setLast(boolean last) {
        super.setLast(last);
        return this;
    }

    @Override
    public SpdySynStreamFrame setInvalid() {
        super.setInvalid();
        return this;
    }

    @Override
    public int associatedStreamId() {
        return associatedStreamId;
    }

    @Override
    public SpdySynStreamFrame setAssociatedStreamId(int associatedStreamId) {
        checkPositiveOrZero(associatedStreamId, "associatedStreamId");
        this.associatedStreamId = associatedStreamId;
        return this;
    }

    @Override
    public byte priority() {
        return priority;
    }

    @Override
    public SpdySynStreamFrame setPriority(byte priority) {
        if (priority < 0 || priority > 7) {
            throw new IllegalArgumentException(
                    "Priority must be between 0 and 7 inclusive: " + priority);
        }
        this.priority = priority;
        return this;
    }

    @Override
    public boolean isUnidirectional() {
        return unidirectional;
    }

    @Override
    public SpdySynStreamFrame setUnidirectional(boolean unidirectional) {
        this.unidirectional = unidirectional;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder()
            .append(StringUtil.simpleClassName(this))
            .append("(last: ")
            .append(isLast())
            .append("; unidirectional: ")
            .append(isUnidirectional())
            .append(')')
            .append(StringUtil.NEWLINE)
            .append("--> Stream-ID = ")
            .append(streamId())
            .append(StringUtil.NEWLINE);
        if (associatedStreamId != 0) {
            buf.append("--> Associated-To-Stream-ID = ")
               .append(associatedStreamId())
               .append(StringUtil.NEWLINE);
        }
        buf.append("--> Priority = ")
           .append(priority())
           .append(StringUtil.NEWLINE)
           .append("--> Headers:")
           .append(StringUtil.NEWLINE);
        appendHeaders(buf);

        // Remove the last newline.
        buf.setLength(buf.length() - StringUtil.NEWLINE.length());
        return buf.toString();
    }
}
