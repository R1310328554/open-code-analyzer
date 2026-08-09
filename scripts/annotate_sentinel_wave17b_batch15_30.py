#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-17b block [15:30] (cluster-server command/config/connection)."""
from __future__ import annotations

import re
import shutil
import sys
from pathlib import Path

ROOT = Path("/workspace")
VER = ROOT / "sentinel/1.8.10"
ORIGINAL = VER / "original"
ANALYZED = VER / "analyzed"
BATCH_LIST = Path("/tmp/sentinel_w17b.txt").read_text(encoding="utf-8").strip().split("\n")

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/command/handler/FetchClusterServerInfoCommandHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 获取集群令牌服务端运行信息的命令处理器。\n * <p>聚合连接分组、传输/流控配置、命名空间集合、各命名空间 QPS 限额及 embedded 模式等，供控制台或客户端查询。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "        // Since 1.5.0 the appName is carried so that the caller can identify the appName of the token server.",
        "        // 自 1.5.0 起携带 appName，便于调用方识别令牌服务端所属应用。",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/command/handler/ModifyClusterFlowRulesCommandHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 修改指定命名空间集群流控规则的命令处理器。\n * <p>接收 URL 解码后的 JSON 数组，解析为 {@link com.alibaba.csp.sentinel.slots.block.flow.FlowRule}\n * 并委托 {@link com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager} 加载。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/command/handler/ModifyClusterParamFlowRulesCommandHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 修改指定命名空间集群热点参数流控规则的命令处理器。\n * <p>接收 URL 解码后的 JSON 数组，解析为 {@link com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule}\n * 并委托 {@link com.alibaba.csp.sentinel.cluster.flow.rule.ClusterParamFlowRuleManager} 加载。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/command/handler/ModifyClusterServerFlowConfigHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 修改集群令牌服务端流控配置的命令处理器。\n * <p>namespace 为空时加载全局流控配置；否则加载指定命名空间的流控配置。\n * 配置经校验后写入 {@link com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager}。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/command/handler/ModifyClusterServerTransportConfigHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 修改集群令牌服务端传输配置的命令处理器。\n * <p>从请求参数读取 port 与 idleSeconds，构造 {@link com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig}\n * 并加载为全局传输配置。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/command/handler/ModifyServerNamespaceSetHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 修改集群令牌服务端命名空间集合的命令处理器。\n * <p>接收 URL 解码后的 JSON 字符串集合，委托\n * {@link com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager#loadServerNamespaceSet} 生效。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/config/ServerFlowConfig.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群令牌服务端流控配置，定义全局 QPS 上限、滑动窗口参数与预占用比例等。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    public static final double DEFAULT_EXCEED_COUNT = 1.0d;",
        "    /** 默认超出计数阈值。 */\n    public static final double DEFAULT_EXCEED_COUNT = 1.0d;",
    ),
    (
        "    public static final double DEFAULT_MAX_OCCUPY_RATIO = 1.0d;",
        "    /** 默认最大预占用比例。 */\n    public static final double DEFAULT_MAX_OCCUPY_RATIO = 1.0d;",
    ),
    (
        "    public static final int DEFAULT_INTERVAL_MS = 1000;",
        "    /** 默认统计窗口间隔（毫秒）。 */\n    public static final int DEFAULT_INTERVAL_MS = 1000;",
    ),
    (
        "    public static final int DEFAULT_SAMPLE_COUNT= 10;",
        "    /** 默认滑动窗口桶数量。 */\n    public static final int DEFAULT_SAMPLE_COUNT= 10;",
    ),
    (
        "    public static final double DEFAULT_MAX_ALLOWED_QPS= 30000;",
        "    /** 默认允许的最大 QPS。 */\n    public static final double DEFAULT_MAX_ALLOWED_QPS= 30000;",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/config/ServerTransportConfig.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群令牌服务端传输配置，定义 Netty 监听端口与连接空闲超时。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    public static final int DEFAULT_IDLE_SECONDS = 600;",
        "    /** 默认连接空闲超时（秒）。 */\n    public static final int DEFAULT_IDLE_SECONDS = 600;",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/config/ServerTransportConfigObserver.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群令牌服务端传输配置变更观察者。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    /**\n     * Callback on server transport config (e.g. port) change.\n     *\n     * @param config new server transport config\n     */",
        "    /**\n     * 服务端传输配置（如端口）变更时的回调。\n     *\n     * @param config 新的服务端传输配置\n     */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/connection/Connection.java"] = [
    (
        "/**\n * @author xuyue\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群令牌服务端连接抽象，封装本地/远端地址与最近读时间等信息。\n *\n * @author xuyue\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    SocketAddress getLocalAddress();",
        "    /** 返回连接的本地地址。 */\n    SocketAddress getLocalAddress();",
    ),
    (
        "    int getRemotePort();",
        "    /** 返回远端端口。 */\n    int getRemotePort();",
    ),
    (
        "    String getRemoteIP();",
        "    /** 返回远端 IP 地址。 */\n    String getRemoteIP();",
    ),
    (
        "    void refreshLastReadTime(long lastReadTime);",
        "    /** 刷新最近读时间戳。\n     *\n     * @param lastReadTime 最近读时间（毫秒）\n     */",
    ),
    (
        "    long getLastReadTime();",
        "    /** 返回最近读时间戳（毫秒）。 */\n    long getLastReadTime();",
    ),
    (
        "    String getConnectionKey();",
        "    /** 返回连接唯一键，通常为 {@code ip:port} 格式。 */\n    String getConnectionKey();",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/connection/ConnectionDescriptor.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 连接描述符，以 address（{@code ip:port}）为主键标识一条客户端连接。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/connection/ConnectionGroup.java"] = [
    (
        "/**\n * The connection group stores connection set for a specific namespace.\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 连接分组，维护某一命名空间下的客户端连接集合与连接计数。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/connection/ConnectionManager.java"] = [
    (
        "/**\n * Manager for namespace-scope {@link ConnectionGroup}.\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 命名空间维度的 {@link ConnectionGroup} 管理器，维护连接注册与下线。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    /**\n     * Connection map (namespace, connection).\n     */",
        "    /**\n     * 连接映射（命名空间 → 连接分组）。\n     */",
    ),
    (
        "    /**\n     * namespace map (address, namespace).\n     */",
        "    /**\n     * 地址到命名空间的反向映射（address → namespace）。\n     */",
    ),
    (
        "    /**\n     * Get connected count for specific namespace.\n     *\n     * @param namespace namespace to check\n     * @return connected count for specific namespace\n     */",
        "    /**\n     * 获取指定命名空间的当前连接数。\n     *\n     * @param namespace 待查询的命名空间\n     * @return 该命名空间的连接数\n     */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/connection/ConnectionPool.java"] = [
    (
        "/**\n * Universal connection pool for connection management.\n *\n * @author xuyue\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 通用连接池，基于 {@code ip:port} 键管理 {@link Connection} 并扫描空闲连接。\n *\n * @author xuyue\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    /**\n     * Format: (\"ip:port\", connection)\n     */",
        "    /**\n     * 连接映射，格式：{@code ip:port} → connection。\n     */",
    ),
    (
        "    /**\n     * Periodic scan task.\n     */",
        "    /**\n     * 周期性扫描空闲连接的任务句柄。\n     */",
    ),
    (
        "    /**\n     * Start the scan task for long-idle connections.\n     */",
        "    /**\n     * 启动长空闲连接的扫描任务。\n     */",
    ),
    (
        "    /**\n     * Format to \"ip:port\".\n     *\n     * @param channel channel\n     * @return formatted key\n     */",
        "    /**\n     * 将 Netty Channel 格式化为 {@code ip:port} 连接键。\n     *\n     * @param channel Netty 通道\n     * @return 格式化后的连接键\n     */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/connection/NettyConnection.java"] = [
    (
        "/**\n * @author xuyue\n * @since 1.4.0\n */",
        "/**\n * 基于 Netty {@link Channel} 的 {@link Connection} 实现。\n *\n * @author xuyue\n * @since 1.4.0\n */",
    ),
    (
        "        // Remove from connection pool.",
        "        // 从连接池移除。",
    ),
    (
        "        // Close the connection.",
        "        // 关闭底层连接。",
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
