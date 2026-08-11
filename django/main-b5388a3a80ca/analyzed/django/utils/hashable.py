"""
django.utils.hashable — 将不可哈希结构转为可哈希表示。

用于 QuerySet 缓存键等场景，dict/list 递归规范化。
"""

from collections.abc import Iterablefrom collections.abc import Iterable


# dict 转 sorted tuple；不可哈希 iterable 转 tuple
def make_hashable(value):
    """
    Attempt to make value hashable or raise a TypeError if it fails.

    The returned value should generate the same hash for equal values.
    """
    if isinstance(value, dict):
        return tuple(
            [
                (key, make_hashable(nested_value))
                for key, nested_value in sorted(value.items())
            ]
        )
    # Try hash to avoid converting a hashable iterable (e.g. string, frozenset)
    # to a tuple.
    try:
        hash(value)
    except TypeError:
        if isinstance(value, Iterable):
            return tuple(map(make_hashable, value))
        # Non-hashable, non-iterable.
        raise
    return value
