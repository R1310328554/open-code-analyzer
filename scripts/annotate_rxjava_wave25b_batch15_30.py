#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-25b internal subscribers/subscriptions [15:30]."""
from __future__ import annotations

import json
import os
import re
import shutil
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "rxjava/4.0.0-alpha-21"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
SCRIPTS = ROOT / "scripts"
WAVE25B_FILE = Path("/tmp/rxjava_w25b.txt")
SCRIPT_NAME = "annotate_rxjava_wave25b_batch15_30.py"
MARK_NOTE = "wave25b [15:30]"
BATCH_FILES = [
    ln.strip()
    for ln in WAVE25B_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

GUARD_FILES = [
    VER
    / "analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "LambdaSubscriber.java": [
        (
            "public final class LambdaSubscriber<T> extends AtomicReference<Subscription>\n        implements FlowableSubscriber<T>, Subscription, Disposable, LambdaConsumerIntrospection {",
            "/**\n"
            " * 基于回调的 Flowable 订阅者：同时实现 {@link Subscription} 与 {@link Disposable}，\n"
            " * 将 onNext/onError/onComplete/onSubscribe 委托给对应 {@link Consumer}/{@link Action}。\n"
            " */\n"
            "public final class LambdaSubscriber<T> extends AtomicReference<Subscription>\n        implements FlowableSubscriber<T>, Subscription, Disposable, LambdaConsumerIntrospection {",
        ),
        (
            "    public LambdaSubscriber(Consumer<? super T> onNext, Consumer<? super Throwable> onError,\n            Action onComplete,\n            Consumer<? super Subscription> onSubscribe) {",
            "    /** @param onNext/onError/onComplete/onSubscribe 各阶段回调 */\n"
            "    public LambdaSubscriber(Consumer<? super T> onNext, Consumer<? super Throwable> onError,\n            Action onComplete,\n            Consumer<? super Subscription> onSubscribe) {",
        ),
        (
            "    @Override\n    public void onSubscribe(Subscription s) {",
            "    /** setOnce 成功后调用 onSubscribe.accept(this)。 */\n"
            "    @Override\n    public void onSubscribe(Subscription s) {",
        ),
        (
            "    @Override\n    public void onNext(T t) {",
            "    /** 未 dispose 时 accept(t)；异常则 cancel 并 onError。 */\n"
            "    @Override\n    public void onNext(T t) {",
        ),
        (
            "    @Override\n    public void onError(Throwable t) {",
            "    /** 置 CANCELLED 后调用 onError；已取消则上报 RxJavaPlugins。 */\n"
            "    @Override\n    public void onError(Throwable t) {",
        ),
        (
            "    @Override\n    public void onComplete() {",
            "    /** 置 CANCELLED 后运行 onComplete.run()。 */\n"
            "    @Override\n    public void onComplete() {",
        ),
        (
            "    @Override\n    public void dispose() {",
            "    /** 等价于 cancel()。 */\n"
            "    @Override\n    public void dispose() {",
        ),
        (
            "    @Override\n    public boolean isDisposed() {",
            "    /** 当前 Subscription 是否为 CANCELLED。 */\n"
            "    @Override\n    public boolean isDisposed() {",
        ),
        (
            "    @Override\n    public void request(long n) {",
            "    /** 转发 request 至上游 Subscription。 */\n"
            "    @Override\n    public void request(long n) {",
        ),
        (
            "    @Override\n    public void cancel() {",
            "    /** 调用 SubscriptionHelper.cancel(this)。 */\n"
            "    @Override\n    public void cancel() {",
        ),
        (
            "    @Override\n    public boolean hasCustomOnError() {",
            "    /** 是否提供了非默认 ON_ERROR_MISSING 的 onError 回调。 */\n"
            "    @Override\n    public boolean hasCustomOnError() {",
        ),
    ],
    "QueueDrainSubscriber.java": [
        (
            "/**\n * Abstract base class for subscribers that hold another subscriber, a queue\n * and requires queue-drain behavior.\n *\n * @param <T> the source type to which this subscriber will be subscribed\n * @param <U> the value type in the queue\n * @param <V> the value type the child subscriber accepts\n */",
            "/**\n"
            " * 队列排空订阅者抽象基类：持有下游 Subscriber、内部队列，\n"
            " * 实现 {@link QueueDrain} 的 wip/request 与 fastPath 发射逻辑。\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " * @param <U> 队列元素类型\n"
            " * @param <V> 下游接收类型\n"
            " */",
        ),
        (
            "    public QueueDrainSubscriber(Subscriber<? super V> actual, SimplePlainQueue<U> queue) {",
            "    /** @param actual 下游 Subscriber；@param queue 缓冲队列 */\n"
            "    public QueueDrainSubscriber(Subscriber<? super V> actual, SimplePlainQueue<U> queue) {",
        ),
        (
            "    public final boolean fastEnter() {",
            "    /** CAS 将 wip 从 0 置 1，表示进入 drain 临界区。 */\n"
            "    public final boolean fastEnter() {",
        ),
        (
            "    protected final void fastPathEmitMax(U value, boolean delayError, Disposable dispose) {",
            "    /** 快速路径发射：有 request 则 accept，否则 dispose 并 MissingBackpressureException。 */\n"
            "    protected final void fastPathEmitMax(U value, boolean delayError, Disposable dispose) {",
        ),
        (
            "    protected final void fastPathOrderedEmitMax(U value, boolean delayError, Disposable dispose) {",
            "    /** 有序快速路径：队列为空时直接 accept，否则入队后 drainMaxLoop。 */\n"
            "    protected final void fastPathOrderedEmitMax(U value, boolean delayError, Disposable dispose) {",
        ),
        (
            "    public final void requested(long n) {",
            "    /** validate 后 BackpressureHelper.add 累加 request。 */\n"
            "    public final void requested(long n) {",
        ),
        (
            "/** Pads the header away from other fields. */",
            "/** 缓存行填充：隔离 header 与其他字段，降低伪共享。 */",
        ),
        (
            "/** The WIP counter. */",
            "/** 持有 wip 计数器（drain 重入保护）。 */",
        ),
        (
            "/** Pads away the wip from the other fields. */",
            "/** 填充 wip 与 requested 之间的字段。 */",
        ),
        (
            "/** Contains the requested field. */",
            "/** 持有 requested 累计请求量。 */",
        ),
        (
            "/** Pads away the requested from the other fields. */",
            "/** 填充 requested 与业务字段之间的区域。 */",
        ),
    ],
    "SinglePostCompleteSubscriber.java": [
        (
            "/**\n * Relays signals from upstream according to downstream requests and allows\n * signaling a final value followed by onComplete in a backpressure-aware manner.\n *\n * @param <T> the input value type\n * @param <R> the output value type\n */",
            "/**\n"
            " * 单值后置完成订阅者：按下游 request 转发上游信号，\n"
            " * 支持在背压感知下发射最终值后 onComplete（如 reduce 末元素）。\n"
            " *\n"
            " * @param <T> 上游输入类型\n"
            " * @param <R> 下游输出类型\n"
            " */",
        ),
        (
            "    /** The downstream consumer. */",
            "    /** 下游 Subscriber。 */",
        ),
        (
            "    /** The upstream subscription. */",
            "    /** 上游 Subscription。 */",
        ),
        (
            "    /** The last value stored in case there is no request for it. */",
            "    /** 尚无 request 时暂存的最终值。 */",
        ),
        (
            "    /** Number of values emitted so far. */",
            "    /** 已向上游产生的元素计数（用于 produced 扣减）。 */",
        ),
        (
            "    /** Masks out the 2^63 bit indicating a completed state. */",
            "    /** 掩码：最高位表示 complete 状态。 */",
        ),
        (
            "    /** Masks out the lower 63 bit holding the current request amount. */",
            "    /** 掩码：低 63 位为当前 request 累计量。 */",
        ),
        (
            "    /**\n     * Signals the given value and an onComplete if the downstream is ready to receive the final value.\n     * @param n the value to emit\n     */",
            "    /**\n"
            "     * 信号最终值：有 request 则立即 onNext+onComplete，否则暂存 value 并置 COMPLETE_MASK。\n"
            "     * @param n 待发射的最终值\n"
            "     */",
        ),
        (
            "    /**\n     * Called in case of multiple calls to complete.\n     * @param n the value dropped\n     */",
            "    /**\n"
            "     * 多次 complete 时丢弃的值回调（默认无操作）。\n"
            "     * @param n 被丢弃的值\n"
            "     */",
        ),
        (
            "    @Override\n    public final void request(long n) {",
            "    /** 合并 request 至 AtomicLong；若已 complete 则补发 value+onComplete。 */\n"
            "    @Override\n    public final void request(long n) {",
        ),
        (
            "    @Override\n    public void cancel() {",
            "    /** 取消上游 Subscription。 */\n"
            "    @Override\n    public void cancel() {",
        ),
    ],
    "StrictSubscriber.java": [
        (
            "/**\n * Ensures that the event flow between the upstream and downstream follow\n * the Reactive-Streams 1.0 specification by honoring the 3 additional rules\n * (which are omitted in standard operators due to performance reasons).\n * <ul>\n * <li>§1.3: onNext should not be called concurrently until onSubscribe returns</li>\n * <li>§2.3: onError or onComplete must not call cancel</li>\n * <li>§3.9: negative requests should emit an onError(IllegalArgumentException)</li>\n * </ul>\n * In addition, if rule §2.12 (onSubscribe must be called at most once) is violated,\n * the sequence is cancelled an onError(IllegalStateException) is emitted.\n * @param <T> the value type\n * @since 2.0.7\n */",
            "/**\n"
            " * 严格 Reactive-Streams 合规包装：强制执行标准算子为性能省略的附加规则。\n"
            " * <ul>\n"
            " * <li>§1.3：onSubscribe 返回前不得并发 onNext</li>\n"
            " * <li>§2.3：onError/onComplete 内不得 cancel</li>\n"
            " * <li>§3.9：负 request 触发 onError(IllegalArgumentException)</li>\n"
            " * </ul>\n"
            " * 违反 §2.12（onSubscribe 至多一次）时 cancel 并 onError(IllegalStateException)。\n"
            " * @param <T> 元素类型\n"
            " * @since 2.0.7\n"
            " */",
        ),
        (
            "    @Override\n    public void request(long n) {",
            "    /** n<=0 时 cancel 并 onError；否则 deferredRequest 转发。 */\n"
            "    @Override\n    public void request(long n) {",
        ),
        (
            "    @Override\n    public void cancel() {",
            "    /** 未完成时 cancel 上游。 */\n"
            "    @Override\n    public void cancel() {",
        ),
        (
            "    @Override\n    public void onSubscribe(Subscription s) {",
            "    /** once CAS 成功则 deferredSetOnce；重复 onSubscribe 违反 §2.12。 */\n"
            "    @Override\n    public void onSubscribe(Subscription s) {",
        ),
        (
            "    @Override\n    public void onNext(T t) {",
            "    /** 通过 HalfSerializer 串行化向下游 onNext。 */\n"
            "    @Override\n    public void onNext(T t) {",
        ),
        (
            "    @Override\n    public void onError(Throwable t) {",
            "    /** 置 done 后 HalfSerializer.onError（不调用 cancel）。 */\n"
            "    @Override\n    public void onError(Throwable t) {",
        ),
        (
            "    @Override\n    public void onComplete() {",
            "    /** 置 done 后 HalfSerializer.onComplete。 */\n"
            "    @Override\n    public void onComplete() {",
        ),
    ],
    "SubscriberResourceWrapper.java": [
        (
            "public final class SubscriberResourceWrapper<T> extends AtomicReference<Disposable> implements FlowableSubscriber<T>, Disposable, Subscription {",
            "/**\n"
            " * 订阅者资源包装：转发 Flowable 事件至 downstream，\n"
            " * 同时持有 upstream Subscription 与可 dispose 的 Disposable 资源。\n"
            " */\n"
            "public final class SubscriberResourceWrapper<T> extends AtomicReference<Disposable> implements FlowableSubscriber<T>, Disposable, Subscription {",
        ),
        (
            "    public SubscriberResourceWrapper(Subscriber<? super T> downstream) {",
            "    /** @param downstream 目标 Subscriber */\n"
            "    public SubscriberResourceWrapper(Subscriber<? super T> downstream) {",
        ),
        (
            "    @Override\n    public void onSubscribe(Subscription s) {",
            "    /** setOnce 成功后向下游传递 this 作为 Subscription。 */\n"
            "    @Override\n    public void onSubscribe(Subscription s) {",
        ),
        (
            "    @Override\n    public void onError(Throwable t) {",
            "    /** dispose 资源后转发 onError。 */\n"
            "    @Override\n    public void onError(Throwable t) {",
        ),
        (
            "    @Override\n    public void onComplete() {",
            "    /** dispose 资源后转发 onComplete。 */\n"
            "    @Override\n    public void onComplete() {",
        ),
        (
            "    @Override\n    public void request(long n) {",
            "    /** validate 后转发 request 至 upstream。 */\n"
            "    @Override\n    public void request(long n) {",
        ),
        (
            "    @Override\n    public void dispose() {",
            "    /** cancel upstream 并 DisposableHelper.dispose 资源。 */\n"
            "    @Override\n    public void dispose() {",
        ),
        (
            "    @Override\n    public void cancel() {",
            "    /** 等价于 dispose()。 */\n"
            "    @Override\n    public void cancel() {",
        ),
        (
            "    public void setResource(Disposable resource) {",
            "    /** 设置 Disposable 资源（DisposableHelper.set）。 */\n"
            "    public void setResource(Disposable resource) {",
        ),
    ],
    "ArrayCompositeSubscription.java": [
        (
            "/**\n * A composite disposable with a fixed number of slots.\n *\n * <p>Note that since the implementation leaks the methods of AtomicReferenceArray, one must be\n * careful to only call setResource, replaceResource and dispose on it. All other methods may lead to undefined behavior\n * and should be used by internal means only.\n */",
            "/**\n"
            " * 固定槽位的 Subscription 复合容器（继承 AtomicReferenceArray）。\n"
            " * <p>\n"
            " * 对外仅应调用 setResource/replaceResource/dispose；\n"
            " * 直接调用数组其它方法可能导致未定义行为。\n"
            " */",
        ),
        (
            "    public ArrayCompositeSubscription(int capacity) {",
            "    /** @param capacity 槽位数量 */\n"
            "    public ArrayCompositeSubscription(int capacity) {",
        ),
        (
            "    /**\n     * Sets the resource at the specified index and disposes the old resource.\n     * @param index the index of the resource to set\n     * @param resource the new resource\n     * @return true if the resource has been set, false if the composite has been disposed\n     */",
            "    /**\n"
            "     * 在 index 设置 Subscription 并 cancel 旧值。\n"
            "     * @param index 槽位索引\n"
            "     * @param resource 新 Subscription\n"
            "     * @return 成功 true；已 dispose（CANCELLED）则 false\n"
            "     */",
        ),
        (
            "    /**\n     * Replaces the resource at the specified index and returns the old resource.\n     * @param index the index of the resource to replace\n     * @param resource the new resource\n     * @return the old resource, can be null\n     */",
            "    /**\n"
            "     * 替换 index 处 Subscription 并返回旧值（不 cancel 旧值）。\n"
            "     * @param index 槽位索引\n"
            "     * @param resource 新 Subscription\n"
            "     * @return 旧 Subscription，可为 null\n"
            "     */",
        ),
        (
            "    @Override\n    public void dispose() {",
            "    /** 将所有槽位置 CANCELLED 并 cancel 各 Subscription。 */\n"
            "    @Override\n    public void dispose() {",
        ),
        (
            "    @Override\n    public boolean isDisposed() {",
            "    /** 槽位 0 是否为 CANCELLED。 */\n"
            "    @Override\n    public boolean isDisposed() {",
        ),
    ],
    "AsyncSubscription.java": [
        (
            "/**\n * A subscription implementation that arbitrates exactly one other Subscription and can\n * hold a single disposable resource.\n *\n * <p>All methods are thread-safe.\n */",
            "/**\n"
            " * 异步 Subscription：仲裁单个 upstream Subscription，\n"
            " * 并可持有单个 Disposable 资源；所有方法线程安全。\n"
            " */",
        ),
        (
            "    public AsyncSubscription(Disposable resource) {",
            "    /** 可选预置 Disposable 资源。 */\n"
            "    public AsyncSubscription(Disposable resource) {",
        ),
        (
            "    @Override\n    public void request(long n) {",
            "    /** deferredRequest 转发或暂存 request。 */\n"
            "    @Override\n    public void request(long n) {",
        ),
        (
            "    @Override\n    public void dispose() {",
            "    /** cancel actual 并 dispose resource。 */\n"
            "    @Override\n    public void dispose() {",
        ),
        (
            "    /**\n     * Sets a new resource and disposes the currently held resource.\n     * @param r the new resource to set\n     * @return false if this AsyncSubscription has been cancelled/disposed\n     * @see #replaceResource(Disposable)\n     */",
            "    /**\n"
            "     * 设置新 Disposable 并 dispose 旧资源。\n"
            "     * @param r 新资源\n"
            "     * @return 已 cancel/dispose 时 false\n"
            "     * @see #replaceResource(Disposable)\n"
            "     */",
        ),
        (
            "    /**\n     * Replaces the currently held resource with the given new one without disposing the old.\n     * @param r the new resource to set\n     * @return false if this AsyncSubscription has been cancelled/disposed\n     */",
            "    /**\n"
            "     * 替换 Disposable 资源，不 dispose 旧值。\n"
            "     * @param r 新资源\n"
            "     * @return 已 cancel/dispose 时 false\n"
            "     */",
        ),
        (
            "    /**\n     * Sets the given subscription if there isn't any subscription held.\n     * @param s the first and only subscription to set\n     */",
            "    /**\n"
            "     * deferredSetOnce 设置唯一 upstream Subscription。\n"
            "     * @param s 首次设置的 Subscription\n"
            "     */",
        ),
    ],
    "BasicIntQueueSubscription.java": [
        (
            "/**\n * Base class extending AtomicInteger (wip or request accounting) and QueueSubscription (fusion).\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 基于 AtomicInteger（wip 或状态位）的 {@link QueueSubscription} 基类，\n"
            " * 用于 fusion 场景；offer 方法禁止调用。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    @Override\n    public final boolean offer(T e) {",
            "    /** 不支持入队，调用即抛 UnsupportedOperationException。 */\n"
            "    @Override\n    public final boolean offer(T e) {",
        ),
    ],
    "BasicQueueSubscription.java": [
        (
            "/**\n * Base class extending AtomicLong (wip or request accounting) and QueueSubscription (fusion).\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 基于 AtomicLong（request 累计）的 {@link QueueSubscription} 基类；\n"
            " * offer 方法禁止调用。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    @Override\n    public final boolean offer(T e) {",
            "    /** 不支持入队，调用即抛 UnsupportedOperationException。 */\n"
            "    @Override\n    public final boolean offer(T e) {",
        ),
    ],
    "BooleanSubscription.java": [
        (
            "/**\n * Subscription implementation that ignores request but remembers the cancellation\n * which can be checked via isCancelled.\n */",
            "/**\n"
            " * 布尔取消 Subscription：request 仅做 validate，\n"
            " * cancel 置 true，可通过 isCancelled 查询。\n"
            " */",
        ),
        (
            "    @Override\n    public void cancel() {",
            "    /** lazySet(true) 标记已取消。 */\n"
            "    @Override\n    public void cancel() {",
        ),
        (
            "    /**\n     * Returns true if this BooleanSubscription has been cancelled.\n     * @return true if this BooleanSubscription has been cancelled\n     */",
            "    /**\n"
            "     * 是否已 cancel。\n"
            "     * @return 已取消则 true\n"
            "     */",
        ),
    ],
    "DeferredScalarSubscription.java": [
        (
            "/**\n * A subscription that signals a single value eventually.\n * <p>\n * Note that the class leaks all methods of {@link java.util.concurrent.atomic.AtomicLong}.\n * Use {@link #complete(Object)} to signal the single value.\n * <p>\n * This atomic integer stores a bit field:<br>\n * bit 0: indicates that there is a value available<br>\n * bit 1: indicates that there was a request made<br>\n * bit 2: indicates there was a cancellation, exclusively set<br>\n * bit 3: indicates in fusion mode but no value yet, exclusively set<br>\n * bit 4: indicates in fusion mode and value is available, exclusively set<br>\n * bit 5: indicates in fusion mode and value has been consumed, exclusively set<br>\n * Where exclusively set means any other bits are 0 when that bit is set.\n * @param <T> the value type\n */",
            "/**\n"
            " * 延迟标量 Subscription：最终在 request 后发射单个值。\n"
            " * <p>\n"
            " * 通过 {@link #complete(Object)} 设置值；AtomicInteger 位域表示\n"
            " * 有无 value/request/cancel 及 fusion 状态（FUSED_*）。\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    /** The Subscriber to emit the value to. */",
            "    /** 接收单值的下游 Subscriber。 */",
        ),
        (
            "    /** The value is stored here if there is no request yet or in fusion mode. */",
            "    /** 尚无 request 或 fusion 模式下暂存的值。 */",
        ),
        (
            "    /** Indicates this Subscription has no value and not requested yet. */",
            "    /** 状态：无 value、无 request。 */",
        ),
        (
            "    /** Indicates this Subscription has a value but not requested yet. */",
            "    /** 状态：有 value、无 request。 */",
        ),
        (
            "    /** Indicates this Subscription has been requested but there is no value yet. */",
            "    /** 状态：有 request、无 value。 */",
        ),
        (
            "    /** Indicates this Subscription has both request and value. */",
            "    /** 状态：有 request 且有 value，可立即发射。 */",
        ),
        (
            "    /** Indicates the Subscription has been cancelled. */",
            "    /** 状态：已 cancel。 */",
        ),
        (
            "    /** Indicates this Subscription is in fusion mode and is currently empty. */",
            "    /** fusion：空队列，等待 complete。 */",
        ),
        (
            "    /** Indicates this Subscription is in fusion mode and has a value. */",
            "    /** fusion：已有值，poll 可取。 */",
        ),
        (
            "    /** Indicates this Subscription is in fusion mode and its value has been consumed. */",
            "    /** fusion：值已被 poll 消费。 */",
        ),
        (
            "    /**\n     * Creates a DeferredScalarSubscription by wrapping the given Subscriber.\n     * @param downstream the Subscriber to wrap, not null (not verified)\n     */",
            "    /**\n"
            "     * 包装下游 Subscriber。\n"
            "     * @param downstream 目标 Subscriber（未校验非 null）\n"
            "     */",
        ),
        (
            "    @Override\n    public final void request(long n) {",
            "    /** validate 后按状态机发射 value+onComplete 或置 HAS_REQUEST。 */\n"
            "    @Override\n    public final void request(long n) {",
        ),
        (
            "    /**\n     * Completes this subscription by indicating the given value should\n     * be emitted when the first request arrives.\n     * <p>Make sure this is called exactly once.\n     * @param v the value to signal, not null (not validated)\n     */",
            "    /**\n"
            "     * 设置单值；有 request 则立即 onNext+onComplete，否则暂存。\n"
            "     * <p>应仅调用一次。\n"
            "     * @param v 待发射的值（未校验非 null）\n"
            "     */",
        ),
        (
            "    @Override\n    public final int requestFusion(int mode) {",
            "    /** 支持 ASYNC fusion 时置 FUSED_EMPTY。 */\n"
            "    @Override\n    public final int requestFusion(int mode) {",
        ),
        (
            "    @Nullable\n    @Override\n    public final T poll() {",
            "    /** FUSED_READY 时取出 value 并置 FUSED_CONSUMED。 */\n"
            "    @Nullable\n    @Override\n    public final T poll() {",
        ),
        (
            "    @Override\n    public void cancel() {",
            "    /** 置 CANCELLED 并清空 value。 */\n"
            "    @Override\n    public void cancel() {",
        ),
        (
            "    /**\n     * Atomically sets a cancelled state and returns true if\n     * the current thread did it successfully.\n     * @return true if the current thread cancelled\n     */",
            "    /**\n"
            "     * 原子置 cancel 并返回是否由当前线程首次取消。\n"
            "     * @return 当前线程成功取消则 true\n"
            "     */",
        ),
    ],
    "EmptySubscription.java": [
        (
            "/**\n * An empty subscription that does nothing other than validates the request amount.\n */",
            "/**\n"
            " * 空 Subscription 单例：request 仅 validate，cancel 无操作；\n"
            " * 用于 error/complete 辅助方法向下游传递占位 Subscription。\n"
            " */",
        ),
        (
            "    /** A singleton, stateless instance. */",
            "    /** 无状态单例 INSTANCE。 */",
        ),
        (
            "    /**\n     * Sets the empty subscription instance on the subscriber and then\n     * calls onError with the supplied error.\n     *\n     * <p>Make sure this is only called if the subscriber hasn't received a\n     * subscription already (there is no way of telling this).\n     *\n     * @param e the error to deliver to the subscriber\n     * @param s the target subscriber\n     */",
            "    /**\n"
            "     * onSubscribe(INSTANCE) 后 onError(e)。\n"
            "     * <p>仅应在下游尚未收到 Subscription 时调用。\n"
            "     * @param e 错误\n"
            "     * @param s 目标 Subscriber\n"
            "     */",
        ),
        (
            "    /**\n     * Sets the empty subscription instance on the subscriber and then\n     * calls onComplete.\n     *\n     * <p>Make sure this is only called if the subscriber hasn't received a\n     * subscription already (there is no way of telling this).\n     *\n     * @param s the target subscriber\n     */",
            "    /**\n"
            "     * onSubscribe(INSTANCE) 后 onComplete。\n"
            "     * <p>仅应在下游尚未收到 Subscription 时调用。\n"
            "     * @param s 目标 Subscriber\n"
            "     */",
        ),
        (
            "    @Override\n    public int requestFusion(int mode) {",
            "    /** 接受 ASYNC fusion（后续仍会 onComplete/onError）。 */\n"
            "    @Override\n    public int requestFusion(int mode) {",
        ),
    ],
    "ScalarSubscription.java": [
        (
            "/**\n * A Subscription that holds a constant value and emits it only when requested.\n * @param <T> the value type\n */",
            "/**\n"
            " * 标量 Subscription：持有常量 value，仅在首次 request 时 onNext+onComplete。\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    /** The single value to emit, set to null. */",
            "    /** 待发射的常量值。 */",
        ),
        (
            "    /** The actual subscriber. */",
            "    /** 下游 Subscriber。 */",
        ),
        (
            "    /** No request has been issued yet. */",
            "    /** 状态：尚未 request。 */",
        ),
        (
            "    /** Request has been called.*/",
            "    /** 状态：已 request 并发射。 */",
        ),
        (
            "    /** Cancel has been called. */",
            "    /** 状态：已 cancel。 */",
        ),
        (
            "    @Override\n    public void request(long n) {",
            "    /** 首次 request 时 onNext(value)+onComplete。 */\n"
            "    @Override\n    public void request(long n) {",
        ),
        (
            "    @Override\n    public void cancel() {",
            "    /** lazySet(CANCELLED)。 */\n"
            "    @Override\n    public void cancel() {",
        ),
        (
            "    @Override\n    public int requestFusion(int mode) {",
            "    /** 支持 SYNC fusion。 */\n"
            "    @Override\n    public int requestFusion(int mode) {",
        ),
    ],
    "SubscriptionArbiter.java": [
        (
            "/**\n * Arbitrates requests and cancellation between Subscriptions.\n */",
            "/**\n"
            " * Subscription 仲裁器：合并 missed request/produced/subscription 变更，\n"
            " * 在 drainLoop 中串行应用到当前 actual Subscription。\n"
            " */",
        ),
        (
            "    /**\n     * The current subscription which may null if no Subscriptions have been set.\n     */",
            "    /** 当前生效的 upstream Subscription，可为 null。 */",
        ),
        (
            "    /**\n     * The current outstanding request amount.\n     */",
            "    /** 当前累计未转发的 request 量。 */",
        ),
        (
            "    public SubscriptionArbiter(boolean cancelOnReplace) {",
            "    /** @param cancelOnReplace 替换 Subscription 时是否 cancel 旧值 */\n"
            "    public SubscriptionArbiter(boolean cancelOnReplace) {",
        ),
        (
            "    /**\n     * Atomically sets a new subscription.\n     * @param s the subscription to set, not null (verified)\n     */",
            "    /**\n"
            "     * 设置新 Subscription；有积压 request 则转发。\n"
            "     * @param s 新 Subscription，非 null（已校验）\n"
            "     */",
        ),
        (
            "    @Override\n    public final void request(long n) {",
            "    /** 累加 request 并 drain；达到 MAX_VALUE 时 unbounded。 */\n"
            "    @Override\n    public final void request(long n) {",
        ),
        (
            "    public final void produced(long n) {",
            "    /** 扣减已生产数量；负值时 reportMoreProduced。 */\n"
            "    public final void produced(long n) {",
        ),
        (
            "    @Override\n    public void cancel() {",
            "    /** 置 cancelled 并 drain 取消 actual/missed。 */\n"
            "    @Override\n    public void cancel() {",
        ),
        (
            "    final void drainLoop() {",
            "    /** 合并 missed 字段并 request/cancel 当前 actual。 */\n"
            "    final void drainLoop() {",
        ),
        (
            "    /**\n     * Returns true if the arbiter runs in unbounded mode.\n     * @return true if the arbiter runs in unbounded mode\n     */",
            "    /**\n"
            "     * 是否已进入无界 request 模式。\n"
            "     * @return unbounded 则 true\n"
            "     */",
        ),
        (
            "    /**\n     * Returns true if the arbiter has been cancelled.\n     * @return true if the arbiter has been cancelled\n     */",
            "    /**\n"
            "     * 是否已 cancel。\n"
            "     * @return 已取消则 true\n"
            "     */",
        ),
    ],
    "SubscriptionHelper.java": [
        (
            "/**\n * Utility methods to validate Subscriptions in the various onSubscribe calls.\n */",
            "/**\n"
            " * Subscription 校验与原子设置工具：validate/set/setOnce/replace/cancel\n"
            " * 及 deferredRequest/deferredSetOnce 等背压辅助方法。\n"
            " */",
        ),
        (
            "    /**\n     * Represents a cancelled Subscription.\n     * <p>Don't leak this instance!\n     */",
            "    /**\n"
            "     * 表示已取消的 Subscription 哨兵实例。\n"
            "     * <p>勿向外泄漏此单例！\n"
            "     */",
        ),
        (
            "    /**\n     * Verifies that current is null, next is not null, otherwise signals errors\n     * to the RxJavaPlugins and returns false.\n     * @param current the current Subscription, expected to be null\n     * @param next the next Subscription, expected to be non-null\n     * @return true if the validation succeeded\n     */",
            "    /**\n"
            "     * 校验 current 为 null 且 next 非 null；否则上报错误。\n"
            "     * @param current 当前 Subscription，应为 null\n"
            "     * @param next 新 Subscription，应非 null\n"
            "     * @return 校验通过则 true\n"
            "     */",
        ),
        (
            "    /**\n     * Reports that the subscription is already set to the RxJavaPlugins error handler,\n     * which is an indication of a onSubscribe management bug.\n     */",
            "    /** 上报“Subscription already set”协议违规。 */",
        ),
        (
            "    /**\n     * Validates that the n is positive.\n     * @param n the request amount\n     * @return false if n is non-positive.\n     */",
            "    /**\n"
            "     * 校验 request 量 n 为正数。\n"
            "     * @param n request 量\n"
            "     * @return n 非正则 false\n"
            "     */",
        ),
        (
            "    /**\n     * Reports to the plugin error handler that there were more values produced than requested, which\n     * is a sign of internal backpressure handling bug.\n     * @param n the overproduction amount\n     */",
            "    /**\n"
            "     * 上报生产量超过 request 的协议违规。\n"
            "     * @param n 超产量\n"
            "     */",
        ),
        (
            "    /**\n     * Atomically sets the subscription on the field and cancels the\n     * previous subscription if any.\n     * @param field the target field to set the new subscription on\n     * @param s the new subscription\n     * @return true if the operation succeeded, false if the target field\n     * holds the {@link #CANCELLED} instance.\n     * @see #replace(AtomicReference, Subscription)\n     */",
            "    /**\n"
            "     * CAS 设置 Subscription 并 cancel 旧值。\n"
            "     * @param field 目标 AtomicReference\n"
            "     * @param s 新 Subscription\n"
            "     * @return 成功 true；field 已为 CANCELLED 则 false\n"
            "     * @see #replace(AtomicReference, Subscription)\n"
            "     */",
        ),
        (
            "    /**\n     * Atomically sets the subscription on the field if it is still null.\n     * <p>If the field is not null and doesn't contain the {@link #CANCELLED}\n     * instance, the {@link #reportSubscriptionSet()} is called.\n     * @param field the target field\n     * @param s the new subscription to set\n     * @return true if the operation succeeded, false if the target field was not null.\n     */",
            "    /**\n"
            "     * 仅在 field 为 null 时 CAS 设置；重复设置则 reportSubscriptionSet。\n"
            "     * @param field 目标 AtomicReference\n"
            "     * @param s 新 Subscription\n"
            "     * @return 首次设置成功则 true\n"
            "     */",
        ),
        (
            "    /**\n     * Atomically sets the subscription on the field but does not\n     * cancel the previous subscription.\n     * @param field the target field to set the new subscription on\n     * @param s the new subscription\n     * @return true if the operation succeeded, false if the target field\n     * holds the {@link #CANCELLED} instance.\n     * @see #set(AtomicReference, Subscription)\n     */",
            "    /**\n"
            "     * CAS 替换 Subscription，不 cancel 旧值。\n"
            "     * @param field 目标 AtomicReference\n"
            "     * @param s 新 Subscription\n"
            "     * @return 成功 true；已为 CANCELLED 则 false\n"
            "     * @see #set(AtomicReference, Subscription)\n"
            "     */",
        ),
        (
            "    /**\n     * Atomically swaps in the common cancelled subscription instance\n     * and cancels the previous subscription if any.\n     * @param field the target field to dispose the contents of\n     * @return true if the swap from the non-cancelled instance to the\n     * common cancelled instance happened in the caller's thread (allows\n     * further one-time actions).\n     */",
            "    /**\n"
            "     * getAndSet(CANCELLED) 并 cancel 原 Subscription。\n"
            "     * @param field 目标 AtomicReference\n"
            "     * @return 由调用线程完成 swap 则 true\n"
            "     */",
        ),
        (
            "    /**\n     * Atomically sets the new Subscription on the field and requests any accumulated amount\n     * from the requested field.\n     * @param field the target field for the new Subscription\n     * @param requested the current requested amount\n     * @param s the new Subscription, not null (verified)\n     * @return true if the Subscription was set the first time\n     */",
            "    /**\n"
            "     * setOnce 后 flush requested 中积压的 request。\n"
            "     * @param field Subscription 字段\n"
            "     * @param requested 暂存 request 的 AtomicLong\n"
            "     * @param s 新 Subscription，非 null\n"
            "     * @return 首次设置成功则 true\n"
            "     */",
        ),
        (
            "    /**\n     * Atomically requests from the Subscription in the field if not null, otherwise accumulates\n     * the request amount in the requested field to be requested once the field is set to non-null.\n     * @param field the target field that may already contain a Subscription\n     * @param requested the current requested amount\n     * @param n the request amount, positive (verified)\n     */",
            "    /**\n"
            "     * field 有 Subscription 则直接 request，否则累加至 requested。\n"
            "     * @param field 可能已持有 Subscription 的字段\n"
            "     * @param requested 暂存 request\n"
            "     * @param n 本次 request 量，为正\n"
            "     */",
        ),
        (
            "    /**\n     * Atomically sets the subscription on the field if it is still null and issues a positive request\n     * to the given {@link Subscription}.\n     * <p>\n     * If the field is not null and doesn't contain the {@link #CANCELLED}\n     * instance, the {@link #reportSubscriptionSet()} is called.\n     * @param field the target field\n     * @param s the new subscription to set\n     * @param request the amount to request, positive (not verified)\n     * @return true if the operation succeeded, false if the target field was not null.\n     * @since 2.1.11\n     */",
            "    /**\n"
            "     * setOnce 后立即 request 指定数量。\n"
            "     * @param field 目标字段\n"
            "     * @param s 新 Subscription\n"
            "     * @param request 初始 request 量\n"
            "     * @return 首次设置成功则 true\n"
            "     * @since 2.1.11\n"
            "     */",
        ),
    ],
}


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def tree_guard(env: dict[str, str] | None = None) -> int:
    tracked = len(
        subprocess.check_output(["git", "-C", str(ROOT), "ls-files"], env=env).splitlines()
    )
    if tracked < 50000:
        raise RuntimeError(f"tree guard failed: tracked={tracked} (expected >=50000)")
    for path in GUARD_FILES:
        if env is None:
            if not path.exists():
                raise RuntimeError(f"guard file missing: {path}")
            blob = path.read_text(encoding="utf-8")
        else:
            rel = path.relative_to(ROOT)
            blob = subprocess.check_output(
                ["git", "-C", str(ROOT), "show", f":{rel}"], env=env, text=True
            )
        if not has_chinese(blob):
            raise RuntimeError(f"guard file lacks Chinese: {path}")
    return tracked


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def annotate_file(rel: str) -> None:
    name = Path(rel).name
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    reps = FILE_REPLACEMENTS.get(name, [])
    if not reps:
        raise ValueError(f"NO_REPLACEMENTS: {rel}")
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)
    text = apply_replacements(dst.read_text(encoding="utf-8"), reps)
    cn = len(re.findall(r"[\u4e00-\u9fff]", text))
    if cn < 10 or "Licensed under the Apache License" not in text:
        raise ValueError(f"VALIDATION cn={cn}: {rel}")
    dst.write_text(text, encoding="utf-8")


