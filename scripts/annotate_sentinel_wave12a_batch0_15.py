#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-12a block [0:15] (webflux/webmvc)."""
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
BATCH_LIST = Path("/tmp/sentinel_w12a.txt").read_text(encoding="utf-8").strip().split("\n")
W12B_LIST = Path("/tmp/sentinel_w12b.txt").read_text(encoding="utf-8").strip().split("\n")

R: dict[str, list[tuple[str, str]]] = {}

R["SentinelWebFluxFilter.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.5.0\n */",
        "/**\n * Spring WebFlux 全局过滤器，对请求路径执行 Sentinel 流控。\n *\n * @author Eric Zhao\n * @since 1.5.0\n */",
    ),
    (
        "        // Maybe we can get the URL pattern elsewhere via:\n        // exchange.getAttributeOrDefault(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, path)",
        "        // 也可通过以下方式获取 URL 模式：\n        // exchange.getAttributeOrDefault(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, path)",
    ),
]

R["BlockRequestHandler.java"] = [
    (
        "/**\n * Reactive handler for the blocked request.\n *\n * @author Eric Zhao\n * @since 1.5.0\n */",
        "/**\n * 被 Sentinel 限流/熔断时的响应式请求处理器。\n *\n * @author Eric Zhao\n * @since 1.5.0\n */",
    ),
    (
        "    /**\n     * Handle the blocked request.\n     *\n     * @param exchange server exchange object\n     * @param t block exception\n     * @return server response to return\n     */",
        "    /**\n     * 处理被 Sentinel 拦截的请求。\n     *\n     * @param exchange server exchange object\n     * @param t block exception\n     * @return server response to return\n     */",
    ),
]

R["DefaultBlockRequestHandler.java"] = [
    (
        "/**\n * The default implementation of {@link BlockRequestHandler}.\n *\n * @author Eric Zhao\n * @since 1.5.0\n */",
        "/**\n * {@link BlockRequestHandler} 的默认实现。\n *\n * @author Eric Zhao\n * @since 1.5.0\n */",
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

R["WebFluxCallbackManager.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.5.0\n */",
        "/**\n * Spring WebFlux 适配器回调管理器，统一管理流控拦截处理器、URL 清洗器与请求来源解析器。\n *\n * @author Eric Zhao\n * @since 1.5.0\n */",
    ),
    (
        "    /**\n     * BlockRequestHandler: (serverExchange, exception) -> response\n     */",
        "    /**\n     * 流控拦截处理器：{@code (serverExchange, exception) -> response}\n     */",
    ),
    (
        "    /**\n     * UrlCleaner: (serverExchange, originalUrl) -> finalUrl\n     */",
        "    /**\n     * URL 清洗器：{@code (serverExchange, originalUrl) -> finalUrl}\n     */",
    ),
    (
        "    /**\n     * RequestOriginParser: (serverExchange) -> origin\n     */",
        "    /**\n     * 请求来源解析器：{@code (serverExchange) -> origin}\n     */",
    ),
]

R["SentinelBlockExceptionHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.5.0\n */",
        "/**\n * Spring WebFlux 的 Sentinel 流控异常处理器，将 {@link BlockException} 转为 HTTP 响应。\n *\n * @author Eric Zhao\n * @since 1.5.0\n */",
    ),
    (
        "        // This exception handler only handles rejection by Sentinel.",
        "        // 本处理器仅处理 Sentinel 触发的流控异常。",
    ),
]

