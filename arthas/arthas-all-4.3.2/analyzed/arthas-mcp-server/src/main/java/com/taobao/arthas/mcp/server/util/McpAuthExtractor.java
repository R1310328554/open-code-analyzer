package com.taobao.arthas.mcp.server.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 从 Netty 通道上下文与 HTTP 请求头中提取 MCP 认证信息的工具类。
 * <p>
 * 支持通过 Channel Attribute 或 HTTP Header 读写用户 ID 与认证主体（auth subject）。
 */
public class McpAuthExtractor {
    private static final Logger logger = LoggerFactory.getLogger(McpAuthExtractor.class);

    /** MCP 传输层上下文中认证主体的字符串键名。 */
    public static final String MCP_AUTH_SUBJECT_KEY = "mcp.auth.subject";
    /** MCP 用户 ID 的字符串键名。 */
    public static final String MCP_USER_ID_KEY = "mcp.user.id";
    /** HTTP 请求头中携带用户 ID 的 Header 名称。 */
    public static final String USER_ID_HEADER = "X-User-Id";
    
    /** Netty Channel 上存储认证主体的 AttributeKey。 */
    public static final AttributeKey<Object> CHANNEL_AUTH_SUBJECT_KEY = AttributeKey.valueOf("mcp.auth.subject");
    /** Netty Channel 上存储用户 ID 的 AttributeKey。 */
    public static final AttributeKey<String> CHANNEL_USER_ID_KEY = AttributeKey.valueOf("mcp.user.id");
    /** Arthas 认证主体在 Channel 上的 AttributeKey（与现有鉴权链兼容）。 */
    public static final AttributeKey<Object> SUBJECT_ATTRIBUTE_KEY =
            AttributeKey.valueOf("arthas.auth.subject");

    /**
     * 从 {@link ChannelHandlerContext} 关联的 Channel 中提取认证主体。
     * @param ctx Netty 处理器上下文
     * @return 认证主体对象，未找到或异常时返回 null
     */
    public static Object extractAuthSubjectFromContext(ChannelHandlerContext ctx) {
        if (ctx == null || ctx.channel() == null) {
            return null;
        }

        try {
            Object subject = ctx.channel().attr(SUBJECT_ATTRIBUTE_KEY).get();
            if (subject != null) {
                logger.debug("Extracted auth subject from channel context: {}", subject.getClass().getSimpleName());
                return subject;
            }
        } catch (Exception e) {
            logger.debug("Failed to extract auth subject from context: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 从 HTTP 请求头 {@link #USER_ID_HEADER} 中提取用户 ID。
     * @param request 完整 HTTP 请求
     * @return 去除空白后的用户 ID，未找到时返回 null
     */
    public static String extractUserIdFromRequest(FullHttpRequest request) {
        if (request == null) {
            return null;
        }
        
        String userId = request.headers().get(USER_ID_HEADER);
        if (userId != null && !userId.trim().isEmpty()) {
            logger.debug("Extracted userId from HTTP header {}: {}", USER_ID_HEADER, userId);
            return userId.trim();
        }
        
        return null;
    }

    /**
     * 从 Channel Attribute 中读取用户 ID。
     * @param channel Netty 通道
     */
    public static String extractUserId(Channel channel) {
        if (channel == null) {
            return null;
        }
        return channel.attr(CHANNEL_USER_ID_KEY).get();
    }

    /**
     * 将用户 ID 写入 Channel Attribute。
     * @param channel Netty 通道
     * @param userId 用户 ID，null 时不写入
     */
    public static void setUserId(Channel channel, String userId) {
        if (channel != null && userId != null) {
            channel.attr(CHANNEL_USER_ID_KEY).set(userId);
        }
    }

    /**
     * 从 Channel Attribute 中读取认证主体。
     * @param channel Netty 通道
     */
    public static Object extractAuthSubject(Channel channel) {
        if (channel == null) {
            return null;
        }
        return channel.attr(CHANNEL_AUTH_SUBJECT_KEY).get();
    }

    /**
     * 将认证主体写入 Channel Attribute。
     * @param channel Netty 通道
     * @param subject 认证主体对象，null 时不写入
     */
    public static void setAuthSubject(Channel channel, Object subject) {
        if (channel != null && subject != null) {
            channel.attr(CHANNEL_AUTH_SUBJECT_KEY).set(subject);
        }
    }
}
