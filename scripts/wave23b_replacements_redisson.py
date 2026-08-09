"""Chinese annotation replacements for Redisson 4.7.0 wave-23b spring-data-30 [15:30]."""
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
W22A = _load("wave22a_replacements_redisson.py", "W22A_REPLACEMENTS")

W23B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-30: decoders & connection layer (identical to spring-data-26/27).
for _key in (
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
    W23B_REPLACEMENTS[_key] = list(W20A[_key])

# spring-data-30: cluster reactive adapters (identical to spring-data-27).
for _key in (
    "RedissonReactiveClusterKeyCommands.java",
    "RedissonReactiveClusterListCommands.java",
    "RedissonReactiveClusterNumberCommands.java",
    "RedissonReactiveClusterSetCommands.java",
):
    W23B_REPLACEMENTS[_key] = list(W22A[_key])

# spring-data-30: cluster Server adds FlushOption overloads for FLUSHDB/FLUSHALL.
W23B_REPLACEMENTS["RedissonReactiveClusterServerCommands.java"] = list(
    W22A["RedissonReactiveClusterServerCommands.java"]
)
