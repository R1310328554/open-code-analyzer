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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

/**
 * {@link SpdyDataFrame} 的默认实现：承载 SPDY 流上的应用层数据载荷。
 * <p>继承 {@link DefaultSpdyStreamFrame} 的 streamId/last 语义；payload 受
 * {@link SpdyCodecUtil#SPDY_MAX_LENGTH} 限制，并实现 {@link ByteBuf} 引用计数。
 */
public class DefaultSpdyDataFrame extends DefaultSpdyStreamFrame implements SpdyDataFrame {

    /** 帧载荷，生命周期由本对象管理（retain/release） */
    private final ByteBuf data;

    /**
     * 创建空载荷的 DATA 帧。
     *
     * @param streamId the Stream-ID of this frame
     */
    public DefaultSpdyDataFrame(int streamId) {
        this(streamId, Unpooled.buffer(0));
    }

    /**
     * 创建带指定载荷的 DATA 帧。
     *
     * @param streamId  the Stream-ID of this frame
     * @param data      the payload of the frame. Can not exceed {@link SpdyCodecUtil#SPDY_MAX_LENGTH}
     */
    public DefaultSpdyDataFrame(int streamId, ByteBuf data) {
        super(streamId);
        this.data = validate(
                ObjectUtil.checkNotNull(data, "data"));
    }

    /** 校验载荷长度不超过 SPDY 帧上限 */
    private static ByteBuf validate(ByteBuf data) {
        if (data.readableBytes() > SpdyCodecUtil.SPDY_MAX_LENGTH) {
            throw new IllegalArgumentException("data payload cannot exceed "
                    + SpdyCodecUtil.SPDY_MAX_LENGTH + " bytes");
        }
        return data;
    }

    @Override
    public SpdyDataFrame setStreamId(int streamId) {
        super.setStreamId(streamId);
        return this;
    }

    @Override
    public SpdyDataFrame setLast(boolean last) {
        super.setLast(last);
        return this;
    }

    @Override
    public ByteBuf content() {
        return ByteBufUtil.ensureAccessible(data);
    }

    @Override
    public SpdyDataFrame copy() {
        return replace(content().copy());
    }

    @Override
    public SpdyDataFrame duplicate() {
        return replace(content().duplicate());
    }

    @Override
    public SpdyDataFrame retainedDuplicate() {
        return replace(content().retainedDuplicate());
    }

    @Override
    public SpdyDataFrame replace(ByteBuf content) {
        SpdyDataFrame frame = new DefaultSpdyDataFrame(streamId(), content);
        frame.setLast(isLast());
        return frame;
    }

    @Override
    public int refCnt() {
        return data.refCnt();
    }

    @Override
    public SpdyDataFrame retain() {
        data.retain();
        return this;
    }

    @Override
    public SpdyDataFrame retain(int increment) {
        data.retain(increment);
        return this;
    }

    @Override
    public SpdyDataFrame touch() {
        data.touch();
        return this;
    }

    @Override
    public SpdyDataFrame touch(Object hint) {
        data.touch(hint);
        return this;
    }

    @Override
    public boolean release() {
        return data.release();
    }

    @Override
    public boolean release(int decrement) {
        return data.release(decrement);
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
            .append("--> Size = ");
        if (refCnt() == 0) {
            buf.append("(freed)");
        } else {
            buf.append(content().readableBytes());
        }
        return buf.toString();
    }
}
