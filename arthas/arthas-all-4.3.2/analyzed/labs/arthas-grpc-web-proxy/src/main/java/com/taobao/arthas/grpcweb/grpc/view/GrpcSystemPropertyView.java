package com.taobao.arthas.grpcweb.grpc.view;

import io.arthas.api.ArthasServices.ResponseBody;
import io.arthas.api.ArthasServices.StringStringMapValue;
import com.taobao.arthas.core.command.model.SystemPropertyModel;
import com.taobao.arthas.grpcweb.grpc.observer.ArthasStreamObserver;

/**
 * {@link SystemPropertyModel} 的 gRPC 结果视图。
 * <p>
 * 将系统属性键值对（单个或全部）序列化为 {@link StringStringMapValue}，
 * 写入 {@link ResponseBody#stringStringMapValue} 推送给客户端。
 */
public class GrpcSystemPropertyView extends GrpcResultView<SystemPropertyModel>{

    /**
     * 将全部或单个系统属性以 map 形式写入 gRPC 响应。
     *
     * @param arthasStreamObserver 流式响应观察者
     * @param result 包含 props 字段的系统属性结果
     */
    @Override
    public void draw(ArthasStreamObserver arthasStreamObserver, SystemPropertyModel result) {
        StringStringMapValue stringStringMapValue = StringStringMapValue.newBuilder()
                .putAllStringStringMap(result.getProps()).build();
        ResponseBody responseBody  = ResponseBody.newBuilder()
                .setJobId(result.getJobId())
                .setType(result.getType())
                .setStringStringMapValue(stringStringMapValue)
                .build();
        arthasStreamObserver.onNext(responseBody);
    }
}
