#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-6a statistic/system [0:15]."""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "sentinel/1.8.10"
ANALYZED = VER / "analyzed"
ORIGINAL = VER / "original"
BATCH_FILES = Path("/tmp/sentinel_w6a.txt").read_text(encoding="utf-8").strip().split("\n")[:15]

R: dict[str, list[tuple[str, str]]] = {}

R["MetricEvent.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * 滑动窗口统计中的指标事件类型。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Normal pass.\n     */",
        "    /** 正常放行。 */",
    ),
    (
        "    /**\n     * Normal block.\n     */",
        "    /** 正常阻断。 */",
    ),
    (
        "    /**\n     * Passed in future quota (pre-occupied, since 1.5.0).\n     */",
        "    /** 占用未来配额而放行（预占，自 1.5.0 起）。 */",
    ),
]

R["StatisticSlot.java"] = [
    (
        "/**\n * <p>\n * A processor slot that dedicates to real time statistics.\n * When entering this slot, we need to separately count the following\n * information:\n * <ul>\n * <li>{@link ClusterNode}: total statistics of a cluster node of the resource ID.</li>\n * <li>Origin node: statistics of a cluster node from different callers/origins.</li>\n * <li>{@link DefaultNode}: statistics for specific resource name in the specific context.</li>\n * <li>Finally, the sum statistics of all entrances.</li>\n * </ul>\n * </p>\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
        "/**\n * <p>\n * 专用于实时统计的 ProcessorSlot。\n * 进入该 Slot 时需分别统计以下信息：\n * <ul>\n * <li>{@link ClusterNode}：资源 ID 对应集群节点的汇总统计。</li>\n * <li>Origin 节点：不同调用来源的集群节点统计。</li>\n * <li>{@link DefaultNode}：特定上下文中某资源名的统计。</li>\n * <li>最后，所有入口的汇总统计。</li>\n * </ul>\n * </p>\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
    ),
    (
        "            // Do some checking.",
        "            // 执行后续 Slot 的检查逻辑。",
    ),
    (
        "            // Request passed, add thread count and pass count.",
        "            // 请求已通过，累加线程数与放行计数。",
    ),
    (
        "                // Add count for origin node.",
        "                // 为来源节点累加计数。",
    ),
    (
        "                // Add count for global inbound entry node for global statistics.",
        "                // 为全局入站入口节点累加计数，用于全局统计。",
    ),
    (
        "            // Handle pass event with registered entry callback handlers.",
        "            // 通过已注册的 entry 回调处理放行事件。",
    ),
    (
        "            // Blocked, set block exception to current entry.",
        "            // 被阻断，将阻断异常设置到当前 Entry。",
    ),
    (
        "            // Add block count.",
        "            // 累加阻断计数。",
    ),
    (
        "            // Handle block event with registered entry callback handlers.",
        "            // 通过已注册的 entry 回调处理阻断事件。",
    ),
    (
        "            // Unexpected internal error, set error to current entry.",
        "            // 非预期内部错误，将异常设置到当前 Entry。",
    ),
    (
        "            // Calculate response time (use completeStatTime as the time of completion).",
        "            // 计算响应时间（以 completeStatTime 作为完成时刻）。",
    ),
    (
        "            // Record response time and success count.",
        "            // 记录响应时间与成功计数。",
    ),
    (
        "        // Handle exit event with registered exit callback handlers.",
        "        // 通过已注册的 exit 回调处理退出事件。",
    ),
    (
        "        // fix bug https://github.com/alibaba/Sentinel/issues/2374",
        "        // 修复 bug：https://github.com/alibaba/Sentinel/issues/2374",
    ),
]

