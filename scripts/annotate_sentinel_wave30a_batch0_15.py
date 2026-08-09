#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-30a block [0:15] (param-flow/prometheus/logging/transport)."""
from __future__ import annotations

import json
import os
import re
import shutil
import subprocess
import sys
import time
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "sentinel/1.8.10"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
SCRIPTS = ROOT / "scripts"
QUEUE = VER / "_reports/class-queue"
BATCH_LIST = [
    ln.strip()
    for ln in Path("/tmp/sentinel_w30a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
SCRIPT_NAME = "annotate_sentinel_wave30a_batch0_15.py"
MARK_NOTE = "wave30a [0:15]"

GUARD_FILES = [
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-extension/sentinel-parameter-flow-control/src/main/java/com/alibaba/csp/sentinel/slots/block/flow/param/TokenUpdateStatus.java"] = [
    (
        "package com.alibaba.csp.sentinel.slots.block.flow.param;\n\nclass TokenUpdateStatus {",
        "package com.alibaba.csp.sentinel.slots.block.flow.param;\n\n/**\n * 令牌桶更新状态快照：记录上次补令时间与剩余可用 QPS。\n * 由 {@link ParamFlowChecker} 在令牌计算过程中创建并传递。\n */\nclass TokenUpdateStatus {",
    ),
    (
        "    private final long lastAddTokenTime;",
        "    /** 上次补充令牌的时间戳（毫秒）。 */\n    private final long lastAddTokenTime;",
    ),
    (
        "    private final long restQps;",
        "    /** 当前剩余可用 QPS（请求计数）。 */\n    private final long restQps;",
    ),
    (
        "    public TokenUpdateStatus(long lastAddTokenTime, long restQps) {",
        "    /**\n     * @param lastAddTokenTime 上次补令时间戳\n     * @param restQps 剩余 QPS\n     */\n    public TokenUpdateStatus(long lastAddTokenTime, long restQps) {",
    ),
    (
        "    public long getLastAddTokenTime() {",
        "    /** @return 上次补充令牌的时间戳。 */\n    public long getLastAddTokenTime() {",
    ),
    (
        "    public long getRestQps() {",
        "    /** @return 剩余可用 QPS。 */\n    public long getRestQps() {",
    ),
]

R["sentinel-extension/sentinel-parameter-flow-control/src/main/java/com/alibaba/csp/sentinel/slots/statistic/ParamFlowStatisticEntryCallback.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 0.2.0\n */",
        "/**\n * 热点参数流控 entry 回调：请求通过 {@link StatisticSlot} 时递增参数并发计数。\n * 仅当资源已配置参数流控规则且存在 {@link ParameterMetric} 时生效。\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
    ),
    (
        "        // The \"hot spot\" parameter metric is present only if parameter flow rules for the resource exist.",
        "        // 仅当资源存在参数流控规则时才有 ParameterMetric",
    ),
    (
        "        // Here we don't add block count here because checking the type of block exception can affect performance.\n        // We add the block count when throwing the ParamFlowException instead.",
        "        // 阻断计数不在此处理，避免判断 BlockException 类型影响性能；\n        // 在抛出 ParamFlowException 时再累加 block 计数",
    ),
]

R["sentinel-extension/sentinel-parameter-flow-control/src/main/java/com/alibaba/csp/sentinel/slots/statistic/ParamFlowStatisticExitCallback.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 0.2.0\n */",
        "/**\n * 热点参数流控 exit 回调：请求正常退出 {@link StatisticSlot} 时递减参数并发计数。\n * 若 entry 已被阻断（{@code getBlockError() != null}）则跳过。\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
    ),
]

