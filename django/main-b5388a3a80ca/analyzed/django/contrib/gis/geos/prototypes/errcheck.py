"""
GEOS ctypes 函数的错误检查回调 — 统一处理返回值与内存释放。

Error checking functions for GEOS ctypes prototype functions."""
Error checking functions for GEOS ctypes prototype functions.
"""

from ctypes import c_void_p, string_at

from django.contrib.gis.geos.error import GEOSException
from django.contrib.gis.geos.libgeos import GEOSFuncFactory

# Getting the `free` routine used to free the memory allocated for
# string pointers returned by GEOS.
free = GEOSFuncFactory("GEOSFree")
free.argtypes = [c_void_p]


# 读取最后一个 C 引用参数的值
def last_arg_byref(args):
    "Return the last C argument's value by reference."
    return args[-1]._obj.value


# 校验状态码并返回 double 引用参数值
def check_dbl(result, func, cargs):
    """
    Check the status code and returns the double value passed in by reference.
    """
    # Checking the status code
    if result != 1:
        return None
    # Double passed in by reference, return its value.
    return last_arg_byref(cargs)


# 校验 GEOS 几何指针返回值
def check_geom(result, func, cargs):
    "Error checking on routines that return Geometries."
    if not result:
        raise GEOSException(
            'Error encountered checking Geometry returned from GEOS C function "%s".'
            % func.__name__
        )
    return result


# 返回值不应为 -1 时的错误检查
def check_minus_one(result, func, cargs):
    "Error checking on routines that should not return -1."
    if result == -1:
        raise GEOSException(
            'Error encountered in GEOS C function "%s".' % func.__name__
        )
    else:
        return result


# 一元/二元谓词：1→True，0→False，其他抛异常
def check_predicate(result, func, cargs):
    "Error checking for unary/binary predicate functions."
    if result == 1:
        return True
    elif result == 0:
        return False
    else:
        raise GEOSException(
            'Error encountered on GEOS C predicate function "%s".' % func.__name__
        )


# 读取带长度参数的字符串并释放 GEOS 分配的内存
def check_sized_string(result, func, cargs):
    """
    Error checking for routines that return explicitly sized strings.

    This frees the memory allocated by GEOS at the result pointer.
    """
    if not result:
        raise GEOSException(
            'Invalid string pointer returned by GEOS C function "%s"' % func.__name__
        )
    # A c_size_t object is passed in by reference for the second
    # argument on these routines, and its needed to determine the
    # correct size.
    s = string_at(result, last_arg_byref(cargs))
    # Freeing the memory allocated within GEOS
    free(result)
    return s


# 读取 C 字符串指针并释放 GEOS 分配的内存
def check_string(result, func, cargs):
    """
    Error checking for routines that return strings.

    This frees the memory allocated by GEOS at the result pointer.
    """
    if not result:
        raise GEOSException(
            'Error encountered checking string return value in GEOS C function "%s".'
            % func.__name__
        )
    # Getting the string value at the pointer address.
    s = string_at(result)
    # Freeing the memory allocated within GEOS
    free(result)
    return s
