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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionEncoder;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionFilter;

import java.util.List;

import static io.netty.handler.codec.http.websocketx.extensions.compression.PerMessageDeflateDecoder.*;
import static io.netty.util.internal.ObjectUtil.*;

/**
 * WebSocket 帧载荷 Deflate 压缩抽象基类。
 * <p>使用 {@link EmbeddedChannel} 包装 zlib 编码器；子类控制 RSV1 置位、
 * 是否剥离 {@link PerMessageDeflateDecoder#FRAME_TAIL} 等 per-frame/per-message 细节。
 */
abstract class DeflateEncoder extends WebSocketExtensionEncoder {

    /** zlib 内部状态默认内存级别（1–9） */
    static final int DEFAULT_MEM_LEVEL = 8;

    /** Deflate 压缩级别 0–9 */
    private final int compressionLevel;
    /** zlib 窗口大小（位数 8–15） */
    private final int windowSize;
    /** zlib 内存级别 1–9 */
    private final int memLevel;
    /** 为 true 时每消息/帧结束后重置压缩上下文 */
    private final boolean noContext;
    /** 决定是否跳过当前帧压缩的过滤器 */
    private final WebSocketExtensionFilter extensionEncoderFilter;

    /** 懒创建的 zlib 压缩 EmbeddedChannel */
    private EmbeddedChannel encoder;

    /**
     * Constructor
     * @param compressionLevel 压缩级别 0–9
     * @param windowSize 压缩窗口位数
     * @param noContext 是否禁用上下文接管
     * @param extensionEncoderFilter 扩展编码过滤器
     */
    DeflateEncoder(int compressionLevel, int windowSize, boolean noContext,
                   WebSocketExtensionFilter extensionEncoderFilter) {
        this(compressionLevel, windowSize, DEFAULT_MEM_LEVEL, noContext, extensionEncoderFilter);
    }

    /**
     * Constructor
     * @param compressionLevel compression level of the compressor.
     * @param windowSize maximum size of the window compressor buffer.
     * @param memLevel zlib 内部状态内存级别（1–9）
     * @param noContext true to disable context takeover.
     * @param extensionEncoderFilter extension encoder filter.
     */
    DeflateEncoder(int compressionLevel, int windowSize, int memLevel, boolean noContext,
                   WebSocketExtensionFilter extensionEncoderFilter) {
        this.compressionLevel = compressionLevel;
        this.windowSize = windowSize;
        this.memLevel = memLevel;
        this.noContext = noContext;
        this.extensionEncoderFilter = checkNotNull(extensionEncoderFilter, "extensionEncoderFilter");
    }

    /**
     * Returns the extension encoder filter.
     */
    protected WebSocketExtensionFilter extensionEncoderFilter() {
        return extensionEncoderFilter;
    }

    /**
     * @param msg 当前 WebSocket 帧
     * @return 压缩后帧应设置的 RSV 位
     */
    protected abstract int rsv(WebSocketFrame msg);

    /**
     * @param msg 当前 WebSocket 帧
     * @return 为 true 时从压缩结果末尾移除 {@code FRAME_TAIL}
     */
    protected abstract boolean removeFrameTail(WebSocketFrame msg);

    @Override
    protected void encode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out) throws Exception {
        final ByteBuf compressedContent;
        if (msg.content().isReadable()) {
            compressedContent = compressContent(ctx, msg);
        } else if (msg.isFinalFragment()) {
            // 末帧空载荷时手动写入空 Deflate 块（RFC 7692 §7.2.3.6）
            // https://tools.ietf.org/html/rfc7692#section-7.2.3.6
            compressedContent = EMPTY_DEFLATE_BLOCK.duplicate();
        } else {
            throw new CodecException("cannot compress content buffer");
        }

        final WebSocketFrame outMsg;
        if (msg instanceof TextWebSocketFrame) {
            outMsg = new TextWebSocketFrame(msg.isFinalFragment(), rsv(msg), compressedContent);
        } else if (msg instanceof BinaryWebSocketFrame) {
            outMsg = new BinaryWebSocketFrame(msg.isFinalFragment(), rsv(msg), compressedContent);
        } else if (msg instanceof ContinuationWebSocketFrame) {
            outMsg = new ContinuationWebSocketFrame(msg.isFinalFragment(), rsv(msg), compressedContent);
        } else {
            throw new CodecException("unexpected frame type: " + msg.getClass().getName());
        }

        out.add(outMsg);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        cleanup();
        super.handlerRemoved(ctx);
    }

    private ByteBuf compressContent(ChannelHandlerContext ctx, WebSocketFrame msg) {
        if (encoder == null) {
            encoder = EmbeddedChannel.builder()
                    .handlers(ZlibCodecFactory.newZlibEncoder(
                            ZlibWrapper.NONE, compressionLevel, windowSize, memLevel))
                    .build();
        }

        encoder.writeOutbound(msg.content().retain());

        CompositeByteBuf fullCompressedContent = ctx.alloc().compositeBuffer();
        for (;;) {
            ByteBuf partCompressedContent = encoder.readOutbound();
            if (partCompressedContent == null) {
                break;
            }
            if (!partCompressedContent.isReadable()) {
                partCompressedContent.release();
                continue;
            }
            fullCompressedContent.addComponent(true, partCompressedContent);
        }

        if (fullCompressedContent.numComponents() <= 0) {
            fullCompressedContent.release();
            throw new CodecException("cannot read compressed buffer");
        }

        if (msg.isFinalFragment() && noContext) {
            cleanup();
        }

        ByteBuf compressedContent;
        if (removeFrameTail(msg)) {
            int realLength = fullCompressedContent.readableBytes() - FRAME_TAIL.readableBytes();
            compressedContent = fullCompressedContent.slice(0, realLength);
        } else {
            compressedContent = fullCompressedContent;
        }

        return compressedContent;
    }

    private void cleanup() {
        if (encoder != null) {
            // 释放未正确清理的 zlib 编码器资源
            encoder.finishAndReleaseAll();
            encoder = null;
        }
    }
}
