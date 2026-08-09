#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-25a block [0:15] (cluster, command-handler, dubbo demos)."""
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
    for ln in Path("/tmp/sentinel_w25a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
SCRIPT_NAME = "annotate_sentinel_wave25a_batch0_15.py"
MARK_NOTE = "wave25a [0:15]"

GUARD_FILES = [
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-demo/sentinel-demo-cluster/sentinel-demo-cluster-embedded/src/main/java/com/alibaba/csp/sentinel/demo/cluster/app/controller/ClusterDemoController.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * 集群限流嵌入式演示 REST 控制器：暴露 /hello/{name} 触发 {@link DemoService}。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    @GetMapping(\"/hello/{name}\")\n    public String apiHello(@PathVariable String name) throws Exception {",
        "    /** GET /hello/{name}：调用 sayHello 并走集群/本地 Sentinel 规则。 */\n    @GetMapping(\"/hello/{name}\")\n    public String apiHello(@PathVariable String name) throws Exception {",
    ),
]

R["sentinel-demo/sentinel-demo-cluster/sentinel-demo-cluster-embedded/src/main/java/com/alibaba/csp/sentinel/demo/cluster/app/service/DemoService.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * 集群演示业务服务：{@link SentinelResource} 绑定 blockHandler 处理限流。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    @SentinelResource(blockHandler = \"sayHelloBlockHandler\")\n    public String sayHello(String name) {",
        "    /** 问候资源，被限流时调用 sayHelloBlockHandler。 */\n    @SentinelResource(blockHandler = \"sayHelloBlockHandler\")\n    public String sayHello(String name) {",
    ),
    (
        "    public String sayHelloBlockHandler(String name, BlockException ex) {\n        // This is the block handler.",
        "    /** sayHello 的 blockHandler：打印异常并返回友好提示。 */\n    public String sayHelloBlockHandler(String name, BlockException ex) {",
    ),
]

R["sentinel-demo/sentinel-demo-cluster/sentinel-demo-cluster-embedded/src/main/java/com/alibaba/csp/sentinel/demo/cluster/entity/ClusterGroupEntity.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 集群分组实体：描述 Token Server 机器及其关联 Client 集合。\n * <p>machineId 格式为 {@code ip@commandPort}，commandPort 为 Dashboard 通信端口。</p>\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
    (
        "    private String machineId;",
        "    /** Token Server 机器标识（ip@commandPort）。 */\n    private String machineId;",
    ),
    (
        "    private String ip;",
        "    /** Token Server 对外 IP。 */\n    private String ip;",
    ),
    (
        "    private Integer port;",
        "    /** Token Server 监听端口。 */\n    private Integer port;",
    ),
    (
        "    private Set<String> clientSet;",
        "    /** 归属该 Server 的 Client machineId 集合。 */\n    private Set<String> clientSet;",
    ),
]

R["sentinel-demo/sentinel-demo-cluster/sentinel-demo-cluster-embedded/src/main/java/com/alibaba/csp/sentinel/demo/cluster/init/DemoClusterInitFunc.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * 嵌入式集群演示 {@link InitFunc}：从 Nacos 加载流控规则、Client/Server 配置与集群拓扑。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "        // Register client dynamic rule data source.",
        "        // 注册 Client 侧动态流控/热点规则数据源",
    ),
    (
        "        // Register token client related data source.\n        // Token client common config:",
        "        // 注册 Token Client 相关数据源\n        // Client 通用配置：",
    ),
    (
        "        // Token client assign config (e.g. target token server) retrieved from assign map:",
        "        // Client 指派配置（目标 Token Server），从 cluster-map 解析：",
    ),
    (
        "        // Register token server related data source.\n        // Register dynamic rule data source supplier for token server:",
        "        // 注册 Token Server 相关数据源\n        // 为 Token Server 注册按 namespace 拉取规则的 Supplier：",
    ),
    (
        "        // Token server transport config extracted from assign map:",
        "        // Token Server 传输配置，同样从 cluster-map 提取：",
    ),
    (
        "        // Init cluster state property for extracting mode from cluster map data source.",
        "        // 根据 cluster-map 推断本机角色（Server / Client / 未启动）",
    ),
    (
        "        // Register cluster flow rule property supplier which creates data source by namespace.\n        // Flow rule dataId format: ${namespace}-flow-rules",
        "        // 按 namespace 注册集群流控规则 Supplier，dataId 格式：${namespace}-flow-rules",
    ),
    (
        "        // Register cluster parameter flow rule property supplier which creates data source by namespace.",
        "        // 按 namespace 注册集群热点参数规则 Supplier",
    ),
    (
        "        // Cluster map format:\n        // [{\"clientSet\":[\"112.12.88.66@8729\",\"112.12.88.67@8727\"],\"ip\":\"112.12.88.68\",\"machineId\":\"112.12.88.68@8728\",\"port\":11111}]\n        // machineId: <ip@commandPort>, commandPort for port exposed to Sentinel dashboard (transport module)",
        "        // cluster-map JSON 示例：\n        // [{\"clientSet\":[\"112.12.88.66@8729\",\"112.12.88.67@8727\"],\"ip\":\"112.12.88.68\",\"machineId\":\"112.12.88.68@8728\",\"port\":11111}]\n        // machineId 为 ip@commandPort，commandPort 即 transport 模块暴露给 Dashboard 的端口",
    ),
    (
        "        // If any server group machineId matches current, then it's token server.",
        "        // 若某 Server 分组的 machineId 与当前机器一致，则本机为 Token Server",
    ),
    (
        "        // If current machine belongs to any of the token server group, then it's token client.\n        // Otherwise it's unassigned, should be set to NOT_STARTED.",
        "        // 若当前 machineId 出现在某 Server 的 clientSet 中，则为 Token Client\n        // 否则尚未分配角色，状态为 NOT_STARTED",
    ),
    (
        "        // Build client assign config from the client set of target server group.",
        "        // 从目标 Server 分组构建 Client 指派配置（Server IP + 端口）",
    ),
    (
        "        // Note: this may not work well for container-based env.",
        "        // 注意：容器环境下 ip@port 识别可能不准确",
    ),
]

