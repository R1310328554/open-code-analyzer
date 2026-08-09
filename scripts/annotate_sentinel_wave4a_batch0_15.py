#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-4a slots [0:15]."""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "sentinel/1.8.10"
ANALYZED = VER / "analyzed"
ORIGINAL = VER / "original"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text(encoding="utf-8"))["files"][:15]

R: dict[str, list[tuple[str, str]]] = {}

R["AbstractLinkedProcessorSlot.java"] = [
    (
        "/**\n * @author qinan.qn\n * @author jialiang.linjl\n */",
        "/**\n * 处理器槽链表的抽象基类，以链表方式串联各 {@link ProcessorSlot}。\n * <p>通过 {@link #fireEntry} / {@link #fireExit} 将调用传递给下一个槽位。</p>\n *\n * @author qinan.qn\n * @author jialiang.linjl\n */",
    ),
    (
        "    @Override\n    public void fireEntry(Context context, ResourceWrapper resourceWrapper, Object obj, int count, boolean prioritized, Object... args)",
        "    /** 触发链中下一个槽位的 entry 处理。 */\n    @Override\n    public void fireEntry(Context context, ResourceWrapper resourceWrapper, Object obj, int count, boolean prioritized, Object... args)",
    ),
    (
        "    @SuppressWarnings(\"unchecked\")\n    void transformEntry(Context context, ResourceWrapper resourceWrapper, Object o, int count, boolean prioritized, Object... args)",
        "    /** 将入参转换为泛型类型后调用本槽位的 {@link #entry}。 */\n    @SuppressWarnings(\"unchecked\")\n    void transformEntry(Context context, ResourceWrapper resourceWrapper, Object o, int count, boolean prioritized, Object... args)",
    ),
    (
        "    @Override\n    public void fireExit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {",
        "    /** 触发链中下一个槽位的 exit 处理。 */\n    @Override\n    public void fireExit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {",
    ),
    (
        "    public AbstractLinkedProcessorSlot<?> getNext() {",
        "    /** 获取链表中的下一个处理器槽。 */\n    public AbstractLinkedProcessorSlot<?> getNext() {",
    ),
    (
        "    public void setNext(AbstractLinkedProcessorSlot<?> next) {",
        "    /** 设置链表中的下一个处理器槽。 */\n    public void setNext(AbstractLinkedProcessorSlot<?> next) {",
    ),
]

R["DefaultProcessorSlotChain.java"] = [
    (
        "/**\n * @author qinan.qn\n * @author jialiang.linjl\n */",
        "/**\n * {@link ProcessorSlotChain} 的默认实现，维护首尾指针以支持槽位增删。\n *\n * @author qinan.qn\n * @author jialiang.linjl\n */",
    ),
    (
        "    @Override\n    public void addFirst(AbstractLinkedProcessorSlot<?> protocolProcessor) {",
        "    /** 在槽链头部插入处理器槽。 */\n    @Override\n    public void addFirst(AbstractLinkedProcessorSlot<?> protocolProcessor) {",
    ),
    (
        "    @Override\n    public void addLast(AbstractLinkedProcessorSlot<?> protocolProcessor) {",
        "    /** 在槽链尾部追加处理器槽。 */\n    @Override\n    public void addLast(AbstractLinkedProcessorSlot<?> protocolProcessor) {",
    ),
    (
        "    /**\n     * Same as {@link #addLast(AbstractLinkedProcessorSlot)}.\n     *\n     * @param next processor to be added.\n     */",
        "    /**\n     * 与 {@link #addLast(AbstractLinkedProcessorSlot)} 等价。\n     *\n     * @param next 待追加的处理器槽\n     */",
    ),
    (
        "    @Override\n    public AbstractLinkedProcessorSlot<?> getNext() {",
        "    /** 返回链中第一个实际业务槽（跳过头节点）。 */\n    @Override\n    public AbstractLinkedProcessorSlot<?> getNext() {",
    ),
    (
        "    @Override\n    public void entry(Context context, ResourceWrapper resourceWrapper, Object t, int count, boolean prioritized, Object... args)",
        "    /** 从链头开始执行 entry 流程。 */\n    @Override\n    public void entry(Context context, ResourceWrapper resourceWrapper, Object t, int count, boolean prioritized, Object... args)",
    ),
    (
        "    @Override\n    public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {",
        "    /** 从链头开始执行 exit 流程。 */\n    @Override\n    public void exit(Context context, ResourceWrapper resourceWrapper, int count, Object... args) {",
    ),
]

