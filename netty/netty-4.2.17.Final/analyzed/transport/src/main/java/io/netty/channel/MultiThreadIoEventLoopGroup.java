/*
 * Copyright 2024 The Netty Project
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

import io.netty.util.concurrent.EventExecutorChooserFactory;
import io.netty.util.internal.EmptyArrays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

/**
 * 使用多线程处理 I/O 与任务的 {@link IoEventLoopGroup} 实现。
 * <p>
 * 可通过自定义 {@link EventExecutorChooserFactory} 实现高级线程管理策略（如动态扩缩容）；
 * 若需基于利用率的自动扩缩容，可传入
 * {@link io.netty.util.concurrent.AutoScalingEventExecutorChooserFactory} 实例。
 * </p>
 */
public class MultiThreadIoEventLoopGroup extends MultithreadEventLoopGroup implements IoEventLoopGroup {

    /**
     * 使用默认线程数与默认 {@link ThreadFactory} 创建 {@link MultiThreadIoEventLoopGroup}。
     *
     * @param ioHandlerFactory 创建 {@link IoHandler} 的工厂
     */
    public MultiThreadIoEventLoopGroup(IoHandlerFactory ioHandlerFactory) {
        this(0, ioHandlerFactory);
    }

    /**
     /**
     * 使用默认 {@link ThreadFactory} 创建实例。
     *
     * @param nThreads          创建的线程数，即 {@link EventLoop} 数量
     * @param ioHandlerFactory  用于创建处理 I/O 的 {@link IoHandler} 的工厂
     */
    public MultiThreadIoEventLoopGroup(int nThreads, IoHandlerFactory ioHandlerFactory) {
        this(nThreads, (Executor) null, ioHandlerFactory);
    }

    /**
     * 使用默认线程数创建实例。
     *
     * @param threadFactory     创建 EventLoop 线程的 {@link ThreadFactory}
     * @param ioHandlerFactory  用于创建处理 I/O 的 {@link IoHandler} 的工厂
     */
    public MultiThreadIoEventLoopGroup(ThreadFactory threadFactory, IoHandlerFactory ioHandlerFactory) {
        this(0, threadFactory, ioHandlerFactory);
    }

    /**
     * 使用默认线程数创建实例。
     *
     * @param executor          执行任务的 {@link Executor}
     * @param ioHandlerFactory  用于创建处理 I/O 的 {@link IoHandler} 的工厂
     */
    public MultiThreadIoEventLoopGroup(Executor executor,
                                       IoHandlerFactory ioHandlerFactory) {
        super(0, executor, ioHandlerFactory);
    }

    /**
     * 创建 {@link MultiThreadIoEventLoopGroup}。
     *
     * @param nThreads          创建的线程数，即 {@link EventLoop} 数量
     * @param executor          执行任务的 {@link Executor}
     * @param ioHandlerFactory  用于创建处理 I/O 的 {@link IoHandler} 的工厂
     */
    public MultiThreadIoEventLoopGroup(int nThreads, Executor executor,
                                       IoHandlerFactory ioHandlerFactory) {
        super(nThreads, executor, ioHandlerFactory);
    }

    /**
     * 创建 {@link MultiThreadIoEventLoopGroup}。
     *
     * @param nThreads          创建的线程数，即 {@link EventLoop} 数量
     * @param threadFactory     创建 EventLoop 线程的 {@link ThreadFactory}
     * @param ioHandlerFactory  用于创建处理 I/O 的 {@link IoHandler} 的工厂
     */
    public MultiThreadIoEventLoopGroup(int nThreads, ThreadFactory threadFactory,
                                       IoHandlerFactory ioHandlerFactory) {
        super(nThreads, threadFactory, ioHandlerFactory);
    }

    /**
     * 创建 {@link MultiThreadIoEventLoopGroup}，可指定 EventLoop 选择策略。
     *
     * @param nThreads          创建的线程数，即 {@link EventLoop} 数量
     * @param executor          执行任务的 {@link Executor}
     * @param chooserFactory    {@link MultiThreadIoEventLoopGroup#next()} 时选择 {@link IoEventLoop} 的策略
     * @param ioHandlerFactory  用于创建处理 I/O 的 {@link IoHandler} 的工厂
     */
    public MultiThreadIoEventLoopGroup(int nThreads, Executor executor,
                                       EventExecutorChooserFactory chooserFactory,
                                       IoHandlerFactory ioHandlerFactory) {
        super(nThreads, executor, chooserFactory, ioHandlerFactory);
    }

