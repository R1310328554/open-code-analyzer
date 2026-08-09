#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-2a context/eagleeye [0:15]."""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "sentinel/1.8.10"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_FILES = json.loads((QUEUE / "batch.json").read_text(encoding="utf-8"))["files"][:15]

R: dict[str, list[tuple[str, str]]] = {}

R["Context.java"] = [
    (
        "/**\n * This class holds metadata of current invocation:<br/>\n *\n * <ul>\n * <li>the {@link EntranceNode}: the root of the current invocation\n * tree.</li>\n * <li>the current {@link Entry}: the current invocation point.</li>\n * <li>the current {@link Node}: the statistics related to the\n * {@link Entry}.</li>\n * <li>the origin: The origin is useful when we want to control different\n * invoker/consumer separately. Usually the origin could be the Service Consumer's app name\n * or origin IP. </li>\n * </ul>\n * <p>\n * Each {@link SphU}#entry() or {@link SphO}#entry() should be in a {@link Context},\n * if we don't invoke {@link ContextUtil}#enter() explicitly, DEFAULT context will be used.\n * </p>\n * <p>\n * A invocation tree will be created if we invoke {@link SphU}#entry() multi times in\n * the same context.\n * </p>\n * <p>\n * Same resource in different context will count separately, see {@link NodeSelectorSlot}.\n * </p>\n *\n * @author jialiang.linjl\n * @author leyou(lihao)\n * @author Eric Zhao\n * @see ContextUtil\n * @see NodeSelectorSlot\n */",
        "/**\n * 保存当前调用的元数据：<br/>\n *\n * <ul>\n * <li>{@link EntranceNode}：当前调用树的根节点。</li>\n * <li>current {@link Entry}：当前调用点。</li>\n * <li>current {@link Node}：与 {@link Entry} 相关的统计节点。</li>\n * <li>origin：来源标识，用于分别控制不同调用方/消费者。\n * 通常来源可以是服务消费者的应用名或来源 IP。</li>\n * </ul>\n * <p>\n * 每次 {@link SphU}#entry() 或 {@link SphO}#entry() 都应在某个 {@link Context} 中执行；\n * 若未显式调用 {@link ContextUtil}#enter()，将使用 DEFAULT 上下文。\n * </p>\n * <p>\n * 在同一上下文中多次调用 {@link SphU}#entry() 会形成调用树。\n * </p>\n * <p>\n * 不同上下文中的相同资源分别计数，见 {@link NodeSelectorSlot}。\n * </p>\n *\n * @author jialiang.linjl\n * @author leyou(lihao)\n * @author Eric Zhao\n * @see ContextUtil\n * @see NodeSelectorSlot\n */",
    ),
    ("    /**\n     * Context name.\n     */", "    /**\n     * 上下文名称。\n     */"),
    (
        "    /**\n     * The entrance node of current invocation tree.\n     */",
        "    /**\n     * 当前调用树的入口节点。\n     */",
    ),
    ("    /**\n     * Current processing entry.\n     */", "    /**\n     * 当前正在处理的 Entry。\n     */"),
    (
        "    /**\n     * The origin of this context (usually indicate different invokers, e.g. service consumer name or origin IP).\n     */",
        "    /**\n     * 本上下文的来源（通常标识不同调用方，例如服务消费者名称或来源 IP）。\n     */",
    ),
    (
        "    /**\n     * Create a new async context.\n     *\n     * @param entranceNode entrance node of the context\n     * @param name context name\n     * @return the new created context\n     * @since 0.2.0\n     */",
        "    /**\n     * 创建新的异步上下文。\n     *\n     * @param entranceNode 上下文的入口节点\n     * @param name 上下文名称\n     * @return 新创建的上下文\n     * @since 0.2.0\n     */",
    ),
    (
        "    /**\n     * Get the parent {@link Node} of the current.\n     *\n     * @return the parent node of the current.\n     */",
        "    /**\n     * 获取当前节点的父 {@link Node}。\n     *\n     * @return 当前节点的父节点\n     */",
    ),
]

R["ContextNameDefineException.java"] = [
    (
        "/**\n * @author qinan.qn\n */",
        "/**\n * 上下文名称定义异常。\n * <p>尝试定义保留的默认上下文名称（如 {@code sentinel_default_context}）时抛出。</p>\n *\n * @author qinan.qn\n */",
    ),
]

