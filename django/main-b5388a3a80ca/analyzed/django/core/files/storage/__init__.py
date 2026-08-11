from django.conf import DEFAULT_STORAGE_ALIAS
from django.utils.functional import LazyObject

from .base import Storage
from .filesystem import FileSystemStorage
from .handler import InvalidStorageError, StorageHandler
from .memory import InMemoryStorage

__all__ = (
    "FileSystemStorage",
    "InMemoryStorage",
    "Storage",
    "DefaultStorage",
    "default_storage",
    "InvalidStorageError",
    "StorageHandler",
    "storages",
)


# 默认存储懒加载代理：指向 STORAGES[default]
class DefaultStorage(LazyObject):
    def _setup(self):
        self._wrapped = storages[DEFAULT_STORAGE_ALIAS]


# 全局存储处理器：按别名懒加载后端
storages = StorageHandler()
default_storage = DefaultStorage()
