"Dummy cache backend"

from django.core.cache.backends.base import DEFAULT_TIMEOUT, BaseCache


# 空操作缓存后端：开发测试用，不持久化任何数据
class DummyCache(BaseCache):
    def __init__(self, host, *args, **kwargs):
        super().__init__(*args, **kwargs)

    # 校验键格式后始终返回 True
    def add(self, key, value, timeout=DEFAULT_TIMEOUT, version=None):
        self.make_and_validate_key(key, version=version)
        return True

    # 校验键格式后始终返回 default
    def get(self, key, default=None, version=None):
        self.make_and_validate_key(key, version=version)
        return default

    def set(self, key, value, timeout=DEFAULT_TIMEOUT, version=None):
        self.make_and_validate_key(key, version=version)

    def touch(self, key, timeout=DEFAULT_TIMEOUT, version=None):
        self.make_and_validate_key(key, version=version)
        return False

    def delete(self, key, version=None):
        self.make_and_validate_key(key, version=version)
        return False

    def has_key(self, key, version=None):
        self.make_and_validate_key(key, version=version)
        return False

    def clear(self):
        pass
