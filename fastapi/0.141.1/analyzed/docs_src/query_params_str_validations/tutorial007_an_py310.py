"""教程 007（Annotated）：Annotated + Query(title=..., min_length=3) 声明文档标题与长度下限。"""

from typing import Annotated

from fastapi import FastAPI, Query

app = FastAPI()  # 创建 FastAPI 应用实例


@app.get("/items/")
async def read_items(
    q: Annotated[str | None, Query(title="Query string", min_length=3)] = None,
):
    """Annotated 将校验与 OpenAPI 元数据绑定到类型；行为与 tutorial007 等价。"""
    results = {"items": [{"item_id": "Foo"}, {"item_id": "Bar"}]}
    if q:
        results.update({"q": q})
    return results
