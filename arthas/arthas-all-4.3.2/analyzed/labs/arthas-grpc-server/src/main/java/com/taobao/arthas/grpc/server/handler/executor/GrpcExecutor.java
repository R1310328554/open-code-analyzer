package com.taobao.arthas.grpc.server.handler.executor;

import com.taobao.arthas.grpc.server.handler.GrpcRequest;
import com.taobao.arthas.grpc.server.handler.constant.GrpcInvokeTypeEnum;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.Http2DataFrame;

/**
 * gRPC 请求执行器接口，按调用类型将 HTTP/2 数据帧分发给业务逻辑。
 * <p>
 * 各实现类由 {@link GrpcExecutorFactory} 按 {@link GrpcInvokeTypeEnum} 注册，
 * 在 Netty pipeline 收到 DATA 帧时被选中并执行。
 *
 * @author: FengYe
 * @date: 2024/10/24 01:50
 * @description: GrpcProcessor
 */
public interface GrpcExecutor {
    /**
     * 返回本执行器支持的 gRPC 调用类型。
     */
    GrpcInvokeTypeEnum supportGrpcType();

    /**
     * 处理一次 gRPC DATA 帧：解析请求、调用业务、写回 HTTP/2 响应帧。
     *
     * @param request 已解析的 gRPC 请求体
     * @param frame 原始 HTTP/2 DATA 帧，含 stream 与 endStream 信息
     * @param context Netty 通道上下文，用于写回响应
     */
    void execute(GrpcRequest request, Http2DataFrame frame, ChannelHandlerContext context) throws Throwable;
}
