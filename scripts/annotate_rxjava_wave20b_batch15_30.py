#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-20b observable operators [15:30]."""
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
WAVE20B_FILE = Path("/tmp/rxjava_w20b.txt")
SCRIPT_NAME = "annotate_rxjava_wave20b_batch15_30.py"
BATCH_FILES = [
    ln.strip()
    for ln in WAVE20B_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

GUARD_FILES = [
    VER
    / "analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ObservableSwitchMap.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableSwitchMap",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 上游每 onNext 用 mapper 映射为新的 ObservableSource 并切换订阅：\n"
            " * 取消旧 inner，仅转发当前 active inner 的队列元素。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> 映射后元素类型\n"
            " */\n"
            "public final class ObservableSwitchMap",
        ),
        (
            "    public ObservableSwitchMap(ObservableSource<T> source,\n                               Function<? super T, ? extends ObservableSource<? extends R>> mapper, int bufferSize,\n                                       boolean delayErrors) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param mapper 将上游元素映射为 inner ObservableSource 的函数\n"
            "     * @param bufferSize inner 队列容量\n"
            "     * @param delayErrors 为 true 时延迟聚合错误直至 drain 结束\n"
            "     */\n"
            "    public ObservableSwitchMap(ObservableSource<T> source,\n                               Function<? super T, ? extends ObservableSource<? extends R>> mapper, int bufferSize,\n                                       boolean delayErrors) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super R> t) {",
            "    /** 尝试标量优化，否则订阅 SwitchMapObserver。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super R> t) {",
        ),
        (
            "    static final class SwitchMapObserver<T, R> extends AtomicInteger implements Observer<T>, Disposable {",
            "    /** 维护 active inner 与 unique 序号；drain 从 inner 队列 poll 转发。 */\n"
            "    static final class SwitchMapObserver<T, R> extends AtomicInteger implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 递增 unique、cancel 旧 inner，subscribe 新 inner。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        void drain() {",
            "        /** wip 门控：从 active inner 队列 poll 并 onNext，处理 done/错误/切换。 */\n"
            "        void drain() {",
        ),
        (
            "        void innerError(SwitchMapInnerObserver<T, R> inner, Throwable ex) {",
            "        /** inner 错误：index 匹配 unique 时聚合错误并 drain。 */\n"
            "        void innerError(SwitchMapInnerObserver<T, R> inner, Throwable ex) {",
        ),
        (
            "    static final class SwitchMapInnerObserver<T, R> extends AtomicReference<Disposable> implements Observer<R> {",
            "    /** inner Observer：index 匹配 parent.unique 时才 offer/onComplete。 */\n"
            "    static final class SwitchMapInnerObserver<T, R> extends AtomicReference<Disposable> implements Observer<R> {",
        ),
        (
            "        @Override\n        public void onNext(R t) {",
            "        /** index 匹配时 offer 至队列并触发 parent.drain。 */\n"
            "        @Override\n        public void onNext(R t) {",
        ),
    ],
    "ObservableTake.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableTake",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 仅向下游转发前 limit 个元素，随后 dispose 上游并 onComplete。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableTake",
        ),
        (
            "    public ObservableTake(ObservableSource<T> source, long limit) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param limit 最多发射的元素个数\n"
            "     */\n"
            "    public ObservableTake(ObservableSource<T> source, long limit) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 订阅 TakeObserver。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class TakeObserver<T> implements Observer<T>, Disposable {",
            "    /** 维护 remaining 计数；归零时触发 onComplete。 */\n"
            "    static final class TakeObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 递减 remaining；最后一项 onNext 后立即 onComplete。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onSubscribe(Disposable d) {",
            "        /** remaining 为 0 时直接 EmptyDisposable.complete。 */\n"
            "        @Override\n        public void onSubscribe(Disposable d) {",
        ),
    ],
    "ObservableTakeLast.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\npublic final class ObservableTakeLast",
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\n"
            "/**\n"
            " * 以 ArrayDeque 滑动窗口缓存上游最后 count 个元素，\n"
            " * 上游 onComplete 后依次 poll 并 onNext。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableTakeLast",
        ),
        (
            "    public ObservableTakeLast(ObservableSource<T> source, int count) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param count 保留的末尾元素个数\n"
            "     */\n"
            "    public ObservableTakeLast(ObservableSource<T> source, int count) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> t) {",
            "    /** 订阅 TakeLastObserver（继承 ArrayDeque）。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> t) {",
        ),
        (
            "    static final class TakeLastObserver<T> extends ArrayDeque<T> implements Observer<T>, Disposable {",
            "    /** deque 满时 poll 队首再 offer；onComplete 时 drain 发射。 */\n"
            "    static final class TakeLastObserver<T> extends ArrayDeque<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 队列 size 达 count 时 poll 最旧元素再 offer 新值。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 循环 poll 并 onNext 直至 deque 空，然后 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "ObservableTakeLastOne.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\npublic final class ObservableTakeLastOne",
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\n"
            "/**\n"
            " * 缓存上游最后一个 onNext 值，onComplete 时发射该值（若有）后完成。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableTakeLastOne",
        ),
        (
            "    public ObservableTakeLastOne(ObservableSource<T> source) {",
            "    /** @param source 上游 ObservableSource */\n"
            "    public ObservableTakeLastOne(ObservableSource<T> source) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
            "    /** 订阅 TakeLastOneObserver。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class TakeLastOneObserver<T> implements Observer<T>, Disposable {",
            "    /** 每次 onNext 覆盖 value；onComplete 时 emit 后完成。 */\n"
            "    static final class TakeLastOneObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 覆盖缓存的最后一个值。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        void emit() {",
            "        /** 非 null 时 onNext 缓存值，然后 onComplete。 */\n"
            "        void emit() {",
        ),
    ],
    "ObservableTakeLastTimed.java": [
        (
            "import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;\n\npublic final class ObservableTakeLastTimed",
            "import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;\n\n"
            "/**\n"
            " * 按时间窗口与 count 限制缓存元素；上游终止后 drain 发射未过期项。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableTakeLastTimed",
        ),
        (
            "    public ObservableTakeLastTimed(ObservableSource<T> source,\n            long count, long time, TimeUnit unit, Scheduler scheduler, int bufferSize, boolean delayError) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param count 最大保留元素数（Long.MAX_VALUE 表示不限）\n"
            "     * @param time 时间窗口长度\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 提供 now 时间戳的 Scheduler\n"
            "     * @param bufferSize 队列容量\n"
            "     * @param delayError 为 true 时延迟报告错误直至 drain\n"
            "     */\n"
            "    public ObservableTakeLastTimed(ObservableSource<T> source,\n            long count, long time, TimeUnit unit, Scheduler scheduler, int bufferSize, boolean delayError) {",
        ),
        (
            "    static final class TakeLastTimedObserver<T>\n    extends AtomicBoolean implements Observer<T>, Disposable {",
            "    /** SpscLinkedArrayQueue 存 (timestamp, value) 对；drain 过滤过期项。 */\n"
            "    static final class TakeLastTimedObserver<T>\n    extends AtomicBoolean implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** offer (now, t) 并剔除超出时间窗口或 count 的旧项。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        void drain() {",
            "        /** compareAndSet 门控：poll 未过期项 onNext，空队列时终止。 */\n"
            "        void drain() {",
        ),
    ],
    "ObservableTakeUntil.java": [
        (
            "import io.reactivex.rxjava4.internal.util.*;\n\npublic final class ObservableTakeUntil",
            "import io.reactivex.rxjava4.internal.util.*;\n\n"
            "/**\n"
            " * 转发上游元素直至 other 发出 onNext/onComplete/onError，\n"
            " * 随后 dispose 上游并以 HalfSerializer 终止下游。\n"
            " * @param <T> 主流元素类型\n"
            " * @param <U> other 元素类型\n"
            " */\n"
            "public final class ObservableTakeUntil",
        ),
        (
            "    public ObservableTakeUntil(ObservableSource<T> source, ObservableSource<? extends U> other) {",
            "    /**\n"
            "     * @param source 主流 ObservableSource\n"
            "     * @param other 触发停止信号的 ObservableSource\n"
            "     */\n"
            "    public ObservableTakeUntil(ObservableSource<T> source, ObservableSource<? extends U> other) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super T> child) {",
            "    /** 并行订阅 other 与 source，共用 TakeUntilMainObserver。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super T> child) {",
        ),
        (
            "    static final class TakeUntilMainObserver<T, U> extends AtomicInteger\n    implements Observer<T>, Disposable {",
            "    /** 主流 Observer：HalfSerializer 串行转发 onNext/onError/onComplete。 */\n"
            "    static final class TakeUntilMainObserver<T, U> extends AtomicInteger\n    implements Observer<T>, Disposable {",
        ),
        (
            "        void otherComplete() {",
            "        /** other 完成：dispose 上游并以 HalfSerializer onComplete。 */\n"
            "        void otherComplete() {",
        ),
        (
            "        final class OtherObserver extends AtomicReference<Disposable>\n        implements Observer<U> {",
            "        /** other 侧 Observer：onNext 触发 otherComplete。 */\n"
            "        final class OtherObserver extends AtomicReference<Disposable>\n        implements Observer<U> {",
        ),
    ],
    "ObservableTakeUntilPredicate.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableTakeUntilPredicate",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 转发元素直至 predicate.test 为 true（含触发项），\n"
            " * 随后 dispose 上游并 onComplete。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableTakeUntilPredicate",
        ),
        (
            "    public ObservableTakeUntilPredicate(ObservableSource<T> source, Predicate<? super T> predicate) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param predicate 为 true 时停止（含当前元素）\n"
            "     */\n"
            "    public ObservableTakeUntilPredicate(ObservableSource<T> source, Predicate<? super T> predicate) {",
        ),
        (
            "    static final class TakeUntilPredicateObserver<T> implements Observer<T>, Disposable {",
            "    /** 先 onNext 再 test predicate；true 时 dispose 并完成。 */\n"
            "    static final class TakeUntilPredicateObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 转发后 test；predicate 异常转 onError。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "ObservableTakeWhile.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableTakeWhile",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 在 predicate.test 为 true 时转发元素；\n"
            " * 首次 false 时 dispose 上游并 onComplete（不发射该项）。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableTakeWhile",
        ),
        (
            "    public ObservableTakeWhile(ObservableSource<T> source, Predicate<? super T> predicate) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param predicate 为 true 时继续转发\n"
            "     */\n"
            "    public ObservableTakeWhile(ObservableSource<T> source, Predicate<? super T> predicate) {",
        ),
        (
            "    static final class TakeWhileObserver<T> implements Observer<T>, Disposable {",
            "    /** test 为 true 才 onNext；false 时停止且不发射当前项。 */\n"
            "    static final class TakeWhileObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** test 失败则 done 并完成；成功则 downstream.onNext。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "ObservableThrottleFirstTimed.java": [
        (
            "import io.reactivex.rxjava4.observers.SerializedObserver;\n\npublic final class ObservableThrottleFirstTimed",
            "import io.reactivex.rxjava4.observers.SerializedObserver;\n\n"
            "/**\n"
            " * 每个 timeout 窗口内仅转发首个 onNext（gate 门控），\n"
            " * 窗口内其余值可选经 onDropped 回调丢弃。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableThrottleFirstTimed",
        ),
        (
            "    public ObservableThrottleFirstTimed(\n            ObservableSource<T> source,\n            long timeout,\n            TimeUnit unit,\n            Scheduler scheduler,\n            Consumer<? super T> onDropped) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param timeout 节流窗口长度\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 调度窗口 reset 的 Scheduler\n"
            "     * @param onDropped 窗口内被丢弃值的回调（可为 null）\n"
            "     */\n"
            "    public ObservableThrottleFirstTimed(\n            ObservableSource<T> source,\n            long timeout,\n            TimeUnit unit,\n            Scheduler scheduler,\n            Consumer<? super T> onDropped) {",
        ),
        (
            "    static final class DebounceTimedObserver<T>\n    extends AtomicReference<Disposable>\n    implements Observer<T>, Disposable, Runnable {",
            "    /** gate 为 true 时阻塞转发；run 重置 gate 开启下一窗口。 */\n"
            "    static final class DebounceTimedObserver<T>\n    extends AtomicReference<Disposable>\n    implements Observer<T>, Disposable, Runnable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** gate 关闭时转发首项并 schedule reset；否则 onDropped。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 定时任务：gate=false 开启下一节流窗口。 */\n"
            "        @Override\n        public void run() {",
        ),
    ],
    "ObservableThrottleLatest.java": [
        (
            "/**\n * Emits the next or latest item when the given time elapses.\n * <p>\n * The operator emits the next item, then starts a timer. When the timer fires,\n * it tries to emit the latest item from upstream. If there was no upstream item,\n * in the meantime, the next upstream item is emitted immediately and the\n * timed process repeats.\n * <p>History: 2.1.14 - experimental\n * @param <T> the upstream and downstream value type\n * @since 2.2\n */",
            "/**\n"
            " * 节流发射最新值：先发射下一项并启动定时器；\n"
            " * 定时器触发时发射 upstream 最新缓存值。窗口内无新值则下一项立即发射。\n"
            " * <p>History: 2.1.14 - experimental\n"
            " * @param <T> 上下游元素类型\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    public ObservableThrottleLatest(Observable<T> source,\n            long timeout, TimeUnit unit,\n            Scheduler scheduler,\n            boolean emitLast,\n            Consumer<? super T> onDropped) {",
            "    /**\n"
            "     * @param source 上游 Observable\n"
            "     * @param timeout 节流窗口长度\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 调度定时任务的 Scheduler\n"
            "     * @param emitLast 完成时是否发射最后一次缓存值\n"
            "     * @param onDropped 被覆盖旧值的回调（可为 null）\n"
            "     */\n"
            "    public ObservableThrottleLatest(Observable<T> source,\n            long timeout, TimeUnit unit,\n            Scheduler scheduler,\n            boolean emitLast,\n            Consumer<? super T> onDropped) {",
        ),
        (
            "    static final class ThrottleLatestObserver<T>\n    extends AtomicInteger\n    implements Observer<T>, Disposable, Runnable {",
            "    /** latest 缓存最新值；drain 协调 timerRunning/timerFired 与 emitLast。 */\n"
            "    static final class ThrottleLatestObserver<T>\n    extends AtomicInteger\n    implements Observer<T>, Disposable, Runnable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** getAndSet 更新 latest；旧值可选 onDropped。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        void drain() {",
            "        /** wip 门控：定时或立即 emit latest，处理 done/emitLast/onDropped。 */\n"
            "        void drain() {",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 定时 tick：timerFired=true 触发 drain。 */\n"
            "        @Override\n        public void run() {",
        ),
    ],
    "ObservableTimeInterval.java": [
        (
            "import io.reactivex.rxjava4.schedulers.Timed;\n\npublic final class ObservableTimeInterval",
            "import io.reactivex.rxjava4.schedulers.Timed;\n\n"
            "/**\n"
            " * 将上游每个元素包装为 {@link Timed}，\n"
            " * value() 为元素，time() 为与上一项的时间间隔。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableTimeInterval",
        ),
        (
            "    public ObservableTimeInterval(ObservableSource<T> source, TimeUnit unit, Scheduler scheduler) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param unit 时间间隔单位\n"
            "     * @param scheduler 提供 now 时间戳的 Scheduler\n"
            "     */\n"
            "    public ObservableTimeInterval(ObservableSource<T> source, TimeUnit unit, Scheduler scheduler) {",
        ),
        (
            "    static final class TimeIntervalObserver<T> implements Observer<T>, Disposable {",
            "    /** 记录 lastTime；onNext 时计算 delta 并发射 Timed。 */\n"
            "    static final class TimeIntervalObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** delta = now - lastTime，发射 new Timed(t, delta, unit)。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "ObservableTimeout.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableTimeout",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 首项与每项 onNext 后启动 itemTimeoutIndicator 超时监视；\n"
            " * 超时触发 TimeoutException 或切换至 other fallback。\n"
            " * @param <T> 主流元素类型\n"
            " * @param <U> 首次超时指示类型\n"
            " * @param <V> 逐项超时指示类型\n"
            " */\n"
            "public final class ObservableTimeout",
        ),
        (
            "    public ObservableTimeout(\n            Observable<T> source,\n            ObservableSource<U> firstTimeoutIndicator,\n            Function<? super T, ? extends ObservableSource<V>> itemTimeoutIndicator,\n            ObservableSource<? extends T> other) {",
            "    /**\n"
            "     * @param source 主流 Observable\n"
            "     * @param firstTimeoutIndicator 首项前的超时指示（可为 null）\n"
            "     * @param itemTimeoutIndicator 每项映射为超时监视 ObservableSource\n"
            "     * @param other 超时后的 fallback（null 则 onError TimeoutException）\n"
            "     */\n"
            "    public ObservableTimeout(\n            Observable<T> source,\n            ObservableSource<U> firstTimeoutIndicator,\n            Function<? super T, ? extends ObservableSource<V>> itemTimeoutIndicator,\n            ObservableSource<? extends T> other) {",
        ),
        (
            "    static final class TimeoutObserver<T> extends AtomicLong\n    implements Observer<T>, Disposable, TimeoutSelectorSupport {",
            "    /** idx 序号匹配 TimeoutConsumer；超时则 onError 或 fallback。 */\n"
            "    static final class TimeoutObserver<T> extends AtomicLong\n    implements Observer<T>, Disposable, TimeoutSelectorSupport {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 转发 t 后 replace 为 itemTimeoutIndicator 的新 TimeoutConsumer。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onTimeout(long idx) {",
            "        /** idx 匹配时 dispose 上游并 onError(TimeoutException)。 */\n"
            "        @Override\n        public void onTimeout(long idx) {",
        ),
        (
            "    static final class TimeoutFallbackObserver<T>\n    extends AtomicReference<Disposable>\n    implements Observer<T>, Disposable, TimeoutSelectorSupport {",
            "    /** 超时后 subscribe fallback 替代主流。 */\n"
            "    static final class TimeoutFallbackObserver<T>\n    extends AtomicReference<Disposable>\n    implements Observer<T>, Disposable, TimeoutSelectorSupport {",
        ),
        (
            "    static final class TimeoutConsumer extends AtomicReference<Disposable>\n    implements Observer<Object>, Disposable {",
            "    /** 超时指示 Observer：onNext/onComplete 触发 parent.onTimeout(idx)。 */\n"
            "    static final class TimeoutConsumer extends AtomicReference<Disposable>\n    implements Observer<Object>, Disposable {",
        ),
    ],
    "ObservableTimeoutTimed.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableTimeoutTimed",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 每项 onNext 后启动固定 timeout 定时器；\n"
            " * 超时触发 TimeoutException 或切换至 other fallback。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableTimeoutTimed",
        ),
        (
            "    public ObservableTimeoutTimed(Observable<T> source,\n            long timeout, TimeUnit unit, Scheduler scheduler, ObservableSource<? extends T> other) {",
            "    /**\n"
            "     * @param source 上游 Observable\n"
            "     * @param timeout 无新项时的超时长度\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 调度 TimeoutTask 的 Scheduler\n"
            "     * @param other 超时 fallback（null 则 onError）\n"
            "     */\n"
            "    public ObservableTimeoutTimed(Observable<T> source,\n            long timeout, TimeUnit unit, Scheduler scheduler, ObservableSource<? extends T> other) {",
        ),
        (
            "    static final class TimeoutObserver<T> extends AtomicLong\n    implements Observer<T>, Disposable, TimeoutSupport {",
            "    /** 每 onNext 后 startTimeout(idx+1) 重置定时器。 */\n"
            "    static final class TimeoutObserver<T> extends AtomicLong\n    implements Observer<T>, Disposable, TimeoutSupport {",
        ),
        (
            "        void startTimeout(long nextIndex) {",
            "        /** worker.schedule TimeoutTask(nextIndex) 替换旧 task。 */\n"
            "        void startTimeout(long nextIndex) {",
        ),
        (
            "    record TimeoutTask(long idx, TimeoutSupport parent) implements Runnable {",
            "    /** 定时到期调用 parent.onTimeout(idx)。 */\n"
            "    record TimeoutTask(long idx, TimeoutSupport parent) implements Runnable {",
        ),
        (
            "    record FallbackObserver<T>(Observer<? super T> downstream,\n                               AtomicReference<Disposable> arbiter) implements Observer<T> {",
            "    /** fallback 侧 Observer：经 arbiter 转发至 downstream。 */\n"
            "    record FallbackObserver<T>(Observer<? super T> downstream,\n                               AtomicReference<Disposable> arbiter) implements Observer<T> {",
        ),
    ],
    "ObservableTimer.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.*;\n\npublic final class ObservableTimer extends Observable<Long> {",
            "import io.reactivex.rxjava4.internal.disposables.*;\n\n"
            "/**\n"
            " * 延迟 delay 后发射单个 0L 并 onComplete 的冷 Observable。\n"
            " */\n"
            "public final class ObservableTimer extends Observable<Long> {",
        ),
        (
            "    public ObservableTimer(long delay, TimeUnit unit, Scheduler scheduler) {",
            "    /**\n"
            "     * @param delay 延迟时间\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 调度 run 的 Scheduler\n"
            "     */\n"
            "    public ObservableTimer(long delay, TimeUnit unit, Scheduler scheduler) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super Long> observer) {",
            "    /** scheduleDirect 触发 TimerObserver.run 发射 0L。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super Long> observer) {",
        ),
        (
            "    static final class TimerObserver extends AtomicReference<Disposable>\n    implements Disposable, Runnable {",
            "    /** run 时 onNext(0L) 并 lazySet EmptyDisposable 后 onComplete。 */\n"
            "    static final class TimerObserver extends AtomicReference<Disposable>\n    implements Disposable, Runnable {",
        ),
    ],
    "ObservableToList.java": [
        (
            "import io.reactivex.rxjava4.internal.util.ExceptionHelper;\n\npublic final class ObservableToList",
            "import io.reactivex.rxjava4.internal.util.ExceptionHelper;\n\n"
            "/**\n"
            " * 收集上游全部元素至 Supplier 提供的 Collection，\n"
            " * onComplete 时 onNext 该集合并完成。\n"
            " * @param <T> 元素类型\n"
            " * @param <U> Collection 类型\n"
            " */\n"
            "public final class ObservableToList",
        ),
        (
            "    public ObservableToList(ObservableSource<T> source, Supplier<U> collectionSupplier) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param collectionSupplier 提供可变 Collection 的 Supplier\n"
            "     */\n"
            "    public ObservableToList(ObservableSource<T> source, Supplier<U> collectionSupplier) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Observer<? super U> t) {",
            "    /** 调用 collectionSupplier 后订阅 ToListObserver。 */\n"
            "    @Override\n    public void subscribeActual(Observer<? super U> t) {",
        ),
        (
            "    static final class ToListObserver<T, U extends Collection<? super T>> implements Observer<T>, Disposable {",
            "    /** 逐 onNext add；onComplete 时 onNext(collection) 再 onComplete。 */\n"
            "    static final class ToListObserver<T, U extends Collection<? super T>> implements Observer<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 发射累积集合并 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
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
    index_file = Path("/tmp/git-index-rxjava-w20b")
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
        "rxjava 4.0.0-alpha-21: Chinese-annotate wave 20b [15:30]",
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
            "wave20b",
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
        "queue: mark rxjava 4.0.0-alpha-21 wave20b done",
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
