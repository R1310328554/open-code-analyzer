from django.conf import settings
from django.utils.module_loading import import_string


# 按 MESSAGE_STORAGE 设置延迟实例化消息存储后端
def default_storage(request):
    """
    Callable with the same interface as the storage classes.

    This isn't just default_storage = import_string(settings.MESSAGE_STORAGE)
    to avoid accessing the settings at the module level.
    """
    return import_string(settings.MESSAGE_STORAGE)(request)
