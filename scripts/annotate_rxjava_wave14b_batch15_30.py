#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-14b Maybe operators [15:30]."""
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
WAVE14B_FILE = Path("/tmp/rxjava_w14b.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE14B_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "MaybeFlatten.java": [
        (
            "/**\n * Maps a value into a MaybeSource and relays its signal.\n *\n * @param <T> the source value type\n * @param <R> the result value type\n */",
            "/**\n"
            " * 将上游 onSuccess 值映射为 {@link MaybeSource} 并转发其信号。\n"
            " *\n * @param <T> 上游元素类型\n"
            " * @param <R> 结果元素类型\n"
            " */",
        ),
        (
            "    public MaybeFlatten(MaybeSource<T> source, Function<? super T, ? extends MaybeSource<? extends R>> mapper) {",
            "    /**\n"
            "     * @param source 上游 Maybe\n"
            "     * @param mapper 将值映射为 MaybeSource 的函数\n"
            "     */\n"
            "    public MaybeFlatten(MaybeSource<T> source, Function<? super T, ? extends MaybeSource<? extends R>> mapper) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super R> observer) {",
            "    /** 订阅 FlatMapMaybeObserver 并在 onSuccess 时 flatMap 内部 MaybeSource。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super R> observer) {",
        ),
        (
            "    static final class FlatMapMaybeObserver<T, R>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable {",
            "    /** onSuccess 时应用 mapper 并订阅内部 MaybeSource。 */\n"
            "    static final class FlatMapMaybeObserver<T, R>\n    extends AtomicReference<Disposable>\n    implements MaybeObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** 应用 mapper 获取 MaybeSource 并订阅 InnerObserver。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
        (
            "        final class InnerObserver implements MaybeObserver<R> {",
            "        /** 转发内部 MaybeSource 的 onSuccess/onError/onComplete。 */\n"
            "        final class InnerObserver implements MaybeObserver<R> {",
        ),
    ],
    "MaybeFromAction.java": [
        (
            "/**\n * Executes an Action and signals its exception or completes normally.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 执行 {@link Action}，异常时 onError，正常完成时 onComplete。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeFromAction(Action action) {",
            "    /** @param action 待执行的 Action */\n"
            "    public MaybeFromAction(Action action) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 执行 action.run() 并转发异常或 onComplete。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "        return null; // considered as onComplete()",
            "        return null; // 视为 onComplete()",
        ),
    ],
    "MaybeFromCallable.java": [
        (
            "/**\n * Executes a callable and signals its value as success or signals an exception.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 执行 {@link Callable}，非 null 值 onSuccess，null 时 onComplete，异常时 onError。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeFromCallable(Callable<? extends T> callable) {",
            "    /** @param callable 待执行的 Callable */\n"
            "    public MaybeFromCallable(Callable<? extends T> callable) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 调用 callable.call() 并按返回值或异常转发信号。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
    ],
    "MaybeFromCompletable.java": [
        (
            "/**\n * Wrap a Completable into a Maybe.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 将 {@link CompletableSource} 包装为 {@link Maybe}，\n"
            " * onComplete 映射为 Maybe 的 onComplete，onError 原样转发。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeFromCompletable(CompletableSource source) {",
            "    /** @param source 上游 Completable */\n"
            "    public MaybeFromCompletable(CompletableSource source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 订阅 Completable 并将 onComplete/onError 映射为 Maybe 信号。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class FromCompletableObserver<T> implements CompletableObserver, Disposable {",
            "    /** 将 Completable 的 onComplete/onError 转发为 Maybe 信号。 */\n"
            "    static final class FromCompletableObserver<T> implements CompletableObserver, Disposable {",
        ),
    ],
    "MaybeFromFuture.java": [
        (
            "/**\n * Waits until the source Future completes or the wait times out; treats a {@code null}\n * result as indication to signal {@code onComplete} instead of {@code onSuccess}.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 等待 {@link Future} 完成或超时；\n"
            " * {@code null} 结果触发 onComplete 而非 onSuccess。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeFromFuture(Future<? extends T> future, long timeout, TimeUnit unit) {",
            "    /**\n"
            "     * @param future 待等待的 Future\n"
            "     * @param timeout 超时时间（≤0 表示无限等待）\n"
            "     * @param unit 时间单位\n"
            "     */\n"
            "    public MaybeFromFuture(Future<? extends T> future, long timeout, TimeUnit unit) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 调用 future.get 并按结果或异常转发信号。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
    ],
    "MaybeFromRunnable.java": [
        (
            "/**\n * Executes a Runnable and signals its exception or completes normally.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 执行 {@link Runnable}，异常时 onError，正常完成时 onComplete。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeFromRunnable(Runnable runnable) {",
            "    /** @param runnable 待执行的 Runnable */\n"
            "    public MaybeFromRunnable(Runnable runnable) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 执行 runnable.run() 并转发异常或 onComplete。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
    ],
    "MaybeFromSingle.java": [
        (
            "/**\n * Wrap a Single into a Maybe.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 将 {@link SingleSource} 包装为 {@link Maybe}，\n"
            " * onSuccess 原样转发，onError 原样转发。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeFromSingle(SingleSource<T> source) {",
            "    /** @param source 上游 Single */\n"
            "    public MaybeFromSingle(SingleSource<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 订阅 Single 并将 onSuccess/onError 映射为 Maybe 信号。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class FromSingleObserver<T> implements SingleObserver<T>, Disposable {",
            "    /** 将 Single 的 onSuccess/onError 转发为 Maybe 信号。 */\n"
            "    static final class FromSingleObserver<T> implements SingleObserver<T>, Disposable {",
        ),
    ],
    "MaybeFromSupplier.java": [
        (
            "/**\n * Executes a supplier and signals its value as success or signals an exception.\n *\n * @param <T> the value type\n * @since 3.0.0\n */",
            "/**\n"
            " * 执行 {@link Supplier}，非 null 值 onSuccess，null 时 onComplete，异常时 onError。\n"
            " *\n * @param <T> 元素类型\n"
            " * @since 3.0.0\n"
            " */",
        ),
        (
            "    public MaybeFromSupplier(Supplier<? extends T> supplier) {",
            "    /** @param supplier 待执行的 Supplier */\n"
            "    public MaybeFromSupplier(Supplier<? extends T> supplier) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 调用 supplier.get() 并按返回值或异常转发信号。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
    ],
    "MaybeHide.java": [
        (
            "/**\n * Hides the identity of the upstream Maybe and its Disposable sent through onSubscribe.\n * \n * @param <T> the value type\n */",
            "/**\n"
            " * 隐藏上游 {@link Maybe} 及其通过 onSubscribe 传递的 {@link Disposable} 身份，\n"
            " * 防止下游直接 dispose 上游。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeHide(MaybeSource<T> source) {",
            "    /** @param source 上游 Maybe */\n"
            "    public MaybeHide(MaybeSource<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 订阅 HideMaybeObserver 并包装 Disposable。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class HideMaybeObserver<T> implements MaybeObserver<T>, Disposable {",
            "    /** 透传信号，onSubscribe 时向下游传递自身而非上游 Disposable。 */\n"
            "    static final class HideMaybeObserver<T> implements MaybeObserver<T>, Disposable {",
        ),
    ],
    "MaybeIgnoreElement.java": [
        (
            "/**\n * Turns an onSuccess into an onComplete, onError and onComplete is relayed as is.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 将 onSuccess 转为 onComplete，onError 与 onComplete 原样转发。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeIgnoreElement(MaybeSource<T> source) {",
            "    /** @param source 上游 Maybe */\n"
            "    public MaybeIgnoreElement(MaybeSource<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 订阅 IgnoreMaybeObserver 并丢弃 onSuccess 值。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
        (
            "    static final class IgnoreMaybeObserver<T> implements MaybeObserver<T>, Disposable {",
            "    /** onSuccess 时丢弃值并转发 onComplete。 */\n"
            "    static final class IgnoreMaybeObserver<T> implements MaybeObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** 忽略值，转发 onComplete。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
    ],
    "MaybeIgnoreElementCompletable.java": [
        (
            "/**\n * Turns an onSuccess into an onComplete, onError and onComplete is relayed as is.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 将 Maybe 的 onSuccess 转为 Completable 的 onComplete，\n"
            " * onError 与 onComplete 原样转发。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeIgnoreElementCompletable(MaybeSource<T> source) {",
            "    /** @param source 上游 Maybe */\n"
            "    public MaybeIgnoreElementCompletable(MaybeSource<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
            "    /** 订阅 IgnoreMaybeObserver 并丢弃 onSuccess 值。 */\n"
            "    @Override\n    protected void subscribeActual(CompletableObserver observer) {",
        ),
        (
            "    @Override\n    public Maybe<T> fuseToMaybe() {",
            "    /** 融合回 {@link MaybeIgnoreElement}。 */\n"
            "    @Override\n    public Maybe<T> fuseToMaybe() {",
        ),
        (
            "    static final class IgnoreMaybeObserver<T> implements MaybeObserver<T>, Disposable {",
            "    /** onSuccess 时丢弃值并转发 Completable onComplete。 */\n"
            "    static final class IgnoreMaybeObserver<T> implements MaybeObserver<T>, Disposable {",
        ),
    ],
    "MaybeIsEmpty.java": [
        (
            "/**\n * Signals true if the source Maybe signals onComplete, signals false if the source Maybe\n * signals onSuccess.\n * \n * @param <T> the value type\n */",
            "/**\n"
            " * 上游 onComplete 时发射 true，onSuccess 时发射 false。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeIsEmpty(MaybeSource<T> source) {",
            "    /** @param source 上游 Maybe */\n"
            "    public MaybeIsEmpty(MaybeSource<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super Boolean> observer) {",
            "    /** 订阅 IsEmptyMaybeObserver 并判断上游是否为空。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super Boolean> observer) {",
        ),
        (
            "    static final class IsEmptyMaybeObserver<T>\n    implements MaybeObserver<T>, Disposable {",
            "    /** onComplete 发射 true；onSuccess 发射 false。 */\n"
            "    static final class IsEmptyMaybeObserver<T>\n    implements MaybeObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** 有值，发射 false。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 空源，发射 true。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "MaybeIsEmptySingle.java": [
        (
            "/**\n * Signals true if the source Maybe signals onComplete, signals false if the source Maybe\n * signals onSuccess.\n * \n * @param <T> the value type\n */",
            "/**\n"
            " * 上游 onComplete 时发射 true，onSuccess 时发射 false，\n"
            " * 以 {@link Single} 形式输出。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeIsEmptySingle(MaybeSource<T> source) {",
            "    /** @param source 上游 Maybe */\n"
            "    public MaybeIsEmptySingle(MaybeSource<T> source) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(SingleObserver<? super Boolean> observer) {",
            "    /** 订阅 IsEmptyMaybeObserver 并判断上游是否为空。 */\n"
            "    @Override\n    protected void subscribeActual(SingleObserver<? super Boolean> observer) {",
        ),
        (
            "    static final class IsEmptyMaybeObserver<T>\n    implements MaybeObserver<T>, Disposable {",
            "    /** onComplete 发射 true；onSuccess 发射 false。 */\n"
            "    static final class IsEmptyMaybeObserver<T>\n    implements MaybeObserver<T>, Disposable {",
        ),
        (
            "        @Override\n        public void onSuccess(T value) {",
            "        /** 有值，发射 false。 */\n"
            "        @Override\n        public void onSuccess(T value) {",
        ),
        (
            "        @Override\n        public void onComplete() {",
            "        /** 空源，发射 true。 */\n"
            "        @Override\n        public void onComplete() {",
        ),
    ],
    "MaybeJust.java": [
        (
            "/**\n * Signals a constant value.\n *\n * @param <T> the value type\n */",
            "/**\n"
            " * 发射常量值，实现 {@link ScalarSupplier} 以支持融合优化。\n"
            " *\n * @param <T> 元素类型\n"
            " */",
        ),
        (
            "    public MaybeJust(T value) {",
            "    /** @param value 待发射的常量值 */\n"
            "    public MaybeJust(T value) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
            "    /** 立即 onSubscribe(Disposable.disposed()) 并 onSuccess(value)。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super T> observer) {",
        ),
    ],
    "MaybeLift.java": [
        (
            "/**\n * Calls a MaybeOperator for the incoming MaybeObserver.\n *\n * @param <T> the upstream value type\n * @param <R> the downstream value type\n */",
            "/**\n"
            " * 对下游 {@link MaybeObserver} 应用 {@link MaybeOperator} 进行变换，\n"
            " * 再用变换后的 Observer 订阅上游。\n"
            " *\n * @param <T> 上游元素类型\n"
            " * @param <R> 下游元素类型\n"
            " */",
        ),
        (
            "    public MaybeLift(MaybeSource<T> source, MaybeOperator<? extends R, ? super T> operator) {",
            "    /**\n"
            "     * @param source 上游 Maybe\n"
            "     * @param operator 对 MaybeObserver 进行变换的 MaybeOperator\n"
            "     */\n"
            "    public MaybeLift(MaybeSource<T> source, MaybeOperator<? extends R, ? super T> operator) {",
        ),
        (
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super R> observer) {",
            "    /** 应用 operator 获取 lifted Observer 后订阅上游。 */\n"
            "    @Override\n    protected void subscribeActual(MaybeObserver<? super R> observer) {",
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
            "wave14b Maybe* [15:30]",
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
        print(f"Marked {ok} files done in queue (note=wave14b)")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
