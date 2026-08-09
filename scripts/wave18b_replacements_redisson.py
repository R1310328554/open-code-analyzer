"""Chinese annotation replacements for Redisson 4.7.0 wave-18b spring-data-25 [15:30]."""
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


W17A = _load("wave17a_replacements_redisson.py", "W17A_REPLACEMENTS")
W17B = _load("wave17b_replacements_redisson.py", "W17B_REPLACEMENTS")

W18B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-25: sources identical to spring-data-24 for these files.
for _key in (
    "DataTypeConvertor.java",
    "DistanceConvertor.java",
    "GeoResultsDecoder.java",
    "ObjectListReplayDecoder2.java",
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
    W18B_REPLACEMENTS[_key] = W17A[_key]

W18B_REPLACEMENTS["RedissonReactiveClusterKeyCommands.java"] = W17B[
    "RedissonReactiveClusterKeyCommands.java"
]
