"""Chinese annotation replacements for Redisson 4.7.0 wave-17a spring-data-24 [0:15]."""
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


W12B = _load("wave12b_replacements_redisson.py", "W12B_REPLACEMENTS")
W13A = _load("wave13a_replacements_redisson.py", "W13A_REPLACEMENTS")

W17A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

for _key in (
    "ByteBufferGeoResultsDecoder.java",
    "DataTypeConvertor.java",
    "DistanceConvertor.java",
    "GeoResultsDecoder.java",
    "ObjectListReplayDecoder2.java",
    "PointDecoder.java",
    "PropertiesListDecoder.java",
    "RedisClusterNodeDecoder.java",
    "RedissonBaseReactive.java",
    "RedissonConnectionFactory.java",
    "RedissonExceptionConverter.java",
):
    W17A_REPLACEMENTS[_key] = W12B[_key]

# spring-data-24 PropertiesDecoder: simplified line split (no trailing \r trim).
W17A_REPLACEMENTS["PropertiesDecoder.java"] = [
    (
        "/**\n * \n * @author Nikita Koksharov\n *\n */",
        "/**\n"
        " * 将 Redis INFO/CONFIG 类 colon 分隔文本解码为 {@link Properties}。\n"
        " * <p>按 {@code \\r\\n} 或 {@code \\n} 分行，解析 {@code key:value} 对写入属性表。\n"
        " *\n"
        " * @author Nikita Koksharov\n"
        " *\n"
        " */",
    ),
    (
        "    @Override\n    public Properties decode(ByteBuf buf, State state) {",
        "    /** 从 Netty {@link ByteBuf} 读取 UTF-8 文本并解析为属性表。 */\n"
        "    @Override\n"
        "    public Properties decode(ByteBuf buf, State state) {",
    ),
    (
        "        for (String entry : value.split(\"\\r\\n|\\n\")) {",
        "        // 兼容 Unix/Windows 换行符分行。\n"
        "        for (String entry : value.split(\"\\r\\n|\\n\")) {",
    ),
]

for _key in (
    "RedissonReactiveClusterGeoCommands.java",
    "RedissonReactiveClusterHashCommands.java",
    "RedissonReactiveClusterHyperLogLogCommands.java",
):
    W17A_REPLACEMENTS[_key] = W13A[_key]
