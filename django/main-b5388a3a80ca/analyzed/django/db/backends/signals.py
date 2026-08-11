# 数据库后端连接生命周期信号
from django.dispatch import Signal

# 新数据库连接建立后发送，携带 connection 参数
connection_created = Signal()
