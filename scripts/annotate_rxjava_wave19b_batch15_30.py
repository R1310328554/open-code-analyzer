#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-19b observable operators [15:30]."""
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
WAVE19B_FILE = Path("/tmp/rxjava_w19b.txt")
SCRIPT_NAME = "annotate_rxjava_wave19b_batch15_30.py"
BATCH_FILES = [
    ln.strip()
    for ln in WAVE19B_FILE.read_text(encoding="utf-8").splitlines()
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
    "ObservablePublishSelector.java": [
        (
            "/**\n * Shares a source Observable for the duration of a selector function.\n * @param <T> the input value type\n * @param <R> the output value type\n */",
            "/**\n"
            " * 在 selector 执行期间通过 {@link PublishSubject} 共享上游 Observable：\n"
            " * selector 返回的 ObservableSource 与上游并行订阅，上游事件经 subject 转发。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> selector 输出类型\n"
            " */",
        ),
        (
            "    public ObservablePublishSelector(final ObservableSource<T> source,\n                                              final Function<? super Observable<T>, ? extends ObservableSource<R>> selector) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param selector 接收共享 Observable 并返回目标 ObservableSource 的函数\n"
            "     */\n"
            "    public ObservablePublishSelector(final ObservableSource<T> source,\n                                              final Function<? super Observable<T>, ? extends ObservableSource<R>> selector) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
            "    /** 创建 PublishSubject，应用 selector 后并行订阅 target 与 source。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
        ),
        (
            "    record SourceObserver<T>(PublishSubject<T> subject, AtomicReference<Disposable> target) implements Observer<T> {",
            "    /** 将上游事件转发至 subject，onSubscribe 绑定 target 的 Disposable。 */\n"
            "    record SourceObserver<T>(PublishSubject<T> subject, AtomicReference<Disposable> target) implements Observer<T> {",
        ),
        (
            "    static final class TargetObserver<R>\n    extends AtomicReference<Disposable> implements Observer<R>, Disposable {",
            "    /** selector 侧下游 Observer：终止时 dispose 自身并转发信号。 */\n"
            "    static final class TargetObserver<R>\n    extends AtomicReference<Disposable> implements Observer<R>, Disposable {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** onError 时 dispose 自身再转发错误。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** onComplete 时 dispose 自身再转发完成。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "ObservableRange.java": [
        (
            "/**\n * Emits a range of integer values from start to end.\n */",
            "/**\n"
            " * 发射 [start, start+count) 范围内的 int 序列，支持 SYNC queue fusion。\n"
            " */",
        ),
        (
            "    public ObservableRange(int start, int count) {",
            "    /**\n"
            "     * @param start 首个发射值（含）\n"
            "     * @param count 元素个数\n"
            "     */\n"
            "    public ObservableRange(int start, int count) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super Integer> o) {",
            "    /** 创建 RangeDisposable 并同步 run 发射整数序列。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super Integer> o) {",
        ),
        (
            "    static final class RangeDisposable\n    extends BasicIntQueueDisposable<Integer> {",
            "    /** 同步迭代 [index, end) 或作为 fusion poll 源。 */\n"
            "    static final class RangeDisposable\n    extends BasicIntQueueDisposable<Integer> {",
        ),
        (
            "        void run() {",
            "        /** 非 fusion 路径：循环 onNext 直至 end 或 dispose，然后 onComplete。 */\n"
            "        void run() {",
        ),
        (
            "        @Nullable\n        @Override\n        public Integer poll() {",
            "        /** fusion 路径：poll 下一 int，耗尽返回 null 并置 done。 */\n"
            "        @Nullable\n        @Override\n        public Integer poll() {",
        ),
        (
            "        @Override\n        public int requestFusion(int mode) {",
            "        /** 请求 SYNC 时启用 fused 并返回 SYNC。 */\n"
            "        @Override\n        public int requestFusion(int mode) {",
        ),
    ],
    "ObservableRangeLong.java": [
        (
            "import java.io.Serial;\n\npublic final class ObservableRangeLong extends Observable<Long> {",
            "import java.io.Serial;\n\n"
            "/**\n"
            " * 发射 [start, start+count) 范围内的 long 序列，支持 SYNC queue fusion。\n"
            " */\n"
            "public final class ObservableRangeLong extends Observable<Long> {",
        ),
        (
            "    public ObservableRangeLong(long start, long count) {",
            "    /**\n"
            "     * @param start 首个发射值（含）\n"
            "     * @param count 元素个数\n"
            "     */\n"
            "    public ObservableRangeLong(long start, long count) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super Long> o) {",
            "    /** 创建 RangeDisposable 并同步 run 发射 long 序列。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super Long> o) {",
        ),
        (
            "    static final class RangeDisposable\n    extends BasicIntQueueDisposable<Long> {",
            "    /** 同步迭代 [index, end) 或作为 fusion poll 源。 */\n"
            "    static final class RangeDisposable\n    extends BasicIntQueueDisposable<Long> {",
        ),
        (
            "        void run() {",
            "        /** 非 fusion 路径：循环 onNext 直至 end 或 dispose，然后 onComplete。 */\n"
            "        void run() {",
        ),
        (
            "        @Nullable\n        @Override\n        public Long poll() {",
            "        /** fusion 路径：poll 下一 long，耗尽返回 null 并置 done。 */\n"
            "        @Nullable\n        @Override\n        public Long poll() {",
        ),
        (
            "        @Override\n        public int requestFusion(int mode) {",
            "        /** 请求 SYNC 时启用 fused 并返回 SYNC。 */\n"
            "        @Override\n        public int requestFusion(int mode) {",
        ),
    ],
    "ObservableReduceMaybe.java": [
        (
            "/**\n * Reduce a sequence of values into a single value via an aggregator function and emit the final value or complete\n * if the source is empty.\n *\n * @param <T> the source and result value type\n */",
            "/**\n"
            " * 用 BiFunction 将 Observable 序列归约为单个值：\n"
            " * 有元素则 onSuccess，空序列则 onComplete。\n"
            " *\n * @param <T> 源与结果类型\n"
            " */",
        ),
        (
            "    public ObservableReduceMaybe(ObservableSource<T> source, BiFunction<T, T, T> reducer) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param reducer 两元素累加器 (acc, value) -> acc\n"
            "     */\n"
            "    public ObservableReduceMaybe(ObservableSource<T> source, BiFunction<T, T, T> reducer) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 订阅 ReduceObserver 逐次 apply reducer。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class ReduceObserver<T> implements Observer<T>, Disposable {",
            "    /** 首元素缓存为 acc，后续 apply reducer；终止时 onSuccess 或 onComplete。 */\n"
            "    static final class ReduceObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T value) {",
            "        /** 首值直接缓存，其后 apply reducer；null 结果转 onError。 */\n"
            "        @Override\n        public void onNext(T value) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 有 acc 则 onSuccess，否则 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "ObservableReduceSeedSingle.java": [
        (
            "/**\n * Reduce a sequence of values, starting from a seed value and by using\n * an accumulator function and return the last accumulated value.\n *\n * @param <T> the source value type\n * @param <R> the accumulated result type\n */",
            "/**\n"
            " * 从 seed 出发用 BiFunction 累加 Observable 元素，\n"
            " * 完成后 onSuccess 最终累积值。\n"
            " *\n * @param <T> 上游元素类型\n"
            " * @param <R> 累积结果类型\n"
            " */",
        ),
        (
            "    public ObservableReduceSeedSingle(ObservableSource<T> source, R seed, BiFunction<R, ? super T, R> reducer) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param seed 初始累积值\n"
            "     * @param reducer 累加器 (acc, value) -> acc\n"
            "     */\n"
            "    public ObservableReduceSeedSingle(ObservableSource<T> source, R seed, BiFunction<R, ? super T, R> reducer) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super R> observer) {",
            "    /** 订阅 ReduceSeedObserver 从 seed 开始累加。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super R> observer) {",
        ),
        (
            "    static final class ReduceSeedObserver<T, R> implements Observer<T>, Disposable {",
            "    /** 每 onNext apply reducer；onComplete 时 onSuccess 当前 acc。 */\n"
            "    static final class ReduceSeedObserver<T, R> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T value) {",
            "        /** apply reducer 更新 acc；异常或 null 结果转 onError。 */\n"
            "        @Override\n        public void onNext(T value) {",
        ),
    ],
    "ObservableReduceWithSingle.java": [
        (
            "/**\n * Reduce a sequence of values, starting from a generated seed value and by using\n * an accumulator function and return the last accumulated value.\n *\n * @param <T> the source value type\n * @param <R> the accumulated result type\n */",
            "/**\n"
            " * 订阅时调用 seedSupplier 获取初始值，再用 reducer 累加上游元素，\n"
            " * 委托 {@link ObservableReduceSeedSingle.ReduceSeedObserver} 完成归约。\n"
            " *\n * @param <T> 上游元素类型\n"
            " * @param <R> 累积结果类型\n"
            " */",
        ),
        (
            "    public ObservableReduceWithSingle(ObservableSource<T> source, Supplier<R> seedSupplier, BiFunction<R, ? super T, R> reducer) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param seedSupplier 提供初始累积值的 Supplier\n"
            "     * @param reducer 累加器 (acc, value) -> acc\n"
            "     */\n"
            "    public ObservableReduceWithSingle(ObservableSource<T> source, Supplier<R> seedSupplier, BiFunction<R, ? super T, R> reducer) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super R> observer) {",
            "    /** 调用 seedSupplier 获取 seed 后订阅 ReduceSeedObserver。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super R> observer) {",
        ),
    ],
    "ObservableRefCount.java": [
        (
            "/**\n * Returns an observable sequence that stays connected to the source as long as\n * there is at least one subscription to the observable sequence.\n *\n * @param <T>\n *            the value type\n */",
            "/**\n"
            " * 对 {@link ConnectableObservable} 做引用计数：\n"
            " * 订阅者数达 n 时 connect，归零后 reset（可配置 timeout 延迟断开）。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public ObservableRefCount(ConnectableObservable<T> source) {",
            "    /** @param source 待引用计数的 ConnectableObservable（n=1，无 timeout） */\n"
            "    public ObservableRefCount(ConnectableObservable<T> source) {",
        ),
        (
            "    public ObservableRefCount(ConnectableObservable<T> source, int n, long timeout, TimeUnit unit,\n            Scheduler scheduler) {",
            "    /**\n"
            "     * @param source ConnectableObservable 源\n"
            "     * @param n 触发 connect 所需的最小订阅者数\n"
            "     * @param timeout 最后订阅者取消后延迟 reset 的时间（0 表示立即）\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 调度 timeout 的 Scheduler\n"
            "     */\n"
            "    public ObservableRefCount(ConnectableObservable<T> source, int n, long timeout, TimeUnit unit,\n            Scheduler scheduler) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 维护 RefConnection 引用计数，达 n 时 connect 上游。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    void cancel(RefConnection rc) {",
            "    /** 订阅者 cancel：计数减一，归零且已 connect 时 timeout 或立即 reset。 */\n"
            "    void cancel(RefConnection rc) {",
        ),
        (
            "    void terminated(RefConnection rc) {",
            "    /** 上游终止：清理 timer，计数归零时 reset 并清空 connection。 */\n"
            "    void terminated(RefConnection rc) {",
        ),
        (
            "    void timeout(RefConnection rc) {",
            "    /** timeout 到期且仍无订阅者时 dispose 连接并 reset 上游。 */\n"
            "    void timeout(RefConnection rc) {",
        ),
        (
            "    static final class RefConnection extends AtomicReference<Disposable>\n    implements Runnable, Consumer<Disposable> {",
            "    /** 引用计数状态：run 触发 timeout，accept 保存 connect Disposable。 */\n"
            "    static final class RefConnection extends AtomicReference<Disposable>\n    implements Runnable, Consumer<Disposable> {",
        ),
        (
            "        @Override\n        public void accept(Disposable t) {",
            "        /** connect 回调：保存 Disposable；若已 early disconnect 则 reset。 */\n"
            "        @Override\n        public void accept(Disposable t) {",
        ),
        (
            "    static final class RefCountObserver<T>\n    extends AtomicBoolean implements Observer<T>, Disposable {",
            "    /** 转发上游事件；dispose/终止时更新 parent 引用计数。 */\n"
            "    static final class RefCountObserver<T>\n    extends AtomicBoolean implements Observer<T>, Disposable {",
        ),
    ],
    "ObservableRepeat.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.SequentialDisposable;\n\npublic final class ObservableRepeat<T> extends AbstractObservableWithUpstream<T, T> {",
            "import io.reactivex.rxjava4.internal.disposables.SequentialDisposable;\n\n"
            "/**\n"
            " * 上游 onComplete 后按 count 次数重新订阅（Long.MAX_VALUE 表示无限）。\n"
            " *\n * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableRepeat<T> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    public ObservableRepeat(Observable<T> source, long count) {",
            "    /**\n"
            "     * @param source 上游 Observable\n"
            "     * @param count 重复次数（含首次后的重订阅次数）\n"
            "     */\n"
            "    public ObservableRepeat(Observable<T> source, long count) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
            "    /** 创建 RepeatObserver 并在 onComplete 时 subscribeNext 重订阅。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class RepeatObserver<T> extends AtomicInteger implements Observer<T> {",
            "    /** 转发 onNext；onComplete 时递减 remaining 并重订阅或完成。 */\n"
            "    static final class RepeatObserver<T> extends AtomicInteger implements Observer<T> {",
        ),
        (
            "        /**\n         * Subscribes to the source again via trampolining.\n         */",
            "        /** 通过 trampolining 再次订阅上游（missed 计数防重入）。 */",
        ),
    ],
    "ObservableRepeatUntil.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.SequentialDisposable;\n\npublic final class ObservableRepeatUntil<T> extends AbstractObservableWithUpstream<T, T> {",
            "import io.reactivex.rxjava4.internal.disposables.SequentialDisposable;\n\n"
            "/**\n"
            " * 上游 onComplete 后若 until 返回 false 则重新订阅，\n"
            " * 直至 until 为 true 或 until 抛异常。\n"
            " *\n * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableRepeatUntil<T> extends AbstractObservableWithUpstream<T, T> {",
        ),
        (
            "    public ObservableRepeatUntil(Observable<T> source, BooleanSupplier until) {",
            "    /**\n"
            "     * @param source 上游 Observable\n"
            "     * @param until 每次 onComplete 后判断是否停止重复\n"
            "     */\n"
            "    public ObservableRepeatUntil(Observable<T> source, BooleanSupplier until) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
            "    /** 创建 RepeatUntilObserver 并在 until 为 false 时 subscribeNext。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class RepeatUntilObserver<T> extends AtomicInteger implements Observer<T> {",
            "    /** onComplete 时调用 stop.getAsBoolean() 决定重订阅或完成。 */\n"
            "    static final class RepeatUntilObserver<T> extends AtomicInteger implements Observer<T> {",
        ),
        (
            "        /**\n         * Subscribes to the source again via trampolining.\n         */",
            "        /** 通过 trampolining 再次订阅上游。 */",
        ),
    ],
    "ObservableRepeatWhen.java": [
        (
            "/**\n * Repeatedly subscribe to a source if a handler ObservableSource signals an item.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 上游 onComplete 时向 handler 的 signaller 发信号；\n"
            " * handler 返回的 ObservableSource 每 onNext 一次即重订阅上游。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public ObservableRepeatWhen(ObservableSource<T> source, Function<? super Observable<Object>, ? extends ObservableSource<?>> handler) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param handler 接收完成信号 Observable 并返回控制重复的 ObservableSource\n"
            "     */\n"
            "    public ObservableRepeatWhen(ObservableSource<T> source, Function<? super Observable<Object>, ? extends ObservableSource<?>> handler) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 创建 PublishSubject signaller，订阅 handler 与 RepeatWhenObserver。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class RepeatWhenObserver<T> extends AtomicInteger implements Observer<T>, Disposable {",
            "    /** 上游完成时 signaller.onNext；inner onNext 触发 subscribeNext 重订阅。 */\n"
            "    static final class RepeatWhenObserver<T> extends AtomicInteger implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 清空 upstream 引用，active=false，向 signaller 发完成信号。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
        (
            "        void subscribeNext() {",
            "        /** wip 门控：active 为 false 时重新 subscribe 上游。 */\n"
            "        void subscribeNext() {",
        ),
        (
            "        final class InnerRepeatObserver extends AtomicReference<Disposable> implements Observer<Object> {",
            "        /** handler 侧 Observer：onNext 触发 innerNext 重订阅。 */\n"
            "        final class InnerRepeatObserver extends AtomicReference<Disposable> implements Observer<Object> {",
        ),
    ],
    "ObservableRetryBiPredicate.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.SequentialDisposable;\n\npublic final class ObservableRetryBiPredicate<T> extends AbstractObservableWithUpstream<T, T> {",
            "import io.reactivex.rxjava4.internal.disposables.SequentialDisposable;\n\n"
            "/**\n"
            " * onError 时用 BiPredicate&lt;Integer, Throwable&gt; 判定是否重试；\n"
            " * 第一个参数为当前重试次数（从 1 起）。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public ObservableRetryBiPredicate(\n            Observable<T> source,\n            BiPredicate<? super Integer, ? super Throwable> predicate) {",
            "    /**\n"
            "     * @param source 上游 Observable\n"
            "     * @param predicate (重试次数, 异常) -> 是否继续重试\n"
            "     */\n"
            "    public ObservableRetryBiPredicate(\n            Observable<T> source,\n            BiPredicate<? super Integer, ? super Throwable> predicate) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
            "    /** 创建 RetryBiObserver，onError 时 test predicate 决定是否 subscribeNext。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class RetryBiObserver<T> extends AtomicInteger implements Observer<T> {",
            "    /** onError 递增 retries 并 test predicate；true 则重订阅。 */\n"
            "    static final class RetryBiObserver<T> extends AtomicInteger implements Observer<T> {",
        ),
        (
            "        /**\n         * Subscribes to the source again via trampolining.\n         */",
            "        /** 通过 trampolining 再次订阅上游。 */",
        ),
    ],
    "ObservableRetryPredicate.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.SequentialDisposable;\n\npublic final class ObservableRetryPredicate<T> extends AbstractObservableWithUpstream<T, T> {",
            "import io.reactivex.rxjava4.internal.disposables.SequentialDisposable;\n\n"
            "/**\n"
            " * onError 时在剩余次数内用 Predicate 判定是否重试；\n"
            " * count 为 Long.MAX_VALUE 表示次数不限（仍受 predicate 约束）。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public ObservableRetryPredicate(Observable<T> source,\n            long count,\n            Predicate<? super Throwable> predicate) {",
            "    /**\n"
            "     * @param source 上游 Observable\n"
            "     * @param count 最大重试次数\n"
            "     * @param predicate 对异常是否继续重试\n"
            "     */\n"
            "    public ObservableRetryPredicate(Observable<T> source,\n            long count,\n            Predicate<? super Throwable> predicate) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
            "    /** 创建 RepeatObserver，onError 时递减 remaining 并 test predicate。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class RepeatObserver<T> extends AtomicInteger implements Observer<T> {",
            "    /** remaining 为 0 直接 onError；否则 predicate.test 决定重订阅。 */\n"
            "    static final class RepeatObserver<T> extends AtomicInteger implements Observer<T> {",
        ),
        (
            "        /**\n         * Subscribes to the source again via trampolining.\n         */",
            "        /** 通过 trampolining 再次订阅上游。 */",
        ),
    ],
    "ObservableRetryWhen.java": [
        (
            "/**\n * Repeatedly subscribe to a source if a handler ObservableSource signals an item.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * onError 时将异常推入 handler 的 signaller；\n"
            " * handler 返回的 ObservableSource 每 onNext 一次即重订阅上游。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public ObservableRetryWhen(ObservableSource<T> source, Function<? super Observable<Throwable>, ? extends ObservableSource<?>> handler) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param handler 接收 Throwable 信号 Observable 并返回控制重试的 ObservableSource\n"
            "     */\n"
            "    public ObservableRetryWhen(ObservableSource<T> source, Function<? super Observable<Throwable>, ? extends ObservableSource<?>> handler) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 创建 PublishSubject&lt;Throwable&gt; signaller，订阅 handler 与 RepeatWhenObserver。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class RepeatWhenObserver<T> extends AtomicInteger implements Observer<T>, Disposable {",
            "    /** onError 时 signaller.onNext(e)；inner onNext 触发 subscribeNext 重订阅。 */\n"
            "    static final class RepeatWhenObserver<T> extends AtomicInteger implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** 清空 upstream，active=false，向 signaller 推送异常。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
        (
            "        void subscribeNext() {",
            "        /** wip 门控：active 为 false 时重新 subscribe 上游。 */\n"
            "        void subscribeNext() {",
        ),
        (
            "        final class InnerRepeatObserver extends AtomicReference<Disposable> implements Observer<Object> {",
            "        /** handler 侧 Observer：onNext 触发 innerNext 重订阅。 */\n"
            "        final class InnerRepeatObserver extends AtomicReference<Disposable> implements Observer<Object> {",
        ),
    ],
    "ObservableSampleTimed.java": [
        (
            "import io.reactivex.rxjava4.observers.SerializedObserver;\n\npublic final class ObservableSampleTimed<T> extends AbstractObservableWithUpstream<T, T> {",
            "import io.reactivex.rxjava4.observers.SerializedObserver;\n\n"
            "/**\n"
            " * 按固定周期采样上游最新值：定时 run 发射缓存值，\n"
            " * 新值覆盖旧值时可经 onDropped 通知；emitLast 控制完成时是否发射末值。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public ObservableSampleTimed(ObservableSource<T> source,\n                                 long period,\n                                 TimeUnit unit,\n                                 Scheduler scheduler,\n                                 boolean emitLast,\n                                 Consumer<? super T> onDropped) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param period 采样周期\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 调度定时任务的 Scheduler\n"
            "     * @param emitLast 完成时是否发射最后一次缓存值\n"
            "     * @param onDropped 被覆盖的旧值回调（可为 null）\n"
            "     */\n"
            "    public ObservableSampleTimed(ObservableSource<T> source,\n                                 long period,\n                                 TimeUnit unit,\n                                 Scheduler scheduler,\n                                 boolean emitLast,\n                                 Consumer<? super T> onDropped) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> t) {",
            "    /** 按 emitLast 选择 SampleTimedEmitLast 或 SampleTimedNoLast。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> t) {",
        ),
        (
            "    abstract static class SampleTimedObserver<T> extends AtomicReference<T> implements Observer<T>, Disposable, Runnable {",
            "    /** 缓存最新值，schedulePeriodicallyDirect 触发 emit；onDropped 处理被覆盖值。 */\n"
            "    abstract static class SampleTimedObserver<T> extends AtomicReference<T> implements Observer<T>, Disposable, Runnable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** getAndSet 新值；旧值非 null 时可选 onDropped.accept。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        void emit() {",
            "        /** 取出并清空缓存值，非 null 则 downstream.onNext。 */\n"
            "        void emit() {",
        ),
        (
            "    static final class SampleTimedNoLast<T> extends SampleTimedObserver<T> {",
            "    /** emitLast=false：定时 emit，完成时不强制发射末值。 */\n"
            "    static final class SampleTimedNoLast<T> extends SampleTimedObserver<T> {",
        ),
        (
            "    static final class SampleTimedEmitLast<T> extends SampleTimedObserver<T> {",
            "    /** emitLast=true：完成与定时 tick 均尝试 emit 末值，wip 协调 onComplete。 */\n"
            "    static final class SampleTimedEmitLast<T> extends SampleTimedObserver<T> {",
        ),
    ],
    "ObservableSampleWithObservable.java": [
        (
            "import io.reactivex.rxjava4.observers.SerializedObserver;\n\npublic final class ObservableSampleWithObservable<T> extends AbstractObservableWithUpstream<T, T> {",
            "import io.reactivex.rxjava4.observers.SerializedObserver;\n\n"
            "/**\n"
            " * 以 other Observable 的 onNext 为采样节拍，\n"
            " * 每次节拍发射上游最新缓存值；emitLast 控制主流完成时是否发射末值。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public ObservableSampleWithObservable(ObservableSource<T> source, ObservableSource<?> other, boolean emitLast) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param other 采样节拍源\n"
            "     * @param emitLast 主流完成时是否发射最后一次缓存值\n"
            "     */\n"
            "    public ObservableSampleWithObservable(ObservableSource<T> source, ObservableSource<?> other, boolean emitLast) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> t) {",
            "    /** 按 emitLast 选择 SampleMainEmitLast 或 SampleMainNoLast。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> t) {",
        ),
        (
            "    abstract static class SampleMainObserver<T> extends AtomicReference<T>\n    implements Observer<T>, Disposable {",
            "    /** 缓存上游最新值；sampler onNext 时 run/emit。 */\n"
            "    abstract static class SampleMainObserver<T> extends AtomicReference<T>\n    implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** lazySet 缓存最新值，等待采样节拍 emit。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        void emit() {",
            "        /** getAndSet(null) 取出缓存值并 onNext。 */\n"
            "        void emit() {",
        ),
        (
            "    record SamplerObserver<T>(SampleMainObserver<T> parent) implements Observer<Object> {",
            "    /** 采样源 Observer：onNext 触发 parent.run() 发射缓存值。 */\n"
            "    record SamplerObserver<T>(SampleMainObserver<T> parent) implements Observer<Object> {",
        ),
        (
            "    static final class SampleMainNoLast<T> extends SampleMainObserver<T> {",
            "    /** emitLast=false：主流完成即 onComplete，不强制 emit 末值。 */\n"
            "    static final class SampleMainNoLast<T> extends SampleMainObserver<T> {",
        ),
        (
            "    static final class SampleMainEmitLast<T> extends SampleMainObserver<T> {",
            "    /** emitLast=true：主流完成时 done=true，run 循环中 emit 末值后 onComplete。 */\n"
            "    static final class SampleMainEmitLast<T> extends SampleMainObserver<T> {",
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
    index_file = Path("/tmp/git-index-rxjava-w19b")
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
        "rxjava 4.0.0-alpha-21: Chinese-annotate wave 19b [15:30]",
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
            "wave19b",
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
        "queue: mark rxjava 4.0.0-alpha-21 wave19b done",
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