R["sentinel-extension/sentinel-parameter-flow-control/src/main/java/com/alibaba/csp/sentinel/slots/statistic/cache/CacheMap.java"] = [
    (
        "/**\n * A common cache map interface.\n *\n * @param <K> type of the key\n * @param <V> type of the value\n * @author Eric Zhao\n * @since 0.2.0\n */",
        "/**\n * 热点参数统计通用缓存映射接口，支持 LRU 淘汰与有序键遍历。\n *\n * @param <K> type of the key\n * @param <V> type of the value\n * @author Eric Zhao\n * @since 0.2.0\n */",
    ),
    (
        "    boolean containsKey(K key);",
        "    /** 是否包含指定键。 */\n    boolean containsKey(K key);",
    ),
    (
        "    V get(K key);",
        "    /** 按键取值，不存在时返回 null。 */\n    V get(K key);",
    ),
    (
        "    V remove(K key);",
        "    /** 移除并返回旧值。 */\n    V remove(K key);",
    ),
    (
        "    V put(K key, V value);",
        "    /** 写入键值对并返回旧值。 */\n    V put(K key, V value);",
    ),
    (
        "    V putIfAbsent(K key, V value);",
        "    /** 键不存在时写入，返回已有值或 null。 */\n    V putIfAbsent(K key, V value);",
    ),
    (
        "    long size();",
        "    /** 当前缓存条目数（加权容量）。 */\n    long size();",
    ),
    (
        "    void clear();",
        "    /** 清空全部条目。 */\n    void clear();",
    ),
    (
        "    Set<K> keySet(boolean ascending);",
        "    /**\n     * 按访问顺序返回键集合。\n     * @param ascending true 升序，false 降序\n     */\n    Set<K> keySet(boolean ascending);",
    ),
]

R["sentinel-extension/sentinel-parameter-flow-control/src/main/java/com/alibaba/csp/sentinel/slots/statistic/cache/ConcurrentLinkedHashMapWrapper.java"] = [
    (
        "/**\n * A {@link ConcurrentLinkedHashMap} wrapper for the universal {@link CacheMap}.\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
        "/**\n * 基于 {@link ConcurrentLinkedHashMap} 的 {@link CacheMap} 实现，\n * 提供线程安全 LRU 缓存与有序键遍历。\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
    ),
    (
        "    private static final int DEFAULT_CONCURRENCY_LEVEL = 16;",
        "    /** 默认并发分段数。 */\n    private static final int DEFAULT_CONCURRENCY_LEVEL = 16;",
    ),
    (
        "    public ConcurrentLinkedHashMapWrapper(long size) {",
        "    /**\n     * 按最大加权容量创建 LRU 缓存。\n     * @param size 最大条目数，须为正数\n     */\n    public ConcurrentLinkedHashMapWrapper(long size) {",
    ),
    (
        "    public ConcurrentLinkedHashMapWrapper(ConcurrentLinkedHashMap<T, R> map) {",
        "    /** 包装已有 {@link ConcurrentLinkedHashMap} 实例。 */\n    public ConcurrentLinkedHashMapWrapper(ConcurrentLinkedHashMap<T, R> map) {",
    ),
]

R["sentinel-extension/sentinel-parameter-flow-control/src/main/java/com/alibaba/csp/sentinel/slots/statistic/data/ParamMapBucket.java"] = [
    (
        "/**\n * Represents metric bucket of frequent parameters in a period of time window.\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
        "/**\n * 滑动窗口内热点参数指标桶：按 {@link RollingParamEvent} 维度\n * 维护参数值到 {@link AtomicInteger} 计数的 LRU 映射。\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
    ),
    (
        "    public ParamMapBucket() {",
        "    /** 使用默认容量 {@link #DEFAULT_MAX_CAPACITY} 构造。 */\n    public ParamMapBucket() {",
    ),
    (
        "    public ParamMapBucket(int capacity) {",
        "    /**\n     * @param capacity 每种事件类型的 LRU 缓存容量\n     */\n    public ParamMapBucket(int capacity) {",
    ),
    (
        "    public void reset() {",
        "    /** 清空全部事件维度的计数缓存。 */\n    public void reset() {",
    ),
    (
        "    public int get(RollingParamEvent event, Object value) {",
        "    /** 获取指定事件与参数值的当前计数，不存在时返回 0。 */\n    public int get(RollingParamEvent event, Object value) {",
    ),
    (
        "        // Note: not strictly concise.",
        "        // 非严格原子：并发下可能短暂重复 putIfAbsent",
    ),
    (
        "    public ParamMapBucket add(RollingParamEvent event, int count, Object value) {",
        "    /** 累加指定事件与参数值的计数，支持链式调用。 */\n    public ParamMapBucket add(RollingParamEvent event, int count, Object value) {",
    ),
    (
        "    public Set<Object> ascendingKeySet(RollingParamEvent type) {",
        "    /** 按访问升序返回参数值键集合。 */\n    public Set<Object> ascendingKeySet(RollingParamEvent type) {",
    ),
    (
        "    public Set<Object> descendingKeySet(RollingParamEvent type) {",
        "    /** 按访问降序返回参数值键集合。 */\n    public Set<Object> descendingKeySet(RollingParamEvent type) {",
    ),
    (
        "    public static final int DEFAULT_MAX_CAPACITY = 200;",
        "    /** 默认 LRU 缓存容量。 */\n    public static final int DEFAULT_MAX_CAPACITY = 200;",
    ),
]

