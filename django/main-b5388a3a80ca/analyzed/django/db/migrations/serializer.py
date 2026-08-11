import builtins
import collections.abc
import datetime
import decimal
import enum
import functools
import math
import os
import pathlib
import re
import types
import uuid
import zoneinfo

from django.conf import SettingsReference
from django.db import models
from django.db.migrations.operations.base import Operation
from django.db.migrations.utils import COMPILED_REGEX_TYPE, RegexObject
from django.db.models.deletion import DatabaseOnDelete
from django.utils.functional import LazyObject, Promise
"""
django.db.migrations.serializer — 迁移文件值序列化。

将 Field、Operation、常量等 Python 对象转为可写入 migrations 模块的源码字符串。
"""
from django.utils.version import get_docs_version

FUNCTION_TYPES = (types.FunctionType, types.BuiltinFunctionType, types.MethodType)

if isinstance(functools._lru_cache_wrapper, type):
    # When using CPython's _functools C module, LRU cache function decorators
    # present as a class and not a function, so add that class to the list of
    # function types. In the pure Python implementation and PyPy they present
    # as normal functions which are already handled.
    FUNCTION_TYPES += (functools._lru_cache_wrapper,)


# 序列化器抽象基类
class BaseSerializer:
    def __init__(self, value):
        self.value = value

    def serialize(self):
        raise NotImplementedError(
            "Subclasses of BaseSerializer must implement the serialize() method."
        )


# 序列/集合类基类：逐项序列化后按 _format 拼接
class BaseSequenceSerializer(BaseSerializer):
    def _format(self):
        raise NotImplementedError(
            "Subclasses of BaseSequenceSerializer must implement the _format() method."
        )

    def serialize(self):
        imports = set()
        strings = []
        for item in self.value:
            item_string, item_imports = serializer_factory(item).serialize()
            imports.update(item_imports)
            strings.append(item_string)
        value = self._format()
        return value % (", ".join(strings)), imports


# 无序集合：先按 repr 排序再序列化以保证确定性
class BaseUnorderedSequenceSerializer(BaseSequenceSerializer):
    def __init__(self, value):
        super().__init__(sorted(value, key=repr))


# 直接用 repr 序列化的简单类型
class BaseSimpleSerializer(BaseSerializer):
    def serialize(self):
        return repr(self.value), set()


# 序列化 models.Choices 枚举成员
class ChoicesSerializer(BaseSerializer):
    def serialize(self):
        return serializer_factory(self.value.value).serialize()


# 序列化 DatabaseOnDelete 外键删除策略
class DatabaseOnDeleteSerializer(BaseSerializer):
    def serialize(self):
        path = self.value.__class__.__module__
        return f"{path}.{self.value.__name__}", {f"import {path}"}


# 序列化 datetime.date/time/timedelta
class DateTimeSerializer(BaseSerializer):
    """For datetime.*, except datetime.datetime."""

    def serialize(self):
        return repr(self.value), {"import datetime"}


# 序列化 datetime.datetime；非 UTC 时区先转为 UTC
class DatetimeDatetimeSerializer(BaseSerializer):
    """For datetime.datetime."""

    def serialize(self):
        if self.value.tzinfo is not None and self.value.tzinfo != datetime.UTC:
            self.value = self.value.astimezone(datetime.UTC)
        imports = ["import datetime"]
        return repr(self.value), set(imports)


# 序列化 decimal.Decimal
class DecimalSerializer(BaseSerializer):
    def serialize(self):
        return repr(self.value), {"from decimal import Decimal"}


