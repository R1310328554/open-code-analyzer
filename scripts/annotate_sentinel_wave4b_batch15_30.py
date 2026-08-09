#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-4b block [15:30]."""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "sentinel/1.8.10"
ANALYZED = VER / "analyzed"
QUEUE = VER / "_reports/class-queue"
BATCH_LIST = Path("/tmp/sentinel_w4b.txt").read_text(encoding="utf-8").strip().split("\n")

R: dict[str, list[tuple[str, str]]] = {}

R["Rule.java"] = [
    (
        "/**\n * Base interface of all rules.\n *\n * @author youji.zj\n */",
        "/**\n * 所有规则的基础接口。\n *\n * @author youji.zj\n */",
    ),
    (
        "    /**\n     * Get target resource of this rule.\n     *\n     * @return target resource of this rule\n     */",
        "    /**\n     * 获取本规则的目标资源名。\n     *\n     * @return 本规则的目标资源名\n     */",
    ),
]

R["RuleConstant.java"] = [
    (
        "/**\n * @author youji.zj\n * @author jialiang.linjl\n */",
        "/**\n * Sentinel 规则相关常量定义。\n *\n * @author youji.zj\n * @author jialiang.linjl\n */",
    ),
    (
        "    /**\n     * Degrade by biz exception ratio in the current {@link IntervalProperty#INTERVAL} second(s).\n     */",
        "    /**\n     * 按当前 {@link IntervalProperty#INTERVAL} 秒内的业务异常比例进行熔断降级。\n     */",
    ),
    (
        "    /**\n     * Degrade by biz exception count in the last 60 seconds.\n     */",
        "    /**\n     * 按最近 60 秒内的业务异常数进行熔断降级。\n     */",
    ),
]

R["RuleManager.java"] = [
    (
        "/**\n * Unified rule management tool, mainly used for matching and caching of regular rules and simple rules.\n * @author quguai\n * @date 2023/10/9 20:35\n */",
        "/**\n * 统一的规则管理工具，主要用于正则规则与简单规则的匹配和缓存。\n *\n * @author quguai\n * @date 2023/10/9 20:35\n */",
    ),
    (
        "    /**\n     * Update rules from datasource, split rules map by regex,\n     * rebuild the regex rule cache to reduce the performance loss caused by publish rules.\n     *\n     * @param rulesMap origin rules map\n     */",
        "    /**\n     * 从数据源更新规则，按正则表达式拆分规则映射，\n     * 重建正则规则缓存以减少发布规则时的性能损耗。\n     *\n     * @param rulesMap 原始规则映射\n     */",
    ),
    (
        "    /**\n     * Get rules by resource name, save the rule list after regular matching to improve performance\n     * @param resource resource name\n     * @return matching rule list\n     */",
        "    /**\n     * 按资源名获取规则，将正则匹配后的规则列表缓存以提升性能。\n     *\n     * @param resource 资源名\n     * @return 匹配到的规则列表\n     */",
    ),
    (
        "    /**\n     * Get rules from regex rules and simple rules\n     * @return rule list\n     */",
        "    /**\n     * 获取正则规则与简单规则中的全部规则。\n     *\n     * @return 规则列表\n     */",
    ),
    (
        "    /**\n     * Get origin rules, includes regex and simple rules\n     * @return original rules\n     */",
        "    /**\n     * 获取原始规则，包含正则规则与简单规则。\n     *\n     * @return 原始规则映射\n     */",
    ),
    (
        "    /**\n     * Determine whether has rule based on the resource name\n     * @param resource resource name\n     * @return whether\n     */",
        "    /**\n     * 根据资源名判断是否配置了规则。\n     *\n     * @param resource 资源名\n     * @return 是否已配置规则\n     */",
    ),
    (
        "    /**\n     * Is valid regex rules\n     * @param rule rule\n     * @return weather valid regex rule\n     */",
        "    /**\n     * 校验规则的资源名字段是否为合法的正则表达式。\n     *\n     * @param rule 待校验规则\n     * @return 是否为合法的正则规则\n     */",
    ),
]

R["SentinelRpcException.java"] = [
    (
        "/**\n * A {@link RuntimeException} marks sentinel RPC exception. The stack trace\n * is removed for high performance.\n *\n * @author leyou\n */",
        "/**\n * 标记 Sentinel RPC 异常的 {@link RuntimeException}。为提升性能，不填充堆栈跟踪。\n *\n * @author leyou\n */",
    ),
]