R["MethodResourceWrapper.java"] = [
    (
        "/**\n * Resource wrapper for method invocation.\n *\n * @author qinan.qn\n */",
        "/**\n * 方法调用的资源包装器，以反射 {@link Method} 标识受保护资源。\n *\n * @author qinan.qn\n */",
    ),
    (
        "    public MethodResourceWrapper(Method method, EntryType e) {",
        "    /** 使用默认资源类型构造方法资源包装器。 */\n    public MethodResourceWrapper(Method method, EntryType e) {",
    ),
    (
        "    public MethodResourceWrapper(Method method, EntryType e, int resType) {",
        "    /** 指定资源类型构造方法资源包装器。 */\n    public MethodResourceWrapper(Method method, EntryType e, int resType) {",
    ),
    (
        "    public Method getMethod() {",
        "    /** 获取被包装的 {@link Method}。 */\n    public Method getMethod() {",
    ),
    (
        "    @Override\n    public String getShowName() {",
        "    /** 返回用于展示的资源名称。 */\n    @Override\n    public String getShowName() {",
    ),
]

R["ProcessorSlot.java"] = [
    (
        "/**\n * A container of some process and ways of notification when the process is finished.\n *\n * @author qinan.qn\n * @author jialiang.linjl\n * @author leyou(lihao)\n * @author Eric Zhao\n */",
        "/**\n * 处理器槽接口：封装一段处理逻辑，并提供 entry/exit 完成后的链式通知机制。\n *\n * @author qinan.qn\n * @author jialiang.linjl\n * @author leyou(lihao)\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Entrance of this slot.\n     *\n     * @param context         current {@link Context}\n     * @param resourceWrapper current resource\n     * @param param           generics parameter, usually is a {@link com.alibaba.csp.sentinel.node.Node}\n     * @param count           tokens needed\n     * @param prioritized     whether the entry is prioritized\n     * @param args            parameters of the original call\n     * @throws Throwable blocked exception or unexpected error\n     */",
        "    /**\n     * 本槽位的入口处理。\n     *\n     * @param context         当前 {@link Context}\n     * @param resourceWrapper 当前资源\n     * @param param           泛型参数，通常为 {@link com.alibaba.csp.sentinel.node.Node}\n     * @param count           所需令牌数\n     * @param prioritized     是否为优先级入口\n     * @param args            原始调用的参数\n     * @throws Throwable 被阻断或发生意外错误\n     */",
    ),
    (
        "    /**\n     * Means finish of {@link #entry(Context, ResourceWrapper, Object, int, boolean, Object...)}.\n     *\n     * @param context         current {@link Context}\n     * @param resourceWrapper current resource\n     * @param obj             relevant object (e.g. Node)\n     * @param count           tokens needed\n     * @param prioritized     whether the entry is prioritized\n     * @param args            parameters of the original call\n     * @throws Throwable blocked exception or unexpected error\n     */",
        "    /**\n     * 表示本槽位 {@link #entry(Context, ResourceWrapper, Object, int, boolean, Object...)} 处理完成，\n     * 继续触发后续槽位。\n     *\n     * @param context         当前 {@link Context}\n     * @param resourceWrapper 当前资源\n     * @param obj             相关对象（如 Node）\n     * @param count           所需令牌数\n     * @param prioritized     是否为优先级入口\n     * @param args            原始调用的参数\n     * @throws Throwable 被阻断或发生意外错误\n     */",
    ),
    (
        "    /**\n     * Exit of this slot.\n     *\n     * @param context         current {@link Context}\n     * @param resourceWrapper current resource\n     * @param count           tokens needed\n     * @param args            parameters of the original call\n     */",
        "    /**\n     * 本槽位的退出处理。\n     *\n     * @param context         当前 {@link Context}\n     * @param resourceWrapper 当前资源\n     * @param count           所需令牌数\n     * @param args            原始调用的参数\n     */",
    ),
    (
        "    /**\n     * Means finish of {@link #exit(Context, ResourceWrapper, int, Object...)}.\n     *\n     * @param context         current {@link Context}\n     * @param resourceWrapper current resource\n     * @param count           tokens needed\n     * @param args            parameters of the original call\n     */",
        "    /**\n     * 表示本槽位 {@link #exit(Context, ResourceWrapper, int, Object...)} 处理完成，\n     * 继续触发后续槽位。\n     *\n     * @param context         当前 {@link Context}\n     * @param resourceWrapper 当前资源\n     * @param count           所需令牌数\n     * @param args            原始调用的参数\n     */",
    ),
]

