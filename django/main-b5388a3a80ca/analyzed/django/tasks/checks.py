"""
django.tasks.checks — 系统检查：遍历 TASKS 后端执行 check()。

注册 check_tasks 供 manage.py check 发现后端配置问题。
"""

from django.core import checksfrom django.core import checks


@checks.register
def check_tasks(app_configs=None, **kwargs):
    """Checks all registered Task backends."""

    from . import task_backends

    for backend in task_backends.all():
        yield from backend.check()
