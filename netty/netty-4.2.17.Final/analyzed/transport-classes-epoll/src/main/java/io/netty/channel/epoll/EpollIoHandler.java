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

import io.netty.channel.Channel;
import io.netty.channel.DefaultSelectStrategyFactory;
import io.netty.channel.IoHandlerContext;
import io.netty.channel.IoHandle;
import io.netty.channel.IoHandler;
import io.netty.channel.IoHandlerFactory;
import io.netty.channel.IoOps;
import io.netty.channel.IoRegistration;
import io.netty.channel.SelectStrategy;
import io.netty.channel.SelectStrategyFactory;
import io.netty.channel.unix.FileDescriptor;
import io.netty.util.IntSupplier;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import io.netty.util.concurrent.ThreadAwareExecutor;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.UnstableApi;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Math.min;

/**
 * {@link IoHandler} which uses epoll under the covers. Only works on Linux!
 * <p>Linux epoll 核心 {@link IoHandler}：管理 epoll fd、eventfd 唤醒、timerfd 定时与通道注册。</p>
 */
public class EpollIoHandler implements IoHandler {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(EpollIoHandler.class);
    private static final long EPOLL_WAIT_MILLIS_THRESHOLD =
            SystemPropertyUtil.getLong("io.netty.channel.epoll.epollWaitThreshold", 10);

    {
        Epoll.ensureAvailability();
    }

    // 初始 deadline 哨兵值，保证与任何真实任务时间不同
    private long prevDeadlineNanos = NONE;
    private FileDescriptor epollFd;
    private FileDescriptor eventFd;
    private FileDescriptor timerFd;
    private final IntObjectMap<DefaultEpollIoRegistration> registrations = new IntObjectHashMap<>(4096);
    private final boolean allowGrowing;
    private final EpollEventArray events;
    private final NativeArrays nativeArrays;

    private final SelectStrategy selectStrategy;
    private final IntSupplier selectNowSupplier = new IntSupplier() {
        @Override
        public int get() throws Exception {
            return epollWaitNow();
        }
    };
    private final ThreadAwareExecutor executor;

    private static final long AWAKE = -1L;
    private static final long NONE = Long.MAX_VALUE;

    // nextWakeupNanos 状态：AWAKE=已唤醒；NONE=阻塞无定时；其他=计划在 T 纳秒唤醒
    private final AtomicLong nextWakeupNanos = new AtomicLong(AWAKE);
    private boolean pendingWakeup;

    private int numChannels;

    // timerfd 单次调度纳秒上限，见 man timerfd_create
    private static final long MAX_SCHEDULED_TIMERFD_NS = 999999999;

    /**
     * Returns a new {@link IoHandlerFactory} that creates {@link EpollIoHandler} instances.
     * <p>创建默认 {@link EpollIoHandler} 工厂（事件数组可增长）。</p>
     */
    public static IoHandlerFactory newFactory() {
        return newFactory(0, DefaultSelectStrategyFactory.INSTANCE);
    }

    /**
     * Returns a new {@link IoHandlerFactory} that creates {@link EpollIoHandler} instances.
     * <p>创建 {@link EpollIoHandler} 实例的工厂。</p>
     */
    public static IoHandlerFactory newFactory(final int maxEvents,
                                              final SelectStrategyFactory selectStrategyFactory) {
        Epoll.ensureAvailability();
        ObjectUtil.checkPositiveOrZero(maxEvents, "maxEvents");
        ObjectUtil.checkNotNull(selectStrategyFactory, "selectStrategyFactory");
        return new IoHandlerFactory() {
            @Override
            public IoHandler newHandler(ThreadAwareExecutor executor) {
                return new EpollIoHandler(executor, maxEvents, selectStrategyFactory.newSelectStrategy());
            }

            @Override
            public boolean isChangingThreadSupported() {
                return true;
            }
        };
    }

    // 包内可见，供单元测试构造
    EpollIoHandler(ThreadAwareExecutor executor, int maxEvents, SelectStrategy strategy) {
        this.executor = ObjectUtil.checkNotNull(executor, "executor");
        selectStrategy = ObjectUtil.checkNotNull(strategy, "strategy");
        if (maxEvents == 0) {
            allowGrowing = true;
            events = new EpollEventArray(4096);
        } else {
            allowGrowing = false;
            events = new EpollEventArray(maxEvents);
        }
        nativeArrays = new NativeArrays();
        openFileDescriptors();
    }

    private static EpollIoHandle cast(IoHandle handle) {
        if (handle instanceof EpollIoHandle) {
            return (EpollIoHandle) handle;
        }
        throw new IllegalArgumentException("IoHandle of type " + StringUtil.simpleClassName(handle) + " not supported");
    }

