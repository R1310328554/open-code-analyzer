#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-11a block [0:15] (scg/scg-v6x)."""
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
BATCH_LIST = Path("/tmp/sentinel_w11a.txt").read_text(encoding="utf-8").strip().split("\n")

R: dict[str, list[tuple[str, str]]] = {}

R["GatewayApiMatcherManager.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * Spring Cloud Gateway 自定义 API 匹配器管理器，维护 API 名称到 {@link WebExchangeApiMatcher} 的映射。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
]

R["SpringCloudGatewayApiDefinitionChangeObserver.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * Spring Cloud Gateway API 定义变更观察者，在定义更新时重新加载匹配器。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
]

R["WebExchangeApiMatcher.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 基于 {@link ServerWebExchange} 的 API 匹配器，将 {@link ApiDefinition} 中的路径谓词转换为路由匹配谓词。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
]

R["BlockRequestHandler.java"] = [
    (
        "/**\n * Reactive handler for the blocked request.\n *\n * @author Eric Zhao\n */",
        "/**\n * 被 Sentinel 限流/熔断时的响应式请求处理器。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Handle the blocked request.\n     *\n     * @param exchange server exchange object\n     * @param t block exception\n     * @return server response to return\n     */",
        "    /**\n     * 处理被 Sentinel 拦截的请求。\n     *\n     * @param exchange server exchange object\n     * @param t block exception\n     * @return server response to return\n     */",
    ),
]

R["DefaultBlockRequestHandler.java"] = [
    (
        "/**\n * The default implementation of {@link BlockRequestHandler}.\n * Compatible with Spring WebFlux and Spring Cloud Gateway.\n *\n * @author Eric Zhao\n */",
        "/**\n * {@link BlockRequestHandler} 的默认实现，兼容 Spring WebFlux 与 Spring Cloud Gateway。\n *\n * @author Eric Zhao\n */",
    ),
    (
        "        // JSON result by default.",
        "        // 默认返回 JSON 结果。",
    ),
    (
        "    /**\n     * Reference from {@code DefaultErrorWebExceptionHandler} of Spring Boot.\n     */",
        "    /**\n     * 参考 Spring Boot 的 {@code DefaultErrorWebExceptionHandler} 实现。\n     */",
    ),
]

R["GatewayCallbackManager.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * Spring Cloud Gateway 适配器回调管理器，统一管理流控拦截处理器与请求来源解析器。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "    /**\n     * BlockRequestHandler: (serverExchange, exception) -> response\n     */",
        "    /**\n     * 流控拦截处理器：{@code (serverExchange, exception) -> response}\n     */",
    ),
    (
        "    /**\n     * RequestOriginParser: (serverExchange) -> origin\n     */",
        "    /**\n     * 请求来源解析器：{@code (serverExchange) -> origin}\n     */",
    ),
]

R["RedirectBlockRequestHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 流控拦截时将客户端临时重定向到指定 URL 的 {@link BlockRequestHandler} 实现。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
]

R["SentinelGatewayBlockExceptionHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * Spring Cloud Gateway 的 Sentinel 流控异常处理器，将 {@link BlockException} 转为 HTTP 响应。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "        // This exception handler only handles rejection by Sentinel.",
        "        // 本处理器仅处理 Sentinel 触发的流控异常。",
    ),
]

R["AntRoutePathMatcher.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 基于 Ant 风格路径模式的路由匹配器，用于匹配 {@link ServerWebExchange} 请求路径。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
]

R["RegexRoutePathMatcher.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 基于正则表达式的路由路径匹配器。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
]

R["RouteMatchers.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 路由路径匹配谓词工厂，提供 Ant、精确与正则等多种匹配策略。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
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
    batch["done"] = batch.get("done", 300) + len(BATCH_LIST)
    batch["remaining_pending"] = batch.get("remaining_pending", 635) - len(BATCH_LIST)
    (QUEUE / "batch.json").write_text(json.dumps(batch, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    for rel in BATCH_LIST:
        apply_replacements(rel)
    subprocess.run([
        sys.executable, str(ROOT / "scripts/mark_batch_done.py"),
        "--project", "sentinel", "--version", "1.8.10",
        "--note", "wave11a scg/scg-v6x [0:15]",
        *BATCH_LIST,
    ], check=True)
    update_batch_json()
    print(f"Annotated {len(BATCH_LIST)} files")


if __name__ == "__main__":
    main()
