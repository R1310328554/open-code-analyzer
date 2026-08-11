"Base Cache class."

import time
import warnings

from asgiref.sync import sync_to_async

from django.core.exceptions import ImproperlyConfigured
from django.utils.module_loading import import_string
from django.utils.regex_helper import _lazy_re_compile


# 配置无效时抛出的缓存后端错误
class InvalidCacheBackendError(ImproperlyConfigured):
    pass


# 缓存键不符合 memcached 规范时的运行时警告
class CacheKeyWarning(RuntimeWarning):
    pass


# 缓存键非法时抛出的值错误
class InvalidCacheKey(ValueError):
    pass


# Stub class to ensure not passing in a `timeout` argument results in
# the default timeout
DEFAULT_TIMEOUT = object()

# Memcached does not accept keys longer than this.
MEMCACHE_MAX_KEY_LENGTH = 250


# 默认键生成函数：拼接 key_prefix、version 与原始键
def default_key_func(key, key_prefix, version):
    """
    Default function to generate keys.

    Construct the key used by all other methods. By default, prepend
    the `key_prefix`. KEY_FUNCTION can be used to specify an alternate
    function with custom key making behavior.
    """
    return "%s:%s:%s" % (key_prefix, version, key)


# 解析 KEY_FUNCTION 设置，返回可调用键生成函数
def get_key_func(key_func):
    """
    Function to decide which key function to use.

    Default to ``default_key_func``.
    """
    if key_func is not None:
        if callable(key_func):
            return key_func
        else:
            return import_string(key_func)
    return default_key_func


