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
 * 双向流 RPC 执行器：同一 HTTP/2 stream 上客户端与服务端均可多次收发消息。
 * <p>
 * 首次收到 DATA 帧时创建请求/响应 {@link StreamObserver} 对并交给
 * {@link GrpcDispatcher#biStreamExecute(GrpcRequest, StreamObserver)}；
 * 后续帧通过 {@code onNext} 推送请求片段，{@code END_STREAM} 时调用 {@code onCompleted}。
 *
 * @author: FengYe
 * @date: 2024/10/24 01:52
 * @description: BiStreamProcessor
 */
public class BiStreamExecutor extends AbstractGrpcExecutor {

    public BiStreamExecutor(GrpcDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public GrpcInvokeTypeEnum supportGrpcType() {
        return GrpcInvokeTypeEnum.BI_STREAM;
    }

    @Override
    public void execute(GrpcRequest request, Http2DataFrame frame, ChannelHandlerContext context) throws Throwable {
        Integer streamId = request.getStreamId();

        // 同一 streamId 复用请求观察者，首次帧时建立双向流回调链
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
                return dispatcher.biStreamExecute(request, responseObserver);
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
