#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-24a block [0:15] (annotation-aop, apache-dubbo, httpclient, apollo demos)."""
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
    for ln in Path("/tmp/sentinel_w24a.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
SCRIPT_NAME = "annotate_sentinel_wave24a_batch0_15.py"
MARK_NOTE = "wave24a [0:15]"

GUARD_FILES = [
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-demo/sentinel-demo-annotation-spring-aop/src/main/java/com/alibaba/csp/sentinel/demo/annotation/aop/controller/DemoController.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Spring AOP 注解限流演示 REST 控制器：暴露 /foo、/bar、/baz 等测试接口。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    @GetMapping(\"/foo\")\n    public String apiFoo(@RequestParam(required = false) Long t) {",
        "    /** GET /foo：调用 test 与 hello(long)，未传 t 时使用当前时间戳。 */\n    @GetMapping(\"/foo\")\n    public String apiFoo(@RequestParam(required = false) Long t) {",
    ),
    (
        "    @GetMapping(\"/bar\")\n    public String apiBar(@RequestParam(required = false) String t) {",
        "    /** GET /bar：调用 test 与 hello(String)。 */\n    @GetMapping(\"/bar\")\n    public String apiBar(@RequestParam(required = false) String t) {",
    ),
    (
        "    @GetMapping(\"/baz/{name}\")\n    public String apiBaz(@PathVariable(\"name\") String name) {",
        "    /** GET /baz/{name}：调用 helloAnother 演示 defaultFallback 与 exceptionsToIgnore。 */\n    @GetMapping(\"/baz/{name}\")\n    public String apiBaz(@PathVariable(\"name\") String name) {",
    ),
]

R["sentinel-demo/sentinel-demo-annotation-spring-aop/src/main/java/com/alibaba/csp/sentinel/demo/annotation/aop/service/ExceptionUtil.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * {@link SentinelResource#blockHandler()} 演示用的 BlockException 处理工具类。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    public static void handleException(BlockException ex) {\n        // Handler method that handles BlockException when blocked.\n        // The method parameter list should match original method, with the last additional\n        // parameter with type BlockException. The return type should be same as the original method.\n        // The block handler method should be located in the same class with original method by default.\n        // If you want to use method in other classes, you can set the blockHandlerClass\n        // with corresponding Class (Note the method in other classes must be static).",
        "    /**\n     * 被限流/熔断时调用的 blockHandler：参数列表须与原方法一致，末尾追加 BlockException。\n     * 默认须与原方法同 class；跨类时通过 blockHandlerClass 指定且方法须为 static。\n     */\n    public static void handleException(BlockException ex) {",
    ),
]

R["sentinel-demo/sentinel-demo-annotation-spring-aop/src/main/java/com/alibaba/csp/sentinel/demo/annotation/aop/service/TestService.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * 注解限流演示服务接口：覆盖 blockHandler、fallback 与 defaultFallback 场景。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    void test();",
        "    /** 无返回值资源，演示 blockHandlerClass 跨类处理。 */\n    void test();",
    ),
    (
        "    String hello(long s);",
        "    /** 按 long 参数问候，演示 fallback。 */\n    String hello(long s);",
    ),
    (
        "    String hello(String s);",
        "    /** 按 String 参数问候，演示同名 fallback 重载。 */\n    String hello(String s);",
    ),
    (
        "    String helloAnother(String name);",
        "    /** 演示 defaultFallback 与 exceptionsToIgnore。 */\n    String helloAnother(String name);",
    ),
]

