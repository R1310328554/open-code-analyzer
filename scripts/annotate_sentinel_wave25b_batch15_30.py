#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-25b block [15:30] (dubbo, file-rule, etcd, jax-rs demos)."""
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
    for ln in Path("/tmp/sentinel_w25b.txt").read_text(encoding="utf-8").splitlines()
    if ln.strip()
]
SCRIPT_NAME = "annotate_sentinel_wave25b_batch15_30.py"
MARK_NOTE = "wave25b [15:30]"

GUARD_FILES = [
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/TransactionDefinition.java",
    ROOT
    / "springframework/7.0.8/analyzed/spring-tx/src/main/java/org/springframework/transaction/interceptor/RollbackRuleAttribute.java",
    ROOT
    / "rxjava/4.0.0-alpha-21/analyzed/src/main/java/io/reactivex/rxjava4/internal/operators/flowable/FlowableSamplePublisher.java",
]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-demo/sentinel-demo-dubbo/src/main/java/com/alibaba/csp/sentinel/demo/dubbo/demo1/FooProviderBootstrap.java"] = [
    (
        "/**\n * Please add the following VM arguments:\n * <pre>\n * -Djava.net.preferIPv4Stack=true\n * -Dcsp.sentinel.api.port=8720\n * -Dproject.name=dubbo-provider-demo\n * </pre>\n *\n * @author Eric Zhao\n */",
        "/**\n * Alibaba Dubbo Provider 演示（demo1）：启动时加载 sayHello 方法 QPS=10 流控规则。\n * <p>启动前请添加 VM 参数：</p>\n * <pre>\n * -Djava.net.preferIPv4Stack=true\n * -Dcsp.sentinel.api.port=8720\n * -Dproject.name=dubbo-provider-demo\n * </pre>\n *\n * @author Eric Zhao\n */",
    ),
    (
        "        // Users don't need to manually call this method.\n        InitExecutor.doInit();",
        "        // 一般无需手动调用；此处仅为 eager 初始化 Sentinel\n        InitExecutor.doInit();",
    ),
    (
        "    private static void initFlowRule() {",
        "    /** 为 sayHello 方法资源加载 QPS=10 的流控规则。 */\n    private static void initFlowRule() {",
    ),
]

R["sentinel-demo/sentinel-demo-dubbo/src/main/java/com/alibaba/csp/sentinel/demo/dubbo/demo1/FooServiceImpl.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * {@link com.alibaba.csp.sentinel.demo.dubbo.FooService} Provider 实现（demo1）。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    @Override\n    public String sayHello(String name) {",
        "    /** 返回带当前时间的问候语。 */\n    @Override\n    public String sayHello(String name) {",
    ),
    (
        "    @Override\n    public String doAnother() {",
        "    /** 返回当前时间字符串。 */\n    @Override\n    public String doAnother() {",
    ),
]

R["sentinel-demo/sentinel-demo-dubbo/src/main/java/com/alibaba/csp/sentinel/demo/dubbo/demo1/ProviderConfiguration.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Dubbo Provider Spring 配置（demo1）：应用名、组播注册中心与 dubbo 协议。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    @Bean\n    public ApplicationConfig applicationConfig() {",
        "    /** 注册 Dubbo 应用 demo-provider。 */\n    @Bean\n    public ApplicationConfig applicationConfig() {",
    ),
    (
        "    @Bean\n    public RegistryConfig registryConfig() {",
        "    /** 使用组播注册中心 224.5.6.7:1234。 */\n    @Bean\n    public RegistryConfig registryConfig() {",
    ),
    (
        "    @Bean\n    public ProtocolConfig protocolConfig() {",
        "    /** dubbo 协议监听 25758 端口。 */\n    @Bean\n    public ProtocolConfig protocolConfig() {",
    ),
]

R["sentinel-demo/sentinel-demo-dubbo/src/main/java/com/alibaba/csp/sentinel/demo/dubbo/demo2/FooConsumerBootstrap.java"] = [
    (
        "/**\n * Please add the following VM arguments:\n * <pre>\n * -Djava.net.preferIPv4Stack=true\n * -Dcsp.sentinel.api.port=8721\n * -Dproject.name=dubbo-consumer-demo\n * </pre>\n *\n * @author Eric Zhao\n */",
        "/**\n * Alibaba Dubbo Consumer 演示（demo2）：并发线程数流控 + 多线程压测 sayHello/doAnother。\n * <p>启动前请添加 VM 参数：</p>\n * <pre>\n * -Djava.net.preferIPv4Stack=true\n * -Dcsp.sentinel.api.port=8721\n * -Dproject.name=dubbo-consumer-demo\n * </pre>\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    private static void initFlowRule() {",
        "    /** 为 sayHello 方法资源加载并发线程数=5 的流控规则。 */\n    private static void initFlowRule() {",
    ),
    (
        "                } catch (SentinelRpcException ex) {\n                    System.out.println(\"Blocked\");",
        "                } catch (SentinelRpcException ex) {\n                    System.out.println(\"Blocked\"); // 被 Sentinel 限流",
    ),
    (
        "    private static void registerFallback() {",
        "    /** 演示用 Consumer Fallback 注册（本 main 未调用）。 */\n    private static void registerFallback() {",
    ),
    (
        "        // Register fallback handler for consumer.\n        // If you only want to handle degrading, you need to\n        // check the type of BlockException.",
        "        // 注册 Consumer Fallback；若仅处理熔断，需判断 BlockException 类型",
    ),
]

