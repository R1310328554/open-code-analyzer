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

import io.netty.util.concurrent.ThreadAwareExecutor;

/**
 * Handles IO dispatching for an {@link ThreadAwareExecutor}.
 * All operations except {@link #wakeup()} and {@link #isCompatible(Class)} <strong>MUST</strong> be executed
 * on the {@link ThreadAwareExecutor} thread (which means {@link ThreadAwareExecutor#isExecutorThread(Thread)} must
 * return {@code true}) and should never be called from the user-directly.
 * <p>
 * Once a {@link IoHandle} is registered via the {@link #register(IoHandle)} method it's possible
 * to submit {@link IoOps} related to the {@link IoHandle} via {@link IoRegistration#submit(IoOps)}.
 * These submitted {@link IoOps} are the "source" of {@link IoEvent}s that are dispatched to the registered
 * {@link IoHandle} via the {@link IoHandle#handle(IoRegistration, IoEvent)} method.
 * These events must be consumed (and handled) as otherwise they might be reported again until handled.
 * <p>为 {@link ThreadAwareExecutor} 分发 I/O 的处理器。除 {@link #wakeup()} 与
 * {@link #isCompatible(Class)} 外，所有操作必须在执行器线程上调用，不应由用户直接调用。
 * 注册 {@link IoHandle} 后可通过 {@link IoRegistration#submit(IoOps)} 提交操作，
 * 完成时产生 {@link IoEvent} 并分派给 {@link IoHandle#handle(IoRegistration, IoEvent)}；
 * 事件须被消费处理，否则可能重复上报。</p>
 *
 */
public interface IoHandler {

    /**
     * Initialize this {@link IoHandler}.
     * <p>初始化本 {@link IoHandler}。</p>
     */
    default void initialize() { }

    /**
     * Run the IO handled by this {@link IoHandler}. The {@link IoHandlerContext} should be used
     * to ensure we not execute too long and so block the processing of other task that are
     * scheduled on the {@link ThreadAwareExecutor}. This is done by taking {@link IoHandlerContext#delayNanos(long)}
     * or {@link IoHandlerContext#deadlineNanos()} into account.
     * <p>执行一轮 I/O 处理。应依据 {@link IoHandlerContext} 的 {@link IoHandlerContext#delayNanos(long)}
     * 或 {@link IoHandlerContext#deadlineNanos()} 避免占用过久、阻塞同线程上的其他任务。</p>
     *
     * @param  context  the {@link IoHandlerContext}.
     * @return          the number of {@link IoHandle} for which I/O was handled.
     *                  Internal events such as wakeups and timer expirations must not be included in this count.
     */
    int run(IoHandlerContext context);

    /**
     * Prepare to destroy this {@link IoHandler}. This method will be called before {@link #destroy()} and may be
     * called multiple times.
     * <p>销毁前的准备阶段，可能在 {@link #destroy()} 之前多次调用。</p>
     */
    default void prepareToDestroy() { }

    /**
     * Destroy the {@link IoHandler} and free all its resources. Once destroyed using the {@link IoHandler} will
     * cause undefined behaviour.
     * <p>销毁并释放资源；销毁后继续使用行为未定义。</p>
     */
    default void destroy() { }

    /**
     * Register a {@link IoHandle} for IO.
     * <p>注册 {@link IoHandle} 以参与 I/O 处理。</p>
     *
     * @param handle        the {@link IoHandle} to register.
     * @throws Exception    thrown if an error happens during registration.
     */
    IoRegistration register(IoHandle handle) throws Exception;

    /**
     * Wakeup the {@link IoHandler}, which means if any operation blocks it should be unblocked and
     * return as soon as possible.
     * <p>唤醒处理器，使阻塞中的 I/O 等待尽快返回。</p>
     */
    void wakeup();

    /**
     * Returns {@code true} if the given type is compatible with this {@link IoHandler} and so can be registered,
     * {@code false} otherwise.
     * <p>若给定 {@link IoHandle} 类型与本处理器兼容、可注册，则返回 {@code true}。</p>
     *
     * @param handleType the type of the {@link IoHandle}.
     * @return if compatible of not.
     */
    boolean isCompatible(Class<? extends IoHandle> handleType);
}
