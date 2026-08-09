"""参数声明辅助函数：Path、Query、Header、Body、Depends、Security 等。"""

from collections.abc import Callable, Sequence
from typing import Annotated, Any, Literal

from annotated_doc import Doc
from fastapi import params
from fastapi._compat import Undefined
from fastapi.datastructures import _Unset
from fastapi.openapi.models import Example
from pydantic import AliasChoices, AliasPath
from typing_extensions import deprecated


def Path(  # noqa: N802
    default: Annotated[
        Any,
        Doc(
            """
            参数未设置时的默认值。

            对 `Path` 参数无效，因其值始终必填；保留此参数仅为兼容。
            """
        ),
    ] = ...,
    *,
    default_factory: Annotated[
        Callable[[], Any] | None,
        Doc(
            """
            用于生成默认值的 callable。

            对 `Path` 参数无效，因其值始终必填；保留此参数仅为兼容。
            """
        ),
    ] = _Unset,
    alias: Annotated[
        str | None,
        Doc(
            """
            参数字段的别名。

            用于提取数据及生成 OpenAPI，在参数名与 Python 保留字冲突时尤其有用。
            """
        ),
    ] = None,
    alias_priority: Annotated[
        int | None,
        Doc(
            """
            别名优先级，影响是否使用别名生成器。
            """
        ),
    ] = _Unset,
    validation_alias: Annotated[
        str | AliasPath | AliasChoices | None,
        Doc(
            """
            验证白名单步骤：仅允许别名或别名集合定义的字段通过。
            """
        ),
    ] = None,
    serialization_alias: Annotated[
        str | None,
        Doc(
            """
            序列化黑名单步骤：仅保留别名字段，其余字段在序列化时忽略。
            """
        ),
    ] = None,
    title: Annotated[
        str | None,
        Doc(
            """
            人类可读的标题。

            详见
            [FastAPI 路径参数与数值验证文档](https://fastapi.tiangolo.com/tutorial/path-params-numeric-validations/#declare-metadata)
            """
        ),
    ] = None,
    description: Annotated[
        str | None,
        Doc(
            """
            人类可读的描述。
            """
        ),
    ] = None,
    gt: Annotated[
        float | None,
        Doc(
            """
            大于：若设置，值必须大于此值，仅适用于数值。

            详见
            [FastAPI 路径参数数值验证文档](https://fastapi.tiangolo.com/tutorial/path-params-numeric-validations/#number-validations-greater-than-and-less-than-or-equal)
            """
        ),
    ] = None,
    ge: Annotated[
        float | None,
        Doc(
            """
            大于等于：若设置，值必须大于等于此值，仅适用于数值。

            详见
            [FastAPI 路径参数数值验证文档](https://fastapi.tiangolo.com/tutorial/path-params-numeric-validations/#number-validations-greater-than-and-less-than-or-equal)
            """
        ),
    ] = None,
    lt: Annotated[
        float | None,
        Doc(
            """
            小于：若设置，值必须小于此值，仅适用于数值。

            详见
            [FastAPI 路径参数数值验证文档](https://fastapi.tiangolo.com/tutorial/path-params-numeric-validations/#number-validations-greater-than-and-less-than-or-equal)
            """
        ),
    ] = None,
    le: Annotated[
        float | None,
        Doc(
            """
            小于等于：若设置，值必须小于等于此值，仅适用于数值。

            详见
            [FastAPI 路径参数数值验证文档](https://fastapi.tiangolo.com/tutorial/path-params-numeric-validations/#number-validations-greater-than-and-less-than-or-equal)
            """
        ),
    ] = None,
    min_length: Annotated[
        int | None,
        Doc(
            """
            字符串最小长度。
            """
        ),
    ] = None,
    max_length: Annotated[
        int | None,
        Doc(
            """
            字符串最大长度。
            """
        ),
    ] = None,
    pattern: Annotated[
        str | None,
        Doc(
            """
            字符串的正则表达式模式。
            """
        ),
    ] = None,
    regex: Annotated[
        str | None,
        Doc(
            """
            字符串的正则表达式模式。
            """
        ),
        deprecated(
            "Deprecated in FastAPI 0.100.0 and Pydantic v2, use `pattern` instead."
        ),
    ] = None,
    discriminator: Annotated[
        str | None,
        Doc(
            """
            标记联合类型中用于区分类型的参数字段名。
            """
        ),
    ] = None,
    strict: Annotated[
        bool | None,
        Doc(
            """
            为 `True` 时对该字段启用严格验证。
            """
        ),
    ] = _Unset,
    multiple_of: Annotated[
        float | None,
        Doc(
            """
            值必须是该数的倍数，仅适用于数值。
            """
        ),
    ] = _Unset,
    allow_inf_nan: Annotated[
        bool | None,
        Doc(
            """
            允许 `inf`、`-inf`、`nan`，仅适用于数值。
            """
        ),
    ] = _Unset,
    max_digits: Annotated[
        int | None,
        Doc(
            """
            小数值允许的最大位数。
            """
        ),
    ] = _Unset,
    decimal_places: Annotated[
        int | None,
        Doc(
            """
            小数值允许的最大小数位数。
            """
        ),
    ] = _Unset,
    examples: Annotated[
        list[Any] | None,
        Doc(
            """
            该字段的示例值。

            详见
            [FastAPI 请求示例数据文档](https://fastapi.tiangolo.com/tutorial/schema-extra-example/)
            """
        ),
    ] = None,
    example: Annotated[
        Any | None,
        deprecated(
            "Deprecated in OpenAPI 3.1.0 that now uses JSON Schema 2020-12, "
            "although still supported. Use examples instead."
        ),
    ] = _Unset,
    openapi_examples: Annotated[
        dict[str, Example] | None,
        Doc(
            """
            OpenAPI 专用示例，将加入生成的 OpenAPI（例如 `/docs` 可见）。

            Swagger UI 对 OpenAPI 示例的支持优于 JSON Schema `examples`，这是主要使用场景。

            详见
            [FastAPI 请求示例数据文档](https://fastapi.tiangolo.com/tutorial/schema-extra-example/#using-the-openapi_examples-parameter).
            """
        ),
    ] = None,
    deprecated: Annotated[
        deprecated | str | bool | None,
        Doc(
            """
            将该参数字段标记为已弃用，将影响生成的 OpenAPI（例如 `/docs` 可见）。
            """
        ),
    ] = None,
    include_in_schema: Annotated[
        bool,
        Doc(
            """
            是否在生成的 OpenAPI 中包含该参数字段（通常无需设置）。

            将影响生成的 OpenAPI（例如 `/docs` 可见）。
            """
        ),
    ] = True,
    json_schema_extra: Annotated[
        dict[str, Any] | None,
        Doc(
            """
            附加的 JSON Schema 数据。
            """
        ),
    ] = None,
    **extra: Annotated[
        Any,
        Doc(
            """
            包含 JSON Schema 使用的额外字段。
            """
        ),
        deprecated(
            """
            The `extra` kwargs is deprecated. Use `json_schema_extra` instead.
            """
        ),
    ],
) -> Any:
    """
    为 *path operation* 声明路径参数。

    详见
    [FastAPI 路径参数与数值验证文档](https://fastapi.tiangolo.com/tutorial/path-params-numeric-validations/).

    ```python
    from typing import Annotated

    from fastapi import FastAPI, Path

    app = FastAPI()


    @app.get("/items/{item_id}")
    async def read_items(
        item_id: Annotated[int, Path(title="The ID of the item to get")],
    ):
        return {"item_id": item_id}
    ```
    """
    return params.Path(
        default=default,
        default_factory=default_factory,
        alias=alias,
        alias_priority=alias_priority,
        validation_alias=validation_alias,
        serialization_alias=serialization_alias,
        title=title,
        description=description,
        gt=gt,
        ge=ge,
        lt=lt,
        le=le,
        min_length=min_length,
        max_length=max_length,
        pattern=pattern,
        regex=regex,
        discriminator=discriminator,
        strict=strict,
        multiple_of=multiple_of,
        allow_inf_nan=allow_inf_nan,
        max_digits=max_digits,
        decimal_places=decimal_places,
        example=example,
        examples=examples,
        openapi_examples=openapi_examples,
        deprecated=deprecated,
        include_in_schema=include_in_schema,
        json_schema_extra=json_schema_extra,
        **extra,
    )


