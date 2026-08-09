#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-3a log/metric/node [0:15]."""
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

R["CspFormatter.java"] = [
    (
        "/**\n * @author xuyue\n */",
        "/**\n * Sentinel JUL 日志格式化器。\n * <p>输出格式：{@code 时间戳 级别 消息 [堆栈]}</p>\n *\n * @author xuyue\n */",
    ),
]

R["DateFileLogHandler.java"] = [
    (
        "class DateFileLogHandler extends Handler {",
        "/**\n * 按日期滚动的异步文件日志 Handler。\n * <p>使用线程池异步写入 {@link java.util.logging.FileHandler}，\n * 并在跨日或日志文件缺失时自动切换新文件。</p>\n */\nclass DateFileLogHandler extends Handler {",
    ),
    (
        "        // allow all thread could be stopped",
        "        // 允许核心线程超时退出，便于 JVM 关闭时回收。",
    ),
    (
        "            // When file count is not 1, the first log file name will end with \".0\"",
        "            // 当文件数量不为 1 时，首个日志文件名以 \".0\" 结尾。",
    ),
    (
        "        // Get current date.",
        "        // 获取当前日期。",
    ),
    (
        "        // Begin of next date.",
        "        // 计算次日零点，作为当前日志文件的有效截止时间。",
    ),
    (
        "        /**\n         * The period of logged rejected records.\n         */",
        "        /**\n         * 被拒绝任务日志的记录周期（毫秒）。\n         */",
    ),
]

R["FormattingTuple.java"] = [
    (
        "/**\n * Holds the results of formatting done by {@link MessageFormatter}.\n *\n * @author Joern Huxhorn\n */",
        "/**\n * 保存 {@link MessageFormatter} 格式化后的结果（消息、参数数组、异常）。\n *\n * @author Joern Huxhorn\n */",
    ),
    (
        "    static public FormattingTuple NULL = new FormattingTuple(null);",
        "    /** 表示空消息的占位实例。 */\n    static public FormattingTuple NULL = new FormattingTuple(null);",
    ),
    (
        "    public String getMessage() {",
        "    /** 获取格式化后的消息文本。 */\n    public String getMessage() {",
    ),
    (
        "    public Object[] getArgArray() {",
        "    /** 获取格式化时使用的参数数组。 */\n    public Object[] getArgArray() {",
    ),
    (
        "    public Throwable getThrowable() {",
        "    /** 获取关联的异常（若有）。 */\n    public Throwable getThrowable() {",
    ),
]

R["JavaLoggingAdapter.java"] = [
    (
        "/**\n * JUL adapter for Sentinel {@link Logger} SPI.\n *\n * @author Eric Zhao\n * @since 1.7.2\n */",
        "/**\n * Sentinel {@link Logger} SPI 的 JUL 适配器。\n * <p>将 Sentinel 日志 API 委托给 {@code java.util.logging}，并按配置创建文件或控制台 Handler。</p>\n *\n * @author Eric Zhao\n * @since 1.7.2\n */",
    ),
    (
        "    public String getLoggerName() {",
        "    /** 获取底层 JUL Logger 名称。 */\n    public String getLoggerName() {",
    ),
    (
        "    public String getFileNamePattern() {",
        "    /** 获取日志文件名模式。 */\n    public String getFileNamePattern() {",
    ),
]

R["Level.java"] = [
    (
        "/**\n * JUL logging levels.\n *\n * @author xue8\n */",
        "/**\n * Sentinel 自定义 JUL 日志级别，与 SLF4J 语义对齐。\n *\n * @author xue8\n */",
    ),
    (
        "    public static final Level ERROR = new Level(\"ERROR\", 1000);",
        "    /** 错误级别。 */\n    public static final Level ERROR = new Level(\"ERROR\", 1000);",
    ),
    (
        "    public static final Level WARNING = new Level(\"WARNING\", 900);",
        "    /** 警告级别。 */\n    public static final Level WARNING = new Level(\"WARNING\", 900);",
    ),
    (
        "    public static final Level INFO = new Level(\"INFO\", 800);",
        "    /** 信息级别。 */\n    public static final Level INFO = new Level(\"INFO\", 800);",
    ),
    (
        "    public static final Level DEBUG = new Level(\"DEBUG\", 700);",
        "    /** 调试级别。 */\n    public static final Level DEBUG = new Level(\"DEBUG\", 700);",
    ),
    (
        "    public static final Level TRACE = new Level(\"TRACE\", 600);",
        "    /** 跟踪级别。 */\n    public static final Level TRACE = new Level(\"TRACE\", 600);",
    ),
]

