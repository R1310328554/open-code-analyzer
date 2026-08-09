package com.taobao.arthas.grpcweb.grpc.view;

import io.arthas.api.ArthasServices.ResponseBody;
import com.taobao.arthas.core.command.model.StatusModel;
import com.taobao.arthas.grpcweb.grpc.observer.ArthasStreamObserver;

/**
 * {@link StatusModel} 的 gRPC 结果视图。
 * <p>
 * 用于推送命令执行状态消息（如成功/失败提示），写入 {@link ResponseBody#stringValue}。
 * @author xuyang 2023/8/15
 */
public class GrpcStatusView extends GrpcResultView<StatusModel> {

    /**
     * 若状态模型含 message，则将其作为字符串响应推送给客户端。
     *
     * @param arthasStreamObserver 流式响应观察者
     * @param result 状态结果模型
     */
    @Override
    public void draw(ArthasStreamObserver arthasStreamObserver, StatusModel result) {
        if (result.getMessage() != null) {
            ResponseBody responseBody  = ResponseBody.newBuilder()
                    .setJobId(result.getJobId())
                    .setType(result.getType())
                    .setStringValue(result.getMessage())
                    .build();
            arthasStreamObserver.onNext(responseBody);
        }
    }
}