R["sentinel-demo/sentinel-demo-annotation-spring-aop/src/main/java/com/alibaba/csp/sentinel/demo/annotation/aop/service/TestServiceImpl.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * {@link TestService} 实现：展示 {@link SentinelResource} 注解的多种降级策略。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    @Override\n    @SentinelResource(value = \"test\", blockHandler = \"handleException\", blockHandlerClass = {ExceptionUtil.class})\n    public void test() {",
        "    /** 资源 test：限流时委托 {@link ExceptionUtil#handleException} 处理。 */\n    @Override\n    @SentinelResource(value = \"test\", blockHandler = \"handleException\", blockHandlerClass = {ExceptionUtil.class})\n    public void test() {",
    ),
    (
        "    @Override\n    @SentinelResource(value = \"hello\", fallback = \"helloFallback\")\n    public String hello(long s) {",
        "    /** 资源 hello：异常或限流时调用 helloFallback(long, Throwable)。 */\n    @Override\n    @SentinelResource(value = \"hello\", fallback = \"helloFallback\")\n    public String hello(long s) {",
    ),
    (
        "    @Override\n    @SentinelResource(value = \"helloStr\", fallback = \"helloFallback\")\n    public String hello(String s) {",
        "    /** 资源 helloStr：与 hello(long) 共用 fallback 方法名但参数签名不同。 */\n    @Override\n    @SentinelResource(value = \"helloStr\", fallback = \"helloFallback\")\n    public String hello(String s) {",
    ),
    (
        "    @Override\n    @SentinelResource(value = \"helloAnother\", defaultFallback = \"defaultFallback\",\n        exceptionsToIgnore = {IllegalStateException.class})\n    public String helloAnother(String name) {",
        "    /** 资源 helloAnother：IllegalStateException 不触发降级，其余异常走 defaultFallback。 */\n    @Override\n    @SentinelResource(value = \"helloAnother\", defaultFallback = \"defaultFallback\",\n        exceptionsToIgnore = {IllegalStateException.class})\n    public String helloAnother(String name) {",
    ),
    (
        "    public String helloFallback(long s, Throwable ex) {\n        // Do some log here.",
        "    /** hello(long) 的 fallback：打印异常并返回友好提示。 */\n    public String helloFallback(long s, Throwable ex) {",
    ),
    (
        "    private String helloFallback(String ignored, Throwable e) {\n        // Do some log here.",
        "    /** hello(String) 的 fallback（private 亦可被 Sentinel 反射调用）。 */\n    private String helloFallback(String ignored, Throwable e) {",
    ),
    (
        "    public String defaultFallback() {",
        "    /** helloAnother 的默认降级返回值。 */\n    public String defaultFallback() {",
    ),
]

R["sentinel-demo/sentinel-demo-apache-dubbo/src/main/java/com/alibaba/csp/sentinel/demo/apache/dubbo/FooConsumerBootstrap.java"] = [
    (
        "/**\n * Please add the following VM arguments:\n * <pre>\n * -Djava.net.preferIPv4Stack=true\n * -Dcsp.sentinel.api.port=8721\n * -Dproject.name=dubbo-consumer-demo\n * </pre>\n *\n * @author Eric Zhao\n */",
        "/**\n * Apache Dubbo 消费者启动类：演示接口/方法级流控与多种 Consumer Fallback。\n * <p>启动前请添加 VM 参数：</p>\n * <pre>\n * -Djava.net.preferIPv4Stack=true\n * -Dcsp.sentinel.api.port=8721\n * -Dproject.name=dubbo-consumer-demo\n * </pre>\n *\n * @author Eric Zhao\n */",
    ),
    (
        "        // method flowcontrol",
        "        // 方法级流控：在接口 QPS 20 基础上对 sayHello 方法单独限 5 QPS",
    ),
    (
        "        // fallback to result",
        "        // 注册返回固定字符串的 Consumer Fallback",
    ),
    (
        "        // fallback to exception",
        "        // 注册返回 RuntimeException 的 Consumer Fallback",
    ),
    (
        "    public static void registryCustomFallback() {",
        "    /** 注册全局 Consumer Fallback：限流时返回 AsyncRpcResult(\"fallback\")。 */\n    public static void registryCustomFallback() {",
    ),
    (
        "    public static void registryCustomFallbackForCustomException() {",
        "    /** 注册 Fallback：限流时将 RuntimeException 包装进 AsyncRpcResult。 */\n    public static void registryCustomFallbackForCustomException() {",
    ),
    (
        "    public static void registryCustomFallbackWhenFallbackError() {",
        "    /** 注册会自身抛错的 Fallback，演示 Fallback 失败时的行为。 */\n    public static void registryCustomFallbackWhenFallbackError() {",
    ),
    (
        "    private static void initFlowRule(int interfaceFlowLimit, boolean method) {",
        "    /** 加载接口级流控规则；method 为 true 时额外加载 sayHello 方法级规则。 */\n    private static void initFlowRule(int interfaceFlowLimit, boolean method) {",
    ),
]

