package com.alibaba.arthas.tunnel.server.app.web;

import java.net.URI;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UriComponentsBuilder;

import com.alibaba.arthas.tunnel.common.MethodConstants;
import com.alibaba.arthas.tunnel.common.SimpleHttpResponse;
import com.alibaba.arthas.tunnel.common.URIConstans;
import com.alibaba.arthas.tunnel.server.AgentInfo;
import com.alibaba.arthas.tunnel.server.TunnelServer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;

/**
 * 代理http请求到具体的 arthas agent里
 * 
 * @author hengyunabc 2020-10-22
 *
 */
@Controller
public class ProxyController {
    private final static Logger logger = LoggerFactory.getLogger(ProxyController.class);

    @Autowired
    TunnelServer tunnelServer;

    /**
     * 将 HTTP 请求经 WebSocket 转发至指定 agent，等待 agent 回传 HTTP 响应（最长 15 秒）。
     *
     * @param agentId 目标 agent 标识
     */
    @RequestMapping(value = "/proxy/{agentId}/**")
    @ResponseBody
    public ResponseEntity<?> execute(@PathVariable(name = "agentId", required = true) String agentId,
            HttpServletRequest request) throws InterruptedException, ExecutionException, TimeoutException {

        String fullPath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        // 去掉 /proxy/{agentId} 前缀，得到 agent 侧目标路径
        String targetUrl = fullPath.substring("/proxy/".length() + agentId.length());

        logger.info("http proxy, agentId: {}, targetUrl: {}", agentId, targetUrl);

        Optional<AgentInfo> findAgent = tunnelServer.findAgent(agentId);

        if (findAgent.isPresent()) {
            String requestId = RandomStringUtils.random(20, true, true).toUpperCase();

            ChannelHandlerContext agentCtx = findAgent.get().getChannelHandlerContext();

            Promise<SimpleHttpResponse> httpResponsePromise = GlobalEventExecutor.INSTANCE.newPromise();

            tunnelServer.addProxyRequestPromise(requestId, httpResponsePromise);

            // 通过 WebSocket 下发 HTTP 代理指令
            URI uri = UriComponentsBuilder.newInstance().scheme(URIConstans.RESPONSE).path("/")
                    .queryParam(URIConstans.METHOD, MethodConstants.HTTP_PROXY).queryParam(URIConstans.ID, agentId)
                    .queryParam(URIConstans.TARGET_URL, targetUrl).queryParam(URIConstans.PROXY_REQUEST_ID, requestId)
                    .build().toUri();

            agentCtx.channel().writeAndFlush(new TextWebSocketFrame(uri.toString()));
            logger.info("waitting for arthas agent http proxy, agentId: {}, targetUrl: {}", agentId, targetUrl);

            SimpleHttpResponse simpleHttpResponse = httpResponsePromise.get(15, TimeUnit.SECONDS);

            BodyBuilder bodyBuilder = ResponseEntity.status(simpleHttpResponse.getStatus());
            for (Entry<String, String> entry : simpleHttpResponse.getHeaders().entrySet()) {
                bodyBuilder.header(entry.getKey(), entry.getValue());
            }
            ResponseEntity<byte[]> responseEntity = bodyBuilder.body(simpleHttpResponse.getContent());
            return responseEntity;
        } else {
            logger.error("can not find agent by agentId: {}", agentId);
        }

        return ResponseEntity.notFound().build();
    }
}
