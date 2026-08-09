package com.taobao.arthas.grpcweb.grpc.view;

import io.arthas.api.ArthasServices.ResponseBody;
import com.taobao.arthas.core.command.model.MessageModel;
import com.taobao.arthas.grpcweb.grpc.observer.ArthasStreamObserver;

/**
 * {@link MessageModel} 的 gRPC 结果视图。
 * <p>
 * 将 Arthas 命令产生的纯文本消息（如 verbose 输出、提示信息）
 * 封装为 {@link ResponseBody#stringValue} 推送给客户端。
 */
public class GrpcMessageView extends GrpcResultView<MessageModel> {
    /**
     * 将消息文本写入 gRPC 响应流。
     *
     * @param arthasStreamObserver 流式响应观察者
     * @param result 包含 message 字段的结果模型
     */
    @Override
    public void draw(ArthasStreamObserver arthasStreamObserver, MessageModel result) {
        ResponseBody responseBody  = ResponseBody.newBuilder()
                .setJobId(result.getJobId())
                .setType(result.getType())
                .setStringValue(result.getMessage())
                .build();
        arthasStreamObserver.onNext(responseBody);
    }
}