R["ContextUtil.java"] = [
    (
        "/**\n * Utility class to get or create {@link Context} in current thread.\n *\n * <p>\n * Each {@link SphU}#entry() or {@link SphO}#entry() should be in a {@link Context}.\n * If we don't invoke {@link ContextUtil}#enter() explicitly, DEFAULT context will be used.\n * </p>\n *\n * @author jialiang.linjl\n * @author leyou(lihao)\n * @author Eric Zhao\n */",
        "/**\n * 在当前线程中获取或创建 {@link Context} 的工具类。\n *\n * <p>\n * 每次 {@link SphU}#entry() 或 {@link SphO}#entry() 都应在某个 {@link Context} 中执行；\n * 若未显式调用 {@link ContextUtil}#enter()，将使用 DEFAULT 上下文。\n * </p>\n *\n * @author jialiang.linjl\n * @author leyou(lihao)\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Store the context in ThreadLocal for easy access.\n     */",
        "    /**\n     * 使用 ThreadLocal 存储上下文，便于访问。\n     */",
    ),
    (
        "    /**\n     * Holds all {@link EntranceNode}. Each {@link EntranceNode} is associated with a distinct context name.\n     */",
        "    /**\n     * 保存全部 {@link EntranceNode}，每个 EntranceNode 对应一个独立的上下文名称。\n     */",
    ),
    ("        // Cache the entrance node for default context.", "        // 缓存默认上下文的入口节点。"),
    ("    /**\n     * Not thread-safe, only for test.\n     */", "    /**\n     * 非线程安全，仅供测试。\n     */"),
    (
        "    /**\n     * <p>\n     * Enter the invocation context, which marks as the entrance of an invocation chain.\n     * The context is wrapped with {@code ThreadLocal}, meaning that each thread has it's own {@link Context}.\n     * New context will be created if current thread doesn't have one.\n     * </p>\n     * <p>\n     * A context will be bound with an {@link EntranceNode}, which represents the entrance statistic node\n     * of the invocation chain. New {@link EntranceNode} will be created if\n     * current context does't have one. Note that same context name will share\n     * same {@link EntranceNode} globally.\n     * </p>\n     * <p>\n     * The origin node will be created in {@link com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot}.\n     * Note that each distinct {@code origin} of different resources will lead to creating different new\n     * {@link Node}, meaning that total amount of created origin statistic nodes will be:<br/>\n     * {@code distinct resource name amount * distinct origin count}.<br/>\n     * So when there are too many origins, memory footprint should be carefully considered.\n     * </p>\n     * <p>\n     * Same resource in different context will count separately, see {@link NodeSelectorSlot}.\n     * </p>\n     *\n     * @param name   the context name\n     * @param origin the origin of this invocation, usually the origin could be the Service\n     *               Consumer's app name. The origin is useful when we want to control different\n     *               invoker/consumer separately.\n     * @return The invocation context of the current thread\n     */",
        "    /**\n     * <p>\n     * 进入调用上下文，标记调用链的入口。\n     * 上下文封装在 {@code ThreadLocal} 中，即每个线程拥有独立的 {@link Context}；\n     * 若当前线程尚无上下文则创建新上下文。\n     * </p>\n     * <p>\n     * 上下文会绑定一个 {@link EntranceNode}，表示调用链的入口统计节点。\n     * 若尚不存在则创建新的 EntranceNode。相同上下文名称全局共享同一 EntranceNode。\n     * </p>\n     * <p>\n     * 来源节点在 {@link com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot} 中创建。\n     * 不同资源的不同 {@code origin} 会创建不同的 {@link Node}，\n     * 即来源统计节点总数约为：<br/>\n     * {@code 不同资源名数量 × 不同 origin 数量}。<br/>\n     * origin 过多时需仔细评估内存占用。\n     * </p>\n     * <p>\n     * 不同上下文中的相同资源分别计数，见 {@link NodeSelectorSlot}。\n     * </p>\n     *\n     * @param name   上下文名称\n     * @param origin 本次调用的来源，通常为服务消费者的应用名；\n     *               用于分别控制不同调用方/消费者\n     * @return 当前线程的调用上下文\n     */",
    ),
    ("                                // Add entrance node.", "                                // 添加入口节点。"),
    ("        // Don't need to be thread-safe.", "        // 无需线程安全。"),
    (
        "    /**\n     * <p>\n     * Enter the invocation context, which marks as the entrance of an invocation chain.\n     * The context is wrapped with {@code ThreadLocal}, meaning that each thread has it's own {@link Context}.\n     * New context will be created if current thread doesn't have one.\n     * </p>\n     * <p>\n     * A context will be bound with an {@link EntranceNode}, which represents the entrance statistic node\n     * of the invocation chain. New {@link EntranceNode} will be created if\n     * current context does't have one. Note that same context name will share\n     * same {@link EntranceNode} globally.\n     * </p>\n     * <p>\n     * Same resource in different context will count separately, see {@link NodeSelectorSlot}.\n     * </p>\n     *\n     * @param name the context name\n     * @return The invocation context of the current thread\n     */",
        "    /**\n     * <p>\n     * 进入调用上下文，标记调用链的入口。\n     * 上下文封装在 {@code ThreadLocal} 中，即每个线程拥有独立的 {@link Context}；\n     * 若当前线程尚无上下文则创建新上下文。\n     * </p>\n     * <p>\n     * 上下文会绑定一个 {@link EntranceNode}，表示调用链的入口统计节点。\n     * 若尚不存在则创建新的 EntranceNode。相同上下文名称全局共享同一 EntranceNode。\n     * </p>\n     * <p>\n     * 不同上下文中的相同资源分别计数，见 {@link NodeSelectorSlot}。\n     * </p>\n     *\n     * @param name 上下文名称\n     * @return 当前线程的调用上下文\n     */",
    ),
    (
        "    /**\n     * Exit context of current thread, that is removing {@link Context} in the\n     * ThreadLocal.\n     */",
        "    /**\n     * 退出当前线程的上下文，即从 ThreadLocal 中移除 {@link Context}。\n     */",
    ),
    (
        "    /**\n     * Get current size of context entrance node map.\n     *\n     * @return current size of context entrance node map\n     * @since 0.2.0\n     */",
        "    /**\n     * 获取上下文入口节点映射的当前大小。\n     *\n     * @return 上下文入口节点映射的当前大小\n     * @since 0.2.0\n     */",
    ),
    (
        "    /**\n     * Check if provided context is a default auto-created context.\n     *\n     * @param context context to check\n     * @return true if it is a default context, otherwise false\n     * @since 0.2.0\n     */",
        "    /**\n     * 检查给定上下文是否为自动创建的默认上下文。\n     *\n     * @param context 待检查的上下文\n     * @return 若为默认上下文则返回 true，否则返回 false\n     * @since 0.2.0\n     */",
    ),
    (
        "    /**\n     * Get {@link Context} of current thread.\n     *\n     * @return context of current thread. Null value will be return if current\n     * thread does't have context.\n     */",
        "    /**\n     * 获取当前线程的 {@link Context}。\n     *\n     * @return 当前线程的上下文；若当前线程无上下文则返回 null\n     */",
    ),
    (
        "    /**\n     * <p>\n     * Replace current context with the provided context.\n     * This is mainly designed for context switching (e.g. in asynchronous invocation).\n     * </p>\n     * <p>\n     * Note: When switching context manually, remember to restore the original context.\n     * For common scenarios, you can use {@link #runOnContext(Context, Runnable)}.\n     * </p>\n     *\n     * @param newContext new context to set\n     * @return old context\n     * @since 0.2.0\n     */",
        "    /**\n     * <p>\n     * 用给定上下文替换当前上下文。\n     * 主要用于上下文切换（例如异步调用场景）。\n     * </p>\n     * <p>\n     * 注意：手动切换上下文后应恢复原始上下文。\n     * 常见场景可使用 {@link #runOnContext(Context, Runnable)}。\n     * </p>\n     *\n     * @param newContext 要设置的新上下文\n     * @return 被替换的旧上下文\n     * @since 0.2.0\n     */",
    ),
    (
        "    /**\n     * Execute the code within provided context.\n     * This is mainly designed for context switching (e.g. in asynchronous invocation).\n     *\n     * @param context the context\n     * @param f       lambda to run within the context\n     * @since 0.2.0\n     */",
        "    /**\n     * 在指定上下文中执行代码。\n     * 主要用于上下文切换（例如异步调用场景）。\n     *\n     * @param context 目标上下文\n     * @param f       在上下文中运行的 Runnable\n     * @since 0.2.0\n     */",
    ),
]