def Query(  # noqa: N802
    default: Annotated[
        Any,
        Doc(
            """
            参数未设置时的默认值。

            详见
            [FastAPI 查询参数文档](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#alternative-old-query-as-the-default-value)
            """
        ),
    ] = Undefined,
    *,
    default_factory: Annotated[
        Callable[[], Any] | None,
        Doc(
            """
            用于生成默认值的 callable。

            对 `Path` 参数无效，因其值始终必填；保留此参数仅为兼容。
            """
        ),
    ] = _Unset,
    alias: Annotated[
        str | None,
        Doc(
            """
            参数字段的别名，用于提取数据及生成 OpenAPI。

            详见
            [FastAPI 查询参数文档](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#alias-parameters)
            """
        ),
    ] = None,
    alias_priority: Annotated[
        int | None,
        Doc(
            """
            别名优先级，影响是否使用别名生成器。
            """
        ),
    ] = _Unset,
    validation_alias: Annotated[
        str | AliasPath | AliasChoices | None,
        Doc(
            """
            验证白名单步骤：仅允许别名或别名集合定义的字段通过。
            """
        ),
    ] = None,
    serialization_alias: Annotated[
        str | None,
        Doc(
            """
            序列化黑名单步骤：仅保留别名字段，其余字段在序列化时忽略。
            """
        ),
    ] = None,
    title: Annotated[
        str | None,
        Doc(
            """
            人类可读的标题。

            详见
            [FastAPI 查询参数文档](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#declare-more-metadata)
            """
        ),
    ] = None,
    description: Annotated[
        str | None,
        Doc(
            """
            Human-readable description.

            Read more about it in the
            [FastAPI docs about Query parameters](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#declare-more-metadata)
            """
        ),
    ] = None,
    gt: Annotated[
        float | None,
        Doc(
            """
            Greater than. If set, value must be greater than this. Only applicable to
            numbers.

            Read more about it in the
            [FastAPI docs about Path parameters numeric validations](https://fastapi.tiangolo.com/tutorial/path-params-numeric-validations/#number-validations-greater-than-and-less-than-or-equal)
            """
        ),
    ] = None,
    ge: Annotated[
        float | None,
        Doc(
            """
            Greater than or equal. If set, value must be greater than or equal to
            this. Only applicable to numbers.

            Read more about it in the
            [FastAPI docs about Path parameters numeric validations](https://fastapi.tiangolo.com/tutorial/path-params-numeric-validations/#number-validations-greater-than-and-less-than-or-equal)
            """
        ),
    ] = None,
    lt: Annotated[
        float | None,
        Doc(
            """
            Less than. If set, value must be less than this. Only applicable to numbers.

            Read more about it in the
            [FastAPI docs about Path parameters numeric validations](https://fastapi.tiangolo.com/tutorial/path-params-numeric-validations/#number-validations-greater-than-and-less-than-or-equal)
            """
        ),
    ] = None,
    le: Annotated[
        float | None,
        Doc(
            """
            Less than or equal. If set, value must be less than or equal to this.
            Only applicable to numbers.

            Read more about it in the
            [FastAPI docs about Path parameters numeric validations](https://fastapi.tiangolo.com/tutorial/path-params-numeric-validations/#number-validations-greater-than-and-less-than-or-equal)
            """
        ),
    ] = None,
    min_length: Annotated[
        int | None,
        Doc(
            """
            Minimum length for strings.

            Read more about it in the
            [FastAPI docs about Query parameters](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/)
            """
        ),
    ] = None,
    max_length: Annotated[
        int | None,
        Doc(
            """
            Maximum length for strings.

            Read more about it in the
            [FastAPI docs about Query parameters](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/)
            """
        ),
    ] = None,
    pattern: Annotated[
        str | None,
        Doc(
            """
            RegEx pattern for strings.

            Read more about it in the
            [FastAPI docs about Query parameters](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#add-regular-expressions
            """
        ),
    ] = None,
    regex: Annotated[
        str | None,
        Doc(
            """
            字符串的正则表达式模式。
            """
        ),
        deprecated(
            "Deprecated in FastAPI 0.100.0 and Pydantic v2, use `pattern` instead."
        ),
    ] = None,
    discriminator: Annotated[
        str | None,
        Doc(
            """
            标记联合类型中用于区分类型的参数字段名。
            """
        ),
    ] = None,
    strict: Annotated[
        bool | None,
        Doc(
            """
            为 `True` 时对该字段启用严格验证。
            """
        ),
    ] = _Unset,
    multiple_of: Annotated[
        float | None,
        Doc(
            """
            值必须是该数的倍数，仅适用于数值。
            """
        ),
    ] = _Unset,
    allow_inf_nan: Annotated[
        bool | None,
        Doc(
            """
            允许 `inf`、`-inf`、`nan`，仅适用于数值。
            """
        ),
    ] = _Unset,
    max_digits: Annotated[
        int | None,
        Doc(
            """
            小数值允许的最大位数。
            """
        ),
    ] = _Unset,
    decimal_places: Annotated[
        int | None,
        Doc(
            """
            小数值允许的最大小数位数。
            """
        ),
    ] = _Unset,
    examples: Annotated[
        list[Any] | None,
        Doc(
            """
            Example values for this field.

            Read more about it in the
            [FastAPI docs for Declare Request Example Data](https://fastapi.tiangolo.com/tutorial/schema-extra-example/)
            """
        ),
    ] = None,
    example: Annotated[
        Any | None,
        deprecated(
            "Deprecated in OpenAPI 3.1.0 that now uses JSON Schema 2020-12, "
            "although still supported. Use examples instead."
        ),
    ] = _Unset,
    openapi_examples: Annotated[
        dict[str, Example] | None,
        Doc(
            """
            OpenAPI 专用示例，将加入生成的 OpenAPI（例如 `/docs` 可见）。

            Swagger UI 对 OpenAPI 示例的支持优于 JSON Schema `examples`，这是主要使用场景。

            详见
            [FastAPI 请求示例数据文档](https://fastapi.tiangolo.com/tutorial/schema-extra-example/#using-the-openapi_examples-parameter).
            """
        ),
    ] = None,
    deprecated: Annotated[
        deprecated | str | bool | None,
        Doc(
            """
            Mark this parameter field as deprecated.

            It will affect the generated OpenAPI (e.g. visible at `/docs`).

            Read more about it in the
            [FastAPI docs about Query parameters](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#deprecating-parameters)
            """
        ),
    ] = None,
    include_in_schema: Annotated[
        bool,
        Doc(
            """
            To include (or not) this parameter field in the generated OpenAPI.
            You probably don't need it, but it's available.

            This affects the generated OpenAPI (e.g. visible at `/docs`).

            Read more about it in the
            [FastAPI docs about Query parameters](https://fastapi.tiangolo.com/tutorial/query-params-str-validations/#exclude-parameters-from-openapi
            """
        ),
    ] = True,
    json_schema_extra: Annotated[
        dict[str, Any] | None,
        Doc(
            """
            附加的 JSON Schema 数据。
            """
        ),
    ] = None,
    **extra: Annotated[
        Any,
        Doc(
            """
            包含 JSON Schema 使用的额外字段。
            """
        ),
        deprecated(
            """
            The `extra` kwargs is deprecated. Use `json_schema_extra` instead.
            """
        ),
    ],
) -> Any:
    return params.Query(
        default=default,
        default_factory=default_factory,
        alias=alias,
        alias_priority=alias_priority,
        validation_alias=validation_alias,
        serialization_alias=serialization_alias,
        title=title,
        description=description,
        gt=gt,
        ge=ge,
        lt=lt,
        le=le,
        min_length=min_length,
        max_length=max_length,
        pattern=pattern,
        regex=regex,
        discriminator=discriminator,
        strict=strict,
        multiple_of=multiple_of,
        allow_inf_nan=allow_inf_nan,
        max_digits=max_digits,
        decimal_places=decimal_places,
        example=example,
        examples=examples,
        openapi_examples=openapi_examples,
        deprecated=deprecated,
        include_in_schema=include_in_schema,
        json_schema_extra=json_schema_extra,
        **extra,
    )