# 序列化支持 deconstruct() 的可解构对象
class DeconstructibleSerializer(BaseSerializer):
    @staticmethod
    def serialize_deconstructed(path, args, kwargs):
        name, imports = DeconstructibleSerializer._serialize_path(path)
        strings = []
        for arg in args:
            arg_string, arg_imports = serializer_factory(arg).serialize()
            strings.append(arg_string)
            imports.update(arg_imports)
        non_ident_kwargs = {}
        for kw, arg in sorted(kwargs.items()):
            if kw.isidentifier():
                arg_string, arg_imports = serializer_factory(arg).serialize()
                imports.update(arg_imports)
                strings.append("%s=%s" % (kw, arg_string))
            else:
                non_ident_kwargs[kw] = arg
        if non_ident_kwargs:
            # Serialize non-identifier keyword arguments as a dict.
            kw_string, kw_imports = serializer_factory(non_ident_kwargs).serialize()
            strings.append(f"**{kw_string}")
            imports.update(kw_imports)

        return "%s(%s)" % (name, ", ".join(strings)), imports

    @staticmethod
    def _serialize_path(path):
        module, name = path.rsplit(".", 1)
        if module == "django.db.models":
            imports = {"from django.db import models"}
            name = "models.%s" % name
        else:
            imports = {"import %s" % module}
            name = path
        return name, imports

    def serialize(self):
        return self.serialize_deconstructed(*self.value.deconstruct())


# 序列化 dict，键值排序保证稳定输出
class DictionarySerializer(BaseSerializer):
    def serialize(self):
        imports = set()
        strings = []
        for k, v in sorted(self.value.items()):
            k_string, k_imports = serializer_factory(k).serialize()
            v_string, v_imports = serializer_factory(v).serialize()
            imports.update(k_imports)
            imports.update(v_imports)
            strings.append((k_string, v_string))
        return "{%s}" % (", ".join("%s: %s" % (k, v) for k, v in strings)), imports


# 序列化 enum.Enum/Flag 成员
class EnumSerializer(BaseSerializer):
    def serialize(self):
        enum_class = self.value.__class__
        module = enum_class.__module__
        if issubclass(enum_class, enum.Flag):
            members = list(self.value)
        else:
            members = (self.value,)
        return (
            " | ".join(
                [
                    f"{module}.{enum_class.__qualname__}[{item.name!r}]"
                    for item in members
                ]
            ),
            {"import %s" % module},
        )


# 序列化 float；NaN/Inf 用 float("...") 形式
class FloatSerializer(BaseSimpleSerializer):
    def serialize(self):
        if math.isnan(self.value) or math.isinf(self.value):
            return 'float("{}")'.format(self.value), set()
        return super().serialize()


# 序列化 frozenset
class FrozensetSerializer(BaseUnorderedSequenceSerializer):
    def _format(self):
        return "frozenset([%s])"


# 序列化函数/方法引用（禁止 lambda 与无模块函数）
class FunctionTypeSerializer(BaseSerializer):
    def serialize(self):
        if getattr(self.value, "__self__", None) and isinstance(
            self.value.__self__, type
        ):
            klass = self.value.__self__
            module = klass.__module__
            return "%s.%s.%s" % (module, klass.__qualname__, self.value.__name__), {
                "import %s" % module
            }
        # Further error checking
        if self.value.__name__ == "<lambda>":
            raise ValueError("Cannot serialize function: lambda")
        if self.value.__module__ is None:
            raise ValueError("Cannot serialize function %r: No module" % self.value)

        module_name = self.value.__module__

        if "<" not in self.value.__qualname__:  # Qualname can include <locals>
            return "%s.%s" % (module_name, self.value.__qualname__), {
                "import %s" % self.value.__module__
            }

        raise ValueError(
            "Could not find function %s in %s.\n" % (self.value.__name__, module_name)
        )


# 序列化 functools.partial/partialmethod
class FunctoolsPartialSerializer(BaseSerializer):
    def serialize(self):
        partial_name = self.value.__class__.__name__
        return DeconstructibleSerializer.serialize_deconstructed(
            f"functools.{partial_name}",
            (self.value.func, *self.value.args),
            self.value.keywords,
        )