R["sentinel-demo/sentinel-demo-apache-dubbo/src/main/java/com/alibaba/csp/sentinel/demo/apache/dubbo/FooConsumerExceptionDegradeBootstrap.java"] = [
    (
        "/**\n * Please add the following VM arguments:\n * <pre>\n * -Djava.net.preferIPv4Stack=true\n * -Dcsp.sentinel.api.port=8721\n * -Dproject.name=dubbo-consumer-demo\n * </pre>\n *\n * @author Zechao zheng\n */",
        "/**\n * Dubbo 消费者异常比例熔断演示：结合 DegradeRule 与异步调用超时触发降级。\n * <p>启动前请添加 VM 参数：</p>\n * <pre>\n * -Djava.net.preferIPv4Stack=true\n * -Dcsp.sentinel.api.port=8721\n * -Dproject.name=dubbo-consumer-demo\n * </pre>\n *\n * @author Zechao zheng\n */",
    ),
    (
        "        // sleep 3s to skip the time window",
        "        // 休眠 3 秒以跳过熔断统计窗口，再测试超时场景",
    ),
    (
        "            // timeout to trigger the fallback",
        "            // 异步调用超时以触发熔断与 Fallback",
    ),
    (
        "    public static void registryCustomFallback() {",
        "    /** 注册 Consumer Fallback：熔断/限流时返回 \"fallback\" 字符串。 */\n    public static void registryCustomFallback() {",
    ),
    (
        "    public static void initExceptionFallback(int timewindow) {",
        "    /** 加载异常比例熔断规则：比例阈值 0.5，统计窗口 timewindow 秒。 */\n    public static void initExceptionFallback(int timewindow) {",
    ),
]

R["sentinel-demo/sentinel-demo-apache-dubbo/src/main/java/com/alibaba/csp/sentinel/demo/apache/dubbo/FooProviderBootstrap.java"] = [
    (
        "/**\n * Provider demo for Apache Dubbo 2.7.x or above. Please add the following VM arguments:\n * <pre>\n * -Djava.net.preferIPv4Stack=true\n * -Dcsp.sentinel.api.port=8720\n * -Dproject.name=dubbo-provider-demo\n * </pre>\n *\n * @author Eric Zhao\n */",
        "/**\n * Apache Dubbo 2.7+ 服务提供者启动类。\n * <p>启动前请添加 VM 参数：</p>\n * <pre>\n * -Djava.net.preferIPv4Stack=true\n * -Dcsp.sentinel.api.port=8720\n * -Dproject.name=dubbo-provider-demo\n * </pre>\n *\n * @author Eric Zhao\n */",
    ),
    (
        "        // Users don't need to manually call this method.\n        // Only for eager initialization.",
        "        // 一般无需手动调用；此处仅为 eager 初始化 Sentinel 组件",
    ),
]

R["sentinel-demo/sentinel-demo-apache-dubbo/src/main/java/com/alibaba/csp/sentinel/demo/apache/dubbo/FooService.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Dubbo 演示 RPC 服务接口。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    String sayHello(String name);",
        "    /** 问候调用，作为流控/熔断主要测试资源。 */\n    String sayHello(String name);",
    ),
    (
        "    String doAnother();",
        "    /** 备用 RPC 方法，Consumer 被限流时可作为降级调用。 */\n    String doAnother();",
    ),
    (
        "    String exceptionTest(boolean biz, boolean timeout);",
        "    /** 异常/超时测试：biz 为 true 抛业务异常，timeout 为 true 模拟慢调用。 */\n    String exceptionTest(boolean biz, boolean timeout);",
    ),
]

R["sentinel-demo/sentinel-demo-apache-dubbo/src/main/java/com/alibaba/csp/sentinel/demo/apache/dubbo/consumer/ConsumerConfiguration.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Dubbo 消费者 Spring 配置：应用名、组播注册中心与 Sentinel Filter。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "        // Uncomment below line if you don't want to enable Sentinel for Dubbo service consumers.",
        "        // 若不想为 Dubbo Consumer 启用 Sentinel，可取消下行注释",
    ),
    (
        "    @Bean\n    public FooServiceConsumer annotationDemoServiceConsumer() {",
        "    /** 注册演示用 Consumer 包装 Bean。 */\n    @Bean\n    public FooServiceConsumer annotationDemoServiceConsumer() {",
    ),
]

