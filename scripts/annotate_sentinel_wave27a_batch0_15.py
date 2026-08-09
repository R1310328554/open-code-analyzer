#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-27a block [0:15] (slotchain-spi, sofa-rpc, sc-gateway, webflux demos)."""
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
    for ln in Path("/tmp/sentinel_w27a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
SCRIPT_NAME = "annotate_sentinel_wave27a_batch0_15.py"
MARK_NOTE = "wave27a [0:15]"

GUARD_FILES = [
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-demo/sentinel-demo-slotchain-spi/src/main/java/com/alibaba/csp/sentinel/demo/slotchain/DemoDegradeRuleApplication.java"] = [
    (
        "/**\n * Demo for degrade rule using custom SlotChainBuilder {@link DemoSlotChainBuilder}.\n *\n * You will see this in sentinel-record.log, indicating that the custom slot chain builder is activated:\n * [SlotChainProvider] Global slot chain builder resolved: com.alibaba.csp.sentinel.demo.slotchain.DemoSlotChainBuilder\n *\n * @author cdfive\n */",
        "/**\n * 基于自定义 {@link DemoSlotChainBuilder} 的熔断降级规则演示。\n *\n * 激活后 sentinel-record.log 会出现：\n * [SlotChainProvider] Global slot chain builder resolved: com.alibaba.csp.sentinel.demo.slotchain.DemoSlotChainBuilder\n *\n * @author cdfive\n */",
    ),
    (
        "    private static void initDegradeRule() {",
        "    /** 加载慢调用比例熔断规则到 {@link DegradeRuleManager}。 */\n    private static void initDegradeRule() {",
    ),
    (
        "                // Max allowed response time",
        "                // 慢调用判定阈值（毫秒）",
    ),
    (
        "                // Retry timeout (in second)",
        "                // 熔断恢复窗口（秒）",
    ),
    (
        "                // Circuit breaker opens when slow request ratio > 20%",
        "                // 慢调用比例超过 20% 时打开熔断器",
    ),
]

R["sentinel-demo/sentinel-demo-slotchain-spi/src/main/java/com/alibaba/csp/sentinel/demo/slotchain/DemoFlowRuleApplication.java"] = [
    (
        "/**\n * Demo for flow rule using custom SlotChainBuilder {@link DemoSlotChainBuilder}.\n *\n * You will see this in sentinel-record.log, indicating that the custom slot chain builder is activated:\n * [SlotChainProvider] Global slot chain builder resolved: com.alibaba.csp.sentinel.demo.slotchain.DemoSlotChainBuilder\n *\n * @author cdfive\n */",
        "/**\n * 基于自定义 {@link DemoSlotChainBuilder} 的 QPS 流控规则演示。\n *\n * 激活后 sentinel-record.log 会出现：\n * [SlotChainProvider] Global slot chain builder resolved: com.alibaba.csp.sentinel.demo.slotchain.DemoSlotChainBuilder\n *\n * @author cdfive\n */",
    ),
    (
        "    private static void initFlowQpsRule() {",
        "    /** 为资源 abc 加载 QPS=5 的 {@link FlowRule}。 */\n    private static void initFlowQpsRule() {",
    ),
    (
        "        // set limit qps to 5",
        "        // QPS 阈值设为 5",
    ),
]

R["sentinel-demo/sentinel-demo-slotchain-spi/src/main/java/com/alibaba/csp/sentinel/demo/slotchain/DemoSlotChainBuilder.java"] = [
    (
        "/**\n * A demo {@link SlotChainBuilder} for build custom slot chain.\n * Two ways to build slot chain are demonstrated.\n *\n * Pay attention to that `ProcessorSlotChain` is not a SPI, but the `SlotChainBuilder`.\n *\n * Most of the time, we don't need to customize `SlotChainBuilder`,\n * maybe customize `ProcessorSlot` is enough, refer to `sentinel-demo-slot-spi` module.\n *\n * Note that the sentinel's default slots and the order of them are very important, be careful when customizing,\n * refer to the constants for slot order definitions in {@link Constants}.\n * You may also refer to {@link DefaultSlotChainBuilder}.\n *\n * @author cdfive\n */",
        "/**\n * 演示如何通过 {@link SlotChainBuilder} SPI 构建自定义 ProcessorSlot 链。\n * 源码中还保留了另一种逐 Slot 加载的写法（已注释）。\n *\n * 注意 {@code ProcessorSlotChain} 本身不是 SPI，可扩展的是 {@code SlotChainBuilder}。\n * 多数场景只需自定义 {@code ProcessorSlot}，可参考 {@code sentinel-demo-slot-spi} 模块。\n *\n * Sentinel 默认 Slot 及其顺序非常关键，定制时务必参考 {@link Constants} 中的顺序常量\n * 及 {@link DefaultSlotChainBuilder} 的默认实现。\n *\n * @author cdfive\n */",
    ),
    (
        "        // Filter out `DegradeSlot`\n        // Test for `DemoDegradeRuleApplication`, the demo will not be blocked by `DegradeException`",
        "        // 移除 DegradeSlot：配合 DemoDegradeRuleApplication，降级规则不会触发 DegradeException",
    ),
    (
        "    /**\n     * Another way to build the slot chain, add slot one by one with `SpiLoader#loadInstance`.\n     * Note that the sentinel's default slots and the order of them are very important, be careful when customizing,\n     * refer to the constants for slot order definitions in {@link com.alibaba.csp.sentinel.Constants}.\n     */",
        "    /**\n     * 另一种构建方式：通过 {@code SpiLoader#loadInstance} 逐个添加 Slot。\n     * 默认 Slot 顺序同样重要，详见 {@link com.alibaba.csp.sentinel.Constants}。\n     */",
    ),
]

R["sentinel-demo/sentinel-demo-sofa-rpc/src/main/java/com/alibaba/csp/sentinel/demo/sofa/rpc/DemoConsumer.java"] = [
    (
        "/**\n * Demo consumer of SOFARPC.\n *\n * Interact with Sentinel Dashboard, add the following VM arguments:\n * <pre>\n * -Dproject.name=DemoProvider -Dcsp.sentinel.dashboard.server=localhost:8080\n * </pre>\n *\n * @author cdfive\n */",
        "/**\n * SOFARPC Consumer 演示：bolt 直连 Provider 循环调用 {@link DemoService}。\n *\n * 对接 Sentinel Dashboard 时可添加 JVM 参数：\n * <pre>\n * -Dproject.name=DemoProvider -Dcsp.sentinel.dashboard.server=localhost:8080\n * </pre>\n *\n * @author cdfive\n */",
    ),
    (
        "        // 设置是否启用Sentinel,默认启用\n        // 也可在rpc-config.json全局设置",
        "        // 是否启用 Sentinel（默认启用），亦可在 rpc-config.json 全局配置",
    ),
]

R["sentinel-demo/sentinel-demo-sofa-rpc/src/main/java/com/alibaba/csp/sentinel/demo/sofa/rpc/DemoProvider.java"] = [
    (
        "/**\n * Demo provider of SOFARPC\n *\n * Interact with Sentinel Dashboard, add the following VM arguments:\n * <pre>\n * -Dproject.name=DemoProvider -Dcsp.sentinel.dashboard.server=localhost:8080\n * </pre>\n *\n * @author cdfive\n */",
        "/**\n * SOFARPC Provider 演示：在 bolt:12001 导出 {@link DemoService}。\n *\n * 对接 Sentinel Dashboard 时可添加 JVM 参数：\n * <pre>\n * -Dproject.name=DemoProvider -Dcsp.sentinel.dashboard.server=localhost:8080\n * </pre>\n *\n * @author cdfive\n */",
    ),
    (
        "        // 设置是否启用Sentinel,默认启用\n        // 也可在rpc-config.json全局设置",
        "        // 是否启用 Sentinel（默认启用），亦可在 rpc-config.json 全局配置",
    ),
]

R["sentinel-demo/sentinel-demo-sofa-rpc/src/main/java/com/alibaba/csp/sentinel/demo/sofa/rpc/service/DemoService.java"] = [
    (
        "/**\n * @author cdfive\n */",
        "/**\n * SOFARPC 演示服务接口。\n *\n * @author cdfive\n */",
    ),
    (
        "    String sayHello(Integer index, String name, int year);",
        "    /** 问候 RPC，index 为调用序号，供 Consumer 压测与流控观测。 */\n    String sayHello(Integer index, String name, int year);",
    ),
]

R["sentinel-demo/sentinel-demo-sofa-rpc/src/main/java/com/alibaba/csp/sentinel/demo/sofa/rpc/service/impl/DemoServiceImpl.java"] = [
    (
        "/**\n * @author cdfive\n */",
        "/**\n * {@link DemoService} 的 Provider 实现：随机 sleep 模拟耗时。\n *\n * @author cdfive\n */",
    ),
    (
        "    @Override\n    public String sayHello(Integer index, String name, int year) {",
        "    /** 打印请求并随机休眠 0~49ms 后返回问候语。 */\n    @Override\n    public String sayHello(Integer index, String name, int year) {",
    ),
]

R["sentinel-demo/sentinel-demo-spring-cloud-gateway/src/main/java/com/alibaba/csp/sentinel/demo/spring/sc/gateway/GatewayConfiguration.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Spring Cloud Gateway 与 Sentinel 集成配置：注册过滤器、异常处理器及网关流控规则。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "        // Register the block exception handler for Spring Cloud Gateway.",
        "        // 注册 Gateway 专用的 Sentinel 阻塞异常处理器",
    ),
    (
        "    @PostConstruct\n    public void doInit() {",
        "    /** 容器启动后加载自定义 API 分组与 Gateway 流控规则。 */\n    @PostConstruct\n    public void doInit() {",
    ),
    (
        "    private void initCustomizedApis() {",
        "    /** 注册 some_customized_api / another_customized_api 两组 API 定义。 */\n    private void initCustomizedApis() {",
    ),
    (
        "    private void initGatewayRules() {",
        "    /** 加载路由级与自定义 API 级 {@link GatewayFlowRule}（含热点参数示例）。 */\n    private void initGatewayRules() {",
    ),
]

