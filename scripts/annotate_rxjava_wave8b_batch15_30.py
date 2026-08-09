#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-8b completable/flowable operators [15:30]."""
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
WAVE8B_FILE = Path("/tmp/rxjava_w8b.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE8B_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "CompletableTimer.java": [
        (
            "/**\n * Signals an {@code onComplete} event after the specified delay.\n */",
            "/**\n * 在指定延迟后发出 {@code onComplete} 事件。\n"
            " */",
        ),
        (
            "    public CompletableTimer(long delay, TimeUnit unit, Scheduler scheduler) {",
            "    /**\n"
            "     * @param delay 延迟量\n"
            "     * @param unit 时间单位\n"
            "     * @param scheduler 调度 {@link Runnable} 的 Scheduler\n"
            "     */\n"
            "    public CompletableTimer(long delay, TimeUnit unit, Scheduler scheduler) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(final CompletableObserver observer) {",
            "    /** 在 scheduler 上调度 TimerDisposable，到期后 onComplete。 */\n"
            "    @Override\n    protected void subscribeActual(final CompletableObserver observer) {",
        ),
        (
            "        @Override\n        public void run() {",
            "        /** 定时到期，向下游发出 onComplete。 */\n"
            "        @Override\n        public void run() {",
        ),
        (
            "        void setFuture(Disposable d) {",
            "        /** 替换当前 Disposable 为 scheduler 返回的任务。 */\n"
            "        void setFuture(Disposable d) {",
        ),
    ],
    "CompletableToFlowable.java": [
        (
            "import io.reactivex.rxjava4.internal.operators.flowable.FlowableFromCompletable;\n\npublic final class CompletableToFlowable",
            "import io.reactivex.rxjava4.internal.operators.flowable.FlowableFromCompletable;\n\n"
            "/**\n"
            " * 将 {@link CompletableSource} 包装为 {@link Flowable}；完成时 onComplete，出错时 onError，不发射元素。\n"
            " * @param <T> Flowable 元素类型\n"
            " */\n"
            "public final class CompletableToFlowable",
        ),
        (
            "    public CompletableToFlowable(CompletableSource source) {",
            "    /** @param source 上游 CompletableSource */\n"
            "    public CompletableToFlowable(CompletableSource source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
            "    /** 通过 {@link FlowableFromCompletable.FromCompletableObserver} 订阅上游。 */\n"
            "    @Override\n    protected void subscribeActual(Subscriber<? super T> s) {",
        ),
    ],
    "CompletableToObservable.java": [
        (
            "/**\n * Wraps a Completable and exposes it as an Observable.\n *\n * @param <T> the value type\n */",
            "/**\n * 包装 Completable 并以 {@link Observable} 形式暴露。\n"
            " *\n * @param <T> 值类型\n"
            " */",
        ),
        (
            "    public CompletableToObservable(CompletableSource source) {",
            "    /** @param source 上游 CompletableSource */\n"
            "    public CompletableToObservable(CompletableSource source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
            "    /** 通过 {@link ObservableFromCompletable.FromCompletableObserver} 订阅上游。 */\n"
            "    @Override\n    protected void subscribeActual(Observer<? super T> observer) {",
        ),
    ],
    "CompletableToSingle.java": [
        (
            "import io.reactivex.rxjava4.functions.Supplier;\n\npublic final class CompletableToSingle",
            "import io.reactivex.rxjava4.functions.Supplier;\n\n"
            "/**\n"
            " * 将 {@link CompletableSource} 转为 {@link Single}；完成时发出 completionValue 或 Supplier 提供的值。\n"
            " * @param <T> Single 值类型\n"
            " */\n"
            "public final class CompletableToSingle",
        ),
        (
            "    public CompletableToSingle(CompletableSource source,\n            Supplier<? extends T> completionValueSupplier, T completionValue) {",
            "    /**\n"
            "     * @param source 上游 CompletableSource\n"
            "     * @param completionValueSupplier 完成时提供值的 Supplier（可为 null）\n"
            "     * @param completionValue Supplier 为 null 时使用的完成值\n"
            "     */\n"
            "    public CompletableToSingle(CompletableSource source,\n            Supplier<? extends T> completionValueSupplier, T completionValue) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 从 Supplier 或 completionValue 取值，非 null 则 onSuccess，否则 onError。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** 转发 onError。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
        (
            "        @Override\n        public void onSubscribe(Disposable d) {",
            "        /** 转发 onSubscribe。 */\n"
            "        @Override\n        public void onSubscribe(Disposable d) {",
        ),
    ],
    "CompletableUsing.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class CompletableUsing",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 获取资源、订阅由 completableFunction 返回的 Completable，并在终止或 dispose 时释放资源。\n"
            " * @param <R> 资源类型\n"
            " */\n"
            "public final class CompletableUsing",
        ),
        (
            "    public CompletableUsing(Supplier<R> resourceSupplier,\n                            Function<? super R, ? extends CompletableSource> completableFunction, Consumer<? super R> disposer,\n                            boolean eager) {",
            "    /**\n"
            "     * @param resourceSupplier 提供资源的 Supplier\n"
            "     * @param completableFunction 由资源生成 CompletableSource 的函数\n"
            "     * @param disposer 释放资源的 Consumer\n"
            "     * @param eager true 时在终止/dispose 前先释放资源\n"
            "     */\n"
            "    public CompletableUsing(Supplier<R> resourceSupplier,\n                            Function<? super R, ? extends CompletableSource> completableFunction, Consumer<? super R> disposer,\n                            boolean eager) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 获取资源、应用 completableFunction 并订阅 UsingObserver。 */\n"
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
        (
            "        @Override\n        public void dispose() {",
            "        /** 按 eager 顺序 dispose 上游并释放资源。 */\n"
            "        @Override\n        public void dispose() {",
        ),
        (
            "        void disposeResource() {",
            "        /** 调用 disposer 释放资源（至多一次）。 */\n"
            "        void disposeResource() {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** eager 时先释放资源，再转发 onError。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** eager 时先释放资源，再转发 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "AbstractBackpressureThrottlingSubscriber.java": [
        (
            "/**\n * Abstract base class for operators that throttle excessive updates from upstream in case if\n * downstream {@link Subscriber} is not ready to receive updates.\n *\n * @param <T> the upstream value type\n * @param <R> the downstream value type\n */",
            "/**\n * 当下游 {@link Subscriber} 未就绪时，对上游过量更新进行节流的抽象基类。\n"
            " *\n * @param <T> 上游值类型\n"
            " * @param <R> 下游值类型\n"
            " */",
        ),
        (
            "    AbstractBackpressureThrottlingSubscriber(Subscriber<? super R> downstream) {",
            "    /** @param downstream 下游 Subscriber */\n"
            "    AbstractBackpressureThrottlingSubscriber(Subscriber<? super R> downstream) {",
        ),
        (
            "    @Override\n    public void onSubscribe(Subscription s) {",
            "    /** 验证上游订阅，向下游传递自身并请求 Long.MAX_VALUE。 */\n"
            "    @Override\n    public void onSubscribe(Subscription s) {",
        ),
        (
            "    void drain() {",
            "    /** 按背压请求从 current 取出最新值并向下游发射。 */\n"
            "    void drain() {",
        ),
        (
            "    boolean checkTerminated(boolean d, boolean empty, Subscriber<?> a, AtomicReference<R> q) {",
            "    /** 检查取消/完成/错误状态并向下游发出相应信号。 */\n"
            "    boolean checkTerminated(boolean d, boolean empty, Subscriber<?> a, AtomicReference<R> q) {",
        ),
    ],
    "AbstractFlowableWithUpstream.java": [
        (
            "/**\n * Abstract base class for operators that take an upstream\n * source {@link Publisher}.\n *\n * @param <T> the upstream value type\n * @param <R> the output value type\n */",
            "/**\n * 接受上游 {@link Publisher} 作为源的 Flowable 算子抽象基类。\n"
            " *\n * @param <T> 上游值类型\n"
            " * @param <R> 输出值类型\n"
            " */",
        ),
        (
            "    /**\n     * The upstream source Publisher.\n     */",
            "    /** 上游 Publisher 源。 */",
        ),
        (
            "    /**\n     * Constructs a FlowableSource wrapping the given non-null (verified)\n     * source Publisher.\n     * @param source the source (upstream) Publisher instance, not null (verified)\n     */",
            "    /**\n"
            "     * 包装已校验非 null 的上游 Publisher。\n"
            "     * @param source 上游 Publisher 实例，非 null（已校验）\n"
            "     */",
        ),
        (
            "    @Override\n    public final Publisher<T> source() {",
            "    /** 返回上游 Publisher。 */\n"
            "    @Override\n    public final Publisher<T> source() {",
        ),
    ],
    "BlockingFlowableIterable.java": [
        (
            "import io.reactivex.rxjava4.operators.SpscArrayQueue;\n\npublic final class BlockingFlowableIterable",
            "import io.reactivex.rxjava4.operators.SpscArrayQueue;\n\n"
            "/**\n"
            " * 阻塞式 {@link Iterable}，按顺序迭代 {@link Flowable} 的所有元素。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class BlockingFlowableIterable",
        ),
        (
            "    public BlockingFlowableIterable(Flowable<T> source, int bufferSize) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param bufferSize 内部队列缓冲大小\n"
            "     */\n"
            "    public BlockingFlowableIterable(Flowable<T> source, int bufferSize) {",
        ),
        (
            "    @Override\n    public Iterator<T> iterator() {",
            "    /** 订阅 source 并返回阻塞迭代器。 */\n"
            "    @Override\n    public Iterator<T> iterator() {",
        ),
        (
            "        @Override\n        public boolean hasNext() {",
            "        /** 阻塞等待直至有元素、完成或出错。 */\n"
            "        @Override\n        public boolean hasNext() {",
        ),
        (
            "        @Override\n        public T next() {",
            "        /** 取出队首元素并按 batch 向上游请求。 */\n"
            "        @Override\n        public T next() {",
        ),
        (
            "        void signalConsumer() {",
            "        /** 唤醒在 hasNext 中等待的线程。 */\n"
            "        void signalConsumer() {",
        ),
    ],
    "BlockingFlowableLatest.java": [
        (
            "/**\n * Wait for and iterate over the latest values of the source observable. If the source works faster than the\n * iterator, values may be skipped, but not the {@code onError} or {@code onComplete} events.\n * @param <T> the value type emitted\n */",
            "/**\n"
            " * 阻塞等待并迭代上游最新值；上游快于迭代器时可能跳过中间值，但不会跳过 {@code onError} 或 {@code onComplete}。\n"
            " * @param <T> 发射的值类型\n"
            " */",
        ),
        (
            "    public BlockingFlowableLatest(Publisher<? extends T> source) {",
            "    /** @param source 上游 Publisher */\n"
            "    public BlockingFlowableLatest(Publisher<? extends T> source) {",
        ),
        (
            "    /** Subscriber of source, iterator for output. */",
            "    /** 上游 Subscriber，对外作为 Iterator 输出。 */",
        ),
        (
            "        @Override\n        public boolean hasNext() {",
            "        /** 阻塞等待下一个 materialized 通知。 */\n"
            "        @Override\n        public boolean hasNext() {",
        ),
        (
            "        @Override\n        public T next() {",
            "        /** 返回当前 onNext 通知中的值。 */\n"
            "        @Override\n        public T next() {",
        ),
    ],
    "BlockingFlowableMostRecent.java": [
        (
            "/**\n * Returns an Iterable that always returns the item most recently emitted by an Observable, or a\n * seed value if no item has yet been emitted.\n * <p>\n * <img width=\"640\" height=\"490\" src=\"https://github.com/ReactiveX/RxJava/wiki/images/rx-operators/B.mostRecent.v3.png\" alt=\"\">\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 返回始终给出 Observable 最近发射项的 Iterable；若尚未发射则返回种子值。\n"
            " * <p>\n"
            " * <img width=\"640\" height=\"490\" src=\"https://github.com/ReactiveX/RxJava/wiki/images/rx-operators/B.mostRecent.v3.png\" alt=\"\">\n"
            " *\n"
            " * @param <T> 值类型\n"
            " */",
        ),
        (
            "    public BlockingFlowableMostRecent(Flowable<T> source, T initialValue) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param initialValue 尚无发射时的初始值\n"
            "     */\n"
            "    public BlockingFlowableMostRecent(Flowable<T> source, T initialValue) {",
        ),
        (
            "        /**\n         * The {@link Iterator} return is not thread safe. In other words don't call {@link Iterator#hasNext()} in one\n         * thread expect {@link Iterator#next()} called from a different thread to work.\n         * @return the Iterator\n         */",
            "        /**\n"
            "         * 返回的 {@link Iterator} 非线程安全；勿在一线程调用 {@link Iterator#hasNext()} 而在另一线程调用 {@link Iterator#next()}。\n"
            "         * @return Iterator\n"
            "         */",
        ),
        (
            "            /**\n             * buffer to make sure that the state of the iterator doesn't change between calling hasNext() and next().\n             */",
            "            /** 缓冲，保证 hasNext() 与 next() 之间迭代器状态不变。 */",
        ),
    ],
    "BlockingFlowableNext.java": [
        (
            "/**\n * Returns an Iterable that blocks until the Observable emits another item, then returns that item.\n * <p>\n * <img width=\"640\" height=\"490\" src=\"https://github.com/ReactiveX/RxJava/wiki/images/rx-operators/B.next.v3.png\" alt=\"\">\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 返回阻塞直至 Observable 再发射一项，然后返回该项的 Iterable。\n"
            " * <p>\n"
            " * <img width=\"640\" height=\"490\" src=\"https://github.com/ReactiveX/RxJava/wiki/images/rx-operators/B.next.v3.png\" alt=\"\">\n"
            " *\n"
            " * @param <T> 值类型\n"
            " */",
        ),
        (
            "    public BlockingFlowableNext(Publisher<? extends T> source) {",
            "    /** @param source 上游 Publisher */\n"
            "    public BlockingFlowableNext(Publisher<? extends T> source) {",
        ),
        (
            "        private boolean moveToNext() {",
            "        /** 阻塞等待下一个 materialized 通知并更新 next。 */\n"
            "        private boolean moveToNext() {",
        ),
        (
            "        public Notification<T> takeNext() throws InterruptedException {",
            "        /** 设置 waiting 并从 buf 阻塞取下一项。 */\n"
            "        public Notification<T> takeNext() throws InterruptedException {",
        ),
    ],
    "FlowableAll.java": [
        (
            "import java.io.Serial;\n\npublic final class FlowableAll",
            "import java.io.Serial;\n\n"
            "/**\n"
            " * 判断上游所有元素是否均满足 {@link Predicate}；全部满足 emit true，任一不满足 emit false。\n"
            " * @param <T> 上游元素类型\n"
            " */\n"
            "public final class FlowableAll",
        ),
        (
            "    public FlowableAll(Flowable<T> source, Predicate<? super T> predicate) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param predicate 测试谓词\n"
            "     */\n"
            "    public FlowableAll(Flowable<T> source, Predicate<? super T> predicate) {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** predicate 为 false 时取消上游并 emit false。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 全部通过则 emit true。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "FlowableAllSingle.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableAllSingle",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 以 {@link Single} 形式判断上游所有元素是否均满足 {@link Predicate}。\n"
            " * @param <T> 上游元素类型\n"
            " */\n"
            "public final class FlowableAllSingle",
        ),
        (
            "    public FlowableAllSingle(Flowable<T> source, Predicate<? super T> predicate) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param predicate 测试谓词\n"
            "     */\n"
            "    public FlowableAllSingle(Flowable<T> source, Predicate<? super T> predicate) {",
        ),
        (
            "    @Override\n    public Flowable<Boolean> fuseToFlowable() {",
            "    /** 融合为 {@link FlowableAll}。 */\n"
            "    @Override\n    public Flowable<Boolean> fuseToFlowable() {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** predicate 为 false 时取消上游并 onSuccess(false)。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 全部通过则 onSuccess(true)。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "FlowableAmb.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class FlowableAmb",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 竞争订阅多个 {@link Publisher}，仅转发首个发出信号的源。\n"
            " * @param <T> 元素类型\n"
            " */\n"
            "public final class FlowableAmb",
        ),
        (
            "    public FlowableAmb(Publisher<? extends T>[] sources, Iterable<? extends Publisher<? extends T>> sourcesIterable) {",
            "    /**\n"
            "     * @param sources Publisher 数组（可为 null，则用 sourcesIterable）\n"
            "     * @param sourcesIterable 可迭代的 Publisher 源\n"
            "     */\n"
            "    public FlowableAmb(Publisher<? extends T>[] sources, Iterable<? extends Publisher<? extends T>> sourcesIterable) {",
        ),
        (
            "        public void subscribe(Publisher<? extends T>[] sources) {",
            "        /** 为每个源创建 AmbInnerSubscriber 并竞争订阅。 */\n"
            "        public void subscribe(Publisher<? extends T>[] sources) {",
        ),
        (
            "        public boolean win(int index) {",
            "        /** 首个获胜者取消其余 inner subscriber。 */\n"
            "        public boolean win(int index) {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** 获胜后转发 onNext，否则尝试 win 或 cancel。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
    ],
    "FlowableAny.java": [
        (
            "import java.io.Serial;\n\npublic final class FlowableAny",
            "import java.io.Serial;\n\n"
            "/**\n"
            " * 判断上游是否存在任一元素满足 {@link Predicate}；存在 emit true，否则 emit false。\n"
            " * @param <T> 上游元素类型\n"
            " */\n"
            "public final class FlowableAny",
        ),
        (
            "    public FlowableAny(Flowable<T> source, Predicate<? super T> predicate) {",
            "    /**\n"
            "     * @param source 上游 Flowable\n"
            "     * @param predicate 测试谓词\n"
            "     */\n"
            "    public FlowableAny(Flowable<T> source, Predicate<? super T> predicate) {",
        ),
        (
            "        @Override\n        public void onNext(T t) {",
            "        /** predicate 为 true 时取消上游并 emit true。 */\n"
            "        @Override\n        public void onNext(T t) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 未找到匹配则 emit false。 */\n"
            "        @Override\n        public void onComplete() {",
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
            "wave8b completable/flowable operators [15:30]",
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
