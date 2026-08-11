"""
django.utils.inspect — 可调用对象签名与参数内省辅助。

兼容 annotationlib 的 getfullargspec/signature 封装。
"""

import functoolsimport functools
import inspect
import threading
from contextlib import contextmanager

from django.utils.version import PY314, PY315

if PY314:
    import annotationlib

    if not PY315:
        lock = threading.Lock()
        safe_signature_from_callable = functools.partial(
            inspect._signature_from_callable,
            annotation_format=annotationlib.Format.FORWARDREF,
        )


@functools.lru_cache(maxsize=512)
def _get_func_parameters(func, remove_first):
    parameters = tuple(signature(func).parameters.values())
    if remove_first:
        parameters = parameters[1:]
    return parameters


def _get_callable_parameters(meth_or_func):
    is_method = inspect.ismethod(meth_or_func)
    func = meth_or_func.__func__ if is_method else meth_or_func
    return _get_func_parameters(func, remove_first=is_method)


ARG_KINDS = frozenset(
    {
        inspect.Parameter.POSITIONAL_ONLY,
        inspect.Parameter.KEYWORD_ONLY,
        inspect.Parameter.POSITIONAL_OR_KEYWORD,
    }
)


# 返回 positional/keyword 参数名列表
def get_func_args(func):
    params = _get_callable_parameters(func)
    return [param.name for param in params if param.kind in ARG_KINDS]


# 返回 (name, default) 元组列表
def get_func_full_args(func):
    """
    Return a list of (argument name, default value) tuples. If the argument
    does not have a default value, omit it in the tuple. Arguments such as
    *args and **kwargs are also included.
    """
    params = _get_callable_parameters(func)
    args = []
    for param in params:
        name = param.name
        # Ignore 'self'
        if name == "self":
            continue
        if param.kind == inspect.Parameter.VAR_POSITIONAL:
            name = "*" + name
        elif param.kind == inspect.Parameter.VAR_KEYWORD:
            name = "**" + name
        if param.default != inspect.Parameter.empty:
            args.append((name, param.default))
        else:
            args.append((name,))
    return args


# 是否接受 **kwargs
def func_accepts_kwargs(func):
    """Return True if function 'func' accepts keyword arguments **kwargs."""
    return any(p for p in _get_callable_parameters(func) if p.kind == p.VAR_KEYWORD)


# 是否接受 *args
def func_accepts_var_args(func):
    """
    Return True if function 'func' accepts positional arguments *args.
    """
    return any(p for p in _get_callable_parameters(func) if p.kind == p.VAR_POSITIONAL)


# 绑定方法除 self 外是否无参数
def method_has_no_args(meth):
    """Return True if a method only accepts 'self'."""
    count = len([p for p in _get_callable_parameters(meth) if p.kind in ARG_KINDS])
    return count == 0 if inspect.ismethod(meth) else count == 1


# 是否声明了指定名称的参数
def func_supports_parameter(func, name):
    return any(param.name == name for param in _get_callable_parameters(func))


# 是否为模块级函数（非 nested/lambda）
def is_module_level_function(func):
    if not inspect.isfunction(func) or inspect.isbuiltin(func):
        return False

    if "<locals>" in func.__qualname__:
        return False

    return True


@contextmanager
# 上下文管理器：延迟解析 forward ref 注解
def lazy_annotations():
    """
    inspect.getfullargspec eagerly evaluates type annotations. To add
    compatibility with Python 3.14+ deferred evaluation, patch the module-level
    helper to provide the annotation_format that we are using elsewhere.

    This private helper should only be used for Python 3.14, as
    https://github.com/python/cpython/issues/141560 was fixed in 3.15.

    This context manager is not reentrant.
    """
    if PY315 or not PY314:
        yield
        return
    with lock:
        original_helper = inspect._signature_from_callable
        inspect._signature_from_callable = safe_signature_from_callable
        try:
            yield
        finally:
            inspect._signature_from_callable = original_helper


# inspect.getfullargspec 的 annotation 格式包装
def getfullargspec(*args, annotation_format=None, **kwargs):
    """
    A wrapper around inspect.getfullargspec that leaves deferred annotations
    unevaluated on Python 3.14+, since they are not used in our case.
    """
    if PY315:
        return inspect.getfullargspec(
            *args, **kwargs, annotation_format=annotationlib.Format.FORWARDREF
        )
    if PY314:
        with lazy_annotations():
            return inspect.getfullargspec(*args, **kwargs)
    else:
        return inspect.getfullargspec(*args, **kwargs)


# inspect.signature 的线程安全/格式包装
def signature(obj):
    """
    A wrapper around inspect.signature that leaves deferred annotations
    unevaluated on Python 3.14+, since they are not used in our case.
    """
    if PY314:
        return inspect.signature(obj, annotation_format=annotationlib.Format.FORWARDREF)
    else:
        return inspect.signature(obj)
