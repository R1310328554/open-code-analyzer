#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-13a block [0:15] (web-servlet/zuul)."""
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
BATCH_LIST = Path("/tmp/sentinel_w13a.txt").read_text(encoding="utf-8").strip().split("\n")
W13B_LIST = Path("/tmp/sentinel_w13b.txt").read_text(encoding="utf-8").strip().split("\n")

R: dict[str, list[tuple[str, str]]] = {}

R["CommonTotalFilter.java"] = [
    (
        "/***\n * Servlet filter for all requests.\n *\n * @author youji.zj\n */",
        "/***\n * 对所有 Servlet 请求进行 Sentinel 总流量统计的过滤器。\n *\n * @author youji.zj\n */",
    ),
]

R["DefaultUrlBlockHandler.java"] = [
    (
        "/***\n * The default {@link UrlBlockHandler}.\n *\n * @author youji.zj\n */",
        "/***\n * {@link UrlBlockHandler} 的默认实现。\n *\n * @author youji.zj\n */",
    ),
    (
        "        // Directly redirect to the default flow control (blocked) page or customized block page.",
        "        // 直接跳转到默认流控拦截页或自定义拦截页。",
    ),
]

R["DefaultUrlCleaner.java"] = [
    (
        "/***\n * @author youji.zj\n */",
        "/***\n * {@link UrlCleaner} 的默认实现，直接返回原始 URL。\n *\n * @author youji.zj\n */",
    ),
]

R["RequestOriginParser.java"] = [
    (
        "/**\n * The origin parser parses request origin (e.g. IP, user, appName) from HTTP request.\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
        "/**\n * 请求来源解析器，从 HTTP 请求中解析来源标识（如 IP、用户、应用名等）。\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
    ),
    (
        "    /**\n     * Parse the origin from given HTTP request.\n     *\n     * @param request HTTP request\n     * @return parsed origin\n     */",
        "    /**\n     * 从给定 HTTP 请求中解析来源标识。\n     *\n     * @param request HTTP request\n     * @return parsed origin\n     */",
    ),
]

R["UrlBlockHandler.java"] = [
    (
        "/***\n * The URL block handler handles requests when blocked.\n *\n * @author youji.zj\n */",
        "/***\n * URL 流控拦截处理器，在请求被 Sentinel 拦截时处理响应。\n *\n * @author youji.zj\n */",
    ),
    (
        "    /**\n     * Handle the request when blocked.\n     *\n     * @param request  Servlet request\n     * @param response Servlet response\n     * @param ex       the block exception.\n     * @throws IOException some error occurs\n     */",
        "    /**\n     * 处理被 Sentinel 拦截的请求。\n     *\n     * @param request  Servlet request\n     * @param response Servlet response\n     * @param ex       the block exception.\n     * @throws IOException some error occurs\n     */",
    ),
]

R["UrlCleaner.java"] = [
    (
        "/***\n * @author youji.zj\n */",
        "/***\n * URL 清洗器，将原始 URL 统一为规范的资源名。\n *\n * @author youji.zj\n */",
    ),
    (
        "    /***\n     * <p>Process the url. Some path variables should be handled and unified.</p>\n     * <p>e.g. collect_item_relation--10200012121-.html will be converted to collect_item_relation.html</p>\n     *\n     * @param originUrl original url\n     * @return processed url\n     */",
        "    /***\n     * <p>处理 URL，对路径变量进行清洗与统一。</p>\n     * <p>例如 collect_item_relation--10200012121-.html 会被转换为 collect_item_relation.html</p>\n     *\n     * @param originUrl original url\n     * @return processed url\n     */",
    ),
]

R["WebCallbackManager.java"] = [
    (
        "/**\n * Registry for URL cleaner and URL block handler.\n *\n * @author youji.zj\n */",
        "/**\n * Web Servlet 适配器回调管理器，统一管理 URL 清洗器、流控拦截处理器与请求来源解析器。\n *\n * @author youji.zj\n */",
    ),
    (
        "    /**\n     * URL cleaner.\n     */",
        "    /**\n     * URL 清洗器。\n     */",
    ),
    (
        "    /**\n     * URL block handler.\n     */",
        "    /**\n     * URL 流控拦截处理器。\n     */",
    ),
]

