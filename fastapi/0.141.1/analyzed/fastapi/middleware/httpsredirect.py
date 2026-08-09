"""从 Starlette 重新导出 HTTPS 重定向中间件。"""

from starlette.middleware.httpsredirect import (  # noqa
    HTTPSRedirectMiddleware as HTTPSRedirectMiddleware,
)
