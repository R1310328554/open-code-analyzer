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
package io.netty.channel.kqueue;

import io.netty.channel.Channel;
import io.netty.channel.DefaultSelectStrategyFactory;
import io.netty.channel.IoHandle;
import io.netty.channel.IoHandler;
import io.netty.channel.IoHandlerContext;
import io.netty.channel.IoHandlerFactory;
import io.netty.channel.IoOps;
import io.netty.channel.IoRegistration;
import io.netty.channel.SelectStrategy;
import io.netty.channel.SelectStrategyFactory;
import io.netty.channel.unix.FileDescriptor;
import io.netty.util.IntSupplier;
import io.netty.util.collection.LongObjectHashMap;
import io.netty.util.collection.LongObjectMap;
import io.netty.util.concurrent.ThreadAwareExecutor;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import static java.lang.Math.min;

/**
 * {@link IoHandler} which uses kqueue under the covers. Only works on BSD!
 * <p>BSD/macOS 原生 kqueue 实现的 {@link IoHandler}：管理 changelist/eventlist、 IoRegistration 与 {@code kevent(2)} 事件循环。</p>
 */
public final class KQueueIoHandler implements IoHandler {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(KQueueIoHandler.class);
    private static final AtomicIntegerFieldUpdater<KQueueIoHandler> WAKEN_UP_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(KQueueIoHandler.class, "wakenUp");
    /** 内部唤醒用的 kevent ident，保留不可被通道占用 */
    private static final int KQUEUE_WAKE_UP_IDENT = 0;
    // kqueue 超时过大可能返回 EINVAL；上限取约 24 小时
    // https://man.freebsd.org/cgi/man.cgi?query=kevent&apropos=0&sektion=0&manpath=FreeBSD+6.1-RELEASE&format=html#end
    /** kevent 等待最大秒数（24 小时减 1 秒） */
    private static final int KQUEUE_MAX_TIMEOUT_SECONDS = 86399; // 24 hours - 1 second

    {
        KQueue.ensureAvailability();
    }

    /** 事件数组是否允许在 wait 填满时自动扩容 */
    private final boolean allowGrowing;
    /** kqueue 文件描述符 */
    private final FileDescriptor kqueueFd;
    /** 提交给 kevent 的 changelist */
    private final KQueueEventArray changeList;
    /** kevent 返回的 eventlist */
    private final KQueueEventArray eventList;
    private final SelectStrategy selectStrategy;
    private final NativeArrays nativeArrays;
    private final IntSupplier selectNowSupplier = new IntSupplier() {
        @Override
        public int get() throws Exception {
            return kqueueWaitNow();
        }
    };
    private final ThreadAwareExecutor executor;
    private final Queue<DefaultKqueueIoRegistration> cancelledRegistrations = new ArrayDeque<>();
    /** udata（registration id）到 IoRegistration 的映射 */
    private final LongObjectMap<DefaultKqueueIoRegistration> registrations = new LongObjectHashMap<>(4096);
    private int numChannels;
    private long nextId;

    /** 是否已被其他线程唤醒（CAS 协调 wakeup 与 select） */
    private volatile int wakenUp;

    private long generateNextId() {
        boolean reset = false;
        for (;;) {
            if (nextId == Long.MAX_VALUE) {
                if (reset) {
                    throw new IllegalStateException("All possible ids in use");
                }
                reset = true;
            }
            nextId++;
            if (nextId == KQUEUE_WAKE_UP_IDENT) {
                continue;
            }
            if (!registrations.containsKey(nextId)) {
                return nextId;
            }
        }
    }

    /**
     * Returns a new {@link IoHandlerFactory} that creates {@link KQueueIoHandler} instances.
     * <p>返回创建 {@link KQueueIoHandler} 的工厂；默认 maxEvents=0 表示可增长数组。</p>
     */
    public static IoHandlerFactory newFactory() {
        return newFactory(0, DefaultSelectStrategyFactory.INSTANCE);
    }