R["sentinel-demo/sentinel-demo-cluster/sentinel-demo-cluster-server-alone/src/main/java/com/alibaba/csp/sentinel/demo/cluster/ClusterServerDemo.java"] = [
    (
        "/**\n * <p>Cluster server demo (alone mode).</p>\n * <p>Here we init the cluster server dynamic data sources in\n * {@link com.alibaba.csp.sentinel.demo.cluster.init.DemoClusterServerInitFunc}.</p>\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * <p>集群 Token Server 独立模式演示入口。</p>\n * <p>动态数据源初始化见 {@link com.alibaba.csp.sentinel.demo.cluster.init.DemoClusterServerInitFunc}。</p>\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "        // Not embedded mode by default (alone mode).",
        "        // 默认非嵌入式，以独立进程运行 Token Server",
    ),
    (
        "        // A sample for manually load config for cluster server.\n        // It's recommended to use dynamic data source to cluster manage config and rules.\n        // See the sample in DemoClusterServerInitFunc for detail.",
        "        // 以下为手动加载 Server 配置的示例；生产环境建议用 Nacos 等动态数据源\n        // 详见 DemoClusterServerInitFunc",
    ),
    (
        "        // Start the server.",
        "        // 启动 Token Server",
    ),
]

R["sentinel-demo/sentinel-demo-cluster/sentinel-demo-cluster-server-alone/src/main/java/com/alibaba/csp/sentinel/demo/cluster/DemoConstants.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * 集群演示常量：应用名与 Nacos 规则 dataId 后缀。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    public static final String APP_NAME = \"appA\";",
        "    /** 演示应用名，亦作为 Server namespace。 */\n    public static final String APP_NAME = \"appA\";",
    ),
    (
        "    public static final String FLOW_POSTFIX = \"-flow-rules\";",
        "    /** 流控规则 dataId 后缀。 */\n    public static final String FLOW_POSTFIX = \"-flow-rules\";",
    ),
    (
        "    public static final String PARAM_FLOW_POSTFIX = \"-param-rules\";",
        "    /** 热点参数规则 dataId 后缀。 */\n    public static final String PARAM_FLOW_POSTFIX = \"-param-rules\";",
    ),
]

R["sentinel-demo/sentinel-demo-cluster/sentinel-demo-cluster-server-alone/src/main/java/com/alibaba/csp/sentinel/demo/cluster/init/DemoClusterServerInitFunc.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * 独立 Token Server 演示 {@link InitFunc}：从 Nacos 加载 namespace、传输配置与集群规则。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "        // Register cluster flow rule property supplier which creates data source by namespace.",
        "        // 按 namespace 注册集群流控规则 Supplier",
    ),
    (
        "        // Register cluster parameter flow rule property supplier.",
        "        // 按 namespace 注册集群热点参数规则 Supplier",
    ),
    (
        "        // Server namespace set (scope) data source.",
        "        // Server 管辖的 namespace 集合数据源",
    ),
    (
        "        // Server transport configuration data source.",
        "        // Server 传输层（端口、空闲超时等）配置数据源",
    ),
]