R["NullContext.java"] = [
    (
        "/**\n * If total {@link Context} exceed {@link Constants#MAX_CONTEXT_NAME_SIZE}, a\n * {@link NullContext} will get when invoke {@link ContextUtil}.enter(), means\n * no rules checking will do.\n *\n * @author qinan.qn\n */",
        "/**\n * 当 {@link Context} 总数超过 {@link Constants#MAX_CONTEXT_NAME_SIZE} 时，\n * 调用 {@link ContextUtil}#enter() 将得到 {@link NullContext}，\n * 表示不再执行规则检查。\n *\n * @author qinan.qn\n */",
    ),
]

R["BaseLoggerBuilder.java"] = [
    (
        "package com.alibaba.csp.sentinel.eagleeye;\n\nclass BaseLoggerBuilder",
        "package com.alibaba.csp.sentinel.eagleeye;\n\n/**\n * EagleEye 统计日志构建器基类，提供日志路径、单文件大小、备份数量与字段分隔符等通用配置。\n *\n * @param <T> 具体构建器类型，用于链式调用\n */\nclass BaseLoggerBuilder",
    ),
    (
        "    public T logFilePath(String logFilePath) {",
        "    /**\n     * 设置 EagleEye 日志目录下的相对日志文件路径。\n     */\n    public T logFilePath(String logFilePath) {",
    ),
    (
        "    public T appFilePath(String appFilePath) {",
        "    /**\n     * 设置应用日志目录下的相对日志文件路径。\n     */\n    public T appFilePath(String appFilePath) {",
    ),
    (
        "    public T baseLogFilePath(String baseLogFilePath) {",
        "    /**\n     * 设置基础日志目录下的相对日志文件路径。\n     */\n    public T baseLogFilePath(String baseLogFilePath) {",
    ),
    (
        "    public T maxFileSizeMB(long maxFileSizeMB) {",
        "    /**\n     * 设置单个日志文件的最大大小（MB）。\n     */\n    public T maxFileSizeMB(long maxFileSizeMB) {",
    ),
    (
        "    public T maxBackupIndex(int maxBackupIndex) {",
        "    /**\n     * 设置滚动保留的历史日志文件数量。\n     */\n    public T maxBackupIndex(int maxBackupIndex) {",
    ),
    (
        "    public T entryDelimiter(char entryDelimiter) {",
        "    /**\n     * 设置统计条目字段之间的分隔符。\n     */\n    public T entryDelimiter(char entryDelimiter) {",
    ),
]

