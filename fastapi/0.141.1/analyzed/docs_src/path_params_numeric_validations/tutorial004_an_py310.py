"""教程 004（Annotated）：Path(ge=1) 要求 item_id ≥ 1，否则返回 422 校验错误。"""

from typing import Annotated

from fastapi import FastAPI, Path

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_items(
    item_id: Annotated[int, Path(title="The ID of the item to get", ge=1)], q: str
):
    """ge=1 为数值下界；0 或负数路径会被 FastAPI 自动拒绝。"""
    results = {"item_id": item_id}
    if q:
        results.update({"q": q})
    return results
