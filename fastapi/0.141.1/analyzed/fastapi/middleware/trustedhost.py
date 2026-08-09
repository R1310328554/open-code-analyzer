"""从 Starlette 重新导出受信任主机中间件。"""

from starlette.middleware.trustedhost import (  # noqa
    TrustedHostMiddleware as TrustedHostMiddleware,
)
