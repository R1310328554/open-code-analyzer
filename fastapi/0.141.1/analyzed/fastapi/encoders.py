import dataclasses
import datetime
from collections import defaultdict, deque
from collections.abc import Callable
from decimal import Decimal
from enum import Enum
from ipaddress import (
    IPv4Address,
    IPv4Interface,
    IPv4Network,
    IPv6Address,
    IPv6Interface,
    IPv6Network,
)
from pathlib import Path, PurePath
from re import Pattern
from types import GeneratorType
from typing import Annotated, Any
from uuid import UUID

from annotated_doc import Doc
from fastapi.exceptions import PydanticV1NotSupportedError
from fastapi.types import IncEx
from pydantic import BaseModel
from pydantic.networks import AnyUrl, NameEmail
from pydantic.types import SecretBytes, SecretStr
from pydantic_core import PydanticUndefinedType

from ._compat import (
    Url,
    is_pydantic_v1_model_instance,
)

try:
    # pydantic.color.Color 自 v2.0b3 起已弃用，但为向后兼容仍保留支持
    from pydantic.color import Color  # ty: ignore[deprecated]
except ImportError:  # pragma: no cover

    class Color:  # type: ignore[no-redef]
        pass


try:
    # 支持新版 Pydantic 的 Color 格式
    from pydantic_extra_types.color import Color as PyExtraColor
except ImportError:  # pragma: no cover

    class PyExtraColor:  # type: ignore[no-redef]
        pass


# 直接取自 Pydantic v1
def isoformat(o: datetime.date | datetime.time) -> str:
    return o.isoformat()


# 改编自 Pydantic v1
# TODO：pv2 是否应改为返回字符串？
def decimal_encoder(dec_value: Decimal) -> int | float:
    """
    若 Decimal 没有指数部分则编码为 int，否则编码为 float。

    当使用 ConstrainedDecimal 表示 Numeric(x,0) 且实际值为整数
    （但类型并非 int）时，这很有用。若编码为 float，
    会在 encode 与 parse 之间往返失败。
    Id 类型就是典型例子。

    >>> decimal_encoder(Decimal("1.0"))
    1.0

    >>> decimal_encoder(Decimal("1"))
    1

    >>> decimal_encoder(Decimal("NaN"))
    nan
    """
    exponent = dec_value.as_tuple().exponent
    if isinstance(exponent, int) and exponent >= 0:
        return int(dec_value)
    else:
        return float(dec_value)


ENCODERS_BY_TYPE: dict[type[Any], Callable[[Any], Any]] = {
    bytes: lambda o: o.decode(),
    Color: str,
    PyExtraColor: str,
    datetime.date: isoformat,
    datetime.datetime: isoformat,
    datetime.time: isoformat,
    datetime.timedelta: lambda td: td.total_seconds(),
    Decimal: decimal_encoder,
    Enum: lambda o: o.value,
    frozenset: list,
    deque: list,
    GeneratorType: list,
    IPv4Address: str,
    IPv4Interface: str,
    IPv4Network: str,
    IPv6Address: str,
    IPv6Interface: str,
    IPv6Network: str,
    NameEmail: str,
    Path: str,
    Pattern: lambda o: o.pattern,
    SecretBytes: str,
    SecretStr: str,
    set: list,
    UUID: str,
    Url: str,
    AnyUrl: str,
}


def generate_encoders_by_class_tuples(
    type_encoder_map: dict[Any, Callable[[Any], Any]],
) -> dict[Callable[[Any], Any], tuple[Any, ...]]:
    encoders_by_class_tuples: dict[Callable[[Any], Any], tuple[Any, ...]] = defaultdict(
        tuple
    )
    for type_, encoder in type_encoder_map.items():
        encoders_by_class_tuples[encoder] += (type_,)
    return encoders_by_class_tuples


encoders_by_class_tuples = generate_encoders_by_class_tuples(ENCODERS_BY_TYPE)