def Header(  # noqa: N802
    default: Annotated[
        Any,
        Doc(
            """
            参数未设置时的默认值。
            """
        ),
    ] = Undefined,
    *,
    default_factory: Annotated[
        Callable[[], Any] | None,
        Doc(
            """
            用于生成默认值的 callable。

            对 `Path` 参数无效，因其值始终必填；保留此参数仅为兼容。
            """
        ),
    ] = _Unset,
    alias: Annotated[
        str | None,
        Doc(
            """
            参数字段的别名。

            用于提取数据及生成 OpenAPI，在参数名与 Python 保留字冲突时尤其有用。
            """
        ),
    ] = None,
    alias_priority: Annotated[
        int | None,
        Doc(
            """
            别名优先级，影响是否使用别名生成器。
            """
        ),
    ] = _Unset,
    validation_alias: Annotated[
        str | AliasPath | AliasChoices | None,
        Doc(
            """
            验证白名单步骤：仅允许别名或别名集合定义的字段通过。
            """
        ),
    ] = None,
    serialization_alias: Annotated[
        str | None,
        Doc(
            """
            序列化黑名单步骤：仅保留别名字段，其余字段在序列化时忽略。
            """
        ),
    ] = None,
    convert_underscores: Annotated[
        bool,
        Doc(
            """
            自动将参数字段名中的下划线转换为连字符。

            详见
            [FastAPI Header 参数文档](https://fastapi.tiangolo.com/tutorial/header-params/#automatic-conversion)
            """
        ),
    ] = True,
    title: Annotated[
        str | None,
        Doc(
            """
            人类可读的标题。
            """
        ),
    ] = None,
    description: Annotated[
        str | None,
        Doc(
            """
            人类可读的描述。
            """
        ),
    ] = None,
    gt: Annotated[
        float | None,
        Doc(
            """
            大于：若设置，值必须大于此值，仅适用于数值。
            """
        ),
    ] = None,
    ge: Annotated[
        float | None,
        Doc(
            """
            大于等于：若设置，值必须大于等于此值，仅适用于数值。
            """
        ),
    ] = None,
    lt: Annotated[
        float | None,
        Doc(
            """
            小于：若设置，值必须小于此值，仅适用于数值。
            """
        ),
    ] = None,
    le: Annotated[
        float | None,
        Doc(
            """
            小于等于：若设置，值必须小于等于此值，仅适用于数值。
            """
        ),
    ] = None,
    min_length: Annotated[
        int | None,
        Doc(
            """
            字符串最小长度。
            """
        ),
    ] = None,
    max_length: Annotated[
        int | None,
        Doc(
            """
            字符串最大长度。
            """
        ),
    ] = None,
    pattern: Annotated[
        str | None,
        Doc(
            """
            字符串的正则表达式模式。
            """
        ),
    ] = None,
    regex: Annotated[
        str | None,
        Doc(
            """
            字符串的正则表达式模式。
            """
        ),
        deprecated(
            "Deprecated in FastAPI 0.100.0 and Pydantic v2, use `pattern` instead."
        ),
    ] = None,
    discriminator: Annotated[
        str | None,
        Doc(
            """
            标记联合类型中用于区分类型的参数字段名。
            """
        ),
    ] = None,
    strict: Annotated[
        bool | None,
        Doc(
            """
            为 `True` 时对该字段启用严格验证。
            """
        ),
    ] = _Unset,
    multiple_of: Annotated[
        float | None,
        Doc(
            """
            值必须是该数的倍数，仅适用于数值。
            """
        ),
    ] = _Unset,
    allow_inf_nan: Annotated[
        bool | None,
        Doc(
            """
            允许 `inf`、`-inf`、`nan`，仅适用于数值。
            """
        ),
    ] = _Unset,
    max_digits: Annotated[
        int | None,
        Doc(
            """
            小数值允许的最大位数。
            """
        ),
    ] = _Unset,
    decimal_places: Annotated[
        int | None,
        Doc(
            """
            小数值允许的最大小数位数。
            """
        ),
    ] = _Unset,
    examples: Annotated[
        list[Any] | None,
        Doc(
            """
            Example values for this field.

            Read more about it in the
            [FastAPI docs for Declare Request Example Data](https://fastapi.tiangolo.com/tutorial/schema-extra-example/)
            """
        ),
    ] = None,
    example: Annotated[
        Any | None,
        deprecated(
            "Deprecated in OpenAPI 3.1.0 that now uses JSON Schema 2020-12, "
            "although still supported. Use examples instead."
        ),
    ] = _Unset,
    openapi_examples: Annotated[
        dict[str, Example] | None,
        Doc(
            """
            OpenAPI 专用示例，将加入生成的 OpenAPI（例如 `/docs` 可见）。

            Swagger UI 对 OpenAPI 示例的支持优于 JSON Schema `examples`，这是主要使用场景。

            详见
            [FastAPI 请求示例数据文档](https://fastapi.tiangolo.com/tutorial/schema-extra-example/#using-the-openapi_examples-parameter).
            """
        ),
    ] = None,
    deprecated: Annotated[
        deprecated | str | bool | None,
        Doc(
            """
            将该参数字段标记为已弃用，将影响生成的 OpenAPI（例如 `/docs` 可见）。
            """
        ),
    ] = None,
    include_in_schema: Annotated[
        bool,
        Doc(
            """
            是否在生成的 OpenAPI 中包含该参数字段（通常无需设置）。

            将影响生成的 OpenAPI（例如 `/docs` 可见）。
            """
        ),
    ] = True,
    json_schema_extra: Annotated[
        dict[str, Any] | None,
        Doc(
            """
            附加的 JSON Schema 数据。
            """
        ),
    ] = None,
    **extra: Annotated[
        Any,
        Doc(
            """
            包含 JSON Schema 使用的额外字段。
            """
        ),
        deprecated(
            """
            The `extra` kwargs is deprecated. Use `json_schema_extra` instead.
            """
        ),
    ],
) -> Any:
    return params.Header(
        default=default,
        default_factory=default_factory,
        alias=alias,
        alias_priority=alias_priority,
        validation_alias=validation_alias,
        serialization_alias=serialization_alias,
        convert_underscores=convert_underscores,
        title=title,
        description=description,
        gt=gt,
        ge=ge,
        lt=lt,
        le=le,
        min_length=min_length,
        max_length=max_length,
        pattern=pattern,
        regex=regex,
        discriminator=discriminator,
        strict=strict,
        multiple_of=multiple_of,
        allow_inf_nan=allow_inf_nan,
        max_digits=max_digits,
        decimal_places=decimal_places,
        example=example,
        examples=examples,
        openapi_examples=openapi_examples,
        deprecated=deprecated,
        include_in_schema=include_in_schema,
        json_schema_extra=json_schema_extra,
        **extra,
    )


