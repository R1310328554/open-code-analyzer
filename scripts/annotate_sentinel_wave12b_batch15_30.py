#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-12b block [15:30] (webmvc-v6x/servlet)."""
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
BATCH_LIST = Path("/tmp/sentinel_w12b.txt").read_text(encoding="utf-8").strip().split("\n")

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-adapter/sentinel-spring-webmvc-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/spring/webmvc/config/SentinelWebMvcTotalConfig.java"] = [
    (
        "/**\n * @author kaizi2009\n * @since 1.7.1\n */",
        "/**\n * Spring Web MVC 全量 URL 请求配置，将所有 URL 统一为单一 Sentinel 资源名。\n *\n * @author kaizi2009\n * @since 1.7.1\n */",
    ),
]

R["sentinel-adapter/sentinel-spring-webmvc-v6x-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/spring/webmvc_v6x/AbstractSentinelInterceptor.java"] = [
    (
        "/**\n * Since request may be reprocessed in flow if any forwarding or including or other action\n * happened (see {@link jakarta.servlet.ServletRequest#getDispatcherType()}) we will only\n * deal with the initial request. So we use <b>reference count</b> to track in\n * dispatching \"onion\" though which we could figure out whether we are in initial type \"REQUEST\".\n * That means the sub-requests which we rarely meet in practice will NOT be recorded in Sentinel.\n * <p>\n * How to implement a forward sub-request in your action:\n * <pre>\n * initialRequest() {\n *     ModelAndView mav = new ModelAndView();\n *     mav.setViewName(\"another\");\n *     return mav;\n * }\n * </pre>\n *\n * @since 1.8.8\n */",
        "/**\n * 请求在转发（forward）、包含（include）等调度过程中可能被重复处理\n * （参见 {@link jakarta.servlet.ServletRequest#getDispatcherType()}），\n * 本拦截器仅处理初始 REQUEST 类型请求。通过<b>引用计数</b>跟踪调度\"洋葱层\"，\n * 以判断当前是否仍处于初始 REQUEST 阶段；实践中极少遇到的子请求不会被 Sentinel 记录。\n * <p>\n * 在控制器中实现转发子请求的示例：\n * <pre>\n * initialRequest() {\n *     ModelAndView mav = new ModelAndView();\n *     mav.setViewName(\"another\");\n *     return mav;\n * }\n * </pre>\n *\n * @since 1.8.8\n */",
    ),
    (
        "    /**\n     * @param request\n     * @param rcKey\n     * @param step\n     * @return reference count after increasing (initial value as zero to be increased)\n     */",
        "    /**\n     * @param request\n     * @param rcKey\n     * @param step\n     * @return 递增后的引用计数（初始值为 0，随后递增）\n     */",
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
        "    /**\n     * 返回目标 Web 资源的 Sentinel 资源名。\n     *\n     * @param request web request\n     * @return the resource name of the target web resource.\n     */",
    ),
    (
        "    /**\n     * Return the context name of the target web resource.\n     *\n     * @param request web request\n     * @return the context name of the target web resource.\n     */",
        "    /**\n     * 返回目标 Web 资源的 Sentinel 上下文名。\n     *\n     * @param request web request\n     * @return the context name of the target web resource.\n     */",
    ),
    (
        "    /**\n     * When a handler starts an asynchronous request, the DispatcherServlet exits without invoking postHandle and afterCompletion\n     * Called instead of postHandle and afterCompletion to exit the context and clean thread-local variables when the handler is being executed concurrently.\n     *\n     * @param request  the current request\n     * @param response the current response\n     * @param handler  the handler (or {@link HandlerMethod}) that started async\n     *                 execution, for type and/or instance examination\n     */",
        "    /**\n     * 当处理器启动异步请求时，DispatcherServlet 会在不调用 postHandle 与 afterCompletion 的情况下退出。\n     * 本方法在并发执行处理器时被调用，替代 postHandle 与 afterCompletion，用于退出上下文并清理线程局部变量。\n     *\n     * @param request  the current request\n     * @param response the current response\n     * @param handler  the handler (or {@link HandlerMethod}) that started async\n     *                 execution, for type and/or instance examination\n     */",
    ),
    (
        "            // should not happen",
        "            // 不应发生",
    ),
    (
        "        // Record the status code here.",
        "        // 在此记录 HTTP 状态码。",
    ),
    (
        "            // Record status when blocked",
        "            // 被流控阻断时记录状态码",
    ),
    (
        "            // Throw BlockException directly. Users need to handle it in Spring global exception handler.",
        "            // 直接抛出 BlockException，需由 Spring 全局异常处理器处理。",
    ),
    (
        "            // NOTE: the status code statistics will be lost here!",
        "            // 注意：此处将丢失状态码统计！",
    ),
]