def jsonable_encoder(
    obj: Annotated[
        Any,
        Doc(
            """
            要转换为 JSON 的输入对象。
            """
        ),
    ],
    include: Annotated[
        IncEx | None,
        Doc(
            """
            Pydantic 的 `include` 参数，传给 Pydantic 模型以指定要包含的字段。
            """
        ),
    ] = None,
    exclude: Annotated[
        IncEx | None,
        Doc(
            """
            Pydantic 的 `exclude` 参数，传给 Pydantic 模型以指定要排除的字段。
            """
        ),
    ] = None,
    by_alias: Annotated[
        bool,
        Doc(
            """
            Pydantic 的 `by_alias` 参数，传给 Pydantic 模型以决定输出
            使用别名（若提供）还是 Python 属性名。在 API 中，
            若设置了 alias，通常意味着希望在结果中使用它，
            因此一般应保持为 `True`。
            """
        ),
    ] = True,
    exclude_unset: Annotated[
        bool,
        Doc(
            """
            Pydantic 的 `exclude_unset` 参数，传给 Pydantic 模型以决定
            是否从输出中排除未显式设置（仅保留默认值）的字段。
            """
        ),
    ] = False,
    exclude_defaults: Annotated[
        bool,
        Doc(
            """
            Pydantic 的 `exclude_defaults` 参数，传给 Pydantic 模型以决定
            是否从输出中排除与默认值相同、即使已被显式设置的字段。
            """
        ),
    ] = False,
    exclude_none: Annotated[
        bool,
        Doc(
            """
            Pydantic 的 `exclude_none` 参数，传给 Pydantic 模型以决定
            是否从输出中排除值为 `None` 的字段。
            """
        ),
    ] = False,
    custom_encoder: Annotated[
        dict[Any, Callable[[Any], Any]] | None,
        Doc(
            """
            Pydantic 的 `custom_encoder` 参数，传给 Pydantic 模型以定义自定义编码器。
            """
        ),
    ] = None,
    sqlalchemy_safe: Annotated[
        bool,
        Doc(
            """
            从输出中排除名称以 `_sa` 开头的字段。

            这主要是为兼容 SQLAlchemy 对象的 hack：SQLAlchemy 会把
            内部状态存放在名为 `_sa` 的属性中，这些对象无法（也不应）
            序列化为 JSON。
            """
        ),
    ] = True,
) -> Any:
    """
    将任意对象转换为可 JSON 编码的形式。

    FastAPI 内部使用此函数，确保返回给客户端的内容在发送前都能编码为 JSON。

    你也可以自行使用，例如在仅支持 JSON 的数据库中保存对象前先转换。

    详见 [FastAPI JSON 兼容编码器文档](https://fastapi.tiangolo.com/tutorial/encoder/)。
    """
    custom_encoder = custom_encoder or {}
    if custom_encoder:
        if type(obj) in custom_encoder:
            return custom_encoder[type(obj)](obj)
        else:
            for encoder_type, encoder_instance in custom_encoder.items():
                if isinstance(obj, encoder_type):
                    return encoder_instance(obj)
    if include is not None and not isinstance(include, (set, dict)):
        include = set(include)  # type: ignore[assignment]  # ty: ignore[invalid-assignment]
    if exclude is not None and not isinstance(exclude, (set, dict)):
        exclude = set(exclude)  # type: ignore[assignment]  # ty: ignore[invalid-assignment]
    if isinstance(obj, BaseModel):
        obj_dict = obj.model_dump(
            mode="json",
            include=include,
            exclude=exclude,
            by_alias=by_alias,
            exclude_unset=exclude_unset,
            exclude_none=exclude_none,
            exclude_defaults=exclude_defaults,
        )
        return jsonable_encoder(
            obj_dict,
            exclude_none=exclude_none,
            exclude_defaults=exclude_defaults,
            sqlalchemy_safe=sqlalchemy_safe,
        )
    if dataclasses.is_dataclass(obj):
        assert not isinstance(obj, type)
        obj_dict = dataclasses.asdict(obj)
        return jsonable_encoder(
            obj_dict,
            include=include,
            exclude=exclude,
            by_alias=by_alias,
            exclude_unset=exclude_unset,
            exclude_defaults=exclude_defaults,
            exclude_none=exclude_none,
            custom_encoder=custom_encoder,
            sqlalchemy_safe=sqlalchemy_safe,
        )
    if isinstance(obj, Enum):
        return obj.value
    if isinstance(obj, PurePath):
        return str(obj)
    if isinstance(obj, (str, int, float, type(None))):
        return obj
    if isinstance(obj, PydanticUndefinedType):
        return None
    if isinstance(obj, dict):
        encoded_dict = {}
        allowed_keys = set(obj.keys())
        if include is not None:
            allowed_keys &= set(include)
        if exclude is not None:
            allowed_keys -= set(exclude)
        for key, value in obj.items():
            if (
                (
                    not sqlalchemy_safe
                    or (not isinstance(key, str))
                    or (not key.startswith("_sa"))
                )
                and (value is not None or not exclude_none)
                and key in allowed_keys
            ):
                encoded_key = jsonable_encoder(
                    key,
                    by_alias=by_alias,
                    exclude_unset=exclude_unset,
                    exclude_defaults=exclude_defaults,
                    exclude_none=exclude_none,
                    custom_encoder=custom_encoder,
                    sqlalchemy_safe=sqlalchemy_safe,
                )
                encoded_value = jsonable_encoder(
                    value,
                    by_alias=by_alias,
                    exclude_unset=exclude_unset,
                    exclude_defaults=exclude_defaults,
                    exclude_none=exclude_none,
                    custom_encoder=custom_encoder,
                    sqlalchemy_safe=sqlalchemy_safe,
                )
                encoded_dict[encoded_key] = encoded_value
        return encoded_dict
    if isinstance(obj, (list, set, frozenset, GeneratorType, tuple, deque)):
        encoded_list = []
        for item in obj:
            encoded_list.append(
                jsonable_encoder(
                    item,
                    include=include,
                    exclude=exclude,
                    by_alias=by_alias,
                    exclude_unset=exclude_unset,
                    exclude_defaults=exclude_defaults,
                    exclude_none=exclude_none,
                    custom_encoder=custom_encoder,
                    sqlalchemy_safe=sqlalchemy_safe,
                )
            )
        return encoded_list

    if type(obj) in ENCODERS_BY_TYPE:
        return ENCODERS_BY_TYPE[type(obj)](obj)
    for encoder, classes_tuple in encoders_by_class_tuples.items():
        if isinstance(obj, classes_tuple):
            return encoder(obj)
    if is_pydantic_v1_model_instance(obj):
        raise PydanticV1NotSupportedError(
            "pydantic.v1 models are no longer supported by FastAPI."
            f" Please update the model {obj!r}."
        )
    try:
        data = dict(obj)
    except Exception as e:
        errors: list[Exception] = []
        errors.append(e)
        try:
            data = vars(obj)
        except Exception as e:
            errors.append(e)
            raise ValueError(errors) from e
    return jsonable_encoder(
        data,
        include=include,
        exclude=exclude,
        by_alias=by_alias,
        exclude_unset=exclude_unset,
        exclude_defaults=exclude_defaults,
        exclude_none=exclude_none,
        custom_encoder=custom_encoder,
        sqlalchemy_safe=sqlalchemy_safe,
    )
