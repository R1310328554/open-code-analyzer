#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-8b block [15:30] (gateway common)."""
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
BATCH_LIST = Path("/tmp/sentinel_w8b.txt").read_text(encoding="utf-8").strip().split("\n")

R: dict[str, list[tuple[str, str]]] = {}

R["GatewayApiDefinitionManager.java"] = [
    (
        "/**\n * Manager for gateway API definitions.\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 网关 API 定义管理器，负责加载、缓存并通知下游观察者。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "    /**\n     * The map keeps all found ApiDefinitionChangeObserver (class name as key).\n     */",
        "    /**\n     * 保存所有已发现的 {@link ApiDefinitionChangeObserver}（以类名为键）。\n     */",
    ),
    (
        "    /**\n     * Load given gateway API definitions and apply to downstream observers.\n     *\n     * @param apiDefinitions set of gateway API definitions\n     * @return true if updated, or else false\n     */",
        "    /**\n     * 加载给定的网关 API 定义并通知下游观察者。\n     *\n     * @param apiDefinitions 网关 API 定义集合\n     * @return 若已更新则返回 true，否则 false\n     */",
    ),
    (
        "            // propagate to downstream.",
        "            // 通知下游监听器。",
    ),
]

R["AbstractApiMatcher.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 网关 API 匹配器抽象基类，将多个 {@link com.alibaba.csp.sentinel.util.function.Predicate} 以 OR 逻辑组合。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "    /**\n     * We use {@link com.alibaba.csp.sentinel.util.function.Predicate} here as the min JDK version is 1.7.\n     */",
        "    /**\n     * 因最低 JDK 版本为 1.7，此处使用 {@link com.alibaba.csp.sentinel.util.function.Predicate}。\n     */",
    ),
    (
        "    /**\n     * Initialize the matchers.\n     */",
        "    /**\n     * 初始化内部匹配器集合。\n     */",
    ),
]

R["GetGatewayApiDefinitionGroupCommandHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 获取所有自定义网关 API 分组定义的命令处理器。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
]

R["GetGatewayRuleCommandHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 获取所有网关流控规则的命令处理器。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
]

R["UpdateGatewayApiDefinitionGroupCommandHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 更新网关 API 定义分组的命令处理器。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "    /**\n     * Parse json data to set of {@link ApiDefinition}.\n     *\n     * Since the predicateItems of {@link ApiDefinition} is set of interface,\n     * here we parse predicateItems to {@link ApiPathPredicateItem} temporarily.\n     */",
        "    /**\n     * 将 JSON 数据解析为 {@link ApiDefinition} 集合。\n     *\n     * 因 {@link ApiDefinition} 的 predicateItems 为接口集合，\n     * 此处临时将其解析为 {@link ApiPathPredicateItem}。\n     */",
    ),
    (
        "    /**\n     * Write target value to given data source.\n     *\n     * @param dataSource writable data source\n     * @param value target value to save\n     * @param <T> value type\n     * @return true if write successful or data source is empty; false if error occurs\n     */",
        "    /**\n     * 将目标值写入给定数据源。\n     *\n     * @param dataSource 可写数据源\n     * @param value 待保存的目标值\n     * @param <T> 值类型\n     * @return 写入成功或数据源为空时返回 true；发生错误时返回 false\n     */",
    ),
]

R["UpdateGatewayRuleCommandHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 更新网关流控规则的命令处理器。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "    /**\n     * Write target value to given data source.\n     *\n     * @param dataSource writable data source\n     * @param value target value to save\n     * @param <T> value type\n     * @return true if write successful or data source is empty; false if error occurs\n     */",
        "    /**\n     * 将目标值写入给定数据源。\n     *\n     * @param dataSource 可写数据源\n     * @param value 待保存的目标值\n     * @param <T> 值类型\n     * @return 写入成功或数据源为空时返回 true；发生错误时返回 false\n     */",
    ),
]

R["ConfigurableRequestItemParser.java"] = [
    (
        "/**\n * delegate RequestItemParser, support add extractors to customize request item parse.\n * <p>\n * example:\n * if you want to get client real ip in multi nginx proxy, you can register SentinelGatewayFilter bean as follows\n *\n * ConfigurableRequestItemParser<ServerWebExchange> parser = new  ConfigurableRequestItemParser<>(new ServerWebExchangeItemParser());\n * List<String> headerNames = Arrays.asList(\"X-Real-IP\", \"Client-IP\");\n * parser.addRemoteAddressExtractor(serverWebExchange -> {\n *      for (String headerKey : headerNames) {\n *          String remoteAddress = serverWebExchange.getRequest().getHeaders().getFirst(headerKey);\n *          if (StringUtils.hasLength(remoteAddress)) {\n *              return remoteAddress;\n *          }\n *      }\n *      return null;\n * });\n * return new SentinelGatewayFilter(parser);\n *\n * @author icodening\n * @date 2022.01.14\n */",
        "/**\n * 委托 {@link RequestItemParser} 的可配置实现，支持添加提取器以自定义请求项解析。\n * <p>\n * 示例：若需在多级 Nginx 代理场景下获取客户端真实 IP，可按如下方式注册 SentinelGatewayFilter bean：\n *\n * ConfigurableRequestItemParser<ServerWebExchange> parser = new  ConfigurableRequestItemParser<>(new ServerWebExchangeItemParser());\n * List<String> headerNames = Arrays.asList(\"X-Real-IP\", \"Client-IP\");\n * parser.addRemoteAddressExtractor(serverWebExchange -> {\n *      for (String headerKey : headerNames) {\n *          String remoteAddress = serverWebExchange.getRequest().getHeaders().getFirst(headerKey);\n *          if (StringUtils.hasLength(remoteAddress)) {\n *              return remoteAddress;\n *          }\n *      }\n *      return null;\n * });\n * return new SentinelGatewayFilter(parser);\n *\n * @author icodening\n * @date 2022.01.14\n */",
    ),
]

