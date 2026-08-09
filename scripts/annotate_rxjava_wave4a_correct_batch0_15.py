#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-4a [0:15] — exceptions/functions slice."""
from __future__ import annotations

import json
import re
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "rxjava/4.0.0-alpha-21"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
WAVE4A_FILE = Path("/tmp/rx_w4a.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE4A_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "src/main/java/io/reactivex/rxjava4/exceptions/QueueOverflowException.java": [
        (
            "/**\n * Indicates an overflow happened because the upstream disregarded backpressure completely or\n * {@link java.util.concurrent.Flow.Subscriber#onNext(Object)} was called concurrently from multiple threads\n * without synchronization. Rarely, it is an indication of bugs inside an operator.\n * @since 3.1.6\n */",
            "/**\n * 表示因上游完全无视背压，或从多个线程并发、未同步地调用\n * {@link java.util.concurrent.Flow.Subscriber#onNext(Object)} 而发生队列溢出。\n * 极少数情况下表示算子内部存在 bug。\n * @since 3.1.6\n */",
        ),
        (
            "    /**\n     * The message for queue overflows.\n     * <p>\n     * This can happen if the upstream disregards backpressure completely or calls\n     * {@link java.util.concurrent.Flow.Subscriber#onNext(Object)} concurrently from multiple threads\n     * without synchronization. Rarely, it is an indication of bugs inside an operator.\n     */",
            "    /**\n     * 队列溢出时的默认消息。\n     * <p>\n     * 若上游完全无视背压，或从多个线程并发、未同步地调用\n     * {@link java.util.concurrent.Flow.Subscriber#onNext(Object)} 可能发生此情况。\n     * 极少数情况下表示算子内部存在 bug。\n     */",
        ),
        (
            "    /**\n     * Constructs a QueueOverflowException with the default message.\n     */",
            "    /**\n     * 使用默认消息构造 QueueOverflowException。\n     */",
        ),
        (
            "    /**\n     * Constructs a QueueOverflowException with the given message but no cause.\n     * @param message the error message\n     */",
            "    /**\n     * 使用给定消息、无 cause 构造 QueueOverflowException。\n     * @param message 错误消息\n     */",
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/exceptions/UndeliverableException.java": [
        (
            "/**\n * Wrapper for Throwable errors that are sent to {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable) RxJavaPlugins.onError}.\n * <p>History: 2.0.6 - experimental; 2.1 - beta\n * @since 2.2\n */",
            "/**\n * 发往 {@link io.reactivex.rxjava4.plugins.RxJavaPlugins#onError(Throwable) RxJavaPlugins.onError} 的\n * Throwable 错误包装类。\n * <p>History: 2.0.6 - experimental; 2.1 - beta\n * @since 2.2\n */",
        ),
        (
            "    /**\n     * Construct an instance by wrapping the given, non-null\n     * cause Throwable.\n     * @param cause the cause, not null\n     */",
            "    /**\n     * 通过包装给定、非 null 的 cause Throwable 构造实例。\n     * @param cause 原因，不可为 null\n     */",
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/exceptions/package-info.java": [
        (
            "/**\n * Exception handling utilities ({@link io.reactivex.rxjava4.exceptions.Exceptions Exceptions}),\n * composite exception container ({@link io.reactivex.rxjava4.exceptions.CompositeException CompositeException}) and\n * various lifecycle-related ({@link io.reactivex.rxjava4.exceptions.MissingBackpressureException UndeliverableException})\n * and behavior-violation exception types ({@link io.reactivex.rxjava4.exceptions.OnErrorNotImplementedException OnErrorNotImplementedException},\n * {@link io.reactivex.rxjava4.exceptions.MissingBackpressureException MissingBackpressureException}).\n */",
            "/**\n * 异常处理工具（{@link io.reactivex.rxjava4.exceptions.Exceptions Exceptions}）、\n * 复合异常容器（{@link io.reactivex.rxjava4.exceptions.CompositeException CompositeException}），\n * 以及各类生命周期相关（{@link io.reactivex.rxjava4.exceptions.MissingBackpressureException UndeliverableException}）\n * 与行为违规异常类型（{@link io.reactivex.rxjava4.exceptions.OnErrorNotImplementedException OnErrorNotImplementedException}、\n * {@link io.reactivex.rxjava4.exceptions.MissingBackpressureException MissingBackpressureException}）。\n */",
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/functions/Action.java": [
        (
            "/**\n * A functional interface similar to Runnable but allows throwing a checked exception.\n */",
            "/**\n * 类似 {@link Runnable} 的函数式接口，但允许抛出受检异常。\n */",
        ),
        (
            "    /**\n     * Runs the action and optionally throws a checked exception.\n     * @throws Throwable if the implementation wishes to throw any type of exception\n     */",
            "    /**\n     * 执行动作，并可选择抛出受检异常。\n     * @throws Throwable 若实现需要可抛出任意类型的异常\n     */",
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/functions/BiConsumer.java": [
        (
            "/**\n * A functional interface (callback) that accepts two values (of possibly different types).\n * @param <T1> the first value type\n * @param <T2> the second value type\n */",
            "/**\n * 接受两个值（可能类型不同）的函数式接口（回调）。\n * @param <T1> 第一个值的类型\n * @param <T2> 第二个值的类型\n */",
        ),
        (
            "    /**\n     * Performs an operation on the given values.\n     * @param t1 the first value\n     * @param t2 the second value\n     * @throws Throwable if the implementation wishes to throw any type of exception\n     */",
            "    /**\n     * 对给定值执行操作。\n     * @param t1 第一个值\n     * @param t2 第二个值\n     * @throws Throwable 若实现需要可抛出任意类型的异常\n     */",
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/functions/BiFunction.java": [
        (
            "/**\n * A functional interface (callback) that computes a value based on multiple input values.\n * @param <T1> the first value type\n * @param <T2> the second value type\n * @param <R> the result type\n */",
            "/**\n * 根据多个输入值计算结果的函数式接口（回调）。\n * @param <T1> 第一个值的类型\n * @param <T2> 第二个值的类型\n * @param <R> 结果类型\n */",
        ),
        (
            "    /**\n     * Calculate a value based on the input values.\n     * @param t1 the first value\n     * @param t2 the second value\n     * @return the result value\n     * @throws Throwable if the implementation wishes to throw any type of exception\n     */",
            "    /**\n     * 根据输入值计算结果。\n     * @param t1 第一个值\n     * @param t2 第二个值\n     * @return 计算结果\n     * @throws Throwable 若实现需要可抛出任意类型的异常\n     */",
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/functions/BiPredicate.java": [
        (
            "/**\n * A functional interface (callback) that returns true or false for the given input values.\n * @param <T1> the first value\n * @param <T2> the second value\n */",
            "/**\n * 对给定输入值返回 true 或 false 的函数式接口（回调）。\n * @param <T1> 第一个值\n * @param <T2> 第二个值\n */",
        ),
        (
            "    /**\n     * Test the given input values and return a boolean.\n     * @param t1 the first value\n     * @param t2 the second value\n     * @return the boolean result\n     * @throws Throwable if the implementation wishes to throw any type of exception\n     */",
            "    /**\n     * 测试给定输入值并返回布尔结果。\n     * @param t1 第一个值\n     * @param t2 第二个值\n     * @return 布尔结果\n     * @throws Throwable 若实现需要可抛出任意类型的异常\n     */",
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/functions/BooleanSupplier.java": [
        (
            "/**\n * A functional interface (callback) that returns a boolean value.\n */",
            "/**\n * 返回布尔值的函数式接口（回调）。\n */",
        ),
        (
            "    /**\n     * Returns a boolean value.\n     * @return a boolean value\n     * @throws Throwable if the implementation wishes to throw any type of exception\n     */",
            "    /**\n     * 返回布尔值。\n     * @return 布尔值\n     * @throws Throwable 若实现需要可抛出任意类型的异常\n     */",
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/functions/Cancellable.java": [
        (
            "/**\n * A functional interface that has a single cancel method\n * that can throw.\n */",
            "/**\n * 具有单个可抛出异常的 cancel 方法的函数式接口。\n */",
        ),
        (
            "    /**\n     * Cancel the action or free a resource.\n     * @throws Throwable if the implementation wishes to throw any type of exception\n     */",
            "    /**\n     * 取消动作或释放资源。\n     * @throws Throwable 若实现需要可抛出任意类型的异常\n     */",
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/functions/Consumer.java": [
        (
            "/**\n * A functional interface (callback) that accepts a single value.\n * @param <T> the value type\n */",
            "/**\n * 接受单个值的函数式接口（回调）。\n * @param <T> 值类型\n */",
        ),
        (
            "    /**\n     * Consume the given value.\n     * @param t the value\n     * @throws Throwable if the implementation wishes to throw any type of exception\n     */",
            "    /**\n     * 消费给定值。\n     * @param t 值\n     * @throws Throwable 若实现需要可抛出任意类型的异常\n     */",
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/functions/Consumer3.java": [
        (
            "/**\n * A functional interface (callback) that accepts two values (of possibly different types).\n * @param <T1> the first value type\n * @param <T2> the second value type\n * @param <T3> the third value type\n * @since 4.0.0\n */",
            "/**\n * 接受三个值（可能类型不同）的函数式接口（回调）。\n * @param <T1> 第一个值的类型\n * @param <T2> 第二个值的类型\n * @param <T3> 第三个值的类型\n * @since 4.0.0\n */",
        ),
        (
            "    /**\n     * Performs an operation on the given values.\n     * @param t1 the first value\n     * @param t2 the second value\n     * @param t3 the third value\n     * @throws Throwable if the implementation wishes to throw any type of exception\n     */",
            "    /**\n     * 对给定值执行操作。\n     * @param t1 第一个值\n     * @param t2 第二个值\n     * @param t3 第三个值\n     * @throws Throwable 若实现需要可抛出任意类型的异常\n     */",
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/functions/Function.java": [
        (
            "/**\n * A functional interface that takes a value and returns another value, possibly with a\n * different type and allows throwing a checked exception.\n *\n * @param <T> the input value type\n * @param <R> the output value type\n */",
            "/**\n * 接收一个值并返回另一个值（可能类型不同）的函数式接口，允许抛出受检异常。\n *\n * @param <T> 输入值类型\n * @param <R> 输出值类型\n */",
        ),
        (
            "    /**\n     * Apply some calculation to the input value and return some other value.\n     * @param t the input value\n     * @return the output value\n     * @throws Throwable if the implementation wishes to throw any type of exception\n     */",
            "    /**\n     * 对输入值进行计算并返回结果值。\n     * @param t 输入值\n     * @return 输出值\n     * @throws Throwable 若实现需要可抛出任意类型的异常\n     */",
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/functions/Function3.java": [
        (
            "/**\n * A functional interface (callback) that computes a value based on multiple input values.\n * @param <T1> the first value type\n * @param <T2> the second value type\n * @param <T3> the third value type\n * @param <R> the result type\n */",
            "/**\n * 根据多个输入值计算结果的函数式接口（回调）。\n * @param <T1> 第一个值的类型\n * @param <T2> 第二个值的类型\n * @param <T3> 第三个值的类型\n * @param <R> 结果类型\n */",
        ),
        (
            "    /**\n     * Calculate a value based on the input values.\n     * @param t1 the first value\n     * @param t2 the second value\n     * @param t3 the third value\n     * @return the result value\n     * @throws Throwable if the implementation wishes to throw any type of exception\n     */",
            "    /**\n     * 根据输入值计算结果。\n     * @param t1 第一个值\n     * @param t2 第二个值\n     * @param t3 第三个值\n     * @return 计算结果\n     * @throws Throwable 若实现需要可抛出任意类型的异常\n     */",
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/functions/Function4.java": [
        (
            "/**\n * A functional interface (callback) that computes a value based on multiple input values.\n * @param <T1> the first value type\n * @param <T2> the second value type\n * @param <T3> the third value type\n * @param <T4> the fourth value type\n * @param <R> the result type\n */",
            "/**\n * 根据多个输入值计算结果的函数式接口（回调）。\n * @param <T1> 第一个值的类型\n * @param <T2> 第二个值的类型\n * @param <T3> 第三个值的类型\n * @param <T4> 第四个值的类型\n * @param <R> 结果类型\n */",
        ),
        (
            "    /**\n     * Calculate a value based on the input values.\n     * @param t1 the first value\n     * @param t2 the second value\n     * @param t3 the third value\n     * @param t4 the fourth value\n     * @return the result value\n     * @throws Throwable if the implementation wishes to throw any type of exception\n     */",
            "    /**\n     * 根据输入值计算结果。\n     * @param t1 第一个值\n     * @param t2 第二个值\n     * @param t3 第三个值\n     * @param t4 第四个值\n     * @return 计算结果\n     * @throws Throwable 若实现需要可抛出任意类型的异常\n     */",
        ),
    ],
    "src/main/java/io/reactivex/rxjava4/functions/Function5.java": [
        (
            "/**\n * A functional interface (callback) that computes a value based on multiple input values.\n * @param <T1> the first value type\n * @param <T2> the second value type\n * @param <T3> the third value type\n * @param <T4> the fourth value type\n * @param <T5> the fifth value type\n * @param <R> the result type\n */",
            "/**\n * 根据多个输入值计算结果的函数式接口（回调）。\n * @param <T1> 第一个值的类型\n * @param <T2> 第二个值的类型\n * @param <T3> 第三个值的类型\n * @param <T4> 第四个值的类型\n * @param <T5> 第五个值的类型\n * @param <R> 结果类型\n */",
        ),
        (
            "    /**\n     * Calculate a value based on the input values.\n     * @param t1 the first value\n     * @param t2 the second value\n     * @param t3 the third value\n     * @param t4 the fourth value\n     * @param t5 the fifth value\n     * @return the result value\n     * @throws Throwable if the implementation wishes to throw any type of exception\n     */",
            "    /**\n     * 根据输入值计算结果。\n     * @param t1 第一个值\n     * @param t2 第二个值\n     * @param t3 第三个值\n     * @param t4 第四个值\n     * @param t5 第五个值\n     * @return 计算结果\n     * @throws Throwable 若实现需要可抛出任意类型的异常\n     */",
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
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    if not src.exists():
        raise FileNotFoundError(f"missing original: {rel}")
    reps = FILE_REPLACEMENTS.get(rel, [])
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
