/*
 * Copyright 2012 The Netty Project
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
package io.netty.channel;

import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

/**
 * <p>默认单线程 {@link EventLoop}：从任务队列取任务执行，并在确认关闭后退出循环。
 * 通常由 {@link DefaultEventLoopGroup} 创建，用于本地传输（local transport）。</p>
 */
public class DefaultEventLoop extends SingleThreadEventLoop {

    /** 使用默认线程工厂创建独立 EventLoop。 */
    public DefaultEventLoop() {
        this((EventLoopGroup) null);
    }

    /** 使用指定 {@link ThreadFactory} 创建。 */
    public DefaultEventLoop(ThreadFactory threadFactory) {
        this(null, threadFactory);
    }

    /** 使用指定 {@link Executor} 创建。 */
    public DefaultEventLoop(Executor executor) {
        this(null, executor);
    }

    /** 作为 {@link EventLoopGroup} 子线程创建，使用默认线程工厂。 */
    public DefaultEventLoop(EventLoopGroup parent) {
        this(parent, new DefaultThreadFactory(DefaultEventLoop.class));
    }

    /** 作为子 EventLoop，使用指定 {@link ThreadFactory}。 */
    public DefaultEventLoop(EventLoopGroup parent, ThreadFactory threadFactory) {
        super(parent, threadFactory, true);
    }

    /** 作为子 EventLoop，使用指定 {@link Executor}。 */
    public DefaultEventLoop(EventLoopGroup parent, Executor executor) {
        super(parent, executor, true);
    }

    /**
     * <p>EventLoop 主循环：阻塞取任务、执行任务、更新执行时间，并在收到关闭信号后退出。</p>
     */
    @Override
    protected void run() {
        for (;;) {
            Runnable task = takeTask();
            if (task != null) {
                runTask(task);
                updateLastExecutionTime();
            }

            if (confirmShutdown()) {
                break;
            }
        }
    }
}
