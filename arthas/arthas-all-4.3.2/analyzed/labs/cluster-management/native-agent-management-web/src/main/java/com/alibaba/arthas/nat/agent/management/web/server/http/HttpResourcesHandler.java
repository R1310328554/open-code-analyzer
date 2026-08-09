package com.alibaba.arthas.nat.agent.management.web.server.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 静态资源 HTTP 处理器，从 classpath 提供 Management Web 前端页面及静态文件。
 *
 * @description: HttpResourcesHandler
 * @author：flzjkl
 * @date: 2024-09-23 7:44
 */
public class HttpResourcesHandler {

    private static final Logger logger = LoggerFactory.getLogger(HttpResourcesHandler.class);
    /** classpath 下静态资源的根目录 */
    private static final String RESOURCES_BASE_PATH = "/native-agent";
    /** 允许访问的文件扩展名白名单 */
    private static final Set<String> ALLOWED_EXTENSIONS;

    static {
        Set<String> tempSet = new HashSet<>();
        tempSet.add(".html");
        tempSet.add(".css");
        tempSet.add(".js");
        tempSet.add(".ico");
        tempSet.add(".png");
        ALLOWED_EXTENSIONS = Collections.unmodifiableSet(tempSet);
    }

    /**
     * 根据请求路径加载 classpath 静态资源并构造 HTTP 响应。
     *
     * @param request 原始 HTTP 请求
     * @param path    已解析的 URI 路径
     */
    public FullHttpResponse handlerResources(FullHttpRequest request, String path) {
        try {
            if (request == null || path == null) {
                return null;
            }
            String normalizedPath = normalizePath(path);
            if (normalizedPath == null) {
                return null;
            }
            URL resourceUrl = getClass().getResource(RESOURCES_BASE_PATH + normalizedPath);
            if (resourceUrl == null) {
                return null;
            }
            try (InputStream is = resourceUrl.openStream()) {
                if (is == null) {
                    return null;
                }

                ByteBuf content = readInputStream(is);
                FullHttpResponse response = new DefaultFullHttpResponse(
                        request.protocolVersion(), HttpResponseStatus.OK, content);

                HttpHeaders headers = response.headers();
                headers.set(HttpHeaderNames.CONTENT_TYPE, getContentType(normalizedPath));
                headers.setInt(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
                headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

                return response;
            }
        } catch (Exception e) {
            logger.error("");
            return null;
        }
    }

    /**
     * 规范化并校验请求路径，过滤目录穿越并限制扩展名白名单。
     */
    private String normalizePath(String path) {
        if (path == null) {
            return null;
        }

        // 移除 ../ 与 ./ 防止路径穿越
        path = path.replaceAll("\\.\\./", "").replaceAll("\\./", "");


        path = path.startsWith("/") ? path : "/" + path;


        path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;


        String finalPath = path;
        boolean hasAllowedExtension = ALLOWED_EXTENSIONS.stream()
                .anyMatch(finalPath::endsWith);

        if (!hasAllowedExtension) {
            return null;
        }

        return path;
    }

    /** 按文件扩展名返回 Content-Type */
    private String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".ico")) return "image/x-icon";
        if (path.endsWith(".png")) return "image/png";
        return "application/octet-stream";
    }

    /** 将 InputStream 读入 Netty ByteBuf */
    private ByteBuf readInputStream(InputStream is) throws IOException {
        ByteBuf buffer = Unpooled.buffer();
        byte[] tmp = new byte[1024];
        int length;
        while ((length = is.read(tmp)) != -1) {
            buffer.writeBytes(tmp, 0, length);
        }
        is.close();
        return buffer;
    }

}
