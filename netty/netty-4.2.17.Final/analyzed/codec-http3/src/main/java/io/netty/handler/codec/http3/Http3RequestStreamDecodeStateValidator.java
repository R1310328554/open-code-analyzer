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
import io.netty.handler.codec.http3.Http3RequestStreamEncodeStateValidator.State;

import static io.netty.handler.codec.http3.Http3FrameValidationUtils.frameTypeUnexpected;
import static io.netty.handler.codec.http3.Http3RequestStreamEncodeStateValidator.evaluateFrame;
import static io.netty.handler.codec.http3.Http3RequestStreamEncodeStateValidator.isFinalHeadersReceived;
import static io.netty.handler.codec.http3.Http3RequestStreamEncodeStateValidator.isStreamStarted;
import static io.netty.handler.codec.http3.Http3RequestStreamEncodeStateValidator.isTrailersReceived;

/**
 * 入站 HTTP 消息帧顺序校验器，实现 {@link Http3RequestStreamCodecState} 供 QPACK 与内容长度校验查询。
 * <p>状态机与 {@link Http3RequestStreamEncodeStateValidator} 共用 {@link State} 定义：
 * None → Headers（1xx）→ FinalHeaders → Trailers，非法帧触发 {@code H3_FRAME_UNEXPECTED}。
 */
final class Http3RequestStreamDecodeStateValidator extends ChannelInboundHandlerAdapter
        implements Http3RequestStreamCodecState {
    private State state = State.None;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Http3RequestStreamFrame)) {
            super.channelRead(ctx, msg);
            return;
        }
        final Http3RequestStreamFrame frame = (Http3RequestStreamFrame) msg;
        final State nextState = evaluateFrame(state, frame);
        if (nextState == null) {
            frameTypeUnexpected(ctx, msg);
            return;
        }
        state = nextState;
        super.channelRead(ctx, msg);
    }

    @Override
    public boolean started() {
        return isStreamStarted(state);
    }

    @Override
    public boolean receivedFinalHeaders() {
        return isFinalHeadersReceived(state);
    }

    @Override
    public boolean terminated() {
        return isTrailersReceived(state);
    }
}
