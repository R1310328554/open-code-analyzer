"""
django.db.models.constants — ORM 通用常量。

查找路径分隔符与 upsert 冲突策略枚举。
"""
"""
Constants used across the ORM in general.
"""

from enum import Enum

# Separator used to split filter strings apart.
LOOKUP_SEP = "__"


# INSERT 冲突时 IGNORE 或 UPDATE（upsert）
class OnConflict(Enum):
    IGNORE = "ignore"
    UPDATE = "update"
