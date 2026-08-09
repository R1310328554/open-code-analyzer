from __future__ import annotations

import typing as t
from contextvars import ContextVar

from werkzeug.local import LocalProxy

if t.TYPE_CHECKING:  # pragma: no cover
    from .app import Flask
    from .ctx import _AppCtxGlobals
    from .ctx import AppContext
    from .ctx import RequestContext
    from .sessions import SessionMixin
    from .wrappers import Request


# 在应用上下文外访问代理时抛出的错误信息（面向开发者，保持英文）。
_no_app_msg = """\
Working outside of application context.

This typically means that you attempted to use functionality that needed
the current application. To solve this, set up an application context
with app.app_context(). See the documentation for more information.\
"""
# 当前应用上下文的 ContextVar，由 AppContext.push/pop 维护。
_cv_app: ContextVar[AppContext] = ContextVar("flask.app_ctx")
app_ctx: AppContext = LocalProxy(  # type: ignore[assignment]
    _cv_app, unbound_message=_no_app_msg
)
current_app: Flask = LocalProxy(  # type: ignore[assignment]
    _cv_app, "app", unbound_message=_no_app_msg
)
g: _AppCtxGlobals = LocalProxy(  # type: ignore[assignment]
    _cv_app, "g", unbound_message=_no_app_msg
)

# 在请求上下文外访问代理时抛出的错误信息。
_no_req_msg = """\
Working outside of request context.

This typically means that you attempted to use functionality that needed
an active HTTP request. Consult the documentation on testing for
information about how to avoid this problem.\
"""
# 当前请求上下文的 ContextVar，由 RequestContext.push/pop 维护。
_cv_request: ContextVar[RequestContext] = ContextVar("flask.request_ctx")
request_ctx: RequestContext = LocalProxy(  # type: ignore[assignment]
    _cv_request, unbound_message=_no_req_msg
)
request: Request = LocalProxy(  # type: ignore[assignment]
    _cv_request, "request", unbound_message=_no_req_msg
)
session: SessionMixin = LocalProxy(  # type: ignore[assignment]
    _cv_request, "session", unbound_message=_no_req_msg
)