R["ProcessorSlotChain.java"] = [
    (
        "/**\n * Link all processor slots as a chain.\n *\n * @author qinan.qn\n */",
        "/**\n * 将所有 {@link ProcessorSlot} 串联成责任链的抽象基类。\n *\n * @author qinan.qn\n */",
    ),
    (
        "    /**\n     * Add a processor to the head of this slot chain.\n     *\n     * @param protocolProcessor processor to be added.\n     */",
        "    /**\n     * 在槽链头部插入处理器槽。\n     *\n     * @param protocolProcessor 待插入的处理器槽\n     */",
    ),
    (
        "    /**\n     * Add a processor to the tail of this slot chain.\n     *\n     * @param protocolProcessor processor to be added.\n     */",
        "    /**\n     * 在槽链尾部追加处理器槽。\n     *\n     * @param protocolProcessor 待追加的处理器槽\n     */",
    ),
]

R["ProcessorSlotEntryCallback.java"] = [
    (
        "/**\n * Callback for entering {@link com.alibaba.csp.sentinel.slots.statistic.StatisticSlot} (passed and blocked).\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
        "/**\n * 进入 {@link com.alibaba.csp.sentinel.slots.statistic.StatisticSlot} 时的回调（通过或被阻断）。\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
    ),
    (
        "    void onPass(Context context, ResourceWrapper resourceWrapper, T param, int count, Object... args) throws Exception;",
        "    /** 资源通过统计槽时的回调。 */\n    void onPass(Context context, ResourceWrapper resourceWrapper, T param, int count, Object... args) throws Exception;",
    ),
    (
        "    void onBlocked(BlockException ex, Context context, ResourceWrapper resourceWrapper, T param, int count, Object... args);",
        "    /** 资源被 Sentinel 阻断时的回调。 */\n    void onBlocked(BlockException ex, Context context, ResourceWrapper resourceWrapper, T param, int count, Object... args);",
    ),
]

R["ProcessorSlotExitCallback.java"] = [
    (
        "/**\n * Callback for exiting {@link com.alibaba.csp.sentinel.slots.statistic.StatisticSlot} (passed and blocked).\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
        "/**\n * 退出 {@link com.alibaba.csp.sentinel.slots.statistic.StatisticSlot} 时的回调。\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
    ),
    (
        "    void onExit(Context context, ResourceWrapper resourceWrapper, int count, Object... args);",
        "    /** 资源退出统计槽时的回调。 */\n    void onExit(Context context, ResourceWrapper resourceWrapper, int count, Object... args);",
    ),
]

