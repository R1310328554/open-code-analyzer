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

package io.netty.handler.codec.http2;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.util.internal.StringUtil;

/**
 * 面向 HTTP/2 的 {@link ChannelDuplexHandler} 基类，在 pipeline 中依赖前置的 {@link Http2FrameCodec}。
 * <ul>
 *     <li>{@link #newStream()} 创建出站流（线程安全）。</li>
 *     <li>{@link #forEachActiveStream(Http2FrameStreamVisitor)} 遍历当前活跃流（仅 event loop 线程）。</li>
 * </ul>
 *
 * <p>必须在 {@link Http2FrameCodec} 之后加入 pipeline，否则 {@link #handlerAdded} 抛出 {@link IllegalStateException}。
 */
public abstract class Http2ChannelDuplexHandler extends ChannelDuplexHandler {

    /** 从 pipeline 解析到的 {@link Http2FrameCodec}，handler 移除后置 null。 */
    private volatile Http2FrameCodec frameCodec;

    @Override
    public final void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        frameCodec = requireHttp2FrameCodec(ctx);
        handlerAdded0(ctx);
    }

    protected void handlerAdded0(@SuppressWarnings("unused") ChannelHandlerContext ctx) throws Exception {
        // 子类可覆写以在 frameCodec 就绪后做初始化
    }

    @Override
    public final void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        try {
            handlerRemoved0(ctx);
        } finally {
            frameCodec = null;
        }
    }

    protected void handlerRemoved0(@SuppressWarnings("unused") ChannelHandlerContext ctx) throws Exception {
        // NOOP
    }

    /**
     * 创建新的 {@link Http2FrameStream}，用于客户端主动发起请求或服务器推送前的流对象分配。
     *
     * <p>This method is <em>thread-safe</em>.
     */
    public final Http2FrameStream newStream() {
        Http2FrameCodec codec = frameCodec;
        if (codec == null) {
            throw new IllegalStateException(StringUtil.simpleClassName(Http2FrameCodec.class) + " not found." +
                    " Has the handler been added to a pipeline?");
        }
        return codec.newStream();
    }

    /**
     * 遍历所有当前活跃（OPEN / HALF_CLOSED）的 HTTP/2 流。
     *
     * <p>This method may only be called from the eventloop thread.
     */
    protected final void forEachActiveStream(Http2FrameStreamVisitor streamVisitor) throws Http2Exception {
        frameCodec.forEachActiveStream(streamVisitor);
    }

    private static Http2FrameCodec requireHttp2FrameCodec(ChannelHandlerContext ctx) {
        ChannelHandlerContext frameCodecCtx = ctx.pipeline().context(Http2FrameCodec.class);
        if (frameCodecCtx == null) {
            throw new IllegalArgumentException(Http2FrameCodec.class.getSimpleName()
                                               + " was not found in the channel pipeline.");
        }
        return (Http2FrameCodec) frameCodecCtx.handler();
    }
}
