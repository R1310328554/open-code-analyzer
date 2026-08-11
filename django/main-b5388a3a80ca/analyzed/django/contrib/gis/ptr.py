# C 指针访问基类 — 防止 NULL 指针传入 GEOS/GDAL 例程
from ctypes import c_void_pfrom ctypes import c_void_p


# 管理底层 C 指针生命周期与类型校验
class CPointerBase:
    """
    Base class for objects that have a pointer access property
    that controls access to the underlying C pointer.
    """

    _ptr = None  # Initially the pointer is NULL.
    ptr_type = c_void_p
    destructor = None
    null_ptr_exception_class = AttributeError

    @property
    # 返回有效指针，NULL 时抛出 null_ptr_exception_class
    def ptr(self):
        # Raise an exception if the pointer isn't valid so that NULL pointers
        # aren't passed to routines -- that's very bad.
        if self._ptr:
            return self._ptr
        raise self.null_ptr_exception_class(
            "NULL %s pointer encountered." % self.__class__.__name__
        )

    @ptr.setter
    def ptr(self, ptr):
        # Only allow the pointer to be set with pointers of the compatible
        # type or None (NULL).
        if not (ptr is None or isinstance(ptr, self.ptr_type)):
            raise TypeError("Incompatible pointer type: %s." % type(ptr))
        self._ptr = ptr

    # 析构时调用 destructor 释放 C 对象内存
    def __del__(self):
        """
        Free the memory used by the C++ object.
        """
        if self.destructor and self._ptr:
            try:
                self.destructor(self.ptr)
            except (AttributeError, ImportError, TypeError):
                pass  # Some part might already have been garbage collected
