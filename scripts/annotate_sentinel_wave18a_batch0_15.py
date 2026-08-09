#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-18a block [0:15] (cluster-server handler/processor/envoy-rls)."""
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
SCRIPT_NAME = "annotate_sentinel_wave18a_batch0_15.py"
BATCH_LIST = Path("/tmp/sentinel_w18a.txt").read_text(encoding="utf-8").strip().split("\n")
MARK_NOTE = "wave18a"

GUARD_FILES = [
    VER
    / "analyzed/sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/connection/NettyConnection.java",
    VER
    / "analyzed/sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/codec/DefaultResponseEntityWriter.java",
    VER
    / "analyzed/sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/ClusterFlowChecker.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/connection/ScanIdleConnectionTask.java"] = [
    (
        "/**\n * @author xuyue\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 周期性扫描并关闭长空闲连接的 Runnable 任务。\n * <p>由 {@link ConnectionPool} 调度，依据 {@link ClusterServerConfigManager#getIdleSeconds()}\n * 判断连接是否超时未读，超时则记录日志并调用 {@link Connection#close()}。\n *\n * @author xuyue\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "            if (idleTimeMillis < 0) {\n                idleTimeMillis = ServerTransportConfig.DEFAULT_IDLE_SECONDS * 1000;\n            }",
        "            if (idleTimeMillis < 0) {\n                // 配置无效时回退到默认空闲超时。\n                idleTimeMillis = ServerTransportConfig.DEFAULT_IDLE_SECONDS * 1000;\n            }",
    ),
    (
        "                if ((now - conn.getLastReadTime()) > idleTimeMillis) {",
        "                // 超过空闲阈值则关闭连接。\n                if ((now - conn.getLastReadTime()) > idleTimeMillis) {",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/handler/TokenServerHandler.java"] = [
    (
        "/**\n * Netty server handler for Sentinel token server.\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * Sentinel 令牌服务端的 Netty 入站处理器。\n * <p>管理连接生命周期，解析 {@link ClusterRequest} 并委托 {@link RequestProcessor} 处理，\n * 同时处理客户端 PING 以注册命名空间连接。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "            // Client ping with its namespace, add to connection manager.",
        "            // 客户端 PING 携带命名空间，注册到连接管理器。",
    ),
    (
        "            // Pick request processor for request type.",
        "            // 按请求类型选择对应的 RequestProcessor。",
    ),
    (
        "        // Add the remote namespace to connection manager.",
        "        // 将远端命名空间注册到连接管理器。",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/init/DefaultClusterServerInitFunc.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群令牌服务端默认初始化函数，注册编解码器与请求处理器。\n * <p>实现 {@link InitFunc}，在 Sentinel 启动时预加载 SPI 组件。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "        // Eagerly-trigger the SPI pre-load of token service.",
        "        // 主动预加载 TokenService SPI 实现。",
    ),
    (
        "        // Eagerly-trigger the SPI pre-load.",
        "        // 主动预加载 RequestProcessor SPI 实现。",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/log/ClusterServerStatLogUtil.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群令牌服务端统计日志工具，基于 EagleEye {@link StatLogger} 写入 sentinel-server.log。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    public static void log(String msg) {",
        "    /** 记录一条计数为 1 的统计日志。\n     *\n     * @param msg 日志消息键\n     */\n    public static void log(String msg) {",
    ),
    (
        "    public static void log(String msg, int count) {",
        "    /** 记录指定计数的统计日志。\n     *\n     * @param msg 日志消息键\n     * @param count 计数值\n     */\n    public static void log(String msg, int count) {",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/processor/FlowRequestProcessor.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群流控令牌请求处理器，处理 {@link ClusterConstants#MSG_TYPE_FLOW} 类型请求。\n * <p>从请求体提取 flowId、count 与优先级标志，委托 {@link TokenService#requestToken} 获取令牌结果。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/processor/ParamFlowRequestProcessor.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群热点参数流控令牌请求处理器，处理 {@link ClusterConstants#MSG_TYPE_PARAM_FLOW} 类型请求。\n * <p>从请求体提取 flowId、count 与参数集合，委托 {@link TokenService#requestParamToken} 判定放行。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/processor/RequestProcessor.java"] = [
    (
        "/**\n * Interface of cluster request processor.\n *\n * @param <T> type of request body\n * @param <R> type of response body\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群请求处理器接口。\n *\n * @param <T> 请求体类型\n * @param <R> 响应体类型\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    /**\n     * Process the cluster request.\n     *\n     * @param request Sentinel cluster request\n     * @return the response after processed\n     */",
        "    /**\n     * 处理集群请求并返回响应。\n     *\n     * @param request Sentinel cluster request\n     * @return the response after processed\n     */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/processor/RequestProcessorProvider.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群请求处理器提供者，通过 SPI 加载并缓存 {@link RequestProcessor} 实例。\n * <p>处理器类型由 {@link RequestType} 注解的 value 决定。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    public static RequestProcessor getProcessor(int type) {",
        "    /** 按消息类型获取已注册的请求处理器。\n     *\n     * @param type 集群消息类型\n     * @return 对应的处理器，未注册时返回 null\n     */\n    public static RequestProcessor getProcessor(int type) {",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/util/ClusterRuleUtil.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群规则工具类，提供规则 ID 等通用校验方法。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    public static boolean validId(Long id) {",
        "    /** 校验规则 ID 是否有效（非 null 且大于 0）。\n     *\n     * @param id 规则 ID\n     * @return 有效返回 true\n     */\n    public static boolean validId(Long id) {",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-envoy-rls/src/main/java/com/alibaba/csp/sentinel/cluster/server/envoy/rls/SentinelEnvoyRlsConstants.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Envoy RLS（Rate Limit Service）集成相关常量。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    public static final int DEFAULT_GRPC_PORT = 10245;",
        "    /** 默认 gRPC 监听端口。 */\n    public static final int DEFAULT_GRPC_PORT = 10245;",
    ),
    (
        "    public static final String SERVER_APP_NAME = \"sentinel-rls-token-server\";",
        "    /** 令牌服务端应用名。 */\n    public static final String SERVER_APP_NAME = \"sentinel-rls-token-server\";",
    ),
    (
        "    public static final String GRPC_PORT_ENV_KEY = \"SENTINEL_RLS_GRPC_PORT\";",
        "    /** gRPC 端口环境变量键名。 */\n    public static final String GRPC_PORT_ENV_KEY = \"SENTINEL_RLS_GRPC_PORT\";",
    ),
    (
        "    public static final String GRPC_PORT_PROPERTY_KEY = \"csp.sentinel.grpc.server.port\";",
        "    /** gRPC 端口配置属性键名。 */\n    public static final String GRPC_PORT_PROPERTY_KEY = \"csp.sentinel.grpc.server.port\";",
    ),
    (
        "    public static final String RULE_FILE_PATH_ENV_KEY = \"SENTINEL_RLS_RULE_FILE_PATH\";",
        "    /** 规则文件路径环境变量键名。 */\n    public static final String RULE_FILE_PATH_ENV_KEY = \"SENTINEL_RLS_RULE_FILE_PATH\";",
    ),
    (
        "    public static final String RULE_FILE_PATH_PROPERTY_KEY = \"csp.sentinel.rls.rule.file\";",
        "    /** 规则文件路径配置属性键名。 */\n    public static final String RULE_FILE_PATH_PROPERTY_KEY = \"csp.sentinel.rls.rule.file\";",
    ),
    (
        "    public static final String ENABLE_ACCESS_LOG_ENV_KEY = \"SENTINEL_RLS_ACCESS_LOG\";",
        "    /** 是否启用访问日志的环境变量键名。 */\n    public static final String ENABLE_ACCESS_LOG_ENV_KEY = \"SENTINEL_RLS_ACCESS_LOG\";",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-envoy-rls/src/main/java/com/alibaba/csp/sentinel/cluster/server/envoy/rls/SentinelEnvoyRlsServer.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Envoy RLS 令牌服务端入口，启动 gRPC 服务并加载规则数据源。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "        // Order: system env > property",
        "        // 解析顺序：系统环境变量优先于配置文件属性。",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-envoy-rls/src/main/java/com/alibaba/csp/sentinel/cluster/server/envoy/rls/SentinelEnvoyRlsServiceImpl.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.7.0\n */",
        "/**\n * Envoy Rate Limit Service gRPC 实现（v2 API）。\n * <p>将 Envoy {@link RateLimitRequest} 描述符映射为 Sentinel 流控规则并执行令牌检查，\n * 返回各描述符及整体限流状态。\n *\n * @author Eric Zhao\n * @since 1.7.0\n */",
    ),
    (
        "            // Not present, use the default \"1\" by default.",
        "            // 未指定 hitsAddend 时默认按 1 计数。",
    ),
    (
        "                // If the rule of the descriptor is absent, the request will pass directly.",
        "                // 描述符对应规则不存在时直接放行。",
    ),
    (
        "            // Pass if the target rule is absent.",
        "            // 目标规则不存在时放行。",
    ),
    (
        "        // If the rule is present, it should be valid.",
        "        // 规则存在时应已通过有效性校验。",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-envoy-rls/src/main/java/com/alibaba/csp/sentinel/cluster/server/envoy/rls/SentinelRlsGrpcServer.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Sentinel Envoy RLS gRPC 服务端封装，同时注册 v2 与 v3 RateLimitService 实现。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "        // The gRPC server has already checked the start status, so we don't check here.",
        "        // gRPC Server 内部已处理启动状态，此处不再重复检查。",
    ),
    (
        "    public void start() throws IOException {",
        "    /** 启动 gRPC 服务端并输出监听端口日志。 */\n    public void start() throws IOException {",
    ),
    (
        "    public void shutdown() {",
        "    /** 立即关闭 gRPC 服务端。 */\n    public void shutdown() {",
    ),
    (
        "    public void blockUntilShutdown() throws InterruptedException {",
        "    /** 阻塞等待 gRPC 服务端终止。 */\n    public void blockUntilShutdown() throws InterruptedException {",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-envoy-rls/src/main/java/com/alibaba/csp/sentinel/cluster/server/envoy/rls/datasource/EnvoyRlsRuleDataSourceService.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.7.0\n */",
        "/**\n * Envoy RLS 规则文件数据源服务，监听 YAML 规则文件并动态加载到 {@link EnvoyRlsRuleManager}。\n *\n * @author Eric Zhao\n * @since 1.7.0\n */",
    ),
    (
        "    public synchronized void init() throws Exception {",
        "    /** 初始化文件数据源；路径为空时抛出 {@link IllegalStateException}。 */\n    public synchronized void init() throws Exception {",
    ),
    (
        "    public synchronized void onShutdown() {",
        "    /** 关闭数据源并释放文件监听资源。 */\n    public synchronized void onShutdown() {",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-envoy-rls/src/main/java/com/alibaba/csp/sentinel/cluster/server/envoy/rls/flow/SimpleClusterFlowChecker.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.7.0\n */",
        "/**\n * Envoy RLS 场景的简化集群流控检查器，基于平均 QPS 与阈值比较判定放行。\n * <p>不含预占用逻辑，适用于 Envoy 侧轻量级限流集成。\n *\n * @author Eric Zhao\n * @since 1.7.0\n */",
    ),
    (
        "            // Remaining count is cut down to a smaller integer.",
        "            // 剩余配额截断为整数返回。",
    ),
    (
        "            // Blocked.",
        "            // 请求被阻断。",
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
    index_file = Path("/tmp/git-index-sentinel-w18a")
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


def update_batch_json() -> None:
    batch_path = QUEUE / "batch.json"
    batch = json.loads(batch_path.read_text(encoding="utf-8"))
    done_set = {
        ln.strip() for ln in (QUEUE / "done.txt").read_text(encoding="utf-8").splitlines() if ln.strip()
    }
    pending = [
        ln for ln in (QUEUE / "pending.txt").read_text(encoding="utf-8").splitlines() if ln.strip()
    ]
    batch["files"] = pending[:15] if pending else []
    batch["done"] = len(done_set)
    batch["remaining_pending"] = len(pending)
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
        return 1

    analyzed_paths = [f"sentinel/1.8.10/analyzed/{rel}" for rel in BATCH_LIST]
    script_path = f"scripts/{SCRIPT_NAME}"
    sha, tree_count = isolated_index_commit(
        "sentinel 1.8.10: Chinese-annotate wave 18a [0:15]",
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
        "queue: mark sentinel 1.8.10 wave18a done",
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
