#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-15b Maybe/mixed operators [15:30]."""
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
WAVE15B_FILE = Path("/tmp/rxjava_w15b.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE15B_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "MaybeTimeoutPublisher.java": [
        (
            "/**\n * Switches to the fallback Maybe if the other Publisher signals a success or completes, or\n * signals TimeoutException if fallback is null.\n * \n * @param <T> the main value type\n * @param <U> the other value type\n */",
            "/**\n"
            " * 若辅助 {@link Publisher} 在主流 Maybe 终止前先 onNext 或 onComplete，\n"
            " * 则切换到 fallback {@link MaybeSource}；fallback 为 null 时抛出 {@link TimeoutException}。\n"
            " *\n * @param <T> 主流元素类型\n"
            " * @param <U> 辅助 Publisher 元素类型\n"
            " */",
        ),
        (
            "    public MaybeTimeoutPublisher(MaybeSource<T> source, Publisher<U> other, MaybeSource<? extends T> fallback) {",
            "    /**\n"
            "     * @param source 主流 Maybe\n"
            "     * @param other 超时触发用的辅助 Publisher\n"
            "     * @param fallback 超时后切换的 Maybe（可为 null）\n"
            "     */\n"
            "    public MaybeTimeoutPublisher(MaybeSource<T> source, Publisher<U> other, MaybeSource<? extends T> fallback) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 同时订阅 other 与 source，由 TimeoutMainMaybeObserver 协调超时逻辑。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class TimeoutMainMaybeObserver<T, U>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable {",
            "    /** 协调主流与辅助 Publisher，处理超时切换与信号转发。 */\n"
            "    static final class TimeoutMainMaybeObserver<T, U>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable {",
        ),
        (
            "        public void otherComplete() {",
            "        /** 辅助 Publisher 完成或 onNext 时触发：无 fallback 则 onError(TimeoutException)，否则订阅 fallback。 */\n"
            "        public void otherComplete() {",
        ),
        (
            "    static final class TimeoutOtherMaybeObserver<T, U>\n    extends AtomicReference<Subscription>\n    implements FlowableSubscriber<Object> {",
            "    /** 订阅辅助 Publisher，onNext/onComplete 时通知 parent 超时。 */\n"
            "    static final class TimeoutOtherMaybeObserver<T, U>\n    extends AtomicReference<Subscription>\n    implements FlowableSubscriber<Object> {",
        ),
        (
            "    static final class TimeoutFallbackMaybeObserver<T>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T> {",
            "    /** 转发 fallback Maybe 的 onSuccess/onError/onComplete。 */\n"
            "    static final class TimeoutFallbackMaybeObserver<T>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T> {",
        ),
    ],
    "MaybeTimer.java": [
        (
            "/**\n * Signals a {@code 0L} after the specified delay.\n */",
            "/**\n * 在指定延迟后以 onSuccess 发射 {@code 0L}。\n */",
        ),
        (
            "    public MaybeTimer(long delay, TimeUnit unit, Scheduler scheduler) {",
            "    /**\n"
            "     * @param delay 延迟量\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 调度定时任务的 Scheduler\n"
            "     */\n"
            "    public MaybeTimer(long delay, TimeUnit unit, Scheduler scheduler) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final MaybeObserver<? super Long> observer) {",
            "    /** 在 scheduler 上调度 TimerDisposable，到期后 onSuccess(0L)。 */\n"
            "    @Override\n    protected void subscribeActual(final MaybeObserver<? super Long> observer) {",
        ),
        (
            "    static final class TimerDisposable extends AtomicReference<Disposable> implements Disposable, Runnable {",
            "    /** 定时 Runnable，到期向 downstream 发射 0L。 */\n"
            "    static final class TimerDisposable extends AtomicReference<Disposable> implements Disposable, Runnable {",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 定时到期，向 downstream 发射 0L。 */\n"
            "        @Override\n        public void run() {",
        ),
    ],
    "MaybeToFlowable.java": [
        (
            "/**\n * Wraps a MaybeSource and exposes it as a Flowable, relaying signals in a backpressure-aware manner\n * and composes cancellation through.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 将 {@link MaybeSource} 包装为 {@link Flowable}，\n"
            " * 以支持背压的方式转发信号，并贯通取消。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeToFlowable(MaybeSource<T> source) {",
            "    /** @param source 上游 MaybeSource */\n"
            "    public MaybeToFlowable(MaybeSource<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 订阅 MaybeToFlowableSubscriber 并转发 Maybe 信号。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class MaybeToFlowableSubscriber<T> extends DeferredScalarSubscription<T>\n    implements MaybeObserver<T> {",
            "    /** onSuccess 经 DeferredScalarSubscription 背压发射；cancel 时 dispose 上游。 */\n"
            "    static final class MaybeToFlowableSubscriber<T> extends DeferredScalarSubscription<T>\n    implements MaybeObserver<T> {",
        ),
    ],
    "MaybeToObservable.java": [
        (
            "/**\n * Wraps a MaybeSource and exposes it as an Observable, relaying signals in a backpressure-aware manner\n * and composes cancellation through.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 将 {@link MaybeSource} 包装为 {@link Observable}，\n"
            " * 转发 Maybe 信号并贯通 dispose。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeToObservable(MaybeSource<T> source) {",
            "    /** @param source 上游 MaybeSource */\n"
            "    public MaybeToObservable(MaybeSource<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 用 create 包装 Observer 后订阅上游 Maybe。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    /**\n     * Creates a {@link MaybeObserver} wrapper around a {@link Observer}.\n     * <p>History: 2.1.11 - experimental\n     * @param <T> the value type\n     * @param downstream the downstream {@code Observer} to talk to\n     * @return the new MaybeObserver instance\n     * @since 2.2\n     */",
            "    /**\n"
            "     * 将 {@link Observer} 包装为 {@link MaybeObserver}。\n"
            "     * <p>History: 2.1.11 - experimental\n"
            "     * @param <T> 元素类型\n"
            "     * @param downstream 下游 Observer\n"
            "     * @return 新的 MaybeObserver 实例\n"
            "     * @since 2.2\n"
            "     */",
        ),
        (
            "    static final class MaybeToObservableObserver<T> extends DeferredScalarDisposable<T>\n    implements MaybeObserver<T> {",
            "    /** onSuccess 经 DeferredScalarDisposable 发射；dispose 时 dispose 上游。 */\n"
            "    static final class MaybeToObservableObserver<T> extends DeferredScalarDisposable<T>\n    implements MaybeObserver<T> {",
        ),
    ],
    "MaybeToPublisher.java": [
        (
            "/**\n * Helper function to merge/concat values of each MaybeSource provided by a Publisher.\n */",
            "/**\n"
            " * 将 {@link MaybeSource} 映射为 {@link Publisher} 的辅助 {@link Function}，\n"
            " * 供 Publisher 提供的各 MaybeSource 合并/串联时使用。\n"
            " */",
        ),
        (
            "    @SuppressWarnings({ \"rawtypes\", \"unchecked\" })\n    public static <T> Function<MaybeSource<T>, Publisher<T>> instance() {",
            "    /** @return 单例 Function 实例 */\n"
            "    @SuppressWarnings({ \"rawtypes\", \"unchecked\" })\n    public static <T> Function<MaybeSource<T>, Publisher<T>> instance() {",
        ),
        (
            "    @Override\n    public Publisher<Object> apply(MaybeSource<Object> t) {",
            "    /** 将 MaybeSource 包装为 {@link MaybeToFlowable}。 */\n"
            "    @Override\n    public Publisher<Object> apply(MaybeSource<Object> t) {",
        ),
    ],
    "MaybeToSingle.java": [
        (
            "/**\n * Wraps a MaybeSource and exposes its onSuccess and onError signals and signals\n * NoSuchElementException for onComplete if {@code defaultValue} is null.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 将 {@link MaybeSource} 包装为 {@link Single}，\n"
            " * onSuccess/onError 原样转发；onComplete 时若 {@code defaultValue} 为 null 则 onError({@link NoSuchElementException})。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeToSingle(MaybeSource<T> source, T defaultValue) {",
            "    /**\n"
            "     * @param source 上游 MaybeSource\n"
            "     * @param defaultValue onComplete 时的默认值（null 表示空源报错）\n"
            "     */\n"
            "    public MaybeToSingle(MaybeSource<T> source, T defaultValue) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 订阅 ToSingleMaybeSubscriber 并映射 Maybe 信号为 Single 信号。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class ToSingleMaybeSubscriber<T> implements MaybeObserver<T>, Disposable {",
            "    /** 将 Maybe 的 onSuccess/onError/onComplete 映射为 Single 信号。 */\n"
            "    static final class ToSingleMaybeSubscriber<T> implements MaybeObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** onComplete 时发射 defaultValue 或 NoSuchElementException。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "MaybeUnsafeCreate.java": [
        (
            "/**\n * Wraps a MaybeSource without safeguard and calls its subscribe() method for each MaybeObserver.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 无防护地包装 {@link MaybeSource}，对每个 {@link MaybeObserver} 直接调用其 subscribe()。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeUnsafeCreate(MaybeSource<T> source) {",
            "    /** @param source 上游 MaybeSource（须自行保证协议安全） */\n"
            "    public MaybeUnsafeCreate(MaybeSource<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 直接将 observer 传给上游 subscribe，无额外包装。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
    ],
    "MaybeUnsubscribeOn.java": [
        (
            "/**\n * Makes sure the dispose() call from downstream happens on the specified scheduler.\n * \n * @param <T> the value type\n */",
            "/**\n"
            " * 确保下游的 dispose() 在指定 {@link Scheduler} 上执行。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeUnsubscribeOn(MaybeSource<T> source, Scheduler scheduler) {",
            "    /**\n"
            "     * @param source 上游 Maybe\n"
            "     * @param scheduler 执行 dispose 的 Scheduler\n"
            "     */\n"
            "    public MaybeUnsubscribeOn(MaybeSource<T> source, Scheduler scheduler) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 订阅 UnsubscribeOnMaybeObserver，在 scheduler 上调度上游 dispose。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class UnsubscribeOnMaybeObserver<T> extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable, Runnable {",
            "    /** dispose 时在 scheduler 上调度 run() 以 dispose 上游。 */\n"
            "    static final class UnsubscribeOnMaybeObserver<T> extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable, Runnable {",
        ),
        (
            "        @Override\n        public void dispose() {",
            "        /** 在 scheduler 上调度上游 dispose。 */\n"
            "        @Override\n        public void dispose() {",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 在 scheduler 线程上 dispose 上游 Disposable。 */\n"
            "        @Override\n        public void run() {",
        ),
    ],
    "MaybeUsing.java": [
        (
            "/**\n * Creates a resource and a dependent Maybe for each incoming Observer and optionally\n * disposes the resource eagerly (before the terminal event is sent out).\n *\n * @param <T> the value type\n * @param <D> the resource type\n */",
            "/**\n"
            " * 为每个订阅者创建资源及依赖的 {@link Maybe}，\n"
            " * 可选在终止事件发出前 eagerly 释放资源。\n"
            " *\n * @param <T> 元素类型\n"
            " * @param <D> 资源类型\n"
            " */",
        ),
        (
            "    public MaybeUsing(Supplier<? extends D> resourceSupplier,\n            Function<? super D, ? extends MaybeSource<? extends T>> sourceSupplier,\n            Consumer<? super D> resourceDisposer,\n            boolean eager) {",
            "    /**\n"
            "     * @param resourceSupplier 资源供应函数\n"
            "     * @param sourceSupplier 由资源创建 MaybeSource 的函数\n"
            "     * @param resourceDisposer 资源释放回调\n"
            "     * @param eager true 时在终止事件前释放资源\n"
            "     */\n"
            "    public MaybeUsing(Supplier<? extends D> resourceSupplier,\n            Function<? super D, ? extends MaybeSource<? extends T>> sourceSupplier,\n            Consumer<? super D> resourceDisposer,\n            boolean eager) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 获取资源、创建 MaybeSource 并订阅 UsingObserver。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class UsingObserver<T, D>\n    extends AtomicReference<Object>\n    implements MaybeObserver<T>, Disposable {",
            "    /** 持有资源并在 eager/非 eager 模式下于适当时机释放。 */\n"
            "    static final class UsingObserver<T, D>\n    extends AtomicReference<Object>\n    implements MaybeObserver<T>, Disposable {",
        ),
        (
            "        @SuppressWarnings(\"unchecked\")\n        void disposeResource() {",
            "        /** 调用 resourceDisposer 释放资源（若尚未释放）。 */\n"
            "        @SuppressWarnings(\"unchecked\")\n        void disposeResource() {",
        ),
    ],
    "MaybeZipArray.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class MaybeZipArray<T, R> extends Maybe<R> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 等待多个 {@link MaybeSource} 均 onSuccess 后，\n"
            " * 用 zipper 将各值组合为单个结果；任一 onError/onComplete 则终止。\n"
            " *\n * @param <T> 各源元素类型\n"
            " * @param <R> 组合结果类型\n"
            " */\n"
            "public final class MaybeZipArray<T, R> extends Maybe<R> {",
        ),
        (
            "    public MaybeZipArray(MaybeSource<? extends T>[] sources, Function<? super Object[], ? extends R> zipper) {",
            "    /**\n"
            "     * @param sources 待 zip 的 MaybeSource 数组\n"
            "     * @param zipper 将 Object[] 映射为结果的函数\n"
            "     */\n"
            "    public MaybeZipArray(MaybeSource<? extends T>[] sources, Function<? super Object[], ? extends R> zipper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super R> observer) {",
            "    /** 单源时退化为 map；多源时用 ZipCoordinator 等待全部 onSuccess。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super R> observer) {",
        ),
        (
            "    static final class ZipCoordinator<T, R> extends AtomicInteger implements Disposable {",
            "    /** 协调多个 ZipMaybeObserver，全部 onSuccess 后应用 zipper。 */\n"
            "    static final class ZipCoordinator<T, R> extends AtomicInteger implements Disposable {",
        ),
        (
            "        void innerSuccess(T value, int index) {",
            "        /** 记录第 index 个值；全部到齐后应用 zipper 并 onSuccess。 */\n"
            "        void innerSuccess(T value, int index) {",
        ),
        (
            "        void innerComplete(int index) {",
            "        /** 任一源 onComplete 则取消其余并向下游 onComplete。 */\n"
            "        void innerComplete(int index) {",
        ),
        (
            "    static final class ZipMaybeObserver<T>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T> {",
            "    /** 单个源的 observer，将信号转发给 ZipCoordinator。 */\n"
            "    static final class ZipMaybeObserver<T>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T> {",
        ),
    ],
    "MaybeZipIterable.java": [
        (
            "import io.reactivex.rxjava4.internal.operators.maybe.MaybeZipArray.ZipCoordinator;\n\npublic final class MaybeZipIterable<T, R> extends Maybe<R> {",
            "import io.reactivex.rxjava4.internal.operators.maybe.MaybeZipArray.ZipCoordinator;\n\n"
            "/**\n"
            " * 与 {@link MaybeZipArray} 类似，但从 {@link Iterable} 收集 MaybeSource 再 zip。\n"
            " *\n * @param <T> 各源元素类型\n"
            " * @param <R> 组合结果类型\n"
            " */\n"
            "public final class MaybeZipIterable<T, R> extends Maybe<R> {",
        ),
        (
            "    public MaybeZipIterable(Iterable<? extends MaybeSource<? extends T>> sources, Function<? super Object[], ? extends R> zipper) {",
            "    /**\n"
            "     * @param sources 待 zip 的 MaybeSource 可迭代集合\n"
            "     * @param zipper 将 Object[] 映射为结果的函数\n"
            "     */\n"
            "    public MaybeZipIterable(Iterable<? extends MaybeSource<? extends T>> sources, Function<? super Object[], ? extends R> zipper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super R> observer) {",
            "    /** 从 Iterable 收集源；空 Iterable 时 onComplete，单源退化为 map。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super R> observer) {",
        ),
    ],
    "CompletableAndThenObservable.java": [
        (
            "/**\n * After Completable completes, it relays the signals\n * of the ObservableSource to the downstream observer.\n * \n * @param <R> the result type of the ObservableSource and this operator\n * @since 2.1.15\n */",
            "/**\n"
            " * {@link CompletableSource} 正常完成后，\n"
            " * 再订阅并转发 {@link ObservableSource} 的信号。\n"
            " *\n * @param <R> ObservableSource 及本算子的结果类型\n"
            " * @since 2.1.15\n"
            " */",
        ),
        (
            "    public CompletableAndThenObservable(CompletableSource source,\n            ObservableSource<? extends R> other) {",
            "    /**\n"
            "     * @param source 先执行的 CompletableSource\n"
            "     * @param other source 完成后订阅的 ObservableSource\n"
            "     */\n"
            "    public CompletableAndThenObservable(CompletableSource source,\n            ObservableSource<? extends R> other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
            "    /** 先订阅 source，完成后再订阅 other。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
        ),
        (
            "    static final class AndThenObservableObserver<R>\n    extends AtomicReference<Disposable>\n    implements Observer<R>, CompletableObserver, Disposable {",
            "    /** 先作为 CompletableObserver 等待 source 完成，再作为 Observer 转发 other 信号。 */\n"
            "    static final class AndThenObservableObserver<R>\n    extends AtomicReference<Disposable>\n    implements Observer<R>, CompletableObserver, Disposable {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** Completable 完成时订阅 other；other 为 null 则直接 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "CompletableAndThenPublisher.java": [
        (
            "/**\n * After Completable completes, it relays the signals\n * of the Publisher to the downstream subscriber.\n * \n * @param <R> the result type of the Publisher and this operator\n * @since 2.1.15\n */",
            "/**\n"
            " * {@link CompletableSource} 正常完成后，\n"
            " * 再订阅并转发 {@link Publisher} 的信号。\n"
            " *\n * @param <R> Publisher 及本算子的结果类型\n"
            " * @since 2.1.15\n"
            " */",
        ),
        (
            "    public CompletableAndThenPublisher(CompletableSource source,\n            Publisher<? extends R> other) {",
            "    /**\n"
            "     * @param source 先执行的 CompletableSource\n"
            "     * @param other source 完成后订阅的 Publisher\n"
            "     */\n"
            "    public CompletableAndThenPublisher(CompletableSource source,\n            Publisher<? extends R> other) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
            "    /** 先订阅 source，完成后再订阅 other。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
        ),
        (
            "    static final class AndThenPublisherSubscriber<R>\n    extends AtomicReference<Subscription>\n    implements FlowableSubscriber<R>, CompletableObserver, Subscription {",
            "    /** 先作为 CompletableObserver 等待 source 完成，再作为 Subscriber 转发 other 信号。 */\n"
            "    static final class AndThenPublisherSubscriber<R>\n    extends AtomicReference<Subscription>\n    implements FlowableSubscriber<R>, CompletableObserver, Subscription {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** Completable 完成时订阅 other；other 为 null 则直接 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "ConcatMapXMainObserver.java": [
        (
            "/**\n * Base class for implementing concatMapX main observers.\n *\n * @param <T> the upstream value type\n * @since 3.0.10\n */",
            "/**\n"
            " * 实现 concatMapX 系列算子主流 {@link Observer} 的基类，\n"
            " * 管理队列、融合与 drain 调度。\n"
            " *\n * @param <T> 上游元素类型\n"
            " * @since 3.0.10\n"
            " */",
        ),
        (
            "    public ConcatMapXMainObserver(int prefetch, ErrorMode errorMode) {",
            "    /**\n"
            "     * @param prefetch 预取队列容量\n"
            "     * @param errorMode 错误处理模式\n"
            "     */\n"
            "    public ConcatMapXMainObserver(int prefetch, ErrorMode errorMode) {",
        ),
        (
            "        // In async fusion mode, t is a drain indicator",
            "        // 异步融合模式下，t 为 drain 指示符",
        ),
        (
            "    /**\n     * Override this to clear values when the downstream disposes.\n     */",
            "    /** 下游 dispose 时覆写此方法以清理缓存值。 */",
        ),
        (
            "    /**\n     * Typically, this should be {@code downstream.onSubscribe(this)}.\n     */",
            "    /** 通常应为 {@code downstream.onSubscribe(this)}。 */",
        ),
        (
            "    /**\n     * Typically, this should be {@code inner.dispose()}.\n     */",
            "    /** 通常应为 {@code inner.dispose()}。 */",
        ),
        (
            "    /**\n     * Implement the serialized inner subscribing and value emission here.\n     */",
            "    /** 在此实现串行化的内部订阅与值发射逻辑。 */",
        ),
    ],
    "ConcatMapXMainSubscriber.java": [
        (
            "/**\n * Base class for implementing concatMapX main subscribers.\n *\n * @param <T> the upstream value type\n * @since 3.0.10\n */",
            "/**\n"
            " * 实现 concatMapX 系列算子主流 {@link FlowableSubscriber} 的基类，\n"
            " * 管理队列、背压请求与 drain 调度。\n"
            " *\n * @param <T> 上游元素类型\n"
            " * @since 3.0.10\n"
            " */",
        ),
        (
            "    public ConcatMapXMainSubscriber(int prefetch, ErrorMode errorMode) {",
            "    /**\n"
            "     * @param prefetch 预取队列容量\n"
            "     * @param errorMode 错误处理模式\n"
            "     */\n"
            "    public ConcatMapXMainSubscriber(int prefetch, ErrorMode errorMode) {",
        ),
        (
            "        // In async fusion mode, t is a drain indicator",
            "        // 异步融合模式下，t 为 drain 指示符",
        ),
        (
            "    /**\n     * Override this to clear values when the downstream disposes.\n     */",
            "    /** 下游 cancel 时覆写此方法以清理缓存值。 */",
        ),
        (
            "    /**\n     * Typically, this should be {@code downstream.onSubscribe(this);}.\n     */",
            "    /** 通常应为 {@code downstream.onSubscribe(this);}。 */",
        ),
        (
            "    /**\n     * Typically, this should be {@code inner.dispose()}.\n     */",
            "    /** 通常应为 {@code inner.dispose()}。 */",
        ),
        (
            "    /**\n     * Implement the serialized inner subscribing and value emission here.\n     */",
            "    /** 在此实现串行化的内部订阅与值发射逻辑。 */",
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
            "wave15b",
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
        print(f"Marked {ok} files done in queue (note=wave15b)")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
