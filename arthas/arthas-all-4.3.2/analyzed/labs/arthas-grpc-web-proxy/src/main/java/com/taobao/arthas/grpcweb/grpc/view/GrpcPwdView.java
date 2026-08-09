package com.taobao.arthas.grpcweb.grpc.view;

import io.arthas.api.ArthasServices.ResponseBody;
import io.arthas.api.ArthasServices.StringStringMapValue;
import com.taobao.arthas.core.command.model.PwdModel;
import com.taobao.arthas.grpcweb.grpc.observer.ArthasStreamObserver;

/**
 * {@link PwdModel} 的 gRPC 结果视图。
 * <p>
 * 将 {@code pwd} 命令返回的工作目录封装为键值 map（key 为 {@code workingDir}），
 * 写入 {@link ResponseBody#stringStringMapValue}。
 * @author xuyang 2023/8/15
 */
public class GrpcPwdView extends GrpcResultView<PwdModel> {

    /**
     * 将当前工作目录序列化为 gRPC map 响应。
     *
     * @param arthasStreamObserver 流式响应观察者
     * @param result 包含 workingDir 的 pwd 结果
     */
    @Override
    public void draw(ArthasStreamObserver arthasStreamObserver, PwdModel result) {
        StringStringMapValue stringStringMapValue = StringStringMapValue.newBuilder()
                .putStringStringMap("workingDir", result.getWorkingDir()).build();
        ResponseBody responseBody  = ResponseBody.newBuilder()
                .setJobId(result.getJobId())
                .setType(result.getType())
                .setStringStringMapValue(stringStringMapValue)
                .build();
        arthasStreamObserver.onNext(responseBody);
    }
}
