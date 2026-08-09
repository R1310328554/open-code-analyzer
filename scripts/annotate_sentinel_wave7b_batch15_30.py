#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-7b block [15:30] (dubbo3/httpclient)."""
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
BATCH_LIST = Path("/tmp/sentinel_w7b.txt").read_text(encoding="utf-8").strip().split("\n")

R: dict[str, list[tuple[str, str]]] = {}

R["DubboOriginParser.java"] = [
    (
        "/**\n * Customized origin parser for Dubbo provider filter.{@link Context#getOrigin()}\n *\n * @author jingzian\n */",
        "/**\n * Dubbo Provider 过滤器的自定义来源解析器，结果写入 {@link Context#getOrigin()}。\n *\n * @author jingzian\n */",
    ),
    (
        "    /**\n     * Parses the origin (caller) from Dubbo invocation.\n     *\n     * @param invoker    Dubbo invoker\n     * @param invocation Dubbo invocation\n     * @return the parsed origin\n     */",
        "    /**\n     * 从 Dubbo 调用中解析来源（调用方）。\n     *\n     * @param invoker    Dubbo invoker\n     * @param invocation Dubbo invocation\n     * @return 解析出的来源标识\n     */",
    ),
]

R["BaseSentinelDubboFilter.java"] = [
    (
        "/**\n * Base class of the {@link SentinelDubboProviderFilter} and {@link SentinelDubboConsumerFilter}.\n *\n * @author Zechao Zheng\n */",
        "/**\n * {@link SentinelDubboProviderFilter} 与 {@link SentinelDubboConsumerFilter} 的基类。\n *\n * @author Zechao Zheng\n */",
    ),
    (
        "    /**\n     * Get method name of dubbo rpc\n     *\n     * @param invoker\n     * @param invocation\n     * @return\n     */",
        "    /**\n     * 获取 Dubbo RPC 方法资源名。\n     *\n     * @param invoker Dubbo invoker\n     * @param invocation Dubbo invocation\n     * @param prefix 资源名前缀\n     * @return 方法资源名\n     */",
    ),
    (
        "    /**\n     * Get interface name of dubbo rpc\n     *\n     * @param invoker\n     * @return\n     */",
        "    /**\n     * 获取 Dubbo RPC 接口资源名。\n     *\n     * @param invoker Dubbo invoker\n     * @param prefix 资源名前缀\n     * @return 接口资源名\n     */",
    ),
]

R["DubboAppContextFilter.java"] = [
    (
        "/**\n * Puts current consumer's application name in the attachment of each invocation.\n *\n * @author Eric Zhao\n */",
        "/**\n * 将当前 Consumer 的应用名写入每次调用的 attachment。\n *\n * @author Eric Zhao\n */",
    ),
]

R["DubboUtils.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Dubbo 适配器工具类，用于解析应用名与 Sentinel 资源名。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "        // 1. try to get application from dubbo context",
        "        // 1. 尝试从 Dubbo 上下文获取应用名",
    ),
    (
        "        // 2. fallback to sentinel application",
        "        // 2. 回退到 Sentinel 写入的应用名",
    ),
]

R["SentinelDubboConsumerFilter.java"] = [
    (
        "/**\n * <p>Dubbo service consumer filter for Sentinel. Auto activated by default.</p>\n * <p>\n * If you want to disable the consumer filter, you can configure:\n * <pre>\n * &lt;dubbo:consumer filter=\"-sentinel.dubbo.consumer.filter\"/&gt;\n * </pre>\n *\n * @author Carpenter Lee\n * @author Eric Zhao\n * @author Lin Liang\n */",
        "/**\n * <p>Sentinel 集成的 Dubbo 服务 Consumer 过滤器，默认自动激活。</p>\n * <p>\n * 如需禁用 Consumer 过滤器，可配置：\n * <pre>\n * &lt;dubbo:consumer filter=\"-sentinel.dubbo.consumer.filter\"/&gt;\n * </pre>\n *\n * @author Carpenter Lee\n * @author Eric Zhao\n * @author Lin Liang\n */",
    ),
]

R["SentinelDubboProviderFilter.java"] = [
    (
        "/**\n * <p>Apache Dubbo service provider filter that enables integration with Sentinel. Auto activated by default.</p>\n * <p>Note: this only works for Apache Dubbo 2.7.x or above version.</p>\n * <p>\n * If you want to disable the provider filter, you can configure:\n * <pre>\n * &lt;dubbo:provider filter=\"-sentinel.dubbo.provider.filter\"/&gt;\n * </pre>\n *\n * @author Carpenter Lee\n * @author Eric Zhao\n */",
        "/**\n * <p>集成 Sentinel 的 Apache Dubbo 服务 Provider 过滤器，默认自动激活。</p>\n * <p>注意：仅适用于 Apache Dubbo 2.7.x 及以上版本。</p>\n * <p>\n * 如需禁用 Provider 过滤器，可配置：\n * <pre>\n * &lt;dubbo:provider filter=\"-sentinel.dubbo.provider.filter\"/&gt;\n * </pre>\n *\n * @author Carpenter Lee\n * @author Eric Zhao\n */",
    ),
    (
        "        // Get origin caller.",
        "        // 获取调用方来源。",
    ),
    (
        "            // Only need to create entrance context at provider side, as context will take effect\n            // at entrance of invocation chain only (for inbound traffic).",
        "            // 仅在 Provider 侧创建入口 Context，Context 仅在调用链入口（入站流量）生效。",
    ),
]

