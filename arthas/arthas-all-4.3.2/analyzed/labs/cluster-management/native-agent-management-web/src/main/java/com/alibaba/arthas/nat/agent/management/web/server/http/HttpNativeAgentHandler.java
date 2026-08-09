package com.alibaba.arthas.nat.agent.management.web.server.http;

import com.alibaba.arthas.nat.agent.common.utils.OkHttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 处理 Management Web 侧 {@code /api/native-agent} 请求，代理转发至 Native Agent Proxy 查询 Agent 列表。
 *
 * @description: HttpNativeAgentHandler
 * @author：flzjkl
 * @date: 2024-08-01 7:32
 */
public class HttpNativeAgentHandler {

    private static final Logger logger = LoggerFactory.getLogger(HttpNativeAgentHandler.class);

    private static HttpNativeAgentProxyHandler httpNativeAgentProxyHandler = new HttpNativeAgentProxyHandler();

    /**
     * 根据请求体中的 operation 字段分发到对应处理逻辑。
     */
    public FullHttpResponse handle(ChannelHandlerContext ctx, FullHttpRequest request) {
        String content = request.content().toString(StandardCharsets.UTF_8);
        Map<String, Object> bodyMap = JSON.parseObject(content, new TypeReference<Map<String, Object>>() {
        });
        String operation = (String) bodyMap.get("operation");

        if ("listNativeAgent".equals(operation)) {
            return doListNativeAgent(ctx, request);
        }
        return null;
    }

    /** 从注册中心选取可用 Proxy，再向其转发 listNativeAgent 请求 */
    private FullHttpResponse doListNativeAgent(ChannelHandlerContext ctx, FullHttpRequest request) {
        // 1、从注册中心获取一个可用的 Native Agent Proxy 地址
        String address = httpNativeAgentProxyHandler.findAvailableProxyAddress();
        if (address == null || "".equals(address)) {
            return null;
        }
        // 2、向 Proxy 发送 HTTP 请求获取 Native Agent 列表
        String resStr = null;
        try {
            String url = "http://" + address + "/api/native-agent-proxy";
            String jsonBody = "{\"operation\":\"listNativeAgent\"}";
            resStr = OkHttpUtil.post(url, jsonBody);
        } catch (IOException e) {
            logger.error("Send http to native agent proxy failed");
            throw new RuntimeException(e);
        }
        if (resStr == null) {
            return null;
        }
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                request.getProtocolVersion(),
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(resStr, StandardCharsets.UTF_8)
        );

        return response;
    }

}