R["ResourceWrapper.java"] = [
    (
        "/**\n * A wrapper of resource name and type.\n *\n * @author qinan.qn\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
        "/**\n * 资源名称与类型的包装器，作为 Slot 链处理的基本输入。\n *\n * @author qinan.qn\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Get the resource name.\n     *\n     * @return the resource name\n     */",
        "    /**\n     * 获取资源名称。\n     *\n     * @return 资源名称\n     */",
    ),
    (
        "    /**\n     * Get {@link EntryType} of this wrapper.\n     *\n     * @return {@link EntryType} of this wrapper.\n     */",
        "    /**\n     * 获取本包装器的 {@link EntryType}。\n     *\n     * @return 入口类型\n     */",
    ),
    (
        "    /**\n     * Get the classification of this resource.\n     *\n     * @return the classification of this resource\n     * @since 1.7.0\n     */",
        "    /**\n     * 获取资源分类。\n     *\n     * @return 资源分类标识\n     * @since 1.7.0\n     */",
    ),
    (
        "    /**\n     * Get the beautified resource name to be showed.\n     *\n     * @return the beautified resource name\n     */",
        "    /**\n     * 获取用于展示的友好资源名称。\n     *\n     * @return 展示用资源名称\n     */",
    ),
    (
        "    /**\n     * Only {@link #getName()} is considered.\n     */\n    @Override\n    public int hashCode() {",
        "    /**\n     * 仅依据 {@link #getName()} 计算哈希值。\n     */\n    @Override\n    public int hashCode() {",
    ),
    (
        "    /**\n     * Only {@link #getName()} is considered.\n     */\n    @Override\n    public boolean equals(Object obj) {",
        "    /**\n     * 仅依据 {@link #getName()} 判断相等性。\n     */\n    @Override\n    public boolean equals(Object obj) {",
    ),
]

R["SlotChainBuilder.java"] = [
    (
        "/**\n * The builder for processor slot chain.\n *\n * @author qinan.qn\n * @author leyou\n * @author Eric Zhao\n */",
        "/**\n * 处理器槽链的构建器 SPI 接口。\n *\n * @author qinan.qn\n * @author leyou\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Build the processor slot chain.\n     *\n     * @return a processor slot that chain some slots together\n     */",
        "    /**\n     * 构建处理器槽链。\n     *\n     * @return 串联多个槽位的处理器链\n     */",
    ),
]

R["SlotChainProvider.java"] = [
    (
        "/**\n * A provider for creating slot chains via resolved slot chain builder SPI.\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
        "/**\n * 通过 SPI 解析 {@link SlotChainBuilder} 并创建槽链的全局提供者。\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
    ),
    (
        "    /**\n     * The load and pick process is not thread-safe, but it's okay since the method should be only invoked\n     * via {@code lookProcessChain} in {@link com.alibaba.csp.sentinel.CtSph} under lock.\n     *\n     * @return new created slot chain\n     */",
        "    /**\n     * 加载并创建新的槽链。加载过程非线程安全，\n     * 但仅在 {@link com.alibaba.csp.sentinel.CtSph} 的 {@code lookProcessChain} 加锁调用，因此可接受。\n     *\n     * @return 新创建的槽链\n     */",
    ),
    (
        "        // Resolve the slot chain builder SPI.",
        "        // 通过 SPI 解析 SlotChainBuilder 实现。",
    ),
    (
        "            // Should not go through here.",
        "            // 不应走到此分支，回退到默认构建器。",
    ),
]

R["StringResourceWrapper.java"] = [
    (
        "/**\n * Common string resource wrapper.\n *\n * @author qinan.qn\n * @author jialiang.linjl\n */",
        "/**\n * 以字符串标识资源的通用包装器。\n *\n * @author qinan.qn\n * @author jialiang.linjl\n */",
    ),
    (
        "    public StringResourceWrapper(String name, EntryType e) {",
        "    /** 使用默认资源类型构造字符串资源包装器。 */\n    public StringResourceWrapper(String name, EntryType e) {",
    ),
    (
        "    public StringResourceWrapper(String name, EntryType e, int resType) {",
        "    /** 指定资源类型构造字符串资源包装器。 */\n    public StringResourceWrapper(String name, EntryType e, int resType) {",
    ),
    (
        "    @Override\n    public String getShowName() {",
        "    /** 返回用于展示的资源名称。 */\n    @Override\n    public String getShowName() {",
    ),
]

