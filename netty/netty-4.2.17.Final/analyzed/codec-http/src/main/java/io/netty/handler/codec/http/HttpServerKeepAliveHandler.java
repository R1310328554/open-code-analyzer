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
package io.netty.handler.codec.http;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;

import static io.netty.handler.codec.http.HttpUtil.*;

/**
 * 服务端 Keep-Alive 连接管理：在适当时机关闭持久连接。
 * <p>
 * 跟踪客户端 pipelining 下的待完成响应数；若响应无法自描述消息长度
（无 CL/chunked/multipart 等）或客户端/响应要求关闭，则在 LastHttpContent 后关闭通道。
 * <p>
 * 须放在 {@link HttpServerCodec} 之后、写 {@link HttpResponse} 的业务 handler 之前。 <blockquote>
 * <pre>
 *  {@link ChannelPipeline} p = ...;
 *  ...
 *  p.addLast("serverCodec", new {@link HttpServerCodec}());
 *  p.addLast("httpKeepAlive", <b>new {@link HttpServerKeepAliveHandler}()</b>);
 *  p.addLast("aggregator", new {@link HttpObjectAggregator}(1048576));
 *  ...
 *  p.addLast("handler", new HttpRequestHandler());
 *  </pre>
 * </blockquote>
 */
public class HttpServerKeepAliveHandler extends ChannelDuplexHandler {
    private static final String MULTIPART_PREFIX = "multipart";

    private boolean persistentConnection = true;
    /** 尚未完成写出的响应数（支持客户端 pipelining，RFC 7230 §6.3.2） */
    private int pendingResponses;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 入站：每收到请求递增待响应计数，并记录客户端是否希望 keep-alive
        if (msg instanceof HttpRequest) {
            final HttpRequest request = (HttpRequest) msg;
            if (persistentConnection) {
                pendingResponses += 1;
                persistentConnection = isKeepAlive(request);
            }
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // modify message on way out to add headers if needed
        if (msg instanceof HttpResponse) {
            final HttpResponse response = (HttpResponse) msg;
            trackResponse(response);
            // 出站：根据响应 keep-alive 与是否自描述长度决定是否维持持久连接
            if (!isKeepAlive(response) || !isSelfDefinedMessageLength(response)) {
                // 无法自描述长度时客户端无法判断消息结束，须关闭连接
                pendingResponses = 0;
                persistentConnection = false;
            }
            // Server might think it can keep connection alive, but we should fix response header if we know better
            if (!shouldKeepAlive()) {
                setKeepAlive(response, false);
            }
        }
        if (msg instanceof LastHttpContent && !shouldKeepAlive()) {
            promise = promise.unvoid().addListener(ChannelFutureListener.CLOSE);
        }
        super.write(ctx, msg, promise);
    }

    private void trackResponse(HttpResponse response) {
        if (!isInformational(response)) {
            pendingResponses -= 1;
        }
    }

    private boolean shouldKeepAlive() {
        return pendingResponses != 0 || persistentConnection;
    }

    /**
     * 持久连接要求客户端能判断消息结束（Content-Length、chunked、multipart、1xx/204 等）。
     * <p>
     * <ul>
     *     <li>See <a href="https://tools.ietf.org/html/rfc7230#section-6.3"/></li>
     *     <li>See <a href="https://tools.ietf.org/html/rfc7230#section-3.3.2"/></li>
     *     <li>See <a href="https://tools.ietf.org/html/rfc7230#section-3.3.3"/></li>
     * </ul>
     *
     * @param response The HttpResponse to check
     *
     * @return true if the response has a self defined message length.
     */
    private static boolean isSelfDefinedMessageLength(HttpResponse response) {
        return isContentLengthSet(response) || isTransferEncodingChunked(response) || isMultipart(response) ||
               isInformational(response) || response.status().code() == HttpResponseStatus.NO_CONTENT.code();
    }

    private static boolean isInformational(HttpResponse response) {
        return response.status().codeClass() == HttpStatusClass.INFORMATIONAL;
    }

    private static boolean isMultipart(HttpResponse response) {
        String contentType = response.headers().get(HttpHeaderNames.CONTENT_TYPE);
        return contentType != null &&
               contentType.regionMatches(true, 0, MULTIPART_PREFIX, 0, MULTIPART_PREFIX.length());
    }
}
