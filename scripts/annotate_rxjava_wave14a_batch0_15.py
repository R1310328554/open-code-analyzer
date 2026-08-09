#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-14a Maybe operators [0:15]."""
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
WAVE14A_FILE = Path("/tmp/rxjava_w14a.txt")
WAVE14B_FILE = Path("/tmp/rxjava_w14b.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE14A_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "MaybeDoOnEvent.java": [
        (
            "/**\n * Calls a BiConsumer with the success, error values of the upstream Maybe or with two nulls if\n * the Maybe completed.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 上游 Maybe 终止时用 {@link BiConsumer} 回调：\n"
            " * onSuccess 传 (value, null)，onError 传 (null, error)，onComplete 传 (null, null)。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeDoOnEvent(MaybeSource<T> source, BiConsumer<? super T, ? super Throwable> onEvent) {",
            "    /**\n"
            "     * @param source 上游 MaybeSource\n"
            "     * @param onEvent 事件回调 BiConsumer\n"
            "     */\n"
            "    public MaybeDoOnEvent(MaybeSource<T> source, BiConsumer<? super T, ? super Throwable> onEvent) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 包装为 DoOnEventMaybeObserver。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class DoOnEventMaybeObserver<T> implements MaybeObserver<T>, Disposable {",
            "    /** 先调用 onEvent 再转发下游；回调异常会中断正常事件。 */\n"
            "    static final class DoOnEventMaybeObserver<T> implements MaybeObserver<T>, Disposable {",
        ),
    ],
    "MaybeDoOnLifecycle.java": [
        (
            "/**\n * Invokes callbacks upon {@code onSubscribe} from upstream and\n * {@code dispose} from downstream.\n *\n * @param <T> the element type of the flow\n * @since 3.0.0\n */",
            "/**\n"
            " * 在上游 {@code onSubscribe} 与下游 {@code dispose} 时调用回调。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " * @since 3.0.0\n"
            " */",
        ),
        (
            "    public MaybeDoOnLifecycle(Maybe<T> upstream, Consumer<? super Disposable> onSubscribe,\n            Action onDispose) {",
            "    /**\n"
            "     * @param upstream 上游 Maybe\n"
            "     * @param onSubscribe 收到上游 Disposable 时调用\n"
            "     * @param onDispose 下游 dispose 时调用\n"
            "     */\n"
            "    public MaybeDoOnLifecycle(Maybe<T> upstream, Consumer<? super Disposable> onSubscribe,\n            Action onDispose) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 包装为 MaybeLifecycleObserver。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "            // this way, multiple calls to onSubscribe can show up in tests that use doOnSubscribe to validate behavior",
            "            // 允许重复 onSubscribe，便于 doOnSubscribe 相关测试验证行为",
        ),
        (
            "    static final class MaybeLifecycleObserver<T> implements MaybeObserver<T>, Disposable {",
            "    /** onSubscribe 回调先于 validate；dispose 时先 onDispose 再 cancel 上游。 */\n"
            "    static final class MaybeLifecycleObserver<T> implements MaybeObserver<T>, Disposable {",
        ),
    ],
    "MaybeDoOnTerminate.java": [
        (
            "import io.reactivex.rxjava4.functions.Action;\n\npublic final class MaybeDoOnTerminate",
            "import io.reactivex.rxjava4.functions.Action;\n\n"
            "/**\n"
            " * 在 onSuccess、onError 或 onComplete 向下游转发前先执行 {@link Action}。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class MaybeDoOnTerminate",
        ),
        (
            "    public MaybeDoOnTerminate(MaybeSource<T> source, Action onTerminate) {",
            "    /**\n"
            "     * @param source 上游 MaybeSource\n"
            "     * @param onTerminate 终止前执行的 Action\n"
            "     */\n"
            "    public MaybeDoOnTerminate(MaybeSource<T> source, Action onTerminate) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 包装为 DoOnTerminate 内部观察者。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    final class DoOnTerminate implements MaybeObserver<T> {",
            "    /** 各终止路径先 run onTerminate，异常则 CompositeException 或 onError。 */\n"
            "    final class DoOnTerminate implements MaybeObserver<T> {",
        ),
    ],
    "MaybeEmpty.java": [
        (
            "/**\n * Signals an onComplete.\n */",
            "/**\n"
            " * 立即向下游发出 onComplete（空 Maybe）。\n"
            " */",
        ),
        (
            "    public static final MaybeEmpty INSTANCE = new MaybeEmpty();",
            "    /** 单例空 Maybe 实例。 */\n"
            "    public static final MaybeEmpty INSTANCE = new MaybeEmpty();",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super Object> observer) {",
            "    /** 通过 EmptyDisposable.complete 完成。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super Object> observer) {",
        ),
        (
            "        return null; // nulls of ScalarCallable are considered empty sources",
            "        return null; // ScalarCallable 返回 null 表示空源",
        ),
    ],
    "MaybeEqualSingle.java": [
        (
            "/**\n * Compares two MaybeSources to see if they are both empty or emit the same value compared\n * via a BiPredicate.\n *\n * @param <T> the common base type of the sources\n */",
            "/**\n"
            " * 比较两个 {@link MaybeSource}：均 empty 则 true；\n"
            " * 均有值则用 {@link BiPredicate} 判定是否相等。\n"
            " *\n"
            " * @param <T> 两源公共元素类型\n"
            " */",
        ),
        (
            "    public MaybeEqualSingle(MaybeSource<? extends T> source1, MaybeSource<? extends T> source2,\n            BiPredicate<? super T, ? super T> isEqual) {",
            "    /**\n"
            "     * @param source1 第一个 MaybeSource\n"
            "     * @param source2 第二个 MaybeSource\n"
            "     * @param isEqual 值相等判定\n"
            "     */\n"
            "    public MaybeEqualSingle(MaybeSource<? extends T> source1, MaybeSource<? extends T> source2,\n            BiPredicate<? super T, ? super T> isEqual) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super Boolean> observer) {",
            "    /** EqualCoordinator 计数两源终止后比较并 onSuccess。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super Boolean> observer) {",
        ),
        (
            "    static final class EqualCoordinator<T>\n    extends AtomicInteger\n    implements Disposable {",
            "    /** 初始计数 2，两源均 done 后比较 value。 */\n"
            "    static final class EqualCoordinator<T>\n    extends AtomicInteger\n    implements Disposable {",
        ),
        (
            "        void done() {",
            "        /** 两源均终止：双 null 为 true，双非 null 用 isEqual。 */\n"
            "        void done() {",
        ),
        (
            "        void error(EqualObserver<T> sender, Throwable ex) {",
            "        /** 首个 error 取消另一源并向下游 onError。 */\n"
            "        void error(EqualObserver<T> sender, Throwable ex) {",
        ),
        (
            "    static final class EqualObserver<T>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T> {",
            "    /** 缓存 onSuccess 值或 onComplete 后通知 parent.done()。 */\n"
            "    static final class EqualObserver<T>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T> {",
        ),
    ],
    "MaybeError.java": [
        (
            "/**\n * Signals a constant Throwable.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 订阅后立即 onError 固定 {@link Throwable}。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeError(Throwable error) {",
            "    /** @param error 要发出的异常 */\n"
            "    public MaybeError(Throwable error) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** onSubscribe(Disposable.disposed()) 后 onError。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
    ],
    "MaybeErrorCallable.java": [
        (
            "/**\n * Signals a Throwable returned by a Supplier.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 订阅时调用 {@link Supplier} 获取 Throwable 并 onError。\n"
            " *\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeErrorCallable(Supplier<? extends Throwable> errorSupplier) {",
            "    /** @param errorSupplier 提供异常的 Supplier */\n"
            "    public MaybeErrorCallable(Supplier<? extends Throwable> errorSupplier) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** Supplier 异常或 null 均转为 onError。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
    ],
    "MaybeFilter.java": [
        (
            "/**\n * Filters the upstream via a predicate, returning the success item or completing if\n * the predicate returns false.\n *\n * @param <T> the upstream value type\n */",
            "/**\n"
            " * 用 {@link Predicate} 过滤上游 onSuccess 值；\n"
            " * 返回 false 时向下游 onComplete。\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " */",
        ),
        (
            "    public MaybeFilter(MaybeSource<T> source, Predicate<? super T> predicate) {",
            "    /**\n"
            "     * @param source 上游 MaybeSource\n"
            "     * @param predicate 过滤谓词\n"
            "     */\n"
            "    public MaybeFilter(MaybeSource<T> source, Predicate<? super T> predicate) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 包装为 FilterMaybeObserver。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class FilterMaybeObserver<T> implements MaybeObserver<T>, Disposable {",
            "    /** predicate.test 为 true 则 onSuccess，否则 onComplete。 */\n"
            "    static final class FilterMaybeObserver<T> implements MaybeObserver<T>, Disposable {",
        ),
    ],
    "MaybeFilterSingle.java": [
        (
            "/**\n * Filters the upstream SingleSource via a predicate, returning the success item or completing if\n * the predicate returns false.\n *\n * @param <T> the upstream value type\n */",
            "/**\n"
            " * 用 {@link Predicate} 过滤上游 {@link SingleSource} 的 onSuccess 值；\n"
            " * 返回 false 时向下游 onComplete。\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " */",
        ),
        (
            "    public MaybeFilterSingle(SingleSource<T> source, Predicate<? super T> predicate) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param predicate 过滤谓词\n"
            "     */\n"
            "    public MaybeFilterSingle(SingleSource<T> source, Predicate<? super T> predicate) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 将 Single 转为 Maybe 并应用 predicate。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class FilterMaybeObserver<T> implements SingleObserver<T>, Disposable {",
            "    /** onSuccess 时 test；false 则 onComplete 而非 onSuccess。 */\n"
            "    static final class FilterMaybeObserver<T> implements SingleObserver<T>, Disposable {",
        ),
    ],
    "MaybeFlatMapBiSelector.java": [
        (
            "/**\n * Maps a source item to another MaybeSource then calls a BiFunction with the\n * original item and the secondary item to generate the final result.\n *\n * @param <T> the main value type\n * @param <U> the second value type\n * @param <R> the result value type\n */",
            "/**\n"
            " * 将源项映射为第二个 {@link MaybeSource}，\n"
            " * 再用 {@link BiFunction} 合并原项与次级项得到最终结果。\n"
            " *\n"
            " * @param <T> 主序列元素类型\n"
            " * @param <U> 次级 Maybe 元素类型\n"
            " * @param <R> 结果类型\n"
            " */",
        ),
        (
            "    public MaybeFlatMapBiSelector(MaybeSource<T> source,\n            Function<? super T, ? extends MaybeSource<? extends U>> mapper,\n            BiFunction<? super T, ? super U, ? extends R> resultSelector) {",
            "    /**\n"
            "     * @param source 上游 MaybeSource\n"
            "     * @param mapper 由 T 映射次级 MaybeSource\n"
            "     * @param resultSelector 合并 T 与 U 为 R\n"
            "     */\n"
            "    public MaybeFlatMapBiSelector(MaybeSource<T> source,\n            Function<? super T, ? extends MaybeSource<? extends U>> mapper,\n            BiFunction<? super T, ? super U, ? extends R> resultSelector) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super R> observer) {",
            "    /** FlatMapBiMainObserver 先 flatMap 再 resultSelector。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super R> observer) {",
        ),
        (
            "    static final class FlatMapBiMainObserver<T, U, R>\n    implements MaybeObserver<T>, Disposable {",
            "    /** onSuccess 时 mapper 得次级源并订阅 InnerObserver。 */\n"
            "    static final class FlatMapBiMainObserver<T, U, R>\n    implements MaybeObserver<T>, Disposable {",
        ),
        (
            "        static final class InnerObserver<T, U, R>\n        extends AtomicReference<Disposable>\n        implements MaybeObserver<U> {",
            "        /** 次级 onSuccess 时用 resultSelector(t, u) 向下游发射。 */\n"
            "        static final class InnerObserver<T, U, R>\n        extends AtomicReference<Disposable>\n        implements MaybeObserver<U> {",
        ),
    ],
    "MaybeFlatMapCompletable.java": [
        (
            "/**\n * Maps the success value of the source MaybeSource into a Completable.\n * @param <T> the value type of the source MaybeSource\n */",
            "/**\n"
            " * 将上游 Maybe 的 onSuccess 值映射为 {@link CompletableSource} 并订阅。\n"
            " * @param <T> 上游元素类型\n"
            " */",
        ),
        (
            "    public MaybeFlatMapCompletable(MaybeSource<T> source, Function<? super T, ? extends CompletableSource> mapper) {",
            "    /**\n"
            "     * @param source 上游 MaybeSource\n"
            "     * @param mapper 由 T 映射 CompletableSource\n"
            "     */\n"
            "    public MaybeFlatMapCompletable(MaybeSource<T> source, Function<? super T, ? extends CompletableSource> mapper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** FlatMapCompletableObserver 同时实现 Maybe 与 Completable 观察者。 */\n"
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
        (
            "    static final class FlatMapCompletableObserver<T>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, CompletableObserver, Disposable {",
            "    /** onSuccess 时 mapper 得 Completable 并 subscribe(this)。 */\n"
            "    static final class FlatMapCompletableObserver<T>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, CompletableObserver, Disposable {",
        ),
    ],
    "MaybeFlatMapIterableFlowable.java": [
        (
            "/**\n * Maps a success value into an Iterable and streams it back as a Flowable.\n *\n * @param <T> the source value type\n * @param <R> the element type of the Iterable\n */",
            "/**\n"
            " * 将 onSuccess 值经 mapper 转为 {@link Iterable}，\n"
            " * 以 {@link Flowable} 形式逐元素向下游发射（支持背压）。\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> Iterable 元素类型\n"
            " */",
        ),
        (
            "    public MaybeFlatMapIterableFlowable(MaybeSource<T> source,\n            Function<? super T, ? extends Iterable<? extends R>> mapper) {",
            "    /**\n"
            "     * @param source 上游 MaybeSource\n"
            "     * @param mapper 由 T 映射 Iterable\n"
            "     */\n"
            "    public MaybeFlatMapIterableFlowable(MaybeSource<T> source,\n            Function<? super T, ? extends Iterable<? extends R>> mapper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
            "    /** FlatMapIterableObserver 实现背压 drain。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
        ),
        (
            "    static final class FlatMapIterableObserver<T, R>\n    extends BasicIntQueueSubscription<R>\n    implements MaybeObserver<T> {",
            "    /** onSuccess 得 iterator；drain 按 requested 发射。 */\n"
            "    static final class FlatMapIterableObserver<T, R>\n    extends BasicIntQueueSubscription<R>\n    implements MaybeObserver<T> {",
        ),
        (
            "        void fastPath(Subscriber<? super R> a, Iterator<? extends R> iterator) {",
            "        /** request 为 MAX 时无背压地迭代发射。 */\n"
            "        void fastPath(Subscriber<? super R> a, Iterator<? extends R> iterator) {",
        ),
        (
            "        void drain() {",
            "        /** 按 requested 计数发射；迭代器耗尽则 onComplete。 */\n"
            "        void drain() {",
        ),
    ],
    "MaybeFlatMapIterableObservable.java": [
        (
            "/**\n * Maps a success value into an Iterable and streams it back as a Flowable.\n *\n * @param <T> the source value type\n * @param <R> the element type of the Iterable\n */",
            "/**\n"
            " * 将 onSuccess 值经 mapper 转为 {@link Iterable}，\n"
            " * 以 {@link Observable} 形式逐元素向下游发射（无背压）。\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> Iterable 元素类型\n"
            " */",
        ),
        (
            "    public MaybeFlatMapIterableObservable(MaybeSource<T> source,\n            Function<? super T, ? extends Iterable<? extends R>> mapper) {",
            "    /**\n"
            "     * @param source 上游 MaybeSource\n"
            "     * @param mapper 由 T 映射 Iterable\n"
            "     */\n"
            "    public MaybeFlatMapIterableObservable(MaybeSource<T> source,\n            Function<? super T, ? extends Iterable<? extends R>> mapper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
            "    /** onSuccess 后同步迭代发射全部元素。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
        ),
        (
            "    static final class FlatMapIterableObserver<T, R>\n    extends BasicQueueDisposable<R>\n    implements MaybeObserver<T> {",
            "    /** outputFused 时 onNext(null)+onComplete；否则 for 循环迭代。 */\n"
            "    static final class FlatMapIterableObserver<T, R>\n    extends BasicQueueDisposable<R>\n    implements MaybeObserver<T> {",
        ),
    ],
    "MaybeFlatMapNotification.java": [
        (
            "/**\n * Maps a value into a MaybeSource and relays its signal.\n *\n * @param <T> the source value type\n * @param <R> the result value type\n */",
            "/**\n"
            " * 按上游事件类型分别映射为新的 {@link MaybeSource} 并转发其信号：\n"
            " * onSuccess/onError/onComplete 各对应独立 mapper/supplier。\n"
            " *\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> 结果类型\n"
            " */",
        ),
        (
            "    public MaybeFlatMapNotification(MaybeSource<T> source,\n            Function<? super T, ? extends MaybeSource<? extends R>> onSuccessMapper,\n            Function<? super Throwable, ? extends MaybeSource<? extends R>> onErrorMapper,\n            Supplier<? extends MaybeSource<? extends R>> onCompleteSupplier) {",
            "    /**\n"
            "     * @param source 上游 MaybeSource\n"
            "     * @param onSuccessMapper onSuccess 时的映射\n"
            "     * @param onErrorMapper onError 时的映射\n"
            "     * @param onCompleteSupplier onComplete 时的 Maybe 供应\n"
            "     */\n"
            "    public MaybeFlatMapNotification(MaybeSource<T> source,\n            Function<? super T, ? extends MaybeSource<? extends R>> onSuccessMapper,\n            Function<? super Throwable, ? extends MaybeSource<? extends R>> onErrorMapper,\n            Supplier<? extends MaybeSource<? extends R>> onCompleteSupplier) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super R> observer) {",
            "    /** FlatMapMaybeObserver 按事件选源并 InnerObserver 转发。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super R> observer) {",
        ),
        (
            "    static final class FlatMapMaybeObserver<T, R>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable {",
            "    /** 三种终止路径各 subscribe InnerObserver。 */\n"
            "    static final class FlatMapMaybeObserver<T, R>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable {",
        ),
        (
            "        final class InnerObserver implements MaybeObserver<R> {",
            "        /** 内层 Maybe 信号直接 relay 到 downstream。 */\n"
            "        final class InnerObserver implements MaybeObserver<R> {",
        ),
    ],
    "MaybeFlatMapSingle.java": [
        (
            "/**\n * Maps the success value of the source MaybeSource into a Single.\n * <p>History: 2.0.2 - experimental\n * @param <T> the input value type\n * @param <R> the result value type\n * @since 2.1\n */",
            "/**\n"
            " * 将上游 Maybe 的 onSuccess 值映射为 {@link SingleSource} 并订阅，\n"
            " * 以 Maybe 形式向下游发射 Single 结果。\n"
            " * <p>History: 2.0.2 - experimental\n"
            " * @param <T> 输入元素类型\n"
            " * @param <R> 结果类型\n"
            " * @since 2.1\n"
            " */",
        ),
        (
            "    public MaybeFlatMapSingle(MaybeSource<T> source, Function<? super T, ? extends SingleSource<? extends R>> mapper) {",
            "    /**\n"
            "     * @param source 上游 MaybeSource\n"
            "     * @param mapper 由 T 映射 SingleSource\n"
            "     */\n"
            "    public MaybeFlatMapSingle(MaybeSource<T> source, Function<? super T, ? extends SingleSource<? extends R>> mapper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super R> downstream) {",
            "    /** FlatMapMaybeObserver onSuccess 时 subscribe FlatMapSingleObserver。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super R> downstream) {",
        ),
        (
            "    static final class FlatMapMaybeObserver<T, R>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable {",
            "    /** 持有 mapper；onSuccess 后切换到 Single 订阅。 */\n"
            "    static final class FlatMapMaybeObserver<T, R>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable {",
        ),
        (
            "    record FlatMapSingleObserver<R>(AtomicReference<Disposable> parent,\n                                    MaybeObserver<? super R> downstream) implements SingleObserver<R> {",
            "    /** Single 结果 relay 到 Maybe downstream；Disposable 写入 parent。 */\n"
            "    record FlatMapSingleObserver<R>(AtomicReference<Disposable> parent,\n                                    MaybeObserver<? super R> downstream) implements SingleObserver<R> {",
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
            "wave14a Maybe* [0:15]",
            *files,
        ],
        check=True,
    )
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    file_set = set(files)
    w14b = [
        ln.strip()
        for ln in WAVE14B_FILE.read_text(encoding="utf-8").splitlines()
        if ln.strip()
    ]
    batch["files"] = w14b
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
        print(f"Marked {ok} files done in queue (note=wave14a)")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
