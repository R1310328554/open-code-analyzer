"""Chinese annotation replacements for Redisson 4.7.0 wave-15b spring-data-23 [15:30]."""
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


W14A = _load("wave14a_replacements_redisson.py", "W14A_REPLACEMENTS")

W15B_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# spring-data-23 sources match spring-data-22 for this batch (extra imports only).
for _key, _reps in W14A.items():
    W15B_REPLACEMENTS[_key] = _reps