R["sentinel-extension/sentinel-prometheus-metric-exporter/src/main/java/com/alibaba/csp/sentinel/metric/prom/MetricConstants.java"] = [
    (
        "/**\n * The{@link PromExporterInit} the Collector for prometheus exporter.\n *\n * @author karl-sy\n * @date 2023-08-08 09:30\n * @since 2.0.0\n */",
        "/**\n * Prometheus 指标导出常量：指标族名称、标签键与各 QPS/RT 类型字段名。\n *\n * @author karl-sy\n * @date 2023-08-08 09:30\n * @since 2.0.0\n */",
    ),
    (
        "    public static final String METRIC_HELP = \"sentinel_metrics\";",
        "    /** Prometheus 指标族 help 文本。 */\n    public static final String METRIC_HELP = \"sentinel_metrics\";",
    ),
    (
        "    public static final String RESOURCE = \"resource\";",
        "    /** 资源名标签键。 */\n    public static final String RESOURCE = \"resource\";",
    ),
    (
        "    public static final String CLASSIFICATION = \"classification\";",
        "    /** 资源分类标签键。 */\n    public static final String CLASSIFICATION = \"classification\";",
    ),
    (
        "    public static final String METRIC_TYPE = \"type\";",
        "    /** 指标类型标签键（passQps、blockQps 等）。 */\n    public static final String METRIC_TYPE = \"type\";",
    ),
    (
        "    public static final String PASS_QPS = \"passQps\";",
        "    /** 通过 QPS 指标类型。 */\n    public static final String PASS_QPS = \"passQps\";",
    ),
    (
        "    public static final String BLOCK_QPS = \"blockQps\";",
        "    /** 阻断 QPS 指标类型。 */\n    public static final String BLOCK_QPS = \"blockQps\";",
    ),
    (
        "    public static final String SUCCESS_QPS = \"successQps\";",
        "    /** 成功 QPS 指标类型。 */\n    public static final String SUCCESS_QPS = \"successQps\";",
    ),
    (
        "    public static final String EXCEPTION_QPS = \"exceptionQps\";",
        "    /** 异常 QPS 指标类型。 */\n    public static final String EXCEPTION_QPS = \"exceptionQps\";",
    ),
    (
        "    public static final String RT = \"rt\";",
        "    /** 平均响应时间指标类型。 */\n    public static final String RT = \"rt\";",
    ),
    (
        "    public static final String OCC_PASS_QPS = \"occupiedPassQps\";",
        "    /** 占用通过 QPS 指标类型。 */\n    public static final String OCC_PASS_QPS = \"occupiedPassQps\";",
    ),
    (
        "    public static final String CONCURRENCY = \"concurrency\";",
        "    /** 并发线程数指标类型。 */\n    public static final String CONCURRENCY = \"concurrency\";",
    ),
]

R["sentinel-extension/sentinel-prometheus-metric-exporter/src/main/java/com/alibaba/csp/sentinel/metric/prom/PromExporterInit.java"] = [
    (
        "/**\n * The{@link PromExporterInit} the InitFunc for prometheus exporter.\n *\n * @author karl-sy\n * @date 2023-07-13 21:15\n * @since 2.0.0\n */",
        "/**\n * {@link InitFunc} 实现：注册 {@link SentinelCollector} 并启动 Prometheus HTTP 抓取端点。\n * 默认暴露 {@code http://ip:port/metrics}；JVM 关闭时自动停止 {@link HTTPServer}。\n *\n * @author karl-sy\n * @date 2023-07-13 21:15\n * @since 2.0.0\n */",
    ),
]

