#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-31a block [0:15] (transport command handlers/vo/interfaces)."""
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
    for ln in Path("/tmp/sentinel_w31a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
SCRIPT_NAME = "annotate_sentinel_wave31a_batch0_15.py"
MARK_NOTE = "wave31a [0:15]"

GUARD_FILES = [
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-transport/sentinel-transport-common/src/main/java/com/alibaba/csp/sentinel/command/handler/FetchSystemStatusCommandHandler.java"] = [
    (
        "/**\n * @author jialiang.linjl\n */",
        "/**\n * 查询入口节点系统级实时指标（成功 QPS、通过 QPS、阻断 QPS、平均 RT、并发线程数），返回 JSON。\n *\n * @author jialiang.linjl\n */",
    ),
    (
        '@CommandMapping(name = "systemStatus", desc = "get system status")',
        '@CommandMapping(name = "systemStatus", desc = "获取系统状态指标")',
    ),
]

R["sentinel-transport/sentinel-transport-common/src/main/java/com/alibaba/csp/sentinel/command/handler/FetchTreeCommandHandler.java"] = [
    (
        "/**\n * @author qinan.qn\n */",
        "/**\n * 以树形文本输出调用链各节点实时指标；参数 {@code id} 可指定根资源名（精确或模糊匹配）。\n *\n * @author qinan.qn\n */",
    ),
    (
        '@CommandMapping(name = "tree", desc = "get metrics in tree mode, use id to specify detailed tree root")',
        '@CommandMapping(name = "tree", desc = "树形输出节点指标，id 指定子树根资源")',
    ),
    (
        "    private void visitTree(int level, DefaultNode node, /*@NonNull*/ StringBuilder sb) {",
        "    /** 递归遍历调用树并以缩进文本格式化各节点 QPS/RT 等指标。 */\n    private void visitTree(int level, DefaultNode node, /*@NonNull*/ StringBuilder sb) {",
    ),
]

R["sentinel-transport/sentinel-transport-common/src/main/java/com/alibaba/csp/sentinel/command/handler/InterceptingCommandHandler.java"] = [
    (
        "/**\n * intercept specified command handler\n *\n * @author icodening\n * @date 2022.03.03\n */",
        "/**\n * 命令处理器装饰器：在委托 {@link CommandHandler} 执行前后串联\n * {@link CommandHandlerInterceptor} 拦截链。\n *\n * @author icodening\n * @date 2022.03.03\n */",
    ),
    (
        "    public InterceptingCommandHandler(CommandHandler<R> delegate, List<CommandHandlerInterceptor<R>> commandHandlerInterceptors) {",
        "    /**\n     * @param delegate 实际命令处理器\n     * @param commandHandlerInterceptors 按顺序执行的拦截器列表\n     */\n    public InterceptingCommandHandler(CommandHandler<R> delegate, List<CommandHandlerInterceptor<R>> commandHandlerInterceptors) {",
    ),
    (
        "    private static class InterceptingRequestExecution<R> implements CommandRequestExecution<R> {",
        "    /** 递归驱动拦截器链，末尾调用真实 {@code handle}。 */\n    private static class InterceptingRequestExecution<R> implements CommandRequestExecution<R> {",
    ),
]

R["sentinel-transport/sentinel-transport-common/src/main/java/com/alibaba/csp/sentinel/command/handler/ModifyRulesCommandHandler.java"] = [
    (
        "/**\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
        "/**\n * 动态修改流控/降级/授权/系统规则：参数 {@code type} 指定规则类型，\n * {@code data} 为 URL 编码的 JSON 规则数组；成功时同步写入已注册 {@link WritableDataSource}。\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
    ),
    (
        '@CommandMapping(name = "setRules", desc = "modify the rules, accept param: type={ruleType}&data={ruleJson}")',
        '@CommandMapping(name = "setRules", desc = "修改规则，参数 type={ruleType}&data={ruleJson}")',
    ),
    (
        "        // XXX from 1.7.2, force to fail when fastjson is older than 1.2.12\n        // We may need a better solution on this.",
        "        // 自 1.7.2 起：fastjson 低于 1.2.12 时拒绝写入规则",
    ),
    (
        "            // fastjson too old",
        "            // fastjson 版本过低",
    ),
    (
        "        // rule data in get parameter",
        "        // 规则 JSON 来自 GET 参数 data",
    ),
    (
        "    /**\n     * Write target value to given data source.\n     *\n     * @param dataSource writable data source\n     * @param value target value to save\n     * @param <T> value type\n     * @return true if write successful or data source is empty; false if error occurs\n     */",
        "    /**\n     * 将规则写入已注册的 {@link WritableDataSource}。\n     *\n     * @param dataSource writable data source\n     * @param value target value to save\n     * @param <T> value type\n     * @return 写入成功或未注册数据源时 true；异常时 false\n     */",
    ),
    (
        "    private static final String WRITE_DS_FAILURE_MSG = \"partial success (write data source failed)\";",
        "    /** 规则已加载但持久化数据源写入失败时的响应文本。 */\n    private static final String WRITE_DS_FAILURE_MSG = \"partial success (write data source failed)\";",
    ),
]

R["sentinel-transport/sentinel-transport-common/src/main/java/com/alibaba/csp/sentinel/command/handler/OnOffGetCommandHandler.java"] = [
    (
        "/**\n * @author youji.zj\n */",
        "/**\n * 查询 Sentinel 全局开关 {@link Constants#ON} 当前值。\n *\n * @author youji.zj\n */",
    ),
    (
        '@CommandMapping(name = "getSwitch", desc = "get sentinel switch status")',
        '@CommandMapping(name = "getSwitch", desc = "获取 Sentinel 全局开关状态")',
    ),
]

R["sentinel-transport/sentinel-transport-common/src/main/java/com/alibaba/csp/sentinel/command/handler/OnOffSetCommandHandler.java"] = [
    (
        "/**\n * @author youji.zj\n */",
        "/**\n * 设置 Sentinel 全局开关 {@link Constants#ON}；参数 {@code value} 为 {@code true|false}。\n *\n * @author youji.zj\n */",
    ),
    (
        '@CommandMapping(name = "setSwitch", desc = "set sentinel switch, accept param: value={true|false}")',
        '@CommandMapping(name = "setSwitch", desc = "设置 Sentinel 开关，参数 value={true|false}")',
    ),
]

R["sentinel-transport/sentinel-transport-common/src/main/java/com/alibaba/csp/sentinel/command/handler/SendMetricCommandHandler.java"] = [
    (
        "/**\n * Retrieve and aggregate {@link MetricNode} metrics.\n *\n * @author leyou\n * @author Eric Zhao\n */",
        "/**\n * 从本地指标日志检索并聚合 {@link MetricNode}；支持时间窗口、行数上限与资源名过滤。\n * 未指定 {@code identity} 时会附加当前 CPU 使用率与系统负载快照。\n *\n * @author leyou\n * @author Eric Zhao\n */",
    ),
    (
        '@CommandMapping(name = "metric", desc = "get and aggregate metrics, accept param: "\n    + "startTime={startTime}&endTime={endTime}&maxLines={maxLines}&identify={resourceName}")',
        '@CommandMapping(name = "metric", desc = "检索聚合指标，参数 startTime/endTime/maxLines/identify")',
    ),
    (
        "        // Note: not thread-safe.",
        "        // 懒加载 MetricSearcher，非严格线程安全",
    ),
    (
        "            // Find by end time if set.",
        "            // 指定 endTime 时按闭区间检索",
    ),
    (
        "    /**\n     * add current cpu usage and load to the metric list.\n     *\n     * @param list metric list, should not be null\n     */",
        "    /**\n     * 将当前 CPU 使用率与系统平均负载追加到指标列表。\n     *\n     * @param list metric list, should not be null\n     */",
    ),
    (
        "    /**\n     * transfer the value to a MetricNode, the value will multiply 10000 then truncate\n     * to long value, and as the {@link MetricNode#passQps}.\n     * <p>\n     * This is an eclectic scheme before we have a standard metric format.\n     * </p>\n     *\n     * @param value    value to save.\n     * @param ts       timestamp\n     * @param resource resource name.\n     * @return a MetricNode represents the value.\n     */",
        "    /**\n     * 将浮点值乘以 10000 后写入 {@link MetricNode#passQps}，用于承载 CPU/负载等非 QPS 指标。\n     * <p>\n     * 在统一指标格式落地前的临时编码方案。\n     * </p>\n     *\n     * @param value    value to save.\n     * @param ts       timestamp\n     * @param resource resource name.\n     * @return a MetricNode represents the value.\n     */",
    ),
    (
        "    private volatile MetricSearcher searcher;",
        "    /** 本地指标文件搜索器，懒加载。 */\n    private volatile MetricSearcher searcher;",
    ),
    (
        "    private final Object lock = new Object();",
        "    /** 延迟初始化 {@link MetricSearcher} 的锁。 */\n    private final Object lock = new Object();",
    ),
]

R["sentinel-transport/sentinel-transport-common/src/main/java/com/alibaba/csp/sentinel/command/handler/VersionCommandHandler.java"] = [
    (
        "/**\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
        "/**\n * 返回当前 Sentinel 版本号 {@link Constants#SENTINEL_VERSION}。\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
    ),
    (
        '@CommandMapping(name = "version", desc = "get sentinel version")',
        '@CommandMapping(name = "version", desc = "获取 Sentinel 版本号")',
    ),
]

R["sentinel-transport/sentinel-transport-common/src/main/java/com/alibaba/csp/sentinel/command/handler/cluster/FetchClusterModeCommandHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 查询集群模式状态：当前 mode、最近变更时间，以及客户端/服务端 SPI 是否可用。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        '@CommandMapping(name = "getClusterMode", desc = "get cluster mode status")',
        '@CommandMapping(name = "getClusterMode", desc = "获取集群模式状态")',
    ),
    (
        "    private boolean isClusterClientSpiAvailable() {",
        "    /** 集群 Token 客户端 SPI 是否已加载。 */\n    private boolean isClusterClientSpiAvailable() {",
    ),
    (
        "    private boolean isClusterServerSpiAvailable() {",
        "    /** 嵌入式集群 Token 服务端 SPI 是否已加载。 */\n    private boolean isClusterServerSpiAvailable() {",
    ),
]

