from django.db.models import CharField, EmailField, TextField

__all__ = ["CICharField", "CIEmailField", "CITextField"]


# 已移除：不区分大小写字符字段，仅保留历史迁移兼容
class CICharField(CharField):
    system_check_removed_details = {
        "msg": (
            "django.contrib.postgres.fields.CICharField is removed except for support "
            "in historical migrations."
        ),
        "hint": (
            'Use CharField(db_collation="…") with a case-insensitive non-deterministic '
            "collation instead."
        ),
        "id": "fields.E905",
    }


# 已移除：不区分大小写邮箱字段
class CIEmailField(EmailField):
    system_check_removed_details = {
        "msg": (
            "django.contrib.postgres.fields.CIEmailField is removed except for support "
            "in historical migrations."
        ),
        "hint": (
            'Use EmailField(db_collation="…") with a case-insensitive '
            "non-deterministic collation instead."
        ),
        "id": "fields.E906",
    }


# 已移除：不区分大小写文本字段，请改用 db_collation 非确定性排序规则
class CITextField(TextField):
    system_check_removed_details = {
        "msg": (
            "django.contrib.postgres.fields.CITextField is removed except for support "
            "in historical migrations."
        ),
        "hint": (
            'Use TextField(db_collation="…") with a case-insensitive non-deterministic '
            "collation instead."
        ),
        "id": "fields.E907",
    }
