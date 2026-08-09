#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-16b mixed/observable operators [15:30]."""
from __future__ import annotations

import json
import re
import shutil
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "rxjava/4.0.0-alpha-21"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
WAVE16B_FILE = Path("/tmp/rxjava_w16b.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE16B_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ObservableConcatMapMaybe.java": [
        (
            "/**\n * Maps each upstream item into a {@link MaybeSource}, subscribes to them one after the other terminates\n * and relays their success values, optionally delaying any errors till the main and inner sources\n * terminate.\n * <p>History: 2.1.11 - experimental\n * @param <T> the upstream element type\n * @param <R> the output element type\n * @since 2.2\n */",
            "/**\n"
            " * 将上游各元素映射为 {@link MaybeSource}，待前一个内部源终止后再订阅下一个，\n"
            " * 并转发其 onSuccess 值；可按 {@link ErrorMode} 延迟错误至主流与内部源均终止。\n"
            " * <p>History: 2.1.11 - experimental\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> 下游元素类型\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    public ObservableConcatMapMaybe(Observable<T> source,\n            Function<? super T, ? extends MaybeSource<? extends R>> mapper,\n                    ErrorMode errorMode, int prefetch) {",
            "    /**\n"
            "     * @param source 上游 Observable\n"
            "     * @param mapper 将元素映射为 MaybeSource 的函数\n"
            "     * @param errorMode 错误处理模式\n"
            "     * @param prefetch 预取队列容量\n"
            "     */\n"
            "    public ObservableConcatMapMaybe(Observable<T> source,\n            Function<? super T, ? extends MaybeSource<? extends R>> mapper,\n                    ErrorMode errorMode, int prefetch) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
            "    /** 标量源走 {@link ScalarXMapZHelper#tryAsMaybe} 快速路径，否则订阅 ConcatMapMaybeMainObserver。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
        ),
        (
            "    static final class ConcatMapMaybeMainObserver<T, R>\n    extends ConcatMapXMainObserver<T> {",
            "    /** 串行 concatMap Maybe：管理内部 Maybe 订阅与 drain 状态机。 */\n"
            "    static final class ConcatMapMaybeMainObserver<T, R>\n    extends ConcatMapXMainObserver<T> {",
        ),
        (
            "        /** No inner MaybeSource is running. */\n        static final int STATE_INACTIVE = 0;\n        /** An inner MaybeSource is running but there are no results yet. */\n        static final int STATE_ACTIVE = 1;\n        /** The inner MaybeSource succeeded with a value in {@link #item}. */\n        static final int STATE_RESULT_VALUE = 2;",
            "        /** 无内部 MaybeSource 在运行。 */\n        static final int STATE_INACTIVE = 0;\n        /** 内部 MaybeSource 运行中，尚无结果。 */\n        static final int STATE_ACTIVE = 1;\n        /** 内部 MaybeSource 已成功，值在 {@link #item} 中。 */\n        static final int STATE_RESULT_VALUE = 2;",
        ),
        (
            "        void innerSuccess(R item) {",
            "        /** 内部 onSuccess：缓存值并触发 drain。 */\n"
            "        void innerSuccess(R item) {",
        ),
        (
            "        void innerComplete() {",
            "        /** 内部 onComplete：重置为 INACTIVE 并 drain。 */\n"
            "        void innerComplete() {",
        ),
        (
            "        void innerError(Throwable ex) {",
            "        /** 内部 onError：按 errorMode 记录错误并 drain。 */\n"
            "        void innerError(Throwable ex) {",
        ),
        (
            "        static final class ConcatMapMaybeObserver<R>\n        extends AtomicReference<Disposable>\n        implements MaybeObserver<R> {",
            "        /** 订阅单个内部 MaybeSource 并将信号转发给 parent。 */\n"
            "        static final class ConcatMapMaybeObserver<R>\n        extends AtomicReference<Disposable>\n        implements MaybeObserver<R> {",
        ),
    ],
    "ObservableConcatMapSingle.java": [
        (
            "/**\n * Maps each upstream item into a {@link SingleSource}, subscribes to them one after the other terminates\n * and relays their success values, optionally delaying any errors till the main and inner sources\n * terminate.\n * <p>History: 2.1.11 - experimental\n * @param <T> the upstream element type\n * @param <R> the output element type\n * @since 2.2\n */",
            "/**\n"
            " * 将上游各元素映射为 {@link SingleSource}，待前一个内部源终止后再订阅下一个，\n"
            " * 并转发其 onSuccess 值；可按 {@link ErrorMode} 延迟错误至主流与内部源均终止。\n"
            " * <p>History: 2.1.11 - experimental\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> 下游元素类型\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    public ObservableConcatMapSingle(ObservableSource<T> source,\n            Function<? super T, ? extends SingleSource<? extends R>> mapper,\n                    ErrorMode errorMode, int prefetch) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param mapper 将元素映射为 SingleSource 的函数\n"
            "     * @param errorMode 错误处理模式\n"
            "     * @param prefetch 预取队列容量\n"
            "     */\n"
            "    public ObservableConcatMapSingle(ObservableSource<T> source,\n            Function<? super T, ? extends SingleSource<? extends R>> mapper,\n                    ErrorMode errorMode, int prefetch) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
            "    /** 标量源走 {@link ScalarXMapZHelper#tryAsSingle} 快速路径，否则订阅 ConcatMapSingleMainObserver。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
        ),
        (
            "    static final class ConcatMapSingleMainObserver<T, R>\n    extends ConcatMapXMainObserver<T> {",
            "    /** 串行 concatMap Single：管理内部 Single 订阅与 drain 状态机。 */\n"
            "    static final class ConcatMapSingleMainObserver<T, R>\n    extends ConcatMapXMainObserver<T> {",
        ),
        (
            "        /** No inner SingleSource is running. */\n        static final int STATE_INACTIVE = 0;\n        /** An inner SingleSource is running but there are no results yet. */\n        static final int STATE_ACTIVE = 1;\n        /** The inner SingleSource succeeded with a value in {@link #item}. */\n        static final int STATE_RESULT_VALUE = 2;",
            "        /** 无内部 SingleSource 在运行。 */\n        static final int STATE_INACTIVE = 0;\n        /** 内部 SingleSource 运行中，尚无结果。 */\n        static final int STATE_ACTIVE = 1;\n        /** 内部 SingleSource 已成功，值在 {@link #item} 中。 */\n        static final int STATE_RESULT_VALUE = 2;",
        ),
        (
            "        void innerSuccess(R item) {",
            "        /** 内部 onSuccess：缓存值并触发 drain。 */\n"
            "        void innerSuccess(R item) {",
        ),
        (
            "        void innerError(Throwable ex) {",
            "        /** 内部 onError：按 errorMode 记录错误并 drain。 */\n"
            "        void innerError(Throwable ex) {",
        ),
        (
            "        static final class ConcatMapSingleObserver<R>\n        extends AtomicReference<Disposable>\n        implements SingleObserver<R> {",
            "        /** 订阅单个内部 SingleSource 并将信号转发给 parent。 */\n"
            "        static final class ConcatMapSingleObserver<R>\n        extends AtomicReference<Disposable>\n        implements SingleObserver<R> {",
        ),
    ],
    "ObservableSwitchMapCompletable.java": [
        (
            "/**\n * Maps the upstream values into {@link CompletableSource}s, subscribes to the newer one while\n * disposing the subscription to the previous {@code CompletableSource}, thus keeping at most one\n * active {@code CompletableSource} running.\n * <p>History: 2.1.11 - experimental\n * @param <T> the upstream value type\n * @since 2.2\n */",
            "/**\n"
            " * 将上游值映射为 {@link CompletableSource}，订阅较新内部源同时 dispose 旧订阅，\n"
            " * 保证至多一个内部 Completable 处于活动状态。\n"
            " * <p>History: 2.1.11 - experimental\n"
            " * @param <T> 上游元素类型\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    public ObservableSwitchMapCompletable(Observable<T> source,\n            Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors) {",
            "    /**\n"
            "     * @param source 上游 Observable\n"
            "     * @param mapper 将元素映射为 CompletableSource 的函数\n"
            "     * @param delayErrors 是否延迟合并错误\n"
            "     */\n"
            "    public ObservableSwitchMapCompletable(Observable<T> source,\n            Function<? super T, ? extends CompletableSource> mapper, boolean delayErrors) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 标量源走 {@link ScalarXMapZHelper#tryAsCompletable} 快速路径，否则订阅 SwitchMapCompletableObserver。 */\n"
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
        (
            "    static final class SwitchMapCompletableObserver<T> implements Observer<T>, Disposable {",
            "    /** 主流 Observer：onNext 时切换内部 Completable 订阅。 */\n"
            "    static final class SwitchMapCompletableObserver<T> implements Observer<T>, Disposable {",
        ),
        (
            "        void innerError(SwitchMapInnerObserver sender, Throwable error) {",
            "        /** 内部 onError：按 delayErrors 决定立即终止或延迟合并。 */\n"
            "        void innerError(SwitchMapInnerObserver sender, Throwable error) {",
        ),
        (
            "        void innerComplete(SwitchMapInnerObserver sender) {",
            "        /** 内部 onComplete：若主流已完成则向下游 onComplete。 */\n"
            "        void innerComplete(SwitchMapInnerObserver sender) {",
        ),
        (
            "        static final class SwitchMapInnerObserver extends AtomicReference<Disposable>\n        implements CompletableObserver {",
            "        /** 单个内部 Completable 的 observer，将信号转发给 parent。 */\n"
            "        static final class SwitchMapInnerObserver extends AtomicReference<Disposable>\n        implements CompletableObserver {",
        ),
    ],
    "ObservableSwitchMapMaybe.java": [
        (
            "/**\n * Maps the upstream items into {@link MaybeSource}s and switches (subscribes) to the newer ones\n * while disposing the older ones and emits the latest success value if available, optionally delaying\n * errors from the main source or the inner sources.\n * <p>History: 2.1.11 - experimental\n * @param <T> the upstream value type\n * @param <R> the downstream value type\n * @since 2.2\n */",
            "/**\n"
            " * 将上游元素映射为 {@link MaybeSource}，切换到较新内部源并 dispose 旧源，\n"
            " * 若有最新 onSuccess 值则发射；可选延迟主流或内部源错误。\n"
            " * <p>History: 2.1.11 - experimental\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> 下游元素类型\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    public ObservableSwitchMapMaybe(Observable<T> source,\n            Function<? super T, ? extends MaybeSource<? extends R>> mapper,\n            boolean delayErrors) {",
            "    /**\n"
            "     * @param source 上游 Observable\n"
            "     * @param mapper 将元素映射为 MaybeSource 的函数\n"
            "     * @param delayErrors 是否延迟合并错误\n"
            "     */\n"
            "    public ObservableSwitchMapMaybe(Observable<T> source,\n            Function<? super T, ? extends MaybeSource<? extends R>> mapper,\n            boolean delayErrors) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
            "    /** 标量源走 {@link ScalarXMapZHelper#tryAsMaybe} 快速路径，否则订阅 SwitchMapMaybeMainObserver。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
        ),
        (
            "    static final class SwitchMapMaybeMainObserver<T, R> extends AtomicInteger\n    implements Observer<T>, Disposable {",
            "    /** switchMap Maybe 主流 observer：onNext 切换内部 Maybe 并 drain 发射最新值。 */\n"
            "    static final class SwitchMapMaybeMainObserver<T, R> extends AtomicInteger\n    implements Observer<T>, Disposable {",
        ),
        (
            "        void drain() {",
            "        /** 串行 drain：在内部 onSuccess 且主流允许时向下游 onNext。 */\n"
            "        void drain() {",
        ),
        (
            "        static final class SwitchMapMaybeObserver<R>\n        extends AtomicReference<Disposable> implements MaybeObserver<R> {",
            "        /** 单个内部 Maybe 的 observer，onSuccess 时缓存 item 并触发 drain。 */\n"
            "        static final class SwitchMapMaybeObserver<R>\n        extends AtomicReference<Disposable> implements MaybeObserver<R> {",
        ),
    ],
    "ObservableSwitchMapSingle.java": [
        (
            "/**\n * Maps the upstream items into {@link SingleSource}s and switches (subscribes) to the newer ones\n * while disposing the older ones and emits the latest success value if available, optionally delaying\n * errors from the main source or the inner sources.\n * <p>History: 2.1.11 - experimental\n * @param <T> the upstream value type\n * @param <R> the downstream value type\n * @since 2.2\n */",
            "/**\n"
            " * 将上游元素映射为 {@link SingleSource}，切换到较新内部源并 dispose 旧源，\n"
            " * 若有最新 onSuccess 值则发射；可选延迟主流或内部源错误。\n"
            " * <p>History: 2.1.11 - experimental\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> 下游元素类型\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    public ObservableSwitchMapSingle(Observable<T> source,\n            Function<? super T, ? extends SingleSource<? extends R>> mapper,\n            boolean delayErrors) {",
            "    /**\n"
            "     * @param source 上游 Observable\n"
            "     * @param mapper 将元素映射为 SingleSource 的函数\n"
            "     * @param delayErrors 是否延迟合并错误\n"
            "     */\n"
            "    public ObservableSwitchMapSingle(Observable<T> source,\n            Function<? super T, ? extends SingleSource<? extends R>> mapper,\n            boolean delayErrors) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
            "    /** 标量源走 {@link ScalarXMapZHelper#tryAsSingle} 快速路径，否则订阅 SwitchMapSingleMainObserver。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
        ),
        (
            "    static final class SwitchMapSingleMainObserver<T, R> extends AtomicInteger\n    implements Observer<T>, Disposable {",
            "    /** switchMap Single 主流 observer：onNext 切换内部 Single 并 drain 发射最新值。 */\n"
            "    static final class SwitchMapSingleMainObserver<T, R> extends AtomicInteger\n    implements Observer<T>, Disposable {",
        ),
        (
            "        void drain() {",
            "        /** 串行 drain：在内部 onSuccess 且主流允许时向下游 onNext。 */\n"
            "        void drain() {",
        ),
        (
            "        static final class SwitchMapSingleObserver<R>\n        extends AtomicReference<Disposable> implements SingleObserver<R> {",
            "        /** 单个内部 Single 的 observer，onSuccess 时缓存 item 并触发 drain。 */\n"
            "        static final class SwitchMapSingleObserver<R>\n        extends AtomicReference<Disposable> implements SingleObserver<R> {",
        ),
    ],
    "ScalarXMapZHelper.java": [
        (
            "/**\n * Utility class to extract a value from a scalar source reactive type,\n * map it to a 0-1 type then subscribe the output type's consumer to it,\n * saving on the overhead of the regular subscription channel.\n * <p>History: 2.1.11 - experimental\n * @since 2.2\n */",
            "/**\n"
            " * 从标量源（实现 {@link Supplier} 的响应式类型）提取值，\n"
            " * 映射为 0-1 类型后直接订阅下游 consumer，跳过常规订阅通道开销。\n"
            " * <p>History: 2.1.11 - experimental\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    /**\n     * Try subscribing to a {@link CompletableSource} mapped from\n     * a scalar source (which implements {@link Supplier}).\n     * @param <T> the upstream value type\n     * @param source the source reactive type ({@code Flowable} or {@code Observable})\n     *               possibly implementing {@link Supplier}.\n     * @param mapper the function that turns the scalar upstream value into a\n     *              {@link CompletableSource}\n     * @param observer the consumer to subscribe to the mapped {@link CompletableSource}\n     * @return true if a subscription did happen and the regular path should be skipped\n     */",
            "    /**\n"
            "     * 尝试从标量源（{@link Supplier}）映射并订阅 {@link CompletableSource}。\n"
            "     * @param <T> 上游元素类型\n"
            "     * @param source 可能实现 {@link Supplier} 的源（{@code Flowable} 或 {@code Observable}）\n"
            "     * @param mapper 将标量值转为 {@link CompletableSource} 的函数\n"
            "     * @param observer 订阅映射后 CompletableSource 的 consumer\n"
            "     * @return 若已订阅且应跳过常规路径则为 true\n"
            "     */",
        ),
        (
            "    /**\n     * Try subscribing to a {@link MaybeSource} mapped from\n     * a scalar source (which implements {@link Supplier}).\n     * @param <T> the upstream value type\n     * @param <R> the downstream value type\n     * @param source the source reactive type ({@code Flowable} or {@code Observable})\n     *               possibly implementing {@link Supplier}.\n     * @param mapper the function that turns the scalar upstream value into a\n     *              {@link MaybeSource}\n     * @param observer the consumer to subscribe to the mapped {@link MaybeSource}\n     * @return true if a subscription did happen and the regular path should be skipped\n     */",
            "    /**\n"
            "     * 尝试从标量源（{@link Supplier}）映射并订阅 {@link MaybeSource}。\n"
            "     * @param <T> 上游元素类型\n"
            "     * @param <R> 下游元素类型\n"
            "     * @param source 可能实现 {@link Supplier} 的源（{@code Flowable} 或 {@code Observable}）\n"
            "     * @param mapper 将标量值转为 {@link MaybeSource} 的函数\n"
            "     * @param observer 订阅映射后 MaybeSource 的 consumer\n"
            "     * @return 若已订阅且应跳过常规路径则为 true\n"
            "     */",
        ),
        (
            "    /**\n     * Try subscribing to a {@link SingleSource} mapped from\n     * a scalar source (which implements {@link Supplier}).\n     * @param <T> the upstream value type\n     * @param <R> the downstream value type\n     * @param source the source reactive type ({@code Flowable} or {@code Observable})\n     *               possibly implementing {@link Supplier}.\n     * @param mapper the function that turns the scalar upstream value into a\n     *              {@link SingleSource}\n     * @param observer the consumer to subscribe to the mapped {@link SingleSource}\n     * @return true if a subscription did happen and the regular path should be skipped\n     */",
            "    /**\n"
            "     * 尝试从标量源（{@link Supplier}）映射并订阅 {@link SingleSource}。\n"
            "     * @param <T> 上游元素类型\n"
            "     * @param <R> 下游元素类型\n"
            "     * @param source 可能实现 {@link Supplier} 的源（{@code Flowable} 或 {@code Observable}）\n"
            "     * @param mapper 将标量值转为 {@link SingleSource} 的函数\n"
            "     * @param observer 订阅映射后 SingleSource 的 consumer\n"
            "     * @return 若已订阅且应跳过常规路径则为 true\n"
            "     */",
        ),
    ],
    "SingleFlatMapObservable.java": [
        (
            "/**\n * Maps the success value of a Single onto an ObservableSource and\n * relays its signals to the downstream observer.\n *\n * @param <T> the success value type of the Single source\n * @param <R> the result type of the ObservableSource and this operator\n * @since 2.1.15\n */",
            "/**\n"
            " * 将 {@link SingleSource} 的 onSuccess 值映射为 {@link ObservableSource}，\n"
            " * 并转发其信号至下游 observer。\n"
            " *\n * @param <T> Single 成功值类型\n"
            " * @param <R> ObservableSource 及本算子结果类型\n"
            " * @since 2.1.15\n"
            " */",
        ),
        (
            "    public SingleFlatMapObservable(SingleSource<T> source,\n            Function<? super T, ? extends ObservableSource<? extends R>> mapper) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param mapper 将成功值映射为 ObservableSource 的函数\n"
            "     */\n"
            "    public SingleFlatMapObservable(SingleSource<T> source,\n            Function<? super T, ? extends ObservableSource<? extends R>> mapper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
            "    /** 订阅 FlatMapObserver，onSuccess 时 flatMap 内部 ObservableSource。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
        ),
        (
            "    static final class FlatMapObserver<T, R>\n    extends AtomicReference<Disposable>\n    implements Observer<R>, SingleObserver<T>, Disposable {",
            "    /** onSuccess 时应用 mapper 并订阅内部 ObservableSource，自身兼作下游 Observer。 */\n"
            "    static final class FlatMapObserver<T, R>\n    extends AtomicReference<Disposable>\n    implements Observer<R>, SingleObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(T t) {",
            "        /** 应用 mapper 获取 ObservableSource 并订阅（若未 dispose）。 */\n"
            "        @Override\n        public void onSuccess(T t) {",
        ),
    ],
    "AbstractObservableWithUpstream.java": [
        (
            "/**\n * Base class for operators with a source consumable.\n *\n * @param <T> the input source type\n * @param <U> the output type\n */",
            "/**\n"
            " * 带上游 {@link ObservableSource} 的 Observable 算子基类，\n"
            " * 实现 {@link HasUpstreamObservableSource}。\n"
            " *\n * @param <T> 上游元素类型\n"
            " * @param <U> 下游元素类型\n"
            " */",
        ),
        (
            "    /** The source consumable Observable. */",
            "    /** 上游 ObservableSource。 */",
        ),
        (
            "    /**\n     * Constructs the ObservableSource with the given consumable.\n     * @param source the consumable Observable\n     */",
            "    /**\n"
            "     * 以上游 ObservableSource 构造算子。\n"
            "     * @param source 上游 ObservableSource\n"
            "     */",
        ),
    ],
    "BlockingObservableIterable.java": [
        (
            "import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;\n\npublic final class BlockingObservableIterable<T> implements Iterable<T> {",
            "import io.reactivex.rxjava4.operators.SpscLinkedArrayQueue;\n\n"
            "/**\n"
            " * 将 {@link ObservableSource} 转为阻塞 {@link Iterable}，\n"
            " * 通过 SPSC 队列与条件变量在 hasNext/next 中等待上游事件。\n"
            " *\n * @param <T> 元素类型\n"
            " */\n"
            "public final class BlockingObservableIterable<T> implements Iterable<T> {",
        ),
        (
            "    public BlockingObservableIterable(ObservableSource<? extends T> source, int bufferSize) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param bufferSize 内部 SPSC 队列容量\n"
            "     */\n"
            "    public BlockingObservableIterable(ObservableSource<? extends T> source, int bufferSize) {",
        ),
        (
            "    @Override\n    public Iterator<T> iterator() {",
            "    /** 创建 BlockingObservableIterator 并立即订阅上游。 */\n"
            "    @Override\n    public Iterator<T> iterator() {",
        ),
        (
            "    static final class BlockingObservableIterator<T>\n    extends AtomicReference<Disposable>\n    implements io.reactivex.rxjava4.core.Observer<T>, Iterator<T>, Disposable {",
            "    /** 阻塞迭代器：Observer 入队，Iterator 出队；hasNext 可阻塞等待。 */\n"
            "    static final class BlockingObservableIterator<T>\n    extends AtomicReference<Disposable>\n    implements io.reactivex.rxjava4.core.Observer<T>, Iterator<T>, Disposable {",
        ),
        (
            "        void signalConsumer() {",
            "        /** 唤醒在 condition 上等待的 hasNext 线程。 */\n"
            "        void signalConsumer() {",
        ),
        (
            "            signalConsumer(); // Just in case it is currently blocking in hasNext.",
            "            signalConsumer(); // 以防当前正阻塞在 hasNext 中",
        ),
    ],
    "BlockingObservableLatest.java": [
        (
            "/**\n * Wait for and iterate over the latest values of the source observable. If the source works faster than the\n * iterator, values may be skipped, but not the {@code onError} or {@code onComplete} events.\n * @param <T> the value type\n */",
            "/**\n"
            " * 阻塞等待并迭代上游 Observable 的最新值；上游快于迭代器时可能跳过中间值，\n"
            " * 但不会跳过 {@code onError} 或 {@code onComplete} 事件。\n"
            " * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public BlockingObservableLatest(ObservableSource<T> source) {",
            "    /** @param source 上游 ObservableSource */\n"
            "    public BlockingObservableLatest(ObservableSource<T> source) {",
        ),
        (
            "    @Override\n    public Iterator<T> iterator() {",
            "    /** materialize 后订阅 BlockingObservableLatestIterator。 */\n"
            "    @Override\n    public Iterator<T> iterator() {",
        ),
        (
            "        // iterator's notification",
            "        // 迭代器侧持有的 Notification",
        ),
        (
            "        // observer's notification",
            "        // observer 侧收到的 Notification",
        ),
        (
            "    static final class BlockingObservableLatestIterator<T> extends DisposableObserver<Notification<T>> implements Iterator<T> {",
            "    /** 用 Semaphore 同步：仅保留最新 Notification，hasNext 阻塞等待。 */\n"
            "    static final class BlockingObservableLatestIterator<T> extends DisposableObserver<Notification<T>> implements Iterator<T> {",
        ),
    ],
    "BlockingObservableMostRecent.java": [
        (
            "/**\n * Returns an Iterable that always returns the item most recently emitted by an Observable, or a\n * seed value if no item has yet been emitted.\n * <p>\n * <img width=\"640\" height=\"490\" src=\"https://github.com/ReactiveX/RxJava/wiki/images/rx-operators/B.mostRecent.v3.png\" alt=\"\">\n * \n * @param <T> the value type\n */",
            "/**\n"
            " * 返回始终反映 Observable 最近发射项的 {@link Iterable}；\n"
            " * 尚未发射任何项时返回种子值。\n"
            " * <p>\n"
            " * <img width=\"640\" height=\"490\" src=\"https://github.com/ReactiveX/RxJava/wiki/images/rx-operators/B.mostRecent.v3.png\" alt=\"\">\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public BlockingObservableMostRecent(ObservableSource<T> source, T initialValue) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param initialValue 尚无发射时的初始值\n"
            "     */\n"
            "    public BlockingObservableMostRecent(ObservableSource<T> source, T initialValue) {",
        ),
        (
            "        /**\n         * The {@link MostRecentIterator} return is not thread safe. In other words don't call {@link MostRecentIterator#hasNext()} in one\n         * thread expect {@link MostRecentIterator#next()} called from a different thread to work.\n         * @return the Iterator\n         */",
            "        /**\n"
            "         * 返回的 {@link MostRecentIterator} 非线程安全：\n"
            "         * 不可在一线程调用 {@link MostRecentIterator#hasNext()} 而在另一线程调用 {@link MostRecentIterator#next()}。\n"
            "         * @return Iterator 实例\n"
            "         */",
        ),
        (
            "            /**\n             * buffer to make sure that the state of the iterator doesn't change between calling hasNext() and next().\n             */",
            "            /** 缓冲区，保证 hasNext() 与 next() 之间迭代器状态不变。 */",
        ),
        (
            "                    // if hasNext wasn't called before calling next.",
            "                    // 若 next() 前未调用 hasNext()",
        ),
    ],
    "BlockingObservableNext.java": [
        (
            "/**\n * Returns an Iterable that blocks until the Observable emits another item, then returns that item.\n * <p>\n * <img width=\"640\" height=\"490\" src=\"https://github.com/ReactiveX/RxJava/wiki/images/rx-operators/B.next.v3.png\" alt=\"\">\n * \n * @param <T> the value type\n */",
            "/**\n"
            " * 返回阻塞直到 Observable 再发射一项后才返回该项的 {@link Iterable}。\n"
            " * <p>\n"
            " * <img width=\"640\" height=\"490\" src=\"https://github.com/ReactiveX/RxJava/wiki/images/rx-operators/B.next.v3.png\" alt=\"\">\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public BlockingObservableNext(ObservableSource<T> source) {",
            "    /** @param source 上游 ObservableSource */\n"
            "    public BlockingObservableNext(ObservableSource<T> source) {",
        ),
        (
            "    // test needs to access the observer.waiting flag",
            "    // 测试需访问 observer.waiting 标志",
        ),
        (
            "            // Since an iterator should not be used in different thread,\n            // so we do not need any synchronization.",
            "            // 迭代器不应跨线程使用，故无需同步",
        ),
        (
            "                // if not started, start now",
            "                // 尚未启动则立即 materialize 并订阅",
        ),
        (
            "            // If an observable is completed or fails,\n            // hasNext() always return false.",
            "            // Observable 完成或失败时 hasNext() 恒为 false",
        ),
        (
            "                // If any error has already been thrown, throw it again.",
            "                // 若已有错误则再次抛出",
        ),
        (
            "            // ignore",
            "            // materialize 流 onComplete 可忽略",
        ),
        (
            "                    // in case if we won race condition with onComplete/onError method",
            "                    // 处理与 onComplete/onError 的竞态",
        ),
    ],
    "ObservableAll.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableAll<T> extends AbstractObservableWithUpstream<T, Boolean> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 判断上游所有元素是否均满足 {@link Predicate}；\n"
            " * 全部满足时 onNext(true) 后 onComplete，任一不满足则 onNext(false) 后 onComplete。\n"
            " *\n * @param <T> 上游元素类型\n"
            " */\n"
            "public final class ObservableAll<T> extends AbstractObservableWithUpstream<T, Boolean> {",
        ),
        (
            "    public ObservableAll(ObservableSource<T> source, Predicate<? super T> predicate) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param predicate 逐元素测试谓词\n"
            "     */\n"
            "    public ObservableAll(ObservableSource<T> source, Predicate<? super T> predicate) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super Boolean> t) {",
            "    /** 订阅 AllObserver 并逐元素测试 predicate。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super Boolean> t) {",
        ),
        (
            "    static final class AllObserver<T> implements Observer<T>, Disposable {",
            "    /** 逐元素测试 predicate，首个 false 或完成/错误时终止。 */\n"
            "    static final class AllObserver<T> implements Observer<T>, Disposable {",
        ),
    ],
    "ObservableAllSingle.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableAllSingle<T> extends Single<Boolean> implements FuseToObservable<Boolean> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * {@link ObservableAll} 的 Single 变体：\n"
            " * 全部满足 predicate 时 onSuccess(true)，否则 onSuccess(false)。\n"
            " *\n * @param <T> 上游元素类型\n"
            " */\n"
            "public final class ObservableAllSingle<T> extends Single<Boolean> implements FuseToObservable<Boolean> {",
        ),
        (
            "    public ObservableAllSingle(ObservableSource<T> source, Predicate<? super T> predicate) {",
            "    /**\n"
            "     * @param source 上游 ObservableSource\n"
            "     * @param predicate 逐元素测试谓词\n"
            "     */\n"
            "    public ObservableAllSingle(ObservableSource<T> source, Predicate<? super T> predicate) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super Boolean> t) {",
            "    /** 订阅 AllObserver 并映射结果为 Single 信号。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super Boolean> t) {",
        ),
        (
            "    @Override\n    public Observable<Boolean> fuseToObservable() {",
            "    /** 融合为 {@link ObservableAll} 实例。 */\n"
            "    @Override\n    public Observable<Boolean> fuseToObservable() {",
        ),
        (
            "    static final class AllObserver<T> implements Observer<T>, Disposable {",
            "    /** 逐元素测试 predicate，结果以 onSuccess 而非 onNext 发出。 */\n"
            "    static final class AllObserver<T> implements Observer<T>, Disposable {",
        ),
    ],
    "ObservableAmb.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ObservableAmb<T> extends Observable<T> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 对多个 {@link ObservableSource} 执行 amb（择优）订阅：\n"
            " * 首个发出信号的源获胜，其余源被 dispose。\n"
            " *\n * @param <T> 元素类型\n"
            " */\n"
            "public final class ObservableAmb<T> extends Observable<T> {",
        ),
        (
            "    public ObservableAmb(ObservableSource<? extends T>[] sources, Iterable<? extends ObservableSource<? extends T>> sourcesIterable) {",
            "    /**\n"
            "     * @param sources 源数组（可为 null，此时从 sourcesIterable 收集）\n"
            "     * @param sourcesIterable 源 Iterable（sources 为 null 时使用）\n"
            "     */\n"
            "    public ObservableAmb(ObservableSource<? extends T>[] sources, Iterable<? extends ObservableSource<? extends T>> sourcesIterable) {",
        ),
        (
            "    @Override\n    @SuppressWarnings(\"unchecked\")\n    public void subscribeActual(Observer<? super T> observer) {",
            "    /** 0 源 onComplete，1 源直接订阅，多源由 AmbCoordinator 竞速。 */\n"
            "    @Override\n    @SuppressWarnings(\"unchecked\")\n    public void subscribeActual(Observer<? super T> observer) {",
        ),
        (
            "    static final class AmbCoordinator<T> implements Disposable {",
            "    /** 协调多路 amb：首个 win 的源转发信号，其余 dispose。 */\n"
            "    static final class AmbCoordinator<T> implements Disposable {",
        ),
        (
            "        public boolean win(int index) {",
            "        /** CAS 设置获胜源索引并 dispose 其余 AmbInnerObserver。 */\n"
            "        public boolean win(int index) {",
        ),
        (
            "    static final class AmbInnerObserver<T> extends AtomicReference<Disposable> implements Observer<T> {",
            "    /** 单路 amb observer：未获胜前尝试 win，获胜后转发信号。 */\n"
            "    static final class AmbInnerObserver<T> extends AtomicReference<Disposable> implements Observer<T> {",
        ),
    ],
}


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
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