R["AuthorityException.java"] = [
    (
        "/**\n * Block exception for request origin access (authority) control.\n *\n * @author youji.zj\n * @author Eric Zhao\n */",
        "/**\n * 请求来源访问控制（黑白名单）被阻断时抛出的异常。\n *\n * @author youji.zj\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Get triggered rule.\n     * Note: the rule result is a reference to rule map and SHOULD NOT be modified.\n     *\n     * @return triggered rule\n     * @since 1.4.2\n     */",
        "    /**\n     * 获取触发的规则。\n     * 注意：返回的规则引用指向规则映射，不应被修改。\n     *\n     * @return 触发的规则\n     * @since 1.4.2\n     */",
    ),
]

R["AuthorityRule.java"] = [
    (
        "/**\n * Authority rule is designed for limiting by request origins.\n *\n * @author youji.zj\n */",
        "/**\n * 按请求来源（origin）进行访问控制的黑白名单规则。\n *\n * @author youji.zj\n */",
    ),
    (
        "    /**\n     * Mode: 0 for whitelist; 1 for blacklist.\n     */",
        "    /**\n     * 策略模式：0 表示白名单；1 表示黑名单。\n     */",
    ),
]

R["AuthorityRuleChecker.java"] = [
    (
        "/**\n * Rule checker for white/black list authority.\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
        "/**\n * 黑白名单访问控制规则的校验器。\n *\n * @author Eric Zhao\n * @since 0.2.0\n */",
    ),
]

R["AuthorityRuleManager.java"] = [
    (
        "/**\n * Manager for authority rules.\n *\n * @author youji.zj\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
        "/**\n * 黑白名单访问控制规则的管理器。\n *\n * @author youji.zj\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Load the authority rules to memory.\n     *\n     * @param rules list of authority rules\n     */",
        "    /**\n     * 将黑白名单规则加载到内存。\n     *\n     * @param rules 黑白名单规则列表\n     */",
    ),
    (
        "    /**\n     * Get a copy of the rules.\n     *\n     * @return a new copy of the rules.\n     */",
        "    /**\n     * 获取规则副本。\n     *\n     * @return 规则的新副本\n     */",
    ),
]

R["AuthoritySlot.java"] = [
    (
        "/**\n * A {@link ProcessorSlot} that dedicates to {@link AuthorityRule} checking.\n *\n * @author leyou\n * @author Eric Zhao\n */",
        "/**\n * 专门负责 {@link AuthorityRule} 校验的 {@link ProcessorSlot}。\n *\n * @author leyou\n * @author Eric Zhao\n */",
    ),
]

R["DefaultCircuitBreakerRuleManager.java"] = [
    (
        "/**\n * The rule manager for universal default circuit breaker rule.\n *\n * @author wuwen\n * @author Eric Zhao\n * @since 2.0.0\n */",
        "/**\n * 通用默认熔断规则的管理器。\n *\n * @author wuwen\n * @author Eric Zhao\n * @since 2.0.0\n */",
    ),
    (
        "    /**\n     * Resources in this set will not be affected by default rules.\n     */",
        "    /**\n     * 此集合中的资源不受默认规则影响。\n     */",
    ),
    (
        "    /**\n     * Listen to the {@link SentinelProperty} for default circuit breaker rules.\n     *\n     * @param property the property to listen.\n     */",
        "    /**\n     * 监听默认熔断规则的 {@link SentinelProperty}。\n     *\n     * @param property 要监听的属性\n     */",
    ),
    (
        "    /**\n     * Exclude the resource that does not require default rules.\n     *\n     * @param resourceName the name of resource that does not require default rules\n     */",
        "    /**\n     * 将不需要默认规则的资源加入排除列表。\n     *\n     * @param resourceName 不需要默认规则的资源名\n     */",
    ),
    (
        "    /**\n     * Load default circuit breaker rules, former rules will be replaced.\n     *\n     * @param rules new rules to load.\n     */",
        "    /**\n     * 加载默认熔断规则，原有规则将被替换。\n     *\n     * @param rules 要加载的新规则\n     */",
    ),
    (
        "    /**\n     * Create a circuit breaker instance from provided circuit breaking rule.\n     *\n     * @param rule a valid circuit breaking rule\n     * @return new circuit breaker based on provided rule; null if rule is invalid or unsupported type\n     */",
        "    /**\n     * 根据给定的熔断降级规则创建熔断器实例。\n     *\n     * @param rule 有效的熔断降级规则\n     * @return 基于规则创建的新熔断器；若规则无效或策略不支持则返回 null\n     */",
    ),
]

R["DefaultCircuitBreakerSlot.java"] = [
    (
        "/**\n * <p>A {@link ProcessorSlot} dedicates to universal default circuit breaker.</p>\n *\n * @author wuwen\n * @since 2.0.0\n */",
        "/**\n * <p>专门负责通用默认熔断的 {@link ProcessorSlot}。</p>\n *\n * @author wuwen\n * @since 2.0.0\n */",
    ),
]

