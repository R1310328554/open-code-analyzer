#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-8a httpclient/gateway [0:15]."""
from __future__ import annotations

import json
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "sentinel/1.8.10"
ANALYZED = VER / "analyzed"
ORIGINAL = VER / "original"
QUEUE = VER / "_reports/class-queue"
BATCH_LIST = Path("/tmp/sentinel_w8a.txt").read_text(encoding="utf-8").strip().split("\n")[:15]

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-adapter/sentinel-apache-httpclient-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/apache/httpclient/extractor/DefaultApacheHttpClientResourceExtractor.java"] = [
    (
        "/**\n * @author zhaoyuguang\n */",
        "/**\n * Apache HttpClient 默认资源名提取器，从请求 URI 提取 Sentinel 资源名。\n *\n * @author zhaoyuguang\n */",
    ),
    (
        "    @Override\n    public String extractor(HttpRequestWrapper request) {",
        "    /** 从请求行 URI 提取资源名。 */\n    @Override\n    public String extractor(HttpRequestWrapper request) {",
    ),
]

R["sentinel-adapter/sentinel-apache-httpclient-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/apache/httpclient/fallback/ApacheHttpClientFallback.java"] = [
    (
        "/**\n * @author zhaoyuguang\n */",
        "/**\n * Apache HttpClient 被 Sentinel 阻断时的降级处理器接口。\n *\n * @author zhaoyuguang\n */",
    ),
    (
        "    CloseableHttpResponse handle(HttpRequestWrapper request, BlockException e);",
        "    /**\n     * 处理被阻断的请求。\n     *\n     * @param request the HTTP request\n     * @param e block exception\n     * @return fallback response\n     */\n    CloseableHttpResponse handle(HttpRequestWrapper request, BlockException e);",
    ),
]

R["sentinel-adapter/sentinel-apache-httpclient-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/apache/httpclient/fallback/DefaultApacheHttpClientFallback.java"] = [
    (
        "/**\n * @author zhaoyuguang\n */",
        "/**\n * Apache HttpClient 默认降级实现，将 {@link BlockException} 包装为 {@link SentinelRpcException}。\n *\n * @author zhaoyuguang\n */",
    ),
    (
        "        // Just wrap and throw the exception.",
        "        // 将阻断异常包装后抛出。",
    ),
    (
        "    @Override\n    public CloseableHttpResponse handle(HttpRequestWrapper request, BlockException e) {",
        "    /** 包装阻断异常并抛出 {@link SentinelRpcException}。 */\n    @Override\n    public CloseableHttpResponse handle(HttpRequestWrapper request, BlockException e) {",
    ),
]

R["sentinel-adapter/sentinel-apache-httpclient5-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/apache/httpclient5/SentinelApacheHttpClient5Handler.java"] = [
    (
        "/**\n * Apache HttpClient 5.x adapter for Sentinel.\n *\n * <p>This handler implements {@link ExecChainHandler} to intercept outgoing HTTP requests\n * and protect them with Sentinel flow control.</p>\n *\n * <p>Usage example:</p>\n * <pre>{@code\n * CloseableHttpClient httpclient = HttpClients.custom()\n *     .addExecInterceptorBefore(ChainElement.MAIN_TRANSPORT.name(), \"sentinel\",\n *         new SentinelApacheHttpClient5Handler())\n *     .build();\n * }</pre>\n *\n * @author uuuyuqi\n */",
        "/**\n * Apache HttpClient 5.x 的 Sentinel 适配器。\n *\n * <p>实现 {@link ExecChainHandler}，拦截出站 HTTP 请求并以 Sentinel 流控保护。</p>\n *\n * <p>Usage example:</p>\n * <pre>{@code\n * CloseableHttpClient httpclient = HttpClients.custom()\n *     .addExecInterceptorBefore(ChainElement.MAIN_TRANSPORT.name(), \"sentinel\",\n *         new SentinelApacheHttpClient5Handler())\n *     .build();\n * }</pre>\n *\n * @author uuuyuqi\n */",
    ),
    (
        "    public SentinelApacheHttpClient5Handler() {",
        "    /** 使用默认配置构造 Handler。 */\n    public SentinelApacheHttpClient5Handler() {",
    ),
    (
        "    public SentinelApacheHttpClient5Handler(SentinelApacheHttpClientConfig config) {",
        "    /** 使用指定配置构造 Handler。 */\n    public SentinelApacheHttpClient5Handler(SentinelApacheHttpClientConfig config) {",
    ),
    (
        "    @Override\n    public ClassicHttpResponse execute(ClassicHttpRequest classicHttpRequest, ExecChain.Scope scope,\n                                       ExecChain execChain) throws IOException, HttpException {",
        "    /**\n     * 拦截请求：提取资源名、entry/exit，阻断时调用降级。\n     */\n    @Override\n    public ClassicHttpResponse execute(ClassicHttpRequest classicHttpRequest, ExecChain.Scope scope,\n                                       ExecChain execChain) throws IOException, HttpException {",
    ),
]

