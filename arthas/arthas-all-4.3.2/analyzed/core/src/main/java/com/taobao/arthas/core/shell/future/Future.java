package com.taobao.arthas.core.shell.future;


import com.taobao.arthas.core.shell.handlers.Handler;

/**
 * 轻量级异步结果容器，类似 Vert.x Future，供 Shell 启动/关闭回调使用。
 * <p>
 * 支持成功/失败两种终态；注册 {@link Handler} 后在 {@link #complete} 或 {@link #fail} 时触发。
 */
public class Future<T> {
    private boolean failed;
    private boolean succeeded;
    private Handler<Future<T>> handler;
    private T result;
    private Throwable throwable;

    /** 创建未完成 Future，稍后调用 complete/fail */
    public Future() {
    }

    /** 构造即失败的 Future */
    public Future(Throwable t) {
        fail(t);
    }

    public Future(String failureMessage) {
        this(new Throwable(failureMessage));
    }

    public Future(T result) {
        complete(result);
    }

    /** @return 未完成的空 Future */
    public static <T> Future<T> future() {
        return new Future<T>();
    }

    public static <T> Future<T> succeededFuture() {
        return new Future<T>((T) null);
    }

    /** @return 已成功并携带 result 的 Future */
    public static <T> Future<T> succeededFuture(T result) {
        return new Future<T>(result);
    }

    /** @return 已失败并携带异常的 Future */
    public static <T> Future<T> failedFuture(Throwable t) {
        return new Future<T>(t);
    }

    public static <T> Future<T> failedFuture(String failureMessage) {
        return new Future<T>(failureMessage);
    }

    /** @return 是否已成功或已失败（终态） */
    public boolean isComplete() {
        return failed || succeeded;
    }

    /** 注册完成回调；若 Future 已终态则立即触发 */
    public Future<T> setHandler(Handler<Future<T>> handler) {
        this.handler = handler;
        checkCallHandler();
        return this;
    }


    /** 标记成功并保存 result，触发 handler */
    public void complete(T result) {
        checkComplete();
        this.result = result;
        succeeded = true;
        checkCallHandler();
    }

    public void complete() {
        complete(null);
    }

    /** 标记失败并保存 cause，触发 handler */
    public void fail(Throwable throwable) {
        checkComplete();
        this.throwable = throwable;
        failed = true;
        checkCallHandler();
    }

    public void fail(String failureMessage) {
        fail(new Throwable(failureMessage));
    }

    public T result() {
        return result;
    }

    public Throwable cause() {
        return throwable;
    }

    public boolean succeeded() {
        return succeeded;
    }

    public boolean failed() {
        return failed;
    }

    /** @return 将另一个 Future 的结果合并到本 Future 的 Handler 适配器 */
    public Handler<Future<T>> completer() {
        return new Handler<Future<T>>() {
            @Override
            public void handle(Future<T> ar) {
                if (ar.succeeded()) {
                    complete(ar.result());
                } else {
                    fail(ar.cause());
                }
            }
        };
    }

    /** handler 已设置且 Future 已终态时同步调用 handle */
    private void checkCallHandler() {
        if (handler != null && isComplete()) {
            handler.handle(this);
        }
    }

    /** 防止对已终态 Future 重复 complete/fail */
    private void checkComplete() {
        if (succeeded || failed) {
            throw new IllegalStateException("Result is already complete: " + (succeeded ? "succeeded" : "failed"));
        }
    }
}
