package com.alibaba.arthas.nat.agent.common.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

/**
 * 处理浏览器 CORS 预检 OPTIONS 请求，返回允许的跨域响应头。
 *
 * <p>根据请求中的 Origin、Access-Control-Request-* 头动态设置
 * {@code Access-Control-Allow-*} 系列字段，供 Native Agent HTTP 层复用。</p>
 *
 * @description: HttpOptionRequestHandler
 * @author：flzjkl
 * @date: 2024-09-22 7:21
 */
public class HttpOptionRequestHandler {

    /**
     * 构造 OPTIONS 预检的 200 响应（空 body），并填充 CORS 头。
     *
     * @param ctx Netty 通道上下文（当前实现未直接使用，保留扩展）
     * @param request 入站 OPTIONS 请求
     * @return 可直接 writeAndFlush 的完整 HTTP 响应
     */
    public FullHttpResponse handleOptionsRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                request.getProtocolVersion(),
                HttpResponseStatus.OK,
                Unpooled.EMPTY_BUFFER);

        // 允许的来源：有 Origin 则回显，否则 *
        String origin = request.headers().get(HttpHeaderNames.ORIGIN);
        if (origin != null) {
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        } else {
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        }
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE, 3600L);
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "Content-Type, Authorization, X-Requested-With, Accept, Origin");

        // 预检请求携带 Access-Control-Request-Method 时需回显允许的方法
        String accessControlRequestMethod = request.headers().get(HttpHeaderNames.ACCESS_CONTROL_REQUEST_METHOD);
        if (accessControlRequestMethod != null) {
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, accessControlRequestMethod);
        }

        // 预检请求携带 Access-Control-Request-Headers 时需回显允许的头
        String accessControlRequestHeaders = request.headers().get(HttpHeaderNames.ACCESS_CONTROL_REQUEST_HEADERS);
        if (accessControlRequestHeaders != null) {
            response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, accessControlRequestHeaders);
        }

        return response;
    }

}
