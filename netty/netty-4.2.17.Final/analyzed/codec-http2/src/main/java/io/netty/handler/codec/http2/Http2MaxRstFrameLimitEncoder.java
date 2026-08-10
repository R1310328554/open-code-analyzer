/*
 * Copyright 2025 The Netty Project
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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Ticker;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * 出站 RST 帧速率限制装饰器：统计本端在窗口内发送的 {@code RST_STREAM} 帧，
 * 防止因对端恶意行为导致本端无节制发送 RST，达到上限后主动拆连以降低 DDoS 风险。
 */
final class Http2MaxRstFrameLimitEncoder extends DecoratingHttp2ConnectionEncoder {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(Http2MaxRstFrameLimitEncoder.class);

    /** 统计窗口长度（纳秒） */
    private final long nanosPerWindow;
    /** 每个窗口内允许发送的最大 RST 帧数 */
    private final int maxRstFramesPerWindow;
    private final Ticker ticker;
    /** 当前窗口起始时刻（纳秒） */
    private long lastRstFrameNano;
    /** 当前窗口内已发送的 RST 帧计数 */
    private int sendRstInWindow;
    private Http2LifecycleManager lifecycleManager;

    Http2MaxRstFrameLimitEncoder(Http2ConnectionEncoder delegate, int maxRstFramesPerWindow, int secondsPerWindow) {
        this(delegate, maxRstFramesPerWindow, secondsPerWindow, Ticker.systemTicker());
    }

    Http2MaxRstFrameLimitEncoder(Http2ConnectionEncoder delegate, int maxRstFramesPerWindow, int secondsPerWindow,
                                 Ticker ticker) {
        super(delegate);
        this.maxRstFramesPerWindow = maxRstFramesPerWindow;
        this.nanosPerWindow = TimeUnit.SECONDS.toNanos(secondsPerWindow);
        this.ticker = ticker;
        lastRstFrameNano = ticker.nanoTime();
    }

    @Override
    public void lifecycleManager(Http2LifecycleManager lifecycleManager) {
        this.lifecycleManager = lifecycleManager;
        super.lifecycleManager(lifecycleManager);
    }

    @Override
    public ChannelFuture writeRstStream(ChannelHandlerContext ctx, int streamId, long errorCode,
                                        ChannelPromise promise) {
        ChannelFuture future = super.writeRstStream(ctx, streamId, errorCode, promise);
        if (countRstFrameErrorCode(errorCode)) {
            long currentNano = ticker.nanoTime();
            if (currentNano - lastRstFrameNano >= nanosPerWindow) {
                // 窗口过期，重置计数
                lastRstFrameNano = currentNano;
                sendRstInWindow = 1;
            } else {
                sendRstInWindow++;
                if (sendRstInWindow > maxRstFramesPerWindow) {
                    Http2Exception exception = Http2Exception.connectionError(Http2Error.ENHANCE_YOUR_CALM,
                            "Maximum number %d of RST frames frames reached within %d seconds", maxRstFramesPerWindow,
                            TimeUnit.NANOSECONDS.toSeconds(nanosPerWindow));

                    logger.debug("{} Maximum number {} of RST frames reached within {} seconds, " +
                                    "closing connection with {} error", ctx.channel(), maxRstFramesPerWindow,
                            TimeUnit.NANOSECONDS.toSeconds(nanosPerWindow), exception.error(),
                            exception);
                    // 先通知生命周期管理器，再关闭连接
                    lifecycleManager.onError(ctx, true, exception);
                    ctx.close();
                }
            }
        }

        return future;
    }

    /**
     * 判断该 errorCode 是否计入 RST 速率；{@code CANCEL} 与 {@code NO_ERROR} 为正常关闭语义，不计入。
     */
    private boolean countRstFrameErrorCode(long errorCode) {
        return errorCode != Http2Error.CANCEL.code() && errorCode != Http2Error.NO_ERROR.code();
    }
}
