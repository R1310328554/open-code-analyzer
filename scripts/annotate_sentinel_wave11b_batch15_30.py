#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-11b block [15:30] (scg-v6x/restclient)."""
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
BATCH_LIST = Path("/tmp/sentinel_w11b.txt").read_text(encoding="utf-8").strip().split("\n")

R: dict[str, list[tuple[str, str]]] = {}

R["WebExchangeApiMatcher.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 基于 {@link ServerWebExchange} 的 API 匹配器，将 {@link ApiDefinition} 中的路径谓词转换为路由匹配谓词。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
]

R["BlockRequestHandler.java"] = [
    (
        "/**\n * Reactive handler for the blocked request.\n *\n * @author Eric Zhao\n */",
        "/**\n * 被流控阻断请求的响应式处理器。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Handle the blocked request.\n     *\n     * @param exchange server exchange object\n     * @param t block exception\n     * @return server response to return\n     */",
        "    /**\n     * 处理被流控阻断的请求。\n     *\n     * @param exchange server exchange object\n     * @param t block exception\n     * @return server response to return\n     */",
    ),
]

R["DefaultBlockRequestHandler.java"] = [
    (
        "/**\n * The default implementation of {@link BlockRequestHandler}.\n * Compatible with Spring WebFlux v6x and Spring Cloud Gateway.\n *\n * @author uuuyuqi\n */",
        "/**\n * {@link BlockRequestHandler} 的默认实现，兼容 Spring WebFlux v6x 与 Spring Cloud Gateway。\n *\n * @author uuuyuqi\n */",
    ),
    (
        "        // JSON result by default.",
        "        // 默认返回 JSON 结果。",
    ),
    (
        "    /**\n     * Reference from {@code DefaultErrorWebExceptionHandler} of Spring Boot.\n     */",
        "    /**\n     * 逻辑参考 Spring Boot 的 {@code DefaultErrorWebExceptionHandler}。\n     */",
    ),
]

R["GatewayCallbackManager.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * Spring Cloud Gateway 回调管理器，统一管理阻断请求处理器与请求来源解析器。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "    /**\n     * BlockRequestHandler: (serverExchange, exception) -> response\n     */",
        "    /**\n     * 阻断请求处理器：(serverExchange, exception) -> response\n     */",
    ),
    (
        "    /**\n     * RequestOriginParser: (serverExchange) -> origin\n     */",
        "    /**\n     * 请求来源解析器：(serverExchange) -> origin\n     */",
    ),
]

R["RedirectBlockRequestHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 将流控阻断请求重定向到指定 URL 的 {@link BlockRequestHandler} 实现。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
]

R["SentinelGatewayBlockExceptionHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * Spring Cloud Gateway 流控异常处理器，将 Sentinel 阻断异常委托给 {@link GatewayCallbackManager}。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "        // This exception handler only handles rejection by Sentinel.",
        "        // 本异常处理器仅处理 Sentinel 流控拒绝。",
    ),
]

R["AntRoutePathMatcher.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 基于 Ant 风格路径模式的路由匹配器，用于网关 API 路径谓词匹配。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
]

R["RegexRoutePathMatcher.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 基于正则表达式的路由路径匹配器，用于网关 API 路径谓词匹配。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
]

R["RouteMatchers.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 网关路由路径匹配谓词工厂，提供全匹配、Ant、精确与正则四种匹配方式。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
]

R["SentinelRestClientConfig.java"] = [
    (
        "/**\n * Configuration for Sentinel RestClient interceptor.\n *\n * @author QHT, uuuyuqi\n */",
        "/**\n * Sentinel RestClient 拦截器配置，管理资源名前缀、资源提取器与降级处理器。\n *\n * @author QHT, uuuyuqi\n */",
    ),
]

R["SentinelRestClientInterceptor.java"] = [
    (
        "/**\n * {@link ClientHttpRequestInterceptor} for integrating Sentinel with Spring's\n * {@link org.springframework.web.client.RestClient}.\n *\n * <p>This interceptor creates two levels of Sentinel resources for each request:\n * <ul>\n * <li><b>Host-level resource</b>: {@code METHOD:scheme://host[:port]},\n * e.g. {@code GET:https://httpbin.org}</li>\n * <li><b>Path-level resource</b>: extracted by {@link RestClientResourceExtractor},\n * by default: {@code METHOD:scheme://host[:port]/path},\n * e.g. {@code GET:https://httpbin.org/get}</li>\n * </ul>\n *\n * <p>This dual-level design allows:\n * <ul>\n * <li>Host-level flow control for overall traffic to a service</li>\n * <li>Path-level flow control for specific endpoints</li>\n * <li>Circuit breaking at either level</li>\n * </ul>\n *\n * <p>Supports:\n * <ul>\n * <li>Flow control (QPS limiting)</li>\n * <li>Circuit breaking (degrade)</li>\n * <li>Custom resource name extraction via {@link RestClientResourceExtractor}</li>\n * <li>Custom fallback responses via {@link RestClientFallback}</li>\n * </ul>\n *\n * @author QHT, uuuyuqi\n * @see SentinelRestClientConfig\n * @see RestClientResourceExtractor\n * @see RestClientFallback\n */",
        "/**\n * 将 Sentinel 与 Spring {@link org.springframework.web.client.RestClient} 集成的 {@link ClientHttpRequestInterceptor}。\n *\n * <p>本拦截器为每个请求创建两级 Sentinel 资源：\n * <ul>\n * <li><b>主机级资源</b>：{@code METHOD:scheme://host[:port]}，\n * 例如 {@code GET:https://httpbin.org}</li>\n * <li><b>路径级资源</b>：由 {@link RestClientResourceExtractor} 提取，\n * 默认格式 {@code METHOD:scheme://host[:port]/path}，\n * 例如 {@code GET:https://httpbin.org/get}</li>\n * </ul>\n *\n * <p>双层设计支持：\n * <ul>\n * <li>主机级流控：限制对某服务的整体流量</li>\n * <li>路径级流控：限制特定端点的流量</li>\n * <li>任一层级均可配置熔断降级</li>\n * </ul>\n *\n * <p>支持能力：\n * <ul>\n * <li>流量控制（QPS 限流）</li>\n * <li>熔断降级（degrade）</li>\n * <li>通过 {@link RestClientResourceExtractor} 自定义资源名提取</li>\n * <li>通过 {@link RestClientFallback} 自定义降级响应</li>\n * </ul>\n *\n * @author QHT, uuuyuqi\n * @see SentinelRestClientConfig\n * @see RestClientResourceExtractor\n * @see RestClientFallback\n */",
    ),
    (
        "            // Path entry does not need to be traced if an IO exception occurred.",
        "            // 发生 IO 异常时无需对路径级 Entry 进行异常追踪。",
    ),
]