R["sentinel-transport/sentinel-transport-common/src/main/java/com/alibaba/csp/sentinel/command/handler/cluster/ModifyClusterModeCommandHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 切换集群运行模式：参数 {@code mode} 为 0（客户端）或 1（服务端）；\n * 切换前校验对应 SPI 是否可用。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        '@CommandMapping(name = "setClusterMode", desc = "set cluster mode, accept param: mode={0|1} 0:client mode 1:server mode")',
        '@CommandMapping(name = "setClusterMode", desc = "设置集群模式，参数 mode={0|1}，0 客户端 1 服务端")',
    ),
]

R["sentinel-transport/sentinel-transport-common/src/main/java/com/alibaba/csp/sentinel/command/vo/NodeVo.java"] = [
    (
        "/**\n * This class is view object of {@link DefaultNode} or {@link ClusterNode}.\n *\n * @author leyou\n */",
        "/**\n * {@link DefaultNode} 或 {@link ClusterNode} 的指标视图对象，供 Dashboard 展示 QPS、RT、线程数等。\n * 树形结构通过 {@link #parentId} 关联父子节点。\n *\n * @author leyou\n */",
    ),
    (
        "    /**\n     * {@link DefaultNode} holds statistics of every node in the invoke tree.\n     * We use parentId to hold the tree structure.\n     *\n     * @param node     the DefaultNode to be presented.\n     * @param parentId random generated parent node id, may be a random UUID\n     * @return node view object.\n     */",
        "    /**\n     * 从调用树 {@link DefaultNode} 构建 {@link NodeVo}，并分配随机 {@link #id}。\n     * {@link DefaultNode} 保存调用链各节点统计；{@code parentId} 用于还原树结构。\n     *\n     * @param node     the DefaultNode to be presented.\n     * @param parentId random generated parent node id, may be a random UUID\n     * @return node view object.\n     */",
    ),
    (
        "    /**\n     * {@link ClusterNode} holds total statistics of the same resource name.\n     *\n     * @param name resource name.\n     * @param node the ClusterNode to be presented.\n     * @return node view object.\n     */\n    public static NodeVo fromClusterNode(ResourceWrapper name, ClusterNode node) {",
        "    /**\n     * 从 {@link ResourceWrapper} 与 {@link ClusterNode} 构建视图对象。\n     * {@link ClusterNode} 聚合同名资源的总体统计。\n     *\n     * @param name resource name.\n     * @param node the ClusterNode to be presented.\n     * @return node view object.\n     */\n    public static NodeVo fromClusterNode(ResourceWrapper name, ClusterNode node) {",
    ),
    (
        "    /**\n     * {@link ClusterNode} holds total statistics of the same resource name.\n     *\n     * @param name resource name.\n     * @param node the ClusterNode to be presented.\n     * @return node view object.\n     */\n    public static NodeVo fromClusterNode(String name, ClusterNode node) {",
        "    /**\n     * 从资源名与 {@link ClusterNode} 构建视图对象。\n     * {@link ClusterNode} 聚合同名资源的总体统计。\n     *\n     * @param name resource name.\n     * @param node the ClusterNode to be presented.\n     * @return node view object.\n     */\n    public static NodeVo fromClusterNode(String name, ClusterNode node) {",
    ),
]

