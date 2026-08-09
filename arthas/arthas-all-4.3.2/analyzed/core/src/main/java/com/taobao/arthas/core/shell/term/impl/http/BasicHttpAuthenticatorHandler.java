package com.taobao.arthas.core.shell.term.impl.http;

import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.common.ArthasConstants;
import com.taobao.arthas.core.security.AuthUtils;
import com.taobao.arthas.core.security.BasicPrincipal;
import com.taobao.arthas.core.security.BearerPrincipal;
import com.taobao.arthas.core.security.SecurityAuthenticator;
import com.taobao.arthas.core.server.ArthasBootstrap;
import com.taobao.arthas.core.shell.term.impl.http.session.HttpSession;
import com.taobao.arthas.core.shell.term.impl.http.session.HttpSessionManager;
import com.taobao.arthas.core.util.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.*;
import io.netty.util.Attribute;

import javax.security.auth.Subject;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import static com.taobao.arthas.mcp.server.util.McpAuthExtractor.SUBJECT_ATTRIBUTE_KEY;


/**
 * HTTP Basic/Bearer 认证 Netty 处理器，位于 pipeline 最前段。
 * <p>
 * 校验 {@link SecurityAuthenticator} 登录态；支持 Session 复用、URL 参数、
 * Authorization 头及本地连接免认证；MCP 端点额外支持 Bearer Token。
 *
 * @author hengyunabc 2021-03-03
 */
public final class BasicHttpAuthenticatorHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(BasicHttpAuthenticatorHandler.class);

    /** HTTP 会话管理器，维护 cookie 与 Subject */
    private HttpSessionManager httpSessionManager;

    /** 全局安全认证器，决定是否需登录及校验凭据 */
    private SecurityAuthenticator securityAuthenticator = ArthasBootstrap.getInstance().getSecurityAuthenticator();

    public BasicHttpAuthenticatorHandler(HttpSessionManager httpSessionManager) {
        this.httpSessionManager = httpSessionManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 非 HttpRequest 消息直接透传
        if (!(msg instanceof HttpRequest)) {
            ctx.fireChannelRead(msg);
            return;
        }

        HttpRequest httpRequest = (HttpRequest) msg;
        HttpSession session = httpSessionManager.getOrCreateHttpSession(ctx, httpRequest);

        // 无论是否强制登录，均从 URL 提取 userId 写入 Session
        extractAndSetUserIdFromUrl(httpRequest, session);

        if (!securityAuthenticator.needLogin()) {
            ctx.fireChannelRead(msg);
            return;
        }

        boolean authed = false;

        // 检查 Session 是否已有已认证的 Subject
        if (session != null) {
            Object subjectObj = session.getAttribute(ArthasConstants.SUBJECT_KEY);
            if (subjectObj != null) {
                authed =true;
                setAuthenticatedSubject(ctx, session, subjectObj);
            }
        }

        Principal principal = null;
        boolean isMcpRequest = isMcpRequest(httpRequest);

        if (!authed) {
            if (isMcpRequest) {
                principal = extractMcpAuthSubject(httpRequest);
            } else {
                principal = extractBasicAuthSubject(httpRequest);
                if (principal == null) {
                    principal = extractBasicAuthSubjectFromUrl(httpRequest);
                }
            }
            if (principal == null) {
                // 本地回环连接可自动授予 localPrincipal
                principal = AuthUtils.localPrincipal(ctx);
            }
            Subject subject = securityAuthenticator.login(principal);
            if (subject != null) {
                authed = true;
                setAuthenticatedSubject(ctx, session, subject);
            }
        }

        if (!authed) {
            // 受保护资源：返回 401 并要求有效凭据
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED);

            if (isMcpRequest) {
                response.headers()
                        .add(HttpHeaderNames.WWW_AUTHENTICATE, "Bearer realm=\"arthas mcp\"")
                        .add(HttpHeaderNames.WWW_AUTHENTICATE, "Basic realm=\"arthas mcp\"");
            } else {
                response.headers().set(HttpHeaderNames.WWW_AUTHENTICATE, "Basic realm=\"arthas webconsole\"");
            }

            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);

            ctx.writeAndFlush(response);
            // 认证失败则关闭连接
            ctx.channel().close();
            return;
        }

        ctx.fireChannelRead(msg);
    }

    private void setAuthenticatedSubject(ChannelHandlerContext ctx, HttpSession session, Object subject) {
        ctx.channel().attr(SUBJECT_ATTRIBUTE_KEY).set(subject);
        if (session != null) {
            session.setAttribute(ArthasConstants.SUBJECT_KEY, subject);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof HttpResponse) {
            // 响应写出时附加 Session Cookie
            HttpResponse response = (HttpResponse) msg;
            Attribute<HttpSession> attribute = ctx.channel().attr(HttpSessionManager.SESSION_KEY);
            HttpSession session = attribute.get();
            if (session != null) {
                HttpSessionManager.setSessionCookie(response, session);
            }
        }
        super.write(ctx, msg, promise);
    }

    /**
     * 从 URL 查询参数提取 userId 并存入 {@link HttpSession}。
     *
     * @param request HTTP 请求
     * @param session 当前 HTTP 会话
     */
    protected static void extractAndSetUserIdFromUrl(HttpRequest request, HttpSession session) {
        if (session == null) {
            return;
        }
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> parameters = queryDecoder.parameters();

        List<String> userIds = parameters.get(ArthasConstants.USER_ID_KEY);
        if (userIds != null && !userIds.isEmpty()) {
            String userId = userIds.get(0);
            if (!StringUtils.isBlank(userId)) {
                session.setAttribute(ArthasConstants.USER_ID_KEY, userId);
                logger.debug("Extracted userId from url: {}", userId);
            }
        }
    }

    /**
     * 从 URL 参数提取 Basic 凭据（{@code ?username=&password=}）。
     *
     * @param request HTTP 请求
     * @return 解析出的 {@link BasicPrincipal}，缺省密码时返回 null
     */
    protected static BasicPrincipal extractBasicAuthSubjectFromUrl(HttpRequest request) {
        QueryStringDecoder queryDecoder = new QueryStringDecoder(request.uri());
        Map<String, List<String>> parameters = queryDecoder.parameters();

        List<String> passwords = parameters.get(ArthasConstants.PASSWORD_KEY);
        if (passwords == null || passwords.size() == 0) {
            return null;
        }
        String password = passwords.get(0);

        String username = ArthasConstants.DEFAULT_USERNAME;
        List<String> usernames = parameters.get(ArthasConstants.USERNAME_KEY);
        if (usernames != null && !usernames.isEmpty()) {
            username = usernames.get(0);
        }
        BasicPrincipal principal = new BasicPrincipal(username, password);
        logger.debug("Extracted Basic Auth principal from url: {}", principal);
        return principal;
    }

    /**
     * 从 {@code Authorization: Basic ...} 头解析用户名与密码。
     * <p>
     * 仅支持 Basic 方案，不支持 Digest。
     *
     * @return 凭据 {@link BasicPrincipal}，无法解析时返回 null
     */
    protected static BasicPrincipal extractBasicAuthSubject(HttpRequest request) {
        String auth = request.headers().get(HttpHeaderNames.AUTHORIZATION);
        if (auth != null) {
            String constraint = StringUtils.before(auth, " ");
            if (constraint != null) {
                if ("Basic".equalsIgnoreCase(constraint.trim())) {
                    String decoded = StringUtils.after(auth, " ");
                    if (decoded == null) {
                        logger.error("Extracted Basic Auth principal failed, bad auth String: {}", auth);
                        return null;
                    }
                    // Base64 解码得到 user:password 明文
                    ByteBuf buf = Unpooled.wrappedBuffer(decoded.getBytes());
                    ByteBuf out = Base64.decode(buf);
                    String userAndPw = out.toString(Charset.defaultCharset());
                    String username = StringUtils.before(userAndPw, ":");
                    String password = StringUtils.after(userAndPw, ":");
                    BasicPrincipal principal = new BasicPrincipal(username, password);
                    logger.debug("Extracted Basic Auth principal from HTTP header: {}", principal);
                    return principal;
                }
            }
        }
        return null;
    }

    /**
     * 判断请求路径是否匹配 MCP 服务端点配置。
     *
     * @param request HTTP 请求
     */
    protected static boolean isMcpRequest(HttpRequest request) {
        try {
            String path = new java.net.URI(request.uri()).getPath();
            if (path == null) {
                return false;
            }

            String mcpEndpoint = ArthasBootstrap.getInstance().getConfigure().getMcpEndpoint();
            if (mcpEndpoint == null || mcpEndpoint.trim().isEmpty()) {
                // MCP 端点未配置则不走 MCP 认证分支
                return false;
            }
            
            return mcpEndpoint.equals(path);
        } catch (Exception e) {
            logger.debug("Failed to parse request URI: {}", request.uri(), e);
            return false;
        }
    }

    /**
     * 为 MCP 请求提取认证主体：Bearer → Basic 头 → URL 参数。
     *
     * @param request HTTP 请求
     */
    protected static Principal extractMcpAuthSubject(HttpRequest request) {
        // 优先尝试 Bearer Token
        BearerPrincipal tokenPrincipal = extractBearerTokenSubject(request);
        if (tokenPrincipal != null) {
            return tokenPrincipal;
        }

        // 其次尝试 Basic Auth 头
        BasicPrincipal basicPrincipal = extractBasicAuthSubject(request);
        if (basicPrincipal != null) {
            return basicPrincipal;
        }

        // 最后回退 URL 参数凭据
        return extractBasicAuthSubjectFromUrl(request);
    }

    /**
     * 从 {@code Authorization: Bearer ...} 头提取 Token。
     *
     * @param request HTTP 请求
     */
    protected static BearerPrincipal extractBearerTokenSubject(HttpRequest request) {
        String auth = request.headers().get(HttpHeaderNames.AUTHORIZATION);
        if (auth != null) {
            String constraint = StringUtils.before(auth, " ");
            if (constraint != null) {
                if ("Bearer".equalsIgnoreCase(constraint.trim())) {
                    String token = StringUtils.after(auth, " ");
                    if (token == null || token.trim().isEmpty()) {
                        logger.error("Extracted Bearer Token failed, bad auth String: {}", auth);
                        return null;
                    }
                    BearerPrincipal principal = new BearerPrincipal(token.trim());
                    logger.debug("Extracted Bearer Token principal: {}", principal);
                    return principal;
                }
            }
        }
        return null;
    }

}