R["AdvancedMetricExtension.java"] = [
    (
        "/**\n * Extended {@link MetricExtension} extending input parameters of each metric\n * collection method with {@link EntryType}.\n *\n * @author bill_yip\n * @author Eric Zhao\n * @since 1.8.0\n */",
        "/**\n * 扩展版 {@link MetricExtension}，各指标采集方法的入参携带 {@link ResourceWrapper} 等更丰富上下文。\n *\n * @author bill_yip\n * @author Eric Zhao\n * @since 1.8.0\n */",
    ),
    (
        "    /**\n     * Add current pass count of the resource name.\n     *\n     * @param rw          resource representation (including resource name, traffic type, etc.)\n     * @param batchCount  count to add\n     * @param args        additional arguments of the resource, eg. if the resource is a method name,\n     *                    the args will be the parameters of the method.\n     */",
        "    /**\n     * 累加资源通过（放行）次数。\n     *\n     * @param rw          资源表示（含资源名、流量类型等）\n     * @param batchCount  待累加次数\n     * @param args        资源附加参数；若资源为方法名，则为方法参数\n     */",
    ),
    (
        "    /**\n     * Add current block count of the resource name.\n     *\n     * @param rw         resource representation (including resource name, traffic type, etc.)\n     * @param batchCount count to add\n     * @param origin     the origin of caller (if present)\n     * @param e          the associated {@code BlockException}\n     * @param args       additional arguments of the resource, eg. if the resource is a method name,\n     *                   the args will be the parameters of the method.\n     */",
        "    /**\n     * 累加资源被限流/阻断次数。\n     *\n     * @param rw         资源表示（含资源名、流量类型等）\n     * @param batchCount 待累加次数\n     * @param origin     调用方来源（若有）\n     * @param e          关联的 {@code BlockException}\n     * @param args       资源附加参数；若资源为方法名，则为方法参数\n     */",
    ),
    (
        "    /**\n     * Add current completed count of the resource name.\n     *\n     * @param rw         resource representation (including resource name, traffic type, etc.)\n     * @param batchCount count to add\n     * @param rt         response time of current invocation\n     * @param args       additional arguments of the resource\n     */",
        "    /**\n     * 累加资源调用完成次数。\n     *\n     * @param rw         资源表示（含资源名、流量类型等）\n     * @param batchCount 待累加次数\n     * @param rt         本次调用响应时间\n     * @param args       资源附加参数\n     */",
    ),
    (
        "    /**\n     * Add current exception count of the resource name.\n     *\n     * @param rw         resource representation (including resource name, traffic type, etc.)\n     * @param batchCount count to add\n     * @param throwable  exception related.\n     * @param args       additional arguments of the resource\n     */",
        "    /**\n     * 累加资源调用异常次数。\n     *\n     * @param rw         资源表示（含资源名、流量类型等）\n     * @param batchCount 待累加次数\n     * @param throwable  关联异常\n     * @param args       资源附加参数\n     */",
    ),
]

R["MetricCallbackInit.java"] = [
    (
        "/**\n * Register callbacks for metric extension.\n *\n * @author Carpenter Lee\n * @since 1.6.1\n */",
        "/**\n * 注册指标扩展所需的 StatisticSlot 回调。\n *\n * @author Carpenter Lee\n * @since 1.6.1\n */",
    ),
]