def Cookie(  # noqa: N802
    default: Annotated[
        Any,
        Doc(
            """
            参数未设置时的默认值。
            """
        ),
    ] = Undefined,
    *,
    default_factory: Annotated[
        Callable[[], Any] | None,
        Doc(
            """
            用于生成默认值的 callable。

            对 `Path` 参数无效，因其值始终必填；保留此参数仅为兼容。
            """
        ),
    ] = _Unset,
    alias: Annotated[
        str | None,
        Doc(
            """
            参数字段的别名。

            用于提取数据及生成 OpenAPI，在参数名与 Python 保留字冲突时尤其有用。
            """
        ),
    ] = None,
    alias_priority: Annotated[
        int | None,
        Doc(
            """
            别名优先级，影响是否使用别名生成器。
            """
        ),
    ] = _Unset,
    validation_alias: Annotated[
        str | AliasPath | AliasChoices | None,
        Doc(
            """
            验证白名单步骤：仅允许别名或别名集合定义的字段通过。
            """
        ),
    ] = None,
    serialization_alias: Annotated[
        str | None,
        Doc(
            """
            序列化黑名单步骤：仅保留别名字段，其余字段在序列化时忽略。
            """
        ),
    ] = None,
    title: Annotated[
        str | None,
        Doc(
            """
            人类可读的标题。
            """
        ),
    ] = None,
    description: Annotated[
        str | None,
        Doc(
            """
            人类可读的描述。
            """
        ),
    ] = None,
    gt: Annotated[
        float | None,
        Doc(
            """
            大于：若设置，值必须大于此值，仅适用于数值。
            """
        ),
    ] = None,
    ge: Annotated[
        float | None,
        Doc(
            """
            大于等于：若设置，值必须大于等于此值，仅适用于数值。
            """
        ),
    ] = None,
    lt: Annotated[
        float | None,
        Doc(
            """
            小于：若设置，值必须小于此值，仅适用于数值。
            """
        ),
    ] = None,
    le: Annotated[
        float | None,
        Doc(
            """
            小于等于：若设置，值必须小于等于此值，仅适用于数值。
            """
        ),
    ] = None,
    min_length: Annotated[
        int | None,
        Doc(
            """
            字符串最小长度。
            """
        ),
    ] = None,
    max_length: Annotated[
        int | None,
        Doc(
            """
            字符串最大长度。
            """
        ),
    ] = None,
    pattern: Annotated[
        str | None,
        Doc(
            """
            字符串的正则表达式模式。
            """
        ),
    ] = None,
    regex: Annotated[
        str | None,
        Doc(
            """
            字符串的正则表达式模式。
            """
        ),
        deprecated(
            "Deprecated in FastAPI 0.100.0 and Pydantic v2, use `pattern` instead."
        ),
    ] = None,
    discriminator: Annotated[
        str | None,
        Doc(
            """
            标记联合类型中用于区分类型的参数字段名。
            """
        ),
    ] = None,
    strict: Annotated[
        bool | None,
        Doc(
            """
            为 `True` 时对该字段启用严格验证。
            """
        ),
    ] = _Unset,
    multiple_of: Annotated[
        float | None,
        Doc(
            """
            值必须是该数的倍数，仅适用于数值。
            """
        ),
    ] = _Unset,
    allow_inf_nan: Annotated[
        bool | None,
        Doc(
            """
            允许 `inf`、`-inf`、`nan`，仅适用于数值。
            """
        ),
    ] = _Unset,
    max_digits: Annotated[
        int | None,
        Doc(
            """
            小数值允许的最大位数。
            """
        ),
    ] = _Unset,
    decimal_places: Annotated[
        int | None,
        Doc(
            """
            小数值允许的最大小数位数。
            """
        ),
    ] = _Unset,
    examples: Annotated[
        list[Any] | None,
        Doc(
            """
            Example values for this field.

            Read more about it in the
            [FastAPI docs for Declare Request Example Data](https://fastapi.tiangolo.com/tutorial/schema-extra-example/)
            """
        ),
    ] = None,
    example: Annotated[
        Any | None,
        deprecated(
            "Deprecated in OpenAPI 3.1.0 that now uses JSON Schema 2020-12, "
            "although still supported. Use examples instead."
        ),
    ] = _Unset,
    openapi_examples: Annotated[
        dict[str, Example] | None,
        Doc(
            """
            OpenAPI 专用示例，将加入生成的 OpenAPI（例如 `/docs` 可见）。

            Swagger UI 对 OpenAPI 示例的支持优于 JSON Schema `examples`，这是主要使用场景。

            详见
            [FastAPI 请求示例数据文档](https://fastapi.tiangolo.com/tutorial/schema-extra-example/#using-the-openapi_examples-parameter).
            """
        ),
    ] = None,
    deprecated: Annotated[
        deprecated | str | bool | None,
        Doc(
            """
            将该参数字段标记为已弃用，将影响生成的 OpenAPI（例如 `/docs` 可见）。
            """
        ),
    ] = None,
    include_in_schema: Annotated[
        bool,
        Doc(
            """
            是否在生成的 OpenAPI 中包含该参数字段（通常无需设置）。

            将影响生成的 OpenAPI（例如 `/docs` 可见）。
            """
        ),
    ] = True,
    json_schema_extra: Annotated[
        dict[str, Any] | None,
        Doc(
            """
            附加的 JSON Schema 数据。
            """
        ),
    ] = None,
    **extra: Annotated[
        Any,
        Doc(
            """
            包含 JSON Schema 使用的额外字段。
            """
        ),
        deprecated(
            """
            The `extra` kwargs is deprecated. Use `json_schema_extra` instead.
            """
        ),
    ],
) -> Any:
    return params.Cookie(
        default=default,
        default_factory=default_factory,
        alias=alias,
        alias_priority=alias_priority,
        validation_alias=validation_alias,
        serialization_alias=serialization_alias,
        title=title,
        description=description,
        gt=gt,
        ge=ge,
        lt=lt,
        le=le,
        min_length=min_length,
        max_length=max_length,
        pattern=pattern,
        regex=regex,
        discriminator=discriminator,
        strict=strict,
        multiple_of=multiple_of,
        allow_inf_nan=allow_inf_nan,
        max_digits=max_digits,
        decimal_places=decimal_places,
        example=example,
        examples=examples,
        openapi_examples=openapi_examples,
        deprecated=deprecated,
        include_in_schema=include_in_schema,
        json_schema_extra=json_schema_extra,
        **extra,
    )


