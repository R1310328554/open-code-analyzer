"""
# 占位数据库后端：除 close 外所有 API 均抛出 ImproperlyConfigured
Dummy database backend for Django."""
Dummy database backend for Django.

Django uses this if the database ENGINE setting is empty (None or empty
string).

Each of these API functions, except connection.close(), raise
ImproperlyConfigured.
"""

from django.core.exceptions import ImproperlyConfigured
from django.db.backends.base.base import BaseDatabaseWrapper
from django.db.backends.base.client import BaseDatabaseClient
from django.db.backends.base.creation import BaseDatabaseCreation
from django.db.backends.base.introspection import BaseDatabaseIntrospection
from django.db.backends.base.operations import BaseDatabaseOperations
from django.db.backends.dummy.features import DummyDatabaseFeatures


# 抛出 DATABASES.ENGINE 未配置错误
def complain(*args, **kwargs):
    raise ImproperlyConfigured(
        "settings.DATABASES is improperly configured. "
        "Please supply the ENGINE value. Check "
        "settings documentation for more details."
    )


# 空操作，用于 rollback/close 等
def ignore(*args, **kwargs):
    pass


# 占位 Operations：quote_name 等调用 complain
class DatabaseOperations(BaseDatabaseOperations):
    quote_name = complain


# 占位 Client：runshell 调用 complain
class DatabaseClient(BaseDatabaseClient):
    runshell = complain


# 占位 Creation：测试库操作多为 ignore
class DatabaseCreation(BaseDatabaseCreation):
    create_test_db = ignore
    destroy_test_db = ignore
    serialize_db_to_string = ignore


# 占位 Introspection：内省方法调用 complain
class DatabaseIntrospection(BaseDatabaseIntrospection):
    get_table_list = complain
    get_table_description = complain
    get_relations = complain
    get_indexes = complain


# 占位连接包装器：实际数据库操作均不可用
class DatabaseWrapper(BaseDatabaseWrapper):
    operators = {}
    # Override the base class implementations with null
    # implementations. Anything that tries to actually
    # do something raises complain; anything that tries
    # to rollback or undo something raises ignore.
    _cursor = complain
    ensure_connection = complain
    _commit = complain
    _rollback = ignore
    _close = ignore
    _savepoint = ignore
    _savepoint_commit = complain
    _savepoint_rollback = ignore
    _set_autocommit = complain
    # Classes instantiated in __init__().
    client_class = DatabaseClient
    creation_class = DatabaseCreation
    features_class = DummyDatabaseFeatures
    introspection_class = DatabaseIntrospection
    ops_class = DatabaseOperations

    # 占位后端始终视为可用（直到真正访问）
    def is_usable(self):
        return True