R["sentinel-adapter/sentinel-apache-httpclient5-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/apache/httpclient5/config/SentinelApacheHttpClientConfig.java"] = [
    (
        "/**\n * @author uuuyuqi\n */",
        "/**\n * Apache HttpClient 5.x Sentinel 适配器配置：资源名前缀、提取器与降级处理器。\n *\n * @author uuuyuqi\n */",
    ),
    (
        "    private String prefix = \"httpclient:\";",
        "    /** Sentinel 资源名前缀，默认 {@code httpclient:}。 */\n    private String prefix = \"httpclient:\";",
    ),
    (
        "    public String getPrefix() {",
        "    /** 获取资源名前缀。 */\n    public String getPrefix() {",
    ),
    (
        "    public void setPrefix(String prefix) {",
        "    /** 设置资源名前缀。 */\n    public void setPrefix(String prefix) {",
    ),
    (
        "    public ApacheHttpClientResourceExtractor getExtractor() {",
        "    /** 获取资源名提取器。 */\n    public ApacheHttpClientResourceExtractor getExtractor() {",
    ),
    (
        "    public void setExtractor(ApacheHttpClientResourceExtractor extractor) {",
        "    /** 设置资源名提取器。 */\n    public void setExtractor(ApacheHttpClientResourceExtractor extractor) {",
    ),
    (
        "    public ApacheHttpClientFallback getFallback() {",
        "    /** 获取降级处理器。 */\n    public ApacheHttpClientFallback getFallback() {",
    ),
    (
        "    public void setFallback(ApacheHttpClientFallback fallback) {",
        "    /** 设置降级处理器。 */\n    public void setFallback(ApacheHttpClientFallback fallback) {",
    ),
]

R["sentinel-adapter/sentinel-apache-httpclient5-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/apache/httpclient5/extractor/ApacheHttpClientResourceExtractor.java"] = [
    (
        "/**\n * Extracts Sentinel resource name from an Apache HttpClient 5.x request.\n *\n * @author uuuyuqi\n */",
        "/**\n * 从 Apache HttpClient 5.x 请求中提取 Sentinel 资源名。\n *\n * @author uuuyuqi\n */",
    ),
    (
        "    /**\n     * Extract resource name from the given request.\n     *\n     * @param request the HTTP request\n     * @return the resource name, or {@code null}/{@code \"\"} to skip Sentinel protection\n     */",
        "    /**\n     * 从给定请求提取资源名。\n     *\n     * @param request the HTTP request\n     * @return the resource name, or {@code null}/{@code \"\"} to skip Sentinel protection\n     */",
    ),
]

R["sentinel-adapter/sentinel-apache-httpclient5-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/apache/httpclient5/extractor/DefaultApacheHttpClientResourceExtractor.java"] = [
    (
        "/**\n * Default implementation of {@link ApacheHttpClientResourceExtractor}.\n *\n * <p>Generates resource name in the format {@code METHOD:url}, with query string\n * and fragment stripped. This is consistent with the OkHttp adapter's resource naming\n * convention.</p>\n *\n * @author uuuyuqi\n */",
        "/**\n * {@link ApacheHttpClientResourceExtractor} 的默认实现。\n *\n * <p>生成 {@code METHOD:url} 格式的资源名，去除 query 与 fragment，与 OkHttp 适配器命名一致。</p>\n *\n * @author uuuyuqi\n */",
    ),
    (
        "    @Override\n    public String extractor(ClassicHttpRequest request) {",
        "    /** 提取 {@code METHOD:url} 资源名，失败时返回 null。 */\n    @Override\n    public String extractor(ClassicHttpRequest request) {",
    ),
]

