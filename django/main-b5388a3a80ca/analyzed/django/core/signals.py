"""
django.core.signals — Django 核心框架信号。

请求生命周期与配置变更时发送，供缓存、数据库连接等子系统响应。
"""
from django.dispatch import Signal

# HTTP 请求开始处理时发送
request_started = Signal()
# HTTP 请求处理完毕时发送
request_finished = Signal()
# 请求处理中发生未捕获异常时发送
got_request_exception = Signal()
# 运行时 settings 被修改时发送
setting_changed = Signal()
