package com.taobao.arthas.grpc.server.handler;

/**
 * gRPC 流式回调接口（Arthas 轻量实现，非 io.grpc.stub 同名类）。
 * <p>
 * 客户端/服务端流式调用中，用于接收下一条消息或通知流结束。
 *
 * @param <V> 流元素类型
 * @author: FengYe
 * @date: 2024/10/24 00:22
 * @description: StreamObserver
 */
public interface StreamObserver<V>  {

    /** 收到流上下一条消息。 */
    void onNext(V req);

    /** 对端正常结束流。 */
    void onCompleted();
}