R["StatisticSlotCallbackRegistry.java"] = [
    (
        "/**\n * <p>\n * Callback registry for {@link StatisticSlot}. Now two kind of callbacks are supported:\n * <ul>\n * <li>{@link ProcessorSlotEntryCallback}: callback for entry (passed and blocked)</li>\n * <li>{@link ProcessorSlotExitCallback}: callback for exiting {@link StatisticSlot}</li>\n * </ul>\n * </p>\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
        "/**\n * <p>\n * {@link StatisticSlot} 的回调注册表，目前支持两类回调：\n * <ul>\n * <li>{@link ProcessorSlotEntryCallback}：entry 阶段回调（放行与阻断）</li>\n * <li>{@link ProcessorSlotExitCallback}：退出 {@link StatisticSlot} 时的回调</li>\n * </ul>\n * </p>\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
    ),
    (
        "    public static void clearEntryCallback() {",
        "    /** 清空全部 entry 回调。 */\n    public static void clearEntryCallback() {",
    ),
    (
        "    public static void clearExitCallback() {",
        "    /** 清空全部 exit 回调。 */\n    public static void clearExitCallback() {",
    ),
    (
        "    public static void addEntryCallback(String key, ProcessorSlotEntryCallback<DefaultNode> callback) {",
        "    /** 注册 entry 回调。 */\n    public static void addEntryCallback(String key, ProcessorSlotEntryCallback<DefaultNode> callback) {",
    ),
    (
        "    public static void addExitCallback(String key, ProcessorSlotExitCallback callback) {",
        "    /** 注册 exit 回调。 */\n    public static void addExitCallback(String key, ProcessorSlotExitCallback callback) {",
    ),
    (
        "    public static ProcessorSlotEntryCallback<DefaultNode> removeEntryCallback(String key) {",
        "    /** 按 key 移除 entry 回调。 */\n    public static ProcessorSlotEntryCallback<DefaultNode> removeEntryCallback(String key) {",
    ),
    (
        "    public static ProcessorSlotExitCallback removeExitCallback(String key) {",
        "    /** 按 key 移除 exit 回调。 */\n    public static ProcessorSlotExitCallback removeExitCallback(String key) {",
    ),
    (
        "    public static Collection<ProcessorSlotEntryCallback<DefaultNode>> getEntryCallbacks() {",
        "    /** 获取全部 entry 回调。 */\n    public static Collection<ProcessorSlotEntryCallback<DefaultNode>> getEntryCallbacks() {",
    ),
    (
        "    public static Collection<ProcessorSlotExitCallback> getExitCallbacks() {",
        "    /** 获取全部 exit 回调。 */\n    public static Collection<ProcessorSlotExitCallback> getExitCallbacks() {",
    ),
]

R["UnaryLeapArray.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * 基于 {@link LongAdder} 的一元滑动窗口数组，用于简单计数统计。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    public UnaryLeapArray(int sampleCount, int intervalInMs) {",
        "    /** 指定采样数与窗口间隔（毫秒）构造滑动数组。 */\n    public UnaryLeapArray(int sampleCount, int intervalInMs) {",
    ),
    (
        "    @Override\n    public LongAdder newEmptyBucket(long time) {",
        "    /** 创建空的 {@link LongAdder} 桶。 */\n    @Override\n    public LongAdder newEmptyBucket(long time) {",
    ),
    (
        "    @Override\n    protected WindowWrap<LongAdder> resetWindowTo(WindowWrap<LongAdder> windowWrap, long startTime) {",
        "    /** 重置窗口起始时间并清空计数。 */\n    @Override\n    protected WindowWrap<LongAdder> resetWindowTo(WindowWrap<LongAdder> windowWrap, long startTime) {",
    ),
]

