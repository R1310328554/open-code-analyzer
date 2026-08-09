package com.taobao.arthas.grpc.server.handler.constant;

/**
 * gRPC 四种调用模式枚举，与 protobuf 定义的 RPC 语义一一对应。
 * <p>
 * 由 {@link com.taobao.arthas.grpc.server.handler.annotation.GrpcMethod#grpcType()} 指定，
 * 并驱动 {@link com.taobao.arthas.grpc.server.handler.executor.GrpcExecutorFactory} 选择对应执行器。
 *
 * @author: FengYe
 * @date: 2024/10/24 01:06
 * @description: StreamTypeEnum
 */
public enum GrpcInvokeTypeEnum {
    /** 一元调用：单请求单响应 */
    UNARY,
    /** 服务端流：单请求多响应 */
    SERVER_STREAM,
    /** 客户端流：多请求单响应 */
    CLIENT_STREAM,
    /** 双向流：多请求多响应 */
    BI_STREAM;
}
