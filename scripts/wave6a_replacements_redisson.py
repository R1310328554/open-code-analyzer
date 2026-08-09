"""Chinese annotation replacements for Redisson 4.7.0 wave-6a [0:15]."""
from __future__ import annotations

import importlib.util
from pathlib import Path

_spec4 = importlib.util.spec_from_file_location(
    "wave4b_replacements_redisson",
    Path(__file__).with_name("wave4b_replacements_redisson.py"),
)
_mod4 = importlib.util.module_from_spec(_spec4)
assert _spec4.loader is not None
_spec4.loader.exec_module(_mod4)
_W4B = _mod4.W4B_REPLACEMENTS

_spec = importlib.util.spec_from_file_location(
    "wave5b_replacements_redisson",
    Path(__file__).with_name("wave5b_replacements_redisson.py"),
)
_mod = importlib.util.module_from_spec(_spec)
assert _spec.loader is not None
_spec.loader.exec_module(_mod)
_W5B = _mod.W5B_REPLACEMENTS


def _adapt(reps: list[tuple[str, str]], *subs: tuple[str, str]) -> list[tuple[str, str]]:
    out: list[tuple[str, str]] = []
    for old, new in reps:
        for a, b in subs:
            new = new.replace(a, b)
        out.append((old, new))
    return out


def _m5(name: str) -> list[tuple[str, str]]:
    return _adapt(_W5B[name], ("Micronaut 4.x", "Micronaut 5.x"))


W6A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

_session = [
    "AttributesClearMessage.java",
    "AttributesPutAllMessage.java",
    "SessionCreatedMessage.java",
    "SessionDestroyedMessage.java",
    "RedissonHttpSessionConfiguration.java",
    "RedissonSession.java",
    "RedissonSessionStore.java",
]
for name in _session:
    W6A_REPLACEMENTS[name] = _adapt(_W5B[name], ("Micronaut 3.x", "Micronaut 4.x"))

W6A_REPLACEMENTS["AttributeRemoveMessage.java"] = _W4B["AttributeRemoveMessage.java"]
W6A_REPLACEMENTS["AttributeUpdateMessage.java"] = _W4B["AttributeUpdateMessage.java"]

W6A_REPLACEMENTS["RedissonConfiguration.java"] = _adapt(
    _W5B["RedissonConfiguration.java"],
    ("Micronaut 4.x", "Micronaut 5.x"),
)
W6A_REPLACEMENTS["RedissonConfiguration.java"] = [
    (old.replace("PropertySourcePropertyResolver", "Environment"), new)
    for old, new in W6A_REPLACEMENTS["RedissonConfiguration.java"]
]

W6A_REPLACEMENTS["RedissonFactory.java"] = _m5("RedissonFactory.java")
W6A_REPLACEMENTS["BaseCacheConfiguration.java"] = _W5B["BaseCacheConfiguration.java"]
W6A_REPLACEMENTS["RedissonAsyncCache.java"] = _W5B["RedissonAsyncCache.java"]
W6A_REPLACEMENTS["RedissonCacheConfiguration.java"] = _W5B["RedissonCacheConfiguration.java"]
W6A_REPLACEMENTS["RedissonCacheNativeConfiguration.java"] = _W5B[
    "RedissonCacheNativeConfiguration.java"
]