R["sentinel-demo/sentinel-demo-apache-dubbo/src/main/java/com/alibaba/csp/sentinel/demo/apache/dubbo/consumer/FooServiceConsumer.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * {@link FooService} 消费者封装：通过 {@link Reference} 直连 Provider。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    @Reference(url = \"dubbo://127.0.0.1:25758\", timeout = 500)\n    private FooService fooService;",
        "    /** 直连本地 Provider，超时 500ms。 */\n    @Reference(url = \"dubbo://127.0.0.1:25758\", timeout = 500)\n    private FooService fooService;",
    ),
    (
        "    public String sayHello(String name) {",
        "    /** 转发 sayHello RPC 调用。 */\n    public String sayHello(String name) {",
    ),
    (
        "    public String doAnother() {",
        "    /** 转发 doAnother RPC 调用。 */\n    public String doAnother() {",
    ),
    (
        "    public String exceptionTest(boolean biz, boolean timeout) {",
        "    /** 转发 exceptionTest RPC 调用。 */\n    public String exceptionTest(boolean biz, boolean timeout) {",
    ),
]

R["sentinel-demo/sentinel-demo-apache-dubbo/src/main/java/com/alibaba/csp/sentinel/demo/apache/dubbo/provider/FooServiceImpl.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * {@link FooService} Provider 实现：供 Consumer 流控/熔断演示调用。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    @Override\n    public String sayHello(String name) {",
        "    /** 返回带当前时间的问候语。 */\n    @Override\n    public String sayHello(String name) {",
    ),
    (
        "    @Override\n    public String doAnother() {",
        "    /** 返回当前时间字符串，用作降级备用结果。 */\n    @Override\n    public String doAnother() {",
    ),
    (
        "    @Override\n    public String exceptionTest(boolean biz, boolean timeout) {",
        "    /** biz 抛 RuntimeException；timeout 睡眠 2 秒模拟慢调用。 */\n    @Override\n    public String exceptionTest(boolean biz, boolean timeout) {",
    ),
]

R["sentinel-demo/sentinel-demo-apache-dubbo/src/main/java/com/alibaba/csp/sentinel/demo/apache/dubbo/provider/ProviderConfiguration.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Dubbo Provider Spring 配置：应用、组播注册中心与 dubbo 协议端口。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    @Bean\n    public ProtocolConfig protocolConfig() {",
        "    /** dubbo 协议监听 25758 端口，与 Consumer 直连地址一致。 */\n    @Bean\n    public ProtocolConfig protocolConfig() {",
    ),
]

R["sentinel-demo/sentinel-demo-apache-httpclient/src/main/java/com/alibaba/csp/sentinel/demo/apache/httpclient/ApacheHttpClientDemoApplication.java"] = [
    (
        "/**\n * @author zhaoyuguang\n */",
        "/**\n * Sentinel Apache HttpClient 适配器 Spring Boot 演示入口。\n *\n * @author zhaoyuguang\n */",
    ),
    (
        "    @Override\n    public void run(String... args) {",
        "    /** 启动后无额外逻辑，HTTP 测试由 Controller 接口触发。 */\n    @Override\n    public void run(String... args) {",
    ),
]

