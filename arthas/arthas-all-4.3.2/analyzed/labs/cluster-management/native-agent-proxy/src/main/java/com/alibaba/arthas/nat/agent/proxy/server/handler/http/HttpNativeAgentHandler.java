package com.alibaba.arthas.nat.agent.proxy.server.handler.http;


import com.alibaba.arthas.nat.agent.common.dto.NativeAgentInfoDTO;
import com.alibaba.arthas.nat.agent.proxy.discovery.NativeAgentDiscovery;
import com.alibaba.arthas.nat.agent.proxy.factory.NativeAgentDiscoveryFactory;
import com.alibaba.arthas.nat.agent.proxy.server.NativeAgentProxyBootstrap;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import okhttp3.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Native Agent Proxy HTTP 业务处理器：列举 Agent、转发进程查询与监控请求。
 *
 * @description: HttpNativeAgentHandler
 * @author：flzjkl
 * @date: 2024-08-01 7:32
 */
public class HttpNativeAgentHandler {

    /** 本地缓存（预留） */
    private Map<Long, String> localCache = new ConcurrentHashMap<>();

    /**
     * 根据请求体 operation 字段分发到具体业务逻辑。
     */
    public FullHttpResponse handle(ChannelHandlerContext ctx, FullHttpRequest request) {
        String content = request.content().toString(StandardCharsets.UTF_8);
        Map<String, Object> bodyMap = JSON.parseObject(content, new TypeReference<Map<String, Object>>() {
        });
        String operation = (String) bodyMap.get("operation");

        if ("listNativeAgent".equals(operation)) {
            return doListNativeAgent(ctx, request);
        }

        if ("listProcess".equals(operation)) {
            String address = (String) bodyMap.get("agentAddress");
            return forwardRequest(request, address);
        }

        if ("monitor".equals(operation)) {
            String address = (String) bodyMap.get("agentAddress");
            return forwardRequest(request, address);
        }

        return null;
    }



    /**
     * 将请求透传至目标 Native Agent 的 /api/native-agent 接口。
     *
     * @param address 目标 Agent 地址（ip:port）
     */
    private FullHttpResponse forwardRequest(FullHttpRequest request, String address) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        String url = "http://" + address + "/api/native-agent";

        RequestBody requestBody = RequestBody.create(
                request.content().toString(CharsetUtil.UTF_8),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request okRequest = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try {
            Response response = client.newCall(okRequest).execute();

            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                DefaultFullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
                        request.getProtocolVersion(),
                        HttpResponseStatus.OK,
                        Unpooled.copiedBuffer(responseBody, StandardCharsets.UTF_8)
                );
                // 设置跨域响应头
                fullHttpResponse.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                fullHttpResponse.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS");
                fullHttpResponse.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "X-Requested-With, Content-Type, Authorization");

                // 设置 Content-Type 与 Content-Length
                fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
                fullHttpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, fullHttpResponse.content().readableBytes());
                return fullHttpResponse;
            } else {
                return new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.valueOf(response.code()),
                        Unpooled.copiedBuffer("Error: " + response.message(), CharsetUtil.UTF_8)
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    Unpooled.copiedBuffer("Error forwarding request: " + e.getMessage(), CharsetUtil.UTF_8)
            );
        }
    }

    /**
     * 从 Agent 注册中心查询所有 Native Agent 并组装为 DTO 列表返回。
     */
    private FullHttpResponse doListNativeAgent(ChannelHandlerContext ctx, FullHttpRequest request) {
        NativeAgentDiscoveryFactory nativeAgentDiscoveryFactory = NativeAgentDiscoveryFactory.getNativeAgentDiscoveryFactory();
        NativeAgentDiscovery nativeAgentDiscovery = nativeAgentDiscoveryFactory.getNativeAgentDiscovery(NativeAgentProxyBootstrap.agentRegistrationType);
        Map<String, String> nativeAgentMap = nativeAgentDiscovery.findNativeAgent(NativeAgentProxyBootstrap.agentRegistrationAddress);

        List<NativeAgentInfoDTO> nativeAgentInfoList = new ArrayList<>();

        for (Map.Entry<String, String> entry : nativeAgentMap.entrySet()) {
            String nativeAgentIp = entry.getKey();
            String value = entry.getValue();
            String[] split = value.split(":");
            nativeAgentInfoList.add(new NativeAgentInfoDTO(nativeAgentIp, Integer.valueOf(split[0]), Integer.valueOf(split[1])));
        }

        String nativeAgentInfoStr = JSON.toJSONString(nativeAgentInfoList);

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                request.getProtocolVersion(),
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(nativeAgentInfoStr, StandardCharsets.UTF_8)
        );

        // 设置跨域响应头
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "X-Requested-With, Content-Type, Authorization");

        // 设置 Content-Type 与 Content-Length
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

        return response;
    }

}
