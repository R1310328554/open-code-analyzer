#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-21b block [15:30] (dashboard repository/metric/rule)."""
from __future__ import annotations

import json
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "sentinel/1.8.10"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
SCRIPTS = ROOT / "scripts"
QUEUE = VER / "_reports/class-queue"
BATCH_LIST = Path("/tmp/sentinel_w21b.txt").read_text(encoding="utf-8").strip().split("\n")
SCRIPT_NAME = "annotate_sentinel_wave21b_batch15_30.py"
MARK_NOTE = "wave21b"

GUARD_FILES = [
    VER
    / "analyzed/sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/connection/ScanIdleConnectionTask.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/vo/gateway/rule/UpdateFlowRuleReqVo.java"] = [
    (
        "/**\n * Value Object for update gateway flow rule.\n *\n * @author cdfive\n * @since 1.7.0\n */",
        "/**\n * 更新网关流控规则的请求值对象，携带阈值、统计窗口与参数流控项。\n *\n * @author cdfive\n * @since 1.7.0\n */",
    ),
    (
        "    private Long id;",
        "    /** 规则 ID。 */\n    private Long id;",
    ),
    (
        "    private String app;",
        "    /** 应用名。 */\n    private String app;",
    ),
    (
        "    private Integer grade;",
        "    /** 限流阈值类型（QPS/线程数等）。 */\n    private Integer grade;",
    ),
    (
        "    private Double count;",
        "    /** 限流阈值。 */\n    private Double count;",
    ),
    (
        "    private Long interval;",
        "    /** 统计窗口长度。 */\n    private Long interval;",
    ),
    (
        "    private Integer intervalUnit;",
        "    /** 统计窗口单位。 */\n    private Integer intervalUnit;",
    ),
    (
        "    private Integer controlBehavior;",
        "    /** 流控行为（直接拒绝/匀速排队等）。 */\n    private Integer controlBehavior;",
    ),
    (
        "    private Integer burst;",
        "    /** 突发流量额外许可数。 */\n    private Integer burst;",
    ),
    (
        "    private Integer maxQueueingTimeoutMs;",
        "    /** 匀速排队最大等待时间（毫秒）。 */\n    private Integer maxQueueingTimeoutMs;",
    ),
    (
        "    private GatewayParamFlowItemVo paramItem;",
        "    /** 参数流控项配置。 */\n    private GatewayParamFlowItemVo paramItem;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/metric/MetricFetcher.java"] = [
    (
        "/**\n * Fetch metric of machines.\n *\n * @author leyou\n */",
        "/**\n * 机器监控指标拉取器，定时从各 Sentinel 客户端异步拉取 metric 并聚合写入仓库。\n *\n * @author leyou\n */",
    ),
    (
        "    public static final String NO_METRICS = \"No metrics\";",
        "    /** 客户端返回无指标数据时的响应前缀。 */\n    public static final String NO_METRICS = \"No metrics\";",
    ),
    (
        "    private static final int HTTP_OK = 200;",
        "    /** HTTP 成功状态码。 */\n    private static final int HTTP_OK = 200;",
    ),
    (
        "    private static final long MAX_LAST_FETCH_INTERVAL_MS = 1000 * 15;",
        "    /** 首次拉取时回溯的最长时间窗口（毫秒）。 */\n    private static final long MAX_LAST_FETCH_INTERVAL_MS = 1000 * 15;",
    ),
    (
        "    private static final long FETCH_INTERVAL_SECOND = 6;",
        "    /** 单次拉取的时间跨度（秒）。 */\n    private static final long FETCH_INTERVAL_SECOND = 6;",
    ),
    (
        "    private final long intervalSecond = 1;",
        "    /** 调度任务执行间隔（秒）。 */\n    private final long intervalSecond = 1;",
    ),
    (
        "    private Map<String, AtomicLong> appLastFetchTime = new ConcurrentHashMap<>();",
        "    /** 各应用上次拉取结束时间戳（毫秒）。 */\n    private Map<String, AtomicLong> appLastFetchTime = new ConcurrentHashMap<>();",
    ),
    (
        "    /**\n     * Traverse each APP, and then pull the metric of all machines for that APP.\n     */",
        "    /**\n     * 遍历所有应用，为每个应用拉取其全部机器的监控指标。\n     */",
    ),
    (
        "    /**\n     * fetch metric between [startTime, endTime], both side inclusive\n     */",
        "    /**\n     * 拉取指定应用在 [startTime, endTime] 区间内的指标（闭区间）。\n     */",
    ),
    (
        "        // auto remove for app",
        "        // 应用已失效则自动移除",
    ),
    (
        "        /** app_resource_timeSecond -> metric */",
        "        /** 聚合键 app_resource_timeSecond -> 指标实体 */",
    ),
    (
        "            // auto remove",
        "            // 机器已失效则自动移除",
    ),
    (
        "        // trim milliseconds",
        "        // 截断毫秒部分，对齐到秒级",
    ),
    (
        "            // too near",
        "            // 距当前时间过近，跳过本次拉取",
    ),
    (
        "        // update last_fetch in advance.",
        "        // 提前更新上次拉取时间，避免并发重复拉取",
    ),
    (
        "            // do real fetch async",
        "            // 异步提交实际拉取任务",
    ),
    (
        "                /*\n                 * aggregation metrics by app_resource_timeSecond, ignore ip and port.\n                 */",
        "                /*\n                 * 按 app_resource_timeSecond 聚合指标，忽略 ip 与 port。\n                 */",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/repository/gateway/InMemApiDefinitionStore.java"] = [
    (
        "/**\n * Store {@link ApiDefinitionEntity} in memory.\n *\n * @author cdfive\n * @since 1.7.0\n */",
        "/**\n * 网关 API 定义内存仓库，基于 {@link InMemoryRuleRepositoryAdapter} 持久化 {@link ApiDefinitionEntity}。\n *\n * @author cdfive\n * @since 1.7.0\n */",
    ),
    (
        "    private static AtomicLong ids = new AtomicLong(0);",
        "    /** 自增 ID 生成器。 */\n    private static AtomicLong ids = new AtomicLong(0);",
    ),
    (
        "    protected long nextId() {",
        "    /** @return 下一个未使用的规则 ID */\n    protected long nextId() {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/repository/gateway/InMemGatewayFlowRuleStore.java"] = [
    (
        "/**\n * Store {@link GatewayFlowRuleEntity} in memory.\n *\n * @author cdfive\n * @since 1.7.0\n */",
        "/**\n * 网关流控规则内存仓库，基于 {@link InMemoryRuleRepositoryAdapter} 持久化 {@link GatewayFlowRuleEntity}。\n *\n * @author cdfive\n * @since 1.7.0\n */",
    ),
    (
        "    private static AtomicLong ids = new AtomicLong(0);",
        "    /** 自增 ID 生成器。 */\n    private static AtomicLong ids = new AtomicLong(0);",
    ),
    (
        "    protected long nextId() {",
        "    /** @return 下一个未使用的规则 ID */\n    protected long nextId() {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/repository/metric/InMemoryMetricsRepository.java"] = [
    (
        "/**\n * Caches metrics data in a period of time in memory.\n *\n * @author Carpenter Lee\n * @author Eric Zhao\n */",
        "/**\n * 内存监控指标仓库，按应用/资源/时间戳缓存近期 metric 并支持区间查询与资源排行。\n *\n * @author Carpenter Lee\n * @author Eric Zhao\n */",
    ),
    (
        "    private static final long MAX_METRIC_LIVE_TIME_MS = 1000 * 60 * 5;",
        "    /** 单条指标最大存活时间（毫秒），超出后由 LRU 淘汰。 */\n    private static final long MAX_METRIC_LIVE_TIME_MS = 1000 * 60 * 5;",
    ),
    (
        "    /**\n     * {@code app -> resource -> timestamp -> metric}\n     */",
        "    /**\n     * 三级索引：{@code app -> resource -> timestamp -> metric}。\n     */",
    ),
    (
        "    private Map<String, Map<String, LinkedHashMap<Long, MetricEntity>>> allMetrics = new ConcurrentHashMap<>();",
        "    /** 全量指标缓存。 */\n    private Map<String, Map<String, LinkedHashMap<Long, MetricEntity>>> allMetrics = new ConcurrentHashMap<>();",
    ),
    (
        "    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();",
        "    /** 读写锁，保证并发访问安全。 */\n    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();",
    ),
    (
        "                            // Metric older than {@link #MAX_METRIC_LIVE_TIME_MS} will be removed.",
        "                            // 超过 {@link #MAX_METRIC_LIVE_TIME_MS} 的指标将被 LRU 淘汰",
    ),
    (
        "        // resource -> timestamp -> metric",
        "        // resource -> timestamp -> metric 二级索引",
    ),
    (
        "            // Order by last minute b_qps DESC.",
        "            // 按最近一分钟 block QPS 降序排列",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/repository/metric/MetricsRepository.java"] = [
    (
        "/**\n * Repository interface for aggregated metrics data.\n *\n * @param <T> type of metrics\n * @author Eric Zhao\n */",
        "/**\n * 聚合监控指标仓库接口，定义保存与按应用/资源/时间区间查询能力。\n *\n * @param <T> 指标实体类型\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Save the metric to the storage repository.\n     *\n     * @param metric metric data to save\n     */",
        "    /**\n     * 保存单条指标到仓库。\n     *\n     * @param metric 待保存的指标数据\n     */",
    ),
    (
        "    /**\n     * Save all metrics to the storage repository.\n     *\n     * @param metrics metrics to save\n     */",
        "    /**\n     * 批量保存指标到仓库。\n     *\n     * @param metrics 待保存的指标集合\n     */",
    ),
    (
        "    /**\n     * Get all metrics by {@code appName} and {@code resourceName} between a period of time.\n     *\n     * @param app       application name for Sentinel\n     * @param resource  resource name\n     * @param startTime start timestamp\n     * @param endTime   end timestamp\n     * @return all metrics in query conditions\n     */",
        "    /**\n     * 按应用名、资源名与时间区间查询全部指标。\n     *\n     * @param app       Sentinel 应用名\n     * @param resource  资源名\n     * @param startTime 起始时间戳\n     * @param endTime   结束时间戳\n     * @return 满足条件的指标列表\n     */",
    ),
    (
        "    /**\n     * List resource name of provided application name.\n     *\n     * @param app application name\n     * @return list of resources\n     */",
        "    /**\n     * 列出指定应用下的资源名，通常按近期限流量排序。\n     *\n     * @param app 应用名\n     * @return 资源名列表\n     */",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/repository/rule/InMemAuthorityRuleStore.java"] = [
    (
        "/**\n * In-memory storage for authority rules.\n *\n * @author Eric Zhao\n * @since 0.2.1\n */",
        "/**\n * 授权规则内存仓库，基于 {@link InMemoryRuleRepositoryAdapter} 持久化 {@link AuthorityRuleEntity}。\n *\n * @author Eric Zhao\n * @since 0.2.1\n */",
    ),
    (
        "    private static AtomicLong ids = new AtomicLong(0);",
        "    /** 自增 ID 生成器。 */\n    private static AtomicLong ids = new AtomicLong(0);",
    ),
    (
        "    protected long nextId() {",
        "    /** @return 下一个未使用的规则 ID */\n    protected long nextId() {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/repository/rule/InMemDegradeRuleStore.java"] = [
    (
        "/**\n * @author leyou\n */",
        "/**\n * 熔断降级规则内存仓库，基于 {@link InMemoryRuleRepositoryAdapter} 持久化 {@link DegradeRuleEntity}。\n *\n * @author leyou\n */",
    ),
    (
        "    private static AtomicLong ids = new AtomicLong(0);",
        "    /** 自增 ID 生成器。 */\n    private static AtomicLong ids = new AtomicLong(0);",
    ),
    (
        "    protected long nextId() {",
        "    /** @return 下一个未使用的规则 ID */\n    protected long nextId() {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/repository/rule/InMemFlowRuleStore.java"] = [
    (
        "/**\n * Store {@link FlowRuleEntity} in memory.\n *\n * @author leyou\n */",
        "/**\n * 流控规则内存仓库，集群模式下自动设置 {@link ClusterFlowConfig#flowId}。\n *\n * @author leyou\n */",
    ),
    (
        "    private static AtomicLong ids = new AtomicLong(0);",
        "    /** 自增 ID 生成器。 */\n    private static AtomicLong ids = new AtomicLong(0);",
    ),
    (
        "    protected long nextId() {",
        "    /** @return 下一个未使用的规则 ID */\n    protected long nextId() {",
    ),
    (
        "            // Set cluster rule id.",
        "            // 设置集群流控规则 ID",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/repository/rule/InMemParamFlowRuleStore.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 0.2.1\n */",
        "/**\n * 热点参数流控规则内存仓库，集群模式下自动设置 {@link ParamFlowClusterConfig#flowId}。\n *\n * @author Eric Zhao\n * @since 0.2.1\n */",
    ),
    (
        "    private static AtomicLong ids = new AtomicLong(0);",
        "    /** 自增 ID 生成器。 */\n    private static AtomicLong ids = new AtomicLong(0);",
    ),
    (
        "    protected long nextId() {",
        "    /** @return 下一个未使用的规则 ID */\n    protected long nextId() {",
    ),
    (
        "            // Set cluster rule id.",
        "            // 设置集群流控规则 ID",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/repository/rule/InMemSystemRuleStore.java"] = [
    (
        "/**\n * @author leyou\n */",
        "/**\n * 系统保护规则内存仓库，基于 {@link InMemoryRuleRepositoryAdapter} 持久化 {@link SystemRuleEntity}。\n *\n * @author leyou\n */",
    ),
    (
        "    private static AtomicLong ids = new AtomicLong(0);",
        "    /** 自增 ID 生成器。 */\n    private static AtomicLong ids = new AtomicLong(0);",
    ),
    (
        "    protected long nextId() {",
        "    /** @return 下一个未使用的规则 ID */\n    protected long nextId() {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/repository/rule/InMemoryRuleRepositoryAdapter.java"] = [
    (
        "/**\n * @author leyou\n */",
        "/**\n * 规则内存仓库抽象适配器，按机器、应用与全局 ID 三级索引管理 {@link RuleEntity}。\n *\n * @author leyou\n */",
    ),
    (
        "    /**\n     * {@code <machine, <id, rule>>}\n     */",
        "    /**\n     * 机器维度索引：{@code <machine, <id, rule>>}。\n     */",
    ),
    (
        "    private Map<MachineInfo, Map<Long, T>> machineRules = new ConcurrentHashMap<>(16);",
        "    /** 按机器索引的规则映射。 */\n    private Map<MachineInfo, Map<Long, T>> machineRules = new ConcurrentHashMap<>(16);",
    ),
    (
        "    private Map<Long, T> allRules = new ConcurrentHashMap<>(16);",
        "    /** 全局 ID -> 规则映射。 */\n    private Map<Long, T> allRules = new ConcurrentHashMap<>(16);",
    ),
    (
        "    private Map<String, Map<Long, T>> appRules = new ConcurrentHashMap<>(16);",
        "    /** 按应用名索引的规则映射。 */\n    private Map<String, Map<Long, T>> appRules = new ConcurrentHashMap<>(16);",
    ),
    (
        "    private static final int MAX_RULES_SIZE = 10000;",
        "    /** 单仓库最大规则数上限（预留常量）。 */\n    private static final int MAX_RULES_SIZE = 10000;",
    ),
    (
        "        // TODO: check here.",
        "        // TODO: 此处需补充校验逻辑",
    ),
    (
        "    protected T preProcess(T entity) {",
        "    /** 保存前预处理钩子，子类可覆写以注入集群配置等。 */\n    protected T preProcess(T entity) {",
    ),
    (
        "    /**\n     * Get next unused id.\n     *\n     * @return next unused id\n     */",
        "    /**\n     * 获取下一个未使用的规则 ID。\n     *\n     * @return 下一个未使用的 ID\n     */",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/repository/rule/RuleRepository.java"] = [
    (
        "/**\n * Interface to store and find rules.\n *\n * @author leyou\n */",
        "/**\n * 规则存储与查询接口，支持按 ID、机器与应用维度访问。\n *\n * @author leyou\n */",
    ),
    (
        "    /**\n     * Save one.\n     *\n     * @param entity\n     * @return\n     */",
        "    /**\n     * 保存单条规则。\n     *\n     * @param entity 规则实体\n     * @return 保存后的实体\n     */",
    ),
    (
        "    /**\n     * Save all.\n     *\n     * @param rules\n     * @return rules saved.\n     */",
        "    /**\n     * 全量保存规则（先清空再写入）。\n     *\n     * @param rules 规则列表\n     * @return 已保存的规则列表\n     */",
    ),
    (
        "    /**\n     * Delete by id\n     *\n     * @param id\n     * @return entity deleted\n     */",
        "    /**\n     * 按 ID 删除规则。\n     *\n     * @param id 规则 ID\n     * @return 被删除的实体\n     */",
    ),
    (
        "    /**\n     * Find by id.\n     *\n     * @param id\n     * @return\n     */",
        "    /**\n     * 按 ID 查询规则。\n     *\n     * @param id 规则 ID\n     * @return 规则实体，不存在时返回 null\n     */",
    ),
    (
        "    /**\n     * Find all by machine.\n     *\n     * @param machineInfo\n     * @return\n     */",
        "    /**\n     * 查询指定机器上的全部规则。\n     *\n     * @param machineInfo 机器信息\n     * @return 规则列表\n     */",
    ),
    (
        "    /**\n     * Find all by application.\n     *\n     * @param appName valid app name\n     * @return all rules of the application\n     * @since 1.4.0\n     */",
        "    /**\n     * 查询指定应用下的全部规则。\n     *\n     * @param appName 有效应用名\n     * @return 该应用的全部规则\n     * @since 1.4.0\n     */",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/rule/DynamicRuleProvider.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 动态规则读取接口，从外部配置中心拉取指定应用的规则。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    T getRules(String appName) throws Exception;",
        "    /**\n     * 获取指定应用的规则。\n     *\n     * @param appName 应用名\n     * @return 规则数据\n     * @throws Exception 读取失败时抛出\n     */\n    T getRules(String appName) throws Exception;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/rule/DynamicRulePublisher.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 动态规则发布接口，将规则推送至外部配置中心。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    /**\n     * Publish rules to remote rule configuration center for given application name.\n     *\n     * @param app app name\n     * @param rules list of rules to push\n     * @throws Exception if some error occurs\n     */",
        "    /**\n     * 将规则发布到远程配置中心。\n     *\n     * @param app 应用名\n     * @param rules 待推送的规则\n     * @throws Exception 发布失败时抛出\n     */",
    ),
]


def has_chinese(text: str) -> bool:
    return bool(re.search(r"[\u4e00-\u9fff]", text))


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
    index_file = Path("/tmp/git-index-sentinel-w21b")
    env = os.environ.copy()
    env["GIT_INDEX_FILE"] = str(index_file)
    base = subprocess.check_output(
        ["git", "-C", str(ROOT), "rev-parse", base_ref], text=True
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "read-tree", base], env=env, check=True)
    subprocess.run(["git", "-C", str(ROOT), "add", "--", *paths], env=env, check=True)
    tree_count = tree_guard(env)
    tree = subprocess.check_output(["git", "-C", str(ROOT), "write-tree"], env=env, text=True).strip()
    commit = subprocess.check_output(
        ["git", "-C", str(ROOT), "commit-tree", tree, "-p", base, "-m", message],
        text=True,
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "update-ref", "refs/heads/main", commit], check=True)
    index_file.unlink(missing_ok=True)
    return commit, tree_count


def apply_replacements(rel: str) -> None:
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    if not src.exists():
        raise FileNotFoundError(f"Missing original: {rel}")
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)
    text = dst.read_text(encoding="utf-8")
    orig = text
    for old, new in R.get(rel, []):
        if old not in text:
            raise ValueError(f"MISSING pattern in {rel}: {old[:80]!r}...")
        text = text.replace(old, new, 1)
    if not has_chinese(text):
        raise ValueError(f"No Chinese in {rel} after annotation")
    if "Licensed under the Apache License" in orig and "Licensed under the Apache License" not in text:
        raise ValueError(f"License missing in {rel}")
    dst.write_text(text, encoding="utf-8")


def update_batch_json() -> None:
    batch_path = QUEUE / "batch.json"
    if not batch_path.exists():
        return
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    done_path = QUEUE / "done.txt"
    pending_path = QUEUE / "pending.txt"
    batch["done"] = len([ln for ln in done_path.read_text(encoding="utf-8").splitlines() if ln.strip()])
    if pending_path.exists():
        batch["remaining_pending"] = len(
            [ln for ln in pending_path.read_text(encoding="utf-8").splitlines() if ln.strip()]
        )
    batch_path.write_text(json.dumps(batch, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def confirm_chinese() -> dict[str, bool]:
    return {rel: has_chinese((ANALYZED / rel).read_text(encoding="utf-8")) for rel in BATCH_LIST}


def main() -> int:
    failures: list[str] = []
    for rel in BATCH_LIST:
        try:
            apply_replacements(rel)
            print(f"OK {rel}")
        except Exception as e:
            failures.append(f"{rel}: {e}")
            print(f"FAIL {rel}: {e}")
    if failures:
        print(json.dumps({"failures": failures}, ensure_ascii=False, indent=2))
        return 1

    analyzed_paths = [f"sentinel/1.8.10/analyzed/{rel}" for rel in BATCH_LIST]
    script_path = f"scripts/{SCRIPT_NAME}"
    sha, tree_count = isolated_index_commit(
        "sentinel 1.8.10: Chinese-annotate wave 21b [15:30]",
        [*analyzed_paths, script_path],
    )
    subprocess.run(["git", "-C", str(ROOT), "push", "-u", "origin", "main"], check=True)

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
    update_batch_json()
    queue_paths = [
        "sentinel/1.8.10/_reports/class-queue/done.txt",
        "sentinel/1.8.10/_reports/class-queue/pending.txt",
        "sentinel/1.8.10/_reports/class-queue/batch.json",
        "sentinel/1.8.10/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        "queue: mark sentinel 1.8.10 wave21b done",
        queue_paths,
        base_ref="HEAD",
    )
    subprocess.run(["git", "-C", str(ROOT), "push", "origin", "main"], check=True)

    done_total = len([ln for ln in (QUEUE / "done.txt").read_text(encoding="utf-8").splitlines() if ln.strip()])
    pending_total = len([ln for ln in (QUEUE / "pending.txt").read_text(encoding="utf-8").splitlines() if ln.strip()])
    chinese_confirmed = confirm_chinese()
    print(
        json.dumps(
            {
                "sha": sha,
                "queue_sha": queue_sha,
                "tree_count": tree_count,
                "done": done_total,
                "pending": pending_total,
                "chinese_confirmed": chinese_confirmed,
                "all_15_chinese": all(chinese_confirmed.values()),
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
