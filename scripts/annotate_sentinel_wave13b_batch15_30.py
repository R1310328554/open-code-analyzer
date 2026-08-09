#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-13b block [15:30] (zuul/zuul2)."""
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
BATCH_LIST = Path("/tmp/sentinel_w13b.txt").read_text(encoding="utf-8").strip().split("\n")

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-adapter/sentinel-zuul-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul/api/route/ZuulRouteMatchers.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * Zuul 路由路径匹配谓词工厂，提供全匹配、Ant、精确与正则四种匹配方式。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
]

R["sentinel-adapter/sentinel-zuul-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul/callback/DefaultRequestOriginParser.java"] = [
    (
        "/**\n * @author tiger\n */",
        "/**\n * {@link RequestOriginParser} 的默认实现，返回空字符串作为请求来源。\n *\n * @author tiger\n */",
    ),
]

R["sentinel-adapter/sentinel-zuul-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul/callback/RequestOriginParser.java"] = [
    (
        "/**\n * The origin parser parses request origin (e.g. IP, user, appName) from HTTP request.\n *\n * @author tiger\n */",
        "/**\n * 请求来源解析器，从 HTTP 请求中解析来源标识（如 IP、用户、应用名等）。\n *\n * @author tiger\n */",
    ),
    (
        "    /**\n     * Parse the origin from given HTTP request.\n     *\n     * @param request HTTP request\n     * @return parsed origin\n     */",
        "    /**\n     * 从给定 HTTP 请求中解析请求来源。\n     *\n     * @param request HTTP request\n     * @return parsed origin\n     */",
    ),
]

R["sentinel-adapter/sentinel-zuul-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul/callback/ZuulGatewayCallbackManager.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * Zuul 网关回调管理器，统一管理请求来源解析器。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
]

R["sentinel-adapter/sentinel-zuul-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul/constants/ZuulConstant.java"] = [
    (
        "/**\n * @author tiger\n */",
        "/**\n * Zuul 适配器常量定义，包含 {@link com.netflix.zuul.context.RequestContext} 键名与过滤器类型。\n *\n * @author tiger\n */",
    ),
    (
        "    /**\n     * Zuul {@link com.netflix.zuul.context.RequestContext} key for use in load balancer.\n     */",
        "    /**\n     * 负载均衡使用的 Zuul {@link com.netflix.zuul.context.RequestContext} 键（服务 ID）。\n     */",
    ),
    (
        "    /**\n     * Zuul {@link com.netflix.zuul.context.RequestContext} key for proxying (route ID).\n     */",
        "    /**\n     * 代理路由使用的 Zuul {@link com.netflix.zuul.context.RequestContext} 键（路由 ID）。\n     */",
    ),
    (
        "    /**\n     * {@link ZuulFilter#filterType()} error type.\n     */",
        "    /**\n     * {@link ZuulFilter#filterType()} 错误类型。\n     */",
    ),
    (
        "    /**\n     * {@link ZuulFilter#filterType()} post type.\n     */",
        "    /**\n     * {@link ZuulFilter#filterType()} 后置类型。\n     */",
    ),
    (
        "    /**\n     * {@link ZuulFilter#filterType()} pre type.\n     */",
        "    /**\n     * {@link ZuulFilter#filterType()} 前置类型。\n     */",
    ),
    (
        "    /**\n     * {@link ZuulFilter#filterType()} route type.\n     */",
        "    /**\n     * {@link ZuulFilter#filterType()} 路由类型。\n     */",
    ),
    (
        "    /**\n     * Filter Order for SEND_RESPONSE_FILTER_ORDER\n     */",
        "    /**\n     * 发送响应过滤器的执行顺序。\n     */",
    ),
    (
        "    /**\n     * Zuul use Sentinel as default context when serviceId is empty.\n     */",
        "    /**\n     * 当 serviceId 为空时，Zuul 使用的 Sentinel 默认上下文名。\n     */",
    ),
    (
        "    /**\n     * Zuul context key for keeping Sentinel entries.\n     *\n     * @since 1.6.0\n     */",
        "    /**\n     * 在 Zuul 上下文中保存 Sentinel Entry 的键名。\n     *\n     * @since 1.6.0\n     */",
    ),
]

R["sentinel-adapter/sentinel-zuul-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul/fallback/BlockResponse.java"] = [
    (
        "/**\n * Fall back response for {@link com.alibaba.csp.sentinel.slots.block.BlockException}\n *\n * @author tiger\n */",
        "/**\n * {@link com.alibaba.csp.sentinel.slots.block.BlockException} 的降级响应体。\n *\n * @author tiger\n */",
    ),
    (
        "    /**\n     * HTTP status code.\n     */",
        "    /**\n     * HTTP 状态码。\n     */",
    ),
]

R["sentinel-adapter/sentinel-zuul-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul/fallback/DefaultBlockFallbackProvider.java"] = [
    (
        "/**\n * Default Fallback provider for sentinel {@link BlockException}, {@literal *} meant for all routes.\n *\n * @author tiger\n */",
        "/**\n * Sentinel {@link BlockException} 的默认降级提供者，路由 {@literal *} 表示匹配所有路由。\n *\n * @author tiger\n */",
    ),
]

R["sentinel-adapter/sentinel-zuul-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul/fallback/ZuulBlockFallbackManager.java"] = [
    (
        "/**\n * This provide fall back class manager.\n *\n * @author tiger\n */",
        "/**\n * Zuul 流控降级提供者管理器，按路由注册并查找 {@link ZuulBlockFallbackProvider}。\n *\n * @author tiger\n */",
    ),
    (
        "    /**\n     * Register special provider for different route.\n     */",
        "    /**\n     * 为指定路由注册降级提供者；路由为 {@code *} 或 null 时设为默认提供者。\n     */",
    ),
]