R["DefaultSlotChainBuilder.java"] = [
    (
        "/**\n * Builder for a default {@link ProcessorSlotChain}.\n *\n * @author qinan.qn\n * @author leyou\n */",
        "/**\n * 默认 {@link ProcessorSlotChain} 的构建器，通过 SPI 加载并排序全部 {@link ProcessorSlot}。\n *\n * @author qinan.qn\n * @author leyou\n */",
    ),
    (
        "    @Override\n    public ProcessorSlotChain build() {",
        "    /** 按 SPI 顺序组装默认槽链。 */\n    @Override\n    public ProcessorSlotChain build() {",
    ),
]

R["AbstractRule.java"] = [
    (
        "/**\n * Abstract rule entity.\n *\n * @author youji.zj\n * @author Eric Zhao\n */",
        "/**\n * 规则实体的抽象基类，封装资源名、来源应用等公共字段。\n *\n * @author youji.zj\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * rule id.\n     */",
        "    /**\n     * 规则 ID。\n     */",
    ),
    (
        "    /**\n     * Resource name.\n     */",
        "    /**\n     * 资源名称。\n     */",
    ),
    (
        "    /**\n     * <p>\n     * Application name that will be limited by origin.\n     * The default limitApp is {@code default}, which means allowing all origin apps.\n     * </p>\n     * <p>\n     * For authority rules, multiple origin name can be separated with comma (',').\n     * </p>\n     */",
        "    /**\n     * <p>\n     * 按来源限流/授权的应用名称。\n     * 默认值为 {@code default}，表示允许全部来源应用。\n     * </p>\n     * <p>\n     * 授权规则中多个来源名可用逗号（{@code ,}）分隔。\n     * </p>\n     */",
    ),
    (
        "    /**\n     * Whether to match resource names according to regular rules\n     */",
        "    /**\n     * 是否使用正则匹配资源名称。\n     */",
    ),
    (
        "    public <T extends AbstractRule> T as(Class<T> clazz) {",
        "    /** 将本规则安全转型为指定子类型。 */\n    public <T extends AbstractRule> T as(Class<T> clazz) {",
    ),
]

R["BlockException.java"] = [
    (
        "/**\n * Abstract exception indicating blocked by Sentinel due to flow control,\n * circuit breaking or system protection triggered.\n *\n * @author youji.zj\n */",
        "/**\n * Sentinel 阻断异常的抽象基类，表示因流控、熔断或系统保护而被拦截。\n *\n * @author youji.zj\n */",
    ),
    (
        "    /**\n     * <p>this constant RuntimeException has no stack trace, just has a message\n     * {@link #BLOCK_EXCEPTION_FLAG} that marks its name.\n     * </p>\n     * <p>\n     * Use {@link #isBlockException(Throwable)} to check whether one Exception\n     * Sentinel Blocked Exception.\n     * </p>\n     */",
        "    /**\n     * <p>无堆栈的占位 {@link RuntimeException}，消息为 {@link #BLOCK_EXCEPTION_FLAG}，\n     * 用于快速抛出阻断信号。\n     * </p>\n     * <p>\n     * 请使用 {@link #isBlockException(Throwable)} 判断异常是否为 Sentinel 阻断异常。\n     * </p>\n     */",
    ),
    (
        "    @Override\n    public Throwable fillInStackTrace() {",
        "    /** 不填充堆栈，降低阻断异常的开销。 */\n    @Override\n    public Throwable fillInStackTrace() {",
    ),
    (
        "    public RuntimeException toRuntimeException() {",
        "    /** 转换为带 Sentinel 前缀的运行时异常。 */\n    public RuntimeException toRuntimeException() {",
    ),
    (
        "    /**\n     * Check whether the exception is sentinel blocked exception. One exception is sentinel blocked\n     * exception only when:\n     * <ul>\n     * <li>the exception or its (sub-)cause is {@link BlockException}, or</li>\n     * <li>the exception's message or any of its sub-cause's message is prefixed by {@link #BLOCK_EXCEPTION_FLAG}</li>\n     * </ul>\n     *\n     * @param t the exception.\n     * @return return true if the exception marks sentinel blocked exception.\n     */",
        "    /**\n     * 判断异常是否为 Sentinel 阻断异常。满足以下任一条件即为阻断异常：\n     * <ul>\n     * <li>异常或其（子）cause 为 {@link BlockException}；或</li>\n     * <li>异常或其子 cause 的消息以 {@link #BLOCK_EXCEPTION_FLAG} 为前缀。</li>\n     * </ul>\n     *\n     * @param t 待检查的异常\n     * @return 若为 Sentinel 阻断异常则返回 true\n     */",
    ),
    (
        "    public AbstractRule getRule() {",
        "    /** 获取触发阻断的规则（若有）。 */\n    public AbstractRule getRule() {",
    ),
]

