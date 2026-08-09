"""Chinese annotation replacements for Redisson 4.7.0 wave-35a spring-data-41 [0:15]."""
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


W20A = _load("wave20a_replacements_redisson.py", "W20A_REPLACEMENTS")
W28B = _load("wave28b_replacements_redisson.py", "W28B_REPLACEMENTS")
W30A = _load("wave30a_replacements_redisson.py", "W30A_REPLACEMENTS")
W30B = _load("wave30b_replacements_redisson.py", "W30B_REPLACEMENTS")

_SD41 = "redisson-spring/redisson-spring-data/redisson-spring-data-41/src/main/java/org/redisson/spring/data/connection/"

W35A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-41: decoders identical to spring-data-40/35/34.
for _name in (
    "ObjectListReplayDecoder2.java",
    "ObjectMapEntryReplayDecoder.java",
    "PointDecoder.java",
):
    W35A_REPLACEMENTS[_name] = W20A[_name]
    W35A_REPLACEMENTS[_SD41 + _name] = W20A[_name]

# spring-data-41: connection layer identical to spring-data-40/35/34.
for _name in (
    "PropertiesDecoder.java",
    "PropertiesListDecoder.java",
    "RedisClusterNodeDecoder.java",
    "RedissonConnectionFactory.java",
    "RedissonExceptionConverter.java",
    "RedissonReactiveClusterGeoCommands.java",
    "RedissonReactiveClusterHashCommands.java",
    "RedissonReactiveClusterHyperLogLogCommands.java",
):
    W35A_REPLACEMENTS[_name] = W30A[_name]
    W35A_REPLACEMENTS[_SD41 + _name] = W30A[_name]

# spring-data-41: getEntry() is package-private (no protected modifier).
W35A_REPLACEMENTS["RedissonBaseReactive.java"] = W28B["RedissonBaseReactive.java"]
W35A_REPLACEMENTS[_SD41 + "RedissonBaseReactive.java"] = W28B["RedissonBaseReactive.java"]

# spring-data-41: cluster reactive adapters identical to spring-data-40/35/34.
for _name in (
    "RedissonReactiveClusterListCommands.java",
    "RedissonReactiveClusterNumberCommands.java",
):
    W35A_REPLACEMENTS[_name] = W30B[_name]
    W35A_REPLACEMENTS[_SD41 + _name] = W30B[_name]

# spring-data-41: cluster Key adds keys(Publisher) and mUnlink overloads.
_SD41_KEY_EXTRA: list[tuple[str, str]] = [
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

W35A_REPLACEMENTS["RedissonReactiveClusterKeyCommands.java"] = list(
    W30B["RedissonReactiveClusterKeyCommands.java"]
) + _SD41_KEY_EXTRA
W35A_REPLACEMENTS[_SD41 + "RedissonReactiveClusterKeyCommands.java"] = W35A_REPLACEMENTS[
    "RedissonReactiveClusterKeyCommands.java"
]