R["GatewayParamParser.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 网关参数解析器，从请求实体中按规则提取热点参数。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "    /**\n     * Parse parameters for given resource from the request entity on condition of the rule predicate.\n     *\n     * @param resource      valid resource name\n     * @param request       valid request\n     * @param rulePredicate rule predicate indicating the rules to refer\n     * @return the parameter array\n     */",
        "    /**\n     * 根据规则谓词，从请求实体中为给定资源解析参数。\n     *\n     * @param resource      有效资源名\n     * @param request       有效请求\n     * @param rulePredicate 指示需参考哪些规则的谓词\n     * @return 参数数组\n     */",
    ),
    (
        "        // TODO: what if the header has multiple values?",
        "        // TODO: 若 header 存在多个值应如何处理？",
    ),
] + [
    (
        "        // Match value according to regex pattern or exact mode.",
        "        // 按正则或精确模式匹配值。",
    )
] * 4

R["GatewayRegexCache.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.2\n */",
        "/**\n * 网关正则表达式编译缓存，避免重复编译相同 pattern。\n *\n * @author Eric Zhao\n * @since 1.6.2\n */",
    ),
]

R["RequestItemParser.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 从网关请求中提取路径、客户端 IP、Header、URL 参数与 Cookie 的解析器接口。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "    /**\n     * Get API path from the request.\n     *\n     * @param request valid request\n     * @return API path\n     */",
        "    /**\n     * 从请求中获取 API 路径。\n     *\n     * @param request 有效请求\n     * @return API 路径\n     */",
    ),
    (
        "    /**\n     * Get remote address from the request.\n     *\n     * @param request valid request\n     * @return remote address\n     */",
        "    /**\n     * 从请求中获取远程地址。\n     *\n     * @param request 有效请求\n     * @return 远程地址\n     */",
    ),
    (
        "    /**\n     * Get the header associated with the header key.\n     *\n     * @param request valid request\n     * @param key     valid header key\n     * @return the header\n     */",
        "    /**\n     * 获取与 header key 关联的请求头值。\n     *\n     * @param request 有效请求\n     * @param key     有效 header key\n     * @return 请求头值\n     */",
    ),
    (
        "    /**\n     * Get the parameter value associated with the parameter name.\n     *\n     * @param request   valid request\n     * @param paramName valid parameter name\n     * @return the parameter value\n     */",
        "    /**\n     * 获取与参数名关联的 URL 参数值。\n     *\n     * @param request   有效请求\n     * @param paramName 有效参数名\n     * @return 参数值\n     */",
    ),
    (
        "    /**\n     * Get the cookie value associated with the cookie name.\n     *\n     * @param request    valid request\n     * @param cookieName valid cookie name\n     * @return the cookie value\n     * @since 1.7.0\n     */",
        "    /**\n     * 获取与 cookie 名关联的 Cookie 值。\n     *\n     * @param request    有效请求\n     * @param cookieName 有效 cookie 名\n     * @return Cookie 值\n     * @since 1.7.0\n     */",
    ),
]

R["GatewayFlowRule.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 网关流控规则，支持路由/API 两种资源模式及参数热点限流。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "    /**\n     * For throttle (rate limiting with queueing).\n     */",
        "    /**\n     * 用于匀速排队（带队列的限流）。\n     */",
    ),
    (
        "    /**\n     * For parameter flow control. If not set, the gateway rule will be\n     * converted to normal flow rule.\n     */",
        "    /**\n     * 用于参数流控。若未设置，网关规则将转换为普通流控规则。\n     */",
    ),
]

R["GatewayParamFlowItem.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 网关参数流控项，描述从请求中提取并匹配参数的策略。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "    /**\n     * Should be set when applying to parameter flow rules.\n     */",
        "    /**\n     * 应用于参数流控规则时需设置的参数索引。\n     */",
    ),
    (
        "    /**\n     * Strategy for parsing item (e.g. client IP, arbitrary headers and URL parameters).\n     */",
        "    /**\n     * 解析策略（如客户端 IP、任意 Header 或 URL 参数）。\n     */",
    ),
    (
        "    /**\n     * Field to get (only required for arbitrary headers or URL parameters mode).\n     */",
        "    /**\n     * 待提取字段名（仅在 Header 或 URL 参数模式下必填）。\n     */",
    ),
    (
        "    /**\n     * Matching pattern. If not set, all values will be kept in LRU map.\n     */",
        "    /**\n     * 匹配模式。若未设置，所有值将保留在 LRU map 中。\n     */",
    ),
    (
        "    /**\n     * Matching strategy for item value.\n     */",
        "    /**\n     * 参数值的匹配策略。\n     */",
    ),
]

