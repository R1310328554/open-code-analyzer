package com.taobao.arthas.grpc.server.handler.executor;

import com.taobao.arthas.grpc.server.handler.GrpcDispatcher;
import com.taobao.arthas.grpc.server.handler.GrpcRequest;
import com.taobao.arthas.grpc.server.handler.StreamObserver;

import java.util.concurrent.ConcurrentHashMap;

/**
 * gRPC 执行器抽象基类，封装调度器引用与流式请求的观察者缓存。
 * <p>
 * 客户端流与双向流需要在同一 HTTP/2 stream 上多次接收请求帧，
 * 因此按 {@code streamId} 维护 {@link StreamObserver} 实例直至流结束。
 *
 * @author: FengYe
 * @date: 2024/10/24 02:07
 * @description: AbstractGrpcExecutor
 */
public abstract class AbstractGrpcExecutor implements GrpcExecutor{
    /** 负责反射调用业务方法的 gRPC 调度器 */
    protected GrpcDispatcher dispatcher;

    /** 按 HTTP/2 streamId 缓存请求侧 StreamObserver，供流式 RPC 复用 */
    protected ConcurrentHashMap<Integer, StreamObserver<GrpcRequest>> requestStreamObserverMap = new ConcurrentHashMap<>();

    public AbstractGrpcExecutor(GrpcDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }
}
