package com.taobao.arthas.grpcweb.grpc.view;

import com.taobao.arthas.core.command.model.ResultModel;
import com.taobao.arthas.grpcweb.grpc.observer.ArthasStreamObserver;

/**
 * gRPC 客户端命令结果的抽象视图基类。
 * <p>
 * 每种 {@link ResultModel} 子类型对应一个具体 View 实现，
 * 负责将结构化结果转换为 {@link io.arthas.api.ArthasServices.ResponseBody} 并写入流。
 * 视图实例无状态、可复用。
 * Command result view for grpc client.
 * Note: Result view is a reusable and stateless instance
 *
 * @author xuyang 2023/8/15
 */
public abstract class GrpcResultView<T extends ResultModel> {

    /**
     * 将命令结果格式化后推送给 gRPC 客户端。
     *
     * @param arthasStreamObserver 流式响应观察者
     * @param result 待渲染的结构化命令结果
     */
    public abstract void draw(ArthasStreamObserver arthasStreamObserver, T result);

}
