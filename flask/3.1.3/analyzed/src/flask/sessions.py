from __future__ import annotations

import collections.abc as c
import hashlib
import typing as t
from collections.abc import MutableMapping
from datetime import datetime
from datetime import timezone

from itsdangerous import BadSignature
from itsdangerous import URLSafeTimedSerializer
from werkzeug.datastructures import CallbackDict

from .json.tag import TaggedJSONSerializer

if t.TYPE_CHECKING:  # pragma: no cover
    import typing_extensions as te

    from .app import Flask
    from .wrappers import Request
    from .wrappers import Response


class SessionMixin(MutableMapping[str, t.Any]):
    """在基本字典上扩展会话属性。"""

    @property
    def permanent(self) -> bool:
        """反映字典中的 ``'_permanent'`` 键。"""
        return self.get("_permanent", False)  # type: ignore[no-any-return]

    @permanent.setter
    def permanent(self, value: bool) -> None:
        self["_permanent"] = bool(value)

    #: 部分实现可检测会话是否新建，但不保证。谨慎使用。
    #: mixin 默认硬编码为 ``False``。
    new = False

    #: 部分实现可检测会话变更并设置此标志。
    #: mixin 默认硬编码为 ``True``。
    modified = True

    accessed = False
    """指示会话是否被访问（即使未修改）。通过请求上下文访问会话对象时
    会设置此标志，包括全局 :data:`.session` 代理。若为 ``True`` 将添加
    ``Vary: cookie`` 响应头。

    .. versionchanged:: 3.1.3
        由请求上下文跟踪。
    """


class SecureCookieSession(CallbackDict[str, t.Any], SessionMixin):
    """基于签名 cookie 的会话基类。

    此后端会设置 :attr:`modified` 和 :attr:`accessed` 属性。
    无法可靠区分新建会话与空会话，因此 :attr:`new` 保持硬编码 ``False``。
    """

    #: 数据变更时设为 ``True``。仅跟踪会话字典本身；
    #: 若会话含可变数据（如嵌套 dict），修改后须手动设为 ``True``。
    #: 仅当此值为 ``True`` 时才会将 cookie 写入响应。
    modified = False

    def __init__(
        self,
        initial: c.Mapping[str, t.Any] | None = None,
    ) -> None:
        def on_update(self: te.Self) -> None:
            self.modified = True

        super().__init__(initial, on_update)


class NullSession(SecureCookieSession):
    """会话不可用时生成更友好错误信息的占位类。
    仍允许只读访问空会话，但写入操作会失败。
    """

    def _fail(self, *args: t.Any, **kwargs: t.Any) -> t.NoReturn:
        raise RuntimeError(
            "The session is unavailable because no secret "
            "key was set.  Set the secret_key on the "
            "application to something unique and secret."
        )

    __setitem__ = __delitem__ = clear = pop = popitem = update = setdefault = _fail
    del _fail


