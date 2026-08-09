/*
 * Copyright 2014 The Netty Project
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
package io.netty.handler.codec.http.websocketx.extensions.compression;

import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtension;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionFilter;

/**
 * perframe-deflate 压缩器：对每个可压缩数据帧独立 Deflate 并设置 RSV1。
 * <p>仅压缩 RSV1 未置位且载荷非空的 Text/Binary/Continuation 帧。
 */
class PerFrameDeflateEncoder extends DeflateEncoder {

    /**
     * Constructor
     *
     * @param compressionLevel 压缩级别
     * @param windowSize 压缩窗口位数
     * @param noContext 是否禁用上下文接管
     */
    PerFrameDeflateEncoder(int compressionLevel, int windowSize, boolean noContext) {
        super(compressionLevel, windowSize, noContext, WebSocketExtensionFilter.NEVER_SKIP);
    }

    /**
     * Constructor
     *
     * @param compressionLevel compression level of the compressor.
     * @param windowSize       maximum size of the window compressor buffer.
     * @param noContext        true to disable context takeover.
     * @param extensionEncoderFilter perframe 编码扩展过滤器
     */
    PerFrameDeflateEncoder(int compressionLevel, int windowSize, boolean noContext,
                           WebSocketExtensionFilter extensionEncoderFilter) {
        super(compressionLevel, windowSize, noContext, extensionEncoderFilter);
    }

    /**
     * Constructor
     *
     * @param compressionLevel compression level of the compressor.
     * @param windowSize       maximum size of the window compressor buffer.
     * @param memLevel zlib 内存级别 1–9
     * @param noContext        true to disable context takeover.
     * @param extensionEncoderFilter extension encoder filter for per frame deflate encoder.
     */
    PerFrameDeflateEncoder(int compressionLevel, int windowSize, int memLevel, boolean noContext,
                           WebSocketExtensionFilter extensionEncoderFilter) {
        super(compressionLevel, windowSize, memLevel, noContext, extensionEncoderFilter);
    }

    @Override
    /** 压缩 RSV1 未置位、载荷可读且未被 filter 跳过的数据帧 */
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        if (!super.acceptOutboundMessage(msg)) {
            return false;
        }

        WebSocketFrame wsFrame = (WebSocketFrame) msg;
        if (extensionEncoderFilter().mustSkip(wsFrame)) {
            return false;
        }

        return (msg instanceof TextWebSocketFrame || msg instanceof BinaryWebSocketFrame ||
                msg instanceof ContinuationWebSocketFrame) &&
               wsFrame.content().readableBytes() > 0 &&
               (wsFrame.rsv() & WebSocketExtension.RSV1) == 0;
    }

    @Override
    /** 压缩后在 RSV 中置位 RSV1 */
    protected int rsv(WebSocketFrame msg) {
        return msg.rsv() | WebSocketExtension.RSV1;
    }

    @Override
    /** perframe 模式始终从压缩结果移除 zlib 同步尾 */
    protected boolean removeFrameTail(WebSocketFrame msg) {
        return true;
    }

}
