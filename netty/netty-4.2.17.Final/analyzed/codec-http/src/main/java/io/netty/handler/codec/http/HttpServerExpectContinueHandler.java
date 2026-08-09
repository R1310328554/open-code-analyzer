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
package io.netty.handler.codec.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * 处理 {@code Expect: 100-continue} 的服务端入站处理器。
 * <p>
 * 对含该头的 {@link HttpRequest} 先回 100 Continue（或子类自定义拒绝响应）；
 * 适用于<b>未</b>安装 {@link HttpObjectAggregator} 的场景。默认接受所有期望。
 * <p>
 * 须放在 {@link HttpServerCodec} 之后、会写 {@link HttpResponse} 的 handler 之前。 <blockquote>
 * <pre>
 *  {@link io.netty.channel.ChannelPipeline} p = ...;
 *  ...
 *  p.addLast("serverCodec", new {@link HttpServerCodec}());
 *  p.addLast("respondExpectContinue", <b>new {@link HttpServerExpectContinueHandler}()</b>);
 *  ...
 *  p.addLast("handler", new HttpRequestHandler());
 *  </pre>
 * </blockquote>
 */
public class HttpServerExpectContinueHandler extends ChannelInboundHandlerAdapter {

    private static final FullHttpResponse EXPECTATION_FAILED = new DefaultFullHttpResponse(
            HTTP_1_1, HttpResponseStatus.EXPECTATION_FAILED, Unpooled.EMPTY_BUFFER);

    private static final FullHttpResponse ACCEPT = new DefaultFullHttpResponse(
            HTTP_1_1, CONTINUE, Unpooled.EMPTY_BUFFER);

    static {
        EXPECTATION_FAILED.headers().set(CONTENT_LENGTH, 0);
        ACCEPT.headers().set(CONTENT_LENGTH, 0);
    }

    /**
     * 为带 Expect 头的请求生成接受响应；返回 {@code null} 表示拒绝，将调用 {@link #rejectResponse}。
     */
    protected HttpResponse acceptMessage(@SuppressWarnings("unused") HttpRequest request) {
        return ACCEPT.retainedDuplicate();
    }

    /**
     * 返回拒绝期望时使用的 4xx 响应（默认 417 Expectation Failed）。
     */
    protected HttpResponse rejectResponse(@SuppressWarnings("unused") HttpRequest request) {
        return EXPECTATION_FAILED.retainedDuplicate();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;

            if (HttpUtil.is100ContinueExpected(req)) {
                HttpResponse accept = acceptMessage(req);

                if (accept == null) {
                    // 期望被拒绝：释放请求并返回拒绝响应
                    HttpResponse rejection = rejectResponse(req);
                    ReferenceCountUtil.release(msg);
                    ctx.writeAndFlush(rejection).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                    return;
                }

                // 先回 100，再移除 Expect 头以便下游正常处理请求体
                ctx.writeAndFlush(accept).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                req.headers().remove(HttpHeaderNames.EXPECT);
            }
        }
        super.channelRead(ctx, msg);
    }
}
