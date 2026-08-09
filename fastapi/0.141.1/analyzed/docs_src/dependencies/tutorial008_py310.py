"""教程 008：yield 依赖嵌套——A→B→C 链式注入，子依赖自动获得父依赖 yield 的值。"""

from fastapi import Depends


async def dependency_a():
    """最外层 yield 依赖：创建 dep_a，响应后 finally 调用 close。"""
    dep_a = generate_dep_a()  # 模拟获取需清理的资源
    try:
        yield dep_a  # 将 dep_a 提供给下游 Depends
    finally:
        dep_a.close()  # yield 之后执行清理


async def dependency_b(dep_a=Depends(dependency_a)):
    """Depends(dependency_a) 先运行 A，再把 yield 出的 dep_a 注入本函数。"""
    dep_b = generate_dep_b()  # 基于 dep_a 创建下一层资源
    try:
        yield dep_b
    finally:
        dep_b.close(dep_a)  # 清理时可使用上游依赖产物


async def dependency_c(dep_b=Depends(dependency_b)):
    """整条链：C 依赖 B，B 依赖 A；FastAPI 按拓扑顺序解析。"""
    dep_c = generate_dep_c()  # 最内层资源
    try:
        yield dep_c
    finally:
        dep_c.close(dep_b)  # 由内向外依次执行 finally
