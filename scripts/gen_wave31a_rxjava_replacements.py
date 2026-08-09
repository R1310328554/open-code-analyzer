#!/usr/bin/env python3
"""Generate wave31a_replacements_rxjava_mega.py from original sources."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path("/workspace")
ORIGINAL = ROOT / "rxjava/4.0.0-alpha-21/original"
OUT = ROOT / "scripts/wave31a_replacements_rxjava_mega.py"
BATCH = Path("/tmp/rxjava_w31a.txt")

FILE_SUMMARIES: dict[str, str] = {
    "Scheduler.java": (
        "RxJava 调度器抽象：定义 Runnable 的立即/延迟/周期调度 API，"
        "Worker 隔离顺序执行，支持 clock drift 与 RxJavaPlugins 钩子。"
    ),
    "Streamable.java": (
        "RxJava 4 核心流式 API：基于 CompletionStage 的异步 pull 模型，"
        "定义 next/finish/cancel 与多种算子工厂方法。"
    ),
    "Functions.java": (
        "内部函数工具类：提供 emptyConsumer、identity、hashCode 等"
        "常用 Function/Predicate/Action 单例与组合辅助。"
    ),
    "FlowableBuffer.java": (
        "按固定计数 size 或 skip 将上游元素收集到 Collection 后批量发射。"
    ),
    "FlowableBufferBoundary.java": (
        "由 boundary Publisher 的开/关信号界定缓冲窗口，"
        "窗口关闭时将 Collection 批量发射。"
    ),
    "FlowableBufferTimed.java": (
        "按 timespan/timeskip 定时收集元素到 Collection 并批量发射，"
        "支持 maxSize 与 Scheduler。"
    ),
    "FlowableCache.java": (
        "自动连接上游并缓存事件，支持多播 replay 与主动终止连接。"
    ),
    "FlowableCombineLatest.java": (
        "并行订阅多路 Publisher，任一路有新值时用 combiner 合并最新快照发射。"
    ),
    "FlowableConcatMap.java": (
        "对每个上游元素映射 inner Publisher 并顺序串联订阅，"
        "前一 inner 完成后再订阅下一 inner。"
    ),
    "FlowableConcatMapScheduler.java": (
        "ConcatMap 的 Scheduler 变体：inner 订阅与事件在指定 Scheduler 上调度。"
    ),
    "FlowableCreate.java": (
        "通过 FlowableOnSubscribe 回调创建 Flowable，"
        "由创建者在 subscribe 时驱动 onNext/onError/onComplete。"
    ),
    "FlowableFlatMap.java": (
        "对每个上游元素映射 inner Publisher 并并发订阅多路 inner，"
        "合并 emission 并处理背压与 delayError。"
    ),
    "FlowableFlatMapMaybe.java": (
        "对每个上游元素映射 MaybeSource，"
        "onSuccess 值可用即向下游发射，支持 maxConcurrency。"
    ),
    "FlowableFlattenIterable.java": (
        "将上游每个元素经 mapper 转为 Iterable 后逐元素展开发射。"
    ),
    "FlowableFromIterable.java": (
        "从 Iterable 同步或异步拉取元素创建 Flowable，"
        "支持 backpressure 与 fusion 优化。"
    ),
}

CLASS_SUMMARIES: dict[str, str] = {
    "Scheduler": "异步边界抽象：createWorker/scheduleDirect/now 等调度 API。",
    "Streamable": "CompletionStage 驱动的流式 pull 接口与算子入口。",
    "Functions": "静态工具方法与常用函数式接口单例。",
    "FlowableBuffer": "BufferExact/Skip/Overlap 等缓冲订阅者实现。",
    "FlowableBufferBoundary": "BoundarySubscriber 管理开/关边界与 buffer 发射。",
    "FlowableBufferTimed": "TimedBufferSubscriber 管理 Scheduler 定时与 buffer。",
    "FlowableCache": "Multicaster 链表缓存事件并向新 Subscriber replay。",
    "FlowableCombineLatest": "CombineLatestCoordinator 协调多路 request/onNext。",
    "FlowableConcatMap": "ConcatMapSubscriber 顺序切换 inner Publisher。",
    "FlowableConcatMapScheduler": "在 Worker 上调度 inner 订阅与 drain。",
    "FlowableCreate": "CreateSubscriber 包装 FlowableEmitter 与背压。",
    "FlowableFlatMap": "FlatMapSubscriber 管理 inner 队列与 maxConcurrency。",
    "FlowableFlatMapMaybe": "FlatMapMaybeSubscriber 合并多路 Maybe 结果。",
    "FlowableFlattenIterable": "FlattenIterableSubscriber 迭代展开 Iterable。",
    "FlowableFromIterable": "FromIterableSubscription 从 Iterable 拉取并 request。",
}

JAVADOC_TRANSLATIONS: list[tuple[str, str]] = [
    (r"@param <(\w+)> the (\w+) type", r"@param <\1> \2 类型"),
    (r"@param <(\w+)> the upstream (\w+) type", r"@param <\1> 上游 \2 类型"),
    (r"@param <(\w+)> the downstream (\w+) type", r"@param <\1> 下游 \2 类型"),
    (r"@param <(\w+)> the (\w+) element type", r"@param <\1> \2 元素类型"),
    (r"@param <(\w+)> the element type", r"@param <\1> 元素类型"),
    (r"@param <(\w+)> the value type", r"@param <\1> 值类型"),
    (r"@param <(\w+)> the item type", r"@param <\1> 元素类型"),
    (r"@param <(\w+)> the result type", r"@param <\1> 结果类型"),
    (r"@return the new Flowable instance", r"@return 新的 Flowable 实例"),
    (r"@return the new Observable instance", r"@return 新的 Observable 实例"),
    (r"A {@code Scheduler} is an object that specifies an API for scheduling",
     "Scheduler 定义 Runnable 的调度 API："),
    (r"Creates a worker that runs tasks in an isolated, sequential fashion\.",
     "创建 Worker，以隔离、顺序方式执行任务。"),
    (r"Returns the 'current time' of the {@code Worker} in the specified time unit\.",
     "以指定时间单位返回 Worker 的「当前时间」。"),
    (r"Indicates whether this Scheduler supports periodic execution\.",
     "本 Scheduler 是否支持周期调度。"),
    (r"Collects items from the upstream Flowable into a Collection and emits",
     "将上游 Flowable 元素收集到 Collection 并批量发射。"),
    (r"Buffers items from the upstream Flowable using a boundary Publisher",
     "以 boundary Publisher 的开/关信号界定缓冲窗口。"),
    (r"Buffers items from the upstream Flowable into a Collection and emits",
     "定时将上游元素收集到 Collection 并批量发射。"),
    (r"An observable which auto-connects to another observable, caches the elements",
     "自动连接上游并缓存元素，"),
    (r"Combines the latest items emitted by multiple Publishers via a combiner function\.",
     "多路 Publisher 各有新值时，用 combiner 合并最新快照发射。"),
    (r"Maps each item from the upstream Publisher into a Publisher and concatenates",
     "将上游各元素映射为 Publisher 并顺序串联订阅。"),
    (r"Creates a Flowable from scratch by means of a callback function",
     "通过回调函数从零创建 Flowable。"),
    (r"Maps each item from the upstream Publisher into a Publisher and merges",
     "将上游各元素映射为 Publisher 并并发合并 emission。"),
    (r"Maps each item from the upstream Publisher into a MaybeSource",
     "将上游各元素映射为 MaybeSource，onSuccess 值可用即发射。"),
    (r"Maps each item from the upstream Publisher into an Iterable",
     "将上游各元素经 mapper 转为 Iterable 后逐元素展开。"),
    (r"Creates a Flowable from an Iterable\.",
     "从 Iterable 创建 Flowable。"),
    (r"Utility class\.?", "工具类。"),
    (r"Returns an empty consumer\.", "返回空 Consumer。"),
    (r"Returns an identity function\.", "返回恒等 Function。"),
]

METHOD_COMMENT_PATTERNS: list[tuple[str, str]] = [
    (
        r"(\s+)@Override\n\s+protected void subscribeActual\(",
        r"\1/** 订阅核心逻辑：组装内部 Subscriber 并连接上游。 */\n\1@Override\n\1protected void subscribeActual(",
    ),
    (
        r"(\s+)@Override\n\s+public void subscribeActual\(",
        r"\1/** 订阅核心逻辑：组装内部 Subscriber 并连接上游。 */\n\1@Override\n\1public void subscribeActual(",
    ),
    (
        r"(\s+)@Override\n\s+public abstract Worker createWorker\(\)",
        r"\1/** 创建隔离 Worker 实例。 */\n\1@Override\n\1public abstract Worker createWorker()",
    ),
    (
        r"(\s+)@Override\n\s+public Disposable scheduleDirect\(",
        r"\1/** 直接在 Scheduler 上调度 Runnable。 */\n\1@Override\n\1public Disposable scheduleDirect(",
    ),
    (
        r"(\s+)@Override\n\s+public void onSubscribe\(Subscription",
        r"\1/** 校验 Subscription 并初始化内部状态。 */\n\1@Override\n\1public void onSubscribe(Subscription",
    ),
    (
        r"(\s+)@Override\n\s+public void onNext\(",
        r"\1/** 处理上游 onNext 并转发或缓存。 */\n\1@Override\n\1public void onNext(",
    ),
    (
        r"(\s+)@Override\n\s+public void onError\(",
        r"\1/** 处理上游/onError 并按策略终止或延迟错误。 */\n\1@Override\n\1public void onError(",
    ),
    (
        r"(\s+)@Override\n\s+public void onComplete\(\)",
        r"\1/** 上游完成：清理资源并向下游发送 onComplete。 */\n\1@Override\n\1public void onComplete()",
    ),
    (
        r"(\s+)@Override\n\s+public void request\(long",
        r"\1/** 处理下游背压 request。 */\n\1@Override\n\1public void request(long",
    ),
    (
        r"(\s+)@Override\n\s+public void cancel\(\)",
        r"\1/** 取消订阅并释放资源。 */\n\1@Override\n\1public void cancel()",
    ),
    (
        r"(\s+)@Override\n\s+public void dispose\(\)",
        r"\1/** dispose 连接/inner 并清理状态。 */\n\1@Override\n\1public void dispose()",
    ),
    (
        r"(\s+)@Override\n\s+public boolean isDisposed\(\)",
        r"\1/** 返回是否已 dispose。 */\n\1@Override\n\1public boolean isDisposed()",
    ),
    (r"(\s+)void drain\(\)", r"\1/** drain 循环：按 request 从队列取元素发射。 */\n\1void drain()"),
    (
        r"(\s+)void innerComplete\(\)",
        r"\1/** inner 完成：更新状态并继续 drain/切换。 */\n\1void innerComplete()",
    ),
    (
        r"(\s+)void innerError\(Throwable",
        r"\1/** inner 错误：按 delayError 策略合并或立即终止。 */\n\1void innerError(Throwable",
    ),
    (
        r"(\s+)static final class (\w+)",
        r"\1/** 内部实现类 \2。 */\n\1static final class \2",
    ),
    (
        r"(\s+)static final class (\w+) implements",
        r"\1/** 内部实现类 \2。 */\n\1static final class \2 implements",
    ),
]

OCA_BEGIN = "/* ===== [OCA 中文解析] ====="
OCA_END = "===== [OCA 中文解析结束] ===== */"


def oca_file_block(summary: str) -> str:
    return f"{OCA_BEGIN}\n文件意图总览\n\n{summary}\n{OCA_END}\n"


def oca_type_block(kind: str, class_name: str, summary: str) -> str:
    return (
        f"{OCA_BEGIN}\n"
        f"{kind} {class_name} — 意图说明\n\n"
        f"{summary}\n\n"
        f"（本注释由 open-code-analyzer 生成，置于原有文档注释之前）\n"
        f"{OCA_END}\n"
    )


def translate_javadoc_block(text: str) -> str:
    for old, new in JAVADOC_TRANSLATIONS:
        text = text.replace(old, new)
    return text


def inject_type_oca(text: str, class_name: str, class_summary: str) -> str:
    patterns = [
        (
            re.compile(
                r"(/\*\*.*?\*/\s*)?(public (?:final )?(?:abstract )?class "
                + re.escape(class_name)
                + r"\b)",
                re.S,
            ),
            "class",
        ),
        (
            re.compile(
                r"(/\*\*.*?\*/\s*)?(public interface " + re.escape(class_name) + r"\b)",
                re.S,
            ),
            "interface",
        ),
    ]
    for class_re, kind in patterns:
        m = class_re.search(text)
        if not m:
            continue
        prefix = m.group(1) or ""
        if OCA_BEGIN in prefix:
            return text
        if prefix.strip():
            translated = translate_javadoc_block(prefix)
            replacement = oca_type_block(kind, class_name, class_summary) + translated + m.group(2)
        else:
            doc = f"/**\n * {class_summary}\n */\n"
            replacement = oca_type_block(kind, class_name, class_summary) + doc + m.group(2)
        return text[: m.start()] + replacement + text[m.end() :]
    return text


def annotate_streamable(text: str) -> str:
    """Streamable 使用 /// Javadoc，需单独处理。"""
    md_block = re.search(
        r"(/// Represents a virtual-thread capable.*?/// @since 4\.0\.0\n)",
        text,
        re.S,
    )
    if md_block and OCA_BEGIN not in md_block.group(1):
        cn_md = (
            "/// 基于 Java {@link CompletionStage} 的虚拟线程友好多值异步序列。\n"
            "///\n"
            "/// 生命周期：consumer 调用 {@link #stream(StreamerCancellation)} →\n"
            "/// 循环 {@link Streamer#next()} → 最后必须 {@link Streamer#finish()} 释放上游资源。\n"
            "/// 取消通过 {@link StreamerCancellation} 传播；敏感源应以 {@link CancellationException} 异常完成 next。\n"
            "///\n"
            "/// {@link Streamer} 方法须顺序、非重叠调用（类似 Reactive Streams §1.3）。\n"
            "///\n"
            "/// @param <T> Streamable 序列元素类型\n"
            "/// @since 4.0.0\n"
        )
        text = text.replace(md_block.group(1), cn_md, 1)
    extra = [
        (
            "    CompletionStage<Streamer<T>> stream(StreamerCancellation cancellation);",
            "    /**\n"
            "     * 实现流并返回 {@link Streamer} 供消费。\n"
            "     * @param cancellation 下游取消句柄\n"
            "     * @return 异步 Streamer 实例\n"
            "     */\n"
            "    CompletionStage<Streamer<T>> stream(StreamerCancellation cancellation);",
        ),
        (
            "    static <T> Streamable<T> fromIterable(Iterable<? extends T> source) {",
            "    /** 从 Iterable 创建 Streamable。 */\n"
            "    static <T> Streamable<T> fromIterable(Iterable<? extends T> source) {",
        ),
        (
            "    static <T> Streamable<T> fromArray(T... array) {",
            "    /** 从数组创建 Streamable。 */\n"
            "    static <T> Streamable<T> fromArray(T... array) {",
        ),
        (
            "    static <T> Streamable<T> empty() {",
            "    /** 返回空 Streamable。 */\n"
            "    static <T> Streamable<T> empty() {",
        ),
    ]
    for old, new in extra:
        if old in text and new not in text:
            text = text.replace(old, new, 1)
    return text