R["sentinel-adapter/sentinel-spring-webmvc-v6x-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/spring/webmvc_v6x/SentinelWebInterceptor.java"] = [
    (
        "/**\n * Spring Web MVC interceptor that integrates with Sentinel.\n * <p>\n * This will record resource as `${uri}`.\n *\n * @since 1.8.8\n */",
        "/**\n * 与 Sentinel 集成的 Spring Web MVC 拦截器。\n * <p>\n * 资源名将记录为 {@code ${uri}} 形式。\n *\n * @since 1.8.8\n */",
    ),
    (
        "            // Use the default config by default.",
        "            // 默认使用内置默认配置。",
    ),
    (
        "        // Resolve the Spring Web URL pattern from the request attribute.",
        "        // 从请求属性中解析 Spring Web URL 匹配模式。",
    ),
]

R["sentinel-adapter/sentinel-spring-webmvc-v6x-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/spring/webmvc_v6x/SentinelWebPrefixInterceptor.java"] = [
    (
        "/**\n * Spring Web MVC interceptor that integrates with Sentinel.\n * <p>\n * This will record resource as `${httpMethod}:${uri}`.\n *\n * @since 1.8.8\n */",
        "/**\n * 与 Sentinel 集成的 Spring Web MVC 拦截器。\n * <p>\n * 资源名将记录为 {@code ${httpMethod}:${uri}} 形式。\n *\n * @since 1.8.8\n */",
    ),
    (
        "        // Add method specification",
        "        // 添加 HTTP 方法前缀",
    ),
]

R["sentinel-adapter/sentinel-spring-webmvc-v6x-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/spring/webmvc_v6x/SentinelWebTotalInterceptor.java"] = [
    (
        "/**\n * The web interceptor for all requests, which will unify all URL as\n * a single resource name (configured in {@link SentinelWebMvcTotalConfig}).\n *\n * @since 1.8.8\n */",
        "/**\n * 针对所有请求的 Web 拦截器，将所有 URL 统一为单一资源名\n * （由 {@link SentinelWebMvcTotalConfig} 配置）。\n *\n * @since 1.8.8\n */",
    ),
]

R["sentinel-adapter/sentinel-spring-webmvc-v6x-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/spring/webmvc_v6x/callback/BlockExceptionHandler.java"] = [
    (
        "/**\n * Handler for the blocked request.\n *\n * @since 1.8.8\n */",
        "/**\n * 被流控阻断请求的处理器接口。\n *\n * @since 1.8.8\n */",
    ),
    (
        "    /**\n     * Handle the request when blocked.\n     *\n     * @param request  Servlet request\n     * @param response Servlet response\n     * @param resourceName resource name\n     * @param e        the block exception\n     * @throws Exception users may throw out the BlockException or other error occurs\n     */",
        "    /**\n     * 处理被流控阻断的请求。\n     *\n     * @param request  Servlet request\n     * @param response Servlet response\n     * @param resourceName resource name\n     * @param e        the block exception\n     * @throws Exception users may throw out the BlockException or other error occurs\n     */",
    ),
]

R["sentinel-adapter/sentinel-spring-webmvc-v6x-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/spring/webmvc_v6x/callback/DefaultBlockExceptionHandler.java"] = [
    (
        "/**\n * Default handler for the blocked request.\n *\n * @since 1.8.8\n */",
        "/**\n * 被流控阻断请求的默认处理器。\n *\n * @since 1.8.8\n */",
    ),
    (
        "        // Return 429 (Too Many Requests) by default.",
        "        // 默认返回 429（Too Many Requests）。",
    ),
]

R["sentinel-adapter/sentinel-spring-webmvc-v6x-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/spring/webmvc_v6x/callback/RequestOriginParser.java"] = [
    (
        "/**\n * The origin parser parses request origin (e.g. IP, user, appName) from HTTP request.\n *\n * @since 1.8.8\n */",
        "/**\n * 请求来源解析器，从 HTTP 请求中解析来源标识（如 IP、用户、应用名等）。\n *\n * @since 1.8.8\n */",
    ),
    (
        "    /**\n     * Parse the origin from given HTTP request.\n     *\n     * @param request HTTP request\n     * @return parsed origin\n     */",
        "    /**\n     * 从给定 HTTP 请求中解析来源标识。\n     *\n     * @param request HTTP request\n     * @return parsed origin\n     */",
    ),
]