R["MetricExtension.java"] = [
    (
        "/**\n * This interface provides extension to Sentinel internal statistics.\n * <p>\n * Please note that all method in this class will invoke in the same thread of biz logic.\n * It's necessary to not do time-consuming operation in any of the interface's method,\n * otherwise biz logic will be blocked.\n * </p>\n *\n * @author Carpenter Lee\n * @since 1.6.1\n */",
        "/**\n * 为 Sentinel 内部统计提供扩展点的接口。\n * <p>\n * 所有方法均在业务逻辑同一线程中调用，请勿执行耗时操作，否则会阻塞业务线程。\n * </p>\n *\n * @author Carpenter Lee\n * @since 1.6.1\n */",
    ),
    (
        "    /**\n     * Add current pass count of the resource name.\n     *\n     * @param n        count to add\n     * @param resource resource name\n     * @param args     additional arguments of the resource, eg. if the resource is a method name,\n     *                 the args will be the parameters of the method.\n     */",
        "    /**\n     * 累加资源通过（放行）次数。\n     *\n     * @param n        待累加次数\n     * @param resource 资源名\n     * @param args     资源附加参数；若资源为方法名，则为方法参数\n     */",
    ),
    (
        "    /**\n     * Add current block count of the resource name.\n     *\n     * @param n              count to add\n     * @param resource       resource name\n     * @param origin         the original invoker.\n     * @param blockException block exception related.\n     * @param args           additional arguments of the resource, eg. if the resource is a method name,\n     *                       the args will be the parameters of the method.\n     */",
        "    /**\n     * 累加资源被限流/阻断次数。\n     *\n     * @param n              待累加次数\n     * @param resource       资源名\n     * @param origin         调用方来源\n     * @param blockException 关联的阻断异常\n     * @param args           资源附加参数；若资源为方法名，则为方法参数\n     */",
    ),
    (
        "    /**\n     * Add current completed count of the resource name.\n     *\n     * @param n        count to add\n     * @param resource resource name\n     * @param args     additional arguments of the resource, eg. if the resource is a method name,\n     *                 the args will be the parameters of the method.\n     */",
        "    /**\n     * 累加资源调用成功完成次数。\n     *\n     * @param n        待累加次数\n     * @param resource 资源名\n     * @param args     资源附加参数；若资源为方法名，则为方法参数\n     */",
    ),
    (
        "    /**\n     * Add current exception count of the resource name.\n     *\n     * @param n         count to add\n     * @param resource  resource name\n     * @param throwable exception related.\n     */",
        "    /**\n     * 累加资源调用异常次数。\n     *\n     * @param n         待累加次数\n     * @param resource  资源名\n     * @param throwable 关联异常\n     */",
    ),
    (
        "    /**\n     * Add response time of the resource name.\n     *\n     * @param rt       response time in millisecond\n     * @param resource resource name\n     * @param args     additional arguments of the resource, eg. if the resource is a method name,\n     *                 the args will be the parameters of the method.\n     */",
        "    /**\n     * 累加资源响应时间。\n     *\n     * @param rt       响应时间（毫秒）\n     * @param resource 资源名\n     * @param args     资源附加参数；若资源为方法名，则为方法参数\n     */",
    ),
    (
        "    /**\n     * Increase current thread count of the resource name.\n     *\n     * @param resource resource name\n     * @param args     additional arguments of the resource, eg. if the resource is a method name,\n     *                 the args will be the parameters of the method.\n     */",
        "    /**\n     * 增加资源当前并发线程数。\n     *\n     * @param resource 资源名\n     * @param args     资源附加参数；若资源为方法名，则为方法参数\n     */",
    ),
    (
        "    /**\n     * Decrease current thread count of the resource name.\n     *\n     * @param resource resource name\n     * @param args     additional arguments of the resource, eg. if the resource is a method name,\n     *                 the args will be the parameters of the method.\n     */",
        "    /**\n     * 减少资源当前并发线程数。\n     *\n     * @param resource 资源名\n     * @param args     资源附加参数；若资源为方法名，则为方法参数\n     */",
    ),
]

R["MetricExtensionProvider.java"] = [
    (
        "/**\n * Get all {@link MetricExtension} via SPI.\n *\n * @author Carpenter Lee\n * @since 1.6.1\n */",
        "/**\n * 通过 SPI 加载并持有全部 {@link MetricExtension} 实例。\n *\n * @author Carpenter Lee\n * @since 1.6.1\n */",
    ),
    (
        "    /**\n     * <p>Get all registered metric extensions.</p>\n     * <p>DO NOT MODIFY the returned list, use {@link #addMetricExtension(MetricExtension)}.</p>\n     *\n     * @return all registered metric extensions\n     */",
        "    /**\n     * <p>获取已注册的全部指标扩展。</p>\n     * <p>请勿直接修改返回列表，应使用 {@link #addMetricExtension(MetricExtension)}。</p>\n     *\n     * @return 已注册的全部指标扩展\n     */",
    ),
    (
        "    /**\n     * Add metric extension.\n     * <p>\n     * Note that this method is NOT thread safe.\n     * </p>\n     *\n     * @param metricExtension the metric extension to add.\n     */",
        "    /**\n     * 添加指标扩展。\n     * <p>\n     * 注意：本方法非线程安全。\n     * </p>\n     *\n     * @param metricExtension 待添加的指标扩展\n     */",
    ),
]

R["MetricEntryCallback.java"] = [
    (
        "/**\n * Metric extension entry callback.\n *\n * @author Carpenter Lee\n * @since 1.6.1\n */",
        "/**\n * 指标扩展的入口（entry）回调。\n * <p>在资源通过或阻断时，将事件分发给已注册的 {@link MetricExtension}。</p>\n *\n * @author Carpenter Lee\n * @since 1.6.1\n */",
    ),
]

