package com.taobao.arthas.core.util;

import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import java.io.UnsupportedEncodingException;
import java.util.Set;

/**
 * Arthas HTTP/Web 终端相关的 Netty HTTP 辅助方法。
 *
 * @author gongdewei 2020/3/31
 */
public class HttpUtils {

    /**
     * 从请求 Cookie 集合中按名称取值。
     *
     * @param cookies 请求携带的 Cookie 集合
     * @param cookieName Cookie 名称
     * @return 匹配的值，未找到时返回 null
     */
    public static String getCookieValue(Set<Cookie> cookies, String cookieName) {
        for (Cookie cookie : cookies) {
            if(cookie.name().equals(cookieName)){
                return cookie.value();
            }
        }
        return null;
    }

    /**
     * 向 HTTP 响应追加 Set-Cookie 头（STRICT 编码）。
     *
     * @param response Netty 全量 HTTP 响应
     * @param name Cookie 名
     * @param value Cookie 值
     */
    public static void setCookie(DefaultFullHttpResponse response, String name, String value) {
        response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(name, value));
    }

    /**
     * 构造带 UTF-8 HTML 正文的标准 HTTP 响应。
     *
     * @param request 原始请求（用于协议版本）
     * @param status HTTP 状态码
     * @param content 响应 HTML 正文
     * @return 填充了 Content-Type 与 body 的响应对象
     */
    public static DefaultHttpResponse createResponse(FullHttpRequest request, HttpResponseStatus status, String content) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), status);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=utf-8");
        try {
            response.content().writeBytes(content.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
        }
        return response;
    }

    /**
     * 构造 302 重定向响应，Location 指向给定 URL。
     *
     * @param request 原始请求
     * @param url 重定向目标地址
     * @return FOUND 状态的重定向响应
     */
    public static HttpResponse createRedirectResponse(FullHttpRequest request, String url) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.FOUND);
        response.headers().set(HttpHeaderNames.LOCATION, url);
        return response;
    }
}
