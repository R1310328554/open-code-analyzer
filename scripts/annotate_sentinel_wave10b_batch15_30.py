#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-10b block [15:30] (reactor/sofa/scg)."""
from __future__ import annotations

import json
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "sentinel/1.8.10"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_LIST = Path("/tmp/sentinel_w10b.txt").read_text(encoding="utf-8").strip().split("\n")

R: dict[str, list[tuple[str, str]]] = {}

R["MonoSentinelOperator.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.5.0\n */",
        "/**\n * 包装 {@link Mono} 的 Sentinel 操作符，在订阅时创建异步 Entry 并委托给 {@link SentinelReactorSubscriber}。\n *\n * @author Eric Zhao\n * @since 1.5.0\n */",
    ),
]

R["ReactorSphU.java"] = [
    (
        "/**\n * A {@link SphU} adapter with Project Reactor.\n *\n * @author Eric Zhao\n * @since 1.5.0\n */",
        "/**\n * 面向 Project Reactor 的 {@link SphU} 适配器，将 {@link Mono} 流与 Sentinel 异步 Entry 绑定。\n *\n * @author Eric Zhao\n * @since 1.5.0\n */",
    ),
    (
        "                    // TODO: check GC friendly?",
        "                    // TODO: 检查对 GC 是否友好？",
    ),
]

R["SentinelReactorConstants.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.5.0\n */",
        "/**\n * Reactor 适配器常量定义。\n *\n * @author Eric Zhao\n * @since 1.5.0\n */",
    ),
]

R["SentinelReactorSubscriber.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.5.0\n */",
        "/**\n * Reactor 订阅者，在响应式流生命周期内管理 Sentinel 异步 Entry 的创建与退出。\n *\n * @author Eric Zhao\n * @since 1.5.0\n */",
    ),
    (
        "            // Provided context is absent, use current context.",
        "            // 未提供上下文时，使用当前上下文。",
    ),
    (
        "            // Run on provided context.",
        "            // 在提供的上下文上执行。",
    ),
    (
        "            // If current we're already in a context, the context config won't work.",
        "            // 若当前已处于上下文中，context 配置将不会生效。",
    ),
    (
        "            // Mark as completed (exited) explicitly.",
        "            // 显式标记 Entry 已完成（已退出）。",
    ),
    (
        "            // Signal cancel and propagate the {@code BlockException}.",
        "            // 发送 cancel 信号并传播 {@code BlockException}。",
    ),
    (
        "            // For some cases of unary operator (Mono), we have to do this during onNext hook.",
        "            // 对于部分一元操作符（Mono）场景，需在 onNext 钩子中提前完成 Entry。",
    ),
    (
        "            // e.g. this kind of order: onSubscribe() -> onNext() -> cancel() -> onComplete()",
        "            // 例如：onSubscribe() -> onNext() -> cancel() -> onComplete()",
    ),
    (
        "            // the onComplete hook will not be executed so we'll need to complete the entry in advance.",
        "            // 此时 onComplete 钩子不会执行，因此需提前完成 Entry。",
    ),
    (
        "        // When flow control triggered or stream terminated, the incoming",
        "        // 流控触发或流终止时，应隐式丢弃后续异常，",
    ),
    (
        "        // deprecated exceptions should be dropped implicitly, so we'll not call the `onErrorDropped` hook.",
        "        // 因此不调用 `onErrorDropped` 钩子。",
    ),
    (
        "            // Normal requests with non-BlockException will go through here.",
        "            // 非 BlockException 的正常请求异常会经过此处。",
    ),
]

R["SentinelReactorTransformer.java"] = [
    (
        "/**\n * A transformer that transforms given {@code Publisher} to a wrapped Sentinel reactor operator.\n *\n * @author Eric Zhao\n * @since 1.5.0\n */",
        "/**\n * 将给定 {@code Publisher} 转换为包装后的 Sentinel Reactor 操作符。\n *\n * @author Eric Zhao\n * @since 1.5.0\n */",
    ),
]

R["AbstractSofaRpcFilter.java"] = [
    (
        "/**\n * @author cdfive\n */",
        "/**\n * SOFARPC Sentinel 过滤器抽象基类，提供启用判断与异常追踪能力。\n *\n * @author cdfive\n */",
    ),
]

