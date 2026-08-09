#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-22b Single operators [15:30]."""
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
WAVE22B_FILE = Path("/tmp/rxjava_w22b.txt")
SCRIPT_NAME = "annotate_rxjava_wave22b_batch15_30.py"
BATCH_FILES = [
    ln.strip()
    for ln in WAVE22B_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

GUARD_FILES = [
    VER
    / "analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "SingleDoOnSuccess.java": [
        (
            "import io.reactivex.rxjava4.functions.Consumer;\n\npublic final class SingleDoOnSuccess<T> extends Single<T> {",
            "import io.reactivex.rxjava4.functions.Consumer;\n\n"
            "/**\n"
            " * 在上游 Single 成功时执行 onSuccess 副作用回调，\n"
            " * 再向下游转发同一成功值（回调异常转 onError）。\n"
            " *\n * @param <T> 元素类型\n"
            " */"
            "\npublic final class SingleDoOnSuccess<T> extends Single<T> {",
        ),
        (
            "    public SingleDoOnSuccess(SingleSource<T> source, Consumer<? super T> onSuccess) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param onSuccess 成功值到达时执行的 Consumer\n"
            "     */\n"
            "    public SingleDoOnSuccess(SingleSource<T> source, Consumer<? super T> onSuccess) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
            "    /** 订阅 DoOnSuccess 包装 Observer 执行副作用后转发。 */\n"
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
        ),
        (
            "    final class DoOnSuccess implements SingleObserver<T> {",
            "    /** onSuccess 时先 onSuccess.accept 再 downstream.onSuccess。 */\n"
            "    final class DoOnSuccess implements SingleObserver<T> {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** 执行副作用后转发成功值；回调异常转 downstream.onError。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
    ],
    "SingleDoOnTerminate.java": [
        (
            "import io.reactivex.rxjava4.functions.Action;\n\npublic final class SingleDoOnTerminate<T> extends Single<T> {",
            "import io.reactivex.rxjava4.functions.Action;\n\n"
            "/**\n"
            " * 在上游 Single 终止（onSuccess 或 onError）时执行 onTerminate 回调，\n"
            " * 再向下游转发原信号（onError 路径下回调异常合并为 CompositeException）。\n"
            " *\n * @param <T> 元素类型\n"
            " */"
            "\npublic final class SingleDoOnTerminate<T> extends Single<T> {",
        ),
        (
            "    public SingleDoOnTerminate(SingleSource<T> source, Action onTerminate) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param onTerminate 终止时执行的 Action\n"
            "     */\n"
            "    public SingleDoOnTerminate(SingleSource<T> source, Action onTerminate) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
            "    /** 订阅 DoOnTerminate 包装 Observer 在终止时执行回调。 */\n"
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
        ),
        (
            "    final class DoOnTerminate implements SingleObserver<T> {",
            "    /** 成功/错误路径均先 onTerminate.run 再转发。 */\n"
            "    final class DoOnTerminate implements SingleObserver<T> {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** onTerminate 成功后 downstream.onSuccess(value)。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** onTerminate 后 onError；回调异常与 e 合并为 CompositeException。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
    ],
    "SingleEquals.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class SingleEquals<T> extends Single<Boolean> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 并行订阅两个 SingleSource，待两者均 onSuccess 后\n"
            " * 用 Objects.equals 比较并发射 Boolean 结果。\n"
            " *\n * @param <T> 上游元素类型\n"
            " */"
            "\npublic final class SingleEquals<T> extends Single<Boolean> {",
        ),
        (
            "    public SingleEquals(SingleSource<? extends T> first, SingleSource<? extends T> second) {",
            "    /**\n"
            "     * @param first 第一个 SingleSource\n"
            "     * @param second 第二个 SingleSource\n"
            "     */\n"
            "    public SingleEquals(SingleSource<? extends T> first, SingleSource<? extends T> second) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super Boolean> observer) {",
            "    /** 并行订阅 first/second，count==2 时比较 values 并 onSuccess。 */\n"
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super Boolean> observer) {",
        ),
        (
            "    record InnerObserver<T>(int index, CompositeDisposable set, Object[] values,\n                            SingleObserver<? super Boolean> downstream,\n                            AtomicInteger count) implements SingleObserver<T> {",
            "    /** 单路 Observer：缓存 values[index]，两路到齐后 Objects.equals 比较。 */\n"
            "    record InnerObserver<T>(int index, CompositeDisposable set, Object[] values,\n                            SingleObserver<? super Boolean> downstream,\n                            AtomicInteger count) implements SingleObserver<T> {",
        ),
        (
            "            @Override\n            public void onSuccess(T value) {",
            "            /** 写入 values[index]；count 达 2 时发射 equals 结果。 */\n"
            "            @Override\n            public void onSuccess(T value) {",
        ),
        (
            "            @Override\n            public void onError(Throwable e) {",
            "            /** 首错时 dispose 集合并 onError；已终止则 RxJavaPlugins.onError。 */\n"
            "            @Override\n            public void onError(Throwable e) {",
        ),
    ],
    "SingleError.java": [
        (
            "import io.reactivex.rxjava4.internal.util.ExceptionHelper;\n\npublic final class SingleError<T> extends Single<T> {",
            "import io.reactivex.rxjava4.internal.util.ExceptionHelper;\n\n"
            "/**\n"
            " * 订阅时调用 errorSupplier 获取 Throwable，\n"
            " * 经 EmptyDisposable.error 立即向下游发射错误。\n"
            " *\n * @param <T> 元素类型（永不成功发射）\n"
            " */"
            "\npublic final class SingleError<T> extends Single<T> {",
        ),
        (
            "    public SingleError(Supplier<? extends Throwable> errorSupplier) {",
            "    /** @param errorSupplier 提供要发射的 Throwable 的 Supplier */\n"
            "    public SingleError(Supplier<? extends Throwable> errorSupplier) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 调用 errorSupplier.get() 后 EmptyDisposable.error 通知下游。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
        ),
    ],
    "SingleFlatMap.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\npublic final class SingleFlatMap<T, R> extends Single<R> {",
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\n"
            "/**\n"
            " * 将上游 Single 的 onSuccess 值经 mapper 映射为 inner SingleSource，\n"
            " * 订阅 inner 并将其结果转发至下游。\n"
            " *\n * @param <T> 上游成功值类型\n"
            " * @param <R> 下游结果类型\n"
            " */"
            "\npublic final class SingleFlatMap<T, R> extends Single<R> {",
        ),
        (
            "    public SingleFlatMap(SingleSource<? extends T> source, Function<? super T, ? extends SingleSource<? extends R>> mapper) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param mapper 将成功值映射为 inner SingleSource 的函数\n"
            "     */\n"
            "    public SingleFlatMap(SingleSource<? extends T> source, Function<? super T, ? extends SingleSource<? extends R>> mapper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super R> downstream) {",
            "    /** 订阅 SingleFlatMapCallback，onSuccess 时 flatMap inner SingleSource。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super R> downstream) {",
        ),
        (
            "    static final class SingleFlatMapCallback<T, R>\n    extends AtomicReference<Disposable>\n    implements SingleObserver<T>, Disposable {",
            "    /** 上游 Observer：onSuccess 时 mapper 并订阅 inner SingleSource。 */\n"
            "    static final class SingleFlatMapCallback<T, R>\n    extends AtomicReference<Disposable>\n    implements SingleObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** mapper 获取 inner SingleSource 并订阅 FlatMapSingleObserver。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
        (
            "        record FlatMapSingleObserver<R>(AtomicReference<Disposable> parent,\n                                        SingleObserver<? super R> downstream) implements SingleObserver<R> {",
            "        /** inner SingleSource 的 Observer：DisposableHelper.replace 管理订阅。 */\n"
            "        record FlatMapSingleObserver<R>(AtomicReference<Disposable> parent,\n                                        SingleObserver<? super R> downstream) implements SingleObserver<R> {",
        ),
    ],
    "SingleFlatMapBiSelector.java": [
        (
            "/**\n * Maps a source item to another SingleSource then calls a BiFunction with the\n * original item and the secondary item to generate the final result.\n *\n * @param <T> the main value type\n * @param <U> the second value type\n * @param <R> the result value type\n * @since 3.0.0\n */",
            "/**\n"
            " * 将上游成功值经 mapper 映射为 inner SingleSource，\n"
            " * 再以 resultSelector 合并原值与 inner 成功值生成最终结果。\n"
            " *\n * @param <T> 主值类型\n"
            " * @param <U> inner SingleSource 成功值类型\n"
            " * @param <R> 最终结果类型\n"
            " * @since 3.0.0\n"
            " */",
        ),
        (
            "    public SingleFlatMapBiSelector(SingleSource<T> source,\n            Function<? super T, ? extends SingleSource<? extends U>> mapper,\n            BiFunction<? super T, ? super U, ? extends R> resultSelector) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param mapper 将主值映射为 inner SingleSource 的函数\n"
            "     * @param resultSelector 合并 (T, U) 为 R 的 BiFunction\n"
            "     */\n"
            "    public SingleFlatMapBiSelector(SingleSource<T> source,\n            Function<? super T, ? extends SingleSource<? extends U>> mapper,\n            BiFunction<? super T, ? super U, ? extends R> resultSelector) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super R> observer) {",
            "    /** 订阅 FlatMapBiMainObserver 执行 map + bi-select。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super R> observer) {",
        ),
        (
            "    static final class FlatMapBiMainObserver<T, U, R>\n    implements SingleObserver<T>, Disposable {",
            "    /** 主 Observer：缓存 value 后订阅 inner SingleSource。 */\n"
            "    static final class FlatMapBiMainObserver<T, U, R>\n    implements SingleObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** mapper 获取 next，缓存 value 后 inner.subscribe(inner)。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
        (
            "        static final class InnerObserver<T, U, R>\n        extends AtomicReference<Disposable>\n        implements SingleObserver<U> {",
            "        /** inner Observer：onSuccess 时 resultSelector.apply(t, u) 发射 R。 */\n"
            "        static final class InnerObserver<T, U, R>\n        extends AtomicReference<Disposable>\n        implements SingleObserver<U> {",
        ),
        (
            "            @Override\n            public void onSuccess(U value) {",
            "            /** resultSelector 合并缓存的 T 与 U 后 downstream.onSuccess(r)。 */\n"
            "            @Override\n            public void onSuccess(U value) {",
        ),
    ],
    "SingleFlatMapCompletable.java": [
        (
            "/**\n * Maps the success value of the source SingleSource into a Completable.\n * @param <T> the value type of the source SingleSource\n */",
            "/**\n"
            " * 将上游 Single 的 onSuccess 值经 mapper 映射为 CompletableSource，\n"
            " * 订阅 inner Completable 并转发其 onComplete/onError。\n"
            " * @param <T> 上游 SingleSource 的值类型\n"
            " */",
        ),
        (
            "    public SingleFlatMapCompletable(SingleSource<T> source, Function<? super T, ? extends CompletableSource> mapper) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param mapper 将成功值映射为 CompletableSource 的函数\n"
            "     */\n"
            "    public SingleFlatMapCompletable(SingleSource<T> source, Function<? super T, ? extends CompletableSource> mapper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 创建 FlatMapCompletableObserver 并订阅上游 SingleSource。 */\n"
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
        (
            "    static final class FlatMapCompletableObserver<T>\n    extends AtomicReference<Disposable>\n    implements SingleObserver<T>, CompletableObserver, Disposable {",
            "    /** 兼作 SingleObserver 与 CompletableObserver：onSuccess 时 flatMap inner Completable。 */\n"
            "    static final class FlatMapCompletableObserver<T>\n    extends AtomicReference<Disposable>\n    implements SingleObserver<T>, CompletableObserver, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** mapper 获取 CompletableSource 并 subscribe(this) 转发信号。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
    ],
    "SingleFlatMapIterableFlowable.java": [
        (
            "/**\n * Maps a success value into an Iterable and streams it back as a Flowable.\n *\n * @param <T> the source value type\n * @param <R> the element type of the Iterable\n */",
            "/**\n"
            " * 将 Single 成功值经 mapper 映射为 Iterable，\n"
            " * 以带背压的 Flowable 逐元素向下游发射。\n"
            " *\n * @param <T> 上游成功值类型\n"
            " * @param <R> Iterable 元素类型\n"
            " */",
        ),
        (
            "    public SingleFlatMapIterableFlowable(SingleSource<T> source,\n            Function<? super T, ? extends Iterable<? extends R>> mapper) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param mapper 将成功值映射为 Iterable 的函数\n"
            "     */\n"
            "    public SingleFlatMapIterableFlowable(SingleSource<T> source,\n            Function<? super T, ? extends Iterable<? extends R>> mapper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
            "    /** 订阅 FlatMapIterableObserver 将 Iterable 展开为 Flowable 序列。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> s) {",
        ),
        (
            "    static final class FlatMapIterableObserver<T, R>\n    extends BasicIntQueueSubscription<R>\n    implements SingleObserver<T> {",
            "    /** 背压 Iterable 展开：onSuccess 后 drain 按 requested 逐元素 onNext。 */\n"
            "    static final class FlatMapIterableObserver<T, R>\n    extends BasicIntQueueSubscription<R>\n    implements SingleObserver<T> {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** mapper 获取 Iterator；空 Iterable 则 onComplete，否则 drain。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
        (
            "        void drain() {",
            "        /** wip 门控：按 requested 从 iterator 逐 next 并 onNext/onComplete。 */\n"
            "        void drain() {",
        ),
        (
            "        void fastPath(Subscriber<? super R> a, Iterator<? extends R> iterator) {",
            "        /** requested==MAX 时无背压限制地逐元素发射。 */\n"
            "        void fastPath(Subscriber<? super R> a, Iterator<? extends R> iterator) {",
        ),
    ],
    "SingleFlatMapIterableObservable.java": [
        (
            "/**\n * Maps a success value into an Iterable and streams it back as an Observable.\n *\n * @param <T> the source value type\n * @param <R> the element type of the Iterable\n */",
            "/**\n"
            " * 将 Single 成功值经 mapper 映射为 Iterable，\n"
            " * 以 Observable 逐元素向下游发射（无背压）。\n"
            " *\n * @param <T> 上游成功值类型\n"
            " * @param <R> Iterable 元素类型\n"
            " */",
        ),
        (
            "    public SingleFlatMapIterableObservable(SingleSource<T> source,\n            Function<? super T, ? extends Iterable<? extends R>> mapper) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param mapper 将成功值映射为 Iterable 的函数\n"
            "     */\n"
            "    public SingleFlatMapIterableObservable(SingleSource<T> source,\n            Function<? super T, ? extends Iterable<? extends R>> mapper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
            "    /** 订阅 FlatMapIterableObserver 将 Iterable 同步展开为 Observable 序列。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super R> observer) {",
        ),
        (
            "    static final class FlatMapIterableObserver<T, R>\n    extends BasicIntQueueDisposable<R>\n    implements SingleObserver<T> {",
            "    /** Iterable 展开 Observer：outputFused 时走 poll 融合路径。 */\n"
            "    static final class FlatMapIterableObserver<T, R>\n    extends BasicIntQueueDisposable<R>\n    implements SingleObserver<T> {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** 空 Iterable 则 onComplete；融合模式或 for 循环逐 next 并 onNext。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
    ],
    "SingleFlatMapMaybe.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\npublic final class SingleFlatMapMaybe<T, R> extends Maybe<R> {",
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\n"
            "/**\n"
            " * 将上游 Single 的 onSuccess 值经 mapper 映射为 MaybeSource，\n"
            " * 订阅 inner Maybe 并转发其 onSuccess/onComplete/onError。\n"
            " *\n * @param <T> 上游成功值类型\n"
            " * @param <R> 下游 Maybe 元素类型\n"
            " */"
            "\npublic final class SingleFlatMapMaybe<T, R> extends Maybe<R> {",
        ),
        (
            "    public SingleFlatMapMaybe(SingleSource<? extends T> source, Function<? super T, ? extends MaybeSource<? extends R>> mapper) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param mapper 将成功值映射为 MaybeSource 的函数\n"
            "     */\n"
            "    public SingleFlatMapMaybe(SingleSource<? extends T> source, Function<? super T, ? extends MaybeSource<? extends R>> mapper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super R> downstream) {",
            "    /** 订阅 FlatMapSingleObserver，onSuccess 时 flatMap inner MaybeSource。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super R> downstream) {",
        ),
        (
            "    static final class FlatMapSingleObserver<T, R>\n    extends AtomicReference<Disposable>\n    implements SingleObserver<T>, Disposable {",
            "    /** 上游 SingleObserver：onSuccess 时 mapper 并订阅 inner MaybeSource。 */\n"
            "    static final class FlatMapSingleObserver<T, R>\n    extends AtomicReference<Disposable>\n    implements SingleObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** mapper 获取 MaybeSource 并订阅 FlatMapMaybeObserver。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
        (
            "    record FlatMapMaybeObserver<R>(AtomicReference<Disposable> parent,\n                                   MaybeObserver<? super R> downstream) implements MaybeObserver<R> {",
            "    /** inner MaybeSource 的 Observer：转发 onSuccess/onComplete/onError。 */\n"
            "    record FlatMapMaybeObserver<R>(AtomicReference<Disposable> parent,\n                                   MaybeObserver<? super R> downstream) implements MaybeObserver<R> {",
        ),
    ],
    "SingleFlatMapNotification.java": [
        (
            "/**\n * Maps a value into a SingleSource and relays its signal.\n *\n * @param <T> the source value type\n * @param <R> the result value type\n * @since 3.0.0\n */",
            "/**\n"
            " * 按上游信号分支映射：onSuccess 走 onSuccessMapper，\n"
            " * onError 走 onErrorMapper，再订阅返回的 SingleSource 并转发。\n"
            " *\n * @param <T> 上游值类型\n"
            " * @param <R> 下游结果类型\n"
            " * @since 3.0.0\n"
            " */",
        ),
        (
            "    public SingleFlatMapNotification(SingleSource<T> source,\n            Function<? super T, ? extends SingleSource<? extends R>> onSuccessMapper,\n            Function<? super Throwable, ? extends SingleSource<? extends R>> onErrorMapper) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param onSuccessMapper 成功值映射为 SingleSource 的函数\n"
            "     * @param onErrorMapper 错误映射为 SingleSource 的函数\n"
            "     */\n"
            "    public SingleFlatMapNotification(SingleSource<T> source,\n            Function<? super T, ? extends SingleSource<? extends R>> onSuccessMapper,\n            Function<? super Throwable, ? extends SingleSource<? extends R>> onErrorMapper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super R> observer) {",
            "    /** 订阅 FlatMapSingleObserver 按成功/错误路径选择 mapper。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super R> observer) {",
        ),
        (
            "    static final class FlatMapSingleObserver<T, R>\n    extends AtomicReference<Disposable>\n    implements SingleObserver<T>, Disposable {",
            "    /** 分支 flatMap：onSuccess/onError 分别应用对应 mapper 并订阅 inner。 */\n"
            "    static final class FlatMapSingleObserver<T, R>\n    extends AtomicReference<Disposable>\n    implements SingleObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** onSuccessMapper 获取 SingleSource 并订阅 InnerObserver。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** onErrorMapper 获取 SingleSource；mapper 异常合并为 CompositeException。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
        (
            "        final class InnerObserver implements SingleObserver<R> {",
            "        /** inner SingleSource 的 Observer：DisposableHelper.setOnce 管理订阅。 */\n"
            "        final class InnerObserver implements SingleObserver<R> {",
        ),
    ],
    "SingleFlatMapPublisher.java": [
        (
            "/**\n * A Flowable that emits items based on applying a specified function to the item emitted by the\n * source Single, where that function returns a Publisher.\n * <p>\n * <img width=\"640\" height=\"305\" src=\"https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/Single.flatMapPublisher.v3.png\" alt=\"\">\n * <dl>\n *  <dt><b>Backpressure:</b></dt>\n *  <dd>The returned {@code Flowable} honors the backpressure of the downstream consumer\n *  and the {@code Publisher} returned by the mapper function is expected to honor it as well.</dd>\n * <dt><b>Scheduler:</b></dt>\n * <dd>{@code flatMapPublisher} does not operate by default on a particular {@link Scheduler}.</dd>\n * </dl>\n * \n * @param <T> the source value type\n * @param <R> the result value type\n * \n * @see <a href=\"http://reactivex.io/documentation/operators/flatmap.html\">ReactiveX operators documentation: FlatMap</a>\n * @since 2.1.15\n */",
            "/**\n"
            " * 将 Single 的 onSuccess 值经 mapper 映射为 Publisher，\n"
            " * 订阅 inner Publisher 并以 Flowable 形式转发其元素（支持背压）。\n"
            " * <p>\n"
            " * <img width=\"640\" height=\"305\" src=\"https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/Single.flatMapPublisher.v3.png\" alt=\"\">\n"
            " * <dl>\n"
            " *  <dt><b>Backpressure:</b></dt>\n"
            " *  <dd>返回的 {@code Flowable} 遵守下游背压，mapper 返回的 {@code Publisher} 亦应遵守。</dd>\n"
            " * <dt><b>Scheduler:</b></dt>\n"
            " * <dd>{@code flatMapPublisher} 默认不在特定 {@link Scheduler} 上运行。</dd>\n"
            " * </dl>\n"
            " * \n"
            " * @param <T> 上游成功值类型\n"
            " * @param <R> 下游元素类型\n"
            " * \n"
            " * @see <a href=\"http://reactivex.io/documentation/operators/flatmap.html\">ReactiveX operators documentation: FlatMap</a>\n"
            " * @since 2.1.15\n"
            " */",
        ),
        (
            "    public SingleFlatMapPublisher(SingleSource<T> source,\n            Function<? super T, ? extends Publisher<? extends R>> mapper) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param mapper 将成功值映射为 Publisher 的函数\n"
            "     */\n"
            "    public SingleFlatMapPublisher(SingleSource<T> source,\n            Function<? super T, ? extends Publisher<? extends R>> mapper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> downstream) {",
            "    /** 订阅 SingleFlatMapPublisherObserver 在 onSuccess 时 flatMap inner Publisher。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super R> downstream) {",
        ),
        (
            "    static final class SingleFlatMapPublisherObserver<S, T> extends AtomicLong\n            implements SingleObserver<S>, FlowableSubscriber<T>, Subscription {",
            "    /** 兼作 SingleObserver 与 FlowableSubscriber：deferredSetOnce 协调背压 request。 */\n"
            "    static final class SingleFlatMapPublisherObserver<S, T> extends AtomicLong\n            implements SingleObserver<S>, FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        public void onSuccess(S value) {",
            "        /** mapper 获取 Publisher 并 subscribe(this) 转发 onNext/onComplete/onError。 */\n"
            "        @Override\n        public void onSuccess(S value) {",
        ),
        (
            "        @Override\n        public void request(long n) {",
            "        /** SubscriptionHelper.deferredRequest 将 request 转发至 inner Subscription。 */\n"
            "        @Override\n        public void request(long n) {",
        ),
    ],
    "SingleFromCallable.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class SingleFromCallable<T> extends Single<T> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 订阅时同步调用 callable.call()，\n"
            " * 成功则 onSuccess，异常则 onError（已 dispose 时 RxJavaPlugins.onError）。\n"
            " *\n * @param <T> 元素类型\n"
            " */"
            "\npublic final class SingleFromCallable<T> extends Single<T> {",
        ),
        (
            "    public SingleFromCallable(Callable<? extends T> callable) {",
            "    /** @param callable 提供单值的 Callable */\n"
            "    public SingleFromCallable(Callable<? extends T> callable) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 先 onSubscribe(empty Disposable)，再 callable.call() 并发射结果。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
        ),
    ],
    "SingleFromPublisher.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class SingleFromPublisher<T> extends Single<T> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 订阅 Publisher 并取首个 onNext 元素作为 Single 成功值；\n"
            " * 多于一个元素则 IndexOutOfBoundsException，空序列则 NoSuchElementException。\n"
            " *\n * @param <T> 元素类型\n"
            " */"
            "\npublic final class SingleFromPublisher<T> extends Single<T> {",
        ),
        (
            "    public SingleFromPublisher(Publisher<? extends T> publisher) {",
            "    /** @param publisher 上游 Publisher（期望 0 或 1 个元素） */\n"
            "    public SingleFromPublisher(Publisher<? extends T> publisher) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
            "    /** 订阅 ToSingleObserver，request(MAX) 收集唯一元素。 */\n"
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class ToSingleObserver<T> implements FlowableSubscriber<T>, Disposable {",
            "    /** Publisher→Single 适配：缓存首个 onNext，onComplete 时 onSuccess 或 onError。 */\n"
            "    static final class ToSingleObserver<T> implements FlowableSubscriber<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 首元素缓存；第二个元素 cancel 并 IndexOutOfBoundsException。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 有缓存值则 onSuccess；否则 NoSuchElementException。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "SingleFromSupplier.java": [
        (
            "/**\n * Calls a supplier and emits its value or exception to the incoming SingleObserver.\n * @param <T> the value type returned\n * @since 3.0.0\n */",
            "/**\n"
            " * 订阅时调用 supplier.get() 获取值并 onSuccess，\n"
            " * 异常则 onError（已 dispose 时 RxJavaPlugins.onError）。\n"
            " * @param <T> 返回值类型\n"
            " * @since 3.0.0\n"
            " */",
        ),
        (
            "    public SingleFromSupplier(Supplier<? extends T> supplier) {",
            "    /** @param supplier 提供单值的 Supplier */\n"
            "    public SingleFromSupplier(Supplier<? extends T> supplier) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 先 onSubscribe(empty Disposable)，再 supplier.get() 并发射结果。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
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
    index_file = Path("/tmp/git-index-rxjava-w22b")
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
        "rxjava 4.0.0-alpha-21: Chinese-annotate wave 22b [15:30]",
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
            "wave22b",
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
        "queue: mark rxjava 4.0.0-alpha-21 wave22b done",
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
