package com.taobao.arthas.grpcweb.grpc.service;

import com.taobao.arthas.core.shell.system.ExecStatus;
import io.arthas.api.ArthasServices.ResponseBody;
import io.arthas.api.PwdGrpc;
import com.google.protobuf.Empty;
import com.taobao.arthas.core.command.model.PwdModel;

import com.taobao.arthas.core.shell.session.SessionManager;
import com.taobao.arthas.grpcweb.grpc.observer.ArthasStreamObserver;
import com.taobao.arthas.grpcweb.grpc.observer.impl.ArthasStreamObserverImpl;
import io.grpc.stub.StreamObserver;

import java.io.File;
import java.lang.instrument.Instrumentation;

/**
 * gRPC {@code pwd} 命令服务：返回 JVM 进程当前工作目录的绝对路径。
 * <p>
 * 实现与 CLI {@code pwd} 等价，结果通过 {@link PwdModel} 经 gRPC 视图推送。
 */
public class PwdCommandService extends PwdGrpc.PwdImplBase{

    private GrpcJobController grpcJobController;

    public PwdCommandService(GrpcJobController grpcJobController) {
        this.grpcJobController = grpcJobController;
    }

    /**
     * 执行 pwd：读取 {@code user.dir} 对应绝对路径并单次响应后完成流。
     *
     * @param empty            空请求体
     * @param responseObserver gRPC 响应流
     */
    @Override
    public void pwd(Empty empty, StreamObserver<ResponseBody> responseObserver){
        String path = new File("").getAbsolutePath();
        ArthasStreamObserver<ResponseBody> arthasStreamObserver = new ArthasStreamObserverImpl<>(responseObserver, null,grpcJobController);
        arthasStreamObserver.setProcessStatus(ExecStatus.RUNNING);
        arthasStreamObserver.appendResult(new PwdModel(path));
        arthasStreamObserver.onCompleted();
    }
}