R["sentinel-demo/sentinel-demo-dubbo/src/main/java/com/alibaba/csp/sentinel/demo/dubbo/demo2/FooProviderBootstrap.java"] = [
    (
        "/**\n * Please add the following VM arguments:\n * <pre>\n * -Djava.net.preferIPv4Stack=true\n * -Dcsp.sentinel.api.port=8720\n * -Dproject.name=dubbo-provider-demo\n * </pre>\n *\n * @author Eric Zhao\n */",
        "/**\n * Alibaba Dubbo Provider 演示（demo2）：无本地流控规则，供 Consumer 侧限流测试。\n * <p>启动前请添加 VM 参数：</p>\n * <pre>\n * -Djava.net.preferIPv4Stack=true\n * -Dcsp.sentinel.api.port=8720\n * -Dproject.name=dubbo-provider-demo\n * </pre>\n *\n * @author Eric Zhao\n */",
    ),
    (
        "        // Users don't need to manually call this method.\n        InitExecutor.doInit();",
        "        // 一般无需手动调用；此处仅为 eager 初始化 Sentinel\n        InitExecutor.doInit();",
    ),
]

R["sentinel-demo/sentinel-demo-dubbo/src/main/java/com/alibaba/csp/sentinel/demo/dubbo/demo2/FooServiceImpl.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * {@link com.alibaba.csp.sentinel.demo.dubbo.FooService} Provider 实现（demo2）。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    @Override\n    public String sayHello(String name) {",
        "    /** 返回带当前时间的问候语。 */\n    @Override\n    public String sayHello(String name) {",
    ),
    (
        "    @Override\n    public String doAnother() {",
        "    /** 返回当前时间字符串，Consumer 压测时不受 sayHello 流控影响。 */\n    @Override\n    public String doAnother() {",
    ),
]

R["sentinel-demo/sentinel-demo-dubbo/src/main/java/com/alibaba/csp/sentinel/demo/dubbo/demo2/ProviderConfiguration.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Dubbo Provider Spring 配置（demo2）：扫描 demo2 包下 {@code @Service} 实现。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    @Bean\n    public ProtocolConfig protocolConfig() {",
        "    /** dubbo 协议监听 25758 端口，与 demo1 配置一致。 */\n    @Bean\n    public ProtocolConfig protocolConfig() {",
    ),
]

R["sentinel-demo/sentinel-demo-dynamic-file-rule/src/main/java/com/alibaba/csp/sentinel/demo/file/rule/FileDataSourceDemo.java"] = [
    (
        "/**\n * <p>\n * This Demo shows how to use {@link FileRefreshableDataSource} to read {@link Rule}s from file. The\n * {@link FileRefreshableDataSource} will automatically fetches the backend file every 3 seconds, and\n * inform the listener if the file is updated.\n * </p>",
        "/**\n * <p>\n * 演示使用 {@link FileRefreshableDataSource} 从本地文件读取 {@link Rule}。\n * 数据源每 3 秒轮询文件，变更时通知 {@link PropertyListener}。\n * </p>",
    ),
    (
        " * Each {@link ReadableDataSource} has a {@link SentinelProperty} to hold the deserialized config data.\n * {@link PropertyListener} will listen to the {@link SentinelProperty} instead of the datasource.\n * {@link Converter} is used for telling how to deserialize the data.",
        " * 每个 {@link ReadableDataSource} 持有 {@link SentinelProperty} 保存反序列化后的配置；\n * {@link PropertyListener} 监听 Property 而非数据源本身；\n * {@link Converter} 负责 JSON 反序列化。",
    ),
    (
        " * {@link FlowRuleManager#register2Property(SentinelProperty)},\n * {@link DegradeRuleManager#register2Property(SentinelProperty)},\n * {@link SystemRuleManager#register2Property(SentinelProperty)} could be called for listening the\n * {@link Rule}s change.",
        " * 可调用 {@link FlowRuleManager#register2Property}、\n * {@link DegradeRuleManager#register2Property}、\n * {@link SystemRuleManager#register2Property} 监听规则热更新。",
    ),
    (
        " * For other kinds of data source, such as <a href=\"https://github.com/alibaba/nacos\">Nacos</a>,\n * Zookeeper, Git, or even CSV file, We could implement {@link ReadableDataSource} interface to read these\n * configs.",
        " * 其他数据源（如 <a href=\"https://github.com/alibaba/nacos\">Nacos</a>、Zookeeper、Git、CSV）\n * 可实现 {@link ReadableDataSource} 接口接入。",
    ),
    (
        "        /*\n         * Start to require tokens, rate will be limited by rule in FlowRule.json\n         */",
        "        /* 启动 QPS 压测，速率由 FlowRule.json 中的规则限制 */",
    ),
    (
        "        // Data source for FlowRule",
        "        // 流控规则文件数据源",
    ),
    (
        "        // Data source for DegradeRule",
        "        // 熔断规则文件数据源",
    ),
    (
        "        // Data source for SystemRule",
        "        // 系统保护规则文件数据源",
    ),
]

