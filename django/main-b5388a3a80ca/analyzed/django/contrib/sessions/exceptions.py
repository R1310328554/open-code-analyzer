# 会话相关异常 — 无效 key、篡改嫌疑与中断
from django.core.exceptions import BadRequest, SuspiciousOperation


# session key 含非法字符时抛出
class InvalidSessionKey(SuspiciousOperation):
    """Invalid characters in session key"""

    pass


# 会话数据可能被篡改时抛出
class SuspiciousSession(SuspiciousOperation):
    """The session may be tampered with"""

    pass


# 请求处理过程中会话被删除（如并发登出）时抛出
class SessionInterrupted(BadRequest):
    """The session was interrupted."""

    pass