R["sentinel-adapter/sentinel-apache-httpclient5-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/apache/httpclient5/fallback/ApacheHttpClientFallback.java"] = [
    (
        "/**\n * Fallback handler for Apache HttpClient 5.x when the request is blocked by Sentinel.\n *\n * @author uuuyuqi\n */",
        "/**\n * Apache HttpClient 5.x 请求被 Sentinel 阻断时的降级处理器。\n *\n * @author uuuyuqi\n */",
    ),
    (
        "    /**\n     * Handle the blocked request.\n     *\n     * @param request the original HTTP request\n     * @param e       the block exception\n     * @return the fallback response\n     */",
        "    /**\n     * 处理被阻断的请求。\n     *\n     * @param request the original HTTP request\n     * @param e       the block exception\n     * @return the fallback response\n     */",
    ),
]

R["sentinel-adapter/sentinel-apache-httpclient5-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/apache/httpclient5/fallback/DefaultApacheHttpClientFallback.java"] = [
    (
        "/**\n * @author uuuyuqi\n */",
        "/**\n * Apache HttpClient 5.x 默认降级实现，将 {@link BlockException} 包装为 {@link SentinelRpcException}。\n *\n * @author uuuyuqi\n */",
    ),
    (
        "    @Override\n    public ClassicHttpResponse handle(ClassicHttpRequest request, BlockException e) {",
        "    /** 包装阻断异常并抛出 {@link SentinelRpcException}。 */\n    @Override\n    public ClassicHttpResponse handle(ClassicHttpRequest request, BlockException e) {",
    ),
]

R["sentinel-adapter/sentinel-api-gateway-adapter-common/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/common/SentinelGatewayConstants.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 网关适配器常量：资源模式、参数解析策略、URL 匹配策略等。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "    public static final int APP_TYPE_GATEWAY = 1;",
        "    /** 应用类型：网关。 */\n    public static final int APP_TYPE_GATEWAY = 1;",
    ),
    (
        "    public static final int RESOURCE_MODE_ROUTE_ID = 0;",
        "    /** 资源模式：按路由 ID。 */\n    public static final int RESOURCE_MODE_ROUTE_ID = 0;",
    ),
    (
        "    public static final int RESOURCE_MODE_CUSTOM_API_NAME = 1;",
        "    /** 资源模式：自定义 API 名称。 */\n    public static final int RESOURCE_MODE_CUSTOM_API_NAME = 1;",
    ),
    (
        "    public static final int PARAM_PARSE_STRATEGY_CLIENT_IP = 0;",
        "    /** 参数解析策略：客户端 IP。 */\n    public static final int PARAM_PARSE_STRATEGY_CLIENT_IP = 0;",
    ),
    (
        "    public static final int PARAM_PARSE_STRATEGY_HOST = 1;",
        "    /** 参数解析策略：Host 头。 */\n    public static final int PARAM_PARSE_STRATEGY_HOST = 1;",
    ),
    (
        "    public static final int PARAM_PARSE_STRATEGY_HEADER = 2;",
        "    /** 参数解析策略：请求头。 */\n    public static final int PARAM_PARSE_STRATEGY_HEADER = 2;",
    ),
    (
        "    public static final int PARAM_PARSE_STRATEGY_URL_PARAM = 3;",
        "    /** 参数解析策略：URL 参数。 */\n    public static final int PARAM_PARSE_STRATEGY_URL_PARAM = 3;",
    ),
    (
        "    public static final int PARAM_PARSE_STRATEGY_COOKIE = 4;",
        "    /** 参数解析策略：Cookie。 */\n    public static final int PARAM_PARSE_STRATEGY_COOKIE = 4;",
    ),
    (
        "    public static final int URL_MATCH_STRATEGY_EXACT = 0;",
        "    /** URL 匹配策略：精确匹配。 */\n    public static final int URL_MATCH_STRATEGY_EXACT = 0;",
    ),
    (
        "    public static final int URL_MATCH_STRATEGY_PREFIX = 1;",
        "    /** URL 匹配策略：前缀匹配。 */\n    public static final int URL_MATCH_STRATEGY_PREFIX = 1;",
    ),
    (
        "    public static final int URL_MATCH_STRATEGY_REGEX = 2;",
        "    /** URL 匹配策略：正则匹配。 */\n    public static final int URL_MATCH_STRATEGY_REGEX = 2;",
    ),
    (
        "    public static final String GATEWAY_CONTEXT_DEFAULT = \"sentinel_gateway_context_default\";",
        "    /** 默认网关 Context 名称。 */\n    public static final String GATEWAY_CONTEXT_DEFAULT = \"sentinel_gateway_context_default\";",
    ),
    (
        "    public static final String GATEWAY_NOT_MATCH_PARAM = \"$NM\";",
        "    /** 参数未匹配时的占位符。 */\n    public static final String GATEWAY_NOT_MATCH_PARAM = \"$NM\";",
    ),
    (
        "    public static final String GATEWAY_DEFAULT_PARAM = \"$D\";",
        "    /** 默认参数占位符。 */\n    public static final String GATEWAY_DEFAULT_PARAM = \"$D\";",
    ),
]