R["sentinel-extension/sentinel-prometheus-metric-exporter/src/main/java/com/alibaba/csp/sentinel/metric/prom/collector/SentinelCollector.java"] = [
    (
        "/**\n * The{@link PromExporterInit} Collector for prometheus exporter.\n *\n * @author karl-sy\n * @date 2023-07-13 21:15\n * @since 2.0.0\n */",
        "/**\n * Prometheus {@link Collector}：从本地指标日志文件增量读取 {@link MetricNode}\n * 并组装为带资源/分类/类型标签的 {@link GaugeMetricFamily}。\n *\n * @author karl-sy\n * @date 2023-07-13 21:15\n * @since 2.0.0\n */",
    ),
    (
        "    private final Object lock = new Object();",
        "    /** 延迟初始化 {@link MetricSearcher} 的锁。 */\n    private final Object lock = new Object();",
    ),
    (
        "    private static final int ONE_SECOND = 1000;",
        "    /** 毫秒与秒换算因子。 */\n    private static final int ONE_SECOND = 1000;",
    ),
    (
        "    private volatile MetricSearcher searcher;",
        "    /** 本地指标文件搜索器，懒加载。 */\n    private volatile MetricSearcher searcher;",
    ),
    (
        "    private volatile Long lastFetchTime;",
        "    /** 上次抓取窗口起始时间戳（秒对齐）。 */\n    private volatile Long lastFetchTime;",
    ),
    (
        "    public double getTypeVal(MetricNode node,String type){",
        "    /** 按指标类型名从 {@link MetricNode} 提取对应数值。 */\n    public double getTypeVal(MetricNode node,String type){",
    ),
]

R["sentinel-extension/sentinel-prometheus-metric-exporter/src/main/java/com/alibaba/csp/sentinel/metric/prom/config/PrometheusGlobalConfig.java"] = [
    (
        "/**\n * The config for prometheus exporter.\n *\n * @author karl-sy\n * @date 2023-07-13 21:15\n * @since 2.0.0\n */",
        "/**\n * Prometheus 指标导出全局配置：端口、抓取条数、延迟、资源过滤与指标类型等。\n * 配置项通过 {@link SentinelConfig} 读取，支持 JVM 参数覆盖。\n *\n * @author karl-sy\n * @date 2023-07-13 21:15\n * @since 2.0.0\n */",
    ),
    (
        "    public static final String PROM_FETCH_PORT = \"csp.sentinel.prometheus.fetch.port\";",
        "    /** HTTP 抓取端口配置键。 */\n    public static final String PROM_FETCH_PORT = \"csp.sentinel.prometheus.fetch.port\";",
    ),
    (
        "    public static final String DEFAULT_PROM_FETCH_PORT = \"9092\";",
        "    /** 默认抓取端口。 */\n    public static final String DEFAULT_PROM_FETCH_PORT = \"9092\";",
    ),
    (
        "    public static final String PROM_FETCH_SIZE = \"csp.sentinel.prometheus.fetch.size\";",
        "    /** 单次抓取最大 MetricNode 条数配置键。 */\n    public static final String PROM_FETCH_SIZE = \"csp.sentinel.prometheus.fetch.size\";",
    ),
    (
        "    public static final String PROM_FETCH_DELAY = \"csp.sentinel.prometheus.fetch.delay\";",
        "    /** 抓取延迟秒数配置键（跳过最近 N 秒未落盘数据）。 */\n    public static final String PROM_FETCH_DELAY = \"csp.sentinel.prometheus.fetch.delay\";",
    ),
    (
        "    public static final String PROM_FETCH_IDENTIFY = \"csp.sentinel.prometheus.fetch.identify\";",
        "    /** 资源名过滤配置键。 */\n    public static final String PROM_FETCH_IDENTIFY = \"csp.sentinel.prometheus.fetch.identify\";",
    ),
    (
        "    public static final String PROM_FETCH_TYPES = \"csp.sentinel.prometheus.fetch.types\";",
        "    /** 导出指标类型列表配置键（{@code |} 分隔）。 */\n    public static final String PROM_FETCH_TYPES = \"csp.sentinel.prometheus.fetch.types\";",
    ),
    (
        "    public static final String PROM_APP = \"csp.sentinel.prometheus.app\";",
        "    /** Prometheus 指标族名称（app）配置键。 */\n    public static final String PROM_APP = \"csp.sentinel.prometheus.app\";",
    ),
    (
        "    public static int getPromFetchPort() {",
        "    /** @return HTTP 抓取端口 */\n    public static int getPromFetchPort() {",
    ),
    (
        "    public static int getPromFetchSize() {",
        "    /** @return 单次抓取条数上限 */\n    public static int getPromFetchSize() {",
    ),
    (
        "    public static int getPromFetchDelayTime() {",
        "    /** @return 抓取延迟秒数 */\n    public static int getPromFetchDelayTime() {",
    ),
    (
        "    public static String getPromFetchIdentify() {",
        "    /** @return 资源名过滤条件，可为 null */\n    public static String getPromFetchIdentify() {",
    ),
    (
        "    public static String[] getPromFetchTypes() {",
        "    /** @return 需导出的指标类型数组 */\n    public static String[] getPromFetchTypes() {",
    ),
    (
        "    public static String getPromFetchApp() {",
        "    /** @return 规范化后的 Prometheus 指标族名称 */\n    public static String getPromFetchApp() {",
    ),
]

