from django.db.models import DateTimeField, Func, UUIDField


# PostgreSQL GEN_RANDOM_UUID() — 生成随机 UUID
class RandomUUID(Func):
    template = "GEN_RANDOM_UUID()"
    output_field = UUIDField()


# 当前事务时间戳 CURRENT_TIMESTAMP
class TransactionNow(Func):
    template = "CURRENT_TIMESTAMP"
    output_field = DateTimeField()
