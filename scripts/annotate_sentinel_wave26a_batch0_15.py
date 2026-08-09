#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-26a block [0:15] (jax-rs, log-logback, motan, nacos, okhttp demos)."""
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
    for ln in Path("/tmp/sentinel_w26a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
SCRIPT_NAME = "annotate_sentinel_wave26a_batch0_15.py"
MARK_NOTE = "wave26a [0:15]"

GUARD_FILES = [
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-demo/sentinel-demo-jax-rs/src/main/java/com/alibaba/csp/sentinel/demo/jaxrs/HelloResource.java"] = [
    (
        "/**\n * HelloResource\n * @author sea\n */",
        "/**\n * JAX-RS Hello REST 资源：提供 /hello 系列 JSON 接口，供 Provider 侧 Sentinel 限流演示。\n *\n * @author sea\n */",
    ),
    (
        "    @GET\n    public HelloEntity sayHello() {",
        "    /** GET /hello：返回固定问候 {@link HelloEntity}。 */\n    @GET\n    public HelloEntity sayHello() {",
    ),
    (
        "    @GET\n    @Path(\"/{id}\")\n    public HelloEntity get(@PathParam(value = \"id\") Long id) {",
        "    /** GET /hello/{id}：按 id 返回 {@link HelloEntity}。 */\n    @GET\n    @Path(\"/{id}\")\n    public HelloEntity get(@PathParam(value = \"id\") Long id) {",
    ),
    (
        "    @GET\n    @Path(\"/list\")\n    public List<HelloEntity> getAll() {",
        "    /** GET /hello/list：返回 1..1000 共 1000 条实体，用于压测。 */\n    @GET\n    @Path(\"/list\")\n    public List<HelloEntity> getAll() {",
    ),
    (
        "    @Path(\"/ex\")\n    @GET\n    @Produces({ MediaType.APPLICATION_JSON })\n    public String exception() {",
        "    /** GET /hello/ex：故意抛异常，验证 {@link CustomExceptionMapper}。 */\n    @Path(\"/ex\")\n    @GET\n    @Produces({ MediaType.APPLICATION_JSON })\n    public String exception() {",
    ),
]

R["sentinel-demo/sentinel-demo-jax-rs/src/main/java/com/alibaba/csp/sentinel/demo/jaxrs/JaxRsClientDemo.java"] = [
    (
        "/**\n * @author sea\n */",
        "/**\n * JAX-RS Client 演示：通过 {@link SentinelJaxRsClientTemplate} 对 GET /hello/1 发起受 Sentinel 保护的出站请求。\n *\n * @author sea\n */",
    ),
    (
        "        final String host = \"http://127.0.0.1:8181\";",
        "        // Provider 基址，需先启动 JaxRsDemoApplication\n        final String host = \"http://127.0.0.1:8181\";",
    ),
    (
        "        String resourceName = \"GET:\" + url;",
        "        // 资源名格式与 Provider Filter 一致：METHOD:path\n        String resourceName = \"GET:\" + url;",
    ),
]

R["sentinel-demo/sentinel-demo-jax-rs/src/main/java/com/alibaba/csp/sentinel/demo/jaxrs/JaxRsDemoApplication.java"] = [
    (
        "/**\n * <p>Add the JVM parameter to connect to the dashboard:</p>\n * {@code -Dcsp.sentinel.dashboard.server=127.0.0.1:8080 -Dproject.name=sentinel-demo-jax-rs}\n *\n * @author sea\n */",
        "/**\n * JAX-RS Provider 演示 Spring Boot 入口，默认端口 8181。\n * <p>连接 Dashboard 请添加 JVM 参数：</p>\n * {@code -Dcsp.sentinel.dashboard.server=127.0.0.1:8080 -Dproject.name=sentinel-demo-jax-rs}\n *\n * @author sea\n */",
    ),
]

R["sentinel-demo/sentinel-demo-jax-rs/src/main/java/com/alibaba/csp/sentinel/demo/jaxrs/SentinelJaxRsConfig.java"] = [
    (
        "/**\n * @author sea\n */",
        "/**\n * 注册 {@link SentinelJaxRsProviderFilter}，对入站 JAX-RS 请求自动创建 Sentinel 资源。\n *\n * @author sea\n */",
    ),
    (
        "    @Bean\n    public SentinelJaxRsProviderFilter sentinelJaxRsProviderFilter() {",
        "    /** 向 Spring 容器注册 Provider 侧 Sentinel 过滤器。 */\n    @Bean\n    public SentinelJaxRsProviderFilter sentinelJaxRsProviderFilter() {",
    ),
]

R["sentinel-demo/sentinel-demo-log-logback/src/main/java/com/alibaba/csp/sentinel/demo/log/logback/CommandCenterLogLoggerImpl.java"] = [
    (
        "/**\n * This class is a demo shows how to create a customized logger implementation.\n *\n * <ul>\n * <li>1. Create a class which implements the {@link Logger} SPI interface</li>\n * <li>2. Use a {@link LogTarget} to specify the log type</li>\n * <li>3. Implement your own method </li>\n * <li>4. Add your logger in {@code com.alibaba.csp.sentinel.log.Logger} file which is stored in\n * {@code resources/META-INF/services/} directory </li>\n * </ul>\n *\n * @author xue8\n */",
        "/**\n * 自定义 {@link CommandCenterLog} 日志实现，将 Sentinel 命令中心日志委托给 Logback。\n *\n * <ul>\n * <li>1. 实现 {@link Logger} SPI 接口</li>\n * <li>2. 用 {@link LogTarget} 指定日志类型</li>\n * <li>3. 实现各级别日志方法</li>\n * <li>4. 在 {@code resources/META-INF/services/com.alibaba.csp.sentinel.log.Logger} 中注册</li>\n * </ul>\n *\n * @author xue8\n */",
    ),
]

R["sentinel-demo/sentinel-demo-log-logback/src/main/java/com/alibaba/csp/sentinel/demo/log/logback/RecordLogLoggerImpl.java"] = [
    (
        "/**\n * This class is a demo shows how to create a customized logger implementation.\n *\n * <ul>\n * <li>1. Create a class which implements the {@link Logger} SPI interface</li>\n * <li>2. Use a {@link LogTarget} to specify the log type</li>\n * <li>3. Implement your own method </li>\n * <li>4. Add your logger in {@code com.alibaba.csp.sentinel.log.Logger} file which is stored in\n * {@code resources/META-INF/services/} directory </li>\n * </ul>\n *\n * @author xue8\n */",
        "/**\n * 自定义 {@link RecordLog} 日志实现，将 Sentinel 记录日志委托给 Logback。\n *\n * <ul>\n * <li>1. 实现 {@link Logger} SPI 接口</li>\n * <li>2. 用 {@link LogTarget} 指定日志类型</li>\n * <li>3. 实现各级别日志方法</li>\n * <li>4. 在 {@code resources/META-INF/services/com.alibaba.csp.sentinel.log.Logger} 中注册</li>\n * </ul>\n *\n * @author xue8\n */",
    ),
]

R["sentinel-demo/sentinel-demo-motan/src/main/java/com/alibaba/csp/sentinel/demo/motan/SentinelMotanConsumerService.java"] = [
    (
        "/**\n * @author zhangxn8\n */",
        "/**\n * Motan Consumer 演示：直连 Provider 并循环调用 hello，验证接口/方法级 QPS 流控。\n *\n * @author zhangxn8\n */",
    ),
    (
        "        //use direct registry",
        "        // 使用 direct 协议直连 Provider",
    ),
    (
        "        // use ZooKeeper: 2181  or consul:8500 registry",
        "        // 亦可改用 ZooKeeper:2181 或 consul:8500 注册中心",
    ),
    (
        "        initFlowRule(5, false);",
        "        // 接口级 QPS=5；method=true 时额外限制 hello 方法\n        initFlowRule(5, false);",
    ),
    (
        "    private static void initFlowRule(int interfaceFlowLimit, boolean method) {",
        "    /** 加载接口级及可选的方法级 {@link FlowRule}。 */\n    private static void initFlowRule(int interfaceFlowLimit, boolean method) {",
    ),
]

R["sentinel-demo/sentinel-demo-motan/src/main/java/com/alibaba/csp/sentinel/demo/motan/SentinelMotanProviderService.java"] = [
    (
        "/**\n * @author zhangxn8\n */",
        "/**\n * Motan Provider 演示：导出 MotanDemoService 于 motan:8002，供 Consumer 直连调用。\n *\n * @author zhangxn8\n */",
    ),
    (
        "        InitExecutor.doInit();",
        "        // 初始化 Sentinel（含 Motan 适配 Filter SPI）\n        InitExecutor.doInit();",
    ),
    (
        "        //use local registry",
        "        // 使用 local 协议，无需外部注册中心",
    ),
    (
        "        // use ZooKeeper: 2181  or consul:8500 registry",
        "        // 亦可改用 ZooKeeper:2181 或 consul:8500 注册中心",
    ),
    (
        "        // registry.setCheck(\"false\"); //是否检查是否注册成功",
        "        // registry.setCheck(\"false\"); // 是否校验注册成功",
    ),
]

R["sentinel-demo/sentinel-demo-motan/src/main/java/com/alibaba/csp/sentinel/demo/motan/service/MotanDemoService.java"] = [
    (
        "/**\n * @author zhangxn8\n */",
        "/**\n * Motan RPC 演示服务接口。\n *\n * @author zhangxn8\n */",
    ),
    (
        "    String hello(String name);",
        "    /** 问候 RPC，Consumer 流控主要测试资源。 */\n    String hello(String name);",
    ),
]

R["sentinel-demo/sentinel-demo-motan/src/main/java/com/alibaba/csp/sentinel/demo/motan/service/impl/MotanDemoServiceImpl.java"] = [
    (
        "/**\n * @author zhangxn8\n */",
        "/**\n * {@link MotanDemoService} Provider 实现。\n *\n * @author zhangxn8\n */",
    ),
    (
        "    @Override\n    public String hello(String name) {",
        "    /** 打印 name 并返回问候语。 */\n    @Override\n    public String hello(String name) {",
    ),
]

R["sentinel-demo/sentinel-demo-nacos-datasource/src/main/java/com/alibaba/csp/sentinel/demo/datasource/nacos/FlowQpsRunner.java"] = [
    (
        "/**\n * Flow QPS runner.\n *\n * @author Carpenter Lee\n * @author Eric Zhao\n */",
        "/**\n * Nacos 动态数据源演示用的 QPS 流量压测 Runner：\n * 多线程对指定资源发起 {@link SphU#entry} 调用并每秒统计 pass/block 数量。\n *\n * @author Carpenter Lee\n * @author Eric Zhao\n */",
    ),
    (
        "    public FlowQpsRunner(String resourceName, int threadCount, int seconds) {",
        "    /** 构造压测 Runner：指定资源名、并发线程数与统计时长（秒）。 */\n    public FlowQpsRunner(String resourceName, int threadCount, int seconds) {",
    ),
    (
        "    public void simulateTraffic() {",
        "    /** 启动 {@link #threadCount} 个线程持续对资源发起 entry 请求。 */\n    public void simulateTraffic() {",
    ),
    (
        "    public void tick() {",
        "    /** 启动定时统计线程，每秒输出 pass/block QPS。 */\n    public void tick() {",
    ),
    (
        "                    // token acquired, means pass",
        "                    // 成功获取令牌，计为通过",
    ),
    (
        "                    // biz exception",
        "                    // 业务异常（本 demo 未模拟）",
    ),
    (
        "                    // ignore",
        "                    // 忽略中断",
    ),
]

R["sentinel-demo/sentinel-demo-nacos-datasource/src/main/java/com/alibaba/csp/sentinel/demo/datasource/nacos/NacosConfigSender.java"] = [
    (
        "/**\n * Nacos config sender for demo.\n *\n * @author Eric Zhao\n */",
        "/**\n * Nacos 规则写入工具：向 Nacos 发布 TestResource 流控规则 JSON，\n * 供 {@link NacosDataSourceDemo} 读取验证。\n *\n * @author Eric Zhao\n */",
    ),
]

R["sentinel-demo/sentinel-demo-nacos-datasource/src/main/java/com/alibaba/csp/sentinel/demo/datasource/nacos/NacosDataSourceDemo.java"] = [
    (
        "/**\n * This demo demonstrates how to use Nacos as the data source of Sentinel rules.\n * Before you start, you need to start a Nacos server in local first, and then\n * use {@link NacosConfigSender} to publish initial rule configuration to Nacos.\n *\n * @author Eric Zhao\n */",
        "/**\n * Nacos 动态数据源演示：从 Nacos 读取流控规则并注册到 {@link FlowRuleManager}。\n * 运行前需先启动本地 Nacos，并用 {@link NacosConfigSender} 发布初始规则。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    // nacos server ip",
        "    // Nacos 服务地址",
    ),
    (
        "    // nacos group",
        "    // Nacos groupId",
    ),
    (
        "    // nacos dataId",
        "    // Nacos dataId",
    ),
    (
        "    // if change to true, should be config NACOS_NAMESPACE_ID",
        "    // 设为 true 时使用命名空间，需配置 NACOS_NAMESPACE_ID",
    ),
    (
        "    // fill your namespace id,if you want to use namespace. for example: 0f5c7314-4983-4022-ad5a-347de1d1057d,you can get it on nacos's console",
        "    // 命名空间 ID，可在 Nacos 控制台获取，例如 0f5c7314-4983-4022-ad5a-347de1d1057d",
    ),
    (
        "        // Assume we config: resource is `TestResource`, initial QPS threshold is 5.",
        "        // 假定 Nacos 中资源 TestResource 初始 QPS 阈值为 5",
    ),
    (
        "    private static void loadRules() {",
        "    /** 默认命名空间：直连 Nacos 并注册流控规则 Property。 */\n    private static void loadRules() {",
    ),
    (
        "    private static void loadMyNamespaceRules() {",
        "    /** 指定命名空间：通过 Properties 连接 Nacos 并注册规则 Property。 */\n    private static void loadMyNamespaceRules() {",
    ),
]

R["sentinel-demo/sentinel-demo-okhttp/src/main/java/com/alibaba/csp/sentinel/demo/okhttp/OkHttpDemoApplication.java"] = [
    (
        "/**\n * @author zhaoyuguang\n */",
        "/**\n * OkHttp 适配器演示 Spring Boot 入口：通过 {@link OkHttpTestController} 发起受 Sentinel 保护的出站 HTTP 请求。\n *\n * @author zhaoyuguang\n */",
    ),
]

R["sentinel-demo/sentinel-demo-okhttp/src/main/java/com/alibaba/csp/sentinel/demo/okhttp/controller/OkHttpTestController.java"] = [
    (
        "/**\n * @author zhaoyuguang\n */",
        "/**\n * OkHttp 出站限流演示控制器：自调用 /okhttp/back 接口并归一化资源名。\n *\n * @author zhaoyuguang\n */",
    ),
    (
        "    private final OkHttpClient client = new OkHttpClient.Builder()",
        "    /** 带 {@link SentinelOkHttpInterceptor} 的客户端，资源名格式 method:url。 */\n    private final OkHttpClient client = new OkHttpClient.Builder()",
    ),
    (
        "    @RequestMapping(\"/okhttp/back\")\n    public String back() {",
        "    /** 本地回显接口（无路径参数）。 */\n    @RequestMapping(\"/okhttp/back\")\n    public String back() {",
    ),
    (
        "    @RequestMapping(\"/okhttp/back/{id}\")\n    public String back(@PathVariable String id) {",
        "    /** 本地回显接口（带 id 路径参数）。 */\n    @RequestMapping(\"/okhttp/back/{id}\")\n    public String back(@PathVariable String id) {",
    ),
    (
        "    @RequestMapping(\"/okhttp/testcase/{id}\")\n    public String testcase(@PathVariable String id) throws Exception {",
        "    /** 通过 OkHttp 调用 /okhttp/back/{id}，触发 Sentinel 出站限流。 */\n    @RequestMapping(\"/okhttp/testcase/{id}\")\n    public String testcase(@PathVariable String id) throws Exception {",
    ),
    (
        "    @RequestMapping(\"/okhttp/testcase\")\n    public String testcase() throws Exception {",
        "    /** 通过 OkHttp 调用 /okhttp/back（无 id）。 */\n    @RequestMapping(\"/okhttp/testcase\")\n    public String testcase() throws Exception {",
    ),
    (
        "    private String getRemoteString(String id) throws IOException {",
        "    /** 构造本地 URL 并用 OkHttp 同步 GET 请求。 */\n    private String getRemoteString(String id) throws IOException {",
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
    index_file = Path("/tmp/git-index-sentinel-w26a")
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
        "sentinel 1.8.10: Chinese-annotate wave 26a [0:15]",
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
