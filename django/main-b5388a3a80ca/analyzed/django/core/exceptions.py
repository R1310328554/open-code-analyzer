"""
# Django 全局异常类：模型、请求、配置与验证相关
Global Django exception classes."""
Global Django exception classes.
"""

import operator

from django.utils.hashable import make_hashable


# 请求的模型字段不存在
class FieldDoesNotExist(Exception):
    """The requested model field does not exist"""

    pass


# django.apps 应用注册表尚未初始化完成
class AppRegistryNotReady(Exception):
    """The django.apps registry is not populated yet"""

    pass


# 查询对象不存在（模板变量静默失败）
class ObjectDoesNotExist(Exception):
    """The requested object does not exist"""

    silent_variable_failure = True


# 更新时目标对象已不存在
class ObjectNotUpdated(Exception):
    """The updated object no longer exists."""


# 期望唯一结果但返回多条记录
class MultipleObjectsReturned(Exception):
    """The query returned multiple objects when only one was expected."""

    pass


# 用户请求行为可疑的基类异常
class SuspiciousOperation(Exception):
    """The user did something suspicious"""


# multipart 表单 MIME 类型可疑
class SuspiciousMultipartForm(SuspiciousOperation):
    """Suspect MIME request in multipart form data"""

    pass


# 可疑的文件系统操作（如路径遍历）
class SuspiciousFileOperation(SuspiciousOperation):
    """A Suspicious filesystem operation was attempted"""

    pass


# HTTP_HOST 头值不在 ALLOWED_HOSTS 中
class DisallowedHost(SuspiciousOperation):
    """HTTP_HOST header contains invalid value"""

    pass


# 重定向 URL 过长或 scheme 不在白名单
class DisallowedRedirect(SuspiciousOperation):
    """Redirect was too long or scheme was not in allowed list."""

    pass


# GET/POST 字段数超过 DATA_UPLOAD_MAX_NUMBER_FIELDS
class TooManyFieldsSent(SuspiciousOperation):
    """
    The number of fields in a GET or POST request exceeded
    settings.DATA_UPLOAD_MAX_NUMBER_FIELDS.
    """

    pass


# 上传文件数超过 DATA_UPLOAD_MAX_NUMBER_FILES
class TooManyFilesSent(SuspiciousOperation):
    """
    The number of fields in a GET or POST request exceeded
    settings.DATA_UPLOAD_MAX_NUMBER_FILES.
    """

    pass


# 请求体大小超过 DATA_UPLOAD_MAX_MEMORY_SIZE
class RequestDataTooBig(SuspiciousOperation):
    """
    The size of the request (excluding any file uploads) exceeded
    settings.DATA_UPLOAD_MAX_MEMORY_SIZE.
    """

    pass


# 请求在完成前被关闭或超时
class RequestAborted(Exception):
    """The request was closed before it was completed, or timed out."""

    pass


# 请求格式错误无法处理
class BadRequest(Exception):
    """The request is malformed and cannot be processed."""

    pass


# 用户无权限执行该操作
class PermissionDenied(Exception):
    """The user did not have permission to do that"""

    pass


# 请求的视图不存在或无法导入
class ViewDoesNotExist(Exception):
    """The requested view does not exist"""

    pass


# 中间件在当前服务器配置中未启用
class MiddlewareNotUsed(Exception):
    """This middleware is not used in this server configuration"""

    pass


# Django 配置不正确
class ImproperlyConfigured(Exception):
    """Django is somehow improperly configured"""

    pass


# 模型字段定义或使用出错
class FieldError(Exception):
    """Some kind of problem with a model field."""

    pass


# 按需获取模型字段被阻止
class FieldFetchBlocked(FieldError):
    """On-demand fetching of a model field blocked."""

    pass


NON_FIELD_ERRORS = "__all__"


# 数据验证失败：支持单条、列表或字段字典形式
class ValidationError(Exception):
    """An error while validating data."""

    # 规范化 message 为 error_list 或 error_dict 结构
    def __init__(self, message, code=None, params=None):
        """
        The `message` argument can be a single error, a list of errors, or a
        dictionary that maps field names to lists of errors. What we define as
        an "error" can be either a simple string or an instance of
        ValidationError with its message attribute set, and what we define as
        list or dictionary can be an actual `list` or `dict` or an instance
        of ValidationError with its `error_list` or `error_dict` attribute set.
        """
        super().__init__(message, code, params)

        if isinstance(message, ValidationError):
            if hasattr(message, "error_dict"):
                message = message.error_dict
            elif not hasattr(message, "message"):
                message = message.error_list
            else:
                message, code, params = message.message, message.code, message.params

        if isinstance(message, dict):
            self.error_dict = {}
            for field, messages in message.items():
                if not isinstance(messages, ValidationError):
                    messages = ValidationError(messages)
                self.error_dict[field] = messages.error_list

        elif isinstance(message, list):
            self.error_list = []
            for message in message:
                # Normalize plain strings to instances of ValidationError.
                if not isinstance(message, ValidationError):
                    message = ValidationError(message)
                if hasattr(message, "error_dict"):
                    self.error_list.extend(sum(message.error_dict.values(), []))
                else:
                    self.error_list.extend(message.error_list)

        else:
            self.message = message
            self.code = code
            self.params = params
            self.error_list = [self]

    @property
    def message_dict(self):
        # Trigger an AttributeError if this ValidationError
        # doesn't have an error_dict.
        getattr(self, "error_dict")

        return dict(self)

    @property
    def messages(self):
        if hasattr(self, "error_dict"):
            return sum(dict(self).values(), [])
        return list(self)

    def update_error_dict(self, error_dict):
        if hasattr(self, "error_dict"):
            for field, error_list in self.error_dict.items():
                error_dict.setdefault(field, []).extend(error_list)
        else:
            error_dict.setdefault(NON_FIELD_ERRORS, []).extend(self.error_list)
        return error_dict

    def __iter__(self):
        if hasattr(self, "error_dict"):
            for field, errors in self.error_dict.items():
                yield field, list(ValidationError(errors))
        else:
            for error in self.error_list:
                message = error.message
                if error.params:
                    message %= error.params
                yield str(message)

    def __str__(self):
        if hasattr(self, "error_dict"):
            return repr(dict(self))
        return repr(list(self))

    def __repr__(self):
        return "ValidationError(%s)" % self

    def __eq__(self, other):
        if not isinstance(other, ValidationError):
            return NotImplemented
        return hash(self) == hash(other)

    def __hash__(self):
        if hasattr(self, "message"):
            return hash(
                (
                    self.message,
                    self.code,
                    make_hashable(self.params),
                )
            )
        if hasattr(self, "error_dict"):
            return hash(make_hashable(self.error_dict))
        return hash(tuple(sorted(self.error_list, key=operator.attrgetter("message"))))


# 数据库查询条件不可能匹配任何行
class EmptyResultSet(Exception):
    """A database query predicate is impossible."""

    pass


# 数据库查询条件匹配全部行
class FullResultSet(Exception):
    """A database query predicate that matches everything."""

    pass


# 在异步上下文中调用了仅同步函数
class SynchronousOnlyOperation(Exception):
    """The user tried to call a sync-only function from an async context."""

    pass
