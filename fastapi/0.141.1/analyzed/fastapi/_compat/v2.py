"""Pydantic v2 兼容层：模型字段、JSON Schema 生成与序列化。"""

import re
import warnings
from collections.abc import Sequence
from copy import copy
from dataclasses import dataclass, is_dataclass
from enum import Enum
from functools import lru_cache
from typing import (
    Annotated,
    Any,
    Literal,
    Union,
    cast,
    get_args,
    get_origin,
)

from fastapi._compat import lenient_issubclass, shared
from fastapi.openapi.constants import REF_TEMPLATE
from fastapi.types import IncEx, ModelNameMap, UnionType
from pydantic import BaseModel, ConfigDict, Field, TypeAdapter, create_model
from pydantic import PydanticSchemaGenerationError as PydanticSchemaGenerationError
from pydantic import PydanticUndefinedAnnotation as PydanticUndefinedAnnotation
from pydantic import ValidationError as ValidationError
from pydantic._internal import _typing_extra as _pydantic_typing_extra
from pydantic._internal._schema_generation_shared import (  # type: ignore[attr-defined]
    GetJsonSchemaHandler as GetJsonSchemaHandler,
)
from pydantic.fields import FieldInfo as FieldInfo
from pydantic.json_schema import GenerateJsonSchema as _GenerateJsonSchema
from pydantic.json_schema import JsonSchemaValue as JsonSchemaValue
from pydantic_core import CoreSchema as CoreSchema
from pydantic_core import PydanticUndefined
from pydantic_core import Url as Url
from pydantic_core.core_schema import (
    with_info_plain_validator_function as with_info_plain_validator_function,
)

RequiredParam = PydanticUndefined
Undefined = PydanticUndefined


def evaluate_forwardref(
    value: Any,
    globalns: dict[str, Any] | None = None,
    localns: dict[str, Any] | None = None,
) -> Any:
    # eval_type_lenient 自 Pydantic v2.10.0b1 起已弃用（PR #10530）
    try_eval_type = getattr(_pydantic_typing_extra, "try_eval_type", None)
    if try_eval_type is not None:
        return try_eval_type(value, globalns, localns)[0]
    return _pydantic_typing_extra.eval_type_lenient(  # ty: ignore[deprecated]
        value, globalns, localns
    )


class GenerateJsonSchema(_GenerateJsonSchema):
    # TODO：待 https://github.com/pydantic/pydantic/pull/12841 合并后移除
    # 并停止支持更早的 Pydantic 版本（预计还需很久）
    def bytes_schema(self, schema: CoreSchema) -> JsonSchemaValue:
        json_schema = {"type": "string", "contentMediaType": "application/octet-stream"}
        bytes_mode = (
            self._config.ser_json_bytes
            if self.mode == "serialization"
            else self._config.val_json_bytes
        )
        if bytes_mode == "base64":
            json_schema["contentEncoding"] = "base64"
        self.update_with_validations(json_schema, schema, self.ValidationsMapping.bytes)
        return json_schema


# TODO：停止支持 Pydantic < v2.12.3 后移除
_Attrs = {
    "default": ...,
    "default_factory": None,
    "alias": None,
    "alias_priority": None,
    "validation_alias": None,
    "serialization_alias": None,
    "title": None,
    "field_title_generator": None,
    "description": None,
    "examples": None,
    "exclude": None,
    "exclude_if": None,
    "discriminator": None,
    "deprecated": None,
    "json_schema_extra": None,
    "frozen": None,
    "validate_default": None,
    "repr": True,
    "init": None,
    "init_var": None,
    "kw_only": None,
}


# TODO：停止支持 Pydantic < v2.12.3 后移除
def asdict(field_info: FieldInfo) -> dict[str, Any]:
    attributes = {}
    for attr in _Attrs:
        value = getattr(field_info, attr, Undefined)
        if value is not Undefined:
            attributes[attr] = value
    return {
        "annotation": field_info.annotation,
        "metadata": field_info.metadata,
        "attributes": attributes,
    }


