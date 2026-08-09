"""教程 006（Annotated）：Path 约束 item_id；float 查询参数 size 用 Query 的 gt/lt 校验。"""

"""教程 006（Annotated）：Path 约束 item_id；float 查询参数 size 用 Query 的 gt/lt 校验。"""

from typing import Annotated

from fastapi import FastAPI, Path, Query

app = FastAPI()  # 创建 FastAPI 应用实例  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_items(
    *,
    item_id: Annotated[int, Path(title="The ID of the item to get", ge=0, le=1000)],  # ge/le：0 ≤ item_id ≤ 1000  # ge/le：0 ≤ item_id ≤ 1000
    q: str,
    size: Annotated[float, Query(gt=0, lt=10.5)],  # 浮点 gt/lt：须大于 0 且小于 10.5（0.5 有效，0 无效）  # 浮点 gt/lt：须大于 0 且小于 10.5（0.5 有效，0 无效）
):
    """组合 path、query 与 float 数值校验示例。"""
    results = {"item_id": item_id}
    if q:
        results.update({"q": q})
    if size:
        results.update({"size": size})
    return results
