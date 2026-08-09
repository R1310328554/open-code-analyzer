#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-4a [0:15]."""
from __future__ import annotations

import json
import re
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "rxjava/4.0.0-alpha-21"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
WAVE4A_FILE = Path("/tmp/rx_w4a.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE4A_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "NeverDisposableStreamerCancellation.java": [
        (
            "/// A [DisposableStreamerCancellation] handler that does nothing.",
            "/**\n * 不执行任何操作的 {@link DisposableStreamerCancellation} 处理器。\n */",
        ),
    ],
    "SequentialDisposable.java": [
        (
            "/**\n * A Disposable container that allows updating/replacing a Disposable\n * atomically and with respect of disposing the container itself.\n * <p>\n * The class extends AtomicReference directly so watch out for the API leak!\n * @since 2.0\n */",
            "/**\n * Disposable 容器，允许原子地更新/替换 Disposable，并正确处理容器自身的 dispose。\n * <p>\n * 本类直接继承 AtomicReference，注意 API 泄漏风险！\n * @since 2.0\n */",
        ),
        (
            "    /**\n     * Constructs an empty SequentialDisposable.\n     */",
            "    /**\n     * 构造空的 SequentialDisposable。\n     */",
        ),
        (
            "    /**\n     * Construct a SequentialDisposable with the initial Disposable provided.\n     * @param initial the initial disposable, null allowed\n     */",
            "    /**\n     * 使用给定初始 Disposable 构造 SequentialDisposable。\n     * @param initial 初始 disposable，允许为 null\n     */",
        ),
        (
            "    /**\n     * Atomically: set the next disposable on this container and dispose the previous\n     * one (if any) or dispose next if the container has been disposed.\n     * @param next the Disposable to set, may be null\n     * @return true if the operation succeeded, false if the container has been disposed\n     * @see #replace(Disposable)\n     */",
            "    /**\n     * 原子操作：在本容器上设置下一个 disposable 并 dispose 前一个（若有）；\n     * 若容器已被 dispose 则 dispose next。\n     * @param next 要设置的 Disposable，可为 null\n     * @return 操作成功则为 true；若容器已被 dispose 则为 false\n     * @see #replace(Disposable)\n     */",
        ),
        (
            "    /**\n     * Atomically: set the next disposable on this container but don't dispose the previous\n     * one (if any) or dispose next if the container has been disposed.\n     * @param next the Disposable to set, may be null\n     * @return true if the operation succeeded, false if the container has been disposed\n     * @see #update(Disposable)\n     */",
            "    /**\n     * 原子操作：在本容器上设置下一个 disposable，但不 dispose 前一个（若有）；\n     * 若容器已被 dispose 则 dispose next。\n     * @param next 要设置的 Disposable，可为 null\n     * @return 操作成功则为 true；若容器已被 dispose 则为 false\n     * @see #update(Disposable)\n     */",
        ),
    ],
    "ObjectHelper.java": [
        (
            "/**\n * Utility methods containing the backport of Java 7's Objects utility class.\n * <p>Named as such to avoid clash with java.util.Objects.\n */",
            "/**\n * 包含 Java 7 {@code Objects} 工具类 backport 的工具方法。\n * <p>命名如此以避免与 {@code java.util.Objects} 冲突。\n */",
        ),
        (
            "    /** Utility class. */",
            "    /** 工具类。 */",
        ),
        (
            "    /**\n     * Returns a BiPredicate that compares its parameters via Objects.equals().\n     * @param <T> the value type\n     * @return the bi-predicate instance\n     */",
            "    /**\n     * 返回通过 {@code Objects.equals()} 比较参数的 {@link BiPredicate}。\n     * @param <T> 值类型\n     * @return bi-predicate 实例\n     */",
        ),
        (
            "    /**\n     * Validate that the given value is positive or report an IllegalArgumentException with\n     * the parameter name.\n     * @param value the value to validate\n     * @param paramName the parameter name of the value\n     * @return value\n     * @throws IllegalArgumentException if bufferSize &lt;= 0\n     */",
            "    /**\n     * 验证给定值为正数，否则抛出带参数名的 {@link IllegalArgumentException}。\n     * @param value 待验证的值\n     * @param paramName 值的参数名\n     * @return value\n     * @throws IllegalArgumentException 若 bufferSize &lt;= 0\n     */",
        ),
    ],
    "AbstractEmptyQueueFuseable.java": [
        (
            "/**\n * Represents an empty, async-only {@link QueueFuseable} instance.\n *\n * @param <T> the output value type\n * @since 3.0.0\n */",
            "/**\n * 表示空的、仅支持异步模式的 {@link QueueFuseable} 实例。\n *\n * @param <T> 输出值类型\n * @since 3.0.0\n */",
        ),
    ],
    "CancellableQueueFuseable.java": [
        (
            "/**\n * Represents an empty, async-only {@link QueueFuseable} instance that tracks and exposes a\n * cancelled/disposed state.\n *\n * @param <T> the output value type\n * @since 3.0.0\n */",
            "/**\n * 表示空的、仅支持异步模式的 {@link QueueFuseable} 实例，跟踪并暴露已取消/dispose 状态。\n *\n * @param <T> 输出值类型\n * @since 3.0.0\n */",
        ),
    ],
    "FuseToFlowable.java": [
        (
            "/**\n * Interface indicating an operator implementation can be macro-fused back to Flowable in case\n * the operator goes from Flowable to some other reactive type and then the sequence calls\n * for toFlowable again:\n * <pre>\n * {@code\n * Single<Integer> single = Flowable.range(1, 10).reduce((a, b) -> a + b);\n * Flowable<Integer> flowable = single.toFlowable();\n * }\n * </pre>\n *\n * The {@code Single.toFlowable()} will check for this interface and call the {@link #fuseToFlowable()}\n * to return a Flowable which could be the Flowable-specific implementation of reduce(BiFunction).\n * <p>\n * This causes a slight overhead in assembly time (1 instanceof check, 1 operator allocation and 1 dropped\n * operator) but does not incur the conversion overhead at runtime.\n *\n * @param <T> the value type\n */",
            "/**\n * 表示算子实现可在从 Flowable 转为其他响应式类型后再次调用 toFlowable 时\n * 宏融合（macro-fuse）回 Flowable 的接口：\n * <pre>\n * {@code\n * Single<Integer> single = Flowable.range(1, 10).reduce((a, b) -> a + b);\n * Flowable<Integer> flowable = single.toFlowable();\n * }\n * </pre>\n *\n * {@code Single.toFlowable()} 会检查本接口并调用 {@link #fuseToFlowable()}，\n * 返回可能是 Flowable 专用 reduce(BiFunction) 实现的 Flowable。\n * <p>\n * 组装时略有开销（1 次 instanceof 检查、1 次算子分配、丢弃 1 个算子），\n * 但运行时不会产生转换开销。\n *\n * @param <T> 值类型\n */",
        ),
        (
            "    /**\n     * Returns a (direct) Flowable for the operator.\n     * <p>The implementation should handle the necessary RxJavaPlugins wrapping.\n     * @return the Flowable instance\n     */",
            "    /**\n     * 返回算子对应的（直接）Flowable。\n     * <p>实现应处理必要的 RxJavaPlugins 包装。\n     * @return Flowable 实例\n     */",
        ),
    ],
    "FuseToMaybe.java": [
        (
            "/**\n * Interface indicating an operator implementation can be macro-fused back to Maybe in case\n * the operator goes from Maybe to some other reactive type and then the sequence calls\n * for toMaybe again:\n * <pre>\n * {@code\n * Single<Integer> single = Maybe.just(1).isEmpty();\n * Maybe<Integer> maybe = single.toMaybe();\n * }\n * </pre>\n *\n * The {@code Single.toMaybe()} will check for this interface and call the {@link #fuseToMaybe()}\n * to return a Maybe which could be the Maybe-specific implementation of isEmpty().\n * <p>\n * This causes a slight overhead in assembly time (1 instanceof check, 1 operator allocation and 1 dropped\n * operator) but does not incur the conversion overhead at runtime.\n *\n * @param <T> the value type\n */",
            "/**\n * 表示算子实现可在从 Maybe 转为其他响应式类型后再次调用 toMaybe 时\n * 宏融合回 Maybe 的接口：\n * <pre>\n * {@code\n * Single<Integer> single = Maybe.just(1).isEmpty();\n * Maybe<Integer> maybe = single.toMaybe();\n * }\n * </pre>\n *\n * {@code Single.toMaybe()} 会检查本接口并调用 {@link #fuseToMaybe()}，\n * 返回可能是 Maybe 专用 isEmpty() 实现的 Maybe。\n * <p>\n * 组装时略有开销（1 次 instanceof 检查、1 次算子分配、丢弃 1 个算子），\n * 但运行时不会产生转换开销。\n *\n * @param <T> 值类型\n */",
        ),
        (
            "    /**\n     * Returns a (direct) Maybe for the operator.\n     * <p>The implementation should handle the necessary RxJavaPlugins wrapping.\n     * @return the Maybe instance\n     */",
            "    /**\n     * 返回算子对应的（直接）Maybe。\n     * <p>实现应处理必要的 RxJavaPlugins 包装。\n     * @return Maybe 实例\n     */",
        ),
    ],
    "FuseToObservable.java": [
        (
            "/**\n * Interface indicating an operator implementation can be macro-fused back to Observable in case\n * the operator goes from Observable to some other reactive type and then the sequence calls\n * for toObservable again:\n * <pre>\n * {@code\n * Single<Integer> single = Observable.range(1, 10).reduce((a, b) -> a + b);\n * Observable<Integer> observable = single.toObservable();\n * }\n * </pre>\n *\n * The {@code Single.toObservable()} will check for this interface and call the {@link #fuseToObservable()}\n * to return an Observable which could be the Observable-specific implementation of reduce(BiFunction).\n * <p>\n * This causes a slight overhead in assembly time (1 instanceof check, 1 operator allocation and 1 dropped\n * operator) but does not incur the conversion overhead at runtime.\n *\n * @param <T> the value type\n */",
            "/**\n * 表示算子实现可在从 Observable 转为其他响应式类型后再次调用 toObservable 时\n * 宏融合回 Observable 的接口：\n * <pre>\n * {@code\n * Single<Integer> single = Observable.range(1, 10).reduce((a, b) -> a + b);\n * Observable<Integer> observable = single.toObservable();\n * }\n * </pre>\n *\n * {@code Single.toObservable()} 会检查本接口并调用 {@link #fuseToObservable()}，\n * 返回可能是 Observable 专用 reduce(BiFunction) 实现的 Observable。\n * <p>\n * 组装时略有开销（1 次 instanceof 检查、1 次算子分配、丢弃 1 个算子），\n * 但运行时不会产生转换开销。\n *\n * @param <T> 值类型\n */",
        ),
        (
            "    /**\n     * Returns a (direct) Observable for the operator.\n     * <p>The implementation should handle the necessary RxJavaPlugins wrapping.\n     * @return the Observable instance\n     */",
            "    /**\n     * 返回算子对应的（直接）Observable。\n     * <p>实现应处理必要的 RxJavaPlugins 包装。\n     * @return Observable 实例\n     */",
        ),
    ],
    "HasUpstreamCompletableSource.java": [
        (
            "/**\n * Interface indicating the implementor has an upstream CompletableSource-like source available\n * via {@link #source()} method.\n */",
            "/**\n * 表示实现者可通过 {@link #source()} 方法提供上游 CompletableSource 类源。\n */",
        ),
        (
            "    /**\n     * Returns the upstream source of this Completable.\n     * <p>Allows discovering the chain of observables.\n     * @return the source CompletableSource\n     */",
            "    /**\n     * 返回本 Completable 的上游源。\n     * <p>用于发现 observable 链。\n     * @return 源 CompletableSource\n     */",
        ),
    ],
    "HasUpstreamMaybeSource.java": [
        (
            "/**\n * Interface indicating the implementor has an upstream MaybeSource-like source available\n * via {@link #source()} method.\n *\n * @param <T> the value type\n */",
            "/**\n * 表示实现者可通过 {@link #source()} 方法提供上游 MaybeSource 类源。\n *\n * @param <T> 值类型\n */",
        ),
        (
            "    /**\n     * Returns the upstream source of this Maybe.\n     * <p>Allows discovering the chain of observables.\n     * @return the source MaybeSource\n     */",
            "    /**\n     * 返回本 Maybe 的上游源。\n     * <p>用于发现 observable 链。\n     * @return 源 MaybeSource\n     */",
        ),
    ],
    "HasUpstreamObservableSource.java": [
        (
            "/**\n * Interface indicating the implementor has an upstream ObservableSource-like source available\n * via {@link #source()} method.\n *\n * @param <T> the value type\n */",
            "/**\n * 表示实现者可通过 {@link #source()} 方法提供上游 ObservableSource 类源。\n *\n * @param <T> 值类型\n */",
        ),
        (
            "    /**\n     * Returns the upstream source of this Observable.\n     * <p>Allows discovering the chain of observables.\n     * @return the source ObservableSource\n     */",
            "    /**\n     * 返回本 Observable 的上游源。\n     * <p>用于发现 observable 链。\n     * @return 源 ObservableSource\n     */",
        ),
    ],
    "HasUpstreamPublisher.java": [
        (
            "/**\n * Interface indicating the implementor has an upstream Publisher-like source available\n * via {@link #source()} method.\n *\n * @param <T> the value type\n */",
            "/**\n * 表示实现者可通过 {@link #source()} 方法提供上游 Publisher 类源。\n *\n * @param <T> 值类型\n */",
        ),
        (
            "    /**\n     * Returns the source Publisher.\n     * <p>\n     * This method is intended to discover the assembly\n     * graph of sequences.\n     * @return the source Publisher\n     */",
            "    /**\n     * 返回源 Publisher。\n     * <p>\n     * 本方法用于发现序列的组装图。\n     * @return 源 Publisher\n     */",
        ),
    ],
    "HasUpstreamSingleSource.java": [
        (
            "/**\n * Interface indicating the implementor has an upstream SingleSource-like source available\n * via {@link #source()} method.\n *\n * @param <T> the value type\n */",
            "/**\n * 表示实现者可通过 {@link #source()} 方法提供上游 SingleSource 类源。\n *\n * @param <T> 值类型\n */",
        ),
        (
            "    /**\n     * Returns the upstream source of this Single.\n     * <p>Allows discovering the chain of observables.\n     * @return the source SingleSource\n     */",
            "    /**\n     * 返回本 Single 的上游源。\n     * <p>用于发现 observable 链。\n     * @return 源 SingleSource\n     */",
        ),
    ],
    "HasUpstreamStreamableSource.java": [
        (
            "/**\n * Interface indicating the implementor has an upstream {@link Streamable}-like source available\n * via {@link #source()} method.\n *\n * @param <T> the value type of the sequence\n * @since 4.0.0\n */",
            "/**\n * 表示实现者可通过 {@link #source()} 方法提供上游 {@link Streamable} 类源。\n *\n * @param <T> 序列的值类型\n * @since 4.0.0\n */",
        ),
        (
            "    /**\n     * Returns the upstream source of this {@link Streamable}.\n     * <p>Allows discovering the chain of streamables.\n     * @return the source {@code Streamable}\n     */",
            "    /**\n     * 返回本 {@link Streamable} 的上游源。\n     * <p>用于发现 streamable 链。\n     * @return 源 {@code Streamable}\n     */",
        ),
    ],
    "package-info.java": [
        (
            "/**\n * Base interfaces and types for supporting operator-fusion.\n */",
            "/**\n * 支持算子融合（operator-fusion）的基础接口与类型。\n */",
        ),
    ],
}


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def apply_replacements(text: str, reps: list[tuple[str, str]]) -> str:
    for old, new in reps:
        if old not in text:
            raise ValueError(f"pattern not found: {old[:80]!r}...")
        text = text.replace(old, new)
    return text


def annotate_file(rel: str) -> None:
    name = Path(rel).name
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    if not src.exists():
        raise FileNotFoundError(f"missing original: {rel}")
    reps = FILE_REPLACEMENTS.get(name, [])
    if not reps:
        raise ValueError(f"NO_REPLACEMENTS: {rel}")
    dst.parent.mkdir(parents=True, exist_ok=True)
    if not dst.exists() or not has_chinese(dst.read_text(encoding="utf-8")):
        shutil.copy2(src, dst)
    text = dst.read_text(encoding="utf-8")
    text = apply_replacements(text, reps)
    cn = len(re.findall(r"[\u4e00-\u9fff]", text))
    lic = "Licensed under the Apache License" in text
    if cn < 10 or not lic:
        raise ValueError(f"VALIDATION cn={cn} lic={lic}: {rel}")
    dst.write_text(text, encoding="utf-8")


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        try:
            annotate_file(rel)
            ok += 1
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