def Body(  # noqa: N802
    default: Annotated[
        Any,
        Doc(
            """
            参数未设置时的默认值。
            """
        ),
    ] = Undefined,
    *,
    default_factory: Annotated[
        Callable[[], Any] | None,
        Doc(
            """
            用于生成默认值的 callable。

            对 `Path` 参数无效，因其值始终必填；保留此参数仅为兼容。
            """
        ),
    ] = _Unset,
    embed: Annotated[
        bool | None,
        Doc(
            """
            当 `embed` 为 `True` 时，参数作为 JSON body 中的键而非 body 本身。

            声明多个 `Body` 参数时会自动 embed。

            详见
            [FastAPI 多 Body 参数文档](https://fastapi.tiangolo.com/tutorial/body-multiple-params/#embed-a-single-body-parameter).
            """
        ),
    ] = None,
    media_type: Annotated[
        str,
        Doc(
            """
            该参数字段的媒体类型。修改会影响生成的 OpenAPI，但目前不影响数据解析。
            """
        ),
    ] = "application/json",
    alias: Annotated[
        str | None,
        Doc(
            """
            参数字段的别名。

            用于提取数据及生成 OpenAPI，在参数名与 Python 保留字冲突时尤其有用。
            """
        ),
    ] = None,
    alias_priority: Annotated[
        int | None,
        Doc(
            """
            别名优先级，影响是否使用别名生成器。
            """
        ),
    ] = _Unset,
    validation_alias: Annotated[
        str | AliasPath | AliasChoices | None,
        Doc(
            """
            验证白名单步骤：仅允许别名或别名集合定义的字段通过。
            """
        ),
    ] = None,
    serialization_alias: Annotated[
        str | None,
        Doc(
            """
            序列化黑名单步骤：仅保留别名字段，其余字段在序列化时忽略。
            """
        ),
    ] = None,
    title: Annotated[
        str | None,
        Doc(
            """
            人类可读的标题。
            """
        ),
    ] = None,
    description: Annotated[
        str | None,
        Doc(
            """
            人类可读的描述。
            """
        ),
    ] = None,
    gt: Annotated[
        float | None,
        Doc(
            """
            大于：若设置，值必须大于此值，仅适用于数值。
            """
        ),
    ] = None,
    ge: Annotated[
        float | None,
        Doc(
            """
            大于等于：若设置，值必须大于等于此值，仅适用于数值。
            """
        ),
    ] = None,
    lt: Annotated[
        float | None,
        Doc(
            """
            小于：若设置，值必须小于此值，仅适用于数值。
            """
        ),
    ] = None,
    le: Annotated[
        float | None,
        Doc(
            """
            小于等于：若设置，值必须小于等于此值，仅适用于数值。
            """
        ),
    ] = None,
    min_length: Annotated[
        int | None,
        Doc(
            """
            字符串最小长度。
            """
        ),
    ] = None,
    max_length: Annotated[
        int | None,
        Doc(
            """
            字符串最大长度。
            """
        ),
    ] = None,
    pattern: Annotated[
        str | None,
        Doc(
            """
            字符串的正则表达式模式。
            """
        ),
    ] = None,
    regex: Annotated[
        str | None,
        Doc(
            """
            字符串的正则表达式模式。
            """
        ),
        deprecated(
            "Deprecated in FastAPI 0.100.0 and Pydantic v2, use `pattern` instead."
        ),
    ] = None,
    discriminator: Annotated[
        str | None,
        Doc(
            """
            标记联合类型中用于区分类型的参数字段名。
            """
        ),
    ] = None,
    strict: Annotated[
        bool | None,
        Doc(
            """
            为 `True` 时对该字段启用严格验证。
            """
        ),
    ] = _Unset,
    multiple_of: Annotated[
        float | None,
        Doc(
            """
            值必须是该数的倍数，仅适用于数值。
            """
        ),
    ] = _Unset,
    allow_inf_nan: Annotated[
        bool | None,
        Doc(
            """
            允许 `inf`、`-inf`、`nan`，仅适用于数值。
            """
        ),
    ] = _Unset,
    max_digits: Annotated[
        int | None,
        Doc(
            """
            小数值允许的最大位数。
            """
        ),
    ] = _Unset,
    decimal_places: Annotated[
        int | None,
        Doc(
            """
            小数值允许的最大小数位数。
            """
        ),
    ] = _Unset,
    examples: Annotated[
        list[Any] | None,
        Doc(
            """
            Example values for this field.

            Read more about it in the
            [FastAPI docs for Declare Request Example Data](https://fastapi.tiangolo.com/tutorial/schema-extra-example/)
            """
        ),
    ] = None,
    example: Annotated[
        Any | None,
        deprecated(
            "Deprecated in OpenAPI 3.1.0 that now uses JSON Schema 2020-12, "
            "although still supported. Use examples instead."
        ),
    ] = _Unset,
    openapi_examples: Annotated[
        dict[str, Example] | None,
        Doc(
            """
            OpenAPI 专用示例，将加入生成的 OpenAPI（例如 `/docs` 可见）。

            Swagger UI 对 OpenAPI 示例的支持优于 JSON Schema `examples`，这是主要使用场景。

            详见
            [FastAPI 请求示例数据文档](https://fastapi.tiangolo.com/tutorial/schema-extra-example/#using-the-openapi_examples-parameter).
            """
        ),
    ] = None,
    deprecated: Annotated[
        deprecated | str | bool | None,
        Doc(
            """
            将该参数字段标记为已弃用，将影响生成的 OpenAPI（例如 `/docs` 可见）。
            """
        ),
    ] = None,
    include_in_schema: Annotated[
        bool,
        Doc(
            """
            是否在生成的 OpenAPI 中包含该参数字段（通常无需设置）。

            将影响生成的 OpenAPI（例如 `/docs` 可见）。
            """
        ),
    ] = True,
    json_schema_extra: Annotated[
        dict[str, Any] | None,
        Doc(
            """
            附加的 JSON Schema 数据。
            """
        ),
    ] = None,
    **extra: Annotated[
        Any,
        Doc(
            """
            包含 JSON Schema 使用的额外字段。
            """
        ),
        deprecated(
            """
            The `extra` kwargs is deprecated. Use `json_schema_extra` instead.
            """
        ),
    ],
) -> Any:
    return params.Body(
        default=default,
        default_factory=default_factory,
        embed=embed,
        media_type=media_type,
        alias=alias,
        alias_priority=alias_priority,
        validation_alias=validation_alias,
        serialization_alias=serialization_alias,
        title=title,
        description=description,
        gt=gt,
        ge=ge,
        lt=lt,
        le=le,
        min_length=min_length,
        max_length=max_length,
        pattern=pattern,
        regex=regex,
        discriminator=discriminator,
        strict=strict,
        multiple_of=multiple_of,
        allow_inf_nan=allow_inf_nan,
        max_digits=max_digits,
        decimal_places=decimal_places,
        example=example,
        examples=examples,
        openapi_examples=openapi_examples,
        deprecated=deprecated,
        include_in_schema=include_in_schema,
        json_schema_extra=json_schema_extra,
        **extra,
    )