# 序列化 types.GenericAlias（如 list[int]）
class GenericAliasSerializer(BaseSerializer):
    def serialize(self):
        imports = set()
        # Avoid iterating self.value, because it returns itself.
        # https://github.com/python/cpython/issues/103450
        for item in self.value.__args__:
            _, item_imports = serializer_factory(item).serialize()
            imports.update(item_imports)
        return repr(self.value), imports


# 序列化一般可迭代对象为元组字面量
class IterableSerializer(BaseSerializer):
    def serialize(self):
        imports = set()
        strings = []
        for item in self.value:
            item_string, item_imports = serializer_factory(item).serialize()
            imports.update(item_imports)
            strings.append(item_string)
        # When len(strings)==0, the empty iterable should be serialized as
        # "()", not "(,)" because (,) is invalid Python syntax.
        value = "(%s)" if len(strings) != 1 else "(%s,)"
        return value % (", ".join(strings)), imports


# 序列化 models.Field 实例
class ModelFieldSerializer(DeconstructibleSerializer):
    def serialize(self):
        attr_name, path, args, kwargs = self.value.deconstruct()
        return self.serialize_deconstructed(path, args, kwargs)


# 序列化 Manager 或 QuerySet.as_manager()
class ModelManagerSerializer(DeconstructibleSerializer):
    def serialize(self):
        as_manager, manager_path, qs_path, args, kwargs = self.value.deconstruct()
        if as_manager:
            name, imports = self._serialize_path(qs_path)
            return "%s.as_manager()" % name, imports
        else:
            return self.serialize_deconstructed(manager_path, args, kwargs)


# 序列化迁移 Operation 为 OperationWriter 输出
class OperationSerializer(BaseSerializer):
    def serialize(self):
        from django.db.migrations.writer import OperationWriter

        string, imports = OperationWriter(self.value, indentation=0).serialize()
        # Nested operation, trailing comma is handled in upper
        # OperationWriter._write()
        return string.rstrip(","), imports


# 序列化 os.PathLike 为 fspath 字符串
class PathLikeSerializer(BaseSerializer):
    def serialize(self):
        return repr(os.fspath(self.value)), {}


# 序列化 pathlib 路径；Concrete 转为 Pure 保证跨平台
class PathSerializer(BaseSerializer):
    def serialize(self):
        # Convert concrete paths to pure paths to avoid issues with migrations
        # generated on one platform being used on a different platform.
        prefix = "Pure" if isinstance(self.value, pathlib.Path) else ""
        return "pathlib.%s%r" % (prefix, self.value), {"import pathlib"}


# 序列化 compiled regex 为 re.compile(...)
class RegexSerializer(BaseSerializer):
    def serialize(self):
        regex_pattern, pattern_imports = serializer_factory(
            self.value.pattern
        ).serialize()
        # Turn off default implicit flags (e.g. re.U) because regexes with the
        # same implicit and explicit flags aren't equal.
        flags = self.value.flags ^ re.compile("").flags
        regex_flags, flag_imports = serializer_factory(flags).serialize()
        imports = {"import re", *pattern_imports, *flag_imports}
        args = [regex_pattern]
        if flags:
            args.append(regex_flags)
        return "re.compile(%s)" % ", ".join(args), imports


# 序列化 list 为 [...]
class SequenceSerializer(BaseSequenceSerializer):
    def _format(self):
        return "[%s]"


# 序列化 set；空集用 set() 避免与 {} 混淆
class SetSerializer(BaseUnorderedSequenceSerializer):
    def _format(self):
        # Serialize as a set literal except when value is empty because {}
        # is an empty dict.
        return "{%s}" if self.value else "set(%s)"


# 序列化 SettingsReference 为 settings.SETTING
class SettingsReferenceSerializer(BaseSerializer):
    def serialize(self):
        return "settings.%s" % self.value.setting_name, {
            "from django.conf import settings"
        }


# 序列化 tuple；单元素元组保留尾逗号
class TupleSerializer(BaseSequenceSerializer):
    def _format(self):
        # When len(value)==0, the empty tuple should be serialized as "()",
        # not "(,)" because (,) is invalid Python syntax.
        return "(%s)" if len(self.value) != 1 else "(%s,)"


