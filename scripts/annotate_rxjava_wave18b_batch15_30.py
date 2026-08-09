#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-18b observable operators [15:30]."""
from __future__ import annotations

import json
import os
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
SCRIPTS = ROOT / "scripts"
WAVE18B_FILE = Path("/tmp/rxjava_w18b.txt")
SCRIPT_NAME = "annotate_rxjava_wave18b_batch15_30.py"
BATCH_FILES = [
    ln.strip()
    for ln in WAVE18B_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

GUARD_FILES = [
    VER
    / "analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/observable/ObservableCreate.java",
    VER
    / "analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/observable/ObservableAny.java",
    VER
    / "analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/mixed/FlowableConcatMapCompletable.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ObservableFromIterable.java": [
        (
            "import io.reactivex.rxjava4.internal.observers.BasicQueueDisposable;\n\npublic final class ObservableFromIterable<T> extends Observable<T> {",
            "import io.reactivex.rxjava4.internal.observers.BasicQueueDisposable;\n\n"
            "/**\n"
            " * 将 {@link Iterable} 转为 Observable：订阅时迭代元素并逐次 onNext，\n"
            " * 支持 queue fusion 的 SYNC 模式。\n"
            " *\n * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableFromIterable<T> extends Observable<T> {",
        ),
        (
            "    public ObservableFromIterable(Iterable<? extends T> source) {",
            "    /** @param source 提供迭代器的 Iterable */\n"
            "    public ObservableFromIterable(Iterable<? extends T> source) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
            "    /** 获取 iterator，空序列 onComplete，否则创建 FromIterableDisposable 并 run。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class FromIterableDisposable<T> extends BasicQueueDisposable<T> {",
            "    /** 同步迭代 Iterable 或作为 fusion poll 源。 */\n"
            "    static final class FromIterableDisposable<T> extends BasicQueueDisposable<T> {",
        ),
        (
            "        void run() {",
            "        /** 循环 it.next() 转发至 downstream，耗尽后 onComplete。 */\n"
            "        void run() {",
        ),
        (
            "        @Override\n        public int requestFusion(int mode) {",
            "        /** 请求 SYNC 时启用 fusionMode 并返回 SYNC。 */\n"
            "        @Override\n        public int requestFusion(int mode) {",
        ),
        (
            "        @Nullable\n        @Override\n        public T poll() {",
            "        /** fusion 路径：poll 下一元素，耗尽返回 null 并置 done。 */\n"
            "        @Nullable\n        @Override\n        public T poll() {",
        ),
    ],
    "ObservableFromPublisher.java": [
        (
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;\n\npublic final class ObservableFromPublisher<T> extends Observable<T> {",
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;\n\n"
            "/**\n"
            " * 将 Reactive Streams {@link Publisher} 适配为 Observable：\n"
            " * 订阅后 request(Long.MAX_VALUE) 并无背压地转发信号。\n"
            " *\n * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableFromPublisher<T> extends Observable<T> {",
        ),
        (
            "    public ObservableFromPublisher(Publisher<? extends T> publisher) {",
            "    /** @param publisher 上游 Publisher */\n"
            "    public ObservableFromPublisher(Publisher<? extends T> publisher) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final Observer<? super T> o) {",
            "    /** 订阅 PublisherSubscriber 桥接 Publisher 与 Observer。 */\n"
            "    @Override\n    protected void subscribeActual(final Observer<? super T> o) {",
        ),
        (
            "    static final class PublisherSubscriber<T>\n    implements FlowableSubscriber<T>, Disposable {",
            "    /** 桥接 Subscription 与 Disposable，无背压转发 Publisher 事件。 */\n"
            "    static final class PublisherSubscriber<T>\n    implements FlowableSubscriber<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSubscribe(Subscription s) {",
            "        /** 校验 Subscription 后 request(MAX) 并将自身作为 Disposable 下发。 */\n"
            "        @Override\n        public void onSubscribe(Subscription s) {",
        ),
        (
            "        @Override\n        public void dispose() {",
            "        /** cancel 上游 Subscription 并标记 CANCELLED。 */\n"
            "        @Override\n        public void dispose() {",
        ),
    ],
    "ObservableFromRunnable.java": [
        (
            "/**\n * Executes an {@link Runnable} and signals its exception or completes normally.\n *\n * @param <T> the value type\n * @since 3.0.0\n */",
            "/**\n"
            " * 执行 {@link Runnable}：成功则 onComplete，异常则 onError；不发射 onNext。\n"
            " * 同时实现 {@link Supplier} 供标量融合路径使用。\n"
            " *\n * @param <T> 元素类型（本算子不发射元素）\n"
            " * @since 3.0.0\n"
            " */",
        ),
        (
            "    public ObservableFromRunnable(Runnable run) {",
            "    /** @param run 订阅时执行的 Runnable */\n"
            "    public ObservableFromRunnable(Runnable run) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 经 CancellableQueueFuseable 执行 run，异常或正常完成对应终止信号。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    @Override\n    public T get() throws Throwable {",
            "    /** Supplier 路径：执行 run 并返回 null（视为 onComplete）。 */\n"
            "    @Override\n    public T get() throws Throwable {",
        ),
        (
            "        return null; // considered as onComplete()",
            "        return null; // 视为 onComplete()",
        ),
    ],
    "ObservableFromSupplier.java": [
        (
            "/**\n * Calls a Supplier and emits its resulting single value or signals its exception.\n * @param <T> the value type\n * @since 3.0.0\n */",
            "/**\n"
            " * 调用 {@link Supplier} 并发射其返回的单个值，或转发异常。\n"
            " * 同时实现 {@link Supplier} 供标量融合路径使用。\n"
            " * @param <T> 元素类型\n"
            " * @since 3.0.0\n"
            " */",
        ),
        (
            "    public ObservableFromSupplier(Supplier<? extends T> supplier) {",
            "    /** @param supplier 提供单个元素的 Supplier */\n"
            "    public ObservableFromSupplier(Supplier<? extends T> supplier) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
            "    /** 经 DeferredScalarDisposable 调用 supplier.get() 并 complete 单值。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    @Override\n    public T get() throws Throwable {",
            "    /** Supplier 路径：直接返回 supplier.get()（null 检查）。 */\n"
            "    @Override\n    public T get() throws Throwable {",
        ),
    ],
    "ObservableFromUnsafeSource.java": [
        (
            "import io.reactivex.rxjava4.core.*;\n\npublic final class ObservableFromUnsafeSource<T> extends Observable<T> {",
            "import io.reactivex.rxjava4.core.*;\n\n"
            "/**\n"
            " * 不包装下游 Observer 的直接委托：将 subscribe 原样转发至上游 ObservableSource。\n"
            " * 用于已知安全的内部路径，避免额外 Disposable 层。\n"
            " *\n * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableFromUnsafeSource<T> extends Observable<T> {",
        ),
        (
            "    public ObservableFromUnsafeSource(ObservableSource<T> source) {",
            "    /** @param source 上游 ObservableSource */\n"
            "    public ObservableFromUnsafeSource(ObservableSource<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 直接将 observer 传给 source.subscribe。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
    ],
    "ObservableGenerate.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableGenerate<T, S> extends Observable<T> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 有状态生成器：初始状态由 stateSupplier 提供，\n"
            " * generator 在每轮通过 {@link Emitter} 推送元素并返回下一状态；\n"
            " * disposeState 在终止时清理状态。\n"
            " *\n * @param <T> 生成元素类型\n"
            " * @param <S> 状态类型\n"
            " */\n"
            "public final class ObservableGenerate<T, S> extends Observable<T> {",
        ),
        (
            "    public ObservableGenerate(Supplier<S> stateSupplier, BiFunction<S, Emitter<T>, S> generator,\n            Consumer<? super S> disposeState) {",
            "    /**\n"
            "     * @param stateSupplier 提供初始状态的 Supplier\n"
            "     * @param generator 每轮 (state, emitter) -> 下一 state 的 BiFunction\n"
            "     * @param disposeState 终止时清理状态的 Consumer\n"
            "     */\n"
            "    public ObservableGenerate(Supplier<S> stateSupplier, BiFunction<S, Emitter<T>, S> generator,\n            Consumer<? super S> disposeState) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
            "    /** 创建 GeneratorDisposable 并启动 run 循环。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class GeneratorDisposable<T, S>\n    implements Emitter<T>, Disposable {",
            "    /** 实现 Emitter 与 Disposable，驱动 generator 状态机循环。 */\n"
            "    static final class GeneratorDisposable<T, S>\n    implements Emitter<T>, Disposable {",
        ),
        (
            "        public void run() {",
            "        /** 循环调用 generator；terminate 或 cancel 时 dispose 状态并退出。 */\n"
            "        public void run() {",
        ),
        (
            "        private void dispose(S s) {",
            "        /** 调用 disposeState.accept 清理状态（异常经 RxJavaPlugins）。 */\n"
            "        private void dispose(S s) {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 每轮至多一次 onNext；null 或重复调用转 onError。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onError(Throwable t) {",
            "        /** 终止序列并向下游转发 onError（已 terminate 则 RxJavaPlugins）。 */\n"
            "        @Override\n        public void onError(Throwable t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 标记 terminate 并向下游 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "ObservableGroupBy.java": [
        (
            "import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;\n\npublic final class ObservableGroupBy<T, K, V> extends AbstractObservableWithUpstream<T, GroupedObservable<K, V>> {",
            "import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;\n\n"
            "/**\n"
            " * 按 keySelector 分组：为每个键创建 {@link GroupedObservable}，\n"
            " * valueSelector 映射组内元素；无订阅者的组可被 tryAbandon 回收。\n"
            " *\n * @param <T> 上游元素类型\n"
            " * @param <K> 分组键类型\n"
            " * @param <V> 组内元素类型\n"
            " */\n"
            "public final class ObservableGroupBy<T, K, V> extends AbstractObservableWithUpstream<T, GroupedObservable<K, V>> {",
        ),
        (
            "    public ObservableGroupBy(ObservableSource<T> source,\n            Function<? super T, ? extends K> keySelector, Function<? super T, ? extends V> valueSelector,\n            int bufferSize, boolean delayError) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param keySelector 提取分组键的函数\n"
            "     * @param valueSelector 映射组内值的函数\n"
            "     * @param bufferSize 每组 SPSC 队列容量\n"
            "     * @param delayError 组内是否延迟错误至队列排空\n"
            "     */\n"
            "    public ObservableGroupBy(ObservableSource<T> source,\n            Function<? super T, ? extends K> keySelector, Function<? super T, ? extends V> valueSelector,\n            int bufferSize, boolean delayError) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super GroupedObservable<K, V>> t) {",
            "    /** 订阅 GroupByObserver 管理分组 Map 与各 GroupedUnicast。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super GroupedObservable<K, V>> t) {",
        ),
        (
            "    public static final class GroupByObserver<T, K, V> extends AtomicInteger implements Observer<T>, Disposable {",
            "    /** 维护 groups Map，创建/完成 GroupedUnicast 并协调上游 dispose。 */\n"
            "    public static final class GroupByObserver<T, K, V> extends AtomicInteger implements Observer<T>, Disposable {",
        ),
        (
            "                // if the main has been cancelled, stop creating groups\n                // and skip this value",
            "                // 主流已 cancel 则不再创建新组并跳过该值",
        ),
        (
            "            // canceling the main source means we don't want any more groups\n            // but running groups still require new values",
            "            // cancel 主流表示不再接受新组，但已有组仍可接收元素",
        ),
        (
            "        public void cancel(K key) {",
            "        /** 从 groups 移除键；引用计数归零时 dispose 上游。 */\n"
            "        public void cancel(K key) {",
        ),
        (
            "    static final class GroupedUnicast<K, T> extends GroupedObservable<K, T> {",
            "    /** 单键分组 Observable，委托 State 管理队列与 drain。 */\n"
            "    static final class GroupedUnicast<K, T> extends GroupedObservable<K, T> {",
        ),
        (
            "        static final int FRESH = 0;\n        static final int HAS_SUBSCRIBER = 1;\n        static final int ABANDONED = 2;\n        static final int ABANDONED_HAS_SUBSCRIBER = ABANDONED | HAS_SUBSCRIBER;",
            "        /** 尚无订阅者。 */\n        static final int FRESH = 0;\n        /** 已有订阅者。 */\n        static final int HAS_SUBSCRIBER = 1;\n        /** 已放弃（无订阅者时被回收）。 */\n        static final int ABANDONED = 2;\n        /** 已放弃且曾有订阅者。 */\n        static final int ABANDONED_HAS_SUBSCRIBER = ABANDONED | HAS_SUBSCRIBER;",
        ),
        (
            "        void drain() {",
            "        /** 从 queue poll 并转发至 actual observer，处理终止与错误。 */\n"
            "        void drain() {",
        ),
        (
            "        boolean tryAbandon() {",
            "        /** FRESH 时 CAS 为 ABANDONED，表示无订阅者的组可立即完成。 */\n"
            "        boolean tryAbandon() {",
        ),
    ],
    "ObservableHide.java": [
        (
            "/**\n * Hides the identity of the wrapped ObservableSource and its Disposable.\n * @param <T> the value type\n *\n * @since 2.0\n */",
            "/**\n"
            " * 隐藏被包装 ObservableSource 及其 Disposable 的身份：\n"
            " * 下游 onSubscribe 收到 HideDisposable 而非上游 Disposable。\n"
            " * @param <T> 元素类型\n"
            " * @since 2.0\n"
            " */",
        ),
        (
            "    public ObservableHide(ObservableSource<T> source) {",
            "    /** @param source 上游 ObservableSource */\n"
            "    public ObservableHide(ObservableSource<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> o) {",
            "    /** 订阅 HideDisposable 转发信号并屏蔽上游 Disposable 类型。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> o) {",
        ),
        (
            "    static final class HideDisposable<T> implements Observer<T>, Disposable {",
            "    /** 透明转发 onNext/onError/onComplete，dispose 委托 upstream。 */\n"
            "    static final class HideDisposable<T> implements Observer<T>, Disposable {",
        ),
    ],
    "ObservableIgnoreElements.java": [
        (
            "import io.reactivex.rxjava4.disposables.Disposable;\n\npublic final class ObservableIgnoreElements<T> extends AbstractObservableWithUpstream<T, T> {",
            "import io.reactivex.rxjava4.disposables.Disposable;\n\n"
            "/**\n"
            " * 忽略所有 onNext，仅转发 onError/onComplete；\n"
            " * 下游类型仍为 T 但通常不收到元素。\n"
            " *\n * @param <T> 上游元素类型\n"
            " */\n"
            "public final class ObservableIgnoreElements<T> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    public ObservableIgnoreElements(ObservableSource<T> source) {",
            "    /** @param source 上游 ObservableSource */\n"
            "    public ObservableIgnoreElements(ObservableSource<T> source) {",
        ),
        (
            "    @Override\n    public void subscribeActual(final Observer<? super T> t) {",
            "    /** 订阅 IgnoreObservable 丢弃 onNext。 */\n"
            "    @Override\n    public void subscribeActual(final Observer<? super T> t) {",
        ),
        (
            "    static final class IgnoreObservable<T> implements Observer<T>, Disposable {",
            "    /** 丢弃 onNext，转发终止信号。 */\n"
            "    static final class IgnoreObservable<T> implements Observer<T>, Disposable {",
        ),
        (
            "            // deliberately ignored",
            "            // 有意忽略 onNext",
        ),
    ],
    "ObservableIgnoreElementsCompletable.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableIgnoreElementsCompletable<T> extends Completable implements FuseToObservable<T> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * {@link ObservableIgnoreElements} 的 Completable 变体：\n"
            " * 忽略所有 onNext，仅关心 onError/onComplete 终止信号。\n"
            " *\n * @param <T> 上游元素类型\n"
            " */\n"
            "public final class ObservableIgnoreElementsCompletable<T> extends Completable implements FuseToObservable<T> {",
        ),
        (
            "    public ObservableIgnoreElementsCompletable(ObservableSource<T> source) {",
            "    /** @param source 上游 ObservableSource */\n"
            "    public ObservableIgnoreElementsCompletable(ObservableSource<T> source) {",
        ),
        (
            "    @Override\n    public void subscribeActual(final CompletableObserver t) {",
            "    /** 订阅 IgnoreObservable 映射为 Completable 信号。 */\n"
            "    @Override\n    public void subscribeActual(final CompletableObserver t) {",
        ),
        (
            "    @Override\n    public Observable<T> fuseToObservable() {",
            "    /** 融合为 {@link ObservableIgnoreElements} 实例。 */\n"
            "    @Override\n    public Observable<T> fuseToObservable() {",
        ),
        (
            "    static final class IgnoreObservable<T> implements Observer<T>, Disposable {",
            "    /** 丢弃 onNext，向 CompletableObserver 转发终止信号。 */\n"
            "    static final class IgnoreObservable<T> implements Observer<T>, Disposable {",
        ),
        (
            "            // deliberately ignored",
            "            // 有意忽略 onNext",
        ),
    ],
    "ObservableInternalHelper.java": [
        (
            "/**\n * Helper utility class to support Observable with inner classes.\n */",
            "/**\n * Observable 内部类与 lambda 适配的辅助工具类（record/Function 工厂）。\n"
            " */",
        ),
        (
            "    public static <T, S> BiFunction<S, Emitter<T>, S> simpleGenerator(Consumer<Emitter<T>> consumer) {",
            "    /** 将 Consumer&lt;Emitter&gt; 包装为 generate 用的 BiFunction（状态不变）。 */\n"
            "    public static <T, S> BiFunction<S, Emitter<T>, S> simpleGenerator(Consumer<Emitter<T>> consumer) {",
        ),
        (
            "    public static <T, S> BiFunction<S, Emitter<T>, S> simpleBiGenerator(BiConsumer<S, Emitter<T>> consumer) {",
            "    /** 将 BiConsumer&lt;S, Emitter&gt; 包装为 generate 用的 BiFunction。 */\n"
            "    public static <T, S> BiFunction<S, Emitter<T>, S> simpleBiGenerator(BiConsumer<S, Emitter<T>> consumer) {",
        ),
        (
            "    public static <T, U> Function<T, ObservableSource<T>> itemDelay(final Function<? super T, ? extends ObservableSource<U>> itemDelay) {",
            "    /** 为每个元素构造 delay Observable，take(1) 后再 defaultIfEmpty 原值。 */\n"
            "    public static <T, U> Function<T, ObservableSource<T>> itemDelay(final Function<? super T, ? extends ObservableSource<U>> itemDelay) {",
        ),
        (
            "    public static <T> Consumer<T> observerOnNext(Observer<T> observer) {",
            "    /** 返回调用 observer.onNext 的 Consumer。 */\n"
            "    public static <T> Consumer<T> observerOnNext(Observer<T> observer) {",
        ),
        (
            "    public static <T> Consumer<Throwable> observerOnError(Observer<T> observer) {",
            "    /** 返回调用 observer.onError 的 Consumer。 */\n"
            "    public static <T> Consumer<Throwable> observerOnError(Observer<T> observer) {",
        ),
        (
            "    public static <T> Action observerOnComplete(Observer<T> observer) {",
            "    /** 返回调用 observer.onComplete 的 Action。 */\n"
            "    public static <T> Action observerOnComplete(Observer<T> observer) {",
        ),
        (
            "    public static <T, U, R> Function<T, ObservableSource<R>> flatMapWithCombiner(",
            "    /** 将 flatMap 与 combiner 组合为 mapper -> ObservableMap 链。 */\n"
            "    public static <T, U, R> Function<T, ObservableSource<R>> flatMapWithCombiner(",
        ),
        (
            "    public static <T, U> Function<T, ObservableSource<U>> flatMapIntoIterable(final Function<? super T, ? extends Iterable<? extends U>> mapper) {",
            "    /** 将元素映射为 Iterable 并包装为 ObservableFromIterable。 */\n"
            "    public static <T, U> Function<T, ObservableSource<U>> flatMapIntoIterable(final Function<? super T, ? extends Iterable<? extends U>> mapper) {",
        ),
        (
            "    public static <T> Supplier<ConnectableObservable<T>> replaySupplier(final Observable<T> parent) {",
            "    /** 返回调用 parent.replay() 的 Supplier。 */\n"
            "    public static <T> Supplier<ConnectableObservable<T>> replaySupplier(final Observable<T> parent) {",
        ),
        (
            "    public static <T> Supplier<ConnectableObservable<T>> replaySupplier(final Observable<T> parent, final int bufferSize, boolean eagerTruncate) {",
            "    /** 返回调用 parent.replay(bufferSize, eagerTruncate) 的 Supplier。 */\n"
            "    public static <T> Supplier<ConnectableObservable<T>> replaySupplier(final Observable<T> parent, final int bufferSize, boolean eagerTruncate) {",
        ),
        (
            "    public static <T> Supplier<ConnectableObservable<T>> replaySupplier(final Observable<T> parent, final int bufferSize,\n            final long time, final TimeUnit unit, final Scheduler scheduler, boolean eagerTruncate) {",
            "    /** 返回带 buffer 与时间窗口的 replay Supplier。 */\n"
            "    public static <T> Supplier<ConnectableObservable<T>> replaySupplier(final Observable<T> parent, final int bufferSize,\n            final long time, final TimeUnit unit, final Scheduler scheduler, boolean eagerTruncate) {",
        ),
        (
            "    public static <T> Supplier<ConnectableObservable<T>> replaySupplier(final Observable<T> parent, final long time,\n            final TimeUnit unit, final Scheduler scheduler, boolean eagerTruncate) {",
            "    /** 返回仅按时间 replay 的 Supplier。 */\n"
            "    public static <T> Supplier<ConnectableObservable<T>> replaySupplier(final Observable<T> parent, final long time,\n            final TimeUnit unit, final Scheduler scheduler, boolean eagerTruncate) {",
        ),
    ],
    "ObservableInterval.java": [
        (
            "import io.reactivex.rxjava4.internal.schedulers.TrampolineScheduler;\n\npublic final class ObservableInterval extends Observable<Long> {",
            "import io.reactivex.rxjava4.internal.schedulers.TrampolineScheduler;\n\n"
            "/**\n"
            " * 在 {@link Scheduler} 上按固定周期发射递增 Long（0, 1, 2, …）。\n"
            " * TrampolineScheduler 使用 Worker，其余用 schedulePeriodicallyDirect。\n"
            " */\n"
            "public final class ObservableInterval extends Observable<Long> {",
        ),
        (
            "    public ObservableInterval(long initialDelay, long period, TimeUnit unit, Scheduler scheduler) {",
            "    /**\n"
            "     * @param initialDelay 首次发射前的延迟\n"
            "     * @param period 相邻发射间隔\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 调度周期任务的 Scheduler\n"
            "     */\n"
            "    public ObservableInterval(long initialDelay, long period, TimeUnit unit, Scheduler scheduler) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super Long> observer) {",
            "    /** 创建 IntervalObserver 并 schedulePeriodically。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super Long> observer) {",
        ),
        (
            "    static final class IntervalObserver\n    extends AtomicReference<Disposable>\n    implements Disposable, Runnable {",
            "    /** Runnable 每次 tick 发射 count++，dispose 后停止。 */\n"
            "    static final class IntervalObserver\n    extends AtomicReference<Disposable>\n    implements Disposable, Runnable {",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 未 dispose 时向下游 onNext(count++)。 */\n"
            "        @Override\n        public void run() {",
        ),
    ],
    "ObservableIntervalRange.java": [
        (
            "import io.reactivex.rxjava4.internal.schedulers.TrampolineScheduler;\n\npublic final class ObservableIntervalRange extends Observable<Long> {",
            "import io.reactivex.rxjava4.internal.schedulers.TrampolineScheduler;\n\n"
            "/**\n"
            " * 在 Scheduler 上按周期发射 [start, end] 范围内的 Long，\n"
            " * 到达 end 后 onComplete 并 dispose。\n"
            " */\n"
            "public final class ObservableIntervalRange extends Observable<Long> {",
        ),
        (
            "    public ObservableIntervalRange(long start, long end, long initialDelay, long period, TimeUnit unit, Scheduler scheduler) {",
            "    /**\n"
            "     * @param start 首个发射值（含）\n"
            "     * @param end 末个发射值（含），到达后 onComplete\n"
            "     * @param initialDelay 首次发射前的延迟\n"
            "     * @param period 相邻发射间隔\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 调度周期任务的 Scheduler\n"
            "     */\n"
            "    public ObservableIntervalRange(long start, long end, long initialDelay, long period, TimeUnit unit, Scheduler scheduler) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super Long> observer) {",
            "    /** 创建 IntervalRangeObserver 并 schedulePeriodically。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super Long> observer) {",
        ),
        (
            "    static final class IntervalRangeObserver\n    extends AtomicReference<Disposable>\n    implements Disposable, Runnable {",
            "    /** 从 start 递增发射至 end，到达 end 后 onComplete。 */\n"
            "    static final class IntervalRangeObserver\n    extends AtomicReference<Disposable>\n    implements Disposable, Runnable {",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 发射 count；等于 end 时 onComplete 并 dispose。 */\n"
            "        @Override\n        public void run() {",
        ),
    ],
    "ObservableJoin.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableJoin<TLeft, TRight, TLeftEnd, TRightEnd, R> extends AbstractObservableWithUpstream<TLeft, R> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 双流 join：左/右源各元素与对侧缓存做笛卡尔 resultSelector 组合；\n"
            " * leftEnd/rightEnd 映射的窗口 Observable 终止时移除对应缓存项。\n"
            " *\n * @param <TLeft> 左源元素类型\n"
            " * @param <TRight> 右源元素类型\n"
            " * @param <TLeftEnd> 左窗口结束信号类型\n"
            " * @param <TRightEnd> 右窗口结束信号类型\n"
            " * @param <R> 组合结果类型\n"
            " */\n"
            "public final class ObservableJoin<TLeft, TRight, TLeftEnd, TRightEnd, R> extends AbstractObservableWithUpstream<TLeft, R> {",
        ),
        (
            "    public ObservableJoin(\n            ObservableSource<TLeft> source,\n            ObservableSource<? extends TRight> other,\n            Function<? super TLeft, ? extends ObservableSource<TLeftEnd>> leftEnd,\n            Function<? super TRight, ? extends ObservableSource<TRightEnd>> rightEnd,\n            BiFunction<? super TLeft, ? super TRight, ? extends R> resultSelector) {",
            "    /**\n"
            "     * @param source 左源 ObservableSource\n"
            "     * @param other 右源 ObservableSource\n"
            "     * @param leftEnd 左元素 -> 左窗口结束 ObservableSource\n"
            "     * @param rightEnd 右元素 -> 右窗口结束 ObservableSource\n"
            "     * @param resultSelector 左/右元素组合函数\n"
            "     */\n"
            "    public ObservableJoin(\n            ObservableSource<TLeft> source,\n            ObservableSource<? extends TRight> other,\n            Function<? super TLeft, ? extends ObservableSource<TLeftEnd>> leftEnd,\n            Function<? super TRight, ? extends ObservableSource<TRightEnd>> rightEnd,\n            BiFunction<? super TLeft, ? super TRight, ? extends R> resultSelector) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
            "    /** 订阅 JoinDisposable 并同时 subscribe 左/右 LeftRightObserver。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
        ),
        (
            "    static final class JoinDisposable<TLeft, TRight, TLeftEnd, TRightEnd, R>\n    extends AtomicInteger implements Disposable, JoinSupport {",
            "    /** 维护 lefts/rights 缓存、窗口订阅与 drain 队列，驱动 join 组合。 */\n"
            "    static final class JoinDisposable<TLeft, TRight, TLeftEnd, TRightEnd, R>\n    extends AtomicInteger implements Disposable, JoinSupport {",
        ),
        (
            "        static final Integer LEFT_VALUE = 1;\n\n        static final Integer RIGHT_VALUE = 2;\n\n        static final Integer LEFT_CLOSE = 3;\n\n        static final Integer RIGHT_CLOSE = 4;",
            "        /** 队列标记：左源新值。 */\n        static final Integer LEFT_VALUE = 1;\n\n        /** 队列标记：右源新值。 */\n        static final Integer RIGHT_VALUE = 2;\n\n        /** 队列标记：左窗口关闭。 */\n        static final Integer LEFT_CLOSE = 3;\n\n        /** 队列标记：右窗口关闭。 */\n        static final Integer RIGHT_CLOSE = 4;",
        ),
        (
            "        void drain() {",
            "        /** 从 queue 取标记/值，更新缓存并 apply resultSelector 发射组合结果。 */\n"
            "        void drain() {",
        ),
        (
            "        @Override\n        public void innerValue(boolean isLeft, Object o) {",
            "        /** 左/右新值入队并触发 drain。 */\n"
            "        @Override\n        public void innerValue(boolean isLeft, Object o) {",
        ),
        (
            "        @Override\n        public void innerClose(boolean isLeft, LeftRightEndObserver index) {",
            "        /** 窗口结束：从 lefts/rights 移除对应索引并 drain。 */\n"
            "        @Override\n        public void innerClose(boolean isLeft, LeftRightEndObserver index) {",
        ),
    ],
    "ObservableJust.java": [
        (
            "/**\n * Represents a constant scalar value.\n * @param <T> the value type\n */",
            "/**\n"
            " * 发射单个常量标量值的 Observable，实现 {@link ScalarSupplier} 供融合优化。\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public ObservableJust(final T value) {",
            "    /** @param value 唯一发射的元素 */\n"
            "    public ObservableJust(final T value) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 经 ScalarDisposable 立即 onNext(value) 并 onComplete。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    @Override\n    public T get() {",
            "    /** ScalarSupplier：返回常量 value。 */\n"
            "    @Override\n    public T get() {",
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
        if not path.exists():
            raise RuntimeError(f"guard file missing: {path}")
        text = path.read_text(encoding="utf-8")
        if env is not None:
            rel = str(path.relative_to(ROOT))
            try:
                text = subprocess.check_output(
                    ["git", "-C", str(ROOT), "show", f":{rel}"], env=env, text=True
                )
            except subprocess.CalledProcessError:
                pass
        if not has_chinese(text):
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
    index_file = Path("/tmp/git-index-rxjava-w18b")
    env = os.environ.copy()
    env["GIT_INDEX_FILE"] = str(index_file)
    base = subprocess.check_output(
        ["git", "-C", str(ROOT), "rev-parse", base_ref], text=True, env=env
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "read-tree", base], env=env, check=True)
    subprocess.run(["git", "-C", str(ROOT), "add", "--", *paths], env=env, check=True)
    tree_count = tree_guard(env)
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
    return commit, tree_count


def update_batch_json() -> None:
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    pending = [
        ln
        for ln in (QUEUE / "pending.txt").read_text(encoding="utf-8").splitlines()
        if ln.strip()
    ]
    batch["files"] = pending[:15] if pending else []
    batch["done"] = len(
        [
            ln
            for ln in (QUEUE / "done.txt").read_text(encoding="utf-8").splitlines()
            if ln.strip()
        ]
    )
    batch["remaining_pending"] = len(pending)
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


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
        "rxjava 4.0.0-alpha-21: Chinese-annotate wave 18b [15:30]",
        [*analyzed_paths, script_path],
    )
    subprocess.run(["git", "-C", str(ROOT), "push", "-u", "origin", "main"], check=True)

    subprocess.run(
        [
            sys.executable,
            str(SCRIPTS / "mark_batch_done.py"),
            "--project",
            "rxjava",
            "--version",
            "4.0.0-alpha-21",
            "--note",
            "wave18b",
            *BATCH_FILES,
        ],
        check=True,
    )
    update_batch_json()
    queue_paths = [
        "rxjava/4.0.0-alpha-21/_reports/class-queue/done.txt",
        "rxjava/4.0.0-alpha-21/_reports/class-queue/pending.txt",
        "rxjava/4.0.0-alpha-21/_reports/class-queue/batch.json",
        "rxjava/4.0.0-alpha-21/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        "queue: mark rxjava 4.0.0-alpha-21 wave18b done",
        queue_paths,
        base_ref="HEAD",
    )
    subprocess.run(["git", "-C", str(ROOT), "push", "origin", "main"], check=True)

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
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0 if all(chinese.values()) else 1


if __name__ == "__main__":
    raise SystemExit(main())
