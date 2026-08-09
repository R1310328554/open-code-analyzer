package com.taobao.arthas.grpc.server.handler;


import arthas.grpc.common.ArthasGrpc;
import com.alibaba.arthas.deps.org.slf4j.Logger;
import com.alibaba.arthas.deps.org.slf4j.LoggerFactory;
import com.taobao.arthas.grpc.server.handler.executor.GrpcExecutorFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http2.*;
import io.netty.util.concurrent.EventExecutorGroup;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Netty HTTP/2 帧处理器，将 HEADERS/DATA/RESET 帧转为 gRPC 调度。
 * <p>
 * 按 streamId 缓冲 {@link GrpcRequest}，DATA 到达后提交到
 * {@link com.taobao.arthas.grpc.server.handler.executor.GrpcExecutorFactory} 异步执行。
 *
 * @author: FengYe
 * @date: 2024/7/7 下午9:58
 * @description: Http2Handler
 */
public class Http2Handler extends SimpleChannelInboundHandler<Http2Frame> {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

    private GrpcDispatcher grpcDispatcher;

    private GrpcExecutorFactory grpcExecutorFactory;

    private final EventExecutorGroup executorGroup = new NioEventLoopGroup();

    /**
     * 暂存收到的所有请求的数据
     */
    private ConcurrentHashMap<Integer, GrpcRequest> dataBuffer = new ConcurrentHashMap<>();

    /** HTTP/2 伪头：gRPC 路径 {@code /Service/Method}。 */
    private static final String HEADER_PATH = ":path";

    /** @param grpcDispatcher 方法路由 @param grpcExecutorFactory 按调用类型选择执行器 */
    public Http2Handler(GrpcDispatcher grpcDispatcher, GrpcExecutorFactory grpcExecutorFactory) {
        this.grpcDispatcher = grpcDispatcher;
        this.grpcExecutorFactory = grpcExecutorFactory;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    @Override
    /** 分发 HEADERS / DATA / RESET 三类 HTTP/2 帧。 */
    protected void channelRead0(ChannelHandlerContext ctx, Http2Frame frame) throws IOException {
        if (frame instanceof Http2HeadersFrame) {
            handleGrpcRequest((Http2HeadersFrame) frame, ctx);
        } else if (frame instanceof Http2DataFrame) {
            handleGrpcData((Http2DataFrame) frame, ctx);
        } else if (frame instanceof Http2ResetFrame) {
            handleResetStream((Http2ResetFrame) frame, ctx);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    /** 解析 :path 创建 GrpcRequest 并写入 stream 缓冲。 */
    private void handleGrpcRequest(Http2HeadersFrame headersFrame, ChannelHandlerContext ctx) {
        int id = headersFrame.stream().id();
        String path = headersFrame.headers().get(HEADER_PATH).toString();
        // 去掉 :path 前导 '/'，按 '/' 拆分为 service 与 method
        String[] parts = path.substring(1).split("/");
        GrpcRequest grpcRequest = new GrpcRequest(headersFrame.stream().id(), parts[0], parts[1]);
        grpcRequest.setHeaders(headersFrame.headers());
        GrpcDispatcher.checkGrpcType(grpcRequest);
        dataBuffer.put(id, grpcRequest);
        System.out.println("Received headers: " + headersFrame.headers());
    }

    /** 追加 DATA 负载并异步交给对应 GrpcExecutor 处理。 */
    private void handleGrpcData(Http2DataFrame dataFrame, ChannelHandlerContext ctx) throws IOException {
        int streamId = dataFrame.stream().id();
        GrpcRequest grpcRequest = dataBuffer.get(streamId);
        ByteBuf content = dataFrame.content();
        grpcRequest.writeData(content);

        executorGroup.execute(() -> {
            try {
                grpcExecutorFactory.getExecutor(grpcRequest.getGrpcType()).execute(grpcRequest, dataFrame, ctx);
            } catch (Throwable e) {
                logger.error("handleGrpcData error", e);
                processError(ctx, e, dataFrame.stream());
            }
        });
    }

    /** 客户端 RESET 时清理 stream 缓冲。 */
    private void handleResetStream(Http2ResetFrame resetFrame, ChannelHandlerContext ctx) {
        int id = resetFrame.stream().id();
        System.out.println("handleResetStream");
        dataBuffer.remove(id);
    }

    /** 构造 ErrorRes 并以 gRPC 标准三帧（头 + 数据 + 尾）写回错误。 */
    private void processError(ChannelHandlerContext ctx, Throwable e, Http2FrameStream stream) {
        GrpcResponse response = new GrpcResponse();
        ArthasGrpc.ErrorRes.Builder builder = ArthasGrpc.ErrorRes.newBuilder();
        ArthasGrpc.ErrorRes errorRes = builder.setErrorMsg(Optional.ofNullable(e.getMessage()).orElse("")).build();
        response.setClazz(ArthasGrpc.ErrorRes.class);
        response.writeResponseData(errorRes);
        ctx.writeAndFlush(new DefaultHttp2HeadersFrame(response.getEndHeader()).stream(stream));
        ctx.writeAndFlush(new DefaultHttp2DataFrame(response.getResponseData()).stream(stream));
        ctx.writeAndFlush(new DefaultHttp2HeadersFrame(response.getEndStreamHeader(), true).stream(stream));
    }
}