"""Chinese annotation replacements for Redisson 4.7.0 wave-37a tomcat-11 + tomcat-7 [0:15]."""
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


W36B = _load("wave36b_replacements_redisson.py", "W36B_REPLACEMENTS")

_T7 = "redisson-tomcat/redisson-tomcat-7/src/main/java/org/redisson/tomcat/"
_T11 = "redisson-tomcat/redisson-tomcat-11/src/main/java/org/redisson/tomcat/"

W37A_REPLACEMENTS: dict[str, list[tuple[str, str]]] = {}

# tomcat-11: remaining session messages, JNDI manager, SSO, valves (parallel to tomcat-10).
for _name in (
    "AttributeRemoveMessage.java",
    "AttributeUpdateMessage.java",
    "AttributesClearMessage.java",
    "AttributesPutAllMessage.java",
    "JndiRedissonSessionManager.java",
    "RedissonSingleSignOn.java",
    "SessionCreatedMessage.java",
    "SessionDestroyedMessage.java",
    "UpdateValve.java",
    "UsageValve.java",
):
    W37A_REPLACEMENTS[_name] = W36B[_name]
    W37A_REPLACEMENTS[f"{_T11}{_name}"] = W36B[_name]

# tomcat-7: start of session cluster message classes (same API as tomcat-10).
for _name in (
    "AttributeMessage.java",
    "AttributeRemoveMessage.java",
    "AttributeUpdateMessage.java",
    "AttributesClearMessage.java",
    "AttributesPutAllMessage.java",
):
    W37A_REPLACEMENTS[f"{_T7}{_name}"] = W36B[_name]
