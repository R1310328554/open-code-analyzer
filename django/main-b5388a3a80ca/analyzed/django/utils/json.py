"""
django.utils.json — 递归将 Python 对象规范为 JSON 兼容类型。

Mapping/Sequence 深度遍历，bytes 尝试 UTF-8 解码。
"""

from collections.abc import Mapping, Sequencefrom collections.abc import Mapping, Sequence


# 递归规范化；不支持的类型抛出 TypeError/ValueError
def normalize_json(obj):
    """Recursively normalize an object into JSON-compatible types."""
    match obj:
        case Mapping():
            return {normalize_json(k): normalize_json(v) for k, v in obj.items()}
        case bytes():
            try:
                return obj.decode("utf-8")
            except UnicodeDecodeError:
                raise ValueError(f"Unsupported value: {type(obj)}")
        case str() | int() | float() | bool() | None:
            return obj
        case Sequence():  # str and bytes were already handled.
            return [normalize_json(v) for v in obj]
        case _:  # Other types can't be serialized to JSON
            raise TypeError(f"Unsupported type: {type(obj)}")