R["AbstractSentinelInterceptor.java"] = [
    (
        "/**\n * Since request may be reprocessed in flow if any forwarding or including or other action\n * happened (see {@link javax.servlet.ServletRequest#getDispatcherType()}) we will only\n * deal with the initial request. So we use <b>reference count</b> to track in\n * dispathing \"onion\" though which we could figure out whether we are in initial type \"REQUEST\".\n * That means the sub-requests which we rarely meet in practice will NOT be recorded in Sentinel.\n * <p>\n * How to implement a forward sub-request in your action:\n * <pre>\n * initalRequest() {\n *     ModelAndView mav = new ModelAndView();\n *     mav.setViewName(\"another\");\n *     return mav;\n * }\n * </pre>\n *\n * @author kaizi2009\n * @since 1.7.1\n */",
        "/**\n * 请求在转发（forward）、包含（include）等调度动作中可能被重复处理\n * （参见 {@link javax.servlet.ServletRequest#getDispatcherType()}），\n * 因此本拦截器仅处理初始请求。通过<b>引用计数</b>跟踪调度\"洋葱\"层，\n * 以判断当前是否处于初始类型 {@code REQUEST} 的请求。\n * 这意味着实践中极少遇到的子请求不会被 Sentinel 记录。\n * <p>\n * 在业务代码中实现转发子请求的示例：\n * <pre>\n * initalRequest() {\n *     ModelAndView mav = new ModelAndView();\n *     mav.setViewName(\"another\");\n *     return mav;\n * }\n * </pre>\n *\n * @author kaizi2009\n * @since 1.7.1\n */",
    ),
    (
        "     * @return reference count after increasing (initial value as zero to be increased)",
        "     * @return 递增后的引用计数（初始值为 0，随后递增）",
    ),
    (
        "            // initial",
        "            // 初始值",
    ),
    (
        "            // Parse the request origin using registered origin parser.",
        "            // 使用已注册的来源解析器解析请求来源。",
    ),
    (
        "    /**\n     * Return the resource name of the target web resource.\n     *\n     * @param request web request\n     * @return the resource name of the target web resource.\n     */",
        "    /**\n     * 返回目标 Web 资源的名称。\n     *\n     * @param request web request\n     * @return the resource name of the target web resource.\n     */",
    ),
    (
        "    /**\n     * Return the context name of the target web resource.\n     *\n     * @param request web request\n     * @return the context name of the target web resource.\n     */",
        "    /**\n     * 返回目标 Web 资源的上下文名称。\n     *\n     * @param request web request\n     * @return the context name of the target web resource.\n     */",
    ),
    (
        "    /**\n     * When a handler starts an asynchronous request, the DispatcherServlet exits without invoking postHandle and afterCompletion\n     * Called instead of postHandle and afterCompletion to exit the context and clean thread-local variables when the handler is being executed concurrently.\n     *\n     * @param request  the current request\n     * @param response the current response\n     * @param handler  the handler (or {@link HandlerMethod}) that started async\n     *                 execution, for type and/or instance examination\n     */",
        "    /**\n     * 当处理器启动异步请求时，{@code DispatcherServlet} 会退出且不调用 {@code postHandle} 与 {@code afterCompletion}。\n     * 本方法在并发执行处理器时替代上述回调，用于退出上下文并清理线程局部变量。\n     *\n     * @param request  the current request\n     * @param response the current response\n     * @param handler  the handler (or {@link HandlerMethod}) that started async\n     *                 execution, for type and/or instance examination\n     */",
    ),
    (
        "            // should not happen",
        "            // 不应出现的情况",
    ),
    (
        "            //Each interceptor can only catch exception once",
        "            // 每个拦截器仅捕获一次异常",
    ),
    (
        "            // Throw BlockException directly. Users need to handle it in Spring global exception handler.",
        "            // 直接抛出 BlockException，由用户在 Spring 全局异常处理器中处理。",
    ),
]

R["SentinelExceptionAware.java"] = [
    (
        "/**\n * Make exception visible to Sentinel.SentinelExceptionAware should be front of ExceptionHandlerExceptionResolver\n * whose order is 0 {@link  org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#handlerExceptionResolver}\n *\n * @author lemonJ\n */",
        "/**\n * 将业务异常暴露给 Sentinel 拦截器。\n * {@code SentinelExceptionAware} 的优先级应高于 order 为 0 的 {@code ExceptionHandlerExceptionResolver}\n * （参见 {@link org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#handlerExceptionResolver}）。\n *\n * @author lemonJ\n */",
    ),
]

R["SentinelWebInterceptor.java"] = [
    (
        "/**\n * Spring Web MVC interceptor that integrates with Sentinel.\n * <p>\n * This will record resource as `${uri}`.\n *\n * @author kaizi2009\n * @since 1.7.1\n */",
        "/**\n * 与 Sentinel 集成的 Spring Web MVC 拦截器。\n * <p>\n * 资源名将记录为 {@code ${uri}}（Spring 最佳匹配 URL 模式）。\n *\n * @author kaizi2009\n * @since 1.7.1\n */",
    ),
    (
        "            // Use the default config by default.",
        "            // 默认使用内置配置。",
    ),
    (
        "        // Resolve the Spring Web URL pattern from the request attribute.",
        "        // 从请求属性中解析 Spring Web URL 模式。",
    ),
    (
        "        // Add method specification if necessary",
        "        // 按需添加 HTTP 方法前缀",
    ),
]

R["SentinelWebTotalInterceptor.java"] = [
    (
        "/**\n * The web interceptor for all requests, which will unify all URL as\n * a single resource name (configured in {@link SentinelWebMvcTotalConfig}).\n *\n * @author kaizi2009\n * @since 1.7.1\n */",
        "/**\n * 针对所有请求的 Web 拦截器，将所有 URL 统一为单一资源名\n * （在 {@link SentinelWebMvcTotalConfig} 中配置）。\n *\n * @author kaizi2009\n * @since 1.7.1\n */",
    ),
]

