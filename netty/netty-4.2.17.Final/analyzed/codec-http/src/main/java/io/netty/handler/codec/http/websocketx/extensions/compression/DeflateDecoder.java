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
import io.netty.buffer.Unpooled;
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
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionDecoder;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionFilter;

import java.util.List;

import static io.netty.util.internal.ObjectUtil.*;

/**
 * WebSocket 帧载荷 Deflate 解压抽象基类。
 * <p>使用 {@link EmbeddedChannel} 包装 zlib 解码器；子类决定 RSV 位处理、
 * 是否在帧尾追加 {@link #FRAME_TAIL} 以及 per-frame/per-message 语义。
 */
abstract class DeflateDecoder extends WebSocketExtensionDecoder {

    /** RFC 7692 要求的 Deflate 块结束标记（0x00 0x00 0xff 0xff） */
    static final ByteBuf FRAME_TAIL = Unpooled.unreleasableBuffer(
            Unpooled.wrappedBuffer(new byte[] {0x00, 0x00, (byte) 0xff, (byte) 0xff}))
            .asReadOnly();

    /** 空 Deflate 块（单字节 0x00），用于零长度压缩帧 */
    static final ByteBuf EMPTY_DEFLATE_BLOCK = Unpooled.unreleasableBuffer(
            Unpooled.wrappedBuffer(new byte[] { 0x00 }))
            .asReadOnly();

    /** 为 true 时禁用上下文接管（每消息/帧结束后重置 zlib 状态） */
    private final boolean noContext;
    /** 决定是否跳过当前帧解压的过滤器 */
    private final WebSocketExtensionFilter extensionDecoderFilter;
    /** 解压缓冲区最大分配字节数，0 表示不限制 */
    private final int maxAllocation;

    /** 懒创建的 zlib 解压 EmbeddedChannel */
    private EmbeddedChannel decoder;

    /**
     * Constructor
     *
     * @param noContext 为 true 禁用上下文接管
     * @param extensionDecoderFilter 扩展解码过滤器
     * @param maxAllocation 解压缓冲上限
     */
    DeflateDecoder(boolean noContext, WebSocketExtensionFilter extensionDecoderFilter, int maxAllocation) {
        this.noContext = noContext;
        this.extensionDecoderFilter = checkNotNull(extensionDecoderFilter, "extensionDecoderFilter");
        this.maxAllocation = maxAllocation;
    }

    /** @return 当前使用的扩展解码过滤器 */

    protected WebSocketExtensionFilter extensionDecoderFilter() {
        return extensionDecoderFilter;
    }

    /** 子类决定是否在解压输入末尾追加 {@link #FRAME_TAIL} */
    protected abstract boolean appendFrameTail(WebSocketFrame msg);

    /** 子类计算解压后输出帧的 RSV 位（通常清除 RSV1） */
    protected abstract int newRsv(WebSocketFrame msg);

    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out) throws Exception {
        final ByteBuf decompressedContent = decompressContent(ctx, msg);

        final WebSocketFrame outMsg;
        if (msg instanceof TextWebSocketFrame) {
            outMsg = new TextWebSocketFrame(msg.isFinalFragment(), newRsv(msg), decompressedContent);
        } else if (msg instanceof BinaryWebSocketFrame) {
            outMsg = new BinaryWebSocketFrame(msg.isFinalFragment(), newRsv(msg), decompressedContent);
        } else if (msg instanceof ContinuationWebSocketFrame) {
            outMsg = new ContinuationWebSocketFrame(msg.isFinalFragment(), newRsv(msg), decompressedContent);
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

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        cleanup();
        super.channelInactive(ctx);
    }

    private ByteBuf decompressContent(ChannelHandlerContext ctx, WebSocketFrame msg) {
        if (decoder == null) {
            if (!(msg instanceof TextWebSocketFrame) && !(msg instanceof BinaryWebSocketFrame)) {
                throw new CodecException("unexpected initial frame type: " + msg.getClass().getName());
            }
            decoder = EmbeddedChannel.builder()
                    .handlers(ZlibCodecFactory.newZlibDecoder(ZlibWrapper.NONE, maxAllocation))
                    .build();
        }

        boolean readable = msg.content().isReadable();
        boolean emptyDeflateBlock = EMPTY_DEFLATE_BLOCK.equals(msg.content());

        decoder.writeInbound(msg.content().retain());
        if (appendFrameTail(msg)) {
            decoder.writeInbound(FRAME_TAIL.duplicate());
        }

        CompositeByteBuf compositeDecompressedContent = ctx.alloc().compositeBuffer();
        for (;;) {
            ByteBuf partUncompressedContent = decoder.readInbound();
            if (partUncompressedContent == null) {
                break;
            }
            if (!partUncompressedContent.isReadable()) {
                partUncompressedContent.release();
                continue;
            }
            compositeDecompressedContent.addComponent(true, partUncompressedContent);
        }
        // 正确处理空帧（见 netty#4348）
        // See https://github.com/netty/netty/issues/4348
        if (!emptyDeflateBlock && readable && compositeDecompressedContent.numComponents() <= 0) {
            // 分片消息末帧可能含不影响解压的残留数据
            // May contain left-over data that doesn't affect decompression
            if (!(msg instanceof ContinuationWebSocketFrame)) {
                compositeDecompressedContent.release();
                throw new CodecException("cannot read uncompressed buffer");
            }
        }

        if (msg.isFinalFragment() && noContext) {
            cleanup();
        }

        return compositeDecompressedContent;
    }

    private void cleanup() {
        if (decoder != null) {
            // 释放未正确清理的 zlib 解码器资源
            decoder.finishAndReleaseAll();
            decoder = null;
        }
    }
}
