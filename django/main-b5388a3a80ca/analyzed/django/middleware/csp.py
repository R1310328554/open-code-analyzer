"""
django.middleware.csp — Content-Security-Policy 响应头中间件。

为请求注入 nonce，并按 SECURE_CSP 配置生成 enforce/report-only 策略。
"""

from django.conf import settingsfrom django.conf import settings
from django.middleware import MiddlewareMixin
from django.utils.csp import CSP, LazyNonce, build_policy


def get_nonce(request):
    return getattr(request, "_csp_nonce", None)


# CSP 中间件：LazyNonce + build_policy 写入响应头
class ContentSecurityPolicyMiddleware(MiddlewareMixin):
    def process_request(self, request):
        request._csp_nonce = LazyNonce()

    def process_response(self, request, response):
        nonce = get_nonce(request)

        sentinel = object()
        if (csp_config := getattr(response, "_csp_config", sentinel)) is sentinel:
            csp_config = settings.SECURE_CSP
        if (csp_ro_config := getattr(response, "_csp_ro_config", sentinel)) is sentinel:
            csp_ro_config = settings.SECURE_CSP_REPORT_ONLY

        for header, config in [
            (CSP.HEADER_ENFORCE, csp_config),
            (CSP.HEADER_REPORT_ONLY, csp_ro_config),
        ]:
            # If headers are already set on the response, don't overwrite them.
            # This allows for views to set their own CSP headers as needed.
            # An empty config means CSP headers are not added to the response.
            if config and header not in response:
                response.headers[str(header)] = build_policy(config, nonce)

        return response
