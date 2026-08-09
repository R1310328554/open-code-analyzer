#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-17b observable operators [15:30]."""
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
WAVE17B_FILE = Path("/tmp/rxjava_w17b.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE17B_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

GUARD_FILES = [
    VER
    / "analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
    VER
    / "analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/mixed/FlowableConcatMapCompletable.java",
    VER
    / "analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/mixed/ObservableConcatMapMaybe.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ObservableCreate.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableCreate<T> extends Observable<T> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 基于 {@link ObservableOnSubscribe} 回调创建 Observable：\n"
            " * 订阅时向回调提供 {@link ObservableEmitter}，由调用方推送 onNext/onError/onComplete。\n"
            " *\n * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableCreate<T> extends Observable<T> {",
        ),
        (
            "    public ObservableCreate(ObservableOnSubscribe<T> source) {",
            "    /** @param source 订阅时调用的 OnSubscribe 回调 */\n"
            "    public ObservableCreate(ObservableOnSubscribe<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 创建 CreateEmitter 并调用 source.subscribe，异常经 emitter 转发。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class CreateEmitter<T>\n    extends AtomicReference<Disposable>\n    implements ObservableEmitter<T>, Disposable {",
            "    /** 桥接 ObservableEmitter 与下游 Observer，管理 Disposable 生命周期。 */\n"
            "    static final class CreateEmitter<T>\n    extends AtomicReference<Disposable>\n    implements ObservableEmitter<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** null 转 onError；未 dispose 时转发 onNext。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public boolean tryOnError(Throwable t) {",
            "        /** 未 dispose 时转发 onError 并 dispose；已 dispose 返回 false。 */\n"
            "        @Override\n        public boolean tryOnError(Throwable t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 未 dispose 时转发 onComplete 并 dispose。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
        (
            "        @Override\n        public ObservableEmitter<T> serialize() {",
            "        /** 返回 SerializedEmitter 包装，保证多线程调用串行化。 */\n"
            "        @Override\n        public ObservableEmitter<T> serialize() {",
        ),
        (
            "    /**\n     * Serializes calls to onNext, onError and onComplete.\n     *\n     * @param <T> the value type\n     */",
            "    /**\n"
            "     * 串行化对 onNext、onError、onComplete 的调用。\n"
            "     *\n     * @param <T> 元素类型\n"
            "     */",
        ),
        (
            "        void drainLoop() {",
            "        /** 从队列 drain 并转发至底层 emitter，处理错误与完成。 */\n"
            "        void drainLoop() {",
        ),
    ],
    "ObservableDebounce.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableDebounce<T, U> extends AbstractObservableWithUpstream<T, T> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 防抖：每个上游 onNext 映射为辅助 ObservableSource，\n"
            " * 仅当该内部源发出信号（或上游完成时最后一个内部源终止）才转发该值。\n"
            " *\n * @param <T> 上游元素类型\n"
            " * @param <U> 防抖选择器返回的内部源元素类型\n"
            " */\n"
            "public final class ObservableDebounce<T, U> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    public ObservableDebounce(ObservableSource<T> source, Function<? super T, ? extends ObservableSource<U>> debounceSelector) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param debounceSelector 将元素映射为防抖用内部 ObservableSource 的函数\n"
            "     */\n"
            "    public ObservableDebounce(ObservableSource<T> source, Function<? super T, ? extends ObservableSource<U>> debounceSelector) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> t) {",
            "    /** 经 SerializedObserver 订阅 DebounceObserver。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> t) {",
        ),
        (
            "    static final class DebounceObserver<T, U>\n    implements Observer<T>, Disposable {",
            "    /** 管理 index 与 debouncer，协调内部防抖订阅。 */\n"
            "    static final class DebounceObserver<T, U>\n    implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 递增 index、dispose 旧 debouncer、订阅新 DebounceInnerObserver。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        void emit(long idx, T value) {",
            "        /** index 仍匹配时向下游转发缓存值。 */\n"
            "        void emit(long idx, T value) {",
        ),
        (
            "        static final class DebounceInnerObserver<T, U> extends DisposableObserver<U> {",
            "    /** 订阅 debounceSelector 返回的内部源，终止时 emit 对应上游值。 */\n"
            "        static final class DebounceInnerObserver<T, U> extends DisposableObserver<U> {",
        ),
        (
            "            void emit() {",
            "            /** CAS 保证至多一次 emit 至 parent。 */\n"
            "            void emit() {",
        ),
    ],
    "ObservableDebounceTimed.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableDebounceTimed<T> extends AbstractObservableWithUpstream<T, T> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 定时防抖：每次 onNext 重置计时器，超时后转发最新值；\n"
            " * 可选 {@link Consumer} 在丢弃旧值时回调。\n"
            " *\n * @param <T> 上游元素类型\n"
            " */\n"
            "public final class ObservableDebounceTimed<T> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    public ObservableDebounceTimed(ObservableSource<T> source, long timeout, TimeUnit unit, Scheduler scheduler, Consumer<? super T> onDropped) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param timeout 防抖超时量\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 调度计时任务的 Scheduler\n"
            "     * @param onDropped 被新值顶替时丢弃的旧值回调（可为 null）\n"
            "     */\n"
            "    public ObservableDebounceTimed(ObservableSource<T> source, long timeout, TimeUnit unit, Scheduler scheduler, Consumer<? super T> onDropped) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> t) {",
            "    /** 在 scheduler Worker 上创建 DebounceTimedObserver。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> t) {",
        ),
        (
            "    static final class DebounceTimedObserver<T>\n    implements Observer<T>, Disposable {",
            "    /** 维护 timer 与 index，每次 onNext  reschedule DebounceEmitter。 */\n"
            "    static final class DebounceTimedObserver<T>\n    implements Observer<T>, Disposable {",
        ),
        (
            "        void emit(long idx, T t, DebounceEmitter<T> emitter) {",
            "        /** index 匹配时转发值并 dispose 对应 emitter。 */\n"
            "        void emit(long idx, T t, DebounceEmitter<T> emitter) {",
        ),
        (
            "    static final class DebounceEmitter<T> extends AtomicReference<Disposable> implements Runnable, Disposable {",
            "    /** 定时 Runnable，到期后通知 parent emit 缓存值。 */\n"
            "    static final class DebounceEmitter<T> extends AtomicReference<Disposable> implements Runnable, Disposable {",
        ),
    ],
    "ObservableDefer.java": [
        (
            "import java.util.Objects;\n\npublic final class ObservableDefer<T> extends Observable<T> {",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 延迟创建：每次订阅时调用 {@link Supplier} 获取新的 {@link ObservableSource} 再订阅。\n"
            " *\n * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableDefer<T> extends Observable<T> {",
        ),
        (
            "    public ObservableDefer(Supplier<? extends ObservableSource<? extends T>> supplier) {",
            "    /** @param supplier 每次订阅时提供 ObservableSource 的 Supplier */\n"
            "    public ObservableDefer(Supplier<? extends ObservableSource<? extends T>> supplier) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
            "    /** 调用 supplier.get()，null 或异常经 EmptyDisposable 处理。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
        ),
    ],
    "ObservableDelay.java": [
        (
            "import io.reactivex.rxjava4.observers.SerializedObserver;\n\npublic final class ObservableDelay<T> extends AbstractObservableWithUpstream<T, T> {",
            "import io.reactivex.rxjava4.observers.SerializedObserver;\n\n"
            "/**\n"
            " * 在 {@link Scheduler.Worker} 上延迟转发 onNext/onError/onComplete。\n"
            " * delayError 为 true 时 onError 也延迟相同时长。\n"
            " *\n * @param <T> 上游元素类型\n"
            " */\n"
            "public final class ObservableDelay<T> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    public ObservableDelay(ObservableSource<T> source, long delay, TimeUnit unit, Scheduler scheduler, boolean delayError) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param delay 延迟量\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 调度延迟任务的 Scheduler\n"
            "     * @param delayError 是否同样延迟 onError\n"
            "     */\n"
            "    public ObservableDelay(ObservableSource<T> source, long delay, TimeUnit unit, Scheduler scheduler, boolean delayError) {",
        ),
        (
            "    @Override\n    @SuppressWarnings(\"unchecked\")\n    public void subscribeActual(Observer<? super T> t) {",
            "    /** delayError 时下游不序列化，否则用 SerializedObserver 包装。 */\n"
            "    @Override\n    @SuppressWarnings(\"unchecked\")\n    public void subscribeActual(Observer<? super T> t) {",
        ),
        (
            "    static final class DelayObserver<T> implements Observer<T>, Disposable {",
            "    /** 将各信号 schedule 到 Worker 上延迟执行。 */\n"
            "    static final class DelayObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        final class OnNext implements Runnable {",
            "        /** 延迟到期后转发 onNext（Worker 未 dispose）。 */\n"
            "        final class OnNext implements Runnable {",
        ),
        (
            "        final class OnError implements Runnable {",
            "        /** 延迟到期后转发 onError 并 dispose Worker。 */\n"
            "        final class OnError implements Runnable {",
        ),
        (
            "        final class OnComplete implements Runnable {",
            "        /** 延迟到期后转发 onComplete 并 dispose Worker。 */\n"
            "        final class OnComplete implements Runnable {",
        ),
    ],
    "ObservableDelaySubscriptionOther.java": [
        (
            "/**\n * Delays the subscription to the main source until the other\n * observable fires an event or completes.\n * @param <T> the main type\n * @param <U> the other value type, ignored\n */",
            "/**\n"
            " * 延迟订阅主流：待 other Observable 发出任意 onNext 或 onComplete 后再订阅 main。\n"
            " * @param <T> 主流元素类型\n"
            " * @param <U> 辅助 Observable 元素类型（仅用于触发，值被忽略）\n"
            " */",
        ),
        (
            "    public ObservableDelaySubscriptionOther(ObservableSource<? extends T> main, ObservableSource<U> other) {",
            "    /**\n"
            "     * @param main 延迟订阅的主流\n"
            "     * @param other 触发订阅的辅助 Observable\n"
            "     */\n"
            "    public ObservableDelaySubscriptionOther(ObservableSource<? extends T> main, ObservableSource<U> other) {",
        ),
        (
            "    @Override\n    public void subscribeActual(final Observer<? super T> child) {",
            "    /** 先订阅 other，由其 DelayObserver 在 onComplete 时订阅 main。 */\n"
            "    @Override\n    public void subscribeActual(final Observer<? super T> child) {",
        ),
        (
            "    final class DelayObserver implements Observer<U> {",
            "    /** 监听 other：onNext 等同 onComplete，完成后订阅 main。 */\n"
            "    final class DelayObserver implements Observer<U> {",
        ),
        (
            "        final class OnComplete implements Observer<T> {",
            "        /** 订阅 main 并将信号转发至 child。 */\n"
            "        final class OnComplete implements Observer<T> {",
        ),
    ],
    "ObservableDematerialize.java": [
        (
            "import java.util.Objects;\n\npublic final class ObservableDematerialize<T, R> extends AbstractObservableWithUpstream<T, R> {",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 物理解包：将上游元素经 selector 转为 {@link Notification}，\n"
            " * 按 onNext/onError/onComplete 语义展开为下游 R 流或终止信号。\n"
            " *\n * @param <T> 上游元素类型\n"
            " * @param <R> 解包后的元素类型\n"
            " */\n"
            "public final class ObservableDematerialize<T, R> extends AbstractObservableWithUpstream<T, R> {",
        ),
        (
            "    public ObservableDematerialize(ObservableSource<T> source, Function<? super T, ? extends Notification<R>> selector) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param selector 将元素映射为 Notification 的函数\n"
            "     */\n"
            "    public ObservableDematerialize(ObservableSource<T> source, Function<? super T, ? extends Notification<R>> selector) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super R> observer) {",
            "    /** 订阅 DematerializeObserver 解包 Notification。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super R> observer) {",
        ),
        (
            "    static final class DematerializeObserver<T, R> implements Observer<T>, Disposable {",
            "    /** 按 Notification 类型转发 onNext 或终止序列。 */\n"
            "    static final class DematerializeObserver<T, R> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T item) {",
            "        /** 解包 Notification：onError/onComplete 终止，否则转发 getValue。 */\n"
            "        @Override\n        public void onNext(T item) {",
        ),
    ],
    "ObservableDetach.java": [
        (
            "/**\n * Breaks the links between the upstream and the downstream (the Disposable and\n * the Observer references) when the sequence terminates or gets disposed.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 在序列终止或 dispose 时断开上下游链接\n"
            " *（Disposable 与 Observer 引用置为 {@link EmptyComponent}）。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public ObservableDetach(ObservableSource<T> source) {",
            "    /** @param source 上游 ObservableSource */\n"
            "    public ObservableDetach(ObservableSource<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 订阅 DetachObserver，终止后释放引用防泄漏。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class DetachObserver<T> implements Observer<T>, Disposable {",
            "    /** 转发信号并在 onError/onComplete/dispose 时清空 upstream/downstream 引用。 */\n"
            "    static final class DetachObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void dispose() {",
            "        /** dispose 上游并将引用替换为 EmptyComponent。 */\n"
            "        @Override\n        public void dispose() {",
        ),
    ],
    "ObservableDistinct.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableDistinct<T, K> extends AbstractObservableWithUpstream<T, T> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 按 keySelector 提取键去重：键首次出现时转发原值，重复键静默丢弃。\n"
            " *\n * @param <T> 上游元素类型\n"
            " * @param <K> 去重键类型\n"
            " */\n"
            "public final class ObservableDistinct<T, K> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    public ObservableDistinct(ObservableSource<T> source, Function<? super T, K> keySelector, Supplier<? extends Collection<? super K>> collectionSupplier) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param keySelector 提取去重键的函数\n"
            "     * @param collectionSupplier 提供存储已见键的 Collection 的 Supplier\n"
            "     */\n"
            "    public ObservableDistinct(ObservableSource<T> source, Function<? super T, K> keySelector, Supplier<? extends Collection<? super K>> collectionSupplier) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 从 collectionSupplier 获取 Collection 并订阅 DistinctObserver。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class DistinctObserver<T, K> extends BasicFuseableObserver<T, T> {",
            "    /** 维护已见键集合，支持 queue fusion poll 路径去重。 */\n"
            "    static final class DistinctObserver<T, K> extends BasicFuseableObserver<T, T> {",
        ),
        (
            "        @Override\n        public void onNext(T value) {",
            "        /** collection.add(key) 成功时转发 value。 */\n"
            "        @Override\n        public void onNext(T value) {",
        ),
    ],
    "ObservableDistinctUntilChanged.java": [
        (
            "import io.reactivex.rxjava4.internal.observers.BasicFuseableObserver;\n\npublic final class ObservableDistinctUntilChanged<T, K> extends AbstractObservableWithUpstream<T, T> {",
            "import io.reactivex.rxjava4.internal.observers.BasicFuseableObserver;\n\n"
            "/**\n"
            " * 相邻去重：连续元素经 keySelector 得键，comparer 判定与上一键相同时丢弃。\n"
            " *\n * @param <T> 上游元素类型\n"
            " * @param <K> 比较键类型\n"
            " */\n"
            "public final class ObservableDistinctUntilChanged<T, K> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    public ObservableDistinctUntilChanged(ObservableSource<T> source, Function<? super T, K> keySelector, BiPredicate<? super K, ? super K> comparer) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param keySelector 提取比较键的函数\n"
            "     * @param comparer 比较相邻键是否相等的 BiPredicate\n"
            "     */\n"
            "    public ObservableDistinctUntilChanged(ObservableSource<T> source, Function<? super T, K> keySelector, BiPredicate<? super K, ? super K> comparer) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 订阅 DistinctUntilChangedObserver。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class DistinctUntilChangedObserver<T, K> extends BasicFuseableObserver<T, T> {",
            "    /** 缓存 last 键，相邻相等则跳过 onNext。 */\n"
            "    static final class DistinctUntilChangedObserver<T, K> extends BasicFuseableObserver<T, T> {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 首元素或键变化时转发，否则丢弃。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "ObservableDoAfterNext.java": [
        (
            "/**\n * Calls a consumer after pushing the current item to the downstream.\n * <p>History: 2.0.1 - experimental\n * @param <T> the value type\n * @since 2.1\n */",
            "/**\n"
            " * 在向下游 onNext 之后调用 {@link Consumer} 副作用。\n"
            " * <p>History: 2.0.1 - experimental\n"
            " * @param <T> 元素类型\n"
            " * @since 2.1\n"
            " */",
        ),
        (
            "    public ObservableDoAfterNext(ObservableSource<T> source, Consumer<? super T> onAfterNext) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param onAfterNext 每个元素转发后执行的 Consumer\n"
            "     */\n"
            "    public ObservableDoAfterNext(ObservableSource<T> source, Consumer<? super T> onAfterNext) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 订阅 DoAfterObserver。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class DoAfterObserver<T> extends BasicFuseableObserver<T, T> {",
            "    /** 先 downstream.onNext 再 onAfterNext.accept。 */\n"
            "    static final class DoAfterObserver<T> extends BasicFuseableObserver<T, T> {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 转发后执行 onAfterNext；fusion 模式下 poll 后同样回调。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "ObservableDoFinally.java": [
        (
            "/**\n * Execute an action after an onError, onComplete or a dispose event.\n * <p>History: 2.0.1 - experimental\n * @param <T> the value type\n * @since 2.1\n */",
            "/**\n"
            " * 在 onError、onComplete 或 dispose 之后执行 {@link Action}（至多一次）。\n"
            " * <p>History: 2.0.1 - experimental\n"
            " * @param <T> 元素类型\n"
            " * @since 2.1\n"
            " */",
        ),
        (
            "    public ObservableDoFinally(ObservableSource<T> source, Action onFinally) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param onFinally 终止或 dispose 后执行的 Action\n"
            "     */\n"
            "    public ObservableDoFinally(ObservableSource<T> source, Action onFinally) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 订阅 DoFinallyObserver。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class DoFinallyObserver<T> extends BasicIntQueueDisposable<T> implements Observer<T> {",
            "    /** 转发信号并在终止/dispose 时 CAS 触发 runFinally。 */\n"
            "    static final class DoFinallyObserver<T> extends BasicIntQueueDisposable<T> implements Observer<T> {",
        ),
        (
            "        void runFinally() {",
            "        /** compareAndSet(0,1) 保证 onFinally 至多执行一次。 */\n"
            "        void runFinally() {",
        ),
    ],
    "ObservableDoOnEach.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableDoOnEach<T> extends AbstractObservableWithUpstream<T, T> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 在转发各生命周期信号前后注入副作用：\n"
            " * onNext 前 onNext 回调，onError/onComplete 前后分别调用对应 Action。\n"
            " *\n * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableDoOnEach<T> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    public ObservableDoOnEach(ObservableSource<T> source, Consumer<? super T> onNext,\n                              Consumer<? super Throwable> onError,\n                              Action onComplete,\n                              Action onAfterTerminate) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param onNext 每个 onNext 前执行的 Consumer\n"
            "     * @param onError onError 时执行的 Consumer\n"
            "     * @param onComplete onComplete 前执行的 Action\n"
            "     * @param onAfterTerminate 终止信号转发后执行的 Action\n"
            "     */\n"
            "    public ObservableDoOnEach(ObservableSource<T> source, Consumer<? super T> onNext,\n                              Consumer<? super Throwable> onError,\n                              Action onComplete,\n                              Action onAfterTerminate) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> t) {",
            "    /** 订阅 DoOnEachObserver。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> t) {",
        ),
        (
            "    static final class DoOnEachObserver<T> implements Observer<T>, Disposable {",
            "    /** 包装下游并在各信号点调用注册的副作用。 */\n"
            "    static final class DoOnEachObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 先 onNext.accept 再 downstream.onNext；副作用异常转 onError。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "ObservableDoOnLifecycle.java": [
        (
            "import io.reactivex.rxjava4.internal.observers.DisposableLambdaObserver;\n\npublic final class ObservableDoOnLifecycle<T> extends AbstractObservableWithUpstream<T, T> {",
            "import io.reactivex.rxjava4.internal.observers.DisposableLambdaObserver;\n\n"
            "/**\n"
            " * 在订阅与 dispose 生命周期注入回调：\n"
            " * onSubscribe 收到 Disposable 时、dispose 时分别执行。\n"
            " *\n * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableDoOnLifecycle<T> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    public ObservableDoOnLifecycle(Observable<T> upstream, Consumer<? super Disposable> onSubscribe,\n            Action onDispose) {",
            "    /**\n"
            "     * @param upstream 上游 Observable\n"
            "     * @param onSubscribe 收到 Disposable 时执行的 Consumer\n"
            "     * @param onDispose dispose 时执行的 Action\n"
            "     */\n"
            "    public ObservableDoOnLifecycle(Observable<T> upstream, Consumer<? super Disposable> onSubscribe,\n            Action onDispose) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 经 DisposableLambdaObserver 包装生命周期回调。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
    ],
    "ObservableElementAt.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableElementAt<T> extends AbstractObservableWithUpstream<T, T> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 取第 index 个（0-based）上游元素后 onComplete；\n"
            " * 元素不足时可发 defaultValue 或 {@link NoSuchElementException}。\n"
            " *\n * @param <T> 上游元素类型\n"
            " */\n"
            "public final class ObservableElementAt<T> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    public ObservableElementAt(ObservableSource<T> source, long index, T defaultValue, boolean errorOnFewer) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param index 目标元素索引（0-based）\n"
            "     * @param defaultValue 元素不足时的默认值（可为 null）\n"
            "     * @param errorOnFewer 无默认值且元素不足时是否 onError(NoSuchElementException)\n"
            "     */\n"
            "    public ObservableElementAt(ObservableSource<T> source, long index, T defaultValue, boolean errorOnFewer) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> t) {",
            "    /** 订阅 ElementAtObserver。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> t) {",
        ),
        (
            "    static final class ElementAtObserver<T> implements Observer<T>, Disposable {",
            "    /** 计数至 index 时 emit 并 dispose 上游，完成时处理默认值逻辑。 */\n"
            "    static final class ElementAtObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** count==index 时转发该元素并 onComplete。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 未命中 index 时发 defaultValue 或 NoSuchElementException。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
}


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def tree_guard() -> int:
    tracked = len(subprocess.check_output(["git", "-C", str(ROOT), "ls-files"]).splitlines())
    if tracked < 50000:
        raise RuntimeError(f"tree guard failed: tracked={tracked} (expected >=50000)")
    for path in GUARD_FILES:
        if not path.exists():
            raise RuntimeError(f"guard file missing: {path}")
        if not has_chinese(path.read_text(encoding="utf-8")):
            raise RuntimeError(f"guard file lacks Chinese: {path}")
    return tracked


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


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
    tree_count = tree_guard() if not failures else 0
    print(json.dumps({"ok": ok, "failures": failures, "tree_count": tree_count}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