@dataclass
class ModelField:
    """封装 Pydantic FieldInfo 与 TypeAdapter，供验证与序列化使用。"""
    field_info: FieldInfo
    name: str
    mode: Literal["validation", "serialization"] = "validation"
    config: ConfigDict | None = None

    @property
    def alias(self) -> str:
        a = self.field_info.alias
        return a if a is not None else self.name

    @property
    def validation_alias(self) -> str | None:
        va = self.field_info.validation_alias
        if isinstance(va, str) and va:
            return va
        return None

    @property
    def serialization_alias(self) -> str | None:
        sa = self.field_info.serialization_alias
        return sa or None

    @property
    def default(self) -> Any:
        return self.get_default()

    def __post_init__(self) -> None:
        with warnings.catch_warnings():
            # Pydantic >= 2.12.0 会对未使用的字段级元数据发出警告
            #（例如 `TypeAdapter(Annotated[int, Field(alias='b')])`）。有时我们
            # 从模型字段注解构建 TypeAdapter，因此需要忽略该警告：
            if shared.PYDANTIC_VERSION_MINOR_TUPLE >= (2, 12):
                from pydantic.warnings import UnsupportedFieldAttributeWarning

                warnings.simplefilter(
                    "ignore", category=UnsupportedFieldAttributeWarning
                )
            # TODO：最低 Pydantic 版本升至 v2.12.3 后移除，改用 self.field_info.asdict()
            field_dict = asdict(self.field_info)
            annotated_args = (
                field_dict["annotation"],
                *field_dict["metadata"],
                # 需重新创建 FieldInfo，避免携带旧元数据，仅保留其余属性
                Field(**field_dict["attributes"]),
            )
            self._type_adapter: TypeAdapter[Any] = TypeAdapter(
                Annotated[annotated_args],  # ty: ignore[invalid-type-form]
                config=self.config,
            )

    def get_default(self) -> Any:
        if self.field_info.is_required():
            return Undefined
        return self.field_info.get_default(call_default_factory=True)

    def validate(
        self,
        value: Any,
        values: dict[str, Any] = {},  # noqa: B006
        *,
        loc: tuple[int | str, ...] = (),
    ) -> tuple[Any, list[dict[str, Any]]]:
        try:
            return (
                self._type_adapter.validate_python(value, from_attributes=True),
                [],
            )
        except ValidationError as exc:
            return None, _regenerate_error_with_loc(
                errors=exc.errors(include_url=False), loc_prefix=loc
            )

    def serialize(
        self,
        value: Any,
        *,
        mode: Literal["json", "python"] = "json",
        include: IncEx | None = None,
        exclude: IncEx | None = None,
        by_alias: bool = True,
        exclude_unset: bool = False,
        exclude_defaults: bool = False,
        exclude_none: bool = False,
    ) -> Any:
        # 调用方传入的值已通过 self._type_adapter.validate_python(value) 验证
        return self._type_adapter.dump_python(
            value,
            mode=mode,
            include=include,
            exclude=exclude,
            by_alias=by_alias,
            exclude_unset=exclude_unset,
            exclude_defaults=exclude_defaults,
            exclude_none=exclude_none,
        )

    def serialize_json(
        self,
        value: Any,
        *,
        include: IncEx | None = None,
        exclude: IncEx | None = None,
        by_alias: bool = True,
        exclude_unset: bool = False,
        exclude_defaults: bool = False,
        exclude_none: bool = False,
    ) -> bytes:
        # 调用方传入的值已通过 self._type_adapter.validate_python(value) 验证
        # 使用 Pydantic dump_json() 经 Rust 一步序列化为 JSON 字节，
        # 避免 dump_python(mode="json") + json.dumps() 的中间 dict 步骤
        return self._type_adapter.dump_json(
            value,
            include=include,
            exclude=exclude,
            by_alias=by_alias,
            exclude_unset=exclude_unset,
            exclude_defaults=exclude_defaults,
            exclude_none=exclude_none,
        )

    def __hash__(self) -> int:
        # 每个 ModelField 实例唯一，便于建立 ModelField 到 JSON Schema 的映射
        return id(self)


def _has_computed_fields(field: ModelField) -> bool:
    computed_fields = field._type_adapter.core_schema.get("schema", {}).get(
        "computed_fields", []
    )
    return len(computed_fields) > 0


