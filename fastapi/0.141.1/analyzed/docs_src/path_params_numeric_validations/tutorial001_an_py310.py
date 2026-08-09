"""教程 001（Annotated）：Annotated[int, Path(...)] 声明路径参数；Query(alias=...) 指定查询参数名。"""

from typing import Annotated

from fastapi import FastAPI, Path, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_items(
    item_id: Annotated[int, Path(title="The ID of the item to get")],
    q: Annotated[str | None, Query(alias="item-query")] = None,
):
    """Path title 写入 OpenAPI；查询参数在 URL 中以 item-query 出现。"""
    results = {"item_id": item_id}
    if q:
        results.update({"q": q})
    return results