R["SentinelConstants.java"] = [
    (
        "/**\n * @author cdfive\n * @since 1.7.2\n */",
        "/**\n * SOFARPC 适配器常量定义。\n *\n * @author cdfive\n * @since 1.7.2\n */",
    ),
]

R["SentinelSofaRpcConsumerFilter.java"] = [
    (
        "/**\n * SOFARPC service consumer filter for Sentinel, auto activated by default.\n *\n * If you want to disable the consumer filter, you can configure:\n * <pre>ConsumerConfig.setParameter(\"sofa.rpc.sentinel.enabled\", \"false\");</pre>\n *\n * or add setting in rpc-config.json:\n * <pre>\"sofa.rpc.sentinel.enabled\": false </pre>\n *\n * @author cdfive\n */",
        "/**\n * Sentinel 集成的 SOFARPC Consumer 过滤器，默认自动激活。\n *\n * 如需禁用 Consumer 过滤器，可配置：\n * <pre>ConsumerConfig.setParameter(\"sofa.rpc.sentinel.enabled\", \"false\");</pre>\n *\n * 或在 rpc-config.json 中添加：\n * <pre>\"sofa.rpc.sentinel.enabled\": false </pre>\n *\n * @author cdfive\n */",
    ),
    (
        "        // Now only support sync invoke.",
        "        // 当前仅支持同步调用。",
    ),
]

R["SentinelSofaRpcProviderFilter.java"] = [
    (
        "/**\n * SOFARPC service provider filter for Sentinel, auto activated by default.\n *\n * If you want to disable the provider filter, you can configure:\n * <pre>ProviderConfig.setParameter(\"sofa.rpc.sentinel.enabled\", \"false\");</pre>\n *\n * or add setting in rpc-config.json file:\n * <pre>\n * {\n *   \"sofa.rpc.sentinel.enabled\": false\n * }\n * </pre>\n *\n * @author cdfive\n */",
        "/**\n * Sentinel 集成的 SOFARPC Provider 过滤器，默认自动激活。\n *\n * 如需禁用 Provider 过滤器，可配置：\n * <pre>ProviderConfig.setParameter(\"sofa.rpc.sentinel.enabled\", \"false\");</pre>\n *\n * 或在 rpc-config.json 中添加：\n * <pre>\n * {\n *   \"sofa.rpc.sentinel.enabled\": false\n * }\n * </pre>\n *\n * @author cdfive\n */",
    ),
    (
        "        // Now only support sync invoke.",
        "        // 当前仅支持同步调用。",
    ),
]

R["SofaRpcUtils.java"] = [
    (
        "/**\n * @author cdfive\n */",
        "/**\n * SOFARPC 适配器工具类，用于构建接口与方法级 Sentinel 资源名。\n *\n * @author cdfive\n */",
    ),
    (
        "    public static String getApplicationName(SofaRequest request) {",
        "    /**\n     * 从请求头获取调用方应用名。\n     *\n     * @param request SOFARPC 请求\n     * @return 应用名，不存在时返回空字符串\n     */\n    public static String getApplicationName(SofaRequest request) {",
    ),
    (
        "    public static String getInterfaceResourceName(SofaRequest request) {",
        "    /**\n     * 获取接口级 Sentinel 资源名。\n     *\n     * @param request SOFARPC 请求\n     * @return 接口资源名\n     */\n    public static String getInterfaceResourceName(SofaRequest request) {",
    ),
    (
        "    public static String getMethodResourceName(SofaRequest request) {",
        "    /**\n     * 获取方法级 Sentinel 资源名，格式为 接口名#方法名(参数签名)。\n     *\n     * @param request SOFARPC 请求\n     * @return 方法资源名\n     */\n    public static String getMethodResourceName(SofaRequest request) {",
    ),
    (
        "    public static Object[] getMethodArguments(SofaRequest request) {",
        "    /**\n     * 获取方法调用参数数组，用于热点参数流控。\n     *\n     * @param request SOFARPC 请求\n     * @return 方法参数数组\n     */\n    public static Object[] getMethodArguments(SofaRequest request) {",
    ),
]

