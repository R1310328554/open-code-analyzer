#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-15a block [0:15] (cluster client config/handlers + cluster-common start)."""
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
BATCH_LIST = Path("/tmp/sentinel_w15a.txt").read_text(encoding="utf-8").strip().split("\n")
W15B_LIST = Path("/tmp/sentinel_w15b.txt").read_text(encoding="utf-8").strip().split("\n")

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/config/ClusterClientAssignConfig.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 集群客户端服务端分配配置，指定令牌服务器的主机与端口。\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/config/ClusterClientConfig.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群客户端全局配置，如请求超时时间等参数。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/config/ClusterClientConfigManager.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群客户端配置管理器，维护服务端分配与客户端参数，并监听动态配置变更。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    /**\n     * Client config properties.\n     */",
        "    /**\n     * 客户端配置属性（服务端地址、端口、超时等）。\n     */",
    ),
    (
        "    /**\n     * Apply new {@link ClusterClientConfig}, while the former config will be replaced.\n     *\n     * @param config new config to apply\n     */",
        "    /**\n     * 应用新的 {@link ClusterClientConfig}，将替换原有配置。\n     *\n     * @param config new config to apply\n     */",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/config/ClusterClientStartUpConfig.java"] = [
    (
        "/**\n * <p>\n * this class dedicated to reading startup configurations of cluster client\n * </p>\n *\n * @author lianglin\n * @since 1.7.0\n */",
        "/**\n * <p>\n * 集群客户端启动配置读取工具，从 Sentinel 全局配置中解析启动参数。\n * </p>\n *\n * @author lianglin\n * @since 1.7.0\n */",
    ),
    (
        "    /**\n     * Get the max bytes params can be serialized\n     *\n     * @return the max bytes, may be null\n     */",
        "    /**\n     * 获取热点参数序列化允许的最大字节数。\n     *\n     * @return the max bytes, may be null\n     */",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/config/ServerChangeObserver.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 远程令牌服务器地址变更观察者。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    /**\n     * Callback on remote server address change.\n     *\n     * @param assignConfig new cluster assignment config\n     */",
        "    /**\n     * 远程服务器地址变更时的回调。\n     *\n     * @param assignConfig new cluster assignment config\n     */",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/handler/TokenClientHandler.java"] = [
    (
        "/**\n * Netty client handler for Sentinel token client.\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * Sentinel 令牌客户端的 Netty 入站处理器，负责 Ping 与响应分发。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "        // Data body: namespace of the client.",
        "        // 数据体：客户端所属命名空间。",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/handler/TokenClientPromiseHolder.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 令牌客户端异步请求 Promise 持有者，按请求 ID 关联 {@link ChannelPromise} 与响应。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/init/DefaultClusterClientInitFunc.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群客户端默认初始化函数，注册 Ping、流控与热点参数请求的编解码器。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/command/entity/ClusterClientStateEntity.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 集群客户端状态实体，用于命令接口查询与修改客户端配置。\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/command/handler/FetchClusterClientConfigHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 获取集群客户端当前配置与连接状态的命令处理器。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/command/handler/ModifyClusterClientConfigHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 修改集群客户端配置的命令处理器，接收 JSON 并应用新配置。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-common-default/src/main/java/com/alibaba/csp/sentinel/cluster/ClusterConstants.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群模块通用常量，定义消息类型、响应状态、参数类型与默认端口/超时。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-common-default/src/main/java/com/alibaba/csp/sentinel/cluster/ClusterErrorMessages.java"] = [
    (
        "/**\n * @author jialiang.ljl\n * @since 1.4.0\n */",
        "/**\n * 集群模块错误消息常量。\n *\n * @author jialiang.ljl\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-common-default/src/main/java/com/alibaba/csp/sentinel/cluster/ClusterTransportClient.java"] = [
    (
        "/**\n * Synchronous transport client for distributed flow control.\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 分布式流控的同步传输客户端接口。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    /**\n     * Start the client.\n     *\n     * @throws Exception some error occurred (e.g. initialization failed)\n     */",
        "    /**\n     * 启动客户端。\n     *\n     * @throws Exception some error occurred (e.g. initialization failed)\n     */",
    ),
    (
        "    /**\n     * Stop the client.\n     *\n     * @throws Exception some error occurred (e.g. shutdown failed)\n     */",
        "    /**\n     * 停止客户端。\n     *\n     * @throws Exception some error occurred (e.g. shutdown failed)\n     */",
    ),
    (
        "    /**\n     * Send request to remote server and get response.\n     *\n     * @param request Sentinel cluster request\n     * @return response from remote server\n     * @throws Exception some error occurs\n     */",
        "    /**\n     * 向远程服务器发送请求并获取响应。\n     *\n     * @param request Sentinel cluster request\n     * @return response from remote server\n     * @throws Exception some error occurs\n     */",
    ),
    (
        "    /**\n     * Check whether the client has been started and ready for sending requests.\n     *\n     * @return true if the client is ready to send requests, otherwise false\n     */",
        "    /**\n     * 检查客户端是否已启动并可发送请求。\n     *\n     * @return true if the client is ready to send requests, otherwise false\n     */",
    ),
]

R["sentinel-cluster/sentinel-cluster-common-default/src/main/java/com/alibaba/csp/sentinel/cluster/annotation/RequestType.java"] = [
    (
        "/**\n * Request type annotation for handlers, codes, etc.\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 请求类型注解，用于标记处理器、编解码器等组件所处理的消息类型。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    /**\n     * Type of the request to handle.\n     *\n     * @return type of the request\n     */",
        "    /**\n     * 待处理请求的消息类型。\n     *\n     * @return type of the request\n     */",
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
    batch["files"] = W15B_LIST
    batch["done"] = batch.get("done", 420) + len(BATCH_LIST)
    batch["remaining_pending"] = batch.get("remaining_pending", 515) - len(BATCH_LIST)
    (QUEUE / "batch.json").write_text(json.dumps(batch, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    for rel in BATCH_LIST:
        apply_replacements(rel)
    subprocess.run([
        sys.executable, str(ROOT / "scripts/mark_batch_done.py"),
        "--project", "sentinel", "--version", "1.8.10",
        "--note", "wave15a cluster-client/common [0:15]",
        *BATCH_LIST,
    ], check=True)
    update_batch_json()
    print(f"Annotated {len(BATCH_LIST)} files")


if __name__ == "__main__":
    main()
