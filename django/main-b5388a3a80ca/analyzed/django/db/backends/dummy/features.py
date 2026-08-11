from django.db.backends.base.features import BaseDatabaseFeatures


# 占位后端特性：不支持事务与保存点
class DummyDatabaseFeatures(BaseDatabaseFeatures):
    supports_transactions = False
    uses_savepoints = False
