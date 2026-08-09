/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.rocketmq.proxy.remoting.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 协议协商解码器：读取前 4 字节匹配 {@link ProtocolHandler} 并配置管道。
 */
public class ProtocolNegotiationHandler extends ByteToMessageDecoder {

    /** 按优先级排列的协议处理器列表。 */
    private final List<ProtocolHandler> protocolHandlerList = new ArrayList<ProtocolHandler>();
    /** 无匹配时使用的默认协议处理器（通常为 Remoting）。 */
    private final ProtocolHandler fallbackProtocolHandler;

    /** 指定兜底协议处理器。 */
    public ProtocolNegotiationHandler(ProtocolHandler fallbackProtocolHandler) {
        this.fallbackProtocolHandler = fallbackProtocolHandler;
    }

    /** 追加协议处理器，支持链式调用。 */
    public ProtocolNegotiationHandler addProtocolHandler(ProtocolHandler protocolHandler) {
        protocolHandlerList.add(protocolHandler);
        return this;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 读取前 4 字节判断协议类型
        if (in.readableBytes() < 4) {
            return;
        }

        ProtocolHandler protocolHandler = null;
        for (ProtocolHandler curProtocolHandler : protocolHandlerList) {
            if (curProtocolHandler.match(in)) {
                protocolHandler = curProtocolHandler;
                break;
            }
        }

        if (protocolHandler == null) {
            protocolHandler = fallbackProtocolHandler;
        }

        protocolHandler.config(ctx, in);
        ctx.pipeline().remove(this);
    }
}
