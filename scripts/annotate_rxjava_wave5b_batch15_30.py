#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-5b jdk8 [15:30]."""
from __future__ import annotations

import json
import re
import shutil
import subprocess
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "rxjava/4.0.0-alpha-21"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
BATCH_LIST = Path("/tmp/rxjava_w5b.txt").read_text(encoding="utf-8").strip().split("\n")
MIN_TREE_FILES = 58_000

_COLLECTOR_JAVADOC = (
    "/**\n * Collect items into a container defined by a Stream {@link Collector} callback set.\n *\n * @param <T> the upstream value type\n * @param <A> the intermediate accumulator type\n * @param <R> the result type\n * @since 3.0.0\n */",
    "/**\n * 使用 Stream {@link Collector} 回调集将元素收集到容器中。\n *\n * @param <T> 上游值类型\n * @param <A> 中间累加器类型\n * @param <R> 结果类型\n * @since 3.0.0\n */",
)

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "MaybeMapOptional.java": [
        (
            "/**\n * Maps the success value to an {@link Optional} and emits its non-empty value or completes.\n *\n * @param <T> the upstream success value type\n * @param <R> the result value type\n * @since 3.0.0\n */",
            "/**\n * 将成功值映射为 {@link Optional}，若有值则发射，否则完成。\n *\n * @param <T> 上游成功值类型\n * @param <R> 结果值类型\n * @since 3.0.0\n */",
        ),
        (
            "    public MaybeMapOptional(Maybe<T> source, Function<? super T, Optional<? extends R>> mapper) {",
            "    /** @param source 上游 Maybe；@param mapper 将成功值映射为 Optional 的函数 */\n    public MaybeMapOptional(Maybe<T> source, Function<? super T, Optional<? extends R>> mapper) {",
        ),
        (
            "    static final class MapOptionalMaybeObserver<T, R> implements MaybeObserver<T>, Disposable {",
            "    /** 将上游成功值经 Optional 映射后转发给下游的 Maybe 观察者。 */\n    static final class MapOptionalMaybeObserver<T, R> implements MaybeObserver<T>, Disposable {",
        ),
    ],
    "ObservableCollectWithCollector.java": [
        _COLLECTOR_JAVADOC,
        (
            "    public ObservableCollectWithCollector(Observable<T> source, Collector<? super T, A, R> collector) {",
            "    /** @param source 上游 Observable；@param collector Stream 收集器 */\n    public ObservableCollectWithCollector(Observable<T> source, Collector<? super T, A, R> collector) {",
        ),
        (
            "    static final class CollectorObserver<T, A, R>",
            "    /** 使用 Collector 累加上游元素并在完成时发射最终结果。 */\n    static final class CollectorObserver<T, A, R>",
        ),
    ],
    "ObservableCollectWithCollectorSingle.java": [
        _COLLECTOR_JAVADOC,
        (
            "    public ObservableCollectWithCollectorSingle(Observable<T> source, Collector<? super T, A, R> collector) {",
            "    /** @param source 上游 Observable；@param collector Stream 收集器 */\n    public ObservableCollectWithCollectorSingle(Observable<T> source, Collector<? super T, A, R> collector) {",
        ),
        (
            "    @Override\n    public Observable<R> fuseToObservable() {",
            "    /** 转换为等价的 ObservableCollectWithCollector。 */\n    @Override\n    public Observable<R> fuseToObservable() {",
        ),
        (
            "    static final class CollectorSingleObserver<T, A, R> implements Observer<T>, Disposable {",
            "    /** 收集上游元素并在完成时以 onSuccess 发射最终结果。 */\n    static final class CollectorSingleObserver<T, A, R> implements Observer<T>, Disposable {",
        ),
    ],
    "ObservableFirstStageObserver.java": [
        (
            "/**\n * Signals the first element of the source via the underlying CompletableFuture,\n * signals a default item if the upstream is empty or signals {@link NoSuchElementException}.\n *\n * @param <T> the element type\n * @since 3.0.0\n */",
            "/**\n * 通过底层 CompletableFuture 传递源序列的首个元素；\n * 若上游为空则传递默认项，否则传递 {@link NoSuchElementException}。\n *\n * @param <T> 元素类型\n * @since 3.0.0\n */",
        ),
        (
            "    public ObservableFirstStageObserver(boolean hasDefault, T defaultItem) {",
            "    /** @param hasDefault 上游为空时是否使用默认值；@param defaultItem 默认元素 */\n    public ObservableFirstStageObserver(boolean hasDefault, T defaultItem) {",
        ),
        (
            "    @Override\n    public void onNext(T t) {",
            "    /** 收到首个元素即完成 CompletableFuture。 */\n    @Override\n    public void onNext(T t) {",
        ),
        (
            "    @Override\n    public void onComplete() {",
            "    /** 上游完成但未收到元素时，传递默认值或 NoSuchElementException。 */\n    @Override\n    public void onComplete() {",
        ),
    ],
    "ObservableFlatMapStream.java": [
        (
            "/**\n * Maps the upstream values onto {@link Stream}s and emits their items in order to the downstream.\n *\n * @param <T> the upstream element type\n * @param <R> the inner {@code Stream} and result element type\n * @since 3.0.0\n */",
            "/**\n * 将上游值映射为 {@link Stream}，并按顺序向下游发射其元素。\n *\n * @param <T> 上游元素类型\n * @param <R> 内部 {@code Stream} 及结果元素类型\n * @since 3.0.0\n */",
        ),
        (
            "    public ObservableFlatMapStream(Observable<T> source, Function<? super T, ? extends Stream<? extends R>> mapper) {",
            "    /** @param source 上游 Observable；@param mapper 将元素映射为 Stream 的函数 */\n    public ObservableFlatMapStream(Observable<T> source, Function<? super T, ? extends Stream<? extends R>> mapper) {",
        ),
        (
            "    static final class FlatMapStreamObserver<T, R> extends AtomicInteger",
            "    /** 对每个上游元素展开 Stream 并顺序发射其项。 */\n    static final class FlatMapStreamObserver<T, R> extends AtomicInteger",
        ),
    ],
    "ObservableFromCompletionStage.java": [
        (
            "/**\n * Wrap a CompletionStage and signal its outcome.\n * @param <T> the element type\n * @since 3.0.0\n */",
            "/**\n * 包装 CompletionStage 并传递其结果。\n * @param <T> 元素类型\n * @since 3.0.0\n */",
        ),
        (
            "        // We need an indirection because one can't detach from a whenComplete\n        // and cancellation should not hold onto the stage.",
            "        // 需要间接层：无法从 whenComplete 分离，且取消时不应继续持有 stage。",
        ),
        (
            "    static final class CompletionStageHandler<T>",
            "    /** 在 CompletionStage 完成时向下游传递结果或错误。 */\n    static final class CompletionStageHandler<T>",
        ),
        (
            "    static final class BiConsumerAtomicReference<T> extends AtomicReference<BiConsumer<T, Throwable>>",
            "    /** 可原子清空的 BiConsumer 引用，用于取消时解除对 stage 的持有。 */\n    static final class BiConsumerAtomicReference<T> extends AtomicReference<BiConsumer<T, Throwable>>",
        ),
    ],
    "ObservableFromStream.java": [
        (
            "/**\n * Wraps a {@link Stream} and emits its values as an {@link Observable} sequence.\n * @param <T> the element type of the Stream\n * @since 3.0.0\n */",
            "/**\n * 包装 {@link Stream}，将其值作为 {@link Observable} 序列发射。\n * @param <T> Stream 的元素类型\n * @since 3.0.0\n */",
        ),
        (
            "    public ObservableFromStream(Stream<T> stream) {",
            "    /** @param stream 要包装的 Stream */\n    public ObservableFromStream(Stream<T> stream) {",
        ),
        (
            "    /**\n     * Subscribes to the Stream.\n     * @param <T> the element type of the flow\n     * @param observer the observer to drive\n     * @param stream the sequence to consume\n     */",
            "    /**\n     * 订阅 Stream。\n     * @param <T> 流的元素类型\n     * @param observer 要驱动的观察者\n     * @param stream 要消费的序列\n     */",
        ),
        (
            "    static void closeSafely(AutoCloseable c) {",
            "    /** 安全关闭 AutoCloseable，异常经 RxJavaPlugins 上报。 */\n    static void closeSafely(AutoCloseable c) {",
        ),
        (
            "    static final class StreamDisposable<T> implements QueueDisposable<T> {",
            "    /** 从 Stream 迭代器拉取元素并向下游发射的 Disposable。 */\n    static final class StreamDisposable<T> implements QueueDisposable<T> {",
        ),
        (
            "        public void run() {",
            "        /** 非融合模式下逐元素迭代 Stream 并通知下游。 */\n        public void run() {",
        ),
    ],
    "ObservableLastStageObserver.java": [
        (
            "/**\n * Signals the last element of the source via the underlying CompletableFuture,\n * signals a default item if the upstream is empty or signals {@link NoSuchElementException}.\n *\n * @param <T> the element type\n * @since 3.0.0\n */",
            "/**\n * 通过底层 CompletableFuture 传递源序列的最后一个元素；\n * 若上游为空则传递默认项，否则传递 {@link NoSuchElementException}。\n *\n * @param <T> 元素类型\n * @since 3.0.0\n */",
        ),
        (
            "    public ObservableLastStageObserver(boolean hasDefault, T defaultItem) {",
            "    /** @param hasDefault 上游为空时是否使用默认值；@param defaultItem 默认元素 */\n    public ObservableLastStageObserver(boolean hasDefault, T defaultItem) {",
        ),
        (
            "    @Override\n    public void onNext(T t) {",
            "    /** 缓存最新元素，等待上游完成后再完成 CompletableFuture。 */\n    @Override\n    public void onNext(T t) {",
        ),
        (
            "    @Override\n    public void onComplete() {",
            "    /** 上游完成时发射最后缓存的元素、默认值或 NoSuchElementException。 */\n    @Override\n    public void onComplete() {",
        ),
    ],
    "ObservableMapOptional.java": [
        (
            "/**\n * Map the upstream values into an Optional and emit its value if any.\n * @param <T> the upstream element type\n * @param <R> the output element type\n * @since 3.0.0\n */",
            "/**\n * 将上游值映射为 Optional，若有值则发射。\n * @param <T> 上游元素类型\n * @param <R> 输出元素类型\n * @since 3.0.0\n */",
        ),
        (
            "    public ObservableMapOptional(Observable<T> source, Function<? super T, Optional<? extends R>> mapper) {",
            "    /** @param source 上游 Observable；@param mapper 将元素映射为 Optional 的函数 */\n    public ObservableMapOptional(Observable<T> source, Function<? super T, Optional<? extends R>> mapper) {",
        ),
        (
            "    static final class MapOptionalObserver<T, R> extends BasicFuseableObserver<T, R> {",
            "    /** 过滤并映射 Optional 非空值的融合观察者。 */\n    static final class MapOptionalObserver<T, R> extends BasicFuseableObserver<T, R> {",
        ),
    ],
    "ObservableSingleStageObserver.java": [
        (
            "/**\n * Signals the only element of the source via the underlying CompletableFuture,\n * signals a default item if the upstream is empty or signals {@link IllegalArgumentException}\n * if the upstream has more than one item.\n *\n * @param <T> the element type\n * @since 3.0.0\n */",
            "/**\n * 通过底层 CompletableFuture 传递源序列的唯一元素；\n * 若上游为空则传递默认项；若上游有多于一个元素则传递 {@link IllegalArgumentException}。\n *\n * @param <T> 元素类型\n * @since 3.0.0\n */",
        ),
        (
            "    public ObservableSingleStageObserver(boolean hasDefault, T defaultItem) {",
            "    /** @param hasDefault 上游为空时是否使用默认值；@param defaultItem 默认元素 */\n    public ObservableSingleStageObserver(boolean hasDefault, T defaultItem) {",
        ),
        (
            "    @Override\n    public void onNext(T t) {",
            "    /** 仅允许一个元素；出现第二个元素时以 IllegalArgumentException 完成。 */\n    @Override\n    public void onNext(T t) {",
        ),
        (
            "    @Override\n    public void onComplete() {",
            "    /** 上游完成时发射唯一元素、默认值或 NoSuchElementException。 */\n    @Override\n    public void onComplete() {",
        ),
    ],
    "ObservableStageObserver.java": [
        (
            "/**\n * Base class that extends CompletableFuture and provides basic infrastructure\n * to notify watchers upon upstream signals.\n * @param <T> the element type\n * @since 3.0.0\n */",
            "/**\n * 继承 CompletableFuture 的基类，提供在上游信号到达时通知等待者的基本基础设施。\n * @param <T> 元素类型\n * @since 3.0.0\n */",
        ),
        (
            "    protected final void disposeUpstream() {",
            "    /** dispose 上游订阅。 */\n    protected final void disposeUpstream() {",
        ),
        (
            "    protected final void clear() {",
            "    /** 清空缓存值并标记上游已 dispose。 */\n    protected final void clear() {",
        ),
        (
            "    @Override\n    public final boolean cancel(boolean mayInterruptIfRunning) {",
            "    /** 取消时同时 dispose 上游订阅。 */\n    @Override\n    public final boolean cancel(boolean mayInterruptIfRunning) {",
        ),
        (
            "    @Override\n    public final boolean complete(T value) {",
            "    /** 正常完成时 dispose 上游订阅。 */\n    @Override\n    public final boolean complete(T value) {",
        ),
        (
            "    @Override\n    public final boolean completeExceptionally(Throwable ex) {",
            "    /** 异常完成时 dispose 上游订阅。 */\n    @Override\n    public final boolean completeExceptionally(Throwable ex) {",
        ),
    ],
    "ParallelCollector.java": [
        (
            "/**\n * Reduces all 'rails' into a single via a Java 8 {@link Collector} callback set.\n *\n * @param <T> the value type\n * @param <A> the accumulator type\n * @param <R> the result type\n * @since 3.0.0\n */",
            "/**\n * 通过 Java 8 {@link Collector} 回调集将所有并行轨道归约为单一结果。\n *\n * @param <T> 值类型\n * @param <A> 累加器类型\n * @param <R> 结果类型\n * @since 3.0.0\n */",
        ),
        (
            "    public ParallelCollector(ParallelFlowable<? extends T> source, Collector<T, A, R> collector) {",
            "    /** @param source 并行上游；@param collector Stream 收集器 */\n    public ParallelCollector(ParallelFlowable<? extends T> source, Collector<T, A, R> collector) {",
        ),
        (
            "    static final class ParallelCollectorSubscriber<T, A, R> extends DeferredScalarSubscription<R> {",
            "    /** 协调各并行轨道累加器并在全部完成后合并结果。 */\n    static final class ParallelCollectorSubscriber<T, A, R> extends DeferredScalarSubscription<R> {",
        ),
        (
            "    static final class ParallelCollectorInnerSubscriber<T, A, R>",
            "    /** 单条并行轨道的收集订阅者。 */\n    static final class ParallelCollectorInnerSubscriber<T, A, R>",
        ),
        (
            "    static final class SlotPair<T> extends AtomicInteger {",
            "    /** 用于两两合并累加器的双槽位配对结构。 */\n    static final class SlotPair<T> extends AtomicInteger {",
        ),
    ],
    "ParallelFlatMapStream.java": [
        (
            "/**\n * Flattens the generated {@link Stream}s on each rail.\n *\n * @param <T> the input value type\n * @param <R> the output value type\n * @since 3.0.0\n */",
            "/**\n * 扁平化每条并行轨道上生成的 {@link Stream}。\n *\n * @param <T> 输入值类型\n * @param <R> 输出值类型\n * @since 3.0.0\n */",
        ),
        (
            "    public ParallelFlatMapStream(",
            "    /** @param source 并行上游；@param mapper 将元素映射为 Stream 的函数；@param prefetch 预取数量 */\n    public ParallelFlatMapStream(",
        ),
    ],
    "ParallelMapOptional.java": [
        (
            "/**\n * Maps each 'rail' of the source ParallelFlowable with a mapper function.\n *\n * @param <T> the input value type\n * @param <R> the output value type\n * @since 3.0.0\n */",
            "/**\n * 用映射函数转换源 ParallelFlowable 的每条并行轨道。\n *\n * @param <T> 输入值类型\n * @param <R> 输出值类型\n * @since 3.0.0\n */",
        ),
        (
            "    public ParallelMapOptional(ParallelFlowable<T> source, Function<? super T, Optional<? extends R>> mapper) {",
            "    /** @param source 并行上游；@param mapper 将元素映射为 Optional 的函数 */\n    public ParallelMapOptional(ParallelFlowable<T> source, Function<? super T, Optional<? extends R>> mapper) {",
        ),
        (
            "    static final class ParallelMapSubscriber<T, R> implements ConditionalSubscriber<T>, Subscription {",
            "    /** 在普通 Subscriber 上过滤并映射 Optional 非空值。 */\n    static final class ParallelMapSubscriber<T, R> implements ConditionalSubscriber<T>, Subscription {",
        ),
        (
            "    static final class ParallelMapConditionalSubscriber<T, R> implements ConditionalSubscriber<T>, Subscription {",
            "    /** 在 ConditionalSubscriber 上过滤并映射 Optional 非空值。 */\n    static final class ParallelMapConditionalSubscriber<T, R> implements ConditionalSubscriber<T>, Subscription {",
        ),
    ],
    "ParallelMapTryOptional.java": [
        (
            "/**\n * Maps each 'rail' of the source ParallelFlowable with a mapper function\n * and handle any failure based on a handler function.\n * @param <T> the input value type\n * @param <R> the output value type\n * @since 3.0.0\n */",
            "/**\n * 用映射函数转换源 ParallelFlowable 的每条并行轨道，\n * 并根据错误处理函数应对映射失败。\n * @param <T> 输入值类型\n * @param <R> 输出值类型\n * @since 3.0.0\n */",
        ),
        (
            "    public ParallelMapTryOptional(",
            "    /** @param source 并行上游；@param mapper 映射函数；@param errorHandler 映射失败时的处理策略 */\n    public ParallelMapTryOptional(",
        ),
        (
            "    static final class ParallelMapTrySubscriber<T, R> implements ConditionalSubscriber<T>, Subscription {",
            "    /** 带错误处理的可选映射，用于普通 Subscriber。 */\n    static final class ParallelMapTrySubscriber<T, R> implements ConditionalSubscriber<T>, Subscription {",
        ),
        (
            "    static final class ParallelMapTryConditionalSubscriber<T, R> implements ConditionalSubscriber<T>, Subscription {",
            "    /** 带错误处理的可选映射，用于 ConditionalSubscriber。 */\n    static final class ParallelMapTryConditionalSubscriber<T, R> implements ConditionalSubscriber<T>, Subscription {",
        ),
    ],
}


def tree_guard() -> int:
    count = int(subprocess.check_output(["find", str(ROOT), "-type", "f"], text=True).count("\n"))
    if count < MIN_TREE_FILES:
        raise SystemExit(f"Tree guard failed: {count} files < {MIN_TREE_FILES}")
    return count


def apply_replacements(text: str, replacements: list[tuple[str, str]]) -> str:
    for old, new in replacements:
        if old not in text:
            raise ValueError(f"Pattern not found:\n{old[:120]}...")
        text = text.replace(old, new, 1)
    return text


def main() -> int:
    tree_count = tree_guard()
    failures: list[str] = []
    ok = 0
    for rel in BATCH_LIST:
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
            text = apply_replacements(dst.read_text(encoding="utf-8"), reps)
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
    print(json.dumps({"ok": ok, "failures": failures, "tree_count": tree_count}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
