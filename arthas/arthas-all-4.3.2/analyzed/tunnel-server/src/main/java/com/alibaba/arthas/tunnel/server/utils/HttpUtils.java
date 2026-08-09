package com.alibaba.arthas.tunnel.server.utils;

import io.netty.handler.codec.http.HttpHeaders;

/**
 * HTTP 请求头工具：从代理/网关透传的头部解析客户端真实 IP 与端口。
 *
 * @author hengyunabc 2021-02-26
 *
 */
public class HttpUtils {

    /**
     * 从 {@code X-Forwarded-For} 解析客户端 IP（多级代理时取第一个）。
     *
     * @param headers Netty HTTP 头
     * @return 客户端 IP，未设置时返回 null
     */
    public static String findClientIP(HttpHeaders headers) {
        String hostStr = headers.get("X-Forwarded-For");
        if (hostStr == null) {
            return null;
        }
        // 逗号分隔的代理链，取最左侧即原始客户端
        int index = hostStr.indexOf(',');
        if (index > 0) {
            hostStr = hostStr.substring(0, index);
        }
        return hostStr;
    }

    /**
     * 从 {@code X-Real-Port} 解析客户端端口。
     *
     * @param headers Netty HTTP 头
     * @return 端口号，未设置时返回 null
     */
    public static Integer findClientPort(HttpHeaders headers) {
        String portStr = headers.get("X-Real-Port");
        if (portStr != null) {
            return Integer.parseInt(portStr);
        }
        return null;
    }
}
