/*
 * Copyright 2022 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.quic;

/**
 * 由 {@link BoringSSL#SSL_getTask(long)} 返回的 SSL 相关异步任务基类。
 * <p>
 * 实现 {@link Runnable}，在 EventLoop 上执行；{@code returnValue} 与 {@code complete} 由 JNI 轮询读取。
 */
abstract class BoringSSLTask implements Runnable {
    private final long ssl;
    protected boolean didRun;

    // 以下字段由 JNI 直接访问
    private int returnValue;
    private volatile boolean complete;

    protected BoringSSLTask(long ssl) {
        // 构造函数绝不能抛异常，否则 native 层无法安全构造任务对象
        this.ssl = ssl;
    }

    @Override
    public final void run() {
        if (!didRun) {
            didRun = true;
            runTask(ssl, (long ssl, int result) -> {
                returnValue = result;
                complete = true;
            });
        }
    }

    /**
     * 任务销毁时调用，子类可释放资源；默认无操作。
     */
    protected void destroy() {
        // Noop
    }

    /**
     * 执行具体任务逻辑，完成后通过 callback 将结果码回传给 OpenSSL/BoringSSL。
     */
    protected abstract void runTask(long ssl, TaskCallback callback);

    /** 任务完成时的结果回调，{@code result} 为 native 层约定的状态码。 */
    interface TaskCallback {
        void onResult(long ssl, int result);
    }
}
