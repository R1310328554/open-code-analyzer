/*
 * Copyright 2023 The Netty Project
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
package io.netty.handler.codec.http2;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Ticker;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 入站 RST 帧速率监听器装饰器：在 {@link #onRstStreamRead} 中按滑动窗口统计 RST 帧，
 * 超限时抛出预分配的 {@link Http2Error#ENHANCE_YOUR_CALM} 异常以硬关闭连接。
 */
final class Http2MaxRstFrameListener extends Http2FrameListenerDecorator {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(Http2MaxRstFrameListener.class);
    /** 预分配异常，避免热路径上重复构造 */
    private static final Http2Exception RST_FRAME_RATE_EXCEEDED = Http2Exception.newStatic(Http2Error.ENHANCE_YOUR_CALM,
            "Maximum number of RST frames reached",
            Http2Exception.ShutdownHint.HARD_SHUTDOWN, Http2MaxRstFrameListener.class, "onRstStreamRead(..)");

    private final long nanosPerWindow;
    private final int maxRstFramesPerWindow;
    private final Ticker ticker;
    private long lastRstFrameNano;
    /** 当前窗口内已收到的 RST 帧计数 */
    private int receivedRstInWindow;

    Http2MaxRstFrameListener(Http2FrameListener listener, int maxRstFramesPerWindow, int secondsPerWindow,
                             Ticker ticker) {
        super(listener);
        this.maxRstFramesPerWindow = maxRstFramesPerWindow;
        this.nanosPerWindow = TimeUnit.SECONDS.toNanos(secondsPerWindow);
        this.ticker = ticker;
        // 初始偏移一个窗口，使首帧立即进入新窗口计数
        this.lastRstFrameNano = ticker.nanoTime() - nanosPerWindow;
    }

    @Override
    public void onRstStreamRead(ChannelHandlerContext ctx, int streamId, long errorCode) throws Http2Exception {
        long currentNano = ticker.nanoTime();
        if (currentNano - lastRstFrameNano >= nanosPerWindow) {
            lastRstFrameNano = currentNano;
            receivedRstInWindow = 1;
        } else {
            receivedRstInWindow++;
            if (receivedRstInWindow > maxRstFramesPerWindow) {
                logger.debug("{} Maximum number {} of RST frames reached within {} seconds, " +
                                "closing connection with {} error", ctx.channel(), maxRstFramesPerWindow,
                        TimeUnit.NANOSECONDS.toSeconds(nanosPerWindow), RST_FRAME_RATE_EXCEEDED.error(),
                        RST_FRAME_RATE_EXCEEDED);
                throw RST_FRAME_RATE_EXCEEDED;
            }
        }
        super.onRstStreamRead(ctx, streamId, errorCode);
    }
}
