"""
Dummy email backend that does nothing.
"""

from django.core.mail.backends.base import BaseEmailBackend


# 空操作邮件后端 — 丢弃邮件仅返回数量
class EmailBackend(BaseEmailBackend):
    # 不实际发送，返回消息条数
    def send_messages(self, email_messages):
        return len(list(email_messages))