def annotate_flowable_from_iterable(text: str) -> str:
    extras = [
        (
            "    public FlowableFromIterable(Iterable<? extends T> source) {",
            "    /**\n"
            "     * @param source 上游 Iterable 数据源\n"
            "     */\n"
            "    public FlowableFromIterable(Iterable<? extends T> source) {",
        ),
        (
            "    public static <T> void subscribe(Subscriber<? super T> s, Iterator<? extends T> it) {",
            "    /** 将 Iterator 订阅到 Subscriber，按 ConditionalSubscriber 选择实现。 */\n"
            "    public static <T> void subscribe(Subscriber<? super T> s, Iterator<? extends T> it) {",
        ),
        (
            "        Iterator<? extends T> iterator;\n\n        volatile boolean cancelled;",
            "        /** 待迭代的 Iterator。 */\n"
            "        Iterator<? extends T> iterator;\n\n"
            "        /** 是否已取消。 */\n"
            "        volatile boolean cancelled;",
        ),
        (
            "        long index;\n\n        BaseRangeSubscription(",
            "        /** 当前迭代索引。 */\n"
            "        long index;\n\n"
            "        BaseRangeSubscription(",
        ),
        (
            "        @Override\n        public final void request(long n) {",
            "        /** 处理下游背压 request，从 Iterator 拉取元素。 */\n"
            "        @Override\n        public final void request(long n) {",
        ),
        (
            "        @Override\n        public final void cancel() {",
            "        /** 取消迭代并标记 cancelled。 */\n"
            "        @Override\n        public final void cancel() {",
        ),
        (
            "        abstract void fastPath();",
            "        /** 快速路径：一次性满足 request。 */\n"
            "        abstract void fastPath();",
        ),
        (
            "        abstract void slowPath(long r);",
            "        /** 慢速路径：分批满足 request。 */\n"
            "        abstract void slowPath(long r);",
        ),
    ]
    for old, new in extras:
        if old in text and new not in text:
            text = text.replace(old, new, 1)
    return text