R["sentinel-adapter/sentinel-spring-webmvc-v6x-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/spring/webmvc_v6x/config/BaseWebMvcConfig.java"] = [
    (
        "/**\n * Common base configuration for Spring Web MVC adapter.\n *\n * @since 1.8.8\n */",
        "/**\n * Spring Web MVC 适配器的公共基础配置。\n *\n * @since 1.8.8\n */",
    ),
    (
        "    /**\n     * Paired with attr name used to track reference count.\n     * \n     * @return\n     */",
        "    /**\n     * 与请求属性名配对，用于跟踪引用计数。\n     * \n     * @return\n     */",
    ),
]

R["sentinel-adapter/sentinel-spring-webmvc-v6x-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/spring/webmvc_v6x/config/SentinelPreWebMvcConfig.java"] = [
    (
        "/**\n * @since 1.8.8\n */",
        "/**\n * Sentinel 前置 Web MVC 拦截器配置（在 HandlerMapping 之前执行）。\n *\n * @since 1.8.8\n */",
    ),
    (
        "    /**\n     * Specify whether the URL resource name should contain the HTTP method prefix (e.g. {@code POST:}).\n     */",
        "    /**\n     * 是否在 URL 资源名中包含 HTTP 方法前缀（如 {@code POST:}）。\n     */",
    ),
    (
        "    /**\n     * Specify whether unify web context(i.e. use the default context name), and is true by default.\n     *\n     * @since 1.7.2\n     */",
        "    /**\n     * 是否统一 Web 上下文（即使用默认上下文名），默认为 true。\n     *\n     * @since 1.7.2\n     */",
    ),
]

R["sentinel-adapter/sentinel-spring-webmvc-v6x-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/spring/webmvc_v6x/config/SentinelWebMvcConfig.java"] = [
    (
        "/**\n * @since 1.8.8\n */",
        "/**\n * Sentinel Spring Web MVC 拦截器配置。\n *\n * @since 1.8.8\n */",
    ),
    (
        "    /**\n     * Specify the URL cleaner that unifies the URL resources.\n     */",
        "    /**\n     * 用于统一 URL 资源的 URL 清洗器。\n     */",
    ),
    (
        "    /**\n     * Specify whether the URL resource name should contain the HTTP method prefix (e.g. {@code POST:}).\n     */",
        "    /**\n     * 是否在 URL 资源名中包含 HTTP 方法前缀（如 {@code POST:}）。\n     */",
    ),
    (
        "    /**\n     * Specify whether unify web context(i.e. use the default context name), and is true by default.\n     *\n     * @since 1.7.2\n     */",
        "    /**\n     * 是否统一 Web 上下文（即使用默认上下文名），默认为 true。\n     *\n     * @since 1.7.2\n     */",
    ),
    (
        "    /**\n     * Specify whether the URL resource name should contain the context-path\n     */",
        "    /**\n     * 是否在 URL 资源名中包含 context-path。\n     */",
    ),
]

R["sentinel-adapter/sentinel-spring-webmvc-v6x-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/spring/webmvc_v6x/config/SentinelWebMvcTotalConfig.java"] = [
    (
        "/**\n * @since 1.8.8\n */",
        "/**\n * Spring Web MVC 全量 URL 请求配置，将所有 URL 统一为单一 Sentinel 资源名。\n *\n * @since 1.8.8\n */",
    ),
]

R["sentinel-adapter/sentinel-spring-webmvc-v6x-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/spring/webmvc_v6x/config/WebServletLocalConfig.java"] = [
    (
        "/**\n * The configuration center for Web Servlet adapter (ported to Spring Web adapter).\n *\n * @since 1.8.8\n */",
        "/**\n * Web Servlet 适配器配置中心（已移植至 Spring Web 适配器）。\n *\n * @since 1.8.8\n */",
    ),
    (
        "    /**\n     * Get redirecting page when blocked by Sentinel.\n     *\n     * @return the block page URL, maybe null if not configured.\n     */",
        "    /**\n     * 获取被 Sentinel 流控阻断时的跳转页面 URL。\n     *\n     * @return the block page URL, maybe null if not configured.\n     */",
    ),
    (
        "    /**\n     * <p>Get the HTTP status when using the default block page.</p>\n     * <p>You can set the status code with the {@code -Dcsp.sentinel.web.servlet.block.status}\n     * property. When the property is empty or invalid, Sentinel will use 429 (Too Many Requests)\n     * as the default status code.</p>\n     *\n     * @return the HTTP status of the default block page\n     */",
        "    /**\n     * <p>获取使用默认阻断页时的 HTTP 状态码。</p>\n     * <p>可通过 {@code -Dcsp.sentinel.web.servlet.block.status}\n     * 属性设置状态码；当属性为空或无效时，Sentinel 默认使用 429（Too Many Requests）。</p>\n     *\n     * @return the HTTP status of the default block page\n     */",
    ),
    (
        "    /**\n     * Set the HTTP status of the default block page.\n     *\n     * @param httpStatus the HTTP status of the default block page\n     */",
        "    /**\n     * 设置默认阻断页的 HTTP 状态码。\n     *\n     * @param httpStatus the HTTP status of the default block page\n     */",
    ),
]