R["DegradeException.java"] = [
    (
        "/***\n * @author youji.zj\n */",
        "/***\n * 熔断降级被触发时抛出的阻断异常。\n *\n * @author youji.zj\n */",
    ),
    (
        "    /**\n     * Get triggered rule.\n     * Note: the rule result is a reference to rule map and SHOULD NOT be modified.\n     *\n     * @return triggered rule\n     * @since 1.4.2\n     */",
        "    /**\n     * 获取触发的规则。\n     * 注意：返回的规则引用指向规则映射，不应被修改。\n     *\n     * @return 触发的规则\n     * @since 1.4.2\n     */",
    ),
]

R["DegradeRule.java"] = [
    (
        "/**\n * <p>\n * Degrade is used when the resources are in an unstable state, these resources\n * will be degraded within the next defined time window. There are two ways to\n * measure whether a resource is stable or not:\n * </p>\n * <ul>\n * <li>\n * Average response time ({@code DEGRADE_GRADE_RT}): When\n * the average RT exceeds the threshold ('count' in 'DegradeRule', in milliseconds), the\n * resource enters a quasi-degraded state. If the RT of next coming 5\n * requests still exceed this threshold, this resource will be downgraded, which\n * means that in the next time window (defined in 'timeWindow', in seconds) all the\n * access to this resource will be blocked.\n * </li>\n * <li>\n * Exception ratio: When the ratio of exception count per second and the\n * success qps exceeds the threshold, access to the resource will be blocked in\n * the coming window.\n * </li>\n * </ul>\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
        "/**\n * <p>\n * 降级（熔断）用于资源处于不稳定状态时，在接下来定义的时间窗口内对该资源进行降级处理。\n * 判断资源是否稳定有两种方式：\n * </p>\n * <ul>\n * <li>\n * 平均响应时间（{@code DEGRADE_GRADE_RT}）：当平均 RT 超过阈值\n * （{@code DegradeRule} 中的 {@code count}，单位为毫秒）时，资源进入准降级状态。\n * 若后续 5 个请求的 RT 仍超过该阈值，则触发降级，即在下一个时间窗口\n * （{@code timeWindow}，单位为秒）内阻断对该资源的所有访问。\n * </li>\n * <li>\n * 异常比例：当每秒异常数与成功 QPS 之比超过阈值时，在即将到来的时间窗口内阻断对该资源的访问。\n * </li>\n * </ul>\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Circuit breaking strategy (0: average RT, 1: exception ratio, 2: exception count).\n     */",
        "    /**\n     * 熔断策略（0：平均 RT，1：异常比例，2：异常数）。\n     */",
    ),
    (
        "    /**\n     * Threshold count. The exact meaning depends on the field of grade.\n     * <ul>\n     *     <li>In average RT mode, it means the maximum response time(RT) in milliseconds.</li>\n     *     <li>In exception ratio mode, it means exception ratio which between 0.0 and 1.0.</li>\n     *     <li>In exception count mode, it means exception count</li>\n     * <ul/>\n     */",
        "    /**\n     * 阈值。具体含义取决于 {@code grade} 字段。\n     * <ul>\n     *     <li>平均 RT 模式下，表示最大响应时间（RT），单位为毫秒。</li>\n     *     <li>异常比例模式下，表示 0.0 到 1.0 之间的异常比例。</li>\n     *     <li>异常数模式下，表示异常计数。</li>\n     * <ul/>\n     */",
    ),
    (
        "    /**\n     * Recovery timeout (in seconds) when circuit breaker opens. After the timeout, the circuit breaker will\n     * transform to half-open state for trying a few requests.\n     */",
        "    /**\n     * 熔断器打开后的恢复超时时间（秒）。超时后熔断器将转为半开状态，尝试放行少量请求。\n     */",
    ),
    (
        "    /**\n     * Minimum number of requests (in an active statistic time span) that can trigger circuit breaking.\n     *\n     * @since 1.7.0\n     */",
        "    /**\n     * 在活跃统计时间窗口内触发熔断所需的最小请求数。\n     *\n     * @since 1.7.0\n     */",
    ),
    (
        "    /**\n     * The threshold of slow request ratio in RT mode.\n     *\n     * @since 1.8.0\n     */",
        "    /**\n     * RT 模式下慢请求比例的阈值。\n     *\n     * @since 1.8.0\n     */",
    ),
    (
        "    /**\n     * The interval statistics duration in millisecond.\n     *\n     * @since 1.8.0\n     */",
        "    /**\n     * 统计时间窗口的间隔（毫秒）。\n     *\n     * @since 1.8.0\n     */",
    ),
]

