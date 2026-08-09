package com.taobao.arthas.grpcweb.proxy.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * gRPC-Web 代理服务端 Pipeline 初始化器。
 *
 * <p>装配顺序：HTTP 编解码 → 请求聚合（最大 64KB）→ 分块写 → {@link GrpcWebProxyHandler}。</p>
 */
public class GrpcWebProxyServerInitializer extends ChannelInitializer<SocketChannel> {

    /** 转发目标本地 gRPC 端口 */
    private int grpcPort;

    public GrpcWebProxyServerInitializer(int grpcPort) {
        this.grpcPort = grpcPort;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new GrpcWebProxyHandler(grpcPort));
    }
}