R["sentinel-extension/sentinel-prometheus-metric-exporter/src/main/java/com/alibaba/csp/sentinel/metric/prom/types/GaugeMetricFamily.java"] = [
    (
        "/**\n * The{@link SentinelCollector} the MetricFamilySamples for prometheus exporter.\n *\n * @author karl-sy\n * @date 2023-07-13 21:15\n * @since 2.0.0\n */",
        "/**\n * 带时间戳的 Prometheus Gauge 指标族，扩展 {@link Collector.MetricFamilySamples}\n * 以支持 {@link Sample} 毫秒级 timestamp。\n *\n * @author karl-sy\n * @date 2023-07-13 21:15\n * @since 2.0.0\n */",
    ),
    (
        "    public GaugeMetricFamily(String name, String help, double value) {",
        "    /** 无标签单值 Gauge 指标族。 */\n    public GaugeMetricFamily(String name, String help, double value) {",
    ),
    (
        "    public GaugeMetricFamily(String name, String help, List<String> labelNames) {",
        "    /** 指定标签名的多序列 Gauge 指标族。 */\n    public GaugeMetricFamily(String name, String help, List<String> labelNames) {",
    ),
    (
        "    public GaugeMetricFamily addMetric(List<String> labelValues, double value, long timestampMs) {",
        "    /**\n     * 追加一条带时间戳的样本。\n     * @param labelValues 标签值，须与 labelNames 数量一致\n     * @param timestampMs 样本时间戳（毫秒）\n     */\n    public GaugeMetricFamily addMetric(List<String> labelValues, double value, long timestampMs) {",
    ),
]

R["sentinel-logging/sentinel-logging-slf4j/src/main/java/com/alibaba/csp/sentinel/logging/slf4j/CommandCenterLogLogger.java"] = [
    (
        "/**\n * @author wavesZh\n */",
        "/**\n * {@link CommandCenterLog} 的 SLF4J 日志适配器，\n * 通过 {@link LogTarget} 绑定到 Sentinel 命令中心日志 SPI。\n *\n * @author wavesZh\n */",
    ),
]

R["sentinel-logging/sentinel-logging-slf4j/src/main/java/com/alibaba/csp/sentinel/logging/slf4j/RecordLogLogger.java"] = [
    (
        "/**\n * @author wavesZh\n */",
        "/**\n * {@link RecordLog} 的 SLF4J 日志适配器，\n * 通过 {@link LogTarget} 绑定到 Sentinel 通用记录日志 SPI。\n *\n * @author wavesZh\n */",
    ),
]

