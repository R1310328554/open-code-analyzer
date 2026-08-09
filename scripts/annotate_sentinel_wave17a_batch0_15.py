#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-17a block [0:15] (cluster-server codec/command)."""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path("/workspace")
ANALYZED = ROOT / "sentinel/1.8.10/analyzed"
BATCH_LIST = Path("/tmp/sentinel_w17a.txt").read_text(encoding="utf-8").strip().split("\n")

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/codec/DefaultResponseEntityWriter.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 默认集群响应实体写入器，先写响应头再按类型委托 {@link ResponseDataWriterRegistry} 序列化 payload。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "        if (responseDataWriter == null) {\n            writeHead(response.setStatus(ClusterConstants.RESPONSE_STATUS_BAD), out);",
        "        if (responseDataWriter == null) {\n            // 找不到匹配 writer 时返回 BAD 状态。\n            writeHead(response.setStatus(ClusterConstants.RESPONSE_STATUS_BAD), out);",
    ),
    (
        "    private void writeHead(Response response, ByteBuf out) {",
        "    /** 写入响应头：请求 id、消息类型与状态码。 */\n    private void writeHead(Response response, ByteBuf out) {",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/codec/ServerEntityCodecProvider.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群服务端编解码 SPI 提供者，在类加载时解析并缓存全局请求解码器与响应写入器。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    private static void resolveInstance() {",
        "    /** 通过 SPI 加载 {@link ResponseEntityWriter} 与 {@link RequestEntityDecoder} 实例。 */\n    private static void resolveInstance() {",
    ),
    (
        "    public static RequestEntityDecoder getRequestEntityDecoder() {",
        "    /** @return 全局请求实体解码器，未解析成功时为 null */\n    public static RequestEntityDecoder getRequestEntityDecoder() {",
    ),
    (
        "    public static ResponseEntityWriter getResponseEntityWriter() {",
        "    /** @return 全局响应实体写入器，未解析成功时为 null */\n    public static ResponseEntityWriter getResponseEntityWriter() {",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/codec/data/FlowRequestDataDecoder.java"] = [
    (
        "/**\n * <p>\n * Decoder for {@link FlowRequestData} from {@code ByteBuf} stream. The layout:\n * </p>\n * <pre>\n * | flow ID (8) | count (4) | priority flag (1) |\n * </pre>\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * <p>\n * 从 {@code ByteBuf} 流解码 {@link FlowRequestData}，字段布局如下：\n * </p>\n * <pre>\n * | flow ID (8) | count (4) | priority flag (1) |\n * </pre>\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/codec/data/FlowResponseDataWriter.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 流控令牌响应数据写入器，序列化剩余配额（remainingCount）与等待时间（waitInMs）。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/codec/data/ParamFlowRequestDataDecoder.java"] = [
    (
        "/**\n * @author jialiang.linjl\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 热点参数流控请求数据解码器，解析 flowId、count 及变长参数列表。\n *\n * @author jialiang.linjl\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    private boolean decodeParam(ByteBuf source, List<Object> params) {",
        "    /** 按类型标记解码单个参数值并追加到列表。 */\n    private boolean decodeParam(ByteBuf source, List<Object> params) {",
    ),
    (
        "                // TODO: take care of charset?",
        "                // TODO：需注意字符集？",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/codec/data/PingRequestDataDecoder.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * Ping 请求数据解码器，读取长度前缀的字符串 payload。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/codec/data/PingResponseDataWriter.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * Ping 响应数据写入器，将整型状态码写入 {@link ByteBuf}。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/codec/netty/NettyRequestDecoder.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * Netty 入站请求解码器，委托全局 {@link RequestEntityDecoder} 将 {@link ByteBuf} 转为 {@link Request}。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "        // TODO: handle decode error here.",
        "        // TODO：此处应处理解码错误。",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/codec/netty/NettyResponseEncoder.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * Netty 出站响应编码器，委托全局 {@link ResponseEntityWriter} 将 {@link ClusterResponse} 写入 {@link ByteBuf}。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    private void writeBadStatusHead(Response response, ByteBuf out) {",
        "    /** 无法解析全局 writer 时，仅写入 BAD 状态响应头。 */\n    private void writeBadStatusHead(Response response, ByteBuf out) {",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/codec/registry/RequestDataDecodeRegistry.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 请求 payload 解码器注册表，按消息类型查找 {@link EntityDecoder}。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    public static boolean addDecoder(int type, EntityDecoder<ByteBuf, ?> decoder) {",
        "    /** 注册指定类型的 payload 解码器；类型已存在时返回 false。 */\n    public static boolean addDecoder(int type, EntityDecoder<ByteBuf, ?> decoder) {",
    ),
    (
        "    public static EntityDecoder<ByteBuf, Object> getDecoder(int type) {",
        "    /** 按消息类型获取 payload 解码器。 */\n    public static EntityDecoder<ByteBuf, Object> getDecoder(int type) {",
    ),
    (
        "    public static boolean removeDecoder(int type) {",
        "    /** 移除指定类型的 payload 解码器。 */\n    public static boolean removeDecoder(int type) {",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/codec/registry/ResponseDataWriterRegistry.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 响应 payload 写入器注册表，按消息类型查找 {@link EntityWriter}。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    public static <T> boolean addWriter(int type, EntityWriter<T, ByteBuf> writer) {",
        "    /** 注册指定类型的 payload 写入器；类型已存在时返回 false。 */\n    public static <T> boolean addWriter(int type, EntityWriter<T, ByteBuf> writer) {",
    ),
    (
        "    public static EntityWriter<Object, ByteBuf> getWriter(int type) {",
        "    /** 按消息类型获取 payload 写入器。 */\n    public static EntityWriter<Object, ByteBuf> getWriter(int type) {",
    ),
    (
        "    public static boolean remove(int type) {",
        "    /** 移除指定类型的 payload 写入器。 */\n    public static boolean remove(int type) {",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/command/handler/FetchClusterFlowRulesCommandHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 获取集群流控规则的命令处理器；未指定 namespace 时返回全部规则。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/command/handler/FetchClusterMetricCommandHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.1\n */",
        "/**\n * 获取集群服务端指标快照的命令处理器，namespace 参数必填。\n *\n * @author Eric Zhao\n * @since 1.4.1\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/command/handler/FetchClusterParamFlowRulesCommandHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 获取集群热点参数流控规则的命令处理器；未指定 namespace 时返回全部规则。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-server-default/src/main/java/com/alibaba/csp/sentinel/cluster/server/command/handler/FetchClusterServerConfigHandler.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 获取集群服务端配置的命令处理器，支持全局配置与命名空间级流控配置。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    private CommandResponse<String> namespaceConfigResult(/*@NonEmpty*/ String namespace) {",
        "    /** 返回指定命名空间的流控配置 JSON。 */\n    private CommandResponse<String> namespaceConfigResult(/*@NonEmpty*/ String namespace) {",
    ),
    (
        "    private CommandResponse<String> globalConfigResult() {",
        "    /** 返回全局传输、流控配置及已注册命名空间集合。 */\n    private CommandResponse<String> globalConfigResult() {",
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