def isolated_index_commit(
    message: str, paths: list[str], base_ref: str = "origin/main"
) -> tuple[str, int]:
    index_file = Path("/tmp/git-index-rxjava-w25b")
    env = os.environ.copy()
    env["GIT_INDEX_FILE"] = str(index_file)
    base = subprocess.check_output(
        ["git", "-C", str(ROOT), "rev-parse", base_ref], text=True, env=env
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "read-tree", base], env=env, check=True)
    tree_before = subprocess.check_output(
        ["git", "-C", str(ROOT), "write-tree"], env=env, text=True
    ).strip()
    tree_count = len(
        subprocess.check_output(
            ["git", "-C", str(ROOT), "ls-tree", "-r", "--name-only", tree_before],
            env=env,
            text=True,
        ).splitlines()
    )
    if tree_count < 50000:
        raise RuntimeError(f"read-tree guard failed: tree_count={tree_count} (expected >=50000)")
    subprocess.run(["git", "-C", str(ROOT), "add", "--", *paths], env=env, check=True)
    tree_guard(env)
    tree = subprocess.check_output(
        ["git", "-C", str(ROOT), "write-tree"], env=env, text=True
    ).strip()
    commit = subprocess.check_output(
        ["git", "-C", str(ROOT), "commit-tree", tree, "-p", base, "-m", message],
        text=True,
        env=env,
    ).strip()
    subprocess.run(
        ["git", "-C", str(ROOT), "update-ref", "refs/heads/main", commit], check=True
    )
    index_file.unlink(missing_ok=True)
    subprocess.run(["git", "-C", str(ROOT), "reset", "--hard", "HEAD"], check=True)
    return commit, tree_count


