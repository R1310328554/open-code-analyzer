#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-21b parallel + single operators [15:30]."""
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
WAVE21B_FILE = Path("/tmp/rxjava_w21b.txt")
SCRIPT_NAME = "annotate_rxjava_wave21b_batch15_30.py"
BATCH_FILES = [
    ln.strip()
    for ln in WAVE21B_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

GUARD_FILES = [
    VER
    / "analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "ParallelFilterTry.java": [
        (
            "/**\n * Filters each 'rail' of the source ParallelFlowable with a predicate function.\n *\n * @param <T> the input value type\n */",
            "/**\n"
            " * 对 ParallelFlowable 每条并行轨道用 predicate 过滤；\n"
            " * predicate 异常时由 errorHandler 决定 RETRY/SKIP/STOP。\n"
            " *\n * @param <T> 输入元素类型\n"
            " */",
        ),
        (
            "    public ParallelFilterTry(ParallelFlowable<T> source, Predicate<? super T> predicate,\n            BiFunction<? super Long, ? super Throwable, ParallelFailureHandling> errorHandler) {",
            "    /**\n"
            "     * @param source 并行上游 ParallelFlowable\n"
            "     * @param predicate 过滤谓词\n"
            "     * @param errorHandler 谓词异常时的失败处理策略\n"
            "     */\n"
            "    public ParallelFilterTry(ParallelFlowable<T> source, Predicate<? super T> predicate,\n            BiFunction<? super Long, ? super Throwable, ParallelFailureHandling> errorHandler) {",
        ),
        (
            "    @Override\n    public void subscribe(Subscriber<? super T>[] subscribers) {",
            "    /** 按 ConditionalSubscriber 分支创建各轨道 Filter Subscriber 并订阅上游。 */\n"
            "    @Override\n    public void subscribe(Subscriber<? super T>[] subscribers) {",
        ),
        (
            "    abstract static class BaseFilterSubscriber<T> implements ConditionalSubscriber<T>, Subscription {",
            "    /** 过滤基类：tryOnNext 失败且未 done 时 request(1) 补偿。 */\n"
            "    abstract static class BaseFilterSubscriber<T> implements ConditionalSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        public final void onNext(T t) {",
            "        /** tryOnNext 返回 false 且未 done 时向上游 request(1)。 */\n"
            "        @Override\n        public final void onNext(T t) {",
        ),
        (
            "    static final class ParallelFilterSubscriber<T> extends BaseFilterSubscriber<T> {",
            "    /** 普通 Subscriber 上的带重试过滤实现。 */\n"
            "    static final class ParallelFilterSubscriber<T> extends BaseFilterSubscriber<T> {",
        ),
        (
            "        @Override\n        public boolean tryOnNext(T t) {",
            "        /** predicate 为 true 时 downstream.onNext；异常走 errorHandler 循环。 */\n"
            "        @Override\n        public boolean tryOnNext(T t) {",
        ),
        (
            "    static final class ParallelFilterConditionalSubscriber<T> extends BaseFilterSubscriber<T> {",
            "    /** ConditionalSubscriber 上的带重试过滤实现。 */\n"
            "    static final class ParallelFilterConditionalSubscriber<T> extends BaseFilterSubscriber<T> {",
        ),
    ],
    "ParallelFlatMap.java": [
        (
            "/**\n * Flattens the generated Publishers on each rail.\n *\n * @param <T> the input value type\n * @param <R> the output value type\n */",
            "/**\n"
            " * 对每条并行轨道将元素经 mapper 映射为 Publisher 并 flatMap 展开。\n"
            " *\n * @param <T> 输入元素类型\n"
            " * @param <R> 输出元素类型\n"
            " */",
        ),
        (
            "    public ParallelFlatMap(\n            ParallelFlowable<T> source,\n            Function<? super T, ? extends Publisher<? extends R>> mapper,\n            boolean delayError,\n            int maxConcurrency,\n            int prefetch) {",
            "    /**\n"
            "     * @param source 并行上游\n"
            "     * @param mapper 将元素映射为 inner Publisher 的函数\n"
            "     * @param delayError 为 true 时延迟聚合错误\n"
            "     * @param maxConcurrency 最大并发 inner 数\n"
            "     * @param prefetch inner 预取数量\n"
            "     */\n"
            "    public ParallelFlatMap(\n            ParallelFlowable<T> source,\n            Function<? super T, ? extends Publisher<? extends R>> mapper,\n            boolean delayError,\n            int maxConcurrency,\n            int prefetch) {",
        ),
        (
            "    @Override\n    public void subscribe(Subscriber<? super R>[] subscribers) {",
            "    /** 每条轨道用 FlowableFlatMap.subscribe 包装下游 Subscriber。 */\n"
            "    @Override\n    public void subscribe(Subscriber<? super R>[] subscribers) {",
        ),
    ],
    "ParallelFlatMapIterable.java": [
        (
            "/**\n * Flattens the generated {@link Iterable}s on each rail.\n *\n * @param <T> the input value type\n * @param <R> the output value type\n * @since 3.0.0\n */",
            "/**\n"
            " * 对每条并行轨道将元素经 mapper 映射为 Iterable 并逐元素展开发射。\n"
            " *\n * @param <T> 输入元素类型\n"
            " * @param <R> 输出元素类型\n"
            " * @since 3.0.0\n"
            " */",
        ),
        (
            "    public ParallelFlatMapIterable(\n            ParallelFlowable<T> source,\n            Function<? super T, ? extends Iterable<? extends R>> mapper,\n            int prefetch) {",
            "    /**\n"
            "     * @param source 并行上游\n"
            "     * @param mapper 将元素映射为 Iterable 的函数\n"
            "     * @param prefetch 预取数量\n"
            "     */\n"
            "    public ParallelFlatMapIterable(\n            ParallelFlowable<T> source,\n            Function<? super T, ? extends Iterable<? extends R>> mapper,\n            int prefetch) {",
        ),
        (
            "    @Override\n    public void subscribe(Subscriber<? super R>[] subscribers) {",
            "    /** 每条轨道用 FlowableFlattenIterable.subscribe 包装下游 Subscriber。 */\n"
            "    @Override\n    public void subscribe(Subscriber<? super R>[] subscribers) {",
        ),
    ],
    "ParallelFromArray.java": [
        (
            "/**\n * Wraps multiple Publishers into a ParallelFlowable which runs them\n * in parallel.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 将多个 Publisher 包装为 ParallelFlowable，各 Publisher 对应一条并行轨道。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public ParallelFromArray(Publisher<T>[] sources) {",
            "    /** @param sources 与并行度等长的 Publisher 数组 */\n"
            "    public ParallelFromArray(Publisher<T>[] sources) {",
        ),
        (
            "    @Override\n    public void subscribe(Subscriber<? super T>[] subscribers) {",
            "    /** 按索引将 sources[i] 订阅至 subscribers[i]。 */\n"
            "    @Override\n    public void subscribe(Subscriber<? super T>[] subscribers) {",
        ),
    ],
    "ParallelMap.java": [
        (
            "/**\n * Maps each 'rail' of the source ParallelFlowable with a mapper function.\n *\n * @param <T> the input value type\n * @param <R> the output value type\n */",
            "/**\n"
            " * 对 ParallelFlowable 每条并行轨道用 mapper 逐元素映射。\n"
            " *\n * @param <T> 输入元素类型\n"
            " * @param <R> 输出元素类型\n"
            " */",
        ),
        (
            "    public ParallelMap(ParallelFlowable<T> source, Function<? super T, ? extends R> mapper) {",
            "    /**\n"
            "     * @param source 并行上游\n"
            "     * @param mapper 映射函数\n"
            "     */\n"
            "    public ParallelMap(ParallelFlowable<T> source, Function<? super T, ? extends R> mapper) {",
        ),
        (
            "    @Override\n    public void subscribe(Subscriber<? super R>[] subscribers) {",
            "    /** 按 ConditionalSubscriber 分支创建各轨道 Map Subscriber 并订阅上游。 */\n"
            "    @Override\n    public void subscribe(Subscriber<? super R>[] subscribers) {",
        ),
        (
            "    static final class ParallelMapSubscriber<T, R> implements FlowableSubscriber<T>, Subscription {",
            "    /** 普通 Subscriber 上的并行 map 实现。 */\n"
            "    static final class ParallelMapSubscriber<T, R> implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 应用 mapper 后 downstream.onNext；异常 cancel 并 onError。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "    static final class ParallelMapConditionalSubscriber<T, R> implements ConditionalSubscriber<T>, Subscription {",
            "    /** ConditionalSubscriber 上的并行 map 实现。 */\n"
            "    static final class ParallelMapConditionalSubscriber<T, R> implements ConditionalSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        public boolean tryOnNext(T t) {",
            "        /** 映射后 downstream.tryOnNext；异常 cancel 并 onError。 */\n"
            "        @Override\n        public boolean tryOnNext(T t) {",
        ),
    ],
    "ParallelMapTry.java": [
        (
            "/**\n * Maps each 'rail' of the source ParallelFlowable with a mapper function\n * and handle any failure based on a handler function.\n * <p>History: 2.0.8 - experimental\n * @param <T> the input value type\n * @param <R> the output value type\n * @since 2.2\n */",
            "/**\n"
            " * 对每条并行轨道用 mapper 映射；mapper 异常时由 errorHandler 决定 RETRY/SKIP/STOP。\n"
            " * <p>History: 2.0.8 - experimental\n"
            " * @param <T> 输入元素类型\n"
            " * @param <R> 输出元素类型\n"
            " * @since 2.2\n"
            " */",
        ),
        (
            "    public ParallelMapTry(ParallelFlowable<T> source, Function<? super T, ? extends R> mapper,\n            BiFunction<? super Long, ? super Throwable, ParallelFailureHandling> errorHandler) {",
            "    /**\n"
            "     * @param source 并行上游\n"
            "     * @param mapper 映射函数\n"
            "     * @param errorHandler 映射异常时的失败处理策略\n"
            "     */\n"
            "    public ParallelMapTry(ParallelFlowable<T> source, Function<? super T, ? extends R> mapper,\n            BiFunction<? super Long, ? super Throwable, ParallelFailureHandling> errorHandler) {",
        ),
        (
            "    static final class ParallelMapTrySubscriber<T, R> implements ConditionalSubscriber<T>, Subscription {",
            "    /** 普通 Subscriber 上的带重试 map 实现。 */\n"
            "    static final class ParallelMapTrySubscriber<T, R> implements ConditionalSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        public boolean tryOnNext(T t) {",
            "        /** mapper 成功后 downstream.onNext；异常走 errorHandler 循环。 */\n"
            "        @Override\n        public boolean tryOnNext(T t) {",
        ),
        (
            "    static final class ParallelMapTryConditionalSubscriber<T, R> implements ConditionalSubscriber<T>, Subscription {",
            "    /** ConditionalSubscriber 上的带重试 map 实现。 */\n"
            "    static final class ParallelMapTryConditionalSubscriber<T, R> implements ConditionalSubscriber<T>, Subscription {",
        ),
    ],
    "ParallelPeek.java": [
        (
            "/**\n * Execute a Consumer in each 'rail' for the current element passing through.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 在每条并行轨道上于元素流经时执行一组 Consumer/Action 回调（peek 副作用）。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public ParallelPeek(ParallelFlowable<T> source,\n            Consumer<? super T> onNext,\n            Consumer<? super T> onAfterNext,\n            Consumer<? super Throwable> onError,\n            Action onComplete,\n            Action onAfterTerminated,\n            Consumer<? super Subscription> onSubscribe,\n            LongConsumer onRequest,\n            Action onCancel\n    ) {",
            "    /**\n"
            "     * @param source 并行上游\n"
            "     * @param onNext 元素到达时回调\n"
            "     * @param onAfterNext 转发后回调\n"
            "     * @param onError 错误时回调\n"
            "     * @param onComplete 完成时回调\n"
            "     * @param onAfterTerminated 终止后回调\n"
            "     * @param onSubscribe 订阅建立时回调\n"
            "     * @param onRequest request 时回调\n"
            "     * @param onCancel cancel 时回调\n"
            "     */\n"
            "    public ParallelPeek(ParallelFlowable<T> source,\n            Consumer<? super T> onNext,\n            Consumer<? super T> onAfterNext,\n            Consumer<? super Throwable> onError,\n            Action onComplete,\n            Action onAfterTerminated,\n            Consumer<? super Subscription> onSubscribe,\n            LongConsumer onRequest,\n            Action onCancel\n    ) {",
        ),
        (
            "    static final class ParallelPeekSubscriber<T> implements FlowableSubscriber<T>, Subscription {",
            "    /** 单轨道 peek：在转发前后调用 parent 中注册的回调。 */\n"
            "    static final class ParallelPeekSubscriber<T> implements FlowableSubscriber<T>, Subscription {",
        ),
        (
            "        @Override\n        public void request(long n) {",
            "        /** 先 onRequest.accept 再 upstream.request。 */\n"
            "        @Override\n        public void request(long n) {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** onNext → downstream.onNext → onAfterNext；回调异常转 onError。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onError(Throwable t) {",
            "        /** onError 回调后转发错误，再 onAfterTerminated。 */\n"
            "        @Override\n        public void onError(Throwable t) {",
        ),
    ],
    "ParallelReduce.java": [
        (
            "/**\n * Reduce the sequence of values in each 'rail' to a single value.\n *\n * @param <T> the input value type\n * @param <R> the result value type\n */",
            "/**\n"
            " * 将每条并行轨道的元素序列归约为单个值（各轨道独立 reduce）。\n"
            " *\n * @param <T> 输入元素类型\n"
            " * @param <R> 归约结果类型\n"
            " */",
        ),
        (
            "    public ParallelReduce(ParallelFlowable<? extends T> source, Supplier<R> initialSupplier, BiFunction<R, ? super T, R> reducer) {",
            "    /**\n"
            "     * @param source 并行上游\n"
            "     * @param initialSupplier 每条轨道的初始累加器 Supplier\n"
            "     * @param reducer 累加函数 (acc, value) -> acc\n"
            "     */\n"
            "    public ParallelReduce(ParallelFlowable<? extends T> source, Supplier<R> initialSupplier, BiFunction<R, ? super T, R> reducer) {",
        ),
        (
            "    void reportError(Subscriber<?>[] subscribers, Throwable ex) {",
            "    /** initialSupplier 失败时向所有 Subscriber 发送 EmptySubscription.error。 */\n"
            "    void reportError(Subscriber<?>[] subscribers, Throwable ex) {",
        ),
        (
            "    static final class ParallelReduceSubscriber<T, R> extends DeferredScalarSubscriber<T, R> {",
            "    /** 单轨道 reduce：逐 onNext 更新 accumulator，onComplete 时 complete。 */\n"
            "    static final class ParallelReduceSubscriber<T, R> extends DeferredScalarSubscriber<T, R> {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** reducer.apply(accumulator, t) 更新累加器；异常 cancel 并 onError。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 发射最终 accumulator 并 complete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "ParallelReduceFull.java": [
        (
            "/**\n * Reduces all 'rails' into a single value which then gets reduced into a single\n * Publisher sequence.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 将所有并行轨道归约为单个值：各轨道先局部 reduce，\n"
            " * 再以 SlotPair 两两合并，最终发射至单一 Flowable 下游。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public ParallelReduceFull(ParallelFlowable<? extends T> source, BiFunction<T, T, T> reducer) {",
            "    /**\n"
            "     * @param source 并行上游\n"
            "     * @param reducer 两值合并函数\n"
            "     */\n"
            "    public ParallelReduceFull(ParallelFlowable<? extends T> source, BiFunction<T, T, T> reducer) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 创建 MainSubscriber 并订阅各 inner 轨道。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class ParallelReduceFullMainSubscriber<T> extends DeferredScalarSubscription<T> {",
            "    /** 协调各 inner 完成值，经 addValue 两两归并后 complete。 */\n"
            "    static final class ParallelReduceFullMainSubscriber<T> extends DeferredScalarSubscription<T> {",
        ),
        (
            "        SlotPair<T> addValue(T value) {",
            "        /** CAS 获取 SlotPair 双槽，凑齐一对后返回供 reducer 合并。 */\n"
            "        SlotPair<T> addValue(T value) {",
        ),
        (
            "        void innerComplete(T value) {",
            "        /** 接收轨道局部结果，循环 addValue+reducer 直至 remaining 归零。 */\n"
            "        void innerComplete(T value) {",
        ),
        (
            "    static final class ParallelReduceFullInnerSubscriber<T>\n    extends AtomicReference<Subscription>\n    implements FlowableSubscriber<T> {",
            "    /** 单轨道局部 reduce：逐 onNext 累加，onComplete 时 innerComplete(value)。 */\n"
            "    static final class ParallelReduceFullInnerSubscriber<T>\n    extends AtomicReference<Subscription>\n    implements FlowableSubscriber<T> {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 首值缓存，其后 reducer.apply 累加至 value。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "    static final class SlotPair<T> extends AtomicInteger {",
            "    /** 双槽配对结构：tryAcquireSlot 占槽，releaseSlot 凑齐后可供合并。 */\n"
            "    static final class SlotPair<T> extends AtomicInteger {",
        ),
    ],
    "ParallelSortedJoin.java": [
        (
            "/**\n * Given sorted rail sequences (according to the provided comparator) as List\n * emit the smallest item from these parallel Lists to the Subscriber.\n * <p>\n * It expects the source to emit exactly one list (which could be empty).\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 各并行轨道各发射一个已排序 List，按 comparator 多路归并\n"
            " * 依次向下游发射当前最小元素（类似 sorted merge）。\n"
            " * <p>\n * 期望每条轨道恰好发射一个 List（可为空）。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public ParallelSortedJoin(ParallelFlowable<List<T>> source, Comparator<? super T> comparator) {",
            "    /**\n"
            "     * @param source 各轨道发射 List 的并行上游\n"
            "     * @param comparator 元素比较器\n"
            "     */\n"
            "    public ParallelSortedJoin(ParallelFlowable<List<T>> source, Comparator<? super T> comparator) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 创建 SortedJoinSubscription 并订阅各 inner 轨道。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
        (
            "    static final class SortedJoinSubscription<T>\n    extends AtomicInteger\n    implements Subscription {",
            "    /** 多路归并协调器：收集各轨道 List 后 drain 发射最小元素。 */\n"
            "    static final class SortedJoinSubscription<T>\n    extends AtomicInteger\n    implements Subscription {",
        ),
        (
            "        void innerNext(List<T> value, int index) {",
            "        /** 存储第 index 轨道的 List；全部到齐后触发 drain。 */\n"
            "        void innerNext(List<T> value, int index) {",
        ),
        (
            "        void drain() {",
            "        /** wip 门控：跨轨道找最小元素 onNext，耗尽则 onComplete。 */\n"
            "        void drain() {",
        ),
        (
            "    static final class SortedJoinInnerSubscriber<T>\n    extends AtomicReference<Subscription>\n    implements FlowableSubscriber<List<T>> {",
            "    /** 单轨道：接收 List 后交给 parent.innerNext。 */\n"
            "    static final class SortedJoinInnerSubscriber<T>\n    extends AtomicReference<Subscription>\n    implements FlowableSubscriber<List<T>> {",
        ),
    ],
    "SingleAmb.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class SingleAmb<T> extends Single<T> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 竞态订阅多个 SingleSource：首个 onSuccess/onError 获胜，\n"
            " * 其余结果被丢弃（winner CAS 门控）。\n"
            " *\n * @param <T> 元素类型\n"
            " */\n"
            "public final class SingleAmb<T> extends Single<T> {",
        ),
        (
            "    public SingleAmb(SingleSource<? extends T>[] sources, Iterable<? extends SingleSource<? extends T>> sourcesIterable) {",
            "    /**\n"
            "     * @param sources SingleSource 数组（可为 null，此时用 sourcesIterable）\n"
            "     * @param sourcesIterable 可迭代的 SingleSource 集合\n"
            "     */\n"
            "    public SingleAmb(SingleSource<? extends T>[] sources, Iterable<? extends SingleSource<? extends T>> sourcesIterable) {",
        ),
        (
            "    @Override\n    @SuppressWarnings(\"unchecked\")\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
            "    /** 并行订阅全部 SingleSource，AmbSingleObserver 以 winner 决定唯一结果。 */\n"
            "    @Override\n    @SuppressWarnings(\"unchecked\")\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class AmbSingleObserver<T> implements SingleObserver<T> {",
            "    /** 竞态 Observer：winner CAS 成功时 dispose 集合并转发结果。 */\n"
            "    static final class AmbSingleObserver<T> implements SingleObserver<T> {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** winner 时 dispose 集合并 downstream.onSuccess。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** winner 时 dispose 并 onError；否则 RxJavaPlugins.onError。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
    ],
    "SingleCache.java": [
        (
            "import io.reactivex.rxjava4.disposables.Disposable;\n\npublic final class SingleCache<T> extends Single<T> implements SingleObserver<T> {",
            "import io.reactivex.rxjava4.disposables.Disposable;\n\n"
            "/**\n"
            " * 缓存 Single 的首个 onSuccess/onError 结果，\n"
            " * 后续订阅者直接重放缓存值（wip 门控仅订阅一次上游）。\n"
            " *\n * @param <T> 元素类型\n"
            " */\n"
            "public final class SingleCache<T> extends Single<T> implements SingleObserver<T> {",
        ),
        (
            "    @SuppressWarnings(\"unchecked\")\n    public SingleCache(SingleSource<? extends T> source) {",
            "    /** @param source 被缓存的 SingleSource */\n"
            "    @SuppressWarnings(\"unchecked\")\n    public SingleCache(SingleSource<? extends T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
            "    /** add 观察者；wip==0 时首次 subscribe 上游，已终止则直接重放缓存。 */\n"
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super T> observer) {",
        ),
        (
            "    boolean add(CacheDisposable<T> observer) {",
            "    /** CAS 将 observer 追加至 observers 数组。 */\n"
            "    boolean add(CacheDisposable<T> observer) {",
        ),
        (
            "    @SuppressWarnings(\"unchecked\")\n    void remove(CacheDisposable<T> observer) {",
            "    /** dispose 时 CAS 从 observers 数组移除 observer。 */\n"
            "    @SuppressWarnings(\"unchecked\")\n    void remove(CacheDisposable<T> observer) {",
        ),
        (
            "    @SuppressWarnings(\"unchecked\")\n    @Override\n    public void onSuccess(T value) {",
            "    /** 缓存 value 并向 TERMINATED 前全部 observer 广播 onSuccess。 */\n"
            "    @SuppressWarnings(\"unchecked\")\n    @Override\n    public void onSuccess(T value) {",
        ),
        (
            "    @SuppressWarnings(\"unchecked\")\n    @Override\n    public void onError(Throwable e) {",
            "    /** 缓存 error 并向全部 observer 广播 onError。 */\n"
            "    @SuppressWarnings(\"unchecked\")\n    @Override\n    public void onError(Throwable e) {",
        ),
        (
            "    static final class CacheDisposable<T>\n    extends AtomicBoolean\n    implements Disposable {",
            "    /** 下游 Disposable 包装：dispose 时从 parent 移除自身。 */\n"
            "    static final class CacheDisposable<T>\n    extends AtomicBoolean\n    implements Disposable {",
        ),
    ],
    "SingleContains.java": [
        (
            "import io.reactivex.rxjava4.functions.BiPredicate;\n\npublic final class SingleContains<T> extends Single<Boolean> {",
            "import io.reactivex.rxjava4.functions.BiPredicate;\n\n"
            "/**\n"
            " * 订阅上游 Single，用 BiPredicate 比较成功值与给定 value，\n"
            " * 发射比较结果 Boolean。\n"
            " *\n * @param <T> 上游元素类型\n"
            " */\n"
            "public final class SingleContains<T> extends Single<Boolean> {",
        ),
        (
            "    public SingleContains(SingleSource<T> source, Object value, BiPredicate<Object, Object> comparer) {",
            "    /**\n"
            "     * @param source 上游 SingleSource\n"
            "     * @param value 待比较的目标值\n"
            "     * @param comparer 比较函数 (upstream, value) -> boolean\n"
            "     */\n"
            "    public SingleContains(SingleSource<T> source, Object value, BiPredicate<Object, Object> comparer) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super Boolean> observer) {",
            "    /** 订阅 ContainsSingleObserver 比较上游成功值。 */\n"
            "    @Override\n    protected void subscribeActual(final SingleObserver<? super Boolean> observer) {",
        ),
        (
            "    final class ContainsSingleObserver implements SingleObserver<T> {",
            "    /** onSuccess 时 comparer.test(v, value) 并发射 Boolean 结果。 */\n"
            "    final class ContainsSingleObserver implements SingleObserver<T> {",
        ),
        (
            "        @Override\n        public void onSuccess(T v) {",
            "        /** comparer.test 后 downstream.onSuccess(b)；异常转 onError。 */\n"
            "        @Override\n        public void onSuccess(T v) {",
        ),
    ],
    "SingleCreate.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class SingleCreate<T> extends Single<T> {",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 由 SingleOnSubscribe 回调驱动：订阅时创建 Emitter 并调用 source.subscribe。\n"
            " *\n * @param <T> 元素类型\n"
            " */\n"
            "public final class SingleCreate<T> extends Single<T> {",
        ),
        (
            "    public SingleCreate(SingleOnSubscribe<T> source) {",
            "    /** @param source 定义如何向 Emitter 发射结果的回调 */\n"
            "    public SingleCreate(SingleOnSubscribe<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 创建 Emitter 并调用 source.subscribe；异常转 parent.onError。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
        ),
        (
            "    static final class Emitter<T>\n    extends AtomicReference<Disposable>\n    implements SingleEmitter<T>, Disposable {",
            "    /** SingleEmitter 实现：onSuccess/onError 后 dispose 并置 DISPOSED。 */\n"
            "    static final class Emitter<T>\n    extends AtomicReference<Disposable>\n    implements SingleEmitter<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** 非 DISPOSED 时 onSuccess 并 dispose 关联 Disposable。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
        (
            "        @Override\n        public boolean tryOnError(Throwable t) {",
            "        /** 非 DISPOSED 时 onError 并 dispose；已 dispose 返回 false。 */\n"
            "        @Override\n        public boolean tryOnError(Throwable t) {",
        ),
    ],
    "SingleDefer.java": [
        (
            "import java.util.Objects;\n\npublic final class SingleDefer<T> extends Single<T> {",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 每次订阅时调用 singleSupplier 获取新的 SingleSource 并订阅，\n"
            " * 实现延迟/工厂式 Single 创建。\n"
            " *\n * @param <T> 元素类型\n"
            " */\n"
            "public final class SingleDefer<T> extends Single<T> {",
        ),
        (
            "    public SingleDefer(Supplier<? extends SingleSource<? extends T>> singleSupplier) {",
            "    /** @param singleSupplier 每次订阅时提供 SingleSource 的 Supplier */\n"
            "    public SingleDefer(Supplier<? extends SingleSource<? extends T>> singleSupplier) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super T> observer) {",
            "    /** 调用 singleSupplier.get() 后 subscribe 返回的 SingleSource。 */\n"
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
    index_file = Path("/tmp/git-index-rxjava-w21b")
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
        "rxjava 4.0.0-alpha-21: Chinese-annotate wave 21b [15:30]",
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
            "wave21b",
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
        "queue: mark rxjava 4.0.0-alpha-21 wave21b done",
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
