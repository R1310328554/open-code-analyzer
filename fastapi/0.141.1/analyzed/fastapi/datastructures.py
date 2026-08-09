from collections.abc import Callable, Mapping
from typing import (
    Annotated,
    Any,
    BinaryIO,
    TypeVar,
    cast,
)

from annotated_doc import Doc
from pydantic import GetJsonSchemaHandler
from starlette.datastructures import URL as URL  # noqa: F401
from starlette.datastructures import Address as Address  # noqa: F401
from starlette.datastructures import FormData as FormData  # noqa: F401
from starlette.datastructures import Headers as Headers  # noqa: F401
from starlette.datastructures import QueryParams as QueryParams  # noqa: F401
from starlette.datastructures import State as State  # noqa: F401
from starlette.datastructures import UploadFile as StarletteUploadFile


class UploadFile(StarletteUploadFile):
    """
    请求中上传的文件。

    将其声明为*路径操作函数*（或依赖项）的参数。

    若使用普通 `def` 函数，可通过 `upload_file.file` 属性访问
    原始的标准 Python 文件对象（阻塞式、非 async），
    这在非 async 代码中很有用且往往是必需的。

    详见 [FastAPI 请求文件文档](https://fastapi.tiangolo.com/tutorial/request-files/)。

    ## 示例

    ```python
    from typing import Annotated

    from fastapi import FastAPI, File, UploadFile

    app = FastAPI()


    @app.post("/files/")
    async def create_file(file: Annotated[bytes, File()]):
        return {"file_size": len(file)}


    @app.post("/uploadfile/")
    async def create_upload_file(file: UploadFile):
        return {"filename": file.filename}
    ```
    """

    file: Annotated[
        BinaryIO,
        Doc("标准 Python 文件对象（非 async）。"),
    ]
    filename: Annotated[str | None, Doc("原始文件名。")]
    size: Annotated[int | None, Doc("文件大小（字节）。")]
    headers: Annotated[Headers, Doc("请求的 headers。")]
    content_type: Annotated[
        str | None, Doc("请求的 content type，来自 headers。")
    ]

    async def write(
        self,
        data: Annotated[
            bytes,
            Doc(
                """
                要写入文件的字节数据。
                """
            ),
        ],
    ) -> None:
        """
        向文件写入若干字节。

        通常不会对你从请求中读取的文件调用此方法。

        为兼容 async 并保持可 await，此操作在线程池中执行。
        """
        return await super().write(data)

    async def read(
        self,
        size: Annotated[
            int,
            Doc(
                """
                要从文件中读取的字节数。
                """
            ),
        ] = -1,
    ) -> bytes:
        """
        从文件读取若干字节。

        为兼容 async 并保持可 await，此操作在线程池中执行。
        """
        return await super().read(size)

    async def seek(
        self,
        offset: Annotated[
            int,
            Doc(
                """
                要在文件中定位到的字节位置。
                """
            ),
        ],
    ) -> None:
        """
        移动到文件中的指定位置。

        之后的任何 read 或 write 都会从该位置开始。

        为兼容 async 并保持可 await，此操作在线程池中执行。
        """
        return await super().seek(offset)

    async def close(self) -> None:
        """
        关闭文件。

        为兼容 async 并保持可 await，此操作在线程池中执行。
        """
        return await super().close()

    @classmethod
    def _validate(cls, __input_value: Any, _: Any) -> "UploadFile":
        if not isinstance(__input_value, StarletteUploadFile):
            raise ValueError(f"Expected UploadFile, received: {type(__input_value)}")
        return cast(UploadFile, __input_value)

    @classmethod
    def __get_pydantic_json_schema__(
        cls, core_schema: Mapping[str, Any], handler: GetJsonSchemaHandler
    ) -> dict[str, Any]:
        return {"type": "string", "contentMediaType": "application/octet-stream"}

    @classmethod
    def __get_pydantic_core_schema__(
        cls, source: type[Any], handler: Callable[[Any], Mapping[str, Any]]
    ) -> Mapping[str, Any]:
        from ._compat.v2 import with_info_plain_validator_function

        return with_info_plain_validator_function(cls._validate)


class DefaultPlaceholder:
    """
    不应直接使用此类。

    内部用于识别默认值是否已被覆盖，
    即使覆盖后的默认值在布尔上下文中仍为真值。
    """

    def __init__(self, value: Any):
        self.value = value

    def __bool__(self) -> bool:
        return bool(self.value)

    def __eq__(self, o: object) -> bool:
        return isinstance(o, DefaultPlaceholder) and o.value == self.value


DefaultType = TypeVar("DefaultType")


def Default(value: DefaultType) -> DefaultType:
    """
    不应直接使用此函数。

    内部用于识别默认值是否已被覆盖，
    即使覆盖后的默认值在布尔上下文中仍为真值。
    """
    return DefaultPlaceholder(value)  # type: ignore


# Param/FieldInfo 中“未提供参数”的哨兵值。
# 类型标注为 None 以满足 ty
_Unset = Default(None)