def Form(  # noqa: N802
    default: Annotated[
        Any,
        Doc(
            """
            参数未设置时的默认值。
            """
        ),
    ] = Undefined,
    *,
    default_factory: Annotated[
        Callable[[], Any] | None,
        Doc(
            """
            用于生成默认值的 callable。

            对 `Path` 参数无效，因其值始终必填；保留此参数仅为兼容。
            """
        ),
    ] = _Unset,
    media_type: Annotated[
        str,
        Doc(
            """
            该参数字段的媒体类型。修改会影响生成的 OpenAPI，但目前不影响数据解析。
            """
        ),
    ] = "application/x-www-form-urlencoded",
    alias: Annotated[
        str | None,
        Doc(
            """
            参数字段的别名。

            用于提取数据及生成 OpenAPI，在参数名与 Python 保留字冲突时尤其有用。
            """
        ),
    ] = None,
    alias_priority: Annotated[
        int | None,
        Doc(
            """
            别名优先级，影响是否使用别名生成器。
            """
        ),
    ] = _Unset,
    validation_alias: Annotated[
        str | AliasPath | AliasChoices | None,
        Doc(
            """
            验证白名单步骤：仅允许别名或别名集合定义的字段通过。
            """
        ),
    ] = None,
    serialization_alias: Annotated[
        str | None,
        Doc(
            """
            序列化黑名单步骤：仅保留别名字段，其余字段在序列化时忽略。
            """
        ),
    ] = None,
    title: Annotated[
        str | None,
        Doc(
            """
            人类可读的标题。
            """
        ),
    ] = None,
    description: Annotated[
        str | None,
        Doc(
            """
            人类可读的描述。
            """
        ),
    ] = None,
    gt: Annotated[
        float | None,
        Doc(
            """
            大于：若设置，值必须大于此值，仅适用于数值。
            """
        ),
    ] = None,
    ge: Annotated[
        float | None,
        Doc(
            """
            大于等于：若设置，值必须大于等于此值，仅适用于数值。
            """
        ),
    ] = None,
    lt: Annotated[
        float | None,
        Doc(
            """
            小于：若设置，值必须小于此值，仅适用于数值。
            """
        ),
    ] = None,
    le: Annotated[
        float | None,
        Doc(
            """
            小于等于：若设置，值必须小于等于此值，仅适用于数值。
            """
        ),
    ] = None,
    min_length: Annotated[
        int | None,
        Doc(
            """
            字符串最小长度。
            """
        ),
    ] = None,
    max_length: Annotated[
        int | None,
        Doc(
            """
            字符串最大长度。
            """
        ),
    ] = None,
    pattern: Annotated[
        str | None,
        Doc(
            """
            字符串的正则表达式模式。
            """
        ),
    ] = None,
    regex: Annotated[
        str | None,
        Doc(
            """
            字符串的正则表达式模式。
            """
        ),
        deprecated(
            "Deprecated in FastAPI 0.100.0 and Pydantic v2, use `pattern` instead."
        ),
    ] = None,
    discriminator: Annotated[
        str | None,
        Doc(
            """
            标记联合类型中用于区分类型的参数字段名。
            """
        ),
    ] = None,
    strict: Annotated[
        bool | None,
        Doc(
            """
            为 `True` 时对该字段启用严格验证。
            """
        ),
    ] = _Unset,
    multiple_of: Annotated[
        float | None,
        Doc(
            """
            值必须是该数的倍数，仅适用于数值。
            """
        ),
    ] = _Unset,
    allow_inf_nan: Annotated[
        bool | None,
        Doc(
            """
            允许 `inf`、`-inf`、`nan`，仅适用于数值。
            """
        ),
    ] = _Unset,
    max_digits: Annotated[
        int | None,
        Doc(
            """
            小数值允许的最大位数。
            """
        ),
    ] = _Unset,
    decimal_places: Annotated[
        int | None,
        Doc(
            """
            小数值允许的最大小数位数。
            """
        ),
    ] = _Unset,
    examples: Annotated[
        list[Any] | None,
        Doc(
            """
            Example values for this field.

            Read more about it in the
            [FastAPI docs for Declare Request Example Data](https://fastapi.tiangolo.com/tutorial/schema-extra-example/)
            """
        ),
    ] = None,
    example: Annotated[
        Any | None,
        deprecated(
            "Deprecated in OpenAPI 3.1.0 that now uses JSON Schema 2020-12, "
            "although still supported. Use examples instead."
        ),
    ] = _Unset,
    openapi_examples: Annotated[
        dict[str, Example] | None,
        Doc(
            """
            OpenAPI 专用示例，将加入生成的 OpenAPI（例如 `/docs` 可见）。

            Swagger UI 对 OpenAPI 示例的支持优于 JSON Schema `examples`，这是主要使用场景。

            详见
            [FastAPI 请求示例数据文档](https://fastapi.tiangolo.com/tutorial/schema-extra-example/#using-the-openapi_examples-parameter).
            """
        ),
    ] = None,
    deprecated: Annotated[
        deprecated | str | bool | None,
        Doc(
            """
            将该参数字段标记为已弃用，将影响生成的 OpenAPI（例如 `/docs` 可见）。
            """
        ),
    ] = None,
    include_in_schema: Annotated[
        bool,
        Doc(
            """
            是否在生成的 OpenAPI 中包含该参数字段（通常无需设置）。

            将影响生成的 OpenAPI（例如 `/docs` 可见）。
            """
        ),
    ] = True,
    json_schema_extra: Annotated[
        dict[str, Any] | None,
        Doc(
            """
            附加的 JSON Schema 数据。
            """
        ),
    ] = None,
    **extra: Annotated[
        Any,
        Doc(
            """
            包含 JSON Schema 使用的额外字段。
            """
        ),
        deprecated(
            """
            The `extra` kwargs is deprecated. Use `json_schema_extra` instead.
            """
        ),
    ],
) -> Any:
    return params.Form(
        default=default,
        default_factory=default_factory,
        media_type=media_type,
        alias=alias,
        alias_priority=alias_priority,
        validation_alias=validation_alias,
        serialization_alias=serialization_alias,
        title=title,
        description=description,
        gt=gt,
        ge=ge,
        lt=lt,
        le=le,
        min_length=min_length,
        max_length=max_length,
        pattern=pattern,
        regex=regex,
        discriminator=discriminator,
        strict=strict,
        multiple_of=multiple_of,
        allow_inf_nan=allow_inf_nan,
        max_digits=max_digits,
        decimal_places=decimal_places,
        example=example,
        examples=examples,
        openapi_examples=openapi_examples,
        deprecated=deprecated,
        include_in_schema=include_in_schema,
        json_schema_extra=json_schema_extra,
        **extra,
    )


