"""
Email backend that writes messages to console instead of sending them.
"""

import sys
import threading

from django.core.mail.backends.base import BaseEmailBackend


# 控制台邮件后端 — 将邮件内容写入 stdout 或指定 stream
class EmailBackend(BaseEmailBackend):
    # 配置输出流与线程锁
    def __init__(self, fail_silently=False, **kwargs):
        self.stream = kwargs.pop("stream", sys.stdout)
        self._lock = threading.RLock()
        super().__init__(**kwargs)
        self.fail_silently = fail_silently

    # 解码并格式化写入单封邮件
    def write_message(self, message):
        msg = message.message()
        msg_data = msg.as_bytes()
        charset = (
            msg.get_charset().get_output_charset() if msg.get_charset() else "utf-8"
        )
        msg_data = msg_data.decode(charset)
        self.stream.write("%s\n" % msg_data)
        self.stream.write("-" * 79)
        self.stream.write("\n")

    # 线程安全地批量写入所有邮件
    def send_messages(self, email_messages):
        """Write all messages to the stream in a thread-safe way."""
        if not email_messages:
            return
        msg_count = 0
        with self._lock:
            try:
                stream_created = self.open()
                for message in email_messages:
                    self.write_message(message)
                    self.stream.flush()  # flush after each message
                    msg_count += 1
                if stream_created:
                    self.close()
            except Exception:
                if not self.fail_silently:
                    raise
        return msg_count