def get_schema_from_model_field(
    *,
    field: ModelField,
    model_name_map: ModelNameMap,
    field_mapping: dict[
        tuple[ModelField, Literal["validation", "serialization"]], JsonSchemaValue
    ],
    separate_input_output_schemas: bool = True,
) -> dict[str, Any]:
    override_mode: Literal["validation"] | None = (
        None
        if (separate_input_output_schemas or _has_computed_fields(field))
        else "validation"
    )
    field_alias = (
        (field.validation_alias or field.alias)
        if field.mode == "validation"
        else (field.serialization_alias or field.alias)
    )

    # 假定 GenerateJsonSchema 已生成 definitions
    json_schema = field_mapping[(field, override_mode or field.mode)]
    if "$ref" not in json_schema:
        # TODO：弃用 Pydantic v1 后移除
        # 参考：https://github.com/pydantic/pydantic/blob/d61792cc42c80b13b23e3ffa74bc37ec7c77f7d1/pydantic/schema.py#L207
        json_schema["title"] = field.field_info.title or field_alias.title().replace(
            "_", " "
        )
    return json_schema


def get_definitions(
    *,
    fields: Sequence[ModelField],
    model_name_map: ModelNameMap,
    separate_input_output_schemas: bool = True,
) -> tuple[
    dict[tuple[ModelField, Literal["validation", "serialization"]], JsonSchemaValue],
    dict[str, dict[str, Any]],
]:
    schema_generator = GenerateJsonSchema(ref_template=REF_TEMPLATE)
    validation_fields = [field for field in fields if field.mode == "validation"]
    serialization_fields = [field for field in fields if field.mode == "serialization"]
    flat_validation_models = get_flat_models_from_fields(
        validation_fields, known_models=set()
    )
    flat_serialization_models = get_flat_models_from_fields(
        serialization_fields, known_models=set()
    )
    flat_validation_model_fields = [
        ModelField(
            field_info=FieldInfo(annotation=model),
            name=model.__name__,
            mode="validation",
        )
        for model in flat_validation_models
    ]
    flat_serialization_model_fields = [
        ModelField(
            field_info=FieldInfo(annotation=model),
            name=model.__name__,
            mode="serialization",
        )
        for model in flat_serialization_models
    ]
    flat_model_fields = flat_validation_model_fields + flat_serialization_model_fields
    input_types = {f.field_info.annotation for f in fields}
    unique_flat_model_fields = {
        f for f in flat_model_fields if f.field_info.annotation not in input_types
    }
    inputs = [
        (
            field,
            (
                field.mode
                if (separate_input_output_schemas or _has_computed_fields(field))
                else "validation"
            ),
            field._type_adapter.core_schema,
        )
        for field in list(fields) + list(unique_flat_model_fields)
    ]
    field_mapping, definitions = schema_generator.generate_definitions(inputs=inputs)
    for item_def in cast(dict[str, dict[str, Any]], definitions).values():
        if "description" in item_def:
            item_description = cast(str, item_def["description"]).split("\f")[0]
            item_def["description"] = item_description
    # definitions: dict[DefsRef, dict[str, Any]]
    # 但 mypy 在其他未声明为 DefsRef 的 str 处报错，而 DefsRef 本质是 str：
    # DefsRef = NewType('DefsRef', str)
    # 因此此处用 cast 简化类型
    return field_mapping, cast(dict[str, dict[str, Any]], definitions)


def is_scalar_field(field: ModelField) -> bool:
    from fastapi import params

    return shared.field_annotation_is_scalar(
        field.field_info.annotation
    ) and not isinstance(field.field_info, params.Body)


def copy_field_info(*, field_info: FieldInfo, annotation: Any) -> FieldInfo:
    cls = type(field_info)
    merged_field_info = cls.from_annotation(annotation)
    new_field_info = copy(field_info)
    new_field_info.metadata = merged_field_info.metadata
    new_field_info.annotation = merged_field_info.annotation
    return new_field_info


def serialize_sequence_value(*, field: ModelField, value: Any) -> Sequence[Any]:
    origin_type = get_origin(field.field_info.annotation) or field.field_info.annotation
    if origin_type is Union or origin_type is UnionType:  # 处理可选序列类型
        union_args = get_args(field.field_info.annotation)
        for union_arg in union_args:
            if union_arg is type(None):
                continue
            origin_type = get_origin(union_arg) or union_arg
            break
    assert issubclass(origin_type, shared.sequence_types)  # type: ignore[arg-type]  # ty: ignore[invalid-argument-type]
    return shared.sequence_annotation_to_type[origin_type](value)  # type: ignore[no-any-return,index]  # ty: ignore[invalid-return-type]


