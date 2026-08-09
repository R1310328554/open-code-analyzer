#!/usr/bin/env python3
"""Generate wave32a_replacements_rxjava_mega.py from original sources."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path("/workspace")
ORIGINAL = ROOT / "rxjava/4.0.0-alpha-21/original"
OUT = ROOT / "scripts/wave32a_replacements_rxjava_mega.py"
BATCH = Path("/tmp/rxjava_w32a.txt")

FILE_SUMMARIES: dict[str, str] = {
    "ObservableConcatMap.java": (
        "Observable concatMap：将上游各元素映射为 inner Observable 并顺序串联订阅，"
        "前一 inner 完成后再订阅下一 inner，支持 delayErrors 与 fusion。"
    ),
    "ObservableConcatMapEager.java": (
        "Observable concatMapEager：预取并 eager 订阅多个 inner Observable，"
        "在 maxConcurrency 限制下仍保持 emission 顺序。"
    ),
    "ObservableConcatMapScheduler.java": (
        "Observable concatMap 的 Scheduler 变体：inner 订阅与 drain 在指定 Scheduler 上调度。"
    ),
    "ObservableFlatMap.java": (
        "Observable flatMap：将上游各元素映射为 inner Observable 并并发订阅，"
        "合并 emission，处理 maxConcurrency 与 delayError。"
    ),
    "ObservableGroupJoin.java": (
        "groupJoin：左/右两路 Observable 按 leftEnd/rightEnd 信号配对，"
        "为每个左元素与右缓冲生成 resultSelector 结果。"
    ),
    "ObservableReplay.java": (
        "ConnectableObservable replay：缓存上游事件并按 buffer 策略向新 Observer 重放，"
        "支持 bounded/unbounded/timed 等多种 ReplayBuffer。"
    ),
    "ObservableWindowBoundarySelector.java": (
        "window(selector)：上游每个元素经 selector 映射为 boundary Observable，"
        "boundary 完成时关闭当前窗口并开启新 UnicastSubject 窗口。"
    ),
    "ObservableWindowTimed.java": (
        "window(timespan/timeskip)：按时间窗口将上游元素分组到 UnicastSubject，"
        "支持 maxSize、restartOnOpen 与 Scheduler。"
    ),
    "ParallelFromPublisher.java": (
        "将 Publisher 按 parallelism 拆分为多路 ParallelFlowable rail，"
        "可选 prefetch 与 asyncMode 背压策略。"
    ),
    "ParallelJoin.java": (
        "将 ParallelFlowable 各 rail 无序合并为单路 Flowable，"
        "协调多路 request/onNext 与 delayErrors。"
    ),
}

CLASS_SUMMARIES: dict[str, str] = {
    "ObservableConcatMap": "SourceObserver/InnerObserver 顺序切换 inner Observable。",
    "ObservableConcatMapEager": "Eager 预取 inner 并在 maxConcurrency 下顺序 drain。",
    "ObservableConcatMapScheduler": "在 Worker 上调度 inner 订阅与 drain 循环。",
    "ObservableFlatMap": "MergeObserver 管理 inner 队列、maxConcurrency 与错误合并。",
    "ObservableGroupJoin": "GroupJoinDisposable 协调左右流、缓冲与 UnicastSubject。",
    "ObservableReplay": "ReplayObserver 链表缓存 Node 并向 InnerDisposable replay。",
    "ObservableWindowBoundarySelector": "WindowBoundaryMainObserver 管理 selector 边界与窗口切换。",
    "ObservableWindowTimed": "WindowExact/Timed 等订阅者管理 Scheduler 定时窗口。",
    "ParallelFromPublisher": "ParallelFromPublisherSubscriber 将 Publisher 分片到多 rail。",
    "ParallelJoin": "JoinSubscription 合并多路 JoinInnerSubscriber 队列。",
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
    (r"@return the new Observable instance", r"@return 新的 Observable 实例"),
    (r"@return the new Flowable instance", r"@return 新的 Flowable 实例"),
    (r"@return the new ConnectableObservable instance", r"@return 新的 ConnectableObservable 实例"),
    (r"The source observable\.", "上游 Observable 源。"),
    (r"Holds the current subscriber that is, will be or just was subscribed to the source observable\.",
     "持有当前/即将/刚完成订阅上游的 ReplayObserver。"),
    (r"A factory that creates the appropriate buffer for the ReplayObserver\.",
     "为 ReplayObserver 创建合适 ReplayBuffer 的工厂。"),
    (r"Given a connectable observable factory, it multicasts over the generated",
     "给定 ConnectableObservable 工厂，经 selector 多播合并："),
    (r"@param connectableFactory the factory that returns a ConnectableObservable for each individual subscriber",
     "@param connectableFactory 为各订阅者返回 ConnectableObservable 的工厂"),
    (r"@param selector the function that receives an Observable and should return another Observable that will be subscribed to",
     "@param selector 接收 Observable 并返回将被订阅的另一 Observable 的函数"),
    (r"Merges the individual 'rails' of the source ParallelFlowable, unordered,",
     "将 ParallelFlowable 各 rail 无序合并为单路 Publisher（Flowable）序列。"),
    (r"into a single regular Publisher sequence \(exposed as Flowable\)\.",
     "暴露为 Flowable。"),
    (r"Maps each item from the upstream Publisher into a Publisher and concatenates",
     "将上游各元素映射为 Publisher 并顺序串联订阅。"),
    (r"Maps each item from the upstream Publisher into a Publisher and merges",
     "将上游各元素映射为 Publisher 并并发合并 emission。"),
    (r"Utility class\.?", "工具类。"),
]

METHOD_COMMENT_PATTERNS: list[tuple[str, str]] = [
    (
        r"(\s+)@Override\n\s+protected void subscribeActual\(Subscriber",
        r"\1/** 组装内部 Subscriber 并订阅上游 ParallelFlowable。 */\n\1@Override\n\1protected void subscribeActual(Subscriber",
    ),
    (
        r"(\s+)@Override\n\s+protected void subscribeActual\(Observer",
        r"\1/** 组装内部 Observer 并订阅上游。 */\n\1@Override\n\1protected void subscribeActual(Observer",
    ),
    (
        r"(\s+)@Override\n\s+public void subscribeActual\(Observer",
        r"\1/** 组装内部 Observer 并订阅上游。 */\n\1@Override\n\1public void subscribeActual(Observer",
    ),
    (
        r"(\s+)@Override\n\s+public void onSubscribe\(Subscription",
        r"\1/** 校验 Subscription 并初始化内部状态。 */\n\1@Override\n\1public void onSubscribe(Subscription",
    ),
    (
        r"(\s+)@Override\n\s+public void onSubscribe\(Disposable",
        r"\1/** 校验 Disposable 并初始化内部状态。 */\n\1@Override\n\1public void onSubscribe(Disposable",
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
    (r"(\s+)void drain\(\)", r"\1/** drain 循环：从队列取元素向下游发射。 */\n\1void drain()"),
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
    (
        r"(\s+)abstract static class (\w+)",
        r"\1/** 内部抽象类 \2。 */\n\1abstract static class \2",
    ),
    (
        r"(\s+)static final class (\w+) extends",
        r"\1/** 内部实现类 \2。 */\n\1static final class \2 extends",
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


def annotate_observable_replay(text: str) -> str:
    extras = [
        (
            "    interface BufferSupplier<T> {",
            "    /** ReplayBuffer 工厂接口。 */\n"
            "    interface BufferSupplier<T> {",
        ),
        (
            "        ReplayBuffer<T> call();",
            "        /** 创建新的 ReplayBuffer 实例。 */\n"
            "        ReplayBuffer<T> call();",
        ),
        (
            "    interface ReplayBuffer<T> {",
            "    /** 缓存并重放事件的 buffer 抽象。 */\n"
            "    interface ReplayBuffer<T> {",
        ),
        (
            "    public static <T> ConnectableObservable<T> create(",
            "    /** 创建带默认 unbounded buffer 的 replay ConnectableObservable。 */\n"
            "    public static <T> ConnectableObservable<T> create(",
        ),
        (
            "    public static <T> ConnectableObservable<T> create(ObservableSource<T> source,",
            "    /** 以指定 bufferFactory 创建 replay ConnectableObservable。 */\n"
            "    public static <T> ConnectableObservable<T> create(ObservableSource<T> source,",
        ),
    ]
    for old, new in extras:
        if old in text and new not in text:
            text = text.replace(old, new, 1)
    return text


def annotate_parallel_from_publisher(text: str) -> str:
    extras = [
        (
            "    public ParallelFromPublisher(Publisher<? extends T> source, int parallelism, int prefetch) {",
            "    /**\n"
            "     * @param source 上游 Publisher\n"
            "     * @param parallelism 并行 rail 数量\n"
            "     * @param prefetch 每 rail 预取量\n"
            "     */\n"
            "    public ParallelFromPublisher(Publisher<? extends T> source, int parallelism, int prefetch) {",
        ),
        (
            "    public void subscribe(Subscriber<? super T>[] subscribers) {",
            "    /** 将上游 Publisher 分片订阅到各 Parallel rail Subscriber。 */\n"
            "    public void subscribe(Subscriber<? super T>[] subscribers) {",
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

    if name == "ObservableReplay.java":
        text = annotate_observable_replay(text)
    elif name == "ParallelFromPublisher.java":
        text = annotate_parallel_from_publisher(text)

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
        '"""Chinese OCA + JavaDoc replacements for RxJava 4.0.0-alpha-21 wave32a mega batch [0:10]."""',
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
