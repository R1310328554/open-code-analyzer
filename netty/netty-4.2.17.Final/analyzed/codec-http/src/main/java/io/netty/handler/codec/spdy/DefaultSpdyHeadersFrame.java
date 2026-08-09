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

import io.netty.util.internal.StringUtil;

import java.util.Map;

/**
 * {@link SpdyHeadersFrame} 的默认实现：携带 HTTP 风格头部的 SPDY 流帧基类。
 * <p>子类包括 SYN_STREAM、SYN_REPLY 及 HEADERS 变体；{@code invalid} 标记协议违规，
 * {@code truncated} 表示头部因大小限制被截断。
 */
public class DefaultSpdyHeadersFrame extends DefaultSpdyStreamFrame
        implements SpdyHeadersFrame {

    /** 帧内容是否违反 SPDY 协议（解码器置位） */
    private boolean invalid;
    /** 头部是否因超出限制被截断 */
    private boolean truncated;
    /** 帧内头部集合 */
    private final SpdyHeaders headers;

    /**
     * 创建带默认校验的 HEADERS 帧。
     *
     * @param streamId the Stream-ID of this frame
     */
    public DefaultSpdyHeadersFrame(int streamId) {
        this(streamId, true);
    }

    /**
     * 创建 HEADERS 帧，可选是否校验头部名/值。
     *
     * @param streamId the Stream-ID of this frame
     * @param validate validate the header names and values when adding them to the {@link SpdyHeaders}
     */
    public DefaultSpdyHeadersFrame(int streamId, boolean validate) {
        super(streamId);
        headers = new DefaultSpdyHeaders(validate);
    }

    @Override
    public SpdyHeadersFrame setStreamId(int streamId) {
        super.setStreamId(streamId);
        return this;
    }

    @Override
    public SpdyHeadersFrame setLast(boolean last) {
        super.setLast(last);
        return this;
    }

    @Override
    public boolean isInvalid() {
        return invalid;
    }

    @Override
    public SpdyHeadersFrame setInvalid() {
        invalid = true;
        return this;
    }

    @Override
    public boolean isTruncated() {
        return truncated;
    }

    @Override
    public SpdyHeadersFrame setTruncated() {
        truncated = true;
        return this;
    }

    @Override
    public SpdyHeaders headers() {
        return headers;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder()
            .append(StringUtil.simpleClassName(this))
            .append("(last: ")
            .append(isLast())
            .append(')')
            .append(StringUtil.NEWLINE)
            .append("--> Stream-ID = ")
            .append(streamId())
            .append(StringUtil.NEWLINE)
            .append("--> Headers:")
            .append(StringUtil.NEWLINE);
        appendHeaders(buf);

        // Remove the last newline.
        buf.setLength(buf.length() - StringUtil.NEWLINE.length());
        return buf.toString();
    }

    /** 子类可覆盖以定制头部 dump 格式 */
    protected void appendHeaders(StringBuilder buf) {
        for (Map.Entry<CharSequence, CharSequence> e: headers()) {
            buf.append("    ");
            buf.append(e.getKey());
            buf.append(": ");
            buf.append(e.getValue());
            buf.append(StringUtil.NEWLINE);
        }
    }
}