R["MetricExitCallback.java"] = [
    (
        "/**\n * Metric extension exit callback.\n *\n * @author Carpenter Lee\n * @author Eric Zhao\n * @since 1.6.1\n */",
        "/**\n * 指标扩展的出口（exit）回调。\n * <p>在资源调用退出时上报 RT、成功、异常及线程数等指标。</p>\n *\n * @author Carpenter Lee\n * @author Eric Zhao\n * @since 1.6.1\n */",
    ),
    (
        "                // Since 1.8.0 (as a temporary workaround for compatibility)",
        "                // 自 1.8.0 起（兼容 AdvancedMetricExtension 的临时方案）",
    ),
]

R["ClusterNode.java"] = [
    (
        "/**\n * <p>\n * This class stores summary runtime statistics of the resource, including rt, thread count, qps\n * and so on. Same resource shares the same {@link ClusterNode} globally, no matter in which\n * {@link com.alibaba.csp.sentinel.context.Context}.\n * </p>\n * <p>\n * To distinguish invocation from different origin (declared in\n * {@link ContextUtil#enter(String name, String origin)}),\n * one {@link ClusterNode} holds an {@link #originCountMap}, this map holds {@link StatisticNode}\n * of different origin. Use {@link #getOrCreateOriginNode(String)} to get {@link Node} of the specific\n * origin.<br/>\n * Note that 'origin' usually is Service Consumer's app name.\n * </p>\n *\n * @author qinan.qn\n * @author jialiang.linjl\n */",
        "/**\n * <p>\n * 保存资源的汇总运行时统计（RT、线程数、QPS 等）。\n * 相同资源全局共享同一 {@link ClusterNode}，与所在 {@link com.alibaba.csp.sentinel.context.Context} 无关。\n * </p>\n * <p>\n * 为区分不同来源（origin，在 {@link ContextUtil#enter(String name, String origin)} 中声明）的调用，\n * 每个 {@link ClusterNode} 持有 {@link #originCountMap}，映射各来源对应的 {@link StatisticNode}。\n * 通过 {@link #getOrCreateOriginNode(String)} 获取指定来源的 {@link Node}。<br/>\n * 通常 origin 为服务消费者的应用名。\n * </p>\n *\n * @author qinan.qn\n * @author jialiang.linjl\n */",
    ),
    (
        "    /**\n     * <p>The origin map holds the pair: (origin, originNode) for one specific resource.</p>\n     * <p>\n     * The longer the application runs, the more stable this mapping will become.\n     * So we didn't use concurrent map here, but a lock, as this lock only happens\n     * at the very beginning while concurrent map will hold the lock all the time.\n     * </p>\n     */",
        "    /**\n     * <p>来源映射表，保存某资源下 (origin, originNode) 对。</p>\n     * <p>\n     * 应用运行越久映射越稳定；仅在初期创建节点时需要加锁，\n     * 因此使用 HashMap + 锁，而非全程持锁的并发 Map。\n     * </p>\n     */",
    ),
    (
        "    /**\n     * Get resource name of the resource node.\n     *\n     * @return resource name\n     * @since 1.7.0\n     */",
        "    /**\n     * 获取资源节点对应的资源名。\n     *\n     * @return 资源名\n     * @since 1.7.0\n     */",
    ),
    (
        "    /**\n     * Get classification (type) of the resource.\n     *\n     * @return resource type\n     * @since 1.7.0\n     */",
        "    /**\n     * 获取资源分类（类型）。\n     *\n     * @return 资源类型\n     * @since 1.7.0\n     */",
    ),
    (
        "    /**\n     * <p>Get {@link Node} of the specific origin. Usually the origin is the Service Consumer's app name.</p>\n     * <p>If the origin node for given origin is absent, then a new {@link StatisticNode}\n     * for the origin will be created and returned.</p>\n     *\n     * @param origin The caller's name, which is designated in the {@code parameter} parameter\n     *               {@link ContextUtil#enter(String name, String origin)}.\n     * @return the {@link Node} of the specific origin\n     */",
        "    /**\n     * <p>获取指定来源的 {@link Node}，通常 origin 为服务消费者的应用名。</p>\n     * <p>若该来源节点尚不存在，则创建并返回新的 {@link StatisticNode}。</p>\n     *\n     * @param origin 调用方名称，在 {@link ContextUtil#enter(String name, String origin)} 的 origin 参数中指定\n     * @return 指定来源对应的 {@link Node}\n     */",
    ),
    (
        "                    // The node is absent, create a new node for the origin.",
        "                    // 来源节点不存在，为该 origin 创建新节点。",
    ),
]

