import functools
import os

"""
django.utils.warnings — Django 安装路径前缀，用于警告栈过滤。
"""

import djangoimport django


@functools.cache
# 缓存 Django 包目录路径前缀
def django_file_prefixes():
    file = getattr(django, "__file__", None)
    if file is None:
        return ()
    return (os.path.join(os.path.dirname(file), ""),)
