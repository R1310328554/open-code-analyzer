import django
from django.core.handlers.wsgi import WSGIHandler


# Django WSGI 公共入口：setup 后返回 WSGIHandler 实例
def get_wsgi_application():
    """
    The public interface to Django's WSGI support. Return a WSGI callable.

    Avoids making django.core.handlers.WSGIHandler a public API, in case the
    internal WSGI implementation changes or moves in the future.
    """
    django.setup(set_prefix=False)
    return WSGIHandler()
