/*
 * Copyright 2016 The Netty Project
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
package io.netty.util.concurrent;

/**
 * Similar to {@link java.util.concurrent.RejectedExecutionHandler} but specific to {@link SingleThreadEventExecutor}.
 *
 * <p>类似 JDK 的拒绝策略，但针对 {@link SingleThreadEventExecutor} 的任务队列满或已关闭场景。</p>
 */
public interface RejectedExecutionHandler {

    /**
     * Called when someone tried to add a task to {@link SingleThreadEventExecutor} but this failed due capacity
     * restrictions.
     *
     * <p>任务因队列容量或关闭状态无法入队时回调；可抛异常、退避重试或丢弃。</p>
     */
    void rejected(Runnable task, SingleThreadEventExecutor executor);
}
