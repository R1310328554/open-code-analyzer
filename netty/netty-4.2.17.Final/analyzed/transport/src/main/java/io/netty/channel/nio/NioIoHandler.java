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
package io.netty.channel.nio;

import io.netty.channel.ChannelException;
import io.netty.channel.DefaultSelectStrategyFactory;
import io.netty.channel.IoHandlerContext;
import io.netty.channel.IoHandle;
import io.netty.channel.IoHandler;
import io.netty.channel.IoHandlerFactory;
import io.netty.channel.IoOps;
import io.netty.channel.IoRegistration;
import io.netty.channel.SelectStrategy;
import io.netty.channel.SelectStrategyFactory;
import io.netty.util.IntSupplier;
import io.netty.util.concurrent.ThreadAwareExecutor;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.ReflectionUtil;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;

import java.nio.channels.spi.SelectorProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link IoHandler} implementation which register the {@link IoHandle}'s to a {@link Selector}.
 * <p>将 {@link IoHandle} 注册到 {@link Selector} 的 {@link IoHandler} 实现，负责 select 循环与就绪 key 分发。</p>
 */
public final class NioIoHandler implements IoHandler {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NioIoHandler.class);

    /** 累计取消 key 达到该间隔时触发 selectNow 清理 */
    private static final int CLEANUP_INTERVAL = 256; // XXX Hard-coded value, but won't need customization.

    /** 为 true 时不注入 SelectedSelectionKeySet 优化 */
    private static final boolean DISABLE_KEY_SET_OPTIMIZATION =
            SystemPropertyUtil.getBoolean("io.netty.noKeySetOptimization", false);

    private static final int MIN_PREMATURE_SELECTOR_RETURNS = 3;
    /** select 空转次数超过该阈值时自动 rebuild Selector */
    private static final int SELECTOR_AUTO_REBUILD_THRESHOLD;

    /** 供 {@link SelectStrategy} 调用的非阻塞 selectNow 供应商 */
    private final IntSupplier selectNowSupplier = new IntSupplier() {
        @Override
        public int get() throws Exception {
            return selectNow();
        }
    };

    // 规避 JDK NIO bug（select 空转 / selectedKeys 实现问题）
    //
    // See:
    // - https://bugs.openjdk.java.net/browse/JDK-6427854 for first few dev (unreleased) builds of JDK 7
    // - https://bugs.openjdk.java.net/browse/JDK-6527572 for JDK prior to 5.0u15-rev and 6u10
    // - https://github.com/netty/netty/issues/203
    static {
        int selectorAutoRebuildThreshold = SystemPropertyUtil.getInt("io.netty.selectorAutoRebuildThreshold", 512);
        if (selectorAutoRebuildThreshold < MIN_PREMATURE_SELECTOR_RETURNS) {
            selectorAutoRebuildThreshold = 0;
        }

        SELECTOR_AUTO_REBUILD_THRESHOLD = selectorAutoRebuildThreshold;

        if (logger.isDebugEnabled()) {
            logger.debug("-Dio.netty.noKeySetOptimization: {}", DISABLE_KEY_SET_OPTIMIZATION);
            logger.debug("-Dio.netty.selectorAutoRebuildThreshold: {}", SELECTOR_AUTO_REBUILD_THRESHOLD);
        }
    }

    /**
     * The NIO {@link Selector}.
     * <p>对外使用的 {@link Selector}（可能为 SelectedSelectionKeySetSelector 包装）。</p>
     */
    private Selector selector;
    /** 未包装的原始 Selector */
    private Selector unwrappedSelector;
    /** 注入的数组型 selectedKeys 集合（优化路径） */
    private SelectedSelectionKeySet selectedKeys;

    /** 打开 Selector 的 Provider */
    private final SelectorProvider provider;

    /**
     * Boolean that controls determines if a blocked Selector.select should
     * break out of its selection process. In our case we use a timeout for
     * the select method and the select method will block for that time unless
     * waken up.
     * <p>是否有线程请求唤醒阻塞中的 select。</p>
     */
    private final AtomicBoolean wakenUp = new AtomicBoolean();

    /** select 策略（是否阻塞、忙等） */
    private final SelectStrategy selectStrategy;
    /** 所属 EventLoop 执行器，用于 wakeup 线程判断 */
    private final ThreadAwareExecutor executor;
    /** 自上次 select 以来 cancel 的 key 计数 */
    private int cancelledKeys;
    /** 处理 selected key 过程中是否需要再次 selectNow 清理 cancel 缓存 */
    private boolean needsToSelectAgain;

    private NioIoHandler(ThreadAwareExecutor executor, SelectorProvider selectorProvider,
                         SelectStrategy strategy) {
        this.executor = ObjectUtil.checkNotNull(executor, "executionContext");
        this.provider = ObjectUtil.checkNotNull(selectorProvider, "selectorProvider");
        this.selectStrategy = ObjectUtil.checkNotNull(strategy, "selectStrategy");
        final SelectorTuple selectorTuple = openSelector();
        this.selector = selectorTuple.selector;
        this.unwrappedSelector = selectorTuple.unwrappedSelector;
    }

    private static final class SelectorTuple {
        /** JDK 原始 Selector */
        final Selector unwrappedSelector;
        /** 可能经 SelectedSelectionKeySetSelector 包装后的 Selector */
        final Selector selector;

        SelectorTuple(Selector unwrappedSelector) {
            this.unwrappedSelector = unwrappedSelector;
            this.selector = unwrappedSelector;
        }

        SelectorTuple(Selector unwrappedSelector, Selector selector) {
            this.unwrappedSelector = unwrappedSelector;
            this.selector = selector;
        }
    }

    private SelectorTuple openSelector() {
        final Selector unwrappedSelector;
        try {
            unwrappedSelector = provider.openSelector();
        } catch (IOException e) {
            throw new ChannelException("failed to open a new selector", e);
        }

        if (DISABLE_KEY_SET_OPTIMIZATION) {
            return new SelectorTuple(unwrappedSelector);
        }

        Object maybeSelectorImplClass = AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                try {
                    return Class.forName(
                            "sun.nio.ch.SelectorImpl",
                            false,
                            PlatformDependent.getSystemClassLoader());
                } catch (Throwable cause) {
                    return cause;
                }
            }
        });

        if (!(maybeSelectorImplClass instanceof Class) ||
                // ensure the current selector implementation is what we can instrument.
                !((Class<?>) maybeSelectorImplClass).isAssignableFrom(unwrappedSelector.getClass())) {
            if (maybeSelectorImplClass instanceof Throwable) {
                Throwable t = (Throwable) maybeSelectorImplClass;
                logger.trace("failed to instrument a special java.util.Set into: {}", unwrappedSelector, t);
            }
            return new SelectorTuple(unwrappedSelector);
        }

        final Class<?> selectorImplClass = (Class<?>) maybeSelectorImplClass;
        final SelectedSelectionKeySet selectedKeySet = new SelectedSelectionKeySet();

        Object maybeException = AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                try {
                    Field selectedKeysField = selectorImplClass.getDeclaredField("selectedKeys");
                    Field publicSelectedKeysField = selectorImplClass.getDeclaredField("publicSelectedKeys");

                    if (PlatformDependent.javaVersion() >= 9 && PlatformDependent.hasUnsafe()) {
                        // Java 9+ 尝试用 Unsafe 替换 selectedKeys Set
                        long selectedKeysFieldOffset = PlatformDependent.objectFieldOffset(selectedKeysField);
                        long publicSelectedKeysFieldOffset =
                                PlatformDependent.objectFieldOffset(publicSelectedKeysField);

                        if (selectedKeysFieldOffset != -1 && publicSelectedKeysFieldOffset != -1) {
                            PlatformDependent.putObject(
                                    unwrappedSelector, selectedKeysFieldOffset, selectedKeySet);
                            PlatformDependent.putObject(
                                    unwrappedSelector, publicSelectedKeysFieldOffset, selectedKeySet);
                            return null;
                        }
                        // 无法取得字段偏移则回退反射
                    }

                    Throwable cause = ReflectionUtil.trySetAccessible(selectedKeysField, true);
                    if (cause != null) {
                        return cause;
                    }
                    cause = ReflectionUtil.trySetAccessible(publicSelectedKeysField, true);
                    if (cause != null) {
                        return cause;
                    }

                    selectedKeysField.set(unwrappedSelector, selectedKeySet);
                    publicSelectedKeysField.set(unwrappedSelector, selectedKeySet);
                    return null;
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    return e;
                }
            }
        });

        if (maybeException instanceof Exception) {
            selectedKeys = null;
            Exception e = (Exception) maybeException;
            logger.trace("failed to instrument a special java.util.Set into: {}", unwrappedSelector, e);
            return new SelectorTuple(unwrappedSelector);
        }
        selectedKeys = selectedKeySet;
        logger.trace("instrumented a special java.util.Set into: {}", unwrappedSelector);
        return new SelectorTuple(unwrappedSelector,
                new SelectedSelectionKeySetSelector(unwrappedSelector, selectedKeySet));
    }

    /**
     * Returns the {@link SelectorProvider} used by this {@link NioEventLoop} to obtain the {@link Selector}.
     * <p>返回用于创建 {@link Selector} 的 {@link SelectorProvider}。</p>
     */
    public SelectorProvider selectorProvider() {
        return provider;
    }

    Selector selector() {
        return selector;
    }

    int numRegistered() {
        return selector().keys().size() - cancelledKeys;
    }

    Set<SelectionKey> registeredSet() {
        return selector().keys();
    }

    void rebuildSelector0() {
        final Selector oldSelector = selector;
        final SelectorTuple newSelectorTuple;

        if (oldSelector == null) {
            return;
        }

        try {
            newSelectorTuple = openSelector();
        } catch (Exception e) {
            logger.warn("Failed to create a new Selector.", e);
            return;
        }

        // 将所有 channel 迁移到新 Selector
        int nChannels = 0;
        for (SelectionKey key : oldSelector.keys()) {
            DefaultNioRegistration handle = (DefaultNioRegistration) key.attachment();
            try {
                if (!key.isValid() || key.channel().keyFor(newSelectorTuple.unwrappedSelector) != null) {
                    continue;
                }

                handle.register(newSelectorTuple.unwrappedSelector);
                nChannels++;
            } catch (Exception e) {
                logger.warn("Failed to re-register a NioHandle to the new Selector.", e);
                handle.cancel();
            }
        }

        selector = newSelectorTuple.selector;
        unwrappedSelector = newSelectorTuple.unwrappedSelector;

        try {
            // 关闭旧 Selector，注册已全部迁移
            oldSelector.close();
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to close the old Selector.", t);
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("Migrated " + nChannels + " channel(s) to the new Selector.");
        }
    }

    private static NioIoHandle nioHandle(IoHandle handle) {
        if (handle instanceof NioIoHandle) {
            return (NioIoHandle) handle;
        }
        throw new IllegalArgumentException("IoHandle of type " + StringUtil.simpleClassName(handle) + " not supported");
    }

    private static NioIoOps cast(IoOps ops) {
        if (ops instanceof NioIoOps) {
            return (NioIoOps) ops;
        }
        throw new IllegalArgumentException("IoOps of type " + StringUtil.simpleClassName(ops) + " not supported");
    }

    /** 单次 {@link IoHandle} 在 Selector 上的注册状态 */
    final class DefaultNioRegistration implements IoRegistration {
        /** 是否已 cancel */
        private final AtomicBoolean canceled = new AtomicBoolean();
        /** 关联的 NIO handle */
        private final NioIoHandle handle;
        /** 当前 SelectionKey */
        private volatile SelectionKey key;

        DefaultNioRegistration(ThreadAwareExecutor executor, NioIoHandle handle, NioIoOps initialOps, Selector selector)
                throws IOException {
            this.handle = handle;
            key = handle.selectableChannel().register(selector, initialOps.value, this);
        }

        NioIoHandle handle() {
            return handle;
        }

        void register(Selector selector) throws IOException {
            SelectionKey newKey = handle.selectableChannel().register(selector, key.interestOps(), this);
            key.cancel();
            key = newKey;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T attachment() {
            return (T) key;
        }

        @Override
        public boolean isValid() {
            return !canceled.get() && key.isValid();
        }

        @Override
        public long submit(IoOps ops) {
            if (!isValid()) {
                return -1;
            }
            int v = cast(ops).value;
            key.interestOps(v);
            return v;
        }

        @Override
        public boolean cancel() {
            if (!canceled.compareAndSet(false, true)) {
                return false;
            }
            key.cancel();
            cancelledKeys++;
            if (cancelledKeys >= CLEANUP_INTERVAL) {
                cancelledKeys = 0;
                needsToSelectAgain = true;
            }
            handle.unregistered();
            return true;
        }

        void close() {
            cancel();
            try {
                handle.close();
            } catch (Exception e) {
                logger.debug("Exception during closing " + handle, e);
            }
        }

        void handle(int ready) {
            if (!isValid()) {
                return;
            }
            handle.handle(this, NioIoOps.eventOf(ready));
        }
    }

    @Override
    public IoRegistration register(IoHandle handle)
            throws Exception {
        NioIoHandle nioHandle = nioHandle(handle);
        NioIoOps ops = NioIoOps.NONE;
        boolean selected = false;
        for (;;) {
            try {
                IoRegistration registration = new DefaultNioRegistration(executor, nioHandle, ops, unwrappedSelector());
                handle.registered();
                return registration;
            } catch (CancelledKeyException e) {
                if (!selected) {
                    // CancelledKey 可能仍被缓存，先 selectNow 再重试 register
                    selectNow();
                    selected = true;
                } else {
                    // We forced a select operation on the selector before but the SelectionKey is still cached
                    // for whatever reason. JDK bug ?
                    throw e;
                }
            }
        }
    }

    @Override
    public int run(IoHandlerContext context) {
        int handled = 0;
        try {
            try {
                switch (selectStrategy.calculateStrategy(selectNowSupplier, !context.canBlock())) {
                    case SelectStrategy.CONTINUE:
                        if (context.shouldReportActiveIoTime()) {
                            context.reportActiveIoTime(0); // Report zero as we did no I/O.
                        }
                        return 0;

                    case SelectStrategy.BUSY_WAIT:
                        // fall-through to SELECT since the busy-wait is not supported with NIO

                    case SelectStrategy.SELECT:
                        select(context, wakenUp.getAndSet(false));

                        // wakenUp 与 selector.wakeup() 的竞态说明见注释
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

                        if (wakenUp.get()) {
                            selector.wakeup();
                        }
                        // fall through
                    default:
                }
            } catch (IOException e) {
                // Selector 异常时 rebuild 并重试
                // https://github.com/netty/netty/issues/8566
                rebuildSelector0();
                handleLoopException(e);
                return 0;
            }

            cancelledKeys = 0;
            needsToSelectAgain = false;

            if (context.shouldReportActiveIoTime()) {
                // We start the timer after the blocking select() call has returned.
                long activeIoStartTimeNanos = System.nanoTime();
                handled = processSelectedKeys();
                long activeIoEndTimeNanos = System.nanoTime();
                context.reportActiveIoTime(activeIoEndTimeNanos - activeIoStartTimeNanos);
            } else {
                handled = processSelectedKeys();
            }
        } catch (Error e) {
            throw e;
        } catch (Throwable t) {
            handleLoopException(t);
        }
        return handled;
    }

    private static void handleLoopException(Throwable t) {
        logger.warn("Unexpected exception in the selector loop.", t);

        // 防止连续失败导致 CPU 空转
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // Ignore.
        }
    }

    private int processSelectedKeys() {
        if (selectedKeys != null) {
            return processSelectedKeysOptimized();
        } else {
            return processSelectedKeysPlain(selector.selectedKeys());
        }
    }

    @Override
    public void destroy() {
        try {
            selector.close();
        } catch (IOException e) {
            logger.warn("Failed to close a selector.", e);
        }
    }

    private int processSelectedKeysPlain(Set<SelectionKey> selectedKeys) {
        // 空 selectedKeys 时不创建 Iterator，减少 GC
        // See https://github.com/netty/netty/issues/597
        if (selectedKeys.isEmpty()) {
            return 0;
        }

        Iterator<SelectionKey> i = selectedKeys.iterator();
        int handled = 0;
        for (;;) {
            final SelectionKey k = i.next();
            i.remove();

            processSelectedKey(k);
            ++handled;

            if (!i.hasNext()) {
                break;
            }

            if (needsToSelectAgain) {
                selectAgain();
                selectedKeys = selector.selectedKeys();

                // Create the iterator again to avoid ConcurrentModificationException
                if (selectedKeys.isEmpty()) {
                    break;
                } else {
                    i = selectedKeys.iterator();
                }
            }
        }
        return handled;
    }

    private int processSelectedKeysOptimized() {
        int handled = 0;
        for (int i = 0; i < selectedKeys.size; ++i) {
            final SelectionKey k = selectedKeys.keys[i];
            // 置 null 便于 Channel 关闭后 GC
            // See https://github.com/netty/netty/issues/2363
            selectedKeys.keys[i] = null;

            processSelectedKey(k);
            ++handled;

            if (needsToSelectAgain) {
                // null out entries in the array to allow to have it GC'ed once the Channel close
                // See https://github.com/netty/netty/issues/2363
                selectedKeys.reset(i + 1);

                selectAgain();
                i = -1;
            }
        }
        return handled;
    }

    private void processSelectedKey(SelectionKey k) {
        final DefaultNioRegistration registration = (DefaultNioRegistration) k.attachment();
        if (!registration.isValid()) {
            try {
                registration.handle.close();
            } catch (Exception e) {
                logger.debug("Exception during closing " + registration.handle, e);
            }
            return;
        }
        registration.handle(k.readyOps());
    }

    @Override
    public void prepareToDestroy() {
        selectAgain();
        Set<SelectionKey> keys = selector.keys();
        Collection<DefaultNioRegistration> registrations = new ArrayList<>(keys.size());
        for (SelectionKey k: keys) {
            DefaultNioRegistration handle = (DefaultNioRegistration) k.attachment();
            registrations.add(handle);
        }

        for (DefaultNioRegistration reg: registrations) {
            reg.close();
        }
    }

    @Override
    public void wakeup() {
        if (!executor.isExecutorThread(Thread.currentThread()) && wakenUp.compareAndSet(false, true)) {
            selector.wakeup();
        }
    }

    @Override
    public boolean isCompatible(Class<? extends IoHandle> handleType) {
        return NioIoHandle.class.isAssignableFrom(handleType);
    }

    Selector unwrappedSelector() {
        return unwrappedSelector;
    }

    private void select(IoHandlerContext runner, boolean oldWakenUp) throws IOException {
        Selector selector = this.selector;
        try {
            int selectCnt = 0;
            long currentTimeNanos = System.nanoTime();
            final long delayNanos = runner.delayNanos(currentTimeNanos);
            // 无定时任务时用 Long.MAX_VALUE 表示无限阻塞
            long selectDeadLineNanos = Long.MAX_VALUE;
            if (delayNanos != Long.MAX_VALUE) {
                selectDeadLineNanos = currentTimeNanos + runner.delayNanos(currentTimeNanos);
            }
            for (;;) {
                final long timeoutMillis;
                if (delayNanos != Long.MAX_VALUE) {
                    long millisBeforeDeadline = millisBeforeDeadline(selectDeadLineNanos, currentTimeNanos);
                    if (millisBeforeDeadline <= 0) {
                        if (selectCnt == 0) {
                            selector.selectNow();
                            selectCnt = 1;
                        }
                        break;
                    }
                    timeoutMillis = millisBeforeDeadline;
                } else {
                    // NIO 下 timeoutMillis=0 表示无超时阻塞
                    timeoutMillis = 0;
                }
                // submit 任务时若 wakenUp 已为 true，须在 select 前再检查任务队列
                if (!runner.canBlock() && wakenUp.compareAndSet(false, true)) {
                    selector.selectNow();
                    selectCnt = 1;
                    break;
                }

                int selectedKeys = selector.select(timeoutMillis);
                selectCnt ++;

                if (selectedKeys != 0 || oldWakenUp || wakenUp.get() || !runner.canBlock()) {
                    // 已选中 key / 被唤醒 / 任务队列非空 / 定时任务到期
                    break;
                }
                if (Thread.interrupted()) {
                    // 线程被 interrupt 时重置并退出，避免 busy loop
                    if (logger.isDebugEnabled()) {
                        logger.debug("Selector.select() returned prematurely because " +
                                "Thread.currentThread().interrupt() was called. Use " +
                                "NioHandler.shutdownGracefully() to shutdown the NioHandler.");
                    }
                    selectCnt = 1;
                    break;
                }

                long time = System.nanoTime();
                if (time - TimeUnit.MILLISECONDS.toNanos(timeoutMillis) >= currentTimeNanos) {
                    // 超时到期仍未选中
                    selectCnt = 1;
                } else if (SELECTOR_AUTO_REBUILD_THRESHOLD > 0 &&
                        selectCnt >= SELECTOR_AUTO_REBUILD_THRESHOLD) {
                    // select 连续空转过多，rebuild Selector
                    selector = selectRebuildSelector(selectCnt);
                    selectCnt = 1;
                    break;
                }

                currentTimeNanos = time;
            }

            if (selectCnt > MIN_PREMATURE_SELECTOR_RETURNS) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Selector.select() returned prematurely {} times in a row for Selector {}.",
                            selectCnt - 1, selector);
                }
            }
        } catch (CancelledKeyException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(CancelledKeyException.class.getSimpleName() + " raised by a Selector {} - JDK bug?",
                        selector, e);
            }
            // Harmless exception - log anyway
        }
    }

    private static long millisBeforeDeadline(long selectDeadLineNanos, long currentTimeNanos) {
        assert selectDeadLineNanos != Long.MAX_VALUE;
        long nanosBeforeDeadline = selectDeadLineNanos - currentTimeNanos;
        // Prevent overflow when adding the rounding bias:
        // if we don't do this, it would appear as the deadline is already reached!
        if (nanosBeforeDeadline >= Long.MAX_VALUE - 500_000L) {
            return Long.MAX_VALUE / 1_000_000L;
        }
        // Add 500_000 to round up to the nearest millisecond.
        return (nanosBeforeDeadline + 500_000L) / 1_000_000L;
    }

    int selectNow() throws IOException {
        try {
            return selector.selectNow();
        } finally {
            // restore wakeup state if needed
            if (wakenUp.get()) {
                selector.wakeup();
            }
        }
    }

    private Selector selectRebuildSelector(int selectCnt) throws IOException {
        // The selector returned prematurely many times in a row.
        // Rebuild the selector to work around the problem.
        logger.warn(
                "Selector.select() returned prematurely {} times in a row; rebuilding Selector {}.",
                selectCnt, selector);

        rebuildSelector0();
        Selector selector = this.selector;

        // Select again to populate selectedKeys.
        selector.selectNow();
        return selector;
    }

    private void selectAgain() {
        needsToSelectAgain = false;
        try {
            selector.selectNow();
        } catch (Throwable t) {
            logger.warn("Failed to update SelectionKeys.", t);
        }
    }

    /**
     * Returns a new {@link IoHandlerFactory} that creates {@link NioIoHandler} instances
     * <p>使用默认 {@link SelectorProvider} 与 {@link SelectStrategy} 创建工厂。</p>
     *
     * @return factory                  the {@link IoHandlerFactory}.
     */
    public static IoHandlerFactory newFactory() {
        return newFactory(SelectorProvider.provider(), DefaultSelectStrategyFactory.INSTANCE);
    }

    /**
     * Returns a new {@link IoHandlerFactory} that creates {@link NioIoHandler} instances.
     * <p>指定 {@link SelectorProvider} 创建 {@link NioIoHandler} 工厂。</p>
     *
     * @param selectorProvider          the {@link SelectorProvider} to use.
     * @return factory                  the {@link IoHandlerFactory}.
     */
    public static IoHandlerFactory newFactory(SelectorProvider selectorProvider) {
        return newFactory(selectorProvider, DefaultSelectStrategyFactory.INSTANCE);
    }

    /**
     * Returns a new {@link IoHandlerFactory} that creates {@link NioIoHandler} instances.
     * <p>同时指定 {@link SelectorProvider} 与 {@link SelectStrategyFactory}。</p>
     *
     * @param selectorProvider          the {@link SelectorProvider} to use.
     * @param selectStrategyFactory     the {@link SelectStrategyFactory} to use.
     * @return factory                  the {@link IoHandlerFactory}.
     */
    public static IoHandlerFactory newFactory(final SelectorProvider selectorProvider,
                                              final SelectStrategyFactory selectStrategyFactory) {
        ObjectUtil.checkNotNull(selectorProvider, "selectorProvider");
        ObjectUtil.checkNotNull(selectStrategyFactory, "selectStrategyFactory");
        return new IoHandlerFactory() {
            @Override
            public IoHandler newHandler(ThreadAwareExecutor executor) {
                return new NioIoHandler(executor, selectorProvider, selectStrategyFactory.newSelectStrategy());
            }

            @Override
            public boolean isChangingThreadSupported() {
                return true;
            }
        };
    }
}