def annotate_text(rel: str, text: str) -> str:
    name = Path(rel).name
    class_name = name.replace(".java", "")
    summary = FILE_SUMMARIES.get(name, f"RxJava 内部类型 {class_name}。")
    class_summary = CLASS_SUMMARIES.get(class_name, f"{class_name} 的核心实现。")

    pkg_match = re.search(r"^(package .+;)", text, re.M)
    if not pkg_match:
        raise ValueError(f"no package in {rel}")
    if OCA_BEGIN not in text:
        text = text.replace(pkg_match.group(1), oca_file_block(summary) + pkg_match.group(1), 1)

    text = inject_type_oca(text, class_name, class_summary)

    if name == "Streamable.java":
        text = annotate_streamable(text)
    elif name == "FlowableFromIterable.java":
        text = annotate_flowable_from_iterable(text)

    def _translate_block(m: re.Match[str]) -> str:
        block = m.group(0)
        if re.search(r"[\u4e00-\u9fff]", block):
            return block
        return translate_javadoc_block(block)

    text = re.sub(r"/\*\*.*?\*/", _translate_block, text, flags=re.S)

    for pat, repl in METHOD_COMMENT_PATTERNS:
        text = re.sub(pat, repl, text)

    return text


def build_replacements() -> dict[str, list[tuple[str, str]]]:
    reps: dict[str, list[tuple[str, str]]] = {}
    for rel in BATCH.read_text(encoding="utf-8").splitlines():
        rel = rel.strip()
        if not rel:
            continue
        orig = (ORIGINAL / rel).read_text(encoding="utf-8")
        annotated = annotate_text(rel, orig)
        if orig == annotated:
            raise ValueError(f"no changes for {rel}")
        name = Path(rel).name
        reps[name] = [(orig, annotated)]
    return reps


def write_replacements_file(reps: dict[str, list[tuple[str, str]]]) -> None:
    lines = [
        '"""Chinese OCA + JavaDoc replacements for RxJava 4.0.0-alpha-21 wave31a mega batch [0:15]."""',
        "",
        "from __future__ import annotations",
        "",
        "FILE_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {",
    ]
    for name, pairs in reps.items():
        lines.append(f'    "{name}": [')
        for old, new in pairs:
            lines.append(f"        ({old!r},")
            lines.append(f"         {new!r}),")
        lines.append("    ],")
    lines.append("}")
    lines.append("")
    OUT.write_text("\n".join(lines), encoding="utf-8")


def main() -> None:
    reps = build_replacements()
    write_replacements_file(reps)
    print(f"Wrote {OUT} with {len(reps)} files")


if __name__ == "__main__":
    main()