def push_main(retries: int = 4) -> None:
    r = subprocess.CompletedProcess([], 1)
    for attempt in range(retries):
        subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
        local = subprocess.check_output(
            ["git", "-C", str(ROOT), "rev-parse", "main"], text=True
        ).strip()
        remote = subprocess.check_output(
            ["git", "-C", str(ROOT), "rev-parse", "origin/main"], text=True
        ).strip()
        if local != remote:
            merge_base = subprocess.check_output(
                ["git", "-C", str(ROOT), "merge-base", local, remote], text=True
            ).strip()
            if merge_base != remote:
                raise RuntimeError(
                    f"main diverged from origin/main (local={local[:8]} remote={remote[:8]})"
                )
        r = subprocess.run(
            ["git", "-C", str(ROOT), "push", "-u", "origin", "main"],
            capture_output=True,
            text=True,
        )
        if r.returncode == 0:
            return
        if attempt + 1 < retries:
            time.sleep(4 * (2**attempt))
    raise subprocess.CalledProcessError(r.returncode, r.args, r.stdout, r.stderr)


def confirm_chinese_on_origin() -> dict[str, bool]:
    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    result: dict[str, bool] = {}
    for rel in BATCH_FILES:
        blob = subprocess.check_output(
            [
                "git",
                "-C",
                str(ROOT),
                "show",
                f"origin/main:rxjava/4.0.0-alpha-21/analyzed/{rel}",
            ],
            text=True,
        )
        result[rel] = has_chinese(blob)
    return result