R["WindowWrap.java"] = [
    (
        "/**\n * Wrapper entity class for a period of time window.\n *\n * @param <T> data type\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
        "/**\n * 时间窗口段的包装实体类。\n *\n * @param <T> 统计数据类型\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Time length of a single window bucket in milliseconds.\n     */",
        "    /** 单个窗口桶的时间长度（毫秒）。 */",
    ),
    (
        "    /**\n     * Start timestamp of the window in milliseconds.\n     */",
        "    /** 窗口起始时间戳（毫秒）。 */",
    ),
    (
        "    /**\n     * Statistic data.\n     */",
        "    /** 统计数据。 */",
    ),
    (
        "    /**\n     * @param windowLengthInMs a single window bucket's time length in milliseconds.\n     * @param windowStart      the start timestamp of the window\n     * @param value            statistic data\n     */",
        "    /**\n     * @param windowLengthInMs 单个窗口桶的时间长度（毫秒）\n     * @param windowStart      窗口起始时间戳\n     * @param value            统计数据\n     */",
    ),
    (
        "    /**\n     * Reset start timestamp of current bucket to provided time.\n     *\n     * @param startTime valid start timestamp\n     * @return bucket after reset\n     */",
        "    /**\n     * 将当前桶的起始时间重置为给定值。\n     *\n     * @param startTime 有效的起始时间戳\n     * @return 重置后的桶\n     */",
    ),
    (
        "    /**\n     * Check whether given timestamp is in current bucket.\n     *\n     * @param timeMillis valid timestamp in ms\n     * @return true if the given time is in current bucket, otherwise false\n     * @since 1.5.0\n     */",
        "    /**\n     * 判断给定时间戳是否落在当前桶内。\n     *\n     * @param timeMillis 有效时间戳（毫秒）\n     * @return 在桶内返回 true，否则 false\n     * @since 1.5.0\n     */",
    ),
]

R["MetricBucket.java"] = [
    (
        "/**\n * Represents metrics data in a period of time span.\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
        "/**\n * 表示一段时间跨度内的指标数据。\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Reset the adders.\n     *\n     * @return new metric bucket in initial state\n     */",
        "    /**\n     * 重置全部计数器。\n     *\n     * @return 处于初始状态的指标桶\n     */",
    ),
    (
        "        // Not thread-safe, but it's okay.",
        "        // 非线程安全，但在此场景可接受。",
    ),
]

R["ArrayMetric.java"] = [
    (
        "/**\n * The basic metric class in Sentinel using a {@link BucketLeapArray} internal.\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
        "/**\n * Sentinel 基础指标类，内部使用 {@link BucketLeapArray}。\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * For unit test.\n     */",
        "    /** 供单元测试使用。 */",
    ),
    (
        "    /**\n     * Get total sum for provided event in {@code intervalInSec}.\n     *\n     * @param event event to calculate\n     * @return total sum for event\n     */",
        "    /**\n     * 获取指定事件在 {@code intervalInSec} 内的总和。\n     *\n     * @param event 待统计的事件\n     * @return 事件总和\n     */",
    ),
    (
        "    /**\n     * Get average count for provided event per second.\n     *\n     * @param event event to calculate\n     * @return average count per second for event\n     */",
        "    /**\n     * 获取指定事件的每秒平均计数。\n     *\n     * @param event 待统计的事件\n     * @return 每秒平均计数\n     */",
    ),
]

R["BucketLeapArray.java"] = [
    (
        "/**\n * The fundamental data structure for metric statistics in a time span.\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n * @see LeapArray\n */",
        "/**\n * 时间跨度内指标统计的基础数据结构。\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n * @see LeapArray\n */",
    ),
    (
        "        // Update the start time and reset value.",
        "        // 更新起始时间并重置统计值。",
    ),
]

R["DebugSupport.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.5.0\n */",
        "/**\n * 调试支持接口，用于输出指标内部状态。\n *\n * @author Eric Zhao\n * @since 1.5.0\n */",
    ),
    (
        "    /**\n     * For debug;\n     */",
        "    /** 输出调试信息。 */",
    ),
]

