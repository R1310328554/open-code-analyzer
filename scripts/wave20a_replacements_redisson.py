"""Chinese annotation replacements for Redisson 4.7.0 wave-20a spring-data-26 [0:15]."""
from __future__ import annotations

import importlib.util
from pathlib import Path

SCRIPTS = Path(__file__).resolve().parent


def _load(module_file: str, attr: str) -> dict[str, list[tuple[str, str]]]:
    spec = importlib.util.spec_from_file_location(module_file, SCRIPTS / module_file)
    mod = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(mod)
    return getattr(mod, attr)


W17A = _load("wave17a_replacements_redisson.py", "W17A_REPLACEMENTS")

W20A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-26: sources identical to spring-data-24/25 for these files.
for _key in (
    "DistanceConvertor.java",
    "GeoResultsDecoder.java",
    "ObjectListReplayDecoder2.java",
    "PointDecoder.java",
    "PropertiesDecoder.java",
    "PropertiesListDecoder.java",
    "RedisClusterNodeDecoder.java",
    "RedissonBaseReactive.java",
    "RedissonConnectionFactory.java",
    "RedissonExceptionConverter.java",
    "RedissonReactiveClusterGeoCommands.java",
    "RedissonReactiveClusterHashCommands.java",
    "RedissonReactiveClusterHyperLogLogCommands.java",
):
    W20A_REPLACEMENTS[_key] = W17A[_key]

# spring-data-26 only: nested list flatten decoder.
W20A_REPLACEMENTS["ListMergeDecoder.java"] = [
    (
        "/**\n *\n * @author Nikita Koksharov\n *\n * @param <T> type\n */",
        "/**\n"
        " * 嵌套列表合并解码器：将多层 {@link List} 响应扁平化为单层列表。\n"
        " * <p>用于 Redis 返回嵌套数组时，通过 {@code flatMap} 合并各子列表元素。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " * @param <T> type\n"
        " */",
    ),
    (
        "    @Override\n    public List<Object> decode(List<Object> parts, State state) {",
        "    /** 将 parts 中每个嵌套列表的元素合并为扁平 {@link List}。 */\n"
        "    @Override\n"
        "    public List<Object> decode(List<Object> parts, State state) {",
    ),
]

# spring-data-26 only: map entry replay decoder for Spring Data connection layer.
W20A_REPLACEMENTS["ObjectMapEntryReplayDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * Map 键值对回放解码器：将 Redis 扁平 key-value 序列解析为 {@link Entry} 列表。\n"
        " * <p>奇偶位分别使用 Codec 的 map key/value 解码器；结果保持插入顺序。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    @Override\n    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {",
        "    /** 奇数位返回 value 解码器，偶数位返回 key 解码器。 */\n"
        "    @Override\n"
        "    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {",
    ),
    (
        "        if (paramNum % 2 != 0) {",
        "        // 奇数索引为 value，偶数索引为 key。\n"
        "        if (paramNum % 2 != 0) {",
    ),
    (
        "    @Override\n    public List<Entry<Object, Object>> decode(List<Object> parts, State state) {",
        "    /** 将扁平 parts 按相邻 key-value 对组装为有序条目列表。 */\n"
        "    @Override\n"
        "    public List<Entry<Object, Object>> decode(List<Object> parts, State state) {",
    ),
    (
        "            if (i % 2 != 0) {",
        "            // 每两个元素构成一对 key-value。\n"
        "            if (i % 2 != 0) {",
    ),
]