R["sentinel-transport/sentinel-transport-common/src/main/java/com/alibaba/csp/sentinel/command/CommandCenterProvider.java"] = [
    (
        "/**\n * Provider for a universal {@link CommandCenter} instance.\n *\n * @author cdfive\n * @since 1.5.0\n */",
        "/**\n * {@link CommandCenter} 单例提供者：类加载时通过 {@link SpiLoader}\n * 解析优先级最高的传输层命令中心实现。\n *\n * @author cdfive\n * @since 1.5.0\n */",
    ),
    (
        "    private static void resolveInstance() {",
        "    /** 从 SPI 加载并缓存 {@link CommandCenter} 实例。 */\n    private static void resolveInstance() {",
    ),
    (
        "     * Get resolved {@link CommandCenter} instance.\n     *\n     * @return resolved {@code CommandCenter} instance",
        "     * 获取已解析的 {@link CommandCenter} 实例。\n     *\n     * @return resolved {@code CommandCenter} instance",
    ),
]

R["sentinel-transport/sentinel-transport-common/src/main/java/com/alibaba/csp/sentinel/command/CommandConstants.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * Sentinel 命令中心 API 常量：版本命令名与通用响应消息。\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
    (
        "    public static final String VERSION_COMMAND = \"version\";",
        "    /** 查询 Sentinel 版本号的命令名。 */\n    public static final String VERSION_COMMAND = \"version\";",
    ),
    (
        "    public static final String MSG_INVALID_COMMAND = \"Invalid command\";",
        "    /** 非法命令响应文本。 */\n    public static final String MSG_INVALID_COMMAND = \"Invalid command\";",
    ),
    (
        "    public static final String MSG_UNKNOWN_COMMAND_PREFIX = \"Unknown command\";",
        "    /** 未知命令响应前缀。 */\n    public static final String MSG_UNKNOWN_COMMAND_PREFIX = \"Unknown command\";",
    ),
    (
        "    public static final String MSG_SUCCESS = \"success\";",
        "    /** 命令执行成功响应文本。 */\n    public static final String MSG_SUCCESS = \"success\";",
    ),
    (
        "    public static final String MSG_FAIL = \"failed\";",
        "    /** 命令执行失败响应文本。 */\n    public static final String MSG_FAIL = \"failed\";",
    ),
]


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


def apply_replacements(rel: str) -> None:
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    if not src.exists():
        raise SystemExit(f"Missing original: {rel}")
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)
    text = dst.read_text(encoding="utf-8")
    src_text = src.read_text(encoding="utf-8")
    for old, new in R.get(rel, []):
        if old not in text:
            raise SystemExit(f"MISSING pattern in {rel}: {old[:80]!r}...")
        text = text.replace(old, new, 1)
    if not has_chinese(text):
        raise SystemExit(f"No Chinese in {rel} after annotation")
    if "Licensed under the Apache License" in src_text and "Licensed under the Apache License" not in text:
        raise SystemExit(f"License missing in {rel}")
    dst.write_text(text, encoding="utf-8")


def tree_guard(env: dict[str, str] | None = None) -> int:
    tracked = len(subprocess.check_output(["git", "-C", str(ROOT), "ls-files"], env=env).splitlines())
    if tracked < 50000:
        raise RuntimeError(f"tree guard failed: tracked={tracked} (expected >=50000)")
    for path in GUARD_FILES:
        if env is None:
            if not path.exists():
                raise RuntimeError(f"guard file missing: {path}")
            blob = path.read_text(encoding="utf-8")
        else:
            rel = path.relative_to(ROOT)
            blob = subprocess.check_output(
                ["git", "-C", str(ROOT), "show", f":{rel}"], env=env, text=True
            )
        if not has_chinese(blob):
            raise RuntimeError(f"guard file lacks Chinese: {path}")
    return tracked


