/*
 * Copyright 2020 The Netty Project
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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.ChannelInputShutdownEvent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.ObjectUtil;
import org.jetbrains.annotations.Nullable;

import static io.netty.handler.codec.http3.Http3CodecUtils.closeOnFailure;

/**
 * 本端控制流出站 handler：流激活时写入流类型前缀与本地 SETTINGS，
 * 并校验 MAX_PUSH_ID / GOAWAY 的单调性约束。
 */
final class Http3ControlStreamOutboundHandler
        extends Http3FrameTypeDuplexValidationHandler<Http3ControlStreamFrame> {
    private final boolean server;
    /** 编解码器，在流类型前缀写出后插入 pipeline 首部。 */
    private final ChannelHandler codec;
    /** 已发送的最大 push ID，用于禁止回退 MAX_PUSH_ID。 */
    private Long sentMaxPushId;
    /** 已发送 GOAWAY 中的流 ID，后续 GOAWAY 只能更小或相等。 */
    private Long sendGoAwayId;
    /** channelActive 时发送一次后即置 null，便于 GC。 */
    private Http3SettingsFrame localSettings;

    Http3ControlStreamOutboundHandler(boolean server, Http3SettingsFrame localSettings, ChannelHandler codec) {
        super(Http3ControlStreamFrame.class);
        this.server = server;
        this.localSettings = ObjectUtil.checkNotNull(localSettings, "localSettings");
        this.codec = ObjectUtil.checkNotNull(codec, "codec");
    }

    /**
     * Returns the last id that was sent in a MAX_PUSH_ID frame or {@code null} if none was sent yet.
     *
     * @return the id.
     */
    @Nullable
    Long sentMaxPushId() {
        return sentMaxPushId;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 控制流首字节必须是变长整数 0x00（HTTP3_CONTROL_STREAM_TYPE）
        // See https://tools.ietf.org/html/draft-ietf-quic-http-32#section-6.2.1
        ByteBuf buffer = ctx.alloc().buffer(8);
        Http3CodecUtils.writeVariableLengthInteger(buffer, Http3CodecUtils.HTTP3_CONTROL_STREAM_TYPE);
        ctx.write(buffer);
        // 流类型前缀必须作为原始 ByteBuf 先写出，再挂载帧编解码器
        ctx.pipeline().addFirst(codec);

        assert localSettings != null;
        // SETTINGS 发送失败则整连接 teardown
        closeOnFailure(ctx.writeAndFlush(localSettings));

        localSettings = null;

        ctx.fireChannelActive();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof ChannelInputShutdownEvent) {
            // 控制流半关闭视为关键流异常，触发 H3_CLOSED_CRITICAL_STREAM
            Http3CodecUtils.criticalStreamClosed(ctx);
        }
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // See https://tools.ietf.org/html/draft-ietf-quic-http-32#section-6.2.1
        Http3CodecUtils.criticalStreamClosed(ctx);
        ctx.fireChannelInactive();
    }

    @Override
    void write(ChannelHandlerContext ctx, Http3ControlStreamFrame msg, ChannelPromise promise) {
        if (msg instanceof Http3MaxPushIdFrame && !handleHttp3MaxPushIdFrame(promise, (Http3MaxPushIdFrame) msg)) {
            ReferenceCountUtil.release(msg);
            return;
        } else if (msg instanceof Http3GoAwayFrame && !handleHttp3GoAwayFrame(promise, (Http3GoAwayFrame) msg)) {
            ReferenceCountUtil.release(msg);
            return;
        }

        ctx.write(msg, promise);
    }

    private boolean handleHttp3MaxPushIdFrame(ChannelPromise promise, Http3MaxPushIdFrame maxPushIdFrame) {
        long id = maxPushIdFrame.id();

        // MAX_PUSH_ID 只能单调递增，不可缩小推送上限
        if (sentMaxPushId != null && id < sentMaxPushId) {
            promise.setFailure(new Http3Exception(Http3ErrorCode.H3_ID_ERROR, "MAX_PUSH_ID reduced limit."));
            return false;
        }

        sentMaxPushId = maxPushIdFrame.id();
        return true;
    }

    private boolean handleHttp3GoAwayFrame(ChannelPromise promise, Http3GoAwayFrame goAwayFrame) {
        long id = goAwayFrame.id();

        // 服务端 GOAWAY 的 id 必须指向推送流（id % 4 == 2）
        if (server && id % 4 != 0) {
            promise.setFailure(new Http3Exception(Http3ErrorCode.H3_ID_ERROR,
                    "GOAWAY id not valid : " + id));
            return false;
        }

        if (sendGoAwayId != null && id > sendGoAwayId) {
            promise.setFailure(new Http3Exception(Http3ErrorCode.H3_ID_ERROR,
                    "GOAWAY id is bigger then the last sent: " + id + " > " + sendGoAwayId));
            return false;
        }

        sendGoAwayId = id;
        return true;
    }

    @Override
    public boolean isSharable() {
        // 维护 sentMaxPushId / sendGoAwayId 等状态，不可共享
        return false;
    }
}