# 序列化 type 对象（含 models.Model 等特殊路径）
class TypeSerializer(BaseSerializer):
    def serialize(self):
        special_cases = [
            (models.Model, "models.Model", ["from django.db import models"]),
            (types.NoneType, "types.NoneType", ["import types"]),
        ]
        for case, string, imports in special_cases:
            if case is self.value:
                return string, set(imports)
        if hasattr(self.value, "__module__"):
            module = self.value.__module__
            if module == builtins.__name__:
                return self.value.__name__, set()
            else:
                return "%s.%s" % (module, self.value.__qualname__), {
                    "import %s" % module
                }


# 序列化 uuid.UUID
class UUIDSerializer(BaseSerializer):
    def serialize(self):
        return "uuid.%s" % repr(self.value), {"import uuid"}


# 序列化 zoneinfo.ZoneInfo
class ZoneInfoSerializer(BaseSerializer):
    def serialize(self):
        return repr(self.value), {"import zoneinfo"}


# 类型到序列化器类的注册表；register/unregister 扩展
class Serializer:
    _registry = {
        # Some of these are order-dependent.
        frozenset: FrozensetSerializer,
        list: SequenceSerializer,
        set: SetSerializer,
        tuple: TupleSerializer,
        dict: DictionarySerializer,
        models.Choices: ChoicesSerializer,
        enum.Enum: EnumSerializer,
        datetime.datetime: DatetimeDatetimeSerializer,
        (datetime.date, datetime.timedelta, datetime.time): DateTimeSerializer,
        SettingsReference: SettingsReferenceSerializer,
        float: FloatSerializer,
        (bool, int, types.NoneType, bytes, str, range): BaseSimpleSerializer,
        decimal.Decimal: DecimalSerializer,
        (functools.partial, functools.partialmethod): FunctoolsPartialSerializer,
        FUNCTION_TYPES: FunctionTypeSerializer,
        types.GenericAlias: GenericAliasSerializer,
        collections.abc.Iterable: IterableSerializer,
        (COMPILED_REGEX_TYPE, RegexObject): RegexSerializer,
        uuid.UUID: UUIDSerializer,
        pathlib.PurePath: PathSerializer,
        os.PathLike: PathLikeSerializer,
        zoneinfo.ZoneInfo: ZoneInfoSerializer,
        DatabaseOnDelete: DatabaseOnDeleteSerializer,
    }

    @classmethod
    def register(cls, type_, serializer):
        if not issubclass(serializer, BaseSerializer):
            raise ValueError(
                "'%s' must inherit from 'BaseSerializer'." % serializer.__name__
            )
        cls._registry[type_] = serializer

    @classmethod
    def unregister(cls, type_):
        cls._registry.pop(type_)


# 按值类型分派到对应 Serializer 实例
def serializer_factory(value):
    if isinstance(value, Promise):
        value = str(value)
    elif isinstance(value, LazyObject):
        # The unwrapped value is returned as the first item of the arguments
        # tuple.
        value = value.__reduce__()[1][0]

    if isinstance(value, models.Field):
        return ModelFieldSerializer(value)
    if isinstance(value, models.manager.BaseManager):
        return ModelManagerSerializer(value)
    if isinstance(value, Operation):
        return OperationSerializer(value)
    if isinstance(value, type):
        return TypeSerializer(value)
    # Anything that knows how to deconstruct itself.
    if hasattr(value, "deconstruct"):
        return DeconstructibleSerializer(value)
    for type_, serializer_cls in Serializer._registry.items():
        if isinstance(value, type_):
            return serializer_cls(value)
    raise ValueError(
        "Cannot serialize: %r\nThere are some values Django cannot serialize into "
        "migration files.\nFor more, see https://docs.djangoproject.com/en/%s/"
        "topics/migrations/#migration-serializing" % (value, get_docs_version())
    )
