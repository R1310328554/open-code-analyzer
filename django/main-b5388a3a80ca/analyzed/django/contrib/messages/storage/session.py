import json

from django.contrib.messages.storage.base import BaseStorage
from django.contrib.messages.storage.cookie import MessageDecoder, MessageEncoder
from django.core.exceptions import ImproperlyConfigured


# 基于 Django Session 的消息存储
class SessionStorage(BaseStorage):
    """
    Store messages in the session (that is, django.contrib.sessions).
    """

    session_key = "_messages"

    def __init__(self, request, *args, **kwargs):
        if not hasattr(request, "session"):
            raise ImproperlyConfigured(
                "The session-based temporary message storage requires session "
                "middleware to be installed, and come before the message "
                "middleware in the MIDDLEWARE list."
            )
        super().__init__(request, *args, **kwargs)

    # 从 session['_messages'] 反序列化消息列表
    def _get(self, *args, **kwargs):
        """
        Retrieve a list of messages from the request's session. This storage
        always stores everything it is given, so return True for the
        all_retrieved flag.
        """
        return (
            self.deserialize_messages(self.request.session.get(self.session_key)),
            True,
        )

    # 将消息序列化写入 session 或清除键
    def _store(self, messages, response, *args, **kwargs):
        """
        Store a list of messages to the request's session.
        """
        if messages:
            self.request.session[self.session_key] = self.serialize_messages(messages)
        else:
            self.request.session.pop(self.session_key, None)
        return []

    # 使用 MessageEncoder 将消息列表编码为 JSON 字符串
    def serialize_messages(self, messages):
        encoder = MessageEncoder()
        return encoder.encode(messages)

    # 从 JSON 字符串或原始数据还原消息列表
    def deserialize_messages(self, data):
        if data and isinstance(data, str):
            return json.loads(data, cls=MessageDecoder)
        return data
