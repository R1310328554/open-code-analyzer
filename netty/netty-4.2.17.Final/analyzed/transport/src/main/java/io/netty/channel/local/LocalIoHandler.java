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
package io.netty.channel.local;

import io.netty.channel.IoHandlerContext;
import io.netty.channel.IoHandle;
import io.netty.channel.IoHandler;
import io.netty.channel.IoHandlerFactory;
import io.netty.channel.IoOps;
import io.netty.channel.IoRegistration;
import io.netty.util.concurrent.ThreadAwareExecutor;
import io.netty.util.internal.StringUtil;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * 本地传输的 {@link IoHandler}：无真实网络 I/O，负责注册/注销 {@link LocalIoHandle} 并在事件循环中阻塞等待。
 */
public final class LocalIoHandler implements IoHandler {
    /** 已注册的本地 I/O 句柄集合。 */
    private final Set<LocalIoHandle> registeredChannels = new HashSet<LocalIoHandle>(64);
    private final ThreadAwareExecutor executor;
    /** 执行 {@link #run} 的线程，用于 {@link #wakeup} 时 unpark。 */
    private volatile Thread executionThread;

    private LocalIoHandler(ThreadAwareExecutor executor) {
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    /**
     * Returns a new {@link IoHandlerFactory} that creates {@link LocalIoHandler} instances.
     * <p>返回创建 {@link LocalIoHandler} 的工厂。</p>
     */
    public static IoHandlerFactory newFactory() {
        return LocalIoHandler::new;
    }

    /** 将 {@link IoHandle} 转为 {@link LocalIoHandle}，类型不符则抛异常。 */
    private static LocalIoHandle cast(IoHandle handle) {
        if (handle instanceof LocalIoHandle) {
            return (LocalIoHandle) handle;
        }
        throw new IllegalArgumentException("IoHandle of type " + StringUtil.simpleClassName(handle) + " not supported");
    }

    @Override
    public int run(IoHandlerContext context) {
        if (executionThread == null) {
            executionThread = Thread.currentThread();
        }
        if (context.canBlock()) {
            // 本地传输无就绪 I/O，可阻塞直到 wakeup 或超时
            LockSupport.parkNanos(this, context.delayNanos(System.nanoTime()));
        }

        if (context.shouldReportActiveIoTime()) {
            context.reportActiveIoTime(0);
        }
        return 0;
    }

    @Override
    public void wakeup() {
        if (!executor.isExecutorThread(Thread.currentThread())) {
            Thread thread = executionThread;
            if (thread != null) {
                // 唤醒正在 park 的事件循环线程
                LockSupport.unpark(thread);
            }
        }
    }

    @Override
    public void prepareToDestroy() {
        for (LocalIoHandle handle : registeredChannels) {
            handle.closeNow();
        }
        registeredChannels.clear();
    }

    @Override
    public void destroy() {
    }

    @Override
    public IoRegistration register(IoHandle handle) {
        LocalIoHandle localHandle = cast(handle);
        if (registeredChannels.add(localHandle)) {
            LocalIoRegistration registration = new LocalIoRegistration(executor, localHandle);
            localHandle.registered();
            return registration;
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean isCompatible(Class<? extends IoHandle> handleType) {
        return LocalIoHandle.class.isAssignableFrom(handleType);
    }

    /** 本地 channel 的 {@link IoRegistration} 实现。 */
    private final class LocalIoRegistration implements IoRegistration {
        private final AtomicBoolean canceled = new AtomicBoolean();
        private final ThreadAwareExecutor executor;
        private final LocalIoHandle handle;

        LocalIoRegistration(ThreadAwareExecutor executor, LocalIoHandle handle) {
            this.executor = executor;
            this.handle = handle;
        }

        @Override
        public <T> T attachment() {
            return null;
        }

        @Override
        public long submit(IoOps ops) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isValid() {
            return !canceled.get();
        }

        @Override
        public boolean cancel() {
            if (!canceled.compareAndSet(false, true)) {
                return false;
            }
            if (executor.isExecutorThread(Thread.currentThread())) {
                cancel0();
            } else {
                executor.execute(this::cancel0);
            }
            return true;
        }

        /** 从注册表移除并触发 unregistered 回调。 */
        private void cancel0() {
            if (registeredChannels.remove(handle)) {
                handle.unregistered();
            }
        }
    }
}
