"""从 Starlette 重新导出 WSGI 中间件，用于挂载 WSGI 应用。"""

from starlette.middleware.wsgi import (
    WSGIMiddleware as WSGIMiddleware,
)  # pragma: no cover # noqa
