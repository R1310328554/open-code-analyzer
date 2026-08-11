"""
# JSON Lines 序列化/反序列化
Serialize data to/from JSON Lines"""
Serialize data to/from JSON Lines
"""

import json

from django.core.serializers.base import DeserializationError
from django.core.serializers.json import DjangoJSONEncoder
from django.core.serializers.python import Deserializer as PythonDeserializer
from django.core.serializers.python import Serializer as PythonSerializer


# 将 queryset 序列化为每行一个 JSON 对象
class Serializer(PythonSerializer):
    """Convert a queryset to JSON Lines."""

    internal_use_only = False

    def _init_options(self):
        self._current = None
        self.json_kwargs = self.options.copy()
        self.json_kwargs.pop("stream", None)
        self.json_kwargs.pop("fields", None)
        self.json_kwargs.pop("indent", None)
        self.json_kwargs["separators"] = (",", ": ")
        self.json_kwargs.setdefault("cls", DjangoJSONEncoder)
        self.json_kwargs.setdefault("ensure_ascii", False)

    def start_serialization(self):
        self._init_options()

    # 每对象一行 JSON 并换行
    def end_object(self, obj):
        # self._current has the field data
        json.dump(self.get_dump_object(obj), self.stream, **self.json_kwargs)
        self.stream.write("\n")
        self._current = None

    def getvalue(self):
        # Grandparent super
        return super(PythonSerializer, self).getvalue()


# 逐行解析 JSON Lines 数据
class Deserializer(PythonDeserializer):
    """Deserialize a stream or string of JSON data."""

    def __init__(self, stream_or_string, **options):
        if isinstance(stream_or_string, bytes):
            stream_or_string = stream_or_string.decode()
        if isinstance(stream_or_string, str):
            stream_or_string = stream_or_string.splitlines()
        super().__init__(self._get_lines(stream_or_string), **options)

    def _handle_object(self, obj):
        try:
            yield from super()._handle_object(obj)
        except (GeneratorExit, DeserializationError):
            raise
        except Exception as exc:
            raise DeserializationError(f"Error deserializing object: {exc}") from exc

    # 跳过空行并逐行 json.loads
    @staticmethod
    def _get_lines(stream):
        for line in stream:
            if not line.strip():
                continue
            try:
                yield json.loads(line)
            except Exception as exc:
                raise DeserializationError() from exc