def get_missing_field_error(loc: tuple[int | str, ...]) -> dict[str, Any]:
    error = ValidationError.from_exception_data(
        "Field required", [{"type": "missing", "loc": loc, "input": {}}]
    ).errors(include_url=False)[0]
    error["input"] = None
    return error  # type: ignore[return-value]  # ty: ignore[invalid-return-type]


def create_body_model(
    *, fields: Sequence[ModelField], model_name: str
) -> type[BaseModel]:
    field_params = {f.name: (f.field_info.annotation, f.field_info) for f in fields}
    BodyModel: type[BaseModel] = create_model(model_name, **field_params)  # type: ignore[call-overload]  # ty: ignore[no-matching-overload]
    return BodyModel


def get_model_fields(model: type[BaseModel]) -> list[ModelField]:
    model_fields: list[ModelField] = []
    for name, field_info in model.model_fields.items():
        type_ = field_info.annotation
        if lenient_issubclass(type_, (BaseModel, dict)) or is_dataclass(type_):
            model_config = None
        else:
            model_config = model.model_config
        model_fields.append(
            ModelField(
                field_info=field_info,
                name=name,
                config=model_config,
            )
        )
    return model_fields


@lru_cache
def get_cached_model_fields(model: type[BaseModel]) -> list[ModelField]:
    return get_model_fields(model)


# 复制自 Pydantic v1 的若干 schema 函数，以兼容 v2 并允许混用模型

TypeModelOrEnum = type["BaseModel"] | type[Enum]
TypeModelSet = set[TypeModelOrEnum]


def normalize_name(name: str) -> str:
    return re.sub(r"[^a-zA-Z0-9.\-_]", "_", name)


def get_model_name_map(unique_models: TypeModelSet) -> dict[TypeModelOrEnum, str]:
    name_model_map = {}
    for model in unique_models:
        model_name = normalize_name(model.__name__)
        name_model_map[model_name] = model
    return {v: k for k, v in name_model_map.items()}


def get_flat_models_from_model(
    model: type["BaseModel"], known_models: TypeModelSet | None = None
) -> TypeModelSet:
    known_models = known_models or set()
    fields = get_model_fields(model)
    get_flat_models_from_fields(fields, known_models=known_models)
    return known_models


def get_flat_models_from_annotation(
    annotation: Any, known_models: TypeModelSet
) -> TypeModelSet:
    origin = get_origin(annotation)
    if origin is not None:
        for arg in get_args(annotation):
            if lenient_issubclass(arg, (BaseModel, Enum)):
                if arg not in known_models:
                    known_models.add(arg)  # type: ignore[arg-type]
                    if lenient_issubclass(arg, BaseModel):
                        get_flat_models_from_model(arg, known_models=known_models)
            else:
                get_flat_models_from_annotation(arg, known_models=known_models)
    return known_models


def get_flat_models_from_field(
    field: ModelField, known_models: TypeModelSet
) -> TypeModelSet:
    field_type = field.field_info.annotation
    if lenient_issubclass(field_type, BaseModel):
        if field_type in known_models:
            return known_models
        known_models.add(field_type)
        get_flat_models_from_model(field_type, known_models=known_models)
    elif lenient_issubclass(field_type, Enum):
        known_models.add(field_type)
    else:
        get_flat_models_from_annotation(field_type, known_models=known_models)
    return known_models


def get_flat_models_from_fields(
    fields: Sequence[ModelField], known_models: TypeModelSet
) -> TypeModelSet:
    for field in fields:
        get_flat_models_from_field(field, known_models=known_models)
    return known_models


def _regenerate_error_with_loc(
    *, errors: Sequence[Any], loc_prefix: tuple[str | int, ...]
) -> list[dict[str, Any]]:
    updated_loc_errors: list[Any] = [
        {**err, "loc": loc_prefix + err.get("loc", ())} for err in errors
    ]

    return updated_loc_errors
