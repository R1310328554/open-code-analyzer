#!/usr/bin/env python3
"""Chinese-annotate Alibaba Sentinel 1.8.10 wave-15b block [15:30] (cluster-common codec)."""
from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path("/workspace")
ANALYZED = ROOT / "sentinel/1.8.10/analyzed"
BATCH_LIST = Path("/tmp/sentinel_w15b.txt").read_text(encoding="utf-8").strip().split("\n")

R: dict[str, list[tuple[str, str]]] = {}

R["sentinel-cluster/sentinel-cluster-common-default/src/main/java/com/alibaba/csp/sentinel/cluster/codec/EntityDecoder.java"] = [
    (
        "/**\n * @param <S> source stream type\n * @param <T> target entity type\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 实体解码器通用接口，将源流解码为目标实体。\n *\n * @param <S> 源流类型\n * @param <T> 目标实体类型\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    /**\n     * Decode target object from source stream.\n     *\n     * @param source source stream\n     * @return decoded target object\n     */",
        "    /**\n     * 从源流解码目标对象。\n     *\n     * @param source 源流\n     * @return 解码后的目标对象\n     */",
    ),
]

R["sentinel-cluster/sentinel-cluster-common-default/src/main/java/com/alibaba/csp/sentinel/cluster/codec/EntityWriter.java"] = [
    (
        "/**\n * A universal interface for publishing entities to a target stream.\n *\n * @param <E> entity type\n * @param <T> target stream type\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 将实体写入目标流的通用接口。\n *\n * @param <E> 实体类型\n * @param <T> 目标流类型\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    /**\n     * Write the provided entity to target stream.\n     *\n     * @param entity entity to publish\n     * @param target the target stream\n     */",
        "    /**\n     * 将给定实体写入目标流。\n     *\n     * @param entity 待发布的实体\n     * @param target 目标流\n     */",
    ),
]

R["sentinel-cluster/sentinel-cluster-common-default/src/main/java/com/alibaba/csp/sentinel/cluster/codec/request/RequestEntityDecoder.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群 {@link Request} 实体解码器接口，继承 {@link EntityDecoder}。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-common-default/src/main/java/com/alibaba/csp/sentinel/cluster/codec/request/RequestEntityWriter.java"] = [
    (
        "/**\n * A universal {@link EntityWriter} interface for publishing {@link Request} to a target stream.\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 将 {@link Request} 写入目标流的通用 {@link EntityWriter} 接口。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-common-default/src/main/java/com/alibaba/csp/sentinel/cluster/codec/response/ResponseEntityDecoder.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群 {@link Response} 实体解码器接口，继承 {@link EntityDecoder}。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-common-default/src/main/java/com/alibaba/csp/sentinel/cluster/codec/response/ResponseEntityWriter.java"] = [
    (
        "/**\n * A universal {@link EntityWriter} interface for publishing {@link Response} to a target stream.\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 将 {@link Response} 写入目标流的通用 {@link EntityWriter} 接口。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-common-default/src/main/java/com/alibaba/csp/sentinel/cluster/exception/SentinelClusterException.java"] = [
    (
        "/**\n * @author jialiang.ljl\n * @since 1.4.0\n */",
        "/**\n * Sentinel 集群模块异常，不填充堆栈以减轻开销。\n *\n * @author jialiang.ljl\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-common-default/src/main/java/com/alibaba/csp/sentinel/cluster/registry/ConfigSupplierRegistry.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群配置供应器注册表，管理命名空间 {@link Supplier} 的注册与获取。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    /**\n     * The default namespace supplier provides appName as namespace.\n     */",
        "    /**\n     * 默认命名空间供应器，以应用名作为命名空间。\n     */",
    ),
    (
        "    /**\n     * Registered namespace supplier.\n     */",
        "    /**\n     * 已注册的命名空间供应器。\n     */",
    ),
    (
        "    /**\n     * Get the registered namespace supplier.\n     *\n     * @return the registered namespace supplier\n     */",
        "    /**\n     * 获取已注册的命名空间供应器。\n     *\n     * @return 已注册的命名空间供应器\n     */",
    ),
]

R["sentinel-cluster/sentinel-cluster-common-default/src/main/java/com/alibaba/csp/sentinel/cluster/request/ClusterRequest.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群请求实体，封装请求 ID、类型与载荷数据。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-common-default/src/main/java/com/alibaba/csp/sentinel/cluster/request/Request.java"] = [
    (
        "/**\n * Cluster transport request interface.\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群传输请求接口。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    /**\n     * Get request type.\n     *\n     * @return request type\n     */",
        "    /**\n     * 获取请求类型。\n     *\n     * @return 请求类型\n     */",
    ),
    (
        "    /**\n     * Get request ID.\n     *\n     * @return unique request ID\n     */",
        "    /**\n     * 获取请求 ID。\n     *\n     * @return 唯一请求 ID\n     */",
    ),
]

R["sentinel-cluster/sentinel-cluster-common-default/src/main/java/com/alibaba/csp/sentinel/cluster/request/data/FlowRequestData.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 流控令牌请求载荷，包含规则 ID、申请数量与优先级标志。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-common-default/src/main/java/com/alibaba/csp/sentinel/cluster/request/data/ParamFlowRequestData.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 热点参数流控请求载荷，包含规则 ID、申请数量与参数列表。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-common-default/src/main/java/com/alibaba/csp/sentinel/cluster/response/ClusterResponse.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群响应实体，封装响应 ID、类型、状态码与载荷数据。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
]

R["sentinel-cluster/sentinel-cluster-common-default/src/main/java/com/alibaba/csp/sentinel/cluster/response/Response.java"] = [
    (
        "/**\n * Cluster transport response interface.\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 集群传输响应接口。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
    ),
    (
        "    /**\n     * Get response ID.\n     *\n     * @return response ID\n     */",
        "    /**\n     * 获取响应 ID。\n     *\n     * @return 响应 ID\n     */",
    ),
    (
        "    /**\n     * Get response type.\n     *\n     * @return response type\n     */",
        "    /**\n     * 获取响应类型。\n     *\n     * @return 响应类型\n     */",
    ),
    (
        "    /**\n     * Get response status.\n     *\n     * @return response status\n     */",
        "    /**\n     * 获取响应状态码。\n     *\n     * @return 响应状态码\n     */",
    ),
]

R["sentinel-cluster/sentinel-cluster-common-default/src/main/java/com/alibaba/csp/sentinel/cluster/response/data/FlowTokenResponseData.java"] = [
    (
        "/**\n * @author Eric Zhao\n * @since 1.4.0\n */",
        "/**\n * 流控令牌响应载荷，包含剩余配额与建议等待时间（毫秒）。\n *\n * @author Eric Zhao\n * @since 1.4.0\n */",
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
