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
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.TypeParameterMatcher;


/**
 * {@link ChannelOutboundHandlerAdapter} which encodes message in a stream-like fashion from one message to an
 * {@link ByteBuf}.
 *
 *
 * Example implementation which encodes {@link Integer}s to a {@link ByteBuf}.
 *
 * <pre>
 *     public class IntegerEncoder extends {@link MessageToByteEncoder}&lt;{@link Integer}&gt; {
 *         {@code @Override}
 *         public void encode({@link ChannelHandlerContext} ctx, {@link Integer} msg, {@link ByteBuf} out)
 *                 throws {@link Exception} {
 *             out.writeInt(msg);
 *         }
 *     }
 * </pre>
 */
public abstract class MessageToByteEncoder<I> extends ChannelOutboundHandlerAdapter {

    /** 出站消息类型匹配器。 */
    /** 出站消息类型匹配器。 */
    private final TypeParameterMatcher matcher;
    /** 是否优先使用直接缓冲区编码。 */
    /** 是否优先使用直接缓冲区编码。 */
    private final boolean preferDirect;

    /**
     * 等价于 {@link #MessageToByteEncoder(boolean)}({@code true})。
     */
    protected MessageToByteEncoder() {
        this(true);
    }

    /**
     * 等价于 {@link #MessageToByteEncoder(Class, boolean)}(..., {@code true})。
     */
    protected MessageToByteEncoder(Class<? extends I> outboundMessageType) {
        this(outboundMessageType, true);
    }

    /**
     * 创建实例，并从泛型参数推断要匹配的消息类型。
     *
      * @param preferDirect 为 {@code true} 时优先分配直接 {@link ByteBuf}；否则分配堆 {@link ByteBuf}。
     */
    protected MessageToByteEncoder(boolean preferDirect) {
        matcher = TypeParameterMatcher.find(this, MessageToByteEncoder.class, "I");
        this.preferDirect = preferDirect;
    }

    /**
     * 创建新实例
     *
      * @param outboundMessageType   要匹配的消息类型
      * @param preferDirect 为 {@code true} 时优先分配直接 {@link ByteBuf}；否则分配堆 {@link ByteBuf}。
     */
    protected MessageToByteEncoder(Class<? extends I> outboundMessageType, boolean preferDirect) {
        matcher = TypeParameterMatcher.get(outboundMessageType);
        this.preferDirect = preferDirect;
    }

    /**
     * 判断给定出站消息是否应由本编码器处理。 为 {@code false} 时转交 {@link ChannelPipeline} 中下一个 {@link ChannelOutboundHandler}。
     */
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        return matcher.match(msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ByteBuf buf = null;
        try {
            if (acceptOutboundMessage(msg)) {
                @SuppressWarnings("unchecked")
                I cast = (I) msg;
                buf = allocateBuffer(ctx, cast, preferDirect);
                try {
                    encode(ctx, cast, buf);
                } finally {
                    ReferenceCountUtil.release(cast);
                }

                if (buf.isReadable()) {
                    ctx.write(buf, promise);
                } else {
                    buf.release();
                    ctx.write(Unpooled.EMPTY_BUFFER, promise);
                }
                buf = null;
            } else {
                ctx.write(msg, promise);
            }
        } catch (EncoderException e) {
            throw e;
        } catch (Throwable e) {
            throw new EncoderException(e);
        } finally {
            if (buf != null) {
                buf.release();
            }
        }
    }

    /**
     * 分配供 {@link #encode(ChannelHandlerContext, I, ByteBuf)} 使用的 {@link ByteBuf}。
     * Sub-classes may override this method to return {@link ByteBuf} with a perfect matching {@code initialCapacity}.
     */
    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, @SuppressWarnings("unused") I msg,
                               boolean preferDirect) throws Exception {
        if (preferDirect) {
            return ctx.alloc().ioBuffer();
        } else {
            return ctx.alloc().heapBuffer();
        }
    }

    /**
     * 将消息编码写入 {@link ByteBuf}。 This method will be called for each written message that can be handled
     * by this encoder.
     *
      * @param ctx            {@link ChannelHandlerContext} which this {@link MessageToByteEncoder} belongs to
      * @param msg           待编码消息
      * @param out           写入编码结果的 {@link ByteBuf}
      * @throws Exception    发生错误时抛出
     */
    protected abstract void encode(ChannelHandlerContext ctx, I msg, ByteBuf out) throws Exception;

    protected boolean isPreferDirect() {
        return preferDirect;
    }
}