R["WebServletConfig.java"] = [
    (
        "/**\n * The configuration center for Web Servlet adapter.\n *\n * @author leyou\n * @author zhaoyuguang\n */",
        "/**\n * Web Servlet 适配器的配置中心。\n *\n * @author leyou\n * @author zhaoyuguang\n */",
    ),
    (
        "    /**\n     * Get redirecting page when Sentinel blocking for {@link CommonFilter} or\n     * {@link CommonTotalFilter} occurs.\n     *\n     * @return the block page URL, maybe null if not configured.\n     */",
        "    /**\n     * 获取 {@link CommonFilter} 或 {@link CommonTotalFilter} 被 Sentinel 拦截时的跳转页面。\n     *\n     * @return the block page URL, maybe null if not configured.\n     */",
    ),
    (
        "    /**\n     * <p>Get the HTTP status when using the default block page.</p>\n     * <p>You can set the status code with the {@code -Dcsp.sentinel.web.servlet.block.status}\n     * property. When the property is empty or invalid, Sentinel will use 429 (Too Many Requests)\n     * as the default status code.</p>\n     *\n     * @return the HTTP status of the default block page\n     * @since 1.7.0\n     */",
        "    /**\n     * <p>获取使用默认拦截页时的 HTTP 状态码。</p>\n     * <p>可通过 {@code -Dcsp.sentinel.web.servlet.block.status} 属性设置状态码。\n     * 当属性为空或无效时，Sentinel 默认使用 429（Too Many Requests）。</p>\n     *\n     * @return the HTTP status of the default block page\n     * @since 1.7.0\n     */",
    ),
    (
        "    /**\n     * Set the HTTP status of the default block page.\n     *\n     * @param httpStatus the HTTP status of the default block page\n     * @since 1.7.0\n     */",
        "    /**\n     * 设置默认拦截页的 HTTP 状态码。\n     *\n     * @param httpStatus the HTTP status of the default block page\n     * @since 1.7.0\n     */",
    ),
]

R["FilterUtil.java"] = [
    (
        "/**\n * Util class for web servlet filter.\n *\n * @author zhaoyuguang\n * @author youji.zj\n * @author Eric Zhao\n */",
        "/**\n * Web Servlet 过滤器工具类，提供 URL 规范化与流控拦截响应处理。\n *\n * @author zhaoyuguang\n * @author youji.zj\n * @author Eric Zhao\n */",
    ),
    (
        "        // Note: pathInfo should be converted to camelCase style.",
        "        // 注意：pathInfo 应转换为 camelCase 风格。",
    ),
    (
        "            // Redirect to the customized block page.",
        "            // 重定向到自定义拦截页。",
    ),
    (
        "        // Check path and slash.",
        "        // 检查路径与斜杠。",
    ),
    (
        "            // Ignore \".\"",
        "            // 忽略 \".\"",
    ),
    (
        "            // Backtrack \"..\"",
        "            // 回退 \"..\"",
    ),
    (
        "        // remove the last \"/\"",
        "        // 移除末尾的 \"/\"",
    ),
    (
        "                    break; // if a slash",
        "                    break; // 遇到斜杠",
    ),
    (
        "                    break; // if not a slash",
        "                    break; // 非斜杠字符",
    ),
]

R["RequestContextItemParser.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 基于 {@link RequestContext} 的请求属性解析器，供 Zuul 网关热点参数流控使用。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
]

R["ZuulApiDefinitionChangeObserver.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * Zuul 网关 API 定义变更观察者，在定义更新时重新加载匹配器。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
]

R["ZuulGatewayApiMatcherManager.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * Zuul 网关自定义 API 匹配器管理器，维护 API 名称到 {@link RequestContextApiMatcher} 的映射。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
]

R["RequestContextApiMatcher.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 基于 {@link RequestContext} 的 API 匹配器，将 {@link ApiDefinition} 中的路径谓词转换为路由匹配谓词。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
]

R["PrefixRoutePathMatcher.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 基于 Ant 风格路径模式的路由匹配器，用于匹配 {@link RequestContext} 请求路径。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "        //Solve the problem of prefix matching",
        "        // 解决前缀路径匹配问题",
    ),
]

R["RegexRoutePathMatcher.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 基于正则表达式的路由路径匹配器。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "        //Solve the problem of route matching",
        "        // 解决路由路径匹配问题",
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
    batch["files"] = W13B_LIST
    batch["done"] = batch.get("done", 360) + len(BATCH_LIST)
    batch["remaining_pending"] = batch.get("remaining_pending", 575) - len(BATCH_LIST)
    (QUEUE / "batch.json").write_text(json.dumps(batch, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    for rel in BATCH_LIST:
        apply_replacements(rel)
    subprocess.run([
        sys.executable, str(ROOT / "scripts/mark_batch_done.py"),
        "--project", "sentinel", "--version", "1.8.10",
        "--note", "wave13a servlet/zuul [0:15]",
        *BATCH_LIST,
    ], check=True)
    update_batch_json()
    print(f"Annotated {len(BATCH_LIST)} files")


if __name__ == "__main__":
    main()