R["BlockExceptionHandler.java"] = [
    (
        "/**\n * Handler for the blocked request.\n *\n * @author kaizi2009\n */",
        "/**\n * 被 Sentinel 限流/熔断时的请求处理器。\n *\n * @author kaizi2009\n */",
    ),
    (
        "    /**\n     * Handle the request when blocked.\n     *\n     * @param request  Servlet request\n     * @param response Servlet response\n     * @param e        the block exception\n     * @throws Exception users may throw out the BlockException or other error occurs\n     */",
        "    /**\n     * 处理被 Sentinel 拦截的请求。\n     *\n     * @param request  Servlet request\n     * @param response Servlet response\n     * @param e        the block exception\n     * @throws Exception users may throw out the BlockException or other error occurs\n     */",
    ),
]

R["DefaultBlockExceptionHandler.java"] = [
    (
        "/**\n * Default handler for the blocked request.\n *\n * @author kaizi2009\n */",
        "/**\n * 被 Sentinel 限流/熔断时的默认请求处理器。\n *\n * @author kaizi2009\n */",
    ),
    (
        "        // Return 429 (Too Many Requests) by default.",
        "        // 默认返回 429（Too Many Requests）。",
    ),
]

R["RequestOriginParser.java"] = [
    (
        "/**\n * The origin parser parses request origin (e.g. IP, user, appName) from HTTP request.\n *\n * @author kaizi2009\n */",
        "/**\n * 请求来源解析器，从 HTTP 请求中解析来源标识（如 IP、用户、应用名等）。\n *\n * @author kaizi2009\n */",
    ),
    (
        "    /**\n     * Parse the origin from given HTTP request.\n     *\n     * @param request HTTP request\n     * @return parsed origin\n     */",
        "    /**\n     * 从给定 HTTP 请求中解析来源标识。\n     *\n     * @param request HTTP request\n     * @return parsed origin\n     */",
    ),
]

R["UrlCleaner.java"] = [
    (
        "/**\n * Unify the resource target.\n *\n * @author kaizi2009\n */",
        "/**\n * URL 清洗器，将原始 URL 统一为规范的资源名。\n *\n * @author kaizi2009\n */",
    ),
    (
        "    /**\n     * Unify the resource target.\n     *\n     * @param originUrl the original URL\n     * @return the unified resource name\n     */",
        "    /**\n     * 将原始 URL 清洗为统一的资源名。\n     *\n     * @param originUrl the original URL\n     * @return the unified resource name\n     */",
    ),
]

R["BaseWebMvcConfig.java"] = [
    (
        "/**\n * Common base configuration for Spring Web MVC adapter.\n *\n * @author kaizi2009\n * @since 1.7.1\n */",
        "/**\n * Spring Web MVC 适配器的公共基础配置。\n *\n * @author kaizi2009\n * @since 1.7.1\n */",
    ),
    (
        "    /**\n     * Paired with attr name used to track reference count.\n     * \n     * @return\n     */",
        "    /**\n     * 与请求属性名配对，用于跟踪引用计数。\n     *\n     * @return 引用计数属性名\n     */",
    ),
]

R["SentinelWebMvcConfig.java"] = [
    (
        "/**\n * @author kaizi2009\n * @since 1.7.1\n */",
        "/**\n * Spring Web MVC 适配器配置，支持 URL 清洗、HTTP 方法前缀与上下文统一等选项。\n *\n * @author kaizi2009\n * @since 1.7.1\n */",
    ),
    (
        "    /**\n     * Specify the URL cleaner that unifies the URL resources.\n     */",
        "    /**\n     * 指定用于统一 URL 资源的清洗器。\n     */",
    ),
    (
        "    /**\n     * Specify whether the URL resource name should contain the HTTP method prefix (e.g. {@code POST:}).\n     */",
        "    /**\n     * 指定 URL 资源名是否包含 HTTP 方法前缀（如 {@code POST:}）。\n     */",
    ),
    (
        "    /**\n     * Specify whether unify web context(i.e. use the default context name), and is true by default.\n     *\n     * @since 1.7.2\n     */",
        "    /**\n     * 指定是否统一 Web 上下文（即使用默认上下文名），默认为 {@code true}。\n     *\n     * @since 1.7.2\n     */",
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
    batch["files"] = W12B_LIST
    batch["done"] = batch.get("done", 330) + len(BATCH_LIST)
    batch["remaining_pending"] = batch.get("remaining_pending", 605) - len(BATCH_LIST)
    (QUEUE / "batch.json").write_text(json.dumps(batch, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    for rel in BATCH_LIST:
        apply_replacements(rel)
    subprocess.run([
        sys.executable, str(ROOT / "scripts/mark_batch_done.py"),
        "--project", "sentinel", "--version", "1.8.10",
        "--note", "wave12a webflux/webmvc [0:15]",
        *BATCH_LIST,
    ], check=True)
    update_batch_json()
    print(f"Annotated {len(BATCH_LIST)} files")


if __name__ == "__main__":
    main()
