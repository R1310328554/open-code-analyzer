#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-19b block [15:30] (dashboard controllers/entities)."""
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
BATCH_LIST = Path("/tmp/sentinel_w19b.txt").read_text(encoding="utf-8").strip().split("\n")
SCRIPT_NAME = "annotate_sentinel_wave19b_batch15_30.py"
MARK_NOTE = "wave19b"

GUARD_FILES = [
    VER
    / "analyzed/sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/connection/ScanIdleConnectionTask.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/controller/SystemController.java"] = [
    (
        "/**\n * @author leyou(lihao)\n */",
        "/**\n * 系统保护规则 REST 控制器，提供查询、新增、更新与删除接口，并将规则下发至 Sentinel 客户端。\n *\n * @author leyou(lihao)\n */",
    ),
    (
        "    private <R> Result<R> checkBasicParams(String app, String ip, Integer port) {",
        "    /** 校验 app、ip、port 基本参数及机器归属关系。 */\n    private <R> Result<R> checkBasicParams(String app, String ip, Integer port) {",
    ),
    (
        "    @GetMapping(\"/rules.json\")\n    @AuthAction(PrivilegeType.READ_RULE)\n    public Result<List<SystemRuleEntity>> apiQueryMachineRules(String app, String ip,",
        "    /**\n     * 查询指定机器上的系统保护规则，拉取后写入本地仓库。\n     */\n    @GetMapping(\"/rules.json\")\n    @AuthAction(PrivilegeType.READ_RULE)\n    public Result<List<SystemRuleEntity>> apiQueryMachineRules(String app, String ip,",
    ),
    (
        "    private int countNotNullAndNotNegative(Number... values) {",
        "    /** 统计参数数组中非空且非负的元素个数。 */\n    private int countNotNullAndNotNegative(Number... values) {",
    ),
    (
        "    @RequestMapping(\"/new.json\")\n    @AuthAction(PrivilegeType.WRITE_RULE)\n    public Result<SystemRuleEntity> apiAdd(String app, String ip, Integer port,",
        "    /**\n     * 新增系统保护规则；五个阈值字段中仅允许设置一个大于 0 的值。\n     */\n    @RequestMapping(\"/new.json\")\n    @AuthAction(PrivilegeType.WRITE_RULE)\n    public Result<SystemRuleEntity> apiAdd(String app, String ip, Integer port,",
    ),
    (
        "        // -1 is a fake value",
        "        // -1 表示未设置该阈值的占位值",
    ),
    (
        "    @GetMapping(\"/save.json\")\n    @AuthAction(PrivilegeType.WRITE_RULE)\n    public Result<SystemRuleEntity> apiUpdateIfNotNull(Long id, String app, Double highestSystemLoad,",
        "    /**\n     * 按 ID 更新系统保护规则，仅更新请求中非 null 的字段。\n     */\n    @GetMapping(\"/save.json\")\n    @AuthAction(PrivilegeType.WRITE_RULE)\n    public Result<SystemRuleEntity> apiUpdateIfNotNull(Long id, String app, Double highestSystemLoad,",
    ),
    (
        "    @RequestMapping(\"/delete.json\")\n    @AuthAction(PrivilegeType.DELETE_RULE)\n    public Result<?> delete(Long id) {",
        "    /** 按 ID 删除系统保护规则并下发最新规则集。 */\n    @RequestMapping(\"/delete.json\")\n    @AuthAction(PrivilegeType.DELETE_RULE)\n    public Result<?> delete(Long id) {",
    ),
    (
        "    private boolean publishRules(String app, String ip, Integer port) {",
        "    /** 将指定机器上的系统规则通过 {@link SentinelApiClient} 下发至客户端。 */\n    private boolean publishRules(String app, String ip, Integer port) {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/controller/VersionController.java"] = [
    (
        "/**\n * @author hisenyuan\n * @since 1.7.0\n */",
        "/**\n * 控制台版本查询接口，从配置项 {@code sentinel.dashboard.version} 读取并返回主版本号。\n *\n * @author hisenyuan\n * @since 1.7.0\n */",
    ),
    (
        "    @GetMapping(\"/version\")\n    public Result<String> apiGetVersion() {",
        "    /** 返回控制台版本字符串，截取首个 \"-\" 之前的主版本部分。 */\n    @GetMapping(\"/version\")\n    public Result<String> apiGetVersion() {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/controller/cluster/ClusterAssignController.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 集群令牌服务端分配控制器，支持全量/单台分配与解绑操作。\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
    (
        "    @PostMapping(\"/all_server/{app}\")\n    public Result<ClusterAppAssignResultVO> apiAssignAllClusterServersOfApp(@PathVariable String app,",
        "    /**\n     * 为应用全量分配集群令牌服务端，覆盖现有映射。\n     *\n     * @param app 应用名\n     * @param assignRequest 包含 clusterMap 与 remainingList 的分配请求\n     */\n    @PostMapping(\"/all_server/{app}\")\n    public Result<ClusterAppAssignResultVO> apiAssignAllClusterServersOfApp(@PathVariable String app,",
    ),
    (
        "    @PostMapping(\"/single_server/{app}\")\n    public Result<ClusterAppAssignResultVO> apiAssignSingleClusterServersOfApp(@PathVariable String app,",
        "    /**\n     * 为应用分配单台集群令牌服务端。\n     *\n     * @param app 应用名\n     * @param assignRequest 包含 clusterMap 与 remainingList 的分配请求\n     */\n    @PostMapping(\"/single_server/{app}\")\n    public Result<ClusterAppAssignResultVO> apiAssignSingleClusterServersOfApp(@PathVariable String app,",
    ),
    (
        "    @PostMapping(\"/unbind_server/{app}\")\n    public Result<ClusterAppAssignResultVO> apiUnbindClusterServersOfApp(@PathVariable String app,",
        "    /**\n     * 解绑应用中指定机器 ID 集合对应的集群令牌服务端。\n     *\n     * @param app 应用名\n     * @param machineIds 待解绑的机器 ID 集合\n     */\n    @PostMapping(\"/unbind_server/{app}\")\n    public Result<ClusterAppAssignResultVO> apiUnbindClusterServersOfApp(@PathVariable String app,",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/controller/cluster/ClusterConfigController.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群模式配置与状态查询控制器，支持修改客户端/服务端配置及查询集群运行状态。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    @PostMapping(\"/config/modify_single\")\n    public Result<Boolean> apiModifyClusterConfig(@RequestBody String payload) {",
        "    /**\n     * 修改单台机器的集群配置，按 mode 区分客户端或服务端。\n     *\n     * @param payload JSON 请求体，须包含 mode 字段\n     */\n    @PostMapping(\"/config/modify_single\")\n    public Result<Boolean> apiModifyClusterConfig(@RequestBody String payload) {",
    ),
    (
        "                        // TODO: bad design here, should refactor!",
        "                        // TODO: 此处设计欠佳，后续应重构！",
    ),
    (
        "    private <T> Result<T> errorResponse(ExecutionException ex) {",
        "    /** 将 ExecutionException 转换为 Result，客户端不支持时返回 4041。 */\n    private <T> Result<T> errorResponse(ExecutionException ex) {",
    ),
    (
        "    @GetMapping(\"/state_single\")\n    public Result<ClusterUniversalStateVO> apiGetClusterState(@RequestParam String app,",
        "    /**\n     * 查询单台机器的集群通用状态（客户端/服务端模式等）。\n     */\n    @GetMapping(\"/state_single\")\n    public Result<ClusterUniversalStateVO> apiGetClusterState(@RequestParam String app,",
    ),
    (
        "    @GetMapping(\"/server_state/{app}\")\n    public Result<List<AppClusterServerStateWrapVO>> apiGetClusterServerStateOfApp(@PathVariable String app) {",
        "    /** 查询应用下所有机器的集群令牌服务端状态。 */\n    @GetMapping(\"/server_state/{app}\")\n    public Result<List<AppClusterServerStateWrapVO>> apiGetClusterServerStateOfApp(@PathVariable String app) {",
    ),
    (
        "    @GetMapping(\"/client_state/{app}\")\n    public Result<List<AppClusterClientStateWrapVO>> apiGetClusterClientStateOfApp(@PathVariable String app) {",
        "    /** 查询应用下所有机器的集群令牌客户端状态。 */\n    @GetMapping(\"/client_state/{app}\")\n    public Result<List<AppClusterClientStateWrapVO>> apiGetClusterClientStateOfApp(@PathVariable String app) {",
    ),
    (
        "    @GetMapping(\"/state/{app}\")\n    public Result<List<ClusterUniversalStatePairVO>> apiGetClusterStateOfApp(@PathVariable String app) {",
        "    /** 查询应用下所有机器的集群通用状态对列表。 */\n    @GetMapping(\"/state/{app}\")\n    public Result<List<ClusterUniversalStatePairVO>> apiGetClusterStateOfApp(@PathVariable String app) {",
    ),
    (
        "    private boolean isNotSupported(Throwable ex) {",
        "    /** 判断异常是否因客户端不支持集群流控命令导致。 */\n    private boolean isNotSupported(Throwable ex) {",
    ),
    (
        "            // If error occurred or cannot retrieve machine info, return true.",
        "            // 出错或无法获取机器信息时默认视为支持。",
    ),
    (
        "    private Result<Boolean> checkValidRequest(ClusterModifyRequest request) {",
        "    /** 校验集群配置修改请求的基本字段与版本兼容性。 */\n    private Result<Boolean> checkValidRequest(ClusterModifyRequest request) {",
    ),
    (
        "    private <R> Result<R> unsupportedVersion() {",
        "    /** 返回客户端版本不支持集群流控的错误结果（4041）。 */\n    private <R> Result<R> unsupportedVersion() {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/controller/gateway/GatewayApiController.java"] = [
    (
        "/**\n * Gateway api Controller for manage gateway api definitions.\n *\n * @author cdfive\n * @since 1.7.0\n */",
        "/**\n * 网关 API 定义管理控制器，提供查询、新增、更新与删除接口。\n *\n * @author cdfive\n * @since 1.7.0\n */",
    ),
    (
        "    @GetMapping(\"/list.json\")\n    @AuthAction(AuthService.PrivilegeType.READ_RULE)\n    public Result<List<ApiDefinitionEntity>> queryApis(String app, String ip, Integer port) {",
        "    /** 从客户端拉取网关 API 定义列表并写入本地仓库。 */\n    @GetMapping(\"/list.json\")\n    @AuthAction(AuthService.PrivilegeType.READ_RULE)\n    public Result<List<ApiDefinitionEntity>> queryApis(String app, String ip, Integer port) {",
    ),
    (
        "    @PostMapping(\"/new.json\")\n    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)\n    public Result<ApiDefinitionEntity> addApi(HttpServletRequest request, @RequestBody AddApiReqVo reqVo) {",
        "    /** 新增网关 API 定义，校验匹配规则后持久化并下发至客户端。 */\n    @PostMapping(\"/new.json\")\n    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)\n    public Result<ApiDefinitionEntity> addApi(HttpServletRequest request, @RequestBody AddApiReqVo reqVo) {",
    ),
    (
        "        // 检查API名称不能重复",
        "        // 检查 API 名称不能重复",
    ),
    (
        "    @PostMapping(\"/save.json\")\n    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)\n    public Result<ApiDefinitionEntity> updateApi(@RequestBody UpdateApiReqVo reqVo) {",
        "    /** 更新已有网关 API 定义的匹配规则并下发至客户端。 */\n    @PostMapping(\"/save.json\")\n    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)\n    public Result<ApiDefinitionEntity> updateApi(@RequestBody UpdateApiReqVo reqVo) {",
    ),
    (
        "    @PostMapping(\"/delete.json\")\n    @AuthAction(AuthService.PrivilegeType.DELETE_RULE)\n\n    public Result<Long> deleteApi(Long id) {",
        "    /** 按 ID 删除网关 API 定义并下发最新规则集。 */\n    @PostMapping(\"/delete.json\")\n    @AuthAction(AuthService.PrivilegeType.DELETE_RULE)\n\n    public Result<Long> deleteApi(Long id) {",
    ),
    (
        "    private boolean publishApis(String app, String ip, Integer port) {",
        "    /** 将指定机器上的网关 API 定义通过 {@link SentinelApiClient} 下发至客户端。 */\n    private boolean publishApis(String app, String ip, Integer port) {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/controller/v2/FlowControllerV2.java"] = [
    (
        "/**\n * Flow rule controller (v2).\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 流控规则 REST 控制器（v2），基于动态规则 Provider/Publisher 实现应用级规则管理。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    @GetMapping(\"/rules\")\n    @AuthAction(PrivilegeType.READ_RULE)\n    public Result<List<FlowRuleEntity>> apiQueryMachineRules(@RequestParam String app) {",
        "    /** 查询指定应用的流控规则，集群模式下以 flowId 作为规则 ID。 */\n    @GetMapping(\"/rules\")\n    @AuthAction(PrivilegeType.READ_RULE)\n    public Result<List<FlowRuleEntity>> apiQueryMachineRules(@RequestParam String app) {",
    ),
    (
        "    private <R> Result<R> checkEntityInternal(FlowRuleEntity entity) {",
        "    /** 校验流控规则实体的必填字段与策略/行为组合合法性。 */\n    private <R> Result<R> checkEntityInternal(FlowRuleEntity entity) {",
    ),
    (
        "    @PostMapping(\"/rule\")\n    @AuthAction(value = AuthService.PrivilegeType.WRITE_RULE)\n    public Result<FlowRuleEntity> apiAddFlowRule(@RequestBody FlowRuleEntity entity) {",
        "    /** 新增流控规则并发布至动态规则源。 */\n    @PostMapping(\"/rule\")\n    @AuthAction(value = AuthService.PrivilegeType.WRITE_RULE)\n    public Result<FlowRuleEntity> apiAddFlowRule(@RequestBody FlowRuleEntity entity) {",
    ),
    (
        "    @PutMapping(\"/rule/{id}\")\n    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)\n\n    public Result<FlowRuleEntity> apiUpdateFlowRule(@PathVariable(\"id\") Long id,",
        "    /** 按 ID 更新流控规则并发布至动态规则源。 */\n    @PutMapping(\"/rule/{id}\")\n    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)\n\n    public Result<FlowRuleEntity> apiUpdateFlowRule(@PathVariable(\"id\") Long id,",
    ),
    (
        "    @DeleteMapping(\"/rule/{id}\")\n    @AuthAction(PrivilegeType.DELETE_RULE)\n    public Result<Long> apiDeleteRule(@PathVariable(\"id\") Long id) {",
        "    /** 按 ID 删除流控规则并发布至动态规则源。 */\n    @DeleteMapping(\"/rule/{id}\")\n    @AuthAction(PrivilegeType.DELETE_RULE)\n    public Result<Long> apiDeleteRule(@PathVariable(\"id\") Long id) {",
    ),
    (
        "    private void publishRules(/*@NonNull*/ String app) throws Exception {",
        "    /** 将应用下全部流控规则通过 {@link DynamicRulePublisher} 发布。 */\n    private void publishRules(/*@NonNull*/ String app) throws Exception {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/datasource/entity/ApplicationEntity.java"] = [
    (
        "/**\n * @author leyou\n */",
        "/**\n * 应用持久化实体，对应控制台发现的应用信息。\n *\n * @author leyou\n */",
    ),
    (
        "    public AppInfo toAppInfo() {",
        "    /** 转换为运行时 {@link AppInfo} 对象。 */\n    public AppInfo toAppInfo() {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/datasource/entity/MachineEntity.java"] = [
    (
        "/**\n * @author leyou\n */",
        "/**\n * 机器持久化实体，记录应用下 Sentinel 客户端实例的注册信息。\n *\n * @author leyou\n */",
    ),
    (
        "    public MachineInfo toMachineInfo() {",
        "    /** 转换为运行时 {@link MachineInfo}，并将 timestamp 映射为心跳时间。 */\n    public MachineInfo toMachineInfo() {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/datasource/entity/MetricEntity.java"] = [
    (
        "/**\n * @author leyou\n */",
        "/**\n * 监控指标持久化实体，聚合单资源在某时间窗口内的 QPS、RT 等统计数据。\n *\n * @author leyou\n */",
    ),
    (
        "    /**\n     * summary rt of all success exit qps.\n     */",
        "    /** 所有成功请求 RT 的累计值（rt = avgRt × successQps）。 */\n",
    ),
    (
        "    public static MetricEntity copyOf(MetricEntity oldEntity) {",
        "    /** 浅拷贝给定 MetricEntity 的全部字段。 */\n    public static MetricEntity copyOf(MetricEntity oldEntity) {",
    ),
    (
        "    public synchronized void addPassQps(Long passQps) {",
        "    /** 累加通过 QPS。 */\n    public synchronized void addPassQps(Long passQps) {",
    ),
    (
        "    public synchronized void addBlockQps(Long blockQps) {",
        "    /** 累加被限流 QPS。 */\n    public synchronized void addBlockQps(Long blockQps) {",
    ),
    (
        "    public synchronized void addExceptionQps(Long exceptionQps) {",
        "    /** 累加异常 QPS。 */\n    public synchronized void addExceptionQps(Long exceptionQps) {",
    ),
    (
        "    public synchronized void addCount(int count) {",
        "    /** 累加本次聚合条目数。 */\n    public synchronized void addCount(int count) {",
    ),
    (
        "    public synchronized void addRtAndSuccessQps(double avgRt, Long successQps) {",
        "    /** 按 avgRt × successQps 累加 RT 与成功 QPS。 */\n    public synchronized void addRtAndSuccessQps(double avgRt, Long successQps) {",
    ),
    (
        "    /**\n     * {@link #rt} = {@code avgRt * successQps}\n     *\n     * @param avgRt      average rt of {@code successQps}\n     * @param successQps\n     */",
        "    /**\n     * 设置 RT 与成功 QPS，满足 {@link #rt} = {@code avgRt × successQps}。\n     *\n     * @param avgRt      成功 QPS 的平均 RT\n     * @param successQps 成功 QPS\n     */",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/datasource/entity/MetricPositionEntity.java"] = [
    (
        "/**\n * @author leyou\n */",
        "/**\n * 监控拉取位点实体，记录各机器上次拉取监控数据的最晚时间戳。\n *\n * @author leyou\n */",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/datasource/entity/SentinelVersion.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 0.2.1\n */",
        "/**\n * Sentinel 语义化版本模型，支持主/次/修订号比较与后缀标识。\n *\n * @author Eric Zhao\n * @since 0.2.1\n */",
    ),
    (
        "    public boolean greaterThan(SentinelVersion version) {",
        "    /** 判断当前版本是否严格大于给定版本。 */\n    public boolean greaterThan(SentinelVersion version) {",
    ),
    (
        "    public boolean greaterOrEqual(SentinelVersion version) {",
        "    /** 判断当前版本是否大于或等于给定版本。 */\n    public boolean greaterOrEqual(SentinelVersion version) {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/datasource/entity/gateway/ApiDefinitionEntity.java"] = [
    (
        "/**\n * Entity for {@link ApiDefinition}.\n *\n * @author cdfive\n * @since 1.7.0\n */",
        "/**\n * 网关 API 定义持久化实体，对应 {@link ApiDefinition}。\n *\n * @author cdfive\n * @since 1.7.0\n */",
    ),
    (
        "    public static ApiDefinitionEntity fromApiDefinition(String app, String ip, Integer port, ApiDefinition apiDefinition) {",
        "    /**\n     * 从 {@link ApiDefinition} 构造实体，含路径匹配谓词列表。\n     */\n    public static ApiDefinitionEntity fromApiDefinition(String app, String ip, Integer port, ApiDefinition apiDefinition) {",
    ),
    (
        "    public ApiDefinition toApiDefinition() {",
        "    /** 转换为 {@link ApiDefinition} 供客户端下发。 */\n    public ApiDefinition toApiDefinition() {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/datasource/entity/gateway/ApiPredicateItemEntity.java"] = [
    (
        "/**\n * Entity for {@link ApiPredicateItem}.\n *\n * @author cdfive\n * @since 1.7.0\n */",
        "/**\n * 网关 API 路径匹配谓词实体，对应 {@link ApiPredicateItem}。\n *\n * @author cdfive\n * @since 1.7.0\n */",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/datasource/entity/gateway/GatewayFlowRuleEntity.java"] = [
    (
        "/**\n * Entity for {@link GatewayFlowRule}.\n *\n * @author cdfive\n * @since 1.7.0\n */",
        "/**\n * 网关流控规则持久化实体，对应 {@link GatewayFlowRule}，含统计窗口单位换算逻辑。\n *\n * @author cdfive\n * @since 1.7.0\n */",
    ),
    (
        "    public static Long calIntervalSec(Long interval, Integer intervalUnit) {",
        "    /** 将 interval 与 intervalUnit 换算为秒级统计窗口。 */\n    public static Long calIntervalSec(Long interval, Integer intervalUnit) {",
    ),
    (
        "    public static Object[] parseIntervalSec(Long intervalSec) {",
        "    /** 将秒级窗口解析为 (interval, intervalUnit) 二元组，优先取最大单位。 */\n    public static Object[] parseIntervalSec(Long intervalSec) {",
    ),
    (
        "    public GatewayFlowRule toGatewayFlowRule() {",
        "    /** 转换为 {@link GatewayFlowRule} 供客户端下发。 */\n    public GatewayFlowRule toGatewayFlowRule() {",
    ),
    (
        "    public static GatewayFlowRuleEntity fromGatewayFlowRule(String app, String ip, Integer port, GatewayFlowRule rule) {",
        "    /** 从 {@link GatewayFlowRule} 构造实体，含参数流控项映射。 */\n    public static GatewayFlowRuleEntity fromGatewayFlowRule(String app, String ip, Integer port, GatewayFlowRule rule) {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/datasource/entity/gateway/GatewayParamFlowItemEntity.java"] = [
    (
        "/**\n * Entity for {@link GatewayParamFlowItem}.\n *\n * @author cdfive\n * @since 1.7.0\n */",
        "/**\n * 网关参数流控项实体，对应 {@link GatewayParamFlowItem}，定义参数解析与匹配策略。\n *\n * @author cdfive\n * @since 1.7.0\n */",
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
    index_file = Path("/tmp/git-index-sentinel-w19b")
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
        "sentinel 1.8.10: Chinese-annotate wave 19b [15:30]",
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
        "queue: mark sentinel 1.8.10 wave19b done",
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
