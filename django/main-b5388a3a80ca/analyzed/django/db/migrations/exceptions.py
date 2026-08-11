"""
django.db.migrations.exceptions — 迁移系统专用异常。
"""
from django.db import DatabaseError


# 前缀匹配到多个迁移文件
class AmbiguityError(Exception):
    """More than one migration matches a name prefix."""

    pass


# 迁移文件不可读或格式错误
class BadMigrationError(Exception):
    """There's a bad migration (unreadable/bad format/etc.)."""

    pass


# 迁移依赖图存在无法消解的环
class CircularDependencyError(Exception):
    """There's an impossible-to-resolve circular dependency."""

    pass


# 已应用迁移的依赖尚未应用
class InconsistentMigrationHistory(Exception):
    """An applied migration has some of its dependencies not applied."""

    pass


class InvalidBasesError(ValueError):
    """A model's base classes can't be resolved."""

    pass


# 尝试回滚不可逆操作（如 RunPython 无 reverse）
class IrreversibleError(RuntimeError):
    """An irreversible migration is about to be reversed."""

    pass


# 依赖图中找不到指定迁移节点
class NodeNotFoundError(LookupError):
    """An attempt on a node is made that is not available in the graph."""

    def __init__(self, message, node, origin=None):
        self.message = message
        self.origin = origin
        self.node = node

    def __str__(self):
        return self.message

    def __repr__(self):
        return "NodeNotFoundError(%r)" % (self.node,)


# django_migrations 表不存在
class MigrationSchemaMissing(DatabaseError):
    pass


# 迁移计划同时含正向与反向步骤
class InvalidMigrationPlan(ValueError):
    pass
