"""
django.tasks.signals — 异步任务生命周期信号与日志接收器。

task_enqueued/started/finished 供监控与调试；settings 变更时重置后端连接。
"""

import loggingimport logging
import sys

from asgiref.local import Local

from django.core.signals import setting_changed
from django.dispatch import Signal, receiver

from .base import TaskResultStatus

logger = logging.getLogger("django.tasks")

# 任务入队时发送
task_enqueued = Signal()
# 任务完成（成功或失败）时发送
task_finished = Signal()
# 任务开始执行时发送
task_started = Signal()


@receiver(setting_changed)
# settings.TASKS 变更时重建 task_backends 配置与 Local 连接
def clear_tasks_handlers(*, setting, **kwargs):
    """Reset the connection handler whenever the settings change."""
    if setting == "TASKS":
        from . import task_backends

        task_backends._settings = task_backends.settings = (
            task_backends.configure_settings(None)
        )
        task_backends._connections = Local()


@receiver(task_enqueued)
# DEBUG 级别记录入队任务 id、模块路径与后端
def log_task_enqueued(sender, task_result, **kwargs):
    logger.debug(
        "Task id=%s path=%s enqueued backend=%s",
        task_result.id,
        task_result.task.module_path,
        task_result.backend,
    )


@receiver(task_started)
# INFO 级别记录任务开始执行
def log_task_started(sender, task_result, **kwargs):
    logger.info(
        "Task id=%s path=%s state=%s",
        task_result.id,
        task_result.task.module_path,
        task_result.status,
    )


@receiver(task_finished)
# 按状态选择 ERROR/INFO 并附带 exc_info 记录完成
def log_task_finished(sender, task_result, **kwargs):
    # Signal is sent inside exception handlers, so exc_info() is available.
    exc_info = sys.exc_info()
    logger.log(
        (
            logging.ERROR
            if task_result.status == TaskResultStatus.FAILED
            else logging.INFO
        ),
        "Task id=%s path=%s state=%s",
        task_result.id,
        task_result.task.module_path,
        task_result.status,
        exc_info=exc_info if exc_info[0] else None,
    )
