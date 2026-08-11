# 会话序列化 — 复用 signing 的 JSONSerializer
from django.core.signing import JSONSerializer as BaseJSONSerializer

# 会话模块导出的 JSON 序列化器别名
JSONSerializer = BaseJSONSerializer
