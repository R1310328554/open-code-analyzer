"""Chinese annotation replacements for Redisson 4.7.0 wave-19a spring-data-25 reactive [0:15]."""
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


W14B = _load("wave14b_replacements_redisson.py", "W14B_REPLACEMENTS")
W15A = _load("wave15a_replacements_redisson.py", "W15A_REPLACEMENTS")
W16B = _load("wave16b_replacements_redisson.py", "W16B_REPLACEMENTS")

W19A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-25: reuse wave-14b (sources identical to spring-data-22/24 for these files).
for _key in (
    "RedissonReactiveClusterListCommands.java",
    "RedissonReactiveClusterNumberCommands.java",
    "RedissonReactiveClusterServerCommands.java",
    "RedissonReactiveClusterSetCommands.java",
    "RedissonReactiveClusterStreamCommands.java",
    "RedissonReactiveClusterStringCommands.java",
    "RedissonReactiveClusterZSetCommands.java",
    "RedissonReactiveGeoCommands.java",
    "RedissonReactiveHashCommands.java",
    "RedissonReactiveHyperLogLogCommands.java",
    "RedissonReactiveKeyCommands.java",
    "RedissonReactiveListCommands.java",
    "RedissonReactiveNumberCommands.java",
):
    W19A_REPLACEMENTS[_key] = W14B[_key]

W19A_REPLACEMENTS["RedissonReactivePubSubCommands.java"] = W15A[
    "RedissonReactivePubSubCommands.java"
]

# spring-data-25: extended cluster admin API (same as spring-data-24).
W19A_REPLACEMENTS["RedissonReactiveRedisClusterConnection.java"] = W16B[
    "RedissonReactiveRedisClusterConnection.java"
]