    private static EpollIoOps cast(IoOps ops) {
        if (ops instanceof EpollIoOps) {
            return (EpollIoOps) ops;
        }
        throw new IllegalArgumentException("IoOps of type " + StringUtil.simpleClassName(ops) + " not supported");
    }

    /**
     * This method is intended for use by a process checkpoint/restore
     * integration, such as OpenJDK CRaC.
     * <p>打开 epoll/eventfd/timerfd；供 CRaC 等检查点恢复集成使用。</p>
     */
    @UnstableApi
    public void openFileDescriptors() {
        boolean success = false;
        FileDescriptor epollFd = null;
        FileDescriptor eventFd = null;
        FileDescriptor timerFd = null;
        try {
            this.epollFd = epollFd = Native.newEpollCreate();
            this.eventFd = eventFd = Native.newEventFd();
            try {
                // eventfd 使用 EPOLLET，每次唤醒只通知一次，避免重复 read
                Native.epollCtlAdd(epollFd.intValue(), eventFd.intValue(), Native.EPOLLIN | Native.EPOLLET);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to add eventFd filedescriptor to epoll", e);
            }
            this.timerFd = timerFd = Native.newTimerFd();
            try {
                // It is important to use EPOLLET here as we only want to get the notification once per
                // wakeup and don't call read(...).
                Native.epollCtlAdd(epollFd.intValue(), timerFd.intValue(), Native.EPOLLIN | Native.EPOLLET);
            } catch (IOException e) {
                throw new IllegalStateException("Unable to add timerFd filedescriptor to epoll", e);
            }
            success = true;
        } finally {
            if (!success) {
                closeFileDescriptor(epollFd);
                closeFileDescriptor(eventFd);
                closeFileDescriptor(timerFd);
            }
        }
    }

