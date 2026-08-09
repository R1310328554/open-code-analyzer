"""教程 001：Path(title=...) 为路径参数添加 OpenAPI 元数据；Query(alias=...) 指定查询参数名。"""

from fastapi import FastAPI, Path, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/{item_id}")
async def read_items(
    item_id: int = Path(title="The ID of the item to get"),
    q: str | None = Query(default=None, alias="item-query"),
):
    """与 Annotated 版等价：item_id 来自路径，q 来自 ?item-query= 查询串。"""
    results = {"item_id": item_id}
    if q:
        results.update({"q": q})
    return results
