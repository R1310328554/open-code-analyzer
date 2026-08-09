"""Chinese annotation replacements for Redisson 4.7.0 wave-21b spring-data-26/27 [15:30]."""
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


W11A = _load("wave11a_replacements_redisson.py", "W11A_REPLACEMENTS")
W20A = _load("wave20a_replacements_redisson.py", "W20A_REPLACEMENTS")

W21B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-26/27: decoders and convertors identical across sd-24..27.
for _key in (
    "SetReplayDecoder.java",
    "BinaryConvertor.java",
    "ByteBufferGeoResultsDecoder.java",
    "DataTypeConvertor.java",
    "DistanceConvertor.java",
    "GeoResultsDecoder.java",
    "ObjectListReplayDecoder2.java",
    "PointDecoder.java",
    "PropertiesDecoder.java",
    "PropertiesListDecoder.java",
    "RedisClusterNodeDecoder.java",
    "RedissonBaseReactive.java",
    "ListMergeDecoder.java",
    "ObjectMapEntryReplayDecoder.java",
):
    W21B_REPLACEMENTS[_key] = W11A.get(_key) or W20A[_key]

W21B_REPLACEMENTS["SetReplayDecoder.java"] = W11A["SetReplayDecoder.java"]
W21B_REPLACEMENTS["BinaryConvertor.java"] = W11A["BinaryConvertor.java"]
W21B_REPLACEMENTS["ByteBufferGeoResultsDecoder.java"] = W11A["ByteBufferGeoResultsDecoder.java"]
W21B_REPLACEMENTS["DataTypeConvertor.java"] = W11A["DataTypeConvertor.java"]
W21B_REPLACEMENTS["DistanceConvertor.java"] = W20A["DistanceConvertor.java"]
W21B_REPLACEMENTS["GeoResultsDecoder.java"] = W20A["GeoResultsDecoder.java"]
W21B_REPLACEMENTS["ObjectListReplayDecoder2.java"] = W20A["ObjectListReplayDecoder2.java"]
W21B_REPLACEMENTS["PointDecoder.java"] = W20A["PointDecoder.java"]
W21B_REPLACEMENTS["PropertiesDecoder.java"] = W20A["PropertiesDecoder.java"]
W21B_REPLACEMENTS["PropertiesListDecoder.java"] = W20A["PropertiesListDecoder.java"]
W21B_REPLACEMENTS["RedisClusterNodeDecoder.java"] = W20A["RedisClusterNodeDecoder.java"]
W21B_REPLACEMENTS["RedissonBaseReactive.java"] = W20A["RedissonBaseReactive.java"]
W21B_REPLACEMENTS["ListMergeDecoder.java"] = W20A["ListMergeDecoder.java"]
W21B_REPLACEMENTS["ObjectMapEntryReplayDecoder.java"] = W20A["ObjectMapEntryReplayDecoder.java"]

# spring-data-26 only: single map entry decoder.
W21B_REPLACEMENTS["SingleMapEntryDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 单条 Map 键值对解码器：将 Redis 返回的 key-value 二元组解析为 {@link Entry}。\n"
        " * <p>奇偶位分别使用 Codec 的 map key/value 解码器。\n"
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
        "    @Override\n    public Entry<Object, Object> decode(List<Object> parts, State state) {",
        "    /** 将 parts 的前两个元素组装为 {@link AbstractMap.SimpleEntry}。 */\n"
        "    @Override\n"
        "    public Entry<Object, Object> decode(List<Object> parts, State state) {",
    ),
]
