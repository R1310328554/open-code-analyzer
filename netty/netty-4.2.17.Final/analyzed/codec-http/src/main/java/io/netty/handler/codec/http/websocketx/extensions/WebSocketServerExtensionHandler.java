/*
 * Copyright 2014 The Netty Project
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
package io.netty.handler.codec.http.websocketx.extensions;

import static io.netty.util.internal.ObjectUtil.checkNonEmpty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

/**
 * 服务端 WebSocket 扩展协商与 pipeline 初始化处理器。
 * <p>按客户端 {@code Sec-WebSocket-Extensions} 声明顺序，依次调用注册的
 * {@link WebSocketServerExtensionHandshaker} 匹配扩展；校验 RSV 位不冲突后，
 * 在 101 响应写出成功时将对应 {@link WebSocketExtensionDecoder}/{@link WebSocketExtensionEncoder}
 * 插入 pipeline 并移除自身。
 * <p>压缩扩展示例见 {@code WebSocketServerCompressionHandler}。
 */
public class WebSocketServerExtensionHandler extends ChannelDuplexHandler {

    /** 按优先级排列的扩展握手器列表（可重复注册以支持降级配置） */
    private final List<WebSocketServerExtensionHandshaker> extensionHandshakers;

    /** 请求阶段协商成功的扩展列表，与后续 101 响应按 FIFO 配对 */
    private final Queue<List<WebSocketServerExtension>> validExtensions = new ArrayDeque<>(4);

