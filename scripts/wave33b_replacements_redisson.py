"""Chinese annotation replacements for Redisson 4.7.0 wave-33b spring-data-40 [15:30]."""
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


W28B = _load("wave28b_replacements_redisson.py", "W28B_REPLACEMENTS")
W30A = _load("wave30a_replacements_redisson.py", "W30A_REPLACEMENTS")
W30B = _load("wave30b_replacements_redisson.py", "W30B_REPLACEMENTS")

_SD40 = "redisson-spring/redisson-spring-data/redisson-spring-data-40/src/main/java/org/redisson/spring/data/connection/"

W33B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-40: decoders/convertors identical to spring-data-35/34.
for _name in (
    "PropertiesDecoder.java",
    "PropertiesListDecoder.java",
):
    W33B_REPLACEMENTS[_name] = W30A[_name]
    W33B_REPLACEMENTS[_SD40 + _name] = W30A[_name]

# spring-data-40: connection layer identical to spring-data-35/34.
for _name in (
    "RedisClusterNodeDecoder.java",
    "RedissonConnectionFactory.java",
    "RedissonExceptionConverter.java",
    "RedissonReactiveClusterGeoCommands.java",
    "RedissonReactiveClusterHashCommands.java",
    "RedissonReactiveClusterHyperLogLogCommands.java",
):
    W33B_REPLACEMENTS[_name] = W30A[_name]
    W33B_REPLACEMENTS[_SD40 + _name] = W30A[_name]

# spring-data-40: getEntry() is package-private (no protected modifier).
W33B_REPLACEMENTS["RedissonBaseReactive.java"] = W28B["RedissonBaseReactive.java"]
W33B_REPLACEMENTS[_SD40 + "RedissonBaseReactive.java"] = W28B["RedissonBaseReactive.java"]

# spring-data-40: cluster reactive adapters identical to spring-data-35/34.
for _name in (
    "RedissonReactiveClusterListCommands.java",
    "RedissonReactiveClusterNumberCommands.java",
    "RedissonReactiveClusterServerCommands.java",
    "RedissonReactiveClusterSetCommands.java",
    "RedissonReactiveClusterStreamCommands.java",
):
    W33B_REPLACEMENTS[_name] = W30B[_name]
    W33B_REPLACEMENTS[_SD40 + _name] = W30B[_name]

# spring-data-40: cluster Key adds keys(Publisher) and mUnlink overloads.
_SD40_KEY_EXTRA: list[tuple[str, str]] = [
    (
        "    @Override\n    public Flux<MultiValueResponse<ByteBuffer, ByteBuffer>> keys(Publisher<ByteBuffer> patterns) {",
        "    /** 集群 KEYS：跨 master 合并匹配结果（生产环境慎用）。 */\n"
        "    @Override\n"
        "    public Flux<MultiValueResponse<ByteBuffer, ByteBuffer>> keys(Publisher<ByteBuffer> patterns) {",
    ),
    (
        "    @Override\n    public Flux<NumericResponse<List<ByteBuffer>, Long>> mUnlink(Publisher<List<ByteBuffer>> keys) {",
        "    /** 批量 UNLINK：一次异步删除多个 key 并汇总删除数量。 */\n"
        "    @Override\n"
        "    public Flux<NumericResponse<List<ByteBuffer>, Long>> mUnlink(Publisher<List<ByteBuffer>> keys) {",
    ),
]

W33B_REPLACEMENTS["RedissonReactiveClusterKeyCommands.java"] = list(
    W30B["RedissonReactiveClusterKeyCommands.java"]
) + _SD40_KEY_EXTRA
W33B_REPLACEMENTS[_SD40 + "RedissonReactiveClusterKeyCommands.java"] = W33B_REPLACEMENTS[
    "RedissonReactiveClusterKeyCommands.java"
]
