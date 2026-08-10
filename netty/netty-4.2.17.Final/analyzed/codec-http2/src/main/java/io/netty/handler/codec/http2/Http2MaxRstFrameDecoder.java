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

import io.netty.util.concurrent.Ticker;

import static io.netty.util.internal.ObjectUtil.checkPositive;


/**
 * 入站 RST 帧速率限制装饰器：在滑动时间窗口内统计收到的 {@code RST_STREAM} 帧数量，
 * 超出阈值时以 {@code GO_AWAY}（{@link Http2Error#ENHANCE_YOUR_CALM}）关闭连接，抵御 RST 洪泛攻击。
 */
final class Http2MaxRstFrameDecoder extends DecoratingHttp2ConnectionDecoder {
    /** 每个时间窗口内允许的最大 RST 帧数 */
    private final int maxRstFramesPerWindow;
    /** 统计窗口长度（秒） */
    private final int secondsPerWindow;

    Http2MaxRstFrameDecoder(Http2ConnectionDecoder delegate, int maxRstFramesPerWindow, int secondsPerWindow) {
        super(delegate);
        this.maxRstFramesPerWindow = checkPositive(maxRstFramesPerWindow, "maxRstFramesPerWindow");
        this.secondsPerWindow = checkPositive(secondsPerWindow, "secondsPerWindow");
    }

    @Override
    public void frameListener(Http2FrameListener listener) {
        if (listener != null) {
            // 在真实 listener 外包一层速率计数器，对调用方透明
            super.frameListener(new Http2MaxRstFrameListener(
                    listener, maxRstFramesPerWindow, secondsPerWindow, Ticker.systemTicker()));
        } else {
            super.frameListener(null);
        }
    }

    @Override
    public Http2FrameListener frameListener() {
        Http2FrameListener frameListener = frameListener0();
        // 对外返回原始 listener，隐藏内部包装层
        if (frameListener instanceof Http2MaxRstFrameListener) {
            return ((Http2MaxRstFrameListener) frameListener).listener;
        }
        return frameListener;
    }

    // Package-private for testing
    Http2FrameListener frameListener0() {
        return super.frameListener();
    }
}