def isolated_index_commit(message: str, paths: list[str], base_ref: str = "origin/main") -> tuple[str, int]:
    index_file = Path("/tmp/git-index-sentinel-w30a")
    env = os.environ.copy()
    env["GIT_INDEX_FILE"] = str(index_file)
    base = subprocess.check_output(
        ["git", "-C", str(ROOT), "rev-parse", base_ref], text=True
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "read-tree", base], env=env, check=True)
    normal = [p for p in paths if not p.endswith("worker.log")]
    forced = [p for p in paths if p.endswith("worker.log")]
    if normal:
        subprocess.run(["git", "-C", str(ROOT), "add", "--", *normal], env=env, check=True)
    if forced:
        subprocess.run(["git", "-C", str(ROOT), "add", "-f", "--", *forced], env=env, check=True)
    tree_count = tree_guard(env)
    tree = subprocess.check_output(["git", "-C", str(ROOT), "write-tree"], env=env, text=True).strip()
    commit = subprocess.check_output(
        ["git", "-C", str(ROOT), "commit-tree", tree, "-p", base, "-m", message],
        text=True,
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "update-ref", "refs/heads/main", commit], check=True)
    index_file.unlink(missing_ok=True)
    return commit, tree_count


def push_main(retries: int = 4) -> None:
    for attempt in range(retries):
        r = subprocess.run(
            ["git", "-C", str(ROOT), "push", "-u", "origin", "main"],
            capture_output=True,
            text=True,
        )
        if r.returncode == 0:
            return
        if attempt + 1 < retries:
            subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
            subprocess.run(["git", "-C", str(ROOT), "reset", "--hard", "origin/main"], check=True)
            time.sleep(4 * (2**attempt))
    raise subprocess.CalledProcessError(r.returncode, r.args, r.stdout, r.stderr)


def confirm_chinese() -> dict[str, bool]:
    return {rel: has_chinese((ANALYZED / rel).read_text(encoding="utf-8")) for rel in BATCH_LIST}


def verify_origin_main() -> dict[str, bool]:
    result: dict[str, bool] = {}
    for rel in BATCH_LIST:
        path = f"sentinel/1.8.10/analyzed/{rel}"
        blob = subprocess.check_output(
            ["git", "-C", str(ROOT), "show", f"origin/main:{path}"],
            text=True,
        )
        result[rel] = has_chinese(blob)
    return result


def main() -> int:
    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    failures: list[str] = []
    for rel in BATCH_LIST:
        try:
            apply_replacements(rel)
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if failures:
        return 1

    analyzed_paths = [f"sentinel/1.8.10/analyzed/{rel}" for rel in BATCH_LIST]
    script_path = f"scripts/{SCRIPT_NAME}"
    sha, tree_count = isolated_index_commit(
        "sentinel 1.8.10: Chinese-annotate wave 30a [0:15]",
        [*analyzed_paths, script_path],
    )
    push_main()

    subprocess.run(
        [
            sys.executable,
            str(SCRIPTS / "mark_batch_done.py"),
            "--project",
            "sentinel",
            "--version",
            "1.8.10",
            "--note",
            MARK_NOTE,
            *BATCH_LIST,
        ],
        check=True,
    )
    queue_paths = [
        "sentinel/1.8.10/_reports/class-queue/done.txt",
        "sentinel/1.8.10/_reports/class-queue/pending.txt",
        "sentinel/1.8.10/_reports/class-queue/batch.json",
        "sentinel/1.8.10/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        f"queue: mark sentinel 1.8.10 {MARK_NOTE} done",
        queue_paths,
        base_ref="HEAD",
    )
    push_main()

    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    origin_chinese = verify_origin_main()
    done_total = len(
        [ln for ln in (QUEUE / "done.txt").read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    pending_total = len(
        [ln for ln in (QUEUE / "pending.txt").read_text(encoding="utf-8").splitlines() if ln.strip()]
    )
    chinese = confirm_chinese()
    print(
        json.dumps(
            {
                "sha": sha,
                "queue_sha": queue_sha,
                "tree_count": tree_count,
                "done": done_total,
                "pending": pending_total,
                "chinese_confirmed": chinese,
                "origin_main_chinese": origin_chinese,
                "all_chinese": all(origin_chinese.values()),
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0 if all(origin_chinese.values()) else 1


if __name__ == "__main__":
    raise SystemExit(main())