class SessionInterface:
    """替换默认会话接口（使用 werkzeug securecookie 实现）须实现的基本接口。
    只需实现 :meth:`open_session` 和 :meth:`save_session`，其他方法有可用默认值。

    :meth:`open_session` 返回的会话对象须提供类字典接口及
    :class:`SessionMixin` 的属性和方法。建议子类化 dict 并添加该 mixin::

        class Session(dict, SessionMixin):
            pass

    若 :meth:`open_session` 返回 ``None``，Flask 会调用
    :meth:`make_null_session` 创建占位会话，用于配置不满足要求而无法
    使用会话支持的情况。默认 :class:`NullSession` 会提示未设置 secret key。

    在应用上替换会话接口只需赋值 :attr:`flask.Flask.session_interface`::

        app = Flask(__name__)
        app.session_interface = MySessionInterface()

    同一 session 的多个请求可能并发发送和处理。实现新会话接口时，
    须考虑对后端存储的读写是否需要同步。不保证各请求会话的打开或
    保存顺序，仅按请求开始和结束处理的顺序进行。

    .. versionadded:: 0.8
    """

    #: :meth:`make_null_session` 查找此类以创建空会话。
    #: :meth:`is_null_session` 对此类型做类型检查。
    null_session_class = NullSession

    #: 指示会话接口是否基于 pickle 的标志。
    #: Flask 扩展可据此决定如何处理会话对象。
    #:
    #: .. versionadded:: 0.10
    pickle_based = False

    def make_null_session(self, app: Flask) -> NullSession:
        """创建空会话，作为因配置错误无法加载真实会话支持时的占位对象。
        空会话的主要作用是支持查找而不报错，修改操作则返回有用的错误信息。

        默认创建 :attr:`null_session_class` 的实例。
        """
        return self.null_session_class()

    def is_null_session(self, obj: object) -> bool:
        """检查给定对象是否为空会话。空会话不会被要求保存。

        默认检查对象是否为 :attr:`null_session_class` 的实例。
        """
        return isinstance(obj, self.null_session_class)

    def get_cookie_name(self, app: Flask) -> str:
        """会话 cookie 名称。使用 ``app.config["SESSION_COOKIE_NAME"]``。"""
        return app.config["SESSION_COOKIE_NAME"]  # type: ignore[no-any-return]

    def get_cookie_domain(self, app: Flask) -> str | None:
        """会话 cookie 的 ``Domain`` 参数值。未设置时浏览器仅向设置 cookie 的
        精确域名发送，否则也向给定值的任意子域发送。

        使用 :data:`SESSION_COOKIE_DOMAIN` 配置。

        .. versionchanged:: 2.3
            默认不设置，不再回退到 ``SERVER_NAME``。
        """
        return app.config["SESSION_COOKIE_DOMAIN"]  # type: ignore[no-any-return]

    def get_cookie_path(self, app: Flask) -> str:
        """返回 cookie 的有效路径。默认使用 ``SESSION_COOKIE_PATH`` 配置值，
        未设置则回退到 ``APPLICATION_ROOT``，若为 ``None`` 则使用 ``/``。
        """
        return app.config["SESSION_COOKIE_PATH"] or app.config["APPLICATION_ROOT"]  # type: ignore[no-any-return]

    def get_cookie_httponly(self, app: Flask) -> bool:
        """返回会话 cookie 是否应为 httponly。当前仅返回
        ``SESSION_COOKIE_HTTPONLY`` 配置值。
        """
        return app.config["SESSION_COOKIE_HTTPONLY"]  # type: ignore[no-any-return]

    def get_cookie_secure(self, app: Flask) -> bool:
        """返回 cookie 是否应为 secure。当前仅返回
        ``SESSION_COOKIE_SECURE`` 设置值。
        """
        return app.config["SESSION_COOKIE_SECURE"]  # type: ignore[no-any-return]

    def get_cookie_samesite(self, app: Flask) -> str | None:
        """若 cookie 应使用 ``SameSite`` 属性，返回 ``'Strict'`` 或 ``'Lax'``。
        当前仅返回 :data:`SESSION_COOKIE_SAMESITE` 设置值。
        """
        return app.config["SESSION_COOKIE_SAMESITE"]  # type: ignore[no-any-return]

    def get_cookie_partitioned(self, app: Flask) -> bool:
        """返回 cookie 是否应分区。默认使用 :data:`SESSION_COOKIE_PARTITIONED` 的值。

        .. versionadded:: 3.1
        """
        return app.config["SESSION_COOKIE_PARTITIONED"]  # type: ignore[no-any-return]

    def get_expiration_time(self, app: Flask, session: SessionMixin) -> datetime | None:
        """辅助方法，返回会话过期时间，或 ``None`` 表示与会话同寿命。
        默认实现返回当前时间加上应用配置的永久会话生命周期。
        """
        if session.permanent:
            return datetime.now(timezone.utc) + app.permanent_session_lifetime
        return None

    def should_set_cookie(self, app: Flask, session: SessionMixin) -> bool:
        """供会话后端判断此响应是否应设置 ``Set-Cookie`` 头。
        会话已修改时设置 cookie。若为永久会话且 ``SESSION_REFRESH_EACH_REQUEST``
        配置为 true，则始终设置。

        会话已删除时通常跳过此检查。

        .. versionadded:: 0.11
        """

        return session.modified or (
            session.permanent and app.config["SESSION_REFRESH_EACH_REQUEST"]
        )

    def open_session(self, app: Flask, request: Request) -> SessionMixin | None:
        """每个请求开始时、推送请求上下文后、URL 匹配前调用。

        须返回实现类字典接口及 :class:`SessionMixin` 接口的对象。

        加载失败且非立即错误时返回 ``None``，请求上下文将回退到
        :meth:`make_null_session`。
        """
        raise NotImplementedError()

    def save_session(
        self, app: Flask, session: SessionMixin, response: Response
    ) -> None:
        """每个请求结束时、生成响应后、移除请求上下文前调用。
        若 :meth:`is_null_session` 返回 ``True`` 则跳过。
        """
        raise NotImplementedError()


