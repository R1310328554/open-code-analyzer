#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-9b flowable operators [15:30]."""
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
WAVE9B_FILE = Path("/tmp/rxjava_w9b.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE9B_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "FlowableDebounceTimed.java": [
        (
            "import io.reactivex.rxjava4.subscribers.SerializedSubscriber;\n\npublic final class FlowableDebounceTimed",
            "import io.reactivex.rxjava4.subscribers.SerializedSubscriber;\n\n"
            "/**\n"
            " * 在指定静默间隔内无新元素时才向下游发射最后一项；新元素到来会重置计时器。\n"
            " * 被丢弃的值可通过 {@code onDropped} 回调处理。\n"
            " * @param <T> 上游元素类型\n"
            " */\n"
            "public final class FlowableDebounceTimed",
        ),
        (
            "    public FlowableDebounceTimed(Flowable<T> source, long timeout, TimeUnit unit, Scheduler scheduler, Consumer<? super T> onDropped) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param timeout 静默超时量\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 调度计时任务的 Scheduler\n"
            "     * @param onDropped 被 debounce 丢弃元素时的回调（可为 null）\n"
            "     */\n"
            "    public FlowableDebounceTimed(Flowable<T> source, long timeout, TimeUnit unit, Scheduler scheduler, Consumer<? super T> onDropped) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 以 SerializedSubscriber 包装下游并订阅 DebounceTimedSubscriber。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 递增 index、取消旧计时器，必要时调用 onDropped，再调度新 DebounceEmitter。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 终止时立即 emit 当前待发射项，再 onComplete 并 dispose worker。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
        (
            "        void emit(long idx, T t, DebounceEmitter<T> emitter) {",
            "        /** index 匹配且背压允许时向下游 onNext 并 produced。 */\n"
            "        void emit(long idx, T t, DebounceEmitter<T> emitter) {",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 计时到期，触发 emit。 */\n"
            "        @Override\n        public void run() {",
        ),
    ],
    "FlowableDefer.java": [
        (
            "import java.util.Objects;\n\npublic final class FlowableDefer",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 每次订阅时通过 {@link Supplier} 获取 {@link Publisher} 并订阅，实现延迟创建上游。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableDefer",
        ),
        (
            "    public FlowableDefer(Supplier<? extends Publisher<? extends T>> supplier) {",
            "    /** @param supplier 每次订阅时提供 Publisher 的 Supplier */\n"
            "    public FlowableDefer(Supplier<? extends Publisher<? extends T>> supplier) {",
        ),
        (
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
            "    /** 调用 supplier 获取 Publisher，null 或异常时通过 EmptySubscription 通知下游。 */\n"
            "    @Override\n    public void subscribeActual(Subscriber<? super T> s) {",
        ),
    ],
    "FlowableDelay.java": [
        (
            "import io.reactivex.rxjava4.subscribers.SerializedSubscriber;\n\npublic final class FlowableDelay",
            "import io.reactivex.rxjava4.subscribers.SerializedSubscriber;\n\n"
            "/**\n"
            " * 将上游 onNext/onError/onComplete 在指定延迟后转发给下游。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableDelay",
        ),
        (
            "    public FlowableDelay(Flowable<T> source, long delay, TimeUnit unit, Scheduler scheduler, boolean delayError) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param delay 延迟量\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 调度延迟任务的 Scheduler\n"
            "     * @param delayError true 时 onError 也延迟；false 时 onError 立即转发且下游需 SerializedSubscriber\n"
            "     */\n"
            "    public FlowableDelay(Flowable<T> source, long delay, TimeUnit unit, Scheduler scheduler, boolean delayError) {",
        ),
        (
            "        @Override\n        public void onNext(final T t) {",
            "        /** 在 worker 上延迟调度 onNext。 */\n"
            "        @Override\n        public void onNext(final T t) {",
        ),
        (
            "        @Override\n        public void onError(final Throwable t) {",
            "        /** 按 delayError 决定是否延迟 onError。 */\n"
            "        @Override\n        public void onError(final Throwable t) {",
        ),
        (
            "            @Override\n            public void run() {",
            "            /** worker 未 dispose 时向下游 onNext。 */\n"
            "            @Override\n            public void run() {",
        ),
    ],
    "FlowableDelaySubscriptionOther.java": [
        (
            "/**\n * Delays the subscription to the main source until the other\n * observable fires an event or completes.\n * @param <T> the main type\n * @param <U> the other value type, ignored\n */",
            "/**\n"
            " * 延迟订阅主源，直到 other {@link Publisher} 发出事件或完成。\n"
            " * @param <T> 主源元素类型\n"
            " * @param <U> other 源元素类型（被忽略）\n"
            " */",
        ),
        (
            "    public FlowableDelaySubscriptionOther(Publisher<? extends T> main, Publisher<U> other) {",
            "    /**\n"
            "     * @param main 主 Publisher\n"
            "     * @param other 触发主源订阅的 Publisher\n"
            "     */\n"
            "    public FlowableDelaySubscriptionOther(Publisher<? extends T> main, Publisher<U> other) {",
        ),
        (
            "        void next() {",
            "        /** other 触发后订阅 main。 */\n"
            "        void next() {",
        ),
        (
            "            @Override\n            public void onNext(Object t) {",
            "            /** other 首项到达后取消 other 并订阅 main。 */\n"
            "            @Override\n            public void onNext(Object t) {",
        ),
        (
            "            @Override\n            public void onComplete() {",
            "            /** other 完成时订阅 main。 */\n"
            "            @Override\n            public void onComplete() {",
        ),
    ],
    "FlowableDematerialize.java": [
        (
            "import java.util.Objects;\n\npublic final class FlowableDematerialize",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 将上游元素经 selector 转为 {@link Notification}，再物化为 onNext/onError/onComplete。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <R> Notification 承载的值类型\n"
            " */\n"
            "public final class FlowableDematerialize",
        ),
        (
            "    public FlowableDematerialize(Flowable<T> source, Function<? super T, ? extends Notification<R>> selector) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param selector 将元素映射为 Notification 的函数\n"
            "     */\n"
            "    public FlowableDematerialize(Flowable<T> source, Function<? super T, ? extends Notification<R>> selector) {",
        ),
        (
            "        @Override\n        public void onNext(T item) {",
            "        /** 应用 selector；OnError 取消上游并 onError，OnComplete 取消并 onComplete，否则 emit 值。 */\n"
            "        @Override\n        public void onNext(T item) {",
        ),
    ],
    "FlowableDetach.java": [
        (
            "import io.reactivex.rxjava4.internal.util.EmptyComponent;\n\npublic final class FlowableDetach",
            "import io.reactivex.rxjava4.internal.util.EmptyComponent;\n\n"
            "/**\n"
            " * 在 cancel/onError/onComplete 后将 upstream/downstream 替换为 {@link EmptyComponent}，防止泄漏。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableDetach",
        ),
        (
            "    public FlowableDetach(Flowable<T> source) {",
            "    /** @param source 上游 Flowable */\n"
            "    public FlowableDetach(Flowable<T> source) {",
        ),
        (
            "        @Override\n        public void cancel() {",
            "        /** 取消 upstream 并将引用置为 EmptyComponent。 */\n"
            "        @Override\n        public void cancel() {",
        ),
        (
            "        @Override\n        public void onError(Throwable t) {",
            "        /** 转发 onError 后 detach upstream/downstream。 */\n"
            "        @Override\n        public void onError(Throwable t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 转发 onComplete 后 detach upstream/downstream。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "FlowableDistinct.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableDistinct",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 按 keySelector 提取键去重，仅首次出现的元素向下游发射。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <K> 键类型\n"
            " */\n"
            "public final class FlowableDistinct",
        ),
        (
            "    public FlowableDistinct(Flowable<T> source, Function<? super T, K> keySelector, Supplier<? extends Collection<? super K>> collectionSupplier) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param keySelector 提取去重键的函数\n"
            "     * @param collectionSupplier 提供存储已见键的 Collection 的 Supplier\n"
            "     */\n"
            "    public FlowableDistinct(Flowable<T> source, Function<? super T, K> keySelector, Supplier<? extends Collection<? super K>> collectionSupplier) {",
        ),
        (
            "        @Override\n        public void onNext(T value) {",
            "        /** 键首次加入 collection 时 onNext，否则 request(1) 跳过重复项。 */\n"
            "        @Override\n        public void onNext(T value) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 清空 collection 后 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "FlowableDistinctUntilChanged.java": [
        (
            "import io.reactivex.rxjava4.operators.ConditionalSubscriber;\n\npublic final class FlowableDistinctUntilChanged",
            "import io.reactivex.rxjava4.operators.ConditionalSubscriber;\n\n"
            "/**\n"
            " * 过滤连续重复项：仅当 key 与上一项 comparer 判定不同时向下游发射。\n"
            " * @param <T> 上游元素类型\n"
            " * @param <K> 比较键类型\n"
            " */\n"
            "public final class FlowableDistinctUntilChanged",
        ),
        (
            "    public FlowableDistinctUntilChanged(Flowable<T> source, Function<? super T, K> keySelector, BiPredicate<? super K, ? super K> comparer) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param keySelector 提取比较键的函数\n"
            "     * @param comparer 比较相邻键是否相等的 BiPredicate\n"
            "     */\n"
            "    public FlowableDistinctUntilChanged(Flowable<T> source, Function<? super T, K> keySelector, BiPredicate<? super K, ? super K> comparer) {",
        ),
        (
            "        @Override\n        public boolean tryOnNext(T t) {",
            "        /** 键与上一项不同时 onNext 并返回 true，相同时返回 false。 */\n"
            "        @Override\n        public boolean tryOnNext(T t) {",
        ),
    ],
    "FlowableDoAfterNext.java": [
        (
            "/**\n * Calls a consumer after pushing the current item to the downstream.\n * <p>History: 2.0.1 - experimental\n * @param <T> the value type\n * @since 2.1\n */",
            "/**\n"
            " * 向下游推送当前项后调用 {@link Consumer}。\n"
            " * <p>History: 2.0.1 - experimental\n"
            " * @param <T> 值类型\n"
            " * @since 2.1\n"
            " */",
        ),
        (
            "    public FlowableDoAfterNext(Flowable<T> source, Consumer<? super T> onAfterNext) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param onAfterNext 下游 onNext 之后调用的 Consumer\n"
            "     */\n"
            "    public FlowableDoAfterNext(Flowable<T> source, Consumer<? super T> onAfterNext) {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 先 downstream.onNext，再在非 fusion 模式下调用 onAfterNext。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public boolean tryOnNext(T t) {",
            "        /** tryOnNext 成功后调用 onAfterNext。 */\n"
            "        @Override\n        public boolean tryOnNext(T t) {",
        ),
    ],
    "FlowableDoFinally.java": [
        (
            "/**\n * Execute an action after an onError, onComplete or a cancel event.\n * <p>History: 2.0.1 - experimental\n * @param <T> the value type\n * @since 2.1\n */",
            "/**\n"
            " * 在 onError、onComplete 或 cancel 后执行 {@link Action}（仅一次）。\n"
            " * <p>History: 2.0.1 - experimental\n"
            " * @param <T> 值类型\n"
            " * @since 2.1\n"
            " */",
        ),
        (
            "    public FlowableDoFinally(Flowable<T> source, Action onFinally) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param onFinally 终止或取消时执行的 Action\n"
            "     */\n"
            "    public FlowableDoFinally(Flowable<T> source, Action onFinally) {",
        ),
        (
            "        @Override\n        public void onError(Throwable t) {",
            "        /** 转发 onError 后 runFinally。 */\n"
            "        @Override\n        public void onError(Throwable t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 转发 onComplete 后 runFinally。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
        (
            "        void runFinally() {",
            "        /** CAS 保证 onFinally 仅执行一次。 */\n"
            "        void runFinally() {",
        ),
    ],
    "FlowableDoOnEach.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableDoOnEach",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 在 onNext/onError/onComplete 及终止后分别调用对应副作用回调。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableDoOnEach",
        ),
        (
            "    public FlowableDoOnEach(Flowable<T> source, Consumer<? super T> onNext,\n            Consumer<? super Throwable> onError,\n            Action onComplete,\n            Action onAfterTerminate) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param onNext 每项 onNext 前调用\n"
            "     * @param onError onError 时调用\n"
            "     * @param onComplete onComplete 时调用\n"
            "     * @param onAfterTerminate 终止后调用\n"
            "     */\n"
            "    public FlowableDoOnEach(Flowable<T> source, Consumer<? super T> onNext,\n            Consumer<? super Throwable> onError,\n            Action onComplete,\n            Action onAfterTerminate) {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 调用 onNext 回调后向下游转发。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onError(Throwable t) {",
            "        /** 调用 onError 与 onAfterTerminate 后转发或合成 CompositeException。 */\n"
            "        @Override\n        public void onError(Throwable t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 调用 onComplete 与 onAfterTerminate 后 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "FlowableDoOnLifecycle.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableDoOnLifecycle",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 在订阅、request 与 cancel 生命周期节点注入副作用回调。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableDoOnLifecycle",
        ),
        (
            "    public FlowableDoOnLifecycle(Flowable<T> source, Consumer<? super Subscription> onSubscribe,\n            LongConsumer onRequest, Action onCancel) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param onSubscribe 上游 onSubscribe 时调用\n"
            "     * @param onRequest downstream request 时调用\n"
            "     * @param onCancel cancel 时调用\n"
            "     */\n"
            "    public FlowableDoOnLifecycle(Flowable<T> source, Consumer<? super Subscription> onSubscribe,\n            LongConsumer onRequest, Action onCancel) {",
        ),
        (
            "        @Override\n        public void onSubscribe(Subscription s) {",
            "        /** 先 onSubscribe.accept，再 validate 并向下游传递 SubscriptionLambdaSubscriber。 */\n"
            "        @Override\n        public void onSubscribe(Subscription s) {",
        ),
        (
            "        @Override\n        public void request(long n) {",
            "        /** 调用 onRequest 后转发 upstream.request。 */\n"
            "        @Override\n        public void request(long n) {",
        ),
        (
            "        @Override\n        public void cancel() {",
            "        /** 调用 onCancel 后 cancel upstream。 */\n"
            "        @Override\n        public void cancel() {",
        ),
    ],
    "FlowableElementAt.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableElementAt",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 发射上游第 index 个元素（0 起）；不足时按 defaultValue 或 errorOnFewer 处理。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableElementAt",
        ),
        (
            "    public FlowableElementAt(Flowable<T> source, long index, T defaultValue, boolean errorOnFewer) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param index 目标索引（0 起）\n"
            "     * @param defaultValue 元素不足时的默认值（可为 null）\n"
            "     * @param errorOnFewer true 且无 defaultValue 时 onError(NoSuchElementException)\n"
            "     */\n"
            "    public FlowableElementAt(Flowable<T> source, long index, T defaultValue, boolean errorOnFewer) {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** count 达 index 时 cancel 上游并 complete(t)。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 未达 index 时按 defaultValue 或 errorOnFewer 完成。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "FlowableElementAtMaybe.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableElementAtMaybe",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 将上游第 index 个元素作为 {@link Maybe} 发射；不存在则 onComplete。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableElementAtMaybe",
        ),
        (
            "    public FlowableElementAtMaybe(Flowable<T> source, long index) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param index 目标索引（0 起）\n"
            "     */\n"
            "    public FlowableElementAtMaybe(Flowable<T> source, long index) {",
        ),
        (
            "    @Override\n    public Flowable<T> fuseToFlowable() {",
            "    /** 融合为 {@link FlowableElementAt}（无 defaultValue，errorOnFewer=false）。 */\n"
            "    @Override\n    public Flowable<T> fuseToFlowable() {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** count 达 index 时 cancel 并 onSuccess(t)。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 未找到目标索引时 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "FlowableElementAtMaybePublisher.java": [
        (
            "/**\n * Emits the indexth element from a Publisher as a Maybe.\n *\n * @param <T> the element type of the source\n * @since 3.0.0\n */",
            "/**\n"
            " * 将 {@link Publisher} 的第 index 个元素作为 {@link Maybe} 发射。\n"
            " *\n"
            " * @param <T> 源元素类型\n"
            " * @since 3.0.0\n"
            " */",
        ),
        (
            "    public FlowableElementAtMaybePublisher(Publisher<T> source, long index) {",
            "    /**\n"
            "     * @param source 上游 Publisher\n"
            "     * @param index 目标索引（0 起）\n"
            "     */\n"
            "    public FlowableElementAtMaybePublisher(Publisher<T> source, long index) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 复用 {@link FlowableElementAtMaybe.ElementAtSubscriber} 订阅 source。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
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
            "wave9b flowable operators [15:30]",
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
        print(f"Marked {ok} files done in queue (note=wave9b)")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
