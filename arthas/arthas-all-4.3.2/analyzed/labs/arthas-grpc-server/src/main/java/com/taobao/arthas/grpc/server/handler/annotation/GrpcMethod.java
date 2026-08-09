package com.taobao.arthas.grpc.server.handler.annotation;

import com.taobao.arthas.grpc.server.handler.constant.GrpcInvokeTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注 gRPC 服务实现类上的单个 RPC 方法。
 * <p>
 * 与 {@link com.taobao.arthas.grpc.server.handler.annotation.GrpcService} 配合，
 * 供 {@link GrpcDispatcher} 扫描注册路由与调用类型。
 *
 * @author: FengYe
 * @date: 2024/9/6 01:57
 * @description: GrpcMethod
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GrpcMethod {
    /** gRPC 方法名（protobuf 定义名）。 */
    String value() default "";

    /** 是否为流式 RPC（遗留字段，优先使用 grpcType）。 */
    boolean stream() default false;

    /** 调用模式：unary / client_stream / server_stream / bi_stream。 */
    GrpcInvokeTypeEnum grpcType() default GrpcInvokeTypeEnum.UNARY;
}