    /**
     * Returns a new {@link IoHandlerFactory} that creates {@link KQueueIoHandler} instances.
     * <p>返回创建 KQueueIoHandler 的工厂。</p>
     */
    public static IoHandlerFactory newFactory(final int maxEvents,
                                              final SelectStrategyFactory selectStrategyFactory) {
        KQueue.ensureAvailability();
        ObjectUtil.checkPositiveOrZero(maxEvents, "maxEvents");
        ObjectUtil.checkNotNull(selectStrategyFactory, "selectStrategyFactory");
        return new IoHandlerFactory() {
            @Override
            public IoHandler newHandler(ThreadAwareExecutor executor) {
                return new KQueueIoHandler(executor, maxEvents, selectStrategyFactory.newSelectStrategy());
            }

            @Override
            public boolean isChangingThreadSupported() {
                return true;
            }
        };
    }

    private KQueueIoHandler(ThreadAwareExecutor executor, int maxEvents, SelectStrategy strategy) {
        this.executor = ObjectUtil.checkNotNull(executor, "executor");
        this.selectStrategy = ObjectUtil.checkNotNull(strategy, "strategy");
        this.kqueueFd = Native.newKQueue();
        if (maxEvents == 0) {
            allowGrowing = true;
            maxEvents = 4096;
        } else {
            allowGrowing = false;
        }
        this.changeList = new KQueueEventArray(maxEvents);
        this.eventList = new KQueueEventArray(maxEvents);
        nativeArrays = new NativeArrays();
        int result = Native.keventAddUserEvent(kqueueFd.intValue(), KQUEUE_WAKE_UP_IDENT);
        if (result < 0) {
            destroy();
            throw new IllegalStateException("kevent failed to add user event with errno: " + (-result));
        }
    }

    @Override
    public void wakeup() {
        if (!executor.isExecutorThread(Thread.currentThread())
                && WAKEN_UP_UPDATER.compareAndSet(this, 0, 1)) {
            wakeup0();
        }
    }

    private void wakeup0() {
        Native.keventTriggerUserEvent(kqueueFd.intValue(), KQUEUE_WAKE_UP_IDENT);
        // 关闭后可能 EBADF；不强制断言返回值 >= 0
    }

    private int kqueueWait(IoHandlerContext context, boolean oldWakeup) throws IOException {
        // wakenUp=1 时提交的任务可能未触发 user event；阻塞前需再检查任务队列
        if (oldWakeup && !context.canBlock()) {
            return kqueueWaitNow();
        }

        long totalDelay = context.delayNanos(System.nanoTime());
        int delaySeconds = (int) min(totalDelay / 1000000000L, KQUEUE_MAX_TIMEOUT_SECONDS);
        int delayNanos = (int) (totalDelay % 1000000000L);
        return kqueueWait(delaySeconds, delayNanos);
    }

    private int kqueueWaitNow() throws IOException {
        return kqueueWait(0, 0);
    }

    private int kqueueWait(int timeoutSec, int timeoutNs) throws IOException {
        int numEvents = Native.keventWait(kqueueFd.intValue(), changeList, eventList, timeoutSec, timeoutNs);
        changeList.clear();
        return numEvents;
    }

    private int processReady(int ready) {
        int ioCount = 0;
        for (int i = 0; i < ready; ++i) {
            final short filter = eventList.filter(i);
            final short flags = eventList.flags(i);
            final int ident = eventList.ident(i);
            if (filter == Native.EVFILT_USER || (flags & Native.EV_ERROR) != 0) {
                // FD 已关闭并从 kqueue 移除后删除过滤器会返回 EV_ERROR
                assert filter != Native.EVFILT_USER ||
                        (filter == Native.EVFILT_USER && ident == KQUEUE_WAKE_UP_IDENT);
                continue;
            }

            ioCount++;
            long id = eventList.udata(i);
            DefaultKqueueIoRegistration registration = registrations.get(id);
            if (registration == null) {
                // 通道已关闭时 registration 可能为 null；EV_ERROR 路径已提前跳过
                logger.warn("events[{}]=[{}, {}, {}] had no registration!", i, ident, id, filter);
                continue;
            }
            registration.handle(ident, filter, flags, eventList.fflags(i), eventList.data(i), id);
        }
        return ioCount;
    }

