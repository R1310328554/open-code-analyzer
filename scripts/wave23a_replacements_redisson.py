"""Chinese annotation replacements for Redisson 4.7.0 wave-23a spring-data-27/30 [0:15]."""
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
W21A = _load("wave21a_replacements_redisson.py", "W21A_REPLACEMENTS")
W21B = _load("wave21b_replacements_redisson.py", "W21B_REPLACEMENTS")

W23A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-27: ZSET replay decoders & convertors (identical to spring-data-18/22).
for _key in (
    "ScoredSortedSetReplayDecoder.java",
    "ScoredSortedSetReplayDecoderV2.java",
    "ScoredSortedSingleBlockingReplayDecoder.java",
    "ScoredSortedSingleReplayDecoder.java",
    "SecondsConvertor.java",
    "SetReplayDecoder.java",
):
    src = W21A if _key.startswith("ScoredSortedSingle") else W11A
    W23A_REPLACEMENTS[_key] = list(src[_key])

W23A_REPLACEMENTS["SingleMapEntryDecoder.java"] = list(W21B["SingleMapEntryDecoder.java"])

# spring-data-30: protocol convertors & replay decoders (identical to spring-data-20/26).
for _key in (
    "BinaryConvertor.java",
    "ByteBufferGeoResultsDecoder.java",
    "DataTypeConvertor.java",
    "DistanceConvertor.java",
    "GeoResultsDecoder.java",
    "ListMergeDecoder.java",
    "ObjectListReplayDecoder2.java",
    "ObjectMapEntryReplayDecoder.java",
):
    src = W20A if _key in ("ListMergeDecoder.java", "ObjectMapEntryReplayDecoder.java") else W11A
    W23A_REPLACEMENTS[_key] = list(src[_key])
