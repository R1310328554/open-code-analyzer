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
package io.netty.handler.codec.spdy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.spdy.SpdyHttpHeaders.Names;
import io.netty.util.ReferenceCountUtil;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/**
 * 自动维护 {@link SpdyHttpHeaders.Names#STREAM_ID} 与 HTTP 响应的对应关系，
 * 使现有纯 HTTP Handler 无需感知 SPDY 流 ID 即可生成正确编码。
 * <p>入站时按请求顺序记录 stream id；出站响应若缺少该头，则从队列取出并补写。
 */
public class SpdyHttpResponseStreamIdHandler extends
        MessageToMessageCodec<Object, HttpMessage> {
    /** 占位符：表示对应请求未携带 stream id（如非 SPDY 路径） */
    private static final Integer NO_ID = -1;
    /** FIFO 队列，与入站 HttpMessage 顺序一一对应 */
    private final Queue<Integer> ids = new ArrayDeque<Integer>();

    public SpdyHttpResponseStreamIdHandler() {
        super(Object.class, HttpMessage.class);
    }

    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        return msg instanceof HttpMessage || msg instanceof SpdyRstStreamFrame;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, HttpMessage msg, List<Object> out) throws Exception {
        Integer id = ids.poll();
        if (id != null && id.intValue() != NO_ID && !msg.headers().contains(SpdyHttpHeaders.Names.STREAM_ID)) {
            msg.headers().setInt(Names.STREAM_ID, id);
        }

        out.add(ReferenceCountUtil.retain(msg));
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        if (msg instanceof HttpMessage) {
            boolean contains = ((HttpMessage) msg).headers().contains(SpdyHttpHeaders.Names.STREAM_ID);
            if (!contains) {
                ids.add(NO_ID);
            } else {
                ids.add(((HttpMessage) msg).headers().getInt(Names.STREAM_ID));
            }
        } else if (msg instanceof SpdyRstStreamFrame) {
            // 流被对端重置时，从队列移除对应 id，避免 id 错位
            ids.remove(((SpdyRstStreamFrame) msg).streamId());
        }

        out.add(ReferenceCountUtil.retain(msg));
    }
}
