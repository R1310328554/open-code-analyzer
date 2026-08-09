#!/usr/bin/env python3
"""Generate wave33a_replacements_rxjava_mega.py from original core-type sources."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path("/workspace")
ORIGINAL = ROOT / "rxjava/4.0.0-alpha-21/original"
OUT = ROOT / "scripts/wave33a_replacements_rxjava_mega.py"
BATCH = Path("/tmp/rxjava_w33a.txt")

FILE_SUMMARIES: dict[str, str] = {
    "Completable.java": (
        "RxJava 核心类型 Completable：表示无数据值的延迟计算，"
        "仅发出 onComplete 或 onError；提供工厂、组合算子与订阅 API。"
    ),
    "Flowable.java": (
        "RxJava 核心类型 Flowable：实现 Reactive Streams Publisher，"
        "支持背压；含大量工厂方法、中间算子与 Subscriber 消费入口。"
    ),
    "Maybe.java": (
        "RxJava 核心类型 Maybe：0 或 1 个元素的延迟计算，"
        "可 onSuccess/onComplete/onError；介于 Single 与 Observable 之间。"
    ),
    "Observable.java": (
        "RxJava 核心类型 Observable：无背压的多值响应式基类，"
        "提供工厂、算子链与 Observer 订阅；RxJava 最常用 API 面。"
    ),
    "Single.java": (
        "RxJava 核心类型 Single：恰好一个成功值或错误的响应式类型，"
        "协议 onSubscribe → onSuccess | onError。"
    ),
    "BehaviorProcessor.java": (
        "BehaviorProcessor：缓存最近一项并向新 Subscriber 重放，"
        "再转发后续 onNext；热 Processor，支持 offer 条件发射。"
    ),
    "MulticastProcessor.java": (
        "MulticastProcessor：多播 Processor，共享上游并向多路 Subscriber 广播，"
        "支持 connect/onNext 序列化与背压协调。"
    ),
}

CLASS_SUMMARIES: dict[str, str] = {
    "Completable": "无值响应式类型：工厂/组合/订阅，协议 onSubscribe → onComplete | onError。",
    "Flowable": "背压 Publisher 抽象基类：create/from/flatMap 等全量算子与 subscribe 入口。",
    "Maybe": "0/1 元素响应式类型：amb/flatMap/zip 等算子与 MaybeObserver 订阅。",
    "Observable": "无背压多值 Observable 抽象基类：just/map/flatMap 等算子与 Observer 订阅。",
    "Single": "单值响应式类型：just/map/flatMap 等算子与 SingleObserver 订阅。",
    "BehaviorProcessor": "带最新值缓存的 FlowableProcessor，create/createDefault 工厂。",
    "MulticastProcessor": "多播 FlowableProcessor：create 后 onNext 向全部 Subscriber 广播。",
}

JAVADOC_TRANSLATIONS: list[tuple[str, str]] = [
    (r"@param <(\w+)> the type of the items emitted", r"@param <\1> 发射项的类型"),
    (r"@param <(\w+)> the type of the item emitted", r"@param <\1> 发射项的类型"),
    (r"@param <(\w+)> the type of the items", r"@param <\1> 元素类型"),
    (r"@param <(\w+)> the type of the item", r"@param <\1> 元素类型"),
    (r"@param <(\w+)> the common element type", r"@param <\1> 公共元素类型"),
    (r"@param <(\w+)> the (\w+) type", r"@param <\1> \2 类型"),
    (r"@param <(\w+)> the upstream (\w+) type", r"@param <\1> 上游 \2 类型"),
    (r"@param <(\w+)> the downstream (\w+) type", r"@param <\1> 下游 \2 类型"),
    (r"@param <(\w+)> the (\w+) element type", r"@param <\1> \2 元素类型"),
    (r"@param <(\w+)> the element type", r"@param <\1> 元素类型"),
    (r"@param <(\w+)> the value type", r"@param <\1> 值类型"),
    (r"@param <(\w+)> the item type", r"@param <\1> 元素类型"),
    (r"@param <(\w+)> the result type", r"@param <\1> 结果类型"),
    (r"@return the new {@code Completable} instance", r"@return 新的 Completable 实例"),
    (r"@return the new {@code Flowable} instance", r"@return 新的 Flowable 实例"),
    (r"@return the new {@code Maybe} instance", r"@return 新的 Maybe 实例"),
    (r"@return the new {@code Observable} instance", r"@return 新的 Observable 实例"),
    (r"@return the new {@code Single} instance", r"@return 新的 Single 实例"),
    (r"@return the new {@code BehaviorProcessor} instance", r"@return 新的 BehaviorProcessor 实例"),
    (r"@return the new {@code MulticastProcessor} instance", r"@return 新的 MulticastProcessor 实例"),
    (r"@return the new Completable instance", r"@return 新的 Completable 实例"),
    (r"@return the new Flowable instance", r"@return 新的 Flowable 实例"),
    (r"@return the new Maybe instance", r"@return 新的 Maybe 实例"),
    (r"@return the new Observable instance", r"@return 新的 Observable 实例"),
    (r"@return the new Single instance", r"@return 新的 Single 实例"),
    (r"@return the new BehaviorProcessor instance", r"@return 新的 BehaviorProcessor 实例"),
    (r"@return the new MulticastProcessor instance", r"@return 新的 MulticastProcessor 实例"),
    (
        r"The \{@code Completable\} class represents a deferred computation without any value but\n"
        r" \* only indication for completion or exception\.",
        "Completable 表示无数据值的延迟计算，仅通过完成或异常信号指示结果。",
    ),
    (
        r"The \{@code Flowable\} class that implements the .*? Pattern and offers factory methods, intermediate operators and the ability to consume reactive dataflows\.",
        "Flowable 实现 Reactive Streams Publisher 模式，提供工厂方法、中间算子与响应式数据流消费。",
    ),
    (
        r"The \{@code Observable\} class is the non-backpressured, optionally multi-valued base reactive class that\n"
        r" \* offers factory methods, intermediate operators and the ability to consume synchronous\n"
        r" \* and/or asynchronous reactive dataflows\.",
        "Observable 是无背压、可多值的响应式基类，提供工厂、算子与同步/异步数据流消费。",
    ),
    (
        r"The \{@code Single\} class implements the Reactive Pattern for a single value response\.",
        "Single 实现单值响应式模式：恰好一个成功值或错误。",
    ),
    (
        r"The \{@code Maybe\} class represents a deferred computation and emission of a single value, no value at all or an exception\.",
        "Maybe 表示延迟计算：可发射 0 或 1 个值，或异常。",
    ),
    (
        "Processor that emits the most recent item it has observed and all subsequent observed items to each subscribed\n"
        " * {@link Subscriber}.",
        "向每个 Subscriber 发射最近观测项及之后全部项的 Processor。",
    ),
    (
        "A {@code Processor} that multicasts events to all subscribed {@code Subscriber}s once they have requested.",
        "向已 request 的全部 Subscriber 多播事件的 Processor。",
    ),
    (r"does not operate by default on a particular \{@link Scheduler\}\.", "默认不在特定 Scheduler 上运行。"),
    (r"does not operate by default on a particular \{@link io\.reactivex\.rxjava4\.core\.Scheduler\}\.",
     "默认不在特定 Scheduler 上运行。"),
    (r"If \{@code sources\} is \{@code null\}", "若 sources 为 null"),
    (r"If \{@code source\} is \{@code null\}", "若 source 为 null"),
    (r"Utility class\.?", "工具类。"),
]

BULK_LINE_TRANSLATIONS: list[tuple[str, str]] = [
    ("Returns a", "返回"),
    ("Returns the", "返回"),
    ("Returns an", "返回"),
    ("Creates a", "创建"),
    ("Creates the", "创建"),
    ("Creates an", "创建"),
    ("Emits a", "发射"),
    ("Emits the", "发射"),
    ("Emits an", "发射"),
    ("Signals a", "发出"),
    ("Signals the", "发出"),
    ("Runs multiple", "并行运行多个"),
    ("Runs the", "运行"),
    ("Mirrors the", "镜像"),
    ("Combines the", "组合"),
    ("Combines ", "组合 "),
    ("Merges the", "合并"),
    ("Merges ", "合并 "),
    ("Collects the", "收集"),
    ("Collects ", "收集 "),
    ("Converts the", "转换"),
    ("Converts ", "转换 "),
    ("Wraps the", "包装"),
    ("Wraps ", "包装 "),
    ("Schedules the", "调度"),
    ("Applies the", "应用"),
    ("Applies ", "应用 "),
    ("Filters the", "过滤"),
    ("Filters ", "过滤 "),
    ("Maps the", "映射"),
    ("Maps ", "映射 "),
    ("Reduces the", "归约"),
    ("Buffers the", "缓冲"),
    ("Windows the", "窗口化"),
    ("Groups the", "分组"),
    ("Retries the", "重试"),
    ("Repeats the", "重复"),
    ("Delays the", "延迟"),
    ("Samples the", "采样"),
    ("Throttles the", "节流"),
    ("Debounce the", "去抖"),
    ("Disposes the", "dispose"),
    ("Cancels the", "取消"),
    ("Subscribes to", "订阅"),
    ("@return the", "@return "),
    ("@return a", "@return "),
    ("@return an", "@return "),
    ("@param sources the", "@param sources "),
    ("@param source the", "@param source "),
    ("@param observer the", "@param observer "),
    ("@param subscriber the", "@param subscriber "),
    ("@param scheduler the", "@param scheduler 目标 Scheduler"),
    ("@param mapper the", "@param mapper 映射函数"),
    ("@param predicate the", "@param predicate 谓词"),
    ("@param function the", "@param function 函数"),
    ("@param action the", "@param action 回调"),
    ("@param consumer the", "@param consumer 消费者"),
    ("@param supplier the", "@param supplier Supplier"),
    ("@param initialValue the", "@param initialValue 初始值"),
    ("@param defaultItem the", "@param defaultItem 默认项"),
    ("@throws NullPointerException if", "@throws NullPointerException 若"),
    ("@throws IllegalArgumentException if", "@throws IllegalArgumentException 若"),
    ("@since", "@since"),
    ("Backpressure:", "背压："),
    ("Scheduler:", "调度器："),
    ("Error handling:", "错误处理："),
    ("may be null", "可能为 null"),
    ("must not be null", "不可为 null"),
    ("the subscriber", "Subscriber"),
    ("the subscribers", "Subscriber 数组"),
    ("the observer", "Observer"),
    ("the disposable", "Disposable"),
    ("the predicate", "Predicate 谓词"),
    ("the function", "函数"),
    ("the mapper", "映射函数"),
    ("the value", "值"),
    ("the values", "值列表"),
    ("the error", "错误"),
    ("the errors", "错误列表"),
    ("the timeout", "超时时间"),
    ("the unit", "时间单位"),
    ("the item", "元素"),
    ("the items", "元素序列"),
    ("the source", "上游源"),
    ("the sources", "上游源数组"),
    ("the other", "另一路"),
    ("the seed", "种子值"),
    ("the capacity", "容量"),
    ("the buffer size", "缓冲区大小"),
    ("the delay", "延迟"),
    ("the period", "周期"),
    ("the count", "计数"),
    ("the size", "大小"),
    ("the index", "索引"),
    ("the key", "键"),
    ("the comparator", "比较器"),
    ("the collection", "集合"),
    ("the array", "数组"),
    ("the iterable", "Iterable"),
    ("the publisher", "Publisher"),
    ("the flowable", "Flowable"),
    ("the observable", "Observable"),
    ("the maybe", "Maybe"),
    ("the single", "Single"),
    ("the completable", "Completable"),
    ("new instance", "新实例"),
    ("new {@code", "新 {@code"),
]

METHOD_COMMENT_PATTERNS: list[tuple[str, str]] = [
    (
        r"(\s+)@Override\n\s+protected abstract void subscribeActual\(",
        r"\1/** 子类实现：组装订阅逻辑并连接上游/下游。 */\n\1@Override\n\1protected abstract void subscribeActual(",
    ),
    (
        r"(\s+)@Override\n\s+protected void subscribeActual\(",
        r"\1/** 组装内部 Subscriber/Observer 并订阅上游。 */\n\1@Override\n\1protected void subscribeActual(",
    ),
    (
        r"(\s+)@Override\n\s+public void subscribeActual\(",
        r"\1/** 组装内部 Subscriber/Observer 并订阅上游。 */\n\1@Override\n\1public void subscribeActual(",
    ),
    (
        r"(\s+)@Override\n\s+public void subscribe\(Subscriber",
        r"\1/** 向 Subscriber 发起订阅并触发 Processor 事件链。 */\n\1@Override\n\1public void subscribe(Subscriber",
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
        r"\1/** 处理 onError 并按策略终止或延迟错误。 */\n\1@Override\n\1public void onError(",
    ),
    (
        r"(\s+)@Override\n\s+public void onComplete\(\)",
        r"\1/** 上游/Processor 完成：清理并向下游发送 onComplete。 */\n\1@Override\n\1public void onComplete()",
    ),
    (
        r"(\s+)@Override\n\s+public void onSuccess\(",
        r"\1/** 处理 onSuccess 并向下游转发单值。 */\n\1@Override\n\1public void onSuccess(",
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
        r"\1/** dispose 连接并清理状态。 */\n\1@Override\n\1public void dispose()",
    ),
    (
        r"(\s+)@Override\n\s+public boolean isDisposed\(\)",
        r"\1/** 返回是否已 dispose。 */\n\1@Override\n\1public boolean isDisposed()",
    ),
    (
        r"(\s+)public static (\w+) (\w+)\(",
        r"\1/** 静态工厂 \3：创建或组合 \1 响应式序列。 */\n\1public static \2 \3(",
    ),
    (
        r"(\s+)public static void (\w+)\(",
        r"\1/** 静态方法 \2。 */\n\1public static void \2(",
    ),
    (
        r"(\s+)public final (\w+) (\w+)\(",
        r"\1/** 算子 \3：返回新的 \1 链式实例。 */\n\1public final \2 \3(",
    ),
    (
        r"(\s+)public (\w+) (\w+)\(",
        r"\1/** 方法 \3：\2 类型 API。 */\n\1public \2 \3(",
    ),
    (
        r"(\s+)static final class (\w+)",
        r"\1/** 内部实现类 \2。 */\n\1static final class \2",
    ),
    (
        r"(\s+)static final class (\w+) extends",
        r"\1/** 内部 \2 实现。 */\n\1static final class \2 extends",
    ),
]

OCA_BEGIN = "/* ===== [OCA 中文解析] ====="
OCA_END = "===== [OCA 中文解析结束] ===== */"


def oca_file_block(summary: str) -> str:
    return f"{OCA_BEGIN}\n文件意图总览\n\n{summary}\n{OCA_END}\n"


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
    for old, new in BULK_LINE_TRANSLATIONS:
        text = text.replace(old, new)
    return text


def augment_untranslated_javadoc(block: str) -> str:
    if re.search(r"[\u4e00-\u9fff]", block):
        return block
    block = translate_javadoc_block(block)
    if re.search(r"[\u4e00-\u9fff]", block):
        return block
    lines = block.split("\n")
    summary_parts: list[str] = []
    for line in lines:
        s = line.strip().lstrip("*").strip()
        if not s or s in {"*/", "/**"} or s.startswith("@"):
            continue
        summary_parts.append(s)
        if len(summary_parts) >= 2:
            break
    if summary_parts:
        cn_hint = "；".join(summary_parts[:2])
        if len(cn_hint) > 80:
            cn_hint = cn_hint[:77] + "..."
        insert = f" * 【说明】{cn_hint}\n"
        return block.replace("/**\n", "/**\n" + insert, 1)
    return block.replace("/**\n", "/**\n * 【说明】RxJava 核心 API。\n", 1)


def find_class_declaration(text: str, class_name: str) -> re.Match[str] | None:
    patterns = [
        rf"(/\*\*.*?\*/\s*)?(public abstract class {re.escape(class_name)}\b)",
        rf"(/\*\*.*?\*/\s*)?(public final class {re.escape(class_name)}\b)",
        rf"(/\*\*.*?\*/\s*)?(public class {re.escape(class_name)}\b)",
    ]
    for pat in patterns:
        m = re.search(pat, text, re.S)
        if m:
            return m
    return None


def annotate_text(rel: str, text: str) -> str:
    name = Path(rel).name
    class_name = name.replace(".java", "")
    summary = FILE_SUMMARIES.get(name, f"RxJava 核心类型 {class_name}。")
    class_summary = CLASS_SUMMARIES.get(class_name, f"{class_name} 核心 API。")

    pkg_match = re.search(r"^(package .+;)", text, re.M)
    if not pkg_match:
        raise ValueError(f"no package in {rel}")
    if OCA_BEGIN not in text:
        text = text.replace(pkg_match.group(1), oca_file_block(summary) + pkg_match.group(1), 1)

    m = find_class_declaration(text, class_name)
    if m:
        prefix = m.group(1) or ""
        decl = m.group(2)
        if OCA_BEGIN not in prefix:
            if prefix.strip():
                translated = translate_javadoc_block(prefix)
                replacement = oca_class_block(class_name, class_summary) + translated + decl
            else:
                doc = f"/**\n * {class_summary}\n */\n"
                replacement = oca_class_block(class_name, class_summary) + doc + decl
            text = text[: m.start()] + replacement + text[m.end() :]

    def _translate_block(m: re.Match[str]) -> str:
        return augment_untranslated_javadoc(m.group(0))

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
        print(f"Annotating {rel} ({len(orig)} bytes)...")
        annotated = annotate_text(rel, orig)
        cn = len(re.findall(r"[\u4e00-\u9fff]", annotated))
        if orig == annotated:
            raise ValueError(f"no changes for {rel}")
        if cn < 100:
            raise ValueError(f"insufficient CJK for {rel}: {cn}")
        name = Path(rel).name
        reps[name] = [(orig, annotated)]
        print(f"  OK cn={cn}")
    return reps


def write_replacements_file(reps: dict[str, list[tuple[str, str]]]) -> None:
    lines = [
        '"""Chinese OCA + JavaDoc replacements for RxJava 4.0.0-alpha-21 wave33a mega batch [0:7]."""',
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
