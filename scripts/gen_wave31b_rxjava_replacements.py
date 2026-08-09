#!/usr/bin/env python3
"""Generate wave31b_replacements_rxjava_mega.py from original sources."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path("/workspace")
ORIGINAL = ROOT / "rxjava/4.0.0-alpha-21/original"
OUT = ROOT / "scripts/wave31b_replacements_rxjava_mega.py"
BATCH = Path("/tmp/rxjava_w31b.txt")

FILE_SUMMARIES: dict[str, str] = {
    "FlowableGroupBy.java": (
        "按 keySelector 将上游元素分组，为每个键发射独立的 GroupedFlowable；"
        "支持自定义 mapFactory 与组 eviction 队列。"
    ),
    "FlowableGroupJoin.java": (
        "GroupJoin 算子：左流元素与右流窗口配对，"
        "由 leftEnd/rightEnd 信号界定窗口生命周期并合并结果。"
    ),
    "FlowableJoin.java": (
        "Join 算子：左/右两路 Publisher 在 leftEnd/rightEnd 界定的时间窗口内配对发射。"
    ),
    "FlowableObserveOn.java": (
        "在指定 Scheduler 的工作线程上调度下游 onNext/onError/onComplete，"
        "支持背压预取与 delayError。"
    ),
    "FlowablePublish.java": (
        "ConnectableFlowable 热连接：共享单一上游订阅并向多路 Subscriber 多播，"
        "上游终止后迟到订阅者仍可收到终止事件直至 dispose。"
    ),
    "FlowablePublishMulticast.java": (
        "通过 MulticastProcessor 多播上游，"
        "将热 Flowable 交给 selector 函数再映射为下游 Publisher。"
    ),
    "FlowableReplay.java": (
        "ConnectableFlowable 重放：缓存上游事件供新订阅者 replay，"
        "支持多种 buffer 策略与 multicastSelector 工厂模式。"
    ),
    "FlowableSwitchMap.java": (
        "对每个上游元素映射 inner Publisher 并只保留最新 inner 的订阅，"
        "取消先前 inner 的 emission。"
    ),
    "FlowableWindow.java": (
        "按固定计数 size/skip 将上游切分为多个窗口 Flowable，"
        "支持 exact、skip 与 overlap 三种模式。"
    ),
    "FlowableWindowBoundarySelector.java": (
        "由 open 信号开启窗口，closingIndicator 返回的 Publisher 完成时关闭窗口。"
    ),
    "FlowableWindowTimed.java": (
        "按时间跨度 timespan/timeskip 切分窗口，"
        "可选 maxSize 与 restartTimerOnMaxSize 控制计数上限与定时器重启。"
    ),
    "FlowableZip.java": (
        "并行订阅多路 Publisher，在各路均有可用元素时调用 zipper 合并发射。"
    ),
    "MaybeMergeArray.java": (
        "同时运行 MaybeSource 数组，"
        "各 inner 的 onSuccess 值可用即向下游发射，全部完成后 onComplete。"
    ),
    "ObservableBufferTimed.java": (
        "Observable 版定时缓冲：按 timespan/timeskip 收集元素到 Collection 并批量发射。"
    ),
    "ObservableCache.java": (
        "自动连接上游 Observable 并缓存事件，"
        "支持多播 replay 与主动终止连接/完成缓存。"
    ),
}

CLASS_SUMMARIES: dict[str, str] = {
    "FlowableGroupBy": "按 key 分组并维护 GroupedUnicast 与背压队列。",
    "FlowableGroupJoin": "协调左右流、窗口结束信号与 resultSelector 合并。",
    "FlowableJoin": "维护 Join 窗口队列并在匹配时发射组合结果。",
    "FlowableObserveOn": "创建 Scheduler Worker 并将事件 post 到指定线程。",
    "FlowablePublish": "管理 PublishConnection 生命周期与 connect/disconnect。",
    "FlowablePublishMulticast": "MulticastProcessor + selector 组合多播映射。",
    "FlowableReplay": "ReplaySubscriber 与多种 ReplayBuffer 实现重放缓存。",
    "FlowableSwitchMap": "SwitchMapSubscriber 切换 inner 并处理背压与错误延迟。",
    "FlowableWindow": "按 size/skip 选择 WindowExact/Skip/Overlap 订阅者。",
    "FlowableWindowBoundarySelector": "WindowBoundaryMainSubscriber 管理开/关边界与 UnicastProcessor。",
    "FlowableWindowTimed": "按时间策略选择 Unbounded/Bounded/Skip 等窗口订阅者。",
    "FlowableZip": "ZipCoordinator 协调多路 request/onNext 同步 zip。",
    "MaybeMergeArray": "MergeMaybeObserver 合并多路 Maybe 并统一背压 drain。",
    "ObservableBufferTimed": "定时/计数缓冲 Observer 管理 Scheduler 与 buffer 发射。",
    "ObservableCache": "Multicaster 链表节点缓存事件并向新 Observer replay。",
}

JAVADOC_TRANSLATIONS: list[tuple[str, str]] = [
    (r"@param <(\w+)> the (\w+) type", r"@param <\1> \2 类型"),
    (r"@param <(\w+)> the upstream (\w+) type", r"@param <\1> 上游 \2 类型"),
    (r"@param <(\w+)> the downstream (\w+) type", r"@param <\1> 下游 \2 类型"),
    (r"@param <(\w+)> the (\w+) element type", r"@param <\1> \2 元素类型"),
    (r"@param <(\w+)> the element type", r"@param <\1> 元素类型"),
    (r"@param <(\w+)> the value type", r"@param <\1> 值类型"),
    (r"@param <(\w+)> the input value type", r"@param <\1> 输入值类型"),
    (r"@param <(\w+)> the output value type", r"@param <\1> 输出值类型"),
    (r"@param <(\w+)> the source element type", r"@param <\1> 源元素类型"),
    (r"@param <(\w+)> the result type", r"@param <\1> 结果类型"),
    (r"@param <(\w+)> the connectable observable type", r"@param <\1> ConnectableFlowable 类型"),
    (r"@return the new Observable instance", r"@return 新的 Flowable 实例"),
    (r"Shares a single underlying connection to the upstream Publisher\n"
     r" \* and multicasts events to all subscribed subscribers until the upstream\n"
     r" \* completes or the connection is disposed\.",
     "共享单一上游连接，向所有订阅者多播事件，直至上游完成或连接被 dispose。"),
    (r"The difference to FlowablePublish is that when the upstream terminates,\n"
     r" \* late subscribers will receive that terminal event until the connection is\n"
     r" \* disposed and the ConnectableFlowable is reset to its fresh state\.",
     "与旧版区别：上游终止后，迟到订阅者在连接 dispose 前仍会收到终止事件。"),
    (r"Multicasts a Flowable over a selector function\.",
     "通过 selector 函数对 Flowable 做多播映射。"),
    (r"Run all MaybeSources of an array at once and signal their values as they become available\.",
     "并行运行 MaybeSource 数组，各 inner 有值即向下游发射。"),
    (r"An observable which auto-connects to another observable, caches the elements\n"
     r" \* from that observable but allows terminating the connection and completing the cache\.",
     "自动连接上游 Observable 并缓存元素，"
     "允许终止连接并完成缓存。"),
    (r"The source observable\.", "上游 Observable。"),
    (r"Holds the current subscriber that is, will be or just was subscribed to the source observable\.",
     "持有当前/即将/刚完成订阅上游的 ReplaySubscriber。"),
    (r"A factory that creates the appropriate buffer for the ReplaySubscriber\.",
     "为 ReplaySubscriber 创建合适 ReplayBuffer 的工厂。"),
    (r"A shared instance of an empty array of observers to avoid creating\n"
     r"     \* a new empty array when all observers dispose\.",
     "空 Observer 数组单例，避免全部 dispose 时重复分配。"),
    (r"A shared instance indicating the source has no more events and there\n"
     r"     \* is no need to remember observers anymore\.",
     "表示上游已无事件、无需再记录 Observer 的单例标记。"),
    (r"The subscription to the source should happen at most once\.",
     "对上游的订阅最多发生一次。"),
    (r"Responsible caching events from the source and multicasting them to each downstream\.",
     "缓存上游事件并向各下游多播 replay。"),
    (r"No inner MaybeSource is running\.", "无 inner MaybeSource 在运行。"),
    (r"An inner MaybeSource is running but there are no results yet\.",
     "inner MaybeSource 运行中但尚无结果。"),
    (r"The inner MaybeSource succeeded with a value in \{@link #item\}\.",
     "inner MaybeSource 已成功，值缓存在 {@link #item}。"),
]

METHOD_COMMENT_PATTERNS: list[tuple[str, str]] = [
    (r"(\s+)@Override\n\s+protected void subscribeActual\(", r"\1/** 订阅核心逻辑：组装内部 Subscriber 并连接上游。 */\n\1@Override\n\1protected void subscribeActual("),
    (r"(\s+)@Override\n\s+public void subscribeActual\(", r"\1/** 订阅核心逻辑：组装内部 Subscriber 并连接上游。 */\n\1@Override\n\1public void subscribeActual("),
    (r"(\s+)@Override\n\s+public void connect\(", r"\1/** 建立或复用共享连接并向 upstream 发起订阅。 */\n\1@Override\n\1public void connect("),
    (r"(\s+)@Override\n\s+public Publisher<T> source\(\)", r"\1/** 返回被包装的上游 Publisher。 */\n\1@Override\n\1public Publisher<T> source()"),
    (r"(\s+)@Override\n\s+public void onSubscribe\(Subscription", r"\1/** 校验 Subscription 并初始化内部状态。 */\n\1@Override\n\1public void onSubscribe(Subscription"),
    (r"(\s+)@Override\n\s+public void onNext\(", r"\1/** 处理上游 onNext 并转发或缓存。 */\n\1@Override\n\1public void onNext("),
    (r"(\s+)@Override\n\s+public void onError\(", r"\1/** 处理上游/onError 并按策略终止或延迟错误。 */\n\1@Override\n\1public void onError("),
    (r"(\s+)@Override\n\s+public void onComplete\(\)", r"\1/** 上游完成：清理资源并向下游发送 onComplete。 */\n\1@Override\n\1public void onComplete()"),
    (r"(\s+)@Override\n\s+public void request\(long", r"\1/** 处理下游背压 request。 */\n\1@Override\n\1public void request(long"),
    (r"(\s+)@Override\n\s+public void cancel\(\)", r"\1/** 取消订阅并释放资源。 */\n\1@Override\n\1public void cancel()"),
    (r"(\s+)@Override\n\s+public void dispose\(\)", r"\1/** dispose 连接/inner 并清理状态。 */\n\1@Override\n\1public void dispose()"),
    (r"(\s+)@Override\n\s+public boolean isDisposed\(\)", r"\1/** 返回是否已 dispose。 */\n\1@Override\n\1public boolean isDisposed()"),
    (r"(\s+)void drain\(\)", r"\1/** drain 循环：按 request 从队列取元素发射。 */\n\1void drain()"),
    (r"(\s+)void innerComplete\(\)", r"\1/** inner 完成：更新状态并继续 drain/切换。 */\n\1void innerComplete()"),
    (r"(\s+)void innerError\(Throwable", r"\1/** inner 错误：按 delayError 策略合并或立即终止。 */\n\1void innerError(Throwable"),
    (r"(\s+)void innerSuccess\(", r"\1/** inner onSuccess：缓存结果并触发 drain。 */\n\1void innerSuccess("),
    (r"(\s+)static final class (\w+)", r"\1/** 内部实现类 \2。 */\n\1static final class \2"),
]

OCA_BEGIN = "/* ===== [OCA 中文解析] ====="
OCA_END = "===== [OCA 中文解析结束] ===== */"


def oca_file_block(summary: str) -> str:
    return (
        f"{OCA_BEGIN}\n"
        f"文件意图总览\n\n"
        f"{summary}\n"
        f"{OCA_END}\n"
    )


def oca_class_block(class_name: str, summary: str) -> str:
    return (
        f"{OCA_BEGIN}\n"
        f"class {class_name} — 意图说明\n\n"
        f"{summary}\n\n"
        f"（本注释由 open-code-analyzer 生成，置于原有文档注释之前）\n"
        f"{OCA_END}\n"
    )


def translate_javadoc_block(text: str) -> str:
    for old, new in JAVADOC_TRANSLATIONS:
        text = text.replace(old, new)
    return text


def annotate_text(rel: str, text: str) -> str:
    name = Path(rel).name
    class_name = name.replace(".java", "")
    summary = FILE_SUMMARIES.get(name, f"RxJava 内部算子 {class_name}。")
    class_summary = CLASS_SUMMARIES.get(class_name, f"{class_name} 的核心实现。")

    # file OCA block
    pkg_match = re.search(r"^(package .+;)", text, re.M)
    if not pkg_match:
        raise ValueError(f"no package in {rel}")
    if OCA_BEGIN not in text:
        text = text.replace(pkg_match.group(1), oca_file_block(summary) + pkg_match.group(1), 1)

    # class-level: find public final class
    class_re = re.compile(
        r"(/\*\*.*?\*/\s*)?(public final class " + re.escape(class_name) + r"\b)",
        re.S,
    )
    m = class_re.search(text)
    if m:
        prefix = m.group(1) or ""
        if OCA_BEGIN not in prefix:
            if prefix.strip():
                translated = translate_javadoc_block(prefix)
                replacement = oca_class_block(class_name, class_summary) + translated + m.group(2)
            else:
                doc = (
                    "/**\n"
                    f" * {class_summary}\n"
                    " */\n"
                )
                replacement = oca_class_block(class_name, class_summary) + doc + m.group(2)
            text = text[: m.start()] + replacement + text[m.end() :]
    else:
        # no javadoc on class
        needle = f"public final class {class_name}"
        if needle in text and oca_class_block(class_name, class_summary) not in text:
            doc = (
                oca_class_block(class_name, class_summary)
                + "/**\n"
                f" * {class_summary}\n"
                " */\n"
            )
            text = text.replace(needle, doc + needle, 1)

    # translate remaining English javadocs (single-line /** ... */ blocks)
    def _translate_block(m: re.Match[str]) -> str:
        block = m.group(0)
        if re.search(r"[\u4e00-\u9fff]", block):
            return block
        return translate_javadoc_block(block)

    text = re.sub(r"/\*\*.*?\*/", _translate_block, text, flags=re.S)

    # method/inner class comments (only if not already commented)
    for pat, repl in METHOD_COMMENT_PATTERNS:
        text = re.sub(pat, repl, text)

    # constructor param docs for public constructors without javadoc
    def _add_ctor_doc(m: re.Match[str]) -> str:
        indent = m.group(1)
        sig = m.group(0)
        if "/**" in text[max(0, m.start() - 200) : m.start()]:
            return sig
        params = []
        for p in re.findall(r"(\w+)\s+\w+\s*[,)]", sig):
            if p not in {"super", "this", "new", "extends", "implements"}:
                params.append(p)
        if not params:
            return sig
        lines = [f"{indent}/**", f"{indent} * 构造 {class_name}。"]
        param_desc = {
            "source": "上游 Flowable/Observable",
            "other": "另一路 Publisher",
            "mapper": "映射函数",
            "bufferSize": "缓冲区大小",
            "delayError": "是否延迟合并错误",
            "delayErrors": "是否延迟合并错误",
            "prefetch": "预取容量",
            "scheduler": "调度 Scheduler",
            "keySelector": "分组键选择器",
            "valueSelector": "分组值选择器",
            "mapFactory": "自定义 Map 工厂",
            "leftEnd": "左流窗口结束信号映射",
            "rightEnd": "右流窗口结束信号映射",
            "resultSelector": "结果组合函数",
            "selector": "selector 函数",
            "bufferFactory": "ReplayBuffer 工厂",
            "connectableFactory": "ConnectableFlowable 工厂",
            "timespan": "窗口时间跨度",
            "timeskip": "窗口跳跃间隔",
            "unit": "时间单位",
            "maxSize": "窗口最大元素数",
            "restartTimerOnMaxSize": "达 maxSize 时是否重启定时器",
            "size": "窗口元素计数",
            "skip": "窗口跳跃计数",
            "open": "开窗信号 Publisher",
            "closingIndicator": "关窗指示 Publisher 映射",
            "zipper": "合并函数",
            "sources": "上游 Publisher 数组",
            "sourcesIterable": "上游 Publisher 可迭代集合",
            "bufferSupplier": "缓冲区 Collection 工厂",
            "maxSize": "缓冲区最大容量",
        }
        for p in params:
            desc = param_desc.get(p, f"{p} 参数")
            lines.append(f"{indent} * @param {p} {desc}")
        lines.append(f"{indent} */")
        return "\n".join(lines) + "\n" + sig

    text = re.sub(
        r"^(\s+)public " + re.escape(class_name) + r"\([^;{]+\)\s*\{",
        _add_ctor_doc,
        text,
        flags=re.M,
    )

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
        '"""Chinese OCA + JavaDoc replacements for RxJava 4.0.0-alpha-21 wave31b mega batch [15:30]."""',
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
