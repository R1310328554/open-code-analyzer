package com.taobao.arthas.grpc.server.handler;

import java.util.List;

/**
 * 单个 HTTP/2 帧内聚合的多条 gRPC 请求占位模型。
 * <p>
 * 一帧 DATA 可能携带多个 gRPC 消息体，本类用于批量持有 {@link GrpcRequest} 列表。
 *
 * @author: FengYe
 * @date: 2024/9/18 23:12
 * @description: 一个 http2 的 frame 中可能存在多个 grpc 的请求体
 */
public class Http2FrameRequest {

    /**
     * grpc 请求体
     */
    private List<GrpcRequest> grpcRequests;
}