def File(  # noqa: N802
    default: Annotated[
        Any,
        Doc(
            """
            参数未设置时的默认值。
            """
        ),
    ] = Undefined,
    *,
    default_factory: Annotated[
        Callable[[], Any] | None,
        Doc(
            """
            用于生成默认值的 callable。

            对 `Path` 参数无效，因其值始终必填；保留此参数仅为兼容。
            """
        ),
    ] = _Unset,
    media_type: Annotated[
        str,
        Doc(
            """
            该参数字段的媒体类型。修改会影响生成的 OpenAPI，但目前不影响数据解析。
            """
        ),
    ] = "multipart/form-data",
    alias: Annotated[
        str | None,
        Doc(
            """
            参数字段的别名。

            用于提取数据及生成 OpenAPI，在参数名与 Python 保留字冲突时尤其有用。
            """
        ),
    ] = None,
    alias_priority: Annotated[
        int | None,
        Doc(
            """
            别名优先级，影响是否使用别名生成器。
            """
        ),
    ] = _Unset,
    validation_alias: Annotated[
        str | AliasPath | AliasChoices | None,
        Doc(
            """
            验证白名单步骤：仅允许别名或别名集合定义的字段通过。
            """
        ),
    ] = None,
    serialization_alias: Annotated[
        str | None,
        Doc(
            """
            序列化黑名单步骤：仅保留别名字段，其余字段在序列化时忽略。
            """
        ),
    ] = None,
    title: Annotated[
        str | None,
        Doc(
            """
            人类可读的标题。
            """
        ),
    ] = None,
    description: Annotated[
        str | None,
        Doc(
            """
            人类可读的描述。
            """
        ),
    ] = None,
    gt: Annotated[
        float | None,
        Doc(
            """
            大于：若设置，值必须大于此值，仅适用于数值。
            """
        ),
    ] = None,
    ge: Annotated[
        float | None,
        Doc(
            """
            大于等于：若设置，值必须大于等于此值，仅适用于数值。
            """
        ),
    ] = None,
    lt: Annotated[
        float | None,
        Doc(
            """
            小于：若设置，值必须小于此值，仅适用于数值。
            """
        ),
    ] = None,
    le: Annotated[
        float | None,
        Doc(
            """
            小于等于：若设置，值必须小于等于此值，仅适用于数值。
            """
        ),
    ] = None,
    min_length: Annotated[
        int | None,
        Doc(
            """
            字符串最小长度。
            """
        ),
    ] = None,
    max_length: Annotated[
        int | None,
        Doc(
            """
            字符串最大长度。
            """
        ),
    ] = None,
    pattern: Annotated[
        str | None,
        Doc(
            """
            字符串的正则表达式模式。
            """
        ),
    ] = None,
    regex: Annotated[
        str | None,
        Doc(
            """
            字符串的正则表达式模式。
            """
        ),
        deprecated(
            "Deprecated in FastAPI 0.100.0 and Pydantic v2, use `pattern` instead."
        ),
    ] = None,
    discriminator: Annotated[
        str | None,
        Doc(
            """
            标记联合类型中用于区分类型的参数字段名。
            """
        ),
    ] = None,
    strict: Annotated[
        bool | None,
        Doc(
            """
            为 `True` 时对该字段启用严格验证。
            """
        ),
    ] = _Unset,
    multiple_of: Annotated[
        float | None,
        Doc(
            """
            值必须是该数的倍数，仅适用于数值。
            """
        ),
    ] = _Unset,
    allow_inf_nan: Annotated[
        bool | None,
        Doc(
            """
            允许 `inf`、`-inf`、`nan`，仅适用于数值。
            """
        ),
    ] = _Unset,
    max_digits: Annotated[
        int | None,
        Doc(
            """
            小数值允许的最大位数。
            """
        ),
    ] = _Unset,
    decimal_places: Annotated[
        int | None,
        Doc(
            """
            小数值允许的最大小数位数。
            """
        ),
    ] = _Unset,
    examples: Annotated[
        list[Any] | None,
        Doc(
            """
            Example values for this field.

            Read more about it in the
            [FastAPI docs for Declare Request Example Data](https://fastapi.tiangolo.com/tutorial/schema-extra-example/)
            """
        ),
    ] = None,
    example: Annotated[
        Any | None,
        deprecated(
            "Deprecated in OpenAPI 3.1.0 that now uses JSON Schema 2020-12, "
            "although still supported. Use examples instead."
        ),
    ] = _Unset,
    openapi_examples: Annotated[
        dict[str, Example] | None,
        Doc(
            """
            OpenAPI 专用示例，将加入生成的 OpenAPI（例如 `/docs` 可见）。

            Swagger UI 对 OpenAPI 示例的支持优于 JSON Schema `examples`，这是主要使用场景。

            详见
            [FastAPI 请求示例数据文档](https://fastapi.tiangolo.com/tutorial/schema-extra-example/#using-the-openapi_examples-parameter).
            """
        ),
    ] = None,
    deprecated: Annotated[
        deprecated | str | bool | None,
        Doc(
            """
            将该参数字段标记为已弃用，将影响生成的 OpenAPI（例如 `/docs` 可见）。
            """
        ),
    ] = None,
    include_in_schema: Annotated[
        bool,
        Doc(
            """
            是否在生成的 OpenAPI 中包含该参数字段（通常无需设置）。

            将影响生成的 OpenAPI（例如 `/docs` 可见）。
            """
        ),
    ] = True,
    json_schema_extra: Annotated[
        dict[str, Any] | None,
        Doc(
            """
            附加的 JSON Schema 数据。
            """
        ),
    ] = None,
    **extra: Annotated[
        Any,
        Doc(
            """
            包含 JSON Schema 使用的额外字段。
            """
        ),
        deprecated(
            """
            The `extra` kwargs is deprecated. Use `json_schema_extra` instead.
            """
        ),
    ],
) -> Any:
    return params.File(
        default=default,
        default_factory=default_factory,
        media_type=media_type,
        alias=alias,
        alias_priority=alias_priority,
        validation_alias=validation_alias,
        serialization_alias=serialization_alias,
        title=title,
        description=description,
        gt=gt,
        ge=ge,
        lt=lt,
        le=le,
        min_length=min_length,
        max_length=max_length,
        pattern=pattern,
        regex=regex,
        discriminator=discriminator,
        strict=strict,
        multiple_of=multiple_of,
        allow_inf_nan=allow_inf_nan,
        max_digits=max_digits,
        decimal_places=decimal_places,
        example=example,
        examples=examples,
        openapi_examples=openapi_examples,
        deprecated=deprecated,
        include_in_schema=include_in_schema,
        json_schema_extra=json_schema_extra,
        **extra,
    )


