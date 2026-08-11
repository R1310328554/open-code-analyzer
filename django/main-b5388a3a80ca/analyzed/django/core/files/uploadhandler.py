"""
Base file upload handler classes, and the built-in concrete subclasses
"""

import os
from io import BytesIO, UnsupportedOperation

from django.conf import settings
from django.core.files.uploadedfile import InMemoryUploadedFile, TemporaryUploadedFile
from django.utils.module_loading import import_string

__all__ = [
    "UploadFileException",
    "StopUpload",
    "SkipFile",
    "FileUploadHandler",
    "TemporaryFileUploadHandler",
    "MemoryFileUploadHandler",
    "load_handler",
    "StopFutureHandlers",
]


# 文件上传相关异常的基类
class UploadFileException(Exception):
    """
    Any error having to do with uploading files.
    """

    pass


# 中止当前上传；connection_reset 控制是否立即断开连接
class StopUpload(UploadFileException):
    """
    This exception is raised when an upload must abort.
    """

    # 记录是否向客户端发送连接重置
    def __init__(self, connection_reset=False):
        """
        If ``connection_reset`` is ``True``, Django knows will halt the upload
        without consuming the rest of the upload. This will cause the browser
        to show a "connection reset" error.
        """
        self.connection_reset = connection_reset

    # 返回描述中止方式的字符串
    def __str__(self):
        if self.connection_reset:
            return "StopUpload: Halt current upload."
        else:
            return "StopUpload: Consume request data, then halt."


# 处理器跳过当前文件时抛出
class SkipFile(UploadFileException):
    """
    This exception is raised by an upload handler that wants to skip a given
    file.
    """

    pass


# 已处理完毕，阻止后续 upload handler 继续处理
class StopFutureHandlers(UploadFileException):
    """
    Upload handlers that have handled a file and do not want future handlers to
    run should raise this exception instead of returning None.
    """

    pass


# 流式上传处理器基类 — 定义 raw/chunk/complete 生命周期钩子
class FileUploadHandler:
    """
    Base class for streaming upload handlers.
    """

    chunk_size = 64 * 2**10  # : The default chunk size is 64 KB.

    # 初始化文件名、类型、长度等元数据
    def __init__(self, request=None):
        self.file_name = None
        self.content_type = None
        self.content_length = None
        self.charset = None
        self.content_type_extra = None
        self.request = request

    # 处理客户端原始输入流（multipart 解析前）
    # 根据 Content-Length 或流大小决定是否激活
    def handle_raw_input(
        self, input_data, META, content_length, boundary, encoding=None
    ):
        """
        Handle the raw input from the client.

        Parameters:

            :input_data:
                An object that supports reading via .read().
            :META:
                ``request.META``.
            :content_length:
                The (integer) value of the Content-Length header from the
                client.
            :boundary: The boundary from the Content-Type header. Be sure to
                prepend two '--'.
        """
        pass

    # 新文件开始上传时更新 handler 状态
    def new_file(
        self,
        field_name,
        file_name,
        content_type,
        content_length,
        charset=None,
        content_type_extra=None,
    ):
        """
        Signal that a new file has been started.

        Warning: As with any data from the client, you should not trust
        content_length (and sometimes won't even get it).
        """
        self.field_name = field_name
        self.file_name = file_name
        self.content_type = content_type
        self.content_length = content_length
        self.charset = charset
        self.content_type_extra = content_type_extra

    # 接收分块数据；子类必须实现
    # 将分块写入临时文件
    # 激活时写入内存，否则透传 raw_data
    def receive_data_chunk(self, raw_data, start):
        """
        Receive data from the streamed upload parser. ``start`` is the position
        in the file of the chunk.
        """
        raise NotImplementedError(
            "subclasses of FileUploadHandler must provide a receive_data_chunk() method"
        )

    # 文件接收完毕，返回 UploadedFile 对象
    # 设置 size 并返回临时 UploadedFile
    # 激活时封装为 InMemoryUploadedFile
    def file_complete(self, file_size):
        """
        Signal that a file has completed. File size corresponds to the actual
        size accumulated by all the chunks.

        Subclasses should return a valid ``UploadedFile`` object.
        """
        raise NotImplementedError(
            "subclasses of FileUploadHandler must provide a file_complete() method"
        )

    # 整个上传流程结束时的清理钩子
    def upload_complete(self):
        """
        Signal that the upload is complete. Subclasses should perform cleanup
        that is necessary for this handler.
        """
        pass

    # 上传被中断时的清理钩子
    # 中断时关闭并删除临时文件
    def upload_interrupted(self):
        """
        Signal that the upload was interrupted. Subclasses should perform
        cleanup that is necessary for this handler.
        """
        pass


