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
package io.netty.handler.codec.http3;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.internal.ObjectUtil;

import static io.netty.handler.codec.http3.Http3FrameValidationUtils.frameTypeUnexpected;
import static io.netty.handler.codec.http3.Http3FrameValidationUtils.validateFrameRead;

/**
 * 入站帧类型守卫：只向下游传递 {@code T} 类型帧，其余触发连接错误或交由子类处理。
 */
class Http3FrameTypeInboundValidationHandler<T extends Http3Frame> extends ChannelInboundHandlerAdapter {

    protected final Class<T> frameType;

    Http3FrameTypeInboundValidationHandler(Class<T> frameType) {
        this.frameType = ObjectUtil.checkNotNull(frameType, "frameType");
    }

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        final T frame = validateFrameRead(frameType, msg);
        if (frame != null) {
            channelRead(ctx, frame);
        } else {
            readFrameDiscarded(ctx, msg);
        }
    }

    void channelRead(ChannelHandlerContext ctx, T frame) throws Exception {
        ctx.fireChannelRead(frame);
    }

    /** 收到非预期类型时的默认处理：连接级 H3_FRAME_UNEXPECTED。 */
    void readFrameDiscarded(ChannelHandlerContext ctx, Object discardedFrame) {
        frameTypeUnexpected(ctx, discardedFrame);
    }
}
