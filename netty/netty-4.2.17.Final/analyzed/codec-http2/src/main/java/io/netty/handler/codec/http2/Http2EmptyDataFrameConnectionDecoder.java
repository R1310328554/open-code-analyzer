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

import io.netty.util.internal.ObjectUtil;

/**
 * 装饰 {@link Http2ConnectionDecoder}，限制连续空 {@code DATA} 帧（无 {@code END_STREAM}）的数量，
 * 超出阈值则以 {@link Http2Error#ENHANCE_YOUR_CALM} 关闭连接，用于缓解空帧 DoS。
 */
final class Http2EmptyDataFrameConnectionDecoder extends DecoratingHttp2ConnectionDecoder {

    /** 允许连续接收的空 DATA 帧上限（不含 end_of_stream）。 */
    private final int maxConsecutiveEmptyFrames;

    Http2EmptyDataFrameConnectionDecoder(Http2ConnectionDecoder delegate, int maxConsecutiveEmptyFrames) {
        super(delegate);
        this.maxConsecutiveEmptyFrames = ObjectUtil.checkPositive(
                maxConsecutiveEmptyFrames, "maxConsecutiveEmptyFrames");
    }

    @Override
    public void frameListener(Http2FrameListener listener) {
        if (listener != null) {
            // 在真实 listener 外包一层空帧计数装饰器
            super.frameListener(new Http2EmptyDataFrameListener(listener, maxConsecutiveEmptyFrames));
        } else {
            super.frameListener(null);
        }
    }

    @Override
    public Http2FrameListener frameListener() {
        Http2FrameListener frameListener = frameListener0();
        // 对外暴露用户设置的原始 listener，而非内部包装器
        if (frameListener instanceof Http2EmptyDataFrameListener) {
            return ((Http2EmptyDataFrameListener) frameListener).listener;
        }
        return frameListener;
    }

    // Package-private for testing
    Http2FrameListener frameListener0() {
        return super.frameListener();
    }
}