# 将上传数据流式写入临时文件的处理器
class TemporaryFileUploadHandler(FileUploadHandler):
    """
    Upload handler that streams data into a temporary file.
    """

    # 创建 TemporaryUploadedFile 作为写入目标
    # 激活时创建 BytesIO 并阻止后续 handler
    def new_file(self, *args, **kwargs):
        """
        Create the file object to append to as data is coming in.
        """
        super().new_file(*args, **kwargs)
        self.file = TemporaryUploadedFile(
            self.file_name, self.content_type, 0, self.charset, self.content_type_extra
        )

    def receive_data_chunk(self, raw_data, start):
        self.file.write(raw_data)

    def file_complete(self, file_size):
        self.file.seek(0)
        self.file.size = file_size
        return self.file

    def upload_interrupted(self):
        if hasattr(self, "file"):
            temp_location = self.file.temporary_file_path()
            try:
                self.file.close()
                os.remove(temp_location)
            except FileNotFoundError:
                pass


# 小文件内存上传处理器 — 受 FILE_UPLOAD_MAX_MEMORY_SIZE 限制
class MemoryFileUploadHandler(FileUploadHandler):
    """
    File upload handler to stream uploads into memory (used for small files).
    """

    def handle_raw_input(
        self, input_data, META, content_length, boundary, encoding=None
    ):
        """
        Use the content_length to signal whether or not this handler should be
        used.
        """
        # If the post is too large, we cannot use the Memory handler.
        # Content-Length can be absent or understated (for example
        # `Transfer-Encoding: chunked` on ASGI), so for seekable streams (such
        # as SpooledTemporaryFile on ASGI), check the actual size.

        stream = getattr(input_data, "_stream", input_data)
        try:
            content_length = stream.seek(0, os.SEEK_END)
        except (UnsupportedOperation, AttributeError):
            # Cannot seek; fall back to the Content-Length parameter.
            # On WSGI the stream enforces this value so it is trustworthy.
            pass
        else:
            stream.seek(0)
        self.activated = (
            content_length is not None
            and content_length <= settings.FILE_UPLOAD_MAX_MEMORY_SIZE
        )

    def new_file(self, *args, **kwargs):
        super().new_file(*args, **kwargs)
        if self.activated:
            self.file = BytesIO()
            raise StopFutureHandlers()

    def receive_data_chunk(self, raw_data, start):
        """Add the data to the BytesIO file."""
        if self.activated:
            self.file.write(raw_data)
        else:
            return raw_data

    def file_complete(self, file_size):
        """Return a file object if this handler is activated."""
        if not self.activated:
            return

        self.file.seek(0)
        return InMemoryUploadedFile(
            file=self.file,
            field_name=self.field_name,
            name=self.file_name,
            content_type=self.content_type,
            size=file_size,
            charset=self.charset,
            content_type_extra=self.content_type_extra,
        )


# 按 dotted path 导入并实例化 upload handler
def load_handler(path, *args, **kwargs):
    """
    Given a path to a handler, return an instance of that handler.

    E.g.::
        >>> from django.http import HttpRequest
        >>> request = HttpRequest()
        >>> load_handler(
        ...     'django.core.files.uploadhandler.TemporaryFileUploadHandler',
        ...     request,
        ... )
        <TemporaryFileUploadHandler object at 0x...>
    """
    return import_string(path)(*args, **kwargs)
