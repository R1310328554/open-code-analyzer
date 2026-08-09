#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-16b block [15:30] (cluster-server-default)."""
from __future__ import annotations

import re
import shutil
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "sentinel/1.8.10"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
BATCH_LIST = Path("/tmp/sentinel_w16b.txt").read_text(encoding="utf-8").strip().split("\n")

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/statistic/concurrent/expire/RegularExpireStrategy.java"] = [
    (
        "/**\n * We need to consider the situation that the token client goes offline\n * or the resource call times out. It can be detected by sourceTimeout\n * and clientTimeout. The resource calls timeout detection is triggered\n * on the token client. If the resource is called over time, the token\n * client will request the token server to release token or refresh the\n * token. The client offline detection is triggered on the token server.\n * If the offline detection time is exceeded, token server will trigger\n * the detection token client’s status. If the token client is offline,\n * token server will delete the corresponding tokenId. If it is not offline,\n * token server will continue to save it.\n *\n * @author yunfeiyanggzq\n **/",
        "/**\n * 定期清理过期并发令牌的策略实现。\n * <p>需考虑令牌客户端离线或资源调用超时的情况，可通过 sourceTimeout 与 clientTimeout 检测。\n * 资源调用超时检测在令牌客户端侧触发；若资源调用超时，客户端会向令牌服务端请求释放或刷新令牌。\n * 客户端离线检测在令牌服务端侧触发；若超过离线检测时间，服务端会探测客户端状态。\n * 若客户端已离线，服务端删除对应 tokenId；否则继续保留。\n *\n * @author yunfeiyanggzq\n **/",
    ),
    (
        "    /**\n     * The max number of token deleted each time,\n     * the number of expired key-value pairs deleted each time does not exceed this number\n     */",
        "    /**\n     * 每次任务最多删除的令牌数量，单次清理的过期键值对不超过此值。\n     */",
    ),
    (
        "    /**\n     * Length of time for task execution\n     */",
        "    /**\n     * 单次任务执行的时间上限（毫秒）。\n     */",
    ),
    (
        "    /**\n     * Frequency of task execution\n     */",
        "    /**\n     * 定时清理任务的执行间隔（毫秒）。\n     */",
    ),
    (
        "    /**\n     * the local cache of tokenId\n     */",
        "    /**\n     * tokenId 的本地缓存。\n     */",
    ),
    (
        "            // time out execution exit",
        "            // 执行超时则退出本轮清理",
    ),
    (
        "            // remove the token whose client is offline and saved for more than clientTimeout",
        "            // 移除客户端已离线且保存时间超过 clientTimeout 的令牌",
    ),
    (
        "            // If we find that token's save time is more than 2 times of the client's call resource timeout time,\n            // the token will be determined to timeout.",
        "            // 若令牌保存时间超过资源调用超时阈值，则判定为超时并清理。",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/statistic/data/ClusterFlowEvent.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群流控统计事件类型枚举。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    /**\n     * Normal pass.\n     */",
        "    /**\n     * 正常通过。\n     */",
    ),
    (
        "    /**\n     * Normal block.\n     */",
        "    /**\n     * 正常限流。\n     */",
    ),
    (
        "    /**\n     * Token request (from client) passed.\n     */",
        "    /**\n     * 客户端令牌请求通过。\n     */",
    ),
    (
        "    /**\n     * Token request (from client) blocked.\n     */",
        "    /**\n     * 客户端令牌请求被限流。\n     */",
    ),
    (
        "    /**\n     * Pass (pre-occupy incoming buckets).\n     */",
        "    /**\n     * 通过（预占用即将到来的时间桶）。\n     */",
    ),
    (
        "    /**\n     * Block (pre-occupy incoming buckets failed).\n     */",
        "    /**\n     * 限流（预占用即将到来的时间桶失败）。\n     */",
    ),
    (
        "    /**\n     * Waiting due to flow shaping or for next bucket tick.\n     */",
        "    /**\n     * 因流控整形或等待下一时间桶刻度而等待。\n     */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/statistic/data/ClusterMetricBucket.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群指标统计桶，按 {@link ClusterFlowEvent} 分类维护各类事件的计数。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/statistic/limit/GlobalRequestLimiter.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 全局请求 QPS 限流器，按命名空间维护 {@link RequestLimiter} 实例。\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/statistic/limit/RequestLimiter.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 基于滑动窗口的请求 QPS 限流器，统计窗口内请求量并与配额比较。\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/statistic/metric/ClusterMetric.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群流控指标统计，基于 {@link ClusterMetricLeapArray} 滑动窗口聚合 {@link ClusterFlowEvent}。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    /**\n     * Get total sum for provided event in {@code intervalInSec}.\n     *\n     * @param event event to calculate\n     * @return total sum for event\n     */",
        "    /**\n     * 获取指定事件在整个统计窗口内的总计数。\n     *\n     * @param event 待统计的事件类型\n     * @return 该事件的总计数\n     */",
    ),
    (
        "    /**\n     * Get average count for provided event per second.\n     *\n     * @param event event to calculate\n     * @return average count per second for event\n     */",
        "    /**\n     * 获取指定事件的每秒平均计数（QPS）。\n     *\n     * @param event 待统计的事件类型\n     * @return 该事件的每秒平均计数\n     */",
    ),
    (
        "    /**\n     * Try to pre-occupy upcoming buckets.\n     *\n     * @return time to wait for next bucket (in ms); 0 if cannot occupy next buckets\n     */",
        "    /**\n     * 尝试预占用即将到来的时间桶。\n     *\n     * @return 等待下一桶的毫秒数；无法预占用时返回 0\n     */",
    ),
    (
        "        //  bucket to occupy (= incoming bucket)\n        //       ↓\n        // | head bucket |    |    |    | current bucket |\n        // +-------------+----+----+----+----------- ----+\n        //   (headPass)",
        "        //  待占用桶（= 即将到来的桶）\n        //       ↓\n        // | 头桶 |    |    |    | 当前桶 |\n        // +------+----+----+----+--------+\n        //   (headPass)",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/statistic/metric/ClusterMetricLeapArray.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群指标滑动窗口数组，扩展 {@link LeapArray} 以支持预占用计数转移。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/statistic/metric/ClusterParamMetric.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群热点参数指标统计，按参数值聚合滑动窗口计数并支持 Top-N 查询。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "        // After merge, get the top set one.",
        "        // 合并各桶后，取计数最高的参数值。",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/flow/statistic/metric/ClusterParameterLeapArray.java"] = [
    (
        "/**\n * @param <C> counter type\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群参数指标滑动窗口数组，每个时间桶为 LRU 限制的参数→计数映射。\n *\n * @param <C> 计数器类型\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/DefaultEmbeddedTokenServer.java"] = [
    (
        "/**\n * Default embedded token server in Sentinel which wraps the {@link SentinelDefaultTokenServer}\n * and the {@link TokenService} from SPI provider.\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * Sentinel 默认嵌入式令牌服务端，封装 {@link SentinelDefaultTokenServer}\n * 与 SPI 提供的 {@link TokenService}。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/NettyTransportServer.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 基于 Netty 的集群令牌服务端传输实现，管理连接池与服务器生命周期。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "        // If still initializing, wait for ready.",
        "        // 若仍在初始化，等待就绪。",
    ),
    (
        "                // Ignore.",
        "                // 忽略。",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/SentinelDefaultTokenServer.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * Sentinel 默认集群令牌服务端，负责 Netty 传输启动/停止与嵌入式模式注册。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "            // Mark server global mode as embedded.",
        "            // 将服务端全局模式标记为嵌入式。",
    ),
    (
        "            // Register self to connection group.",
        "            // 将自身注册到连接分组。",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/ServerConstants.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群令牌服务端常量，定义服务器状态码与默认命名空间。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/TokenServiceProvider.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 全局 {@link TokenService} SPI 提供者，启动时解析并缓存令牌服务实例。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/codec/DefaultRequestEntityDecoder.java"] = [
    (
        "/**\n * <p>Default entity decoder for any {@link ClusterRequest} entity.</p>\n *\n * <p>Decode format:</p>\n * <pre>\n * +--------+---------+---------+\n * | xid(4) | type(1) | data... |\n * +--------+---------+---------+\n * </pre>\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * <p>任意 {@link ClusterRequest} 实体的默认解码器。</p>\n *\n * <p>解码格式：</p>\n * <pre>\n * +--------+---------+---------+\n * | xid(4) | type(1) | data... |\n * +--------+---------+---------+\n * </pre>\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]


def apply_replacements(rel: str) -> None:
    src = ORIGINAL / rel
    dst = ANALYZED / rel
    if not src.exists():
        raise SystemExit(f"Missing original: {rel}")
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)
    text = dst.read_text(encoding="utf-8")
    for old, new in R.get(rel, []):
        if old not in text:
            raise SystemExit(f"MISSING pattern in {rel}: {old[:80]!r}...")
        text = text.replace(old, new, 1)
    if not re.search(r"[\u4e00-\u9fff]", text):
        raise SystemExit(f"No Chinese in {rel} after annotation")
    if "Licensed under the Apache License" not in text:
        raise SystemExit(f"License missing in {rel}")
    dst.write_text(text, encoding="utf-8")


def main() -> None:
    for rel in BATCH_LIST:
        apply_replacements(rel)
    print(f"Annotated {len(BATCH_LIST)} files")


if __name__ == "__main__":
    main()