    @Override
    public int run(IoHandlerContext context) {
        int handled = 0;
        try {
            int strategy = selectStrategy.calculateStrategy(selectNowSupplier, !context.canBlock());
            switch (strategy) {
                case SelectStrategy.CONTINUE:
                    if (context.shouldReportActiveIoTime()) {
                        context.reportActiveIoTime(0); // Report zero as we did no I/O.
                    }
                    return 0;

                case SelectStrategy.BUSY_WAIT:
                    // kqueue 不支持 busy-wait，回落到 SELECT

                case SelectStrategy.SELECT:
                    strategy = kqueueWait(context, WAKEN_UP_UPDATER.getAndSet(this, 0) == 1);

                    // 'wakenUp.compareAndSet(false, true)' is always evaluated
                    // before calling 'selector.wakeup()' to reduce the wake-up
                    // overhead. (Selector.wakeup() is an expensive operation.)
                    //
                    // However, there is a race condition in this approach.
                    // The race condition is triggered when 'wakenUp' is set to
                    // true too early.
                    //
                    // 'wakenUp' is set to true too early if:
                    // 1) Selector is waken up between 'wakenUp.set(false)' and
                    //    'selector.select(...)'. (BAD)
                    // 2) Selector is waken up between 'selector.select(...)' and
                    //    'if (wakenUp.get()) { ... }'. (OK)
                    //
                    // In the first case, 'wakenUp' is set to true and the
                    // following 'selector.select(...)' will wake up immediately.
                    // Until 'wakenUp' is set to false again in the next round,
                    // 'wakenUp.compareAndSet(false, true)' will fail, and therefore
                    // any attempt to wake up the Selector will fail, too, causing
                    // the following 'selector.select(...)' call to block
                    // unnecessarily.
                    //
                    // To fix this problem, we wake up the selector again if wakenUp
                    // is true immediately after selector.select(...).
                    // It is inefficient in that it wakes up the selector for both
                    // the first case (BAD - wake-up required) and the second case
                    // (OK - no wake-up required).

                    if (wakenUp == 1) {
                        wakeup0();
                    }
                    // fall-through
                default:
            }

            if (strategy > 0) {
                if (context.shouldReportActiveIoTime()) {
                    long activeIoStartTimeNanos = System.nanoTime();
                    handled = processReady(strategy);
                    long activeIoEndTimeNanos = System.nanoTime();
                    context.reportActiveIoTime(activeIoEndTimeNanos - activeIoStartTimeNanos);
                } else {
                    handled = processReady(strategy);
                }
            } else if (context.shouldReportActiveIoTime()) {
                context.reportActiveIoTime(0);
            }

            if (allowGrowing && strategy == eventList.capacity()) {
                // eventlist 已满，按需扩容
                eventList.realloc(false);
            }
        } catch (Error e) {
            throw e;
        } catch (Throwable t) {
            handleLoopException(t);
        } finally {
            processCancelledRegistrations();
        }
        return handled;
    }

    // 处理已取消的 registration，从映射中移除
    private void processCancelledRegistrations() {
        for (;;) {
            DefaultKqueueIoRegistration cancelledRegistration = cancelledRegistrations.poll();
            if (cancelledRegistration == null) {
                return;
            }
            DefaultKqueueIoRegistration removed = registrations.remove(cancelledRegistration.id);
            assert removed == cancelledRegistration;
            if (removed.isHandleForChannel()) {
                numChannels--;
            }
            removed.handle.unregistered();
        }
    }

    int numRegisteredChannels() {
        return numChannels;
    }

    List<Channel> registeredChannelsList() {
        LongObjectMap<DefaultKqueueIoRegistration> ch = registrations;
        if (ch.isEmpty()) {
            return Collections.emptyList();
        }

        List<Channel> channels = new ArrayList<>(ch.size());

        for (DefaultKqueueIoRegistration registration : ch.values()) {
            if (registration.handle instanceof AbstractKQueueChannel.AbstractKQueueUnsafe) {
                channels.add(((AbstractKQueueChannel.AbstractKQueueUnsafe) registration.handle).channel());
            }
        }
        return Collections.unmodifiableList(channels);
    }

