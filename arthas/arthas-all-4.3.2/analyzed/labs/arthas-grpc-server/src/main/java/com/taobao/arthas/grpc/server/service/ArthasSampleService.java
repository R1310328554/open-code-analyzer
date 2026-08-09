package com.taobao.arthas.grpc.server.service;

import arthas.grpc.unittest.ArthasUnittest;
import com.taobao.arthas.grpc.server.handler.GrpcRequest;
import com.taobao.arthas.grpc.server.handler.GrpcResponse;
import com.taobao.arthas.grpc.server.handler.StreamObserver;


/**
 * Arthas gRPC 示例服务接口，演示四种 RPC 调用模式及单元测试场景。
 * <p>
 * 由 {@link com.taobao.arthas.grpc.server.service.impl.ArthasSampleServiceImpl} 实现，
 * 供 grpc-server 模块集成测试与 Arthas 探针联调使用。
 *
 * @author: FengYe
 * @date: 2024/6/30 下午11:42
 * @description: ArthasSampleService
 */
public interface ArthasSampleService {
    /** 一元调用：回显请求消息 */
    ArthasUnittest.ArthasUnittestResponse unary(ArthasUnittest.ArthasUnittestRequest command);

    /** 一元调用：按 id 累加 num 并存储 */
    ArthasUnittest.ArthasUnittestResponse unaryAddSum(ArthasUnittest.ArthasUnittestRequest command);

    /** 一元调用：按 id 查询已累加的 sum */
    ArthasUnittest.ArthasUnittestResponse unaryGetSum(ArthasUnittest.ArthasUnittestRequest command);

    /** 客户端流：接收多帧请求，流结束时返回 num 总和 */
    StreamObserver<GrpcRequest<ArthasUnittest.ArthasUnittestRequest>> clientStreamSum(StreamObserver<GrpcResponse<ArthasUnittest.ArthasUnittestResponse>> observer);

    /** 服务端流：单次请求，连续推送 5 条响应 */
    void serverStream(ArthasUnittest.ArthasUnittestRequest request, StreamObserver<GrpcResponse<ArthasUnittest.ArthasUnittestResponse>> observer);

    /** 双向流：每收到一帧请求即回写一帧响应 */
    StreamObserver<GrpcRequest<ArthasUnittest.ArthasUnittestRequest>> biStream(StreamObserver<GrpcResponse<ArthasUnittest.ArthasUnittestResponse>> observer);
}
