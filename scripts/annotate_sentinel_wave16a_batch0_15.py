#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-16a block [0:15] (cluster-server flow/checkers/statistics)."""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path("/workspace")
ANALYZED = ROOT / "sentinel/1.8.10/analyzed"
BATCH_LIST = Path("/tmp/sentinel_w16a.txt").read_text(encoding="utf-8").strip().split("\n")

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/ClusterFlowChecker.java"] = [
    (
        "/**\n * Flow checker for cluster flow rules.\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群流控规则检查器，基于滑动窗口指标判断是否发放令牌。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "            // TODO: checking logic and metric operation should be separated.",
        "            // TODO：检查逻辑与指标写入应分离。",
    ),
    (
        "                // Add prioritized pass.",
        "                // 记录优先级通过的令牌数。",
    ),
    (
        "            // Remaining count is cut down to a smaller integer.",
        "            // 剩余配额截断为整数返回。",
    ),
    (
        "                // Try to occupy incoming buckets.",
        "                // 尝试预占后续时间窗口的配额。",
    ),
    (
        "                    // waitInMs > 0 indicates pre-occupy incoming buckets successfully.",
        "                    // waitInMs > 0 表示成功预占后续窗口配额。",
    ),
    (
        "                    // Or else occupy failed, should be blocked.",
        "                    // 否则预占失败，请求应被阻断。",
    ),
    (
        "            // Blocked.",
        "            // 请求被阻断。",
    ),
    (
        "                // Add prioritized block.",
        "                // 记录优先级阻断的令牌数。",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/ClusterParamFlowChecker.java"] = [
    (
        "/**\n * @author jialiang.linjl\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群热点参数流控检查器，按参数值维度判断是否放行。\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "            // Unexpected state, return FAIL.",
        "            // 异常状态，返回 FAIL。",
    ),
    (
        "            // Empty parameter list will always pass.",
        "            // 空参数列表始终放行。",
    ),
    (
        "            // Remaining field is unsupported for multi-values.",
        "            // 多参数场景不支持返回 remaining 字段。",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/ConcurrentClusterFlowChecker.java"] = [
    (
        "/**\n * @author yunfeiyanggzq\n */",
        "/**\n * 集群并发流控检查器，基于当前并发数限制同时执行的请求数量。\n *\n * @author yunfeiyanggzq\n */",
    ),
    (
        "        // check before enter the lock to improve the efficiency",
        "        // 加锁前先检查，提升效率",
    ),
    (
        "        // ensure the atomicity of operations",
        "        // 保证并发计数操作的原子性",
    ),
    (
        "        // lock different nowCalls to improve the efficiency",
        "        // 按 flowId 分别加锁，减少锁竞争",
    ),
    (
        "            // check again whether the request can pass.",
        "            // 加锁后再次检查是否可放行。",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/DefaultTokenService.java"] = [
    (
        "/**\n * Default implementation for cluster {@link TokenService}.\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群 {@link TokenService} 的默认实现，委托各检查器处理令牌请求。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "        // The rule should be valid.",
        "        // 规则必须有效。",
    ),
    (
        "        // The rule should be valid.",
        "        // 规则必须有效。",
    ),
    (
        "        // The rule should be valid.",
        "        // 规则必须有效。",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/rule/ClusterParamFlowRuleManager.java"] = [
    (
        "/**\n * Manager for cluster parameter flow rules.\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群热点参数流控规则管理器，维护规则注册、动态更新与指标初始化。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    /**\n     * The default cluster parameter flow rule property supplier that creates a new\n     * dynamic property for a specific namespace to manually do rule management.\n     */",
        "    /**\n     * 默认集群热点参数流控规则属性供应器，为指定命名空间创建动态属性以手动管理规则。\n     */",
    ),
    (
        "    /**\n     * (id, clusterParamRule)\n     */",
        "    /**\n     * 规则 ID 到集群热点参数规则的映射。\n     */",
    ),
    (
        "    /**\n     * (namespace, [flowId...])\n     */",
        "    /**\n     * 命名空间到 flowId 集合的映射。\n     */",
    ),
    (
        "    /**\n     * (flowId, namespace)\n     */",
        "    /**\n     * flowId 到命名空间的映射。\n     */",
    ),
    (
        "    /**\n     * (namespace, property-listener wrapper)\n     */",
        "    /**\n     * 命名空间到属性-监听器包装器的映射。\n     */",
    ),
    (
        "    /**\n     * Cluster parameter flow rule property supplier for a specific namespace.\n     */",
        "    /**\n     * 为指定命名空间提供集群热点参数流控规则属性的供应器。\n     */",
    ),
    (
        "    /**\n     * Listen to the {@link SentinelProperty} for cluster {@link ParamFlowRule}s.\n     * The property is the source of cluster {@link ParamFlowRule}s for a specific namespace.\n     *\n     * @param namespace namespace to register\n     */",
        "    /**\n     * 监听指定命名空间的集群 {@link ParamFlowRule} {@link SentinelProperty}。\n     * 该属性是该命名空间集群 {@link ParamFlowRule} 的数据源。\n     *\n     * @param namespace namespace to register\n     */",
    ),
    (
        "    /**\n     * Get all cluster parameter flow rules within a specific namespace.\n     *\n     * @param namespace a valid namespace\n     * @return cluster parameter flow rules within the provided namespace\n     */",
        "    /**\n     * 获取指定命名空间内的全部集群热点参数流控规则。\n     *\n     * @param namespace a valid namespace\n     * @return cluster parameter flow rules within the provided namespace\n     */",
    ),
    (
        "    /**\n     * Load parameter flow rules for a specific namespace. The former rules of the namespace will be replaced.\n     *\n     * @param namespace a valid namespace\n     * @param rules rule list\n     */",
        "    /**\n     * 为指定命名空间加载热点参数流控规则，将替换该命名空间原有规则。\n     *\n     * @param namespace a valid namespace\n     * @param rules rule list\n     */",
    ),
    (
        "    /**\n     * Get connected count for associated namespace of given {@code flowId}.\n     *\n     * @param flowId existing rule ID\n     * @return connected count\n     */",
        "    /**\n     * 获取给定 {@code flowId} 关联命名空间的已连接客户端数量。\n     *\n     * @param flowId existing rule ID\n     * @return connected count\n     */",
    ),
    (
        "            // Flow id should not be null after filtered.",
        "            // 过滤后 flowId 不应为空。",
    ),
    (
        "            // Prepare cluster parameter metric from valid rule ID.",
        "            // 根据有效规则 ID 初始化集群热点参数指标。",
    ),
    (
        "        // Cleanup unused cluster parameter metrics.",
        "        // 清理不再使用的集群热点参数指标。",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/rule/NamespaceFlowProperty.java"] = [
    (
        "/**\n * A property wrapper for list of rules of a given namespace.\n * This is useful for auto-management of the property and listener.\n *\n * @param <T> type of the rule\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 给定命名空间规则列表的属性包装器，便于统一管理属性与监听器。\n *\n * @param <T> type of the rule\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/statistic/ClusterMetricNode.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 集群流控指标快照节点，汇总某资源的通过/阻断 QPS 与热点参数。\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/statistic/ClusterMetricNodeGenerator.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 集群指标节点生成器，将流控与热点参数指标转换为 {@link ClusterMetricNode}。\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/statistic/ClusterMetricStatistics.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群流控指标统计容器，按 flowId 管理 {@link ClusterMetric} 实例。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/statistic/ClusterParamMetricStatistics.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群热点参数指标统计容器，按 flowId 管理 {@link ClusterParamMetric} 实例。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/statistic/concurrent/ClusterConcurrentCheckerLogListener.java"] = [
    (
        "/**\n * @author yunfeiyanggzq\n */",
        "/**\n * 集群并发流控定期日志采集任务，记录各资源当前并发与令牌缓存规模。\n *\n * @author yunfeiyanggzq\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/statistic/concurrent/CurrentConcurrencyManager.java"] = [
    (
        "/**\n * We use a ConcurrentHashMap<long, AtomicInteger> type structure to store nowCalls corresponding to\n * rules, where the key is flowId and the value is nowCalls. Because nowCalls may be accessed and\n * modified by multiple threads, we consider to design it as an AtomicInteger class . Each newly\n * created rule will add a nowCalls object to this map. If the concurrency corresponding to a rule changes,\n * we will update the corresponding nowCalls in real time. Each request to obtain a token will increase the nowCalls;\n * and the request to release the token will reduce the nowCalls.\n *\n * @author yunfeiyanggzq\n */",
        "/**\n * 使用 ConcurrentHashMap&lt;Long, AtomicInteger&gt; 存储各规则的当前并发数（nowCalls），\n * key 为 flowId，value 为并发计数。因多线程并发访问与修改，值设计为 {@link AtomicInteger}。\n * 新建规则时会向 map 添加计数器；并发阈值变更时实时更新对应计数。\n * 获取令牌时递增 nowCalls，释放令牌时递减。\n *\n * @author yunfeiyanggzq\n */",
    ),
    (
        "    /**\n     * use ConcurrentHashMap to store the nowCalls of rules.\n     */",
        "    /**\n     * 使用 ConcurrentHashMap 存储各规则的 nowCalls。\n     */",
    ),
    (
        "    /**\n     * add current concurrency.\n     */",
        "    /**\n     * 增加当前并发数。\n     */",
    ),
    (
        "    /**\n     * get the current concurrency.\n     */",
        "    /**\n     * 获取指定 flowId 的当前并发计数器。\n     */",
    ),
    (
        "    /**\n     * delete the current concurrency.\n     */",
        "    /**\n     * 删除指定 flowId 的并发计数器。\n     */",
    ),
    (
        "    /**\n     * put the current concurrency.\n     */",
        "    /**\n     * 设置指定 flowId 的初始并发数。\n     */",
    ),
    (
        "    /**\n     * check flow id.\n     */",
        "    /**\n     * 检查 flowId 是否已注册。\n     */",
    ),
    (
        "    /**\n     * get NOW_CALLS_MAP.\n     */",
        "    /**\n     * 获取并发 map 的全部 flowId 键集合。\n     */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/statistic/concurrent/TokenCacheNode.java"] = [
    (
        "/**\n * We use TokenCacheNodeManager to store the tokenId, whose the underlying storage structure\n * is ConcurrentLinkedHashMap, Its storage node is TokenCacheNode. In order to operate the nowCalls value when\n * the expired tokenId is deleted regularly, we need to store the flowId in TokenCacheNode.\n *\n * @author yunfeiyanggzq\n */",
        "/**\n * 并发令牌缓存节点，由 {@link TokenCacheNodeManager} 以 ConcurrentLinkedHashMap 存储。\n * 定期清理过期 token 时需据此更新 nowCalls，故需保存 flowId 等信息。\n *\n * @author yunfeiyanggzq\n */",
    ),
    (
        "    /**\n     * the TokenId of the token\n     */",
        "    /**\n     * 令牌 ID。\n     */",
    ),
    (
        "    /**\n     * the client goes offline detection time\n     */",
        "    /**\n     * 客户端离线检测截止时间（绝对时间戳）。\n     */",
    ),
    (
        "    /**\n     * the resource called over time detection time\n     */",
        "    /**\n     * 资源调用超时检测截止时间（绝对时间戳）。\n     */",
    ),
    (
        "    /**\n     * the flow rule id  corresponding to the token\n     */",
        "    /**\n     * 令牌对应的流控规则 ID。\n     */",
    ),
    (
        "    /**\n     * the number this token occupied\n     */",
        "    /**\n     * 该令牌占用的并发配额数。\n     */",
    ),
    (
        "    /**\n     * the address of the client holds the token.\n     */",
        "    /**\n     * 持有该令牌的客户端地址。\n     */",
    ),
    (
        "        // getMostSignificantBits() returns the most significant 64 bits of this UUID's 128 bit value.",
        "        // getMostSignificantBits() 取 UUID 128 位值的高 64 位作为 tokenId。",
    ),
    (
        "        // The probability of collision is extremely low.",
        "        // 碰撞概率极低。",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/statistic/concurrent/TokenCacheNodeManager.java"] = [
    (
        "/**\n * @author yunfeiyanggzq\n */",
        "/**\n * 并发令牌缓存管理器，基于 ConcurrentLinkedHashMap 存储 {@link TokenCacheNode} 并定期清理过期条目。\n *\n * @author yunfeiyanggzq\n */",
    ),
    (
        "        // Start the task of regularly clearing expired keys",
        "        // 启动定期清理过期令牌的定时任务",
    ),
    (
        "        //use getQuietly to prevent disorder",
        "        // 使用 getQuietly 避免影响 LRU 顺序",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/statistic/concurrent/expire/ExpireStrategy.java"] = [
    (
        "/**\n * @author yunfeiyagnggzq\n */",
        "/**\n * 过期令牌清理策略接口。\n *\n * @author yunfeiyagnggzq\n */",
    ),
    (
        "    /**\n     * clean expired token regularly.\n     */",
        "    /**\n     * 启动定期清理过期令牌的任务。\n     */",
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


def main() -> None:
    for rel in BATCH_LIST:
        apply_replacements(rel)
    print(f"Annotated {len(BATCH_LIST)} files")


if __name__ == "__main__":
    main()
