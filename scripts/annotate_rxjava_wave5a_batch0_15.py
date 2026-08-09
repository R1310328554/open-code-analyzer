#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-5a jdk8 [0:15]."""
from __future__ import annotations

import json
import re
import shutil
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "rxjava/4.0.0-alpha-21"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
WAVE5A_FILE = Path("/tmp/rxjava_w5a.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE5A_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "CompletableFromCompletionStage.java": [
        (
            "/**\n * Wrap a CompletionStage and signal its outcome.\n * @param <T> the element type of the CompletionsStage\n * @since 3.0.0\n */",
            "/**\n * 包装 {@link CompletionStage} 并将其结果作为信号发出。\n * @param <T> CompletionStage 的元素类型\n * @since 3.0.0\n */",
        ),
        (
            "        // We need an indirection because one can't detach from a whenComplete\n        // and cancellation should not hold onto the stage.",
            "        // 需要一层间接引用：无法从 whenComplete 解绑，且取消时不应继续持有 stage。",
        ),
    ],
    "CompletionStageConsumer.java": [
        (
            "/**\n * Class that extends CompletableFuture and converts multiple types of reactive consumers\n * and their signals into completion signals.\n * @param <T> the element type\n * @since 3.0.0\n */",
            "/**\n * 继承 {@link CompletableFuture} 的类，将多种响应式消费者及其信号\n * 转换为 completion 信号。\n * @param <T> 元素类型\n * @since 3.0.0\n */",
        ),
    ],
    "FlowableCollectWithCollector.java": [
        (
            "/**\n * Collect items into a container defined by a Stream {@link Collector} callback set.\n *\n * @param <T> the upstream value type\n * @param <A> the intermediate accumulator type\n * @param <R> the result type\n * @since 3.0.0\n */",
            "/**\n * 使用 Stream {@link Collector} 回调集定义容器，将上游元素收集到其中。\n *\n * @param <T> 上游值类型\n * @param <A> 中间累加器类型\n * @param <R> 结果类型\n * @since 3.0.0\n */",
        ),
    ],
    "FlowableCollectWithCollectorSingle.java": [
        (
            "/**\n * Collect items into a container defined by a Stream {@link Collector} callback set.\n *\n * @param <T> the upstream value type\n * @param <A> the intermediate accumulator type\n * @param <R> the result type\n * @since 3.0.0\n */",
            "/**\n * 使用 Stream {@link Collector} 回调集定义容器，将上游元素收集到其中。\n *\n * @param <T> 上游值类型\n * @param <A> 中间累加器类型\n * @param <R> 结果类型\n * @since 3.0.0\n */",
        ),
    ],
    "FlowableFirstStageSubscriber.java": [
        (
            "/**\n * Signals the first element of the source via the underlying CompletableFuture,\n * signals a default item if the upstream is empty or signals {@link NoSuchElementException}.\n *\n * @param <T> the element type\n * @since 3.0.0\n */",
            "/**\n * 通过底层 {@link CompletableFuture} 发出源序列的第一个元素；\n * 若上游为空则发出默认项，否则发出 {@link NoSuchElementException}。\n *\n * @param <T> 元素类型\n * @since 3.0.0\n */",
        ),
    ],
    "FlowableFlatMapStream.java": [
        (
            "/**\n * Maps the upstream values onto {@link Stream}s and emits their items in order to the downstream.\n *\n * @param <T> the upstream element type\n * @param <R> the inner {@code Stream} and result element type\n * @since 3.0.0\n */",
            "/**\n * 将上游值映射为 {@link Stream}，并按顺序向下游发出其中的元素。\n *\n * @param <T> 上游元素类型\n * @param <R> 内部 {@code Stream} 及结果元素类型\n * @since 3.0.0\n */",
        ),
        (
            "    /**\n     * Create a {@link Subscriber} with the given parameters.\n     * @param <T> the upstream value type\n     * @param <R> the {@link Stream} and output value type\n     * @param downstream the downstream {@code Subscriber} to wrap\n     * @param mapper the mapper function\n     * @param prefetch the number of items to prefetch\n     * @return the new {@code Subscriber}\n     */",
            "    /**\n     * 使用给定参数创建 {@link Subscriber}。\n     * @param <T> 上游值类型\n     * @param <R> {@link Stream} 及输出值类型\n     * @param downstream 要包装的下游 {@code Subscriber}\n     * @param mapper 映射函数\n     * @param prefetch 预取元素数量\n     * @return 新的 {@code Subscriber}\n     */",
        ),
    ],
    "FlowableFromCompletionStage.java": [
        (
            "/**\n * Wrap a CompletionStage and signal its outcome.\n * @param <T> the element type\n * @since 3.0.0\n */",
            "/**\n * 包装 {@link CompletionStage} 并将其结果作为信号发出。\n * @param <T> 元素类型\n * @since 3.0.0\n */",
        ),
        (
            "        // We need an indirection because one can't detach from a whenComplete\n        // and cancellation should not hold onto the stage.",
            "        // 需要一层间接引用：无法从 whenComplete 解绑，且取消时不应继续持有 stage。",
        ),
    ],
    "FlowableFromStream.java": [
        (
            "/**\n * Wraps a {@link Stream} and emits its values as a Flowable sequence.\n * @param <T> the element type of the Stream\n * @since 3.0.0\n */",
            "/**\n * 包装 {@link Stream}，将其元素作为 Flowable 序列发出。\n * @param <T> Stream 的元素类型\n * @since 3.0.0\n */",
        ),
        (
            "    /**\n     * Subscribes to the Stream by picking the normal or conditional stream Subscription implementation.\n     * @param <T> the element type of the flow\n     * @param s the subscriber to drive\n     * @param stream the sequence to consume\n     */",
            "    /**\n     * 订阅 Stream，根据下游类型选择普通或条件式 Subscription 实现。\n     * @param <T> 流的元素类型\n     * @param s 要驱动的 subscriber\n     * @param stream 要消费的序列\n     */",
        ),
    ],
    "FlowableLastStageSubscriber.java": [
        (
            "/**\n * Signals the last element of the source via the underlying CompletableFuture,\n * signals a default item if the upstream is empty or signals {@link NoSuchElementException}.\n *\n * @param <T> the element type\n * @since 3.0.0\n */",
            "/**\n * 通过底层 {@link CompletableFuture} 发出源序列的最后一个元素；\n * 若上游为空则发出默认项，否则发出 {@link NoSuchElementException}。\n *\n * @param <T> 元素类型\n * @since 3.0.0\n */",
        ),
    ],
    "FlowableMapOptional.java": [
        (
            "/**\n * Map the upstream values into an Optional and emit its value if any.\n * @param <T> the upstream element type\n * @param <R> the output element type\n * @since 3.0.0\n */",
            "/**\n * 将上游值映射为 {@link Optional}，若存在值则向下游发出。\n * @param <T> 上游元素类型\n * @param <R> 输出元素类型\n * @since 3.0.0\n */",
        ),
    ],
    "FlowableSingleStageSubscriber.java": [
        (
            "/**\n * Signals the only element of the source via the underlying CompletableFuture,\n * signals a default item if the upstream is empty or signals {@link IllegalArgumentException}\n * if the upstream has more than one item.\n *\n * @param <T> the element type\n * @since 3.0.0\n */",
            "/**\n * 通过底层 {@link CompletableFuture} 发出源序列的唯一元素；\n * 若上游为空则发出默认项；若上游有多于一个元素则发出 {@link IllegalArgumentException}。\n *\n * @param <T> 元素类型\n * @since 3.0.0\n */",
        ),
    ],
    "FlowableStageSubscriber.java": [
        (
            "/**\n * Base class that extends CompletableFuture and provides basic infrastructure\n * to notify watchers upon upstream signals.\n * @param <T> the element type\n * @since 3.0.0\n */",
            "/**\n * 继承 {@link CompletableFuture} 的基类，提供在上游信号到达时\n * 通知等待者的基础基础设施。\n * @param <T> 元素类型\n * @since 3.0.0\n */",
        ),
    ],
    "MaybeFlattenStreamAsFlowable.java": [
        (
            "/**\n * Map the success value into a Java {@link Stream} and emits its values.\n *\n * @param <T> the source value type\n * @param <R> the output value type\n * @since 3.0.0\n */",
            "/**\n * 将成功值映射为 Java {@link Stream} 并发出其中的元素。\n *\n * @param <T> 源值类型\n * @param <R> 输出值类型\n * @since 3.0.0\n */",
        ),
    ],
    "MaybeFlattenStreamAsObservable.java": [
        (
            "/**\n * Map the success value into a Java {@link Stream} and emits its values.\n *\n * @param <T> the source value type\n * @param <R> the output value type\n * @since 3.0.0\n */",
            "/**\n * 将成功值映射为 Java {@link Stream} 并发出其中的元素。\n *\n * @param <T> 源值类型\n * @param <R> 输出值类型\n * @since 3.0.0\n */",
        ),
    ],
    "MaybeFromCompletionStage.java": [
        (
            "/**\n * Wrap a CompletionStage and signal its outcome.\n * @param <T> the element type\n * @since 3.0.0\n */",
            "/**\n * 包装 {@link CompletionStage} 并将其结果作为信号发出。\n * @param <T> 元素类型\n * @since 3.0.0\n */",
        ),
        (
            "        // We need an indirection because one can't detach from a whenComplete\n        // and cancellation should not hold onto the stage.",
            "        // 需要一层间接引用：无法从 whenComplete 解绑，且取消时不应继续持有 stage。",
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
