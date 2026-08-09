"""教程 002（Annotated）：无默认值的 q 在前会被视为查询参数；Path 显式标记 item_id。"""

from typing import Annotated

from fastapi import FastAPI, Path

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_items(
    q: str, item_id: Annotated[int, Path(title="The ID of the item to get")]
):
    """参数顺序敏感：未用 Path/Query 标注的简单类型 q 解析为查询参数。"""
    results = {"item_id": item_id}
    if q:
        results.update({"q": q})
    return results
