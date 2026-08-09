"""Chinese annotation replacements for Redisson 4.7.0 wave-30a spring-data-34 [0:15]."""
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
W23B = _load("wave23b_replacements_redisson.py", "W23B_REPLACEMENTS")
W28B = _load("wave28b_replacements_redisson.py", "W28B_REPLACEMENTS")

_SD34 = "redisson-spring/redisson-spring-data/redisson-spring-data-34/src/main/java/org/redisson/spring/data/connection/"

W30A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-34: decoders/convertors identical to spring-data-26/33.
for _name in (
    "DistanceConvertor.java",
    "GeoResultsDecoder.java",
    "ListMergeDecoder.java",
    "ObjectListReplayDecoder2.java",
    "ObjectMapEntryReplayDecoder.java",
    "PointDecoder.java",
    "PropertiesDecoder.java",
    "PropertiesListDecoder.java",
):
    W30A_REPLACEMENTS[_name] = W20A[_name]
    W30A_REPLACEMENTS[_SD34 + _name] = W20A[_name]

# spring-data-34: connection layer identical to spring-data-30/33.
for _name in (
    "RedisClusterNodeDecoder.java",
    "RedissonConnectionFactory.java",
    "RedissonExceptionConverter.java",
    "RedissonReactiveClusterGeoCommands.java",
    "RedissonReactiveClusterHashCommands.java",
    "RedissonReactiveClusterHyperLogLogCommands.java",
):
    W30A_REPLACEMENTS[_name] = W23B[_name]
    W30A_REPLACEMENTS[_SD34 + _name] = W23B[_name]

# spring-data-34: getEntry() is package-private (no protected modifier).
W30A_REPLACEMENTS["RedissonBaseReactive.java"] = W28B["RedissonBaseReactive.java"]
W30A_REPLACEMENTS[_SD34 + "RedissonBaseReactive.java"] = W28B["RedissonBaseReactive.java"]
