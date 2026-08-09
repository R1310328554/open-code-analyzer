/*
 * Copyright 2012 The Netty Project
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
package io.netty.handler.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.internal.TypeParameterMatcher;

import java.util.List;

/**
 * 字节流与消息双向编解码的复合 {@link ChannelDuplexHandler}，
 * 等价于 {@link ByteToMessageDecoder} 与 {@link MessageToByteEncoder} 的组合。
 * <p>
 * 子类<strong>不得</strong>标注 {@link @Sharable}。
 */
public abstract class ByteToMessageCodec<I> extends ChannelDuplexHandler {

    private final TypeParameterMatcher outboundMsgMatcher;
    private final MessageToByteEncoder<I> encoder;

    private final ByteToMessageDecoder decoder = new ByteToMessageDecoder() {
        @Override
        public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            ByteToMessageCodec.this.decode(ctx, in, out);
        }

        @Override
        protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            ByteToMessageCodec.this.decodeLast(ctx, in, out);
        }
    };

    /** 等价于 {@link #ByteToMessageCodec(boolean)}({@code true})。 */
    protected ByteToMessageCodec() {
        this(true);
    }

    /** 等价于 {@link #ByteToMessageCodec(Class, boolean)}(..., {@code true})。 */
    protected ByteToMessageCodec(Class<? extends I> outboundMessageType) {
        this(outboundMessageType, true);
    }

    /**
     * 从泛型参数推断出站消息类型。
     * @param preferDirect {@code true} 优先使用直接 {@link ByteBuf} 作为编码目标；
     *                     {@code false} 则分配堆 {@link ByteBuf}。
     */
    protected ByteToMessageCodec(boolean preferDirect) {
        ensureNotSharable();
        outboundMsgMatcher = TypeParameterMatcher.find(this, ByteToMessageCodec.class, "I");
        encoder = new Encoder(preferDirect);
    }

    /**
     * @param outboundMessageType 出站消息类型
     * @param preferDirect 同 {@link #ByteToMessageCodec(boolean)}
     */
    protected ByteToMessageCodec(Class<? extends I> outboundMessageType, boolean preferDirect) {
        ensureNotSharable();
        outboundMsgMatcher = TypeParameterMatcher.get(outboundMessageType);
        encoder = new Encoder(preferDirect, outboundMessageType);
    }

    /** @param msg 待编码消息；{@code true} 表示本 codec 可处理 */
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        return outboundMsgMatcher.match(msg);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        decoder.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        encoder.write(ctx, msg, promise);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        decoder.channelReadComplete(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        decoder.channelInactive(ctx);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        try {
            decoder.handlerAdded(ctx);
        } finally {
            encoder.handlerAdded(ctx);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        try {
            decoder.handlerRemoved(ctx);
        } finally {
            encoder.handlerRemoved(ctx);
        }
    }

    /** @see MessageToByteEncoder#encode(ChannelHandlerContext, Object, ByteBuf) 子类实现编码逻辑 */
    protected abstract void encode(ChannelHandlerContext ctx, I msg, ByteBuf out) throws Exception;

    /** @see ByteToMessageDecoder#decode(ChannelHandlerContext, ByteBuf, List) 子类实现解码逻辑 */
    protected abstract void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception;

    /** @see ByteToMessageDecoder#decodeLast(ChannelHandlerContext, ByteBuf, List) 通道关闭时的收尾解码 */
    protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.isReadable()) {
            // 缓冲仍有可读字节时才 decode；见 issue #4386
            decode(ctx, in, out);
        }
    }

    private final class Encoder extends MessageToByteEncoder<I> {
        Encoder(boolean preferDirect) {
            super(preferDirect);
        }

        Encoder(boolean preferDirect, Class<? extends I> outboundMessageType) {
            super(outboundMessageType, preferDirect);
        }

        @Override
        public boolean acceptOutboundMessage(Object msg) throws Exception {
            return ByteToMessageCodec.this.acceptOutboundMessage(msg);
        }

        @Override
        protected void encode(ChannelHandlerContext ctx, I msg, ByteBuf out) throws Exception {
            ByteToMessageCodec.this.encode(ctx, msg, out);
        }
    }
}