def Depends(  # noqa: N802
    dependency: Annotated[
        Callable[..., Any] | None,
        Doc(
            """
            A "dependable" callable (like a function).

            不要直接调用，FastAPI 会自动调用，只需传入对象本身。

            详见
            [FastAPI 依赖项文档](https://fastapi.tiangolo.com/tutorial/dependencies/)
            """
        ),
    ] = None,
    *,
    use_cache: Annotated[
        bool,
        Doc(
            """
            默认同一次请求中依赖首次调用后，若后续再次声明同一依赖，将复用已求值结果。

            将 `use_cache` 设为 `False` 可禁用该行为，确保同一请求内多次声明时重新调用。

            详见
            [FastAPI 子依赖文档](https://fastapi.tiangolo.com/tutorial/dependencies/sub-dependencies/#using-the-same-dependency-multiple-times)
            """
        ),
    ] = True,
    scope: Annotated[
        Literal["function", "request"] | None,
        Doc(
            """
            主要用于带 `yield` 的依赖，定义依赖函数何时开始（yield 前）与结束（yield 后）。

            * `"function"`：在 path operation 函数前后执行，但在响应发送给客户端**之前**结束。
            * `"request"`：类似 `"function"`，但在响应发送给客户端**之后**结束。

            详见
            [FastAPI yield 依赖文档](https://fastapi.tiangolo.com/tutorial/dependencies/dependencies-with-yield/#early-exit-and-scope)
            """
        ),
    ] = None,
) -> Any:
    """
    声明 FastAPI 依赖项。

    接受单个可调用“dependable”（如函数）；不要直接调用，FastAPI 会自动调用。

    详见
    [FastAPI 依赖项文档](https://fastapi.tiangolo.com/tutorial/dependencies/).

    **Example**

    ```python
    from typing import Annotated

    from fastapi import Depends, FastAPI

    app = FastAPI()


    async def common_parameters(q: str | None = None, skip: int = 0, limit: int = 100):
        return {"q": q, "skip": skip, "limit": limit}


    @app.get("/items/")
    async def read_items(commons: Annotated[dict, Depends(common_parameters)]):
        return commons
    ```
    """
    return params.Depends(dependency=dependency, use_cache=use_cache, scope=scope)


def Security(  # noqa: N802
    dependency: Annotated[
        Callable[..., Any] | None,
        Doc(
            """
            A "dependable" callable (like a function).

            不要直接调用，FastAPI 会自动调用，只需传入对象本身。

            详见
            [FastAPI 依赖项文档](https://fastapi.tiangolo.com/tutorial/dependencies/)
            """
        ),
    ] = None,
    *,
    scopes: Annotated[
        Sequence[str] | None,
        Doc(
            """
            使用该 Security 依赖的 *path operation* 所需的 OAuth2 作用域。

            “scope” 来自 OAuth2 规范，通常表示权限或角色，并集成到 OpenAPI 与 `/docs`。

            详见
            [FastAPI OAuth2 作用域文档](https://fastapi.tiangolo.com/advanced/security/oauth2-scopes/)
            """
        ),
    ] = None,
    use_cache: Annotated[
        bool,
        Doc(
            """
            默认同一次请求中依赖首次调用后，若后续再次声明同一依赖，将复用已求值结果。

            将 `use_cache` 设为 `False` 可禁用该行为，确保同一请求内多次声明时重新调用。

            详见
            [FastAPI 子依赖文档](https://fastapi.tiangolo.com/tutorial/dependencies/sub-dependencies/#using-the-same-dependency-multiple-times)
            """
        ),
    ] = True,
) -> Any:
    """
    声明 FastAPI Security 依赖项。

    与普通依赖的唯一区别是可声明 OAuth2 作用域，并集成到 OpenAPI 与 `/docs` UI。

    接受单个可调用“dependable”；不要直接调用，FastAPI 会自动调用。

    详见
    [FastAPI 安全文档](https://fastapi.tiangolo.com/tutorial/security/) 与
    [OAuth2 作用域文档](https://fastapi.tiangolo.com/advanced/security/oauth2-scopes/).

    **Example**

    ```python
    from typing import Annotated

    from fastapi import Security, FastAPI

    from .db import User
    from .security import get_current_active_user

    app = FastAPI()

    @app.get("/users/me/items/")
    async def read_own_items(
        current_user: Annotated[User, Security(get_current_active_user, scopes=["items"])]
    ):
        return [{"item_id": "Foo", "owner": current_user.username}]
    ```
    """
    return params.Security(dependency=dependency, scopes=scopes, use_cache=use_cache)