R["DefaultSofaRpcFallback.java"] = [
    (
        "/**\n * Default Sentinel fallback handler for SOFARPC services.\n * Just wrap and throw the exception.\n *\n * @author cdfive\n */",
        "/**\n * SOFARPC 适配器默认降级处理器，将 {@link BlockException} 包装为 {@link SentinelRpcException} 抛出。\n *\n * @author cdfive\n */",
    ),
    (
        "        // Just wrap and throw the exception.",
        "        // 包装并抛出异常。",
    ),
]

R["SofaRpcFallback.java"] = [
    (
        "/**\n * Sentinel fallback handler for SOFARPC services.\n *\n * @author cdfive\n */",
        "/**\n * SOFARPC 适配器降级处理器接口。\n *\n * @author cdfive\n */",
    ),
    (
        "    /**\n     * Handle the block exception and provide fallback result.\n     *\n     * @param invoker FilterInvoker\n     * @param request SofaRequest\n     * @param ex block exception\n     * @return fallback result\n     */",
        "    /**\n     * 处理流控异常并提供降级结果。\n     *\n     * @param invoker FilterInvoker\n     * @param request SofaRequest\n     * @param ex 流控异常\n     * @return 降级结果\n     */",
    ),
]

R["SofaRpcFallbackRegistry.java"] = [
    (
        "/**\n * Global Sentinel fallback registry for SOFARPC services.\n *\n * @author cdfive\n */",
        "/**\n * SOFARPC 适配器全局降级处理器注册表。\n *\n * @author cdfive\n */",
    ),
    (
        "    public static SofaRpcFallback getProviderFallback() {",
        "    /**\n     * 获取 Provider 侧降级处理器。\n     *\n     * @return Provider 降级处理器\n     */\n    public static SofaRpcFallback getProviderFallback() {",
    ),
    (
        "    public static void setProviderFallback(SofaRpcFallback providerFallback) {",
        "    /**\n     * 设置 Provider 侧降级处理器。\n     *\n     * @param providerFallback Provider 降级处理器\n     */\n    public static void setProviderFallback(SofaRpcFallback providerFallback) {",
    ),
    (
        "    public static SofaRpcFallback getConsumerFallback() {",
        "    /**\n     * 获取 Consumer 侧降级处理器。\n     *\n     * @return Consumer 降级处理器\n     */\n    public static SofaRpcFallback getConsumerFallback() {",
    ),
    (
        "    public static void setConsumerFallback(SofaRpcFallback consumerFallback) {",
        "    /**\n     * 设置 Consumer 侧降级处理器。\n     *\n     * @param consumerFallback Consumer 降级处理器\n     */\n    public static void setConsumerFallback(SofaRpcFallback consumerFallback) {",
    ),
]

R["SentinelGatewayFilter.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * Spring Cloud Gateway 全局过滤器，对路由与自定义 API 定义执行 Sentinel 流控。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
]

R["ServerWebExchangeItemParser.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 基于 {@link ServerWebExchange} 的请求属性解析器，供网关热点参数流控使用。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
]


def apply_replacements(rel: str) -> None:
    name = Path(rel).name
    path = ANALYZED / rel
    text = path.read_text(encoding="utf-8")
    for old, new in R.get(name, []):
        if old not in text:
            raise SystemExit(f"MISSING pattern in {rel}: {old[:80]!r}...")
        text = text.replace(old, new, 1)
    if not re.search(r"[\u4e00-\u9fff]", text):
        raise SystemExit(f"No Chinese in {rel} after annotation")
    path.write_text(text, encoding="utf-8")


def update_batch_json() -> None:
    batch = json.loads((QUEUE / "batch.json").read_text(encoding="utf-8"))
    remaining = [f for f in batch["files"] if f not in BATCH_LIST]
    batch["files"] = remaining
    batch["done"] = batch.get("done", 270) + len(BATCH_LIST)
    batch["remaining_pending"] = batch.get("remaining_pending", 665) - len(BATCH_LIST)
    (QUEUE / "batch.json").write_text(json.dumps(batch, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    for rel in BATCH_LIST:
        apply_replacements(rel)
    subprocess.run([
        sys.executable, str(ROOT / "scripts/mark_batch_done.py"),
        "--project", "sentinel", "--version", "1.8.10",
        "--note", "wave10b reactor/sofa/scg [15:30]",
        *BATCH_LIST,
    ], check=True)
    update_batch_json()
    print(f"Annotated {len(BATCH_LIST)} files")


if __name__ == "__main__":
    main()
