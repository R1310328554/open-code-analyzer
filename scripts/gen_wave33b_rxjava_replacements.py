#!/usr/bin/env python3
"""Generate wave33b_replacements_rxjava_mega.py from original sources."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path("/workspace")
ORIGINAL = ROOT / "rxjava/4.0.0-alpha-21/original"
OUT = ROOT / "scripts/wave33b_replacements_rxjava_mega.py"
BATCH = Path("/tmp/rxjava_w33b.txt")

FILE_SUMMARIES: dict[str, str] = {
    "PublishProcessor.java": (
        "Publish 多播 Processor：将后续 onNext 广播给当前全部 Subscriber，"
        "不缓存历史项，支持 offer 非阻塞发射与 toSerialized 串行化。"
    ),
    "ReplayProcessor.java": (
        "Replay 多播 Processor：按 unbounded/size/time 策略缓存历史项并重放给新 Subscriber，"
        "含多种 ReplayBuffer 实现与背压协调。"
    ),
    "UnicastProcessor.java": (
        "Unicast 单播 Processor：仅允许一个 Subscriber，内置 SpscLinkedArrayQueue 缓冲，"
        "支持 delayError 与 onTerminate 回调。"
    ),
    "Schedulers.java": (
        "标准 Scheduler 静态工厂：single/computation/io/newThread/trampoline/from 等入口，"
        "配合 RxJavaPlugins hook 与系统属性配置线程池。"
    ),
    "BehaviorSubject.java": (
        "Behavior 热 Observable Subject：缓存最新一项并重放给新 Observer，"
        "提供 getValue/hasValue 非阻塞读取与 toSerialized 保护。"
    ),
    "ReplaySubject.java": (
        "Replay 热 Observable Subject：按容量/时间策略缓存并重放历史事件，"
        "Observer 侧无背压，late 订阅者收到完整缓存序列。"
    ),
    "UnicastSubject.java": (
        "Unicast 单播 Subject：仅允许一个 Observer，队列缓冲上游事件，"
        "终止时可触发 onTerminate Runnable。"
    ),
}

CLASS_SUMMARIES: dict[str, str] = {
    "PublishProcessor": "无缓存多播 FlowableProcessor，PublishSubscription 管理各 Subscriber 背压。",
    "ReplayProcessor": "可配置 ReplayBuffer 的多播 Processor 与 ReplaySubscription 重放逻辑。",
    "UnicastProcessor": "单 Subscriber 队列 Processor，UnicastQueueSubscription 实现 drain。",
    "Schedulers": "全局 Scheduler 单例 holder 与 Supplier 延迟初始化任务。",
    "BehaviorSubject": "AtomicReference 缓存最新值，BehaviorDisposable 管理 Observer 订阅。",
    "ReplaySubject": "ReplayBuffer 缓存策略与 ReplayDisposable 重放 Observer 事件。",
    "UnicastSubject": "单 Observer 队列 Subject，UnicastQueueDisposable 协调 drain。",
}

JAVADOC_TRANSLATIONS: list[tuple[str, str]] = [
    (r"@param <(\w+)> the type of item expected to be observed and emitted by the Processor",
     r"@param <\1> Processor 期望观察并发射的元素类型"),
    (r"@param <(\w+)> the type of item expected to be observed and emitted by the Subject",
     r"@param <\1> Subject 期望观察并发射的元素类型"),
    (r"@param <(\w+)> the type of item the (\w+) will emit",
     r"@param <\1> \2 将发射的元素类型"),
    (r"@param <(\w+)> the (\w+) type", r"@param <\1> \2 类型"),
    (r"@param <(\w+)> the upstream (\w+) type", r"@param <\1> 上游 \2 类型"),
    (r"@param <(\w+)> the downstream (\w+) type", r"@param <\1> 下游 \2 类型"),
    (r"@param <(\w+)> the element type", r"@param <\1> 元素类型"),
    (r"@param <(\w+)> the value type", r"@param <\1> 值类型"),
    (r"@param <(\w+)> the item type", r"@param <\1> 项类型"),
    (r"Processor that multicasts all subsequently observed items to its current \{@link Subscriber\}s\.",
     "多播 Processor：将之后观察到的所有项广播给当前全部 Subscriber。"),
    (r"Processor that emits the most recent item it has observed and all subsequent observed items to each subscribed\n"
     r" \* \{@link Subscriber\}\.",
     "Behavior Processor：向每个已订阅 Subscriber 发射最近观察项及之后全部项。"),
    (r"Subject that emits the most recent item it has observed and all subsequent observed items to each subscribed\n"
     r" \* \{@link Observer\}\.",
     "Behavior Subject：向每个已订阅 Observer 发射最近观察项及之后全部项。"),
    (r"Static factory methods for returning standard \{@link Scheduler\} instances\.",
     "返回标准 Scheduler 实例的静态工厂方法。"),
    (r"Creates a \{@link PublishProcessor\} without a default item\.",
     "创建无默认项的 PublishProcessor。"),
    (r"Creates a \{@link BehaviorProcessor\} without a default item\.",
     "创建无默认项的 BehaviorProcessor。"),
    (r"Creates a \{@link BehaviorSubject\} without a default item\.",
     "创建无默认项的 BehaviorSubject。"),
    (r"Returns \{@code true\} if this \{@code PublishProcessor\} has any \{@link Subscriber\}\.",
     "若本 PublishProcessor 存在任意 Subscriber 则返回 true。"),
    (r"Returns \{@code true\} if this \{@code BehaviorSubject\} has any \{@link Observer\}\.",
     "若本 BehaviorSubject 存在任意 Observer 则返回 true。"),
    (r"Returns \{@code true\} if this \{@code (\w+)\} has \{@code Subscriber\}s\.",
     r"若本 \1 存在 Subscriber 则返回 true。"),
    (r"Returns \{@code true\} if this \{@code (\w+)\} has \{@link Observer\}s\.",
     r"若本 \1 存在 Observer 则返回 true。"),
    (r"Returns \{@code true\} if the \{@code (\w+)\} has completed\.",
     r"若 \1 已完成则返回 true。"),
    (r"Returns \{@code true\} if the \{@code (\w+)\} has terminated with an error\.",
     r"若 \1 因错误终止则返回 true。"),
    (r"Returns the terminal \{@link Throwable\} if this \{@code (\w+)\} has terminated with an error, null otherwise\.",
     r"若 \1 因错误终止则返回终端 Throwable，否则 null。"),
    (r"Returns the latest observed item if any, null otherwise\.",
     "返回最近观察到的项（若有），否则 null。"),
    (r"Returns \{@code true\} if this \{@code (\w+)\} has a value\.",
     r"若 \1 已缓存值则返回 true。"),
    (r"Offers a value to this \{@code (\w+)\} if all \{@code Subscriber\}s are ready to receive it\.",
     r"当全部 Subscriber 就绪时向 \1 非阻塞 offer 一项。"),
    (r"Offers a value to this \{@code (\w+)\} if all \{@code Observer\}s are ready to receive it\.",
     r"当全部 Observer 就绪时向 \1 非阻塞 offer 一项。"),
    (r"Returns a \{@code (\w+)\} that serializes calls to \{@link #onNext\(Object\)\}, \{@link #onError\(Throwable\)\} and \{@link #onComplete\(\)\}\.",
     r"返回串行化 onNext/onError/onComplete 调用的 \1。"),
    (r"Returns a \{@code (\w+)\} that serializes calls to \{@link #onNext\(Object\)\}, \{@link #offer\(Object\)\}, \{@link #onError\(Throwable\)\} and \{@link #onComplete\(\)\}\.",
     r"返回串行化 onNext/offer/onError/onComplete 调用的 \1。"),
    (r"Returns a standard \{@link Scheduler\} which executes tasks on a single background thread\.",
     "返回在单后台线程执行任务的标准 Scheduler（single）。"),
    (r"Returns a standard \{@link Scheduler\} intended for computational work\.",
     "返回用于计算密集型工作的标准 Scheduler（computation）。"),
    (r"Returns a standard \{@link Scheduler\} intended for IO-bound work\.",
     "返回用于 IO 密集型工作的标准 Scheduler（io/cached）。"),
    (r"Returns a standard \{@link Scheduler\} which creates a new thread for each task\.",
     "返回为每个任务创建新线程的标准 Scheduler（newThread）。"),
    (r"Returns a \{@link Scheduler\} that queues work on the current thread\.",
     "返回在当前线程排队执行工作的 Scheduler（trampoline）。"),
    (r"Returns a \{@link Scheduler\} which executes tasks immediately on the current thread\.",
     "返回在当前线程立即执行任务的 Scheduler（immediate）。"),
    (r"Returns a \{@link Scheduler\} which executes tasks after the specified delay\.",
     "返回在指定延迟后执行任务的 Scheduler。"),
    (r"Error handling:", "错误处理："),
    (r"Example usage:", "用法示例："),
    (r"History:", "版本历史："),
]

BULK_LINE_TRANSLATIONS: list[tuple[str, str]] = [
    ("Returns the current", "返回当前"),
    ("Returns true if", "若满足条件则返回 true："),
    ("Returns the", "返回"),
    ("Returns a", "返回一个"),
    ("Returns an", "返回一个"),
    ("Sets the", "设置"),
    ("Removes the", "移除"),
    ("Creates a", "创建"),
    ("Creates the", "创建"),
    ("Applies the", "应用"),
    ("Calls the", "调用"),
    ("Checks if", "检查是否"),
    ("Checks the", "检查"),
    ("Validates the", "校验"),
    ("Assert that", "断言"),
    ("Awaits until", "阻塞等待直至"),
    ("Runs the", "运行"),
    ("Copies the", "复制"),
    ("Clears the", "清空"),
    ("Resets the", "重置"),
    ("Requests the", "请求"),
    ("Signals a", "发出"),
    ("Emits a", "发射"),
    ("Maps the", "映射"),
    ("Filters the", "过滤"),
    ("Reduces the", "归约"),
    ("Combines the", "合并"),
    ("Merges the", "合并"),
    ("Collects the", "收集"),
    ("Converts the", "转换"),
    ("Wraps the", "包装"),
    ("Schedules the", "调度"),
    ("Disposes the", "dispose"),
    ("Cancels the", "取消"),
    ("Offers the", "非阻塞 offer"),
    ("Tries to offer", "尝试 offer"),
    ("@return the", "@return "),
    ("@param subscribers the subscribers", "@param subscribers 订阅者数组"),
    ("@param subscriber the subscriber", "@param subscriber 订阅者"),
    ("@param observer the observer", "@param observer Observer"),
    ("@param scheduler the", "@param scheduler 目标 Scheduler"),
    ("@param source the", "@param source 上游源"),
    ("@param capacityHint the", "@param capacityHint 队列容量提示"),
    ("@param onTerminate the", "@param onTerminate 终止时 Runnable 回调"),
    ("@param delayError delay", "@param delayError 是否延迟错误"),
    ("@param maxSize the", "@param maxSize 最大缓存条数"),
    ("@param maxAge the", "@param maxAge 最大缓存时长"),
    ("@param unit the", "@param unit 时间单位"),
    ("@param executor the", "@param executor Executor"),
    ("@param threadFactory the", "@param threadFactory ThreadFactory"),
    ("@throws NullPointerException if", "@throws NullPointerException 若参数为 null"),
    ("@throws IllegalArgumentException if", "@throws IllegalArgumentException 若参数非法"),
    ("Backpressure:", "背压："),
    ("Scheduler:", "调度器："),
    ("Error handling:", "错误处理："),
    ("may be null", "可能为 null"),
    ("must not be null", "不可为 null"),
    ("the subscriber", "订阅者"),
    ("the subscribers", "订阅者数组"),
    ("the observer", "Observer"),
    ("the scheduler", "Scheduler"),
    ("the queue", "队列"),
    ("the buffer", "缓冲区"),
    ("the disposable", "Disposable"),
    ("the subscription", "Subscription"),
    ("the predicate", "Predicate 谓词"),
    ("the function", "函数"),
    ("the value", "值"),
    ("the values", "值列表"),
    ("the error", "错误"),
    ("the item", "项"),
    ("the items", "项列表"),
    ("the timeout", "超时时间"),
    ("the tag", "tag 标签"),
]

METHOD_COMMENT_PATTERNS: list[tuple[str, str]] = [
    (r"(\s+)public static (\w+) (\w+)\(", r"\1/** 静态工厂 \3：创建或获取 \2 实例。 */\n\1public static \2 \3("),
    (r"(\s+)public static void (\w+)\(", r"\1/** 静态方法 \2：配置 Scheduler 或插件。 */\n\1public static void \2("),
    (r"(\s+)public static boolean (\w+)\(", r"\1/** 静态方法 \2：返回 Scheduler/插件布尔状态。 */\n\1public static boolean \2("),
    (r"(\s+)public (\w+) (\w+)\(", r"\1/** 实例方法 \2：Processor/Subject 状态或发射 API。 */\n\1public \2 \3("),
    (r"(\s+)public final (\w+) (\w+)\(", r"\1/** 方法 \2：Processor/Subject 核心 API。 */\n\1public final \2 \3("),
    (r"(\s+)protected final (\w+) (\w+)\(", r"\1/** 受保护方法 \2：内部协调工具。 */\n\1protected final \2 \3("),
    (r"(\s+)@Override\n\s+protected void subscribeActual\(", r"\1/** 订阅核心逻辑：组装内部 Subscriber 并连接上游。 */\n\1@Override\n\1protected void subscribeActual("),
    (r"(\s+)@Override\n\s+public void subscribeActual\(", r"\1/** 订阅核心逻辑：向 Observer/Subscriber 推送缓存或实时事件。 */\n\1@Override\n\1public void subscribeActual("),
    (r"(\s+)@Override\n\s+public void onSubscribe\(Subscription", r"\1/** 校验 Subscription 并初始化内部状态。 */\n\1@Override\n\1public void onSubscribe(Subscription"),
    (r"(\s+)@Override\n\s+public void onSubscribe\(Disposable", r"\1/** 校验 Disposable 并初始化内部状态。 */\n\1@Override\n\1public void onSubscribe(Disposable"),
    (r"(\s+)@Override\n\s+public void onNext\(", r"\1/** 处理上游 onNext 并转发或缓存。 */\n\1@Override\n\1public void onNext("),
    (r"(\s+)@Override\n\s+public void onError\(", r"\1/** 处理 onError 并按策略终止或延迟错误。 */\n\1@Override\n\1public void onError("),
    (r"(\s+)@Override\n\s+public void onComplete\(\)", r"\1/** 上游完成：清理资源并向下游发送 onComplete。 */\n\1@Override\n\1public void onComplete()"),
    (r"(\s+)@Override\n\s+public void request\(long", r"\1/** 处理下游背压 request。 */\n\1@Override\n\1public void request(long"),
    (r"(\s+)@Override\n\s+public void cancel\(\)", r"\1/** 取消订阅并释放资源。 */\n\1@Override\n\1public void cancel()"),
    (r"(\s+)@Override\n\s+public void dispose\(\)", r"\1/** dispose 连接/inner 并清理状态。 */\n\1@Override\n\1public void dispose()"),
    (r"(\s+)@Override\n\s+public boolean isDisposed\(\)", r"\1/** 返回是否已 dispose。 */\n\1@Override\n\1public boolean isDisposed()"),
    (r"(\s+)@Override\n\s+public boolean offer\(", r"\1/** 非阻塞 offer：全部下游就绪时才发射。 */\n\1@Override\n\1public boolean offer("),
    (r"(\s+)public boolean offer\(", r"\1/** 非阻塞 offer：Subscriber/Observer 就绪时尝试发射。 */\n\1public boolean offer("),
    (r"(\s+)public boolean hasSubscribers\(\)", r"\1/** 是否存在活跃 Subscriber。 */\n\1public boolean hasSubscribers()"),
    (r"(\s+)public boolean hasObservers\(\)", r"\1/** 是否存在活跃 Observer。 */\n\1public boolean hasObservers()"),
    (r"(\s+)public boolean hasComplete\(\)", r"\1/** 是否已正常完成（onComplete）。 */\n\1public boolean hasComplete()"),
    (r"(\s+)public boolean hasThrowable\(\)", r"\1/** 是否因错误终止。 */\n\1public boolean hasThrowable()"),
    (r"(\s+)public boolean hasValue\(\)", r"\1/** 是否已缓存最新值。 */\n\1public boolean hasValue()"),
    (r"(\s+)public Throwable getThrowable\(\)", r"\1/** 返回终端 Throwable（若有）。 */\n\1public Throwable getThrowable()"),
    (r"(\s+)public T getValue\(\)", r"\1/** 非阻塞读取最近缓存值。 */\n\1public T getValue()"),
    (r"(\s+)public (\w+) toSerialized\(\)", r"\1/** 返回串行化包装，防止并发/重入 onNext。 */\n\1public \2 toSerialized()"),
    (r"(\s+)void drain\(\)", r"\1/** drain 循环：按 request 从队列取元素发射。 */\n\1void drain()"),
    (r"(\s+)void replay\(\)", r"\1/** 向新订阅者重放缓存事件。 */\n\1void replay()"),
    (r"(\s+)void add\(", r"\1/** 将项追加到 replay 缓冲区。 */\n\1void add("),
    (r"(\s+)void trim\(\)", r"\1/** 按 size/time 策略裁剪缓冲区。 */\n\1void trim()"),
    (r"(\s+)static final class (\w+)", r"\1/** 内部实现类 \2：协调订阅/背压/重放。 */\n\1static final class \2"),
    (r"(\s+)final class (\w+)", r"\1/** 内部 \2：单播队列 drain 与终端处理。 */\n\1final class \2"),
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
        text = re.sub(old, new, text)
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
    return block.replace("/**\n", "/**\n * 【说明】RxJava Processor/Subject API。\n", 1)


def find_class_declaration(text: str, class_name: str) -> re.Match[str] | None:
    patterns = [
        rf"(/\*\*.*?\*/\s*)?(public final class {re.escape(class_name)}\b)",
        rf"(/\*\*.*?\*/\s*)?(public abstract class {re.escape(class_name)}\b)",
        rf"(/\*\*.*?\*/\s*)?(public enum {re.escape(class_name)}\b)",
    ]
    for pat in patterns:
        m = re.search(pat, text, re.S)
        if m:
            return m
    return None


def annotate_text(rel: str, text: str) -> str:
    name = Path(rel).name
    class_name = name.replace(".java", "")
    summary = FILE_SUMMARIES.get(name, f"RxJava 组件 {class_name}。")
    class_summary = CLASS_SUMMARIES.get(class_name, f"{class_name} 的核心实现。")

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
                doc = (
                    "/**\n"
                    f" * {class_summary}\n"
                    " */\n"
                )
                replacement = oca_class_block(class_name, class_summary) + doc + decl
            text = text[: m.start()] + replacement + text[m.end() :]
    else:
        for needle in (
            f"public final class {class_name}",
            f"public abstract class {class_name}",
            f"public enum {class_name}",
        ):
            if needle in text and oca_class_block(class_name, class_summary) not in text:
                doc = (
                    oca_class_block(class_name, class_summary)
                    + "/**\n"
                    f" * {class_summary}\n"
                    " */\n"
                )
                text = text.replace(needle, doc + needle, 1)
                break

    def _translate_block(m: re.Match[str]) -> str:
        return augment_untranslated_javadoc(m.group(0))

    text = re.sub(r"/\*\*.*?\*/", _translate_block, text, flags=re.S)

    for pat, repl in METHOD_COMMENT_PATTERNS:
        text = re.sub(pat, repl, text)

    def _translate_field(m: re.Match[str]) -> str:
        block = m.group(0)
        if re.search(r"[\u4e00-\u9fff]", block):
            return block
        return augment_untranslated_javadoc(block)

    text = re.sub(r"/\*\*[^*].*?\*/", _translate_field, text)

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
            "capacityHint": "队列/缓冲容量提示",
            "onTerminate": "终止 Runnable 回调",
            "delayError": "是否延迟合并错误",
            "maxSize": "最大缓存条数",
            "maxAge": "最大缓存时长",
            "unit": "时间单位",
            "scheduler": "目标 Scheduler",
            "bufferSize": "缓冲区大小",
            "parent": "父 Processor/Subject",
        }
        for p in params:
            desc = param_desc.get(p, f"{p} 参数")
            lines.append(f"{indent} * @param {p} {desc}")
        lines.append(f"{indent} */")
        return "\n".join(lines) + "\n" + sig

    text = re.sub(
        rf"^(\s+)(?:public |protected )?{re.escape(class_name)}\([^;{{]+\)\s*\{{",
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
        '"""Chinese OCA + JavaDoc replacements for RxJava 4.0.0-alpha-21 wave33b mega batch [7:14]."""',
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
