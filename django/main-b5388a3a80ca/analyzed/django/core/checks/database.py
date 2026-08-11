from django.db import connections

from . import Tags, register


# 对指定数据库别名调用各连接 validation.check
@register(Tags.database)
def check_database_backends(databases=None, **kwargs):
    if databases is None:
        return []
    issues = []
    for alias in databases:
        conn = connections[alias]
        issues.extend(conn.validation.check(**kwargs))
    return issues