    /**
     * Constructor
     *
     * @param extensionHandshakers 按优先级排列的握手器；可重复注册同一类型以提供降级参数
     */
    public WebSocketServerExtensionHandler(WebSocketServerExtensionHandshaker... extensionHandshakers) {
        this.extensionHandshakers = Arrays.asList(checkNonEmpty(extensionHandshakers, "extensionHandshakers"));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // JDK instanceof 对未实现接口的检查为 O(N)，对常见具体类型走快速路径
        // N is the number of interfaces already implemented by the concrete type that's being tested.
        // The only requirement for this call is to make HttpRequest(s) implementors to call onHttpRequestChannelRead
        // and super.channelRead the others, but due to the O(n) cost we perform few fast-path for commonly met
        // singleton and/or concrete types, to save performing such slow type checks.
        if (msg != LastHttpContent.EMPTY_LAST_CONTENT) {
            if (msg instanceof DefaultHttpRequest) {
                // 快速路径：DefaultHttpRequest
                onHttpRequestChannelRead(ctx, (DefaultHttpRequest) msg);
            } else if (msg instanceof HttpRequest) {
                // 慢路径：其他 HttpRequest 实现
                onHttpRequestChannelRead(ctx, (HttpRequest) msg);
            } else {
                super.channelRead(ctx, msg);
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }

    /**
     * 供子类对自定义 {@link HttpRequest} 类型做快速分发；完整示例见源码注释。
     * <p>若 {@link #channelRead} 还会收到 {@link LastHttpContent#EMPTY_LAST_CONTENT}，
     * 可像下面这样仅对 {@code CustomHttpRequest} 调用本方法：
     * <pre>
     *     public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
     *         if (msg != LastHttpContent.EMPTY_LAST_CONTENT) {
     *             if (msg instanceof CustomHttpRequest) {
     *                 onHttpRequestChannelRead(ctx, (CustomHttpRequest) msg);
     *             } else {
     *                 // if it's handling other HttpRequest types it MUST use onHttpRequestChannelRead again
     *                 // or have to delegate it to super.channelRead (that can perform redundant checks).
     *                 // If msg is not implementing HttpRequest, it can call ctx.fireChannelRead(msg) on it
     *                 // ...
     *                 super.channelRead(ctx, msg);
     *             }
     *         } else {
     *             // given that msg isn't a HttpRequest type we can just skip calling super.channelRead
     *             ctx.fireChannelRead(msg);
     *         }
     *     }
     * </pre>
     * <strong>IMPORTANT:</strong>
     * 返回前已调用 {@code super.channelRead(ctx, request)}。
     */
    /** 解析升级请求中的扩展头，按 handshaker 顺序协商并暂存有效扩展 */
    protected void onHttpRequestChannelRead(ChannelHandlerContext ctx, HttpRequest request) throws Exception {
        List<WebSocketServerExtension> validExtensionsList = null;

        if (WebSocketExtensionUtil.isWebsocketUpgrade(request.headers())) {
            String extensionsHeader = request.headers().getAsString(HttpHeaderNames.SEC_WEBSOCKET_EXTENSIONS);

            if (extensionsHeader != null) {
                List<WebSocketExtensionData> extensions =
                        WebSocketExtensionUtil.extractExtensions(extensionsHeader);
                int rsv = 0;

                for (WebSocketExtensionData extensionData : extensions) {
                    Iterator<WebSocketServerExtensionHandshaker> extensionHandshakersIterator =
                            extensionHandshakers.iterator();
                    WebSocketServerExtension validExtension = null;

                    while (validExtension == null && extensionHandshakersIterator.hasNext()) {
                        WebSocketServerExtensionHandshaker extensionHandshaker =
                                extensionHandshakersIterator.next();
                        validExtension = extensionHandshaker.handshakeExtension(extensionData);
                    }

                    if (validExtension != null && ((validExtension.rsv() & rsv) == 0)) {
                        if (validExtensionsList == null) {
                            validExtensionsList = new ArrayList<WebSocketServerExtension>(1);
                        }
                        rsv = rsv | validExtension.rsv();
                        validExtensionsList.add(validExtension);
                    }
                }
            }
        }

        if (validExtensionsList == null) {
            validExtensionsList = Collections.emptyList();
        }
        validExtensions.offer(validExtensionsList);
        super.channelRead(ctx, request);
    }

    @Override
    public void write(final ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg != Unpooled.EMPTY_BUFFER && !(msg instanceof ByteBuf)) {
            if (msg instanceof DefaultHttpResponse) {
                onHttpResponseWrite(ctx, (DefaultHttpResponse) msg, promise);
            } else if (msg instanceof HttpResponse) {
                onHttpResponseWrite(ctx, (HttpResponse) msg, promise);
            } else {
                super.write(ctx, msg, promise);
            }
        } else {
            super.write(ctx, msg, promise);
        }
    }

    /**
     * 供子类对自定义 {@link HttpResponse} 类型做快速分发；完整示例见源码注释。
     * <p>若 {@link #write} 还会收到 {@link ByteBuf}，可仅对 {@code CustomHttpResponse} 调用本方法：
     * <pre>
     *     public void write(final ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
     *         if (msg != Unpooled.EMPTY_BUFFER && !(msg instanceof ByteBuf)) {
     *             if (msg instanceof CustomHttpResponse) {
     *                 onHttpResponseWrite(ctx, (CustomHttpResponse) msg, promise);
     *             } else {
     *                 // if it's handling other HttpResponse types it MUST use onHttpResponseWrite again
     *                 // or have to delegate it to super.write (that can perform redundant checks).
     *                 // If msg is not implementing HttpResponse, it can call ctx.write(msg, promise) on it
     *                 // ...
     *                 super.write(ctx, msg, promise);
     *             }
     *         } else {
     *             // given that msg isn't a HttpResponse type we can just skip calling super.write
     *             ctx.write(msg, promise);
     *         }
     *     }
     * </pre>
     * <strong>IMPORTANT:</strong>
     * 返回前已调用 {@code super.write(ctx, response, promise)}。
     */
    protected void onHttpResponseWrite(ChannelHandlerContext ctx, HttpResponse response, ChannelPromise promise)
            throws Exception {
        List<WebSocketServerExtension> validExtensionsList = validExtensions.poll();
        // 先比对 101 状态码，比解析头更快
        if (HttpResponseStatus.SWITCHING_PROTOCOLS.equals(response.status())) {
            handlePotentialUpgrade(ctx, promise, response, validExtensionsList);
        }
        super.write(ctx, response, promise);
    }

    /**
     * 写出 101 响应时是否确认并写入协商成功的扩展头。
     * <p>子类可覆盖以在最终响应阶段否决部分扩展。
     */
    protected boolean isExtensionNegotiationEnabled(ChannelHandlerContext ctx, HttpResponse response) {
        return true;
    }

    /** 101 升级响应：合并扩展头，写成功后插入编解码器并移除本 handler */
    private void handlePotentialUpgrade(final ChannelHandlerContext ctx,
                                        ChannelPromise promise, HttpResponse httpResponse,
                                        final List<WebSocketServerExtension> validExtensionsList) {
        HttpHeaders headers = httpResponse.headers();

        if (WebSocketExtensionUtil.isWebsocketUpgrade(headers)) {
            if (isExtensionNegotiationEnabled(ctx, httpResponse)
                    && validExtensionsList != null && !validExtensionsList.isEmpty()) {
                String headerValue = headers.getAsString(HttpHeaderNames.SEC_WEBSOCKET_EXTENSIONS);
                List<WebSocketExtensionData> extraExtensions =
                  new ArrayList<>(extensionHandshakers.size());
                for (WebSocketServerExtension extension : validExtensionsList) {
                    extraExtensions.add(extension.newReponseData());
                }
                String newHeaderValue = WebSocketExtensionUtil
                  .computeMergeExtensionsHeaderValue(headerValue, extraExtensions);
                promise.addListener(future -> {
                    if (future.isSuccess()) {
                        for (WebSocketServerExtension extension : validExtensionsList) {
                            WebSocketExtensionDecoder decoder = extension.newExtensionDecoder();
                            WebSocketExtensionEncoder encoder = extension.newExtensionEncoder();
                            String name = ctx.name();
                            ctx.pipeline()
                                .addAfter(name, decoder.getClass().getName(), decoder)
                                .addAfter(name, encoder.getClass().getName(), encoder);
                        }
                    }
                });

                headers.set(HttpHeaderNames.SEC_WEBSOCKET_EXTENSIONS, newHeaderValue);
            }

            promise.addListener(future -> {
                if (future.isSuccess()) {
                    ctx.pipeline().remove(WebSocketServerExtensionHandler.this);
                }
            });
        }
    }
}
