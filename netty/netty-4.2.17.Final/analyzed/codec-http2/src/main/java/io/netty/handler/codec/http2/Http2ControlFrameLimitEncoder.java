/*
 * Copyright 2019 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.netty.handler.codec.http2;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * {@link DecoratingHttp2ConnectionEncoder} 装饰器：限制未 ACK 的控制帧（SETTINGS ACK、PING ACK、RST_STREAM）积压数量。
 * <p>对端大量触发控制帧却不消费应答时，达到阈值后发送 GOAWAY 并关闭连接，降低慢速控制帧类 DoS 风险。
 */
final class Http2ControlFrameLimitEncoder extends DecoratingHttp2ConnectionEncoder {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(Http2ControlFrameLimitEncoder.class);
    /** 当前已写出但 promise 尚未完成的控制帧计数。 */
    private int outstandingControlFrames;

    private final int maxOutstandingControlFrames;
    /** promise 完成时递减 {@link #outstandingControlFrames}。 */
    private final ChannelFutureListener outstandingControlFramesListener = f-> outstandingControlFrames--;
    private Http2LifecycleManager lifecycleManager;
    /** 已达上限后不再发送新的控制帧应答。 */
    private boolean limitReached;

    Http2ControlFrameLimitEncoder(Http2ConnectionEncoder delegate, int maxOutstandingControlFrames) {
        super(delegate);
        this.maxOutstandingControlFrames = ObjectUtil.checkPositive(maxOutstandingControlFrames,
                "maxOutstandingControlFrames");
    }

    @Override
    public void lifecycleManager(Http2LifecycleManager lifecycleManager) {
        this.lifecycleManager = lifecycleManager;
        super.lifecycleManager(lifecycleManager);
    }

    @Override
    public ChannelFuture writeSettingsAck(ChannelHandlerContext ctx, ChannelPromise promise) {
        ChannelPromise newPromise = handleOutstandingControlFrames(ctx, promise);
        if (newPromise == null) {
            return promise;
        }
        return super.writeSettingsAck(ctx, newPromise);
    }

    @Override
    public ChannelFuture writePing(ChannelHandlerContext ctx, boolean ack, long data, ChannelPromise promise) {
        // 仅对 PING ACK 计入控制帧上限，主动 PING 不受限
        if (ack) {
            ChannelPromise newPromise = handleOutstandingControlFrames(ctx, promise);
            if (newPromise == null) {
                return promise;
            }
            return super.writePing(ctx, ack, data, newPromise);
        }
        return super.writePing(ctx, ack, data, promise);
    }

    @Override
    public ChannelFuture writeRstStream(
            ChannelHandlerContext ctx, int streamId, long errorCode, ChannelPromise promise) {
        ChannelPromise newPromise = handleOutstandingControlFrames(ctx, promise);
        if (newPromise == null) {
            return promise;
        }
        return super.writeRstStream(ctx, streamId, errorCode, newPromise);
    }

    private ChannelPromise handleOutstandingControlFrames(ChannelHandlerContext ctx, ChannelPromise promise) {
        if (!limitReached) {
            if (outstandingControlFrames == maxOutstandingControlFrames) {
                // 达上限前先 flush 一次，尝试清空已完成的控制帧
                ctx.flush();
            }
            if (outstandingControlFrames == maxOutstandingControlFrames) {
                limitReached = true;
                Http2Exception exception = Http2Exception.connectionError(Http2Error.ENHANCE_YOUR_CALM,
                        "Maximum number %d of outstanding control frames reached", maxOutstandingControlFrames);
                logger.info("{} Maximum number {} of outstanding control frames reached, closing channel.",
                        ctx.channel(), maxOutstandingControlFrames, exception);

                // 先通知 lifecycleManager 再关闭 channel
                lifecycleManager.onError(ctx, true, exception);
                ctx.close();
            }
            outstandingControlFrames++;

            // 未达上限：promise 完成时递减计数
            return promise.unvoid().addListener(outstandingControlFramesListener);
        }
        return promise;
    }
}
