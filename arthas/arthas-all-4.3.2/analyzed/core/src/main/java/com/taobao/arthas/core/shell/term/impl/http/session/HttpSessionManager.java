package com.taobao.arthas.core.shell.term.impl.http.session;

import java.util.Collections;
import java.util.Set;

import com.taobao.arthas.common.ArthasConstants;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * Netty HTTP 会话管理器：Cookie 与 Channel 属性双层绑定。
 * <p>
 * 同一域名不同端口共享 Cookie，故全局 {@link LRUCache} 统一存储会话，
 * 供 {@link com.taobao.arthas.core.shell.term.impl.http.BasicHttpAuthenticatorHandler}
 * 与 {@link com.taobao.arthas.core.shell.term.impl.http.api.HttpApiHandler} 使用。
 *
 * @author hengyunabc 2021-03-03
 *
 */
public class HttpSessionManager {
    /** Channel 上挂载当前 {@link HttpSession} 的属性键 */
    public static AttributeKey<HttpSession> SESSION_KEY = AttributeKey.valueOf("session");

    /** 全局会话 LRU 缓存，最多 1024 个 */
    private LRUCache<String, HttpSession> sessions = new LRUCache<String, HttpSession>(1024);

    public HttpSessionManager() {

    }

    /** 从请求 Cookie 中解析 {@link ArthasConstants#ASESSION_KEY} 并查找会话 */
    private HttpSession getSession(HttpRequest httpRequest) {
        // TODO 增加从 url中获取 session id 功能？

        Set<Cookie> cookies;
        String value = httpRequest.headers().get(HttpHeaderNames.COOKIE);
        if (value == null) {
            cookies = Collections.emptySet();
        } else {
            cookies = ServerCookieDecoder.STRICT.decode(value);
        }
        for (Cookie cookie : cookies) {
            if (ArthasConstants.ASESSION_KEY.equals(cookie.name())) {
                String sessionId = cookie.value();
                return sessions.get(sessionId);
            }
        }
        return null;
    }

    /** 从 Netty Channel 上下文读取已绑定的 HTTP 会话 */
    public static HttpSession getHttpSessionFromContext(ChannelHandlerContext ctx) {
        return ctx.channel().attr(SESSION_KEY).get();
    }

    /**
     * 获取或创建 HTTP 会话：优先 Channel 属性，其次 Cookie，最后新建。
     *
     * @param ctx         Netty 上下文
     * @param httpRequest 当前 HTTP 请求
     * @return 有效会话实例
     */
    public HttpSession getOrCreateHttpSession(ChannelHandlerContext ctx, HttpRequest httpRequest) {
        // 尝试用 ctx 和从 cookie里读取出 session
        Attribute<HttpSession> attribute = ctx.channel().attr(SESSION_KEY);
        HttpSession httpSession = attribute.get();
        if (httpSession != null) {
            return httpSession;
        }
        httpSession = getSession(httpRequest);
        if (httpSession != null) {
            attribute.set(httpSession);
            return httpSession;
        }
        // 创建session，并设置到ctx里
        httpSession = newHttpSession();
        attribute.set(httpSession);
        return httpSession;
    }

    /** 创建新会话并注册到 LRU 缓存 */
    private HttpSession newHttpSession() {
        SimpleHttpSession session = new SimpleHttpSession();
        this.sessions.put(session.getId(), session);
        return session;
    }

    /** 在响应头写入会话 Cookie，供客户端后续请求携带 */
    public static void setSessionCookie(HttpResponse response, HttpSession session) {
        response.headers().add(HttpHeaderNames.SET_COOKIE,
                ServerCookieEncoder.STRICT.encode(ArthasConstants.ASESSION_KEY, session.getId()));
    }

    public void start() {

    }

    /** 停止时清空全部缓存会话 */
    public void stop() {
        sessions.clear();
    }

}
