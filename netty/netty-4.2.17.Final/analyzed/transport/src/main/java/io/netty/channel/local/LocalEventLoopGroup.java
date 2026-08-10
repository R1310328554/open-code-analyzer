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
package io.netty.channel.local;

import io.netty.channel.MultiThreadIoEventLoopGroup;

import java.util.concurrent.ThreadFactory;

/**
 * @deprecated Use {@link MultiThreadIoEventLoopGroup} with {@link LocalIoHandler#newFactory()}.
 *
 * <p>已废弃：请改用 {@link MultiThreadIoEventLoopGroup} 配合 {@link LocalIoHandler#newFactory()}。</p>
 */
@Deprecated
public class LocalEventLoopGroup extends MultiThreadIoEventLoopGroup {

    /**
     * Create a new instance with the default number of threads.
     * <p>使用默认线程数创建本地事件循环组。</p>
     */
    public LocalEventLoopGroup() {
        this(0);
    }

    /**
     * Create a new instance
     *
     * @param nThreads          the number of threads to use
     * <p>指定线程数创建实例。</p>
     */
    public LocalEventLoopGroup(int nThreads) {
        this(nThreads, null);
    }

    /**
     * Create a new instance with the default number of threads and the given {@link ThreadFactory}.
     *
     * @param threadFactory     the {@link ThreadFactory} or {@code null} to use the default
     * <p>使用默认线程数与指定 {@link ThreadFactory} 创建实例。</p>
     */
    public LocalEventLoopGroup(ThreadFactory threadFactory) {
        this(0, threadFactory);
    }

    /**
     * Create a new instance
     *
     * @param nThreads          the number of threads to use
     * @param threadFactory     the {@link ThreadFactory} or {@code null} to use the default
     * <p>指定线程数与 {@link ThreadFactory} 创建本地传输专用事件循环组。</p>
     */
    public LocalEventLoopGroup(int nThreads, ThreadFactory threadFactory) {
        super(nThreads, threadFactory, LocalIoHandler.newFactory());
    }
}
