from django.conf import settings
from django.contrib.sessions.backends.base import CreateError, SessionBase, UpdateError
from django.core.cache import caches

KEY_PREFIX = "django.contrib.sessions.cache"


# 纯缓存会话存储：数据保存在 SESSION_CACHE_ALIAS 指向的缓存后端
class SessionStore(SessionBase):
    """
    A cache-based session store.
    """

    cache_key_prefix = KEY_PREFIX

    # 绑定 caches[SESSION_CACHE_ALIAS] 并委托 SessionBase 初始化
    def __init__(self, session_key=None):
        self._cache = caches[settings.SESSION_CACHE_ALIAS]
        super().__init__(session_key)

    @property
    def cache_key(self):
        return self.cache_key_prefix + self._get_or_create_session_key()

    async def acache_key(self):
        return self.cache_key_prefix + await self._aget_or_create_session_key()

    # 从 cache_key 读取；无效键或缺失时重置 session_key 并返回 {}
    def load(self):
        try:
            session_data = self._cache.get(self.cache_key)
        except Exception:
            # Some backends (e.g. memcache) raise an exception on invalid
            # cache keys. If this happens, reset the session. See #17810.
            session_data = None
        if session_data is not None:
            return session_data
        self._session_key = None
        return {}

    async def aload(self):
        try:
            session_data = await self._cache.aget(await self.acache_key())
        except Exception:
            session_data = None
        if session_data is not None:
            return session_data
        self._session_key = None
        return {}

    # 最多重试 10000 次生成唯一键并 save(must_create=True)
    def create(self):
        # Because a cache can fail silently (e.g. memcache), we don't know if
        # we are failing to create a new session because of a key collision or
        # because the cache is missing. So we try for a (large) number of times
        # and then raise an exception. That's the risk you shoulder if using
        # cache backing.
        for i in range(10000):
            self._session_key = self._get_new_session_key()
            try:
                self.save(must_create=True)
            except CreateError:
                continue
            self.modified = True
            return
        raise RuntimeError(
            "Unable to create a new session key. "
            "It is likely that the cache is unavailable."
        )

    async def acreate(self):
        for i in range(10000):
            self._session_key = await self._aget_new_session_key()
            try:
                await self.asave(must_create=True)
            except CreateError:
                continue
            self.modified = True
            return
        raise RuntimeError(
            "Unable to create a new session key. "
            "It is likely that the cache is unavailable."
        )

    # add/set 写入缓存，TTL 为 get_expiry_age()
    def save(self, must_create=False):
        if self.session_key is None:
            return self.create()
        if must_create:
            func = self._cache.add
        elif self._cache.get(self.cache_key) is not None:
            func = self._cache.set
        else:
            raise UpdateError
        result = func(
            self.cache_key,
            self._get_session(no_load=must_create),
            self.get_expiry_age(),
        )
        if must_create and not result:
            raise CreateError

    async def asave(self, must_create=False):
        if self.session_key is None:
            return await self.acreate()
        if must_create:
            func = self._cache.aadd
        elif await self._cache.aget(await self.acache_key()) is not None:
            func = self._cache.aset
        else:
            raise UpdateError
        result = await func(
            await self.acache_key(),
            await self._aget_session(no_load=must_create),
            await self.aget_expiry_age(),
        )
        if must_create and not result:
            raise CreateError

    # 检查 cache 中是否存在 cache_key_prefix + session_key
    def exists(self, session_key):
        return (
            bool(session_key) and (self.cache_key_prefix + session_key) in self._cache
        )

    async def aexists(self, session_key):
        return bool(session_key) and await self._cache.ahas_key(
            self.cache_key_prefix + session_key
        )

    # 从缓存删除对应键
    def delete(self, session_key=None):
        if session_key is None:
            if self.session_key is None:
                return
            session_key = self.session_key
        self._cache.delete(self.cache_key_prefix + session_key)

    async def adelete(self, session_key=None):
        if session_key is None:
            if self.session_key is None:
                return
            session_key = self.session_key
        await self._cache.adelete(self.cache_key_prefix + session_key)

    @classmethod
    # 缓存后端自带 TTL，此处为 no-op
    def clear_expired(cls):
        pass

    @classmethod
    async def aclear_expired(cls):
        pass
