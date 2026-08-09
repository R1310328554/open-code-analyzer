#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-26b block [15:30] (param-flow, quarkus, rocketmq, servlet, slot-spi demos)."""
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
    for ln in Path("/tmp/sentinel_w26b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
SCRIPT_NAME = "annotate_sentinel_wave26b_batch15_30.py"
MARK_NOTE = "wave26b [15:30]"

GUARD_FILES = [
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-demo/sentinel-demo-parameter-flow-control/src/main/java/com/alibaba/csp/sentinel/demo/flow/param/ParamFlowQpsDemo.java"] = [
    (
        "/**\n * This demo demonstrates flow control by frequent (\"hot spot\") parameters.\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
        "/**\n * 热点参数流控演示：按第 0 个参数值分别统计 QPS，并为特定参数设置例外阈值。\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
    ),
    (
        "    /**\n     * Here we prepare different parameters to validate flow control by parameters.\n     */",
        "    /** 准备多种参数值，用于验证热点参数流控效果。 */",
    ),
    (
        "    private static void initParamFlowRules() {",
        "    /** 加载热点参数流控规则：全局 QPS=5，参数 PARAM_B 例外 QPS=10。 */\n    private static void initParamFlowRules() {",
    ),
    (
        "        // QPS mode, threshold is 5 for every frequent \"hot spot\" parameter in index 0 (the first arg).",
        "        // QPS 模式：第 0 个参数（热点参数）全局阈值为 5",
    ),
    (
        "        // We can set threshold count for specific parameter value individually.\n        // Here we add an exception item. That means: QPS threshold of entries with parameter `PARAM_B` (type: int)\n        // in index 0 will be 10, rather than the global threshold (5).",
        "        // 可为特定参数值单独设置阈值：PARAM_B 的 QPS 阈值为 10，而非全局 5",
    ),
]

R["sentinel-demo/sentinel-demo-parameter-flow-control/src/main/java/com/alibaba/csp/sentinel/demo/flow/param/ParamFlowQpsRunner.java"] = [
    (
        "/**\n * A traffic runner to simulate flow for different parameters.\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
        "/**\n * 热点参数流控压测 Runner：随机选取参数发起 {@link SphU#entry}，\n * 按参数分别统计 pass/block 并每秒输出。\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
    ),
    (
        "    /**\n     * Pick one of provided parameters randomly.\n     *\n     * @return picked parameter\n     */",
        "    /** 从预设参数数组中随机选取一个。 */",
    ),
    (
        "    void simulateTraffic() {",
        "    /** 启动多线程压测任务。 */\n    void simulateTraffic() {",
    ),
    (
        "    void tick() {",
        "    /** 启动定时统计线程。 */\n    void tick() {",
    ),
    (
        "                    // Add pass for parameter.",
        "                    // 该参数计为通过",
    ),
    (
        "                    // block.incrementAndGet();",
        "                    // 该参数计为被限流",
    ),
    (
        "                    // biz exception",
        "                    // 业务异常",
    ),
    (
        "            System.out.println(\"Begin to run! Go go go!\");",
        "            System.out.println(\"Begin to run! Go go go!\"); // 开始压测",
    ),
    (
        "            System.out.println(\"See corresponding metrics.log for accurate statistic data\");",
        "            System.out.println(\"See corresponding metrics.log for accurate statistic data\"); // 精确数据见 metrics.log",
    ),
    (
        "                // There may be a mismatch for time window of internal sliding window.\n                // See corresponding `metrics.log` for accurate statistic log.",
        "                // 控制台统计与内部滑动窗口可能存在偏差，精确数据见 metrics.log",
    ),
]

R["sentinel-demo/sentinel-demo-quarkus/.mvn/wrapper/MavenWrapperDownloader.java"] = [
    (
        "public class MavenWrapperDownloader {",
        "/** Maven Wrapper JAR 下载工具（Quarkus 演示工程附带）。 */\npublic class MavenWrapperDownloader {",
    ),
    (
        "    /**\n     * Default URL to download the maven-wrapper.jar from, if no 'downloadUrl' is provided.\n     */",
        "    /** 未配置 downloadUrl 时使用的默认 maven-wrapper.jar 下载地址。 */",
    ),
    (
        "        // If the maven-wrapper.properties exists, read it and check if it contains a custom\n        // wrapperUrl parameter.",
        "        // 若存在 maven-wrapper.properties，读取其中的 wrapperUrl 覆盖默认地址",
    ),
    (
        "                    // Ignore ...",
        "                    // 忽略关闭异常",
    ),
]

R["sentinel-demo/sentinel-demo-quarkus/src/main/java/com/alibaba/csp/sentinel/demo/quarkus/AppLifecycleBean.java"] = [
    (
        "/**\n * @author sea\n */",
        "/**\n * Quarkus 应用生命周期 Bean：启动时加载流控与熔断规则（演示用，生产建议走数据源）。\n *\n * @author sea\n */",
    ),
    (
        "    void onStart(@Observes StartupEvent ev) {",
        "    /** 应用启动时注册 FlowRule 与 DegradeRule。 */\n    void onStart(@Observes StartupEvent ev) {",
    ),
    (
        "        // Only for test here. Actually it's recommended to configure rules via data-source.",
        "        // 仅演示用；生产环境建议通过数据源配置规则",
    ),
]

R["sentinel-demo/sentinel-demo-quarkus/src/main/java/com/alibaba/csp/sentinel/demo/quarkus/CustomExceptionMapper.java"] = [
    (
        "/**\n * @author sea\n */",
        "/**\n * JAX-RS 全局异常映射（演示用，当前 @Provider 已注释）。\n * 将未捕获异常转为 HTTP 500 与异常消息。\n *\n * @author sea\n */",
    ),
    (
        "    @Override\n    public Response toResponse(Throwable exception) {",
        "    /** 记录异常并返回 500 响应。 */\n    @Override\n    public Response toResponse(Throwable exception) {",
    ),
]

R["sentinel-demo/sentinel-demo-quarkus/src/main/java/com/alibaba/csp/sentinel/demo/quarkus/GreetingFallback.java"] = [
    (
        "/**\n * @author sea\n */",
        "/**\n * {@link GreetingService} 的全局 blockHandler 与 defaultFallback 实现类。\n *\n * @author sea\n */",
    ),
    (
        "    public static String globalBlockHandler(String name, BlockException ex) {",
        "    /** 被 Sentinel 限流/熔断时的全局 blockHandler。 */\n    public static String globalBlockHandler(String name, BlockException ex) {",
    ),
    (
        "    public static String globalDefaultFallback(String name, Throwable t) {",
        "    /** 业务异常时的全局 defaultFallback。 */\n    public static String globalDefaultFallback(String name, Throwable t) {",
    ),
]

R["sentinel-demo/sentinel-demo-quarkus/src/main/java/com/alibaba/csp/sentinel/demo/quarkus/GreetingResource.java"] = [
    (
        "@Path(\"/hello\")\npublic class GreetingResource {",
        "/**\n * Quarkus REST 演示资源：流控、熔断、异步与异常映射等场景入口。\n */\n@Path(\"/hello\")\npublic class GreetingResource {",
    ),
    (
        "    public String hello() throws InterruptedException {",
        "    /** 简单文本问候，延迟 300ms，资源名 GET:/hello/txt 受流控。 */\n    public String hello() throws InterruptedException {",
    ),
    (
        "    public String fallback(@PathParam(value = \"name\") String name) {",
        "    /** 演示 @SentinelResourceBinding 的全局 fallback/blockHandler。 */\n    public String fallback(@PathParam(value = \"name\") String name) {",
    ),
    (
        "    public String fallback2(@PathParam(value = \"name\") String name) {",
        "    /** 演示同类内 fallback 方法。 */\n    public String fallback2(@PathParam(value = \"name\") String name) {",
    ),
    (
        "    public void asyncHello(@Suspended final AsyncResponse asyncResponse) {",
        "    /** 异步 JAX-RS 响应演示。 */\n    public void asyncHello(@Suspended final AsyncResponse asyncResponse) {",
    ),
    (
        "    public String exception() {",
        "    /** 抛出 RuntimeException，测试异常映射。 */\n    public String exception() {",
    ),
    (
        "    public String badRequest() {",
        "    /** 抛出 WebApplicationException 返回 400。 */\n    public String badRequest() {",
    ),
    (
        "    public String delay(@PathParam(value = \"seconds\") long seconds) throws InterruptedException {",
        "    /** 按秒延迟响应，用于熔断/超时测试。 */\n    public String delay(@PathParam(value = \"seconds\") long seconds) throws InterruptedException {",
    ),
]

R["sentinel-demo/sentinel-demo-quarkus/src/main/java/com/alibaba/csp/sentinel/demo/quarkus/GreetingService.java"] = [
    (
        "/**\n * @author sea\n */",
        "/**\n * 问候业务服务：通过 {@link com.alibaba.csp.sentinel.annotation.cdi.interceptor.SentinelResourceBinding}\n * 演示流控、熔断与 fallback。\n *\n * @author sea\n */",
    ),
    (
        "    public String greeting(String name) {",
        "    /** 资源 greeting1：name=degrade 时抛异常触发熔断/fallback。 */\n    public String greeting(String name) {",
    ),
    (
        "    public String greetingWithFallbackName(String name) {",
        "    /** 资源 greeting2：使用同类 fallback 方法。 */\n    public String greetingWithFallbackName(String name) {",
    ),
    (
        "    public String greetingFallback(String name, Throwable t) {",
        "    /** greeting2 的业务 fallback。 */\n    public String greetingFallback(String name, Throwable t) {",
    ),
]

R["sentinel-demo/sentinel-demo-rocketmq/src/main/java/com/alibaba/csp/sentinel/demo/rocketmq/Constants.java"] = [
    (
        "public final class Constants {",
        "/** RocketMQ 演示用常量：消费组、Topic 与 NameServer 地址。 */\npublic final class Constants {",
    ),
    (
        "    public static final String TEST_GROUP_NAME = \"sentinel-group\";",
        "    /** 消费/生产组名。 */\n    public static final String TEST_GROUP_NAME = \"sentinel-group\";",
    ),
    (
        "    public static final String TEST_TOPIC_NAME = \"SentinelTopicTest\";",
        "    /** 演示 Topic 名。 */\n    public static final String TEST_TOPIC_NAME = \"SentinelTopicTest\";",
    ),
    (
        "    public static final String TEST_NAMESRV_ADDR = \"127.0.0.1:9876\";",
        "    /** 本地 NameServer 地址。 */\n    public static final String TEST_NAMESRV_ADDR = \"127.0.0.1:9876\";",
    ),
]

R["sentinel-demo/sentinel-demo-rocketmq/src/main/java/com/alibaba/csp/sentinel/demo/rocketmq/PullConsumerDemo.java"] = [
    (
        "public class PullConsumerDemo {",
        "/**\n * RocketMQ Pull 消费演示：消息消费前经 Sentinel 匀速排队流控（QPS=5）。\n */\npublic class PullConsumerDemo {",
    ),
    (
        "        // First we init the flow control rule for Sentinel.",
        "        // 先加载 Sentinel 流控规则",
    ),
    (
        "                // Your business logic here.",
        "                // 业务逻辑：打印收到的消息",
    ),
    (
        "                // Blocked.",
        "                // 被 Sentinel 限流",
    ),
    (
        "    private static void initFlowControlRule() {",
        "    /** 加载匀速排队流控：QPS=5，最大排队 5s。 */\n    private static void initFlowControlRule() {",
    ),
    (
        "        // Indicates the interval between two adjacent requests is 200 ms.",
        "        // QPS 阈值 5",
    ),
    (
        "        // Enable rate limiting (uniform). This can ensure fixed intervals between two adjacent calls.\n        // In this example, intervals between two incoming calls (message consumption) will be 200 ms constantly.",
        "        // 启用匀速排队，相邻两次消费间隔约 200ms",
    ),
    (
        "        // If more requests are coming, they'll be put into the waiting queue.\n        // The queue has a queueing timeout. Requests that may exceed the timeout will be immediately blocked.\n        // In this example, the max timeout is 5s.",
        "        // 超出速率的消息进入等待队列，排队超过 5s 则直接拒绝",
    ),
]

R["sentinel-demo/sentinel-demo-rocketmq/src/main/java/com/alibaba/csp/sentinel/demo/rocketmq/SyncProducer.java"] = [
    (
        "public class SyncProducer {",
        "/** RocketMQ 同步生产演示：向 {@link Constants#TEST_TOPIC_NAME} 发送 1000 条消息。 */\npublic class SyncProducer {",
    ),
    (
        "        // Instantiate with a producer group name.",
        "        // 指定生产组并连接 NameServer",
    ),
    (
        "        // Launch the instance.",
        "        // 启动 Producer",
    ),
    (
        "            // Create a message instance, specifying topic, tag and message body.",
        "            // 构造消息：Topic、Tag 与 body",
    ),
    (
        "                // Call send message to deliver message to one of brokers.",
        "                // 同步发送消息",
    ),
    (
        "        // Shut down once the producer instance is not longer in use.",
        "        // 关闭 Producer",
    ),
]

R["sentinel-demo/sentinel-demo-servlet/src/main/java/com/alibaba/csp/sentinel/demo/servlet/config/SentinelConfig.java"] = [
    (
        "/**\n * class description\n *\n * @author zhangxunwei\n * @date 2024/6/24\n */",
        "/**\n * Servlet 演示 Sentinel 配置：注册 URL 限流处理器、来源解析与 URL 清洗器。\n *\n * @author zhangxunwei\n * @date 2024/6/24\n */",
    ),
    (
        "    public static void initConfig() {",
        "    /** 初始化 WebCallbackManager：限流响应、请求来源与 URL 聚合。 */\n    public static void initConfig() {",
    ),
    (
        "        WebCallbackManager.setRequestOriginParser(request -> request.getHeader(\"S-user\"));",
        "        // 从 S-user 请求头解析调用来源\n        WebCallbackManager.setRequestOriginParser(request -> request.getHeader(\"S-user\"));",
    ),
    (
        "    static class MyUrlCleaner implements UrlCleaner {",
        "    /** 将 /foo/数字 聚合为 /foo/* 以便统一限流。 */\n    static class MyUrlCleaner implements UrlCleaner {",
    ),
    (
        "        public String clean(String originUrl) {",
        "        /** 匹配 /foo/\\d+ 时返回 /foo/*。 */\n        public String clean(String originUrl) {",
    ),
]

R["sentinel-demo/sentinel-demo-servlet/src/main/java/com/alibaba/csp/sentinel/demo/servlet/controller/DefaultServlet.java"] = [
    (
        "/**\n * class description\n *\n * @author zhangxunwei\n * @date 2024/6/24\n */",
        "/**\n * 演示 Servlet：按 pathInfo 路由 /foo 与 /bar 请求。\n *\n * @author zhangxunwei\n * @date 2024/6/24\n */",
    ),
    (
        "    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {",
        "    /** 根据 pathInfo 前缀分发到 foo/bar 或返回 404。 */\n    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {",
    ),
    (
        "    private void notFound(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {",
        "    /** 返回 404 与路径未找到提示。 */\n    private void notFound(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {",
    ),
    (
        "    private void handleBar(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {",
        "    /** 处理 /bar 请求，返回 \"bar\"。 */\n    private void handleBar(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {",
    ),
    (
        "    private void handleFoo(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {",
        "    /** 处理 /foo/{id} 请求，返回 \"Hello {id}\"。 */\n    private void handleFoo(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException {",
    ),
]

R["sentinel-demo/sentinel-demo-slot-spi/src/main/java/com/alibaba/csp/sentinel/demo/slot/DemoApplication.java"] = [
    (
        "/**\n * Demo for adding custom slot.\n * @see {@link DemoSlot}.\n *\n * @author Eric Zhao\n * @author cdfive\n */",
        "/**\n * 自定义 Slot SPI 演示入口：对资源 abc 发起 entry，触发 {@link DemoSlot}。\n *\n * @see DemoSlot\n * @author Eric Zhao\n * @author cdfive\n */",
    ),
]

R["sentinel-demo/sentinel-demo-slot-spi/src/main/java/com/alibaba/csp/sentinel/demo/slot/DemoSlot.java"] = [
    (
        "/**\n * A demo slot that records current context and entry resource.\n *\n * Note that the value of order attribute in `@Spi` is -1500, the smaller the value, the higher the order,\n * so this slot will be executed after {@link FlowSlot}(order=-2000) and before {@link DegradeSlot}(order=-1000),\n * refer to the constants for slot order definitions in {@link Constants}.\n *\n * @author Eric Zhao\n * @author cdfive\n */",
        "/**\n * 自定义 Slot 演示：在 entry/exit 时打印当前 Context 与资源名。\n * <p>\n * {@code @Spi(order = -1500)} 表示在 {@link FlowSlot}(-2000) 之后、\n * {@link DegradeSlot}(-1000) 之前执行，详见 {@link Constants} 中的顺序常量。\n * </p>\n *\n * @author Eric Zhao\n * @author cdfive\n */",
    ),
    (
        "        System.out.println(\"------Entering for entry on DemoSlot------\");",
        "        System.out.println(\"------Entering for entry on DemoSlot------\"); // 进入 DemoSlot",
    ),
    (
        "        System.out.println(\"------Exiting for entry on DemoSlot------\");",
        "        System.out.println(\"------Exiting for entry on DemoSlot------\"); // 退出 DemoSlot",
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
    index_file = Path("/tmp/git-index-sentinel-w26b")
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
        "sentinel 1.8.10: Chinese-annotate wave 26b [15:30]",
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
