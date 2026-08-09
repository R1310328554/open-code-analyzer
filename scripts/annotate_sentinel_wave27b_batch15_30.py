#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-27b block [15:30] (webflux, webmvc, transport, zookeeper, zuul demos)."""
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
    for ln in Path("/tmp/sentinel_w27b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
SCRIPT_NAME = "annotate_sentinel_wave27b_batch15_30.py"
MARK_NOTE = "wave27b [15:30]"

GUARD_FILES = [
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-demo/sentinel-demo-spring-webflux/src/main/java/com/alibaba/csp/sentinel/demo/spring/webflux/service/FooService.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * WebFlux 演示业务服务：提供 Mono/Flux 响应式方法，供 {@link com.alibaba.csp.sentinel.demo.spring.webflux.controller.FooController} 限流演示。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    public Mono<String> emitSingle() {",
        "    /** 返回随机整数拼接 \"d\" 的单值 Mono。 */\n    public Mono<String> emitSingle() {",
    ),
    (
        "    public Flux<Integer> emitMultiple() {",
        "    /** 从随机起点连续发射 10 个整数的 Flux。 */\n    public Flux<Integer> emitMultiple() {",
    ),
    (
        "    public Mono<String> doSomethingSlow() {",
        "    /** 在独立线程池上执行 2s 延迟任务，演示异步限流场景。 */\n    public Mono<String> doSomethingSlow() {",
    ),
]

R["sentinel-demo/sentinel-demo-spring-webmvc/src/main/java/com/alibaba/csp/sentinel/demo/spring/webmvc/WebMvcDemoApplication.java"] = [
    (
        "/**\n * <p>Add the JVM parameter to connect to the dashboard:</p>\n * {@code -Dcsp.sentinel.dashboard.server=127.0.0.1:8080 -Dproject.name=sentinel-demo-spring-webmvc}\n *\n * @author kaizi2009\n */",
        "/**\n * Spring WebMvc Sentinel 演示入口。\n * <p>连接 Dashboard 请添加 JVM 参数：</p>\n * {@code -Dcsp.sentinel.dashboard.server=127.0.0.1:8080 -Dproject.name=sentinel-demo-spring-webmvc}\n *\n * @author kaizi2009\n */",
    ),
]

R["sentinel-demo/sentinel-demo-spring-webmvc/src/main/java/com/alibaba/csp/sentinel/demo/spring/webmvc/config/InterceptorConfig.java"] = [
    (
        "/**\n * Config sentinel interceptor\n *\n * @author kaizi2009\n */",
        "/**\n * 注册 Sentinel WebMvc 拦截器：URL 级限流、来源解析与 Web 上下文配置。\n *\n * @author kaizi2009\n */",
    ),
    (
        "        // Add Sentinel interceptor",
        "        // 注册 Sentinel 拦截器",
    ),
    (
        "        //Make exception visible to Sentinel if you have configured ExceptionHandler",
        "        // 配置 ExceptionHandler 后，使异常对 Sentinel 可见",
    ),
    (
        "        // Depending on your situation, you can choose to process the BlockException via\n        // the BlockExceptionHandler or throw it directly, then handle it\n        // in Spring web global exception handler.",
        "        // 可按需通过 BlockExceptionHandler 处理，或直接抛出由全局异常处理器捕获",
    ),
    (
        "        // Use the default handler.",
        "        // 使用默认 BlockExceptionHandler",
    ),
    (
        "        // Custom configuration if necessary",
        "        // 按需自定义配置",
    ),
    (
        "        // By default web context is true, means that unify web context(i.e. use the default context name),\n        // in most scenarios that's enough, and it could reduce the memory footprint.\n        // If set it to false, entrance contexts will be separated by different URLs,\n        // which is useful to support \"chain\" relation flow strategy.\n        // We can change it and view different result in `Resource Chain` menu of dashboard.",
        "        // webContextUnify=true 时统一 Web 上下文（默认），可节省内存；\n        // 设为 false 则按 URL 分离入口上下文，便于链路流控，可在 Dashboard「资源链路」查看效果",
    ),
    (
        "        // Add sentinel interceptor",
        "        // 注册 SentinelWebInterceptor",
    ),
    (
        "    private void addSpringMvcTotalInterceptor(InterceptorRegistry registry) {",
        "    /** 注册全站 URL 总量统计拦截器（演示用，当前未调用）。 */\n    private void addSpringMvcTotalInterceptor(InterceptorRegistry registry) {",
    ),
    (
        "        //Config",
        "        // 全站统计配置",
    ),
    (
        "        //Custom configuration if necessary",
        "        // 按需自定义属性名与总资源名",
    ),
    (
        "        //Add sentinel interceptor",
        "        // 注册 SentinelWebTotalInterceptor",
    ),
]

R["sentinel-demo/sentinel-demo-spring-webmvc/src/main/java/com/alibaba/csp/sentinel/demo/spring/webmvc/config/SentinelSpringMvcBlockHandlerConfig.java"] = [
    (
        "/**\n * Spring configuration for global exception handler.\n * This will be activated when the {@code BlockExceptionHandler}\n * throws {@link BlockException directly}.\n *\n * @author kaizi2009\n */",
        "/**\n * Sentinel 限流全局异常处理：当 {@code BlockExceptionHandler} 直接抛出 {@link BlockException} 时生效。\n *\n * @author kaizi2009\n */",
    ),
    (
        "        // Return the customized result.",
        "        // 返回自定义限流响应",
    ),
]

R["sentinel-demo/sentinel-demo-spring-webmvc/src/main/java/com/alibaba/csp/sentinel/demo/spring/webmvc/controller/WebMvcTestController.java"] = [
    (
        "/**\n * Test controller\n *\n * @author kaizi2009\n */",
        "/**\n * WebMvc 限流演示控制器：提供 hello、foo、async 等测试接口。\n *\n * @author kaizi2009\n */",
    ),
    (
        "    public String apiHello() {",
        "    /** GET /hello：简单问候接口。 */\n    public String apiHello() {",
    ),
    (
        "    public String apiError() {",
        "    /** GET /err：模拟业务错误响应。 */\n    public String apiError() {",
    ),
    (
        "    public String apiFoo(@PathVariable(\"id\") Long id) {",
        "    /** GET /foo/{id}：带路径参数的问候接口。 */\n    public String apiFoo(@PathVariable(\"id\") Long id) {",
    ),
    (
        "    public String apiExclude(@PathVariable(\"id\") Long id) {",
        "    /** GET /exclude/{id}：用于排除限流的测试接口。 */\n    public String apiExclude(@PathVariable(\"id\") Long id) {",
    ),
    (
        "    public ModelAndView apiForward() {",
        "    /** GET /forward：视图转发演示。 */\n    public ModelAndView apiForward() {",
    ),
    (
        "    public DeferredResult<String> distribute() throws Exception {",
        "    /** GET /async：DeferredResult 异步响应演示。 */\n    public DeferredResult<String> distribute() throws Exception {",
    ),
    (
        "    private void doBusiness() {",
        "    /** 模拟随机 0~100ms 业务耗时。 */\n    private void doBusiness() {",
    ),
]

R["sentinel-demo/sentinel-demo-spring-webmvc/src/main/java/com/alibaba/csp/sentinel/demo/spring/webmvc/vo/ResultWrapper.java"] = [
    (
        "/**\n * @author kaizi2009\n */",
        "/**\n * 统一 API 响应包装：限流时返回 code=-1 与提示消息。\n *\n * @author kaizi2009\n */",
    ),
    (
        "    public static ResultWrapper blocked() {",
        "    /** 构造 Sentinel 限流响应。 */\n    public static ResultWrapper blocked() {",
    ),
    (
        "    public String toJsonString() {",
        "    /** 序列化为 JSON 字符串。 */\n    public String toJsonString() {",
    ),
]

R["sentinel-demo/sentinel-demo-transport-spring-mvc/src/main/java/com/alibaba/csp/sentinel/demo/transport/springmvc/TransportSpringMvcDemoApplication.java"] = [
    (
        "/**\n * <p>Add the JVM parameter to connect to the dashboard:</p>\n * {@code -Dcsp.sentinel.dashboard.server=127.0.0.1:8080 -Dproject.name=sentinel-demo-transport-spring-mvc}\n *\n * <p>Add the JVM parameter to tell dashboard your application port:</p>\n * {@code -Dcsp.sentinel.api.port=10000}\n *\n * @author shenbaoyong\n */",
        "/**\n * Spring MVC + Sentinel Transport 演示：暴露 HTTP API 供 Dashboard 拉取指标。\n * <p>连接 Dashboard：</p>\n * {@code -Dcsp.sentinel.dashboard.server=127.0.0.1:8080 -Dproject.name=sentinel-demo-transport-spring-mvc}\n * <p>指定应用 API 端口：</p>\n * {@code -Dcsp.sentinel.api.port=10000}\n *\n * @author shenbaoyong\n */",
    ),
    (
        "    public static void initFlowRules() {",
        "    /** 加载 demo-hello-api 流控规则：QPS=1。 */\n    public static void initFlowRules() {",
    ),
    (
        "    public String hello() {",
        "    /** GET /hello：受 Sentinel 保护的问候接口。 */\n    public String hello() {",
    ),
    (
        "    private static void triggerSentinelInit() {",
        "    /** 异步触发 Sentinel InitExecutor 初始化。 */\n    private static void triggerSentinelInit() {",
    ),
]

R["sentinel-demo/sentinel-demo-zookeeper-datasource/src/main/java/com/alibaba/csp/sentinel/demo/datasource/zookeeper/ZookeeperConfigSender.java"] = [
    (
        "/**\n * Zookeeper config sender for demo\n *\n * @author guonanjun\n */",
        "/**\n * Zookeeper 规则推送工具：向 ZK 节点写入流控规则 JSON，供 {@link ZookeeperDataSourceDemo} 动态加载。\n *\n * @author guonanjun\n */",
    ),
]

R["sentinel-demo/sentinel-demo-zookeeper-datasource/src/main/java/com/alibaba/csp/sentinel/demo/datasource/zookeeper/ZookeeperDataSourceDemo.java"] = [
    (
        "/**\n * Zookeeper ReadableDataSource Demo\n *\n * @author guonanjun\n */",
        "/**\n * Zookeeper {@link ReadableDataSource} 演示：从 ZK 节点动态加载并热更新流控规则。\n *\n * @author guonanjun\n */",
    ),
    (
        "        // 使用zookeeper的场景",
        "        // 方式一：直接指定 ZK 路径",
    ),
    (
        "        // 方便扩展的场景",
        "        // 方式二：使用 groupId/dataId（便于与 Nacos 切换）",
    ),
    (
        "        // 引入groupId和dataId的概念，是为了方便和Nacos进行切换",
        "        // groupId/dataId 便于与 Nacos 数据源切换",
    ),
    (
        "        // 规则会持久化到zk的/groupId/flowDataId节点",
        "        // 规则持久化到 /groupId/flowDataId 节点",
    ),
    (
        "        // groupId和和flowDataId可以用/开头也可以不用",
        "        // groupId 与 dataId 可带或不带前导 /",
    ),
    (
        "        // 建议不用以/开头，目的是为了如果从Zookeeper切换到Nacos的话，只需要改数据源类名就可以",
        "        // 建议不以 / 开头，便于从 Zookeeper 切换到 Nacos 时仅改数据源类",
    ),
]

R["sentinel-demo/sentinel-demo-zuul-gateway/src/main/java/com/alibaba/csp/sentinel/demo/zuul/gateway/GatewayRuleConfig.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Zuul 1.x 网关演示规则配置：自定义 API 分组与 Gateway 流控规则。\n * <p>生产环境建议通过动态数据源或 Dashboard 推送规则。</p>\n *\n * @author Eric Zhao\n */",
    ),
    (
        "        // Prepare some gateway rules and API definitions (only for demo).\n        // It's recommended to leverage dynamic data source or the Sentinel dashboard to push the rules.",
        "        // 演示用网关规则与 API 定义；生产建议走动态数据源或 Dashboard",
    ),
    (
        "    private void initCustomizedApis() {",
        "    /** 注册自定义 API 分组：some_customized_api 与 another_customized_api。 */\n    private void initCustomizedApis() {",
    ),
    (
        "    private void initGatewayRules() {",
        "    /** 加载路由级与 API 级 Gateway 流控规则（含参数限流）。 */\n    private void initGatewayRules() {",
    ),
]

