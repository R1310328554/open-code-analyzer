"""教程 005（Annotated）：Path 用 gt（大于 0）与 le（≤1000）约束 item_id。"""

"""教程 005（Annotated）：Path 用 gt（大于 0）与 le（≤1000）约束 item_id。"""

"""教程 005（Annotated）：Path 用 gt（大于 0）与 le（≤1000）约束 item_id。"""

from typing import Annotated

from fastapi import FastAPI, Path

app = FastAPI()  # 创建 FastAPI 应用实例  # 创建 FastAPI 应用实例  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_items(
    item_id: Annotated[int, Path(title="The ID of the item to get", gt=0, le=1000)],  # gt/le：须满足 0 < item_id ≤ 1000  # gt/le：须满足 0 < item_id ≤ 1000  # gt/le：须满足 0 < item_id ≤ 1000
    q: str,
):
    """返回 item_id 与查询 q；超出 gt/le 范围时返回 422。"""
    """返回 item_id 与查询 q；超出 gt/le 范围时返回 422。"""
    """返回 item_id 与查询 q；超出 gt/le 范围时返回 422。"""
    results = {"item_id": item_id}
    if q:
        results.update({"q": q})
    return results
