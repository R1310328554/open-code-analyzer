"""
django.tasks.exceptions — 任务框架专用异常层次。

InvalidTask、TaskResultDoesNotExist 等，基类 TaskException。
"""

from django.core.exceptions import ImproperlyConfiguredfrom django.core.exceptions import ImproperlyConfigured


# 任务相关异常基类（勿直接抛出）
class TaskException(Exception):
    """Base class for task-related exceptions. Do not raise directly."""


# Task 定义或参数不符合后端能力时抛出
class InvalidTask(TaskException):
    """The provided Task is invalid."""


# TASKS 配置中的 BACKEND 无法加载或无效
class InvalidTaskBackend(ImproperlyConfigured):
    """The provided Task backend is invalid."""


# 按 id 查询 TaskResult 不存在
class TaskResultDoesNotExist(TaskException):
    """The requested TaskResult does not exist."""


# 结果存在但所属 Task 函数与当前 Task 不匹配
class TaskResultMismatch(TaskException):
    """The requested TaskResult is invalid."""
