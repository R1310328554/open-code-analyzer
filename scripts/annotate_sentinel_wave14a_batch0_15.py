#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-14a block [0:15] (zuul2 adapter)."""
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
BATCH_LIST = Path("/tmp/sentinel_w14a.txt").read_text(encoding="utf-8").strip().split("\n")
W14B_LIST = Path("/tmp/sentinel_w14b.txt").read_text(encoding="utf-8").strip().split("\n")

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-adapter/sentinel-zuul2-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul2/api/ZuulApiDefinitionChangeObserver.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.7.2\n */",
        "/**\n * Zuul2 网关 API 定义变更观察者，在定义更新时重新加载匹配器。\n *\n * @author Eric Zhao\n * @since 1.7.2\n */",
    ),
]

R["sentinel-adapter/sentinel-zuul2-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul2/api/ZuulGatewayApiMatcherManager.java"] = [
    (
        "/**\n * @author wavesZh\n * @since 1.7.2\n */",
        "/**\n * Zuul2 网关自定义 API 匹配器管理器，维护 API 名称到 {@link HttpRequestMessageApiMatcher} 的映射。\n *\n * @author wavesZh\n * @since 1.7.2\n */",
    ),
]

R["sentinel-adapter/sentinel-zuul2-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul2/api/matcher/HttpRequestMessageApiMatcher.java"] = [
    (
        "/**\n * @author wavesZh\n */",
        "/**\n * 基于 {@link HttpRequestMessage} 的 API 匹配器，将 {@link ApiDefinition} 中的路径谓词转换为路由匹配谓词。\n *\n * @author wavesZh\n */",
    ),
]

R["sentinel-adapter/sentinel-zuul2-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul2/api/route/PrefixRoutePathMatcher.java"] = [
    (
        "/**\n * @author wavesZh\n */",
        "/**\n * 基于 Ant 风格路径模式的路由匹配器，用于匹配 {@link HttpRequestMessage} 请求路径。\n *\n * @author wavesZh\n */",
    ),
]

R["sentinel-adapter/sentinel-zuul2-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul2/api/route/RegexRoutePathMatcher.java"] = [
    (
        "/**\n * @author wavesZh\n */",
        "/**\n * 基于正则表达式的路由路径匹配器，用于匹配 {@link HttpRequestMessage} 入站请求路径。\n *\n * @author wavesZh\n */",
    ),
]

R["sentinel-adapter/sentinel-zuul2-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul2/api/route/ZuulRouteMatchers.java"] = [
    (
        "/**\n * @author wavesZh\n */",
        "/**\n * Zuul2 路由路径匹配谓词工厂，提供全匹配、Ant、精确与正则四种匹配方式。\n *\n * @author wavesZh\n */",
    ),
]

R["sentinel-adapter/sentinel-zuul2-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul2/constants/SentinelZuul2Constants.java"] = [
    (
        "/**\n * @author wavesZh\n */",
        "/**\n * Zuul2 适配器相关常量定义。\n *\n * @author wavesZh\n */",
    ),
    (
        "    /**\n     * The default entrance (context) name when the routeId is empty.\n     */",
        "    /**\n     * 当 routeId 为空时使用的默认入口（上下文）名称。\n     */",
    ),
    (
        "    /**\n     * Zuul context key for keeping Sentinel entries.\n     */",
        "    /**\n     * 在 Zuul 上下文中保存 Sentinel Entry 的键名。\n     */",
    ),
    (
        "    /**\n     * Indicate if request is blocked .\n     */",
        "    /**\n     * 标记请求是否已被 Sentinel 拦截。\n     */",
    ),
]

R["sentinel-adapter/sentinel-zuul2-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul2/fallback/BlockResponse.java"] = [
    (
        "/**\n * Fall back response for {@link com.alibaba.csp.sentinel.slots.block.BlockException}\n *\n * @author tiger\n */",
        "/**\n * {@link com.alibaba.csp.sentinel.slots.block.BlockException} 的降级响应体。\n *\n * @author tiger\n */",
    ),
    (
        "    /**\n     * HTTP status code.\n     */",
        "    /**\n     * HTTP 状态码。\n     */",
    ),
]

R["sentinel-adapter/sentinel-zuul2-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul2/fallback/DefaultBlockFallbackProvider.java"] = [
    (
        "/**\n * Default fallback provider for Sentinel {@link BlockException}, {@literal *} meant for all routes.\n *\n * @author tiger\n */",
        "/**\n * Sentinel {@link BlockException} 的默认降级提供者，路由 {@literal *} 表示匹配所有路由。\n *\n * @author tiger\n */",
    ),
]

