package com.taobao.arthas.grpcweb.grpc.distribution;

import com.taobao.arthas.core.command.model.ResultModel;
import com.taobao.arthas.core.distribution.ResultDistributor;
import com.taobao.arthas.grpcweb.grpc.observer.ArthasStreamObserver;
import com.taobao.arthas.grpcweb.grpc.view.GrpcResultView;
import com.taobao.arthas.grpcweb.grpc.view.GrpcResultViewResolver;


/**
 * Arthas 命令结果到 gRPC 流的分发器实现。
 * <p>
 * 实现 {@link ResultDistributor}，将 {@link ResultModel} 按类型解析为
 * {@link GrpcResultView} 并通过 {@link ArthasStreamObserver} 推送给 gRPC-Web 客户端。
 */
public class GrpcResultDistributorImpl implements ResultDistributor {

    /** 向 gRPC 双向流写出结果的观察者 */
    private final ArthasStreamObserver arthasStreamObserver;

    /** 按 ResultModel 类型选择对应视图渲染器 */
    private final GrpcResultViewResolver grpcResultViewResolver;

    public GrpcResultDistributorImpl(ArthasStreamObserver arthasStreamObserver, GrpcResultViewResolver resultViewResolver) {
        this.arthasStreamObserver = arthasStreamObserver;
        this.grpcResultViewResolver = resultViewResolver;
    }

    /**
     * 追加一条命令执行结果：解析视图后调用 {@link GrpcResultView#draw} 写入流。
     *
     * @param model Arthas 命令产生的结构化结果
     */
    @Override
    public void appendResult(ResultModel model) {
        GrpcResultView resultView = grpcResultViewResolver.getResultView(model);
        if (resultView != null) {
            resultView.draw(arthasStreamObserver, model);
        }
    }

    /** 关闭分发器；当前 gRPC 流生命周期由上层管理，此处无额外清理 */
    @Override
    public void close() {

    }
}