session_json_serializer = TaggedJSONSerializer()


def _lazy_sha1(string: bytes = b"") -> t.Any:
    """延迟访问 ``hashlib.sha1`` 直至运行时。FIPS 构建可能不包含 SHA-1，
    过早导入并作为默认值会在开发者配置替代方案前失败。
    """
    return hashlib.sha1(string)


class SecureCookieSessionInterface(SessionInterface):
    """默认会话接口，通过 :mod:`itsdangerous` 模块在签名 cookie 中存储会话。
    """

    #: 在 secret key 之上用于签名基于 cookie 的会话的盐值。
    salt = "cookie-session"
    #: 签名使用的哈希函数，默认为 sha1。
    digest_method = staticmethod(_lazy_sha1)
    #: itsdangerous 支持的密钥派生名称，默认为 hmac。
    key_derivation = "hmac"
    #: 载荷的 Python 序列化器。默认为紧凑 JSON 派生序列化器，
    #: 支持 datetime、tuple 等额外 Python 类型。
    serializer = session_json_serializer
    session_class = SecureCookieSession

    def get_signing_serializer(self, app: Flask) -> URLSafeTimedSerializer | None:
        """构建用于签名/验证会话 cookie 的序列化器。"""
        if not app.secret_key:
            return None

        keys: list[str | bytes] = []

        if fallbacks := app.config["SECRET_KEY_FALLBACKS"]:
            keys.extend(fallbacks)

        keys.append(app.secret_key)  # itsdangerous 要求当前密钥在首位
        return URLSafeTimedSerializer(
            keys,  # type: ignore[arg-type]
            salt=self.salt,
            serializer=self.serializer,
            signer_kwargs={
                "key_derivation": self.key_derivation,
                "digest_method": self.digest_method,
            },
        )

    def open_session(self, app: Flask, request: Request) -> SecureCookieSession | None:
        s = self.get_signing_serializer(app)
        if s is None:
            return None
        val = request.cookies.get(self.get_cookie_name(app))
        if not val:
            return self.session_class()
        max_age = int(app.permanent_session_lifetime.total_seconds())
        try:
            data = s.loads(val, max_age=max_age)
            return self.session_class(data)
        except BadSignature:
            return self.session_class()

    def save_session(
        self, app: Flask, session: SessionMixin, response: Response
    ) -> None:
        name = self.get_cookie_name(app)
        domain = self.get_cookie_domain(app)
        path = self.get_cookie_path(app)
        secure = self.get_cookie_secure(app)
        partitioned = self.get_cookie_partitioned(app)
        samesite = self.get_cookie_samesite(app)
        httponly = self.get_cookie_httponly(app)

        # 会话被访问过则添加 "Vary: Cookie" 头。
        if session.accessed:
            response.vary.add("Cookie")

        # 会话被修改为空则删除 cookie；会话为空则不设置 cookie。
        if not session:
            if session.modified:
                response.delete_cookie(
                    name,
                    domain=domain,
                    path=path,
                    secure=secure,
                    partitioned=partitioned,
                    samesite=samesite,
                    httponly=httponly,
                )
                response.vary.add("Cookie")

            return

        if not self.should_set_cookie(app, session):
            return

        expires = self.get_expiration_time(app, session)
        val = self.get_signing_serializer(app).dumps(dict(session))  # type: ignore[union-attr]
        response.set_cookie(
            name,
            val,
            expires=expires,
            httponly=httponly,
            domain=domain,
            path=path,
            secure=secure,
            partitioned=partitioned,
            samesite=samesite,
        )
        response.vary.add("Cookie")
