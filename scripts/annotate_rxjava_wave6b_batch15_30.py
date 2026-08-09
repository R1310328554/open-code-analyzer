#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-6b observers [15:30]."""
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
WAVE6B_FILE = Path("/tmp/rxjava_w6b.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE6B_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "CallbackCompletableObserver.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class CallbackCompletableObserver",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 基于 {@link Consumer} 与 {@link Action} 回调的 {@link CompletableObserver} 实现，\n"
            " * 同时作为 {@link Disposable} 管理上游订阅。\n"
            " */\n"
            "public final class CallbackCompletableObserver",
        ),
        (
            "    public CallbackCompletableObserver(Consumer<? super Throwable> onError, Action onComplete) {",
            "    /**\n"
            "     * @param onError 错误回调\n"
            "     * @param onComplete 完成回调\n"
            "     */\n"
            "    public CallbackCompletableObserver(Consumer<? super Throwable> onError, Action onComplete) {",
        ),
        (
            "    @Override\n    public void onComplete() {",
            "    /** 调用 onComplete 回调并将自身标记为已 dispose。 */\n"
            "    @Override\n    public void onComplete() {",
        ),
        (
            "    @Override\n    public void onError(Throwable e) {",
            "    /** 调用 onError 回调并将自身标记为已 dispose。 */\n"
            "    @Override\n    public void onError(Throwable e) {",
        ),
        (
            "    @Override\n    public boolean hasCustomOnError() {",
            "    /** 若 onError 不是默认的 ON_ERROR_MISSING 则返回 true。 */\n"
            "    @Override\n    public boolean hasCustomOnError() {",
        ),
    ],
    "ConsumerSingleObserver.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ConsumerSingleObserver",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 基于 {@link Consumer} 回调的 {@link SingleObserver} 实现，\n"
            " * 同时作为 {@link Disposable} 管理上游订阅。\n"
            " * @param <T> 成功值的类型\n"
            " */\n"
            "public final class ConsumerSingleObserver",
        ),
        (
            "    public ConsumerSingleObserver(Consumer<? super T> onSuccess, Consumer<? super Throwable> onError) {",
            "    /**\n"
            "     * @param onSuccess 成功回调\n"
            "     * @param onError 错误回调\n"
            "     */\n"
            "    public ConsumerSingleObserver(Consumer<? super T> onSuccess, Consumer<? super Throwable> onError) {",
        ),
        (
            "    @Override\n    public void onSuccess(T value) {",
            "    /** 调用 onSuccess 回调并将自身标记为已 dispose。 */\n"
            "    @Override\n    public void onSuccess(T value) {",
        ),
        (
            "    @Override\n    public boolean hasCustomOnError() {",
            "    /** 若 onError 不是默认的 ON_ERROR_MISSING 则返回 true。 */\n"
            "    @Override\n    public boolean hasCustomOnError() {",
        ),
    ],
    "DeferredScalarDisposable.java": [
        (
            "/**\n * Represents a fuseable container for a single value.\n *\n * @param <T> the value type received and emitted\n */",
            "/**\n * 表示可融合（fuseable）的单值容器。\n *\n * @param <T> 接收并发射的值类型\n */",
        ),
        (
            "    /** The target of the events. */",
            "    /** 事件的目标 Observer。 */",
        ),
        (
            "    /** The value stored temporarily when in fusion mode. */",
            "    /** 融合模式下临时存储的值。 */",
        ),
        (
            "    /** Indicates there was a call to complete(T). */",
            "    /** 表示曾调用 complete(T)。 */",
        ),
        (
            "    /** Indicates the Disposable has been disposed. */",
            "    /** 表示 Disposable 已被 dispose。 */",
        ),
        (
            "    /** Indicates this Disposable is in fusion mode and is currently empty. */",
            "    /** 表示本 Disposable 处于融合模式且当前为空。 */",
        ),
        (
            "    /** Indicates this Disposable is in fusion mode and has a value. */",
            "    /** 表示本 Disposable 处于融合模式且已有值。 */",
        ),
        (
            "    /** Indicates this Disposable is in fusion mode and its value has been consumed. */",
            "    /** 表示本 Disposable 处于融合模式且其值已被消费。 */",
        ),
        (
            "    /**\n     * Constructs a DeferredScalarDisposable by wrapping the Observer.\n     * @param downstream the Observer to wrap, not null (not verified)\n     */",
            "    /**\n     * 通过包装 Observer 构造 DeferredScalarDisposable。\n     * @param downstream 要包装的 Observer，非 null（未校验）\n     */",
        ),
        (
            "    /**\n     * Complete the target with a single value or indicate there is a value available in\n     * fusion mode.\n     * @param value the value to signal, not null (not verified)\n     */",
            "    /**\n     * 以单个值完成目标，或在融合模式下指示已有可用值。\n     * @param value 要发出的值，非 null（未校验）\n     */",
        ),
        (
            "    /**\n     * Complete the target with an error signal.\n     * @param t the Throwable to signal, not null (not verified)\n     */",
            "    /**\n     * 以错误信号完成目标。\n     * @param t 要发出的 Throwable，非 null（未校验）\n     */",
        ),
        (
            "    /**\n     * Complete the target without any value.\n     */",
            "    /**\n     * 无值完成目标。\n     */",
        ),
        (
            "    /**\n     * Try disposing this Disposable and return true if the current thread succeeded.\n     * @return true if the current thread succeeded\n     */",
            "    /**\n     * 尝试 dispose 本 Disposable；若当前线程成功则返回 true。\n     * @return 若当前线程成功则为 true\n     */",
        ),
    ],
    "DeferredScalarObserver.java": [
        (
            "/**\n * A fuseable Observer that can generate 0 or 1 resulting value.\n * @param <T> the input value type\n * @param <R> the output value type\n */",
            "/**\n * 可生成 0 或 1 个结果值的可融合 Observer。\n * @param <T> 输入值类型\n * @param <R> 输出值类型\n */",
        ),
        (
            "    /** The upstream disposable. */",
            "    /** 上游 disposable。 */",
        ),
        (
            "    /**\n     * Creates a DeferredScalarObserver instance and wraps a downstream Observer.\n     * @param downstream the downstream subscriber, not null (not verified)\n     */",
            "    /**\n     * 创建 DeferredScalarObserver 实例并包装下游 Observer。\n     * @param downstream 下游 subscriber，非 null（未校验）\n     */",
        ),
    ],
    "DisposableAutoReleaseMultiObserver.java": [
        (
            "/**\n * Wraps lambda callbacks and when the upstream terminates or this (Single | Maybe | Completable)\n * observer gets disposed, removes itself from a {@link io.reactivex.rxjava4.disposables.CompositeDisposable}.\n * <p>History: 0.18.0 @ RxJavaExtensions\n * @param <T> the element type consumed\n * @since 3.1.0\n */",
            "/**\n * 包装 lambda 回调；当上游终止或本（Single | Maybe | Completable）observer 被 dispose 时，\n"
            " * 从 {@link io.reactivex.rxjava4.disposables.CompositeDisposable} 中移除自身。\n"
            " * <p>History: 0.18.0 @ RxJavaExtensions\n"
            " * @param <T> 消费的元素类型\n"
            " * @since 3.1.0\n"
            " */",
        ),
        (
            "    public DisposableAutoReleaseMultiObserver(\n            DisposableContainer composite,\n            Consumer<? super T> onSuccess,\n            Consumer<? super Throwable> onError,\n            Action onComplete\n    ) {",
            "    /**\n"
            "     * @param composite 要从中移除自身的复合容器\n"
            "     * @param onSuccess 成功回调\n"
            "     * @param onError 错误回调\n"
            "     * @param onComplete 完成回调\n"
            "     */\n"
            "    public DisposableAutoReleaseMultiObserver(\n"
            "            DisposableContainer composite,\n"
            "            Consumer<? super T> onSuccess,\n"
            "            Consumer<? super Throwable> onError,\n"
            "            Action onComplete\n"
            "    ) {",
        ),
        (
            "    @Override\n    public void onSuccess(T t) {",
            "    /** 调用 onSuccess 回调并从复合容器中移除自身。 */\n"
            "    @Override\n    public void onSuccess(T t) {",
        ),
    ],
    "DisposableAutoReleaseObserver.java": [
        (
            "/**\n * Wraps lambda callbacks and when the upstream terminates or this observer gets disposed,\n * removes itself from a {@link io.reactivex.rxjava4.disposables.CompositeDisposable}.\n * <p>History: 0.18.0 @ RxJavaExtensions\n * @param <T> the element type consumed\n * @since 3.1.0\n */",
            "/**\n * 包装 lambda 回调；当上游终止或本 observer 被 dispose 时，\n"
            " * 从 {@link io.reactivex.rxjava4.disposables.CompositeDisposable} 中移除自身。\n"
            " * <p>History: 0.18.0 @ RxJavaExtensions\n"
            " * @param <T> 消费的元素类型\n"
            " * @since 3.1.0\n"
            " */",
        ),
        (
            "    public DisposableAutoReleaseObserver(\n            DisposableContainer composite,\n            Consumer<? super T> onNext,\n            Consumer<? super Throwable> onError,\n            Action onComplete\n    ) {",
            "    /**\n"
            "     * @param composite 要从中移除自身的复合容器\n"
            "     * @param onNext 下一项回调\n"
            "     * @param onError 错误回调\n"
            "     * @param onComplete 完成回调\n"
            "     */\n"
            "    public DisposableAutoReleaseObserver(\n"
            "            DisposableContainer composite,\n"
            "            Consumer<? super T> onNext,\n"
            "            Consumer<? super Throwable> onError,\n"
            "            Action onComplete\n"
            "    ) {",
        ),
        (
            "    @Override\n    public void onNext(T t) {",
            "    /** 调用 onNext 回调；若回调抛出异常则 dispose 并转发 onError。 */\n"
            "    @Override\n    public void onNext(T t) {",
        ),
    ],
    "DisposableLambdaObserver.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class DisposableLambdaObserver",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 包装下游 {@link Observer}，并在订阅与 dispose 时调用用户提供的 lambda 回调。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class DisposableLambdaObserver",
        ),
        (
            "        // this way, multiple calls to onSubscribe can show up in tests that use doOnSubscribe to validate behavior",
            "        // 这样在使用 doOnSubscribe 验证行为的测试中，多次 onSubscribe 调用才会显现",
        ),
        (
            "    public DisposableLambdaObserver(Observer<? super T> actual,\n            Consumer<? super Disposable> onSubscribe,\n            Action onDispose) {",
            "    /**\n"
            "     * @param actual 下游 Observer\n"
            "     * @param onSubscribe 订阅时回调\n"
            "     * @param onDispose dispose 时回调\n"
            "     */\n"
            "    public DisposableLambdaObserver(Observer<? super T> actual,\n"
            "            Consumer<? super Disposable> onSubscribe,\n"
            "            Action onDispose) {",
        ),
        (
            "    @Override\n    public void dispose() {",
            "    /** 调用 onDispose 回调并 dispose 上游。 */\n"
            "    @Override\n    public void dispose() {",
        ),
    ],
    "EmptyCompletableObserver.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class EmptyCompletableObserver",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 无自定义回调的 {@link CompletableObserver}；\n"
            " * onError 时通过 {@link OnErrorNotImplementedException} 通知 {@link RxJavaPlugins}。\n"
            " */\n"
            "public final class EmptyCompletableObserver",
        ),
        (
            "    @Override\n    public void onComplete() {\n        // no-op",
            "    /** 无操作完成处理。 */\n"
            "    @Override\n    public void onComplete() {\n        // no-op",
        ),
        (
            "    @Override\n    public boolean hasCustomOnError() {",
            "    /** 始终返回 false，表示未实现自定义 onError。 */\n"
            "    @Override\n    public boolean hasCustomOnError() {",
        ),
    ],
    "ForEachWhileObserver.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class ForEachWhileObserver",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 对每个上游元素调用 {@link Predicate}；当 predicate 返回 false 或序列结束时 dispose 并触发完成。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class ForEachWhileObserver",
        ),
        (
            "    public ForEachWhileObserver(Predicate<? super T> onNext,\n            Consumer<? super Throwable> onError, Action onComplete) {",
            "    /**\n"
            "     * @param onNext 对每个元素执行的 predicate\n"
            "     * @param onError 错误回调\n"
            "     * @param onComplete 完成回调\n"
            "     */\n"
            "    public ForEachWhileObserver(Predicate<? super T> onNext,\n"
            "            Consumer<? super Throwable> onError, Action onComplete) {",
        ),
        (
            "    @Override\n    public void onNext(T t) {",
            "    /** 测试元素；若 predicate 返回 false 则 dispose 并调用 onComplete。 */\n"
            "    @Override\n    public void onNext(T t) {",
        ),
    ],
    "FutureMultiObserver.java": [
        (
            "/**\n * An Observer + Future that expects exactly one upstream value and provides it\n * via the (blocking) Future API.\n *\n * @param <T> the value type\n */",
            "/**\n * 同时实现 Observer 与 Future，期望恰好一个上游值，\n"
            " * 并通过（阻塞）Future API 提供该值。\n *\n * @param <T> 值类型\n"
            " */",
        ),
        (
            "    @Override\n    public void dispose() {\n        // ignoring as `this` means a finished Disposable only",
            "    /** 忽略：终止后 `this` 仅表示已完成的 Disposable。 */\n"
            "    @Override\n    public void dispose() {\n        // ignoring as `this` means a finished Disposable only",
        ),
    ],
    "FutureObserver.java": [
        (
            "/**\n * An Observer + Future that expects exactly one upstream value and provides it\n * via the (blocking) Future API.\n *\n * @param <T> the value type\n */",
            "/**\n * 同时实现 Observer 与 Future，期望恰好一个上游值，\n"
            " * 并通过（阻塞）Future API 提供该值。\n *\n * @param <T> 值类型\n"
            " */",
        ),
        (
            "    @Override\n    public void onNext(T t) {",
            "    /** 接收唯一元素；若收到多个元素则 dispose 上游并报告错误。 */\n"
            "    @Override\n    public void onNext(T t) {",
        ),
        (
            "    @Override\n    public void dispose() {\n        // ignoring as `this` means a finished Disposable only",
            "    /** 忽略：终止后 `this` 仅表示已完成的 Disposable。 */\n"
            "    @Override\n    public void dispose() {\n        // ignoring as `this` means a finished Disposable only",
        ),
    ],
    "InnerQueuedObserver.java": [
        (
            "/**\n * Subscriber that can fuse with the upstream and calls a support interface\n * whenever an event is available.\n *\n * @param <T> the value type\n */",
            "/**\n * 可与上游融合的 Subscriber，在有事件可用时回调支持接口。\n *\n * @param <T> 值类型\n"
            " */",
        ),
        (
            "    public InnerQueuedObserver(InnerQueuedObserverSupport<T> parent, int prefetch) {",
            "    /**\n"
            "     * @param parent 父级支持接口\n"
            "     * @param prefetch 预取数量\n"
            "     */\n"
            "    public InnerQueuedObserver(InnerQueuedObserverSupport<T> parent, int prefetch) {",
        ),
        (
            "    public boolean isDone() {",
            "    /** 若内部序列已完成则返回 true。 */\n"
            "    public boolean isDone() {",
        ),
        (
            "    public void setDone() {",
            "    /** 将内部序列标记为已完成。 */\n"
            "    public void setDone() {",
        ),
        (
            "    public SimpleQueue<T> queue() {",
            "    /** @return 内部队列 */\n"
            "    public SimpleQueue<T> queue() {",
        ),
    ],
    "InnerQueuedObserverSupport.java": [
        (
            "/**\n * Interface to allow the InnerQueuedSubscriber to call back a parent\n * with signals.\n *\n * @param <T> the value type\n */",
            "/**\n * 允许 InnerQueuedSubscriber 向父级回传信号的接口。\n *\n * @param <T> 值类型\n"
            " */",
        ),
        (
            "    void innerNext(InnerQueuedObserver<T> inner, T value);",
            "    /** 内部 observer 收到下一项时回调。 */\n"
            "    void innerNext(InnerQueuedObserver<T> inner, T value);",
        ),
        (
            "    void innerError(InnerQueuedObserver<T> inner, Throwable e);",
            "    /** 内部 observer 收到错误时回调。 */\n"
            "    void innerError(InnerQueuedObserver<T> inner, Throwable e);",
        ),
        (
            "    void innerComplete(InnerQueuedObserver<T> inner);",
            "    /** 内部 observer 完成时回调。 */\n"
            "    void innerComplete(InnerQueuedObserver<T> inner);",
        ),
        (
            "    void drain();",
            "    /** 触发队列排空（drain）逻辑。 */\n"
            "    void drain();",
        ),
    ],
    "LambdaObserver.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class LambdaObserver",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 基于 lambda 回调的 {@link Observer} 实现，同时作为 {@link Disposable} 管理上游订阅。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class LambdaObserver",
        ),
        (
            "    public LambdaObserver(Consumer<? super T> onNext, Consumer<? super Throwable> onError,\n            Action onComplete,\n            Consumer<? super Disposable> onSubscribe) {",
            "    /**\n"
            "     * @param onNext 下一项回调\n"
            "     * @param onError 错误回调\n"
            "     * @param onComplete 完成回调\n"
            "     * @param onSubscribe 订阅回调\n"
            "     */\n"
            "    public LambdaObserver(Consumer<? super T> onNext, Consumer<? super Throwable> onError,\n"
            "            Action onComplete,\n"
            "            Consumer<? super Disposable> onSubscribe) {",
        ),
        (
            "    @Override\n    public void onSubscribe(Disposable d) {",
            "    /** 设置上游 disposable 并调用 onSubscribe 回调。 */\n"
            "    @Override\n    public void onSubscribe(Disposable d) {",
        ),
        (
            "    @Override\n    public boolean hasCustomOnError() {",
            "    /** 若 onError 不是默认的 ON_ERROR_MISSING 则返回 true。 */\n"
            "    @Override\n    public boolean hasCustomOnError() {",
        ),
    ],
    "QueueDrainObserver.java": [
        (
            "/**\n * Abstract base class for subscribers that hold another subscriber, a queue\n * and requires queue-drain behavior.\n *\n * @param <T> the source type to which this subscriber will be subscribed\n * @param <U> the value type in the queue\n * @param <V> the value type the child subscriber accepts\n */",
            "/**\n * 持有另一 subscriber、队列并需要 queue-drain 行为的 subscriber 抽象基类。\n *\n"
            " * @param <T> 本 subscriber 订阅的源类型\n"
            " * @param <U> 队列中的值类型\n"
            " * @param <V> 子 subscriber 接受的值类型\n"
            " */",
        ),
        (
            "    public QueueDrainObserver(Observer<? super V> actual, SimplePlainQueue<U> queue) {",
            "    /**\n"
            "     * @param actual 下游 Observer\n"
            "     * @param queue 内部队列\n"
            "     */\n"
            "    public QueueDrainObserver(Observer<? super V> actual, SimplePlainQueue<U> queue) {",
        ),
        (
            "    /**\n     * Makes sure the fast-path emits in order.\n     * @param value the value to emit or queue up\n     * @param delayError if true, errors are delayed until the source has terminated\n     * @param disposable the resource to dispose if the drain terminates\n     */",
            "    /**\n"
            "     * 确保 fast-path 按序发射。\n"
            "     * @param value 要发射或入队的值\n"
            "     * @param delayError 若为 true，错误延迟到源终止后再发出\n"
            "     * @param disposable drain 终止时要 dispose 的资源\n"
            "     */",
        ),
        (
            "    @Override\n    public void accept(Observer<? super V> a, U v) {\n        // ignored by default",
            "    /** 默认忽略；子类可覆盖以处理队列中的值。 */\n"
            "    @Override\n    public void accept(Observer<? super V> a, U v) {\n        // ignored by default",
        ),
        (
            "/** Pads the header away from other fields. */",
            "/** 在 header 与其它字段之间填充，避免伪共享。 */",
        ),
        (
            "/** The wip counter. */",
            "/** work-in-progress 计数器。 */",
        ),
        (
            "/** Pads away the wip from the other fields. */",
            "/** 在 wip 与其它字段之间填充，避免伪共享。 */",
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
            "wave6b observers [15:30]",
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