R["sentinel-demo/sentinel-demo-zuul-gateway/src/main/java/com/alibaba/csp/sentinel/demo/zuul/gateway/ZuulConfig.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * 注册 Sentinel Zuul 1.x 过滤器：Pre/Post/Error 三阶段限流与降级。\n *\n * @author Eric Zhao\n */",
    ),
]

R["sentinel-demo/sentinel-demo-zuul-gateway/src/main/java/com/alibaba/csp/sentinel/demo/zuul/gateway/ZuulGatewayDemoApplication.java"] = [
    (
        "/**\n * <p>A demo for using Zuul 1.x with Spring Cloud and Sentinel.</p>\n *\n * <p>To integrate with Sentinel dashboard, you can run the demo with the parameters (an example):\n * <code>\n * -Dproject.name=zuul-gateway -Dcsp.sentinel.dashboard.server=localhost:8080\n * -Dcsp.sentinel.api.port=8720 -Dcsp.sentinel.app.type=1\n * </code>\n * </p>\n *\n * @author Eric Zhao\n */",
        "/**\n * Zuul 1.x + Spring Cloud + Sentinel 网关演示入口。\n * <p>连接 Dashboard 示例参数：</p>\n * <code>\n * -Dproject.name=zuul-gateway -Dcsp.sentinel.dashboard.server=localhost:8080\n * -Dcsp.sentinel.api.port=8720 -Dcsp.sentinel.app.type=1\n * </code>\n *\n * @author Eric Zhao\n */",
    ),
]