    private static void handleLoopException(Throwable t) {
        logger.warn("Unexpected exception in the selector loop.", t);

        // 避免 selector 循环连续失败导致 CPU 空转
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Ignore.
        }
    }

    @Override
    public void prepareToDestroy() {
        try {
            kqueueWaitNow();
        } catch (IOException e) {
            // ignore on close
        }

        // 拷贝数组避免 close 时修改 registrations 导致 CME
        DefaultKqueueIoRegistration[] copy = registrations.values().toArray(new DefaultKqueueIoRegistration[0]);

        for (DefaultKqueueIoRegistration reg: copy) {
            reg.close();
        }

        processCancelledRegistrations();
    }

    @Override
    public void destroy() {
        try {
            try {
                kqueueFd.close();
            } catch (IOException e) {
                logger.warn("Failed to close the kqueue fd.", e);
            }
        } finally {
            // 释放 kqueue fd 与堆外 kevent/Iov 内存
            nativeArrays.free();
            changeList.free();
            eventList.free();
        }
    }

    @Override
    public IoRegistration register(IoHandle handle) {
        final KQueueIoHandle kqueueHandle = cast(handle);
        if (kqueueHandle.ident() == KQUEUE_WAKE_UP_IDENT) {
            throw new IllegalArgumentException("ident " + KQUEUE_WAKE_UP_IDENT + " is reserved for internal usage");
        }

        DefaultKqueueIoRegistration registration = new DefaultKqueueIoRegistration(
                executor, kqueueHandle);
        DefaultKqueueIoRegistration old = registrations.put(registration.id, registration);
        if (old != null) {
            // 理论上不应发生 id 冲突
            registrations.put(old.id, old);
            throw new IllegalStateException();
        }
        if (registration.isHandleForChannel()) {
            numChannels++;
        }
        handle.registered();
        return registration;
    }

    private static KQueueIoHandle cast(IoHandle handle) {
        if (handle instanceof KQueueIoHandle) {
            return (KQueueIoHandle) handle;
        }
        throw new IllegalArgumentException("IoHandle of type " + StringUtil.simpleClassName(handle) + " not supported");
    }

    private static KQueueIoOps cast(IoOps ops) {
        if (ops instanceof KQueueIoOps) {
            return (KQueueIoOps) ops;
        }
        throw new IllegalArgumentException("IoOps of type " + StringUtil.simpleClassName(ops) + " not supported");
    }

    @Override
    public boolean isCompatible(Class<? extends IoHandle> handleType) {
        return KQueueIoHandle.class.isAssignableFrom(handleType);
    }

    /** 单条 {@link KQueueIoHandle} 在 kqueue 上的注册与 changelist 提交 */
    private final class DefaultKqueueIoRegistration implements IoRegistration {
        private boolean cancellationPending;
        private final AtomicBoolean canceled = new AtomicBoolean();
        private final KQueueIoEvent event = new KQueueIoEvent();

        final KQueueIoHandle handle;
        final long id;
        private final ThreadAwareExecutor executor;

        DefaultKqueueIoRegistration(ThreadAwareExecutor executor, KQueueIoHandle handle) {
            this.executor = executor;
            this.handle = handle;
            id = generateNextId();
        }

        boolean isHandleForChannel() {
            return handle instanceof AbstractKQueueChannel.AbstractKQueueUnsafe;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T attachment() {
            return (T) nativeArrays;
        }

        @Override
        public long submit(IoOps ops) {
            KQueueIoOps kQueueIoOps = cast(ops);
            if (!isValid()) {
                return -1;
            }
            short filter = kQueueIoOps.filter();
            short flags = kQueueIoOps.flags();
            int fflags = kQueueIoOps.fflags();
            long data = kQueueIoOps.data();
            if (executor.isExecutorThread(Thread.currentThread())) {
                evSet(filter, flags, fflags, data);
            } else {
                executor.execute(() -> evSet(filter, flags, fflags, data));
            }
            return 0;
        }

        void handle(int ident, short filter, short flags, int fflags, long data, long udata) {
            if (cancellationPending) {
                // 已取消但尚未从 map 移除，忽略后续 kevent
                return;
            }
            event.update(ident, filter, flags, fflags, data, udata);
            handle.handle(this, event);
        }

        private void evSet(short filter, short flags, int fflags, long data) {
            if (cancellationPending) {
                // This registration was already cancelled but not removed from the map yet, just ignore.
                return;
            }
            changeList.evSet(handle.ident(), filter, flags, fflags, data, id);
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

        private void cancel0() {
            // 延迟到本轮事件处理后再从 map 移除，避免未处理事件丢失
            cancellationPending = true;
            cancelledRegistrations.offer(this);
        }

        void close() {
            cancel();
            try {
                handle.close();
            } catch (Exception e) {
                logger.debug("Exception during closing " + handle, e);
            }
        }
    }
}
