"""教程 008（Annotated）：嵌套 yield 依赖链，用 Annotated 声明类型与 Depends。"""

from typing import Annotated

from fastapi import Depends


async def dependency_a():
    """与 tutorial008 相同的最外层 yield 依赖。"""
    dep_a = generate_dep_a()
    try:
        yield dep_a  # 产出 DepA 实例
    finally:
        dep_a.close()  # 请求结束后清理


async def dependency_b(dep_a: Annotated[DepA, Depends(dependency_a)]):
    """Annotated 同时标注 DepA 类型与 Depends(dependency_a) 元数据。"""
    dep_b = generate_dep_b()
    try:
        yield dep_b
    finally:
        dep_b.close(dep_a)  # finally 中引用上游 dep_a


async def dependency_c(dep_b: Annotated[DepB, Depends(dependency_b)]):
    """链末端依赖；Annotated 写法便于 IDE 与类型检查。"""
    dep_c = generate_dep_c()
    try:
        yield dep_c
    finally:
        dep_c.close(dep_b)  # 嵌套依赖的清理顺序与 Depends 版一致
