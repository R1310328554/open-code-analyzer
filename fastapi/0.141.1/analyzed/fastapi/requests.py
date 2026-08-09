"""从 Starlette 重新导出 HTTP 连接与请求类型。"""

from starlette.requests import HTTPConnection as HTTPConnection  # noqa: F401
from starlette.requests import Request as Request  # noqa: F401
