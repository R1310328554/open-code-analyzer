"""Chinese annotation replacements for Redisson 4.7.0 wave-33a spring-data-35/40 [0:15]."""
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
W20A = _load("wave20a_replacements_redisson.py", "W20A_REPLACEMENTS")
W21B = _load("wave21b_replacements_redisson.py", "W21B_REPLACEMENTS")
W23A = _load("wave23a_replacements_redisson.py", "W23A_REPLACEMENTS")
W28A = _load("wave28a_replacements_redisson.py", "W28A_REPLACEMENTS")

_SD35 = "redisson-spring/redisson-spring-data/redisson-spring-data-35/src/main/java/org/redisson/spring/data/connection/"
_SD40 = "redisson-spring/redisson-spring-data/redisson-spring-data-40/src/main/java/org/redisson/spring/data/connection/"

W33A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-35: ZSET replay decoders identical to spring-data-34.
for _name in (
    "ScoredSortedSetReplayDecoderV2.java",
    "ScoredSortedSingleBlockingReplayDecoder.java",
    "ScoredSortedSingleReplayDecoder.java",
):
    src = W28A if _name != "ScoredSortedSingleReplayDecoder.java" else W23A
    W33A_REPLACEMENTS[_name] = src[_name]
    W33A_REPLACEMENTS[_SD35 + _name] = src[_name]

# spring-data-35: convertors/decoders identical to spring-data-34.
W33A_REPLACEMENTS["SecondsConvertor.java"] = W11A["SecondsConvertor.java"]
W33A_REPLACEMENTS[_SD35 + "SecondsConvertor.java"] = W11A["SecondsConvertor.java"]
W33A_REPLACEMENTS["SetReplayDecoder.java"] = W11A["SetReplayDecoder.java"]
W33A_REPLACEMENTS[_SD35 + "SetReplayDecoder.java"] = W11A["SetReplayDecoder.java"]
W33A_REPLACEMENTS["SingleMapEntryDecoder.java"] = W21B["SingleMapEntryDecoder.java"]
W33A_REPLACEMENTS[_SD35 + "SingleMapEntryDecoder.java"] = W21B["SingleMapEntryDecoder.java"]

# spring-data-40: protocol convertors identical to spring-data-35/34.
for _name in (
    "BinaryConvertor.java",
    "ByteBufferGeoResultsDecoder.java",
    "DataTypeConvertor.java",
):
    W33A_REPLACEMENTS[_name] = W11A[_name]
    W33A_REPLACEMENTS[_SD40 + _name] = W11A[_name]

# spring-data-40: decoders/convertors identical to spring-data-35/34.
for _name in (
    "DistanceConvertor.java",
    "GeoResultsDecoder.java",
    "ListMergeDecoder.java",
    "ObjectListReplayDecoder2.java",
    "ObjectMapEntryReplayDecoder.java",
    "PointDecoder.java",
):
    W33A_REPLACEMENTS[_name] = W20A[_name]
    W33A_REPLACEMENTS[_SD40 + _name] = W20A[_name]