    /**
     * 子类扩展用构造器；额外参数会传给 {@link #newChild(Executor, Object...)}。
     *
     * @param nThreads          创建的线程数
     * @param executor          执行任务的 {@link Executor}
     * @param ioHandlerFactory  用于创建 {@link IoHandler} 的工厂
     * @param args              传给 {@link #newChild(Executor, Object...)} 的额外参数
     */
    protected MultiThreadIoEventLoopGroup(int nThreads, Executor executor,
                                          IoHandlerFactory ioHandlerFactory, Object... args) {
        super(nThreads, executor, combine(ioHandlerFactory, args));
    }

    /**
     * 子类扩展用构造器；额外参数会传给 {@link #newChild(Executor, Object...)}。
     *
     * @param nThreads          创建的线程数
     * @param threadFactory     创建 EventLoop 线程的 {@link ThreadFactory}
     * @param ioHandlerFactory  用于创建 {@link IoHandler} 的工厂
     * @param args              传给 {@link #newChild(Executor, Object...)} 的额外参数
     */
    protected MultiThreadIoEventLoopGroup(int nThreads, ThreadFactory threadFactory,
                                          IoHandlerFactory ioHandlerFactory, Object... args) {
        super(nThreads, threadFactory, combine(ioHandlerFactory, args));
    }

    /**
     * 子类扩展用构造器；额外参数会传给 {@link #newChild(Executor, Executor, Object...)}。
     *
     * @param nThreads          创建的线程数
     * @param threadFactory     创建 EventLoop 线程的 {@link ThreadFactory}
     * @param ioHandlerFactory  用于创建 {@link IoHandler} 的工厂
     * @param chooserFactory    EventLoop 选择策略
     * @param args              传给 {@link #newChild(Executor, Object...)} 的额外参数
     */
    protected MultiThreadIoEventLoopGroup(int nThreads, ThreadFactory threadFactory,
                                          IoHandlerFactory ioHandlerFactory,
                                          EventExecutorChooserFactory chooserFactory,
                                          Object... args) {
        super(nThreads, threadFactory, chooserFactory, combine(ioHandlerFactory, args));
    }

    /**
     * 子类扩展用构造器；额外参数会传给 {@link #newChild(Executor, Object...)}。
     *
     * @param nThreads          创建的线程数
     * @param executor          执行任务的 {@link Executor}
     * @param ioHandlerFactory  用于创建 {@link IoHandler} 的工厂
     * @param chooserFactory    EventLoop 选择策略
     * @param args              传给 {@link #newChild(Executor, Object...)} 的额外参数
     */
    protected MultiThreadIoEventLoopGroup(int nThreads, Executor executor,
                                          IoHandlerFactory ioHandlerFactory,
                                          EventExecutorChooserFactory chooserFactory,
                                          Object... args) {
        super(nThreads, executor, chooserFactory, combine(ioHandlerFactory, args));
    }

    // 返回类型本应为 IoHandleEventLoop，为引入 IoHandle 概念且不破坏 API 而声明为 EventLoop
    @Override
    protected EventLoop newChild(Executor executor, Object... args) throws Exception {
        IoHandlerFactory handlerFactory = (IoHandlerFactory) args[0];
        Object[] argsCopy;
        if (args.length > 1) {
            argsCopy = new Object[args.length - 1];
            System.arraycopy(args, 1, argsCopy, 0, argsCopy.length);
        } else {
            argsCopy = EmptyArrays.EMPTY_OBJECTS;
        }
        return newChild(executor, handlerFactory, argsCopy);
    }

    /**
     * 使用给定 {@link Executor} 与 {@link IoHandlerFactory} 创建新的 {@link IoEventLoop}。
     *
     * @param executor              执行任务与 I/O 的 {@link Executor}
     * @param ioHandlerFactory      获取 {@link IoHandler} 的工厂
     * @param args                  构造器传入的额外参数
     * @return                      新创建的 {@link IoEventLoop}
     */
    protected IoEventLoop newChild(Executor executor, IoHandlerFactory ioHandlerFactory,
                                   @SuppressWarnings("unused") Object... args) {
        return new SingleThreadIoEventLoop(this, executor, ioHandlerFactory);
    }

    @Override
    public IoEventLoop next() {
        return (IoEventLoop) super.next();
    }

    /** 将 handler 工厂与额外参数合并为 {@link #newChild} 所需的 args 数组。 */
    private static Object[] combine(IoHandlerFactory handlerFactory, Object... args) {
        List<Object> combinedList = new ArrayList<Object>();
        combinedList.add(handlerFactory);
        if (args != null) {
            Collections.addAll(combinedList, args);
        }
        return combinedList.toArray(new Object[0]);
    }
}