R["sentinel-demo/sentinel-demo-dynamic-file-rule/src/main/java/com/alibaba/csp/sentinel/demo/file/rule/FileDataSourceInit.java"] = [
    (
        "/**\n * <p>\n * A sample showing how to register readable and writable data source via Sentinel init SPI mechanism.\n * </p>",
        "/**\n * <p>\n * 演示通过 Sentinel InitFunc SPI 注册可读/可写文件数据源。\n * </p>",
    ),
    (
        " * To activate this, you can add the class name to `com.alibaba.csp.sentinel.init.InitFunc` file\n * in `META-INF/services/` directory of the resource directory. Then the data source will be automatically\n * registered during the initialization of Sentinel.",
        " * 在 `META-INF/services/com.alibaba.csp.sentinel.init.InitFunc` 中注册本类名，\n * Sentinel 初始化时会自动加载数据源。",
    ),
    (
        "        // A fake path.",
        "        // 演示用路径：~/sentinel/rules/flowRule.json",
    ),
    (
        "        // Register to flow rule manager.",
        "        // 注册到 FlowRuleManager，支持文件热更新",
    ),
    (
        "        // Register to writable data source registry so that rules can be updated to file\n        // when there are rules pushed from the Sentinel Dashboard.",
        "        // 注册可写数据源，Dashboard 推送规则时可写回本地文件",
    ),
]

