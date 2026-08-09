package com.taobao.arthas.grpc.server.handler.executor;

import com.taobao.arthas.grpc.server.handler.GrpcDispatcher;
import com.taobao.arthas.grpc.server.handler.GrpcRequest;
import com.taobao.arthas.grpc.server.handler.GrpcResponse;
import com.taobao.arthas.grpc.server.handler.StreamObserver;
import com.taobao.arthas.grpc.server.handler.constant.GrpcInvokeTypeEnum;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.DefaultHttp2DataFrame;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.http2.Http2DataFrame;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 服务端流 RPC 执行器：客户端发送单个请求，服务端通过 {@link StreamObserver} 多次推送响应。
 * <p>
 * 收到首帧即调用 {@link GrpcDispatcher#serverStreamExecute(GrpcRequest, StreamObserver)}，
 * 业务层每 {@code onNext} 一次即写出一帧 DATA，{@code onCompleted} 时发送带 END_STREAM 的尾帧。
 *
 * @author: FengYe
 * @date: 2024/10/24 01:51
 * @description: UnaryProcessor
 */
public class ServerStreamExecutor extends AbstractGrpcExecutor {

    public ServerStreamExecutor(GrpcDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public GrpcInvokeTypeEnum supportGrpcType() {
        return GrpcInvokeTypeEnum.SERVER_STREAM;
    }

    @Override
    public void execute(GrpcRequest request, Http2DataFrame frame, ChannelHandlerContext context) throws Throwable {
        StreamObserver<GrpcResponse> responseObserver = new StreamObserver<GrpcResponse>() {
            AtomicBoolean sendHeader = new AtomicBoolean(false);

            @Override
            public void onNext(GrpcResponse res) {
                // 每个 HTTP/2 stream 的 gRPC 响应头只能发送一次
                if (!sendHeader.get()) {
                    sendHeader.compareAndSet(false, true);
                    context.writeAndFlush(new DefaultHttp2HeadersFrame(res.getEndHeader()).stream(frame.stream()));
                }
                context.writeAndFlush(new DefaultHttp2DataFrame(res.getResponseData()).stream(frame.stream()));
            }

            @Override
            public void onCompleted() {
                context.writeAndFlush(new DefaultHttp2HeadersFrame(GrpcResponse.getDefaultEndStreamHeader(), true).stream(frame.stream()));
            }
        };
        try {
            dispatcher.serverStreamExecute(request, responseObserver);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
