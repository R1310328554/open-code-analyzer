package com.alibaba.arthas.tunnel.client;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.arthas.tunnel.common.SimpleHttpResponse;
import com.taobao.arthas.common.ArthasConstants;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;

/**
 * 经 Netty LocalChannel 向本机 Arthas Server 发起 HTTP GET 代理查询，
 * 将响应封装为 {@link SimpleHttpResponse} 返回。
 *
 * @author hengyunabc 2020-10-22
 *
 */
public class ProxyClient {
    private static final Logger logger = LoggerFactory.getLogger(ProxyClient.class);

    /**
     * 对本地 Arthas Server 执行 GET 请求并等待响应（超时 5 秒）。
     *
     * @param targetUrl 请求路径（相对本地 Server）
     * @return HTTP 响应；异常时返回 content 为 "error" 的占位对象
     */
    public SimpleHttpResponse query(String targetUrl) throws InterruptedException {
        final Promise<SimpleHttpResponse> httpResponsePromise = GlobalEventExecutor.INSTANCE.newPromise();

        final EventLoopGroup group = new NioEventLoopGroup(1, new DefaultThreadFactory("arthas-ProxyClient", true));
        ChannelFuture closeFuture = null;
        try {
            Bootstrap b = new Bootstrap();
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
            b.group(group).channel(LocalChannel.class).handler(new ChannelInitializer<LocalChannel>() {
                @Override
                protected void initChannel(LocalChannel ch) {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new HttpClientCodec(), new HttpObjectAggregator(ArthasConstants.MAX_HTTP_CONTENT_LENGTH),
                            new HttpProxyClientHandler(httpResponsePromise));
                }
            });

            LocalAddress localAddress = new LocalAddress(ArthasConstants.NETTY_LOCAL_ADDRESS);
            Channel localChannel = b.connect(localAddress).sync().channel();

            // 构造 HTTP GET 请求
            HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, targetUrl,
                    Unpooled.EMPTY_BUFFER);
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);

            localChannel.writeAndFlush(request);

            closeFuture = localChannel.closeFuture();
            logger.info("proxy client connect to server success, targetUrl: " + targetUrl);

            return httpResponsePromise.get(5000, TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            logger.error("ProxyClient error, targetUrl: {}", targetUrl, e);
        } finally {
            if (closeFuture != null) {
                closeFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        group.shutdownGracefully();
                    }
                });
            } else {
                group.shutdownGracefully();
            }
        }

        SimpleHttpResponse httpResponse = new SimpleHttpResponse();
        try {
            httpResponse.setContent("error".getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            // ignore
        }
        return httpResponse;
    }

    /** 解析 HTTP 响应头与 body，完成后通过 Promise 交付结果 */
    static class HttpProxyClientHandler extends SimpleChannelInboundHandler<HttpObject> {

        private Promise<SimpleHttpResponse> promise;

        private SimpleHttpResponse simpleHttpResponse = new SimpleHttpResponse();

        public HttpProxyClientHandler(Promise<SimpleHttpResponse> promise) {
            this.promise = promise;
        }

        @Override
        public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
            if (msg instanceof HttpResponse) {
                HttpResponse response = (HttpResponse) msg;

                simpleHttpResponse.setStatus(response.status().code());
                if (!response.headers().isEmpty()) {
                    for (String name : response.headers().names()) {
                        for (String value : response.headers().getAll(name)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("header: {}, value: {}", name, value);
                            }

                            simpleHttpResponse.addHeader(name, value);
                        }
                    }
                }
            }
            if (msg instanceof HttpContent) {
                HttpContent content = (HttpContent) msg;

                ByteBuf byteBuf = null;
                try{
                    byteBuf = content.content();
                    byte[] bytes = new byte[byteBuf.readableBytes()];
                    byteBuf.readBytes(bytes);

                    simpleHttpResponse.setContent(bytes);

                    promise.setSuccess(simpleHttpResponse);

                    if (content instanceof LastHttpContent) {
                        ctx.close();
                    }
                }finally {
                    if (byteBuf != null) {
                        byteBuf.release();
                    }
                }

            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            logger.error("Proxy Client error", cause);
            ctx.close();
        }
    }
}
