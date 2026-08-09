"""Chinese annotation replacements for Redisson 4.7.0 wave-17b spring-data-24 reactive [15:30]."""
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

W17B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-24: reuse wave-14b (sources identical to spring-data-22/23 for these files).
for _key in (
    "RedissonReactiveClusterKeyCommands.java",
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
    W17B_REPLACEMENTS[_key] = W14B[_key]

W17B_REPLACEMENTS["RedissonReactivePubSubCommands.java"] = W15A[
    "RedissonReactivePubSubCommands.java"
]
