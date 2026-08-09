"""Chinese annotation replacements for Redisson 4.7.0 wave-12b spring-data-20/21 [15:30]."""
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


W11A = _load("wave11a_replacements_redisson.py", "W11A_REPLACEMENTS")
W11B = _load("wave11b_replacements_redisson.py", "W11B_REPLACEMENTS")

W12B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

for _key in (
    "SecondsConvertor.java",
    "SetReplayDecoder.java",
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
):
    W12B_REPLACEMENTS[_key] = W11A[_key]

for _key in (
    "RedissonBaseReactive.java",
    "RedissonConnectionFactory.java",
    "RedissonExceptionConverter.java",
):
    W12B_REPLACEMENTS[_key] = W11B[_key]
