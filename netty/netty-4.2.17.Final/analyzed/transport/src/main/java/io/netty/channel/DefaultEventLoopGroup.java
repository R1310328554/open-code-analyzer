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

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

/**
 * {@link MultithreadEventLoopGroup} which must be used for the local transport.
 * <p>本地传输专用的 {@link MultithreadEventLoopGroup}，子线程为 {@link DefaultEventLoop}。
 * 已废弃，新代码应使用对应 transport 模块的 EventLoopGroup。</p>
 */
@Deprecated
public class DefaultEventLoopGroup extends MultithreadEventLoopGroup {

    /**
     * Create a new instance with the default number of threads.
     * <p>使用默认线程数（通常为 CPU 核数 × 2）创建实例。</p>
     */
    public DefaultEventLoopGroup() {
        this(0);
    }

    /**
     * Create a new instance
     * <p>指定线程数创建 EventLoopGroup。</p>
     *
     * @param nThreads          the number of threads to use
     */
    public DefaultEventLoopGroup(int nThreads) {
        this(nThreads, (ThreadFactory) null);
    }

    /**
     * Create a new instance with the default number of threads and the given {@link ThreadFactory}.
     * <p>使用默认线程数与指定 {@link ThreadFactory} 创建。</p>
     *
     * @param threadFactory     the {@link ThreadFactory} or {@code null} to use the default
     */
    public DefaultEventLoopGroup(ThreadFactory threadFactory) {
        this(0, threadFactory);
    }

    /**
     * Create a new instance
     * <p>指定线程数与 {@link ThreadFactory} 创建。</p>
     *
     * @param nThreads          the number of threads to use
     * @param threadFactory     the {@link ThreadFactory} or {@code null} to use the default
     */
    public DefaultEventLoopGroup(int nThreads, ThreadFactory threadFactory) {
        super(nThreads, threadFactory);
    }

    /**
     * Create a new instance
     * <p>指定线程数与 {@link Executor} 创建。</p>
     *
     * @param nThreads          the number of threads to use
     * @param executor          the Executor to use, or {@code null} if the default should be used.
     */
    public DefaultEventLoopGroup(int nThreads, Executor executor) {
        super(nThreads, executor);
    }

    /** 为每个子线程创建 {@link DefaultEventLoop} 实例。 */
    @Override
    protected EventLoop newChild(Executor executor, Object... args) throws Exception {
        return new DefaultEventLoop(this, executor);
    }
}