R["DegradeRuleManager.java"] = [
    (
        "/**\n * The rule manager for circuit breaking rules ({@link DegradeRule}).\n *\n * @author youji.zj\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
        "/**\n * 熔断降级规则（{@link DegradeRule}）的管理器。\n *\n * @author youji.zj\n * @author jialiang.linjl\n * @author Eric Zhao\n */",
    ),
    (
        "    /**\n     * Listen to the {@link SentinelProperty} for {@link DegradeRule}s. The property is the source\n     * of {@link DegradeRule}s. Degrade rules can also be set by {@link #loadRules(List)} directly.\n     *\n     * @param property the property to listen.\n     */",
        "    /**\n     * 监听 {@link DegradeRule} 的 {@link SentinelProperty}。该属性是 {@link DegradeRule}\n     * 的配置来源；也可通过 {@link #loadRules(List)} 直接设置规则。\n     *\n     * @param property 要监听的属性\n     */",
    ),
    (
        "    /**\n     * <p>Get existing circuit breaking rules.</p>\n     * <p>Note: DO NOT modify the rules from the returned list directly.\n     * The behavior is <strong>undefined</strong>.</p>\n     *\n     * @return list of existing circuit breaking rules, or empty list if no rules were loaded\n     */",
        "    /**\n     * <p>获取当前已加载的熔断降级规则。</p>\n     * <p>注意：不要直接修改返回列表中的规则，否则行为<strong>未定义</strong>。</p>\n     *\n     * @return 已加载的熔断降级规则列表；若无规则则返回空列表\n     */",
    ),
    (
        "    /**\n     * Load {@link DegradeRule}s, former rules will be replaced.\n     *\n     * @param rules new rules to load.\n     */",
        "    /**\n     * 加载 {@link DegradeRule}，原有规则将被替换。\n     *\n     * @param rules 要加载的新规则\n     */",
    ),
    (
        "    /**\n     * Set degrade rules for provided resource. Former rules of the resource will be replaced.\n     *\n     * @param resourceName valid resource name\n     * @param rules        new rule set to load\n     * @return whether the rules has actually been updated\n     * @since 1.5.0\n     */",
        "    /**\n     * 为指定资源设置熔断降级规则，该资源原有规则将被替换。\n     *\n     * @param resourceName 有效的资源名\n     * @param rules        要加载的新规则集\n     * @return 规则是否实际被更新\n     * @since 1.5.0\n     */",
    ),
    (
        "    /**\n     * Create a circuit breaker instance from provided circuit breaking rule.\n     *\n     * @param rule a valid circuit breaking rule\n     * @return new circuit breaker based on provided rule; null if rule is invalid or unsupported type\n     */",
        "    /**\n     * 根据给定的熔断降级规则创建熔断器实例。\n     *\n     * @param rule 有效的熔断降级规则\n     * @return 基于规则创建的新熔断器；若规则无效或策略不支持则返回 null\n     */",
    ),
]

R["DegradeSlot.java"] = [
    (
        "/**\n * A {@link ProcessorSlot} dedicates to circuit breaking.\n *\n * @author Carpenter Lee\n * @author Eric Zhao\n */",
        "/**\n * 专门负责熔断降级校验的 {@link ProcessorSlot}。\n *\n * @author Carpenter Lee\n * @author Eric Zhao\n */",
    ),
]


def apply_replacements(rel: str) -> None:
    name = Path(rel).name
    path = ANALYZED / rel
    text = path.read_text(encoding="utf-8")
    for old, new in R.get(name, []):
        if old not in text:
            raise SystemExit(f"MISSING pattern in {rel}: {old[:60]!r}...")
        text = text.replace(old, new, 1)
    if not re.search(r"[\u4e00-\u9fff]", text):
        raise SystemExit(f"No Chinese in {rel} after annotation")
    path.write_text(text, encoding="utf-8")


def update_tracking() -> None:
    done_path = QUEUE / "done.txt"
    done = done_path.read_text(encoding="utf-8").rstrip("\n").split("\n")
    done_set = set(done)
    for rel in BATCH_LIST:
        if rel not in done_set:
            done.append(rel)
            done_set.add(rel)
    done_path.write_text("\n".join(done) + "\n", encoding="utf-8")

    batch = json.loads((QUEUE / "batch.json").read_text(encoding="utf-8"))
    remaining = [f for f in batch["files"] if f not in BATCH_LIST]
    batch["files"] = remaining
    batch["done"] = batch.get("done", 105) + len(BATCH_LIST)
    batch["remaining_pending"] = batch.get("remaining_pending", 845) - len(BATCH_LIST)
    (QUEUE / "batch.json").write_text(json.dumps(batch, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    for rel in BATCH_LIST:
        apply_replacements(rel)
    update_tracking()
    print(f"Annotated {len(BATCH_LIST)} files")


if __name__ == "__main__":
    main()
