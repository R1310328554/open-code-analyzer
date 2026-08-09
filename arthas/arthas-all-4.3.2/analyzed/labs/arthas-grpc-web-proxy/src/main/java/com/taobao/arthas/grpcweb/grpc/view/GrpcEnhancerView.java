package com.taobao.arthas.grpcweb.grpc.view;

import io.arthas.api.ArthasServices.ResponseBody;
import com.taobao.arthas.core.command.model.EnhancerModel;
import com.taobao.arthas.core.command.view.ViewRenderUtil;
import com.taobao.arthas.grpcweb.grpc.observer.ArthasStreamObserver;

/**
 * {@link EnhancerModel} 的 gRPC 结果视图。
 * <p>
 * 将字节码增强的影响统计（类数、方法数、失败原因等）格式化为文本，
 * 写入 {@link ResponseBody#stringValue} 推送给客户端。
 * Term grpc view for EnhancerModel
 * @author xuyang 2023/8/15
 */
public class GrpcEnhancerView extends GrpcResultView<EnhancerModel> {
    /**
     * 渲染增强影响信息并写入 gRPC 流。
     *
     * @param arthasStreamObserver 流式响应观察者
     * @param result 增强命令产生的 {@link EnhancerModel}
     */
    @Override
    public void draw(ArthasStreamObserver arthasStreamObserver, EnhancerModel result) {
        if (result.getEffect() != null) {
            // 复用终端视图的格式化逻辑，生成可读的影响摘要
            String msg = ViewRenderUtil.renderEnhancerAffect(result.getEffect());
            ResponseBody responseBody  = ResponseBody.newBuilder()
                    .setJobId(result.getJobId())
                    .setType(result.getType())
                    .setStringValue(msg)
                    .build();
            arthasStreamObserver.onNext(responseBody);
        }
    }
}
