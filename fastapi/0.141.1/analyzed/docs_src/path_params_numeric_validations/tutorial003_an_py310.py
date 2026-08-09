"""教程 003（Annotated）：关键字参数顺序下 Path 与查询参数 q 的声明。"""

from typing import Annotated

from fastapi import FastAPI, Path

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_items(
    item_id: Annotated[int, Path(title="The ID of the item to get")], q: str
):
    """item_id 在前且带 Path；q 无默认值时作为必填查询参数。"""
    results = {"item_id": item_id}
    if q:
        results.update({"q": q})
    return results