R["sentinel-demo/sentinel-demo-zuul2-gateway/src/main/java/com/alibaba/csp/sentinel/demo/zuul2/gateway/FiltersRegisteringService.java"] = [
    (
        "public class FiltersRegisteringService {",
        "/**\n * Zuul 2.x 过滤器注册服务：启动时将 Spring 容器中的 {@link ZuulFilter} 注册到 {@link FilterRegistry}。\n */\npublic class FiltersRegisteringService {",
    ),
    (
        "    public void initialize() {",
        "    /** {@link PostConstruct} 回调：将所有过滤器按名称注册。 */\n    public void initialize() {",
    ),
]

R["sentinel-demo/sentinel-demo-zuul2-gateway/src/main/java/com/alibaba/csp/sentinel/demo/zuul2/gateway/GatewayRuleConfig.java"] = [
    (
        "public class GatewayRuleConfig {",
        "/**\n * Zuul 2.x 网关演示规则配置：自定义 API 分组与 Gateway 流控规则。\n * <p>生产环境建议通过动态数据源或 Dashboard 推送规则。</p>\n */\npublic class GatewayRuleConfig {",
    ),
    (
        "        // Prepare some gateway rules and API definitions (only for demo).\n        // It's recommended to leverage dynamic data source or the Sentinel dashboard to push the rules.",
        "        // 演示用网关规则与 API 定义；生产建议走动态数据源或 Dashboard",
    ),
    (
        "    private void initCustomizedApis() {",
        "    /** 注册自定义 API 分组：some_customized_api 与 another_customized_api。 */\n    private void initCustomizedApis() {",
    ),
    (
        "    private void initGatewayRules() {",
        "    /** 加载路由级与 API 级 Gateway 流控规则（含参数限流）。 */\n    private void initGatewayRules() {",
    ),
]