R["sentinel-demo/sentinel-demo-spring-cloud-gateway/src/main/java/com/alibaba/csp/sentinel/demo/spring/sc/gateway/GatewayDemoApplication.java"] = [
    (
        "/**\n * <p>A demo for Spring Cloud Gateway.</p>\n *\n * <p>To integrate with Sentinel dashboard, you can run the demo with the parameters (an example):\n * <code>-Dproject.name=spring-cloud-gateway -Dcsp.sentinel.dashboard.server=localhost:8080\n * -Dcsp.sentinel.api.port=8720 -Dcsp.sentinel.app.type=1\n * </code>\n * </p>\n *\n * @author Eric Zhao\n */",
        "/**\n * Spring Cloud Gateway 与 Sentinel 适配演示入口。\n *\n * <p>对接 Dashboard 示例 JVM 参数：</p>\n * <code>-Dproject.name=spring-cloud-gateway -Dcsp.sentinel.dashboard.server=localhost:8080\n * -Dcsp.sentinel.api.port=8720 -Dcsp.sentinel.app.type=1\n * </code>\n *\n * @author Eric Zhao\n */",
    ),
]

R["sentinel-demo/sentinel-demo-spring-webflux/src/main/java/com/alibaba/csp/sentinel/demo/spring/webflux/WebFluxDemoApplication.java"] = [
    (
        "/**\n * <p>A demo for Spring WebFlux reactive application.</p>\n *\n * <p>To integrate with Sentinel dashboard, you can run the demo with the parameters (an example):\n * <code>-Dproject.name=WebFluxDemoApplication -Dcsp.sentinel.dashboard.server=localhost:8080\n * -Dcsp.sentinel.api.port=8720\n * </code>\n * </p>\n *\n * @author Eric Zhao\n */",
        "/**\n * Spring WebFlux 响应式应用与 Sentinel 适配演示入口。\n *\n * <p>对接 Dashboard 示例 JVM 参数：</p>\n * <code>-Dproject.name=WebFluxDemoApplication -Dcsp.sentinel.dashboard.server=localhost:8080\n * -Dcsp.sentinel.api.port=8720\n * </code>\n *\n * @author Eric Zhao\n */",
    ),
]