R["EagleEye.java"] = [
    (
        "import java.util.concurrent.TimeUnit;\n\npublic final class EagleEye {",
        "import java.util.concurrent.TimeUnit;\n\n/**\n * EagleEye 日志框架入口类。\n * <p>负责初始化自日志、后台守护线程与统计日志控制器，并提供统计日志器工厂方法。</p>\n */\npublic final class EagleEye {",
    ),
    ("    // 200MB", "    // 自日志单文件上限 200MB"),
    (
        "    public static void shutdown() {",
        "    /**\n     * 关闭 EagleEye：刷新日志、停止统计控制器与日志守护线程。\n     */\n    public static void shutdown() {",
    ),
    (
        "    public static void selfLog(String log) {",
        "    /**\n     * 写入 EagleEye 自日志（内部诊断日志）。\n     */\n    public static void selfLog(String log) {",
    ),
    (
        "    public static void selfLog(String log, Throwable e) {",
        "    /**\n     * 写入带异常堆栈的自日志，并通过令牌桶限流避免刷屏。\n     */\n    public static void selfLog(String log, Throwable e) {",
    ),
    (
        "    static public void flush() {",
        "    /**\n     * 刷新所有被监视 Appender 的缓冲输出。\n     */\n    static public void flush() {",
    ),
]

R["EagleEyeAppender.java"] = [
    (
        "package com.alibaba.csp.sentinel.eagleeye;\n\npublic abstract class EagleEyeAppender {",
        "package com.alibaba.csp.sentinel.eagleeye;\n\n/**\n * EagleEye 日志输出器抽象基类。\n * <p>定义 append、flush、rollOver、reload、close 与 cleanup 等生命周期钩子。</p>\n */\npublic abstract class EagleEyeAppender {",
    ),
    (
        "    public abstract void append(String log);",
        "    /**\n     * 追加一条日志行。\n     */\n    public abstract void append(String log);",
    ),
    (
        "    public void flush() {\n        // do nothing",
        "    /**\n     * 刷新缓冲输出。\n     */\n    public void flush() {\n        // 默认无操作",
    ),
    (
        "    public void rollOver() {\n        // do nothing",
        "    /**\n     * 滚动日志文件（按大小或策略切分）。\n     */\n    public void rollOver() {\n        // 默认无操作",
    ),
    (
        "    public void reload() {\n        // do nothing",
        "    /**\n     * 检测外部滚动并重新打开输出流。\n     */\n    public void reload() {\n        // 默认无操作",
    ),
    (
        "    public void close() {\n        // do nothing",
        "    /**\n     * 关闭输出资源。\n     */\n    public void close() {\n        // 默认无操作",
    ),
    (
        "    public void cleanup() {\n        // do nothing",
        "    /**\n     * 清理已标记删除的历史日志文件。\n     */\n    public void cleanup() {\n        // 默认无操作",
    ),
    (
        "    public String getOutputLocation() {",
        "    /**\n     * 返回日志输出位置（例如文件路径）。\n     */\n    public String getOutputLocation() {",
    ),
]

