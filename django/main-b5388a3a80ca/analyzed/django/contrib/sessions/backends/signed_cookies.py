from django.contrib.sessions.backends.base import SessionBase
from django.core import signing


# 签名 Cookie 会话存储 — 将会话数据直接编码进客户端 Cookie 而非外部后端
class SessionStore(SessionBase):
    # 从 session_key 解码签名载荷；失败时 create 并返回空字典
    def load(self):
        """
        Load the data from the key itself instead of fetching from some
        external data store. Opposite of _get_session_key(), raise BadSignature
        if signature fails.
        """
        try:
            return signing.loads(
                self.session_key,
                serializer=self.serializer,
                # This doesn't handle non-default expiry dates, see #19201
                max_age=self.get_session_cookie_age(),
                salt="django.contrib.sessions.backends.signed_cookies",
            )
        except Exception:
            # BadSignature, ValueError, or unpickling exceptions. If any of
            # these happen, reset the session.
            self.create()
        return {}

    # load 的异步包装
    async def aload(self):
        return self.load()

    # 新建会话：置 modified 以便响应中写入 Cookie
    def create(self):
        """
        To create a new key, set the modified flag so that the cookie is set
        on the client for the current request.
        """
        self.modified = True

    # create 的异步包装
    async def acreate(self):
        return self.create()

    # 将会话序列化为签名串并置 modified 以更新客户端 Cookie
    def save(self, must_create=False):
        """
        To save, get the session key as a securely signed string and then set
        the modified flag so that the cookie is set on the client for the
        current request.
        """
        self._session_key = self._get_session_key()
        self.modified = True

    # save 的异步包装
    async def asave(self, must_create=False):
        return self.save(must_create=must_create)

    # Cookie 后端无共享存储，始终返回 False
    def exists(self, session_key=None):
        """
        This method makes sense when you're talking to a shared resource, but
        it doesn't matter when you're storing the information in the client's
        cookie.
        """
        return False

    # exists 的异步包装
    async def aexists(self, session_key=None):
        return self.exists(session_key=session_key)

    # 清空 session_key 与缓存并置 modified 以删除 Cookie
    def delete(self, session_key=None):
        """
        To delete, clear the session key and the underlying data structure
        and set the modified flag so that the cookie is set on the client for
        the current request.
        """
        self._session_key = ""
        self._session_cache = {}
        self.modified = True

    # delete 的异步包装
    async def adelete(self, session_key=None):
        return self.delete(session_key=session_key)

    # 保留数据但换新 key：调用 save 后在响应写入新 Cookie
    def cycle_key(self):
        """
        Keep the same data but with a new key. Call save() and it will
        automatically save a cookie with a new key at the end of the request.
        """
        self.save()

    # cycle_key 的异步包装
    async def acycle_key(self):
        return self.cycle_key()

    # 用 signing.dumps 将会话字典编码为 URL 安全 base64 串
    def _get_session_key(self):
        """
        Instead of generating a random string, generate a secure url-safe
        base64-encoded string of data as our session key.
        """
        return signing.dumps(
            self._session,
            compress=True,
            salt="django.contrib.sessions.backends.signed_cookies",
            serializer=self.serializer,
        )

    # Cookie 后端无过期条目可清理，空操作
    @classmethod
    def clear_expired(cls):
        pass

    # clear_expired 的异步包装
    @classmethod
    async def aclear_expired(cls):
        pass
