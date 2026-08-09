#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-4b [15:30]."""
from __future__ import annotations

import importlib.util
import json
import re
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "rxjava/4.0.0-alpha-21"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = [
    "src/main/java/io/reactivex/rxjava4/functions/Function6.java",
    "src/main/java/io/reactivex/rxjava4/functions/Function7.java",
    "src/main/java/io/reactivex/rxjava4/functions/Function8.java",
    "src/main/java/io/reactivex/rxjava4/functions/Function9.java",
    "src/main/java/io/reactivex/rxjava4/functions/IntFunction.java",
    "src/main/java/io/reactivex/rxjava4/functions/LongConsumer.java",
    "src/main/java/io/reactivex/rxjava4/functions/Predicate.java",
    "src/main/java/io/reactivex/rxjava4/functions/Supplier.java",
    "src/main/java/io/reactivex/rxjava4/functions/package-info.java",
    "src/main/java/io/reactivex/rxjava4/internal/disposables/ArrayCompositeDisposable.java",
    "src/main/java/io/reactivex/rxjava4/internal/disposables/CancellableDisposable.java",
    "src/main/java/io/reactivex/rxjava4/internal/disposables/DisposableHelper.java",
    "src/main/java/io/reactivex/rxjava4/internal/disposables/DisposableOnly.java",
    "src/main/java/io/reactivex/rxjava4/internal/disposables/EmptyDisposable.java",
    "src/main/java/io/reactivex/rxjava4/internal/disposables/ListCompositeDisposable.java",
]

_COMMON_FN = (
    "A functional interface (callback) that computes a value based on multiple input values.",
    "根据多个输入值计算结果的函数式接口（回调）。",
)
_COMMON_APPLY = (
    "     * Calculate a value based on the input values.\n"
    "     * @return the result value\n"
    "     * @throws Throwable if the implementation wishes to throw any type of exception",
    "     * 根据输入值计算结果。\n"
    "     * @return 计算结果\n"
    "     * @throws Throwable 若实现需要可抛出任意类型的异常",
)
_ORDINALS = (
    "first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth",
)
_ORDINALS_ZH = ("第一", "第二", "第三", "第四", "第五", "第六", "第七", "第八", "第九")


def _fn_replacements(n: int) -> list[tuple[str, str]]:
    reps: list[tuple[str, str]] = []
    old_params = "\n".join(f" * @param <T{i}> the {_ORDINALS[i - 1]} value type" for i in range(1, n + 1))
    new_params = "\n".join(f" * @param <T{i}> 第{_ORDINALS_ZH[i - 1]}个值的类型" for i in range(1, n + 1))
    old_params += "\n * @param <R> the result type"
    new_params += "\n * @param <R> 结果类型"
    reps.append(
        (
            f"/**\n * {_COMMON_FN[0]}\n{old_params}\n */",
            f"/**\n * {_COMMON_FN[1]}\n{new_params}\n */",
        )
    )
    old_apply = "    /**\n" + _COMMON_APPLY[0].replace(
        "     * @return the result value",
        "\n".join(f"     * @param t{i} the {_ORDINALS[i - 1]} value" for i in range(1, n + 1))
        + "\n     * @return the result value",
    ) + "\n     */"
    new_apply = "    /**\n" + _COMMON_APPLY[1].replace(
        "     * @return 计算结果",
        "\n".join(f"     * @param t{i} 第{_ORDINALS_ZH[i - 1]}个值" for i in range(1, n + 1))
        + "\n     * @return 计算结果",
    ) + "\n     */"
    reps.append((old_apply, new_apply))
    return reps


def _load_large() -> dict[str, list[tuple[str, str]]]:
    path = Path(__file__).with_name("annotate_rxjava_wave4b_large.py")
    if not path.exists():
        return {}
    spec = importlib.util.spec_from_file_location("rxjava_w4b_large", path)
    if spec is None or spec.loader is None:
        return {}
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return getattr(mod, "LARGE_REPLACEMENTS", {})


FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "Function6.java": _fn_replacements(6),
    "Function7.java": _fn_replacements(7),
    "Function8.java": _fn_replacements(8),
    "Function9.java": _fn_replacements(9),
    "IntFunction.java": [
        (
            "/**\n * A functional interface (callback) that takes a primitive value and return value of type T.\n * @param <T> the returned value type\n */",
            "/**\n * 接受基本类型值并返回类型 T 的值的函数式接口（回调）。\n * @param <T> 返回值类型\n */",
        ),
        (
            "    /**\n     * Calculates a value based on a primitive integer input.\n     * @param i the input value\n     * @return the result Object\n     * @throws Throwable if the implementation wishes to throw any type of exception\n     */",
            "    /**\n     * 根据基本类型 int 输入计算结果。\n     * @param i 输入值\n     * @return 结果对象\n     * @throws Throwable 若实现需要可抛出任意类型的异常\n     */",
        ),
    ],
    "LongConsumer.java": [
        (
            "/**\n * A functional interface (callback) that consumes a primitive long value.\n */",
            "/**\n * 消费基本类型 long 值的函数式接口（回调）。\n */",
        ),
        (
            "    /**\n     * Consume a primitive long input.\n     * @param t the primitive long value\n     * @throws Throwable if the implementation wishes to throw any type of exception\n     */",
            "    /**\n     * 消费基本类型 long 输入。\n     * @param t 基本类型 long 值\n     * @throws Throwable 若实现需要可抛出任意类型的异常\n     */",
        ),
    ],
    "Predicate.java": [
        (
            "/**\n * A functional interface (callback) that returns true or false for the given input value.\n * @param <T> the first value\n */",
            "/**\n * 对给定输入值返回 true 或 false 的函数式接口（回调）。\n * @param <T> 输入值类型\n */",
        ),
        (
            "    /**\n     * Test the given input value and return a boolean.\n     * @param t the value\n     * @return the boolean result\n     * @throws Throwable if the implementation wishes to throw any type of exception\n     */",
            "    /**\n     * 测试给定输入值并返回布尔结果。\n     * @param t 输入值\n     * @return 布尔结果\n     * @throws Throwable 若实现需要可抛出任意类型的异常\n     */",
        ),
    ],
    "Supplier.java": [
        (
            "/**\n * A functional interface (callback) that provides a single value or\n * throws an exception.\n * <p>\n * This interface was added to allow throwing any subclass of {@link Throwable}s,\n * which is not directly possible with the Java standard {@link java.util.concurrent.Callable} interface.\n * @param <T> the value type returned\n * @since 3.0.0\n */",
            "/**\n * 提供单个值或抛出异常的函数式接口（回调）。\n * <p>\n * 添加此接口是为了允许抛出 {@link Throwable} 的任意子类，\n * 而标准 Java {@link java.util.concurrent.Callable} 接口无法直接做到。\n * @param <T> 返回的值类型\n * @since 3.0.0\n */",
        ),
        (
            "    /**\n     * Produces a value or throws an exception.\n     * @return the value produced\n     * @throws Throwable if the implementation wishes to throw any type of exception\n     */",
            "    /**\n     * 产生一个值或抛出异常。\n     * @return 产生的值\n     * @throws Throwable 若实现需要可抛出任意类型的异常\n     */",
        ),
    ],
    "package-info.java": [
        (
            "/**\n * Functional interfaces of functions and actions of arity 0 to 9 and related\n * utility classes.\n */",
            "/**\n * 元数 0 至 9 的函数与动作函数式接口及相关工具类。\n */",
        ),
    ],
    "ArrayCompositeDisposable.java": [
        (
            "/**\n * A composite disposable with a fixed number of slots.\n *\n * <p>Note that since the implementation leaks the methods of AtomicReferenceArray, one must be\n * careful to only call setResource, replaceResource and dispose on it. All other methods may lead to undefined behavior\n * and should be used by internal means only.\n */",
            "/**\n * 具有固定槽位数量的复合 disposable。\n *\n * <p>注意：由于实现暴露了 {@link AtomicReferenceArray} 的方法，应仅调用 setResource、replaceResource 与 dispose；\n * 调用其它方法可能导致未定义行为，仅供内部使用。\n */",
        ),
        (
            "    public ArrayCompositeDisposable(int capacity) {",
            "    /** @param capacity 槽位数量 */\n    public ArrayCompositeDisposable(int capacity) {",
        ),
        (
            "    /**\n     * Sets the resource at the specified index and disposes the old resource.\n     * @param index the index of the resource to set\n     * @param resource the new resource\n     * @return true if the resource has been set, false if the composite has been disposed\n     */",
            "    /**\n     * 在指定索引设置资源并 dispose 旧资源。\n     * @param index 要设置资源的索引\n     * @param resource 新资源\n     * @return 若资源已设置则为 true；若复合体已被 dispose 则为 false\n     */",
        ),
        (
            "    /**\n     * Replaces the resource at the specified index and returns the old resource.\n     * @param index the index of the resource to replace\n     * @param resource the new resource\n     * @return the old resource, can be null\n     */",
            "    /**\n     * 替换指定索引的资源并返回旧资源。\n     * @param index 要替换资源的索引\n     * @param resource 新资源\n     * @return 旧资源，可为 null\n     */",
        ),
        (
            "    @Override\n    public void dispose() {",
            "    /** dispose 所有槽位中的资源。 */\n    @Override\n    public void dispose() {",
        ),
        (
            "    @Override\n    public boolean isDisposed() {",
            "    /** 若首个槽位标记为已 dispose 则返回 true。 */\n    @Override\n    public boolean isDisposed() {",
        ),
    ],
    "CancellableDisposable.java": [
        (
            "/**\n * A disposable container that wraps a Cancellable instance.\n * <p>\n * Watch out for the AtomicReference API leak!\n */",
            "/**\n * 包装 {@link Cancellable} 实例的 disposable 容器。\n * <p>\n * 注意 {@link AtomicReference} API 泄漏问题！\n */",
        ),
        (
            "    public CancellableDisposable(Cancellable cancellable) {",
            "    /** @param cancellable 要包装的 Cancellable */\n    public CancellableDisposable(Cancellable cancellable) {",
        ),
        (
            "    @Override\n    public boolean isDisposed() {",
            "    /** 若内部引用已为 null 则视为已 dispose。 */\n    @Override\n    public boolean isDisposed() {",
        ),
        (
            "    @Override\n    public void dispose() {",
            "    /** 原子取出 Cancellable 并调用 cancel，仅执行一次。 */\n    @Override\n    public void dispose() {",
        ),
    ],
    "DisposableOnly.java": [
        (
            "/**\n * An extension to {@link Disposable} that allows not\n * implementing the {@link Disposable#isDisposed()} as it\n * is practically never needed or cannot be observed anyways.\n * @since 4.0.0\n */",
            "/**\n * {@link Disposable} 的扩展，允许不实现 {@link Disposable#isDisposed()}，\n * 因为在实践中几乎不需要或无法观测。\n * @since 4.0.0\n */",
        ),
        (
            "    @Override\n    default boolean isDisposed() {",
            "    /** 默认不支持 isDisposed，调用时抛出 {@link UnsupportedOperationException}。 */\n    @Override\n    default boolean isDisposed() {",
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
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    done = [ln.strip() for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    pending = [ln.strip() for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
    done_set = set(done)
    pending_set = set(pending)
    for rel in files:
        if rel not in done_set:
            done.append(rel)
            done_set.add(rel)
        pending_set.discard(rel)
    done_path.write_text(("\n".join(done) + ("\n" if done else "")), encoding="utf-8")
    pending = [ln for ln in pending if ln in pending_set]
    pending_path.write_text(("\n".join(pending) + ("\n" if pending else "")), encoding="utf-8")
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    batch["done"] = len(done)
    batch["remaining_pending"] = len(pending)
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main() -> int:
    all_replacements = {**FILE_REPLACEMENTS, **_load_large()}
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        name = Path(rel).name
        src = ORIGINAL / rel
        dst = ANALYZED / rel
        if not src.exists():
            failures.append(f"MISSING original: {rel}")
            continue
        reps = all_replacements.get(name, [])
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
    if not failures:
        mark_queue_done(BATCH_FILES)
        print("Marked 15 files done in queue")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
