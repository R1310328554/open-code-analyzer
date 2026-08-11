"""
GDAL ctypes 原型的错误检查回调：字符串释放、几何与 SRS 指针校验。

This module houses the error-checking routines used by the GDAL"""
This module houses the error-checking routines used by the GDAL
ctypes prototypes.
"""

from ctypes import c_void_p, string_at

from django.contrib.gis.gdal.error import GDALException, SRSException, check_err
from django.contrib.gis.gdal.libgdal import lgdal


# 从 byref 参数中提取指针或值的辅助函数
# Helper routines for retrieving pointers and/or values from
# arguments passed in by reference.
# 返回引用参数中的整型错误码或数值
def arg_byref(args, offset=-1):
    "Return the pointer argument's by-reference value."
    return args[offset]._obj.value


# 返回引用参数中的 ctypes 指针对象
def ptr_byref(args, offset=-1):
    "Return the pointer argument passed in by-reference."
    return args[offset]._obj


# 字符串输出检查：常量字符串与需 VSIFree 释放的堆字符串
# ### String checking Routines ###
# 检查常量字符串返回值，不释放指针
def check_const_string(result, func, cargs, offset=None, cpl=False):
    """
    Similar functionality to `check_string`, but does not free the pointer.
    """
    if offset:
        check_err(result, cpl=cpl)
        ptr = ptr_byref(cargs, offset)
        return ptr.value
    else:
        return result


# 检查 OGR 分配的字符串并在读取后调用 VSIFree 释放
def check_string(result, func, cargs, offset=-1, str_result=False):
    """
    Check the string output returned from the given function, and free
    the string pointer allocated by OGR. The `str_result` keyword
    may be used when the result is the string pointer, otherwise
    the OGR error code is assumed. The `offset` keyword may be used
    to extract the string pointer passed in by-reference at the given
    slice offset in the function arguments.
    """
    if str_result:
        # For routines that return a string.
        ptr = result
        if not ptr:
            s = None
        else:
            s = string_at(result)
    else:
        # Error-code return specified.
        check_err(result)
        ptr = ptr_byref(cargs, offset)
        # Getting the string value
        s = ptr.value
    # Correctly freeing the allocated memory behind GDAL pointer
    # with the VSIFree routine.
    if ptr:
        lgdal.VSIFree(ptr)
    return s


# ### DataSource, Layer error-checking ###


# 包络矩形：从 byref 参数取 OGREnvelope
# ### Envelope checking ###
# 返回函数写入的 OGR Envelope 引用
def check_envelope(result, func, cargs, offset=-1):
    "Check a function that returns an OGR Envelope by reference."
    return ptr_byref(cargs, offset)


# 几何指针校验：直接返回或从 byref 偏移读取
# ### Geometry error-checking routines ###
# 校验几何指针非空，空则抛出 GDALException
def check_geom(result, func, cargs):
    "Check a function that returns a geometry."
    # OGR_G_Clone may return an integer, even though the
    # restype is set to c_void_p
    if isinstance(result, int):
        result = c_void_p(result)
    if not result:
        raise GDALException(
            'Invalid geometry pointer returned from "%s".' % func.__name__
        )
    return result


# 先检查错误码，再从指定参数位置取几何指针
def check_geom_offset(result, func, cargs, offset=-1):
    "Check the geometry at the given offset in the C parameter list."
    check_err(result)
    geom = ptr_byref(cargs, offset=offset)
    return check_geom(geom, func, cargs)


# 空间参考系统指针校验
# ### Spatial Reference error-checking routines ###
# 校验 SRS 指针有效，无效则抛出 SRSException
def check_srs(result, func, cargs):
    if isinstance(result, int):
        result = c_void_p(result)
    if not result:
        raise SRSException(
            'Invalid spatial reference pointer returned from "%s".' % func.__name__
        )
    return result


# 通用错误码、指针与 OSR 单位字符串检查
# ### Other error-checking routines ###
# 错误码在最后一个 byref 参数中，校验后返回主结果
def check_arg_errcode(result, func, cargs, cpl=False):
    """
    The error code is returned in the last argument, by reference.
    Check its value with `check_err` before returning the result.
    """
    check_err(arg_byref(cargs), cpl=cpl)
    return result


# 直接对 c_int 返回值调用 check_err
def check_errcode(result, func, cargs, cpl=False):
    """
    Check the error code returned (c_int).
    """
    check_err(result, cpl=cpl)


# 确保 void 指针非空
def check_pointer(result, func, cargs):
    "Make sure the result pointer is valid."
    if isinstance(result, int):
        result = c_void_p(result)
    if result:
        return result
    else:
        raise GDALException('Invalid pointer returned from "%s"' % func.__name__)


# OSRGetAngular/LinearUnits：返回 double 与不可释放的字符串
def check_str_arg(result, func, cargs):
    """
    This is for the OSRGet[Angular|Linear]Units functions, which
    require that the returned string pointer not be freed. This
    returns both the double and string values.
    """
    dbl = result
    ptr = cargs[-1]._obj
    return dbl, ptr.value.decode()
