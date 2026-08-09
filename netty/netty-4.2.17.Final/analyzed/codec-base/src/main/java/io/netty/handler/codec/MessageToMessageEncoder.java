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
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import io.netty.util.concurrent.PromiseCombiner;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.TypeParameterMatcher;

import java.util.List;

/**
 * 将一种消息编码为另一种消息的 {@link ChannelOutboundHandlerAdapter}。
 *
 * For example here is an implementation which decodes an {@link Integer} to an {@link String}.
 *
 * <pre>
 *     public class IntegerToStringEncoder extends
 *             {@link MessageToMessageEncoder}&lt;{@link Integer}&gt; {
 *
 *         {@code @Override}
 *         public void encode({@link ChannelHandlerContext} ctx, {@link Integer} message, List&lt;Object&gt; out)
 *                 throws {@link Exception} {
 *             out.add(message.toString());
 *         }
 *     }
 * </pre>
 *
 * <p>
 * 透传 {@link ReferenceCounted} 消息时须 {@link ReferenceCounted#retain()}，
 * 因编码后会 {@link ReferenceCounted#release()} 原消息。
 */
public abstract class MessageToMessageEncoder<I> extends ChannelOutboundHandlerAdapter {

    /** 出站消息类型匹配器。 */
    private final TypeParameterMatcher matcher;

    /** 从泛型参数推断待匹配出站消息类型。 */
    protected MessageToMessageEncoder() {
        matcher = TypeParameterMatcher.find(this, MessageToMessageEncoder.class, "I");
    }

    /**
     * 创建新实例。
     *
     * @param outboundMessageType   待匹配并编码的出站消息类型
     */
    protected MessageToMessageEncoder(Class<? extends I> outboundMessageType) {
        matcher = TypeParameterMatcher.get(outboundMessageType);
    }

    /**
     * 返回 {@code true} 表示由本编码器处理；否则交给下一个 {@link ChannelOutboundHandler}。
     */
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        return matcher.match(msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        CodecOutputList out = null;
        try {
            if (acceptOutboundMessage(msg)) {
                out = CodecOutputList.newInstance();
                @SuppressWarnings("unchecked")
                I cast = (I) msg;
                try {
                    encode(ctx, cast, out);
                } catch (Throwable th) {
                    ReferenceCountUtil.safeRelease(cast);
                    PlatformDependent.throwException(th);
                }
                ReferenceCountUtil.release(cast);

                if (out.isEmpty()) {
                    throw new EncoderException(
                            StringUtil.simpleClassName(this) + " must produce at least one message.");
                }
            } else {
                ctx.write(msg, promise);
            }
        } catch (EncoderException e) {
            throw e;
        } catch (Throwable t) {
            throw new EncoderException(t);
        } finally {
            if (out != null) {
                try {
                    final int sizeMinusOne = out.size() - 1;
                    if (sizeMinusOne == 0) {
                        ctx.write(out.getUnsafe(0), promise);
                    } else if (sizeMinusOne > 0) {
                        // 多条出站写时可用 voidPromise 降低 GC 压力（见 issue #2525）
                        if (promise == ctx.voidPromise()) {
                            writeVoidPromise(ctx, out);
                        } else {
                            writePromiseCombiner(ctx, out, promise);
                        }
                    }
                } finally {
                    out.recycle();
                }
            }
        }
    }

    private static void writeVoidPromise(ChannelHandlerContext ctx, CodecOutputList out) {
        final ChannelPromise voidPromise = ctx.voidPromise();
        for (int i = 0; i < out.size(); i++) {
            ctx.write(out.getUnsafe(i), voidPromise);
        }
    }

    private static void writePromiseCombiner(ChannelHandlerContext ctx, CodecOutputList out, ChannelPromise promise) {
        final PromiseCombiner combiner = new PromiseCombiner(ctx.executor());
        for (int i = 0; i < out.size(); i++) {
            combiner.add(ctx.write(out.getUnsafe(i)));
        }
        combiner.finish(promise);
    }

    /**
     * 将出站消息编码为零条或多条下游消息（至少一条，否则抛 {@link EncoderException}）。
     *
     * @param ctx           本 {@link MessageToMessageEncoder} 所属的 {@link ChannelHandlerContext}
     * @param msg           待编码消息
     * @param out           编码结果写入此 {@link List}
     * @throws Exception    编码出错时抛出
     */
    protected abstract void encode(ChannelHandlerContext ctx, I msg, List<Object> out) throws Exception;
}