R["sentinel-adapter/sentinel-web-adapter-common/src/main/java/com/alibaba/csp/sentinel/adapter/web/common/UrlCleaner.java"] = [
    (
        "/**\n * Unify the resource target.\n *\n * @since 1.8.8\n */",
        "/**\n * URL 清洗器，统一规范化 Sentinel 资源目标。\n *\n * @since 1.8.8\n */",
    ),
    (
        "    /**\n     * Unify the resource target.\n     *\n     * @param originUrl the original URL\n     * @return the unified resource name\n     */",
        "    /**\n     * 将原始 URL 清洗为统一的资源名。\n     *\n     * @param originUrl the original URL\n     * @return the unified resource name\n     */",
    ),
]

R["sentinel-adapter/sentinel-web-servlet/src/main/java/com/alibaba/csp/sentinel/adapter/servlet/CommonFilter.java"] = [
    (
        "/**\n * Servlet filter that integrates with Sentinel.\n *\n * @author youji.zj\n * @author Eric Zhao\n * @author zhaoyuguang\n */",
        "/**\n * 与 Sentinel 集成的 Servlet 过滤器。\n *\n * @author youji.zj\n * @author Eric Zhao\n * @author zhaoyuguang\n */",
    ),
    (
        "    /**\n     * Specify whether the URL resource name should contain the HTTP method prefix (e.g. {@code POST:}).\n     */",
        "    /**\n     * 是否在 URL 资源名中包含 HTTP 方法前缀（如 {@code POST:}）。\n     */",
    ),
    (
        "    /**\n     * If enabled, use the default context name, or else use the URL path as the context name,\n     * {@link WebServletConfig#WEB_SERVLET_CONTEXT_NAME}. Please pay attention to the number of context (EntranceNode),\n     * which may affect the memory footprint.\n     *\n     * @since 1.7.0\n     */",
        "    /**\n     * 若启用则使用默认上下文名，否则以 URL 路径作为上下文名\n     * （参见 {@link WebServletConfig#WEB_SERVLET_CONTEXT_NAME}）。\n     * 请注意上下文（EntranceNode）数量可能影响内存占用。\n     *\n     * @since 1.7.0\n     */",
    ),
    (
        "            // Clean and unify the URL.",
        "            // 清洗并统一 URL。",
    ),
    (
        "            // For REST APIs, you have to clean the URL (e.g. `/foo/1` and `/foo/2` -> `/foo/:id`), or",
        "            // 对于 REST API，需清洗 URL（如 `/foo/1` 与 `/foo/2` -> `/foo/:id`），否则",
    ),
    (
        "            // the amount of context and resources will exceed the threshold.",
        "            // 上下文与资源数量可能超出阈值。",
    ),
    (
        "            // If you intend to exclude some URLs, you can convert the URLs to the empty string \"\"",
        "            // 若需排除某些 URL，可在 UrlCleaner 实现中将其转为空字符串 \"\"",
    ),
    (
        "            // in the UrlCleaner implementation.",
        "            // （见 UrlCleaner 实现）。",
    ),
    (
        "                // Parse the request origin using registered origin parser.",
        "                // 使用已注册的来源解析器解析请求来源。",
    ),
    (
        "                    // Add HTTP method prefix if necessary.",
        "                    // 按需添加 HTTP 方法前缀。",
    ),
    (
        "            // Return the block page, or redirect to another URL.",
        "            // 返回阻断页或重定向至其他 URL。",
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
    batch["done"] = batch.get("done", 330) + len(BATCH_LIST)
    batch["remaining_pending"] = batch.get("remaining_pending", 605) - len(BATCH_LIST)
    (QUEUE / "batch.json").write_text(json.dumps(batch, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    for rel in BATCH_LIST:
        apply_replacements(rel)
    subprocess.run([
        sys.executable, str(ROOT / "scripts/mark_batch_done.py"),
        "--project", "sentinel", "--version", "1.8.10",
        "--note", "wave12b webmvc-v6x/servlet [15:30]",
        *BATCH_LIST,
    ], check=True)
    update_batch_json()
    print(f"Annotated {len(BATCH_LIST)} files")


if __name__ == "__main__":
    main()