R["DefaultRestClientResourceExtractor.java"] = [
    (
        "/**\n * Default resource extractor for RestClient.\n * \n * <p>Extracts resource name in format: {@code METHOD:scheme://host[:port]/path}\n * \n * <p>Examples:\n * <ul>\n *   <li>{@code GET:https://httpbin.org/get}</li>\n *   <li>{@code POST:http://localhost:8080/api/users}</li>\n *   <li>{@code GET:http://localhost:8080/api/users/123}</li>\n * </ul>\n *\n * <p>Note: Query parameters are not included in the resource name by default.\n * Use a custom extractor if you need query parameters.\n *\n * @author QHT, uuuyuqi\n */",
        "/**\n * RestClient 默认资源名提取器。\n * \n * <p>资源名格式：{@code METHOD:scheme://host[:port]/path}\n * \n * <p>示例：\n * <ul>\n *   <li>{@code GET:https://httpbin.org/get}</li>\n *   <li>{@code POST:http://localhost:8080/api/users}</li>\n *   <li>{@code GET:http://localhost:8080/api/users/123}</li>\n * </ul>\n *\n * <p>注意：默认不包含查询参数。如需包含查询参数，请使用自定义提取器。\n *\n * @author QHT, uuuyuqi\n */",
    ),
]

R["RestClientResourceExtractor.java"] = [
    (
        "/**\n * Extractor for RestClient resource name.\n *\n * @author QHT, uuuyuqi\n */",
        "/**\n * RestClient 资源名提取器接口。\n *\n * @author QHT, uuuyuqi\n */",
    ),
    (
        "    /**\n     * Extracts the resource name from the HTTP request.\n     *\n     * @param request HTTP request entity\n     * @return the resource name of current request\n     */",
        "    /**\n     * 从 HTTP 请求中提取 Sentinel 资源名。\n     *\n     * @param request HTTP request entity\n     * @return the resource name of current request\n     */",
    ),
]

R["DefaultRestClientFallback.java"] = [
    (
        "/**\n * Default fallback handler for RestClient.\n *\n * @author QHT, uuuyuqi\n */",
        "/**\n * RestClient 默认降级处理器，将 {@link BlockException} 包装为 {@link SentinelRpcException} 抛出。\n *\n * @author QHT, uuuyuqi\n */",
    ),
    (
        "        // Just wrap and throw the exception.",
        "        // 包装并抛出异常。",
    ),
]

R["RestClientFallback.java"] = [
    (
        "/**\n * Fallback handler for RestClient when request is blocked by Sentinel.\n *\n * @author QHT, uuuyuqi\n */",
        "/**\n * RestClient 被 Sentinel 流控阻断时的降级处理器接口。\n *\n * @author QHT, uuuyuqi\n */",
    ),
    (
        "    /**\n     * Handle the blocked request and return a fallback response.\n     *\n     * @param request HTTP request entity\n     * @param body request body\n     * @param execution request execution\n     * @param ex the block exception\n     * @return fallback response\n     */",
        "    /**\n     * 处理被流控阻断的请求并返回降级响应。\n     *\n     * @param request HTTP request entity\n     * @param body request body\n     * @param execution request execution\n     * @param ex the block exception\n     * @return fallback response\n     */",
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
    batch["done"] = batch.get("done", 300) + len(BATCH_LIST)
    batch["remaining_pending"] = batch.get("remaining_pending", 635) - len(BATCH_LIST)
    (QUEUE / "batch.json").write_text(json.dumps(batch, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    for rel in BATCH_LIST:
        apply_replacements(rel)
    subprocess.run([
        sys.executable, str(ROOT / "scripts/mark_batch_done.py"),
        "--project", "sentinel", "--version", "1.8.10",
        "--note", "wave11b scg-v6x/restclient [15:30]",
        *BATCH_LIST,
    ], check=True)
    update_batch_json()
    print(f"Annotated {len(BATCH_LIST)} files")


if __name__ == "__main__":
    main()
