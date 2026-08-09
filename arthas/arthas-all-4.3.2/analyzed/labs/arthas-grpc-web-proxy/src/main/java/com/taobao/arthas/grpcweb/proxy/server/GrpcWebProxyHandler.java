package com.taobao.arthas.grpcweb.proxy.server;

import com.taobao.arthas.grpcweb.proxy.GrpcServiceConnectionManager;
import com.taobao.arthas.grpcweb.proxy.GrpcWebRequestHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Netty 入站处理器：接收 HTTP 请求并委托 {@link GrpcWebRequestHandler} 完成 gRPC-Web 代理。
 *
 * <p>每个 Handler 实例共享静态 {@link GrpcServiceConnectionManager}，连接本地 gRPC 后端端口。</p>
 */
public class GrpcWebProxyHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());
    /** 解析 URI、调用 Stub 的业务处理器 */
    private GrpcWebRequestHandler requestHandler;

    /** 与后端 gRPC 服务的连接管理器（类级共享） */
    private static GrpcServiceConnectionManager manager;

    /**
     * @param grpcPort 本地 gRPC 服务监听端口
     */
    public GrpcWebProxyHandler(int grpcPort) {
        manager = new GrpcServiceConnectionManager(grpcPort);
        requestHandler = new GrpcWebRequestHandler(manager);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        logger.debug("http request: {} ", request);

        send100Continue(ctx);
        requestHandler.handle(ctx, request);
    }

    /** 若客户端带 Expect: 100-continue，先回复 100 Continue。 */
    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE, Unpooled.EMPTY_BUFFER);
        ctx.write(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("grpc web proxy handler error", cause);
        ctx.close();
    }

}
