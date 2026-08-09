package com.taobao.arthas.grpcweb.grpc.view;

import com.taobao.arthas.core.view.ObjectView;
import com.taobao.arthas.grpcweb.grpc.model.WatchRequestModel;
import io.arthas.api.ArthasServices.JavaObject;
import io.arthas.api.ArthasServices.ResponseBody;
import io.arthas.api.ArthasServices.WatchResponse;
import com.taobao.arthas.core.command.model.ObjectVO;
import com.taobao.arthas.core.util.DateUtils;
import com.taobao.arthas.grpcweb.grpc.model.WatchResponseModel;
import com.taobao.arthas.grpcweb.grpc.observer.ArthasStreamObserver;

import static com.taobao.arthas.grpcweb.grpc.objectUtils.JavaObjectConverter.toJavaObjectWithExpand;

/**
 * {@link WatchResponseModel} 的 gRPC 结果视图。
 * <p>
 * 将 watch 命令捕获的方法调用上下文（类名、方法名、耗时、观测值、接入点等）
 * 序列化为 {@link WatchResponse} protobuf 消息推送给客户端。
 * Term view for WatchModel
 *
 * @author xuyang 2023/8/15
 */
public class GrpcWatchView extends GrpcResultView<WatchResponseModel> {

    /**
     * 将 watch 观测结果转换为 {@link WatchResponse} 并写入 gRPC 流。
     *
     * @param arthasStreamObserver 流式响应观察者
     * @param model watch 命令产生的响应模型
     */
    @Override
    public void draw(ArthasStreamObserver arthasStreamObserver, WatchResponseModel model) {
        ObjectVO objectVO = model.getValue();
//        Object obj = objectVO.needExpand() ? new ObjectView(model.getSizeLimit(), objectVO).draw() : objectVO.getObject();
        // 按 expand 深度将 Java 对象转为可序列化的 JavaObject
        JavaObject javaObject = toJavaObjectWithExpand(objectVO.getObject(), objectVO.getExpand());
        WatchResponse watchResponse = WatchResponse.newBuilder()
                .setAccessPoint(model.getAccessPoint())
                .setClassName(model.getClassName())
                .setCost(model.getCost())
                .setMethodName(model.getMethodName())
                .setSizeLimit(model.getSizeLimit())
                .setTs(DateUtils.formatDateTime(model.getTs()))
                .setValue(javaObject)
                .build();
        ResponseBody responseBody  = ResponseBody.newBuilder()
                .setJobId(model.getJobId())
                .setResultId(model.getResultId())
                .setType(model.getType())
                .setWatchResponse(watchResponse)
                .build();
        arthasStreamObserver.onNext(responseBody);
    }
}
