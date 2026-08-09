#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-7a observers + Completable* [0:15]."""
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
WAVE7A_FILE = Path("/tmp/rxjava_w7a.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE7A_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ResumeSingleObserver.java": [
        (
            "/**\n * A SingleObserver implementation used for subscribing to the actual SingleSource\n * and replace the current Disposable in a parent AtomicReference.\n *\n * @param <T> the value type\n */",
            "/**\n * 用于订阅实际 SingleSource 并在父级 {@link AtomicReference} 中\n * 替换当前 Disposable 的 {@link SingleObserver} 实现。\n *\n * @param <T> 值类型\n */",
        ),
        (
            "    public ResumeSingleObserver(AtomicReference<Disposable> parent, SingleObserver<? super T> downstream) {",
            "    /**\n"
            "     * @param parent 持有上游 disposable 的原子引用\n"
            "     * @param downstream 下游 SingleObserver\n"
            "     */\n"
            "    public ResumeSingleObserver(AtomicReference<Disposable> parent, SingleObserver<? super T> downstream) {",
        ),
        (
            "    @Override\n    public void onSubscribe(Disposable d) {",
            "    /** 将 parent 中的 disposable 替换为上游订阅。 */\n"
            "    @Override\n    public void onSubscribe(Disposable d) {",
        ),
        (
            "    @Override\n    public void onSuccess(T value) {",
            "    /** 将成功值转发给下游。 */\n"
            "    @Override\n    public void onSuccess(T value) {",
        ),
        (
            "    @Override\n    public void onError(Throwable e) {",
            "    /** 将错误转发给下游。 */\n"
            "    @Override\n    public void onError(Throwable e) {",
        ),
    ],
    "SafeCompletableObserver.java": [
        (
            "/**\n * Wraps another {@link CompletableObserver} and catches exceptions thrown by its\n * {@code onSubscribe}, {@code onError} or\n * {@code onComplete} methods despite the protocol forbids it.\n * <p>\n * Such exceptions are routed to the {@link RxJavaPlugins#onError(Throwable)} handler.\n *\n * @since 3.0.0\n */",
            "/**\n * 包装另一个 {@link CompletableObserver}，捕获其 {@code onSubscribe}、\n * {@code onError} 或 {@code onComplete} 方法抛出的异常（尽管协议禁止如此）。\n * <p>\n * 此类异常会路由到 {@link RxJavaPlugins#onError(Throwable)} 处理器。\n *\n * @since 3.0.0\n */",
        ),
        (
            "    public SafeCompletableObserver(CompletableObserver downstream) {",
            "    /** @param downstream 被包装的下游 CompletableObserver */\n"
            "    public SafeCompletableObserver(CompletableObserver downstream) {",
        ),
        (
            "    @Override\n    public void onSubscribe(@NonNull Disposable d) {",
            "    /** 安全调用下游 onSubscribe；异常时 dispose 上游并上报 RxJavaPlugins。 */\n"
            "    @Override\n    public void onSubscribe(@NonNull Disposable d) {",
        ),
        (
            "    @Override\n    public void onError(@NonNull Throwable e) {",
            "    /** 安全转发错误或上报；onSubscribe 失败时仅上报。 */\n"
            "    @Override\n    public void onError(@NonNull Throwable e) {",
        ),
        (
            "    @Override\n    public void onComplete() {",
            "    /** 安全转发完成信号；onSubscribe 失败时忽略。 */\n"
            "    @Override\n    public void onComplete() {",
        ),
    ],
    "SafeMaybeObserver.java": [
        (
            "/**\n * Wraps another {@link MaybeObserver} and catches exceptions thrown by its\n * {@code onSubscribe}, {@code onSuccess}, {@code onError} or\n * {@code onComplete} methods despite the protocol forbids it.\n * <p>\n * Such exceptions are routed to the {@link RxJavaPlugins#onError(Throwable)} handler.\n *\n * @param <T> the element type of the sequence\n * @since 3.0.0\n */",
            "/**\n * 包装另一个 {@link MaybeObserver}，捕获其 {@code onSubscribe}、\n * {@code onSuccess}、{@code onError} 或 {@code onComplete} 方法抛出的异常\n * （尽管协议禁止如此）。\n * <p>\n * 此类异常会路由到 {@link RxJavaPlugins#onError(Throwable)} 处理器。\n *\n * @param <T> 序列元素类型\n * @since 3.0.0\n */",
        ),
        (
            "    public SafeMaybeObserver(MaybeObserver<? super T> downstream) {",
            "    /** @param downstream 被包装的下游 MaybeObserver */\n"
            "    public SafeMaybeObserver(MaybeObserver<? super T> downstream) {",
        ),
        (
            "    @Override\n    public void onSubscribe(@NonNull Disposable d) {",
            "    /** 安全调用下游 onSubscribe；异常时 dispose 上游并上报 RxJavaPlugins。 */\n"
            "    @Override\n    public void onSubscribe(@NonNull Disposable d) {",
        ),
        (
            "    @Override\n    public void onSuccess(@NonNull T t) {",
            "    /** 安全转发成功值；onSubscribe 失败时忽略。 */\n"
            "    @Override\n    public void onSuccess(@NonNull T t) {",
        ),
        (
            "    @Override\n    public void onError(@NonNull Throwable e) {",
            "    /** 安全转发错误或上报；onSubscribe 失败时仅上报。 */\n"
            "    @Override\n    public void onError(@NonNull Throwable e) {",
        ),
        (
            "    @Override\n    public void onComplete() {",
            "    /** 安全转发完成信号；onSubscribe 失败时忽略。 */\n"
            "    @Override\n    public void onComplete() {",
        ),
    ],
    "SafeSingleObserver.java": [
        (
            "/**\n * Wraps another {@link SingleObserver} and catches exceptions thrown by its\n * {@code onSubscribe}, {@code onSuccess} or {@code onError} methods despite\n * the protocol forbids it.\n * <p>\n * Such exceptions are routed to the {@link RxJavaPlugins#onError(Throwable)} handler.\n *\n * @param <T> the element type of the sequence\n * @since 3.0.0\n */",
            "/**\n * 包装另一个 {@link SingleObserver}，捕获其 {@code onSubscribe}、\n * {@code onSuccess} 或 {@code onError} 方法抛出的异常（尽管协议禁止如此）。\n * <p>\n * 此类异常会路由到 {@link RxJavaPlugins#onError(Throwable)} 处理器。\n *\n * @param <T> 序列元素类型\n * @since 3.0.0\n */",
        ),
        (
            "    public SafeSingleObserver(SingleObserver<? super T> downstream) {",
            "    /** @param downstream 被包装的下游 SingleObserver */\n"
            "    public SafeSingleObserver(SingleObserver<? super T> downstream) {",
        ),
        (
            "    @Override\n    public void onSubscribe(@NonNull Disposable d) {",
            "    /** 安全调用下游 onSubscribe；异常时 dispose 上游并上报 RxJavaPlugins。 */\n"
            "    @Override\n    public void onSubscribe(@NonNull Disposable d) {",
        ),
        (
            "    @Override\n    public void onSuccess(@NonNull T t) {",
            "    /** 安全转发成功值；onSubscribe 失败时忽略。 */\n"
            "    @Override\n    public void onSuccess(@NonNull T t) {",
        ),
        (
            "    @Override\n    public void onError(@NonNull Throwable e) {",
            "    /** 安全转发错误或上报；onSubscribe 失败时仅上报。 */\n"
            "    @Override\n    public void onError(@NonNull Throwable e) {",
        ),
    ],
    "CompletableAmb.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class CompletableAmb extends Completable {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 对多个 {@link CompletableSource} 执行 amb（竞争）操作：\n"
            " * 仅第一个发出终止事件的源会通知下游，其余源被取消。\n"
            " */\n"
            "public final class CompletableAmb extends Completable {",
        ),
        (
            "    public CompletableAmb(CompletableSource[] sources, Iterable<? extends CompletableSource> sourcesIterable) {",
            "    /**\n"
            "     * @param sources CompletableSource 数组，可为 null（此时使用 sourcesIterable）\n"
            "     * @param sourcesIterable 当 sources 为 null 时使用的可迭代源\n"
            "     */\n"
            "    public CompletableAmb(CompletableSource[] sources, Iterable<? extends CompletableSource> sourcesIterable) {",
        ),
        (
            "    @Override\n    public void subscribeActual(final CompletableObserver observer) {",
            "    /** 订阅所有源；首个完成或错误的源通知下游。 */\n"
            "    @Override\n    public void subscribeActual(final CompletableObserver observer) {",
        ),
        (
            "    static final class Amb implements CompletableObserver {",
            "    /** 单个 amb 竞争源的内部 observer。 */\n"
            "    static final class Amb implements CompletableObserver {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 若尚未有胜者，取消其余源并通知下游完成。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** 若尚未有胜者，取消其余源并通知下游错误；否则上报 RxJavaPlugins。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
    ],
    "CompletableAndThenCompletable.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\npublic final class CompletableAndThenCompletable extends Completable {",
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\n"
            "/**\n"
            " * 顺序执行两个 {@link CompletableSource}：\n"
            " * 先订阅 source，其正常完成后才订阅 next。\n"
            " */\n"
            "public final class CompletableAndThenCompletable extends Completable {",
        ),
        (
            "    public CompletableAndThenCompletable(CompletableSource source, CompletableSource next) {",
            "    /**\n"
            "     * @param source 先执行的 CompletableSource\n"
            "     * @param next source 完成后执行的 CompletableSource\n"
            "     */\n"
            "    public CompletableAndThenCompletable(CompletableSource source, CompletableSource next) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 订阅 source，完成后链接到 next。 */\n"
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
        (
            "    static final class SourceObserver",
            "    /** 订阅第一个 source 并在完成时启动 next 的内部 observer。 */\n"
            "    static final class SourceObserver",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** source 完成后订阅 next。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "CompletableCache.java": [
        (
            "/**\n * Consume the upstream source exactly once and cache its terminal event.\n * <p>History: 2.0.4 - experimental\n * @since 2.1\n */",
            "/**\n * 恰好消费上游源一次并缓存其终止事件。\n * <p>History: 2.0.4 - experimental\n * @since 2.1\n */",
        ),
        (
            "    public CompletableCache(CompletableSource source) {",
            "    /** @param source 要缓存的上游 CompletableSource */\n"
            "    public CompletableCache(CompletableSource source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 注册 observer；首个订阅者触发上游订阅，后续订阅者直接收到缓存的终止事件。 */\n"
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
        (
            "    @Override\n    public void onError(Throwable e) {",
            "    /** 缓存错误并通知所有已注册 observer。 */\n"
            "    @Override\n    public void onError(Throwable e) {",
        ),
        (
            "    @Override\n    public void onComplete() {",
            "    /** 通知所有已注册 observer 完成。 */\n"
            "    @Override\n    public void onComplete() {",
        ),
        (
            "    boolean add(InnerCompletableCache inner) {",
            "    /** 将 inner observer 加入活跃集合。 */\n"
            "    boolean add(InnerCompletableCache inner) {",
        ),
        (
            "    void remove(InnerCompletableCache inner) {",
            "    /** 从活跃集合移除 inner observer。 */\n"
            "    void remove(InnerCompletableCache inner) {",
        ),
        (
            "    final class InnerCompletableCache",
            "    /** 表示单个缓存订阅者的内部 Disposable。 */\n"
            "    final class InnerCompletableCache",
        ),
    ],
    "CompletableConcat.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class CompletableConcat extends Completable {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 按顺序串联 {@link Publisher} 发出的多个 {@link CompletableSource}，\n"
            " * 一次仅订阅一个内部源。\n"
            " */\n"
            "public final class CompletableConcat extends Completable {",
        ),
        (
            "    public CompletableConcat(Publisher<? extends CompletableSource> sources, int prefetch) {",
            "    /**\n"
            "     * @param sources 发出 CompletableSource 的 Publisher\n"
            "     * @param prefetch 预取缓冲大小\n"
            "     */\n"
            "    public CompletableConcat(Publisher<? extends CompletableSource> sources, int prefetch) {",
        ),
        (
            "    @Override\n    public void subscribeActual(CompletableObserver observer) {",
            "    /** 订阅 sources Publisher 并顺序执行各 CompletableSource。 */\n"
            "    @Override\n    public void subscribeActual(CompletableObserver observer) {",
        ),
        (
            "    static final class CompletableConcatSubscriber",
            "    /** 从 Publisher 取源并顺序订阅的内部 subscriber。 */\n"
            "    static final class CompletableConcatSubscriber",
        ),
        (
            "        void drain() {",
            "        /** 从队列取出下一个 CompletableSource 并订阅，或在上游完成且队列为空时完成下游。 */\n"
            "        void drain() {",
        ),
        (
            "        void innerComplete() {",
            "        /** 当前内部源完成，继续 drain 下一个。 */\n"
            "        void innerComplete() {",
        ),
    ],
    "CompletableConcatArray.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.SequentialDisposable;\n\npublic final class CompletableConcatArray extends Completable {",
            "import io.reactivex.rxjava4.internal.disposables.SequentialDisposable;\n\n"
            "/**\n"
            " * 按数组顺序串联多个 {@link CompletableSource}，\n"
            " * 前一个正常完成后才订阅下一个。\n"
            " */\n"
            "public final class CompletableConcatArray extends Completable {",
        ),
        (
            "    public CompletableConcatArray(CompletableSource[] sources) {",
            "    /** @param sources 要顺序执行的 CompletableSource 数组 */\n"
            "    public CompletableConcatArray(CompletableSource[] sources) {",
        ),
        (
            "    @Override\n    public void subscribeActual(CompletableObserver observer) {",
            "    /** 从数组首元素开始顺序订阅。 */\n"
            "    @Override\n    public void subscribeActual(CompletableObserver observer) {",
        ),
        (
            "    static final class ConcatInnerObserver extends AtomicInteger implements CompletableObserver {",
            "    /** 顺序遍历数组并订阅各源的内部 observer。 */\n"
            "    static final class ConcatInnerObserver extends AtomicInteger implements CompletableObserver {",
        ),
        (
            "        void next() {",
            "        /** 订阅数组中的下一个 CompletableSource，或全部完成后通知下游。 */\n"
            "        void next() {",
        ),
    ],
    "CompletableConcatIterable.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.*;\n\npublic final class CompletableConcatIterable extends Completable {",
            "import io.reactivex.rxjava4.internal.disposables.*;\n\n"
            "/**\n"
            " * 按 {@link Iterable} 迭代顺序串联多个 {@link CompletableSource}，\n"
            " * 前一个正常完成后才订阅下一个。\n"
            " */\n"
            "public final class CompletableConcatIterable extends Completable {",
        ),
        (
            "    public CompletableConcatIterable(Iterable<? extends CompletableSource> sources) {",
            "    /** @param sources 要顺序执行的 CompletableSource 可迭代对象 */\n"
            "    public CompletableConcatIterable(Iterable<? extends CompletableSource> sources) {",
        ),
        (
            "    @Override\n    public void subscribeActual(CompletableObserver observer) {",
            "    /** 从 Iterable 迭代器开始顺序订阅各源。 */\n"
            "    @Override\n    public void subscribeActual(CompletableObserver observer) {",
        ),
        (
            "    static final class ConcatInnerObserver extends AtomicInteger implements CompletableObserver {",
            "    /** 顺序遍历 Iterable 并订阅各源的内部 observer。 */\n"
            "    static final class ConcatInnerObserver extends AtomicInteger implements CompletableObserver {",
        ),
        (
            "        void next() {",
            "        /** 订阅迭代器中的下一个 CompletableSource，或全部完成后通知下游。 */\n"
            "        void next() {",
        ),
    ],
    "CompletableCreate.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class CompletableCreate extends Completable {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 通过 {@link CompletableOnSubscribe} 回调创建 {@link Completable} 的实现。\n"
            " */\n"
            "public final class CompletableCreate extends Completable {",
        ),
        (
            "    public CompletableCreate(CompletableOnSubscribe source) {",
            "    /** @param source 订阅时调用的 CompletableOnSubscribe 回调 */\n"
            "    public CompletableCreate(CompletableOnSubscribe source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 创建 Emitter 并调用 source.subscribe；异常时通过 emitter 报告错误。 */\n"
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
        (
            "    static final class Emitter",
            "    /** 向下游传递完成/错误信号并管理 disposable 的 CompletableEmitter 实现。 */\n"
            "    static final class Emitter",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 通知下游完成并 dispose 关联的 disposable。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
        (
            "        @Override\n        public boolean tryOnError(Throwable t) {",
            "        /** 尝试向 downstream 报告错误；若已 dispose 则返回 false。 */\n"
            "        @Override\n        public boolean tryOnError(Throwable t) {",
        ),
    ],
    "CompletableDefer.java": [
        (
            "import java.util.Objects;\n\npublic final class CompletableDefer extends Completable {",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 每次订阅时通过 {@link Supplier} 获取新的 {@link CompletableSource} 并订阅。\n"
            " */\n"
            "public final class CompletableDefer extends Completable {",
        ),
        (
            "    public CompletableDefer(Supplier<? extends CompletableSource> completableSupplier) {",
            "    /** @param completableSupplier 每次订阅时提供 CompletableSource 的 Supplier */\n"
            "    public CompletableDefer(Supplier<? extends CompletableSource> completableSupplier) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 调用 supplier 获取 CompletableSource 并订阅；supplier 异常时报告错误。 */\n"
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
    ],
    "CompletableDelay.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\npublic final class CompletableDelay extends Completable {",
            "import io.reactivex.rxjava4.internal.disposables.DisposableHelper;\n\n"
            "/**\n"
            " * 延迟上游 {@link CompletableSource} 的终止事件（完成或错误）后再通知下游。\n"
            " */\n"
            "public final class CompletableDelay extends Completable {",
        ),
        (
            "    public CompletableDelay(CompletableSource source, long delay, TimeUnit unit, Scheduler scheduler, boolean delayError) {",
            "    /**\n"
            "     * @param source 上游 CompletableSource\n"
            "     * @param delay 延迟时长\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 执行延迟的 Scheduler\n"
            "     * @param delayError 为 true 时错误信号也延迟相同时长\n"
            "     */\n"
            "    public CompletableDelay(CompletableSource source, long delay, TimeUnit unit, Scheduler scheduler, boolean delayError) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final CompletableObserver observer) {",
            "    /** 订阅 source 并在终止事件到达后按配置延迟转发。 */\n"
            "    @Override\n    protected void subscribeActual(final CompletableObserver observer) {",
        ),
        (
            "    static final class Delay extends AtomicReference<Disposable>",
            "    /** 缓存终止事件并在 scheduler 上延迟转发的内部 observer。 */\n"
            "    static final class Delay extends AtomicReference<Disposable>",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 延迟到期后向 downstream 发出缓存的错误或完成。 */\n"
            "        @Override\n        public void run() {",
        ),
    ],
    "CompletableDetach.java": [
        (
            "/**\n * Breaks the references between the upstream and downstream when the Completable terminates.\n * <p>History: 2.1.5 - experimental\n * @since 2.2\n */",
            "/**\n * 在 Completable 终止时断开上游与下游之间的引用，便于垃圾回收。\n * <p>History: 2.1.5 - experimental\n * @since 2.2\n */",
        ),
        (
            "    public CompletableDetach(CompletableSource source) {",
            "    /** @param source 上游 CompletableSource */\n"
            "    public CompletableDetach(CompletableSource source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 订阅 source 并在终止后清除对下游的引用。 */\n"
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
        (
            "    static final class DetachCompletableObserver implements CompletableObserver, Disposable {",
            "    /** 终止时断开 upstream/downstream 引用的内部 observer。 */\n"
            "    static final class DetachCompletableObserver implements CompletableObserver, Disposable {",
        ),
        (
            "        @Override\n        public void dispose() {",
            "        /** 清除 downstream 引用并 dispose 上游。 */\n"
            "        @Override\n        public void dispose() {",
        ),
    ],
    "CompletableDisposeOn.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class CompletableDisposeOn extends Completable {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 在指定 {@link Scheduler} 上执行上游 disposable 的 dispose 操作。\n"
            " */\n"
            "public final class CompletableDisposeOn extends Completable {",
        ),
        (
            "    public CompletableDisposeOn(CompletableSource source, Scheduler scheduler) {",
            "    /**\n"
            "     * @param source 上游 CompletableSource\n"
            "     * @param scheduler 执行 dispose 的 Scheduler\n"
            "     */\n"
            "    public CompletableDisposeOn(CompletableSource source, Scheduler scheduler) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final CompletableObserver observer) {",
            "    /** 订阅 source；下游 dispose 时在 scheduler 上异步取消上游。 */\n"
            "    @Override\n    protected void subscribeActual(final CompletableObserver observer) {",
        ),
        (
            "    static final class DisposeOnObserver implements CompletableObserver, Disposable, Runnable {",
            "    /** 在 scheduler 上异步 dispose 上游的内部 observer。 */\n"
            "    static final class DisposeOnObserver implements CompletableObserver, Disposable, Runnable {",
        ),
        (
            "        @Override\n        public void dispose() {",
            "        /** 标记 disposed 并在 scheduler 上调度上游 dispose。 */\n"
            "        @Override\n        public void dispose() {",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 在 scheduler 线程上 dispose 上游 disposable。 */\n"
            "        @Override\n        public void run() {",
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
            "wave7a observers+Completable* [0:15]",
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