R["sentinel-adapter/sentinel-api-gateway-adapter-common/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/common/api/ApiDefinition.java"] = [
    (
        "/**\n * A group of HTTP API patterns.\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 一组 HTTP API 匹配模式，用于定义网关自定义 API 分组。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "    private String apiName;",
        "    /** API 分组名称。 */\n    private String apiName;",
    ),
    (
        "    private Set<ApiPredicateItem> predicateItems;",
        "    /** 匹配谓词项集合。 */\n    private Set<ApiPredicateItem> predicateItems;",
    ),
    (
        "    public ApiDefinition() {}",
        "    /** 无参构造。 */\n    public ApiDefinition() {}",
    ),
    (
        "    public ApiDefinition(String apiName) {",
        "    /** 指定 API 名称构造。 */\n    public ApiDefinition(String apiName) {",
    ),
    (
        "    public String getApiName() {",
        "    /** 获取 API 名称。 */\n    public String getApiName() {",
    ),
    (
        "    public ApiDefinition setApiName(String apiName) {",
        "    /** 设置 API 名称。 */\n    public ApiDefinition setApiName(String apiName) {",
    ),
    (
        "    public Set<ApiPredicateItem> getPredicateItems() {",
        "    /** 获取谓词项集合。 */\n    public Set<ApiPredicateItem> getPredicateItems() {",
    ),
    (
        "    public ApiDefinition setPredicateItems(Set<ApiPredicateItem> predicateItems) {",
        "    /** 设置谓词项集合。 */\n    public ApiDefinition setPredicateItems(Set<ApiPredicateItem> predicateItems) {",
    ),
]

R["sentinel-adapter/sentinel-api-gateway-adapter-common/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/common/api/ApiDefinitionChangeObserver.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 网关 API 定义变更观察者，在 API 分组更新时收到通知。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "    /**\n     * Notify the observer about the new gateway API definitions.\n     *\n     * @param apiDefinitions new set of gateway API definition\n     */",
        "    /**\n     * 通知观察者网关 API 定义已更新。\n     *\n     * @param apiDefinitions new set of gateway API definition\n     */",
    ),
]

