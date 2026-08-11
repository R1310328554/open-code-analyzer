from django.db.models import JSONField as BuiltinJSONField

__all__ = ["JSONField"]


# 已移除：PostgreSQL JSONB 字段别名，请改用 django.db.models.JSONField
class JSONField(BuiltinJSONField):
    system_check_removed_details = {
        "msg": (
            "django.contrib.postgres.fields.JSONField is removed except for "
            "support in historical migrations."
        ),
        "hint": "Use django.db.models.JSONField instead.",
        "id": "fields.E904",
    }
