from .api import get_messages

# Make unittest ignore frames in this module when reporting failures.
__unittest = True


# 测试混入 — 断言响应中的 flash 消息
class MessagesTestMixin:
    # 比较响应请求上的消息与期望值（可选顺序或集合相等）
    def assertMessages(self, response, expected_messages, *, ordered=True):
        request_messages = list(get_messages(response.wsgi_request))
        assertion = self.assertEqual if ordered else self.assertCountEqual
        assertion(request_messages, expected_messages)