# 缓存后端抽象基类：定义 get/set/add 等 API 及异步包装
class BaseCache:
    _missing_key = object()

    # 从配置读取 timeout、max_entries、cull_frequency 与键前缀
    def __init__(self, params):
        timeout = params.get("timeout", params.get("TIMEOUT", 300))
        if timeout is not None:
            try:
                timeout = int(timeout)
            except (ValueError, TypeError):
                timeout = 300
        self.default_timeout = timeout

        options = params.get("OPTIONS", {})
        max_entries = params.get("max_entries", options.get("MAX_ENTRIES", 300))
        try:
            self._max_entries = int(max_entries)
        except (ValueError, TypeError):
            self._max_entries = 300

        cull_frequency = params.get("cull_frequency", options.get("CULL_FREQUENCY", 3))
        try:
            self._cull_frequency = int(cull_frequency)
        except (ValueError, TypeError):
            self._cull_frequency = 3

        self.key_prefix = params.get("KEY_PREFIX", "")
        self.version = params.get("VERSION", 1)
        self.key_func = get_key_func(params.get("KEY_FUNCTION"))

    # 将相对超时秒数转换为后端可用的绝对时间戳
    def get_backend_timeout(self, timeout=DEFAULT_TIMEOUT):
        """
        Return the timeout value usable by this backend based upon the provided
        timeout.
        """
        if timeout == DEFAULT_TIMEOUT:
            timeout = self.default_timeout
        elif timeout == 0:
            # ticket 21147 - avoid time.time() related precision issues
            timeout = -1
        return None if timeout is None else time.time() + timeout

    # 调用 key_func 生成带前缀与版本的完整缓存键
    def make_key(self, key, version=None):
        """
        Construct the key used by all other methods. By default, use the
        key_func to generate a key (which, by default, prepends the
        `key_prefix' and 'version'). A different key function can be provided
        at the time of cache construction; alternatively, you can subclass the
        cache backend to provide custom key making behavior.
        """
        if version is None:
            version = self.version

        return self.key_func(key, self.key_prefix, version)

    # 检查键长度与字符集，对 memcached 不兼容键发出警告
    def validate_key(self, key):
        """
        Warn about keys that would not be portable to the memcached
        backend. This encourages (but does not force) writing backend-portable
        cache code.
        """
        for warning in memcache_key_warnings(key):
            warnings.warn(warning, CacheKeyWarning)

    def make_and_validate_key(self, key, version=None):
        """Helper to make and validate keys."""
        key = self.make_key(key, version=version)
        self.validate_key(key)
        return key

    # 仅当键不存在时写入，子类必须实现
    def add(self, key, value, timeout=DEFAULT_TIMEOUT, version=None):
        """
        Set a value in the cache if the key does not already exist. If
        timeout is given, use that timeout for the key; otherwise use the
        default cache timeout.

        Return True if the value was stored, False otherwise.
        """
        raise NotImplementedError(
            "subclasses of BaseCache must provide an add() method"
        )

    async def aadd(self, key, value, timeout=DEFAULT_TIMEOUT, version=None):
        return await sync_to_async(self.add, thread_sensitive=True)(
            key, value, timeout, version
        )

    # 按键读取缓存值，缺失时返回 default
    def get(self, key, default=None, version=None):
        """
        Fetch a given key from the cache. If the key does not exist, return
        default, which itself defaults to None.
        """
        raise NotImplementedError("subclasses of BaseCache must provide a get() method")

    async def aget(self, key, default=None, version=None):
        return await sync_to_async(self.get, thread_sensitive=True)(
            key, default, version
        )

    # 写入或覆盖缓存项
    def set(self, key, value, timeout=DEFAULT_TIMEOUT, version=None):
        """
        Set a value in the cache. If timeout is given, use that timeout for the
        key; otherwise use the default cache timeout.
        """
        raise NotImplementedError("subclasses of BaseCache must provide a set() method")

    async def aset(self, key, value, timeout=DEFAULT_TIMEOUT, version=None):
        return await sync_to_async(self.set, thread_sensitive=True)(
            key, value, timeout, version
        )

    def touch(self, key, timeout=DEFAULT_TIMEOUT, version=None):
        """
        Update the key's expiry time using timeout. Return True if successful
        or False if the key does not exist.
        """
        raise NotImplementedError(
            "subclasses of BaseCache must provide a touch() method"
        )

    async def atouch(self, key, timeout=DEFAULT_TIMEOUT, version=None):
        return await sync_to_async(self.touch, thread_sensitive=True)(
            key, timeout, version
        )

    # 删除指定键
    def delete(self, key, version=None):
        """
        Delete a key from the cache and return whether it succeeded, failing
        silently.
        """
        raise NotImplementedError(
            "subclasses of BaseCache must provide a delete() method"
        )

    async def adelete(self, key, version=None):
        return await sync_to_async(self.delete, thread_sensitive=True)(key, version)

    # 批量读取多个键，返回存在的键值映射
    def get_many(self, keys, version=None):
        """
        Fetch a bunch of keys from the cache. For certain backends (memcached,
        pgsql) this can be *much* faster when fetching multiple values.

        Return a dict mapping each key in keys to its value. If the given
        key is missing, it will be missing from the response dict.
        """
        d = {}
        for k in keys:
            val = self.get(k, self._missing_key, version=version)
            if val is not self._missing_key:
                d[k] = val
        return d

    async def aget_many(self, keys, version=None):
        """See get_many()."""
        if self.get_many.__func__ is not BaseCache.get_many:
            return await sync_to_async(self.get_many, thread_sensitive=True)(
                keys, version=version
            )
        d = {}
        for k in keys:
            val = await self.aget(k, self._missing_key, version=version)
            if val is not self._missing_key:
                d[k] = val
        return d

    # 读取键，不存在则写入 default（可调用）并返回
    def get_or_set(self, key, default, timeout=DEFAULT_TIMEOUT, version=None):
        """
        Fetch a given key from the cache. If the key does not exist,
        add the key and set it to the default value. The default value can
        also be any callable. If timeout is given, use that timeout for the
        key; otherwise use the default cache timeout.

        Return the value of the key stored or retrieved.
        """
        val = self.get(key, self._missing_key, version=version)
        if val is self._missing_key:
            if callable(default):
                default = default()
            self.add(key, default, timeout=timeout, version=version)
            # Fetch the value again to avoid a race condition if another caller
            # added a value between the first get() and the add() above.
            return self.get(key, default, version=version)
        return val

    async def aget_or_set(self, key, default, timeout=DEFAULT_TIMEOUT, version=None):
        """See get_or_set()."""
        if self.get_or_set.__func__ is not BaseCache.get_or_set:
            return await sync_to_async(self.get_or_set, thread_sensitive=True)(
                key, default, timeout=timeout, version=version
            )
        val = await self.aget(key, self._missing_key, version=version)
        if val is self._missing_key:
            if callable(default):
                default = default()
            await self.aadd(key, default, timeout=timeout, version=version)
            # Fetch the value again to avoid a race condition if another caller
            # added a value between the first aget() and the aadd() above.
            return await self.aget(key, default, version=version)
        return val

    # 判断键是否存在且未过期
    def has_key(self, key, version=None):
        """
        Return True if the key is in the cache and has not expired.
        """
        return (
            self.get(key, self._missing_key, version=version) is not self._missing_key
        )

    async def ahas_key(self, key, version=None):
        if self.has_key.__func__ is not BaseCache.has_key:
            return await sync_to_async(self.has_key, thread_sensitive=True)(
                key, version=version
            )
        return (
            await self.aget(key, self._missing_key, version=version)
            is not self._missing_key
        )

    # 原子递增数值，键不存在时抛出 ValueError
    def incr(self, key, delta=1, version=None):
        """
        Add delta to value in the cache. If the key does not exist, raise a
        ValueError exception.
        """
        value = self.get(key, self._missing_key, version=version)
        if value is self._missing_key:
            raise ValueError("Key '%s' not found" % key)
        new_value = value + delta
        self.set(key, new_value, version=version)
        return new_value

    async def aincr(self, key, delta=1, version=None):
        """See incr()."""
        if self.incr.__func__ is not BaseCache.incr:
            return await sync_to_async(self.incr, thread_sensitive=True)(
                key, delta=delta, version=version
            )
        value = await self.aget(key, self._missing_key, version=version)
        if value is self._missing_key:
            raise ValueError("Key '%s' not found" % key)
        new_value = value + delta
        await self.aset(key, new_value, version=version)
        return new_value

    def decr(self, key, delta=1, version=None):
        """
        Subtract delta from value in the cache. If the key does not exist,
        raise a ValueError exception.
        """
        return self.incr(key, -delta, version=version)

    async def adecr(self, key, delta=1, version=None):
        return await self.aincr(key, -delta, version=version)

    def __contains__(self, key):
        """
        Return True if the key is in the cache and has not expired.
        """
        # This is a separate method, rather than just a copy of has_key(),
        # so that it always has the same functionality as has_key(), even
        # if a subclass overrides it.
        return self.has_key(key)

    def set_many(self, data, timeout=DEFAULT_TIMEOUT, version=None):
        """
        Set a bunch of values in the cache at once from a dict of key/value
        pairs. For certain backends (memcached), this is much more efficient
        than calling set() multiple times.

        If timeout is given, use that timeout for the key; otherwise use the
        default cache timeout.

        On backends that support it, return a list of keys that failed
        insertion, or an empty list if all keys were inserted successfully.
        """
        for key, value in data.items():
            self.set(key, value, timeout=timeout, version=version)
        return []

    async def aset_many(self, data, timeout=DEFAULT_TIMEOUT, version=None):
        if self.set_many.__func__ is not BaseCache.set_many:
            return await sync_to_async(self.set_many, thread_sensitive=True)(
                data, timeout=timeout, version=version
            )
        for key, value in data.items():
            await self.aset(key, value, timeout=timeout, version=version)
        return []

    def delete_many(self, keys, version=None):
        """
        Delete a bunch of values in the cache at once. For certain backends
        (memcached), this is much more efficient than calling delete() multiple
        times.
        """
        for key in keys:
            self.delete(key, version=version)

    async def adelete_many(self, keys, version=None):
        if self.delete_many.__func__ is not BaseCache.delete_many:
            return await sync_to_async(self.delete_many, thread_sensitive=True)(
                keys, version=version
            )
        for key in keys:
            await self.adelete(key, version=version)

    # 清空全部缓存项，子类必须实现
    def clear(self):
        """Remove *all* values from the cache at once."""
        raise NotImplementedError(
            "subclasses of BaseCache must provide a clear() method"
        )

    async def aclear(self):
        return await sync_to_async(self.clear, thread_sensitive=True)()

    def incr_version(self, key, delta=1, version=None):
        """
        Add delta to the cache version for the supplied key. Return the new
        version.
        """
        if version is None:
            version = self.version

        value = self.get(key, self._missing_key, version=version)
        if value is self._missing_key:
            raise ValueError("Key '%s' not found" % key)

        self.set(key, value, version=version + delta)
        self.delete(key, version=version)
        return version + delta

    async def aincr_version(self, key, delta=1, version=None):
        """See incr_version()."""
        if self.incr_version.__func__ is not BaseCache.incr_version:
            return await sync_to_async(self.incr_version, thread_sensitive=True)(
                key, delta=delta, version=version
            )
        if version is None:
            version = self.version

        value = await self.aget(key, self._missing_key, version=version)
        if value is self._missing_key:
            raise ValueError("Key '%s' not found" % key)

        await self.aset(key, value, version=version + delta)
        await self.adelete(key, version=version)
        return version + delta

    def decr_version(self, key, delta=1, version=None):
        """
        Subtract delta from the cache version for the supplied key. Return the
        new version.
        """
        return self.incr_version(key, -delta, version)

    async def adecr_version(self, key, delta=1, version=None):
        return await self.aincr_version(key, -delta, version)

    def close(self, **kwargs):
        """Close the cache connection"""
        pass

    async def aclose(self, **kwargs):
        return await sync_to_async(self.close, thread_sensitive=True)(**kwargs)


memcached_error_chars_re = _lazy_re_compile(r"[\x00-\x20\x7f]")


# 生成 memcached 键长度与非法字符相关的警告消息
def memcache_key_warnings(key):
    if len(key) > MEMCACHE_MAX_KEY_LENGTH:
        yield (
            "Cache key will cause errors if used with memcached: %r "
            "(longer than %s)" % (key, MEMCACHE_MAX_KEY_LENGTH)
        )
    if memcached_error_chars_re.search(key):
        yield (
            "Cache key contains characters that will cause errors if used with "
            f"memcached: {key!r}"
        )
