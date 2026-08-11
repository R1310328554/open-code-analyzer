"""
django.urls.converters — path() 路径参数类型转换器。

内置 int/str/slug/uuid/path；register_converter 扩展自定义转换器。
"""

import functoolsimport functools
import uuid


# 整型路径参数：regex [0-9]+
class IntConverter:
    regex = "[0-9]+"

    def to_python(self, value):
        return int(value)

    def to_url(self, value):
        return str(value)


# 字符串路径参数：不含 '/'
class StringConverter:
    regex = "[^/]+"

    def to_python(self, value):
        return value

    def to_url(self, value):
        return value


# UUID 路径参数
class UUIDConverter:
    regex = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"

    def to_python(self, value):
        return uuid.UUID(value)

    def to_url(self, value):
        return str(value)


# slug 参数：字母数字下划线连字符
class SlugConverter(StringConverter):
    regex = "[-a-zA-Z0-9_]+"


# 贪婪路径段：含 '/'
class PathConverter(StringConverter):
    regex = ".+"


# 内置转换器名 -> 实例
DEFAULT_CONVERTERS = {
    "int": IntConverter(),
    "path": PathConverter(),
    "slug": SlugConverter(),
    "str": StringConverter(),
    "uuid": UUIDConverter(),
}


# 用户 register_converter 注册的扩展
REGISTERED_CONVERTERS = {}


# 注册自定义转换器并清缓存
def register_converter(converter, type_name):
    if type_name in REGISTERED_CONVERTERS or type_name in DEFAULT_CONVERTERS:
        raise ValueError(f"Converter {type_name!r} is already registered.")
    REGISTERED_CONVERTERS[type_name] = converter()
    get_converters.cache_clear()

    from django.urls.resolvers import _route_to_regex

    _route_to_regex.cache_clear()


# 合并默认与已注册转换器
@functools.cache
def get_converters():
    return {**DEFAULT_CONVERTERS, **REGISTERED_CONVERTERS}