def main() -> int:
    if len(BATCH_FILES) != 15:
        raise RuntimeError(f"batch guard failed: expected 15 files, got {len(BATCH_FILES)}")

    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    subprocess.run(["git", "-C", str(ROOT), "checkout", "main"], check=True)
    subprocess.run(["git", "-C", str(ROOT), "reset", "--hard", "origin/main"], check=True)

    failures: list[str] = []
    for rel in BATCH_FILES:
        try:
            annotate_file(rel)
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if failures:
        print(json.dumps({"ok": 0, "failures": failures}, ensure_ascii=False, indent=2))
        return 1

    analyzed_paths = [f"rxjava/4.0.0-alpha-21/analyzed/{rel}" for rel in BATCH_FILES]
    script_path = f"scripts/{SCRIPT_NAME}"
    sha, tree_count = isolated_index_commit(
        "rxjava 4.0.0-alpha-21: Chinese-annotate wave 25b [15:30]",
        [*analyzed_paths, script_path],
    )
    push_main()

    subprocess.run(
        [
            sys.executable,
            str(SCRIPTS / "mark_batch_done.py"),
            "--project",
            "rxjava",
            "--version",
            "4.0.0-alpha-21",
            "--note",
            MARK_NOTE,
            *BATCH_FILES,
        ],
        check=True,
    )
    queue_paths = [
        "rxjava/4.0.0-alpha-21/_reports/class-queue/done.txt",
        "rxjava/4.0.0-alpha-21/_reports/class-queue/pending.txt",
        "rxjava/4.0.0-alpha-21/_reports/class-queue/batch.json",
        "rxjava/4.0.0-alpha-21/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        "queue: mark rxjava 4.0.0-alpha-21 wave25b done",
        queue_paths,
        base_ref="HEAD",
    )
    push_main()

    done_total = len(
        [ln for ln in (QUEUE / "done.txt").read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    pending_total = len(
        [
            ln
            for ln in (QUEUE / "pending.txt").read_text(encoding="utf-8").splitlines()
            if ln.strip()
        ]
    )
    chinese = confirm_chinese_on_origin()
    verified = sum(chinese.values())
    print(
        json.dumps(
            {
                "sha": sha,
                "queue_sha": queue_sha,
                "tree_count": tree_count,
                "done": done_total,
                "pending": pending_total,
                "chinese_confirmed": chinese,
                "all_chinese": all(chinese.values()),
                "verified": f"{verified}/15",
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0 if all(chinese.values()) and verified == 15 else 1


if __name__ == "__main__":
    raise SystemExit(main())