R["sentinel-adapter/sentinel-zuul2-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul2/fallback/ZuulBlockFallbackManager.java"] = [
    (
        "/**\n * This provide fall back class manager.\n *\n * @author tiger\n */",
        "/**\n * Zuul2 流控降级提供者管理器，按路由注册并查找 {@link ZuulBlockFallbackProvider}。\n *\n * @author tiger\n */",
    ),
    (
        "    /**\n     * Register special provider for different route.\n     */",
        "    /**\n     * 为指定路由注册降级提供者；路由为 {@code *} 或 null 时设为默认提供者。\n     */",
    ),
]

R["sentinel-adapter/sentinel-zuul2-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul2/fallback/ZuulBlockFallbackProvider.java"] = [
    (
        "/**\n * This interface is compatible for different spring cloud version.\n *\n * @author tiger\n */",
        "/**\n * Zuul2 流控降级提供者接口，兼容不同 Spring Cloud 版本。\n *\n * @author tiger\n */",
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

R["sentinel-adapter/sentinel-zuul2-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul2/filters/EntryHolder.java"] = [
    (
        "/**\n * @author wavesZh\n */",
        "/**\n * Sentinel {@link Entry} 与热点参数的持有容器，供 Zuul2 过滤器在请求上下文中传递。\n *\n * @author wavesZh\n */",
    ),
]

R["sentinel-adapter/sentinel-zuul2-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul2/filters/endpoint/SentinelZuulEndpoint.java"] = [
    (
        "/**\n * Default Endpoint for handling exception.\n *\n * @author wavesZh\n */",
        "/**\n * 处理流控拦截异常的默认 Endpoint，根据降级提供者生成 HTTP 响应。\n *\n * @author wavesZh\n */",
    ),
]

R["sentinel-adapter/sentinel-zuul2-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul2/filters/inbound/SentinelZuulInboundFilter.java"] = [
    (
        "/**\n * The Zuul inbound filter wrapped with Sentinel route and customized API group entries.\n *\n * @author wavesZh\n */",
        "/**\n * Zuul2 入站过滤器，对路由 ID 与匹配的自定义 API 组执行 Sentinel 资源埋点。\n *\n * @author wavesZh\n */",
    ),
    (
        "    /**\n     * If the executor is null, flow control action will be performed on I/O thread\n     */",
        "    /**\n     * 若 executor 为 null，流控检查将在 I/O 线程上执行。\n     */",
    ),
    (
        "    /**\n     * If true, the rest of inbound filters will be skipped when the request is blocked.\n     */",
        "    /**\n     * 为 true 时，请求被拦截后将跳过后续入站过滤器。\n     */",
    ),
    (
        "    /**\n     * Constructor of the inbound filter, which extracts the route from the context route VIP attribute by default.\n     *\n     * @param order the order of the filter\n     */",
        "    /**\n     * 入站过滤器构造器，默认从上下文的 route VIP 属性提取路由 ID。\n     *\n     * @param order the order of the filter\n     */",
    ),
    (
        "    /**\n     * Constructor of the inbound filter.\n     *\n     * @param order the order of the filter\n     * @param blockedEndpointName the endpoint to go when the request is blocked\n     * @param executor the executor where Sentinel do flow checking. If null, it will be executed in current thread.\n     * @param fastError whether the rest of the filters will be skipped if the request is blocked\n     * @param routeExtractor the route ID extractor\n     */",
        "    /**\n     * 入站过滤器构造器。\n     *\n     * @param order the order of the filter\n     * @param blockedEndpointName the endpoint to go when the request is blocked\n     * @param executor the executor where Sentinel do flow checking. If null, it will be executed in current thread.\n     * @param fastError whether the rest of the filters will be skipped if the request is blocked\n     * @param routeExtractor the route ID extractor\n     */",
    ),
    (
        "            // clear context to avoid another request use incorrect context",
        "            // 清理上下文，避免后续请求复用错误的 Sentinel 上下文",
    ),
]

R["sentinel-adapter/sentinel-zuul2-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/zuul2/filters/outbound/SentinelZuulOutboundFilter.java"] = [
    (
        "/**\n * The Zuul outbound filter which will complete the Sentinel entries and\n * trace the exception that happened in previous filters.\n *\n * @author wavesZh\n */",
        "/**\n * Zuul2 出站过滤器，完成 Sentinel Entry 退出并追踪前置过滤器中的异常。\n *\n * @author wavesZh\n */",
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
    batch["files"] = W14B_LIST
    batch["done"] = batch.get("done", 390) + len(BATCH_LIST)
    batch["remaining_pending"] = batch.get("remaining_pending", 545) - len(BATCH_LIST)
    (QUEUE / "batch.json").write_text(json.dumps(batch, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    for rel in BATCH_LIST:
        apply_replacements(rel)
    subprocess.run([
        sys.executable, str(ROOT / "scripts/mark_batch_done.py"),
        "--project", "sentinel", "--version", "1.8.10",
        "--note", "wave14a zuul2 [0:15]",
        *BATCH_LIST,
    ], check=True)
    update_batch_json()
    print(f"Annotated {len(BATCH_LIST)} files")


if __name__ == "__main__":
    main()
