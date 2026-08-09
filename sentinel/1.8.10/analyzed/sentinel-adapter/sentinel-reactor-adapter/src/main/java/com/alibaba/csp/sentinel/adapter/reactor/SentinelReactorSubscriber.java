/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.reactor;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.csp.sentinel.AsyncEntry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.function.Supplier;

import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

/**
 * Reactor 订阅者，在响应式流生命周期内管理 Sentinel 异步 Entry 的创建与退出。
 *
 * @author Eric Zhao
 * @since 1.5.0
 */
public class SentinelReactorSubscriber<T> extends InheritableBaseSubscriber<T> {

    private final EntryConfig entryConfig;

    private final CoreSubscriber<? super T> actual;
    private final boolean unary;

    private volatile AsyncEntry currentEntry;
    private final AtomicBoolean entryExited = new AtomicBoolean(false);

    public SentinelReactorSubscriber(EntryConfig entryConfig,
                                     CoreSubscriber<? super T> actual,
                                     boolean unary) {
        checkEntryConfig(entryConfig);
        this.entryConfig = entryConfig;
        this.actual = actual;
        this.unary = unary;
    }

    private void checkEntryConfig(EntryConfig config) {
        AssertUtil.notNull(config, "entryConfig cannot be null");
    }

    @Override
    public Context currentContext() {
        if (currentEntry == null || entryExited.get()) {
            return actual.currentContext();
        }
        com.alibaba.csp.sentinel.context.Context sentinelContext = currentEntry.getAsyncContext();
        if (sentinelContext == null) {
            return actual.currentContext();
        }
        return actual.currentContext()
            .put(SentinelReactorConstants.SENTINEL_CONTEXT_KEY, currentEntry.getAsyncContext());
    }

    private void doWithContextOrCurrent(Supplier<Optional<com.alibaba.csp.sentinel.context.Context>> contextSupplier,
                                        Runnable f) {
        Optional<com.alibaba.csp.sentinel.context.Context> contextOpt = contextSupplier.get();
        if (!contextOpt.isPresent()) {
            // 未提供上下文时，使用当前上下文。
            f.run();
        } else {
            // 在提供的上下文上执行。
            ContextUtil.runOnContext(contextOpt.get(), f);
        }
    }

    private void entryWhenSubscribed() {
        ContextConfig sentinelContextConfig = entryConfig.getContextConfig();
        if (sentinelContextConfig != null) {
            // 若当前已处于上下文中，context 配置将不会生效。
            ContextUtil.enter(sentinelContextConfig.getContextName(), sentinelContextConfig.getOrigin());
        }
        try {
            AsyncEntry entry = SphU.asyncEntry(entryConfig.getResourceName(), entryConfig.getResourceType(),
                entryConfig.getEntryType(), entryConfig.getAcquireCount(), entryConfig.getArgs());
            this.currentEntry = entry;
            actual.onSubscribe(this);
        } catch (BlockException ex) {
            // 显式标记 Entry 已完成（已退出）。
            entryExited.set(true);
            // 发送 cancel 信号并传播 {@code BlockException}。
            cancel();
            actual.onSubscribe(this);
            actual.onError(ex);
        } finally {
            if (sentinelContextConfig != null) {
                ContextUtil.exit();
            }
        }
    }

    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        doWithContextOrCurrent(() -> currentContext().getOrEmpty(SentinelReactorConstants.SENTINEL_CONTEXT_KEY),
            this::entryWhenSubscribed);
    }

    @Override
    protected void hookOnNext(T value) {
        if (isDisposed()) {
            tryCompleteEntry();
            return;
        }
        doWithContextOrCurrent(() -> Optional.ofNullable(currentEntry).map(AsyncEntry::getAsyncContext),
            () -> actual.onNext(value));

        if (unary) {
            // 对于部分一元操作符（Mono）场景，需在 onNext 钩子中提前完成 Entry。
            // 例如：onSubscribe() -> onNext() -> cancel() -> onComplete()
            // 此时 onComplete 钩子不会执行，因此需提前完成 Entry。
            tryCompleteEntry();
        }
    }

    @Override
    protected void hookOnComplete() {
        tryCompleteEntry();
        actual.onComplete();
    }

    @Override
    protected boolean shouldCallErrorDropHook() {
        // 流控触发或流终止时，应隐式丢弃后续异常，
        // 因此不调用 `onErrorDropped` 钩子。
        return !entryExited.get();
    }

    @Override
    protected void hookOnError(Throwable t) {
        if (currentEntry != null && currentEntry.getAsyncContext() != null) {
            // 非 BlockException 的正常请求异常会经过此处。
            Tracer.traceContext(t, 1, currentEntry.getAsyncContext());
        }
        tryCompleteEntry();
        actual.onError(t);
    }

    @Override
    protected void hookOnCancel() {
        tryCompleteEntry();
    }

    private boolean tryCompleteEntry() {
        if (currentEntry != null && entryExited.compareAndSet(false, true)) {
            currentEntry.exit(1, entryConfig.getArgs());
            return true;
        }
        return false;
    }
}