R["Metric.java"] = [
    (
        "/**\n * Represents a basic structure recording invocation metrics of protected resources.\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
        "/**\n * 记录受保护资源调用指标的基础结构。\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Get total success count.\n     *\n     * @return success count\n     */",
        "    /**\n     * 获取成功总数。\n     *\n     * @return 成功计数\n     */",
    ),
    (
        "    /**\n     * Get max success count.\n     *\n     * @return max success count\n     */",
        "    /**\n     * 获取最大成功计数。\n     *\n     * @return 最大成功计数\n     */",
    ),
    (
        "    /**\n     * Get total exception count.\n     *\n     * @return exception count\n     */",
        "    /**\n     * 获取异常总数。\n     *\n     * @return 异常计数\n     */",
    ),
    (
        "    /**\n     * Get total block count.\n     *\n     * @return block count\n     */",
        "    /**\n     * 获取阻断总数。\n     *\n     * @return 阻断计数\n     */",
    ),
    (
        "    /**\n     * Get total pass count. not include {@link #occupiedPass()}\n     *\n     * @return pass count\n     */",
        "    /**\n     * 获取放行总数，不包含 {@link #occupiedPass()}。\n     *\n     * @return 放行计数\n     */",
    ),
    (
        "    /**\n     * Get total response time.\n     *\n     * @return total RT\n     */",
        "    /**\n     * 获取响应时间总和。\n     *\n     * @return 总 RT\n     */",
    ),
    (
        "    /**\n     * Get the minimal RT.\n     *\n     * @return minimal RT\n     */",
        "    /**\n     * 获取最小 RT。\n     *\n     * @return 最小 RT\n     */",
    ),
    (
        "    /**\n     * Get aggregated metric nodes of all resources.\n     *\n     * @return metric node list of all resources\n     */",
        "    /**\n     * 获取全部资源的聚合指标节点。\n     *\n     * @return 全部资源的指标节点列表\n     */",
    ),
    (
        "    /**\n     * Generate aggregated metric items that satisfies the time predicate.\n     *\n     * @param timePredicate time predicate\n     * @return aggregated metric items\n     * @since 1.7.0\n     */",
        "    /**\n     * 生成满足时间谓词的聚合指标项。\n     *\n     * @param timePredicate 时间谓词\n     * @return 聚合指标项\n     * @since 1.7.0\n     */",
    ),
    (
        "    /**\n     * Get the raw window array.\n     *\n     * @return window metric array\n     */",
        "    /**\n     * 获取原始窗口数组。\n     *\n     * @return 窗口指标数组\n     */",
    ),
    (
        "    /**\n     * Add current exception count.\n     *\n     * @param n count to add\n     */",
        "    /**\n     * 累加当前异常计数。\n     *\n     * @param n 要增加的数量\n     */",
    ),
    (
        "    /**\n     * Add current block count.\n     *\n     * @param n count to add\n     */",
        "    /**\n     * 累加当前阻断计数。\n     *\n     * @param n 要增加的数量\n     */",
    ),
    (
        "    /**\n     * Add current completed count.\n     *\n     * @param n count to add\n     */",
        "    /**\n     * 累加当前完成计数。\n     *\n     * @param n 要增加的数量\n     */",
    ),
    (
        "    /**\n     * Add current pass count.\n     *\n     * @param n count to add\n     */",
        "    /**\n     * 累加当前放行计数。\n     *\n     * @param n 要增加的数量\n     */",
    ),
    (
        "    /**\n     * Add given RT to current total RT.\n     *\n     * @param rt RT\n     */",
        "    /**\n     * 将给定 RT 累加到当前总 RT。\n     *\n     * @param rt 响应时间\n     */",
    ),
    (
        "    /**\n     * Get the sliding window length in seconds.\n     *\n     * @return the sliding window length\n     */",
        "    /**\n     * 获取滑动窗口长度（秒）。\n     *\n     * @return 滑动窗口长度\n     */",
    ),
    (
        "    /**\n     * Get sample count of the sliding window.\n     *\n     * @return sample count of the sliding window.\n     */",
        "    /**\n     * 获取滑动窗口采样数。\n     *\n     * @return 滑动窗口采样数\n     */",
    ),
    (
        "    /**\n     * Note: this operation will not perform refreshing, so will not generate new buckets.\n     *\n     * @param timeMillis valid time in ms\n     * @return pass count of the bucket exactly associated to provided timestamp, or 0 if the timestamp is invalid\n     * @since 1.5.0\n     */",
        "    /**\n     * 注意：此操作不会刷新窗口，因此不会生成新桶。\n     *\n     * @param timeMillis 有效时间（毫秒）\n     * @return 与给定时间戳精确对应的桶的放行数；时间戳无效时返回 0\n     * @since 1.5.0\n     */",
    ),
    (
        "    // Occupy-based (@since 1.5.0)",
        "    // 预占相关（@since 1.5.0）",
    ),
    (
        "    /**\n     * Add occupied pass, which represents pass requests that borrow the latter windows' token.\n     *\n     * @param acquireCount tokens count.\n     * @since 1.5.0\n     */",
        "    /**\n     * 累加预占放行数，表示借用后续窗口配额的放行请求。\n     *\n     * @param acquireCount 令牌数量\n     * @since 1.5.0\n     */",
    ),
    (
        "    /**\n     * Add request that occupied.\n     *\n     * @param futureTime   future timestamp that the acquireCount should be added on.\n     * @param acquireCount tokens count.\n     * @since 1.5.0\n     */",
        "    /**\n     * 添加预占请求。\n     *\n     * @param futureTime   应累加 acquireCount 的未来时间戳\n     * @param acquireCount 令牌数量\n     * @since 1.5.0\n     */",
    ),
    (
        "    /**\n     * Get waiting pass account\n     *\n     * @return waiting pass count\n     * @since 1.5.0\n     */",
        "    /**\n     * 获取等待中的放行数。\n     *\n     * @return 等待放行计数\n     * @since 1.5.0\n     */",
    ),
    (
        "    /**\n     * Get occupied pass count.\n     *\n     * @return occupied pass count\n     * @since 1.5.0\n     */",
        "    /**\n     * 获取预占放行计数。\n     *\n     * @return 预占放行计数\n     * @since 1.5.0\n     */",
    ),
    (
        "    // Tool methods.",
        "    // 工具方法。",
    ),
]

