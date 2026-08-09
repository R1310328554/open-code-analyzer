#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-11a Flowable* [0:15]."""
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
WAVE11A_FILE = Path("/tmp/rxjava_w11a.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE11A_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "FlowableMapNotification.java": [
        (
            "import java.util.Objects;\n\npublic final class FlowableMapNotification",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 将上游 onNext、onError、onComplete 分别映射为下游元素后发射。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> 映射后元素类型\n"
            " */\n"
            "public final class FlowableMapNotification",
        ),
        (
            "    public FlowableMapNotification(\n"
            "            Flowable<T> source,\n"
            "            Function<? super T, ? extends R> onNextMapper,\n"
            "            Function<? super Throwable, ? extends R> onErrorMapper,\n"
            "            Supplier<? extends R> onCompleteSupplier) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param onNextMapper onNext 元素映射函数\n"
            "     * @param onErrorMapper onError 异常映射函数\n"
            "     * @param onCompleteSupplier onComplete 时提供元素的 Supplier\n"
            "     */\n"
            "    public FlowableMapNotification(\n"
            "            Flowable<T> source,\n"
            "            Function<? super T, ? extends R> onNextMapper,\n"
            "            Function<? super Throwable, ? extends R> onErrorMapper,\n"
            "            Supplier<? extends R> onCompleteSupplier) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
            "    /** 订阅上游并将三类终止事件映射为下游 onNext。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
        ),
        (
            "    static final class MapNotificationSubscriber<T, R>\n    extends SinglePostCompleteSubscriber<T, R> {",
            "    /** 分别映射 onNext/onError/onComplete 为下游元素的 subscriber。 */\n"
            "    static final class MapNotificationSubscriber<T, R>\n    extends SinglePostCompleteSubscriber<T, R> {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 应用 onNextMapper 并向下游 onNext。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onError(Throwable t) {",
            "        /** 应用 onErrorMapper 后通过 complete 发出最后一个元素。 */\n"
            "        @Override\n        public void onError(Throwable t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 调用 onCompleteSupplier 后通过 complete 发出最后一个元素。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "FlowableMapPublisher.java": [
        (
            "/**\n * Map working with an arbitrary Publisher source.\n * <p>History: 2.0.7 - experimental\n * @param <T> the input value type\n * @param <U> the output value type\n * @since 2.1\n */",
            "/**\n"
            " * 对任意 {@link Publisher} 源中的每个元素应用 map 映射。\n"
            " * <p>History: 2.0.7 - experimental\n"
            " * @param <T> 输入值类型\n"
            " * @param <U> 输出值类型\n"
            " * @since 2.1\n"
            " */",
        ),
        (
            "    public FlowableMapPublisher(Publisher<T> source, Function<? super T, ? extends U> mapper) {",
            "    /**\n"
            "     * @param source 上游 Publisher\n"
            "     * @param mapper 元素映射函数\n"
            "     */\n"
            "    public FlowableMapPublisher(Publisher<T> source, Function<? super T, ? extends U> mapper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super U> s) {",
            "    /** 复用 {@link FlowableMap.MapSubscriber} 订阅任意 Publisher。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super U> s) {",
        ),
    ],
    "FlowableMaterialize.java": [
        (
            "import java.io.Serial;\n\npublic final class FlowableMaterialize",
            "import java.io.Serial;\n\n"
            "/**\n"
            " * 将上游 onNext/onError/onComplete 包装为 {@link Notification} 向下游发射。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableMaterialize",
        ),
        (
            "    public FlowableMaterialize(Flowable<T> source) {",
            "    /** @param source 上游 Flowable */\n"
            "    public FlowableMaterialize(Flowable<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super Notification<T>> s) {",
            "    /** 订阅上游并将每个事件物化为 Notification。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super Notification<T>> s) {",
        ),
        (
            "    static final class MaterializeSubscriber<T> extends SinglePostCompleteSubscriber<T, Notification<T>> {",
            "    /** 将三类事件转换为对应 Notification 的 subscriber。 */\n"
            "    static final class MaterializeSubscriber<T> extends SinglePostCompleteSubscriber<T, Notification<T>> {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 发射 {@link Notification#createOnNext}。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onError(Throwable t) {",
            "        /** 以 {@link Notification#createOnError} 完成序列。 */\n"
            "        @Override\n        public void onError(Throwable t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 以 {@link Notification#createOnComplete} 完成序列。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
        (
            "        @Override\n        protected void onDrop(Notification<T> n) {",
            "        /** 被丢弃的 onError Notification 通过 {@link RxJavaPlugins#onError} 上报。 */\n"
            "        @Override\n        protected void onDrop(Notification<T> n) {",
        ),
    ],
    "FlowableMergeWithCompletable.java": [
        (
            "/**\n * Merges a Flowable and a Completable by emitting the items of the Flowable and waiting until\n * both the Flowable and Completable complete normally.\n * <p>History: 2.1.10 - experimental\n * @param <T> the element type of the Flowable\n * @since 2.2\n */",
            "/**\n"
            " * 合并 {@link Flowable} 与 {@link Completable}：发射 Flowable 元素，\n"
            " * 并等待两者均正常完成后才向下游 onComplete。\n"
            " * <p>History: 2.1.10 - experimental\n"
            " * @param <T> Flowable 元素类型\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    public FlowableMergeWithCompletable(Flowable<T> source, CompletableSource other) {",
            "    /**\n"
            "     * @param source 主 Flowable 源\n"
            "     * @param other 并行的 Completable 源\n"
            "     */\n"
            "    public FlowableMergeWithCompletable(Flowable<T> source, CompletableSource other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> subscriber) {",
            "    /** 同时订阅主源与 Completable，二者均完成时才结束。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> subscriber) {",
        ),
        (
            "    static final class MergeWithSubscriber<T> extends AtomicInteger\n    implements FlowableSubscriber<T>, Subscription {",
            "    /** 协调主 Flowable 与 Completable 完成时机的 subscriber。 */\n"
            "    static final class MergeWithSubscriber<T> extends AtomicInteger\n    implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 主源完成；若 Completable 已完成则通知下游 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
        (
            "        void otherComplete() {",
            "        /** Completable 完成；若主源已完成则通知下游 onComplete。 */\n"
            "        void otherComplete() {",
        ),
        (
            "        static final class OtherObserver extends AtomicReference<Disposable>\n        implements CompletableObserver {",
            "    /** 监听 Completable 终止事件的内部 observer。 */\n"
            "        static final class OtherObserver extends AtomicReference<Disposable>\n        implements CompletableObserver {",
        ),
    ],
    "FlowableMergeWithMaybe.java": [
        (
            "/**\n * Merges an Observable and a Maybe by emitting the items of the Observable and the success\n * value of the Maybe and waiting until both the Observable and Maybe terminate normally.\n * <p>History: 2.1.10 - experimental\n * @param <T> the element type of the Observable\n * @since 2.2\n */",
            "/**\n"
            " * 合并 {@link Flowable} 与 {@link Maybe}：发射 Flowable 元素及 Maybe 的成功值，\n"
            " * 并等待两者均正常终止后才向下游 onComplete。\n"
            " * <p>History: 2.1.10 - experimental\n"
            " * @param <T> 元素类型\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    public FlowableMergeWithMaybe(Flowable<T> source, MaybeSource<? extends T> other) {",
            "    /**\n"
            "     * @param source 主 Flowable 源\n"
            "     * @param other 并行的 Maybe 源\n"
            "     */\n"
            "    public FlowableMergeWithMaybe(Flowable<T> source, MaybeSource<? extends T> other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> subscriber) {",
            "    /** 同时订阅主源与 Maybe，协调元素与终止事件。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> subscriber) {",
        ),
        (
            "    static final class MergeWithObserver<T> extends AtomicInteger\n    implements FlowableSubscriber<T>, Subscription {",
            "    /** 合并主 Flowable 与 Maybe 成功值的协调 subscriber。 */\n"
            "    static final class MergeWithObserver<T> extends AtomicInteger\n    implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        void otherSuccess(T value) {",
            "        /** Maybe 成功时发射其值或暂存待 drain 转发。 */\n"
            "        void otherSuccess(T value) {",
        ),
        (
            "        void drainLoop() {",
            "        /** 按 request 从队列与 Maybe 值中向下游 drain 元素。 */\n"
            "        void drainLoop() {",
        ),
        (
            "        static final class OtherObserver<T> extends AtomicReference<Disposable>\n        implements MaybeObserver<T> {",
            "    /** 监听 Maybe 终止与成功值的内部 observer。 */\n"
            "        static final class OtherObserver<T> extends AtomicReference<Disposable>\n        implements MaybeObserver<T> {",
        ),
    ],
    "FlowableMergeWithSingle.java": [
        (
            "/**\n * Merges an Observable and a Maybe by emitting the items of the Observable and the success\n * value of the Maybe and waiting until both the Observable and Maybe terminate normally.\n * <p>History: 2.1.10 - experimental\n * @param <T> the element type of the Observable\n * @since 2.2\n */",
            "/**\n"
            " * 合并 {@link Flowable} 与 {@link Single}：发射 Flowable 元素及 Single 的成功值，\n"
            " * 并等待主源正常终止后才向下游 onComplete（Single 无 onComplete）。\n"
            " * <p>History: 2.1.10 - experimental\n"
            " * @param <T> 元素类型\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    public FlowableMergeWithSingle(Flowable<T> source, SingleSource<? extends T> other) {",
            "    /**\n"
            "     * @param source 主 Flowable 源\n"
            "     * @param other 并行的 Single 源\n"
            "     */\n"
            "    public FlowableMergeWithSingle(Flowable<T> source, SingleSource<? extends T> other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> subscriber) {",
            "    /** 同时订阅主源与 Single，协调元素与终止事件。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> subscriber) {",
        ),
        (
            "    static final class MergeWithObserver<T> extends AtomicInteger\n    implements FlowableSubscriber<T>, Subscription {",
            "    /** 合并主 Flowable 与 Single 成功值的协调 subscriber。 */\n"
            "    static final class MergeWithObserver<T> extends AtomicInteger\n    implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        void otherSuccess(T value) {",
            "        /** Single 成功时发射其值或暂存待 drain 转发。 */\n"
            "        void otherSuccess(T value) {",
        ),
        (
            "        void drainLoop() {",
            "        /** 按 request 从队列与 Single 值中向下游 drain 元素。 */\n"
            "        void drainLoop() {",
        ),
        (
            "        static final class OtherObserver<T> extends AtomicReference<Disposable>\n        implements SingleObserver<T> {",
            "    /** 监听 Single 成功与错误的内部 observer。 */\n"
            "        static final class OtherObserver<T> extends AtomicReference<Disposable>\n        implements SingleObserver<T> {",
        ),
    ],
    "FlowableNever.java": [
        (
            "import io.reactivex.rxjava4.internal.subscriptions.EmptySubscription;\n\npublic final class FlowableNever",
            "import io.reactivex.rxjava4.internal.subscriptions.EmptySubscription;\n\n"
            "/**\n"
            " * 仅发出 onSubscribe、永不 onNext/onError/onComplete 的 {@link Flowable} 源。\n"
            " */\n"
            "public final class FlowableNever",
        ),
        (
            "    private FlowableNever() {",
            "    /** 单例构造，禁止外部实例化。 */\n"
            "    private FlowableNever() {",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super Object> s) {",
            "    /** 向下游传递 {@link EmptySubscription#INSTANCE}，永不终止。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super Object> s) {",
        ),
    ],
    "FlowableOnBackpressureBuffer.java": [
        (
            "import io.reactivex.rxjava4.operators.*;\n\npublic final class FlowableOnBackpressureBuffer",
            "import io.reactivex.rxjava4.operators.*;\n\n"
            "/**\n"
            " * 以有界或无界队列缓冲上游过量元素，按下游 request 逐批 drain。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableOnBackpressureBuffer",
        ),
        (
            "    public FlowableOnBackpressureBuffer(Flowable<T> source, int bufferSize, boolean unbounded,\n            boolean delayError, Consumer<? super T> onDropped) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param bufferSize 缓冲区容量\n"
            "     * @param unbounded true 时使用可扩容无界队列\n"
            "     * @param delayError true 时先 drain 队列再转发错误\n"
            "     * @param onDropped 缓冲区满时被丢弃元素的回调\n"
            "     */\n"
            "    public FlowableOnBackpressureBuffer(Flowable<T> source, int bufferSize, boolean unbounded,\n            boolean delayError, Consumer<? super T> onDropped) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 订阅上游并以 BackpressureBufferSubscriber 缓冲过量元素。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class BackpressureBufferSubscriber<T> extends BasicIntQueueSubscription<T> implements FlowableSubscriber<T> {",
            "    /** 有界/无界 SPSC 队列缓冲并按 request drain 的 subscriber。 */\n"
            "    static final class BackpressureBufferSubscriber<T> extends BasicIntQueueSubscription<T> implements FlowableSubscriber<T> {",
        ),
        (
            "        @Override\n        public void onSubscribe(Subscription s) {",
            "        /** 向上游请求 Long.MAX_VALUE 以接收全部元素。 */\n"
            "        @Override\n        public void onSubscribe(Subscription s) {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 入队；队列满时 cancel 上游并触发 MissingBackpressureException。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        void drain() {",
            "        /** 按 requested 从队列 poll 并向下游 onNext。 */\n"
            "        void drain() {",
        ),
        (
            "        boolean checkTerminated(boolean d, boolean empty, Subscriber<? super T> a) {",
            "        /** 根据 cancelled/done/delayError 决定终止或继续 drain。 */\n"
            "        boolean checkTerminated(boolean d, boolean empty, Subscriber<? super T> a) {",
        ),
    ],
    "FlowableOnBackpressureBufferStrategy.java": [
        (
            "/**\n * Handle backpressure with a bounded buffer and custom strategy.\n *\n * @param <T> the input and output value type\n */",
            "/**\n"
            " * 以有界双端队列缓冲上游元素，并在溢出时按 {@link BackpressureOverflowStrategy} 处理。\n"
            " *\n"
            " * @param <T> 输入与输出值类型\n"
            " */",
        ),
        (
            "    public FlowableOnBackpressureBufferStrategy(Flowable<T> source,\n            long bufferSize,\n            BackpressureOverflowStrategy strategy,\n            Consumer<? super T> onDropped) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param bufferSize 缓冲区最大容量\n"
            "     * @param strategy 溢出策略（丢弃最新/最旧或报错）\n"
            "     * @param onDropped 被丢弃元素的回调\n"
            "     */\n"
            "    public FlowableOnBackpressureBufferStrategy(Flowable<T> source,\n            long bufferSize,\n            BackpressureOverflowStrategy strategy,\n            Consumer<? super T> onDropped) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 订阅上游并按策略缓冲过量元素。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class OnBackpressureBufferStrategySubscriber<T>\n    extends AtomicInteger\n    implements FlowableSubscriber<T>, Subscription {",
            "    /** 按策略维护 Deque 并按 request drain 的 subscriber。 */\n"
            "    static final class OnBackpressureBufferStrategySubscriber<T>\n    extends AtomicInteger\n    implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "                   default:\n                       // signal error",
            "                   default:\n                       // 溢出时报 MissingBackpressureException",
        ),
        (
            "        void drain() {",
            "        /** 同步 poll Deque 并按 requested 向下游发射。 */\n"
            "        void drain() {",
        ),
    ],
    "FlowableOnBackpressureDrop.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableOnBackpressureDrop",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 当下游无 demand 时丢弃上游元素；可向 {@link Consumer} 通知被丢弃项。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableOnBackpressureDrop",
        ),
        (
            "    public FlowableOnBackpressureDrop(Flowable<T> source) {",
            "    /** @param source 上游 Flowable（丢弃时不回调） */\n"
            "    public FlowableOnBackpressureDrop(Flowable<T> source) {",
        ),
        (
            "    public FlowableOnBackpressureDrop(Flowable<T> source, Consumer<? super T> onDrop) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param onDrop 元素被丢弃时的回调\n"
            "     */\n"
            "    public FlowableOnBackpressureDrop(Flowable<T> source, Consumer<? super T> onDrop) {",
        ),
        (
            "    @Override\n    public void accept(T t) {",
            "    /** 默认 onDrop 实现：静默忽略被丢弃元素。 */\n"
            "    @Override\n    public void accept(T t) {",
        ),
        (
            "        // deliberately ignoring",
            "        // 故意忽略被丢弃的元素",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 订阅上游；无 demand 时丢弃 onNext。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class BackpressureDropSubscriber<T>\n    extends AtomicLong implements FlowableSubscriber<T>, Subscription {",
            "    /** 以 AtomicLong 跟踪 demand，无余量时调用 onDrop。 */\n"
            "    static final class BackpressureDropSubscriber<T>\n    extends AtomicLong implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** demand > 0 时转发，否则调用 onDrop 丢弃。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "FlowableOnBackpressureError.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableOnBackpressureError",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 当下游无 demand 时以 {@link MissingBackpressureException} 终止序列。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableOnBackpressureError",
        ),
        (
            "    public FlowableOnBackpressureError(Flowable<T> source) {",
            "    /** @param source 上游 Flowable */\n"
            "    public FlowableOnBackpressureError(Flowable<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 订阅上游；无 demand 时 cancel 并报错。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class BackpressureErrorSubscriber<T>\n            extends AtomicLong implements FlowableSubscriber<T>, Subscription {",
            "    /** 无 demand 时触发 MissingBackpressureException 的 subscriber。 */\n"
            "    static final class BackpressureErrorSubscriber<T>\n            extends AtomicLong implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** demand > 0 时转发，否则 cancel 上游并 onError。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "FlowableOnBackpressureLatest.java": [
        (
            "import static java.util.concurrent.Flow.*;\n\npublic final class FlowableOnBackpressureLatest",
            "import static java.util.concurrent.Flow.*;\n\n"
            "/**\n"
            " * 当下游无 demand 时仅保留最新上游值，丢弃较早未消费的元素。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableOnBackpressureLatest",
        ),
        (
            "    public FlowableOnBackpressureLatest(Flowable<T> source, Consumer<? super T> onDropped) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param onDropped 被覆盖的旧值回调（可为 null）\n"
            "     */\n"
            "    public FlowableOnBackpressureLatest(Flowable<T> source, Consumer<? super T> onDropped) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 订阅上游并以 latest 策略节流背压。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class BackpressureLatestSubscriber<T> extends AbstractBackpressureThrottlingSubscriber<T, T> {",
            "    /** 用 AtomicReference 保存最新值并在有 demand 时 drain。 */\n"
            "    static final class BackpressureLatestSubscriber<T> extends AbstractBackpressureThrottlingSubscriber<T, T> {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 替换 current 中的旧值（可选 onDropped）并触发 drain。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "FlowableOnBackpressureReduce.java": [
        (
            "import java.util.Objects;\n\npublic final class FlowableOnBackpressureReduce",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 当下游无 demand 时用 {@link BiFunction} 将积压元素归约为单一值。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableOnBackpressureReduce",
        ),
        (
            "    public FlowableOnBackpressureReduce(@NonNull Flowable<T> source, @NonNull  BiFunction<T, T, T> reducer) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param reducer 两元素归约函数（返回值不可为 null）\n"
            "     */\n"
            "    public FlowableOnBackpressureReduce(@NonNull Flowable<T> source, @NonNull  BiFunction<T, T, T> reducer) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(@NonNull Subscriber<? super T> s) {",
            "    /** 订阅上游并以 reduce 策略节流背压。 */\n"
            "    @Override\n    protected void subscribeActual(@NonNull Subscriber<? super T> s) {",
        ),
        (
            "    static final class BackpressureReduceSubscriber<T> extends AbstractBackpressureThrottlingSubscriber<T, T> {",
            "    /** 用 reducer 合并 current 与新元素的 subscriber。 */\n"
            "    static final class BackpressureReduceSubscriber<T> extends AbstractBackpressureThrottlingSubscriber<T, T> {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 将 t 与 current 归约后写入 current 并 drain。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "FlowableOnBackpressureReduceWith.java": [
        (
            "import java.util.Objects;\n\npublic final class FlowableOnBackpressureReduceWith",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 当下游无 demand 时以 {@link Supplier} 种子值启动，\n"
            " * 用 {@link BiFunction} 将积压元素归约为类型 {@code R} 的单一值。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> 归约结果类型\n"
            " */\n"
            "public final class FlowableOnBackpressureReduceWith",
        ),
        (
            "    public FlowableOnBackpressureReduceWith(@NonNull Flowable<T> source,\n                                            @NonNull Supplier<R> supplier,\n                                            @NonNull BiFunction<R, ? super T, R> reducer) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param supplier 首次归约的种子值 Supplier\n"
            "     * @param reducer 累加归约函数（返回值不可为 null）\n"
            "     */\n"
            "    public FlowableOnBackpressureReduceWith(@NonNull Flowable<T> source,\n                                            @NonNull Supplier<R> supplier,\n                                            @NonNull BiFunction<R, ? super T, R> reducer) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(@NonNull Subscriber<? super R> s) {",
            "    /** 订阅上游并以 reduceWith 策略节流背压。 */\n"
            "    @Override\n    protected void subscribeActual(@NonNull Subscriber<? super R> s) {",
        ),
        (
            "    static final class BackpressureReduceWithSubscriber<T, R> extends AbstractBackpressureThrottlingSubscriber<T, R> {",
            "    /** 以 supplier 种子启动 reducer 累加的 subscriber。 */\n"
            "    static final class BackpressureReduceWithSubscriber<T, R> extends AbstractBackpressureThrottlingSubscriber<T, R> {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 用 supplier/reducer 更新 current 并 drain。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "FlowableOnErrorComplete.java": [
        (
            "/**\n * Emits an onComplete if the source emits an onError and the predicate returns true for\n * that Throwable.\n * \n * @param <T> the value type\n * @since 3.0.0\n */",
            "/**\n"
            " * 若上游 onError 且 {@link Predicate} 对该 {@link Throwable} 返回 true，\n"
            " * 则转为向下游发射 onComplete 而非 onError。\n"
            " * \n"
            " * @param <T> 值类型\n"
            " * @since 3.0.0\n"
            " */",
        ),
        (
            "    public FlowableOnErrorComplete(Flowable<T> source,\n            Predicate<? super Throwable> predicate) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param predicate 返回 true 时将错误转为完成\n"
            "     */\n"
            "    public FlowableOnErrorComplete(Flowable<T> source,\n            Predicate<? super Throwable> predicate) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> observer) {",
            "    /** 订阅上游并按 predicate 过滤错误。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> observer) {",
        ),
        (
            "    public static final class OnErrorCompleteSubscriber<T>\n    implements FlowableSubscriber<T>, Subscription {",
            "    /** 根据 predicate 决定转发错误或转为 onComplete 的 subscriber。 */\n"
            "    public static final class OnErrorCompleteSubscriber<T>\n    implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** predicate 为 true 时 onComplete，否则转发 onError。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
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
            "wave11a Flowable* [0:15]",
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
