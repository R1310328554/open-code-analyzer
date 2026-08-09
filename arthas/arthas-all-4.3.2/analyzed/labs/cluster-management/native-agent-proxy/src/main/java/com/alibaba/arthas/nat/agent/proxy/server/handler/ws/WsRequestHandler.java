package com.alibaba.arthas.nat.agent.proxy.server.handler.ws;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 代理处理器：解析目标 Agent 地址，建立出站连接并双向转发帧。
 *
 * @description: hello world
 * @author：flzjkl
 * @date: 2024-10-20 11:26
 */
public class WsRequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(WsRequestHandler.class);
    /** 入站 Channel → 出站 Channel 映射 */
    private final ConcurrentHashMap<Channel, Channel> channelMappings = new ConcurrentHashMap<>();

    /**
     * 处理 WebSocket 帧：关闭帧直接断开；否则确保出站连接已建立后转发。
     */
    public void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) {
            closeOutboundChannel(ctx.channel());
            ctx.close();
            return;
        }

        Channel outboundChannel = channelMappings.get(ctx.channel());
        if (outboundChannel == null || !outboundChannel.isActive()) {
            connectToDestinationServer(ctx, frame);
        } else {
            forwardWebSocketFrame(frame, outboundChannel);
        }
    }

    /**
     * 懒连接目标 Native Agent WebSocket 端点，握手成功后缓存映射并转发首帧。
     */
    private void connectToDestinationServer(ChannelHandlerContext ctx, WebSocketFrame frame) {
        String nativeAgentAddress = (String) ctx.channel().attr(AttributeKey.valueOf("nativeAgentAddress")).get();
        Bootstrap b = new Bootstrap();
        b.group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(new HttpClientCodec());
                        p.addLast(new HttpObjectAggregator(65536));
                        p.addLast(new WebSocketClientProtocolHandler(
                                WebSocketClientHandshakerFactory.newHandshaker(
                                        URI.create("ws://"+ nativeAgentAddress +"/ws"),
                                        WebSocketVersion.V13, null, false, new DefaultHttpHeaders())));
                        p.addLast(new WebSocketClientHandler(ctx.channel()));
                    }
                });
        String[] addressSplit = nativeAgentAddress.split(":");
        ChannelFuture f = b.connect(addressSplit[0], Integer.parseInt(addressSplit[1]));
        f.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                Channel outboundChannel = future.channel();
                channelMappings.put(ctx.channel(), outboundChannel);
                forwardWebSocketFrame(frame, outboundChannel);
            } else {
                logger.error("Failed to connect to destination server", future.cause());
                ctx.close();
            }
        });
    }

    /** 将 WebSocket 帧写入出站 Channel（保留引用计数） */
    private void forwardWebSocketFrame(WebSocketFrame frame, Channel outboundChannel) {
        if (outboundChannel != null && outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(frame.retain()).addListener(future -> {
                if (!future.isSuccess()) {
                    logger.error("Failed to forward WebSocket frame", future.cause());
                }
            });
        } else {
            logger.warn("Outbound channel is not active. Cannot forward frame.");
        }
    }

    /** 关闭并移除入站 Channel 对应的出站连接 */
    private void closeOutboundChannel(Channel inboundChannel) {
        Channel outboundChannel = channelMappings.remove(inboundChannel);
        if (outboundChannel != null) {
            logger.info("Closing outbound channel");
            outboundChannel.close();
        }
    }

    /** 入站 Channel 失活时清理出站连接 */
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.info("Channel inactive, closing outbound channel");
        closeOutboundChannel(ctx.channel());
    }

    /**
     * 处理 WebSocket 升级握手：从查询参数提取 nativeAgentAddress 并写入 Channel 属性。
     */
    public void handleWebSocketUpgrade(ChannelHandlerContext ctx, FullHttpRequest request) {
        URI uri = null;
        try {
            uri = new URI(request.uri());
        } catch (URISyntaxException e) {
            // URI 解析失败，忽略本次升级
            return;
        }

        Map<String, String> params = parseQueryString(uri.getQuery());

        String nativeAgentAddress = params.get("nativeAgentAddress");

        if (nativeAgentAddress != null) {
            ctx.channel().attr(AttributeKey.valueOf("nativeAgentAddress")).set(nativeAgentAddress);
        }

        request.setUri(uri.getPath());

        ctx.fireChannelRead(request.retain());
    }

    /** 解析 URL 查询字符串为键值对 */
    private Map<String, String> parseQueryString(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                try {
                    String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
                    String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
                    params.put(key, value);
                } catch (UnsupportedEncodingException e) {
                    // 解码失败，跳过该参数
                }
            }
        }
        return params;
    }
}
