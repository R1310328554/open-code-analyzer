from contextlib import AsyncExitStack

from starlette.types import ASGIApp, Receive, Scope, Send


# 主要用于在请求结束后关闭文件；依赖项在各自的 AsyncExitStack 中关闭
class AsyncExitStackMiddleware:
    """在 ASGI scope 中注入 AsyncExitStack，供依赖项与资源清理使用。"""

    def __init__(
        self, app: ASGIApp, context_name: str = "fastapi_middleware_astack"
    ) -> None:
        self.app = app
        self.context_name = context_name

    async def __call__(self, scope: Scope, receive: Receive, send: Send) -> None:
        async with AsyncExitStack() as stack:
            scope[self.context_name] = stack
            await self.app(scope, receive, send)
