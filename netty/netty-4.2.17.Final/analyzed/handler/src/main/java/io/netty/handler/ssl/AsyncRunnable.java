/*
 * Copyright 2021 The Netty Project
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
package io.netty.handler.ssl;

/**
 * 支持异步执行并在完成后回调的 {@link Runnable} 扩展。
 *
 * <p>SSL 握手等场景在 EventLoop 外执行耗时任务时，通过 {@code completionCallback} 通知完成。</p>
 */
interface AsyncRunnable extends Runnable {
    /**
     * 执行任务，完成后调用 {@code completionCallback}。
     *
     * @param completionCallback 任务结束（成功或失败）后在原线程或指定线程执行的回调
     */
    void run(Runnable completionCallback);
}