R["sentinel-adapter/sentinel-api-gateway-adapter-common/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/common/api/ApiPathPredicateItem.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 基于 URL 路径的 API 谓词项，支持精确/前缀/正则匹配策略。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "    private String pattern;",
        "    /** URL 路径匹配模式。 */\n    private String pattern;",
    ),
    (
        "    private int matchStrategy = SentinelGatewayConstants.URL_MATCH_STRATEGY_EXACT;",
        "    /** URL 匹配策略，默认精确匹配。 */\n    private int matchStrategy = SentinelGatewayConstants.URL_MATCH_STRATEGY_EXACT;",
    ),
    (
        "    public ApiPathPredicateItem setPattern(String pattern) {",
        "    /** 设置路径匹配模式。 */\n    public ApiPathPredicateItem setPattern(String pattern) {",
    ),
    (
        "    public ApiPathPredicateItem setMatchStrategy(int matchStrategy) {",
        "    /** 设置 URL 匹配策略。 */\n    public ApiPathPredicateItem setMatchStrategy(int matchStrategy) {",
    ),
    (
        "    public String getPattern() {",
        "    /** 获取路径匹配模式。 */\n    public String getPattern() {",
    ),
    (
        "    public int getMatchStrategy() {",
        "    /** 获取 URL 匹配策略。 */\n    public int getMatchStrategy() {",
    ),
]

R["sentinel-adapter/sentinel-api-gateway-adapter-common/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/common/api/ApiPredicateGroupItem.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * API 谓词组合项，将多个 {@link ApiPredicateItem} 组合为 AND 关系。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "    private final Set<ApiPredicateItem> items = new HashSet<>();",
        "    /** 组合内的谓词项集合。 */\n    private final Set<ApiPredicateItem> items = new HashSet<>();",
    ),
    (
        "    public ApiPredicateGroupItem addItem(ApiPredicateItem item) {",
        "    /** 添加一个谓词项到组合中。 */\n    public ApiPredicateGroupItem addItem(ApiPredicateItem item) {",
    ),
    (
        "    public Set<ApiPredicateItem> getItems() {",
        "    /** 获取组合内所有谓词项。 */\n    public Set<ApiPredicateItem> getItems() {",
    ),
]

R["sentinel-adapter/sentinel-api-gateway-adapter-common/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/common/api/ApiPredicateItem.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 网关 API 谓词项标记接口，用于定义 URL 或参数匹配条件。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "    /**\n     * Combine two {@link ApiPredicateItem}.\n     *\n     * @param item another predicate item\n     * @return combined predicate group item\n     */",
        "    /**\n     * 将两个 {@link ApiPredicateItem} 组合为 AND 关系（已注释）。\n     *\n     * @param item another predicate item\n     * @return combined predicate group item\n     */",
    ),
]


def ensure_analyzed(rel: str) -> Path:
    dst = ANALYZED / rel
    if not dst.exists():
        src = ORIGINAL / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        dst.write_text(src.read_text(encoding="utf-8"), encoding="utf-8")
    return dst


def apply_replacements(rel: str) -> None:
    path = ensure_analyzed(rel)
    text = path.read_text(encoding="utf-8")
    if len(re.findall(r"[\u4e00-\u9fff]", text)) >= 10:
        return
    for old, new in R.get(rel, []):
        if old not in text:
            raise SystemExit(f"MISSING pattern in {rel}: {old[:80]!r}...")
        text = text.replace(old, new, 1)
    cn = len(re.findall(r"[\u4e00-\u9fff]", text))
    if cn < 10:
        raise SystemExit(f"Insufficient Chinese (cn={cn}) in {rel}")
    path.write_text(text, encoding="utf-8")


def update_batch_json() -> None:
    batch = json.loads((QUEUE / "batch.json").read_text(encoding="utf-8"))
    remaining = [f for f in batch["files"] if f not in BATCH_LIST]
    batch["files"] = remaining
    batch["done"] = batch.get("done", 210) + len(BATCH_LIST)
    batch["remaining_pending"] = batch.get("remaining_pending", 725) - len(BATCH_LIST)
    (QUEUE / "batch.json").write_text(json.dumps(batch, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    for rel in BATCH_LIST:
        apply_replacements(rel)
    subprocess.run(
        [
            sys.executable,
            str(ROOT / "scripts/mark_batch_done.py"),
            "--project",
            "sentinel",
            "--version",
            "1.8.10",
            "--note",
            "wave8a httpclient/gateway [0:15]",
            *BATCH_LIST,
        ],
        check=True,
    )
    update_batch_json()
    print(f"Annotated {len(BATCH_LIST)} files")


if __name__ == "__main__":
    main()
