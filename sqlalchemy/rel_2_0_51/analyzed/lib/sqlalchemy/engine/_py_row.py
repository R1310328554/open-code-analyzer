# engine/_py_row.py
# Copyright (C) 2005-2026 the SQLAlchemy authors and contributors
# <see AUTHORS file>
#
# This module is part of SQLAlchemy and is released under
# the MIT License: https://www.opensource.org/licenses/mit-license.php
# 纯 Python Row 实现（C 扩展不可用时的后备）

from __future__ import annotations

import operator
import typing
from typing import Any
from typing import Callable
from typing import Dict
from typing import Iterator
from typing import List
from typing import Mapping
from typing import Optional
from typing import Tuple
from typing import Type

if typing.TYPE_CHECKING:
    from .result import _KeyType
    from .result import _ProcessorsType
    from .result import _RawRowType
    from .result import _TupleGetterType
    from .result import ResultMetaData

MD_INDEX = 0  # integer index in cursor.description


# 结果行：不可变 tuple + 列名映射
class BaseRow:
    __slots__ = ("_parent", "_data", "_key_to_index")

    _parent: ResultMetaData
    _key_to_index: Mapping[_KeyType, int]
    _data: _RawRowType

    # 应用 result processors
    def __init__(
        self,
        parent: ResultMetaData,
        processors: Optional[_ProcessorsType],
        key_to_index: Mapping[_KeyType, int],
        data: _RawRowType,
    ):
        """Row objects are constructed by CursorResult objects."""
        object.__setattr__(self, "_parent", parent)

        object.__setattr__(self, "_key_to_index", key_to_index)

        if processors:
            object.__setattr__(
                self,
                "_data",
                tuple(
                    [
                        proc(value) if proc else value
                        for proc, value in zip(processors, data)
                    ]
                ),
            )
        else:
            object.__setattr__(self, "_data", tuple(data))

    # pickle 支持
    def __reduce__(self) -> Tuple[Callable[..., BaseRow], Tuple[Any, ...]]:
        return (
            rowproxy_reconstructor,
            (self.__class__, self.__getstate__()),
        )

    # 序列化状态
    def __getstate__(self) -> Dict[str, Any]:
        return {"_parent": self._parent, "_data": self._data}

    # 反序列化
    def __setstate__(self, state: Dict[str, Any]) -> None:
        parent = state["_parent"]
        object.__setattr__(self, "_parent", parent)
        object.__setattr__(self, "_data", state["_data"])
        object.__setattr__(self, "_key_to_index", parent._key_to_index)

    # 值列表
    def _values_impl(self) -> List[Any]:
        return list(self)

    def __iter__(self) -> Iterator[Any]:
        return iter(self._data)

    def __len__(self) -> int:
        return len(self._data)

    def __hash__(self) -> int:
        return hash(self._data)

    # 索引/切片访问
    def __getitem__(self, key: Any) -> Any:
        return self._data[key]

    # 按列名映射访问
    def _get_by_key_impl_mapping(self, key: str) -> Any:
        try:
            return self._data[self._key_to_index[key]]
        except KeyError:
            pass
        self._parent._key_not_found(key, False)

    # 属性式列访问
    def __getattr__(self, name: str) -> Any:
        try:
            return self._data[self._key_to_index[name]]
        except KeyError:
            pass
        self._parent._key_not_found(name, True)

    # 底层 tuple
    def _to_tuple_instance(self) -> Tuple[Any, ...]:
        return self._data


# This reconstructor is necessary so that pickles with the Cy extension or
# without use the same Binary format.
# pickle 重建 Row
def rowproxy_reconstructor(
    cls: Type[BaseRow], state: Dict[str, Any]
) -> BaseRow:
    obj = cls.__new__(cls)
    obj.__setstate__(state)
    return obj


# 连续索引用 slice itemgetter 优化
def tuplegetter(*indexes: int) -> _TupleGetterType:
    if len(indexes) != 1:
        for i in range(1, len(indexes)):
            if indexes[i - 1] != indexes[i] - 1:
                return operator.itemgetter(*indexes)
    # slice form is faster but returns a list if input is list
    return operator.itemgetter(slice(indexes[0], indexes[-1] + 1))
