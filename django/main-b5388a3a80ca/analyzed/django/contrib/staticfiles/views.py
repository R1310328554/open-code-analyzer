"""
Views and functions for serving static files. These are only to be used during
development, and SHOULD NOT be used in a production setting.
"""

# 开发环境静态文件视图 — 通过 finder 定位并用 django.views.static 提供development, and SHOULD NOT be used in a production setting.

"""

import os
import posixpath

from django.conf import settings
from django.contrib.staticfiles import finders
from django.http import Http404
from django.views import static


# 查找静态文件绝对路径并以 static.serve 响应；非 DEBUG 且非 insecure 时 404
def serve(request, path, insecure=False, **kwargs):
    """
    Serve static files below a given point in the directory structure or
    from locations inferred from the staticfiles finders.

    To use, put a URL pattern such as::

        from django.contrib.staticfiles import views

        path('<path:path>', views.serve)

    in your URLconf.

    It uses the django.views.static.serve() view to serve the found files.
    """
    if not settings.DEBUG and not insecure:
        raise Http404
    normalized_path = posixpath.normpath(path).lstrip("/")
    absolute_path = finders.find(normalized_path)
    if not absolute_path:
        if path.endswith("/") or path == "":
            raise Http404("Directory indexes are not allowed here.")
        raise Http404("'%s' could not be found" % path)
    document_root, path = os.path.split(absolute_path)
    return static.serve(request, path, document_root=document_root, **kwargs)
