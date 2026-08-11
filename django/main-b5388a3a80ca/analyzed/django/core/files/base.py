import os
from io import BytesIO, StringIO, UnsupportedOperation

from django.core.files.utils import FileProxyMixin
from django.utils.functional import cached_property


# 文件包装基类：封装底层 file-like 对象并提供 chunks 迭代
class File(FileProxyMixin):
    DEFAULT_CHUNK_SIZE = 64 * 2**10

    # 绑定底层文件对象并读取 name/mode 属性
    def __init__(self, file, name=None):
        self.file = file
        if name is None:
            name = getattr(file, "name", None)
        self.name = name
        if hasattr(file, "mode"):
            self.mode = file.mode

    def __str__(self):
        return self.name or ""

    def __repr__(self):
        return "<%s: %s>" % (self.__class__.__name__, self.name or "None")

    def __bool__(self):
        return True

    def __len__(self):
        return self.size

    @cached_property
    def size(self):
        if hasattr(self.file, "size"):
            return self.file.size
        if hasattr(self.file, "name"):
            try:
                return os.path.getsize(self.file.name)
            except (OSError, TypeError):
                pass
        if hasattr(self.file, "tell") and hasattr(self.file, "seek"):
            pos = self.file.tell()
            self.file.seek(0, os.SEEK_END)
            size = self.file.tell()
            self.file.seek(pos)
            return size
        raise AttributeError("Unable to determine the file's size.")

    # 按块读取文件内容，默认块大小 DEFAULT_CHUNK_SIZE
    def chunks(self, chunk_size=None):
        """
        Read the file and yield chunks of ``chunk_size`` bytes (defaults to
        ``File.DEFAULT_CHUNK_SIZE``).
        """
        chunk_size = chunk_size or self.DEFAULT_CHUNK_SIZE
        try:
            self.seek(0)
        except (AttributeError, UnsupportedOperation):
            pass

        while True:
            data = self.read(chunk_size)
            if not data:
                break
            yield data

    # 判断是否需要多块读取（内存文件应返回 False）
    def multiple_chunks(self, chunk_size=None):
        """
        Return ``True`` if you can expect multiple chunks.

        NB: If a particular file representation is in memory, subclasses should
        always return ``False`` -- there's no good reason to read from memory
        in chunks.
        """
        return self.size > (chunk_size or self.DEFAULT_CHUNK_SIZE)

    def __iter__(self):
        # Iterate over this file-like object by newlines
        buffer_ = []
        for chunk in self.chunks():
            for line in chunk.splitlines(True):
                if buffer_:
                    if endswith_cr(buffer_[-1]) and not equals_lf(line):
                        # Line split after a \r newline; yield buffer_.
                        yield type(buffer_[0])().join(buffer_)
                        # Continue with line.
                        buffer_ = []
                    else:
                        # Line either split without a newline (line
                        # continues after buffer_) or with \r\n
                        # newline (line == b'\n').
                        buffer_.append(line)

                if not buffer_:
                    # If this is the end of a \n or \r\n line, yield.
                    if endswith_lf(line):
                        yield line
                    else:
                        buffer_.append(line)
                elif endswith_lf(line):
                    yield type(buffer_[0])().join(buffer_)
                    buffer_ = []

        if buffer_:
            yield type(buffer_[0])().join(buffer_)

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_value, tb):
        self.close()

    def open(self, mode=None, *args, **kwargs):
        if not self.closed:
            self.seek(0)
        elif self.name and os.path.exists(self.name):
            self.file = open(self.name, mode or self.mode, *args, **kwargs)
        else:
            raise ValueError("The file cannot be reopened.")
        return self

    def close(self):
        self.file.close()


# 基于原始内容的 File 对象：无需真实磁盘文件
class ContentFile(File):
    """
    A File-like object that takes just raw content, rather than an actual file.
    """

    def __init__(self, content, name=None):
        stream_class = StringIO if isinstance(content, str) else BytesIO
        super().__init__(stream_class(content), name=name)
        self.size = len(content)

    def __str__(self):
        return "Raw content"

    def open(self, mode=None):
        self.seek(0)
        return self

    def close(self):
        pass

    def write(self, data):
        self.__dict__.pop("size", None)  # Clear the computed size.
        return self.file.write(data)


# 判断行是否以 \r 结尾（兼容 str/bytes）
def endswith_cr(line):
    """Return True if line (a text or bytestring) ends with '\r'."""
    return line.endswith("\r" if isinstance(line, str) else b"\r")


# 判断行是否以 \n 结尾（兼容 str/bytes）
def endswith_lf(line):
    """Return True if line (a text or bytestring) ends with '\n'."""
    return line.endswith("\n" if isinstance(line, str) else b"\n")


# 判断行是否等于单独的 \n（兼容 str/bytes）
def equals_lf(line):
    """Return True if line (a text or bytestring) equals '\n'."""
    return line == ("\n" if isinstance(line, str) else b"\n")