R["DefaultNode.java"] = [
    (
        "/**\n * <p>\n * A {@link Node} used to hold statistics for specific resource name in the specific context.\n * Each distinct resource in each distinct {@link Context} will corresponding to a {@link DefaultNode}.\n * </p>\n * <p>\n * This class may have a list of sub {@link DefaultNode}s. Child nodes will be created when\n * calling {@link SphU}#entry() or {@link SphO}@entry() multiple times in the same {@link Context}.\n * </p>\n *\n * @author qinan.qn\n * @see NodeSelectorSlot\n */",
        "/**\n * <p>\n * 在特定 {@link Context} 中为特定资源名保存统计数据的 {@link Node}。\n * 每个上下文中的每个不同资源对应一个 {@link DefaultNode}。\n * </p>\n * <p>\n * 本类可持有子 {@link DefaultNode} 列表；在同一 {@link Context} 中多次调用\n * {@link SphU}#entry() 或 {@link SphO}@entry() 时会创建子节点。\n * </p>\n *\n * @author qinan.qn\n * @see NodeSelectorSlot\n */",
    ),
    (
        "    /**\n     * The resource associated with the node.\n     */",
        "    /**\n     * 节点关联的资源。\n     */",
    ),
    (
        "    /**\n     * The list of all child nodes.\n     */",
        "    /**\n     * 全部子节点列表。\n     */",
    ),
    (
        "    /**\n     * Associated cluster node.\n     */",
        "    /**\n     * 关联的集群统计节点。\n     */",
    ),
    (
        "    /**\n     * Add child node to current node.\n     *\n     * @param node valid child node\n     */",
        "    /**\n     * 向当前节点添加子节点。\n     *\n     * @param node 有效的子节点\n     */",
    ),
    (
        "    /**\n     * Reset the child node list.\n     */",
        "    /**\n     * 重置子节点列表。\n     */",
    ),
]

R["EntranceNode.java"] = [
    (
        "/**\n * <p>\n * A {@link Node} represents the entrance of the invocation tree.\n * </p>\n * <p>\n * One {@link Context} will related to a {@link EntranceNode},\n * which represents the entrance of the invocation tree. New {@link EntranceNode} will be created if\n * current context does't have one. Note that same context name will share same {@link EntranceNode}\n * globally.\n * </p>\n *\n * @author qinan.qn\n * @see ContextUtil\n * @see ContextUtil#enter(String, String)\n * @see NodeSelectorSlot\n */",
        "/**\n * <p>\n * 表示调用树入口的 {@link Node}。\n * </p>\n * <p>\n * 每个 {@link Context} 关联一个 {@link EntranceNode}，作为调用树的入口统计节点。\n * 若当前上下文尚无入口节点则创建新的 EntranceNode；相同上下文名称全局共享同一 EntranceNode。\n * </p>\n *\n * @author qinan.qn\n * @see ContextUtil\n * @see ContextUtil#enter(String, String)\n * @see NodeSelectorSlot\n */",
    ),
]

R["IntervalProperty.java"] = [
    (
        "/**\n * QPS statistics interval.\n *\n * @author youji.zj\n * @author jialiang.linjl\n * @author Carpenter Lee\n * @author Eric Zhao\n */",
        "/**\n * QPS 统计滑动窗口间隔配置。\n *\n * @author youji.zj\n * @author jialiang.linjl\n * @author Carpenter Lee\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * <p>Interval in milliseconds. This variable determines sensitivity of the QPS calculation.</p>\n     * <p>\n     * DO NOT MODIFY this value directly, use {@link #updateInterval(int)}, otherwise the modification will not\n     * take effect.\n     * </p>\n     */",
        "    /**\n     * <p>滑动窗口间隔（毫秒），决定 QPS 计算的灵敏度。</p>\n     * <p>\n     * 请勿直接修改本值，应使用 {@link #updateInterval(int)}，否则修改不会生效。\n     * </p>\n     */",
    ),
    (
        "    /**\n     * Update the {@link #INTERVAL}, All {@link ClusterNode}s will be reset if newInterval is\n     * different from {@link #INTERVAL}\n     *\n     * @param newInterval New interval to set.\n     */",
        "    /**\n     * 更新 {@link #INTERVAL}；若新值与当前值不同，将重置全部 {@link ClusterNode}。\n     *\n     * @param newInterval 新的间隔（毫秒）\n     */",
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
    print(json.dumps({"ok": ok, "failures": failures}, ensure_ascii=False))
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