R["sentinel-demo/sentinel-demo-spring-webflux/src/main/java/com/alibaba/csp/sentinel/demo/spring/webflux/config/RedisConfig.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * 响应式 Redis 客户端配置，供 {@link com.alibaba.csp.sentinel.demo.spring.webflux.service.BazService} 使用。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    @Bean\n    public ReactiveRedisTemplate<String, String> stringReactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory){",
        "    /** 创建 String 类型的 {@link ReactiveRedisTemplate} Bean。 */\n    @Bean\n    public ReactiveRedisTemplate<String, String> stringReactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory){",
    ),
]

R["sentinel-demo/sentinel-demo-spring-webflux/src/main/java/com/alibaba/csp/sentinel/demo/spring/webflux/config/WebFluxConfig.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * WebFlux Sentinel 适配配置：注册 {@link SentinelWebFluxFilter} 与阻塞异常处理器。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "        // Register the block exception handler for Spring WebFlux.",
        "        // 注册 WebFlux 专用的 Sentinel 阻塞异常处理器",
    ),
    (
        "        // Register the Sentinel WebFlux filter.",
        "        // 注册 Sentinel WebFlux 过滤器，对入站请求自动创建资源",
    ),
]

R["sentinel-demo/sentinel-demo-spring-webflux/src/main/java/com/alibaba/csp/sentinel/demo/spring/webflux/controller/BazController.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Redis 读写 REST 接口，通过 {@link SentinelReactorTransformer} 对 Mono Publisher 限流。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    @GetMapping(\"/{id}\")\n    public Mono<String> apiGetValue(@PathVariable(\"id\") Long id) {",
        "    /** GET /baz/{id}：按 id 读取 Redis 值，资源名 BazService:getById。 */\n    @GetMapping(\"/{id}\")\n    public Mono<String> apiGetValue(@PathVariable(\"id\") Long id) {",
    ),
    (
        "    @PostMapping(\"/{id}\")\n    public Mono<Boolean> apiSetValue(@PathVariable(\"id\") Long id, @RequestBody String value) {",
        "    /** POST /baz/{id}：写入 Redis，资源名 BazService:setValue。 */\n    @PostMapping(\"/{id}\")\n    public Mono<Boolean> apiSetValue(@PathVariable(\"id\") Long id, @RequestBody String value) {",
    ),
]

