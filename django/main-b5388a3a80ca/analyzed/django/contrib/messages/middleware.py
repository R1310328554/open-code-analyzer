from django.conf import settings
from django.contrib.messages.storage import default_storage
from django.middleware import MiddlewareMixin


# 消息中间件 — 请求时挂载存储，响应时持久化未读消息
class MessageMiddleware(MiddlewareMixin):
    """
    Middleware that handles temporary messages.
    """

    # 在请求上创建默认消息存储
    def process_request(self, request):
        request._messages = default_storage(request)

    # 将未存储的消息写入后端，DEBUG 下未全部存储则抛 ValueError
    def process_response(self, request, response):
        """
        Update the storage backend (i.e., save the messages).

        Raise ValueError if not all messages could be stored and DEBUG is True.
        """
        # A higher middleware layer may return a request which does not contain
        # messages storage, so make no assumption that it will be there.
        if hasattr(request, "_messages"):
            unstored_messages = request._messages.update(response)
            if unstored_messages and settings.DEBUG:
                raise ValueError("Not all temporary messages could be stored.")
        return response