R["sentinel-demo/sentinel-demo-zuul2-gateway/src/main/java/com/alibaba/csp/sentinel/demo/zuul2/gateway/SampleServerStartup.java"] = [
    (
        "@Singleton\npublic class SampleServerStartup extends BaseServerStartup {",
        "/**\n * Zuul 2.x 演示服务器启动类：配置 Netty 端口与 Channel 初始化。\n */\n@Singleton\npublic class SampleServerStartup extends BaseServerStartup {",
    ),
    (
        "        /* These settings may need to be tweaked depending if you're running behind an ELB HTTP listener, TCP listener,\n         * or directly on the internet.\n         */",
        "        /* 以下配置需按部署环境调整：ELB HTTP/TCP 监听或直接暴露公网 */",
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
    index_file = Path("/tmp/git-index-sentinel-w27b")
    index_file.unlink(missing_ok=True)
    Path("/workspace/.git/index.lock").unlink(missing_ok=True)
    env = os.environ.copy()
    env["GIT_INDEX_FILE"] = str(index_file)
    base = subprocess.check_output(
        ["git", "-C", str(ROOT), "rev-parse", base_ref], text=True
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "read-tree", base], env=env, check=True)
    tree_before = subprocess.check_output(
        ["git", "-C", str(ROOT), "write-tree"], env=env, text=True
    ).strip()
    tree_count = len(
        subprocess.check_output(
            ["git", "-C", str(ROOT), "ls-tree", "-r", "--name-only", tree_before],
            env=env,
            text=True,
        ).splitlines()
    )
    if tree_count < 50000:
        raise RuntimeError(f"read-tree guard failed: tree_count={tree_count} (expected >=50000)")
    normal = [p for p in paths if not p.endswith("worker.log")]
    forced = [p for p in paths if p.endswith("worker.log")]
    if normal:
        subprocess.run(["git", "-C", str(ROOT), "add", "--", *normal], env=env, check=True)
    if forced:
        subprocess.run(["git", "-C", str(ROOT), "add", "-f", "--", *forced], env=env, check=True)
    tree_guard(env)
    tree = subprocess.check_output(["git", "-C", str(ROOT), "write-tree"], env=env, text=True).strip()
    commit = subprocess.check_output(
        ["git", "-C", str(ROOT), "commit-tree", tree, "-p", base, "-m", message],
        text=True,
    ).strip()
    subprocess.run(["git", "-C", str(ROOT), "update-ref", "refs/heads/main", commit], check=True)
    index_file.unlink(missing_ok=True)
    return commit, tree_count


def push_main(retries: int = 4) -> None:
    r = subprocess.CompletedProcess([], 1)
    for attempt in range(retries):
        Path("/workspace/.git/index.lock").unlink(missing_ok=True)
        r = subprocess.run(
            ["git", "-C", str(ROOT), "push", "-u", "origin", "main"],
            capture_output=True,
            text=True,
        )
        if r.returncode == 0:
            return
        if attempt + 1 < retries:
            subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
            time.sleep(4 * (2**attempt))
            Path("/workspace/.git/index.lock").unlink(missing_ok=True)
            subprocess.run(["git", "-C", str(ROOT), "reset", "--hard", "origin/main"], check=True)
            time.sleep(1)
    raise subprocess.CalledProcessError(r.returncode, r.args, r.stdout, r.stderr)


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
    if len(BATCH_LIST) != 15:
        raise SystemExit(f"Expected 15 files in batch list, got {len(BATCH_LIST)}")
    subprocess.run(["git", "-C", str(ROOT), "fetch", "origin", "main"], check=True)
    subprocess.run(["git", "-C", str(ROOT), "reset", "--hard", "origin/main"], check=True)
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
        "sentinel 1.8.10: Chinese-annotate wave 27b [15:30]",
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
    update_batch_json()
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
                "origin_main_chinese": origin_chinese,
                "all_15_chinese": all(origin_chinese.values()),
            },
            ensure_ascii=False,
            indent=2,
        )
    )
    return 0 if all(origin_chinese.values()) else 1


if __name__ == "__main__":
    raise SystemExit(main())
