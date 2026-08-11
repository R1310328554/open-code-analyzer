"""
django.contrib.admin.exceptions — 管理后台专用异常类型。
"""
from django.core.exceptions import SuspiciousOperation


# URL 查询串含非法 list_filter 查找参数
class DisallowedModelAdminLookup(SuspiciousOperation):
    """Invalid filter was passed to admin view via URL querystring"""

    pass


# URL 查询串含非法 to_field 参数
class DisallowedModelAdminToField(SuspiciousOperation):
    """Invalid to_field was passed to admin view via URL query string"""

    pass


# 模型已在 AdminSite 上注册
class AlreadyRegistered(Exception):
    """The model is already registered."""

    pass


# 模型尚未在 AdminSite 上注册
class NotRegistered(Exception):
    """The model is not registered."""

    pass