R["sentinel-demo/sentinel-demo-command-handler/src/main/java/com/alibaba/csp/sentinel/demo/commandhandler/CommandDemo.java"] = [
    (
        "/**\n * <p>To run this demo, we need to add the {@code sentinel-transport-simple-http} dependency.</p>\n * <p>\n * As soon as the CommandCenter has been initialized, we can visit {@code http://ip:commandPort/api}\n * to see all available command APIs (by default the port is 8719).\n * We can also visit our customized {@code /echo} command.\n * </p>\n *\n * @author Eric Zhao\n */",
        "/**\n * <p>运行本演示需引入 {@code sentinel-transport-simple-http} 依赖。</p>\n * <p>\n * CommandCenter 初始化后可访问 {@code http://ip:commandPort/api} 查看内置命令 API（默认端口 8719）。\n * 亦可访问自定义 {@code /echo} 命令。\n * </p>\n *\n * @author Eric Zhao\n */",
    ),
    (
        "        // Only for demo. You don't have to do this in your application.",
        "        // 仅演示用；业务应用通常由 SPI 自动触发 InitFunc",
    ),
]

R["sentinel-demo/sentinel-demo-command-handler/src/main/java/com/alibaba/csp/sentinel/demo/commandhandler/EchoCommandHandler.java"] = [
    (
        "/**\n * This class is a demo shows how to create and register a customized CommandHandler.\n *\n * <ul>\n * <li>1. Create a class which implements the {@link CommandHandler} SPI interface</li>\n * <li>2. Use a {@link CommandMapping} to specify the url and desc of your CommandHandler</li>\n * <li>3. Implement your own {@code handle} method </li>\n * <li>4. Add your CommandHandler in {@code com.alibaba.csp.sentinel.command.CommandHandler} file which is stored in\n * {@code resources/META-INF/services/} directory </li>\n * </ul>\n *\n * @author houyi\n */",
        "/**\n * 自定义 {@link CommandHandler} 注册演示。\n *\n * <ul>\n * <li>1. 实现 {@link CommandHandler} SPI 接口</li>\n * <li>2. 用 {@link CommandMapping} 声明命令名与描述</li>\n * <li>3. 实现 {@code handle} 方法</li>\n * <li>4. 在 {@code resources/META-INF/services/com.alibaba.csp.sentinel.command.CommandHandler} 中注册</li>\n * </ul>\n *\n * @author houyi\n */",
    ),
    (
        "    @Override\n    public CommandResponse<String> handle(CommandRequest request) {",
        "    /** 读取 name 参数并回显；未传参时提示提交 name。 */\n    @Override\n    public CommandResponse<String> handle(CommandRequest request) {",
    ),
]

R["sentinel-demo/sentinel-demo-command-handler/src/main/java/com/alibaba/csp/sentinel/demo/commandhandler/interceptor/AllCommandHandlerInterceptor.java"] = [
    (
        "/**\n * @author icodening\n * @date 2022.03.23\n */",
        "/**\n * 全局命令拦截器：拦截所有 CommandHandler 调用并统计耗时。\n *\n * @author icodening\n * @date 2022.03.23\n */",
    ),
    (
        "    @Override\n    public boolean shouldIntercept(String commandName) {",
        "    /** 拦截全部命令。 */\n    @Override\n    public boolean shouldIntercept(String commandName) {",
    ),
    (
        "    @Override\n    public CommandResponse intercept(CommandRequest request, CommandRequestExecution execution) {",
        "    /** 打印开始/结束日志，捕获异常后原样抛出，finally 输出耗时。 */\n    @Override\n    public CommandResponse intercept(CommandRequest request, CommandRequestExecution execution) {",
    ),
]

R["sentinel-demo/sentinel-demo-command-handler/src/main/java/com/alibaba/csp/sentinel/demo/commandhandler/interceptor/EchoCommandHandlerInterceptor.java"] = [
    (
        "/**\n * @author icodening\n * @date 2022.03.23\n */",
        "/**\n * 仅拦截 {@code echo} 命令：打印参数并包装返回结果。\n *\n * @author icodening\n * @date 2022.03.23\n */",
    ),
    (
        "    @Override\n    public boolean shouldIntercept(String commandName) {",
        "    /** 仅当命令名为 echo 时拦截。 */\n    @Override\n    public boolean shouldIntercept(String commandName) {",
    ),
    (
        "    @Override\n    public CommandResponse<String> intercept(CommandRequest request, CommandRequestExecution<String> execution) {",
        "    /** 记录请求参数，执行原 handler 后将结果包在 intercept result 前缀中返回。 */\n    @Override\n    public CommandResponse<String> intercept(CommandRequest request, CommandRequestExecution<String> execution) {",
    ),
]

