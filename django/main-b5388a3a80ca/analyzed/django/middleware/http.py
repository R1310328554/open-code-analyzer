"""
django.middleware.http — 条件 GET（If-None-Match / If-Modified-Since）。

为可缓存 GET 响应补 ETag 并可能返回 304 Not Modified。
"""

from django.middleware import MiddlewareMixinfrom django.middleware import MiddlewareMixin
from django.utils.cache import get_conditional_response, set_response_etag
from django.utils.http import parse_http_date_safe, split_directive_names


# 条件 GET 中间件：needs_etag 与 get_conditional_response
class ConditionalGetMiddleware(MiddlewareMixin):
    """
    Handle conditional GET operations. If the response has an ETag or
    Last-Modified header and the request has If-None-Match or
    If-Modified-Since, replace the response with HttpNotModified. Add an ETag
    header if needed.
    """

    def process_response(self, request, response):
        # It's too late to prevent an unsafe request with a 412 response, and
        # for a HEAD request, the response body is always empty so computing
        # an accurate ETag isn't possible.
        if request.method != "GET":
            return response

        if self.needs_etag(response) and not response.has_header("ETag"):
            set_response_etag(response)

        etag = response.get("ETag")
        last_modified = response.get("Last-Modified")
        last_modified = last_modified and parse_http_date_safe(last_modified)

        if etag or last_modified:
            return get_conditional_response(
                request,
                etag=etag,
                last_modified=last_modified,
                response=response,
            )

        return response

    def needs_etag(self, response):
        """Return True if an ETag header should be added to response."""
        directives = split_directive_names(response.get("Cache-Control", ""))
        return "no-store" not in directives
