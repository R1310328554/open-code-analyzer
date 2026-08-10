/*
 * Copyright 2025 The Netty Project
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

import java.util.concurrent.Executor;

/**
 * Executor that is aware its execution thread.
 *
 * <p>能识别“是否由本执行器的工作线程执行”的 {@link Executor} 扩展。</p>
 */
public interface ThreadAwareExecutor extends Executor {
    /**
     * Return {@code true} if the given {@link Thread} is used by this {@link ThreadAwareExecutor} to execute
     * work.
     *
     * <p>判断给定线程是否为本执行器用于执行任务的线程（类似 {@link EventExecutor#inEventLoop(Thread)}）。</p>
     */
    boolean isExecutorThread(Thread thread);
}