R["sentinel-demo/sentinel-demo-dubbo/src/main/java/com/alibaba/csp/sentinel/demo/dubbo/FooService.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * 经典 Dubbo 演示 RPC 服务接口。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    String sayHello(String name);",
        "    /** 问候调用，Consumer 流控主要测试资源。 */\n    String sayHello(String name);",
    ),
    (
        "    String doAnother();",
        "    /** 备用 RPC 方法。 */\n    String doAnother();",
    ),
]

R["sentinel-demo/sentinel-demo-dubbo/src/main/java/com/alibaba/csp/sentinel/demo/dubbo/consumer/ConsumerConfiguration.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * 经典 Dubbo Consumer Spring 配置：应用、组播注册中心与 Sentinel Filter。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "        // Uncomment below line if you don't want to enable Sentinel for Dubbo service consumers.",
        "        // 若不想为 Dubbo Consumer 启用 Sentinel，可取消下行注释",
    ),
    (
        "    @Bean\n    public ApplicationConfig applicationConfig() {",
        "    /** 配置 Consumer 应用名为 demo-consumer。 */\n    @Bean\n    public ApplicationConfig applicationConfig() {",
    ),
    (
        "    @Bean\n    public RegistryConfig registryConfig() {",
        "    /** 使用组播注册中心 224.5.6.7:1234。 */\n    @Bean\n    public RegistryConfig registryConfig() {",
    ),
    (
        "    @Bean\n    public ConsumerConfig consumerConfig() {",
        "    /** Consumer 全局配置，默认启用 sentinel.dubbo.consumer.filter。 */\n    @Bean\n    public ConsumerConfig consumerConfig() {",
    ),
    (
        "    @Bean\n    public FooServiceConsumer annotationDemoServiceConsumer() {",
        "    /** 注册演示用 Consumer 包装 Bean。 */\n    @Bean\n    public FooServiceConsumer annotationDemoServiceConsumer() {",
    ),
]

R["sentinel-demo/sentinel-demo-dubbo/src/main/java/com/alibaba/csp/sentinel/demo/dubbo/consumer/FooServiceConsumer.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * {@link FooService} 消费者封装：通过 {@link Reference} 直连 Provider。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    @Reference(url = \"dubbo://127.0.0.1:25758\", timeout = 3000)\n    private FooService fooService;",
        "    /** 直连本地 Provider，超时 3000ms。 */\n    @Reference(url = \"dubbo://127.0.0.1:25758\", timeout = 3000)\n    private FooService fooService;",
    ),
    (
        "    public String sayHello(String name) {",
        "    /** 转发 sayHello RPC 调用。 */\n    public String sayHello(String name) {",
    ),
    (
        "    public String doAnother() {",
        "    /** 转发 doAnother RPC 调用。 */\n    public String doAnother() {",
    ),
]

R["sentinel-demo/sentinel-demo-dubbo/src/main/java/com/alibaba/csp/sentinel/demo/dubbo/demo1/FooConsumerBootstrap.java"] = [
    (
        "/**\n * Please add the following VM arguments:\n * <pre>\n * -Djava.net.preferIPv4Stack=true\n * -Dcsp.sentinel.api.port=8721\n * -Dproject.name=dubbo-consumer-demo\n * </pre>\n *\n * @author Eric Zhao\n */",
        "/**\n * 经典 Dubbo Consumer 启动类：循环调用 sayHello 演示流控阻塞。\n * <p>启动前请添加 VM 参数：</p>\n * <pre>\n * -Djava.net.preferIPv4Stack=true\n * -Dcsp.sentinel.api.port=8721\n * -Dproject.name=dubbo-consumer-demo\n * </pre>\n *\n * @author Eric Zhao\n */",
    ),
    (
        "            } catch (SentinelRpcException ex) {\n                System.out.println(\"Blocked\");",
        "            } catch (SentinelRpcException ex) {\n                // 被 Sentinel 限流/熔断时抛出 SentinelRpcException\n                System.out.println(\"Blocked\");",
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
    index_file = Path("/tmp/git-index-sentinel-w25a")
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
        "sentinel 1.8.10: Chinese-annotate wave 25a [0:15]",
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