R["sentinel-demo/sentinel-demo-apache-httpclient/src/main/java/com/alibaba/csp/sentinel/demo/apache/httpclient/controller/ApacheHttpClientTestController.java"] = [
    (
        "/**\n * @author zhaoyuguang\n */",
        "/**\n * Apache HttpClient + Sentinel 出站调用演示控制器。\n *\n * @author zhaoyuguang\n */",
    ),
    (
        "    @RequestMapping(\"/httpclient/back\")\n    public String back() {",
        "    /** 被调用的简单回显接口。 */\n    @RequestMapping(\"/httpclient/back\")\n    public String back() {",
    ),
    (
        "    @RequestMapping(\"/httpclient/back/{id}\")\n    public String back(@PathVariable String id) {",
        "    /** 带路径变量的回显接口，用于资源名归一化演示。 */\n    @RequestMapping(\"/httpclient/back/{id}\")\n    public String back(@PathVariable String id) {",
    ),
    (
        "    @RequestMapping(\"/httpclient/sync\")\n    public String sync() throws Exception {",
        "    /** 同步 HttpClient 调用 /httpclient/back，自定义资源提取器将 URI 模板化。 */\n    @RequestMapping(\"/httpclient/sync\")\n    public String sync() throws Exception {",
    ),
    (
        "            @Override\n            public String extractor(HttpRequestWrapper request) {",
        "            /** 将 /httpclient/back/{id} 归一化为 METHOD:/httpclient/back/{id} 作为 Sentinel 资源名。 */\n            @Override\n            public String extractor(HttpRequestWrapper request) {",
    ),
    (
        "    @RequestMapping(\"/httpclient/sync/{id}\")\n    public String sync(@PathVariable String id) throws Exception {",
        "    /** 同步调用带 id 的回显接口，验证路径参数资源归一化。 */\n    @RequestMapping(\"/httpclient/sync/{id}\")\n    public String sync(@PathVariable String id) throws Exception {",
    ),
    (
        "    private String getRemoteString(CloseableHttpClient httpclient, HttpGet httpGet) throws IOException {",
        "    /** 执行 GET 请求并读取 UTF-8 响应体，最后关闭 client。 */\n    private String getRemoteString(CloseableHttpClient httpclient, HttpGet httpGet) throws IOException {",
    ),
]

R["sentinel-demo/sentinel-demo-apollo-datasource/src/main/java/com/alibaba/csp/sentinel/demo/datasource/apollo/ApolloDataSourceDemo.java"] = [
    (
        "/**\n * This demo shows how to use Apollo as the data source of Sentinel rules.\n * <br />\n * You need to first set up data as follows:\n * <ol>\n *  <li>Create an application with app id as sentinel-demo in Apollo</li>\n *  <li>\n *    Create a configuration with key as flowRules and value as follows:\n *    <pre>\n *      [\n          {\n            \"resource\": \"TestResource\",\n            \"controlBehavior\": 0,\n            \"count\": 5.0,\n            \"grade\": 1,\n            \"limitApp\": \"default\",\n            \"strategy\": 0\n          }\n        ]\n *    </pre>\n *  </li>\n *  <li>Publish the application namespace</li>\n * </ol>\n * Then you could start this demo and adjust the rule configuration as you wish.\n * The rule changes will take effect in real time.\n *\n * @author Jason Song\n */",
        "/**\n * 演示以 Apollo 作为 Sentinel 规则动态数据源。\n * <p>使用前请在 Apollo 中完成以下配置：</p>\n * <ol>\n *  <li>创建 appId 为 sentinel-demo 的应用</li>\n *  <li>\n *    新增 key 为 flowRules 的配置，值为：\n *    <pre>\n *      [\n          {\n            \"resource\": \"TestResource\",\n            \"controlBehavior\": 0,\n            \"count\": 5.0,\n            \"grade\": 1,\n            \"limitApp\": \"default\",\n            \"strategy\": 0\n          }\n        ]\n *    </pre>\n *  </li>\n *  <li>发布 application 命名空间</li>\n * </ol>\n * 启动后可实时修改 Apollo 中的规则，变更会立即生效。\n *\n * @author Jason Song\n */",
    ),
    (
        "        // Assume we config: resource is `TestResource`, initial QPS threshold is 5.",
        "        // 假定 Apollo 中 resource=TestResource、初始 QPS 阈值为 5",
    ),
    (
        "        // Set up basic information, only for demo purpose. You may adjust them based on your actual environment.\n        // For more information, please refer https://github.com/ctripcorp/apollo",
        "        // 演示用 Apollo 连接参数，生产环境请按实际部署调整，详见 https://github.com/ctripcorp/apollo",
    ),
    (
        "        // It's better to provide a meaningful default value.",
        "        // 建议提供有意义的默认规则 JSON，避免 Apollo 无配置时解析失败",
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
    index_file = Path("/tmp/git-index-sentinel-w24a")
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
        "sentinel 1.8.10: Chinese-annotate wave 24a [0:15]",
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