R["sentinel-transport/sentinel-transport-common/src/main/java/com/alibaba/csp/sentinel/heartbeat/HeartbeatSenderProvider.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * {@link HeartbeatSender} 单例提供者：类加载时通过 {@link SpiLoader}\n * 解析优先级最高的心跳发送实现。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "    private static void resolveInstance() {",
        "    /** 从 SPI 加载并缓存 {@link HeartbeatSender} 实例。 */\n    private static void resolveInstance() {",
    ),
    (
        "     * Get resolved {@link HeartbeatSender} instance.\n     *\n     * @return resolved {@code HeartbeatSender} instance",
        "     * 获取已解析的 {@link HeartbeatSender} 实例。\n     *\n     * @return resolved {@code HeartbeatSender} instance",
    ),
]

R["sentinel-transport/sentinel-transport-common/src/main/java/com/alibaba/csp/sentinel/transport/CommandCenter.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * 传输层命令中心 SPI：负责注册命令、启动 HTTP/Netty 等服务端并优雅停止。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Prepare and init for the command center (e.g. register commands).\n     * This will be executed before starting.\n     *\n     * @throws Exception if error occurs\n     */",
        "    /**\n     * 启动前初始化（如注册 {@link com.alibaba.csp.sentinel.command.CommandHandler}）。\n     *\n     * @throws Exception if error occurs\n     */",
    ),
    (
        "    /**\n     * Start the command center in the background.\n     * This method should NOT block.\n     *\n     * @throws Exception if error occurs\n     */",
        "    /**\n     * 在后台启动命令中心，本方法不得阻塞调用线程。\n     *\n     * @throws Exception if error occurs\n     */",
    ),
    (
        "    /**\n     * Stop the command center and do cleanup.\n     *\n     * @throws Exception if error occurs\n     */",
        "    /**\n     * 停止命令中心并释放资源。\n     *\n     * @throws Exception if error occurs\n     */",
    ),
]

