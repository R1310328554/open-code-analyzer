"""
django.contrib.auth.signals — 认证生命周期信号。

登录成功、失败与登出时由 auth 后端与视图发送，供审计或会话扩展使用。
"""
from django.dispatch import Signal

# 用户成功登录后发送
user_logged_in = Signal()
# 凭据验证失败时发送
user_login_failed = Signal()
# 用户登出后发送
user_logged_out = Signal()
