package com.taobao.arthas.grpcweb.grpc.observer.impl;

import io.arthas.api.ArthasServices.ResponseBody;
import com.taobao.arthas.core.advisor.AdviceListener;
import com.taobao.arthas.core.advisor.AdviceWeaver;
import com.taobao.arthas.core.command.model.ResultModel;
import com.taobao.arthas.core.command.model.StatusModel;
import com.taobao.arthas.core.distribution.ResultDistributor;

import com.taobao.arthas.core.shell.system.ExecStatus;
import com.taobao.arthas.core.shell.system.ProcessAware;
import com.taobao.arthas.grpcweb.grpc.DemoBootstrap;
import com.taobao.arthas.grpcweb.grpc.distribution.GrpcResultDistributorImpl;
import com.taobao.arthas.grpcweb.grpc.observer.ArthasStreamObserver;
import com.taobao.arthas.grpcweb.grpc.service.GrpcJobController;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link ArthasStreamObserver} 的默认实现，包装 gRPC {@link StreamObserver} 并接入 Arthas 任务体系。
 * <p>
 * 构造时向 {@link GrpcJobController} 注册 jobId，通过 {@link GrpcResultDistributorImpl}
 * 将 {@link ResultModel} 渲染为 gRPC 消息后写出；客户端取消时自动结束任务。
 *
 * @param <T> gRPC 响应 protobuf 类型
 */
public class ArthasStreamObserverImpl<T> implements ArthasStreamObserver<T> {

    /** 底层 gRPC 流观察者 */
    private StreamObserver<T> streamObserver;

    /** watch 等命令的输出次数统计 */
    private AtomicInteger times = new AtomicInteger();

    /** 模拟 shell 进程，跟踪 READY/RUNNING/TERMINATED */
    private GrpcProcess process;

    /** 请求参数模型，如 WatchRequestModel */
    private Object requestModel;
    private AdviceListener listener;

    /** 增强时注册的 ClassFileTransformer */
    private ClassFileTransformer transformer;

    /** 本流任务唯一 ID */
    private final int jobId;

    /** 将 ResultModel 分发到 gRPC 视图 */
    private ResultDistributor resultDistributor;

    private GrpcJobController grpcJobController;

    private Instrumentation instrumentation;

    /**
     * 创建流观察者：分配 jobId、初始化结果分发器、注册到任务控制器并绑定客户端取消回调。
     *
     * @param streamObserver    gRPC 响应流
     * @param requestModel      命令请求模型，可为 null
     * @param grpcJobController 全局 gRPC 任务注册中心
     */
    public ArthasStreamObserverImpl(StreamObserver<T> streamObserver, Object requestModel, GrpcJobController grpcJobController){
        this.streamObserver = streamObserver;
        this.jobId = grpcJobController.generateGrpcJobId();
        this.instrumentation = grpcJobController.getInstrumentation();
        if (resultDistributor == null) {
            resultDistributor = new GrpcResultDistributorImpl(this, grpcJobController.getResultViewResolver());
        }
        this.process = new GrpcProcess();
        this.process.setProcessStatus(ExecStatus.READY);
        this.requestModel = requestModel;
        // 客户端断开连接时结束任务
        this.setOnCancelHandler();
        this.grpcJobController = grpcJobController;
        this.grpcJobController.registerGrpcJob(jobId, this);
    }

    @Override
    public void onNext(T value) {
        streamObserver.onNext(value);
    }

    @Override
    public void onError(Throwable t) {
        streamObserver.onError(t);
    }

    @Override
    public void onCompleted() {
        this.process.setProcessStatus(ExecStatus.TERMINATED);
//        grpcJobController.unRegisterGrpcJob(this.jobId);
        streamObserver.onCompleted();
    }

    @Override
    public AtomicInteger times() {
        return times;
    }

    /**
     * 注册 Advice 监听器：若监听器实现 {@link ProcessAware} 则绑定本 {@link GrpcProcess}，
     * 并向 {@link AdviceWeaver} 全局注册。
     */
    @Override
    public void register(AdviceListener adviceListener, ClassFileTransformer transformer) {
        if (adviceListener instanceof ProcessAware) {
            ProcessAware processAware = (ProcessAware) adviceListener;
            // listener 可能由其它 command 创建，仅在本 process 为空时绑定
            if(processAware.getProcess() == null) {
                this.process.setProcessStatus(ExecStatus.RUNNING);
                processAware.setProcess(this.process);
            }
        }
        this.listener = adviceListener;
        AdviceWeaver.reg(listener);

        this.transformer = transformer;
    }

    /** 移除 transformer 并从 AdviceWeaver 注销监听器（需确认 process 归属） */
    @Override
    public void unregister() {
        if (transformer != null) {
            DemoBootstrap.getRunningInstance().getTransformerManager().removeTransformer(transformer);
        }
        this.process.setProcessStatus(ExecStatus.TERMINATED);
        if (listener instanceof ProcessAware) {
            // listener 可能由其它 command 创建，不能误 unReg
            if (this.process.equals(((ProcessAware) listener).getProcess())) {
                AdviceWeaver.unReg(listener);
            }
        } else {
            AdviceWeaver.unReg(listener);
        }
    }

    @Override
    public void end() {
        end(0);
    }

    @Override
    public ExecStatus getPorcessStatus() {
        return this.process.status();
    }

    @Override
    public void setProcessStatus(ExecStatus execStatus){
        this.process.setProcessStatus(execStatus);
    }

    @Override
    public void end(int statusCode) {
        end(statusCode, null);
    }

    @Override
    public void end(int statusCode, String message) {
        terminate(statusCode, message);
    }

    /** 将字符串包装为 {@link ResponseBody} 并写入流 */
    @Override
    public ArthasStreamObserver write(String msg) {
        ResponseBody result = ResponseBody.newBuilder().setStringValue(msg).build();
        onNext((T) result);
        return this;
    }

    /**
     * 追加命令结果：仅 RUNNING 状态允许写入，并附带 jobId 后交给分发器渲染。
     */
    @Override
    public void appendResult(ResultModel result) {
        if (process.status() != ExecStatus.RUNNING) {
            throw new IllegalStateException(
                    "Cannot write to standard output when " + process.status().name().toLowerCase());
        }
        result.setJobId(jobId);
        if (resultDistributor != null) {
            resultDistributor.appendResult(result);
        }
    }

    @Override
    public int getJobId() {
        return jobId;
    }

    @Override
    public Object getRequestModel() {
        return requestModel;
    }

    @Override
    public void setRequestModel(Object requestModel) {
        this.requestModel = requestModel;
    }

    /** 注册 gRPC 客户端取消回调，断开时调用 {@link #end()} */
    public void setOnCancelHandler() {
        ServerCallStreamObserver<T> observer = (ServerCallStreamObserver<T>) this.streamObserver;
        observer.setOnCancelHandler(() -> {
            this.end();
        });
    }

    /**
     * 终止任务：写入 {@link StatusModel}、注销增强、并完成 gRPC 流。
     *
     * @return 若此前未 TERMINATED 则返回 true
     */
    private synchronized boolean terminate(int exitCode, String message) {
        boolean flag;
        if (process.status() != ExecStatus.TERMINATED) {
            this.appendResult(new StatusModel(exitCode, message));
            if (process != null) {
                this.unregister();
            }
            flag = true;
        } else {
            flag = false;
        }
        this.onCompleted();
        return flag;
    }

    @Override
    public AdviceListener getListener() {
        return listener;
    }

    @Override
    public Instrumentation getInstrumentation() {
        return instrumentation;
    }
}