R["FutureBucketLeapArray.java"] = [
    (
        "/**\n * A kind of {@code BucketLeapArray} that only reserves for future buckets.\n *\n * @author jialiang.linjl\n * @since 1.5.0\n */",
        "/**\n * 仅用于未来窗口桶的 {@code BucketLeapArray} 变体。\n *\n * @author jialiang.linjl\n * @since 1.5.0\n */",
    ),
    (
        "        // This class is the original \"BorrowBucketArray\".",
        "        // 本类即原 BorrowBucketArray。",
    ),
    (
        "        // Update the start time and reset value.",
        "        // 更新起始时间并重置统计值。",
    ),
    (
        "        // Tricky: will only calculate for future.",
        "        // 技巧：仅对未来窗口进行计算。",
    ),
]

R["OccupiableBucketLeapArray.java"] = [
    (
        "/**\n * @author jialiang.linjl\n * @since 1.5.0\n */",
        "/**\n * 支持预占（占用未来配额）的滑动窗口数组，组合当前桶与未来借用桶。\n *\n * @author jialiang.linjl\n * @since 1.5.0\n */",
    ),
    (
        "        // This class is the original \"CombinedBucketArray\".",
        "        // 本类即原 CombinedBucketArray。",
    ),
    (
        "        // Update the start time and reset value.",
        "        // 更新起始时间并重置统计值。",
    ),
]

