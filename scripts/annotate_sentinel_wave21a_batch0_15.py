#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-21a block [0:15] (cluster state + dashboard VOs)."""
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
SCRIPT_NAME = "annotate_sentinel_wave21a_batch0_15.py"
BATCH_LIST = Path("/tmp/sentinel_w21a.txt").read_text(encoding="utf-8").strip().split("\n")
MARK_NOTE = "wave21a"

GUARD_FILES = [
    VER
    / "analyzed/sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/controller/SystemController.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/state/AppClusterServerStateWrapVO.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 应用维度集群令牌服务端状态包装视图，聚合机器标识与 {@link ClusterServerStateVO} 详情。\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
    (
        "    /**\n     * {ip}@{transport_command_port}.\n     */",
        "    /** 机器唯一标识，格式为 {@code ip@transport_command_port}。 */\n",
    ),
    (
        "    private String ip;\n    private Integer port;",
        "    /** 机器 IP 地址。 */\n    private String ip;\n    /** Sentinel 命令端口。 */\n    private Integer port;",
    ),
    (
        "    private Integer connectedCount;",
        "    /** 当前已建立的客户端连接数量。 */\n    private Integer connectedCount;",
    ),
    (
        "    private Boolean belongToApp;",
        "    /** 该服务端机器是否归属当前应用。 */\n    private Boolean belongToApp;",
    ),
    (
        "    private ClusterServerStateVO state;",
        "    /** 集群令牌服务端运行状态详情。 */\n    private ClusterServerStateVO state;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/state/ClusterClientStateVO.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群令牌客户端状态视图，封装 {@link ClusterClientInfoVO} 客户端配置与运行信息。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    /**\n     * Cluster token client state.\n     */",
        "    /** 集群令牌客户端配置与状态信息。 */",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/state/ClusterRequestLimitVO.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 命名空间级集群请求限流视图，展示当前 QPS 与允许的最大 QPS。\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
    (
        "    private String namespace;\n    private Double currentQps;\n    private Double maxAllowedQps;",
        "    /** 限流所属命名空间。 */\n    private String namespace;\n    /** 当前统计窗口内的 QPS。 */\n    private Double currentQps;\n    /** 该命名空间允许的最大 QPS 上限。 */\n    private Double maxAllowedQps;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/state/ClusterServerStateVO.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群令牌服务端完整状态视图，含传输/流控配置、连接分组与限流数据。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    private String appName;",
        "    /** 服务端所属应用名。 */\n    private String appName;",
    ),
    (
        "    private ServerTransportConfig transport;\n    private ServerFlowConfig flow;\n    private Set<String> namespaceSet;",
        "    /** 服务端传输层配置（端口、空闲超时等）。 */\n    private ServerTransportConfig transport;\n    /** 服务端流控配置。 */\n    private ServerFlowConfig flow;\n    /** 该服务端负责的命名空间集合。 */\n    private Set<String> namespaceSet;",
    ),
    (
        "    private Integer port;",
        "    /** 服务端监听端口。 */\n    private Integer port;",
    ),
    (
        "    private List<ConnectionGroupVO> connection;\n    private List<ClusterRequestLimitVO> requestLimitData;",
        "    /** 按命名空间分组的客户端连接列表。 */\n    private List<ConnectionGroupVO> connection;\n    /** 各命名空间的实时限流统计数据。 */\n    private List<ClusterRequestLimitVO> requestLimitData;",
    ),
    (
        "    private Boolean embedded;",
        "    /** 是否为嵌入式（与应用同进程）Token Server。 */\n    private Boolean embedded;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/state/ClusterStateSimpleEntity.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群运行模式与可用性摘要实体，供 Dashboard 快速展示客户端/服务端是否就绪。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    private Integer mode;\n    private Long lastModified;\n    private Boolean clientAvailable;\n    private Boolean serverAvailable;",
        "    /** 集群运行模式（未启用/客户端/服务端等）。 */\n    private Integer mode;\n    /** 状态最后变更时间戳（毫秒）。 */\n    private Long lastModified;\n    /** 客户端组件是否可用。 */\n    private Boolean clientAvailable;\n    /** 服务端组件是否可用。 */\n    private Boolean serverAvailable;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/state/ClusterUniversalStatePairVO.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 单台机器的集群通用状态对，绑定 IP、命令端口与 {@link ClusterUniversalStateVO}。\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
    (
        "    private String ip;\n    private Integer commandPort;",
        "    /** 机器 IP 地址。 */\n    private String ip;\n    /** Sentinel 命令端口。 */\n    private Integer commandPort;",
    ),
    (
        "    private ClusterUniversalStateVO state;",
        "    /** 该机器的集群通用状态详情。 */\n    private ClusterUniversalStateVO state;",
    ),
    (
        "    public ClusterUniversalStatePairVO() {}",
        "    /** 无参构造，供 JSON 反序列化使用。 */\n    public ClusterUniversalStatePairVO() {}",
    ),
    (
        "    public ClusterUniversalStatePairVO(String ip, Integer commandPort, ClusterUniversalStateVO state) {",
        "    /** @param ip 机器 IP\n     * @param commandPort 命令端口\n     * @param state 集群通用状态 */\n    public ClusterUniversalStatePairVO(String ip, Integer commandPort, ClusterUniversalStateVO state) {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/state/ClusterUniversalStateVO.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群通用状态视图，同时携带模式摘要、客户端状态与服务端状态。\n * <p>Dashboard 查询单台机器集群信息时的顶层聚合对象。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    private ClusterStateSimpleEntity stateInfo;\n    private ClusterClientStateVO client;\n    private ClusterServerStateVO server;",
        "    /** 运行模式与可用性摘要。 */\n    private ClusterStateSimpleEntity stateInfo;\n    /** 集群令牌客户端状态（客户端模式下有效）。 */\n    private ClusterClientStateVO client;\n    /** 集群令牌服务端状态（服务端模式下有效）。 */\n    private ClusterServerStateVO server;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/vo/MachineInfoVo.java"] = [
    (
        "/**\n * @author leyou\n */",
        "/**\n * 机器信息视图对象，供 Dashboard API 返回客户端实例列表。\n * <p>由 {@link MachineInfo} 转换而来，含心跳与健康状态。\n *\n * @author leyou\n */",
    ),
    (
        "    private String app;\n    private String hostname;\n    private String ip;\n    private int port;\n    private long heartbeatVersion;\n    private long lastHeartbeat;\n    private boolean healthy;",
        "    /** 所属应用名。 */\n    private String app;\n    /** 主机名。 */\n    private String hostname;\n    /** 机器 IP。 */\n    private String ip;\n    /** 客户端端口。 */\n    private int port;\n    /** 心跳协议版本号。 */\n    private long heartbeatVersion;\n    /** 最后一次心跳时间戳（毫秒）。 */\n    private long lastHeartbeat;\n    /** 当前是否健康（心跳未超时）。 */\n    private boolean healthy;",
    ),
    (
        "    private String version;",
        "    /** Sentinel 客户端版本号。 */\n    private String version;",
    ),
    (
        "    public static List<MachineInfoVo> fromMachineInfoList(List<MachineInfo> machines) {",
        "    /** 批量将 {@link MachineInfo} 列表转换为 VO 列表。 */\n    public static List<MachineInfoVo> fromMachineInfoList(List<MachineInfo> machines) {",
    ),
    (
        "    public static MachineInfoVo fromMachineInfo(MachineInfo machine) {",
        "    /** 从单条 {@link MachineInfo} 拷贝字段构建 VO。 */\n    public static MachineInfoVo fromMachineInfo(MachineInfo machine) {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/vo/MetricVo.java"] = [
    (
        "/**\n * @author leyou\n */",
        "/**\n * 监控指标视图对象，展示单条资源在某时刻的 QPS、RT 等统计数据。\n * <p>可由 {@link MetricEntity} 或客户端上报的管道分隔行解析得到。\n *\n * @author leyou\n */",
    ),
    (
        "    private Long id;\n    private String app;\n    private Long timestamp;\n    private Long gmtCreate = System.currentTimeMillis();\n    private String resource;\n    private Long passQps;\n    private Long blockQps;\n    private Long successQps;\n    private Long exceptionQps;",
        "    /** 指标记录主键。 */\n    private Long id;\n    /** 所属应用名。 */\n    private String app;\n    /** 指标统计时刻（毫秒时间戳）。 */\n    private Long timestamp;\n    /** 记录创建时间（毫秒）。 */\n    private Long gmtCreate = System.currentTimeMillis();\n    /** 受监控资源名。 */\n    private String resource;\n    /** 通过 QPS。 */\n    private Long passQps;\n    /** 被限流 QPS。 */\n    private Long blockQps;\n    /** 成功 QPS。 */\n    private Long successQps;\n    /** 异常 QPS。 */\n    private Long exceptionQps;",
    ),
    (
        "    /**\n     * average rt\n     */",
        "    /** 平均响应时间（毫秒）。 */",
    ),
    (
        "    private Integer count;",
        "    /** 聚合样本数量。 */\n    private Integer count;",
    ),
    (
        "    public MetricVo() {",
        "    /** 无参构造。 */\n    public MetricVo() {",
    ),
    (
        "    public static List<MetricVo> fromMetricEntities(Collection<MetricEntity> entities) {",
        "    /** 将 {@link MetricEntity} 集合批量转换为 VO 列表。 */\n    public static List<MetricVo> fromMetricEntities(Collection<MetricEntity> entities) {",
    ),
    (
        "     * @return\n     */\n    public static List<MetricVo> fromMetricEntities(Collection<MetricEntity> entities, String identity) {",
        "     * @return 匹配 identity 的指标 VO 列表\n     */\n    public static List<MetricVo> fromMetricEntities(Collection<MetricEntity> entities, String identity) {",
    ),
    (
        "    public static MetricVo fromMetricEntity(MetricEntity entity) {",
        "    /** 从单条 {@link MetricEntity} 构建 VO，RT 按 successQps 加权平均。 */\n    public static MetricVo fromMetricEntity(MetricEntity entity) {",
    ),
    (
        "    public static MetricVo parse(String line) {",
        "    /** 解析客户端上报的管道分隔指标行（timestamp|resource|pass|block|exception|rt|success）。 */\n    public static MetricVo parse(String line) {",
    ),
    (
        "    @Override\n    public int compareTo(MetricVo o) {",
        "    /** 按 timestamp 升序排序。 */\n    @Override\n    public int compareTo(MetricVo o) {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/vo/ResourceVo.java"] = [
    (
        "/**\n * @author leyou\n */",
        "/**\n * 资源调用树节点视图，用于 Dashboard 实时展示 QPS、RT、线程数等指标。\n * <p>可由 {@link NodeVo} 列表或 {@link ResourceTreeNode} 递归遍历生成。\n *\n * @author leyou\n */",
    ),
    (
        "    private String parentTtId;\n    private String ttId;\n    private String resource;",
        "    /** 父节点 id。 */\n    private String parentTtId;\n    /** 本节点 id。 */\n    private String ttId;\n    /** 资源名称。 */\n    private String resource;",
    ),
    (
        "    private Integer threadNum;\n    private Long passQps;\n    private Long blockQps;\n    private Long totalQps;\n    private Long averageRt;\n    private Long passRequestQps;\n    private Long exceptionQps;\n    private Long oneMinutePass;\n    private Long oneMinuteBlock;\n    private Long oneMinuteException;\n    private Long oneMinuteTotal;",
        "    /** 当前占用线程数。 */\n    private Integer threadNum;\n    /** 通过 QPS。 */\n    private Long passQps;\n    /** 被限流 QPS。 */\n    private Long blockQps;\n    /** 总 QPS（通过 + 限流）。 */\n    private Long totalQps;\n    /** 平均响应时间（毫秒）。 */\n    private Long averageRt;\n    /** 通过请求 QPS。 */\n    private Long passRequestQps;\n    /** 异常 QPS。 */\n    private Long exceptionQps;\n    /** 近一分钟通过数。 */\n    private Long oneMinutePass;\n    /** 近一分钟被限流数。 */\n    private Long oneMinuteBlock;\n    /** 近一分钟异常数。 */\n    private Long oneMinuteException;\n    /** 近一分钟总请求数。 */\n    private Long oneMinuteTotal;",
    ),
    (
        "    private boolean visible = true;",
        "    /** 搜索过滤后是否在 UI 中可见。 */\n    private boolean visible = true;",
    ),
    (
        "    public ResourceVo() {",
        "    /** 无参构造。 */\n    public ResourceVo() {",
    ),
    (
        "    public static List<ResourceVo> fromNodeVoList(List<NodeVo> nodeVos) {",
        "    /** 将客户端 {@link NodeVo} 列表转换为扁平 {@link ResourceVo} 列表。 */\n    public static List<ResourceVo> fromNodeVoList(List<NodeVo> nodeVos) {",
    ),
    (
        "    public static List<ResourceVo> fromResourceTreeNode(ResourceTreeNode root) {",
        "    /** 从 {@link ResourceTreeNode} 根节点递归收集可见节点为 VO 列表。 */\n    public static List<ResourceVo> fromResourceTreeNode(ResourceTreeNode root) {",
    ),
    (
        "        //if(!list.isEmpty()){\n        //    list.remove(0);\n        //}",
        "        // 可选：移除根节点占位项（当前保留全部可见节点）。",
    ),
    (
        "    /**\n     * This node is visible when this.visible==true or one of this's parents is visible,\n     * root node is always invisible.\n     */",
        "    /**\n     * 递归遍历资源树：根节点始终不可见；\n     * 子节点在本节点 visible 为 true 或父链已可见时加入结果列表。\n     */",
    ),
    (
        "        //boolean visible = node.isVisible();",
        "        // 备选：仅依据节点自身 visible 标记。",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/vo/gateway/api/AddApiReqVo.java"] = [
    (
        "/**\n * Value Object for add gateway api.\n *\n * @author cdfive\n * @since 1.7.0\n */",
        "/**\n * 新增网关 API 定义请求体，指定目标机器与 API 名称及 URL 匹配谓词列表。\n *\n * @author cdfive\n * @since 1.7.0\n */",
    ),
    (
        "    private String app;\n\n    private String ip;\n\n    private Integer port;\n\n    private String apiName;\n\n    private List<ApiPredicateItemVo> predicateItems;",
        "    /** 目标应用名。 */\n    private String app;\n\n    /** 目标客户端机器 IP。 */\n    private String ip;\n\n    /** 目标客户端机器端口。 */\n    private Integer port;\n\n    /** 网关 API 名称（唯一标识）。 */\n    private String apiName;\n\n    /** URL 匹配谓词项列表。 */\n    private List<ApiPredicateItemVo> predicateItems;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/vo/gateway/api/ApiPredicateItemVo.java"] = [
    (
        "/**\n * Value Object for add or update gateway api.\n *\n * @author cdfive\n * @since 1.7.0\n */",
        "/**\n * 网关 API URL 匹配谓词项，定义匹配模式与策略。\n * <p>matchStrategy 常量见 {@code SentinelGatewayConstants}：\n * 0 精确匹配、1 前缀匹配、2 正则匹配。\n *\n * @author cdfive\n * @since 1.7.0\n */",
    ),
    (
        "    /**\n     * The pattern for matching url.\n     */",
        "    /** URL 匹配模式（路径或正则表达式）。 */",
    ),
    (
        "    /**\n     * The matching Strategy in url. Constants are defined in class SentinelGatewayConstants.\\\n     *\n     * <ul>\n     *     <li>0(URL_MATCH_STRATEGY_EXACT): exact match mode</li>\n     *     <li>1(URL_MATCH_STRATEGY_PREFIX): prefix match mode</li>\n     *     <li>2(URL_MATCH_STRATEGY_REGEX): regex match mode</li>\n     * </ul>\n     */",
        "    /**\n     * URL 匹配策略，常量定义于 {@code SentinelGatewayConstants}：\n     * <ul>\n     *     <li>0（URL_MATCH_STRATEGY_EXACT）：精确匹配</li>\n     *     <li>1（URL_MATCH_STRATEGY_PREFIX）：前缀匹配</li>\n     *     <li>2（URL_MATCH_STRATEGY_REGEX）：正则匹配</li>\n     * </ul>\n     */",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/vo/gateway/api/UpdateApiReqVo.java"] = [
    (
        "/**\n * Value Object for update gateway api.\n *\n * @author cdfive\n * @since 1.7.0\n */",
        "/**\n * 更新网关 API 定义请求体，按 id 定位规则并替换谓词列表。\n *\n * @author cdfive\n * @since 1.7.0\n */",
    ),
    (
        "    private Long id;\n\n    private String app;\n\n    private List<ApiPredicateItemVo> predicateItems;",
        "    /** Dashboard 侧 API 定义主键 id。 */\n    private Long id;\n\n    /** 所属应用名。 */\n    private String app;\n\n    /** 更新后的 URL 匹配谓词项列表。 */\n    private List<ApiPredicateItemVo> predicateItems;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/vo/gateway/rule/AddFlowRuleReqVo.java"] = [
    (
        "/**\n * Value Object for add gateway flow rule.\n *\n * @author cdfive\n * @since 1.7.0\n */",
        "/**\n * 新增网关流控规则请求体，含资源标识、限流阈值、统计窗口及可选参数流控项。\n *\n * @author cdfive\n * @since 1.7.0\n */",
    ),
    (
        "    private String app;\n\n    private String ip;\n\n    private Integer port;\n\n    private String resource;\n\n    private Integer resourceMode;\n\n    private Integer grade;\n\n    private Double count;\n\n    private Long interval;\n\n    private Integer intervalUnit;\n\n    private Integer controlBehavior;\n\n    private Integer burst;\n\n    private Integer maxQueueingTimeoutMs;\n\n    private GatewayParamFlowItemVo paramItem;",
        "    /** 目标应用名。 */\n    private String app;\n\n    /** 目标客户端机器 IP。 */\n    private String ip;\n\n    /** 目标客户端机器端口。 */\n    private Integer port;\n\n    /** 受流控资源名（路由 ID 或 API 分组名）。 */\n    private String resource;\n\n    /** 资源模式（路由/API 分组等，见 {@code SentinelGatewayConstants}）。 */\n    private Integer resourceMode;\n\n    /** 限流维度（QPS/并发线程数等）。 */\n    private Integer grade;\n\n    /** 限流阈值。 */\n    private Double count;\n\n    /** 统计窗口长度。 */\n    private Long interval;\n\n    /** 统计窗口时间单位。 */\n    private Integer intervalUnit;\n\n    /** 流控效果（直接拒绝/匀速排队等）。 */\n    private Integer controlBehavior;\n\n    /** 突发流量允许额外通过的请求数。 */\n    private Integer burst;\n\n    /** 匀速排队模式下的最大排队超时（毫秒）。 */\n    private Integer maxQueueingTimeoutMs;\n\n    /** 可选的参数流控匹配项。 */\n    private GatewayParamFlowItemVo paramItem;",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/vo/gateway/rule/GatewayParamFlowItemVo.java"] = [
    (
        "/**\n * Value Object for add or update gateway flow rule.\n *\n * @author cdfive\n * @since 1.7.0\n */",
        "/**\n * 网关参数流控匹配项视图，定义参数解析策略、字段名、模式与匹配方式。\n * <p>用于 {@link AddFlowRuleReqVo} 中按请求参数维度细化限流。\n *\n * @author cdfive\n * @since 1.7.0\n */",
    ),
    (
        "    private Integer parseStrategy;\n\n    private String fieldName;\n\n    private String pattern;\n\n    private Integer matchStrategy;",
        "    /** 参数解析策略（Header/URL 参数/客户端 IP 等）。 */\n    private Integer parseStrategy;\n\n    /** 待解析的字段名（如 Header 名或参数名）。 */\n    private String fieldName;\n\n    /** 参数值匹配模式。 */\n    private String pattern;\n\n    /** 参数值匹配策略（精确/包含/正则等）。 */\n    private Integer matchStrategy;",
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
    index_file = Path("/tmp/git-index-sentinel-w21a")
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
        return 1

    analyzed_paths = [f"sentinel/1.8.10/analyzed/{rel}" for rel in BATCH_LIST]
    script_path = f"scripts/{SCRIPT_NAME}"
    sha, tree_count = isolated_index_commit(
        "sentinel 1.8.10: Chinese-annotate wave 21a [0:15]",
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
    queue_paths = [
        "sentinel/1.8.10/_reports/class-queue/done.txt",
        "sentinel/1.8.10/_reports/class-queue/pending.txt",
        "sentinel/1.8.10/_reports/class-queue/batch.json",
        "sentinel/1.8.10/_reports/class-queue/worker.log",
    ]
    queue_sha, _ = isolated_index_commit(
        "queue: mark sentinel 1.8.10 wave21a done",
        queue_paths,
        base_ref="HEAD",
    )
    subprocess.run(["git", "-C", str(ROOT), "push", "origin", "main"], check=True)

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
                "all_chinese": all(chinese.values()),
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
