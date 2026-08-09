/*
 * Copyright 2016 The Netty Project
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
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.internal.StringUtil;
import static io.netty.util.internal.ObjectUtil.checkNotNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

/**
 * 将 {@link AddressedEnvelope} 中的消息经指定编码器转为 {@link ByteBuf}，
 * 再封装为带收发地址的 {@link DatagramPacket}。
 * <p>
 * UDP 无序且易分片，编码后载荷应小于路径 MTU 安全上限。
 * @param <M> 信封内待编码的消息类型
 */
public class DatagramPacketEncoder<M> extends MessageToMessageEncoder<AddressedEnvelope<M, InetSocketAddress>> {

    private final MessageToMessageEncoder<? super M> encoder;

    /** @param encoder 将 {@code M} 编码为 {@link ByteBuf} 的编码器 */
    public DatagramPacketEncoder(MessageToMessageEncoder<? super M> encoder) {
        this.encoder = checkNotNull(encoder, "encoder");
    }

    @Override
    public boolean acceptOutboundMessage(Object msg) throws Exception {
        if (super.acceptOutboundMessage(msg)) {
            @SuppressWarnings("rawtypes")
            AddressedEnvelope envelope = (AddressedEnvelope) msg;
            return encoder.acceptOutboundMessage(envelope.content())
                    && (envelope.sender() instanceof InetSocketAddress || envelope.sender() == null)
                    && envelope.recipient() instanceof InetSocketAddress;
        }
        return false;
    }

    @Override
    protected void encode(
            ChannelHandlerContext ctx, AddressedEnvelope<M, InetSocketAddress> msg, List<Object> out) throws Exception {
        assert out.isEmpty();

        encoder.encode(ctx, msg.content(), out);
        if (out.size() != 1) {
            throw new EncoderException(
                    StringUtil.simpleClassName(encoder) + " must produce only one message.");
        }
        Object content = out.get(0);
        if (content instanceof ByteBuf) {
            // 将 ByteBuf 替换为带地址的 DatagramPacket
            out.set(0, new DatagramPacket((ByteBuf) content, msg.recipient(), msg.sender()));
        } else {
            throw new EncoderException(
                    StringUtil.simpleClassName(encoder) + " must produce only ByteBuf.");
        }
    }

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        encoder.bind(ctx, localAddress, promise);
    }

    @Override
    public void connect(
            ChannelHandlerContext ctx, SocketAddress remoteAddress,
            SocketAddress localAddress, ChannelPromise promise) throws Exception {
        encoder.connect(ctx, remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        encoder.disconnect(ctx, promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        encoder.close(ctx, promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        encoder.deregister(ctx, promise);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        encoder.read(ctx);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        encoder.flush(ctx);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        encoder.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        encoder.handlerRemoved(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        encoder.exceptionCaught(ctx, cause);
    }

    @Override
    public boolean isSharable() {
        return encoder.isSharable();
    }
}
