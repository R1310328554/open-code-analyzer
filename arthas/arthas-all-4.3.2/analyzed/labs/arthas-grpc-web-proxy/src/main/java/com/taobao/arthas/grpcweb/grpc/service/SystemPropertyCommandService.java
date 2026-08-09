package com.taobao.arthas.grpcweb.grpc.service;

import com.taobao.arthas.core.shell.system.ExecStatus;
import io.arthas.api.ArthasServices.ResponseBody;
import io.arthas.api.ArthasServices.StringKey;
import io.arthas.api.ArthasServices.StringStringMapValue;
import io.arthas.api.SystemPropertyGrpc;
import com.google.protobuf.Empty;
import com.taobao.arthas.core.command.model.SystemPropertyModel;
import com.taobao.arthas.grpcweb.grpc.observer.ArthasStreamObserver;
import com.taobao.arthas.grpcweb.grpc.observer.impl.ArthasStreamObserverImpl;
import io.grpc.stub.StreamObserver;

import java.util.Map;

/**
 * 系统属性命令的 gRPC 服务实现。
 * <p>
 * 对应 Arthas {@code sysprop} 命令，支持查询全部属性、按 key 查询以及更新属性值，
 * 结果通过 {@link SystemPropertyModel} 封装后经 gRPC 流返回客户端。
 */
public class SystemPropertyCommandService extends SystemPropertyGrpc.SystemPropertyImplBase{

    /** 管理 gRPC 任务生命周期与流式响应的控制器 */
    private GrpcJobController grpcJobController;

    public SystemPropertyCommandService(GrpcJobController grpcJobController) {
        this.grpcJobController = grpcJobController;
    }

    /**
     * 获取 JVM 全部系统属性。
     *
     * @param empty 空请求体，gRPC 接口占位参数
     * @param responseObserver 用于向客户端推送 {@link ResponseBody} 的流观察者
     */
    @Override
    public void get(Empty empty, StreamObserver<ResponseBody> responseObserver){
        ArthasStreamObserver<ResponseBody> arthasStreamObserver = new ArthasStreamObserverImpl<>(responseObserver, null, grpcJobController);
        arthasStreamObserver.setProcessStatus(ExecStatus.RUNNING);
        arthasStreamObserver.appendResult(new SystemPropertyModel(System.getProperties()));
        arthasStreamObserver.end();
    }

    /**
     * 按 key 查询单个系统属性。
     *
     * @param request 包含属性名的请求
     * @param responseObserver 流式响应观察者
     */
    @Override
    public void getByKey(StringKey request, StreamObserver<ResponseBody> responseObserver){
        String propertyName = request.getKey();
        ArthasStreamObserver<ResponseBody> arthasStreamObserver = new ArthasStreamObserverImpl<>(responseObserver,null, grpcJobController);
        arthasStreamObserver.setProcessStatus(ExecStatus.RUNNING);
        // 读取指定 key 的系统属性
        String value = System.getProperty(propertyName);
        if (value == null) {
            arthasStreamObserver.end(-1, "There is no property with the key " + propertyName);
            return;
        } else {
            arthasStreamObserver.appendResult(new SystemPropertyModel(propertyName, value));
            arthasStreamObserver.end();
        }
    }

    /**
     * 更新（设置）系统属性。
     * <p>
     * 客户端通过 map 传递 key/value，服务端调用 {@link System#setProperty} 写入 JVM。
     *
     * @param request 包含待更新属性键值对的请求
     * @param responseObserver 流式响应观察者
     */
    @Override
    public void update(StringStringMapValue request, StreamObserver<ResponseBody> responseObserver){
        // 从客户端请求中解析属性键值对
        Map<String, String> properties = request.getStringStringMapMap();
        String propertyName = "";
        String propertyValue = "";
        // 遍历 map 提取最后一组 key/value（接口约定单次只更新一项）
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            propertyName = entry.getKey();
            propertyValue = entry.getValue();
        }
        ArthasStreamObserver<ResponseBody> arthasStreamObserver = new ArthasStreamObserverImpl<>(responseObserver,null, grpcJobController);
        arthasStreamObserver.setProcessStatus(ExecStatus.RUNNING);
        try {
            System.setProperty(propertyName, propertyValue);
            arthasStreamObserver.appendResult(new SystemPropertyModel(propertyName, System.getProperty(propertyName)));
            arthasStreamObserver.onCompleted();
        }catch (Throwable t) {
            arthasStreamObserver.end(-1, "Error during setting system property: " + t.getMessage());
        }
    }
}