R["sentinel-transport/sentinel-transport-common/src/main/java/com/alibaba/csp/sentinel/transport/HeartbeatSender.java"] = [
    (
        "/**\n * The heartbeat sender which is responsible for sending heartbeat to remote dashboard\n * periodically per {@code interval}.\n *\n * @author leyou\n * @author Eric Zhao\n */",
        "/**\n * 心跳发送 SPI：按 {@link #intervalMs()} 周期向 Sentinel Dashboard 上报机器存活信息。\n *\n * @author leyou\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Send heartbeat to Sentinel Dashboard. Each invocation of this method will send\n     * heartbeat once. Sentinel core is responsible for invoking this method\n     * at every {@link #intervalMs()} interval.\n     *\n     * @return whether heartbeat is successfully send.\n     * @throws Exception if error occurs\n     */",
        "    /**\n     * 向 Dashboard 发送一次心跳；核心模块按 {@link #intervalMs()} 间隔调用。\n     *\n     * @return whether heartbeat is successfully send.\n     * @throws Exception if error occurs\n     */",
    ),
    (
        "    /**\n     * Default interval in milliseconds of the sender. It would take effect only when\n     * the heartbeat interval is not configured in Sentinel config property.\n     *\n     * @return default interval of the sender in milliseconds\n     */",
        "    /**\n     * 默认心跳间隔（毫秒）；仅当配置项未指定 {@code csp.sentinel.heartbeat.interval.ms} 时生效。\n     *\n     * @return default interval of the sender in milliseconds\n     */",
    ),
]

R["sentinel-transport/sentinel-transport-common/src/main/java/com/alibaba/csp/sentinel/transport/client/CommandClient.java"] = [
    (
        "/**\n * Basic interface for clients that sending commands.\n *\n * @author Eric Zhao\n */",
        "/**\n * 命令客户端 SPI：向远程 Sentinel 命令端口发送 {@link CommandRequest} 并接收 {@link CommandResponse}。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Send a command to target destination.\n     *\n     * @param host    target host\n     * @param port    target port\n     * @param request command request\n     * @return the response from target command server\n     * @throws Exception when unexpected error occurs\n     */",
        "    /**\n     * 向目标主机命令端口发送一次命令请求。\n     *\n     * @param host    target host\n     * @param port    target port\n     * @param request command request\n     * @return the response from target command server\n     * @throws Exception when unexpected error occurs\n     */",
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
    index_file = Path("/tmp/git-index-sentinel-w31a")
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
        "sentinel 1.8.10: Chinese-annotate wave 31a [0:15]",
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
