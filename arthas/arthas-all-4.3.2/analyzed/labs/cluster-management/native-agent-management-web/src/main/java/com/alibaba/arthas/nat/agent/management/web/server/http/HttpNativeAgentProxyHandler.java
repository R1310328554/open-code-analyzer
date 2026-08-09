package com.alibaba.arthas.nat.agent.management.web.server.http;

import com.alibaba.arthas.nat.agent.management.web.discovery.NativeAgentProxyDiscovery;
import com.alibaba.arthas.nat.agent.management.web.factory.NativeAgentProxyDiscoveryFactory;
import com.alibaba.arthas.nat.agent.management.web.server.NativeAgentManagementWebBootstrap;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 处理 {@code /api/native-agent-proxy} 请求，从注册中心发现 Proxy 并返回可用地址。
 *
 * @description: HttpNativeAgentProxyHandler
 * @author：flzjkl
 * @date: 2024-10-21 7:01
 */
public class HttpNativeAgentProxyHandler {

    /**
     * 根据 operation 字段路由到具体处理逻辑。
     */
    public FullHttpResponse handle(ChannelHandlerContext ctx, FullHttpRequest request) {
        String content = request.content().toString(StandardCharsets.UTF_8);
        Map<String, Object> bodyMap = JSON.parseObject(content, new TypeReference<Map<String, Object>>() {
        });
        String operation = (String) bodyMap.get("operation");

        if ("findAvailableProxyAddress".equals(operation)) {
            return responseFindAvailableProxyAddress(ctx, request);
        }

        return null;
    }


    /** 查询可用 Proxy 地址并以 HTTP 200 响应返回 */
    public FullHttpResponse responseFindAvailableProxyAddress(ChannelHandlerContext ctx, FullHttpRequest request) {
        String availableProxyAddress = findAvailableProxyAddress();
        if (availableProxyAddress == null || "".equals(availableProxyAddress)) {
            return null;
        }
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                request.getProtocolVersion(),
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(availableProxyAddress, StandardCharsets.UTF_8)
        );
        return response;
    }


    /**
     * 从注册中心拉取 Proxy 列表并随机选取一个地址，用于简单负载均衡。
     *
     * @return 形如 127.0.0.1:2233 的代理地址，无可用节点时返回 null
     */
    public String findAvailableProxyAddress() {
        // 通过 SPI 工厂获取对应注册类型的发现实现
        NativeAgentProxyDiscoveryFactory proxyDiscoveryFactory = NativeAgentProxyDiscoveryFactory.getNativeAgentProxyDiscoveryFactory();
        NativeAgentProxyDiscovery proxyDiscovery = proxyDiscoveryFactory.getNativeAgentProxyDiscovery(NativeAgentManagementWebBootstrap.registrationType);
        List<String> proxyList = proxyDiscovery.listNativeAgentProxy(NativeAgentManagementWebBootstrap.registrationAddress);
        if (proxyList == null || proxyList.size() == 0) {
            return null;
        }
        // 随机选取一个 Proxy 地址
        Random random = new Random();
        int randomIndex = random.nextInt(proxyList.size());
        return proxyList.get(randomIndex);
    }


}
