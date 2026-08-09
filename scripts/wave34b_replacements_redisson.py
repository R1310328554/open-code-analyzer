"""Chinese annotation replacements for Redisson 4.7.0 wave-34b spring-data-40/41 [15:30]."""
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
W31A = _load("wave31a_replacements_redisson.py", "W31A_REPLACEMENTS")

_SD40 = "redisson-spring/redisson-spring-data/redisson-spring-data-40/src/main/java/org/redisson/spring/data/connection/"
_SD41 = "redisson-spring/redisson-spring-data/redisson-spring-data-41/src/main/java/org/redisson/spring/data/connection/"

W34B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-40: ZSET replay decoders identical to spring-data-34/35.
for _name in (
    "ScoredSortedListReplayDecoder.java",
    "ScoredSortedSetBlockingReplayDecoder.java",
    "ScoredSortedSetReplayDecoder.java",
    "ScoredSortedSetReplayDecoderV2.java",
    "ScoredSortedSingleBlockingReplayDecoder.java",
    "ScoredSortedSingleReplayDecoder.java",
):
    W34B_REPLACEMENTS[_name] = W31A[_name]
    W34B_REPLACEMENTS[_SD40 + _name] = W31A[_name]

# spring-data-40: convertors/decoders identical to spring-data-34/35.
W34B_REPLACEMENTS["SecondsConvertor.java"] = W11A["SecondsConvertor.java"]
W34B_REPLACEMENTS[_SD40 + "SecondsConvertor.java"] = W11A["SecondsConvertor.java"]
W34B_REPLACEMENTS["SetReplayDecoder.java"] = W11A["SetReplayDecoder.java"]
W34B_REPLACEMENTS[_SD40 + "SetReplayDecoder.java"] = W11A["SetReplayDecoder.java"]
W34B_REPLACEMENTS["SingleMapEntryDecoder.java"] = W21B["SingleMapEntryDecoder.java"]
W34B_REPLACEMENTS[_SD40 + "SingleMapEntryDecoder.java"] = W21B["SingleMapEntryDecoder.java"]

# spring-data-41: protocol convertors identical to spring-data-40/35/34.
for _name in (
    "BinaryConvertor.java",
    "ByteBufferGeoResultsDecoder.java",
    "DataTypeConvertor.java",
):
    W34B_REPLACEMENTS[_name] = W11A[_name]
    W34B_REPLACEMENTS[_SD41 + _name] = W11A[_name]

# spring-data-41: decoders/convertors identical to spring-data-40/35/34.
for _name in (
    "DistanceConvertor.java",
    "GeoResultsDecoder.java",
    "ListMergeDecoder.java",
):
    W34B_REPLACEMENTS[_name] = W20A[_name]
    W34B_REPLACEMENTS[_SD41 + _name] = W20A[_name]
