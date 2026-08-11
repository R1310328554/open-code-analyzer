# GEOS 线程安全 C 函数包装 — 每线程独立上下文句柄
import threadingimport threading

from django.contrib.gis.geos.base import GEOSBase
from django.contrib.gis.geos.libgeos import CONTEXT_PTR, error_h, lgeos, notice_h


# GEOS 线程上下文句柄：init/finish 与错误/通知回调
class GEOSContextHandle(GEOSBase):
    """Represent a GEOS context handle."""

    ptr_type = CONTEXT_PTR
    destructor = lgeos.GEOS_finish_r

    # 初始化 GEOS 上下文并注册 notice/error 处理器
    def __init__(self):
        self.ptr = lgeos.GEOS_init_r()
        lgeos.GEOSContext_setNoticeHandler_r(self.ptr, notice_h)
        lgeos.GEOSContext_setErrorHandler_r(self.ptr, error_h)


# Defining a thread-local object and creating an instance
# to hold a reference to GEOSContextHandle for this thread.
# 线程本地 GEOS 上下文句柄存储
class GEOSContext(threading.local):
    handle = None


thread_context = GEOSContext()


# GEOS C 函数包装器，自动注入线程上下文指针
class GEOSFunc:
    """
    Serve as a wrapper for GEOS C Functions. Use thread-safe function
    variants when available.
    """

    # 绑定 *_r 后缀的线程安全 C 函数
    def __init__(self, func_name):
        # GEOS thread-safe function signatures end with '_r' and take an
        # additional context handle parameter.
        self.cfunc = getattr(lgeos, func_name + "_r")
        # Create a reference to thread_context so it's not garbage-collected
        # before an attempt to call this object.
        self.thread_context = thread_context

    # 懒创建上下文句柄并以首参传入 C 函数
    def __call__(self, *args):
        # Create a context handle if one doesn't exist for this thread.
        self.thread_context.handle = self.thread_context.handle or GEOSContextHandle()
        # Call the threaded GEOS routine with the pointer of the context handle
        # as the first argument.
        return self.cfunc(self.thread_context.handle.ptr, *args)

    def __str__(self):
        return self.cfunc.__name__

    # argtypes property
    def _get_argtypes(self):
        return self.cfunc.argtypes

    def _set_argtypes(self, argtypes):
        self.cfunc.argtypes = [CONTEXT_PTR, *argtypes]

    argtypes = property(_get_argtypes, _set_argtypes)

    # restype property
    def _get_restype(self):
        return self.cfunc.restype

    def _set_restype(self, restype):
        self.cfunc.restype = restype

    restype = property(_get_restype, _set_restype)

    # errcheck property
    def _get_errcheck(self):
        return self.cfunc.errcheck

    def _set_errcheck(self, errcheck):
        self.cfunc.errcheck = errcheck

    errcheck = property(_get_errcheck, _set_errcheck)
