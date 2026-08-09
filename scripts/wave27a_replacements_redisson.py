"""Chinese annotation replacements for Redisson 4.7.0 wave-27a spring-data-32 [0:15]."""
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
W20B = _load("wave20b_replacements_redisson.py", "W20B_REPLACEMENTS")
W11B = _load("wave11b_replacements_redisson.py", "W11B_REPLACEMENTS")

W27A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-32: sources identical to spring-data-26/27 for these files.
for _key in (
    "RedissonConnectionFactory.java",
    "RedissonExceptionConverter.java",
    "RedissonReactiveClusterGeoCommands.java",
    "RedissonReactiveClusterHashCommands.java",
    "RedissonReactiveClusterHyperLogLogCommands.java",
    "RedissonReactiveClusterKeyCommands.java",
    "RedissonReactiveClusterListCommands.java",
    "RedissonReactiveClusterNumberCommands.java",
    "RedissonReactiveClusterSetCommands.java",
    "RedissonReactiveClusterStreamCommands.java",
    "RedissonReactiveClusterStringCommands.java",
    "RedissonReactiveClusterZSetCommands.java",
    "RedissonReactiveGeoCommands.java",
    "RedissonReactiveHashCommands.java",
):
    W27A_REPLACEMENTS[_key] = W20A.get(_key) or W20B[_key]

# spring-data-32: cluster Server adds FlushOption overloads for FLUSHDB/FLUSHALL.
W27A_REPLACEMENTS["RedissonReactiveClusterServerCommands.java"] = list(
    W11B["RedissonReactiveClusterServerCommands.java"]
) + [
    (
        "    @Override\n    public Mono<String> flushDb(RedisClusterNode node, RedisServerCommands.FlushOption option) {",
        "    /** 在指定节点执行 FLUSHDB，{@link RedisServerCommands.FlushOption#ASYNC} 时追加 ASYNC 参数。 */\n"
        "    @Override\n"
        "    public Mono<String> flushDb(RedisClusterNode node, RedisServerCommands.FlushOption option) {",
    ),
    (
        "    @Override\n    public Mono<String> flushAll(RedisClusterNode node, RedisServerCommands.FlushOption option) {",
        "    /** 在指定节点执行 FLUSHALL，{@link RedisServerCommands.FlushOption#ASYNC} 时追加 ASYNC 参数。 */\n"
        "    @Override\n"
        "    public Mono<String> flushAll(RedisClusterNode node, RedisServerCommands.FlushOption option) {",
    ),
]
