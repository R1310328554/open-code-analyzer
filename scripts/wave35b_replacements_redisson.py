"""Chinese annotation replacements for Redisson 4.7.0 wave-35b spring-data-41 [15:30]."""
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


W33B = _load("wave33b_replacements_redisson.py", "W33B_REPLACEMENTS")
W34A = _load("wave34a_replacements_redisson.py", "W34A_REPLACEMENTS")

_SD41 = "redisson-spring/redisson-spring-data/redisson-spring-data-41/src/main/java/org/redisson/spring/data/connection/"

W35B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-41: cluster server/set/stream identical to spring-data-40 (wave-33b/30b).
for _name in (
    "RedissonReactiveClusterServerCommands.java",
    "RedissonReactiveClusterSetCommands.java",
    "RedissonReactiveClusterStreamCommands.java",
):
    W35B_REPLACEMENTS[_name] = W33B[_name]
    W35B_REPLACEMENTS[_SD41 + _name] = W33B[_name]

# spring-data-41: cluster string/zset and standalone reactive commands identical to spring-data-40 (wave-34a).
for _name in (
    "RedissonReactiveClusterStringCommands.java",
    "RedissonReactiveClusterZSetCommands.java",
    "RedissonReactiveGeoCommands.java",
    "RedissonReactiveHyperLogLogCommands.java",
    "RedissonReactiveListCommands.java",
    "RedissonReactiveNumberCommands.java",
    "RedissonReactivePubSubCommands.java",
    "RedissonReactiveRedisClusterConnection.java",
    "RedissonReactiveRedisConnection.java",
    "RedissonReactiveScriptingCommands.java",
    "RedissonReactiveServerCommands.java",
    "RedissonReactiveSetCommands.java",
):
    W35B_REPLACEMENTS[_name] = W34A[_name]
    W35B_REPLACEMENTS[_SD41 + _name] = W34A[_name]