R["sentinel-demo/sentinel-demo-dynamic-file-rule/src/main/java/com/alibaba/csp/sentinel/demo/file/rule/FlowQpsRunner.java"] = [
    (
        "/**\n * Flow Rule demo.\n *\n * @author Carpenter Lee\n */",
        "/**\n * 文件动态规则演示用的 QPS 压测 Runner：\n * 对资源 abc 持续 {@link SphU#entry} 并每秒统计 pass/block。\n *\n * @author Carpenter Lee\n */",
    ),
    (
        "    public void simulateTraffic() {",
        "    /** 启动压测线程，随机间隔 0–50ms 发起 entry 请求。 */\n    public void simulateTraffic() {",
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
    (
        "            System.out.println(\"begin to statistic!!!\");",
        "            System.out.println(\"begin to statistic!!!\"); // 开始每秒统计",
    ),
]

R["sentinel-demo/sentinel-demo-dynamic-file-rule/src/main/java/com/alibaba/csp/sentinel/demo/file/rule/JarFileDataSourceDemo.java"] = [
    (
        "/**\n * <p>\n * This Demo shows how to use {@link FileInJarReadableDataSource} to read {@link Rule}s from jarfile. The\n * {@link FileInJarReadableDataSource} will automatically fetches the backend file every 3 seconds, and\n * inform the listener if the file is updated.\n * </p>",
        "/**\n * <p>\n * 演示使用 {@link FileInJarReadableDataSource} 从 JAR 内读取 {@link Rule}。\n * 数据源每 3 秒轮询 JAR 内文件，变更时通知监听器。\n * </p>",
    ),
    (
        " * Each {@link ReadableDataSource} has a {@link SentinelProperty} to hold the deserialized config data.\n * {@link PropertyListener} will listen to the {@link SentinelProperty} instead of the datasource.\n * {@link Converter} is used for telling how to deserialize the data.",
        " * 每个 {@link ReadableDataSource} 持有 {@link SentinelProperty}；\n * {@link PropertyListener} 监听 Property；{@link Converter} 负责反序列化。",
    ),
    (
        "        // Start to require tokens, rate will be limited by rule of FlowRule.json in jar.",
        "        // 启动压测，速率由 JAR 内 FlowRule.json 限制",
    ),
    (
        "        // Modify the path with your real path.",
        "        // 请按实际构建产物路径修改 jarPath",
    ),
    (
        "        // eg: if flowRuleInJarName full path is 'sentinel-demo-dynamic-file-rule.jar!/classes/FlowRule.json',\n        // your flowRuleInJarName is 'classes/FlowRule.json'",
        "        // 例：完整路径为 jar!/classes/FlowRule.json 时，flowRuleInJarPath 填 classes/FlowRule.json",
    ),
]

R["sentinel-demo/sentinel-demo-etcd-datasource/src/main/java/com/alibaba/csp/sentinel/demo/datasource/etcd/EtcdConfigSender.java"] = [
    (
        "/**\n * Etcd config sender for demo.\n *\n * @author lianglin\n * @since 1.7.0\n */",
        "/**\n * Etcd 规则写入工具：向 etcd 写入 sentinel_demo_rule_key 流控规则 JSON，\n * 供 {@link EtcdDataSourceDemo} 读取验证。\n *\n * @author lianglin\n * @since 1.7.0\n */",
    ),
    (
        "        System.out.println(\"setting rule success\");",
        "        System.out.println(\"setting rule success\"); // 规则写入成功",
    ),
]

R["sentinel-demo/sentinel-demo-etcd-datasource/src/main/java/com/alibaba/csp/sentinel/demo/datasource/etcd/EtcdDataSourceDemo.java"] = [
    (
        "/**\n * @author lianglin\n * @since 1.7.0\n */",
        "/**\n * Etcd 动态数据源演示：从 etcd 读取流控规则并注册到 {@link FlowRuleManager}。\n *\n * @author lianglin\n * @since 1.7.0\n */",
    ),
    (
        "        ReadableDataSource<String, List<FlowRule>> flowRuleEtcdDataSource = new EtcdDataSource<>(rule_key, (rule) -> JSON.parseArray(rule, FlowRule.class));",
        "        // 创建 EtcdDataSource，key 为 sentinel_demo_rule_key\n        ReadableDataSource<String, List<FlowRule>> flowRuleEtcdDataSource = new EtcdDataSource<>(rule_key, (rule) -> JSON.parseArray(rule, FlowRule.class));",
    ),
    (
        "        FlowRuleManager.register2Property(flowRuleEtcdDataSource.getProperty());",
        "        // 注册 Property，etcd 规则变更时自动热更新\n        FlowRuleManager.register2Property(flowRuleEtcdDataSource.getProperty());",
    ),
]

R["sentinel-demo/sentinel-demo-jax-rs/src/main/java/com/alibaba/csp/sentinel/demo/jaxrs/CustomExceptionMapper.java"] = [
    (
        "/**\n * @author sea\n */",
        "/**\n * JAX-RS 全局异常映射：将未捕获异常转为 HTTP 500 与固定错误消息。\n *\n * @author sea\n */",
    ),
    (
        "    @Override\n    public Response toResponse(Throwable exception) {",
        "    /** 返回 500 状态与 \"Unknown Server Error\" 响应体。 */\n    @Override\n    public Response toResponse(Throwable exception) {",
    ),
]

R["sentinel-demo/sentinel-demo-jax-rs/src/main/java/com/alibaba/csp/sentinel/demo/jaxrs/HelloEntity.java"] = [
    (
        "/**\n * @author sea\n */",
        "/**\n * JAX-RS 演示用简单 POJO：携带 id 与 msg 字段。\n *\n * @author sea\n */",
    ),
    (
        "    public HelloEntity() {",
        "    /** 无参构造。 */\n    public HelloEntity() {",
    ),
    (
        "    public HelloEntity(String msg) {",
        "    /** 仅设置 msg 的构造。 */\n    public HelloEntity(String msg) {",
    ),
    (
        "    public HelloEntity(Long id, String msg) {",
        "    /** 设置 id 与 msg 的构造。 */\n    public HelloEntity(Long id, String msg) {",
    ),
    (
        "    public Long getId() {",
        "    /** 返回实体 id。 */\n    public Long getId() {",
    ),
    (
        "    public void setId(Long id) {",
        "    /** 设置实体 id。 */\n    public void setId(Long id) {",
    ),
    (
        "    public String getMsg() {",
        "    /** 返回消息内容。 */\n    public String getMsg() {",
    ),
    (
        "    public void setMsg(String msg) {",
        "    /** 设置消息内容。 */\n    public void setMsg(String msg) {",
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
    index_file = Path("/tmp/git-index-sentinel-w25b")
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
        "sentinel 1.8.10: Chinese-annotate wave 25b [15:30]",
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
