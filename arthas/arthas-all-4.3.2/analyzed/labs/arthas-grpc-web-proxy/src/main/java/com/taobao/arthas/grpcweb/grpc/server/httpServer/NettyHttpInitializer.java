package com.taobao.arthas.grpcweb.grpc.server.httpServer;
 
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * Netty HTTP 通道初始化器，组装静态资源服务所需的编解码与业务 Handler。
 */
public class NettyHttpInitializer extends ChannelInitializer<SocketChannel> {

    /** 静态文件根目录绝对路径 */
    private final String STATIC_LOCATION;

    public NettyHttpInitializer(String staticLocation) {
        this.STATIC_LOCATION = staticLocation;
    }

    /**
     * 配置 pipeline：HTTP 编解码 → 消息聚合 → 分块写 → 静态文件 Handler。
     */
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 将请求和应答消息编码或解码为 HTTP 消息
        pipeline.addLast(new HttpServerCodec());
        // 将 HTTP 消息的多个部分组合成一条完整的 HTTP 消息
        pipeline.addLast(new HttpObjectAggregator(64 * 1024));
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new NettyHttpStaticFileHandler(this.STATIC_LOCATION));
    }
}
