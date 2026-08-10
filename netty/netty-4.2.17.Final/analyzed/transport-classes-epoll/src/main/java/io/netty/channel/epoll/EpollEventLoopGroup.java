/*
 * Copyright 2014 The Netty Project
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
package io.netty.channel.epoll;

import io.netty.channel.DefaultSelectStrategyFactory;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.EventLoopTaskQueueFactory;
import io.netty.channel.IoEventLoop;
import io.netty.channel.IoHandlerFactory;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.SelectStrategyFactory;
import io.netty.channel.SingleThreadEventLoop;
import io.netty.util.concurrent.EventExecutorChooserFactory;
import io.netty.util.concurrent.RejectedExecutionHandler;
import io.netty.util.concurrent.RejectedExecutionHandlers;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

/**
 * {@link EventLoopGroup} which uses epoll under the covers. Because of this
 * it only works on linux.
 * <p>Linux epoll 多线程 {@link EventLoopGroup}；已弃用，请改用 {@link MultiThreadIoEventLoopGroup} + {@link EpollIoHandler#newFactory()}。</p>
 *
 * @deprecated Use {@link MultiThreadIoEventLoopGroup} with {@link EpollIoHandler#newFactory()}.
 */
@Deprecated
public final class EpollEventLoopGroup extends MultiThreadIoEventLoopGroup {

    // 非 static 初始化块：仅在实例化时触发 JNI 可用性检查
    {
        // 确保实例化前 epoll JNI 已加载
        Epoll.ensureAvailability();
    }
    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(EpollEventLoopGroup.class);

    /**
     * Create a new instance using the default number of threads and the default {@link ThreadFactory}.
     * <p>使用默认线程数与默认 {@link ThreadFactory} 创建实例。</p>
     */
    public EpollEventLoopGroup() {
        this(0);
    }

    /**
     * Create a new instance using the specified number of threads and the default {@link ThreadFactory}.
     * <p>指定线程数，使用默认 {@link ThreadFactory}。</p>
     */
    public EpollEventLoopGroup(int nThreads) {
        this(nThreads, (ThreadFactory) null);
    }

    /**
     * Create a new instance using the default number of threads and the given {@link ThreadFactory}.
     * <p>默认线程数，指定 {@link ThreadFactory}。</p>
     */
    @SuppressWarnings("deprecation")
    public EpollEventLoopGroup(ThreadFactory threadFactory) {
        this(0, threadFactory, 0);
    }

    /**
     * Create a new instance using the specified number of threads and the default {@link ThreadFactory}.
     * <p>指定线程数，使用默认 {@link ThreadFactory}。</p>
     */
    @SuppressWarnings("deprecation")
    public EpollEventLoopGroup(int nThreads, SelectStrategyFactory selectStrategyFactory) {
        this(nThreads, (ThreadFactory) null, selectStrategyFactory);
    }

    /**
     * Create a new instance using the specified number of threads and the given {@link ThreadFactory}.
     * <p>指定线程数与 {@link ThreadFactory}。</p>
     */
    @SuppressWarnings("deprecation")
    public EpollEventLoopGroup(int nThreads, ThreadFactory threadFactory) {
        this(nThreads, threadFactory, 0);
    }

    public EpollEventLoopGroup(int nThreads, Executor executor) {
        this(nThreads, executor, DefaultSelectStrategyFactory.INSTANCE);
    }

    /**
     * Create a new instance using the specified number of threads and the given {@link ThreadFactory}.
     * <p>指定线程数与 {@link ThreadFactory}。</p>
     */
    @SuppressWarnings("deprecation")
    public EpollEventLoopGroup(int nThreads, ThreadFactory threadFactory, SelectStrategyFactory selectStrategyFactory) {
        this(nThreads, threadFactory, 0, selectStrategyFactory);
    }

    /**
     * Create a new instance using the specified number of threads, the given {@link ThreadFactory} and the given
     * maximal amount of epoll events to handle per epollWait(...).
     * <p>可指定单次 {@code epollWait} 最大事件数（已弃用）。</p>
     *
     * @deprecated  Use {@link #EpollEventLoopGroup(int)} or {@link #EpollEventLoopGroup(int, ThreadFactory)}
     */
    @Deprecated
    public EpollEventLoopGroup(int nThreads, ThreadFactory threadFactory, int maxEventsAtOnce) {
        this(nThreads, threadFactory, maxEventsAtOnce, DefaultSelectStrategyFactory.INSTANCE);
    }