R["sentinel-adapter/sentinel-zuul-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul/fallback/ZuulBlockFallbackProvider.java"] = [
    (
        "/**\n * This interface is compatible for different spring cloud version.\n *\n * @author tiger\n */",
        "/**\n * Zuul 流控降级提供者接口，兼容不同 Spring Cloud 版本。\n *\n * @author tiger\n */",
    ),
    (
        "    /**\n     * The route this fallback will be used for.\n     * @return The route the fallback will be used for.\n     */",
        "    /**\n     * 本降级提供者适用的路由 ID。\n     * @return The route the fallback will be used for.\n     */",
    ),
    (
        "    /**\n     * Provides a fallback response based on the cause of the failed execution.\n     *\n     * @param route The route the fallback is for\n     * @param cause cause of the main method failure, may be <code>null</code>\n     * @return the fallback response\n     */",
        "    /**\n     * 根据执行失败原因生成降级响应。\n     *\n     * @param route The route the fallback is for\n     * @param cause cause of the main method failure, may be <code>null</code>\n     * @return the fallback response\n     */",
    ),
]

R["sentinel-adapter/sentinel-zuul-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul/filters/EntryHolder.java"] = [
    (
        "/**\n * @author wavesZh\n */",
        "/**\n * Sentinel {@link Entry} 与热点参数的持有容器，供 Zuul 过滤器在请求上下文中传递。\n *\n * @author wavesZh\n */",
    ),
]

R["sentinel-adapter/sentinel-zuul-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul/filters/SentinelEntryUtils.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * Zuul 过滤器中 Sentinel Entry 的退出与异常追踪工具类。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
]

R["sentinel-adapter/sentinel-zuul-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul/filters/SentinelZuulErrorFilter.java"] = [
    (
        "/**\n * This filter track routing exception and exit entry;\n *\n * @author tiger\n * @author Eric Zhao\n */",
        "/**\n * Zuul 错误过滤器，追踪路由异常并按序退出 Sentinel Entry。\n *\n * @author tiger\n * @author Eric Zhao\n */",
    ),
    (
        "                // Trace exception for each entry and exit entries in order.\n                // The entries can be retrieved from the request context.",
        "                // 为每个 Entry 记录异常并按序退出；Entry 可从请求上下文中获取。",
    ),
]

R["sentinel-adapter/sentinel-zuul-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul/filters/SentinelZuulPostFilter.java"] = [
    (
        "/**\n * This filter will mark complete and exit {@link com.alibaba.csp.sentinel.Entry}.\n *\n * @author tiger\n * @author Eric Zhao\n */",
        "/**\n * Zuul 后置过滤器，标记请求完成并按序退出 {@link com.alibaba.csp.sentinel.Entry}。\n *\n * @author tiger\n * @author Eric Zhao\n */",
    ),
    (
        "        // Exit the entries in order.\n        // The entries can be retrieved from the request context.",
        "        // 按序退出 Entry；Entry 可从请求上下文中获取。",
    ),
]

R["sentinel-adapter/sentinel-zuul-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul/filters/SentinelZuulPreFilter.java"] = [
    (
        "/**\n * This pre-filter will regard all {@code proxyId} and all customized API as resources.\n * When a BlockException caught, the filter will try to find a fallback to execute.\n *\n * @author tiger\n * @author Eric Zhao\n */",
        "/**\n * Zuul 前置过滤器，将所有 {@code proxyId} 与匹配的自定义 API 视为 Sentinel 资源。\n * 捕获 {@link BlockException} 时尝试查找并执行降级逻辑。\n *\n * @author tiger\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * This run before route filter so we can get more accurate RT time.\n     */",
        "    /**\n     * 在路由过滤器之前执行，以便更准确地统计响应时间（RT）。\n     */",
    ),
    (
        "            // Prevent routing from running",
        "            // 阻止后续路由执行",
    ),
    (
        "            // Set fallback response.",
        "            // 设置降级响应体。",
    ),
    (
        "            // Set Response ContentType",
        "            // 设置响应 Content-Type",
    ),
    (
        "            // We don't exit the entry here. We need to exit the entries in post filter to record Rt correctly.\n            // So here the entries will be carried in the request context.",
        "            // 此处不退出 Entry，需在 post 过滤器中退出以正确记录 RT；\n            // 因此将 Entry 暂存于请求上下文中。",
    ),
]

R["sentinel-adapter/sentinel-zuul2-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul2/HttpRequestMessageItemParser.java"] = [
    (
        "/**\n * @author wavesZh\n * @since 1.7.2\n */",
        "/**\n * 基于 {@link HttpRequestMessage} 的请求属性解析器，供 Zuul2 网关热点参数流控使用。\n *\n * @author wavesZh\n * @since 1.7.2\n */",
    ),
]


def apply_replacements(rel: str) -> None:
    path = ANALYZED / rel
    text = path.read_text(encoding="utf-8")
    for old, new in R.get(rel, []):
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
    batch["done"] = batch.get("done", 360) + len(BATCH_LIST)
    batch["remaining_pending"] = batch.get("remaining_pending", 575) - len(BATCH_LIST)
    (QUEUE / "batch.json").write_text(json.dumps(batch, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    for rel in BATCH_LIST:
        apply_replacements(rel)
    subprocess.run([
        sys.executable, str(ROOT / "scripts/mark_batch_done.py"),
        "--project", "sentinel", "--version", "1.8.10",
        "--note", "wave13b zuul/zuul2 [15:30]",
        *BATCH_LIST,
    ], check=True)
    update_batch_json()
    print(f"Annotated {len(BATCH_LIST)} files")


if __name__ == "__main__":
    main()
