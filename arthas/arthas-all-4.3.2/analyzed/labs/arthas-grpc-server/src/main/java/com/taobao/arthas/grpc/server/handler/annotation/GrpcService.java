package com.taobao.arthas.grpc.server.handler.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记 gRPC 服务实现类，供 {@link com.taobao.arthas.grpc.server.handler.GrpcDispatcher} 扫描注册。
 * <p>
 * 标注在类上后，{@code value} 指定 protobuf 生成的 service 全限定名，
 * 与 {@link GrpcMethod} 配合完成方法级路由绑定。
 *
 * @author: FengYe
 * @date: 2024/9/6 01:57
 * @description: GrpcService
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GrpcService {
    /**
     * protobuf service 名称，例如 {@code arthas.grpc.unittest.ArthasUnittestService}。
     */
    String value() default "";
}
