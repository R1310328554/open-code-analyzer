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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtension;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionFilter;

import java.util.List;

/**
 * permessage-deflate 压缩编码器：整条逻辑消息（含分片）共享一次 Deflate 流。
 * <p>首帧 Text/Binary 置 RSV1 启动压缩，Continuation 帧延续同一 zlib 上下文；
 * 仅在消息末帧（FIN）移除 {@link DeflateEncoder} 的帧尾标记。
 */
class PerMessageDeflateEncoder extends DeflateEncoder {

    /** 是否处于一条多帧压缩消息的编码过程中 */
    private boolean compressing;

    /**
     * 构造压缩器（默认不过滤任何帧）。
     *
     * @param compressionLevel compression level of the compressor.
     * @param windowSize maximum size of the window compressor buffer.
     * @param noContext true to disable context takeover.
     */
    PerMessageDeflateEncoder(int compressionLevel, int windowSize, boolean noContext) {
        super(compressionLevel, windowSize, noContext, WebSocketExtensionFilter.NEVER_SKIP);
    }

    /**
     * 构造压缩器，可指定扩展过滤器以跳过特定帧。
     *
     * @param compressionLevel compression level of the compressor.
     * @param windowSize maximum size of the window compressor buffer.
     * @param noContext true to disable context takeover.
     * @param extensionEncoderFilter extension filter for per message deflate encoder.
     */
    PerMessageDeflateEncoder(int compressionLevel, int windowSize, boolean noContext,
                             WebSocketExtensionFilter extensionEncoderFilter) {
        super(compressionLevel, windowSize, noContext, extensionEncoderFilter);
    }

    /**
     * 构造压缩器，可指定 zlib 内存级别与扩展过滤器。
     *
     * @param compressionLevel compression level of the compressor.
     * @param windowSize maximum size of the window compressor buffer.
     * @param memLevel internal compression state memory level (1..9).
     * @param noContext true to disable context takeover.
     * @param extensionEncoderFilter extension filter for per message deflate encoder.
     */
    PerMessageDeflateEncoder(int compressionLevel, int windowSize, int memLevel, boolean noContext,
                             WebSocketExtensionFilter extensionEncoderFilter) {
        super(compressionLevel, windowSize, memLevel, noContext, extensionEncoderFilter);
    }

    @Override
    /** 接受未压缩的首帧或压缩进行中的 Continuation；filter 跳过且进行中则抛异常 */
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        if (!super.acceptOutboundMessage(msg)) {
            return false;
        }

        WebSocketFrame wsFrame = (WebSocketFrame) msg;
        if (extensionEncoderFilter().mustSkip(wsFrame)) {
            if (compressing) {
                throw new IllegalStateException("Cannot skip per message deflate encoder, compression in progress");
            }
            return false;
        }

        return ((wsFrame instanceof TextWebSocketFrame || wsFrame instanceof BinaryWebSocketFrame) &&
                (wsFrame.rsv() & WebSocketExtension.RSV1) == 0) ||
               (wsFrame instanceof ContinuationWebSocketFrame && compressing);
    }

    @Override
    /** 首帧 Text/Binary 置 RSV1 标记压缩，Continuation 保持原 RSV */
    protected int rsv(WebSocketFrame msg) {
        return msg instanceof TextWebSocketFrame || msg instanceof BinaryWebSocketFrame?
                msg.rsv() | WebSocketExtension.RSV1 : msg.rsv();
    }

    @Override
    /** 仅在消息末帧（FIN）移除 Deflate 帧尾 */
    protected boolean removeFrameTail(WebSocketFrame msg) {
        return msg.isFinalFragment();
    }

    @Override
    /** 编码后更新 compressing 状态：末帧清零，首帧置位 */
    protected void encode(ChannelHandlerContext ctx, WebSocketFrame msg,
                          List<Object> out) throws Exception {
        super.encode(ctx, msg, out);

        if (msg.isFinalFragment()) {
            compressing = false;
        } else if (msg instanceof TextWebSocketFrame || msg instanceof BinaryWebSocketFrame) {
            compressing = true;
        }
    }

}