R["GatewayRuleConverter.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 网关规则转换器，将 {@link GatewayFlowRule} 转换为内部 {@link ParamFlowRule}。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "    /**\n     * Convert a gateway rule to parameter flow rule, then apply the generated\n     * parameter index to {@link GatewayParamFlowItem} of the rule.\n     *\n     * @param gatewayRule a valid gateway rule that should contain valid parameter items\n     * @param idx generated parameter index (callers should guarantee it's unique and incremental)\n     * @return converted parameter flow rule\n     */",
        "    /**\n     * 将网关规则转换为参数流控规则，并将生成的参数索引写入规则的 {@link GatewayParamFlowItem}。\n     *\n     * @param gatewayRule 有效的网关规则，应包含有效的参数项\n     * @param idx 生成的参数索引（调用方应保证唯一且递增）\n     * @return 转换后的参数流控规则\n     */",
    ),
    (
        "        // Apply the current idx to gateway rule item.",
        "        // 将当前 idx 写入网关规则项。",
    ),
    (
        "        // Apply for pattern-based parameters.",
        "        // 为基于 pattern 的参数添加非匹配放行项。",
    ),
]

R["GatewayRuleManager.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.0\n */",
        "/**\n * 网关流控规则管理器，负责加载规则并转换为参数流控规则。\n *\n * @author Eric Zhao\n * @since 1.6.0\n */",
    ),
    (
        "    /**\n     * Gateway flow rule map: (resource, [rules...])\n     */",
        "    /**\n     * 网关流控规则映射：(resource, [rules...])。\n     */",
    ),
    (
        "    /**\n     * Load all provided gateway rules into memory, while\n     * previous rules will be replaced.\n     *\n     * @param rules rule set\n     * @return true if updated, otherwise false\n     */",
        "    /**\n     * 将所有给定网关规则加载到内存，并替换之前的规则。\n     *\n     * @param rules 规则集合\n     * @return 若已更新则返回 true，否则 false\n     */",
    ),
    (
        "    /**\n     * <p>Get all converted parameter rules.</p>\n     * <p>Note: caller SHOULD NOT modify the list and rules.</p>\n     *\n     * @param resourceName valid resource name\n     * @return converted parameter rules\n     */",
        "    /**\n     * <p>获取所有已转换的参数流控规则。</p>\n     * <p>注意：调用方不应修改返回的列表与规则。</p>\n     *\n     * @param resourceName 有效资源名\n     * @return 已转换的参数流控规则\n     */",
    ),
    (
        "        // Check required field name for item types.",
        "        // 校验特定解析策略所需的字段名。",
    ),
    (
        "            // Prepare index map.",
        "            // 准备索引映射。",
    ),
    (
        "                    // Cache the rules with no parameter config, then skip.",
        "                    // 暂存无参数配置的规则，稍后处理。",
    ),
    (
        "                    // Convert to parameter flow rule.",
        "                    // 转换为参数流控规则。",
    ),
    (
        "                // Apply to the gateway rule map.",
        "                // 写入网关规则映射。",
    ),
    (
        "            // Handle non-param mode rules.",
        "            // 处理无参数模式的规则。",
    ),
    (
        "                    // Always use the same index (the last position).",
        "                    // 始终使用同一索引（最后一个位置）。",
    ),
    (
        "                // No parameter flow rules, so clear all the metrics.",
        "                // 无参数流控规则，清除所有指标。",
    ),
    (
        "            // Clear unused parameter metrics.",
        "            // 清除不再使用的参数指标。",
    ),
    (
        "            // Apply to converted rule map.",
        "            // 写入转换后的规则映射。",
    ),
]

R["GatewayFlowSlot.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.6.1\n */",
        "/**\n * 网关参数流控插槽，在调用链中执行网关热点参数限流校验。\n *\n * @author Eric Zhao\n * @since 1.6.1\n */",
    ),
    (
        "            // Initialize the parameter metrics.",
        "            // 初始化参数指标。",
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
    batch["done"] = batch.get("done", 210) + len(BATCH_LIST)
    batch["remaining_pending"] = batch.get("remaining_pending", 725) - len(BATCH_LIST)
    (QUEUE / "batch.json").write_text(json.dumps(batch, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    for rel in BATCH_LIST:
        apply_replacements(rel)
    subprocess.run([
        sys.executable, str(ROOT / "scripts/mark_batch_done.py"),
        "--project", "sentinel", "--version", "1.8.10",
        "--note", "wave8b gateway-common [15:30]",
        *BATCH_LIST,
    ], check=True)
    update_batch_json()
    print(f"Annotated {len(BATCH_LIST)} files")


if __name__ == "__main__":
    main()
