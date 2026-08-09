#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-20a block [0:15] (dashboard rule entities/discovery/domain)."""
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
SCRIPT_NAME = "annotate_sentinel_wave20a_batch0_15.py"
BATCH_LIST = Path("/tmp/sentinel_w20a.txt").read_text(encoding="utf-8").strip().split("\n")
MARK_NOTE = "wave20a"

GUARD_FILES = [
    VER
    / "analyzed/sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/connection/ScanIdleConnectionTask.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/datasource/entity/rule/AbstractRuleEntity.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 0.2.1\n */",
        "/**\n * 规则实体抽象基类，封装 Dashboard 侧通用元数据与 {@link AbstractRule} 载荷。\n * <p>子类通过泛型 {@code T} 绑定具体规则类型，{@link #toRule()} 直接返回内存中的 rule 对象。\n *\n * @author Eric Zhao\n * @since 0.2.1\n */",
    ),
    (
        "    protected Long id;",
        "    /** Dashboard 仓库中的规则主键 id。 */\n    protected Long id;",
    ),
    (
        "    protected String app;\n    protected String ip;\n    protected Integer port;",
        "    /** 规则所属应用名。 */\n    protected String app;\n    /** 规则绑定的客户端机器 IP。 */\n    protected String ip;\n    /** 规则绑定的客户端机器端口。 */\n    protected Integer port;",
    ),
    (
        "    protected T rule;",
        "    /** 具体 Sentinel 规则对象（流控/授权/热点参数等）。 */\n    protected T rule;",
    ),
    (
        "    private Date gmtCreate;\n    private Date gmtModified;",
        "    /** 记录创建时间。 */\n    private Date gmtCreate;\n    /** 记录最后修改时间。 */\n    private Date gmtModified;",
    ),
    (
        "    public T getRule() {",
        "    /** @return 内嵌的 Sentinel 规则对象 */\n    public T getRule() {",
    ),
    (
        "    public AbstractRuleEntity<T> setRule(T rule) {",
        "    /** @param rule 要绑定的规则对象 */\n    public AbstractRuleEntity<T> setRule(T rule) {",
    ),
    (
        "    @Override\n    public T toRule() {",
        "    /** 将实体转换为客户端可用的 {@link Rule} 实例。 */\n    @Override\n    public T toRule() {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/datasource/entity/rule/AuthorityRuleEntity.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 0.2.1\n */",
        "/**\n * 授权（黑白名单）规则实体，包装 {@link AuthorityRule}。\n * <p>提供从客户端规则到 Dashboard 实体的工厂方法；\n * 带 {@link JsonIgnore} 的 getter 便于 API 层读取规则字段而不重复序列化 rule 对象。\n *\n * @author Eric Zhao\n * @since 0.2.1\n */",
    ),
    (
        "    public AuthorityRuleEntity() {",
        "    /** 无参构造，供 JSON 反序列化使用。 */\n    public AuthorityRuleEntity() {",
    ),
    (
        "    public AuthorityRuleEntity(AuthorityRule authorityRule) {",
        "    /** @param authorityRule 非空的授权规则对象 */\n    public AuthorityRuleEntity(AuthorityRule authorityRule) {",
    ),
    (
        "    public static AuthorityRuleEntity fromAuthorityRule(String app, String ip, Integer port, AuthorityRule rule) {",
        "    /** 从客户端 {@link AuthorityRule} 构建带机器绑定的 Dashboard 实体。 */\n    public static AuthorityRuleEntity fromAuthorityRule(String app, String ip, Integer port, AuthorityRule rule) {",
    ),
    (
        "    @JsonIgnore\n    @JSONField(serialize = false)\n    public String getLimitApp() {",
        "    /** @return 受控来源应用（limitApp） */\n    @JsonIgnore\n    @JSONField(serialize = false)\n    public String getLimitApp() {",
    ),
    (
        "    @JsonIgnore\n    @JSONField(serialize = false)\n    public String getResource() {",
        "    /** @return 受保护资源名 */\n    @JsonIgnore\n    @JSONField(serialize = false)\n    public String getResource() {",
    ),
    (
        "    @JsonIgnore\n    @JSONField(serialize = false)\n    public int getStrategy() {",
        "    /** @return 授权策略（白名单/黑名单） */\n    @JsonIgnore\n    @JSONField(serialize = false)\n    public int getStrategy() {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/datasource/entity/rule/DegradeRuleEntity.java"] = [
    (
        "/**\n * @author leyou\n */",
        "/**\n * 熔断降级规则实体，字段与 {@link DegradeRule} 一一对应。\n * <p>支持从客户端规则导入及 {@link #toRule()} 回写，含慢调用比例、统计窗口等 1.8+ 字段。\n *\n * @author leyou\n */",
    ),
    (
        "    private String resource;\n    private String limitApp;\n    private Double count;\n    private Integer timeWindow;\n    private Integer grade;",
        "    /** 受保护资源名。 */\n    private String resource;\n    /** 来源应用限制，默认 default。 */\n    private String limitApp;\n    /** 熔断阈值（异常比例/慢调用比例/异常数等，取决于 grade）。 */\n    private Double count;\n    /** 熔断时长（秒）。 */\n    private Integer timeWindow;\n    /** 熔断策略 grade（慢调用比例/异常比例/异常数）。 */\n    private Integer grade;",
    ),
    (
        "    private Integer minRequestAmount;\n    private Double slowRatioThreshold;\n    private Integer statIntervalMs;",
        "    /** 触发熔断所需的最小请求数。 */\n    private Integer minRequestAmount;\n    /** 慢调用比例阈值（慢调用比例模式）。 */\n    private Double slowRatioThreshold;\n    /** 统计窗口长度（毫秒）。 */\n    private Integer statIntervalMs;",
    ),
    (
        "    public static DegradeRuleEntity fromDegradeRule(String app, String ip, Integer port, DegradeRule rule) {",
        "    /** 从客户端 {@link DegradeRule} 拷贝字段并绑定 app/ip/port。 */\n    public static DegradeRuleEntity fromDegradeRule(String app, String ip, Integer port, DegradeRule rule) {",
    ),
    (
        "    @Override\n    public DegradeRule toRule() {",
        "    /** 组装 {@link DegradeRule}，可选字段仅在非 null 时写入。 */\n    @Override\n    public DegradeRule toRule() {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/datasource/entity/rule/FlowRuleEntity.java"] = [
    (
        "/**\n * @author leyou\n */",
        "/**\n * 流控规则实体，映射 {@link FlowRule} 的全部配置项。\n * <p>含限流维度、流控效果、关联/链路策略及集群流控 {@link ClusterFlowConfig}。\n *\n * @author leyou\n */",
    ),
    (
        "    /**\n     * 0. default, 1. warm up, 2. rate limiter\n     */",
        "    /** 流控效果：0 直接拒绝，1 预热，2 匀速排队。 */",
    ),
    (
        "    /**\n     * max queueing time in rate limiter behavior\n     */",
        "    /** 匀速排队模式下的最大排队等待时间（毫秒）。 */",
    ),
    (
        "    private boolean clusterMode;\n    /**\n     * Flow rule config for cluster mode.\n     */",
        "    /** 是否启用集群流控模式。 */\n    private boolean clusterMode;\n    /** 集群流控模式下的 {@link ClusterFlowConfig} 配置。 */",
    ),
    (
        "    public static FlowRuleEntity fromFlowRule(String app, String ip, Integer port, FlowRule rule) {",
        "    /** 从客户端 {@link FlowRule} 构建 Dashboard 实体。 */\n    public static FlowRuleEntity fromFlowRule(String app, String ip, Integer port, FlowRule rule) {",
    ),
    (
        "    @Override\n    public FlowRule toRule() {",
        "    /** 转换为 {@link FlowRule}，controlBehavior 等可选字段按需设置。 */\n    @Override\n    public FlowRule toRule() {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/datasource/entity/rule/ParamFlowRuleEntity.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 0.2.1\n */",
        "/**\n * 热点参数流控规则实体，包装 {@link ParamFlowRule}。\n * <p>通过 {@link JsonIgnore} getter 暴露参数索引、阈值、例外项及集群配置等字段。\n *\n * @author Eric Zhao\n * @since 0.2.1\n */",
    ),
    (
        "    public ParamFlowRuleEntity() {",
        "    /** 无参构造，供 JSON 反序列化使用。 */\n    public ParamFlowRuleEntity() {",
    ),
    (
        "    public ParamFlowRuleEntity(ParamFlowRule rule) {",
        "    /** @param rule 非空的热点参数流控规则 */\n    public ParamFlowRuleEntity(ParamFlowRule rule) {",
    ),
    (
        "    public static ParamFlowRuleEntity fromParamFlowRule(String app, String ip, Integer port, ParamFlowRule rule) {",
        "    /** 从客户端 {@link ParamFlowRule} 构建带机器绑定的 Dashboard 实体。 */\n    public static ParamFlowRuleEntity fromParamFlowRule(String app, String ip, Integer port, ParamFlowRule rule) {",
    ),
    (
        "    @JsonIgnore\n    @JSONField(serialize = false)\n    public Integer getParamIdx() {",
        "    /** @return 热点参数在方法参数列表中的索引 */\n    @JsonIgnore\n    @JSONField(serialize = false)\n    public Integer getParamIdx() {",
    ),
    (
        "    @JsonIgnore\n    @JSONField(serialize = false)\n    public List<ParamFlowItem> getParamFlowItemList() {",
        "    /** @return 参数例外项列表（特定参数值单独限流） */\n    @JsonIgnore\n    @JSONField(serialize = false)\n    public List<ParamFlowItem> getParamFlowItemList() {",
    ),
    (
        "    @JsonIgnore\n    @JSONField(serialize = false)\n    public boolean isClusterMode() {",
        "    /** @return 是否启用集群热点参数流控 */\n    @JsonIgnore\n    @JSONField(serialize = false)\n    public boolean isClusterMode() {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/datasource/entity/rule/RuleEntity.java"] = [
    (
        "/**\n * @author leyou\n */",
        "/**\n * Dashboard 规则实体通用接口。\n * <p>所有规则类型实体均绑定 app/ip/port 元数据，并可转换为客户端 {@link Rule}。\n *\n * @author leyou\n */",
    ),
    (
        "    Long getId();",
        "    /** @return Dashboard 侧规则 id */\n    Long getId();",
    ),
    (
        "    void setId(Long id);",
        "    /** @param id 规则主键 */\n    void setId(Long id);",
    ),
    (
        "    String getApp();",
        "    /** @return 所属应用名 */\n    String getApp();",
    ),
    (
        "    String getIp();",
        "    /** @return 绑定机器 IP */\n    String getIp();",
    ),
    (
        "    Integer getPort();",
        "    /** @return 绑定机器端口 */\n    Integer getPort();",
    ),
    (
        "    Date getGmtCreate();",
        "    /** @return 创建时间 */\n    Date getGmtCreate();",
    ),
    (
        "    Rule toRule();",
        "    /** 转换为 Sentinel 客户端 {@link Rule} 实例。 */\n    Rule toRule();",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/datasource/entity/rule/SystemRuleEntity.java"] = [
    (
        "/**\n * @author leyou\n */",
        "/**\n * 系统保护规则实体，对应 {@link SystemRule} 的全局阈值配置。\n * <p>可限制系统负载、平均 RT、入口 QPS、线程数及 CPU 使用率等。\n *\n * @author leyou\n */",
    ),
    (
        "    private Double highestSystemLoad;\n    private Long avgRt;\n    private Long maxThread;\n    private Double qps;\n    private Double highestCpuUsage;",
        "    /** 系统 LOAD 上限（-1 表示不启用）。 */\n    private Double highestSystemLoad;\n    /** 所有入口平均 RT 上限（毫秒）。 */\n    private Long avgRt;\n    /** 入口并发线程数上限。 */\n    private Long maxThread;\n    /** 入口总 QPS 上限。 */\n    private Double qps;\n    /** CPU 使用率上限（0~1）。 */\n    private Double highestCpuUsage;",
    ),
    (
        "    public static SystemRuleEntity fromSystemRule(String app, String ip, Integer port, SystemRule rule) {",
        "    /** 从客户端 {@link SystemRule} 拷贝阈值并绑定机器信息。 */\n    public static SystemRuleEntity fromSystemRule(String app, String ip, Integer port, SystemRule rule) {",
    ),
    (
        "    @Override\n    public SystemRule toRule() {",
        "    /** 组装 {@link SystemRule} 供客户端加载。 */\n    @Override\n    public SystemRule toRule() {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/discovery/AppInfo.java"] = [
    (
        "public class AppInfo {",
        "/**\n * 已注册 Sentinel 应用及其机器集合。\n * <p>维护心跳状态，并依据 {@link DashboardConfig} 判定是否在侧栏展示或应被移除。\n */\npublic class AppInfo {",
    ),
    (
        "    private String app = \"\";",
        "    /** 应用名称。 */\n    private String app = \"\";",
    ),
    (
        "    private Integer appType = 0;",
        "    /** 应用类型标识（客户端上报）。 */\n    private Integer appType = 0;",
    ),
    (
        "    private Set<MachineInfo> machines = ConcurrentHashMap.newKeySet();",
        "    /** 当前应用下已注册的机器集合（线程安全）。 */\n    private Set<MachineInfo> machines = ConcurrentHashMap.newKeySet();",
    ),
    (
        "    /**\n     * Get the current machines.\n     *\n     * @return a new copy of the current machines.\n     */",
        "    /**\n     * 获取当前机器集合的副本，避免外部直接修改内部 Set。\n     *\n     * @return 机器信息副本\n     */",
    ),
    (
        "    public boolean addMachine(MachineInfo machineInfo) {",
        "    /** 添加或更新机器（同 app/ip/port 先移除再插入）。 */\n    public boolean addMachine(MachineInfo machineInfo) {",
    ),
    (
        "    public synchronized boolean removeMachine(String ip, int port) {",
        "    /** 按 IP 与端口移除本应用下的机器。 */\n    public synchronized boolean removeMachine(String ip, int port) {",
    ),
    (
        "    public Optional<MachineInfo> getMachine(String ip, int port) {",
        "    /** 按 IP 与端口查找机器。 */\n    public Optional<MachineInfo> getMachine(String ip, int port) {",
    ),
    (
        "    public Optional<MachineInfo> getMachine(String ip) {",
        "    /** 按 IP 查找第一台匹配机器（端口未指定时）。 */\n    public Optional<MachineInfo> getMachine(String ip) {",
    ),
    (
        "    private boolean heartbeatJudge(final int threshold) {",
        "    /** 依据阈值判断应用是否仍“存活”（存在健康机器或最近心跳未超时）。 */\n    private boolean heartbeatJudge(final int threshold) {",
    ),
    (
        "                // No healthy machines.",
        "                // 无健康机器时，看最近一次心跳是否仍在阈值内。",
    ),
    (
        "    /**\n     * Check whether current application has no healthy machines and should not be displayed.\n     *\n     * @return true if the application should be displayed in the sidebar, otherwise false\n     */",
        "    /**\n     * 是否应在 Dashboard 侧栏展示。\n     * <p>无健康机器且最后心跳超过 {@link DashboardConfig#getHideAppNoMachineMillis()} 时隐藏。\n     *\n     * @return true 表示应展示\n     */",
    ),
    (
        "    /**\n     * Check whether current application has no healthy machines and should be removed.\n     *\n     * @return true if the application is dead and should be removed, otherwise false\n     */",
        "    /**\n     * 是否应被从注册表移除（“死亡”应用）。\n     *\n     * @return true 表示应移除\n     */",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/discovery/AppManagement.java"] = [
    (
        "@Component\npublic class AppManagement implements MachineDiscovery {",
        "/**\n * 机器发现门面组件，委托 {@link SimpleMachineDiscovery} 管理应用与机器注册表。\n * <p>Spring 容器启动后通过 {@link #init()} 注入具体发现实现。\n */\n@Component\npublic class AppManagement implements MachineDiscovery {",
    ),
    (
        "    @PostConstruct\n    public void init() {",
        "    /** 初始化时绑定 {@link SimpleMachineDiscovery} 实现。 */\n    @PostConstruct\n    public void init() {",
    ),
    (
        "    public boolean isValidMachineOfApp(String app, String ip) {",
        "    /** 校验指定 IP 是否属于该应用下已注册的机器。 */\n    public boolean isValidMachineOfApp(String app, String ip) {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/discovery/MachineDiscovery.java"] = [
    (
        "public interface MachineDiscovery {",
        "/**\n * 机器与应用发现 SPI，供 Dashboard 查询注册表及增删机器。\n */\npublic interface MachineDiscovery {",
    ),
    (
        "    String UNKNOWN_APP_NAME = \"CLUSTER_NOT_STARTED\";",
        "    /** 集群未启动时使用的占位应用名。 */\n    String UNKNOWN_APP_NAME = \"CLUSTER_NOT_STARTED\";",
    ),
    (
        "    List<String> getAppNames();",
        "    /** @return 全部已注册应用名称列表 */\n    List<String> getAppNames();",
    ),
    (
        "    Set<AppInfo> getBriefApps();",
        "    /** @return 应用摘要集合（含机器列表） */\n    Set<AppInfo> getBriefApps();",
    ),
    (
        "    AppInfo getDetailApp(String app);",
        "    /** @param app 应用名\n     * @return 应用详情，不存在时返回 null */\n    AppInfo getDetailApp(String app);",
    ),
    (
        "    /**\n     * Remove the given app from the application registry.\n     *\n     * @param app application name\n     * @since 1.5.0\n     */",
        "    /**\n     * 从注册表移除整个应用及其全部机器。\n     *\n     * @param app 应用名称\n     * @since 1.5.0\n     */",
    ),
    (
        "    long addMachine(MachineInfo machineInfo);",
        "    /** 注册或更新一台客户端机器心跳信息。 */\n    long addMachine(MachineInfo machineInfo);",
    ),
    (
        "    /**\n     * Remove the given machine instance from the application registry.\n     *\n     * @param app the application name of the machine\n     * @param ip machine IP\n     * @param port machine port\n     * @return true if removed, otherwise false\n     * @since 1.5.0\n     */",
        "    /**\n     * 从指定应用中移除一台机器。\n     *\n     * @param app 应用名\n     * @param ip 机器 IP\n     * @param port 机器端口\n     * @return 移除成功返回 true\n     * @since 1.5.0\n     */",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/discovery/MachineInfo.java"] = [
    (
        "public class MachineInfo implements Comparable<MachineInfo> {",
        "/**\n * Sentinel 客户端机器实例信息，含心跳时间与客户端版本。\n * <p>实现 {@link Comparable} 以便在集合中按端口、应用名、IP 排序。\n */\npublic class MachineInfo implements Comparable<MachineInfo> {",
    ),
    (
        "    private String app = \"\";\n    private Integer appType = 0;\n    private String hostname = \"\";\n    private String ip = \"\";\n    private Integer port = -1;\n    private long lastHeartbeat;\n    private long heartbeatVersion;",
        "    /** 所属应用名。 */\n    private String app = \"\";\n    /** 应用类型。 */\n    private Integer appType = 0;\n    /** 主机名。 */\n    private String hostname = \"\";\n    /** 机器 IP。 */\n    private String ip = \"\";\n    /** 客户端端口，-1 表示尚未就绪。 */\n    private Integer port = -1;\n    /** 最后一次心跳时间戳（毫秒）。 */\n    private long lastHeartbeat;\n    /** 心跳协议版本号。 */\n    private long heartbeatVersion;",
    ),
    (
        "    /**\n     * Indicates the version of Sentinel client (since 0.2.0).\n     */",
        "    /** Sentinel 客户端版本号（0.2.0 起上报）。 */",
    ),
    (
        "    public static MachineInfo of(String app, String ip, Integer port) {",
        "    /** 快速构造仅含 app/ip/port 的机器信息。 */\n    public static MachineInfo of(String app, String ip, Integer port) {",
    ),
    (
        "    public String toHostPort() {",
        "    /** @return {@code ip:port} 形式的主机端口字符串 */\n    public String toHostPort() {",
    ),
    (
        "    public boolean isHealthy() {",
        "    /** 距上次心跳是否在 {@link DashboardConfig#getUnhealthyMachineMillis()} 以内。 */\n    public boolean isHealthy() {",
    ),
    (
        "    /**\n     * whether dead should be removed\n     * \n     * @return\n     */",
        "    /**\n     * 是否超过自动移除阈值，应被从注册表清理。\n     *\n     * @return true 表示已“死亡”且应移除\n     */",
    ),
    (
        "    @Override\n    public int compareTo(MachineInfo o) {",
        "    /** 先比端口，再比应用名（忽略大小写），最后比 IP。 */\n    @Override\n    public int compareTo(MachineInfo o) {",
    ),
    (
        "    /**\n     * Information for log\n     *\n     * @return\n     */",
        "    /**\n     * 生成日志友好的单行描述。\n     *\n     * @return {@code app|ip|port|version}\n     */",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/discovery/SimpleMachineDiscovery.java"] = [
    (
        "/**\n * @author leyou\n */",
        "/**\n * 基于内存 {@link ConcurrentHashMap} 的机器发现实现。\n * <p>按应用名维护 {@link AppInfo}，支持心跳注册、机器/应用移除及列表查询。\n *\n * @author leyou\n */",
    ),
    (
        "    private final ConcurrentMap<String, AppInfo> apps = new ConcurrentHashMap<>();",
        "    /** 应用名 → {@link AppInfo} 注册表。 */\n    private final ConcurrentMap<String, AppInfo> apps = new ConcurrentHashMap<>();",
    ),
    (
        "    @Override\n    public long addMachine(MachineInfo machineInfo) {",
        "    /** 注册机器，不存在应用时自动创建 {@link AppInfo}。 */\n    @Override\n    public long addMachine(MachineInfo machineInfo) {",
    ),
    (
        "    @Override\n    public boolean removeMachine(String app, String ip, int port) {",
        "    /** 从指定应用移除机器，应用不存在时返回 false。 */\n    @Override\n    public boolean removeMachine(String app, String ip, int port) {",
    ),
    (
        "    @Override\n    public void removeApp(String app) {",
        "    /** 从注册表删除整个应用条目。 */\n    @Override\n    public void removeApp(String app) {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/ResourceTreeNode.java"] = [
    (
        "/**\n * @author leyou\n */",
        "/**\n * 资源调用树节点，用于 Dashboard 展示实时 QPS、RT 等指标。\n * <p>由客户端 {@link NodeVo} 列表构建父子树，并支持关键字过滤可见性。\n *\n * @author leyou\n */",
    ),
    (
        "    private String id;\n    private String parentId;\n    private String resource;",
        "    /** 节点唯一 id。 */\n    private String id;\n    /** 父节点 id，根节点为空。 */\n    private String parentId;\n    /** 资源名称。 */\n    private String resource;",
    ),
    (
        "    private boolean visible = true;",
        "    /** 搜索过滤后是否在 UI 中可见。 */\n    private boolean visible = true;",
    ),
    (
        "    public static ResourceTreeNode fromNodeVoList(List<NodeVo> nodeVos) {",
        "    /** 将扁平 {@link NodeVo} 列表组装为单棵调用树根节点。 */\n    public static ResourceTreeNode fromNodeVoList(List<NodeVo> nodeVos) {",
    ),
    (
        "            // real root",
        "            // 无 parentId 的节点为调用树根。",
    ),
    (
        "                // impossible",
        "                // 父节点尚未出现，正常不应发生。",
    ),
    (
        "    public void searchIgnoreCase(String searchKey) {",
        "    /** 对整棵树执行忽略大小写的资源名关键字过滤。 */\n    public void searchIgnoreCase(String searchKey) {",
    ),
    (
        "    /**\n     * This node is visible only when searchKey matches this.resource or at least\n     * one of this's children is visible\n     */",
        "    /**\n     * 递归标记可见性：资源名匹配或任一子节点可见则本节点可见。\n     */",
    ),
    (
        "        // empty matches all",
        "        // 空关键字视为匹配全部节点。",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/Result.java"] = [
    (
        "/**\n * @author leyou\n * @author Eric Zhao\n */",
        "/**\n * Dashboard REST API 统一响应包装。\n * <p>包含 success、code、msg 与泛型 data 载荷。\n *\n * @author leyou\n * @author Eric Zhao\n */",
    ),
    (
        "    private boolean success;\n    private int code;\n    private String msg;\n    private R data;",
        "    /** 请求是否成功。 */\n    private boolean success;\n    /** 业务错误码，成功时通常为 0。 */\n    private int code;\n    /** 提示或错误信息。 */\n    private String msg;\n    /** 响应数据体。 */\n    private R data;",
    ),
    (
        "    public static <R> Result<R> ofSuccess(R data) {",
        "    /** 构造带数据的成功响应。 */\n    public static <R> Result<R> ofSuccess(R data) {",
    ),
    (
        "    public static <R> Result<R> ofSuccessMsg(String msg) {",
        "    /** 构造仅含自定义消息的成功响应（无 data）。 */\n    public static <R> Result<R> ofSuccessMsg(String msg) {",
    ),
    (
        "    public static <R> Result<R> ofFail(int code, String msg) {",
        "    /** 构造失败响应。 */\n    public static <R> Result<R> ofFail(int code, String msg) {",
    ),
    (
        "    public static <R> Result<R> ofThrowable(int code, Throwable throwable) {",
        "    /** 将异常类型与 message 写入 msg 的失败响应。 */\n    public static <R> Result<R> ofThrowable(int code, Throwable throwable) {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/domain/cluster/ClusterAppAssignResultVO.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 集群 Token Server 分配结果视图对象。\n * <p>记录分配失败的服务端/客户端机器集合及参与分配的总数。\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
    (
        "    private Set<String> failedServerSet;\n    private Set<String> failedClientSet;\n\n    private Integer totalCount;",
        "    /** 分配失败的 Token Server 机器标识集合。 */\n    private Set<String> failedServerSet;\n    /** 分配失败的客户端机器标识集合。 */\n    private Set<String> failedClientSet;\n\n    /** 参与本次分配的机器总数。 */\n    private Integer totalCount;",
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
    index_file = Path("/tmp/git-index-sentinel-w20a")
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
        "sentinel 1.8.10: Chinese-annotate wave 20a [0:15]",
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
        "queue: mark sentinel 1.8.10 wave20a done",
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
