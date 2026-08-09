#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-22a block [0:15] (flow rule API + cluster assign/config + dashboard JS)."""
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
SCRIPT_NAME = "annotate_sentinel_wave22a_batch0_15.py"
BATCH_LIST = Path("/tmp/sentinel_w22a.txt").read_text(encoding="utf-8").strip().split("\n")
MARK_NOTE = "wave22a"

GUARD_FILES = [
    VER
    / "analyzed/sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/controller/SystemController.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/rule/FlowRuleApiProvider.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * 流控规则动态数据源提供者，从应用健康机器拉取 {@link FlowRuleEntity} 列表。\n * <p>优先选取最近心跳的健康机器作为数据源。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    @Override\n    public List<FlowRuleEntity> getRules(String appName) throws Exception {",
        "    /** 从指定应用最近心跳的健康机器拉取流控规则。 */\n    @Override\n    public List<FlowRuleEntity> getRules(String appName) throws Exception {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/rule/FlowRuleApiPublisher.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 流控规则动态发布器，将规则推送到应用下所有健康机器。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "            // TODO: parse the results",
        "            // TODO: 解析客户端返回结果并处理失败情况",
    ),
    (
        "    @Override\n    public void publish(String app, List<FlowRuleEntity> rules) throws Exception {",
        "    /** 向应用下每台健康机器推送流控规则列表。 */\n    @Override\n    public void publish(String app, List<FlowRuleEntity> rules) throws Exception {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/service/ClusterAssignService.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 集群令牌服务端/客户端分配服务接口，支持绑定、解绑与批量应用分配方案。\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
    (
        "    /**\n     * Unbind a specific cluster server and its clients.\n     *\n     * @param app app name\n     * @param machineId valid machine ID ({@code host@commandPort})\n     * @return assign result\n     */",
        "    /**\n     * 解绑指定集群令牌服务端及其关联客户端。\n     *\n     * @param app 应用名\n     * @param machineId 有效机器标识（{@code host@commandPort}）\n     * @return 分配操作结果\n     */",
    ),
    (
        "    /**\n     * Unbind a set of cluster servers and its clients.\n     *\n     * @param app app name\n     * @param machineIdSet set of valid machine ID ({@code host@commandPort})\n     * @return assign result\n     */",
        "    /**\n     * 批量解绑多台集群令牌服务端及其关联客户端。\n     *\n     * @param app 应用名\n     * @param machineIdSet 机器标识集合（{@code host@commandPort}）\n     * @return 分配操作结果\n     */",
    ),
    (
        "    /**\n     * Apply cluster server and client assignment for provided app.\n     *\n     * @param app app name\n     * @param clusterMap cluster assign map (server -> clients)\n     * @param remainingSet unassigned set of machine ID\n     * @return assign result\n     */",
        "    /**\n     * 对指定应用应用完整的集群服务端/客户端分配方案。\n     *\n     * @param app 应用名\n     * @param clusterMap 集群分配映射（服务端 → 客户端集合）\n     * @param remainingSet 未分配机器标识集合\n     * @return 分配操作结果\n     */",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/service/ClusterAssignServiceImpl.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 集群令牌分配服务实现，负责解绑、模式切换与传输/流控/命名空间配置下发。\n * <p>通过 {@link SentinelApiClient} 异步调用客户端 API，汇总失败机器集合。\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
    (
        "            // Modify mode to NOT-STARTED for all associated token clients.",
        "            // 将所有关联令牌客户端模式切换为 NOT-STARTED。",
    ),
    (
        "            // Modify mode to NOT-STARTED for all chosen token servers and associated token clients.",
        "            // 将所选令牌服务端及关联客户端模式切换为 NOT-STARTED。",
    ),
    (
        "        // Assign server and apply config.",
        "        // 分配服务端角色并下发传输/流控/命名空间配置。",
    ),
    (
        "        // Assign client of servers and apply config.",
        "        // 为各服务端分配客户端并下发客户端配置。",
    ),
    (
        "        // Unbind remaining (unassigned) machines.",
        "        // 解绑剩余未分配机器（切换为 NOT-STARTED）。",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/service/ClusterConfigService.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群配置查询与修改服务，封装客户端/服务端配置的拉取与推送。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    /**\n     * Get cluster state list of all available machines of provided application.\n     *\n     * @param app application name\n     * @return cluster state list of all available machines of the application\n     * @since 1.4.1\n     */",
        "    /**\n     * 获取指定应用所有健康机器的集群通用状态列表。\n     *\n     * @param app 应用名\n     * @return 各机器的 {@link ClusterUniversalStatePairVO} 列表\n     * @since 1.4.1\n     */",
    ),
    (
        "    public CompletableFuture<Void> modifyClusterClientConfig(ClusterClientModifyRequest request) {",
        "    /** 修改集群令牌客户端配置并切换为 CLIENT 模式。 */\n    public CompletableFuture<Void> modifyClusterClientConfig(ClusterClientModifyRequest request) {",
    ),
    (
        "    public CompletableFuture<Void> modifyClusterServerConfig(ClusterServerModifyRequest request) {",
        "    /** 修改集群令牌服务端配置（命名空间、传输、流控）并切换为 SERVER 模式。 */\n    public CompletableFuture<Void> modifyClusterServerConfig(ClusterServerModifyRequest request) {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/util/AsyncUtils.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * CompletableFuture 辅助工具，提供失败 Future 构造、批量等待与超时取值。\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
    (
        "    public static <R> CompletableFuture<R> newFailedFuture(Throwable ex) {",
        "    /** 构造已异常完成的 Future。 */\n    public static <R> CompletableFuture<R> newFailedFuture(Throwable ex) {",
    ),
    (
        "    public static <R> CompletableFuture<List<R>> sequenceFuture(List<CompletableFuture<R>> futures) {",
        "    /** 等待全部 Future 完成后收集非 null 结果（allOf 语义）。 */\n    public static <R> CompletableFuture<List<R>> sequenceFuture(List<CompletableFuture<R>> futures) {",
    ),
    (
        "    public static <R> CompletableFuture<List<R>> sequenceSuccessFuture(List<CompletableFuture<R>> futures) {",
        "    /** 并行等待各 Future 并收集非 null 结果，单个失败不影响其余。 */\n    public static <R> CompletableFuture<List<R>> sequenceSuccessFuture(List<CompletableFuture<R>> futures) {",
    ),
    (
        "    public static <T> T getValue(CompletableFuture<T> future) {",
        "    /** 带 10 秒超时的阻塞取值，异常时记录日志并返回 null。 */\n    public static <T> T getValue(CompletableFuture<T> future) {",
    ),
    (
        "    public static boolean isSuccessFuture(CompletableFuture future) {",
        "    /** 判断 Future 是否已成功完成（非异常、非取消）。 */\n    public static boolean isSuccessFuture(CompletableFuture future) {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/util/ClusterEntityUtils.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 集群状态实体转换工具，将 {@link ClusterUniversalStatePairVO} 列表聚合为\n * 服务端/客户端包装视图或 {@link ClusterGroupEntity} 分组。\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
    (
        "        // Handle token servers that belong to current app.",
        "        // 处理归属当前应用的令牌服务端。",
    ),
    (
        "        // Handle token servers from other app.",
        "        // 处理来自其他应用、经客户端配置引用的外部令牌服务端。",
    ),
    (
        "                // We are not able to get the commandPort of foreign token server directly.",
        "                // 外部令牌服务端的 commandPort 无法直接获取，仅用 ip:port 标识。",
    ),
    (
        "    public static List<AppClusterServerStateWrapVO> wrapToAppClusterServerState(",
        "    /** 将通用状态列表转换为应用维度令牌服务端状态包装视图。 */\n    public static List<AppClusterServerStateWrapVO> wrapToAppClusterServerState(",
    ),
    (
        "    public static List<AppClusterClientStateWrapVO> wrapToAppClusterClientState(",
        "    /** 将通用状态列表转换为应用维度令牌客户端状态包装视图。 */\n    public static List<AppClusterClientStateWrapVO> wrapToAppClusterClientState(",
    ),
    (
        "    public static List<ClusterGroupEntity> wrapToClusterGroup(List<ClusterUniversalStatePairVO> list) {",
        "    /** 按服务端地址聚合客户端，生成集群分组实体列表。 */\n    public static List<ClusterGroupEntity> wrapToClusterGroup(List<ClusterUniversalStatePairVO> list) {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/util/MachineUtils.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * 机器标识解析工具，支持 {@code ip@commandPort} 格式拆分。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    public static Optional<Integer> parseCommandPort(String machineIp) {",
        "    /** 从机器标识解析命令端口，格式无效时返回 empty。 */\n    public static Optional<Integer> parseCommandPort(String machineIp) {",
    ),
    (
        "    public static Optional<Tuple2<String, Integer>> parseCommandIpAndPort(String machineIp) {",
        "    /** 从机器标识解析 IP 与命令端口元组，格式无效时返回 empty。 */\n    public static Optional<Tuple2<String, Integer>> parseCommandIpAndPort(String machineIp) {",
    ),
]

R["sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/util/VersionUtils.java"] = [
    (
        "/**\n * Util class for parsing version.\n *\n * @author Eric Zhao\n * @since 0.2.1\n */",
        "/**\n * Sentinel 版本号解析工具，支持 {@code x.y.z-postfix} 格式。\n *\n * @author Eric Zhao\n * @since 0.2.1\n */",
    ),
    (
        "    /**\n     * Parse version of Sentinel from raw string.\n     *\n     * @param verStr version string\n     * @return parsed {@link SentinelVersion} if the version is valid; empty if\n     * there is something wrong with the format\n     */",
        "    /**\n     * 从原始字符串解析 Sentinel 版本号。\n     *\n     * @param verStr 版本字符串\n     * @return 格式合法时返回 {@link SentinelVersion}，否则 empty\n     */",
    ),
    (
        "            // postfix",
        "            // 解析后缀（如 -SNAPSHOT）",
    ),
    (
        "                // Start with \"-\"",
        "                // 以 \"-\" 开头，格式非法",
    ),
    (
        "                // End with \"-\"",
        "                // 以 \"-\" 结尾，忽略空后缀",
    ),
    (
        "            // x.x.x",
        "            // 解析主版本号 x.y.z",
    ),
    (
        "                // Wrong format, return empty.",
        "                // 主版本号 < 1，格式非法",
    ),
    (
        "            // Parse fail, return empty.",
        "            // 解析异常，返回 empty",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/app.js"] = [
    (
        "/**\n * @ngdoc overview\n * @name sentinelDashboardApp\n * @description\n * # sentinelDashboardApp\n *\n * Main module of the application.\n */",
        "/**\n * @ngdoc overview\n * @name sentinelDashboardApp\n * @description\n * # sentinelDashboardApp\n *\n * Sentinel Dashboard 前端主模块，注册路由、HTTP 拦截器与懒加载配置。\n */",
    ),
    (
        "          // If not auth, clear session in localStorage and jump to the login page",
        "          // 未授权时清除 localStorage 会话并跳转登录页",
    ),
    (
        "        // Resolved resource loading failure after configuring ContextPath",
        "        // 配置 ContextPath 后，为相对 URL 拼接 base 前缀",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/controllers/authority.js"] = [
    (
        "/**\n * Authority rule controller.\n */",
        "/**\n * 授权规则控制器，管理指定应用机器的黑白名单规则 CRUD。\n */",
    ),
    (
        "                        // $scope.machines = data.data;",
        "                        // 仅展示健康机器供选择",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/controllers/cluster_app_assign_manage.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/**\n * 集群令牌分配管理控制器：可视化编辑服务端-客户端映射并一键推送。\n */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "        const CLUSTER_MODE_CLIENT = 0;\n        const CLUSTER_MODE_SERVER = 1;\n        const DEFAULT_CLUSTER_SERVER_PORT = 18730;",
        "        const CLUSTER_MODE_CLIENT = 0;  // 客户端模式\n        const CLUSTER_MODE_SERVER = 1;    // 服务端模式\n        const DEFAULT_CLUSTER_SERVER_PORT = 18730;  // 默认 Token Server 端口",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/controllers/cluster_app_server_manage.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/**\n * 集群令牌服务端管理控制器（与分配页共用同一控制器实现）。\n */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "        const CLUSTER_MODE_CLIENT = 0;\n        const CLUSTER_MODE_SERVER = 1;\n        const DEFAULT_CLUSTER_SERVER_PORT = 18730;",
        "        const CLUSTER_MODE_CLIENT = 0;  // 客户端模式\n        const CLUSTER_MODE_SERVER = 1;    // 服务端模式\n        const DEFAULT_CLUSTER_SERVER_PORT = 18730;  // 默认 Token Server 端口",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/controllers/cluster_app_server_monitor.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/**\n * 集群令牌服务端监控控制器，展示各 Token Server 连接与限流状态。\n */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "        const CLUSTER_MODE_SERVER = 1;",
        "        const CLUSTER_MODE_SERVER = 1;  // 服务端模式",
    ),
]

R["sentinel-dashboard/src/main/webapp/resources/app/scripts/controllers/cluster_app_token_client_list.js"] = [
    (
        "var app = angular.module('sentinelDashboardApp');",
        "/**\n * 集群令牌客户端列表控制器，展示 Token Client 并支持修改连接配置。\n */\nvar app = angular.module('sentinelDashboardApp');",
    ),
    (
        "        const CLUSTER_MODE_CLIENT = 0;\n        const CLUSTER_MODE_SERVER = 1;",
        "        const CLUSTER_MODE_CLIENT = 0;  // 客户端模式\n        const CLUSTER_MODE_SERVER = 1;    // 服务端模式",
    ),
    (
        "        function processClientData(clientVO) {\n\n        }",
        "        /** 预处理客户端 VO（当前无额外转换逻辑）。 */\n        function processClientData(clientVO) {\n\n        }",
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
    index_file = Path("/tmp/git-index-sentinel-w22a")
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
        "sentinel 1.8.10: Chinese-annotate wave 22a [0:15]",
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
        "queue: mark sentinel 1.8.10 wave22a done",
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