    /**
     * Create a new instance using the specified number of threads, the given {@link ThreadFactory} and the given
     * <p>创建新实例。</p>
     * maximal amount of epoll events to handle per epollWait(...).
     *
     * @deprecated  Use {@link #EpollEventLoopGroup(int)}, {@link #EpollEventLoopGroup(int, ThreadFactory)}, or
     * {@link #EpollEventLoopGroup(int, SelectStrategyFactory)}
     */
    @Deprecated
    public EpollEventLoopGroup(int nThreads, ThreadFactory threadFactory, int maxEventsAtOnce,
                               SelectStrategyFactory selectStrategyFactory) {
        super(nThreads, threadFactory, EpollIoHandler.newFactory(maxEventsAtOnce, selectStrategyFactory),
                RejectedExecutionHandlers.reject());
    }

    public EpollEventLoopGroup(int nThreads, Executor executor, SelectStrategyFactory selectStrategyFactory) {
        super(nThreads, executor, EpollIoHandler.newFactory(0, selectStrategyFactory),
                RejectedExecutionHandlers.reject());
    }

    public EpollEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory,
                               SelectStrategyFactory selectStrategyFactory) {
        super(nThreads, executor, EpollIoHandler.newFactory(0, selectStrategyFactory), chooserFactory,
                RejectedExecutionHandlers.reject());
    }

    public EpollEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory,
                               SelectStrategyFactory selectStrategyFactory,
                               RejectedExecutionHandler rejectedExecutionHandler) {
        super(nThreads, executor, EpollIoHandler.newFactory(0, selectStrategyFactory), chooserFactory,
                rejectedExecutionHandler);
    }

    public EpollEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory,
                               SelectStrategyFactory selectStrategyFactory,
                               RejectedExecutionHandler rejectedExecutionHandler,
                               EventLoopTaskQueueFactory queueFactory) {
        super(nThreads, executor, EpollIoHandler.newFactory(0, selectStrategyFactory), chooserFactory,
                rejectedExecutionHandler, queueFactory);
    }

    /**
     * @param nThreads the number of threads that will be used by this instance.
     * @param executor the Executor to use, or {@code null} if default one should be used.
     * @param chooserFactory the {@link EventExecutorChooserFactory} to use.
     * @param selectStrategyFactory the {@link SelectStrategyFactory} to use.
     * @param rejectedExecutionHandler the {@link RejectedExecutionHandler} to use.
     * @param taskQueueFactory the {@link EventLoopTaskQueueFactory} to use for
     *                         {@link SingleThreadEventLoop#execute(Runnable)},
     *                         or {@code null} if default one should be used.
     * @param tailTaskQueueFactory the {@link EventLoopTaskQueueFactory} to use for
     *                             {@link SingleThreadEventLoop#executeAfterEventLoopIteration(Runnable)},
     *                             or {@code null} if default one should be used.
      * <p>Netty epoll 传输实现；详见上方英文说明。</p>
     */
    public EpollEventLoopGroup(int nThreads, Executor executor, EventExecutorChooserFactory chooserFactory,
                               SelectStrategyFactory selectStrategyFactory,
                               RejectedExecutionHandler rejectedExecutionHandler,
                               EventLoopTaskQueueFactory taskQueueFactory,
                               EventLoopTaskQueueFactory tailTaskQueueFactory) {
        super(nThreads, executor, EpollIoHandler.newFactory(0, selectStrategyFactory),
                chooserFactory, rejectedExecutionHandler, taskQueueFactory, tailTaskQueueFactory);
    }

    /**
     * This method is a no-op.
     * <p>IO 比例设置已移除，调用无效果。</p>
     *
     * @deprecated
     */
    @Deprecated
    public void setIoRatio(int ioRatio) {
        LOGGER.debug("EpollEventLoopGroup.setIoRatio(int) logic was removed, this is a no-op");
    }

    @Override
    protected IoEventLoop newChild(Executor executor, IoHandlerFactory ioHandlerFactory, Object... args) {
        RejectedExecutionHandler rejectedExecutionHandler = (RejectedExecutionHandler) args[0];
        EventLoopTaskQueueFactory taskQueueFactory = null;
        EventLoopTaskQueueFactory tailTaskQueueFactory = null;

        int argsLength = args.length;
        if (argsLength > 1) {
            taskQueueFactory = (EventLoopTaskQueueFactory) args[1];
        }
        if (argsLength > 2) {
            tailTaskQueueFactory = (EventLoopTaskQueueFactory) args[2];
        }
        return new EpollEventLoop(this, executor, ioHandlerFactory, taskQueueFactory, tailTaskQueueFactory,
                rejectedExecutionHandler);
    }
}
