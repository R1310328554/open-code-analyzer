/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.netty.handler.codec.sctp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.sctp.SctpMessage;
import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * A ChannelHandler which receives {@link SctpMessage}s which belong to a application protocol form a specific
 * SCTP Stream  and decode it as {@link ByteBuf}.
 * <p>入站 SCTP 字节流解码器：仅接受指定 {@code protocolIdentifier} 与 {@code streamIdentifier} 的 完整 {@link SctpMessage}，将其 {@link ByteBuf} 负载转发到下游。分片消息需先经 {@link SctpMessageCompletionHandler} 组装。</p>
 */
public class SctpInboundByteStreamHandler extends MessageToMessageDecoder<SctpMessage> {
    private final int protocolIdentifier;
    private final int streamIdentifier;

    /**
     * @param streamIdentifier   accepted stream number, this should be >=0 or <= max stream number of the association.
     * @param protocolIdentifier supported application protocol.
     * <p>绑定要监听的 SCTP 流号与应用层协议标识（PPID）。</p>
     */
    public SctpInboundByteStreamHandler(int protocolIdentifier, int streamIdentifier) {
        super(SctpMessage.class);
        this.protocolIdentifier = protocolIdentifier;
        this.streamIdentifier = streamIdentifier;
    }

    @Override
    public final boolean acceptInboundMessage(Object msg) throws Exception {
        if (super.acceptInboundMessage(msg)) {
            return acceptInboundMessage((SctpMessage) msg);
        }
        return false;
    }

    /** 仅当协议 ID 与流 ID 均匹配时接受该 {@link SctpMessage} */
    protected boolean acceptInboundMessage(SctpMessage msg) {
        return msg.protocolIdentifier() == protocolIdentifier && msg.streamIdentifier() == streamIdentifier;
    }

    @Override
    /** 校验消息完整后 retain 其 content 并写入 out */
    protected void decode(ChannelHandlerContext ctx, SctpMessage msg, List<Object> out) throws Exception {
        if (!msg.isComplete()) {
            throw new CodecException(String.format("Received SctpMessage is not complete, please add %s in the " +
                    "pipeline before this handler", SctpMessageCompletionHandler.class.getSimpleName()));
        }
        out.add(msg.content().retain());
    }
}
