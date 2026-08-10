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

import io.netty.channel.sctp.SctpMessage;
import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * SCTP 消息到应用消息的抽象解码基类。
 * <p>仅接受 {@code isComplete()==true} 的 {@link SctpMessage}； 不完整消息抛出 {@link CodecException} 并提示在 pipeline 中前置 {@link SctpMessageCompletionHandler}。</p>
 */
public abstract class SctpMessageToMessageDecoder extends MessageToMessageDecoder<SctpMessage> {

    /** 声明入站类型为 {@link SctpMessage} */
    public SctpMessageToMessageDecoder() {
        super(SctpMessage.class);
    }

    @Override
    /** 过滤非 SctpMessage；拒绝未完整组装的 SCTP 分片 */
    public boolean acceptInboundMessage(Object msg) throws Exception {
        if (msg instanceof SctpMessage) {
            SctpMessage sctpMsg = (SctpMessage) msg;
            if (sctpMsg.isComplete()) {
                return true;
            }

            throw new CodecException(String.format("Received SctpMessage is not complete, please add %s in " +
                    "the pipeline before this handler", SctpMessageCompletionHandler.class.getSimpleName()));
        } else {
            return false;
        }
    }
}