R["sentinel-demo/sentinel-demo-spring-webflux/src/main/java/com/alibaba/csp/sentinel/demo/spring/webflux/controller/FooController.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * 响应式 Mono/Flux 演示控制器，展示 {@link SentinelReactorTransformer} 对 Publisher 的限流。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    @GetMapping(\"/single\")\n    public Mono<String> apiNormalSingle() {",
        "    /** GET /foo/single：单值 Mono，资源名 demo_foo_normal_single。 */\n    @GetMapping(\"/single\")\n    public Mono<String> apiNormalSingle() {",
    ),
    (
        "            // transform the publisher here.",
        "            // 在此对 Publisher 应用 Sentinel 变换",
    ),
    (
        "    @GetMapping(\"/flux\")\n    public Flux<Integer> apiNormalFlux() {",
        "    /** GET /foo/flux：整数 Flux 流，资源名 demo_foo_normal_flux。 */\n    @GetMapping(\"/flux\")\n    public Flux<Integer> apiNormalFlux() {",
    ),
    (
        "    @GetMapping(\"/slow\")\n    public Mono<String> apiDoSomethingSlow(ServerHttpResponse response) {",
        "    /** GET /foo/slow：慢调用接口（未套 SentinelReactorTransformer，依赖 WebFlux Filter）。 */\n    @GetMapping(\"/slow\")\n    public Mono<String> apiDoSomethingSlow(ServerHttpResponse response) {",
    ),
]

R["sentinel-demo/sentinel-demo-spring-webflux/src/main/java/com/alibaba/csp/sentinel/demo/spring/webflux/service/BazService.java"] = [
    (
        "/**\n * <p>A sample service for interacting with Redis via reactive Redis client.</p>\n * <p>To play this service, you need a Redis instance running in local.</p>\n *\n * @author Eric Zhao\n */",
        "/**\n * 基于 {@link ReactiveRedisTemplate} 的响应式 Redis 读写服务。\n * <p>运行前需在本地启动 Redis 实例。</p>\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    public Mono<String> getById(Long id) {",
        "    /** 按 id 读取键 sentinel-reactor-test:{id}，不存在时返回 not_found。 */\n    public Mono<String> getById(Long id) {",
    ),
    (
        "    public Mono<Boolean> setValue(Long id, String value) {",
        "    /** 向 sentinel-reactor-test:{id} 写入 value。 */\n    public Mono<Boolean> setValue(Long id, String value) {",
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
    index_file = Path("/tmp/git-index-sentinel-w27a")
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
        "sentinel 1.8.10: Chinese-annotate wave 27a [0:15]",
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
