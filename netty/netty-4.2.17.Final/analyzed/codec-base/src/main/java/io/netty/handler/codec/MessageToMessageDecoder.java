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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import io.netty.util.internal.TypeParameterMatcher;

import java.util.List;

/**
 * 将一种消息解码为另一种消息的 {@link ChannelInboundHandlerAdapter}。
 *
 *
 * For example here is an implementation which decodes a {@link String} to an {@link Integer} which represent
 * the length of the {@link String}.
 *
 * <pre>
 *     public class StringToIntegerDecoder extends
 *             {@link MessageToMessageDecoder}&lt;{@link String}&gt; {
 *
 *         {@code @Override}
 *         public void decode({@link ChannelHandlerContext} ctx, {@link String} message,
 *                            List&lt;Object&gt; out) throws {@link Exception} {
 *             out.add(message.length());
 *         }
 *     }
 * </pre>
 *
 * <p>
 * 透传 {@link ReferenceCounted} 消息时须 {@link ReferenceCounted#retain()}，
 * 因解码后会 {@link ReferenceCounted#release()} 原消息。
 */
public abstract class MessageToMessageDecoder<I> extends ChannelInboundHandlerAdapter {

    /** 入站消息类型匹配器。 */
    private final TypeParameterMatcher matcher;
    /** 本轮 {@link #channelRead} 是否已调用 decode。 */
    private boolean decodeCalled;
    /** 本轮是否已向下游 fire 至少一条消息。 */
    private boolean messageProduced;

    /** 从泛型参数推断待匹配入站消息类型。 */
    protected MessageToMessageDecoder() {
        matcher = TypeParameterMatcher.find(this, MessageToMessageDecoder.class, "I");
    }

    /**
     * 创建新实例。
     *
     * @param inboundMessageType    待匹配并解码的入站消息类型
     */
    protected MessageToMessageDecoder(Class<? extends I> inboundMessageType) {
        matcher = TypeParameterMatcher.get(inboundMessageType);
    }

    /**
     * 返回 {@code true} 表示由本解码器处理；否则交给 {@link ChannelPipeline} 中下一个
     * {@link ChannelInboundHandler}。
     */
    public boolean acceptInboundMessage(Object msg) throws Exception {
        return matcher.match(msg);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        decodeCalled = true;
        CodecOutputList out = CodecOutputList.newInstance();
        try {
            if (acceptInboundMessage(msg)) {
                @SuppressWarnings("unchecked")
                I cast = (I) msg;
                try {
                    decode(ctx, cast, out);
                } finally {
                    // 解码完成后释放入站消息引用
                    ReferenceCountUtil.release(cast);
                }
            } else {
                out.add(msg);
            }
        } catch (DecoderException e) {
            throw e;
        } catch (Exception e) {
            throw new DecoderException(e);
        } finally {
            try {
                int size = out.size();
                messageProduced |= size > 0;
                for (int i = 0; i < size; i++) {
                    ctx.fireChannelRead(out.getUnsafe(i));
                }
            } finally {
                out.recycle();
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if (!isSharable()) {
            // 非 sharable 时才用局部状态触发 read，避免共享实例竞态
            if (decodeCalled && !messageProduced && !ctx.channel().config().isAutoRead()) {
                ctx.read();
            }
            decodeCalled = false;
            messageProduced = false;
        }
        ctx.fireChannelReadComplete();
    }

    /**
     * 将入站消息解码为零条或多条下游消息。
     *
     * @param ctx           本 {@link MessageToMessageDecoder} 所属的 {@link ChannelHandlerContext}
     * @param msg           待解码消息
     * @param out           解码结果写入此 {@link List}
     * @throws Exception    解码出错时抛出
     */
    protected abstract void decode(ChannelHandlerContext ctx, I msg, List<Object> out) throws Exception;
}