R["DubboAdapterGlobalConfig.java"] = [
    (
        "/**\n * <p>\n * Responsible for dubbo service provider, consumer attribute configuration\n * </p>\n *\n * @author lianglin\n * @since 1.7.0\n */",
        "/**\n * <p>\n * 负责 Dubbo 服务 Provider 与 Consumer 侧的属性配置。\n * </p>\n *\n * @author lianglin\n * @since 1.7.0\n */",
    ),
    (
        "    /**\n     * Get the origin parser of Dubbo adapter.\n     *\n     * @return the origin parser\n     * @since 1.8.0\n     */",
        "    /**\n     * 获取 Dubbo 适配器的来源解析器。\n     *\n     * @return 来源解析器\n     * @since 1.8.0\n     */",
    ),
    (
        "    /**\n     * Set the origin parser of Dubbo adapter.\n     *\n     * @param originParser the origin parser\n     * @since 1.8.0\n     */",
        "    /**\n     * 设置 Dubbo 适配器的来源解析器。\n     *\n     * @param originParser 来源解析器\n     * @since 1.8.0\n     */",
    ),
]

R["DefaultDubboFallback.java"] = [
    (
        "/**\n * @author Eric Zhao\n */",
        "/**\n * Dubbo 阻断时的默认降级处理器，将 {@link BlockException} 包装为运行时异常返回。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "        // Just wrap the exception.",
        "        // 将异常包装后返回。",
    ),
]

R["DubboFallback.java"] = [
    (
        "/**\n * Fallback handler for Dubbo services.\n *\n * @author Eric Zhao\n */",
        "/**\n * Dubbo 服务被 Sentinel 阻断时的降级处理器。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Handle the block exception and provide fallback result.\n     *\n     * @param invoker Dubbo invoker\n     * @param invocation Dubbo invocation\n     * @param ex block exception\n     * @return fallback result\n     */",
        "    /**\n     * 处理阻断异常并返回降级结果。\n     *\n     * @param invoker Dubbo invoker\n     * @param invocation Dubbo invocation\n     * @param ex 阻断异常\n     * @return 降级结果\n     */",
    ),
]

R["DubboFallbackRegistry.java"] = [
    (
        "/**\n * <p>Global fallback registry for Dubbo.</p>\n *\n * @author Eric Zhao\n * @deprecated use {@link DubboAdapterGlobalConfig} instead since 1.8.0.\n */",
        "/**\n * <p>Dubbo 全局降级处理器注册表。</p>\n *\n * @author Eric Zhao\n * @deprecated 自 1.8.0 起请改用 {@link DubboAdapterGlobalConfig}。\n */",
    ),
]

R["DefaultDubboOriginParser.java"] = [
    (
        "/**\n * Default Dubbo origin parser.\n *\n * @author jingzian\n */",
        "/**\n * 默认 Dubbo 来源解析器，从调用 attachment 中读取远程应用名。\n *\n * @author jingzian\n */",
    ),
]

R["SentinelApacheHttpClientBuilder.java"] = [
    (
        "/**\n * @author zhaoyuguang\n */",
        "/**\n * 集成 Sentinel 流控的 Apache HttpClient 构建器，在出站请求执行链中创建资源入口。\n *\n * @author zhaoyuguang\n */",
    ),
]

R["SentinelApacheHttpClientConfig.java"] = [
    (
        "/**\n * @author zhaoyuguang\n */",
        "/**\n * Apache HttpClient Sentinel 适配器配置，包含资源名前缀、提取器与降级处理器。\n *\n * @author zhaoyuguang\n */",
    ),
]

R["ApacheHttpClientResourceExtractor.java"] = [
    (
        "/**\n * @author zhaoyuguang\n */",
        "/**\n * 从 Apache HttpClient 请求中提取 Sentinel 资源名的接口。\n *\n * @author zhaoyuguang\n */",
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
    batch["done"] = batch.get("done", 180) + len(BATCH_LIST)
    batch["remaining_pending"] = batch.get("remaining_pending", 755) - len(BATCH_LIST)
    (QUEUE / "batch.json").write_text(json.dumps(batch, indent=2) + "\n", encoding="utf-8")

def main() -> None:
    for rel in BATCH_LIST:
        apply_replacements(rel)
    subprocess.run([
        sys.executable, str(ROOT / "scripts/mark_batch_done.py"),
        "--project", "sentinel", "--version", "1.8.10",
        "--note", "wave7b dubbo3/httpclient [15:30]",
        *BATCH_LIST,
    ], check=True)
    update_batch_json()
    print(f"Annotated {len(BATCH_LIST)} files")

if __name__ == "__main__":
    main()
