#!/usr/bin/env python3
"""Generate wave32b_replacements_rxjava_mega.py from original sources."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path("/workspace")
ORIGINAL = ROOT / "rxjava/4.0.0-alpha-21/original"
OUT = ROOT / "scripts/wave32b_replacements_rxjava_mega.py"
BATCH = Path("/tmp/rxjava_w32b.txt")

FILE_SUMMARIES: dict[str, str] = {
    "ParallelRunOn.java": (
        "Parallel runOn 算子：为 ParallelFlowable 每条 rail 分配 Scheduler Worker，"
        "在指定线程上调度上游事件并维护背压队列。"
    ),
    "StreamableHelper.java": (
        "Streamable、CompletableFuture 与 CompletionStage 互操作辅助："
        "竞速合并、终端事件传播与 StreamableInterceptConfig 拦截。"
    ),
    "BlockingCurrentThreadScheduler.java": (
        "阻塞式当前线程 Scheduler：以事件循环方式在当前线程执行 Action，"
        "支持从其他线程 post 任务并阻塞等待。"
    ),
    "DeferredExecutorScheduler.java": (
        "延迟创建 Executor 的 Scheduler 包装：按需从 Supplier 获取 Executor，"
        "提供与 ExecutorScheduler 一致的 Worker 语义。"
    ),
    "ExecutorScheduler.java": (
        "将 Executor 包装为 Scheduler API：支持可中断 Worker、公平/非公平任务队列，"
        "以及延迟/周期调度。"
    ),
    "ParallelScheduler.java": (
        "可配置并行度的固定线程池 Scheduler，实现 SchedulerMultiWorkerSupport，"
        "支持 tracking 模式与动态 resize。"
    ),
    "QueueDrainHelper.java": (
        "队列 drain 串行化工具：协调 QueueDrain 状态、背压 request 与 delayError，"
        "供 observeOn/groupBy 等算子复用。"
    ),
    "BaseTestConsumer.java": (
        "TestSubscriber/TestObserver 共享测试基础设施："
        "values/errors 收集、await 断言与 tag 支持。"
    ),
    "ParallelFlowable.java": (
        "并行多路 Subscriber 发布抽象基类：from/runOn/sequential 等入口，"
        "将 Publisher 拆分为多条 rail 并行处理后再合并。"
    ),
    "RxJavaPlugins.java": (
        "RxJava 全局插件与 hook 注入：错误处理、Scheduler 装饰、"
        "assembly/subscribe 拦截及各类 onXxx 回调。"
    ),
}

CLASS_SUMMARIES: dict[str, str] = {
    "ParallelRunOn": "为每条 parallel rail 创建 RunOnSubscriber 与 Worker 队列。",
    "StreamableHelper": "Streamable/CF/CS 互操作的静态辅助枚举。",
    "BlockingCurrentThreadScheduler": "当前线程阻塞事件循环 Scheduler 与 Worker。",
    "DeferredExecutorScheduler": "Supplier<Executor> 延迟实例化的 Scheduler。",
    "ExecutorScheduler": "Executor + MpscLinkedQueue 驱动的 Scheduler/Worker。",
    "ParallelScheduler": "固定大小 ScheduledExecutorService 池与 MultiWorker 支持。",
    "QueueDrainHelper": "QueueDrain 协议下的 drain/fail-fast 工具方法。",
    "BaseTestConsumer": "测试消费者基类：latch、values/errors 与断言 API。",
    "ParallelFlowable": "parallelism 级多 Subscriber 订阅与算子工厂。",
    "RxJavaPlugins": "volatile hook 字段与 onAssembly/onSubscribe 拦截入口。",
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
    (r"@param <(\w+)> the common element type", r"@param <\1> 公共元素类型"),
    (r"@param <(\w+)> the queue value type", r"@param <\1> 队列值类型"),
    (r"@param <(\w+)> the emission value type", r"@param <\1> 发射值类型"),
    (r"@param <(\w+)> the subclass of this \{@code BaseTestConsumer\}", r"@param <\1> BaseTestConsumer 子类"),
    (r"@return the number of expected parallel \{@code Subscriber\}s", r"@return 期望的并行 Subscriber 数量"),
    (r"@return \{@code true\} if the number of subscribers equals to the parallelism level",
     r"@return 若 subscribers 数量等于 parallelism 则返回 true"),
    (r"Utility class to inject handlers to certain standard RxJava operations\.",
     "工具类：向标准 RxJava 操作注入 hook 处理器。"),
    (r"Utility class to help with the queue-drain serialization idiom\.",
     "队列 drain 串行化模式的工具类。"),
    (r"Utility class\.", "工具类，禁止实例化。"),
    (r"Base class with shared infrastructure to support\n"
     r" \* \{@link io\.reactivex\.rxjava4\.subscribers\.TestSubscriber TestSubscriber\} and \{@link TestObserver\}\.",
     "TestSubscriber 与 TestObserver 共享基础设施的基类。"),
    (r"Abstract base class for parallel publishing of events signaled to an array of \{@link Subscriber\}s\.",
     "向 Subscriber 数组并行发布事件的抽象基类。"),
    (r"Ensures each 'rail' from upstream runs on a Worker from a Scheduler\.",
     "确保上游每条 rail 在 Scheduler 的 Worker 上运行。"),
    (r"Wraps an Executor and provides the Scheduler API over it\.",
     "包装 Executor 并提供 Scheduler API。"),
    (r"Wraps an Executor supplier and provides the Scheduler API over an instance of Executor\n"
     r" \* created on demand\.",
     "包装 Executor Supplier，按需创建 Executor 并提供 Scheduler API。"),
    (r"A Scheduler that uses the current thread, in an event-loop and\n"
     r" \* blocking fashion to execute actions\.",
     "在当前线程以阻塞事件循环方式执行 Action 的 Scheduler。"),
    (r"Scheduler with a configurable fixed amount of thread-pools\.",
     "可配置固定线程池数量的 Scheduler。"),
    (r"Helper static methods for \{@link Streamable\}s, \{@link CompletableFuture\}s and \{@link CompletionStage\}s\.",
     "Streamable、CompletableFuture 与 CompletionStage 的静态辅助方法。"),
    (r"The latch that indicates an onError or onComplete has been called\.",
     "表示已收到 onError 或 onComplete 的 CountDownLatch。"),
    (r"The list of values received\.", "已接收的值列表。"),
    (r"The list of errors received\.", "已接收的错误列表。"),
    (r"The number of completions\.", "完成次数。"),
    (r"The last thread seen by the observer\.", "Observer 最后见到的线程。"),
    (r"Prevents changing the plugins\.", "禁止修改插件配置。"),
    (r"Returns true if the plugins were locked down\.", "若插件已锁定则返回 true。"),
    (r"Returns the current hook function\.", "返回当前 hook 函数。"),
    (r"Returns the hook consumer\.", "返回全局错误处理 hook Consumer。"),
    (r"Sets the hook function\.?", "设置 hook 函数。"),
    (r"Removes the hook function\.?", "移除 hook 函数。"),
    (r"@return the hook function, may be null", "@return hook 函数，可能为 null"),
    (r"@return the hook consumer, may be null", "@return hook Consumer，可能为 null"),
    (r"@return true if the plugins were locked down", "@return 插件是否已锁定"),
    (r"Subscribes an array of \{@link Subscriber\}s to this \{@code ParallelFlowable\}",
     "向本 ParallelFlowable 订阅 Subscriber 数组并触发各 rail 执行链。"),
    (r"Returns the number of expected parallel \{@link Subscriber\}s\.",
     "返回期望的并行 Subscriber 数量。"),
    (r"Validates the number of subscribers and returns \{@code true\} if their number\n"
     r" \* matches the parallelism level of this \{@code ParallelFlowable\}\.",
     "校验 subscribers 数量是否与 parallelism 一致。"),
    (r"Drain the queue but give up with an error if there aren't enough requests\.",
     "drain 队列；若 request 不足则报错终止。"),
    (r"Checks which source completes first, calls the given acceptor with 1 or 2 indicating the winner,\n"
     r" \* then terminates the resulting \{@link CompletableFuture\} with said terminal event\.",
     "竞速两路 CompletionStage，先完成者标记为 1 或 2，并将终端事件写入 CompletableFuture。"),
    (r"The optional tag associated with this test consumer\.", "与本测试消费者关联的可选 tag。"),
    (r"Creates a \{@link TestObserver\} or \{@link io\.reactivex\.rxjava4\.subscribers\.TestSubscriber\}\.",
     "创建 TestObserver 或 TestSubscriber。"),
    (r"Assert that this TestSubscriber/TestObserver received exactly one \{@code onComplete\} event\.",
     "断言恰好收到一次 onComplete。"),
    (r"Assert that this TestSubscriber/TestObserver received no \{@code onComplete\} events\.",
     "断言未收到任何 onComplete。"),
    (r"Assert that this TestSubscriber/TestObserver received exactly one \{@code onError\} event\.",
     "断言恰好收到一次 onError。"),
    (r"Assert that this TestSubscriber/TestObserver received no \{@code onError\} events\.",
     "断言未收到任何 onError。"),
    (r"Assert that this TestSubscriber/TestObserver has not received any events\.",
     "断言尚未收到任何事件。"),
    (r"Assert that this TestSubscriber/TestObserver has not received any terminal events\.",
     "断言尚未收到任何终端事件。"),
    (r"Awaits until this TestSubscriber/TestObserver receives a terminal event\.",
     "阻塞等待直至收到终端事件（onComplete 或 onError）。"),
    (r"Awaits until this TestSubscriber/TestObserver receives at least the given number of \{@code onNext\} events\.",
     "阻塞等待直至收到至少指定数量的 onNext。"),
    (r"Returns a new TestSubscriber/TestObserver instance\.", "返回新的 TestSubscriber/TestObserver 实例。"),
    (r"Returns the list of values received so far\.", "返回目前已收到的值列表。"),
    (r"Returns the list of errors received so far\.", "返回目前已收到的错误列表。"),
    (r"Returns the number of \{@code onComplete\} events received so far\.", "返回目前已收到的 onComplete 次数。"),
    (r"Returns the last thread that interacted with this TestSubscriber/TestObserver\.",
     "返回最后与本测试消费者交互的线程。"),
    (r"Returns the tag associated with this test consumer\.", "返回关联的 tag。"),
    (r"Sets a tag for this test consumer\.", "为本测试消费者设置 tag。"),
    (r"Use \{@link #from\(Publisher\)\} to start processing a regular \{@link Publisher\} in 'rails'\.",
     "使用 {@link #from(Publisher)} 将普通 Publisher 拆分为多条 rail 并行处理。"),
    (r"Use \{@link #runOn\(Scheduler\)\} to introduce where each 'rail' should run on thread-vise\.",
     "使用 {@link #runOn(Scheduler)} 指定每条 rail 运行的 Scheduler。"),
    (r"Use \{@link #sequential\(\)\} to merge the sources back into a single \{@link Flowable\}\.",
     "使用 {@link #sequential()} 将各 rail 合并回单一 Flowable。"),
]

BULK_LINE_TRANSLATIONS: list[tuple[str, str]] = [
    ("Returns the current", "返回当前"),
    ("Returns true if", "若满足条件则返回 true："),
    ("Returns the", "返回"),
    ("Returns a", "返回一个"),
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
    ("@return the", "@return "),
    ("@param subscribers the subscribers", "@param subscribers 订阅者数组"),
    ("@param scheduler the", "@param scheduler 目标 Scheduler"),
    ("@param source the", "@param source 上游源"),
    ("@param enable enable or disable", "@param enable 启用或禁用"),
    ("@throws NullPointerException if", "@throws NullPointerException 若参数为 null"),
    ("@throws IllegalArgumentException if", "@throws IllegalArgumentException 若参数非法"),
    ("@since", "@since"),
    ("Backpressure:", "背压："),
    ("Scheduler:", "调度器："),
    ("may be null", "可能为 null"),
    ("must not be null", "不可为 null"),
    ("the subscriber", "订阅者"),
    ("the subscribers", "订阅者数组"),
    ("the scheduler", "Scheduler"),
    ("the queue", "队列"),
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
    ("the tag", "tag 标签"),
]

METHOD_COMMENT_PATTERNS: list[tuple[str, str]] = [
    (r"(\s+)public static (\w+) (\w+)\(", r"\1/** 静态方法 \3：\2 返回值工具入口。 */\n\1public static \2 \3("),
    (r"(\s+)public static void (\w+)\(", r"\1/** 静态方法 \2：配置或执行 hook。 */\n\1public static void \2("),
    (r"(\s+)public static boolean (\w+)\(", r"\1/** 静态方法 \2：返回插件/配置布尔状态。 */\n\1public static boolean \2("),
    (r"(\s+)public (\w+) (\w+)\(", r"\1/** 实例方法 \2：测试断言或状态查询。 */\n\1public \2 \3("),
    (r"(\s+)public final (\w+) (\w+)\(", r"\1/** 方法 \2：ParallelFlowable 算子或工厂。 */\n\1public final \2 \3("),
    (r"(\s+)protected final (\w+) (\w+)\(", r"\1/** 受保护方法 \2：parallel 内部工具。 */\n\1protected final \2 \3("),
    (r"(\s+)@Override\n\s+protected void subscribeActual\(", r"\1/** 订阅核心逻辑：组装内部 Subscriber 并连接上游。 */\n\1@Override\n\1protected void subscribeActual("),
    (r"(\s+)@Override\n\s+public void subscribeActual\(", r"\1/** 订阅核心逻辑：组装内部 Subscriber 并连接上游。 */\n\1@Override\n\1public void subscribeActual("),
    (r"(\s+)@Override\n\s+public void subscribe\(Subscriber", r"\1/** 校验 subscribers 并为每条 rail 建立订阅链。 */\n\1@Override\n\1public void subscribe(Subscriber"),
    (r"(\s+)@Override\n\s+public abstract void subscribe\(Subscriber", r"\1/** 向并行 Subscriber 数组发起订阅。 */\n\1@Override\n\1public abstract void subscribe(Subscriber"),
    (r"(\s+)@Override\n\s+public int parallelism\(\)", r"\1/** 返回并行度（期望的 Subscriber 数量）。 */\n\1@Override\n\1public int parallelism()"),
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
    (r"(\s+)@Override\n\s+public Disposable scheduleDirect\(", r"\1/** 在 Scheduler 上直接调度 Runnable。 */\n\1@Override\n\1public Disposable scheduleDirect("),
    (r"(\s+)@Override\n\s+public Disposable schedulePeriodicallyDirect\(", r"\1/** 在 Scheduler 上直接周期调度 Runnable。 */\n\1@Override\n\1public Disposable schedulePeriodicallyDirect("),
    (r"(\s+)@Override\n\s+public Worker createWorker\(\)", r"\1/** 创建绑定本 Scheduler 的 Worker。 */\n\1@Override\n\1public Worker createWorker()"),
    (r"(\s+)void drain\(\)", r"\1/** drain 循环：按 request 从队列取元素发射。 */\n\1void drain()"),
    (r"(\s+)void innerComplete\(\)", r"\1/** inner 完成：更新状态并继续 drain/切换。 */\n\1void innerComplete()"),
    (r"(\s+)void innerError\(Throwable", r"\1/** inner 错误：按 delayError 策略合并或立即终止。 */\n\1void innerError(Throwable"),
    (r"(\s+)void innerSuccess\(", r"\1/** inner onSuccess：缓存结果并触发 drain。 */\n\1void innerSuccess("),
    (r"(\s+)static final class (\w+)", r"\1/** 内部实现类 \2。 */\n\1static final class \2"),
    (r"(\s+)static final class (\w+) extends", r"\1/** 内部 \1 实现。 */\n\1static final class \2 extends"),
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
    return block.replace("/**\n", "/**\n * 【说明】RxJava 内部 API。\n", 1)


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
    summary = FILE_SUMMARIES.get(name, f"RxJava 内部组件 {class_name}。")
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

    # field-level one-liner javadocs
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
            "source": "上游 ParallelFlowable",
            "parent": "上游 ParallelFlowable",
            "scheduler": "目标 Scheduler",
            "prefetch": "预取/队列容量",
            "executor": "底层 Executor",
            "executorSupplier": "Executor Supplier",
            "interruptibleWorker": "Worker 是否可中断",
            "fair": "是否公平调度任务",
            "factory": "ThreadFactory",
            "parallelism": "并行度/线程池大小",
            "tracking": "是否跟踪 Worker 状态",
            "subscribers": "Subscriber 数组",
        }
        for p in params:
            desc = param_desc.get(p, f"{p} 参数")
            lines.append(f"{indent} * @param {p} {desc}")
        lines.append(f"{indent} */")
        return "\n".join(lines) + "\n" + sig

    text = re.sub(
        rf"^(\s+)public {re.escape(class_name)}\([^;{{]+\)\s*\{{",
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
        '"""Chinese OCA + JavaDoc replacements for RxJava 4.0.0-alpha-21 wave32b mega batch [10:20]."""',
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
