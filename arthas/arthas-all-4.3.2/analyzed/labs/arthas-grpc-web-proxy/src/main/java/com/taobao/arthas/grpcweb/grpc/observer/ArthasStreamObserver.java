package com.taobao.arthas.grpcweb.grpc.observer;

import com.taobao.arthas.core.advisor.AdviceListener;
import com.taobao.arthas.core.command.model.ResultModel;
import com.taobao.arthas.core.shell.system.ExecStatus;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Arthas gRPC 双向流的观察者抽象，桥接 gRPC {@code StreamObserver} 与 Arthas 命令生命周期。
 * <p>
 * 负责推送 {@link ResultModel}、管理 Advice 注册/注销、以及以 shell 语义结束任务进程。
 *
 * @param <T> gRPC 响应消息类型
 */
public interface ArthasStreamObserver<T>  {

    /** 向客户端发送下一条流式消息 */
    void onNext(T value);

    /** 以错误终止流 */
    void onError(Throwable t);

    /** 正常完成流 */
    void onCompleted();

    /** @return 当前 JVM 的 {@link Instrumentation}，用于字节码增强 */
    Instrumentation getInstrumentation();

    /** 向流写入纯文本消息并返回自身以便链式调用 */
    ArthasStreamObserver write(String msg);

    /** 追加一条结构化命令结果（需处于 RUNNING 状态） */
    void appendResult(ResultModel result);

    /** @return 输出次数计数器，供 watch 等命令限流 */
    AtomicInteger times();

    /** 注册 Advice 监听器及关联的 ClassFileTransformer */
    void register(AdviceListener listener, ClassFileTransformer transformer);

    /** 注销监听器并移除 transformer */
    void unregister();

    /** 以退出码 0 结束任务 */
    void end();

    /** @return 当前命令进程状态（READY/RUNNING/TERMINATED 等） */
    ExecStatus getPorcessStatus();

    /** 设置命令进程状态 */
    void setProcessStatus(ExecStatus execStatus);

    /**
     * End the process.
     *
     * @param status the exit status.
     */
    void end(int status);

    /**
     * End the process.
     *
     * @param status the exit status.
     * @param message 结束时的附加说明消息
     */
    void end(int status, String message);

    /** @return 本 gRPC 任务分配的唯一 jobId */
    int getJobId();

    /** @return 与本次请求绑定的请求模型（如 {@code WatchRequestModel}） */
    Object getRequestModel();

    void setRequestModel(Object requestModel);

    /** @return 已注册的 Advice 监听器 */
    AdviceListener getListener();
}