    private static void closeFileDescriptor(FileDescriptor fd) {
        if (fd != null) {
            try {
                fd.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Override
    public void wakeup() {
        if (!executor.isExecutorThread(Thread.currentThread()) && nextWakeupNanos.getAndSet(AWAKE) != AWAKE) {
            // 向 eventfd 写入以唤醒阻塞在 epoll_wait 的线程
            Native.eventFdWrite(eventFd.intValue(), 1L);
        }
    }

    @Override
    public void prepareToDestroy() {
        // 先拷贝注册表再关闭，避免 close 时修改 map 导致 CME
        DefaultEpollIoRegistration[] copy = registrations.values().toArray(new DefaultEpollIoRegistration[0]);

        for (DefaultEpollIoRegistration reg: copy) {
            reg.close();
        }
    }

    @Override
    public void destroy() {
        try {
            closeFileDescriptors();
        } finally {
            nativeArrays.free();
            events.free();
        }
    }

    private enum RegistrationState {
        // 尚未 EPOLL_CTL_ADD
        Pending,
        // 已通过 EPOLL_CTL_ADD 注册
        Added,
        // 已取消并从 epoll 删除
        Cancelled
    }

    private final class DefaultEpollIoRegistration
            implements IoRegistration {
        private final ThreadAwareExecutor executor;
        private RegistrationState state = RegistrationState.Pending;
        final EpollIoHandle handle;

        DefaultEpollIoRegistration(ThreadAwareExecutor executor, EpollIoHandle handle) {
            this.executor = executor;
            this.handle = handle;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T attachment() {
            return (T) nativeArrays;
        }

        @Override
        public long submit(IoOps ops) {
            EpollIoOps epollIoOps = cast(ops);
            try {
                synchronized (this) {
                    switch (state) {
                        case Cancelled:
                            return -1;
                        case Pending:
                            if (epollIoOps.value == EpollIoOps.NONE.value) {
                                // ops 为 0 表示不监听；Pending 状态直接返回
                                return 0;
                            }
                            Native.epollCtlAdd(epollFd.intValue(), handle.fd().intValue(), epollIoOps.value);
                            state = RegistrationState.Added;
                            return epollIoOps.value;
                        case Added:
                            if (epollIoOps.value == EpollIoOps.NONE.value) {
                                // ops 为 0 则 EPOLL_CTL_DEL，避免 EPOLLHUP/ERR 永久通知
                                Native.epollCtlDel(epollFd.intValue(), handle.fd().intValue());
                                return 0;
                            }
                            Native.epollCtlMod(epollFd.intValue(), handle.fd().intValue(), epollIoOps.value);
                            return epollIoOps.value;
                        default:
                            throw new IllegalStateException();
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public synchronized boolean isValid() {
            return state != RegistrationState.Cancelled;
        }

        @Override
        public boolean cancel() {
            synchronized (this) {
                if (state == RegistrationState.Cancelled) {
                    return false;
                }
                state = RegistrationState.Cancelled;
            }
            if (executor.isExecutorThread(Thread.currentThread())) {
                cancel0();
            } else {
                executor.execute(this::cancel0);
            }
            return true;
        }

        private void cancel0() {
            int fd = handle.fd().intValue();
            DefaultEpollIoRegistration old = registrations.remove(fd);
            if (old != null) {
                if (old != this) {
                    // fd 复用导致映射已被替换，恢复旧映射
                    registrations.put(fd, old);
                    return;
                } else if (old.handle instanceof AbstractEpollChannel.AbstractEpollUnsafe) {
                    numChannels--;
                }
                if (handle.fd().isOpen()) {
                    try {
                        // fd 仍打开时显式 EPOLL_CTL_DEL；关闭后内核会自动移除
                        Native.epollCtlDel(epollFd.intValue(), fd);
                    } catch (IOException e) {
                        logger.debug("Unable to remove fd {} from epoll {}", fd, epollFd.intValue());
                    }
                }
                handle.unregistered();
            }
        }

        void close() {
            try {
                cancel();
            } catch (Exception e) {
                logger.debug("Exception during canceling " + this, e);
            }
            try {
                handle.close();
            } catch (Exception e) {
                logger.debug("Exception during closing " + handle, e);
            }
        }

        void handle(long ev) {
            handle.handle(this, EpollIoOps.eventOf((int) ev));
        }
    }

    @Override
    public IoRegistration register(IoHandle handle)
            throws Exception {
        final EpollIoHandle epollHandle = cast(handle);
        DefaultEpollIoRegistration registration = new DefaultEpollIoRegistration(executor, epollHandle);
        int fd = epollHandle.fd().intValue();
        DefaultEpollIoRegistration old = registrations.put(fd, registration);

        // 同 fd 无有效旧注册，或旧 fd 已关闭
        assert old == null || !old.isValid();

        if (epollHandle instanceof AbstractEpollChannel.AbstractEpollUnsafe) {
            numChannels++;
        }
        handle.registered();
        return registration;
    }

    @Override
    public boolean isCompatible(Class<? extends IoHandle> handleType) {
        return EpollIoHandle.class.isAssignableFrom(handleType);
    }

    int numRegisteredChannels() {
        return numChannels;
    }

    List<Channel> registeredChannelsList() {
        IntObjectMap<DefaultEpollIoRegistration> ch = registrations;
        if (ch.isEmpty()) {
            return Collections.emptyList();
        }

        List<Channel> channels = new ArrayList<>(ch.size());
        for (DefaultEpollIoRegistration registration : ch.values()) {
            if (registration.handle instanceof AbstractEpollChannel.AbstractEpollUnsafe) {
                channels.add(((AbstractEpollChannel.AbstractEpollUnsafe) registration.handle).channel());
            }
        }
        return Collections.unmodifiableList(channels);
    }

    private long epollWait(IoHandlerContext context, long deadlineNanos) throws IOException {
        if (deadlineNanos == NONE) {
            return Native.epollWait(epollFd, events, timerFd,
                    Integer.MAX_VALUE, 0, EPOLL_WAIT_MILLIS_THRESHOLD); // disarm timer
        }
        long totalDelay = context.delayNanos(System.nanoTime());
        int delaySeconds = (int) min(totalDelay / 1000000000L, Integer.MAX_VALUE);
        int delayNanos = (int) min(totalDelay - delaySeconds * 1000000000L, MAX_SCHEDULED_TIMERFD_NS);
        return Native.epollWait(epollFd, events, timerFd, delaySeconds, delayNanos, EPOLL_WAIT_MILLIS_THRESHOLD);
    }

    private int epollWaitNoTimerChange() throws IOException {
        return Native.epollWait(epollFd, events, false);
    }

    private int epollWaitNow() throws IOException {
        return Native.epollWait(epollFd, events, true);
    }

    private int epollBusyWait() throws IOException {
        return Native.epollBusyWait(epollFd, events);
    }

    private int epollWaitTimeboxed() throws IOException {
        // 带 1 秒 safeguard 超时的 epoll_wait
        return Native.epollWait(epollFd, events, 1000);
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
                    strategy = epollBusyWait();
                    break;

                case SelectStrategy.SELECT:
                    if (pendingWakeup) {
                        // 即将被唤醒，无需重置 wakeup 或调整 timerfd
                        strategy = epollWaitTimeboxed();
                        if (strategy != 0) {
                            break;
                        }
                        // 超时认为遗漏 eventfd 写事件（异常 syscall）
                        logger.warn("Missed eventfd write (not seen after > 1 second)");
                        pendingWakeup = false;
                        if (!context.canBlock()) {
                            break;
                        }
                        // 继续 fall-through 到 SELECT 分支
                    }

                    long curDeadlineNanos = context.deadlineNanos();
                    if (curDeadlineNanos == -1L) {
                        curDeadlineNanos = NONE; // 无定时任务
                    }
                    nextWakeupNanos.set(curDeadlineNanos);
                    try {
                        if (context.canBlock()) {
                            if (curDeadlineNanos == prevDeadlineNanos) {
                                // deadline 未变，无需重设 timerfd
                                strategy = epollWaitNoTimerChange();
                            } else {
                                // deadline 变化，需重设或解除 timerfd
                                long result = epollWait(context, curDeadlineNanos);
                                // The result contains the actual return value and if a timer was used or not.
                                // We need to "unpack" using the helper methods exposed in Native.
                                strategy = Native.epollReady(result);
                                prevDeadlineNanos = Native.epollTimerWasUsed(result) ? curDeadlineNanos : NONE;
                            }
                        }
                    } finally {
                        // Try get() first to avoid much more expensive CAS in the case we
                        // were woken via the wakeup() method (submitted task)
                        if (nextWakeupNanos.get() == AWAKE || nextWakeupNanos.getAndSet(AWAKE) == AWAKE) {
                            pendingWakeup = true;
                        }
                    }
                    // fallthrough 到 default 处理就绪事件
                default:
            }
            if (strategy > 0) {
                int packed;
                if (context.shouldReportActiveIoTime()) {
                    long activeIoStartTimeNanos = System.nanoTime();
                    packed = processReady(events, strategy);
                    long activeIoEndTimeNanos = System.nanoTime();
                    context.reportActiveIoTime(activeIoEndTimeNanos - activeIoStartTimeNanos);
                } else {
                    packed = processReady(events, strategy);
                }
                handled = packed >>> 1;
                if ((packed & 1) != 0) {
                    prevDeadlineNanos = NONE;
                }
            } else if (context.shouldReportActiveIoTime()) {
                context.reportActiveIoTime(0);
            }

            if (allowGrowing && strategy == events.length()) {
                // 就绪数等于数组长度时扩容，避免下次溢出
                events.increase();
            }
        } catch (Error e) {
            throw e;
        } catch (Throwable t) {
            handleLoopException(t);
        }
        return handled;
    }

    /**
     * Visible only for testing!
     * <p>selector 循环异常处理：打日志并 sleep 1s 防止 CPU 空转（仅测试可见）。</p>
     */
    void handleLoopException(Throwable t) {
        logger.warn("Unexpected exception in the selector loop.", t);

        // 连续失败时 sleep，防止 selector 循环占满 CPU
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Ignore.
        }
    }

    // 打包返回值：每个真实 I/O 事件 +2，timer 触发 +1；解包见注释
    private int processReady(EpollEventArray events, int ready) {
        int result = 0;
        for (int i = 0; i < ready; i ++) {
            final int fd = events.fd(i);
            if (fd == eventFd.intValue()) {
                pendingWakeup = false;
            } else if (fd == timerFd.intValue()) {
                result |= 1;
            } else {
                result += 2;
                final long ev = events.events(i);

                DefaultEpollIoRegistration registration = registrations.get(fd);
                if (registration != null) {
                    registration.handle(ev);
                } else {
                    // 收到已注销 fd 的事件，从 epoll 集删除
                    try {
                        Native.epollCtlDel(epollFd.intValue(), fd);
                    } catch (IOException ignore) {
                        // 清理孤儿 fd；已删或已关闭时 epollCtlDel 失败可忽略
                    }
                }
            }
        }
        return result;
    }

    /**
     * This method is intended for use by process checkpoint/restore
     * integration, such as OpenJDK CRaC.
     * It's up to the caller to ensure that there is no concurrent use
     * of the FDs while these are closed, e.g. by blocking the executor.
     * <p>关闭 eventfd/timerfd/epoll fd；调用方须保证无并发 I/O（如阻塞 executor）。</p>
     */
    @UnstableApi
    public void closeFileDescriptors() {
        // 关闭 eventFd 前尽量消费未处理的 wakeup 写
        while (pendingWakeup) {
            try {
                int count = epollWaitTimeboxed();
                if (count == 0) {
                    // 超时则假定不会再有 wakeup 写
                    break;
                }
                for (int i = 0; i < count; i++) {
                    if (events.fd(i) == eventFd.intValue()) {
                        pendingWakeup = false;
                        break;
                    }
                }
            } catch (IOException ignore) {
                // ignore
            }
        }
        try {
            eventFd.close();
        } catch (IOException e) {
            logger.warn("Failed to close the event fd.", e);
        }
        try {
            timerFd.close();
        } catch (IOException e) {
            logger.warn("Failed to close the timer fd.", e);
        }

        try {
            epollFd.close();
        } catch (IOException e) {
            logger.warn("Failed to close the epoll fd.", e);
        }
    }
}