R["EagleEyeCoreUtils.java"] = [
    (
        "import java.util.concurrent.TimeUnit;\n\nfinal class EagleEyeCoreUtils {",
        "import java.util.concurrent.TimeUnit;\n\n/**\n * EagleEye 内部工具类。\n * <p>提供字符串处理、日志字段转义、时间格式化、系统属性读取与线程池关闭等辅助方法。</p>\n */\nfinal class EagleEyeCoreUtils {",
    ),
    (
        "    public static String formatTime(long timestamp) {",
        "    /**\n     * 将毫秒时间戳格式化为 {@code yyyy-MM-dd HH:mm:ss.SSS}。\n     */\n    public static String formatTime(long timestamp) {",
    ),
    (
        "    public static void shutdownThreadPool(ExecutorService pool, long awaitTimeMillis) {",
        "    /**\n     * 优雅关闭线程池，超时后强制 shutdownNow。\n     */\n    public static void shutdownThreadPool(ExecutorService pool, long awaitTimeMillis) {",
    ),
]

R["EagleEyeLogDaemon.java"] = [
    (
        "import java.util.concurrent.atomic.AtomicBoolean;\n\nclass EagleEyeLogDaemon implements Runnable {",
        "import java.util.concurrent.atomic.AtomicBoolean;\n\n/**\n * EagleEye 后台日志守护线程。\n * <p>定期清理历史文件、刷新并重载被监视的 Appender，保证多进程/滚动场景下日志一致性。</p>\n */\nclass EagleEyeLogDaemon implements Runnable {",
    ),
    (
        "    static EagleEyeAppender watch(EagleEyeAppender appender) {",
        "    /**\n     * 注册 Appender 到守护线程监视列表。\n     */\n    static EagleEyeAppender watch(EagleEyeAppender appender) {",
    ),
    (
        "    static void flushAndWait() {",
        "    /**\n     * 刷新全部被监视 Appender 并等待完成。\n     */\n    static void flushAndWait() {",
    ),
]

R["EagleEyeRollingFileAppender.java"] = [
    (
        "import java.util.concurrent.atomic.AtomicBoolean;\n\nclass EagleEyeRollingFileAppender extends EagleEyeAppender {",
        "import java.util.concurrent.atomic.AtomicBoolean;\n\n/**\n * 基于大小滚动的文件 Appender。\n * <p>支持缓冲写入、定时 flush、文件锁滚动，以及多进程写入检测。</p>\n */\nclass EagleEyeRollingFileAppender extends EagleEyeAppender {",
    ),
    (
        "    private static final int DEFAULT_BUFFER_SIZE = 4 * 1024; // 4KB",
        "    private static final int DEFAULT_BUFFER_SIZE = 4 * 1024; // 默认缓冲 4KB",
    ),
    (
        "    void waitUntilRollFinish() {",
        "    /**\n     * 等待当前滚动操作完成后再写入，避免并发冲突。\n     */\n    void waitUntilRollFinish() {",
    ),
]

R["FastDateFormat.java"] = [
    (
        "import java.util.TimeZone;\n\nclass FastDateFormat {",
        "import java.util.TimeZone;\n\n/**\n * 高性能日期格式化器。\n * <p>在同一秒内复用字符缓冲区，仅更新毫秒部分，减少 {@link SimpleDateFormat} 调用开销。</p>\n */\nclass FastDateFormat {",
    ),
    (
        "    public String format(long timestamp) {",
        "    /**\n     * 格式化毫秒时间戳为 {@code yyyy-MM-dd HH:mm:ss.SSS} 字符串。\n     */\n    public String format(long timestamp) {",
    ),
    (
        "    String formatWithoutMs(long timestamp) {",
        "    /**\n     * 格式化为不含毫秒的 {@code yyyy-MM-dd HH:mm:ss} 字符串。\n     */\n    String formatWithoutMs(long timestamp) {",
    ),
]

