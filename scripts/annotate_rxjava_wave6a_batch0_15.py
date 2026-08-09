#!/usr/bin/env python3
"""Chinese-annotate RxJava 4.0.0-alpha-21 wave-6a jdk8/observers [0:15]."""
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
WAVE6A_FILE = Path("/tmp/rxjava_w6a.txt")
BATCH_FILES = [
    ln.strip()
    for ln in WAVE6A_FILE.read_text(encoding="utf-8").splitlines()
    if ln.strip()
]

FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {
    "SingleFlattenStreamAsFlowable.java": [
        (
            "/**\n * Map the success value into a Java {@link Stream} and emits its values.\n *\n * @param <T> the source value type\n * @param <R> the output value type\n * @since 3.0.0\n */",
            "/**\n * 将成功值映射为 Java {@link Stream} 并发出其中的元素。\n *\n * @param <T> 源值类型\n * @param <R> 输出值类型\n * @since 3.0.0\n */",
        ),
    ],
    "SingleFlattenStreamAsObservable.java": [
        (
            "/**\n * Map the success value into a Java {@link Stream} and emits its values.\n *\n * @param <T> the source value type\n * @param <R> the output value type\n * @since 3.0.0\n */",
            "/**\n * 将成功值映射为 Java {@link Stream} 并发出其中的元素。\n *\n * @param <T> 源值类型\n * @param <R> 输出值类型\n * @since 3.0.0\n */",
        ),
    ],
    "SingleFromCompletionStage.java": [
        (
            "/**\n * Wrap a CompletionStage and signal its outcome.\n * @param <T> the element type\n * @since 3.0.0\n */",
            "/**\n * 包装 {@link CompletionStage} 并将其结果作为信号发出。\n * @param <T> 元素类型\n * @since 3.0.0\n */",
        ),
        (
            "        // We need an indirection because one can't detach from a whenComplete\n        // and cancellation should not hold onto the stage.",
            "        // 需要一层间接引用：无法从 whenComplete 解绑，且取消时不应继续持有 stage。",
        ),
    ],
    "SingleMapOptional.java": [
        (
            "/**\n * Maps the success value to an {@link Optional} and emits its non-empty value or completes.\n *\n * @param <T> the upstream success value type\n * @param <R> the result value type\n * @since 3.0.0\n */",
            "/**\n * 将成功值映射为 {@link Optional}，若存在非空值则向下游发出，否则完成。\n *\n * @param <T> 上游成功值类型\n * @param <R> 结果值类型\n * @since 3.0.0\n */",
        ),
    ],
    "AbstractDisposableAutoRelease.java": [
        (
            "/**\n * Wraps lambda callbacks and when the upstream terminates or the observer gets disposed,\n * removes itself from a {@link io.reactivex.rxjava4.disposables.CompositeDisposable}.\n * <p>History: 0.18.0 @ RxJavaExtensions\n * @since 3.1.0\n */",
            "/**\n * 包装 lambda 回调；上游终止或 observer 被 dispose 时，\n * 从 {@link io.reactivex.rxjava4.disposables.CompositeDisposable} 中移除自身。\n * <p>History: 0.18.0 @ RxJavaExtensions\n * @since 3.1.0\n */",
        ),
    ],
    "BasicFuseableObserver.java": [
        (
            "/**\n * Base class for a fuseable intermediate observer.\n * @param <T> the upstream value type\n * @param <R> the downstream value type\n */",
            "/**\n * 可融合（fuseable）中间 observer 的基类。\n * @param <T> 上游值类型\n * @param <R> 下游值类型\n */",
        ),
        (
            "    /** The downstream subscriber. */",
            "    /** 下游 subscriber。 */",
        ),
        (
            "    /** The upstream subscription. */",
            "    /** 上游 subscription。 */",
        ),
        (
            "    /** The upstream's QueueDisposable if not null. */",
            "    /** 上游的 QueueDisposable（非 null 时）。 */",
        ),
        (
            "    /** Flag indicating no further onXXX event should be accepted. */",
            "    /** 标志：不再接受 onXXX 事件。 */",
        ),
        (
            "    /** Holds the established fusion mode of the upstream. */",
            "    /** 保存上游已建立的融合模式。 */",
        ),
        (
            "    /**\n     * Construct a BasicFuseableObserver by wrapping the given subscriber.\n     * @param downstream the subscriber, not null (not verified)\n     */",
            "    /**\n     * 通过包装给定 subscriber 构造 BasicFuseableObserver。\n     * @param downstream subscriber，不可为 null（未校验）\n     */",
        ),
        (
            "    /**\n     * Override this to perform actions before the call {@code actual.onSubscribe(this)} happens.\n     * @return true if onSubscribe should continue with the call\n     */",
            "    /**\n     * 在调用 {@code actual.onSubscribe(this)} 之前执行操作，可覆盖本方法。\n     * @return 若应继续 onSubscribe 调用则为 true\n     */",
        ),
        (
            "    /**\n     * Override this to perform actions after the call to {@code actual.onSubscribe(this)} happened.\n     */",
            "    /**\n     * 在 {@code actual.onSubscribe(this)} 调用完成后执行操作，可覆盖本方法。\n     */",
        ),
        (
            "    /**\n     * Rethrows the throwable if it is a fatal exception or calls {@link #onError(Throwable)}.\n     * @param t the throwable to rethrow or signal to the actual subscriber\n     */",
            "    /**\n     * 若为致命异常则重新抛出，否则调用 {@link #onError(Throwable)}。\n     * @param t 要重新抛出或向实际 subscriber 发出的异常\n     */",
        ),
        (
            "    /**\n     * Calls the upstream's QueueDisposable.requestFusion with the mode and\n     * saves the established mode in {@link #sourceMode} if that mode doesn't\n     * have the {@link QueueDisposable#BOUNDARY} flag set.\n     * <p>\n     * If the upstream doesn't support fusion ({@link #qd} is null), the method\n     * returns {@link QueueDisposable#NONE}.\n     * @param mode the fusion mode requested\n     * @return the established fusion mode\n     */",
            "    /**\n     * 以给定 mode 调用上游 QueueDisposable.requestFusion，\n     * 若该 mode 未设置 {@link QueueDisposable#BOUNDARY} 标志，\n     * 则将已建立的模式保存到 {@link #sourceMode}。\n     * <p>\n     * 若上游不支持融合（{@link #qd} 为 null），返回 {@link QueueDisposable#NONE}。\n     * @param mode 请求的融合模式\n     * @return 已建立的融合模式\n     */",
        ),
    ],
    "BasicIntQueueDisposable.java": [
        (
            "/**\n * An abstract QueueDisposable implementation, extending an AtomicInteger,\n * that defaults all unnecessary Queue methods to throw UnsupportedOperationException.\n * @param <T> the output value type\n */",
            "/**\n * 继承 AtomicInteger 的抽象 QueueDisposable 实现，\n * 将所有不必要的 Queue 方法默认实现为抛出 UnsupportedOperationException。\n * @param <T> 输出值类型\n */",
        ),
    ],
    "BasicQueueDisposable.java": [
        (
            "/**\n * An abstract QueueDisposable implementation that defaults all\n * unnecessary Queue methods to throw UnsupportedOperationException.\n * @param <T> the output value type\n */",
            "/**\n * 抽象 QueueDisposable 实现，\n * 将所有不必要的 Queue 方法默认实现为抛出 UnsupportedOperationException。\n * @param <T> 输出值类型\n */",
        ),
    ],
    "BiConsumerSingleObserver.java": [
        (
            "public final class BiConsumerSingleObserver<T>",
            "/**\n * 将成功或错误结果转发给 {@link BiConsumer} 的 SingleObserver。\n *\n * @param <T> 值类型\n */\npublic final class BiConsumerSingleObserver<T>",
        ),
    ],
    "BlockingBaseObserver.java": [
        (
            "public abstract class BlockingBaseObserver<T> extends CountDownLatch",
            "/**\n * 使用 {@link CountDownLatch} 等待终止的阻塞 observer 基类。\n *\n * @param <T> 值类型\n */\npublic abstract class BlockingBaseObserver<T> extends CountDownLatch",
        ),
        (
            "    /**\n     * Block until the first value arrives and return it, otherwise\n     * return null for an empty source and rethrow any exception.\n     * @return the first value or null if the source is empty\n     */",
            "    /**\n     * 阻塞直到首个值到达并返回；若源为空则返回 null，\n     * 若有异常则重新抛出。\n     * @return 首个值，或源为空时返回 null\n     */",
        ),
    ],
    "BlockingDisposableMultiObserver.java": [
        (
            "/**\n * Blocks until the upstream terminates and dispatches the outcome to\n * the actual observer.\n *\n * @param <T> the element type of the source\n * @since 3.0.0\n */",
            "/**\n * 阻塞直到上游终止，并将结果分发给实际 observer。\n *\n * @param <T> 源的元素类型\n * @since 3.0.0\n */",
        ),
    ],
    "BlockingFirstObserver.java": [
        (
            "/**\n * Blocks until the upstream signals its first value or completes.\n *\n * @param <T> the value type\n */",
            "/**\n * 阻塞直到上游发出首个值或完成。\n *\n * @param <T> 值类型\n */",
        ),
    ],
    "BlockingLastObserver.java": [
        (
            "/**\n * Blocks until the upstream signals its last value or completes.\n *\n * @param <T> the value type\n */",
            "/**\n * 阻塞直到上游发出最后一个值或完成。\n *\n * @param <T> 值类型\n */",
        ),
    ],
    "BlockingMultiObserver.java": [
        (
            "/**\n * A combined Observer that awaits the success or error signal via a CountDownLatch.\n * @param <T> the value type\n */",
            "/**\n * 通过 CountDownLatch 等待成功或错误信号的合并 Observer。\n * @param <T> 值类型\n */",
        ),
        (
            "    /**\n     * Block until the latch is counted down then rethrow any exception received (wrapped if checked)\n     * or return the received value (null if none).\n     * @return the value received or null if no value received\n     */",
            "    /**\n     * 阻塞直到 latch 计数归零，然后重新抛出收到的异常（checked 异常会包装），\n     * 或返回收到的值（无值时为 null）。\n     * @return 收到的值，无值时返回 null\n     */",
        ),
        (
            "    /**\n     * Block until the latch is counted down then rethrow any exception received (wrapped if checked)\n     * or return the received value (the defaultValue if none).\n     * @param defaultValue the default value to return if no value was received\n     * @return the value received or defaultValue if no value received\n     */",
            "    /**\n     * 阻塞直到 latch 计数归零，然后重新抛出收到的异常（checked 异常会包装），\n     * 或返回收到的值（无值时返回 defaultValue）。\n     * @param defaultValue 未收到值时的默认返回值\n     * @return 收到的值，无值时返回 defaultValue\n     */",
        ),
        (
            "    /**\n     * Block until the observer terminates and return true; return false if\n     * the wait times out.\n     * @param timeout the timeout value\n     * @param unit the time unit\n     * @return true if the observer terminated in time, false otherwise\n     */",
            "    /**\n     * 阻塞直到 observer 终止并返回 true；若等待超时则返回 false。\n     * @param timeout 超时值\n     * @param unit 时间单位\n     * @return observer 及时终止则为 true，否则为 false\n     */",
        ),
        (
            "    /**\n     * Blocks until the source completes and calls the appropriate callback.\n     * @param onSuccess for a succeeding source\n     * @param onError for a failing source\n     * @param onComplete for an empty source\n     */",
            "    /**\n     * 阻塞直到源完成并调用相应回调。\n     * @param onSuccess 源成功时的回调\n     * @param onError 源失败时的回调\n     * @param onComplete 源为空时的回调\n     */",
        ),
    ],
    "BlockingObserver.java": [
        (
            "public final class BlockingObserver<T> extends AtomicReference<Disposable> implements Observer<T>, Disposable {",
            "/**\n * 将通知入队到阻塞 {@link Queue} 的 Observer。\n *\n * @param <T> 值类型\n */\npublic final class BlockingObserver<T> extends AtomicReference<Disposable> implements Observer<T>, Disposable {",
        ),
    ],
}


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def apply_replacements(text: str, reps: list[tuple[str, str]]) -> str:
    for old, new in reps:
        if old not in text:
            raise ValueError(f"pattern not found: {old[:80]!r}...")
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
    if not failures:
        mark_queue_done(BATCH_FILES)
        print(f"Marked {len(BATCH_FILES)} wave-6a files done in queue")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False, indent=2))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
