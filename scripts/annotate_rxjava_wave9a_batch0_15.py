#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-9a Flowable* [0:15]."""
from __future__ import annotations

import json
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "rxjava/4.0.0-alpha-21"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
WAVE9A_FILE = Path("/tmp/rxjava_w9a.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE9A_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "FlowableAnySingle.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableAnySingle",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 判断上游 {@link Flowable} 是否存在任一元素满足 {@link Predicate}；\n"
            " * 存在则 {@link Single} 发出 true，否则在完成后发出 false。\n"
            " * @param <T> 上游元素类型\n"
            " */\n"
            "public final class FlowableAnySingle",
        ),
        (
            "    public FlowableAnySingle(Flowable<T> source, Predicate<? super T> predicate) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param predicate 测试谓词\n"
            "     */\n"
            "    public FlowableAnySingle(Flowable<T> source, Predicate<? super T> predicate) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super Boolean> observer) {",
            "    /** 订阅 source 并在找到匹配或完成时发出 Boolean 结果。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super Boolean> observer) {",
        ),
        (
            "    @Override\n    public Flowable<Boolean> fuseToFlowable() {",
            "    /** 融合为 {@link FlowableAny} 以支持背压。 */\n"
            "    @Override\n    public Flowable<Boolean> fuseToFlowable() {",
        ),
        (
            "    static final class AnySubscriber<T> implements FlowableSubscriber<T>, Disposable {",
            "    /** 逐项测试 predicate；匹配时取消上游并发出 true。 */\n"
            "    static final class AnySubscriber<T> implements FlowableSubscriber<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** predicate 为 true 时取消上游并 onSuccess(true)。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 未找到匹配则 onSuccess(false)。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "FlowableAutoConnect.java": [
        (
            "/**\n * Wraps a {@link ConnectableFlowable} and calls its {@code connect()} method once\n * the specified number of {@link Subscriber}s have subscribed.\n *\n * @param <T> the value type of the chain\n */",
            "/**\n"
            " * 包装 {@link ConnectableFlowable}，在指定数量的 {@link Subscriber} 订阅后\n"
            " * 调用其 {@code connect()} 方法。\n"
            " *\n"
            " * @param <T> 链中元素类型\n"
            " */",
        ),
        (
            "    public FlowableAutoConnect(ConnectableFlowable<? extends T> source,\n            int numberOfSubscribers,\n            Consumer<? super Disposable> connection) {",
            "    /**\n"
            "     * @param source 可连接的 ConnectableFlowable\n"
            "     * @param numberOfSubscribers 触发 connect 所需的订阅者数量\n"
            "     * @param connection connect 时接收 Disposable 的回调\n"
            "     */\n"
            "    public FlowableAutoConnect(ConnectableFlowable<? extends T> source,\n            int numberOfSubscribers,\n            Consumer<? super Disposable> connection) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super T> child) {",
            "    /** 订阅 source 并在达到 numberOfSubscribers 时自动 connect。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super T> child) {",
        ),
    ],
    "FlowableBlockingSubscribe.java": [
        (
            "/**\n * Utility methods to consume a Publisher in a blocking manner with callbacks or Subscriber.\n */",
            "/**\n"
            " * 以阻塞方式消费 {@link Publisher} 的工具方法，支持回调或 {@link Subscriber}。\n"
            " */",
        ),
        (
            "    /** Utility class. */\n    private FlowableBlockingSubscribe() {",
            "    /** 工具类，禁止实例化。 */\n    private FlowableBlockingSubscribe() {",
        ),
        (
            "    /**\n     * Subscribes to the source and calls the Subscriber methods on the current thread.\n     * <p>\n     * @param source the source publisher\n     * The cancellation and backpressure is composed through.\n     * @param subscriber the subscriber to forward events and calls to in the current thread\n     * @param <T> the value type\n     */",
            "    /**\n"
            "     * 订阅 source 并在当前线程上调用 Subscriber 方法。\n"
            "     * <p>取消与背压通过 BlockingSubscriber 组合传递。\n"
            "     * @param source 源 Publisher\n"
            "     * @param subscriber 在当前线程接收事件的 Subscriber\n"
            "     * @param <T> 元素类型\n"
            "     */",
        ),
        (
            "    /**\n     * Runs the source observable to a terminal event, ignoring any values and rethrowing any exception.\n     * @param source the source to await\n     * @param <T> the value type\n     */",
            "    /**\n"
            "     * 阻塞等待 source 到达终止事件，忽略所有元素并在有异常时重新抛出。\n"
            "     * @param source 要等待的源 Publisher\n"
            "     * @param <T> 元素类型\n"
            "     */",
        ),
        (
            "    /**\n     * Subscribes to the source and calls the given actions on the current thread.\n     * @param o the source publisher\n     * @param onNext the callback action for each source value\n     * @param onError the callback action for an error event\n     * @param onComplete the callback action for the completion event.\n     * @param <T> the value type\n     */",
            "    /**\n"
            "     * 订阅 source 并在当前线程上调用给定回调。\n"
            "     * @param o 源 Publisher\n"
            "     * @param onNext 每个元素的回调\n"
            "     * @param onError 错误事件回调\n"
            "     * @param onComplete 完成事件回调\n"
            "     * @param <T> 元素类型\n"
            "     */",
        ),
        (
            "    /**\n     * Subscribes to the source and calls the given actions on the current thread.\n     * @param o the source publisher\n     * @param onNext the callback action for each source value\n     * @param onError the callback action for an error event\n     * @param onComplete the callback action for the completion event.\n     * @param bufferSize the number of elements to prefetch from the source Publisher\n     * @param <T> the value type\n     */",
            "    /**\n"
            "     * 订阅 source 并在当前线程上调用给定回调，使用有界预取缓冲。\n"
            "     * @param o 源 Publisher\n"
            "     * @param onNext 每个元素的回调\n"
            "     * @param onError 错误事件回调\n"
            "     * @param onComplete 完成事件回调\n"
            "     * @param bufferSize 从源 Publisher 预取的元素数量\n"
            "     * @param <T> 元素类型\n"
            "     */",
        ),
    ],
    "FlowableBufferExactBoundary.java": [
        (
            "import io.reactivex.rxjava4.subscribers.*;\n\npublic final class FlowableBufferExactBoundary",
            "import io.reactivex.rxjava4.subscribers.*;\n\n"
            "/**\n"
            " * 按 {@link Publisher} 边界信号将上游元素收集到 {@link Collection} 中；\n"
            " * 每次 boundary 发出信号时刷新并下发当前缓冲区。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <U> 缓冲区类型\n"
            " * @param <B> 边界信号类型\n"
            " */\n"
            "public final class FlowableBufferExactBoundary",
        ),
        (
            "    public FlowableBufferExactBoundary(Flowable<T> source, Publisher<B> boundary, Supplier<U> bufferSupplier) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param boundary 触发缓冲区刷新的边界 Publisher\n"
            "     * @param bufferSupplier 创建新缓冲区的 Supplier\n"
            "     */\n"
            "    public FlowableBufferExactBoundary(Flowable<T> source, Publisher<B> boundary, Supplier<U> bufferSupplier) {",
        ),
        (
            "    static final class BufferExactBoundarySubscriber<T, U extends Collection<? super T>, B>\n    extends QueueDrainSubscriber<T, U, U> implements Subscription, Disposable {",
            "    /** 管理当前缓冲区并在 boundary 信号时刷新下发。 */\n"
            "    static final class BufferExactBoundarySubscriber<T, U extends Collection<? super T>, B>\n    extends QueueDrainSubscriber<T, U, U> implements Subscription, Disposable {",
        ),
        (
            "        void next() {",
            "        /** boundary 信号到达：下发旧缓冲区并创建新缓冲区。 */\n"
            "        void next() {",
        ),
        (
            "    static final class BufferBoundarySubscriber<T, U extends Collection<? super T>, B> extends DisposableSubscriber<B> {",
            "    /** 订阅 boundary Publisher 并在信号时通知 parent 刷新缓冲区。 */\n"
            "    static final class BufferBoundarySubscriber<T, U extends Collection<? super T>, B> extends DisposableSubscriber<B> {",
        ),
    ],
    "FlowableCollect.java": [
        (
            "import java.util.Objects;\n\npublic final class FlowableCollect",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 使用 {@link BiConsumer} 将上游元素累积到可变容器中，\n"
            " * 完成后发出最终累积结果。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <U> 累积容器类型\n"
            " */\n"
            "public final class FlowableCollect",
        ),
        (
            "    public FlowableCollect(Flowable<T> source, Supplier<? extends U> initialSupplier, BiConsumer<? super U, ? super T> collector) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param initialSupplier 提供初始累积容器的 Supplier\n"
            "     * @param collector 将每个元素合并到容器的 BiConsumer\n"
            "     */\n"
            "    public FlowableCollect(Flowable<T> source, Supplier<? extends U> initialSupplier, BiConsumer<? super U, ? super T> collector) {",
        ),
        (
            "    static final class CollectSubscriber<T, U> extends DeferredScalarSubscription<U> implements FlowableSubscriber<T> {",
            "    /** 逐项调用 collector 并在完成时发出累积结果。 */\n"
            "    static final class CollectSubscriber<T, U> extends DeferredScalarSubscription<U> implements FlowableSubscriber<T> {",
        ),
    ],
    "FlowableCollectSingle.java": [
        (
            "import java.util.Objects;\n\npublic final class FlowableCollectSingle",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 使用 {@link BiConsumer} 将上游元素累积到可变容器中，\n"
            " * 完成后以 {@link Single} 发出最终累积结果。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <U> 累积容器类型\n"
            " */\n"
            "public final class FlowableCollectSingle",
        ),
        (
            "    public FlowableCollectSingle(Flowable<T> source, Supplier<? extends U> initialSupplier, BiConsumer<? super U, ? super T> collector) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param initialSupplier 提供初始累积容器的 Supplier\n"
            "     * @param collector 将每个元素合并到容器的 BiConsumer\n"
            "     */\n"
            "    public FlowableCollectSingle(Flowable<T> source, Supplier<? extends U> initialSupplier, BiConsumer<? super U, ? super T> collector) {",
        ),
        (
            "    @Override\n    public Flowable<U> fuseToFlowable() {",
            "    /** 融合为 {@link FlowableCollect} 以支持背压。 */\n"
            "    @Override\n    public Flowable<U> fuseToFlowable() {",
        ),
        (
            "    static final class CollectSubscriber<T, U> implements FlowableSubscriber<T>, Disposable {",
            "    /** 逐项调用 collector 并在完成时 onSuccess 累积结果。 */\n"
            "    static final class CollectSubscriber<T, U> implements FlowableSubscriber<T>, Disposable {",
        ),
    ],
    "FlowableConcatArray.java": [
        (
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionArbiter;\n\npublic final class FlowableConcatArray",
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionArbiter;\n\n"
            "/**\n"
            " * 顺序订阅 {@link Publisher} 数组中的各源，依次转发元素；\n"
            " * 可选 delayError 模式收集所有错误后再终止。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableConcatArray",
        ),
        (
            "    public FlowableConcatArray(Publisher<? extends T>[] sources, boolean delayError) {",
            "    /**\n"
            "     * @param sources 要顺序拼接的 Publisher 数组\n"
            "     * @param delayError 为 true 时收集所有错误后再报告\n"
            "     */\n"
            "    public FlowableConcatArray(Publisher<? extends T>[] sources, boolean delayError) {",
        ),
        (
            "    static final class ConcatArraySubscriber<T> extends SubscriptionArbiter implements FlowableSubscriber<T> {",
            "    /** 按 index 顺序订阅各 Publisher 并转发事件。 */\n"
            "    static final class ConcatArraySubscriber<T> extends SubscriptionArbiter implements FlowableSubscriber<T> {",
        ),
        (
            "        @Override\n        public void onError(Throwable t) {",
            "        /** delayError 时收集错误并继续下一源，否则立即转发错误。 */\n"
            "        @Override\n        public void onError(Throwable t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 当前源完成后订阅下一 Publisher 或终止序列。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "FlowableConcatMapEager.java": [
        (
            "import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;\n\npublic final class FlowableConcatMapEager",
            "import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;\n\n"
            "/**\n"
            " * 对上游每个元素映射为 inner {@link Publisher} 并 eager 并行订阅，\n"
            " * 按映射顺序串行转发 inner 元素，受 maxConcurrency 限制。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> inner Publisher 元素类型\n"
            " */\n"
            "public final class FlowableConcatMapEager",
        ),
        (
            "    public FlowableConcatMapEager(Flowable<T> source,\n            Function<? super T, ? extends Publisher<? extends R>> mapper,\n            int maxConcurrency,\n            int prefetch,\n            ErrorMode errorMode) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param mapper 将元素映射为 inner Publisher 的函数\n"
            "     * @param maxConcurrency 最大并行 inner 订阅数\n"
            "     * @param prefetch 每个 inner 的预取量\n"
            "     * @param errorMode 错误处理模式\n"
            "     */\n"
            "    public FlowableConcatMapEager(Flowable<T> source,\n            Function<? super T, ? extends Publisher<? extends R>> mapper,\n            int maxConcurrency,\n            int prefetch,\n            ErrorMode errorMode) {",
        ),
        (
            "    static final class ConcatMapEagerDelayErrorSubscriber<T, R>\n    extends AtomicInteger\n    implements FlowableSubscriber<T>, Subscription, InnerQueuedSubscriberSupport<R> {",
            "    /** eager 订阅 inner Publisher 并按序 drain 转发元素。 */\n"
            "    static final class ConcatMapEagerDelayErrorSubscriber<T, R>\n    extends AtomicInteger\n    implements FlowableSubscriber<T>, Subscription, InnerQueuedSubscriberSupport<R> {",
        ),
        (
            "        void drainAndCancel() {",
            "        /** 取消所有 inner 订阅并清空队列。 */\n"
            "        void drainAndCancel() {",
        ),
        (
            "        @Override\n        public void drain() {",
            "        /** 从当前 inner 队列按背压转发元素，完成后切换下一 inner。 */\n"
            "        @Override\n        public void drain() {",
        ),
    ],
    "FlowableConcatMapEagerPublisher.java": [
        (
            "/**\n * ConcatMapEager which works with an arbitrary Publisher source.\n * <p>History: 2.0.7 - experimental\n * @param <T> the input value type\n * @param <R> the output type\n * @since 2.1\n */",
            "/**\n"
            " * 适用于任意 {@link Publisher} 源的 ConcatMapEager 实现。\n"
            " * <p>History: 2.0.7 - experimental\n"
            " * @param <T> 输入元素类型\n"
            " * @param <R> 输出元素类型\n"
            " * @since 2.1\n"
            " */",
        ),
        (
            "    public FlowableConcatMapEagerPublisher(Publisher<T> source,\n            Function<? super T, ? extends Publisher<? extends R>> mapper,\n            int maxConcurrency,\n            int prefetch,\n            ErrorMode errorMode) {",
            "    /**\n"
            "     * @param source 上游 Publisher\n"
            "     * @param mapper 将元素映射为 inner Publisher 的函数\n"
            "     * @param maxConcurrency 最大并行 inner 订阅数\n"
            "     * @param prefetch 每个 inner 的预取量\n"
            "     * @param errorMode 错误处理模式\n"
            "     */\n"
            "    public FlowableConcatMapEagerPublisher(Publisher<T> source,\n            Function<? super T, ? extends Publisher<? extends R>> mapper,\n            int maxConcurrency,\n            int prefetch,\n            ErrorMode errorMode) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
            "    /** 订阅 source 并使用 ConcatMapEagerDelayErrorSubscriber 转发。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
        ),
    ],
    "FlowableConcatWithCompletable.java": [
        (
            "/**\n * Subscribe to a main Flowable first, then when it completes normally, subscribe to a Completable\n * and terminate when it terminates.\n * <p>History: 2.1.10 - experimental\n * @param <T> the element type of the main source and output type\n * @since 2.2\n */",
            "/**\n"
            " * 先订阅主 {@link Flowable}，正常完成后订阅 {@link CompletableSource}，\n"
            " * 并在 Completable 终止时结束序列。\n"
            " * <p>History: 2.1.10 - experimental\n"
            " * @param <T> 主源及输出元素类型\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    public FlowableConcatWithCompletable(Flowable<T> source, CompletableSource other) {",
            "    /**\n"
            "     * @param source 主 Flowable 源\n"
            "     * @param other 主源完成后订阅的 CompletableSource\n"
            "     */\n"
            "    public FlowableConcatWithCompletable(Flowable<T> source, CompletableSource other) {",
        ),
        (
            "    static final class ConcatWithSubscriber<T>\n    extends AtomicReference<Disposable>\n    implements FlowableSubscriber<T>, CompletableObserver, Subscription {",
            "    /** 主 Flowable 完成后切换订阅 Completable 并转发终止事件。 */\n"
            "    static final class ConcatWithSubscriber<T>\n    extends AtomicReference<Disposable>\n    implements FlowableSubscriber<T>, CompletableObserver, Subscription {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 主源完成时订阅 other Completable，Completable 完成后再通知下游。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "FlowableConcatWithMaybe.java": [
        (
            "/**\n * Subscribe to a main Flowable first, then when it completes normally, subscribe to a Maybe,\n * signal its success value followed by a completion or signal its error or completion signal as is.\n * <p>History: 2.1.10 - experimental\n * @param <T> the element type of the main source and output type\n * @since 2.2\n */",
            "/**\n"
            " * 先订阅主 {@link Flowable}，正常完成后订阅 {@link MaybeSource}，\n"
            " * 发出其成功值后完成，或按原样转发错误/完成信号。\n"
            " * <p>History: 2.1.10 - experimental\n"
            " * @param <T> 主源及输出元素类型\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    public FlowableConcatWithMaybe(Flowable<T> source, MaybeSource<? extends T> other) {",
            "    /**\n"
            "     * @param source 主 Flowable 源\n"
            "     * @param other 主源完成后订阅的 MaybeSource\n"
            "     */\n"
            "    public FlowableConcatWithMaybe(Flowable<T> source, MaybeSource<? extends T> other) {",
        ),
        (
            "    static final class ConcatWithSubscriber<T>\n    extends SinglePostCompleteSubscriber<T, T>\n    implements MaybeObserver<T> {",
            "    /** 主 Flowable 完成后切换订阅 Maybe 并转发成功值。 */\n"
            "    static final class ConcatWithSubscriber<T>\n    extends SinglePostCompleteSubscriber<T, T>\n    implements MaybeObserver<T> {",
        ),
        (
            "        @Override\n        public void onSuccess(T t) {",
            "        /** 将 Maybe 成功值作为最后一个元素发出后完成。 */\n"
            "        @Override\n        public void onSuccess(T t) {",
        ),
    ],
    "FlowableConcatWithSingle.java": [
        (
            "/**\n * Subscribe to a main Flowable first, then when it completes normally, subscribe to a Single,\n * signal its success value followed by a completion or signal its error as is.\n * <p>History: 2.1.10 - experimental\n * @param <T> the element type of the main source and output type\n * @since 2.2\n */",
            "/**\n"
            " * 先订阅主 {@link Flowable}，正常完成后订阅 {@link SingleSource}，\n"
            " * 发出其成功值后完成，或按原样转发错误。\n"
            " * <p>History: 2.1.10 - experimental\n"
            " * @param <T> 主源及输出元素类型\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    public FlowableConcatWithSingle(Flowable<T> source, SingleSource<? extends T> other) {",
            "    /**\n"
            "     * @param source 主 Flowable 源\n"
            "     * @param other 主源完成后订阅的 SingleSource\n"
            "     */\n"
            "    public FlowableConcatWithSingle(Flowable<T> source, SingleSource<? extends T> other) {",
        ),
        (
            "    static final class ConcatWithSubscriber<T>\n    extends SinglePostCompleteSubscriber<T, T>\n    implements SingleObserver<T> {",
            "    /** 主 Flowable 完成后切换订阅 Single 并转发成功值。 */\n"
            "    static final class ConcatWithSubscriber<T>\n    extends SinglePostCompleteSubscriber<T, T>\n    implements SingleObserver<T> {",
        ),
        (
            "        @Override\n        public void onSuccess(T t) {",
            "        /** 将 Single 成功值作为最后一个元素发出后完成。 */\n"
            "        @Override\n        public void onSuccess(T t) {",
        ),
    ],
    "FlowableCount.java": [
        (
            "import java.io.Serial;\n\npublic final class FlowableCount",
            "import java.io.Serial;\n\n"
            "/**\n"
            " * 统计上游 {@link Flowable} 发出的元素个数，完成后发出总计数。\n"
            " * @param <T> 上游元素类型\n"
            " */\n"
            "public final class FlowableCount",
        ),
        (
            "    static final class CountSubscriber extends DeferredScalarSubscription<Long>\n    implements FlowableSubscriber<Object> {",
            "    /** 递增 count 并在完成时发出总计数。 */\n"
            "    static final class CountSubscriber extends DeferredScalarSubscription<Long>\n    implements FlowableSubscriber<Object> {",
        ),
    ],
    "FlowableCountSingle.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableCountSingle",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 统计上游 {@link Flowable} 发出的元素个数，\n"
            " * 完成后以 {@link Single} 发出总计数。\n"
            " * @param <T> 上游元素类型\n"
            " */\n"
            "public final class FlowableCountSingle",
        ),
        (
            "    public FlowableCountSingle(Flowable<T> source) {",
            "    /** @param source 要计数的 Flowable 源 */\n"
            "    public FlowableCountSingle(Flowable<T> source) {",
        ),
        (
            "    @Override\n    public Flowable<Long> fuseToFlowable() {",
            "    /** 融合为 {@link FlowableCount} 以支持背压。 */\n"
            "    @Override\n    public Flowable<Long> fuseToFlowable() {",
        ),
        (
            "    static final class CountSubscriber implements FlowableSubscriber<Object>, Disposable {",
            "    /** 递增 count 并在完成时 onSuccess 总计数。 */\n"
            "    static final class CountSubscriber implements FlowableSubscriber<Object>, Disposable {",
        ),
    ],
    "FlowableDebounce.java": [
        (
            "import io.reactivex.rxjava4.subscribers.*;\n\npublic final class FlowableDebounce",
            "import io.reactivex.rxjava4.subscribers.*;\n\n"
            "/**\n"
            " * 对每个上游元素通过 debounceSelector 映射为信号 Publisher；\n"
            " * 仅当该 Publisher 在下一元素到达前终止时才发出该元素。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <U> debounce 信号类型\n"
            " */\n"
            "public final class FlowableDebounce",
        ),
        (
            "    public FlowableDebounce(Flowable<T> source, Function<? super T, ? extends Publisher<U>> debounceSelector) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param debounceSelector 为每个元素生成 debounce 信号的函数\n"
            "     */\n"
            "    public FlowableDebounce(Flowable<T> source, Function<? super T, ? extends Publisher<U>> debounceSelector) {",
        ),
        (
            "    static final class DebounceSubscriber<T, U> extends AtomicLong\n    implements FlowableSubscriber<T>, Subscription {",
            "    /** 管理 debounce 计时器并在信号终止时发出对应元素。 */\n"
            "    static final class DebounceSubscriber<T, U> extends AtomicLong\n    implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        void emit(long idx, T value) {",
            "        /** index 匹配且背压允许时向下游发出 value。 */\n"
            "        void emit(long idx, T value) {",
        ),
        (
            "        static final class DebounceInnerSubscriber<T, U> extends DisposableSubscriber<U> {",
            "    /** 监听 debounce 信号 Publisher；终止时通知 parent 发出对应元素。 */\n"
            "        static final class DebounceInnerSubscriber<T, U> extends DisposableSubscriber<U> {",
        ),
        (
            "            void emit() {",
            "            /** 仅一次向 parent 发出缓存的元素。 */\n"
            "            void emit() {",
        ),
    ],
}


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def mark_queue_done(files: list[str]) -> None:
    subprocess.run(
        [
            sys.executable,
            str(ROOT / "scripts/mark_batch_done.py"),
            "--project",
            "rxjava",
            "--version",
            "4.0.0-alpha-21",
            "--note",
            "wave9a Flowable* [0:15]",
            *files,
        ],
        check=True,
    )
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    file_set = set(files)
    batch["files"] = [f for f in batch.get("files", []) if f not in file_set]
    batch["done"] = len([ln for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()])
    batch["remaining_pending"] = len(
        [ln for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        name = Path(rel).name
        src = ORIGINAL / rel
        dst = ANALYZED / rel
        if not src.exists():
            failures.append(f"MISSING original: {rel}")
            continue
        reps = FILE_REPLACEMENTS.get(name, [])
        if not reps:
            failures.append(f"NO_REPLACEMENTS: {rel}")
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)
        try:
            text = dst.read_text(encoding="utf-8")
            text = apply_replacements(text, reps)
            cn = len(re.findall(r"[\u4e00-\u9fff]", text))
            lic = "Licensed under the Apache License" in text
            if cn < 10 or not lic:
                failures.append(f"VALIDATION cn={cn} lic={lic}: {rel}")
                continue
            dst.write_text(text, encoding="utf-8")
            ok += 1
            print(f"OK cn={cn} {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if ok == len(BATCH_FILES) and not failures:
        mark_queue_done(BATCH_FILES)
        print(f"Marked {ok} files done in queue")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
