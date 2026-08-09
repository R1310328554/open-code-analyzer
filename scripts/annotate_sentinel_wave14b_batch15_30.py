#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-14b block [15:30] (cluster client-default)."""
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
BATCH_LIST = Path("/tmp/sentinel_w14b.txt").read_text(encoding="utf-8").strip().split("\n")

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/ClientConstants.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群令牌客户端常量，定义消息类型与客户端状态码。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/DefaultClusterTokenClient.java"] = [
    (
        "/**\n * Default implementation of {@link ClusterTokenClient}.\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * {@link ClusterTokenClient} 的默认实现。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "            // Replace with new, even if the new client is not ready.",
        "            // 即使新客户端尚未就绪，也替换为新连接。",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/NettyTransportClient.java"] = [
    (
        "/**\n * Netty transport client implementation for Sentinel cluster transport.\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * Sentinel 集群传输的 Netty 客户端实现。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "        // Stop retrying for connection.",
        "        // 停止连接重试。",
    ),
    (
        "                // Ignore.",
        "                // 忽略。",
    ),
    (
        "                // Should not go through here.",
        "                // 不应执行到此处。",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/codec/ClientEntityCodecProvider.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群客户端实体编解码器提供者，通过 SPI 加载请求写入器与响应解码器。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/codec/DefaultRequestEntityWriter.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 默认集群请求实体写入器，将 {@link ClusterRequest} 序列化为 {@link ByteBuf}。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "        // Write head part of request.",
        "        // 写入请求头部分。",
    ),
    (
        "        // Write data part.",
        "        // 写入请求数据部分。",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/codec/DefaultResponseEntityDecoder.java"] = [
    (
        "/**\n * <p>Default entity decoder for any {@link ClusterResponse} entity.</p>\n *\n * <p>Decode format:</p>\n * <pre>\n * +--------+---------+-----------+---------+\n * | xid(4) | type(1) | status(1) | data... |\n * +--------+---------+-----------+---------+\n * </pre>\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * <p>任意 {@link ClusterResponse} 实体的默认解码器。</p>\n *\n * <p>解码格式：</p>\n * <pre>\n * +--------+---------+-----------+---------+\n * | xid(4) | type(1) | status(1) | data... |\n * +--------+---------+-----------+---------+\n * </pre>\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/codec/data/FlowRequestDataWriter.java"] = [
    (
        "/**\n * +-------------------+--------------+----------------+---------------+------------------+\n * | RequestID(8 byte) | Type(1 byte) | FlowID(8 byte) | Count(4 byte) | PriorityFlag (1) |\n * +-------------------+--------------+----------------+---------------+------------------+\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 流控令牌请求数据写入器，二进制布局如下：\n * <pre>\n * +-------------------+--------------+----------------+---------------+------------------+\n * | RequestID(8 byte) | Type(1 byte) | FlowID(8 byte) | Count(4 byte) | PriorityFlag (1) |\n * +-------------------+--------------+----------------+---------------+------------------+\n * </pre>\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/codec/data/FlowResponseDataDecoder.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 流控令牌响应数据解码器，解析剩余配额与等待时间。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/codec/data/ParamFlowRequestDataWriter.java"] = [
    (
        "/**\n * @author jialiang.linjl\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 热点参数流控请求数据写入器，将参数列表序列化为 {@link ByteBuf}。\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "        // Serialize parameters with type flag.",
        "        // 按类型标志序列化各参数。",
    ),
    (
        "    /**\n     * Get valid parameters in provided parameter list\n     *\n     * @param params\n     * @return\n     */",
        "    /**\n     * 从给定参数列表中筛选可传输的有效参数。\n     *\n     * @param params\n     * @return\n     */",
    ),
    (
        "        // Handle primitive type.",
        "        // 处理基本类型。",
    ),
    (
        "            // Unexpected type, drop.",
        "            // 未知类型，丢弃。",
    ),
    (
        "        // Layout for primitives: |type flag(1)|value|\n        // size = original size + type flag (1)",
        "        // 基本类型布局：|type flag(1)|value|\n        // 大小 = 原始大小 + 类型标志(1)",
    ),
    (
        "            // Layout for string: |type flag(1)|length(4)|string content|",
        "            // 字符串布局：|type flag(1)|length(4)|string content|",
    ),
    (
        "            // Ignore unexpected type.",
        "            // 忽略未知类型。",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/codec/data/PingRequestDataWriter.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * Ping 请求数据写入器，将字符串载荷写入 {@link ByteBuf}。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/codec/data/PingResponseDataDecoder.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * Ping 响应数据解码器，解析服务端返回的状态码。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "            // Compatible with old version (< 1.7.0).",
        "            // 兼容旧版本（< 1.7.0）。",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/codec/netty/NettyRequestEncoder.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * Netty 集群请求编码器，将 {@link ClusterRequest} 编码为字节流。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/codec/netty/NettyResponseDecoder.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * Netty 集群响应解码器，将字节流解码为 {@link Response}。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "        // TODO: handle decode error here.",
        "        // TODO: 在此处理解码错误。",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/codec/registry/RequestDataWriterRegistry.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 请求数据写入器注册表，按消息类型映射 {@link EntityWriter}。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-client-default/src/main/java/com/alibaba/csp/sentinel/cluster/client/codec/registry/ResponseDataDecodeRegistry.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 响应数据解码器注册表，按消息类型映射 {@link EntityDecoder}。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
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
    batch["done"] = batch.get("done", 390) + len(BATCH_LIST)
    batch["remaining_pending"] = batch.get("remaining_pending", 545) - len(BATCH_LIST)
    (QUEUE / "batch.json").write_text(json.dumps(batch, indent=2) + "\n", encoding="utf-8")


def main() -> None:
    for rel in BATCH_LIST:
        apply_replacements(rel)
    subprocess.run([
        sys.executable, str(ROOT / "scripts/mark_batch_done.py"),
        "--project", "sentinel", "--version", "1.8.10",
        "--note", "wave14b cluster-client [15:30]",
        *BATCH_LIST,
    ], check=True)
    update_batch_json()
    print(f"Annotated {len(BATCH_LIST)} files")


if __name__ == "__main__":
    main()
