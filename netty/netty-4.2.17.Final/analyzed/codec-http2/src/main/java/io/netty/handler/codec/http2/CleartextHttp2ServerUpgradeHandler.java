/*
 * Copyright 2017 The Netty Project
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
package io.netty.handler.codec.http2;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;

import java.util.List;

import static io.netty.buffer.Unpooled.unreleasableBuffer;
import static io.netty.handler.codec.http2.Http2CodecUtil.connectionPrefaceBuf;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * 明文 HTTP/2（h2c）服务端升级处理器：支持 HTTP Upgrade 与 Prior Knowledge 两种路径。
 * <p>{@link #handlerAdded} 时在 pipeline 中插入 HTTP/1 编解码与升级处理器；
 * {@link #decode} 窥视首包：若非 HTTP/2 前导则移除自身走 HTTP/1，若完整匹配前导则切换到 {@code http2ServerHandler}。
 */
public final class CleartextHttp2ServerUpgradeHandler extends ByteToMessageDecoder {
    /** 不可释放的 HTTP/2 连接前导（{@code PRI * HTTP/2.0...}），用于 Prior Knowledge 探测。 */
    private static final ByteBuf CONNECTION_PREFACE = unreleasableBuffer(connectionPrefaceBuf()).asReadOnly();

    private final HttpServerCodec httpServerCodec;
    private final HttpServerUpgradeHandler httpServerUpgradeHandler;
    private final ChannelHandler http2ServerHandler;

    /**
     * 创建 h2c 升级处理器。
     *
     * @param httpServerCodec the http server codec
     * @param httpServerUpgradeHandler the http server upgrade handler for HTTP/2
     * @param http2ServerHandler the http2 server handler, will be added into pipeline
     *                           when starting HTTP/2 by prior knowledge
     */
    public CleartextHttp2ServerUpgradeHandler(HttpServerCodec httpServerCodec,
                                              HttpServerUpgradeHandler httpServerUpgradeHandler,
                                              ChannelHandler http2ServerHandler) {
        this.httpServerCodec = checkNotNull(httpServerCodec, "httpServerCodec");
        this.httpServerUpgradeHandler = checkNotNull(httpServerUpgradeHandler, "httpServerUpgradeHandler");
        this.http2ServerHandler = checkNotNull(http2ServerHandler, "http2ServerHandler");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // 在自身之后挂载 HTTP/1 升级链，待 decode 判定协议后再决定是否移除
        ctx.pipeline()
                .addAfter(ctx.name(), null, httpServerUpgradeHandler)
                .addAfter(ctx.name(), null, httpServerCodec);
    }

    /**
     * 窥视入站字节：判断连接走 HTTP Upgrade 还是 Prior Knowledge（直接发送 h2 前导）。
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int prefaceLength = CONNECTION_PREFACE.readableBytes();
        int bytesRead = Math.min(in.readableBytes(), prefaceLength);

        if (!ByteBufUtil.equals(CONNECTION_PREFACE, CONNECTION_PREFACE.readerIndex(),
                in, in.readerIndex(), bytesRead)) {
            // 前缀不匹配 — 非 Prior Knowledge，交给 HTTP/1 栈处理
            ctx.pipeline().remove(this);
        } else if (bytesRead == prefaceLength) {
            // 完整 h2 前导 — 移除 HTTP/1 组件，接入纯 HTTP/2 处理器
            ctx.pipeline()
                    .remove(httpServerCodec)
                    .remove(httpServerUpgradeHandler);

            ctx.pipeline().addAfter(ctx.name(), null, http2ServerHandler);
            ctx.pipeline().remove(this);

            ctx.fireUserEventTriggered(PriorKnowledgeUpgradeEvent.INSTANCE);
        }
        // 部分匹配且字节不足：等待更多数据，不消费 in
    }

    /**
     * 用户事件：通知应用层连接已通过 Prior Knowledge 直接进入 HTTP/2。
     */
    public static final class PriorKnowledgeUpgradeEvent {
        private static final PriorKnowledgeUpgradeEvent INSTANCE = new PriorKnowledgeUpgradeEvent();

        private PriorKnowledgeUpgradeEvent() {
        }
    }
}
