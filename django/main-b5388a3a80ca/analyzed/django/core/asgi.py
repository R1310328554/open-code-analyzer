import django
from django.core.handlers.asgi import ASGIHandler


# ASGI 公共入口：初始化 Django 并返回 ASGIHandler 可调用对象
def get_asgi_application():
    """
    The public interface to Django's ASGI support. Return an ASGI 3 callable.

    Avoids making django.core.handlers.ASGIHandler a public API, in case the
    internal implementation changes or moves in the future.
    """
    django.setup(set_prefix=False)
    return ASGIHandler()