R["ClusterRuleConstant.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群流控规则相关常量。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    public static final int FLOW_CLUSTER_STRATEGY_NORMAL = 0;",
        "    /** 普通集群流控策略。 */\n    public static final int FLOW_CLUSTER_STRATEGY_NORMAL = 0;",
    ),
    (
        "    public static final int FLOW_CLUSTER_STRATEGY_BORROW_REF = 1;",
        "    /** 借用令牌（Ref）集群流控策略。 */\n    public static final int FLOW_CLUSTER_STRATEGY_BORROW_REF = 1;",
    ),
    (
        "    public static final int FLOW_THRESHOLD_AVG_LOCAL = 0;",
        "    /** 阈值按本地实例平均分配。 */\n    public static final int FLOW_THRESHOLD_AVG_LOCAL = 0;",
    ),
    (
        "    public static final int FLOW_THRESHOLD_GLOBAL = 1;",
        "    /** 阈值为集群全局阈值。 */\n    public static final int FLOW_THRESHOLD_GLOBAL = 1;",
    ),
    (
        "    public static final int DEFAULT_CLUSTER_SAMPLE_COUNT = 10;",
        "    /** 集群流控默认采样窗口数。 */\n    public static final int DEFAULT_CLUSTER_SAMPLE_COUNT = 10;",
    ),
]


def apply(text: str, reps: list[tuple[str, str]]) -> str:
    for old, new in reps:
        if old in text:
            text = text.replace(old, new)
    return text


def ensure_analyzed(rel: str) -> Path:
    dst = ANALYZED / rel
    if not dst.exists():
        src = ORIGINAL / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        dst.write_text(src.read_text(encoding="utf-8"), encoding="utf-8")
    return dst


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


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        dst = ensure_analyzed(rel)
        name = Path(rel).name
        text = dst.read_text(encoding="utf-8")
        if len(re.findall(r"[\u4e00-\u9fff]", text)) >= 10:
            ok += 1
            print(f"SKIP {rel}")
            continue
        text = apply(text, R.get(name, []))
        cn = len(re.findall(r"[\u4e00-\u9fff]", text))
        if cn < 10 or "Licensed under the Apache License" not in text:
            failures.append(f"VALIDATION cn={cn}: {rel}")
            print(f"FAIL cn={cn} {rel}")
            continue
        dst.write_text(text, encoding="utf-8")
        ok += 1
        print(f"OK cn={cn} {rel}")
    if not failures:
        mark_queue_done(BATCH_FILES)
        print(f"Marked {len(BATCH_FILES)} files done in queue")
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
