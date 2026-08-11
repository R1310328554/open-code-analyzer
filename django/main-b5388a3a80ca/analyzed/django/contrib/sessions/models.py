from django.contrib.sessions.base_session import AbstractBaseSession, BaseSessionManager


# Session 模型管理器 — 标记可在迁移中使用
class SessionManager(BaseSessionManager):
    use_in_migrations = True


# 数据库会话模型 — 服务端存储匿名访客会话数据
class Session(AbstractBaseSession):
    """
    Django provides full support for anonymous sessions. The session
    framework lets you store and retrieve arbitrary data on a
    per-site-visitor basis. It stores data on the server side and
    abstracts the sending and receiving of cookies. Cookies contain a
    session ID -- not the data itself.

    The Django sessions framework is entirely cookie-based. It does
    not fall back to putting session IDs in URLs. This is an intentional
    design decision. Not only does that behavior make URLs ugly, it makes
    your site vulnerable to session-ID theft via the "Referer" header.

    For complete documentation on using Sessions in your code, consult
    the sessions documentation that is shipped with Django (also available
    on the Django web site).
    """

    objects = SessionManager()

    # 返回 db 后端的 SessionStore
    @classmethod
    def get_session_store_class(cls):
        from django.contrib.sessions.backends.db import SessionStore

        return SessionStore

    class Meta(AbstractBaseSession.Meta):
        db_table = "django_session"