R["StatEntry.java"] = [
    (
        "import java.util.List;\n\npublic final class StatEntry {",
        "import java.util.List;\n\n/**\n * 统计项键组合。\n * <p>由若干 key 唯一标识一条统计记录，并通过 {@link StatLogger} 聚合 count、sum 或 min/max 等指标。</p>\n */\npublic final class StatEntry {",
    ),
    (
        "    public void count() {",
        "    /**\n     * 计数加 1。\n     */\n    public void count() {",
    ),
    (
        "    public void count(long count) {",
        "    /**\n     * 增加指定计数值。\n     */\n    public void count(long count) {",
    ),
    (
        "    public void countAndSum(long valueToSum) {",
        "    /**\n     * 计数加 1 并累加给定数值。\n     */\n    public void countAndSum(long valueToSum) {",
    ),
    (
        "    public void countAndSum(long count, long valueToSum) {",
        "    /**\n     * 增加指定计数并累加给定数值。\n     */\n    public void countAndSum(long count, long valueToSum) {",
    ),
    (
        "    public void minMax(long candidate) {",
        "    /**\n     * 更新当前时间窗口内的最小值与最大值。\n     */\n    public void minMax(long candidate) {",
    ),
    (
        "    public void minMax(long candidate, String ref) {",
        "    /**\n     * 更新最小/最大值，并附带达到极值时的参考标识。\n     */\n    public void minMax(long candidate, String ref) {",
    ),
]

R["StatEntryFunc.java"] = [
    (
        "import java.util.concurrent.atomic.LongAdder;\n\ninterface StatEntryFunc {",
        "import java.util.concurrent.atomic.LongAdder;\n\n/**\n * 统计项聚合函数接口。\n * <p>定义 count/sum、min/max 等不同统计类型的写入、读取与序列化行为。</p>\n */\ninterface StatEntryFunc {",
    ),
    (
        "enum StatEntryFuncFactory {",
        "/**\n * 统计函数工厂，按类型创建 {@link StatEntryFunc} 实现。\n */\nenum StatEntryFuncFactory {",
    ),
    (
        "class StatEntryFuncCountAndSum implements StatEntryFunc {",
        "/** 计数与求和统计实现。 */\nclass StatEntryFuncCountAndSum implements StatEntryFunc {",
    ),
    (
        "class StatEntryFuncMinMax implements StatEntryFunc {",
        "/** 最小值/最大值统计实现，可附带参考标识。 */\nclass StatEntryFuncMinMax implements StatEntryFunc {",
    ),
]

R["StatLogController.java"] = [
    (
        "import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;\n\nclass StatLogController {",
        "import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;\n\n/**\n * 统计日志控制器。\n * <p>管理 {@link StatLogger} 实例的全局注册表，并调度按时间窗口滚动与异步写入任务。</p>\n */\nclass StatLogController {",
    ),
    (
        "    static StatLogger createLoggerIfNotExists(StatLoggerBuilder builder) {",
        "    /**\n     * 若不存在则创建并注册 StatLogger，并安排首次滚动任务。\n     */\n    static StatLogger createLoggerIfNotExists(StatLoggerBuilder builder) {",
    ),
    (
        "    static void scheduleWriteTask(StatRollingData statRollingData) {",
        "    /**\n     * 延迟调度写入任务，留出统计条目冷却时间。\n     */\n    static void scheduleWriteTask(StatRollingData statRollingData) {",
    ),
    (
        "                    // time|statType|keys|values",
        "                    // 格式：time|statType|keys|values",
    ),
]

R["StatLogger.java"] = [
    (
        "/**\n * @author jifeng\n */",
        "/**\n * EagleEye 统计日志器。\n * <p>按固定时间窗口聚合 {@link StatEntry} 指标，滚动后异步写入日志文件。</p>\n *\n * @author jifeng\n */",
    ),
    (
        "    public StatEntry stat(String key) {",
        "    /**\n     * 创建单 key 统计项。\n     */\n    public StatEntry stat(String key) {",
    ),
    (
        "    StatRollingData rolling() {",
        "    /**\n     * 滚动到下一时间窗口，返回上一窗口的聚合数据供写入。\n     */\n    StatRollingData rolling() {",
    ),
]


def apply(text: str, reps: list[tuple[str, str]]) -> str:
    for old, new in reps:
        if old in text:
            text = text.replace(old, new)
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


def main() -> int:
    failures: list[str] = []
    ok = 0
    for rel in BATCH_FILES:
        dst = ANALYZED / rel
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