R["SystemBlockException.java"] = [
    (
        "/**\n * @author jialiang.linjl\n */",
        "/**\n * 系统保护规则触发的阻断异常。\n *\n * @author jialiang.linjl\n */",
    ),
    (
        "    public SystemBlockException(String resourceName, String message, Throwable cause) {",
        "    /** 指定资源名、消息与原因构造异常。 */\n    public SystemBlockException(String resourceName, String message, Throwable cause) {",
    ),
    (
        "    public SystemBlockException(String resourceName, String limitType) {",
        "    /** 指定资源名与限流类型构造异常。 */\n    public SystemBlockException(String resourceName, String limitType) {",
    ),
    (
        "    @Override\n    public Throwable fillInStackTrace() {",
        "    /** 不填充堆栈，降低阻断异常开销。 */\n    @Override\n    public Throwable fillInStackTrace() {",
    ),
    (
        "    /**\n     * Return the limit type of system rule.\n     *\n     * @return the limit type\n     * @since 1.4.2\n     */",
        "    /**\n     * 返回触发的系统规则限流类型。\n     *\n     * @return 限流类型\n     * @since 1.4.2\n     */",
    ),
]

R["SystemRule.java"] = [
    (
        "/**\n * <p>\n * Sentinel System Rule makes the inbound traffic and capacity meet. It takes\n * average RT, QPS and thread count of requests into account. And it also\n * provides a measurement of system's load, but only available on Linux.\n * </p>\n * <p>\n * We recommend to coordinate {@link #highestSystemLoad}, {@link #qps}, {@link #avgRt}\n * and {@link #maxThread} to make sure your system run in safety level.\n * </p>\n * <p>\n * To set the threshold appropriately, performance test may be needed.\n * </p>\n *\n * @author jialiang.linjl\n * @author Carpenter Lee\n * @see SystemRuleManager\n */",
        "/**\n * <p>\n * Sentinel 系统规则使入站流量与系统容量相匹配，综合考虑平均 RT、QPS 与线程数。\n * 还提供系统负载度量（仅 Linux 可用）。\n * </p>\n * <p>\n * 建议协调 {@link #highestSystemLoad}、{@link #qps}、{@link #avgRt}\n * 与 {@link #maxThread}，确保系统运行在安全水位。\n * </p>\n * <p>\n * 合理设置阈值通常需要性能测试。\n * </p>\n *\n * @author jialiang.linjl\n * @author Carpenter Lee\n * @see SystemRuleManager\n */",
    ),
    (
        "    /**\n     * negative value means no threshold checking.\n     */",
        "    /** 负值表示不检查该阈值。 */",
    ),
    (
        "    /**\n     * cpu usage, between [0, 1]\n     */",
        "    /** CPU 使用率，取值范围 [0, 1]。 */",
    ),
    (
        "    /**\n     * Set max total QPS. In a high concurrency condition, real passed QPS may be greater than max QPS set.\n     * The real passed QPS will nearly satisfy the following formula:<br/>\n     *\n     * <pre>real passed QPS = QPS set + concurrent thread number</pre>\n     *\n     * @param qps max total QOS, values <= 0 are special for clearing the threshold.\n     */",
        "    /**\n     * 设置全局最大 QPS。高并发下实际放行 QPS 可能略高于设定值，近似满足：<br/>\n     *\n     * <pre>实际放行 QPS = 设定 QPS + 并发线程数</pre>\n     *\n     * @param qps 全局最大 QPS，≤ 0 表示清除该阈值\n     */",
    ),
    (
        "    /**\n     * Set max PARALLEL working thread. When concurrent thread number is greater than {@code maxThread} only\n     * maxThread will run in parallel.\n     *\n     * @param maxThread max parallel thread number, values <= 0 are special for clearing the threshold.\n     */",
        "    /**\n     * 设置最大并行工作线程数。并发线程数超过 {@code maxThread} 时，\n     * 仅允许 {@code maxThread} 个线程并行执行。\n     *\n     * @param maxThread 最大并行线程数，≤ 0 表示清除该阈值\n     */",
    ),
    (
        "    /**\n     * Set max average RT(response time) of all passed requests.\n     *\n     * @param avgRt max average response time, values <= 0 are special for clearing the threshold.\n     */",
        "    /**\n     * 设置全部已通过请求的最大平均 RT（响应时间）。\n     *\n     * @param avgRt 最大平均响应时间，≤ 0 表示清除该阈值\n     */",
    ),
    (
        "    /**\n     * <p>\n     * Set highest load. The load is not same as Linux system load, which is not sensitive enough.\n     * To calculate the load, both Linux system load, current global response time and global QPS will be considered,\n     * which means that we need to coordinate with {@link #setAvgRt(long)} and {@link #setQps(double)}\n     * </p>\n     * <p>\n     * Note that this parameter is only available on Unix like system.\n     * </p>\n     *\n     * @param highestSystemLoad highest system load, values <= 0 are special for clearing the threshold.\n     * @see SystemRuleManager\n     */",
        "    /**\n     * <p>\n     * 设置最高系统负载。该负载与 Linux load 不同，后者不够敏感。\n     * 计算时会综合考虑 Linux 系统负载、全局 RT 与全局 QPS，\n     * 因此需与 {@link #setAvgRt(long)}、{@link #setQps(double)} 协调配置。\n     * </p>\n     * <p>\n     * 注意：该参数仅在类 Unix 系统上可用。\n     * </p>\n     *\n     * @param highestSystemLoad 最高系统负载，≤ 0 表示清除该阈值\n     * @see SystemRuleManager\n     */",
    ),
    (
        "    /**\n     * Get highest cpu usage. Cpu usage is between [0, 1]\n     *\n     * @return highest cpu usage\n     */",
        "    /**\n     * 获取最高 CPU 使用率，取值范围 [0, 1]。\n     *\n     * @return 最高 CPU 使用率\n     */",
    ),
    (
        "    /**\n     * set highest cpu usage. Cpu usage is between [0, 1]\n     *\n     * @param highestCpuUsage the value to set.\n     */",
        "    /**\n     * 设置最高 CPU 使用率，取值范围 [0, 1]。\n     *\n     * @param highestCpuUsage 待设置的值\n     */",
    ),
]

