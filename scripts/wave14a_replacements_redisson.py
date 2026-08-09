"""Chinese annotation replacements for Redisson 4.7.0 wave-14a spring-data-22 [0:15]."""
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


W12B = _load("wave12b_replacements_redisson.py", "W12B_REPLACEMENTS")
W13A = _load("wave13a_replacements_redisson.py", "W13A_REPLACEMENTS")

W14A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

for _key in (
    "BinaryConvertor.java",
    "ByteBufferGeoResultsDecoder.java",
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
):
    W14A_REPLACEMENTS[_key] = W12B[_key]

for _key in (
    "RedissonReactiveClusterGeoCommands.java",
    "RedissonReactiveClusterHashCommands.java",
):
    W14A_REPLACEMENTS[_key] = W13A[_key]
