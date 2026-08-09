"""Chinese annotation replacements for Redisson 4.7.0 wave-29a spring-data-33 [0:15]."""
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


W27A = _load("wave27a_replacements_redisson.py", "W27A_REPLACEMENTS")
W27B = _load("wave27b_replacements_redisson.py", "W27B_REPLACEMENTS")

_SD33 = "redisson-spring/redisson-spring-data/redisson-spring-data-33/src/main/java/org/redisson/spring/data/connection/"

W29A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-33: sources identical to spring-data-32 for these reactive adapters.
for _name in (
    "RedissonReactiveClusterStreamCommands.java",
    "RedissonReactiveClusterStringCommands.java",
    "RedissonReactiveClusterZSetCommands.java",
    "RedissonReactiveGeoCommands.java",
    "RedissonReactiveHashCommands.java",
):
    W29A_REPLACEMENTS[_name] = W27A[_name]
    W29A_REPLACEMENTS[_SD33 + _name] = W27A[_name]

for _name in (
    "RedissonReactiveHyperLogLogCommands.java",
    "RedissonReactiveKeyCommands.java",
    "RedissonReactiveListCommands.java",
    "RedissonReactiveNumberCommands.java",
    "RedissonReactivePubSubCommands.java",
    "RedissonReactiveRedisClusterConnection.java",
    "RedissonReactiveRedisConnection.java",
    "RedissonReactiveScriptingCommands.java",
    "RedissonReactiveServerCommands.java",
    "RedissonReactiveSetCommands.java",
):
    W29A_REPLACEMENTS[_name] = W27B[_name]
    W29A_REPLACEMENTS[_SD33 + _name] = W27B[_name]
