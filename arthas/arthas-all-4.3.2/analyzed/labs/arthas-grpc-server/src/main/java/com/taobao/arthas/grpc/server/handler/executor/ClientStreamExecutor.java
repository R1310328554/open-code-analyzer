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
 * 客户端流 RPC 执行器：客户端分多帧发送请求，服务端汇总后返回单个响应。
 * <p>
 * 按 {@code streamId} 缓存请求侧 {@link StreamObserver}，逐帧 {@code onNext} 推送数据；
 * 收到 {@code END_STREAM} 后触发 {@code onCompleted}，由业务层写出最终响应。
 *
 * @author: FengYe
 * @date: 2024/10/24 01:51
 * @description: UnaryProcessor
 */
public class ClientStreamExecutor extends AbstractGrpcExecutor {

    public ClientStreamExecutor(GrpcDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public GrpcInvokeTypeEnum supportGrpcType() {
        return GrpcInvokeTypeEnum.CLIENT_STREAM;
    }

    @Override
    public void execute(GrpcRequest request, Http2DataFrame frame, ChannelHandlerContext context) throws Throwable {
        Integer streamId = request.getStreamId();

        StreamObserver<GrpcRequest> requestObserver = requestStreamObserverMap.computeIfAbsent(streamId, id->{
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
                return dispatcher.clientStreamExecute(request, responseObserver);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });

        requestObserver.onNext(request);
        if (frame.isEndStream()) {
            requestObserver.onCompleted();
        }
    }
}
