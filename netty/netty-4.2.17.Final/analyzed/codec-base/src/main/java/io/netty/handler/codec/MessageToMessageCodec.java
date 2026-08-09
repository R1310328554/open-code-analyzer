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

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCounted;
import io.netty.util.internal.TypeParameterMatcher;

import java.util.List;

/**
 * 消息到消息的双向编解码器，可在运行时对消息做即时转换。
 * <p>
 * 可视为 {@link MessageToMessageDecoder} 与 {@link MessageToMessageEncoder} 的组合。
 *
 * Here is an example of a {@link MessageToMessageCodec} which just decode from {@link Integer} to {@link Long}
 * and encode from {@link Long} to {@link Integer}.
 *
 * <pre>
 *     public class NumberCodec extends
 *             {@link MessageToMessageCodec}&lt;{@link Integer}, {@link Long}&gt; {
 *         {@code @Override}
 *         public {@link Long} decode({@link ChannelHandlerContext} ctx, {@link Integer} msg, List&lt;Object&gt; out)
 *                 throws {@link Exception} {
 *             out.add(msg.longValue());
 *         }
 *
 *         {@code @Override}
 *         public {@link Integer} encode({@link ChannelHandlerContext} ctx, {@link Long} msg, List&lt;Object&gt; out)
 *                 throws {@link Exception} {
 *             out.add(msg.intValue());
 *         }
 *     }
 * </pre>
 *
 * <p>
 * 若透传 {@link ReferenceCounted} 消息，须先 {@link ReferenceCounted#retain()}，
 * 因本类会对已编/解码消息调用 {@link ReferenceCounted#release()}。
 */
public abstract class MessageToMessageCodec<INBOUND_IN, OUTBOUND_IN> extends ChannelDuplexHandler {

    /** 委托入站解码逻辑的内部解码器。 */
    private final MessageToMessageDecoder<Object> decoder = new MessageToMessageDecoder<Object>(Object.class) {

        @Override
        public boolean acceptInboundMessage(Object msg) throws Exception {
            return MessageToMessageCodec.this.acceptInboundMessage(msg);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void decode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
            MessageToMessageCodec.this.decode(ctx, (INBOUND_IN) msg, out);
        }

        @Override
        public boolean isSharable() {
            return MessageToMessageCodec.this.isSharable();
        }
    };
    /** 委托出站编码逻辑的内部编码器。 */
    private final MessageToMessageEncoder<Object> encoder = new MessageToMessageEncoder<Object>(Object.class) {

        @Override
        public boolean acceptOutboundMessage(Object msg) throws Exception {
            return MessageToMessageCodec.this.acceptOutboundMessage(msg);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
            MessageToMessageCodec.this.encode(ctx, (OUTBOUND_IN) msg, out);
        }

        @Override
        public boolean isSharable() {
            return MessageToMessageCodec.this.isSharable();
        }
    };

    /** 入站消息类型匹配器。 */
    private final TypeParameterMatcher inboundMsgMatcher;
    /** 出站消息类型匹配器。 */
    private final TypeParameterMatcher outboundMsgMatcher;

    /** 从泛型参数推断入站/出站消息类型并创建实例。 */
    protected MessageToMessageCodec() {
        inboundMsgMatcher = TypeParameterMatcher.find(this, MessageToMessageCodec.class, "INBOUND_IN");
        outboundMsgMatcher = TypeParameterMatcher.find(this, MessageToMessageCodec.class, "OUTBOUND_IN");
    }

    /**
     * 创建新实例。
     *
     * @param inboundMessageType    待解码的入站消息类型
     * @param outboundMessageType   待编码的出站消息类型
     */
    protected MessageToMessageCodec(
            Class<? extends INBOUND_IN> inboundMessageType, Class<? extends OUTBOUND_IN> outboundMessageType) {
        inboundMsgMatcher = TypeParameterMatcher.get(inboundMessageType);
        outboundMsgMatcher = TypeParameterMatcher.get(outboundMessageType);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        decoder.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        decoder.channelReadComplete(ctx);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        encoder.write(ctx, msg, promise);
    }

    /**
     * 判断指定消息是否可由本 codec 解码。
     *
     * @param msg 待判断的消息
     */
    public boolean acceptInboundMessage(Object msg) throws Exception {
        return inboundMsgMatcher.match(msg);
    }

    /**
     * 判断指定消息是否可由本 codec 编码。
     *
     * @param msg 待判断的消息
     */
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        return outboundMsgMatcher.match(msg);
    }

    /** @see MessageToMessageEncoder#encode(ChannelHandlerContext, Object, List) 子类实现出站编码 */
    protected abstract void encode(ChannelHandlerContext ctx, OUTBOUND_IN msg, List<Object> out)
            throws Exception;

    /** @see MessageToMessageDecoder#decode(ChannelHandlerContext, Object, List) 子类实现入站解码 */
    protected abstract void decode(ChannelHandlerContext ctx, INBOUND_IN msg, List<Object> out)
            throws Exception;
}
