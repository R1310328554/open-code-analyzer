#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-7b completable operators [15:30]."""
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
WAVE7B_FILE = Path("/tmp/rxjava_w7b.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE7B_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "CompletableDoFinally.java": [
        (
            "/**\n * Execute an action after an onError, onComplete or a dispose event.\n * <p>History: 2.0.1 - experimental\n * @since 2.1\n */",
            "/**\n * 在 onError、onComplete 或 dispose 事件之后执行一个 action。\n"
            " * <p>History: 2.0.1 - experimental\n"
            " * @since 2.1\n"
            " */",
        ),
        (
            "    public CompletableDoFinally(CompletableSource source, Action onFinally) {",
            "    /**\n"
            "     * @param source 上游 CompletableSource\n"
            "     * @param onFinally 终止或 dispose 时执行的 action\n"
            "     */\n"
            "    public CompletableDoFinally(CompletableSource source, Action onFinally) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 订阅上游并在 DoFinallyObserver 中包装下游 observer。 */\n"
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
        (
            "        void runFinally() {",
            "        /** 保证 onFinally 至多执行一次。 */\n"
            "        void runFinally() {",
        ),
    ],
    "CompletableDoOnEvent.java": [
        (
            "import io.reactivex.rxjava4.functions.Consumer;\n\npublic final class CompletableDoOnEvent",
            "import io.reactivex.rxjava4.functions.Consumer;\n\n"
            "/**\n"
            " * 在上游完成或出错时调用 {@link Consumer}；完成时传入 null，出错时传入 {@link Throwable}。\n"
            " */\n"
            "public final class CompletableDoOnEvent",
        ),
        (
            "    public CompletableDoOnEvent(final CompletableSource source, final Consumer<? super Throwable> onEvent) {",
            "    /**\n"
            "     * @param source 上游 CompletableSource\n"
            "     * @param onEvent 事件回调（完成时为 null，出错时为 Throwable）\n"
            "     */\n"
            "    public CompletableDoOnEvent(final CompletableSource source, final Consumer<? super Throwable> onEvent) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 调用 onEvent(null) 后转发 onComplete。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
        (
            "        @Override\n        public void onError(Throwable e) {",
            "        /** 调用 onEvent(e) 后转发 onError。 */\n"
            "        @Override\n        public void onError(Throwable e) {",
        ),
    ],
    "CompletableEmpty.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;\n\npublic final class CompletableEmpty",
            "import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;\n\n"
            "/**\n"
            " * 立即完成的 {@link Completable}；使用单例 {@link #INSTANCE}。\n"
            " */\n"
            "public final class CompletableEmpty",
        ),
        (
            "    public static final Completable INSTANCE = new CompletableEmpty();",
            "    /** 单例实例。 */\n"
            "    public static final Completable INSTANCE = new CompletableEmpty();",
        ),
        (
            "    @Override\n    public void subscribeActual(CompletableObserver observer) {",
            "    /** 通过 {@link EmptyDisposable#complete} 立即完成下游。 */\n"
            "    @Override\n    public void subscribeActual(CompletableObserver observer) {",
        ),
    ],
    "CompletableError.java": [
        (
            "import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;\n\npublic final class CompletableError",
            "import io.reactivex.rxjava4.internal.disposables.EmptyDisposable;\n\n"
            "/**\n"
            " * 订阅时立即向 observer 发出指定错误的 {@link Completable}。\n"
            " */\n"
            "public final class CompletableError",
        ),
        (
            "    public CompletableError(Throwable error) {",
            "    /** @param error 要发出的错误 */\n"
            "    public CompletableError(Throwable error) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 通过 {@link EmptyDisposable#error} 立即发出错误。 */\n"
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
    ],
    "CompletableErrorSupplier.java": [
        (
            "import java.util.Objects;\n\npublic final class CompletableErrorSupplier",
            "import java.util.Objects;\n\n"
            "/**\n"
            " * 订阅时调用 {@link Supplier} 获取错误并立即向 observer 发出。\n"
            " */\n"
            "public final class CompletableErrorSupplier",
        ),
        (
            "    public CompletableErrorSupplier(Supplier<? extends Throwable> errorSupplier) {",
            "    /** @param errorSupplier 提供错误的 Supplier */\n"
            "    public CompletableErrorSupplier(Supplier<? extends Throwable> errorSupplier) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 获取错误并通过 {@link EmptyDisposable#error} 发出。 */\n"
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
    ],
    "CompletableFromAction.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class CompletableFromAction",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 订阅时同步执行 {@link Action}，成功则 onComplete，异常则 onError。\n"
            " */\n"
            "public final class CompletableFromAction",
        ),
        (
            "    public CompletableFromAction(Action run) {",
            "    /** @param run 要执行的 action */\n"
            "    public CompletableFromAction(Action run) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 执行 action 并根据结果完成或出错。 */\n"
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
    ],
    "CompletableFromCallable.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class CompletableFromCallable",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 订阅时同步调用 {@link Callable}，成功则 onComplete，异常则 onError。\n"
            " */\n"
            "public final class CompletableFromCallable",
        ),
        (
            "    public CompletableFromCallable(Callable<?> callable) {",
            "    /** @param callable 要调用的 Callable */\n"
            "    public CompletableFromCallable(Callable<?> callable) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 调用 callable 并根据结果完成或出错。 */\n"
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
    ],
    "CompletableFromObservable.java": [
        (
            "import io.reactivex.rxjava4.disposables.Disposable;\n\npublic final class CompletableFromObservable",
            "import io.reactivex.rxjava4.disposables.Disposable;\n\n"
            "/**\n"
            " * 将 {@link ObservableSource} 转为 {@link Completable}，忽略 onNext，仅传递 onComplete/onError。\n"
            " * @param <T> Observable 元素类型\n"
            " */\n"
            "public final class CompletableFromObservable",
        ),
        (
            "    public CompletableFromObservable(ObservableSource<T> observable) {",
            "    /** @param observable 上游 ObservableSource */\n"
            "    public CompletableFromObservable(ObservableSource<T> observable) {",
        ),
        (
            "            public void onNext(T value) {\n                // Deliberately ignored.",
            "            /** 故意忽略 onNext 值。 */\n"
            "            public void onNext(T value) {\n                // Deliberately ignored.",
        ),
    ],
    "CompletableFromPublisher.java": [
        (
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;\n\npublic final class CompletableFromPublisher",
            "import io.reactivex.rxjava4.internal.subscriptions.SubscriptionHelper;\n\n"
            "/**\n"
            " * 将 {@link Publisher} 转为 {@link Completable}，忽略 onNext，仅传递 onComplete/onError。\n"
            " * @param <T> Publisher 元素类型\n"
            " */\n"
            "public final class CompletableFromPublisher",
        ),
        (
            "    public CompletableFromPublisher(Publisher<T> flowable) {",
            "    /** @param flowable 上游 Publisher */\n"
            "    public CompletableFromPublisher(Publisher<T> flowable) {",
        ),
        (
            "        @Override\n        public void onNext(T t) {\n            // ignored",
            "        /** 忽略 onNext 元素。 */\n"
            "        @Override\n        public void onNext(T t) {\n            // ignored",
        ),
        (
            "        @Override\n        public void dispose() {",
            "        /** 取消上游订阅。 */\n"
            "        @Override\n        public void dispose() {",
        ),
    ],
    "CompletableFromRunnable.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class CompletableFromRunnable",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 订阅时同步执行 {@link Runnable}，成功则 onComplete，异常则 onError。\n"
            " */\n"
            "public final class CompletableFromRunnable",
        ),
        (
            "    public CompletableFromRunnable(Runnable runnable) {",
            "    /** @param runnable 要执行的 Runnable */\n"
            "    public CompletableFromRunnable(Runnable runnable) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 执行 runnable 并根据结果完成或出错。 */\n"
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
    ],
    "CompletableFromSingle.java": [
        (
            "import io.reactivex.rxjava4.disposables.Disposable;\n\npublic final class CompletableFromSingle",
            "import io.reactivex.rxjava4.disposables.Disposable;\n\n"
            "/**\n"
            " * 将 {@link SingleSource} 转为 {@link Completable}，onSuccess 映射为 onComplete。\n"
            " * @param <T> Single 值类型\n"
            " */\n"
            "public final class CompletableFromSingle",
        ),
        (
            "    public CompletableFromSingle(SingleSource<T> single) {",
            "    /** @param single 上游 SingleSource */\n"
            "    public CompletableFromSingle(SingleSource<T> single) {",
        ),
        (
            "            public void onSuccess(T value) {",
            "            /** 将 onSuccess 映射为 onComplete。 */\n"
            "            public void onSuccess(T value) {",
        ),
    ],
    "CompletableFromSupplier.java": [
        (
            "/**\n * Call a Supplier for each incoming CompletableObserver and signal completion or the thrown exception.\n * @since 3.0.0\n */",
            "/**\n * 对每个订阅的 CompletableObserver 调用 Supplier，成功则 onComplete，异常则 onError。\n"
            " * @since 3.0.0\n"
            " */",
        ),
        (
            "    public CompletableFromSupplier(Supplier<?> supplier) {",
            "    /** @param supplier 要调用的 Supplier */\n"
            "    public CompletableFromSupplier(Supplier<?> supplier) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 调用 supplier 并根据结果完成或出错。 */\n"
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
    ],
    "CompletableFromUnsafeSource.java": [
        (
            "import io.reactivex.rxjava4.core.*;\n\npublic final class CompletableFromUnsafeSource",
            "import io.reactivex.rxjava4.core.*;\n\n"
            "/**\n"
            " * 直接订阅上游 {@link CompletableSource}，不做 null 校验或插件包装。\n"
            " */\n"
            "public final class CompletableFromUnsafeSource",
        ),
        (
            "    public CompletableFromUnsafeSource(CompletableSource source) {",
            "    /** @param source 上游 CompletableSource */\n"
            "    public CompletableFromUnsafeSource(CompletableSource source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 直接将 observer 传给上游 source。 */\n"
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
    ],
    "CompletableHide.java": [
        (
            "/**\n * Hides the identity of the upstream Completable and its Disposable sent through onSubscribe.\n */",
            "/**\n * 隐藏上游 Completable 的身份，以及通过 onSubscribe 传递的 Disposable。\n"
            " */",
        ),
        (
            "    public CompletableHide(CompletableSource source) {",
            "    /** @param source 上游 CompletableSource */\n"
            "    public CompletableHide(CompletableSource source) {",
        ),
        (
            "        @Override\n        public void onSubscribe(Disposable d) {",
            "        /** 包装上游 Disposable，向下游传递自身作为 Disposable。 */\n"
            "        @Override\n        public void onSubscribe(Disposable d) {",
        ),
        (
            "        @Override\n        public void dispose() {",
            "        /** dispose 上游并将自身标记为已 dispose。 */\n"
            "        @Override\n        public void dispose() {",
        ),
    ],
    "CompletableLift.java": [
        (
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\npublic final class CompletableLift",
            "import io.reactivex.rxjava4.plugins.RxJavaPlugins;\n\n"
            "/**\n"
            " * 通过 {@link CompletableOperator} 变换下游 observer 后再订阅上游。\n"
            " */\n"
            "public final class CompletableLift",
        ),
        (
            "    public CompletableLift(CompletableSource source, CompletableOperator onLift) {",
            "    /**\n"
            "     * @param source 上游 CompletableSource\n"
            "     * @param onLift 变换 observer 的 operator\n"
            "     */\n"
            "    public CompletableLift(CompletableSource source, CompletableOperator onLift) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 应用 onLift 变换 observer 后订阅上游。 */\n"
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
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
            "wave7b completable operators [15:30]",
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