R["SystemRuleManager.java"] = [
    (
        "/**\n * <p>\n * Sentinel System Rule makes the inbound traffic and capacity meet. It takes\n * average rt, qps, thread count of incoming requests into account. And it also\n * provides a measurement of system's load, but only available on Linux.\n * </p>\n * <p>\n * rt, qps, thread count is easy to understand. If the incoming requests'\n * rt,qps, thread count exceeds its threshold, the requests will be\n * rejected.however, we use a different method to calculate the load.\n * </p>\n * <p>\n * Consider the system as a pipeline，transitions between constraints result in\n * three different regions (traffic-limited, capacity-limited and danger area)\n * with qualitatively different behavior. When there isn’t enough request in\n * flight to fill the pipe, RTprop determines behavior; otherwise, the system\n * capacity dominates. Constraint lines intersect at inflight = Capacity ×\n * RTprop. Since the pipe is full past this point, the inflight –capacity excess\n * creates a queue, which results in the linear dependence of RTT on inflight\n * traffic and an increase in system load.In danger area, system will stop\n * responding.<br/>\n * Referring to BBR algorithm to learn more.\n * </p>\n * <p>\n * Note that {@link SystemRule} only effect on inbound requests, outbound traffic\n * will not limit by {@link SystemRule}\n * </p>\n *\n * @author jialiang.linjl\n * @author leyou\n */",
        "/**\n * <p>\n * Sentinel 系统规则使入站流量与系统容量相匹配，综合考虑入站请求的平均 RT、QPS 与线程数。\n * 还提供系统负载度量（仅 Linux 可用）。\n * </p>\n * <p>\n * RT、QPS、线程数较易理解：若入站请求的 RT、QPS 或线程数超过阈值，请求将被拒绝。\n * 负载则采用不同的计算方法。\n * </p>\n * <p>\n * 将系统视为管道，约束之间的转换形成三个不同区域（流量受限、容量受限与危险区），\n * 行为定性不同。在途请求不足以填满管道时，由 RTprop 决定行为；否则由系统容量主导。\n * 约束线相交于 inflight = Capacity × RTprop。超过该点后管道已满，\n * 超出容量的在途请求形成队列，导致 RTT 与在途流量线性相关并推高系统负载。\n * 危险区内系统将停止响应。<br/>\n * 可参考 BBR 算法了解更多。\n * </p>\n * <p>\n * 注意：{@link SystemRule} 仅对入站请求生效，出站流量不受其限制。\n * </p>\n *\n * @author jialiang.linjl\n * @author leyou\n */",
    ),
    (
        "    /**\n     * cpu usage, between [0, 1]\n     */",
        "    /** CPU 使用率，取值范围 [0, 1]。 */",
    ),
    (
        "    /**\n     * mark whether the threshold are set by user.\n     */",
        "    /** 标记该阈值是否已由用户设置。 */",
    ),
    (
        "    /**\n     * Listen to the {@link SentinelProperty} for {@link SystemRule}s. The property is the source\n     * of {@link SystemRule}s. System rules can also be set by {@link #loadRules(List)} directly.\n     *\n     * @param property the property to listen.\n     */",
        "    /**\n     * 监听 {@link SystemRule} 的动态配置源 {@link SentinelProperty}。\n     * 也可通过 {@link #loadRules(List)} 直接加载规则。\n     *\n     * @param property 待监听的配置属性\n     */",
    ),
    (
        "    /**\n     * Load {@link SystemRule}s, former rules will be replaced.\n     *\n     * @param rules new rules to load.\n     */",
        "    /**\n     * 加载 {@link SystemRule} 列表，替换原有规则。\n     *\n     * @param rules 新规则列表\n     */",
    ),
    (
        "    /**\n     * Get a copy of the rules.\n     *\n     * @return a new copy of the rules.\n     */",
        "    /**\n     * 获取全部系统规则的副本。\n     *\n     * @return 规则列表副本\n     */",
    ),
    (
        "            // systemRules = rules;",
        "            // systemRules = rules;（历史遗留注释）",
    ),
    (
        "            // should restore changes",
        "            // 恢复默认阈值",
    ),
    (
        "        // Check if it's valid.",
        "        // 校验规则是否有效。",
    ),
    (
        "    /**\n     * Apply {@link SystemRule} to the resource. Only inbound traffic will be checked.\n     *\n     * @param resourceWrapper the resource.\n     * @throws BlockException when any system rule's threshold is exceeded.\n     */",
        "    /**\n     * 对资源应用 {@link SystemRule}，仅检查入站流量。\n     *\n     * @param resourceWrapper 资源包装\n     * @throws BlockException 任一系统规则阈值被超过时抛出\n     */",
    ),
    (
        "        // Ensure the checking switch is on.",
        "        // 确保系统检查开关已开启。",
    ),
    (
        "        // for inbound traffic only",
        "        // 仅对入站流量生效",
    ),
    (
        "        // total qps",
        "        // 全局 QPS 检查",
    ),
    (
        "        // total thread",
        "        // 全局线程数检查",
    ),
    (
        "        // load. BBR algorithm.",
        "        // 负载检查（BBR 算法）",
    ),
    (
        "        // cpu usage",
        "        // CPU 使用率检查",
    ),
]


def apply(text: str, reps: list[tuple[str, str]]) -> str:
    for old, new in reps:
        if old not in text:
            raise SystemExit(f"MISSING pattern: {old[:80]!r}...")
        text = text.replace(old, new, 1)
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
        try:
            text = apply(text, R.get(name, []))
        except SystemExit as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
            continue
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
