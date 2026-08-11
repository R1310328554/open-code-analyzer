# PSE 扩展构建脚本：Cython 编译 pse.pyx 为 C++ 加速模块
from setuptools import setup, Extension
from Cython.Build import cythonize
import numpy

setup(
    ext_modules=cythonize(
        Extension(
            "pse",
            sources=["pse.pyx"],
            language="c++",
            include_dirs=[numpy.get_include()],
            library_dirs=[],
            libraries=[],
            extra_compile_args=["-O3"],
            extra_link_args=[],
        )
    )
)
