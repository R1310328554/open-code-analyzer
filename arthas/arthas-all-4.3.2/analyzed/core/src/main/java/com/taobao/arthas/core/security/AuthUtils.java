package com.taobao.arthas.core.security;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.Principal;

import com.taobao.arthas.core.config.Configure;
import com.taobao.arthas.core.server.ArthasBootstrap;

import io.netty.channel.ChannelHandlerContext;

/**
 * Arthas 连接认证辅助工具：识别本地回环连接并授予免密主体。
 * <p>
 * 当配置 {@link Configure#isLocalConnectionNonAuth()} 为 true 且客户端来自 127.0.0.1 时，
 * 返回 {@link LocalConnectionPrincipal}，否则返回 null 由常规鉴权流程处理。
 *
 * @author hengyunabc 2021-09-01
 */
public class AuthUtils {
    private static Configure configure = ArthasBootstrap.getInstance().getConfigure();

    /**
     * 若允许本地免认证且为本地连接，返回本地连接主体。
     * @param ctx Netty 通道上下文
     */
    public static Principal localPrincipal(ChannelHandlerContext ctx) {
        if (configure.isLocalConnectionNonAuth() && isLocalConnection(ctx)) {
            return new LocalConnectionPrincipal();
        }
        return null;
    }

    /** 判断远程地址是否为 IPv4 回环 127.0.0.1 */
    public static boolean isLocalConnection(ChannelHandlerContext ctx) {
        SocketAddress remoteAddress = ctx.channel().remoteAddress();
        if (remoteAddress instanceof InetSocketAddress) {
            String hostAddress = ((InetSocketAddress) remoteAddress).getAddress().getHostAddress();
            if ("127.0.0.1".equals(hostAddress)) {
                return true;
            }
        }
        return false;
    }
}
